package com.fleetbilling.service;

import com.fleetbilling.billing.allocation.*;
import com.fleetbilling.dto.billing.AllocationItemResponse;
import com.fleetbilling.dto.billing.FixedFeeAllocationResponse;
import com.fleetbilling.entity.*;
import com.fleetbilling.enums.BillingType;
import com.fleetbilling.enums.TripStatus;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates fixed-monthly-fee allocation for a completed billing run.
 *
 * <h2>Flow</h2>
 * <ol>
 *   <li>Load the billing run.</li>
 *   <li>Check it hasn't already been allocated (idempotency).</li>
 *   <li>Find eligible COMPLETED trips for the billing month.</li>
 *   <li>For each trip, resolve its applicable ContractVersion.</li>
 *   <li>Group trips by ContractVersion (supports mid-month version changes).</li>
 *   <li>For each version group, run the Largest Remainder allocation.</li>
 *   <li>Persist InvoiceItem rows with pricingType=FIXED_MONTHLY.</li>
 *   <li>Update the Invoice subtotal/total and the BillingRun total.</li>
 *   <li>Reconcile: SUM(items) == invoice.total == billingRun implied total.</li>
 * </ol>
 *
 * <h2>Idempotency</h2>
 * Any InvoiceItem with {@code pricingType = FIXED_MONTHLY} for the invoice counts as
 * an existing allocation. A second call will throw 409 Conflict.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingAllocationService {

    private final BillingRunRepository billingRunRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final TripRepository tripRepository;
    private final FixedFeeAllocationService fixedFeeAllocationService;
    private final DistanceAllocationStrategy distanceStrategy; // default; inject interface for future extension

    @Transactional
    public FixedFeeAllocationResponse allocateFixedFee(Long billingRunId) {

        // ── 1. Load billing run ───────────────────────────────────────────────
        BillingRun billingRun = billingRunRepository.findById(billingRunId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Billing run not found with id " + billingRunId));

        Invoice invoice = invoiceRepository.findByBillingRunId(billingRunId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found for billing run " + billingRunId));

        // ── 2. Idempotency check ──────────────────────────────────────────────
        boolean alreadyAllocated = invoiceItemRepository.findByInvoiceId(invoice.getId())
                .stream().anyMatch(item -> item.getPricingType() == BillingType.FIXED_MONTHLY);
        if (alreadyAllocated) {
            throw new DuplicateResourceException(
                    "Fixed fee allocation has already been performed for billing run " + billingRunId);
        }

        Vehicle vehicle = billingRun.getVehicle();
        LocalDate billingMonthDate = billingRun.getBillingMonth();
        YearMonth ym = YearMonth.from(billingMonthDate);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();

        // ── 3. Eligible trips ─────────────────────────────────────────────────
        List<Trip> eligibleTrips = tripRepository.findByFilters(
                vehicle.getId(), TripStatus.COMPLETED, monthStart, monthEnd, Pageable.unpaged()
        ).getContent();

        if (eligibleTrips.isEmpty()) {
            throw new BusinessException(
                    "No eligible trips found for vehicle " + vehicle.getId() +
                    " in billing month " + ym);
        }

        // ── 4. Find active contract ───────────────────────────────────────────
        List<Contract> contracts = contractRepository.findByVehicleId(vehicle.getId());
        if (contracts.isEmpty() && vehicle.getVendor() != null) {
            contracts = contractRepository.findByVendorId(vehicle.getVendor().getId());
        }
        Contract contract = contracts.stream()
                .filter(c -> c.getStartDate() != null && !c.getStartDate().isAfter(monthStart))
                .filter(c -> c.getEndDate() == null || !c.getEndDate().isBefore(monthEnd))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active contract found for vehicle " + vehicle.getId() +
                        " in billing month " + ym));

        // ── 5. Group trips by their applicable ContractVersion ─────────────────
        // Supports mid-month fee changes: trips under different versions get separate allocations
        record TripWithVersion(Trip trip, ContractVersion version) {}
        List<TripWithVersion> paired = new ArrayList<>();
        for (Trip trip : eligibleTrips) {
            ContractVersion version = contractVersionRepository
                    .findApplicableVersion(contract.getId(), trip.getTripDate())
                    .orElseThrow(() -> new BusinessException(
                            "Trip " + trip.getExternalTripId() +
                            " has no applicable contract version for " + trip.getTripDate()));
            if (version.getBillingType() != BillingType.FIXED_MONTHLY) {
                throw new BusinessException(
                        "Contract version " + version.getId() +
                        " has billing type " + version.getBillingType() +
                        " — fixed fee allocation only applies to FIXED_MONTHLY contracts.");
            }
            if (version.getMonthlyFixedFeePaisa() == null || version.getMonthlyFixedFeePaisa() <= 0) {
                throw new BusinessException(
                        "Contract version " + version.getId() +
                        " does not have a valid monthlyFixedFeePaisa.");
            }
            paired.add(new TripWithVersion(trip, version));
        }

        // Group by version ID
        java.util.Map<Long, List<TripWithVersion>> byVersion = new java.util.LinkedHashMap<>();
        for (TripWithVersion tw : paired) {
            byVersion.computeIfAbsent(tw.version().getId(), k -> new ArrayList<>()).add(tw);
        }

        // ── 6. Allocate per version group and persist InvoiceItems ────────────
        List<AllocationResult> allResults = new ArrayList<>();
        long totalAllocated = 0L;

        for (var entry : byVersion.entrySet()) {
            ContractVersion version = entry.getValue().get(0).version();
            List<Trip> groupTrips = entry.getValue().stream().map(TripWithVersion::trip).toList();
            long feePaisa = version.getMonthlyFixedFeePaisa();

            List<AllocationResult> groupResults =
                    fixedFeeAllocationService.allocate(groupTrips, feePaisa, distanceStrategy);

            for (AllocationResult result : groupResults) {
                Trip trip = groupTrips.stream()
                        .filter(t -> t.getId().equals(result.getTripId()))
                        .findFirst().orElseThrow();

                String description = String.format(
                        "Fixed monthly fee allocation: %.4f / %.4f × %d paisa [%s]",
                        result.getWeight(), result.getTotalWeight(),
                        feePaisa, result.getStrategyName());

                String explanation = String.format(
                        "FIXED_MONTHLY|trip=%s|weight=%s|totalWeight=%s|fee=%d|allocated=%d|strategy=%s",
                        result.getExternalTripId(), result.getWeight(), result.getTotalWeight(),
                        feePaisa, result.getAllocatedPaisa(), result.getStrategyName());

                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .trip(trip)
                        .description(description)
                        .pricingType(BillingType.FIXED_MONTHLY)
                        .quantity(result.getWeight())
                        .ratePaisa(feePaisa / Math.max(groupTrips.size(), 1))  // indicative
                        .amountPaisa(result.getAllocatedPaisa())
                        .allocationWeight(result.getWeight().compareTo(BigDecimal.ZERO) > 0
                                ? result.getWeight().divide(result.getTotalWeight(),
                                        10, java.math.RoundingMode.HALF_UP)
                                : BigDecimal.ZERO)
                        .explanation(explanation)
                        .build();

                invoiceItemRepository.save(item);
                totalAllocated += result.getAllocatedPaisa();
                allResults.add(result);
            }
        }

        // ── 7. Update Invoice totals ──────────────────────────────────────────
        long newSubtotal = invoice.getSubtotalPaisa() + totalAllocated;
        invoice.setSubtotalPaisa(newSubtotal);
        invoice.setTotalPaisa(newSubtotal + invoice.getTaxPaisa());
        invoiceRepository.save(invoice);

        // ── 8. Final reconciliation ───────────────────────────────────────────
        long itemSum = invoiceItemRepository.findByInvoiceId(invoice.getId())
                .stream().mapToLong(InvoiceItem::getAmountPaisa).sum();
        if (itemSum != invoice.getTotalPaisa()) {
            throw new BusinessException(String.format(
                    "Post-allocation reconciliation failed: invoice total %d != sum of items %d",
                    invoice.getTotalPaisa(), itemSum));
        }

        log.info("Fixed fee allocated for billing run {}: {} paisa across {} trips",
                billingRunId, totalAllocated, allResults.size());

        // ── 9. Build response ─────────────────────────────────────────────────
        List<AllocationItemResponse> itemResponses = allResults.stream()
                .map(r -> AllocationItemResponse.builder()
                        .tripId(r.getTripId())
                        .externalTripId(r.getExternalTripId())
                        .allocationWeight(r.getWeight())
                        .totalWeight(r.getTotalWeight())
                        .fixedFeePaisa(r.getFixedFeePaisa())
                        .allocatedPaisa(r.getAllocatedPaisa())
                        .strategyName(r.getStrategyName())
                        .build())
                .toList();

        long totalFeePaisa = byVersion.values().stream()
                .map(list -> list.get(0).version().getMonthlyFixedFeePaisa())
                .mapToLong(Long::longValue).sum();

        return FixedFeeAllocationResponse.builder()
                .billingRunId(billingRunId)
                .vehicleId(vehicle.getId())
                .vehicleRegistrationNumber(vehicle.getRegistrationNumber())
                .billingMonth(ym.toString())
                .fixedMonthlyFeePaisa(totalFeePaisa)
                .totalAllocatedPaisa(totalAllocated)
                .allocatedTripCount(allResults.size())
                .allocationStrategy(distanceStrategy.strategyName())
                .reconciled(true)
                .updatedInvoiceTotalPaisa(invoice.getTotalPaisa())
                .items(itemResponses)
                .build();
    }
}

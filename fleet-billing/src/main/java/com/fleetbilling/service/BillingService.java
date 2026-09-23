package com.fleetbilling.service;

import com.fleetbilling.billing.BillingCalculationResult;
import com.fleetbilling.billing.PricingEngine;
import com.fleetbilling.dto.billing.*;
import com.fleetbilling.entity.*;
import com.fleetbilling.enums.BillingRunStatus;
import com.fleetbilling.enums.BillingType;
import com.fleetbilling.enums.TripStatus;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Core billing service.
 *
 * Billing flow:
 *   1. Parse and validate billing month
 *   2. Check duplicate billing run (idempotency)
 *   3. Load vehicle
 *   4. Find eligible trips (COMPLETED, within month date range)
 *   5. Find active contract for vehicle
 *   6. For each trip: resolve contract version by trip date
 *   7. Calculate charge via PricingEngine
 *   8. Persist BillingRun, Invoice, InvoiceItems atomically
 *   9. Reconcile: SUM(items) == invoice.total == billingRun.total
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRunRepository billingRunRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final VehicleRepository vehicleRepository;
    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final TripRepository tripRepository;
    private final PricingEngine pricingEngine;

    // ── Create Billing Run ────────────────────────────────────────────────────

    @Transactional
    public BillingRunResponse createBillingRun(BillingRunRequest request) {
        LocalDate billingMonthDate = parseBillingMonth(request.getBillingMonth());

        // Idempotency: reject duplicate billing runs
        if (billingRunRepository.existsByVehicleIdAndBillingMonth(request.getVehicleId(), billingMonthDate)) {
            throw new DuplicateResourceException(
                    "Billing already exists for vehicle " + request.getVehicleId() +
                    " for " + request.getBillingMonth());
        }

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found with id " + request.getVehicleId()));

        // Create billing run in STARTED state
        BillingRun billingRun = BillingRun.builder()
                .vehicle(vehicle)
                .billingMonth(billingMonthDate)
                .status(BillingRunStatus.STARTED)
                .startedAt(LocalDateTime.now())
                .build();
        billingRun = billingRunRepository.save(billingRun);

        try {
            billingRun.setStatus(BillingRunStatus.PROCESSING);
            billingRun = billingRunRepository.save(billingRun);

            // Find eligible trips
            YearMonth ym = YearMonth.of(billingMonthDate.getYear(), billingMonthDate.getMonth());
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd   = ym.atEndOfMonth();

            List<Trip> eligibleTrips = tripRepository.findByFilters(
                    vehicle.getId(), TripStatus.COMPLETED, monthStart, monthEnd, Pageable.unpaged()
            ).getContent();

            log.info("Billing run {}: {} eligible trips for vehicle {} in {}",
                    billingRun.getId(), eligibleTrips.size(), vehicle.getId(), request.getBillingMonth());

            // Find the active contract for this vehicle
            Contract contract = findActiveContract(vehicle, monthStart);

            // Calculate charges per trip
            List<BillingCalculationResult> results = new ArrayList<>();
            for (Trip trip : eligibleTrips) {
                ContractVersion version = contractVersionRepository
                        .findApplicableVersion(contract.getId(), trip.getTripDate())
                        .orElseThrow(() -> new BusinessException(
                                "Trip " + trip.getExternalTripId() +
                                " has no applicable contract version for " + trip.getTripDate()));
                results.add(pricingEngine.calculate(trip, version));
            }

            // Aggregate total
            long subtotalPaisa = results.stream()
                    .mapToLong(BillingCalculationResult::getTotalChargePaisa)
                    .sum();

            // Generate invoice number
            String invoiceNumber = generateInvoiceNumber(vehicle, billingMonthDate);

            // Create Invoice
            Invoice invoice = Invoice.builder()
                    .billingRun(billingRun)
                    .vehicle(vehicle)
                    .invoiceNumber(invoiceNumber)
                    .invoiceDate(LocalDate.now())
                    .subtotalPaisa(subtotalPaisa)
                    .taxPaisa(0L) // Tax calculation is out of scope for Phase 7
                    .totalPaisa(subtotalPaisa)
                    .build();
            invoice = invoiceRepository.save(invoice);

            // Create Invoice Items
            for (int i = 0; i < results.size(); i++) {
                BillingCalculationResult result = results.get(i);
                Trip trip = eligibleTrips.get(i);
                ContractVersion version = contractVersionRepository
                        .findApplicableVersion(contract.getId(), trip.getTripDate())
                        .orElseThrow();

                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .trip(trip)
                        .description(result.getDescription())
                        .pricingType(version.getBillingType())
                        .quantity(result.getDistanceKm() != null ? result.getDistanceKm() : BigDecimal.ONE)
                        .ratePaisa(resolveDisplayRate(version, result))
                        .amountPaisa(result.getTotalChargePaisa())
                        .explanation(result.getExplanation())
                        .build();
                invoiceItemRepository.save(item);
            }

            // Reconciliation assertion
            long itemSum = invoiceItemRepository.findByInvoiceId(invoice.getId())
                    .stream().mapToLong(InvoiceItem::getAmountPaisa).sum();
            if (itemSum != invoice.getTotalPaisa()) {
                throw new BusinessException(
                        "Billing reconciliation failed: invoice total " + invoice.getTotalPaisa() +
                        " != sum of items " + itemSum);
            }

            // Complete the billing run
            billingRun.setStatus(BillingRunStatus.COMPLETED);
            billingRun.setCompletedAt(LocalDateTime.now());
            billingRun = billingRunRepository.save(billingRun);

            log.info("Billing run {} COMPLETED: {} trips, total {} paisa, invoice {}",
                    billingRun.getId(), eligibleTrips.size(), subtotalPaisa, invoiceNumber);

            return mapToResponse(billingRun, invoice.getId(), subtotalPaisa, eligibleTrips.size());

        } catch (Exception e) {
            billingRun.setStatus(BillingRunStatus.FAILED);
            billingRun.setErrorMessage(e.getMessage());
            billingRun.setCompletedAt(LocalDateTime.now());
            billingRunRepository.save(billingRun);
            log.error("Billing run {} FAILED: {}", billingRun.getId(), e.getMessage());
            throw e;
        }
    }

    // ── Preview (no persistence) ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public BillingPreviewResponse previewBilling(BillingRunRequest request) {
        LocalDate billingMonthDate = parseBillingMonth(request.getBillingMonth());

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found with id " + request.getVehicleId()));

        YearMonth ym = YearMonth.of(billingMonthDate.getYear(), billingMonthDate.getMonth());
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();

        List<Trip> eligibleTrips = tripRepository.findByFilters(
                vehicle.getId(), TripStatus.COMPLETED, monthStart, monthEnd, Pageable.unpaged()
        ).getContent();

        Contract contract = findActiveContract(vehicle, monthStart);

        long estimatedTotal = 0L;
        for (Trip trip : eligibleTrips) {
            ContractVersion version = contractVersionRepository
                    .findApplicableVersion(contract.getId(), trip.getTripDate())
                    .orElse(null);
            if (version != null) {
                estimatedTotal += pricingEngine.calculate(trip, version).getTotalChargePaisa();
            }
        }

        return BillingPreviewResponse.builder()
                .vehicleId(vehicle.getId())
                .vehicleRegistrationNumber(vehicle.getRegistrationNumber())
                .billingMonth(request.getBillingMonth())
                .tripCount(eligibleTrips.size())
                .estimatedTotalPaisa(estimatedTotal)
                .build();
    }

    // ── Get Billing Run ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public BillingRunResponse getBillingRunById(Long id) {
        BillingRun run = billingRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing run not found with id " + id));
        Invoice invoice = invoiceRepository.findByBillingRunId(run.getId()).orElse(null);
        long totalPaisa = invoice != null ? invoice.getTotalPaisa() : 0L;
        int tripCount = invoice != null
                ? invoiceItemRepository.findByInvoiceId(invoice.getId()).size()
                : 0;
        return mapToResponse(run, invoice != null ? invoice.getId() : null, totalPaisa, tripCount);
    }

    @Transactional(readOnly = true)
    public Page<BillingRunResponse> getBillingRuns(Long vehicleId, Pageable pageable) {
        Page<BillingRun> page = vehicleId != null
                ? billingRunRepository.findByVehicleId(vehicleId, pageable)
                : billingRunRepository.findAll(pageable);
        return page.map(run -> {
            Invoice invoice = invoiceRepository.findByBillingRunId(run.getId()).orElse(null);
            long total = invoice != null ? invoice.getTotalPaisa() : 0L;
            int count = invoice != null ? invoiceItemRepository.findByInvoiceId(invoice.getId()).size() : 0;
            return mapToResponse(run, invoice != null ? invoice.getId() : null, total, count);
        });
    }

    // ── Invoice APIs ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + id));
        return mapInvoiceToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoices(Long vehicleId, Pageable pageable) {
        Page<Invoice> page = vehicleId != null
                ? invoiceRepository.findByVehicleId(vehicleId, pageable)
                : invoiceRepository.findAll(pageable);
        return page.map(this::mapInvoiceToResponse);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private LocalDate parseBillingMonth(String billingMonth) {
        try {
            YearMonth ym = YearMonth.parse(billingMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
            return ym.atDay(1);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Invalid billing month format: '" + billingMonth + "'. Expected YYYY-MM");
        }
    }

    /**
     * Find the active/signed contract for a vehicle on the given date.
     * Contracts with vehicle_id matching are preferred; falls back to vendor-level contracts.
     */
    private Contract findActiveContract(Vehicle vehicle, LocalDate date) {
        List<Contract> contracts = contractRepository.findByVehicleId(vehicle.getId());
        if (contracts.isEmpty() && vehicle.getVendor() != null) {
            contracts = contractRepository.findByVendorId(vehicle.getVendor().getId());
        }
        return contracts.stream()
                .filter(c -> c.getStartDate() != null && !c.getStartDate().isAfter(date))
                .filter(c -> c.getEndDate() == null || !c.getEndDate().isBefore(date))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active contract found for vehicle " + vehicle.getId() +
                        " (Vendor " + (vehicle.getVendor() != null ? vehicle.getVendor().getName() : "N/A") + ") on " + date));
    }

    private String generateInvoiceNumber(Vehicle vehicle, LocalDate billingMonthDate) {
        String base = String.format("INV-%d-%02d-VEH-%d",
                billingMonthDate.getYear(),
                billingMonthDate.getMonthValue(),
                vehicle.getId());
        // Ensure uniqueness by appending a suffix if name already used
        if (!invoiceRepository.existsByInvoiceNumber(base)) return base;
        return base + "-" + System.currentTimeMillis();
    }

    private long resolveDisplayRate(ContractVersion version, BillingCalculationResult result) {
        if (version.getBillingType() == BillingType.PER_KM && version.getOveragePerKmPaisa() != null) {
            return version.getOveragePerKmPaisa();
        }
        return 0L; // composite rate for tiered — stored in explanation
    }

    private BillingRunResponse mapToResponse(BillingRun run, Long invoiceId, long totalPaisa, int tripCount) {
        YearMonth ym = YearMonth.from(run.getBillingMonth());
        return BillingRunResponse.builder()
                .billingRunId(run.getId())
                .vehicleId(run.getVehicle().getId())
                .vehicleRegistrationNumber(run.getVehicle().getRegistrationNumber())
                .billingMonth(ym.toString())
                .billingMonthDate(run.getBillingMonth())
                .status(run.getStatus())
                .invoiceId(invoiceId)
                .totalPaisa(totalPaisa)
                .tripCount(tripCount)
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .errorMessage(run.getErrorMessage())
                .build();
    }

    private InvoiceResponse mapInvoiceToResponse(Invoice invoice) {
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        BillingRun run = invoice.getBillingRun();
        YearMonth ym = YearMonth.from(run.getBillingMonth());

        List<InvoiceItemResponse> itemResponses = items.stream()
                .map(item -> InvoiceItemResponse.builder()
                        .id(item.getId())
                        .tripId(item.getTrip() != null ? item.getTrip().getId() : null)
                        .externalTripId(item.getTrip() != null ? item.getTrip().getExternalTripId() : null)
                        .description(item.getDescription())
                        .pricingType(item.getPricingType())
                        .quantity(item.getQuantity())
                        .ratePaisa(item.getRatePaisa())
                        .amountPaisa(item.getAmountPaisa())
                        .explanation(item.getExplanation())
                        .createdAt(item.getCreatedAt())
                        .build())
                .toList();

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .billingRunId(run.getId())
                .vehicleId(invoice.getVehicle().getId())
                .vehicleRegistrationNumber(invoice.getVehicle().getRegistrationNumber())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .billingMonth(ym.toString())
                .subtotalPaisa(invoice.getSubtotalPaisa())
                .taxPaisa(invoice.getTaxPaisa())
                .totalPaisa(invoice.getTotalPaisa())
                .items(itemResponses)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}

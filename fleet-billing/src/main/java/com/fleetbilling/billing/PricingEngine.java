package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.enums.BillingType;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.repository.PricingSlabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Core pricing engine that:
 * 1. Selects the correct PricingStrategy based on billing type
 * 2. Calculates base charge
 * 3. Calculates extra charges (night, waiting, toll)
 * 4. Builds the BillingCalculationResult
 *
 * All monetary values remain as Long (paisa) throughout.
 * BigDecimal is used only for intermediate non-monetary calculations (hours × rate).
 */
@Component
@RequiredArgsConstructor
public class PricingEngine {

    private final PerKmPricingStrategy perKmStrategy;
    private final PerTripPricingStrategy perTripStrategy;
    private final FixedMonthlyPricingStrategy fixedMonthlyStrategy;
    private final PricingSlabRepository pricingSlabRepository;

    /**
     * Calculate the full billing result for a single trip under the given contract version.
     *
     * @param trip            the trip to bill
     * @param contractVersion the version active on trip.tripDate
     * @return a complete BillingCalculationResult with all charge components in paisa
     */
    public BillingCalculationResult calculate(Trip trip, ContractVersion contractVersion) {
        List<PricingSlab> slabs = pricingSlabRepository
                .findByContractVersionIdOrderByFromValueAsc(contractVersion.getId());

        // ── Base charge ──────────────────────────────────────────────────────
        PricingStrategy strategy = selectStrategy(contractVersion.getBillingType(), contractVersion.getId());
        long baseChargePaisa = strategy.calculateBaseChargePaisa(trip, contractVersion, slabs);

        // ── Extra charges ─────────────────────────────────────────────────────
        long nightChargePaisa    = calculateNightCharge(trip, contractVersion);
        long waitingChargePaisa  = calculateWaitingCharge(trip, contractVersion);
        long tollPassThroughPaisa = trip.getTollAmountPaisa() != null ? trip.getTollAmountPaisa() : 0L;

        long totalChargePaisa = baseChargePaisa + nightChargePaisa + waitingChargePaisa + tollPassThroughPaisa;

        String description  = strategy.buildDescription(trip, contractVersion, baseChargePaisa);
        String explanation  = strategy.buildExplanation(trip, contractVersion, slabs, baseChargePaisa)
                + "|night=" + nightChargePaisa
                + "|waiting=" + waitingChargePaisa
                + "|toll=" + tollPassThroughPaisa;

        return BillingCalculationResult.builder()
                .tripId(trip.getId())
                .externalTripId(trip.getExternalTripId())
                .contractVersionId(contractVersion.getId())
                .distanceKm(trip.getDistanceKm())
                .dutyHours(trip.getDutyHours())
                .waitingHours(trip.getWaitingHours())
                .tollAmountPaisa(trip.getTollAmountPaisa())
                .nightTrip(Boolean.TRUE.equals(trip.getNight()))
                .baseChargePaisa(baseChargePaisa)
                .nightChargePaisa(nightChargePaisa)
                .waitingChargePaisa(waitingChargePaisa)
                .tollPassThroughPaisa(tollPassThroughPaisa)
                .totalChargePaisa(totalChargePaisa)
                .description(description)
                .explanation(explanation)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private PricingStrategy selectStrategy(BillingType billingType, Long contractVersionId) {
        return switch (billingType) {
            case PER_KM    -> perKmStrategy;
            case PER_TRIP  -> perTripStrategy;
            case FIXED_MONTHLY -> fixedMonthlyStrategy;
        };
    }

    /**
     * Night surcharge: flat paisa amount per night trip, from ContractVersion.
     */
    private long calculateNightCharge(Trip trip, ContractVersion cv) {
        if (!Boolean.TRUE.equals(trip.getNight())) return 0L;
        return cv.getNightChargePaisa() != null ? cv.getNightChargePaisa() : 0L;
    }

    /**
     * Waiting charge: waitingHours × waitingChargePerHourPaisa.
     * Rounding: HALF_UP to nearest paisa.
     */
    private long calculateWaitingCharge(Trip trip, ContractVersion cv) {
        if (trip.getWaitingHours() == null || trip.getWaitingHours().compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        if (cv.getWaitingChargePerHourPaisa() == null || cv.getWaitingChargePerHourPaisa() == 0L) {
            return 0L;
        }
        return trip.getWaitingHours()
                .multiply(BigDecimal.valueOf(cv.getWaitingChargePerHourPaisa()))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }
}

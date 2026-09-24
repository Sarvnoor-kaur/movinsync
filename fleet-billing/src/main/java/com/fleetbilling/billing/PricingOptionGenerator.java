package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.enums.BillingType;
import com.fleetbilling.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates all valid pricing options permitted by a ContractVersion for a given Trip.
 *
 * <p>Only options explicitly defined or permitted by the contract configuration are generated.
 * Arbitrary or unconfigured options are never generated.
 */
@Component
@RequiredArgsConstructor
public class PricingOptionGenerator {

    private final PerKmPricingStrategy perKmPricingStrategy;
    private final PerTripPricingStrategy perTripPricingStrategy;
    private final FixedMonthlyPricingStrategy fixedMonthlyPricingStrategy;

    /**
     * Generate all valid contract-permitted pricing options for a trip.
     *
     * @param trip            the trip to evaluate
     * @param contractVersion the active contract version
     * @param slabs           the pricing slabs configured for this version
     * @return list of valid pricing options
     */
    public List<PricingOption> generateValidOptions(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs) {
        List<PricingOption> options = new ArrayList<>();

        long nightCharge    = calculateNightCharge(trip, contractVersion);
        long waitingCharge  = calculateWaitingCharge(trip, contractVersion);
        long tollPassThrough = trip.getTollAmountPaisa() != null ? trip.getTollAmountPaisa() : 0L;
        long extraCharges   = nightCharge + waitingCharge + tollPassThrough;

        BillingType billingType = contractVersion.getBillingType();

        // ── Option 1: Progressive Tiered Slabs ──────────────────────────────────
        if ((billingType == BillingType.PER_KM || slabs != null) && slabs != null && !slabs.isEmpty()) {
            long baseCharge = perKmPricingStrategy.calculateBaseChargePaisa(trip, contractVersion, slabs);
            long total = baseCharge + extraCharges;
            String desc = perKmPricingStrategy.buildDescription(trip, contractVersion, baseCharge);
            String expl = perKmPricingStrategy.buildExplanation(trip, contractVersion, slabs, baseCharge);

            options.add(PricingOption.builder()
                    .optionName("PROGRESSIVE_SLAB")
                    .pricingMethod("Progressive Tiered Slabs")
                    .priority(1)
                    .baseChargePaisa(baseCharge)
                    .nightChargePaisa(nightCharge)
                    .waitingChargePaisa(waitingCharge)
                    .tollPassThroughPaisa(tollPassThrough)
                    .totalChargePaisa(total)
                    .description(desc)
                    .explanation(expl)
                    .selected(false)
                    .build());
        }

        // ── Option 2: Flat Rate Per Km ──────────────────────────────────────────
        if (contractVersion.getOveragePerKmPaisa() != null && contractVersion.getOveragePerKmPaisa() > 0) {
            BigDecimal distance = trip.getDistanceKm() != null ? trip.getDistanceKm() : BigDecimal.ZERO;
            long baseCharge = distance.multiply(BigDecimal.valueOf(contractVersion.getOveragePerKmPaisa()))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            long total = baseCharge + extraCharges;
            String desc = String.format("Flat distance charge: %s km @ %d paisa/km = %d paisa",
                    distance, contractVersion.getOveragePerKmPaisa(), baseCharge);
            String expl = String.format("FLAT_PER_KM|distance=%skm|rate=%dpaisa|base=%dpaisa",
                    distance, contractVersion.getOveragePerKmPaisa(), baseCharge);

            options.add(PricingOption.builder()
                    .optionName("FLAT_PER_KM")
                    .pricingMethod("Flat Rate Per Kilometer")
                    .priority(2)
                    .baseChargePaisa(baseCharge)
                    .nightChargePaisa(nightCharge)
                    .waitingChargePaisa(waitingCharge)
                    .tollPassThroughPaisa(tollPassThrough)
                    .totalChargePaisa(total)
                    .description(desc)
                    .explanation(expl)
                    .selected(false)
                    .build());
        }

        // ── Option 3: Per Trip Flat Rate ───────────────────────────────────────
        if (billingType == BillingType.PER_TRIP) {
            long baseCharge = perTripPricingStrategy.calculateBaseChargePaisa(trip, contractVersion, slabs);
            long total = baseCharge + extraCharges;
            String desc = perTripPricingStrategy.buildDescription(trip, contractVersion, baseCharge);
            String expl = perTripPricingStrategy.buildExplanation(trip, contractVersion, slabs, baseCharge);

            options.add(PricingOption.builder()
                    .optionName("PER_TRIP")
                    .pricingMethod("Flat Rate Per Trip")
                    .priority(3)
                    .baseChargePaisa(baseCharge)
                    .nightChargePaisa(nightCharge)
                    .waitingChargePaisa(waitingCharge)
                    .tollPassThroughPaisa(tollPassThrough)
                    .totalChargePaisa(total)
                    .description(desc)
                    .explanation(expl)
                    .selected(false)
                    .build());
        }

        // ── Option 4: Monthly Fixed Fee ───────────────────────────────────────
        if (billingType == BillingType.FIXED_MONTHLY) {
            long baseCharge = fixedMonthlyPricingStrategy.calculateBaseChargePaisa(trip, contractVersion, slabs);
            long total = baseCharge + extraCharges;
            String desc = fixedMonthlyPricingStrategy.buildDescription(trip, contractVersion, baseCharge);
            String expl = fixedMonthlyPricingStrategy.buildExplanation(trip, contractVersion, slabs, baseCharge);

            options.add(PricingOption.builder()
                    .optionName("FIXED_MONTHLY")
                    .pricingMethod("Monthly Fixed Retainer Fee")
                    .priority(4)
                    .baseChargePaisa(baseCharge)
                    .nightChargePaisa(nightCharge)
                    .waitingChargePaisa(waitingCharge)
                    .tollPassThroughPaisa(tollPassThrough)
                    .totalChargePaisa(total)
                    .description(desc)
                    .explanation(expl)
                    .selected(false)
                    .build());
        }

        if (options.isEmpty()) {
            throw new BusinessException(
                    "No valid pricing configuration found for contract version " + contractVersion.getId());
        }

        return options;
    }

    private long calculateNightCharge(Trip trip, ContractVersion cv) {
        if (!Boolean.TRUE.equals(trip.getNight())) return 0L;
        return cv.getNightChargePaisa() != null ? cv.getNightChargePaisa() : 0L;
    }

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

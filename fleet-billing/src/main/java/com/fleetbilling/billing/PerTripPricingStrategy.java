package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PER_TRIP pricing strategy.
 *
 * Each completed trip receives a flat rate as defined by the first TRIP-unit slab.
 * The rate comes from the PricingSlab with unitType=TRIP, slabOrder=1 (the base rate).
 *
 * If no TRIP slab exists, falls back to the first available slab rate.
 */
@Component
public class PerTripPricingStrategy implements PricingStrategy {

    @Override
    public long calculateBaseChargePaisa(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs) {
        if (slabs == null || slabs.isEmpty()) {
            throw new BusinessException(
                    "No pricing slabs found for contract version " + contractVersion.getId() +
                    " (PER_TRIP billing requires at least one TRIP slab)");
        }
        // Use the first slab's rate as the per-trip flat rate
        long ratePaisa = slabs.get(0).getRatePaisa();
        return ratePaisa;
    }

    @Override
    public String buildDescription(Trip trip, ContractVersion contractVersion, long baseChargePaisa) {
        return String.format("Per-trip charge (Contract Version %d): %d paisa",
                contractVersion.getId(), baseChargePaisa);
    }

    @Override
    public String buildExplanation(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs, long baseChargePaisa) {
        return String.format("PER_TRIP|tripId=%d|ratePaisa=%d|total=%dpaisa",
                trip.getId(), slabs.isEmpty() ? 0 : slabs.get(0).getRatePaisa(), baseChargePaisa);
    }
}

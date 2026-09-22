package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;

import java.util.List;

/**
 * Strategy interface for calculating the base trip charge.
 * Each billing type (PER_KM, PER_TRIP, etc.) has its own implementation.
 *
 * Extra charges (night, waiting, toll) are applied by the PricingEngine
 * AFTER the base strategy runs — they are not the responsibility of any individual strategy.
 */
public interface PricingStrategy {

    /**
     * Calculate the base charge in paisa for the given trip, version and slabs.
     *
     * @param trip            the trip being billed
     * @param contractVersion the contract version applicable on the trip date
     * @param slabs           the pricing slabs ordered by fromValue ascending
     * @return the base charge in paisa (never negative)
     */
    long calculateBaseChargePaisa(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs);

    /**
     * Human-readable description for the invoice item.
     */
    String buildDescription(Trip trip, ContractVersion contractVersion, long baseChargePaisa);

    /**
     * Machine-readable explanation for audit.
     */
    String buildExplanation(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs, long baseChargePaisa);
}

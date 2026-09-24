package com.fleetbilling.billing;

import lombok.Builder;
import lombok.Getter;

/**
 * Value object representing a complete contract-permitted pricing option
 * for calculating the base fare and total charges of a trip.
 */
@Getter
@Builder
public class PricingOption {

    /**
     * Name or identifier of the pricing option (e.g. "PROGRESSIVE_SLAB", "FLAT_PER_KM", "PER_TRIP", "FIXED_MONTHLY").
     */
    private final String optionName;

    /**
     * Human-readable description of the pricing method used.
     */
    private final String pricingMethod;

    /**
     * Deterministic priority order used for tie-breaking when two options have identical costs.
     * Lower numbers indicate higher priority.
     */
    private final int priority;

    /**
     * Calculated base charge for the trip in integer paisa.
     */
    private final long baseChargePaisa;

    /**
     * Calculated night surcharge in integer paisa.
     */
    private final long nightChargePaisa;

    /**
     * Calculated waiting charge in integer paisa.
     */
    private final long waitingChargePaisa;

    /**
     * Pass-through toll charge in integer paisa.
     */
    private final long tollPassThroughPaisa;

    /**
     * Sum of all charges (base + night + waiting + toll) in integer paisa.
     */
    private final long totalChargePaisa;

    /**
     * Invoice item line description.
     */
    private final String description;

    /**
     * Detailed breakdown of how the fare was calculated for auditing.
     */
    private final String explanation;

    /**
     * Flag indicating whether this option was selected as the best valid option.
     */
    private final boolean selected;
}

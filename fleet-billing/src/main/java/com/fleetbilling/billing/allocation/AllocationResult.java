package com.fleetbilling.billing.allocation;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Immutable result of the Largest Remainder allocation for one trip.
 *
 * All monetary values in paisa (Long). BigDecimal used only for non-monetary weights.
 */
@Getter
@Builder
public class AllocationResult {

    /** ID of the trip that received this allocation. */
    private final Long tripId;

    /** External trip identifier (for invoice item description). */
    private final String externalTripId;

    /** This trip's weight (e.g. distanceKm). */
    private final BigDecimal weight;

    /** Sum of all eligible trips' weights (denominator). */
    private final BigDecimal totalWeight;

    /** The fixed monthly fee being distributed (in paisa). */
    private final long fixedFeePaisa;

    /** The paisa amount allocated to this trip after Largest Remainder. */
    private final long allocatedPaisa;

    /** Name of the strategy used (e.g. "DISTANCE"). */
    private final String strategyName;
}

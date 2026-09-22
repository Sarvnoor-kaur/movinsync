package com.fleetbilling.billing.allocation;

import com.fleetbilling.entity.Trip;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy interface for computing a weight for each trip in the fixed-fee allocation pool.
 *
 * The allocation service uses the weight to proportionally divide the fixed monthly fee
 * across eligible trips using the Largest Remainder Method.
 *
 * New strategies (e.g. TRIP_COUNT, DURATION) can be added without changing the
 * allocation service by implementing this interface.
 */
public interface AllocationStrategy {

    /**
     * Returns the allocation weight for a single trip.
     * A weight of ZERO means the trip contributes nothing to the denominator.
     *
     * @param trip the completed trip
     * @return non-negative BigDecimal weight
     */
    BigDecimal weightFor(Trip trip);

    /**
     * Human-readable strategy identifier used in InvoiceItem explanations.
     */
    String strategyName();
}

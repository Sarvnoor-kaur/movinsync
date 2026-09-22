package com.fleetbilling.billing.allocation;

import com.fleetbilling.entity.Trip;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Allocation strategy that weights trips by their distanceKm.
 *
 * If a trip has null or zero distanceKm it contributes zero weight.
 * If ALL trips have zero weight the allocation service will throw BusinessException.
 */
@Component
public class DistanceAllocationStrategy implements AllocationStrategy {

    @Override
    public BigDecimal weightFor(Trip trip) {
        if (trip.getDistanceKm() == null) return BigDecimal.ZERO;
        return trip.getDistanceKm().max(BigDecimal.ZERO);
    }

    @Override
    public String strategyName() {
        return "DISTANCE";
    }
}

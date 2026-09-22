package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * PER_KM pricing strategy.
 *
 * Supports tiered slab pricing. For each slab, the distance falling within
 * the slab boundaries is multiplied by the slab rate in paisa.
 *
 * Rounding rule: When converting BigDecimal km × Long paisa/km, the result
 * is rounded to the nearest whole paisa using HALF_UP.
 * Example: 42.5 km × 1500 paisa/km = 63750 paisa (exact, no rounding needed)
 * Example: 1.333 km × 1800 paisa/km = 2399.4 → 2399 paisa (HALF_UP)
 */
@Component
public class PerKmPricingStrategy implements PricingStrategy {

    @Override
    public long calculateBaseChargePaisa(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs) {
        if (trip.getDistanceKm() == null || trip.getDistanceKm().compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        if (slabs == null || slabs.isEmpty()) {
            throw new BusinessException(
                    "No pricing slabs found for contract version " + contractVersion.getId() +
                    " (PER_KM billing requires at least one KM slab)");
        }

        BigDecimal distanceKm = trip.getDistanceKm();
        long totalPaisa = 0L;
        BigDecimal remaining = distanceKm;

        for (PricingSlab slab : slabs) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal slabFrom = BigDecimal.valueOf(slab.getFromValue());
            BigDecimal slabTo = slab.getToValue() != null
                    ? BigDecimal.valueOf(slab.getToValue())
                    : null; // open-ended slab

            // Width of this slab
            BigDecimal slabWidth = slabTo != null
                    ? slabTo.subtract(slabFrom).add(BigDecimal.ONE)
                    : null; // unlimited

            // How much of this slab is consumed
            BigDecimal inSlab = slabWidth != null
                    ? remaining.min(slabWidth)
                    : remaining;

            // paisa = km × rate_paisa/km
            long slabCharge = inSlab
                    .multiply(BigDecimal.valueOf(slab.getRatePaisa()))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();

            totalPaisa += slabCharge;
            remaining = remaining.subtract(inSlab);
        }

        return totalPaisa;
    }

    @Override
    public String buildDescription(Trip trip, ContractVersion contractVersion, long baseChargePaisa) {
        return String.format("Distance charge: %s km (Contract Version %d) = %d paisa",
                trip.getDistanceKm(), contractVersion.getId(), baseChargePaisa);
    }

    @Override
    public String buildExplanation(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs, long baseChargePaisa) {
        StringBuilder sb = new StringBuilder();
        sb.append("PER_KM|distance=").append(trip.getDistanceKm()).append("km|slabs:");
        for (PricingSlab slab : slabs) {
            sb.append("[").append(slab.getFromValue()).append("-")
              .append(slab.getToValue() != null ? slab.getToValue() : "∞")
              .append("@").append(slab.getRatePaisa()).append("paisa]");
        }
        sb.append("|total=").append(baseChargePaisa).append("paisa");
        return sb.toString();
    }
}

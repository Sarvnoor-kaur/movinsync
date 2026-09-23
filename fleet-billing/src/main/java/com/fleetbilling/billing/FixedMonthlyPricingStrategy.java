package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * FIXED_MONTHLY pricing strategy.
 *
 * For FIXED_MONTHLY contracts, base charge per trip is 0 paisa.
 * The lump sum fixed monthly fee is allocated across eligible trips via BillingAllocationService.
 */
@Component
public class FixedMonthlyPricingStrategy implements PricingStrategy {

    @Override
    public long calculateBaseChargePaisa(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs) {
        return 0L;
    }

    @Override
    public String buildDescription(Trip trip, ContractVersion contractVersion, long baseChargePaisa) {
        return String.format("Fixed monthly contract trip (Contract Version %d)", contractVersion.getId());
    }

    @Override
    public String buildExplanation(Trip trip, ContractVersion contractVersion, List<PricingSlab> slabs, long baseChargePaisa) {
        return String.format("FIXED_MONTHLY|tripId=%d|baseChargePaisa=0", trip.getId());
    }
}

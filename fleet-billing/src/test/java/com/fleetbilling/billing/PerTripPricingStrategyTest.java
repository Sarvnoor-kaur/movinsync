package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.enums.BillingType;
import com.fleetbilling.enums.PricingUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerTripPricingStrategyTest {

    private PerTripPricingStrategy strategy;
    private ContractVersion version;

    @BeforeEach
    void setUp() {
        strategy = new PerTripPricingStrategy();
        version = ContractVersion.builder()
                .id(1L)
                .billingType(BillingType.PER_TRIP)
                .build();
    }

    private Trip trip() {
        return Trip.builder().id(1L).externalTripId("TRIP-001")
                .distanceKm(new BigDecimal("10")).build();
    }

    private PricingSlab tripSlab(long rate) {
        return PricingSlab.builder().fromValue(1).toValue(null)
                .ratePaisa(rate).unitType(PricingUnitType.TRIP).slabOrder(1).build();
    }

    @Test
    void perTrip_singleTrip_500rupees() {
        // ₹500/trip = 50000 paisa
        long result = strategy.calculateBaseChargePaisa(trip(), version, List.of(tripSlab(50_000L)));
        assertEquals(50_000L, result);
    }

    @Test
    void perTrip_differentRate() {
        // ₹250/trip = 25000 paisa
        long result = strategy.calculateBaseChargePaisa(trip(), version, List.of(tripSlab(25_000L)));
        assertEquals(25_000L, result);
    }
}

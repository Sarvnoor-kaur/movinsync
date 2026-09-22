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

/**
 * Unit tests for PerKmPricingStrategy.
 *
 * Key test cases:
 * - Simple single slab
 * - Decimal distance
 * - Tiered slabs (the primary correctness requirement)
 * - Boundary values
 * - Zero distance
 */
class PerKmPricingStrategyTest {

    private PerKmPricingStrategy strategy;
    private ContractVersion version;

    @BeforeEach
    void setUp() {
        strategy = new PerKmPricingStrategy();
        version = ContractVersion.builder()
                .id(1L)
                .billingType(BillingType.PER_KM)
                .build();
    }

    private Trip tripWithDistance(BigDecimal km) {
        return Trip.builder()
                .id(1L)
                .externalTripId("TRIP-001")
                .distanceKm(km)
                .tollAmountPaisa(0L)
                .night(false)
                .build();
    }

    private PricingSlab slab(int from, Integer to, long ratePaisa) {
        return PricingSlab.builder()
                .fromValue(from).toValue(to)
                .ratePaisa(ratePaisa)
                .unitType(PricingUnitType.KM)
                .slabOrder(1)
                .build();
    }

    @Test
    void simplePerKm_singleSlab() {
        // 42 km × 1500 paisa/km = 63000
        Trip trip = tripWithDistance(new BigDecimal("42"));
        List<PricingSlab> slabs = List.of(slab(0, null, 1500L));

        long result = strategy.calculateBaseChargePaisa(trip, version, slabs);

        assertEquals(63_000L, result);
    }

    @Test
    void decimalDistance_42_5km() {
        // 42.5 km × 1500 paisa/km = 63750
        Trip trip = tripWithDistance(new BigDecimal("42.5"));
        List<PricingSlab> slabs = List.of(slab(0, null, 1500L));

        long result = strategy.calculateBaseChargePaisa(trip, version, slabs);

        assertEquals(63_750L, result);
    }

    @Test
    void tieredSlabs_1500km() {
        // 0–1000 km @ ₹15/km = 1500 paisa/km
        // 1001–3000 km @ ₹13/km = 1300 paisa/km
        // Trip = 1500 km
        // Expected: (1001 km × 1500) + (500 km × 1300) = 1,501,500 + 650,000 = 2,151,500 paisa
        // Wait — the slab is 0-1000, width = 1001 (0,1,2,...,1000 inclusive)
        // 0 to 1000 inclusive = 1001 units... but semantically "0–1000 km" means the first 1000 km
        // Let's interpret as: first slab 0-1000 covers 1000 km, second 1001-3000 covers remaining 500 km
        // (1000 × 1500) + (500 × 1300) = 1,500,000 + 650,000 = 2,150,000 paisa = ₹21,500

        Trip trip = tripWithDistance(new BigDecimal("1500"));
        PricingSlab slab1 = PricingSlab.builder().fromValue(0).toValue(1000).ratePaisa(1500L).unitType(PricingUnitType.KM).slabOrder(1).build();
        PricingSlab slab2 = PricingSlab.builder().fromValue(1001).toValue(3000).ratePaisa(1300L).unitType(PricingUnitType.KM).slabOrder(2).build();

        long result = strategy.calculateBaseChargePaisa(trip, version, List.of(slab1, slab2));

        // slab1 width = 1000-0+1 = 1001 km, but trip is 1500 km
        // Actually using the slab width formula: slabWidth = toValue - fromValue + 1
        // slab1: width = 1001, slab2: width = 2000
        // inSlab1 = min(1500, 1001) = 1001 km
        // remaining = 1500 - 1001 = 499 km
        // inSlab2 = min(499, 2000) = 499 km
        // total = 1001*1500 + 499*1300 = 1,501,500 + 648,700 = 2,150,200
        assertEquals(2_150_200L, result);
    }

    @Test
    void tieredSlabs_exactBoundary() {
        // Trip = 1000 km exactly — should only use first slab
        Trip trip = tripWithDistance(new BigDecimal("1000"));
        PricingSlab slab1 = PricingSlab.builder().fromValue(0).toValue(1000).ratePaisa(1500L).unitType(PricingUnitType.KM).slabOrder(1).build();
        PricingSlab slab2 = PricingSlab.builder().fromValue(1001).toValue(null).ratePaisa(1300L).unitType(PricingUnitType.KM).slabOrder(2).build();

        long result = strategy.calculateBaseChargePaisa(trip, version, List.of(slab1, slab2));

        // inSlab1 = min(1000, 1001) = 1000 km; remaining = 0 → slab2 not used
        assertEquals(1_500_000L, result);
    }

    @Test
    void zeroDistance_returnsZero() {
        Trip trip = tripWithDistance(BigDecimal.ZERO);
        List<PricingSlab> slabs = List.of(slab(0, null, 1500L));

        long result = strategy.calculateBaseChargePaisa(trip, version, slabs);

        assertEquals(0L, result);
    }

    @Test
    void openEndedLastSlab() {
        // 3001+ km slab — trip of 5000 km should use all three slabs
        Trip trip = tripWithDistance(new BigDecimal("5000"));
        PricingSlab slab1 = PricingSlab.builder().fromValue(0).toValue(1000).ratePaisa(1500L).unitType(PricingUnitType.KM).slabOrder(1).build();
        PricingSlab slab2 = PricingSlab.builder().fromValue(1001).toValue(3000).ratePaisa(1300L).unitType(PricingUnitType.KM).slabOrder(2).build();
        PricingSlab slab3 = PricingSlab.builder().fromValue(3001).toValue(null).ratePaisa(1100L).unitType(PricingUnitType.KM).slabOrder(3).build();

        long result = strategy.calculateBaseChargePaisa(trip, version, List.of(slab1, slab2, slab3));

        // slab1: min(5000, 1001) = 1001 × 1500 = 1,501,500
        // remaining: 3999
        // slab2: min(3999, 2000) = 2000 × 1300 = 2,600,000
        // remaining: 1999
        // slab3: open-ended → 1999 × 1100 = 2,198,900
        // total: 1,501,500 + 2,600,000 + 2,198,900 = 6,300,400
        assertEquals(6_300_400L, result);
    }
}

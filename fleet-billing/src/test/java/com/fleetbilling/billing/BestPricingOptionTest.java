package com.fleetbilling.billing;

import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.enums.BillingType;
import com.fleetbilling.enums.PricingUnitType;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.repository.PricingSlabRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BestPricingOptionTest {

    private PerKmPricingStrategy perKmStrategy;
    private PerTripPricingStrategy perTripStrategy;
    private FixedMonthlyPricingStrategy fixedMonthlyStrategy;
    private PricingOptionGenerator optionGenerator;
    private BestPricingSelector bestPricingSelector;
    private PricingSlabRepository pricingSlabRepository;
    private PricingEngine pricingEngine;

    @BeforeEach
    void setUp() {
        perKmStrategy = new PerKmPricingStrategy();
        perTripStrategy = new PerTripPricingStrategy();
        fixedMonthlyStrategy = new FixedMonthlyPricingStrategy();
        optionGenerator = new PricingOptionGenerator(perKmStrategy, perTripStrategy, fixedMonthlyStrategy);
        bestPricingSelector = new BestPricingSelector();
        pricingSlabRepository = mock(PricingSlabRepository.class);
        pricingEngine = new PricingEngine(pricingSlabRepository, optionGenerator, bestPricingSelector);
    }

    private Trip createTrip(BigDecimal distanceKm, BigDecimal waitingHours, boolean night, Long tollPaisa) {
        return Trip.builder()
                .id(100L)
                .externalTripId("TRIP-TEST-100")
                .distanceKm(distanceKm)
                .waitingHours(waitingHours)
                .night(night)
                .tollAmountPaisa(tollPaisa)
                .build();
    }

    private PricingSlab createSlab(int from, Integer to, long ratePaisa, int order) {
        return PricingSlab.builder()
                .fromValue(from)
                .toValue(to)
                .ratePaisa(ratePaisa)
                .unitType(PricingUnitType.KM)
                .slabOrder(order)
                .build();
    }

    @Test
    @DisplayName("1. Single pricing option selected directly")
    void test1_singlePricingOption() {
        ContractVersion cv = ContractVersion.builder()
                .id(1L)
                .billingType(BillingType.PER_KM)
                .build();

        Trip trip = createTrip(new BigDecimal("100"), BigDecimal.ZERO, false, 0L);
        PricingSlab slab1 = createSlab(0, null, 1500L, 1);

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(1L)).thenReturn(List.of(slab1));

        BillingCalculationResult result = pricingEngine.calculate(trip, cv);

        assertEquals(150_000L, result.getBaseChargePaisa());
        assertEquals(150_000L, result.getTotalChargePaisa());
        assertTrue(result.getExplanation().contains("SELECTED_OPTION: PROGRESSIVE_SLAB"));
    }

    @Test
    @DisplayName("2. Progressive slab pricing for 1500 km trip")
    void test2_progressiveSlabPricing() {
        // Slab 1: 0-1000 km @ ₹15/km (1500 paisa)
        // Slab 2: 1001-3000 km @ ₹13/km (1300 paisa)
        // Trip = 1500 km
        // Expected: (1001 km × 1500) + (499 km × 1300) = 1,501,500 + 648,700 = 2,150,200 paisa
        ContractVersion cv = ContractVersion.builder()
                .id(2L)
                .billingType(BillingType.PER_KM)
                .build();

        Trip trip = createTrip(new BigDecimal("1500"), BigDecimal.ZERO, false, 0L);
        PricingSlab slab1 = createSlab(0, 1000, 1500L, 1);
        PricingSlab slab2 = createSlab(1001, 3000, 1300L, 2);

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(2L)).thenReturn(List.of(slab1, slab2));

        BillingCalculationResult result = pricingEngine.calculate(trip, cv);

        assertEquals(2_150_200L, result.getBaseChargePaisa());
        assertEquals(2_150_200L, result.getTotalChargePaisa());
    }

    @Test
    @DisplayName("3 & 4. Multiple valid pricing options — Cheapest valid option is selected")
    void test3_4_multipleOptions_cheapestSelected() {
        // Contract defines:
        // Option A (FLAT_PER_KM): Flat ₹15/km = 1500 km × 1500 = 2,250,000 paisa (₹22,500)
        // Option B (PROGRESSIVE_SLAB): 0-1000 @ ₹15/km, 1001-3000 @ ₹13/km = 2,150,200 paisa (₹21,502)
        // System should select Option B (PROGRESSIVE_SLAB) because 2,150,200 < 2,250,000
        ContractVersion cv = ContractVersion.builder()
                .id(3L)
                .billingType(BillingType.PER_KM)
                .overagePerKmPaisa(1500L) // Flat rate option available
                .build();

        Trip trip = createTrip(new BigDecimal("1500"), BigDecimal.ZERO, false, 0L);
        PricingSlab slab1 = createSlab(0, 1000, 1500L, 1);
        PricingSlab slab2 = createSlab(1001, 3000, 1300L, 2);

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(3L)).thenReturn(List.of(slab1, slab2));

        BillingCalculationResult result = pricingEngine.calculate(trip, cv);

        assertEquals(2_150_200L, result.getTotalChargePaisa());
        assertTrue(result.getExplanation().contains("SELECTED_OPTION: PROGRESSIVE_SLAB"));
        assertTrue(result.getExplanation().contains("COMPARED_OPTIONS"));
    }

    @Test
    @DisplayName("5. Two options with exact same price use deterministic priority tie-breaking")
    void test5_tieBreakingDeterministic() {
        // Both Option A (PROGRESSIVE_SLAB) and Option B (FLAT_PER_KM) yield 150,000 paisa for 100 km @ ₹15/km
        // Priority: PROGRESSIVE_SLAB (1) vs FLAT_PER_KM (2)
        // PROGRESSIVE_SLAB must be selected deterministically
        ContractVersion cv = ContractVersion.builder()
                .id(5L)
                .billingType(BillingType.PER_KM)
                .overagePerKmPaisa(1500L)
                .build();

        Trip trip = createTrip(new BigDecimal("100"), BigDecimal.ZERO, false, 0L);
        PricingSlab slab1 = createSlab(0, 1000, 1500L, 1);

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(5L)).thenReturn(List.of(slab1));

        BillingCalculationResult result = pricingEngine.calculate(trip, cv);

        assertEquals(150_000L, result.getTotalChargePaisa());
        assertTrue(result.getExplanation().contains("SELECTED_OPTION: PROGRESSIVE_SLAB"));
    }

    @Test
    @DisplayName("7. Open-ended final slab (3001+ km)")
    void test7_openEndedFinalSlab() {
        ContractVersion cv = ContractVersion.builder()
                .id(7L)
                .billingType(BillingType.PER_KM)
                .build();

        Trip trip = createTrip(new BigDecimal("5000"), BigDecimal.ZERO, false, 0L);
        PricingSlab slab1 = createSlab(0, 1000, 1500L, 1);
        PricingSlab slab2 = createSlab(1001, 3000, 1300L, 2);
        PricingSlab slab3 = createSlab(3001, null, 1100L, 3); // Open-ended

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(7L)).thenReturn(List.of(slab1, slab2, slab3));

        BillingCalculationResult result = pricingEngine.calculate(trip, cv);

        assertEquals(6_300_400L, result.getTotalChargePaisa());
    }

    @Test
    @DisplayName("8. Monthly fixed fee contract option")
    void test8_monthlyFixedFee() {
        ContractVersion cv = ContractVersion.builder()
                .id(8L)
                .billingType(BillingType.FIXED_MONTHLY)
                .monthlyFixedFeePaisa(5_000_000L) // ₹50,000
                .build();

        Trip trip = createTrip(new BigDecimal("50"), BigDecimal.ZERO, false, 0L);

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(8L)).thenReturn(List.of());

        BillingCalculationResult result = pricingEngine.calculate(trip, cv);

        assertEquals(0L, result.getBaseChargePaisa());
        assertEquals(0L, result.getTotalChargePaisa());
        assertTrue(result.getExplanation().contains("SELECTED_OPTION: FIXED_MONTHLY"));
    }

    @Test
    @DisplayName("9 & 10. Night, waiting, and toll charges added correctly")
    void test9_10_additionalTimeCharges() {
        ContractVersion cv = ContractVersion.builder()
                .id(10L)
                .billingType(BillingType.PER_KM)
                .nightChargePaisa(50_000L)           // ₹500 night charge
                .waitingChargePerHourPaisa(20_000L) // ₹200/hr waiting charge
                .build();

        // Trip = 100 km @ ₹15/km = 150,000 paisa
        // Night = true -> +50,000 paisa
        // Waiting = 2.5 hrs -> 2.5 × 20,000 = 50,000 paisa
        // Toll = ₹150 -> 15,000 paisa
        // Total = 150,000 + 50,000 + 50,000 + 15,000 = 265,000 paisa (₹2,650)
        Trip trip = createTrip(new BigDecimal("100"), new BigDecimal("2.5"), true, 15_000L);
        PricingSlab slab1 = createSlab(0, null, 1500L, 1);

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(10L)).thenReturn(List.of(slab1));

        BillingCalculationResult result = pricingEngine.calculate(trip, cv);

        assertEquals(150_000L, result.getBaseChargePaisa());
        assertEquals(50_000L, result.getNightChargePaisa());
        assertEquals(50_000L, result.getWaitingChargePaisa());
        assertEquals(15_000L, result.getTollPassThroughPaisa());
        assertEquals(265_000L, result.getTotalChargePaisa());
    }

    @Test
    @DisplayName("11. Determinism: Same trip + same contract config yields exact same result across multiple runs")
    void test11_determinism() {
        ContractVersion cv = ContractVersion.builder()
                .id(11L)
                .billingType(BillingType.PER_KM)
                .overagePerKmPaisa(1500L)
                .build();

        Trip trip = createTrip(new BigDecimal("1500"), new BigDecimal("1.0"), true, 5_000L);
        PricingSlab slab1 = createSlab(0, 1000, 1500L, 1);
        PricingSlab slab2 = createSlab(1001, 3000, 1300L, 2);

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(11L)).thenReturn(List.of(slab1, slab2));

        BillingCalculationResult run1 = pricingEngine.calculate(trip, cv);
        BillingCalculationResult run2 = pricingEngine.calculate(trip, cv);

        assertEquals(run1.getTotalChargePaisa(), run2.getTotalChargePaisa());
        assertEquals(run1.getExplanation(), run2.getExplanation());
        assertEquals(run1.getDescription(), run2.getDescription());
    }

    @Test
    @DisplayName("12. Invalid/unconfigured pricing options throw BusinessException")
    void test12_unconfiguredPricingOption_throwsException() {
        ContractVersion cv = ContractVersion.builder()
                .id(12L)
                .billingType(BillingType.PER_KM) // PER_KM requires slabs or overagePerKmPaisa
                .build();

        Trip trip = createTrip(new BigDecimal("100"), BigDecimal.ZERO, false, 0L);

        when(pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(12L)).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> pricingEngine.calculate(trip, cv));
    }
}

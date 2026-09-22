package com.fleetbilling.billing.allocation;

import com.fleetbilling.entity.Trip;
import com.fleetbilling.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FixedFeeAllocationService (Largest Remainder Method).
 *
 * All monetary amounts are in paisa (Long). BigDecimal for weights only.
 */
class FixedFeeAllocationServiceTest {

    private FixedFeeAllocationService service;
    private DistanceAllocationStrategy strategy;

    @BeforeEach
    void setUp() {
        service  = new FixedFeeAllocationService();
        strategy = new DistanceAllocationStrategy();
    }

    private Trip trip(long id, double km) {
        return Trip.builder()
                .id(id)
                .externalTripId("TRIP-" + id)
                .distanceKm(km > 0 ? new BigDecimal(String.valueOf(km)) : BigDecimal.ZERO)
                .build();
    }

    // ── Test 1: exact proportional split (no rounding needed) ─────────────────
    @Test
    void basicProportional_exactSplit() {
        // Fee = ₹30,000 → 3,000,000 paisa
        // Weights: 100, 200, 300 → total 600
        // Expected: 500,000 / 1,000,000 / 1,500,000 paisa = ₹5,000 / ₹10,000 / ₹15,000
        List<Trip> trips = List.of(trip(1, 100), trip(2, 200), trip(3, 300));
        long fee = 3_000_000L;

        List<AllocationResult> results = service.allocate(trips, fee, strategy);

        assertEquals(500_000L,   results.get(0).getAllocatedPaisa());
        assertEquals(1_000_000L, results.get(1).getAllocatedPaisa());
        assertEquals(1_500_000L, results.get(2).getAllocatedPaisa());
        assertReconciled(results, fee);
    }

    // ── Test 2: rounding — SUM must equal fee exactly ────────────────────────
    @Test
    void rounding_sumMustEqualFeeExactly() {
        // Fee = 100 paisa, 3 equal trips: exact 33.33... each
        List<Trip> trips = List.of(trip(1, 1), trip(2, 1), trip(3, 1));
        long fee = 100L;

        List<AllocationResult> results = service.allocate(trips, fee, strategy);

        // floor = 33 each → sum = 99 → one trip gets +1
        long total = results.stream().mapToLong(AllocationResult::getAllocatedPaisa).sum();
        assertEquals(fee, total, "Total must equal fee exactly");
    }

    // ── Test 3: Largest Remainder chooses correct recipient ──────────────────
    @Test
    void largestRemainder_correctDistribution() {
        // Fee = 10 paisa, weights 1, 2, 3 (total 6)
        // Exact: 1.666.., 3.333.., 5.0
        // Floor: 1, 3, 5 → sum = 9 → 1 leftover
        // Remainders: 0.666, 0.333, 0.0 → trip 1 gets +1
        List<Trip> trips = List.of(trip(1, 1), trip(2, 2), trip(3, 3));
        long fee = 10L;

        List<AllocationResult> results = service.allocate(trips, fee, strategy);

        assertEquals(2L, results.get(0).getAllocatedPaisa()); // 1 + 1 (largest remainder)
        assertEquals(3L, results.get(1).getAllocatedPaisa());
        assertEquals(5L, results.get(2).getAllocatedPaisa());
        assertReconciled(results, fee);
    }

    // ── Test 4: zero weight → BusinessException ───────────────────────────────
    @Test
    void zeroWeight_throwsBusinessException() {
        List<Trip> trips = List.of(trip(1, 0), trip(2, 0), trip(3, 0));
        assertThrows(BusinessException.class, () -> service.allocate(trips, 3_000_000L, strategy));
    }

    // ── Test 5: single trip receives 100% ─────────────────────────────────────
    @Test
    void singleTrip_receivesFullFee() {
        List<Trip> trips = List.of(trip(1, 500));
        long fee = 3_000_000L;

        List<AllocationResult> results = service.allocate(trips, fee, strategy);

        assertEquals(1, results.size());
        assertEquals(fee, results.get(0).getAllocatedPaisa());
    }

    // ── Test 6: zero fee ──────────────────────────────────────────────────────
    @Test
    void zeroFee_everyTripReceivesZero() {
        List<Trip> trips = List.of(trip(1, 100), trip(2, 200));
        List<AllocationResult> results = service.allocate(trips, 0L, strategy);
        results.forEach(r -> assertEquals(0L, r.getAllocatedPaisa()));
        assertReconciled(results, 0L);
    }

    // ── Test 7: empty trip list → BusinessException ───────────────────────────
    @Test
    void emptyTripList_throwsBusinessException() {
        assertThrows(BusinessException.class, () -> service.allocate(List.of(), 1_000L, strategy));
    }

    // ── Test 8: large fee with many trips — reconciliation holds ──────────────
    @Test
    void largeFee_manyTrips_alwaysReconciled() {
        // 7 trips with different distances, prime-number fee to force complex remainders
        List<Trip> trips = List.of(
                trip(1, 10), trip(2, 23), trip(3, 47), trip(4, 7),
                trip(5, 100), trip(6, 3), trip(7, 60));
        long fee = 9_999_997L; // prime-ish, forces non-trivial remainders

        List<AllocationResult> results = service.allocate(trips, fee, strategy);

        assertReconciled(results, fee);
    }

    private void assertReconciled(List<AllocationResult> results, long expectedFee) {
        long sum = results.stream().mapToLong(AllocationResult::getAllocatedPaisa).sum();
        assertEquals(expectedFee, sum,
                "SUM(allocated paisa) must equal fixedFeePaisa exactly. Got: " + sum + " expected: " + expectedFee);
    }
}

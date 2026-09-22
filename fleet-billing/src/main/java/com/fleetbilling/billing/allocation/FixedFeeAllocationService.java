package com.fleetbilling.billing.allocation;

import com.fleetbilling.entity.Trip;
import com.fleetbilling.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements the Largest Remainder Method (LRM) for distributing a fixed
 * monthly fee in integer paisa across a list of trips without any rounding loss.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Compute each trip's exact proportional share: {@code weight / totalWeight * fixedFeePaisa}</li>
 *   <li>Take the floor of each share → floor allocation.</li>
 *   <li>Compute each trip's fractional remainder.</li>
 *   <li>Sum all floor allocations → {@code floorSum}.</li>
 *   <li>Compute {@code remainder = fixedFeePaisa - floorSum}.</li>
 *   <li>Sort trips descending by fractional remainder.</li>
 *   <li>Give +1 paisa to the first {@code remainder} trips.</li>
 * </ol>
 *
 * <h2>Guarantee</h2>
 * {@code SUM(allocatedPaisa) == fixedFeePaisa} — always, regardless of weights.
 *
 * <h2>Money rules</h2>
 * All intermediate calculations use {@link BigDecimal} with sufficient precision.
 * The final allocated value is cast to {@code long} (paisa). No {@code double}/{@code float}.
 */
@Service
public class FixedFeeAllocationService {

    private static final MathContext MC = MathContext.DECIMAL128; // 34 significant digits

    /**
     * Allocate the fixed monthly fee across eligible trips using the given strategy.
     *
     * @param trips         eligible COMPLETED trips for the billing month
     * @param fixedFeePaisa the monthly fixed fee in paisa (must be >= 0)
     * @param strategy      weight function for each trip
     * @return ordered list of AllocationResult, one per trip
     * @throws BusinessException if total weight is zero
     */
    public List<AllocationResult> allocate(
            List<Trip> trips,
            long fixedFeePaisa,
            AllocationStrategy strategy) {

        if (trips == null || trips.isEmpty()) {
            throw new BusinessException(
                "Cannot allocate fixed monthly fee: no eligible trips found for this billing period.");
        }

        // ── Step 1: compute individual weights ──────────────────────────────
        Map<Long, BigDecimal> weights = new LinkedHashMap<>();
        for (Trip trip : trips) {
            weights.put(trip.getId(), strategy.weightFor(trip));
        }

        BigDecimal totalWeight = weights.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(
                "Cannot allocate fixed monthly fee because total trip allocation weight is zero. " +
                "All eligible trips have " + strategy.strategyName() + " = 0.");
        }

        // ── Step 2: exact proportional shares and floor allocations ─────────
        BigDecimal fee = BigDecimal.valueOf(fixedFeePaisa);
        record TripShare(Long tripId, long floor, BigDecimal remainder) {}

        List<TripShare> shares = new ArrayList<>();
        long floorSum = 0L;

        for (Trip trip : trips) {
            BigDecimal weight    = weights.get(trip.getId());
            BigDecimal exact     = weight.multiply(fee, MC).divide(totalWeight, MC);
            long       floor     = exact.longValue();          // floor via truncation
            BigDecimal remainder = exact.subtract(BigDecimal.valueOf(floor));
            shares.add(new TripShare(trip.getId(), floor, remainder));
            floorSum += floor;
        }

        // ── Step 3: distribute leftover paisa by largest remainder ───────────
        long leftover = fixedFeePaisa - floorSum;  // always >= 0 due to floor
        // Sort descending by remainder; stable tie-breaking by trip position keeps order deterministic
        List<TripShare> sorted = shares.stream()
                .sorted(Comparator.comparing(TripShare::remainder).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        Set<Long> extraPaisaIds = new HashSet<>();
        for (int i = 0; i < leftover; i++) {
            extraPaisaIds.add(sorted.get(i).tripId());
        }

        // ── Step 4: build results in original trip order ─────────────────────
        List<AllocationResult> results = new ArrayList<>();
        for (Trip trip : trips) {
            long floor    = shares.stream().filter(s -> s.tripId().equals(trip.getId()))
                                .findFirst().map(TripShare::floor).orElse(0L);
            long allocated = floor + (extraPaisaIds.contains(trip.getId()) ? 1L : 0L);

            results.add(AllocationResult.builder()
                    .tripId(trip.getId())
                    .externalTripId(trip.getExternalTripId())
                    .weight(weights.get(trip.getId()))
                    .totalWeight(totalWeight)
                    .fixedFeePaisa(fixedFeePaisa)
                    .allocatedPaisa(allocated)
                    .strategyName(strategy.strategyName())
                    .build());
        }

        // ── Step 5: reconciliation assertion ─────────────────────────────────
        long allocatedSum = results.stream().mapToLong(AllocationResult::getAllocatedPaisa).sum();
        if (allocatedSum != fixedFeePaisa) {
            throw new BusinessException(String.format(
                "Allocation reconciliation failed: allocated %d paisa but fee is %d paisa",
                allocatedSum, fixedFeePaisa));
        }

        return results;
    }
}

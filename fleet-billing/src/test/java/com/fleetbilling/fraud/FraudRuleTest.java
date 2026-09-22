package com.fleetbilling.fraud;

import com.fleetbilling.config.FraudConfig;
import com.fleetbilling.entity.FraudAlert;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.enums.FraudAlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FraudRuleTest {

    private FraudConfig config;

    @BeforeEach
    void setUp() {
        config = new FraudConfig();
        config.setEnabled(true);
        config.setMaxTripDistanceKm(1000);
        config.setMaxTripDurationMinutes(1440);
        config.setMaxAverageSpeedKmph(150);
    }

    private Trip trip(String extId, double km, double dutyHours) {
        return Trip.builder()
                .id(1L)
                .externalTripId(extId)
                .distanceKm(km > 0 ? new BigDecimal(String.valueOf(km)) : BigDecimal.ZERO)
                .dutyHours(dutyHours > 0 ? new BigDecimal(String.valueOf(dutyHours)) : null)
                .build();
    }

    // ── ImpossibleDistanceRule ─────────────────────────────────────────────────

    @Test
    void impossibleDistance_exceedsMax_raisesAlert() {
        ImpossibleDistanceRule rule = new ImpossibleDistanceRule(config);
        Trip trip = trip("TRIP-001", 1500, 10);

        Optional<FraudAlert> alert = rule.evaluate(trip);

        assertTrue(alert.isPresent());
        assertEquals(FraudAlertType.IMPOSSIBLE_DISTANCE, alert.get().getAlertType());
    }

    @Test
    void impossibleDistance_withinMax_noAlert() {
        ImpossibleDistanceRule rule = new ImpossibleDistanceRule(config);
        Trip trip = trip("TRIP-002", 200, 3);

        assertTrue(rule.evaluate(trip).isEmpty());
    }

    @Test
    void impossibleDistance_exactMax_noAlert() {
        ImpossibleDistanceRule rule = new ImpossibleDistanceRule(config);
        Trip trip = trip("TRIP-003", 1000, 10);

        assertTrue(rule.evaluate(trip).isEmpty());
    }

    // ── ImpossibleDurationRule ─────────────────────────────────────────────────

    @Test
    void impossibleDuration_exceedsMax_raisesAlert() {
        ImpossibleDurationRule rule = new ImpossibleDurationRule(config);
        // 2000 minutes = 33.33 hours
        Trip trip = trip("TRIP-004", 100, 33.34);

        Optional<FraudAlert> alert = rule.evaluate(trip);

        assertTrue(alert.isPresent());
        assertEquals(FraudAlertType.IMPOSSIBLE_DURATION, alert.get().getAlertType());
    }

    @Test
    void impossibleDuration_withinMax_noAlert() {
        ImpossibleDurationRule rule = new ImpossibleDurationRule(config);
        // 23 hours = 1380 minutes < 1440
        Trip trip = trip("TRIP-005", 100, 23);

        assertTrue(rule.evaluate(trip).isEmpty());
    }

    @Test
    void impossibleDuration_nullDutyHours_noAlert() {
        ImpossibleDurationRule rule = new ImpossibleDurationRule(config);
        Trip tripNoHours = Trip.builder().id(2L).externalTripId("TRIP-006")
                .distanceKm(new BigDecimal("100")).dutyHours(null).build();

        assertTrue(rule.evaluate(tripNoHours).isEmpty());
    }

    // ── AverageSpeedRule ───────────────────────────────────────────────────────

    @Test
    void averageSpeed_implausiblyFast_raisesAlert() {
        AverageSpeedRule rule = new AverageSpeedRule(config);
        // 500 km in 1 hour = 500 km/h >> 150 km/h max
        Trip trip = trip("TRIP-007", 500, 1);

        Optional<FraudAlert> alert = rule.evaluate(trip);

        assertTrue(alert.isPresent());
        assertEquals(FraudAlertType.SUSPICIOUS_TRIP, alert.get().getAlertType());
    }

    @Test
    void averageSpeed_plausible_noAlert() {
        AverageSpeedRule rule = new AverageSpeedRule(config);
        // 100 km in 2 hours = 50 km/h — well within limit
        Trip trip = trip("TRIP-008", 100, 2);

        assertTrue(rule.evaluate(trip).isEmpty());
    }

    @Test
    void averageSpeed_zeroDuration_noAlert() {
        AverageSpeedRule rule = new AverageSpeedRule(config);
        // If dutyHours == 0, skip to avoid division by zero
        Trip trip = Trip.builder().id(3L).externalTripId("TRIP-009")
                .distanceKm(new BigDecimal("100"))
                .dutyHours(BigDecimal.ZERO).build();

        assertTrue(rule.evaluate(trip).isEmpty());
    }

    @Test
    void normalTrip_noAlerts() {
        ImpossibleDistanceRule distanceRule = new ImpossibleDistanceRule(config);
        ImpossibleDurationRule durationRule = new ImpossibleDurationRule(config);
        AverageSpeedRule speedRule         = new AverageSpeedRule(config);

        // Normal 40 km trip in 1 hour — perfectly plausible
        Trip trip = trip("TRIP-010", 40, 1);

        assertTrue(distanceRule.evaluate(trip).isEmpty());
        assertTrue(durationRule.evaluate(trip).isEmpty());
        assertTrue(speedRule.evaluate(trip).isEmpty());
    }
}

package com.fleetbilling.fraud;

import com.fleetbilling.config.FraudConfig;
import com.fleetbilling.entity.FraudAlert;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.enums.FraudAlertStatus;
import com.fleetbilling.enums.FraudAlertType;
import com.fleetbilling.enums.FraudSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

/**
 * Flags trips where the implied average speed is physically implausible.
 *
 * averageSpeed = distanceKm / (dutyHours)
 *
 * Uses BigDecimal for the division to avoid floating-point imprecision propagating
 * into fraud decisions. Final comparison is done against an int threshold.
 * Severity: MEDIUM
 */
@Component
@RequiredArgsConstructor
public class AverageSpeedRule implements FraudRule {

    private final FraudConfig fraudConfig;

    @Override
    public Optional<FraudAlert> evaluate(Trip trip) {
        if (trip.getDistanceKm() == null || trip.getDutyHours() == null) return Optional.empty();
        if (trip.getDutyHours().compareTo(BigDecimal.ZERO) == 0) return Optional.empty();

        // averageSpeedKmph = distanceKm / dutyHours  (dutyHours already in hours)
        BigDecimal avgSpeed = trip.getDistanceKm()
                .divide(trip.getDutyHours(), MathContext.DECIMAL64);

        int maxSpeed = fraudConfig.getMaxAverageSpeedKmph();
        if (avgSpeed.compareTo(BigDecimal.valueOf(maxSpeed)) > 0) {
            return Optional.of(FraudAlert.builder()
                    .trip(trip)
                    .alertType(FraudAlertType.SUSPICIOUS_TRIP)
                    .severity(FraudSeverity.MEDIUM)
                    .status(FraudAlertStatus.OPEN)
                    .resolved(false)
                    .message(String.format(
                            "Trip %s has an implausible average speed of %.1f km/h " +
                            "(distance=%.2f km, duration=%.2f hours, max allowed=%d km/h).",
                            trip.getExternalTripId(), avgSpeed.doubleValue(),
                            trip.getDistanceKm().doubleValue(),
                            trip.getDutyHours().doubleValue(), maxSpeed))
                    .build());
        }
        return Optional.empty();
    }
}

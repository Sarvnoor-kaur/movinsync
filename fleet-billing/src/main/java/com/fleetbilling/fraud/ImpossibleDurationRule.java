package com.fleetbilling.fraud;

import com.fleetbilling.config.FraudConfig;
import com.fleetbilling.entity.FraudAlert;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.enums.FraudAlertStatus;
import com.fleetbilling.enums.FraudAlertType;
import com.fleetbilling.enums.FraudSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Flags trips whose duty hours exceed the configured maximum plausible duration.
 * Uses dutyHours (BigDecimal) converted to minutes for comparison.
 * Severity: MEDIUM
 */
@Component
@RequiredArgsConstructor
public class ImpossibleDurationRule implements FraudRule {

    private final FraudConfig fraudConfig;

    @Override
    public Optional<FraudAlert> evaluate(Trip trip) {
        if (trip.getDutyHours() == null) return Optional.empty();
        // Convert dutyHours to minutes for comparison
        double durationMinutes = trip.getDutyHours().doubleValue() * 60.0;
        int maxMinutes = fraudConfig.getMaxTripDurationMinutes();

        if (durationMinutes > maxMinutes) {
            return Optional.of(FraudAlert.builder()
                    .trip(trip)
                    .alertType(FraudAlertType.IMPOSSIBLE_DURATION)
                    .severity(FraudSeverity.MEDIUM)
                    .status(FraudAlertStatus.OPEN)
                    .resolved(false)
                    .message(String.format(
                            "Trip %s has duration %.1f minutes which exceeds the maximum allowed %d minutes.",
                            trip.getExternalTripId(), durationMinutes, maxMinutes))
                    .build());
        }
        return Optional.empty();
    }
}

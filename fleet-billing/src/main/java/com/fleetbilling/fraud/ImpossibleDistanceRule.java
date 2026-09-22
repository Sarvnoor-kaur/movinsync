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
 * Flags trips whose distance exceeds the configured maximum plausible distance.
 * Severity: HIGH
 */
@Component
@RequiredArgsConstructor
public class ImpossibleDistanceRule implements FraudRule {

    private final FraudConfig fraudConfig;

    @Override
    public Optional<FraudAlert> evaluate(Trip trip) {
        if (trip.getDistanceKm() == null) return Optional.empty();
        double distance = trip.getDistanceKm().doubleValue();
        int maxKm = fraudConfig.getMaxTripDistanceKm();

        if (distance > maxKm) {
            return Optional.of(FraudAlert.builder()
                    .trip(trip)
                    .alertType(FraudAlertType.IMPOSSIBLE_DISTANCE)
                    .severity(FraudSeverity.HIGH)
                    .status(FraudAlertStatus.OPEN)
                    .resolved(false)
                    .message(String.format(
                            "Trip %s has distance %.2f km which exceeds the maximum allowed %d km.",
                            trip.getExternalTripId(), distance, maxKm))
                    .build());
        }
        return Optional.empty();
    }
}

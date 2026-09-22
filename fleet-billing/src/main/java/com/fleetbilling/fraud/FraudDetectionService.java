package com.fleetbilling.fraud;

import com.fleetbilling.config.FraudConfig;
import com.fleetbilling.entity.FraudAlert;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.enums.FraudAlertStatus;
import com.fleetbilling.enums.FraudAlertType;
import com.fleetbilling.repository.FraudAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates all fraud detection rules against a trip.
 *
 * Rules are evaluated in sequence. Each rule is independent.
 * An alert is only created if no active alert of the same type already exists for the trip,
 * preventing duplicate alerts from repeated evaluations.
 *
 * The trip is never rejected here — fraud alerts are informational and advisory.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final FraudConfig fraudConfig;
    private final FraudAlertRepository fraudAlertRepository;
    private final ImpossibleDistanceRule impossibleDistanceRule;
    private final ImpossibleDurationRule impossibleDurationRule;
    private final AverageSpeedRule averageSpeedRule;

    /**
     * Evaluate all fraud rules against the given trip and persist any new alerts.
     * Duplicate alert detection: if an OPEN alert of the same type already exists, skip.
     *
     * @param trip the saved trip to evaluate
     * @return list of newly created FraudAlert entities
     */
    @Transactional
    public List<FraudAlert> evaluateTrip(Trip trip) {
        if (!fraudConfig.isEnabled()) {
            log.debug("Fraud detection is disabled. Skipping evaluation for trip {}", trip.getId());
            return List.of();
        }

        List<FraudRule> rules = List.of(impossibleDistanceRule, impossibleDurationRule, averageSpeedRule);
        List<FraudAlert> created = new ArrayList<>();

        for (FraudRule rule : rules) {
            Optional<FraudAlert> alertOpt = rule.evaluate(trip);
            if (alertOpt.isEmpty()) continue;

            FraudAlert candidate = alertOpt.get();

            // Duplicate prevention: don't create a second OPEN alert of the same type for this trip
            boolean alreadyOpen = fraudAlertRepository.findByTripId(trip.getId()).stream()
                    .anyMatch(existing ->
                            existing.getAlertType() == candidate.getAlertType() &&
                            existing.getStatus() == FraudAlertStatus.OPEN);

            if (alreadyOpen) {
                log.debug("Skipping duplicate {} alert for trip {}", candidate.getAlertType(), trip.getId());
                continue;
            }

            FraudAlert saved = fraudAlertRepository.save(candidate);
            created.add(saved);
            log.warn("Fraud alert created: {} [{}] for trip {} — {}",
                    saved.getAlertType(), saved.getSeverity(),
                    trip.getExternalTripId(), saved.getMessage());
        }
        return created;
    }
}

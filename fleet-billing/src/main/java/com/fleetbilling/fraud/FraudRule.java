package com.fleetbilling.fraud;

import com.fleetbilling.entity.FraudAlert;
import com.fleetbilling.entity.Trip;

import java.util.Optional;

/**
 * Strategy interface for a single fraud detection rule.
 *
 * Each rule evaluates exactly one anomaly type and returns an unsaved FraudAlert
 * if the rule fires, or empty if the trip is clean.
 *
 * The FraudDetectionService collects alerts from all rules and persists them.
 */
public interface FraudRule {

    /**
     * Evaluate the rule against the given trip.
     *
     * @param trip the trip to evaluate
     * @return an unsaved FraudAlert if anomalous, or Optional.empty() if clean
     */
    Optional<FraudAlert> evaluate(Trip trip);
}

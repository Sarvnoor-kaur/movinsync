package com.fleetbilling.enums;

/**
 * Categories of anomaly or fraud that can be detected in the billing system.
 *
 * <p>These are stored in FraudAlert records. Detection logic will be
 * implemented in a later phase.
 *
 * <ul>
 *   <li>DUPLICATE_TRIP       — same externalTripId submitted more than once</li>
 *   <li>MISSING_RIDE         — a trip was expected (scheduled) but not recorded</li>
 *   <li>IMPOSSIBLE_DISTANCE  — trip distance is physically impossible in the given time
 *                              (e.g., 2000 km in 1 hour)</li>
 *   <li>DUPLICATE_BILLING    — a BillingRun attempted to bill an already-billed period</li>
 * </ul>
 */
public enum FraudAlertType {
    DUPLICATE_TRIP,
    MISSING_RIDE,
    IMPOSSIBLE_DISTANCE,
    DUPLICATE_BILLING
}

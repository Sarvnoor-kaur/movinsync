package com.fleetbilling.enums;

/**
 * Status of a Trip record in the system.
 *
 * <ul>
 *   <li>COMPLETED — trip was successfully completed and can be billed</li>
 *   <li>CANCELLED — trip was cancelled; should NOT be billed</li>
 *   <li>MISSING   — trip was expected but not recorded (anomaly detection);
 *                   may trigger a FraudAlert of type MISSING_RIDE</li>
 * </ul>
 */
public enum TripStatus {
    COMPLETED,
    CANCELLED,
    MISSING,
    NO_SHOW
}

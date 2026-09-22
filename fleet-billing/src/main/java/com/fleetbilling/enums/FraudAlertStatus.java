package com.fleetbilling.enums;

/**
 * Lifecycle status of a FraudAlert review process.
 */
public enum FraudAlertStatus {
    /** Alert has been raised and not yet reviewed. */
    OPEN,
    /** An operator is currently reviewing. */
    REVIEWED,
    /** Alert confirmed as real fraud — corrective action taken. */
    RESOLVED,
    /** Alert was investigated and found to be a false positive. */
    DISMISSED
}

package com.fleetbilling.enums;

/**
 * Lifecycle states of a BillingRun.
 *
 * <p>A BillingRun is the process that calculates and generates an Invoice
 * for one vehicle for one billing month.
 *
 * <p>State transitions:
 * <pre>
 *   STARTED → PROCESSING → COMPLETED
 *                        → FAILED
 * </pre>
 *
 * <ul>
 *   <li>STARTED    — billing run has been initiated</li>
 *   <li>PROCESSING — billing engine is actively calculating</li>
 *   <li>COMPLETED  — billing run finished successfully; Invoice generated</li>
 *   <li>FAILED     — billing run encountered an error; see errorMessage field</li>
 * </ul>
 *
 * <p>The unique constraint on (vehicle_id, billing_month) ensures that only ONE
 * BillingRun exists per vehicle per month, guaranteeing idempotency.
 */
public enum BillingRunStatus {
    STARTED,
    PROCESSING,
    COMPLETED,
    FAILED
}

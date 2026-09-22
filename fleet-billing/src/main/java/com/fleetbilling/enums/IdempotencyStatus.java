package com.fleetbilling.enums;

/**
 * Lifecycle status for the idempotency record of a write operation.
 */
public enum IdempotencyStatus {
    /** The operation is currently being executed. */
    PROCESSING,
    /** The operation completed successfully — cached response is ready. */
    COMPLETED,
    /** The operation failed — cached error response is stored. */
    FAILED
}

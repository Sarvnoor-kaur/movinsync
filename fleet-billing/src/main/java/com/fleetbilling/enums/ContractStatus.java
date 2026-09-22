package com.fleetbilling.enums;

/**
 * Lifecycle states of a rental Contract.
 *
 * <p>State transitions:
 * <pre>
 *   DRAFT → ACTIVE → EXPIRED
 *                 → TERMINATED (early termination)
 * </pre>
 *
 * <ul>
 *   <li>DRAFT      — contract created but not yet active</li>
 *   <li>ACTIVE     — contract is currently in force</li>
 *   <li>EXPIRED    — contract reached its end date naturally</li>
 *   <li>TERMINATED — contract was ended before its end date</li>
 * </ul>
 */
public enum ContractStatus {
    DRAFT,
    ACTIVE,
    EXPIRED,
    TERMINATED
}

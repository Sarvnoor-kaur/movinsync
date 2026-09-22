package com.fleetbilling.enums;

/**
 * Severity level of a FraudAlert.
 *
 * <p>Used to prioritise which alerts should be reviewed first.
 *
 * <ul>
 *   <li>LOW      — informational; unlikely to affect billing significantly</li>
 *   <li>MEDIUM   — should be reviewed; may have moderate billing impact</li>
 *   <li>HIGH     — requires prompt attention; significant billing risk</li>
 *   <li>CRITICAL — must be resolved before billing is finalised</li>
 * </ul>
 */
public enum FraudSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

package com.fleetbilling.enums;

/**
 * The billing model used by a ContractVersion.
 *
 * <p>This determines how the billing engine calculates charges for trips
 * covered by that contract version. Different versions of the same contract
 * can have different billing types (e.g., the rate changes mid-month).
 *
 * <ul>
 *   <li>PER_KM        — charge is calculated per kilometre driven</li>
 *   <li>PER_TRIP      — a fixed charge is applied per completed trip</li>
 *   <li>FIXED_MONTHLY — a fixed monthly fee is agreed; it will later be
 *                       proportionally distributed across all trips in the month</li>
 * </ul>
 *
 * <p>Extra charges (night, waiting, toll) apply on top of any billing type.
 */
public enum BillingType {
    PER_KM,
    PER_TRIP,
    FIXED_MONTHLY
}

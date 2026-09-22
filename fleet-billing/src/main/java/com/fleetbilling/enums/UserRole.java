package com.fleetbilling.enums;

/**
 * Roles that a User can hold in the Fleet Billing system.
 *
 * <p>Stored as a STRING in the database (@Enumerated(EnumType.STRING)) so that
 * adding a new role in the future does NOT change existing rows.
 * Never use EnumType.ORDINAL — it breaks when enum order changes.
 *
 * <ul>
 *   <li>ADMIN          — full access: manage vendors, contracts, users, billing</li>
 *   <li>BILLING_MANAGER — can trigger billing runs and view invoices</li>
 *   <li>VIEWER         — read-only access to reports and dashboards</li>
 * </ul>
 */
public enum UserRole {
    ADMIN,
    BILLING_MANAGER,
    VIEWER
}

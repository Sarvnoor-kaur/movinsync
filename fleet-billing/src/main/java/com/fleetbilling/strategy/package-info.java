package com.fleetbilling.strategy;

/**
 * Strategy package placeholder — Phase 1.
 *
 * <p>The billing engine will use the Strategy design pattern to support
 * multiple billing models without giant if-else chains:
 *
 * <ul>
 *   <li>PerKmBillingStrategy    — charges based on kilometers driven</li>
 *   <li>PerTripBillingStrategy  — fixed charge per trip</li>
 *   <li>FixedMonthlyStrategy    — fixed monthly fee distributed across trips</li>
 * </ul>
 *
 * <p>All strategies will implement a common BillingStrategy interface.
 * Implementations will be added in the billing engine phase.
 */

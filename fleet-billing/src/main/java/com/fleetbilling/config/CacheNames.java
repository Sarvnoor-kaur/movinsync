package com.fleetbilling.config;

/**
 * Centralized Redis Cache Name constants.
 */
public final class CacheNames {

    private CacheNames() {
        // Utility class
    }

    public static final String CONTRACTS = "contracts";
    public static final String CONTRACT_VERSIONS = "contractVersions";
    public static final String PRICING_SLABS = "pricingSlabs";
    public static final String VEHICLES = "vehicles";
}

package com.fleetbilling.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externally-configurable thresholds for fraud/anomaly detection rules.
 * All values can be overridden via environment variables or application.properties.
 *
 * Example application.properties:
 *   fraud.enabled=true
 *   fraud.max-trip-distance-km=1000
 *   fraud.max-trip-duration-minutes=1440
 *   fraud.max-average-speed-kmph=150
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fraud")
public class FraudConfig {

    /** Master switch — set to false to disable all fraud checks. */
    private boolean enabled = true;

    /** Maximum plausible trip distance in kilometres. */
    private int maxTripDistanceKm = 1000;

    /** Maximum plausible trip duration in minutes (default 24 hours = 1440 min). */
    private int maxTripDurationMinutes = 1440;

    /** Maximum plausible average speed in km/h for consistency check. */
    private int maxAverageSpeedKmph = 150;
}

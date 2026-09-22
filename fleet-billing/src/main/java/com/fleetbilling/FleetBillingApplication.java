package com.fleetbilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Fleet Billing application.
 *
 * <p>@SpringBootApplication is a convenience annotation that combines:
 * <ul>
 *   <li>@Configuration   — marks this class as a source of bean definitions</li>
 *   <li>@EnableAutoConfiguration — lets Spring Boot auto-configure beans based on the classpath</li>
 *   <li>@ComponentScan   — scans com.fleetbilling and all sub-packages for Spring components</li>
 * </ul>
 *
 * <p>Phase 1: Project setup only. Business logic will be added in later phases.
 */
@SpringBootApplication
public class FleetBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetBillingApplication.class, args);
    }
}

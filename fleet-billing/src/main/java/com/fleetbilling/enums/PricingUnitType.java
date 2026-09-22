package com.fleetbilling.enums;

/**
 * The unit of measurement that a PricingSlab rate applies to.
 *
 * <p>Used in conjunction with PricingSlab.fromValue and PricingSlab.toValue
 * to define tiered pricing brackets.
 *
 * <p>Example slabs using KM:
 * <pre>
 *   Slab 1: 0   – 100 km  → ₹50/km
 *   Slab 2: 101 – 200 km  → ₹45/km
 *   Slab 3: 201 – null    → ₹40/km   (open-ended final slab)
 * </pre>
 *
 * <ul>
 *   <li>KM   — slabs based on distance in kilometres</li>
 *   <li>HOUR — slabs based on duty hours</li>
 *   <li>TRIP — slabs based on number of trips in the period</li>
 * </ul>
 */
public enum PricingUnitType {
    KM,
    HOUR,
    TRIP
}

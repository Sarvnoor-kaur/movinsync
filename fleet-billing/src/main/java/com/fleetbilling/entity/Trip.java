package com.fleetbilling.entity;

import com.fleetbilling.enums.TripStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a single vehicle trip that has been recorded in the system.
 *
 * <p>Trips are the atomic unit of billing. Every charge in an invoice must
 * be traceable to one or more trips.
 *
 * <p>Relationships:
 * <pre>
 *   Vehicle 1 ---- N Trip
 *   Trip    1 ---- N InvoiceItem   (what was charged for this trip)
 *   Trip    1 ---- N FraudAlert    (anomalies detected on this trip)
 * </pre>
 *
 * <p>DUPLICATE PREVENTION:
 * externalTripId is a unique identifier from the source system (GPS tracker,
 * booking platform, etc.). The UNIQUE constraint on this field prevents the
 * same trip from being ingested twice, regardless of how many times the
 * source system retries sending it.
 *
 * <p>DATE/TIME CHOICES:
 * - tripDate: LocalDate — the calendar date of the trip (for billing period grouping)
 * - startTime/endTime: LocalTime — time-of-day for night-charge calculation
 * - createdAt/updatedAt: LocalDateTime — full timestamp for audit trail
 *
 * <p>DISTANCE AND HOURS:
 * Why BigDecimal for distanceKm and dutyHours?
 * These are NOT monetary values, but they need decimal precision.
 * Example: a trip of 12.7 km or 2.5 hours cannot be stored in an Integer.
 * BigDecimal is used (not double/float) to avoid floating-point rounding issues
 * that could propagate into monetary calculations downstream.
 * MySQL column type: DECIMAL(10,2) — up to 99,999,999.99 km
 *
 * <p>MONETARY FIELDS:
 * tollAmountPaisa: Long — toll is a pass-through amount charged to the client.
 * Stored in paisa. NEVER use double/float for money.
 *
 * <p>Index on (vehicle_id, trip_date) is critical for billing:
 * "get all trips for vehicle V in month M" is the most frequent query.
 */
@Entity
@Table(
        name = "trips",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_trip_external_id", columnNames = "external_trip_id")
        },
        indexes = {
                @Index(name = "idx_trip_vehicle_date", columnList = "vehicle_id, trip_date"),
                @Index(name = "idx_trip_status",       columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier from the external source system (GPS, booking platform).
     * Acts as an idempotency key — duplicate trip submissions are rejected at the DB level.
     */
    @NotBlank
    @Column(nullable = false, name = "external_trip_id", length = 100)
    private String externalTripId;

    /**
     * The vehicle that performed this trip.
     * FetchType.LAZY — billing queries load thousands of trips; loading the full
     * Vehicle for each would be extremely expensive.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_trip_vehicle"))
    private Vehicle vehicle;

    /**
     * Calendar date of the trip. Used to:
     * 1. Determine the billing month this trip falls in.
     * 2. Find the applicable ContractVersion (effectiveFrom ≤ tripDate ≤ effectiveTo).
     */
    @NotNull
    @Column(nullable = false, name = "trip_date")
    private LocalDate tripDate;

    /**
     * Trip start time (time of day, no date).
     * Used to determine whether the trip qualifies for a night charge.
     * Night hours are typically defined in the ContractVersion (Phase 3).
     */
    @Column(name = "start_time")
    private LocalTime startTime;

    /**
     * Trip end time (time of day, no date).
     */
    @Column(name = "end_time")
    private LocalTime endTime;

    /**
     * Pickup location name/address. Stored for reporting and fraud detection.
     */
    @Column(name = "start_location", length = 300)
    private String startLocation;

    /**
     * Drop location name/address.
     */
    @Column(name = "end_location", length = 300)
    private String endLocation;

    /**
     * Distance of the trip in kilometres, with up to 2 decimal places.
     *
     * <p>Why BigDecimal, not Integer?
     * GPS-recorded distances like 12.73 km cannot be represented in Integer.
     * Why not double/float? These have rounding errors that would corrupt
     * per-km calculations (e.g., 12.73 * 1800 paisa/km = 22914 paisa exactly,
     * but floating-point may give 22913.999... which rounds to 22913).
     * BigDecimal with scale=2 is exact.
     *
     * <p>MySQL type: DECIMAL(10, 2) — supports up to 99,999,999.99 km.
     */
    @PositiveOrZero
    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    /**
     * Total duty hours of the vehicle on this trip (including waiting time).
     * Used for hour-based billing and waiting-hour calculation.
     * MySQL type: DECIMAL(8, 2) — supports up to 999,999.99 hours.
     */
    @PositiveOrZero
    @Column(name = "duty_hours", precision = 8, scale = 2)
    private BigDecimal dutyHours;

    /**
     * Whether this trip qualifies for a night surcharge.
     * Set by the ingestion layer based on startTime/endTime and company policy.
     * The actual charge amount comes from ContractVersion.nightChargePaisa.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean night = false;

    /**
     * Hours the vehicle was kept waiting (e.g., at pickup point).
     * Billed using ContractVersion.waitingChargePerHourPaisa.
     * MySQL type: DECIMAL(6, 2).
     */
    @PositiveOrZero
    @Column(name = "waiting_hours", precision = 6, scale = 2)
    private BigDecimal waitingHours;

    /**
     * Toll amount paid by the driver during this trip, in PAISA.
     *
     * <p>Monetary value — stored as Long (paisa). NEVER use double/float.
     * This is a pass-through charge: the client reimburses the exact toll paid.
     * Example: ₹150 toll = 15000 paisa.
     */
    @PositiveOrZero
    @Column(name = "toll_amount_paisa")
    private Long tollAmountPaisa;

    /**
     * Current status of the trip in the system.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private TripStatus status = TripStatus.COMPLETED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

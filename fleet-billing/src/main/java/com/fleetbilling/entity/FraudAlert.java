package com.fleetbilling.entity;

import com.fleetbilling.enums.FraudAlertType;
import com.fleetbilling.enums.FraudSeverity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Records an anomaly or potential fraud detected during trip ingestion or billing.
 *
 * <p>Fraud detection logic will be implemented in a later phase.
 * This entity is created now so the schema supports it from day one.
 *
 * <p>Examples of what creates a FraudAlert:
 * <ul>
 *   <li>DUPLICATE_TRIP    — externalTripId was submitted twice from the source system</li>
 *   <li>MISSING_RIDE      — a scheduled trip has no corresponding trip record</li>
 *   <li>IMPOSSIBLE_DISTANCE — 500 km trip recorded in 30 minutes</li>
 *   <li>DUPLICATE_BILLING — billing attempted for an already-billed month</li>
 * </ul>
 *
 * <p>NULLABLE REFERENCES:
 * - trip is nullable because some alerts (e.g., DUPLICATE_BILLING) are generated
 *   at the billing run level, not tied to a specific trip.
 * - billingRun is nullable because trip-level alerts (e.g., DUPLICATE_TRIP) can be
 *   raised during trip ingestion, before any billing run exists.
 *
 * <p>Relationships:
 * <pre>
 *   Trip       1 ---- N FraudAlert
 *   BillingRun 1 ---- N FraudAlert
 * </pre>
 *
 * <p>Index on (trip_id) and (billing_run_id) for fast lookups.
 * Index on (resolved) allows operations team to quickly find unresolved alerts.
 */
@Entity
@Table(
        name = "fraud_alerts",
        indexes = {
                @Index(name = "idx_fraud_trip",        columnList = "trip_id"),
                @Index(name = "idx_fraud_billing_run", columnList = "billing_run_id"),
                @Index(name = "idx_fraud_resolved",    columnList = "resolved"),
                @Index(name = "idx_fraud_severity",    columnList = "severity")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The trip that triggered this alert.
     * Nullable — billing-level alerts are not tied to a specific trip.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id",
                foreignKey = @ForeignKey(name = "fk_fraud_trip"))
    private Trip trip;

    /**
     * The billing run that detected this alert.
     * Nullable — trip-ingestion alerts exist before any billing run.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_run_id",
                foreignKey = @ForeignKey(name = "fk_fraud_billing_run"))
    private BillingRun billingRun;

    /**
     * The category of anomaly detected.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "alert_type", length = 30)
    private FraudAlertType alertType;

    /**
     * How serious is this alert? Determines review priority.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FraudSeverity severity;

    /**
     * Human-readable explanation of why this alert was raised.
     * Example: "Trip ABC-123 has the same externalTripId as DEF-456 ingested on 2024-09-10"
     */
    @Column(length = 1000)
    private String message;

    /**
     * Whether an operations user has reviewed and resolved this alert.
     * Unresolved HIGH/CRITICAL alerts should block invoice finalisation (Phase 3).
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean resolved = false;

    /**
     * Automatically set when record is first saved (not updatable).
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when an operations user marked this alert as resolved.
     * Null until resolved = true.
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}

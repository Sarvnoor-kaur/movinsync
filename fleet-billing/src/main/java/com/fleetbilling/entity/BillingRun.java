package com.fleetbilling.entity;

import com.fleetbilling.enums.BillingRunStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents one execution of the billing process for a vehicle in a specific month.
 *
 * <p>A BillingRun orchestrates:
 * 1. Fetching all COMPLETED trips for the vehicle in the billing month.
 * 2. Finding the applicable ContractVersion for each trip date.
 * 3. Calculating charges using the billing engine.
 * 4. Creating an Invoice with itemised InvoiceItems.
 *
 * <p>IDEMPOTENCY GUARANTEE:
 * The UNIQUE constraint on (vehicle_id, billing_month) ensures that the billing
 * system can NEVER accidentally run twice for the same vehicle+month.
 * If a second attempt is made, the database will reject it with a constraint violation.
 * The billing engine (implemented later) will check for an existing BillingRun
 * before creating a new one.
 *
 * <p>BILLING MONTH CONVENTION:
 * billingMonth is always stored as the FIRST DAY of the month.
 * Example: September 2024 → 2024-09-01
 * This allows simple LocalDate-based queries without dealing with month/year separately.
 *
 * <p>Relationships:
 * <pre>
 *   Vehicle    1 ---- N BillingRun
 *   BillingRun 1 ---- 1 Invoice
 *   BillingRun 1 ---- N FraudAlert
 * </pre>
 *
 * <p>Index on (vehicle_id, billing_month) is the primary lookup index.
 * The unique constraint implicitly creates this index in MySQL.
 */
@Entity
@Table(
        name = "billing_runs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_billing_run_vehicle_month",
                                  columnNames = {"vehicle_id", "billing_month"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The vehicle being billed in this run.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_billing_run_vehicle"))
    private Vehicle vehicle;

    /**
     * The billing period, always stored as the first day of the month.
     * Example: September 2024 → 2024-09-01
     *
     * <p>Why LocalDate instead of YearMonth?
     * JPA does not have built-in support for java.time.YearMonth.
     * LocalDate (first-of-month) is a clean convention that works with standard
     * JPA column mappings and BETWEEN queries.
     */
    @NotNull
    @Column(nullable = false, name = "billing_month")
    private LocalDate billingMonth;

    /**
     * Current processing state of this billing run.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private BillingRunStatus status = BillingRunStatus.STARTED;

    /**
     * Timestamp when the billing run was initiated.
     * Distinct from createdAt for clarity in operational monitoring.
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * Timestamp when the billing run finished (success or failure).
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Human-readable error message if the billing run failed.
     * Null for successful runs.
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

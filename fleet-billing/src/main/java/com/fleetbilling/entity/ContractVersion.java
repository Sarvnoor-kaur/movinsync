package com.fleetbilling.entity;

import com.fleetbilling.enums.BillingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores the PRICING for a contract during a specific date range.
 *
 * <p>This is the most important entity for billing correctness.
 *
 * <p>Why a separate table?
 * Contracts can be renegotiated mid-month. If pricing was stored only on the
 * Contract, we'd lose the historical rates the moment the contract was updated.
 * ContractVersion gives us an audit trail and lets the billing engine find
 * the exact rate that applied on the trip date.
 *
 * <p>Example:
 * <pre>
 *   Contract #C-2024-001
 *     Version 1:  Sep 1  → Sep 14  | PER_KM | ₹18/km (1800 paisa)
 *     Version 2:  Sep 15 → Sep 30  | PER_KM | ₹20/km (2000 paisa)
 * </pre>
 *
 * <p>Billing query:
 * <pre>
 *   SELECT cv FROM ContractVersion cv
 *   WHERE cv.contract = :contract
 *     AND :tripDate BETWEEN cv.effectiveFrom AND cv.effectiveTo
 * </pre>
 *
 * <p>All monetary fields are stored in PAISA (1 rupee = 100 paisa).
 * Why paisa? Integer arithmetic is exact; floating-point arithmetic is not.
 * Example: ₹18.50/km = 1850 paisa stored as Long.
 *
 * <p>Unique constraint on (contract_id, version_number) prevents duplicate versions.
 * Index on (contract_id, effective_from, effective_to) makes date-range lookups fast.
 */
@Entity
@Table(
        name = "contract_versions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cv_contract_version",
                                  columnNames = {"contract_id", "version_number"})
        },
        indexes = {
                @Index(name = "idx_cv_contract_dates",
                       columnList = "contract_id, effective_from, effective_to")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent contract this version belongs to.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_cv_contract"))
    private Contract contract;

    /**
     * Sequential version counter within a contract. Starts at 1.
     * Useful for auditing: "when did the rate change?"
     */
    @NotNull
    @Positive
    @Column(nullable = false, name = "version_number")
    private Integer versionNumber;

    /**
     * First date (inclusive) this version's rates apply.
     * LocalDate chosen because contract rates change on calendar days, not times.
     */
    @NotNull
    @Column(nullable = false, name = "effective_from")
    private LocalDate effectiveFrom;

    /**
     * Last date (inclusive) this version's rates apply.
     * Nullable — the latest version of an active contract has no end date yet.
     * When a new version is created, effectiveTo of the previous version is set to
     * (new version's effectiveFrom - 1 day).
     */
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    /**
     * The billing model for this version period.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "billing_type", length = 20)
    private BillingType billingType;

    // ─── Monetary fields ────────────────────────────────────────────────────
    // All monetary values are stored in PAISA (Long).
    // Formula: ₹X = X * 100 paisa
    // Example: ₹30,000 = 3,000,000 paisa
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Fixed monthly fee in paisa. Applicable only when billingType = FIXED_MONTHLY.
     * Will later be distributed proportionally across all trips in the month.
     * Nullable — not applicable for PER_KM and PER_TRIP billing types.
     */
    @Column(name = "monthly_fixed_fee_paisa")
    private Long monthlyFixedFeePaisa;

    /**
     * Free kilometres included in the contract before per-km charges apply.
     * Nullable — not all contracts have a free-km allowance.
     */
    @Column(name = "free_km")
    private Integer freeKm;

    /**
     * Free duty hours included in the contract.
     * Nullable — not all contracts have a free-hours allowance.
     */
    @Column(name = "free_hours")
    private Integer freeHours;

    /**
     * Per-km charge in paisa, applied after free-km allowance is exhausted.
     * Applicable for PER_KM billing type or overage on FIXED_MONTHLY contracts.
     */
    @Column(name = "overage_per_km_paisa")
    private Long overagePerKmPaisa;

    /**
     * Per-hour charge in paisa, applied after free-hours allowance is exhausted.
     */
    @Column(name = "overage_per_hour_paisa")
    private Long overagePerHourPaisa;

    /**
     * Surcharge in paisa for trips that involve night driving.
     * Typically a flat amount added per night trip.
     */
    @Column(name = "night_charge_paisa")
    private Long nightChargePaisa;

    /**
     * Waiting charge in paisa per hour when the vehicle is kept waiting.
     */
    @Column(name = "waiting_charge_per_hour_paisa")
    private Long waitingChargePerHourPaisa;

    /**
     * Flat handling fee in paisa applied per trip when toll charges are incurred.
     * The actual toll amount is stored on the Trip and passed through as-is.
     */
    @Column(name = "toll_handling_charge_paisa")
    private Long tollHandlingChargePaisa;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

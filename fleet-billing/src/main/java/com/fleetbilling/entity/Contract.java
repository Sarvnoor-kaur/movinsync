package com.fleetbilling.entity;

import com.fleetbilling.enums.ContractStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a rental agreement between the company and a vendor for a specific vehicle.
 *
 * <p>A contract is the top-level agreement. Pricing details are NOT stored here —
 * they live in ContractVersion, because pricing can change during the month.
 *
 * <p>Example:
 * <pre>
 *   Contract #C-2024-001 (Vehicle MH12AB1234, Vendor MERU, Sep 2024)
 *     ├── ContractVersion 1 (Sep 1–14) : ₹18/km
 *     └── ContractVersion 2 (Sep 15–30): ₹20/km  ← rate revision
 * </pre>
 *
 * <p>Relationships:
 * <pre>
 *   Vendor  1 ---- N Contract
 *   Vehicle 1 ---- N Contract
 *   Contract 1 ---- N ContractVersion
 * </pre>
 *
 * <p>Index on (vehicle_id, start_date) supports queries like
 * "find the active contract for this vehicle on this date".
 */
@Entity
@Table(
        name = "contracts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_contract_number", columnNames = "contract_number")
        },
        indexes = {
                @Index(name = "idx_contract_vehicle_start", columnList = "vehicle_id, start_date"),
                @Index(name = "idx_contract_vendor",        columnList = "vendor_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable contract reference number.
     * Example: "C-2024-001", "CONTRACT-MERU-SEP24"
     */
    @NotBlank
    @Column(nullable = false, name = "contract_number", length = 100)
    private String contractNumber;

    /**
     * The vendor providing the vehicle(s) under this contract.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_contract_vendor"))
    private Vendor vendor;

    /**
     * The specific vehicle covered by this contract.
     * One contract covers exactly one vehicle.
     * A vehicle can have multiple contracts over time (not simultaneously active).
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_contract_vehicle"))
    private Vehicle vehicle;

    /**
     * The calendar date from which this contract is valid.
     * Why LocalDate: contracts are date-based, not datetime-based.
     * Using LocalDate avoids timezone confusion.
     */
    @NotNull
    @Column(nullable = false, name = "start_date")
    private LocalDate startDate;

    /**
     * The calendar date until which this contract is valid (inclusive).
     * Nullable — an open-ended contract has no end date yet.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Current lifecycle status of this contract.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

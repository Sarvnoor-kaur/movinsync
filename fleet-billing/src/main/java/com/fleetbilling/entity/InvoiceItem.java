package com.fleetbilling.entity;

import com.fleetbilling.enums.BillingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a single line item on an Invoice.
 *
 * <p>Every InvoiceItem must be traceable to a Trip and to the pricing rule
 * that produced it. This provides full billing explainability:
 * "why was this amount charged?"
 *
 * <p>For PER_KM billing:    quantity=12.73, ratePaisa=1800 → amountPaisa=22914
 * For PER_TRIP billing:   quantity=1.00,  ratePaisa=25000 → amountPaisa=25000
 * For FIXED_MONTHLY:      the fixed fee is divided across all trips using
 *                         allocationWeight (Largest Remainder method, Phase 3).
 *
 * <p>TRIP REFERENCE:
 * trip is nullable because some items (e.g., monthly standing charges,
 * or manual adjustments) may not map to a specific trip.
 *
 * <p>MONETARY VALUES:
 * ratePaisa and amountPaisa are Long (paisa). NEVER use double/float.
 *
 * <p>NON-MONETARY NUMERIC VALUES:
 * quantity and allocationWeight use BigDecimal for decimal precision without
 * floating-point rounding errors.
 * - quantity: how many km, hours, or trips
 * - allocationWeight: the proportion of the fixed fee allocated to this trip
 *                     (e.g., 0.0347 = 3.47% of the monthly fee)
 *
 * <p>Index on invoice_id speeds up "get all items for this invoice".
 * Index on trip_id speeds up "which invoice items reference this trip?".
 */
@Entity
@Table(
        name = "invoice_items",
        indexes = {
                @Index(name = "idx_item_invoice", columnList = "invoice_id"),
                @Index(name = "idx_item_trip",    columnList = "trip_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The invoice this line item belongs to.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_item_invoice"))
    private Invoice invoice;

    /**
     * The trip that this charge relates to.
     * Nullable — some charges (monthly standing fees, adjustments) have no trip.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id",
                foreignKey = @ForeignKey(name = "fk_item_trip"))
    private Trip trip;

    /**
     * Human-readable description of this charge line.
     * Example: "Distance charge: 12.73 km @ ₹18/km"
     *          "Night surcharge"
     *          "Waiting charge: 1.5 hrs @ ₹75/hr"
     *          "Fixed monthly fee allocation (trip share)"
     */
    @NotBlank
    @Column(nullable = false, length = 500)
    private String description;

    /**
     * The billing model that produced this line item.
     * Stored as STRING for readability in audit queries.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "pricing_type", length = 20)
    private BillingType pricingType;

    /**
     * The number of units billed (km, hours, trips).
     * BigDecimal for decimal precision. Example: 12.73 km, 2.50 hours.
     */
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    /**
     * The rate applied per unit, in PAISA.
     * Monetary value — Long (paisa). NEVER use double/float.
     * Example: ₹18/km = 1800 paisa.
     */
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, name = "rate_paisa")
    private Long ratePaisa;

    /**
     * Total charge for this line item, in PAISA.
     * = quantity * ratePaisa (for per-unit items)
     * = allocationWeight * monthlyFixedFeePaisa (for fixed monthly items)
     *
     * <p>Monetary value — Long (paisa). NEVER use double/float.
     */
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, name = "amount_paisa")
    private Long amountPaisa;

    /**
     * The proportion of a fixed monthly fee allocated to this trip.
     * Only populated for FIXED_MONTHLY billing items.
     * Example: 0.034700 means 3.47% of the monthly fee goes to this trip.
     *
     * <p>BigDecimal (not double) ensures the weights sum exactly to 1.0.
     * The Largest Remainder method (Phase 3) ensures the total allocated paisa
     * equals the original monthlyFixedFeePaisa exactly.
     */
    @Column(name = "allocation_weight", precision = 10, scale = 6)
    private BigDecimal allocationWeight;

    /**
     * Machine-readable explanation of how this charge was calculated.
     * Stored for customer dispute resolution and audit.
     * Example: "RATE_APPLIED: overagePerKmPaisa=1800, distance=12.73km"
     */
    @Column(length = 1000)
    private String explanation;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

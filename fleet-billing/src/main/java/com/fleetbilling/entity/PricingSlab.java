package com.fleetbilling.entity;

import com.fleetbilling.enums.PricingUnitType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single tier in a tiered pricing slab structure.
 *
 * <p>A ContractVersion can have multiple PricingSlabs that define how the rate
 * decreases (or increases) as usage crosses thresholds. The billing engine will
 * evaluate slabs in slabOrder and apply the appropriate rate.
 *
 * <p>Example (KM-based slabs):
 * <pre>
 *   Slab 1: slabOrder=1, from=0,   to=100,  rate=5000 paisa (₹50/km)
 *   Slab 2: slabOrder=2, from=101, to=200,  rate=4500 paisa (₹45/km)
 *   Slab 3: slabOrder=3, from=201, to=null, rate=4000 paisa (₹40/km)  ← open-ended
 * </pre>
 *
 * <p>Open-ended final slab: toValue = NULL means "201 km and above".
 * The billing engine must check for null toValue to detect the final slab.
 *
 * <p>Why Integer for fromValue/toValue?
 * Pricing slabs are defined in whole units (whole km, whole hours).
 * No sub-unit precision is needed for slab boundaries.
 * Rate in paisa (Long) gives sufficient precision for the charge itself.
 *
 * <p>Index on (contract_version_id, slab_order) ensures efficient ordered retrieval.
 */
@Entity
@Table(
        name = "pricing_slabs",
        indexes = {
                @Index(name = "idx_slab_cv_order",
                       columnList = "contract_version_id, slab_order")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingSlab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The contract version that contains this slab.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_version_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_slab_contract_version"))
    private ContractVersion contractVersion;

    /**
     * Position of this slab in the evaluation order.
     * Slabs are evaluated from lowest to highest slabOrder.
     * Must be unique within a ContractVersion.
     */
    @NotNull
    @Column(nullable = false, name = "slab_order")
    private Integer slabOrder;

    /**
     * Lower boundary of this slab (inclusive), in the unit defined by unitType.
     * Example: 0 km, 101 km, 201 km
     */
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, name = "from_value")
    private Integer fromValue;

    /**
     * Upper boundary of this slab (inclusive), in the unit defined by unitType.
     * NULL means this is the open-ended final slab (applies to all usage above fromValue).
     * Example: 100, 200, null
     */
    @Column(name = "to_value")
    private Integer toValue;

    /**
     * The charge rate for usage falling within this slab, stored in PAISA.
     * Example: ₹50/km = 5000 paisa; ₹45/km = 4500 paisa
     *
     * <p>Monetary value — stored as Long (paisa). NEVER use double/float.
     */
    @NotNull
    @Column(nullable = false, name = "rate_paisa")
    private Long ratePaisa;

    /**
     * The unit this slab applies to (KM, HOUR, or TRIP).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "unit_type", length = 10)
    private PricingUnitType unitType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

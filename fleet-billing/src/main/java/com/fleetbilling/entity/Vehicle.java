package com.fleetbilling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a physical rental vehicle in the fleet.
 *
 * <p>Each vehicle belongs to exactly one vendor and can be part of multiple
 * contracts over its lifetime (one active at a time in practice).
 *
 * <p>Relationships:
 * <pre>
 *   Vendor  1 ----  N  Vehicle
 *   Vehicle 1 ----  N  Contract
 *   Vehicle 1 ----  N  Trip
 *   Vehicle 1 ----  N  BillingRun
 * </pre>
 *
 * <p>Index on (vendor_id) speeds up "list all vehicles for a vendor" queries.
 */
@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicle_registration", columnNames = "registration_number")
        },
        indexes = {
                @Index(name = "idx_vehicle_vendor", columnList = "vendor_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Vehicle registration/licence plate number — unique in the system.
     * Example: "MH12AB1234", "DL01CA5678"
     */
    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, name = "registration_number", length = 20)
    private String registrationNumber;

    /**
     * Category of the vehicle.
     * Example: "SEDAN", "SUV", "TEMPO_TRAVELLER", "BUS"
     * Stored as a free-text string to allow flexibility;
     * an enum may be introduced in a later phase if standardisation is needed.
     */
    @NotBlank
    @Column(nullable = false, name = "vehicle_type", length = 50)
    private String vehicleType;

    /**
     * Manufacturer/brand of the vehicle. Example: "Toyota", "Maruti Suzuki"
     */
    @Column(length = 100)
    private String make;

    /**
     * Model name. Example: "Innova Crysta", "Swift Dzire"
     */
    @Column(length = 100)
    private String model;

    /**
     * The vendor who owns and operates this vehicle.
     *
     * <p>FetchType.LAZY — we explicitly override the JPA default (EAGER for @ManyToOne)
     * because loading the full Vendor object every time we load a Vehicle is wasteful,
     * especially in bulk trip-processing queries.
     * Phase 2 note: always access this field inside an active @Transactional method.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_vehicle_vendor"))
    private Vendor vendor;

    /**
     * Soft-delete flag. Inactive vehicles are retained for historical billing.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

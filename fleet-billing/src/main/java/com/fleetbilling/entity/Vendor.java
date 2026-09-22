package com.fleetbilling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a vehicle rental vendor (supplier).
 *
 * <p>A vendor owns multiple vehicles and provides contracts to the company.
 * The vendor code is a short identifier used in reports and contracts
 * (e.g., "MERU", "OLA_CORP").
 *
 * <p>Vendor 1 ---- N Vehicle
 * <p>Vendor 1 ---- N Contract
 */
@Entity
@Table(
        name = "vendors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vendor_code",          columnNames = "code"),
                @UniqueConstraint(name = "uk_vendor_contact_email", columnNames = "contact_email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Full legal name of the vendor company.
     */
    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Short unique code used to identify the vendor in contracts and reports.
     * Example: "MERU", "OLA_CORP", "TAXIFOR_SURE"
     */
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false, length = 50)
    private String code;

    /**
     * Name of the vendor's primary contact person.
     */
    @NotBlank
    @Column(nullable = false, name = "contact_name", length = 150)
    private String contactName;

    /**
     * Email of the vendor's contact person.
     * Used for invoice delivery and escalation.
     */
    @Email
    @NotBlank
    @Column(nullable = false, name = "contact_email", length = 255)
    private String contactEmail;

    /**
     * Phone number of the vendor's contact person.
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * Registered address of the vendor.
     */
    @Column(length = 500)
    private String address;

    /**
     * Whether this vendor is currently active.
     * Soft-delete approach: inactive vendors are retained for historical billing records.
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

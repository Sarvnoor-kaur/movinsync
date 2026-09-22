package com.fleetbilling.entity;

import com.fleetbilling.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a system user with a specific role.
 *
 * <p>Authentication and JWT implementation will be added in a later phase.
 * The passwordHash field stores the BCrypt/Argon2 hash — NEVER the plain-text password.
 *
 * <p>Why NOT @Data:
 * Lombok's @Data generates equals/hashCode using ALL fields including the id.
 * For JPA entities, this causes problems:
 * - Two unsaved entities (id=null) would be equal even if they have different data.
 * - toString() on a proxied entity can trigger lazy loading, causing LazyInitializationException.
 * We use @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor instead.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_email",    columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Full name of the user.
     */
    @Column(length = 150)
    private String name;

    /**
     * Login username — unique across the system.
     */
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    private String username;

    /**
     * User's email — unique, used for login and notifications.
     */
    @Email
    @NotBlank
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * BCrypt hash of the user's password.
     * Plain-text passwords must NEVER be stored here.
     */
    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Role that controls what the user can do in the system.
     * Stored as a STRING so future role additions don't corrupt existing rows.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    /**
     * Whether the account is active/enabled. Defaults to true.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * Automatically set by Hibernate when the record is first persisted.
     * 'updatable = false' prevents accidental overwrites.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically updated by Hibernate every time the record changes.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

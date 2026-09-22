package com.fleetbilling.entity;

import com.fleetbilling.enums.IdempotencyStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Persists the result of a write operation keyed by an idempotency key.
 *
 * <p>Flow:
 * <ol>
 *   <li>Client sends {@code Idempotency-Key: <uuid>} header.</li>
 *   <li>Service inserts a PROCESSING record (unique constraint prevents duplicates).</li>
 *   <li>Operation executes. Response body is stored.</li>
 *   <li>Record transitions to COMPLETED or FAILED.</li>
 *   <li>Subsequent requests with the same key return the stored response.</li>
 * </ol>
 *
 * <p>requestFingerprint is a SHA-256 hash of the request body (excluding auth headers).
 * If the same key is reused with a different fingerprint → 409 Conflict.
 */
@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_idempotency_key", columnNames = "idempotency_key")
        },
        indexes = {
                @Index(name = "idx_idempotency_key",     columnList = "idempotency_key"),
                @Index(name = "idx_idempotency_expires", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The client-supplied idempotency key (UUID or any opaque string). */
    @NotBlank
    @Column(nullable = false, name = "idempotency_key", length = 128)
    private String idempotencyKey;

    /** SHA-256 hash of the canonical request body (no auth data). */
    @NotBlank
    @Column(nullable = false, name = "request_fingerprint", length = 64)
    private String requestFingerprint;

    /** The API endpoint path (e.g. "/api/billing/runs"). */
    @Column(length = 255)
    private String endpoint;

    /** HTTP method (e.g. "POST"). */
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private IdempotencyStatus status = IdempotencyStatus.PROCESSING;

    /** HTTP response status code of the stored response. */
    @Column(name = "response_status")
    private Integer responseStatus;

    /** JSON-serialised response body (max 16 KB). */
    @Column(name = "response_body", length = 16384)
    private String responseBody;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Record is safe to purge after this timestamp. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}

package com.fleetbilling.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetbilling.entity.IdempotencyRecord;
import com.fleetbilling.enums.IdempotencyStatus;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provides database-backed idempotency for write operations.
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>Client supplies optional {@code Idempotency-Key} header.</li>
 *   <li>Service checks for an existing record with that key.</li>
 *   <li>If COMPLETED → return cached response.</li>
 *   <li>If PROCESSING → return 409 (concurrent duplicate).</li>
 *   <li>If key not found → create PROCESSING record, execute, store result.</li>
 * </ol>
 *
 * <h2>Transaction design</h2>
 * The idempotency record is saved in a REQUIRES_NEW transaction so it
 * survives rollback of the outer business transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    /** How long to keep completed idempotency records (24 hours). */
    private static final long TTL_HOURS = 24;

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    /**
     * Execute an operation with idempotency protection.
     *
     * @param idempotencyKey nullable header value
     * @param endpoint       API path for logging
     * @param requestBody    the request object (used to compute fingerprint)
     * @param operation      the actual business operation
     * @param responseType   expected response class
     * @return the response (new or cached)
     */
    public <T> T executeIdempotently(
            String idempotencyKey,
            String endpoint,
            Object requestBody,
            Supplier<T> operation,
            Class<T> responseType) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // No key — execute normally without idempotency tracking
            return operation.get();
        }

        String fingerprint = computeFingerprint(requestBody);

        Optional<IdempotencyRecord> existing = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();

            // Fingerprint mismatch: same key, different request body
            if (!record.getRequestFingerprint().equals(fingerprint)) {
                throw new DuplicateResourceException(
                        "Idempotency key '" + idempotencyKey + "' was already used for a different request.");
            }

            return switch (record.getStatus()) {
                case COMPLETED -> {
                    log.info("Idempotency cache hit for key '{}', returning stored response", idempotencyKey);
                    yield deserialize(record.getResponseBody(), responseType);
                }
                case PROCESSING -> throw new BusinessException(
                        "Request with Idempotency-Key '" + idempotencyKey + "' is still being processed. " +
                        "Please retry after a moment.");
                case FAILED -> throw new BusinessException(
                        "Previous request with Idempotency-Key '" + idempotencyKey + "' failed. " +
                        "Please use a new key to retry.");
            };
        }

        // Create PROCESSING record in separate transaction so it's visible to concurrent requests
        IdempotencyRecord record = createProcessingRecord(idempotencyKey, fingerprint, endpoint);

        try {
            T result = operation.get();
            markCompleted(record, result);
            return result;
        } catch (Exception e) {
            markFailed(record, e);
            throw e;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord createProcessingRecord(String key, String fingerprint, String endpoint) {
        try {
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(key)
                    .requestFingerprint(fingerprint)
                    .endpoint(endpoint)
                    .httpMethod("POST")
                    .status(IdempotencyStatus.PROCESSING)
                    .expiresAt(LocalDateTime.now().plusHours(TTL_HOURS))
                    .build();
            return idempotencyRecordRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException(
                    "Concurrent request detected for Idempotency-Key '" + key + "'. " +
                    "Only one request may use a key at a time.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> void markCompleted(IdempotencyRecord record, T result) {
        record.setStatus(IdempotencyStatus.COMPLETED);
        record.setResponseStatus(200);
        record.setResponseBody(serialize(result));
        idempotencyRecordRepository.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(IdempotencyRecord record, Exception e) {
        record.setStatus(IdempotencyStatus.FAILED);
        record.setResponseStatus(500);
        record.setResponseBody("{\"error\":\"" + e.getMessage() + "\"}");
        idempotencyRecordRepository.save(record);
    }

    /**
     * Computes a deterministic SHA-256 fingerprint of the request body.
     * No auth headers or secrets are included.
     */
    public String computeFingerprint(Object requestBody) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new BusinessException("Failed to compute request fingerprint: " + e.getMessage());
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new BusinessException("Failed to deserialize cached response: " + e.getMessage());
        }
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException("Failed to serialize response for idempotency cache: " + e.getMessage());
        }
    }
}

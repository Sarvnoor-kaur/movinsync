package com.fleetbilling.service;

import com.fleetbilling.entity.IdempotencyRecord;
import com.fleetbilling.enums.IdempotencyStatus;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    IdempotencyRecordRepository repo;

    @InjectMocks
    IdempotencyService idempotencyService;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void injectMapper() throws Exception {
        var field = IdempotencyService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(idempotencyService, objectMapper);
    }

    record TestRequest(String vehicleId, String billingMonth) {}
    record TestResponse(Long id, String status) {}

    // ── Test 1: no key → executes directly ───────────────────────────────────
    @Test
    void noIdempotencyKey_executesDirectly() {
        TestResponse result = idempotencyService.executeIdempotently(
                null, "/api/test", new TestRequest("1", "2026-09"),
                () -> new TestResponse(1L, "OK"), TestResponse.class);
        assertEquals(1L, result.id());
        verify(repo, never()).findByIdempotencyKey(any());
    }

    // ── Test 2: blank key → executes directly ────────────────────────────────
    @Test
    void blankIdempotencyKey_executesDirectly() {
        TestResponse result = idempotencyService.executeIdempotently(
                "  ", "/api/test", new TestRequest("1", "2026-09"),
                () -> new TestResponse(2L, "OK"), TestResponse.class);
        assertEquals(2L, result.id());
        verify(repo, never()).findByIdempotencyKey(any());
    }

    // ── Test 3: COMPLETED key with same fingerprint → returns cache ───────────
    @Test
    void completedKey_sameFingerprint_returnsCachedResult() throws Exception {
        TestRequest req = new TestRequest("1", "2026-09");
        String fingerprint = idempotencyService.computeFingerprint(req);
        String cachedJson = objectMapper.writeValueAsString(new TestResponse(42L, "CACHED"));

        IdempotencyRecord record = IdempotencyRecord.builder()
                .idempotencyKey("KEY-123")
                .requestFingerprint(fingerprint)
                .status(IdempotencyStatus.COMPLETED)
                .responseBody(cachedJson)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        when(repo.findByIdempotencyKey("KEY-123")).thenReturn(Optional.of(record));

        TestResponse result = idempotencyService.executeIdempotently(
                "KEY-123", "/api/test", req,
                () -> { throw new RuntimeException("Should not execute!"); },
                TestResponse.class);

        assertEquals(42L, result.id());
        assertEquals("CACHED", result.status());
    }

    // ── Test 4: same key + different fingerprint → DuplicateResourceException ─
    @Test
    void sameKey_differentRequest_throwsConflict() {
        IdempotencyRecord record = IdempotencyRecord.builder()
                .idempotencyKey("KEY-456")
                .requestFingerprint("aaaa")
                .status(IdempotencyStatus.COMPLETED)
                .build();

        when(repo.findByIdempotencyKey("KEY-456")).thenReturn(Optional.of(record));

        assertThrows(DuplicateResourceException.class, () ->
                idempotencyService.executeIdempotently(
                        "KEY-456", "/api/test", new TestRequest("99", "2026-01"),
                        () -> new TestResponse(1L, "OK"), TestResponse.class));
    }

    // ── Test 5: PROCESSING key → BusinessException ───────────────────────────
    @Test
    void processingKey_throwsBusinessException() {
        TestRequest req = new TestRequest("1", "2026-09");
        String fingerprint = idempotencyService.computeFingerprint(req);

        IdempotencyRecord record = IdempotencyRecord.builder()
                .idempotencyKey("KEY-789")
                .requestFingerprint(fingerprint)
                .status(IdempotencyStatus.PROCESSING)
                .build();

        when(repo.findByIdempotencyKey("KEY-789")).thenReturn(Optional.of(record));

        assertThrows(BusinessException.class, () ->
                idempotencyService.executeIdempotently(
                        "KEY-789", "/api/test", req,
                        () -> new TestResponse(1L, "OK"), TestResponse.class));
    }

    // ── Test 6: fingerprint is deterministic ──────────────────────────────────
    @Test
    void fingerprint_isDeterministic() {
        TestRequest req = new TestRequest("5", "2026-10");
        String fp1 = idempotencyService.computeFingerprint(req);
        String fp2 = idempotencyService.computeFingerprint(req);
        assertEquals(fp1, fp2);
    }

    // ── Test 7: different requests produce different fingerprints ─────────────
    @Test
    void fingerprint_differsByContent() {
        String fp1 = idempotencyService.computeFingerprint(new TestRequest("1", "2026-09"));
        String fp2 = idempotencyService.computeFingerprint(new TestRequest("2", "2026-09"));
        assertNotEquals(fp1, fp2);
    }
}

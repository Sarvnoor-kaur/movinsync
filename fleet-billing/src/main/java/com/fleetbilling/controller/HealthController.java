package com.fleetbilling.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * HealthController — Phase 1 smoke-test endpoint.
 *
 * <p>This is NOT the same as Spring Actuator's /actuator/health.
 * It is a simple custom endpoint that proves:
 * <ol>
 *   <li>The application started successfully.</li>
 *   <li>Spring Security is configured to allow this path without authentication.</li>
 *   <li>The REST layer is working correctly.</li>
 * </ol>
 *
 * <p>Accessible at: GET /api/health
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Returns a lightweight JSON status object.
     *
     * <p>Response example:
     * <pre>
     * {
     *   "status":  "UP",
     *   "service": "fleet-billing"
     * }
     * </pre>
     *
     * @return 200 OK with status payload
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = Map.of(
                "status",  "UP",
                "service", "fleet-billing"
        );
        return ResponseEntity.ok(response);
    }
}

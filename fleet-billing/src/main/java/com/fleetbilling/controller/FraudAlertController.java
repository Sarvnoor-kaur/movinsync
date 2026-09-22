package com.fleetbilling.controller;

import com.fleetbilling.dto.fraud.FraudAlertResponse;
import com.fleetbilling.enums.FraudAlertStatus;
import com.fleetbilling.enums.FraudAlertType;
import com.fleetbilling.enums.FraudSeverity;
import com.fleetbilling.service.FraudAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/fraud-alerts")
@RequiredArgsConstructor
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Page<FraudAlertResponse>> getAlerts(
            @RequestParam(required = false) FraudAlertStatus status,
            @RequestParam(required = false) FraudSeverity severity,
            @RequestParam(required = false) FraudAlertType alertType,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) Long billingRunId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(fraudAlertService.getAlerts(
                status, severity, alertType, vehicleId, tripId, billingRunId, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<FraudAlertResponse> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(fraudAlertService.getAlertById(id));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<FraudAlertResponse> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(fraudAlertService.resolveAlert(id));
    }

    @PatchMapping("/{id}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<FraudAlertResponse> dismissAlert(@PathVariable Long id) {
        return ResponseEntity.ok(fraudAlertService.dismissAlert(id));
    }
}

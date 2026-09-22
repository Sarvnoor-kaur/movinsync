package com.fleetbilling.controller;

import com.fleetbilling.dto.billing.BillingPreviewResponse;
import com.fleetbilling.dto.billing.BillingRunRequest;
import com.fleetbilling.dto.billing.BillingRunResponse;
import com.fleetbilling.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<BillingRunResponse> createBillingRun(@Valid @RequestBody BillingRunRequest request) {
        return new ResponseEntity<>(billingService.createBillingRun(request), HttpStatus.CREATED);
    }

    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<BillingPreviewResponse> previewBilling(@Valid @RequestBody BillingRunRequest request) {
        return ResponseEntity.ok(billingService.previewBilling(request));
    }

    @GetMapping("/runs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<BillingRunResponse> getBillingRunById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getBillingRunById(id));
    }

    @GetMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<Page<BillingRunResponse>> getBillingRuns(
            @RequestParam(required = false) Long vehicleId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(billingService.getBillingRuns(vehicleId, pageable));
    }
}

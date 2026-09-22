package com.fleetbilling.controller;

import com.fleetbilling.dto.billing.BillingPreviewResponse;
import com.fleetbilling.dto.billing.BillingRunRequest;
import com.fleetbilling.dto.billing.BillingRunResponse;
import com.fleetbilling.dto.billing.FixedFeeAllocationResponse;
import com.fleetbilling.service.BillingAllocationService;
import com.fleetbilling.service.BillingService;
import com.fleetbilling.service.IdempotencyService;
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
    private final BillingAllocationService billingAllocationService;
    private final IdempotencyService idempotencyService;

    @PostMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<BillingRunResponse> createBillingRun(
            @Valid @RequestBody BillingRunRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        BillingRunResponse response = idempotencyService.executeIdempotently(
                idempotencyKey, "/api/billing/runs", request,
                () -> billingService.createBillingRun(request),
                BillingRunResponse.class);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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

    @PostMapping("/runs/{id}/allocate-fixed-fee")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<FixedFeeAllocationResponse> allocateFixedFee(
            @PathVariable Long id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        FixedFeeAllocationResponse response = idempotencyService.executeIdempotently(
                idempotencyKey, "/api/billing/runs/" + id + "/allocate-fixed-fee", id,
                () -> billingAllocationService.allocateFixedFee(id),
                FixedFeeAllocationResponse.class);
        return ResponseEntity.ok(response);
    }
}

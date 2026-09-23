package com.fleetbilling.controller;

import com.fleetbilling.dto.pricingslab.PricingSlabCreateRequest;
import com.fleetbilling.dto.pricingslab.PricingSlabResponse;
import com.fleetbilling.dto.pricingslab.PricingSlabUpdateRequest;
import com.fleetbilling.service.PricingSlabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts/{contractId}/versions/{versionId}/slabs")
@RequiredArgsConstructor
public class PricingSlabController {

    private final PricingSlabService pricingSlabService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PricingSlabResponse> createPricingSlab(
            @PathVariable Long contractId,
            @PathVariable Long versionId,
            @Valid @RequestBody PricingSlabCreateRequest request) {
        PricingSlabResponse response = pricingSlabService.createPricingSlab(versionId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<List<PricingSlabResponse>> getSlabsByContractVersionId(
            @PathVariable Long contractId,
            @PathVariable Long versionId) {
        return ResponseEntity.ok(pricingSlabService.getSlabsByContractVersionId(versionId));
    }

    @GetMapping("/{slabId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<PricingSlabResponse> getPricingSlabById(
            @PathVariable Long contractId,
            @PathVariable Long versionId,
            @PathVariable Long slabId) {
        return ResponseEntity.ok(pricingSlabService.getPricingSlabById(versionId, slabId));
    }

    @PutMapping("/{slabId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PricingSlabResponse> updatePricingSlab(
            @PathVariable Long contractId,
            @PathVariable Long versionId,
            @PathVariable Long slabId,
            @Valid @RequestBody PricingSlabUpdateRequest request) {
        return ResponseEntity.ok(pricingSlabService.updatePricingSlab(versionId, slabId, request));
    }

    @DeleteMapping("/{slabId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Void> deletePricingSlab(
            @PathVariable Long contractId,
            @PathVariable Long versionId,
            @PathVariable Long slabId) {
        pricingSlabService.deletePricingSlab(versionId, slabId);
        return ResponseEntity.noContent().build();
    }
}

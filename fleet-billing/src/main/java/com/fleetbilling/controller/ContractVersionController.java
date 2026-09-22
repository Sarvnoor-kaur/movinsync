package com.fleetbilling.controller;

import com.fleetbilling.dto.contractversion.ContractVersionCreateRequest;
import com.fleetbilling.dto.contractversion.ContractVersionResponse;
import com.fleetbilling.dto.contractversion.ContractVersionUpdateRequest;
import com.fleetbilling.service.ContractVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts/{contractId}/versions")
@RequiredArgsConstructor
public class ContractVersionController {

    private final ContractVersionService contractVersionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractVersionResponse> createContractVersion(
            @PathVariable Long contractId,
            @Valid @RequestBody ContractVersionCreateRequest request) {
        ContractVersionResponse response = contractVersionService.createContractVersion(contractId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<List<ContractVersionResponse>> getVersionsByContractId(
            @PathVariable Long contractId) {
        return ResponseEntity.ok(contractVersionService.getVersionsByContractId(contractId));
    }

    @GetMapping("/{versionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ContractVersionResponse> getContractVersionById(
            @PathVariable Long contractId,
            @PathVariable Long versionId) {
        return ResponseEntity.ok(contractVersionService.getContractVersionById(contractId, versionId));
    }

    @PutMapping("/{versionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractVersionResponse> updateContractVersion(
            @PathVariable Long contractId,
            @PathVariable Long versionId,
            @Valid @RequestBody ContractVersionUpdateRequest request) {
        return ResponseEntity.ok(contractVersionService.updateContractVersion(contractId, versionId, request));
    }

    @DeleteMapping("/{versionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContractVersion(
            @PathVariable Long contractId,
            @PathVariable Long versionId) {
        contractVersionService.deleteContractVersion(contractId, versionId);
        return ResponseEntity.noContent().build();
    }
}

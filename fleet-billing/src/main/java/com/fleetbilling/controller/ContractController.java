package com.fleetbilling.controller;

import com.fleetbilling.dto.contract.ContractCreateRequest;
import com.fleetbilling.dto.contract.ContractResponse;
import com.fleetbilling.dto.contract.ContractUpdateRequest;
import com.fleetbilling.enums.ContractStatus;
import com.fleetbilling.service.ContractService;
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
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody ContractCreateRequest request) {
        ContractResponse response = contractService.createContract(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ContractResponse> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<Page<ContractResponse>> getAllContracts(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) ContractStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contractService.getAllContracts(vendorId, vehicleId, status, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractUpdateRequest request) {
        return ResponseEntity.ok(contractService.updateContract(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }
}

package com.fleetbilling.controller;

import com.fleetbilling.dto.vehicle.VehicleCreateRequest;
import com.fleetbilling.dto.vehicle.VehicleResponse;
import com.fleetbilling.dto.vehicle.VehicleUpdateRequest;
import com.fleetbilling.service.VehicleService;
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
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleCreateRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<Page<VehicleResponse>> getVehicles(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<VehicleResponse> vehicles = vehicleService.getVehicles(vendorId, active, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable Long id,
                                                        @Valid @RequestBody VehicleUpdateRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> activateVehicle(@PathVariable Long id) {
        VehicleResponse response = vehicleService.activateVehicle(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> deactivateVehicle(@PathVariable Long id) {
        VehicleResponse response = vehicleService.deactivateVehicle(id);
        return ResponseEntity.ok(response);
    }
}

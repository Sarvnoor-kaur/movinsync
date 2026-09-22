package com.fleetbilling.controller;

import com.fleetbilling.dto.trip.TripCreateRequest;
import com.fleetbilling.dto.trip.TripResponse;
import com.fleetbilling.dto.trip.TripUpdateRequest;
import com.fleetbilling.enums.TripStatus;
import com.fleetbilling.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripCreateRequest request) {
        TripResponse response = tripService.createTrip(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<Page<TripResponse>> getAllTrips(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) TripStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(tripService.getAllTrips(vehicleId, status, from, to, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripUpdateRequest request) {
        return ResponseEntity.ok(tripService.updateTrip(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Void> cancelTrip(@PathVariable Long id) {
        tripService.cancelTrip(id);
        return ResponseEntity.noContent().build();
    }
}

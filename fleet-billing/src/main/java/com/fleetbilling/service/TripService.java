package com.fleetbilling.service;

import com.fleetbilling.dto.trip.TripCreateRequest;
import com.fleetbilling.dto.trip.TripResponse;
import com.fleetbilling.dto.trip.TripUpdateRequest;
import com.fleetbilling.entity.Trip;
import com.fleetbilling.entity.Vehicle;
import com.fleetbilling.enums.TripStatus;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.TripRepository;
import com.fleetbilling.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public TripResponse createTrip(TripCreateRequest request) {
        if (tripRepository.existsByExternalTripId(request.getExternalTripId())) {
            throw new DuplicateResourceException("Trip already exists with externalTripId: " + request.getExternalTripId());
        }

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + request.getVehicleId()));

        if (request.getStartTime() != null && request.getEndTime() != null && request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException("tripEndTime cannot be before tripStartTime");
        }

        Trip trip = Trip.builder()
                .externalTripId(request.getExternalTripId())
                .vehicle(vehicle)
                .tripDate(request.getTripDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .distanceKm(request.getDistanceKm())
                .dutyHours(request.getDutyHours())
                .waitingHours(request.getWaitingHours())
                .tollAmountPaisa(request.getTollAmountPaisa())
                .night(request.getNight() != null ? request.getNight() : false)
                .status(request.getStatus() != null ? request.getStatus() : TripStatus.COMPLETED)
                .startLocation(request.getStartLocation())
                .endLocation(request.getEndLocation())
                .build();

        Trip saved = tripRepository.save(trip);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + id));
        return mapToResponse(trip);
    }

    @Transactional(readOnly = true)
    public Page<TripResponse> getAllTrips(Long vehicleId, TripStatus status, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return tripRepository.findByFilters(vehicleId, status, fromDate, toDate, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripUpdateRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + id));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + request.getVehicleId()));

        if (request.getStartTime() != null && request.getEndTime() != null && request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException("tripEndTime cannot be before tripStartTime");
        }

        trip.setVehicle(vehicle);
        trip.setTripDate(request.getTripDate());
        trip.setStartTime(request.getStartTime());
        trip.setEndTime(request.getEndTime());
        trip.setDistanceKm(request.getDistanceKm());
        trip.setDutyHours(request.getDutyHours());
        trip.setWaitingHours(request.getWaitingHours());
        trip.setTollAmountPaisa(request.getTollAmountPaisa());
        trip.setNight(request.getNight() != null ? request.getNight() : false);
        trip.setStatus(request.getStatus());
        trip.setStartLocation(request.getStartLocation());
        trip.setEndLocation(request.getEndLocation());

        Trip updated = tripRepository.save(trip);
        return mapToResponse(updated);
    }

    @Transactional
    public void cancelTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id " + id));

        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new BusinessException("Trip is already cancelled");
        }

        trip.setStatus(TripStatus.CANCELLED);
        tripRepository.save(trip);
    }

    private TripResponse mapToResponse(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .externalTripId(trip.getExternalTripId())
                .vehicleId(trip.getVehicle().getId())
                .vehicleRegistrationNumber(trip.getVehicle().getRegistrationNumber())
                .tripDate(trip.getTripDate())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .distanceKm(trip.getDistanceKm())
                .dutyHours(trip.getDutyHours())
                .waitingHours(trip.getWaitingHours())
                .tollAmountPaisa(trip.getTollAmountPaisa())
                .night(trip.getNight())
                .status(trip.getStatus())
                .startLocation(trip.getStartLocation())
                .endLocation(trip.getEndLocation())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }
}

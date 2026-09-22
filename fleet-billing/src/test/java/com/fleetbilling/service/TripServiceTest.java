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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private TripService tripService;

    private Vehicle vehicle;
    private Trip trip;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .id(1L)
                .registrationNumber("UP32AB1234")
                .active(true)
                .build();

        trip = Trip.builder()
                .id(1L)
                .externalTripId("TRIP-2026-000001")
                .vehicle(vehicle)
                .tripDate(LocalDate.of(2026, 9, 20))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .distanceKm(new BigDecimal("42.5"))
                .dutyHours(new BigDecimal("1.5"))
                .waitingHours(new BigDecimal("0.17"))
                .tollAmountPaisa(1500L)
                .night(false)
                .status(TripStatus.COMPLETED)
                .startLocation("Lucknow")
                .endLocation("Kanpur")
                .build();
    }

    @Test
    void createTrip_Success() {
        TripCreateRequest request = TripCreateRequest.builder()
                .externalTripId("TRIP-2026-000001")
                .vehicleId(1L)
                .tripDate(LocalDate.of(2026, 9, 20))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .distanceKm(new BigDecimal("42.5"))
                .dutyHours(new BigDecimal("1.5"))
                .waitingHours(new BigDecimal("0.17"))
                .tollAmountPaisa(1500L)
                .night(false)
                .status(TripStatus.COMPLETED)
                .startLocation("Lucknow")
                .endLocation("Kanpur")
                .build();

        when(tripRepository.existsByExternalTripId(request.getExternalTripId())).thenReturn(false);
        when(vehicleRepository.findById(request.getVehicleId())).thenReturn(Optional.of(vehicle));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        TripResponse response = tripService.createTrip(request);

        assertNotNull(response);
        assertEquals(request.getExternalTripId(), response.getExternalTripId());
        assertEquals(request.getVehicleId(), response.getVehicleId());
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    void createTrip_VehicleNotFound_ThrowsException() {
        TripCreateRequest request = TripCreateRequest.builder()
                .externalTripId("TRIP-2026-000001")
                .vehicleId(99L)
                .build();

        when(tripRepository.existsByExternalTripId(request.getExternalTripId())).thenReturn(false);
        when(vehicleRepository.findById(request.getVehicleId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tripService.createTrip(request));
    }

    @Test
    void createTrip_DuplicateExternalId_ThrowsException() {
        TripCreateRequest request = TripCreateRequest.builder()
                .externalTripId("TRIP-2026-000001")
                .build();

        when(tripRepository.existsByExternalTripId(request.getExternalTripId())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> tripService.createTrip(request));
    }
    
    @Test
    void createTrip_InvalidTime_ThrowsException() {
        TripCreateRequest request = TripCreateRequest.builder()
                .externalTripId("TRIP-2026-000001")
                .vehicleId(1L)
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(9, 0))
                .build();

        when(tripRepository.existsByExternalTripId(request.getExternalTripId())).thenReturn(false);
        when(vehicleRepository.findById(request.getVehicleId())).thenReturn(Optional.of(vehicle));

        assertThrows(BusinessException.class, () -> tripService.createTrip(request));
    }

    @Test
    void getTripById_Success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        TripResponse response = tripService.getTripById(1L);
        assertNotNull(response);
        assertEquals("TRIP-2026-000001", response.getExternalTripId());
    }

    @Test
    void getTripById_NotFound_ThrowsException() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tripService.getTripById(99L));
    }

    @Test
    void getAllTrips_Success() {
        Page<Trip> page = new PageImpl<>(List.of(trip));
        when(tripRepository.findByFilters(1L, TripStatus.COMPLETED, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), PageRequest.of(0, 10)))
                .thenReturn(page);

        Page<TripResponse> response = tripService.getAllTrips(1L, TripStatus.COMPLETED, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void updateTrip_Success() {
        TripUpdateRequest request = TripUpdateRequest.builder()
                .vehicleId(1L)
                .tripDate(LocalDate.of(2026, 9, 21))
                .distanceKm(new BigDecimal("45.0"))
                .status(TripStatus.COMPLETED)
                .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(vehicleRepository.findById(request.getVehicleId())).thenReturn(Optional.of(vehicle));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        TripResponse response = tripService.updateTrip(1L, request);

        assertNotNull(response);
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    void cancelTrip_Success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        tripService.cancelTrip(1L);
        assertEquals(TripStatus.CANCELLED, trip.getStatus());
        verify(tripRepository, times(1)).save(trip);
    }
    
    @Test
    void cancelTrip_AlreadyCancelled_ThrowsException() {
        trip.setStatus(TripStatus.CANCELLED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        assertThrows(BusinessException.class, () -> tripService.cancelTrip(1L));
    }
}

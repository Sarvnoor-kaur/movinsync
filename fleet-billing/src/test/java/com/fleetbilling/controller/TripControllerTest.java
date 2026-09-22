package com.fleetbilling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetbilling.dto.trip.TripCreateRequest;
import com.fleetbilling.dto.trip.TripResponse;
import com.fleetbilling.dto.trip.TripUpdateRequest;
import com.fleetbilling.enums.TripStatus;
import com.fleetbilling.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripService tripService;

    private TripResponse tripResponse;
    private TripCreateRequest createRequest;
    private TripUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        tripResponse = TripResponse.builder()
                .id(1L)
                .externalTripId("TRIP-2026-000001")
                .vehicleId(1L)
                .vehicleRegistrationNumber("UP32AB1234")
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

        createRequest = TripCreateRequest.builder()
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

        updateRequest = TripUpdateRequest.builder()
                .vehicleId(1L)
                .tripDate(LocalDate.of(2026, 9, 21))
                .distanceKm(new BigDecimal("45.0"))
                .dutyHours(new BigDecimal("2.0"))
                .waitingHours(new BigDecimal("0.5"))
                .tollAmountPaisa(2000L)
                .night(false)
                .status(TripStatus.COMPLETED)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTrip_AsAdmin_ReturnsCreated() throws Exception {
        when(tripService.createTrip(any(TripCreateRequest.class))).thenReturn(tripResponse);

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalTripId").value("TRIP-2026-000001"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createTrip_AsEmployee_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTripById_ReturnsOk() throws Exception {
        when(tripService.getTripById(1L)).thenReturn(tripResponse);

        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalTripId").value("TRIP-2026-000001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTrips_ReturnsOk() throws Exception {
        Page<TripResponse> page = new PageImpl<>(List.of(tripResponse));
        when(tripService.getAllTrips(eq(1L), eq(TripStatus.COMPLETED), any(), any(), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/trips")
                .param("vehicleId", "1")
                .param("status", "COMPLETED")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalTripId").value("TRIP-2026-000001"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void updateTrip_AsHr_ReturnsOk() throws Exception {
        when(tripService.updateTrip(eq(1L), any(TripUpdateRequest.class))).thenReturn(tripResponse);

        mockMvc.perform(put("/api/trips/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalTripId").value("TRIP-2026-000001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelTrip_ReturnsNoContent() throws Exception {
        doNothing().when(tripService).cancelTrip(1L);

        mockMvc.perform(patch("/api/trips/1/cancel"))
                .andExpect(status().isNoContent());

        verify(tripService, times(1)).cancelTrip(1L);
    }
}

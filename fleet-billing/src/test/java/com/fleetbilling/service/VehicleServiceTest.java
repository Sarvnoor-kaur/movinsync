package com.fleetbilling.service;

import com.fleetbilling.dto.vehicle.VehicleCreateRequest;
import com.fleetbilling.dto.vehicle.VehicleResponse;
import com.fleetbilling.dto.vehicle.VehicleUpdateRequest;
import com.fleetbilling.entity.Vehicle;
import com.fleetbilling.entity.Vendor;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.VehicleRepository;
import com.fleetbilling.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vendor vendor;
    private Vehicle vehicle;
    private VehicleCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        vendor = Vendor.builder()
                .id(1L)
                .code("VENDOR001")
                .name("ABC Fleet Services")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@abcfleet.com")
                .build();

        vehicle = Vehicle.builder()
                .id(10L)
                .registrationNumber("UP32AB1234")
                .vendor(vendor)
                .vehicleType("SEDAN")
                .make("Toyota")
                .model("Camry")
                .active(true)
                .build();

        createRequest = VehicleCreateRequest.builder()
                .registrationNumber("UP32AB1234")
                .vendorId(1L)
                .vehicleType("SEDAN")
                .make("Toyota")
                .model("Camry")
                .active(true)
                .build();
    }

    @Test
    void createVehicle_Success() {
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));
        when(vehicleRepository.existsByRegistrationNumberIgnoreCase("UP32AB1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.createVehicle(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getRegistrationNumber()).isEqualTo("UP32AB1234");
        assertThat(response.getVendorId()).isEqualTo(1L);
        assertThat(response.getVendorCode()).isEqualTo("VENDOR001");
    }

    @Test
    void createVehicle_VendorNotFound_ThrowsException() {
        when(vendorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.createVehicle(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vendor not found");
    }

    @Test
    void createVehicle_DuplicateRegistration_ThrowsException() {
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));
        when(vehicleRepository.existsByRegistrationNumberIgnoreCase("UP32AB1234")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getVehicleById_Success() {
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getRegistrationNumber()).isEqualTo("UP32AB1234");
    }

    @Test
    void getVehicles_FilterByVendorAndActive_Success() {
        PageImpl<Vehicle> page = new PageImpl<>(List.of(vehicle));
        when(vehicleRepository.findByVendorIdAndActive(eq(1L), eq(true), any(PageRequest.class))).thenReturn(page);

        Page<VehicleResponse> result = vehicleService.getVehicles(1L, true, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRegistrationNumber()).isEqualTo("UP32AB1234");
    }

    @Test
    void activateVehicle_Success() {
        vehicle.setActive(false);
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.activateVehicle(10L);

        assertThat(response.getActive()).isTrue();
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void deactivateVehicle_Success() {
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.deactivateVehicle(10L);

        assertThat(response.getActive()).isFalse();
        verify(vehicleRepository).save(vehicle);
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}

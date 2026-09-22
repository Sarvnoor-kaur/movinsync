package com.fleetbilling.service;

import com.fleetbilling.dto.vendor.VendorCreateRequest;
import com.fleetbilling.dto.vendor.VendorResponse;
import com.fleetbilling.dto.vendor.VendorUpdateRequest;
import com.fleetbilling.entity.Vendor;
import com.fleetbilling.exception.BusinessException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VendorService vendorService;

    private Vendor vendor;
    private VendorCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        vendor = Vendor.builder()
                .id(1L)
                .code("VENDOR001")
                .name("ABC Fleet Services")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@abcfleet.com")
                .contactPhone("+919876543210")
                .address("New Delhi")
                .active(true)
                .build();

        createRequest = VendorCreateRequest.builder()
                .code("VENDOR001")
                .name("ABC Fleet Services")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@abcfleet.com")
                .contactPhone("+919876543210")
                .address("New Delhi")
                .build();
    }

    @Test
    void createVendor_Success() {
        when(vendorRepository.existsByCodeIgnoreCase("VENDOR001")).thenReturn(false);
        when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

        VendorResponse response = vendorService.createVendor(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("VENDOR001");
        assertThat(response.getName()).isEqualTo("ABC Fleet Services");
    }

    @Test
    void createVendor_DuplicateCode_ThrowsException() {
        when(vendorRepository.existsByCodeIgnoreCase("VENDOR001")).thenReturn(true);

        assertThatThrownBy(() -> vendorService.createVendor(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getVendorById_Success() {
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));

        VendorResponse response = vendorService.getVendorById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("ABC Fleet Services");
    }

    @Test
    void getVendorById_NotFound_ThrowsException() {
        when(vendorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vendorService.getVendorById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vendor not found");
    }

    @Test
    void getAllVendors_Success() {
        PageImpl<Vendor> vendorPage = new PageImpl<>(List.of(vendor));
        when(vendorRepository.findAll(any(PageRequest.class))).thenReturn(vendorPage);

        Page<VendorResponse> result = vendorService.getAllVendors(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("VENDOR001");
    }

    @Test
    void updateVendor_Success() {
        VendorUpdateRequest updateRequest = VendorUpdateRequest.builder()
                .name("ABC Fleet Services Pvt Ltd")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@abcfleet.com")
                .active(true)
                .build();

        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));
        when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);

        VendorResponse response = vendorService.updateVendor(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(vendorRepository).save(vendor);
    }

    @Test
    void deleteVendor_WithoutVehicles_Success() {
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));
        when(vehicleRepository.existsByVendorId(1L)).thenReturn(false);

        vendorService.deleteVendor(1L);

        verify(vendorRepository).delete(vendor);
    }

    @Test
    void deleteVendor_WithVehicles_ThrowsBusinessException() {
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));
        when(vehicleRepository.existsByVendorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> vendorService.deleteVendor(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete vendor because vehicles are associated with it");

        verify(vendorRepository, never()).delete(any());
    }
}

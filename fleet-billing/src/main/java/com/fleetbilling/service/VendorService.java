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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public VendorResponse createVendor(VendorCreateRequest request) {
        log.info("Creating new vendor with code: {}", request.getCode());

        if (vendorRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateResourceException("Vendor with code '" + request.getCode() + "' already exists");
        }

        Vendor vendor = Vendor.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .contactName(request.getContactName().trim())
                .contactEmail(request.getContactEmail().trim())
                .contactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : null)
                .address(request.getAddress() != null ? request.getAddress().trim() : null)
                .active(true)
                .build();

        Vendor saved = vendorRepository.save(vendor);
        return mapToVendorResponse(saved);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendorById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return mapToVendorResponse(vendor);
    }

    @Transactional(readOnly = true)
    public Page<VendorResponse> getAllVendors(Pageable pageable) {
        return vendorRepository.findAll(pageable).map(this::mapToVendorResponse);
    }

    @Transactional
    public VendorResponse updateVendor(Long id, VendorUpdateRequest request) {
        log.info("Updating vendor with id: {}", id);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendor.setName(request.getName().trim());
        vendor.setContactName(request.getContactName().trim());
        vendor.setContactEmail(request.getContactEmail().trim());
        if (request.getContactPhone() != null) vendor.setContactPhone(request.getContactPhone().trim());
        if (request.getAddress() != null) vendor.setAddress(request.getAddress().trim());
        if (request.getActive() != null) vendor.setActive(request.getActive());

        Vendor updated = vendorRepository.save(vendor);
        return mapToVendorResponse(updated);
    }

    @Transactional
    public void deleteVendor(Long id) {
        log.info("Attempting to delete vendor with id: {}", id);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        if (vehicleRepository.existsByVendorId(id)) {
            log.warn("Vendor deletion rejected: Vehicles exist for vendor id {}", id);
            throw new BusinessException("Cannot delete vendor because vehicles are associated with it");
        }

        vendorRepository.delete(vendor);
        log.info("Vendor id {} deleted successfully", id);
    }

    private VendorResponse mapToVendorResponse(Vendor vendor) {
        return VendorResponse.builder()
                .id(vendor.getId())
                .code(vendor.getCode())
                .name(vendor.getName())
                .contactName(vendor.getContactName())
                .contactEmail(vendor.getContactEmail())
                .contactPhone(vendor.getContactPhone())
                .address(vendor.getAddress())
                .active(vendor.getActive())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }
}

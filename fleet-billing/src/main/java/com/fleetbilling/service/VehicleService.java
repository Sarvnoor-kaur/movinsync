package com.fleetbilling.service;

import com.fleetbilling.config.CacheNames;
import com.fleetbilling.dto.vehicle.VehicleCreateRequest;
import com.fleetbilling.dto.vehicle.VehicleResponse;
import com.fleetbilling.dto.vehicle.VehicleUpdateRequest;
import com.fleetbilling.entity.Vehicle;
import com.fleetbilling.entity.Vendor;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.VehicleRepository;
import com.fleetbilling.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VendorRepository vendorRepository;

    @Transactional
    @CacheEvict(value = CacheNames.VEHICLES, allEntries = true)
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        log.info("Creating vehicle with registration: {}", request.getRegistrationNumber());

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + request.getVendorId()));

        if (vehicleRepository.existsByRegistrationNumberIgnoreCase(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Vehicle with registration number '" + request.getRegistrationNumber() + "' already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .registrationNumber(request.getRegistrationNumber().trim().toUpperCase())
                .vendor(vendor)
                .vehicleType(request.getVehicleType().trim().toUpperCase())
                .make(request.getMake() != null ? request.getMake().trim() : null)
                .model(request.getModel() != null ? request.getModel().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToVehicleResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.VEHICLES, key = "#id")
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return mapToVehicleResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponse> getVehicles(Long vendorId, Boolean active, Pageable pageable) {
        Page<Vehicle> vehicles;

        if (vendorId != null && active != null) {
            vehicles = vehicleRepository.findByVendorIdAndActive(vendorId, active, pageable);
        } else if (vendorId != null) {
            vehicles = vehicleRepository.findByVendorId(vendorId, pageable);
        } else if (active != null) {
            vehicles = vehicleRepository.findByActive(active, pageable);
        } else {
            vehicles = vehicleRepository.findAll(pageable);
        }

        return vehicles.map(this::mapToVehicleResponse);
    }

    @Transactional
    @CacheEvict(value = CacheNames.VEHICLES, key = "#id")
    public VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request) {
        log.info("Updating vehicle id: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        if (vehicleRepository.existsByRegistrationNumberIgnoreCaseAndIdNot(request.getRegistrationNumber(), id)) {
            throw new DuplicateResourceException("Vehicle with registration number '" + request.getRegistrationNumber() + "' already exists");
        }

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + request.getVendorId()));

        vehicle.setRegistrationNumber(request.getRegistrationNumber().trim().toUpperCase());
        vehicle.setVendor(vendor);
        vehicle.setVehicleType(request.getVehicleType().trim().toUpperCase());
        if (request.getMake() != null) vehicle.setMake(request.getMake().trim());
        if (request.getModel() != null) vehicle.setModel(request.getModel().trim());
        if (request.getActive() != null) vehicle.setActive(request.getActive());

        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToVehicleResponse(updated);
    }

    @Transactional
    @CacheEvict(value = CacheNames.VEHICLES, key = "#id")
    public VehicleResponse activateVehicle(Long id) {
        log.info("Activating vehicle id: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        vehicle.setActive(true);
        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToVehicleResponse(saved);
    }

    @Transactional
    @CacheEvict(value = CacheNames.VEHICLES, key = "#id")
    public VehicleResponse deactivateVehicle(Long id) {
        log.info("Deactivating vehicle id: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        vehicle.setActive(false);
        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToVehicleResponse(saved);
    }

    private VehicleResponse mapToVehicleResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .vendorId(vehicle.getVendor().getId())
                .vendorCode(vehicle.getVendor().getCode())
                .vendorName(vehicle.getVendor().getName())
                .vehicleType(vehicle.getVehicleType())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .active(vehicle.getActive())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}

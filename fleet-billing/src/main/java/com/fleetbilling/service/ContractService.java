package com.fleetbilling.service;

import com.fleetbilling.config.CacheNames;
import com.fleetbilling.dto.contract.ContractCreateRequest;
import com.fleetbilling.dto.contract.ContractResponse;
import com.fleetbilling.dto.contract.ContractUpdateRequest;
import com.fleetbilling.entity.Contract;
import com.fleetbilling.entity.Vehicle;
import com.fleetbilling.entity.Vendor;
import com.fleetbilling.enums.ContractStatus;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.ContractRepository;
import com.fleetbilling.repository.VehicleRepository;
import com.fleetbilling.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    @CacheEvict(value = CacheNames.CONTRACTS, allEntries = true)
    public ContractResponse createContract(ContractCreateRequest request) {
        if (contractRepository.existsByContractNumber(request.getContractCode())) {
            throw new DuplicateResourceException("Contract already exists with contractNumber: " + request.getContractCode());
        }

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id " + request.getVendorId()));

        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + request.getVehicleId()));
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }

        Contract contract = Contract.builder()
                .contractNumber(request.getContractCode())
                .name(request.getName())
                .billingType(request.getBillingType())
                .vendor(vendor)
                .vehicle(vehicle)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : ContractStatus.DRAFT)
                .build();

        Contract savedContract = contractRepository.save(contract);
        return mapToResponse(savedContract);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CONTRACTS, key = "#id")
    public ContractResponse getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + id));
        return mapToResponse(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getAllContracts(Long vendorId, Long vehicleId, ContractStatus status, Pageable pageable) {
        return contractRepository.findByFilters(vendorId, vehicleId, status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CONTRACTS, key = "#id")
    public ContractResponse updateContract(Long id, ContractUpdateRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + id));

        if (request.getName() != null) {
            contract.setName(request.getName());
        }
        if (request.getStatus() != null) {
            contract.setStatus(request.getStatus());
        }
        if (request.getBillingType() != null) {
            contract.setBillingType(request.getBillingType());
        }
        
        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + request.getVehicleId()));
            contract.setVehicle(vehicle);
        }

        if (request.getStartDate() != null) {
            contract.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            contract.setEndDate(request.getEndDate());
        }

        if (contract.getEndDate() != null && contract.getEndDate().isBefore(contract.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }

        Contract updatedContract = contractRepository.save(contract);
        return mapToResponse(updatedContract);
    }

    @Transactional
    @CacheEvict(value = CacheNames.CONTRACTS, key = "#id")
    public void deleteContract(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + id));
        contractRepository.delete(contract);
    }

    private ContractResponse mapToResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .contractCode(contract.getContractNumber())
                .name(contract.getName())
                .vendorId(contract.getVendor().getId())
                .vendorCode(contract.getVendor().getCode())
                .vendorName(contract.getVendor().getName())
                .vehicleId(contract.getVehicle() != null ? contract.getVehicle().getId() : null)
                .vehicleRegistrationNumber(contract.getVehicle() != null ? contract.getVehicle().getRegistrationNumber() : null)
                .billingType(contract.getBillingType())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .status(contract.getStatus())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}

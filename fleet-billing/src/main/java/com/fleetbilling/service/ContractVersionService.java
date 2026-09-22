package com.fleetbilling.service;

import com.fleetbilling.dto.contractversion.ContractVersionCreateRequest;
import com.fleetbilling.dto.contractversion.ContractVersionResponse;
import com.fleetbilling.dto.contractversion.ContractVersionUpdateRequest;
import com.fleetbilling.entity.Contract;
import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.ContractRepository;
import com.fleetbilling.repository.ContractVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractVersionService {

    private final ContractVersionRepository contractVersionRepository;
    private final ContractRepository contractRepository;

    @Transactional
    public ContractVersionResponse createContractVersion(Long contractId, ContractVersionCreateRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + contractId));

        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new BusinessException("effectiveTo cannot be before effectiveFrom");
        }

        validateNoOverlap(contractId, null, request.getEffectiveFrom(), request.getEffectiveTo());
        validateNoDuplicateVersionNumber(contractId, request.getVersionNumber());

        ContractVersion version = ContractVersion.builder()
                .contract(contract)
                .versionNumber(request.getVersionNumber())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .billingType(request.getBillingType())
                .monthlyFixedFeePaisa(request.getMonthlyFixedFeePaisa())
                .freeKm(request.getFreeKm())
                .freeHours(request.getFreeHours())
                .overagePerKmPaisa(request.getOveragePerKmPaisa())
                .overagePerHourPaisa(request.getOveragePerHourPaisa())
                .nightChargePaisa(request.getNightChargePaisa())
                .waitingChargePerHourPaisa(request.getWaitingChargePerHourPaisa())
                .tollHandlingChargePaisa(request.getTollHandlingChargePaisa())
                .build();

        ContractVersion saved = contractVersionRepository.save(version);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ContractVersionResponse> getVersionsByContractId(Long contractId) {
        return contractVersionRepository.findByContractIdOrderByEffectiveFromAsc(contractId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContractVersionResponse getContractVersionById(Long contractId, Long versionId) {
        ContractVersion version = contractVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractVersion not found with id " + versionId));

        if (!version.getContract().getId().equals(contractId)) {
            throw new BusinessException("Version does not belong to the specified contract");
        }

        return mapToResponse(version);
    }

    @Transactional
    public ContractVersionResponse updateContractVersion(Long contractId, Long versionId, ContractVersionUpdateRequest request) {
        ContractVersion version = contractVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractVersion not found with id " + versionId));

        if (!version.getContract().getId().equals(contractId)) {
            throw new BusinessException("Version does not belong to the specified contract");
        }

        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new BusinessException("effectiveTo cannot be before effectiveFrom");
        }

        validateNoOverlap(contractId, versionId, request.getEffectiveFrom(), request.getEffectiveTo());

        version.setEffectiveFrom(request.getEffectiveFrom());
        version.setEffectiveTo(request.getEffectiveTo());
        version.setBillingType(request.getBillingType());
        version.setMonthlyFixedFeePaisa(request.getMonthlyFixedFeePaisa());
        version.setFreeKm(request.getFreeKm());
        version.setFreeHours(request.getFreeHours());
        version.setOveragePerKmPaisa(request.getOveragePerKmPaisa());
        version.setOveragePerHourPaisa(request.getOveragePerHourPaisa());
        version.setNightChargePaisa(request.getNightChargePaisa());
        version.setWaitingChargePerHourPaisa(request.getWaitingChargePerHourPaisa());
        version.setTollHandlingChargePaisa(request.getTollHandlingChargePaisa());

        ContractVersion updated = contractVersionRepository.save(version);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteContractVersion(Long contractId, Long versionId) {
        ContractVersion version = contractVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractVersion not found with id " + versionId));

        if (!version.getContract().getId().equals(contractId)) {
            throw new BusinessException("Version does not belong to the specified contract");
        }

        contractVersionRepository.delete(version);
    }

    private void validateNoOverlap(Long contractId, Long excludeVersionId, LocalDate newStart, LocalDate newEnd) {
        List<ContractVersion> existingVersions = contractVersionRepository.findByContractIdOrderByEffectiveFromAsc(contractId);
        
        LocalDate reqStart = newStart;
        LocalDate reqEnd = newEnd != null ? newEnd : LocalDate.MAX;

        for (ContractVersion existing : existingVersions) {
            if (excludeVersionId != null && existing.getId().equals(excludeVersionId)) {
                continue;
            }

            LocalDate exStart = existing.getEffectiveFrom();
            LocalDate exEnd = existing.getEffectiveTo() != null ? existing.getEffectiveTo() : LocalDate.MAX;

            if (!reqStart.isAfter(exEnd) && !exStart.isAfter(reqEnd)) {
                throw new BusinessException("Contract version dates overlap with an existing version (Version " + existing.getVersionNumber() + ")");
            }
        }
    }

    private void validateNoDuplicateVersionNumber(Long contractId, Integer versionNumber) {
        List<ContractVersion> existingVersions = contractVersionRepository.findByContractIdOrderByEffectiveFromAsc(contractId);
        boolean exists = existingVersions.stream().anyMatch(v -> v.getVersionNumber().equals(versionNumber));
        if (exists) {
            throw new DuplicateResourceException("ContractVersion already exists with versionNumber: " + versionNumber);
        }
    }

    private ContractVersionResponse mapToResponse(ContractVersion version) {
        return ContractVersionResponse.builder()
                .id(version.getId())
                .contractId(version.getContract().getId())
                .contractCode(version.getContract().getContractNumber())
                .versionNumber(version.getVersionNumber())
                .effectiveFrom(version.getEffectiveFrom())
                .effectiveTo(version.getEffectiveTo())
                .billingType(version.getBillingType())
                .monthlyFixedFeePaisa(version.getMonthlyFixedFeePaisa())
                .freeKm(version.getFreeKm())
                .freeHours(version.getFreeHours())
                .overagePerKmPaisa(version.getOveragePerKmPaisa())
                .overagePerHourPaisa(version.getOveragePerHourPaisa())
                .nightChargePaisa(version.getNightChargePaisa())
                .waitingChargePerHourPaisa(version.getWaitingChargePerHourPaisa())
                .tollHandlingChargePaisa(version.getTollHandlingChargePaisa())
                .createdAt(version.getCreatedAt())
                .updatedAt(version.getUpdatedAt())
                .build();
    }
}

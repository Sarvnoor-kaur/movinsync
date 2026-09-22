package com.fleetbilling.service;

import com.fleetbilling.dto.pricingslab.PricingSlabCreateRequest;
import com.fleetbilling.dto.pricingslab.PricingSlabResponse;
import com.fleetbilling.dto.pricingslab.PricingSlabUpdateRequest;
import com.fleetbilling.entity.ContractVersion;
import com.fleetbilling.entity.PricingSlab;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.ContractVersionRepository;
import com.fleetbilling.repository.PricingSlabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingSlabService {

    private final PricingSlabRepository pricingSlabRepository;
    private final ContractVersionRepository contractVersionRepository;

    @Transactional
    public PricingSlabResponse createPricingSlab(Long contractVersionId, PricingSlabCreateRequest request) {
        ContractVersion contractVersion = contractVersionRepository.findById(contractVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractVersion not found with id " + contractVersionId));

        if (request.getMaxValue() != null && request.getMaxValue() < request.getMinValue()) {
            throw new BusinessException("maxValue cannot be less than minValue");
        }

        validateNoOverlap(contractVersionId, null, request.getMinValue(), request.getMaxValue());
        validateNoDuplicateSlabOrder(contractVersionId, request.getSlabOrder());

        PricingSlab slab = PricingSlab.builder()
                .contractVersion(contractVersion)
                .slabOrder(request.getSlabOrder())
                .fromValue(request.getMinValue())
                .toValue(request.getMaxValue())
                .ratePaisa(request.getRatePaisa())
                .unitType(request.getUnitType())
                .build();

        PricingSlab saved = pricingSlabRepository.save(slab);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PricingSlabResponse> getSlabsByContractVersionId(Long contractVersionId) {
        return pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(contractVersionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PricingSlabResponse getPricingSlabById(Long contractVersionId, Long slabId) {
        PricingSlab slab = pricingSlabRepository.findById(slabId)
                .orElseThrow(() -> new ResourceNotFoundException("PricingSlab not found with id " + slabId));

        if (!slab.getContractVersion().getId().equals(contractVersionId)) {
            throw new BusinessException("PricingSlab does not belong to the specified ContractVersion");
        }

        return mapToResponse(slab);
    }

    @Transactional
    public PricingSlabResponse updatePricingSlab(Long contractVersionId, Long slabId, PricingSlabUpdateRequest request) {
        PricingSlab slab = pricingSlabRepository.findById(slabId)
                .orElseThrow(() -> new ResourceNotFoundException("PricingSlab not found with id " + slabId));

        if (!slab.getContractVersion().getId().equals(contractVersionId)) {
            throw new BusinessException("PricingSlab does not belong to the specified ContractVersion");
        }

        if (request.getMaxValue() != null && request.getMaxValue() < request.getMinValue()) {
            throw new BusinessException("maxValue cannot be less than minValue");
        }

        if (!slab.getSlabOrder().equals(request.getSlabOrder())) {
            validateNoDuplicateSlabOrder(contractVersionId, request.getSlabOrder());
        }
        
        validateNoOverlap(contractVersionId, slabId, request.getMinValue(), request.getMaxValue());

        slab.setSlabOrder(request.getSlabOrder());
        slab.setFromValue(request.getMinValue());
        slab.setToValue(request.getMaxValue());
        slab.setRatePaisa(request.getRatePaisa());
        slab.setUnitType(request.getUnitType());

        PricingSlab updated = pricingSlabRepository.save(slab);
        return mapToResponse(updated);
    }

    @Transactional
    public void deletePricingSlab(Long contractVersionId, Long slabId) {
        PricingSlab slab = pricingSlabRepository.findById(slabId)
                .orElseThrow(() -> new ResourceNotFoundException("PricingSlab not found with id " + slabId));

        if (!slab.getContractVersion().getId().equals(contractVersionId)) {
            throw new BusinessException("PricingSlab does not belong to the specified ContractVersion");
        }

        pricingSlabRepository.delete(slab);
    }

    private void validateNoOverlap(Long contractVersionId, Long excludeSlabId, Integer newMin, Integer newMax) {
        List<PricingSlab> existingSlabs = pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(contractVersionId);

        int reqMin = newMin;
        int reqMax = newMax != null ? newMax : Integer.MAX_VALUE;

        for (PricingSlab existing : existingSlabs) {
            if (excludeSlabId != null && existing.getId().equals(excludeSlabId)) {
                continue;
            }

            int exMin = existing.getFromValue();
            int exMax = existing.getToValue() != null ? existing.getToValue() : Integer.MAX_VALUE;

            if (reqMin <= exMax && exMin <= reqMax) {
                throw new BusinessException("PricingSlab ranges overlap with an existing slab (Order " + existing.getSlabOrder() + ")");
            }
        }
    }

    private void validateNoDuplicateSlabOrder(Long contractVersionId, Integer slabOrder) {
        List<PricingSlab> existingSlabs = pricingSlabRepository.findByContractVersionIdOrderByFromValueAsc(contractVersionId);
        boolean exists = existingSlabs.stream().anyMatch(s -> s.getSlabOrder().equals(slabOrder));
        if (exists) {
            throw new DuplicateResourceException("PricingSlab already exists with slabOrder: " + slabOrder);
        }
    }

    private PricingSlabResponse mapToResponse(PricingSlab slab) {
        return PricingSlabResponse.builder()
                .id(slab.getId())
                .contractVersionId(slab.getContractVersion().getId())
                .slabOrder(slab.getSlabOrder())
                .minValue(slab.getFromValue())
                .maxValue(slab.getToValue())
                .ratePaisa(slab.getRatePaisa())
                .unitType(slab.getUnitType())
                .createdAt(slab.getCreatedAt())
                .updatedAt(slab.getUpdatedAt())
                .build();
    }
}

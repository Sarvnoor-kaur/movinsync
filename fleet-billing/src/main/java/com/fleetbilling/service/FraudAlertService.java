package com.fleetbilling.service;

import com.fleetbilling.dto.fraud.FraudAlertResponse;
import com.fleetbilling.entity.FraudAlert;
import com.fleetbilling.enums.FraudAlertStatus;
import com.fleetbilling.enums.FraudAlertType;
import com.fleetbilling.enums.FraudSeverity;
import com.fleetbilling.exception.BusinessException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.FraudAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;

    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> getAlerts(
            FraudAlertStatus status, FraudSeverity severity, FraudAlertType alertType,
            Long vehicleId, Long tripId, Long billingRunId,
            LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        return fraudAlertRepository.findByFilters(
                status, severity, alertType, vehicleId, tripId, billingRunId,
                fromDate, toDate, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public FraudAlertResponse getAlertById(Long id) {
        return mapToResponse(fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found with id " + id)));
    }

    @Transactional
    public FraudAlertResponse resolveAlert(Long id) {
        FraudAlert alert = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found with id " + id));
        if (alert.getStatus() == FraudAlertStatus.RESOLVED) {
            throw new BusinessException("Alert " + id + " is already resolved.");
        }
        alert.setStatus(FraudAlertStatus.RESOLVED);
        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        return mapToResponse(fraudAlertRepository.save(alert));
    }

    @Transactional
    public FraudAlertResponse dismissAlert(Long id) {
        FraudAlert alert = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found with id " + id));
        if (alert.getStatus() == FraudAlertStatus.DISMISSED) {
            throw new BusinessException("Alert " + id + " is already dismissed.");
        }
        alert.setStatus(FraudAlertStatus.DISMISSED);
        alert.setResolvedAt(LocalDateTime.now());
        return mapToResponse(fraudAlertRepository.save(alert));
    }

    private FraudAlertResponse mapToResponse(FraudAlert alert) {
        return FraudAlertResponse.builder()
                .id(alert.getId())
                .tripId(alert.getTrip() != null ? alert.getTrip().getId() : null)
                .externalTripId(alert.getTrip() != null ? alert.getTrip().getExternalTripId() : null)
                .billingRunId(alert.getBillingRun() != null ? alert.getBillingRun().getId() : null)
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .message(alert.getMessage())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}

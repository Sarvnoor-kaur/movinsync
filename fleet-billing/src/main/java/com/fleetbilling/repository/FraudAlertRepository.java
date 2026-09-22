package com.fleetbilling.repository;

import com.fleetbilling.entity.FraudAlert;
import com.fleetbilling.enums.FraudAlertStatus;
import com.fleetbilling.enums.FraudAlertType;
import com.fleetbilling.enums.FraudSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByTripId(Long tripId);
    List<FraudAlert> findByBillingRunId(Long billingRunId);
    List<FraudAlert> findByResolvedFalse();

    Page<FraudAlert> findByStatus(FraudAlertStatus status, Pageable pageable);
    Page<FraudAlert> findBySeverity(FraudSeverity severity, Pageable pageable);
    Page<FraudAlert> findByAlertType(FraudAlertType alertType, Pageable pageable);

    @Query("SELECT fa FROM FraudAlert fa WHERE " +
           "(:status IS NULL OR fa.status = :status) AND " +
           "(:severity IS NULL OR fa.severity = :severity) AND " +
           "(:alertType IS NULL OR fa.alertType = :alertType) AND " +
           "(:vehicleId IS NULL OR fa.trip.vehicle.id = :vehicleId) AND " +
           "(:tripId IS NULL OR fa.trip.id = :tripId) AND " +
           "(:billingRunId IS NULL OR fa.billingRun.id = :billingRunId) AND " +
           "(:fromDate IS NULL OR fa.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR fa.createdAt <= :toDate)")
    Page<FraudAlert> findByFilters(
            @Param("status") FraudAlertStatus status,
            @Param("severity") FraudSeverity severity,
            @Param("alertType") FraudAlertType alertType,
            @Param("vehicleId") Long vehicleId,
            @Param("tripId") Long tripId,
            @Param("billingRunId") Long billingRunId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
}

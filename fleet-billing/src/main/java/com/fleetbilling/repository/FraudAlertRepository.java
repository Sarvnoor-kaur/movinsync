package com.fleetbilling.repository;

import com.fleetbilling.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByTripId(Long tripId);
    List<FraudAlert> findByBillingRunId(Long billingRunId);
    List<FraudAlert> findByResolvedFalse();
}

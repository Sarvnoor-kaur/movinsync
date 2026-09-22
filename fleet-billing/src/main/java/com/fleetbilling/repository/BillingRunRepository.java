package com.fleetbilling.repository;

import com.fleetbilling.entity.BillingRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface BillingRunRepository extends JpaRepository<BillingRun, Long> {
    Optional<BillingRun> findByVehicleIdAndBillingMonth(Long vehicleId, YearMonth billingMonth);
    boolean existsByVehicleIdAndBillingMonth(Long vehicleId, YearMonth billingMonth);
}

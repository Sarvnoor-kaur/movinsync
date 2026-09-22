package com.fleetbilling.repository;

import com.fleetbilling.entity.BillingRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRunRepository extends JpaRepository<BillingRun, Long> {
    Optional<BillingRun> findByVehicleIdAndBillingMonth(Long vehicleId, LocalDate billingMonth);
    boolean existsByVehicleIdAndBillingMonth(Long vehicleId, LocalDate billingMonth);
    Page<BillingRun> findByVehicleId(Long vehicleId, Pageable pageable);
    List<BillingRun> findByVehicleId(Long vehicleId);
}

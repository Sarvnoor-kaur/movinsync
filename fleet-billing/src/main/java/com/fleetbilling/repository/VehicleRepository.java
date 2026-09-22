package com.fleetbilling.repository;

import com.fleetbilling.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    Optional<Vehicle> findByRegistrationNumberIgnoreCase(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);
    boolean existsByRegistrationNumberIgnoreCaseAndIdNot(String registrationNumber, Long id);
    boolean existsByVendorId(Long vendorId);

    List<Vehicle> findByVendorId(Long vendorId);

    Page<Vehicle> findByVendorId(Long vendorId, Pageable pageable);
    Page<Vehicle> findByActive(Boolean active, Pageable pageable);
    Page<Vehicle> findByVendorIdAndActive(Long vendorId, Boolean active, Pageable pageable);
}

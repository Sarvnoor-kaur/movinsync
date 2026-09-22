package com.fleetbilling.repository;

import com.fleetbilling.entity.Contract;
import com.fleetbilling.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findByContractNumber(String contractNumber);
    Optional<Contract> findByContractNumberIgnoreCase(String contractNumber);
    boolean existsByContractNumber(String contractNumber);
    boolean existsByContractNumberIgnoreCase(String contractNumber);
    boolean existsByContractNumberIgnoreCaseAndIdNot(String contractNumber, Long id);

    List<Contract> findByVendorId(Long vendorId);
    List<Contract> findByVehicleId(Long vehicleId);

    Page<Contract> findByVendorId(Long vendorId, Pageable pageable);
    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);
    Page<Contract> findByVendorIdAndStatus(Long vendorId, ContractStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Contract c " +
            "WHERE (:vendorId IS NULL OR c.vendor.id = :vendorId) " +
            "AND (:vehicleId IS NULL OR c.vehicle.id = :vehicleId) " +
            "AND (:status IS NULL OR c.status = :status)")
    Page<Contract> findByFilters(
            @org.springframework.data.repository.query.Param("vendorId") Long vendorId,
            @org.springframework.data.repository.query.Param("vehicleId") Long vehicleId,
            @org.springframework.data.repository.query.Param("status") ContractStatus status,
            Pageable pageable);
}

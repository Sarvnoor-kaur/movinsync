package com.fleetbilling.repository;

import com.fleetbilling.entity.ContractVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractVersionRepository extends JpaRepository<ContractVersion, Long> {
    List<ContractVersion> findByContractIdOrderByEffectiveFromAsc(Long contractId);

    /**
     * Find the contract version whose effective period covers the given trip date.
     * effectiveFrom <= tripDate AND (effectiveTo IS NULL OR effectiveTo >= tripDate)
     */
    @Query("SELECT cv FROM ContractVersion cv " +
           "WHERE cv.contract.id = :contractId " +
           "AND cv.effectiveFrom <= :tripDate " +
           "AND (cv.effectiveTo IS NULL OR cv.effectiveTo >= :tripDate)")
    Optional<ContractVersion> findApplicableVersion(
            @Param("contractId") Long contractId,
            @Param("tripDate") LocalDate tripDate);
}

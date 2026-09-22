package com.fleetbilling.repository;

import com.fleetbilling.entity.PricingSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingSlabRepository extends JpaRepository<PricingSlab, Long> {
    List<PricingSlab> findByContractVersionIdOrderByFromValueAsc(Long contractVersionId);
}

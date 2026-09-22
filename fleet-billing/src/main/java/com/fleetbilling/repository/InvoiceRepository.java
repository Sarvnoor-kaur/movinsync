package com.fleetbilling.repository;

import com.fleetbilling.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByBillingRunId(Long billingRunId);
    Page<Invoice> findByVehicleId(Long vehicleId, Pageable pageable);
    boolean existsByInvoiceNumber(String invoiceNumber);
}

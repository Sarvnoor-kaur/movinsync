package com.fleetbilling.service;

import com.fleetbilling.billing.BillingCalculationResult;
import com.fleetbilling.billing.PricingEngine;
import com.fleetbilling.dto.billing.BillingRunRequest;
import com.fleetbilling.dto.billing.BillingRunResponse;
import com.fleetbilling.entity.*;
import com.fleetbilling.enums.BillingRunStatus;
import com.fleetbilling.enums.BillingType;
import com.fleetbilling.enums.TripStatus;
import com.fleetbilling.exception.DuplicateResourceException;
import com.fleetbilling.exception.ResourceNotFoundException;
import com.fleetbilling.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock BillingRunRepository billingRunRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock InvoiceItemRepository invoiceItemRepository;
    @Mock VehicleRepository vehicleRepository;
    @Mock ContractRepository contractRepository;
    @Mock ContractVersionRepository contractVersionRepository;
    @Mock TripRepository tripRepository;
    @Mock PricingEngine pricingEngine;

    @InjectMocks
    BillingService billingService;

    private Vehicle vehicle;
    private Contract contract;
    private ContractVersion version;
    private Trip trip;
    private BillingRun billingRun;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder().id(1L).registrationNumber("UP32AB1234").active(true).build();

        contract = Contract.builder()
                .id(1L)
                .contractNumber("C-2026-001")
                .vehicle(vehicle)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .billingType(BillingType.PER_KM)
                .build();

        version = ContractVersion.builder()
                .id(1L)
                .contract(contract)
                .versionNumber(1)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(null)
                .billingType(BillingType.PER_KM)
                .build();

        trip = Trip.builder()
                .id(1L)
                .externalTripId("TRIP-2026-000001")
                .vehicle(vehicle)
                .tripDate(LocalDate.of(2026, 9, 20))
                .distanceKm(new BigDecimal("42.5"))
                .tollAmountPaisa(0L)
                .night(false)
                .status(TripStatus.COMPLETED)
                .build();

        billingRun = BillingRun.builder()
                .id(1L)
                .vehicle(vehicle)
                .billingMonth(LocalDate.of(2026, 9, 1))
                .status(BillingRunStatus.COMPLETED)
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        invoice = Invoice.builder()
                .id(1L)
                .billingRun(billingRun)
                .vehicle(vehicle)
                .invoiceNumber("INV-2026-09-VEH-1")
                .invoiceDate(LocalDate.now())
                .subtotalPaisa(63_750L)
                .taxPaisa(0L)
                .totalPaisa(63_750L)
                .build();
    }

    @Test
    void createBillingRun_Success() {
        BillingRunRequest request = new BillingRunRequest(1L, "2026-09");

        when(billingRunRepository.existsByVehicleIdAndBillingMonth(1L, LocalDate.of(2026, 9, 1))).thenReturn(false);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(billingRunRepository.save(any())).thenReturn(billingRun);
        when(tripRepository.findByFilters(eq(1L), eq(TripStatus.COMPLETED),
                eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 30)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(trip)));
        when(contractRepository.findByVehicleId(1L)).thenReturn(List.of(contract));
        when(contractVersionRepository.findApplicableVersion(1L, LocalDate.of(2026, 9, 20)))
                .thenReturn(Optional.of(version));
        when(pricingEngine.calculate(trip, version)).thenReturn(
                BillingCalculationResult.builder()
                        .tripId(1L).externalTripId("TRIP-2026-000001")
                        .contractVersionId(1L).distanceKm(new BigDecimal("42.5"))
                        .baseChargePaisa(63_750L).nightChargePaisa(0).waitingChargePaisa(0)
                        .tollPassThroughPaisa(0).totalChargePaisa(63_750L)
                        .description("Distance charge").explanation("PER_KM|42.5km")
                        .build());
        when(invoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
        when(invoiceRepository.save(any())).thenReturn(invoice);
        when(invoiceItemRepository.save(any())).thenReturn(InvoiceItem.builder().id(1L).amountPaisa(63_750L).build());
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(
                List.of(InvoiceItem.builder().amountPaisa(63_750L).build()));

        BillingRunResponse response = billingService.createBillingRun(request);

        assertNotNull(response);
        assertEquals(BillingRunStatus.COMPLETED, response.getStatus());
        verify(invoiceRepository, times(1)).save(any());
    }

    @Test
    void createBillingRun_DuplicateRejected() {
        BillingRunRequest request = new BillingRunRequest(1L, "2026-09");
        when(billingRunRepository.existsByVehicleIdAndBillingMonth(1L, LocalDate.of(2026, 9, 1))).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> billingService.createBillingRun(request));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createBillingRun_VehicleNotFound() {
        BillingRunRequest request = new BillingRunRequest(99L, "2026-09");
        when(billingRunRepository.existsByVehicleIdAndBillingMonth(99L, LocalDate.of(2026, 9, 1))).thenReturn(false);
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billingService.createBillingRun(request));
    }

    @Test
    void createBillingRun_NoEligibleTrips_CreatesZeroInvoice() {
        BillingRunRequest request = new BillingRunRequest(1L, "2026-09");

        when(billingRunRepository.existsByVehicleIdAndBillingMonth(1L, LocalDate.of(2026, 9, 1))).thenReturn(false);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(billingRunRepository.save(any())).thenReturn(billingRun);
        when(tripRepository.findByFilters(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(contractRepository.findByVehicleId(1L)).thenReturn(List.of(contract));

        Invoice emptyInvoice = Invoice.builder().id(2L).billingRun(billingRun).vehicle(vehicle)
                .invoiceNumber("INV-2026-09-VEH-1").invoiceDate(LocalDate.now())
                .subtotalPaisa(0L).taxPaisa(0L).totalPaisa(0L).build();
        when(invoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
        when(invoiceRepository.save(any())).thenReturn(emptyInvoice);
        when(invoiceItemRepository.findByInvoiceId(2L)).thenReturn(List.of());

        BillingRunResponse response = billingService.createBillingRun(request);

        assertNotNull(response);
        assertEquals(0L, response.getTotalPaisa());
    }

    @Test
    void getBillingRunById_NotFound() {
        when(billingRunRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> billingService.getBillingRunById(99L));
    }

    @Test
    void getInvoiceById_NotFound() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> billingService.getInvoiceById(99L));
    }
}

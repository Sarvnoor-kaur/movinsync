package com.fleetbilling.dto.billing;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private Long id;
    private Long billingRunId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String billingMonth;
    private Long subtotalPaisa;
    private Long taxPaisa;
    private Long totalPaisa;
    private List<InvoiceItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

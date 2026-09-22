package com.fleetbilling.dto.billing;

import com.fleetbilling.enums.BillingType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemResponse {
    private Long id;
    private Long tripId;
    private String externalTripId;
    private String description;
    private BillingType pricingType;
    private BigDecimal quantity;
    private Long ratePaisa;
    private Long amountPaisa;
    private String explanation;
    private LocalDateTime createdAt;
}

package com.fleetbilling.dto.billing;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingPreviewResponse {
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String billingMonth;
    private Integer tripCount;
    private Long estimatedTotalPaisa;
}

package com.fleetbilling.dto.billing;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationItemResponse {
    private Long tripId;
    private String externalTripId;
    private BigDecimal allocationWeight;
    private BigDecimal totalWeight;
    private Long fixedFeePaisa;
    private Long allocatedPaisa;
    private String strategyName;
}

package com.fleetbilling.dto.billing;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedFeeAllocationResponse {
    private Long billingRunId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String billingMonth;
    private Long fixedMonthlyFeePaisa;
    private Long totalAllocatedPaisa;
    private Integer allocatedTripCount;
    private String allocationStrategy;
    private boolean reconciled;
    private Long updatedInvoiceTotalPaisa;
    private List<AllocationItemResponse> items;
}

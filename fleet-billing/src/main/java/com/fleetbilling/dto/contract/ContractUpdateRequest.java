package com.fleetbilling.dto.contract;

import com.fleetbilling.enums.BillingType;
import com.fleetbilling.enums.ContractStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractUpdateRequest {

    private String name;
    private Long vehicleId;
    private ContractStatus status;
    private BillingType billingType;
    private LocalDate startDate;
    private LocalDate endDate;
}

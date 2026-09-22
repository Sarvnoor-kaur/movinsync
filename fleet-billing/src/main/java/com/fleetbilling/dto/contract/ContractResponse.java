package com.fleetbilling.dto.contract;

import com.fleetbilling.enums.BillingType;
import com.fleetbilling.enums.ContractStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponse {

    private Long id;
    private Long vendorId;
    private String vendorCode;
    private String vendorName;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String contractCode;
    private String name;
    private ContractStatus status;
    private BillingType billingType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

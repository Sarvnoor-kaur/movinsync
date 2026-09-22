package com.fleetbilling.dto.contractversion;

import com.fleetbilling.enums.BillingType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractVersionResponse {

    private Long id;
    private Long contractId;
    private String contractCode;
    private Integer versionNumber;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private BillingType billingType;

    private Long monthlyFixedFeePaisa;
    private Integer freeKm;
    private Integer freeHours;
    private Long overagePerKmPaisa;
    private Long overagePerHourPaisa;
    private Long nightChargePaisa;
    private Long waitingChargePerHourPaisa;
    private Long tollHandlingChargePaisa;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

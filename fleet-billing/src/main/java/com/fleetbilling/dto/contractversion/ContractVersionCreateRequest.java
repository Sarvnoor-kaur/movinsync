package com.fleetbilling.dto.contractversion;

import com.fleetbilling.enums.BillingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractVersionCreateRequest {

    @Positive(message = "Version number must be greater than 0")
    private Integer versionNumber;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotNull(message = "Billing type is required")
    private BillingType billingType;

    private Long monthlyFixedFeePaisa;
    private Integer freeKm;
    private Integer freeHours;
    private Long overagePerKmPaisa;
    private Long overagePerHourPaisa;
    private Long nightChargePaisa;
    private Long waitingChargePerHourPaisa;
    private Long tollHandlingChargePaisa;
}

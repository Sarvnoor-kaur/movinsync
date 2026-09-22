package com.fleetbilling.dto.contract;

import com.fleetbilling.enums.BillingType;
import com.fleetbilling.enums.ContractStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractCreateRequest {

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    private Long vehicleId;

    @NotBlank(message = "Contract code is required")
    @Size(min = 2, max = 100, message = "Contract code must be between 2 and 100 characters")
    private String contractCode;

    private String name;

    private ContractStatus status;

    private BillingType billingType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
}

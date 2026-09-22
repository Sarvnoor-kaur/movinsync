package com.fleetbilling.dto.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRunRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    /**
     * Billing month in YYYY-MM format. Example: "2026-09"
     */
    @NotBlank(message = "Billing month is required")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Billing month must be in YYYY-MM format")
    private String billingMonth;
}

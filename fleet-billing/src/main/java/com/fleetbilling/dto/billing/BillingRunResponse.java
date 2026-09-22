package com.fleetbilling.dto.billing;

import com.fleetbilling.enums.BillingRunStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRunResponse {
    private Long billingRunId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String billingMonth;        // YYYY-MM display string
    private LocalDate billingMonthDate; // actual stored LocalDate
    private BillingRunStatus status;
    private Long invoiceId;
    private Long totalPaisa;
    private Integer tripCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
}

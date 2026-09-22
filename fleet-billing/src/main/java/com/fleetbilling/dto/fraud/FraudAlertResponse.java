package com.fleetbilling.dto.fraud;

import com.fleetbilling.enums.FraudAlertStatus;
import com.fleetbilling.enums.FraudAlertType;
import com.fleetbilling.enums.FraudSeverity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertResponse {
    private Long id;
    private Long tripId;
    private String externalTripId;
    private Long billingRunId;
    private FraudAlertType alertType;
    private FraudSeverity severity;
    private FraudAlertStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}

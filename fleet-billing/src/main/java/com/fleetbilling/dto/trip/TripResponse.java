package com.fleetbilling.dto.trip;

import com.fleetbilling.enums.TripStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {

    private Long id;
    private String externalTripId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    
    private LocalDate tripDate;
    private LocalTime startTime;
    private LocalTime endTime;
    
    private BigDecimal distanceKm;
    private BigDecimal dutyHours;
    private BigDecimal waitingHours;
    private Long tollAmountPaisa;
    private Boolean night;
    
    private TripStatus status;
    private String startLocation;
    private String endLocation;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

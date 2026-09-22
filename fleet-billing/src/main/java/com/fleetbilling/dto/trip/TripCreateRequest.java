package com.fleetbilling.dto.trip;

import com.fleetbilling.enums.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripCreateRequest {

    @NotBlank(message = "External Trip ID is required")
    private String externalTripId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Trip date is required")
    private LocalDate tripDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @NotNull(message = "Distance is required")
    @PositiveOrZero(message = "Distance cannot be negative")
    private BigDecimal distanceKm;

    @NotNull(message = "Duty hours is required")
    @PositiveOrZero(message = "Duty hours cannot be negative")
    private BigDecimal dutyHours;

    @NotNull(message = "Waiting hours is required")
    @PositiveOrZero(message = "Waiting hours cannot be negative")
    private BigDecimal waitingHours;

    @NotNull(message = "Toll amount is required")
    @PositiveOrZero(message = "Toll amount cannot be negative")
    private Long tollAmountPaisa;

    @NotNull(message = "Night trip status is required")
    private Boolean night;

    @NotNull(message = "Status is required")
    private TripStatus status;

    private String startLocation;

    private String endLocation;
}

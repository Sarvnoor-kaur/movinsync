package com.fleetbilling.dto.vehicle;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private Long id;
    private String registrationNumber;
    private Long vendorId;
    private String vendorCode;
    private String vendorName;
    private String vehicleType;
    private String make;
    private String model;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

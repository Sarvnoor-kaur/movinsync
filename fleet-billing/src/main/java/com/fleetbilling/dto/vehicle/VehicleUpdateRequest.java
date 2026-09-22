package com.fleetbilling.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUpdateRequest {

    @NotBlank(message = "Registration number is required")
    @Size(max = 20, message = "Registration number cannot exceed 20 characters")
    private String registrationNumber;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 50, message = "Vehicle type cannot exceed 50 characters")
    private String vehicleType;

    @Size(max = 100, message = "Make cannot exceed 100 characters")
    private String make;

    @Size(max = 100, message = "Model cannot exceed 100 characters")
    private String model;

    private Boolean active;
}

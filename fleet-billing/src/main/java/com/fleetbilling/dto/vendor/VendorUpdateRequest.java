package com.fleetbilling.dto.vendor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorUpdateRequest {

    @NotBlank(message = "Vendor name is required")
    @Size(min = 2, max = 255, message = "Vendor name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "Contact name is required")
    @Size(max = 150, message = "Contact name cannot exceed 150 characters")
    private String contactName;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid contact email format")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    private String contactPhone;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    private Boolean active;
}

package com.fleetbilling.dto.vendor;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {

    private Long id;
    private String code;
    private String name;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

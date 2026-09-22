package com.fleetbilling.dto;

import com.fleetbilling.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;
    private Long userId;
    private String name;
    private String email;
    private UserRole role;
}

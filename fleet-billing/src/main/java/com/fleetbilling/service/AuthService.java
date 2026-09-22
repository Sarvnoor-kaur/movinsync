package com.fleetbilling.service;

import com.fleetbilling.dto.AuthResponse;
import com.fleetbilling.dto.LoginRequest;
import com.fleetbilling.dto.RegisterRequest;
import com.fleetbilling.dto.UserResponse;
import com.fleetbilling.entity.User;
import com.fleetbilling.enums.UserRole;
import com.fleetbilling.exception.EmailAlreadyExistsException;
import com.fleetbilling.exception.InvalidRoleException;
import com.fleetbilling.repository.UserRepository;
import com.fleetbilling.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new EmailAlreadyExistsException("User with email '" + request.getEmail() + "' already exists");
        }

        if (request.getRole() == UserRole.ADMIN) {
            throw new InvalidRoleException("Self-registration as ADMIN is not permitted");
        }

        UserRole assignedRole = (request.getRole() != null) ? request.getRole() : UserRole.EMPLOYEE;

        User user = User.builder()
                .name(request.getName())
                .username(request.getEmail())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new DisabledException("User account is disabled");
        }

        String jwtToken = jwtService.generateToken(user);
        long expiresInSeconds = jwtService.getExpirationMs() / 1000;

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .expiresIn(expiresInSeconds)
                .userId(user.getId())
                .name(user.getName() != null ? user.getName() : user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName() != null ? user.getName() : user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .build();
    }
}

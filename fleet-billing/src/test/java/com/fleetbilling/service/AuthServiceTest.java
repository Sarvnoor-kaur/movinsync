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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Sarvnoor")
                .email("sarvnoor@example.com")
                .password("Password@123")
                .role(UserRole.EMPLOYEE)
                .build();

        savedUser = User.builder()
                .id(1L)
                .name("Sarvnoor")
                .username("sarvnoor@example.com")
                .email("sarvnoor@example.com")
                .passwordHash("encoded_bcrypt_hash")
                .role(UserRole.EMPLOYEE)
                .enabled(true)
                .build();
    }

    @Test
    void register_Successful() {
        when(userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_bcrypt_hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("sarvnoor@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.EMPLOYEE);
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void register_AdminRoleSelfRegistration_ThrowsException() {
        registerRequest.setRole(UserRole.ADMIN);

        when(userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(InvalidRoleException.class)
                .hasMessageContaining("ADMIN");
    }

    @Test
    void register_NullRole_DefaultsToEmployee() {
        registerRequest.setRole(null);

        when(userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_bcrypt_hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = authService.register(registerRequest);

        assertThat(response.getRole()).isEqualTo(UserRole.EMPLOYEE);
    }

    @Test
    void login_Successful() {
        LoginRequest loginRequest = new LoginRequest("sarvnoor@example.com", "Password@123");

        when(userRepository.findByEmailIgnoreCase("sarvnoor@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(savedUser)).thenReturn("fake.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse authResponse = authService.login(loginRequest);

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getAccessToken()).isEqualTo("fake.jwt.token");
        assertThat(authResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(authResponse.getExpiresIn()).isEqualTo(3600L);
        assertThat(authResponse.getEmail()).isEqualTo("sarvnoor@example.com");
    }
}

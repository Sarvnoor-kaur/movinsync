package com.fleetbilling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig — Phase 1 minimal security configuration.
 *
 * <p>Spring Security is on the classpath, so by default it locks down EVERY
 * endpoint and generates a random password at startup. That would break our
 * health check and Actuator endpoints immediately.
 *
 * <p>This class replaces that auto-configuration with explicit, readable rules:
 * <ul>
 *   <li>/api/health      — public, no login required (smoke-test endpoint)</li>
 *   <li>/actuator/**     — public in Phase 1 (will be secured in a later phase)</li>
 *   <li>Everything else  — requires authentication (safe default)</li>
 * </ul>
 *
 * <p>CSRF is disabled because this API is stateless (JWT-based auth comes later).
 * Session creation is set to STATELESS for the same reason.
 *
 * <p>Phase 2 will add JWT filter, role-based rules, and method-level security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the HTTP security filter chain that Spring Security applies to every request.
     *
     * @param http the HttpSecurity builder provided by Spring
     * @return a fully configured SecurityFilterChain bean
     * @throws Exception if the security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for a stateless REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session — no HttpSession will be created or used
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules — order matters: more specific rules first
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()   // custom health check
                        .requestMatchers("/actuator/**").permitAll()  // actuator endpoints
                        .anyRequest().authenticated()                 // everything else: secure
                );

        return http.build();
    }
}

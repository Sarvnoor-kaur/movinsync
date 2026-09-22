package com.fleetbilling.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetbilling.dto.LoginRequest;
import com.fleetbilling.entity.User;
import com.fleetbilling.enums.UserRole;
import com.fleetbilling.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_sec;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6370",
        "jwt.secret=c3VwZXItc2VjcmV0LWtleS1mb3ItZmxlZXQtYmlsbGluZy1hcHBsaWNhdGlvbi0yMDI2",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // Create Admin user
        User admin = User.builder()
                .name("System Admin")
                .username("admin@fleet.com")
                .email("admin@fleet.com")
                .passwordHash(passwordEncoder.encode("AdminPass123"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        // Create Employee user
        User employee = User.builder()
                .name("John Employee")
                .username("emp@fleet.com")
                .email("emp@fleet.com")
                .passwordHash(passwordEncoder.encode("EmpPass123"))
                .role(UserRole.EMPLOYEE)
                .enabled(true)
                .build();
        userRepository.save(employee);

        // Get Tokens
        adminToken = obtainToken("admin@fleet.com", "AdminPass123");
        employeeToken = obtainToken("emp@fleet.com", "EmpPass123");
    }

    private String obtainToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void adminEndpoint_AllowedForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void adminEndpoint_ForbiddenForEmployee() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void employeeEndpoint_AllowedForEmployee() throws Exception {
        mockMvc.perform(get("/api/employee/test")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    void unauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/api/employee/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}

package com.fleetbilling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetbilling.dto.LoginRequest;
import com.fleetbilling.dto.vendor.VendorCreateRequest;
import com.fleetbilling.dto.vendor.VendorUpdateRequest;
import com.fleetbilling.entity.User;
import com.fleetbilling.entity.Vendor;
import com.fleetbilling.enums.UserRole;
import com.fleetbilling.repository.UserRepository;
import com.fleetbilling.repository.VendorRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_vendor;DB_CLOSE_DELAY=-1",
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
class VendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VendorRepository vendorRepository;

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
        vendorRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder()
                .name("Admin User")
                .username("admin@test.com")
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("Pass@123"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        User employee = User.builder()
                .name("Emp User")
                .username("emp@test.com")
                .email("emp@test.com")
                .passwordHash(passwordEncoder.encode("Pass@123"))
                .role(UserRole.EMPLOYEE)
                .enabled(true)
                .build();
        userRepository.save(employee);

        adminToken = obtainToken("admin@test.com", "Pass@123");
        employeeToken = obtainToken("emp@test.com", "Pass@123");
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
    void createVendor_Admin_Success() throws Exception {
        VendorCreateRequest request = VendorCreateRequest.builder()
                .code("VENDOR001")
                .name("ABC Fleet Services")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@abcfleet.com")
                .build();

        mockMvc.perform(post("/api/vendors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("VENDOR001"))
                .andExpect(jsonPath("$.name").value("ABC Fleet Services"));
    }

    @Test
    void createVendor_Employee_Forbidden() throws Exception {
        VendorCreateRequest request = VendorCreateRequest.builder()
                .code("VENDOR001")
                .name("ABC Fleet Services")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@abcfleet.com")
                .build();

        mockMvc.perform(post("/api/vendors")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createVendor_DuplicateCode_Conflict() throws Exception {
        Vendor existing = Vendor.builder()
                .code("VENDOR001")
                .name("Existing Fleet")
                .contactName("Contact")
                .contactEmail("c@test.com")
                .active(true)
                .build();
        vendorRepository.save(existing);

        VendorCreateRequest request = VendorCreateRequest.builder()
                .code("VENDOR001")
                .name("ABC Fleet Services")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@abcfleet.com")
                .build();

        mockMvc.perform(post("/api/vendors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}

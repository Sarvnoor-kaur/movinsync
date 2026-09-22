package com.fleetbilling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetbilling.dto.LoginRequest;
import com.fleetbilling.dto.vehicle.VehicleCreateRequest;
import com.fleetbilling.entity.User;
import com.fleetbilling.entity.Vehicle;
import com.fleetbilling.entity.Vendor;
import com.fleetbilling.enums.UserRole;
import com.fleetbilling.repository.UserRepository;
import com.fleetbilling.repository.VehicleRepository;
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
        "spring.datasource.url=jdbc:h2:mem:testdb_vehicle;DB_CLOSE_DELAY=-1",
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
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

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
    private Vendor testVendor;

    @BeforeEach
    void setUp() throws Exception {
        vehicleRepository.deleteAll();
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

        testVendor = Vendor.builder()
                .code("VENDOR001")
                .name("ABC Fleet Services")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@abcfleet.com")
                .active(true)
                .build();
        testVendor = vendorRepository.save(testVendor);
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
    void createVehicle_Admin_Success() throws Exception {
        VehicleCreateRequest request = VehicleCreateRequest.builder()
                .registrationNumber("UP32AB1234")
                .vendorId(testVendor.getId())
                .vehicleType("SEDAN")
                .make("Toyota")
                .model("Camry")
                .active(true)
                .build();

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("UP32AB1234"))
                .andExpect(jsonPath("$.vendorCode").value("VENDOR001"));
    }

    @Test
    void getVehicles_EmployeeAllowed_Success() throws Exception {
        Vehicle vehicle = Vehicle.builder()
                .registrationNumber("DL01AB5678")
                .vendor(testVendor)
                .vehicleType("SUV")
                .active(true)
                .build();
        vehicleRepository.save(vehicle);

        mockMvc.perform(get("/api/vehicles?vendorId=" + testVendor.getId())
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registrationNumber").value("DL01AB5678"));
    }

    @Test
    void deactivateVehicle_Admin_Success() throws Exception {
        Vehicle vehicle = Vehicle.builder()
                .registrationNumber("DL01AB5678")
                .vendor(testVendor)
                .vehicleType("SUV")
                .active(true)
                .build();
        Vehicle saved = vehicleRepository.save(vehicle);

        mockMvc.perform(patch("/api/vehicles/" + saved.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteVendor_WithAssociatedVehicle_Rejected() throws Exception {
        Vehicle vehicle = Vehicle.builder()
                .registrationNumber("DL01AB5678")
                .vendor(testVendor)
                .vehicleType("SUV")
                .active(true)
                .build();
        vehicleRepository.save(vehicle);

        mockMvc.perform(delete("/api/vendors/" + testVendor.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete vendor because vehicles are associated with it"));
    }
}

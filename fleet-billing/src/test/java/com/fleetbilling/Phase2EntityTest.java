package com.fleetbilling;

import com.fleetbilling.entity.Vendor;
import com.fleetbilling.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6370",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
class Phase2EntityTest {

    @Autowired
    private VendorRepository vendorRepository;

    @Test
    void testVendorEntityPersistence() {
        Vendor vendor = Vendor.builder()
                .name("FastFleet Logistics")
                .code("FF-001")
                .contactName("Rajesh Kumar")
                .contactEmail("contact@fastfleet.com")
                .contactPhone("+919876543210")
                .active(true)
                .build();

        Vendor saved = vendorRepository.save(vendor);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("FF-001");

        Vendor fetched = vendorRepository.findByCode("FF-001").orElse(null);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getName()).isEqualTo("FastFleet Logistics");
    }
}

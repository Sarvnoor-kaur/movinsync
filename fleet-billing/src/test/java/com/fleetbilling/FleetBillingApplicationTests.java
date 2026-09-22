package com.fleetbilling;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * FleetBillingApplicationTests — Smoke test for Phase 1.
 *
 * <p>@SpringBootTest loads the full Spring application context.
 * If any bean fails to wire, this test will fail with a clear error message.
 *
 * <p>@TestPropertySource overrides the datasource and Redis URLs so the test
 * does NOT require a running MySQL or Redis instance on the CI/CD machine.
 * Spring Boot's auto-configuration detects an H2-compatible embedded DB
 * when the real driver URL is not reachable. However, since we use MySQL
 * driver only, we point to a test database that must be available, OR we
 * use application-test.properties with a simpler setup.
 *
 * <p>For Phase 1, we simply override the datasource to an in-memory H2
 * database for fast, dependency-free testing.
 */
@SpringBootTest
@TestPropertySource(properties = {
        // Use H2 in-memory database so tests run without a real MySQL server
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        // Disable Redis auto-configuration for tests
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6370",   // wrong port — LazyConnectionDataSourceProxy prevents failure
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
class FleetBillingApplicationTests {

    /**
     * Verifies that the Spring application context loads without errors.
     * This is the most important test in Phase 1.
     */
    @Test
    void contextLoads() {
        // If the application context fails to start, this test will fail automatically.
        // No explicit assertion needed here.
    }
}

# Fleet Billing

> **LPU 2026 Backend Case Study** — Rental Fleet Billing & Fair Cost Split

---

## Project Description

Fleet Billing is a Java Spring Boot backend system for managing rental vehicle contracts,
calculating trip-based charges, and fairly distributing fixed monthly costs across thousands of trips.

The system is designed to support multiple billing models (per-km, per-trip, fixed monthly),
tiered pricing slabs, night/waiting/toll surcharges, and exact monetary reconciliation —
making it suitable for large-scale corporate fleet management.

> **Current Phase: Phase 1 — Project Setup**
> No business logic has been implemented yet.

---

## Technology Stack

| Technology              | Version   | Purpose                                   |
|-------------------------|-----------|-------------------------------------------|
| Java                    | 21        | Language                                  |
| Spring Boot             | 3.3.x     | Application framework                     |
| Maven                   | 3.9+      | Build tool and dependency management      |
| Spring Web              | —         | REST API / embedded Tomcat                |
| Spring Data JPA         | —         | ORM / database access layer               |
| MySQL                   | 8.x       | Primary relational database               |
| Spring Security         | —         | Authentication and authorization          |
| Jakarta Bean Validation | —         | Request/DTO validation                    |
| Lombok                  | —         | Boilerplate reduction                     |
| Spring Boot Actuator    | —         | Health checks and metrics                 |
| Spring Data Redis       | —         | Caching foundation (configured in Phase 2)|
| H2                      | —         | In-memory database for tests only         |
| Docker                  | —         | Containerization                          |

---

## Prerequisites

Make sure the following are installed on your machine before running the project:

- **Java 21** — [Download](https://adoptium.net/)
  ```bash
  java -version   # Should show: openjdk 21...
  ```
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
  ```bash
  mvn -version
  ```
- **MySQL 8.x** — [Download](https://dev.mysql.com/downloads/)
  ```bash
  mysql --version
  ```
- **Git** — [Download](https://git-scm.com/)
- **Docker** (optional, for containerized run) — [Download](https://www.docker.com/)

---

## MySQL Database Setup

Run the following commands in your MySQL client or terminal:

```sql
-- 1. Connect to MySQL
mysql -u root -p

-- 2. Create the database
CREATE DATABASE IF NOT EXISTS fleet_billing
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 3. (Optional) Create a dedicated application user
--    Replace 'yourpassword' with a strong password
CREATE USER IF NOT EXISTS 'fleet_user'@'localhost' IDENTIFIED BY 'yourpassword';
GRANT ALL PRIVILEGES ON fleet_billing.* TO 'fleet_user'@'localhost';
FLUSH PRIVILEGES;

-- 4. Verify
SHOW DATABASES;
```

> **Note:** In Phase 1, `spring.jpa.hibernate.ddl-auto=update` is configured.
> This means Hibernate will automatically create tables when entities are added later.
> You do NOT need to create any tables manually right now.

---

## Environment Variables

The application reads all sensitive credentials from environment variables.
**Never hardcode passwords in `application.properties`.**

### Option 1: Set environment variables directly in your terminal (simplest)

**Windows (PowerShell):**
```powershell
$env:DB_URL      = "jdbc:mysql://localhost:3306/fleet_billing"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "yourpassword"
$env:REDIS_HOST  = "localhost"
$env:REDIS_PORT  = "6379"
$env:REDIS_PASSWORD = ""
```

**macOS / Linux (bash/zsh):**
```bash
export DB_URL=jdbc:mysql://localhost:3306/fleet_billing
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=
```

### Option 2: Use a `.env` file (for Docker Compose)

```bash
cp .env.example .env
# Edit .env and fill in your values
```

| Variable         | Description                   | Default (dev only)                          |
|------------------|-------------------------------|---------------------------------------------|
| `DB_URL`         | JDBC URL for MySQL            | `jdbc:mysql://localhost:3306/fleet_billing` |
| `DB_USERNAME`    | MySQL username                | `root`                                      |
| `DB_PASSWORD`    | MySQL password                | `root`                                      |
| `REDIS_HOST`     | Redis server hostname         | `localhost`                                 |
| `REDIS_PORT`     | Redis server port             | `6379`                                      |
| `REDIS_PASSWORD` | Redis password (blank if none)| *(empty)*                                   |

---

## Running the Application

### Method 1: Maven (recommended for development)

```bash
# Navigate to the project root
cd fleet-billing

# Set environment variables first (see above), then:
mvn spring-boot:run
```

### Method 2: Build JAR and run

```bash
# Build the JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/fleet-billing-0.0.1-SNAPSHOT.jar
```

### Method 3: Docker

```bash
# Build the image
docker build -t fleet-billing:latest .

# Run the container (replace values with your credentials)
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/fleet_billing \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=yourpassword \
  fleet-billing:latest
```

### Method 4: Docker Compose

```bash
# Copy and fill in your .env file first
cp .env.example .env

docker compose up --build
```

---

## Running Tests

```bash
# Run all tests (no MySQL or Redis required — tests use H2 in-memory database)
mvn test

# Run with verbose output
mvn test -Dsurefire.useFile=false
```

Expected output:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Testing the Endpoints

Once the application is running on port 8080:

### Custom Health Endpoint

```
GET http://localhost:8080/api/health
```

**Using curl:**
```bash
curl -s http://localhost:8080/api/health
```

**Expected response (200 OK):**
```json
{
  "status": "UP",
  "service": "fleet-billing"
}
```

### Spring Actuator Health Endpoint

```
GET http://localhost:8080/actuator/health
```

**Using curl:**
```bash
curl -s http://localhost:8080/actuator/health
```

**Expected response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "MySQL", ... } },
    "diskSpace": { "status": "UP", ... },
    "ping": { "status": "UP" },
    "redis": { "status": "UP", ... }
  }
}
```

### Actuator Metrics

```
GET http://localhost:8080/actuator/metrics
```

### Actuator Info

```
GET http://localhost:8080/actuator/info
```

---

## Common Errors and Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| `Communications link failure` | MySQL is not running or wrong DB_URL | Start MySQL: `net start mysql` (Windows) / `brew services start mysql` (Mac) |
| `Access denied for user 'root'@'localhost'` | Wrong DB_USERNAME or DB_PASSWORD | Check env variables; verify MySQL credentials |
| `Unknown database 'fleet_billing'` | Database not created yet | Run `CREATE DATABASE fleet_billing;` in MySQL |
| `Connection refused (Redis)` | Redis not running | Start Redis or set `REDIS_HOST`/`REDIS_PORT` correctly |
| `Port 8080 already in use` | Another app is on port 8080 | Kill the other process or change `server.port` in properties |
| `Using generated security password` in logs | Spring Security auto-config active | This is expected in Phase 1 but `/api/health` is still public |
| `Test failed: HibernateJdbcException` | H2 dialect mismatch | Check `@TestPropertySource` in `FleetBillingApplicationTests` |

---

## Project Structure

```
fleet-billing/
├── src/
│   ├── main/
│   │   ├── java/com/fleetbilling/
│   │   │   ├── FleetBillingApplication.java   ← Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java        ← Security rules (Phase 1: minimal)
│   │   │   ├── controller/
│   │   │   │   └── HealthController.java      ← GET /api/health
│   │   │   ├── service/                       ← Business logic (Phase 2+)
│   │   │   ├── repository/                    ← Spring Data JPA repos (Phase 2+)
│   │   │   ├── entity/                        ← JPA entities / DB tables (Phase 2+)
│   │   │   ├── dto/                           ← Request/response DTOs (Phase 2+)
│   │   │   ├── exception/                     ← Custom exceptions + @RestControllerAdvice (Phase 2+)
│   │   │   ├── security/                      ← JWT filter + UserDetailsService (Phase 2+)
│   │   │   ├── strategy/                      ← Billing strategy implementations (Phase 3+)
│   │   │   └── util/                          ← Helper classes and constants (Phase 2+)
│   │   └── resources/
│   │       └── application.properties         ← All app configuration
│   └── test/
│       └── java/com/fleetbilling/
│           ├── FleetBillingApplicationTests.java  ← Context load smoke test
│           └── controller/
│               └── HealthControllerTest.java      ← Web layer unit test
├── .env.example                               ← Template for environment variables
├── .gitignore                                 ← Ignores secrets, build artifacts, IDE files
├── Dockerfile                                 ← Two-stage Docker build
├── docker-compose.yml                         ← Phase 1 app-only Docker Compose
└── pom.xml                                    ← Maven build configuration
```

---

## File Explanations

| File | Why it exists |
|------|---------------|
| `FleetBillingApplication.java` | Entry point; `@SpringBootApplication` bootstraps the entire framework |
| `pom.xml` | Declares all dependencies and the Spring Boot Maven plugin for fat-JAR packaging |
| `SecurityConfig.java` | Prevents Spring Security from locking down all endpoints by default; permits health + actuator |
| `HealthController.java` | Proves the REST layer works; used in demos, load balancer checks, and interviews |
| `application.properties` | Centralized config for datasource, JPA, Redis, Actuator, and logging |
| `FleetBillingApplicationTests.java` | Smoke test that the full Spring context wires up correctly |
| `HealthControllerTest.java` | Focused unit test for the REST layer without touching the database |
| `.gitignore` | Keeps secrets, IDE files, and build output out of the repository |
| `.env.example` | Safe template committed to git so teammates know what variables to set |
| `Dockerfile` | Reproducible two-stage Docker image for deployment |
| `docker-compose.yml` | Easy local container run for Phase 1 (app only) |
| `package-info.java` (×7) | Documents each package's purpose; helps teammates understand the architecture |

---

## Phase 1 Completion Checklist

See the [verification checklist](#phase-1-verification-checklist) section below.

---

## Phase 1 Verification Checklist

Use this checklist to confirm Phase 1 is 100% complete before moving to Phase 2:

### Build
- [ ] `mvn clean package -DskipTests` succeeds with `BUILD SUCCESS`
- [ ] A JAR file appears in `target/fleet-billing-0.0.1-SNAPSHOT.jar`

### Tests
- [ ] `mvn test` passes with 0 failures and 0 errors
- [ ] `FleetBillingApplicationTests.contextLoads()` passes
- [ ] `HealthControllerTest.healthEndpoint_shouldReturn200WithStatusUp()` passes

### Application Startup
- [ ] Application starts without errors when MySQL is running
- [ ] No `APPLICATION FAILED TO START` message in logs
- [ ] Log shows: `Started FleetBillingApplication in X seconds`

### Custom Health Endpoint
- [ ] `GET http://localhost:8080/api/health` returns `200 OK`
- [ ] Response body is `{"status":"UP","service":"fleet-billing"}`
- [ ] No login prompt (endpoint is public)

### Actuator
- [ ] `GET http://localhost:8080/actuator/health` returns `200 OK`
- [ ] `GET http://localhost:8080/actuator/metrics` returns `200 OK`
- [ ] `GET http://localhost:8080/actuator/info` returns `200 OK`
- [ ] `GET http://localhost:8080/actuator/beans` returns `404` (not exposed — good!)
- [ ] `GET http://localhost:8080/actuator/env` returns `404` (not exposed — good!)

### Security
- [ ] `GET http://localhost:8080/api/someOtherPath` returns `401 Unauthorized`
- [ ] `/api/health` is accessible without authentication

### Database
- [ ] `fleet_billing` database exists in MySQL
- [ ] Connection is made using environment variables, not hardcoded credentials

### Docker (optional for Phase 1)
- [ ] `docker build -t fleet-billing:latest .` succeeds
- [ ] Container starts and `/api/health` responds correctly

### Code Quality
- [ ] No `double` or `float` used for any monetary values
- [ ] No business logic in controllers
- [ ] No hardcoded passwords in any committed file
- [ ] `.env` file is NOT committed to git (verify with `git status`)

---

*Phase 2 will add: JWT authentication, role-based authorization, JPA entities, repositories,
service layer, DTOs, global exception handling, and API endpoints for vendors and vehicles.*

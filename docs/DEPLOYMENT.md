# FleetFlow — Deployment & Local Operations Guide

## 1. System Requirements & Environment Prerequisites

Before running FleetFlow locally or deploying in containers, ensure the following software is installed:

| Requirement | Minimum Version | Recommended Version |
|:---|:---|:---|
| **Java Development Kit (JDK)** | Java 21 | OpenJDK 21 / Temurin 21 |
| **Node.js & npm** | Node.js 18.x | Node.js 20.x LTS & npm 10.x |
| **Relational Database** | MySQL 8.0 | MySQL 8.0 Community Server |
| **In-Memory Cache** | Redis 6.x | Redis 7.2 Alpine |
| **Container Engine** | Docker 24.x | Docker Desktop 4.28+ with Docker Compose v2 |

---

## 2. Environment Configuration (`application.properties`)

The backend configuration is managed via `c:\Users\sarvn\OneDrive\Desktop\movinsync\fleet-billing\src\main\resources\application.properties`.

### Environment Variables Template:
```properties
# Server Settings
server.port=8089

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/fleet_billing?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA & Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Spring Data Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Redis Cache TTL Settings (Seconds)
app.cache.contract-ttl-seconds=600
app.cache.pricing-ttl-seconds=600
app.cache.vehicle-ttl-seconds=300

# Security & JWT Configuration
jwt.secret=c3VwZXItc2VjcmV0LWtleS1mb3ItZmxlZXQtYmlsbGluZy1hcHBsaWNhdGlvbi0yMDI2
jwt.expiration-ms=3600000

# CORS Allowed Origins
app.cors.allowed-origins=http://localhost:5173
```

---

## 3. Local Development Setup (Manual Execution)

### Step 1: Start MySQL Database
Create the `fleet_billing` database in MySQL:
```sql
CREATE DATABASE IF NOT EXISTS fleet_billing;
```

### Step 2: Start Redis Cache Container
Run Redis on port `6379`:
```bash
docker run -d --name fleet-redis -p 6379:6379 redis:alpine
```

### Step 3: Compile and Run Backend (Spring Boot)
Open a terminal in `fleet-billing/`:
```bash
cd fleet-billing
mvn clean spring-boot:run
```
*(Backend will start on `http://localhost:8089`)*

### Step 4: Install and Run Frontend (React + Vite)
Open a second terminal in `fleet-billing/frontend/`:
```bash
cd fleet-billing/frontend
npm install
npm run dev
```
*(Frontend dev server opens on `http://localhost:5173`)*

---

## 4. Containerized Setup via Docker Compose

FleetFlow includes multi-stage Docker containerization support (`Dockerfile` & `docker-compose.yml`).

### Running Full Stack with One Command:
From the `fleet-billing/` directory:
```bash
cd fleet-billing
docker-compose up --build -d
```

### Managed Docker Containers:
- **`fleet-mysql`**: MySQL 8.0 on port `3306`.
- **`fleet-redis`**: Redis Alpine on port `6379`.
- **`fleet-app`**: Multi-stage Spring Boot JAR on port `8089`.

### Stopping Container Stack:
```bash
docker-compose down
```

---

## 5. Health Check & Monitoring Endpoints

Verify system operational status:

- **Custom Application Smoke Test**:
  ```bash
  curl http://localhost:8089/api/health
  ```
  *Response*: `{"status": "UP", "service": "fleet-billing"}`

- **Spring Boot Actuator Health Check**:
  ```bash
  curl http://localhost:8089/actuator/health
  ```
  *Response*: `{"status": "UP"}`

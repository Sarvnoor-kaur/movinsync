# FleetFlow — Master Project Presentation & Complete File-by-File Technical Guide

---

## 📌 Executive Summary & Project Pitch

### What is FleetFlow?
**FleetFlow** is an enterprise-grade, multi-tenant B2B **Rental Fleet Billing & Fair Cost Split Platform**. It automates employee transport logistics management, contract versioning, trip fare calculations, monthly retainer allocations, and fraudulent trip anomaly detection for corporate fleets (operating cabs, SUVs, vans, and buses).

---

## 🎯 Business Problem & Key Technical Solutions

### 1. Money Precision (Zero Rounding Errors)
* **Problem**: Standard floating-point arithmetic (`double`/`float`) introduces rounding drift (e.g. `$0.000000001` or off-by-one paisa), causing corporate audit failures when summing thousands of monthly trips.
* **Solution**: Every financial transaction and rate slab is calculated exclusively in **integer paisa** (₹1 = 100 paisa) using `long` and `BigDecimal` scaling, eliminating rounding errors completely.

### 2. Fair Monthly Retainer Allocation (Largest Remainder Method)
* **Problem**: Vendors charge fixed monthly retainers (e.g., ₹30,000/month per vehicle). Distributing this retainer across 150 trips proportionally by kilometer yields fractional paisa remainder drift.
* **Solution**: FleetFlow uses the **Hamilton/Vinton Largest Remainder Algorithm** to distribute integer retainers across trips, guaranteeing that the sum of all trip-allocated charges matches the monthly contract total to the exact rupee and paisa.

### 3. Rate Contract Versioning & Dispute Prevention
* **Problem**: Vendors update rate contracts mid-month. Applying new rates retroactively to past trips or old rates to new trips causes costly rate disputes.
* **Solution**: Rate contracts support date-ranged versioning (`effectiveFrom` / `effectiveTo`) with strict overlap validation. Every trip fare is evaluated against the exact contract version effective on the trip's timestamp.

### 4. High Availability & Performance Caching
* **Problem**: Database lookups for rate slabs during high-volume trip logging create database bottlenecks.
* **Solution**: Implements **Spring Cache with Redis** to cache contract versions, vehicles, and rate slabs with automatic TTLs. Includes a custom `CacheErrorHandler` that seamlessly falls back to MySQL if Redis encounters a connection outage.

### 5. Financial Idempotency & Fraud Prevention
* **Problem**: Network retries during billing execution can cause duplicate invoices; drivers can log impossible trips (e.g., 500 km in 1 hour).
* **Solution**: Idempotency protection via `Idempotency-Key` headers prevents duplicate execution. Automated rule evaluators detect speed, distance, and duration anomalies, raising real-time `FraudAlert` items.

---

## 🛠️ Complete Technology Stack

| Layer | Technologies & Libraries |
|:---|:---|
| **Frontend UI** | React 18, Vite, React Router DOM v6, Lucide React Icons, Axios, Vanilla CSS Design System |
| **Backend Framework** | Java 21, Spring Boot 3.3.4, Spring Data JPA, Hibernate ORM, Jakarta Validation |
| **Security & Auth** | Spring Security 6, JJWT (Java JWT 0.12.6), BCrypt Password Hashing, MDC Tracing Filter |
| **Database & Caching** | MySQL 8.0 (Relational Storage), Redis (High-Performance Caching), H2 (In-Memory Testing) |
| **Testing & Tools** | JUnit 5, Mockito, Spring Security Test, Postman Collection v2.1, Maven, Docker Compose |

---

## 📐 System Architecture & Data Flow

```text
                               ┌──────────────────────────────────┐
                               │   React 18 Frontend (Vite)       │
                               └────────────────┬─────────────────┘
                                                │ HTTP REST / JSON
                                                ▼
                               ┌──────────────────────────────────┐
                               │  CorrelationIdFilter (MDC Log)   │
                               └────────────────┬─────────────────┘
                                                │
                                                ▼
                               ┌──────────────────────────────────┐
                               │  JwtAuthenticationFilter (RBAC)  │
                               └────────────────┬─────────────────┘
                                                │
                        ┌───────────────────────┴───────────────────────┐
                        ▼                                               ▼
             ┌─────────────────────┐                         ┌─────────────────────┐
             │   REST Controllers  │                         │ Redis Cache Layer   │
             └──────────┬──────────┘                         │ (Contracts / Slabs) │
                        │                                    └──────────▲──────────┘
                        ▼                                               │ Cache Hit / Fallback
             ┌─────────────────────┐                                    │
             │   Services & Engine ├────────────────────────────────────┘
             └──────────┬──────────┘
                        │
                        ▼
             ┌─────────────────────┐
             │   Spring Data JPA   │
             └──────────┬──────────┘
                        │
                        ▼
             ┌─────────────────────┐
             │  MySQL Database 8.0 │
             └─────────────────────┘
```

---

## 📂 Complete File-by-File Breakdown & Codebase Map

Here is the exact explanation of every single file in the project, categorized by layer:

### 1. Backend Core & Configuration (`fleet-billing/src/main/java/com/fleetbilling/`)

#### Entrypoint & Configuration
* 📄 [`FleetBillingApplication.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/FleetBillingApplication.java):
  Main Spring Boot entrypoint class annotated with `@SpringBootApplication`, `@EnableCaching`, and `@EnableAsync`. Boots the Spring application context on port `8089`.
* 📄 [`config/SecurityConfig.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/config/SecurityConfig.java):
  Configures Spring Security 6 `SecurityFilterChain`. Sets up stateless session management, CORS configuration allowing `http://localhost:5173`, endpoint authorization rules by role (`ADMIN`, `HR`, `EMPLOYEE`), and registers `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
* 📄 [`config/RedisConfig.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/config/RedisConfig.java):
  Configures Redis templates and Spring CacheManager. Uses `GenericJackson2JsonRedisSerializer` for JSON caching and implements a custom `CacheErrorHandler` so DB reads continue seamlessly if Redis goes offline.

#### Security & Authentication
* 📄 [`security/JwtService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/security/JwtService.java):
  Utility service for generating HMAC-SHA256 signed JWT tokens, extracting username/role claims, and validating token expiration.
* 📄 [`security/JwtAuthenticationFilter.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/security/JwtAuthenticationFilter.java):
  Per-request security filter that inspects the `Authorization: Bearer <token>` header, validates the JWT, extracts user identity, and populates Spring Security's `SecurityContextHolder`.
* 📄 [`security/CustomUserDetailsService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/security/CustomUserDetailsService.java):
  Implements `UserDetailsService` to fetch user credentials and roles from MySQL by email during login authentication.
* 📄 [`security/CustomAuthenticationEntryPoint.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/security/CustomAuthenticationEntryPoint.java):
  Custom handler returning a clean `401 Unauthorized` JSON response when unauthenticated requests hit protected endpoints.
* 📄 [`security/CustomAccessDeniedHandler.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/security/CustomAccessDeniedHandler.java):
  Custom handler returning a clean `403 Forbidden` JSON response when a user lacks the required role for an endpoint.

#### Observability & Tracing Filter
* 📄 [`filter/CorrelationIdFilter.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/filter/CorrelationIdFilter.java):
  Extracts or generates an `X-Correlation-ID` header for every incoming HTTP request and binds it to SLF4J MDC context, enabling distributed end-to-end log tracing across services.

#### JPA Domain Entities (`entity/`)
* 📄 [`entity/User.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/User.java): Entity representing system users with roles (`ADMIN`, `HR`, `EMPLOYEE`), email, and BCrypt hashed passwords.
* 📄 [`entity/Vendor.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/Vendor.java): Represents vehicle vendors with code, tax ID (GSTIN), contact info, and status.
* 📄 [`entity/Vehicle.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/Vehicle.java): Represents fleet cabs/buses mapped to vendors, registration numbers, capacity, and fuel types.
* 📄 [`entity/Contract.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/Contract.java): Represents billing contracts with billing type (`PER_KM`, `PER_TRIP`, `SLAB_BASED`, `MONTHLY_FIXED`), monthly retainer, and vendor mapping.
* 📄 [`entity/ContractVersion.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/ContractVersion.java): Tracks rate version history with strict `effectiveFrom` and `effectiveTo` date ranges.
* 📄 [`entity/PricingSlab.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/PricingSlab.java): Defines tiered rate slabs (e.g. 0-100 km @ ₹50/km, 101-200 km @ ₹45/km) in integer paisa.
* 📄 [`entity/Trip.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/Trip.java): Logs individual trip duty details, driver, start/end time, distance, dead-leg status, waiting time, tolls, and night charges.
* 📄 [`entity/BillingRun.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/BillingRun.java): Represents a monthly billing execution run for a vehicle or vendor.
* 📄 [`entity/Invoice.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/Invoice.java): Master invoice record generated for a vendor after a billing run.
* 📄 [`entity/InvoiceItem.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/InvoiceItem.java): Itemized invoice breakdown line (base fare, overage, tolls, night charges, fixed fee split).
* 📄 [`entity/IdempotencyRecord.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/IdempotencyRecord.java): Stores hashed idempotency request keys and cached responses to prevent duplicate operations.
* 📄 [`entity/FraudAlert.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/FraudAlert.java): Logs anomalous trip alerts (excessive speed, impossible distance, overlapping timestamps) for resolution.

#### Domain Enums (`enums/`)
* 📄 [`enums/Role.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/enums/Role.java): `ADMIN`, `HR`, `EMPLOYEE`.
* 📄 [`enums/BillingType.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/enums/BillingType.java): `PER_KM`, `PER_TRIP`, `SLAB_BASED`, `MONTHLY_FIXED`.
* 📄 [`enums/TripStatus.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/enums/TripStatus.java): `COMPLETED`, `CANCELLED`, `FLAGGED`, `BILLED`.
* 📄 [`enums/BillingRunStatus.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/enums/BillingRunStatus.java): `DRAFT`, `COMPLETED`, `APPROVED`, `CANCELLED`.
* 📄 [`enums/IdempotencyStatus.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/enums/IdempotencyStatus.java): `IN_PROGRESS`, `SUCCESS`, `FAILED`.
* 📄 [`enums/FraudSeverity.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/enums/FraudSeverity.java): `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

#### Business Calculation Engine & Pricing Strategies (`billing/` & `strategy/`)
* 📄 [`billing/PricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PricingStrategy.java): Strategy interface for computing trip fares.
* 📄 [`billing/PerKmPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PerKmPricingStrategy.java): Computes distance charges and per-km overage in integer paisa.
* 📄 [`billing/PerTripPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PerTripPricingStrategy.java): Computes flat per-trip charges + overage.
* 📄 [`billing/SlabBasedPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/SlabBasedPricingStrategy.java): Evaluates applicable tiered rate slab based on total mileage.
* 📄 [`billing/MonthlyFixedPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/MonthlyFixedPricingStrategy.java): Handles monthly retainer contracts.
* 📄 [`billing/allocation/FixedFeeAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/allocation/FixedFeeAllocationService.java): Implements the Largest Remainder Method (Hamilton/Vinton Algorithm) to split monthly retainers across trips without losing any paisa.

#### Fraud Detection Engine (`fraud/`)
* 📄 [`fraud/FraudRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/FraudRule.java): Interface for trip anomaly checks.
* 📄 [`fraud/ImpossibleDistanceRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/ImpossibleDistanceRule.java): Flags single trips exceeding 1000 km.
* 📄 [`fraud/ImpossibleDurationRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/ImpossibleDurationRule.java): Flags trips lasting over 24 hours.
* 📄 [`fraud/AverageSpeedRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/AverageSpeedRule.java): Flags trips with average calculated speed exceeding 150 km/h.
* 📄 [`fraud/FraudDetectionService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/FraudDetectionService.java): Runs all active rules on new trips and logs `FraudAlert` records.

#### Service Layer (`service/`)
* 📄 [`service/AuthService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/AuthService.java): Handles user registration, authentication, BCrypt password checking, and JWT issuance.
* 📄 [`service/VendorService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/VendorService.java): Business logic for vendor onboarding and retrieval (with Redis caching `@Cacheable`).
* 📄 [`service/VehicleService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/VehicleService.java): Fleet vehicle registry operations.
* 📄 [`service/ContractService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/ContractService.java) & 📄 [`service/ContractVersionService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/ContractVersionService.java): Contract lifecycle and version overlap validation.
* 📄 [`service/TripService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/TripService.java): Trip logging, status updates, and automatic fraud evaluation trigger.
* 📄 [`service/BillingService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/BillingService.java): Billing run execution, invoking strategies, generating invoices and itemized lines.
* 📄 [`service/BillingAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/BillingAllocationService.java): Orchestrates fixed-fee allocation across billing runs.
* 📄 [`service/IdempotencyService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/IdempotencyService.java): Deduplicates billing requests using `Idempotency-Key` headers.
* 📄 [`service/FraudAlertService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/FraudAlertService.java): Manages alert status updates and resolutions.

#### REST Controllers (`controller/`)
* 📄 [`controller/AuthController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/AuthController.java): Endpoints for `/api/auth/register` and `/api/auth/login`.
* 📄 [`controller/VendorController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/VendorController.java): `/api/vendors` CRUD endpoints.
* 📄 [`controller/VehicleController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/VehicleController.java): `/api/vehicles` CRUD endpoints.
* 📄 [`controller/ContractController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/ContractController.java): `/api/contracts` CRUD endpoints.
* 📄 [`controller/ContractVersionController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/ContractVersionController.java): `/api/contracts/{id}/versions` endpoints.
* 📄 [`controller/TripController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/TripController.java): `/api/trips` management endpoints.
* 📄 [`controller/BillingController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/BillingController.java): `/api/billing/runs` and invoice generation.
* 📄 [`controller/FraudAlertController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/FraudAlertController.java): `/api/fraud-alerts` listing and resolution endpoints.
* 📄 [`controller/HealthController.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/controller/HealthController.java): Health & actuator status endpoint.

#### Global Exception Handling
* 📄 [`exception/GlobalExceptionHandler.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/exception/GlobalExceptionHandler.java):
  Catches `ResourceNotFoundException`, `DuplicateResourceException`, `BusinessException`, `MethodArgumentNotValidException`, and returns standardized `ProblemDetail` / JSON error responses with exact HTTP status codes.

---

### 2. Frontend React Application (`fleet-billing/frontend/src/`)

#### Core Entrypoint & Router
* 📄 [`main.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/main.jsx): React 18 DOM root rendering `App.jsx` inside StrictMode.
* 📄 [`App.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/App.jsx): Main router definition (`BrowserRouter`, `Routes`, `Route`), wrapping pages in `AuthProvider` and `NotificationProvider`.
* 📄 [`index.css`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/index.css) & 📄 [`App.css`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/App.css): Design tokens (CSS custom variables for dark/light themes, glassmorphic cards, responsive flex/grid layouts).

#### Context Providers (`context/`)
* 📄 [`context/AuthContext.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/context/AuthContext.jsx): Global authentication context managing JWT token storage, user object state, login, logout, and role helpers (`isAdmin`, `isHr`).
* 📄 [`context/NotificationContext.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/context/NotificationContext.jsx): Manages temporary toast banners for success/error alerts across the UI.

#### API Clients (`api/`)
* 📄 [`api/axios.js`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/api/axios.js): Centralized Axios instance with baseURL `/api`, automatically attaching `Authorization: Bearer <token>` from localStorage and handling 401 error redirects.
* 📄 [`api/authApi.js`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/api/authApi.js): API calls for login and registration.
* 📄 [`api/vendorApi.js`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/api/vendorApi.js): API calls for vendor management.
* 📄 [`api/vehicleApi.js`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/api/vehicleApi.js): API calls for vehicle fleet.
* 📄 [`api/contractApi.js`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/api/contractApi.js): API calls for contracts, versions, and pricing slabs.
* 📄 [`api/tripApi.js`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/api/tripApi.js): API calls for logging and retrieving trips.
* 📄 [`api/billingApi.js`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/api/billingApi.js): API calls for executing billing runs and generating invoices.
* 📄 [`api/fraudApi.js`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/api/fraudApi.js): API calls for listing and resolving fraud alerts.

#### UI Components (`components/`)
* 📄 [`components/Navbar.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/Navbar.jsx): Navigation header with user badge, active role indicator, and profile dropdown.
* 📄 [`components/Sidebar.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/Sidebar.jsx): Sidebar menu providing navigation links filtered according to the user's role.
* 📄 [`components/AppLayout.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/AppLayout.jsx): Core layout structure combining Navbar, Sidebar, and page content outlet.
* 📄 [`components/ProtectedRoute.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/ProtectedRoute.jsx): Route wrapper restricting unauthenticated or unauthorized access.
* 📄 [`components/StatCard.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/StatCard.jsx): Dashboard metric card showing figures, trends, and icons.
* 📄 [`components/StatusBadge.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/StatusBadge.jsx): Visual badge with status colors for trips, billing runs, and alerts.
* 📄 [`components/Modal.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/Modal.jsx): Reusable modal dialog component.
* 📄 [`components/Pagination.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/Pagination.jsx): Table pagination controller.
* 📄 [`components/SkeletonLoader.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/SkeletonLoader.jsx): Loading state placeholder animation.
* 📄 [`components/EmptyState.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/EmptyState.jsx): Visual feedback component for empty table views.
* 📄 [`components/NotificationBanner.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/components/NotificationBanner.jsx): Top toast banner component.

#### UI Pages (`pages/`)
* 📄 [`pages/LandingPage.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/LandingPage.jsx): Public product landing page highlighting feature overview, technology stack, and platform capabilities.
* 📄 [`pages/Login.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Login.jsx) & 📄 [`pages/Register.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Register.jsx): Authentication forms.
* 📄 [`pages/Dashboard.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Dashboard.jsx): Main dashboard displaying high-level fleet statistics, active runs, and alert highlights.
* 📄 [`pages/Vendors.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Vendors.jsx): Vendor lifecycle management page.
* 📄 [`pages/Vehicles.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Vehicles.jsx): Fleet vehicle registry management page.
* 📄 [`pages/Contracts.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Contracts.jsx): Rate contract versioning & pricing slab editor interface.
* 📄 [`pages/Trips.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Trips.jsx): Trip logging and duty management page.
* 📄 [`pages/BillingRuns.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/BillingRuns.jsx): Billing engine run trigger & monthly reconciliation view.
* 📄 [`pages/Invoices.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Invoices.jsx): Vendor invoice breakdown & printable invoice detail viewer.
* 📄 [`pages/FraudAlerts.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/FraudAlerts.jsx): Anomaly detection dashboard and alert resolution portal.
* 📄 [`pages/Profile.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Profile.jsx): User account settings page.
* 📄 [`pages/Unauthorized.jsx`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/frontend/src/pages/Unauthorized.jsx): Access denied error view.

---

### 3. Documentation & Infrastructure Files

* 📄 [`README.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/README.md): Primary landing documentation page with project highlights, tech matrix, and quick start guide.
* 📄 [`docs/PROJECT_OVERVIEW.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/docs/PROJECT_OVERVIEW.md): Business domain description, target users, and RBAC matrix.
* 📄 [`docs/ARCHITECTURE.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/docs/ARCHITECTURE.md): Technical architecture guide, security filter chain, and caching strategy.
* 📄 [`docs/DATABASE.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/docs/DATABASE.md): Relational schema definitions, Mermaid ER diagram, and table relationships.
* 📄 [`docs/BUSINESS_LOGIC.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/docs/BUSINESS_LOGIC.md): Core mathematical algorithms (Paisa calculations, Hamilton/Vinton algorithm, pricing rules).
* 📄 [`docs/API_DOCUMENTATION.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/docs/API_DOCUMENTATION.md): Complete REST API specification across 50 endpoints.
* 📄 [`docs/API_TESTING.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/docs/API_TESTING.md): Postman test suite results and security validation report.
* 📄 [`docs/DEPLOYMENT.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/docs/DEPLOYMENT.md): Containerization and deployment guide.
* 📄 [`postman/Fleet-Billing-API.postman_collection.json`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/postman/Fleet-Billing-API.postman_collection.json): Runnable Postman collection v2.1.
* 📄 [`fleet-billing/pom.xml`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/pom.xml): Maven project object model managing Spring Boot dependencies and plugins.
* 📄 [`fleet-billing/Dockerfile`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/Dockerfile): Multi-stage Docker build file for backend containerization.
* 📄 [`fleet-billing/docker-compose.yml`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/docker-compose.yml): Docker Compose orchestration file for MySQL, Redis, and Spring Boot service.

---

## 🎬 How to Present & Demo FleetFlow to Evaluators

Follow this 5-step demo flow for an impressive presentation:

### Step 1: System Overview & Tech Stack (2 Mins)
1. Present the **problem statement**: B2B transport billing for corporate fleets suffers from rate disputes, rounding errors, fixed fee splitting issues, and fraudulent trip claims.
2. Explain the **architecture**: Spring Boot 3.3.4 + React 18 + MySQL 8.0 + Redis + Spring Security 6 with JWT RBAC.

### Step 2: Onboard Vendor & Fleet (1 Min)
1. Log in as `ADMIN` (`admin@movinsync.com`).
2. Navigate to **Vendors** and show an onboarded transport vendor (e.g. *FastTrack Cabs*).
3. Navigate to **Vehicles** and view registered cabs linked to vendors.

### Step 3: Demonstrate Rate Contracts & Versioning (2 Mins)
1. Open **Contracts** and showcase a contract configured with `SLAB_BASED` or `MONTHLY_FIXED` pricing.
2. Highlight contract versioning: point out `effectiveFrom` / `effectiveTo` dates, demonstrating how rate changes mid-month are handled deterministically.

### Step 4: Trip Processing & Automated Billing Run (3 Mins)
1. Go to **Trips** and show completed duty logs (distance, waiting time, tolls, night charges).
2. Go to **Billing Runs** and trigger a monthly billing run.
3. Show the generated **Invoice** and explain:
   - **Integer Paisa Calculation**: No float rounding errors.
   - **Fixed Fee Allocation**: Show how a ₹30,000 monthly retainer was distributed across trips using the **Hamilton/Vinton Largest Remainder Algorithm**, matching the contract total to the exact paisa.

### Step 5: Fraud Anomaly Alerts & Idempotency (2 Mins)
1. Navigate to **Fraud Alerts** and show real-time flags generated by the fraud detection engine (e.g. average speed > 150 km/h or impossible trip duration).
2. Resolve an alert as `HR` or `ADMIN`.
3. Mention **Idempotency Protection** (`Idempotency-Key` header) that prevents duplicate billing runs during network retries.

---

## 🏆 Summary Checklist for Audits

- ✅ **Paisa Precision**: 100% financial accuracy using integer arithmetic.
- ✅ **Exact Reconciled Retainer**: Hamilton/Vinton Largest Remainder Algorithm.
- ✅ **Version Control**: Date-ranged contract versioning prevents disputes.
- ✅ **Security**: Spring Security 6 + JWT + Granular RBAC (`ADMIN`, `HR`, `EMPLOYEE`).
- ✅ **Resilience**: Redis caching with seamless MySQL fallback.
- ✅ **Observability**: MDC `X-Correlation-ID` header tracing across all logs.

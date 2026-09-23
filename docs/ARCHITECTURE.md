# FleetFlow — Technical Architecture & Request Lifecycle

## 1. High-Level System Architecture

FleetFlow is structured as a decoupled B2B web application. The backend is a Spring Boot 3.3.4 RESTful API microservice integrated with a MySQL 8.0 relational database and a Redis in-memory cache layer. The frontend is a single-page application built with React 18, Vite, and React Router DOM.

```text
                                  ┌───────────────────────────┐
                                  │   React 18 SPA (Vite)     │
                                  └─────────────┬─────────────┘
                                                │ REST API (JSON)
                                                ▼
                                  ┌───────────────────────────┐
                                  │    CorrelationIdFilter    │
                                  │  (Inject X-Correlation-ID)│
                                  └─────────────┬─────────────┘
                                                │
                                                ▼
                                  ┌───────────────────────────┐
                                  │   JwtAuthenticationFilter │
                                  │  (Validate Bearer Token)  │
                                  └─────────────┬─────────────┘
                                                │
                                                ▼
                                  ┌───────────────────────────┐
                                  │  Spring Security Manager  │
                                  │  (Role Authorization Check)│
                                  └─────────────┬─────────────┘
                                                │
                                                ▼
                                  ┌───────────────────────────┐
                                  │     REST Controllers      │
                                  └─────────────┬─────────────┘
                                                │
                    ┌───────────────────────────┴───────────────────────────┐
                    ▼                                                       ▼
       ┌───────────────────────────┐                           ┌───────────────────────────┐
       │   Spring Data JPA Services│                           │    Redis Cache Layer      │
       └────────────┬──────────────┘                           │ (Contract/Slabs/Vehicles) │
                    │                                          └────────────▲──────────────┘
                    ▼                                                       │
       ┌───────────────────────────┐                                        │
       │    MySQL 8.0 Database     │◄───────────────────────────────────────┘
       └───────────────────────────┘         Fallback on Cache Miss / Outage
```

---

## 2. Spring Security & Request Filter Pipeline

Every incoming HTTP request passes through a specialized filter chain before reaching the REST Controller layer:

```text
Request ──► [CorrelationIdFilter] ──► [CorsFilter] ──► [JwtAuthenticationFilter] ──► [AuthorizationFilter] ──► Controller
```

### 1. CorrelationIdFilter (`OncePerRequestFilter`)
- Checks for an incoming `X-Correlation-ID` header.
- If missing, generates a random UUID (`UUID.randomUUID().toString()`).
- Injects `correlationId` into the **SLF4J MDC context** for thread-scoped log tracing.
- Attaches `X-Correlation-ID` to the outgoing HTTP response header.

### 2. CorsFilter
- Configured via `SecurityConfig.corsConfigurationSource()`.
- Allows origin patterns (`http://localhost:*`, `http://127.0.0.1:*`).
- Exposes headers: `Authorization`, `Idempotency-Key`, `X-Correlation-ID`.

### 3. JwtAuthenticationFilter
- Extracts the `Authorization: Bearer <token>` header.
- Validates token signature using HMAC-SHA256 secret key (`jwt.secret`).
- Loads `UserDetails` via `CustomUserDetailsService`.
- Establishes `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`.

### 4. AuthorizationFilter
- Enforces role restrictions (`hasRole('ADMIN')`, `hasAnyRole('ADMIN', 'HR')`, `hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')`).
- Unauthenticated requests to protected endpoints yield **HTTP 401 Unauthorized**.
- Unauthorized role access yields **HTTP 403 Forbidden**.

---

## 3. High-Performance Caching Layer (Spring Data Redis)

FleetFlow uses Redis to reduce database read pressure for frequency-accessed, infrequently-changed configuration entities (Rate Contracts, Pricing Slabs, and Vehicle metadata).

```text
                                  ┌───────────────────────────┐
                                  │      Service Call         │
                                  │  (e.g., getContractById)  │
                                  └─────────────┬─────────────┘
                                                │
                                                ▼
                                  ┌───────────────────────────┐
                                  │ Is Key in Redis Cache?    │
                                  └──────┬─────────────┬──────┘
                                         │             │
                                  YES    │             │   NO (or Redis Down)
                                         ▼             ▼
                           ┌──────────────────┐   ┌──────────────────┐
                           │   Return JSON    │   │ Query MySQL DB   │
                           │   from Redis     │   └────────┬─────────┘
                           └──────────────────┘            │
                                                           ▼
                                                  ┌──────────────────┐
                                                  │ Store in Redis   │
                                                  │ & Return Payload │
                                                  └──────────────────┘
```

### Cache TTL & Invalidation Policy

| Cache Name | Key Strategy | TTL | Eviction Strategy |
|:---|:---|:---|:---|
| `contracts` | `#id` | 600s (10 min) | `@CacheEvict(allEntries=true)` on Create/Update/Delete |
| `contract_versions` | `'contract_' + #contractId` | 600s (10 min) | `@CacheEvict(allEntries=true)` on Version changes |
| `pricing_slabs` | `'version_' + #versionId` | 600s (10 min) | `@CacheEvict(allEntries=true)` on Slab changes |
| `vehicles` | `#id` | 300s (5 min) | `@CacheEvict` on Update/Activate/Deactivate |

### Redis Resilience (`RedisCacheErrorHandler`)
If the Redis server crashes or network connectivity is lost:
1. `RedisCacheErrorHandler` intercepts `RedisConnectionException` and `QueryTimeoutException`.
2. Logs a warning: `"Redis error encountered... falling back to MySQL source of truth"`.
3. Execution proceeds seamlessly to MySQL without throwing HTTP 500 errors.

---

## 4. Centralized Error Handling Architecture

Exceptions thrown anywhere in the application are intercepted by `GlobalExceptionHandler` (`@RestControllerAdvice`).

```text
Exception Thrown ──► GlobalExceptionHandler ──► CorrelationIdFilter.getCorrelationId() ──► ErrorResponse (HTTP 40x / 500)
```

### Standardized `ErrorResponse` Schema:
```json
{
  "timestamp": "2026-09-23T11:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/vendors",
  "correlationId": "b4c2e1d0-3f4a-5b6c-7d8e-9f0a1b2c3d4e",
  "fieldErrors": {
    "code": "Vendor code is required",
    "contactEmail": "Invalid email format"
  }
}
```

---

## 5. Idempotency Execution Flow

Financial endpoints like `POST /api/billing/runs` and `POST /api/billing/runs/{id}/allocate-fixed-fee` accept an optional `Idempotency-Key` header:

```text
Request + Idempotency-Key ──► IdempotencyService ──► Hash (Endpoint + Key + Body)
                                                          │
                                         ┌────────────────┴────────────────┐
                                         ▼                                 ▼
                                Record Exists?                    Record Missing?
                                         │                                 │
                                         YES                               NO
                                         ▼                                 ▼
                               Return Stored Response           Execute Business Logic
                               (Skip Double Billing)            & Save IdempotencyRecord
```

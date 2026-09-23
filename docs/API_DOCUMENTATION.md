# FleetFlow — Complete REST API Reference Specification

Base URL: `http://localhost:8089`

---

## 1. Authentication & Security Endpoints (`/api/auth`)

### 1.1 Register User
- **Method**: `POST`
- **Path**: `/api/auth/register`
- **Auth Required**: No (Public)
- **Request Body**:
```json
{
  "name": "Sarvnoor Kaur",
  "email": "sarvnoor@movinsync.com",
  "password": "Password@123",
  "role": "EMPLOYEE"
}
```
- **Success Response** (`201 Created`):
```json
{
  "id": 1,
  "name": "Sarvnoor Kaur",
  "email": "sarvnoor@movinsync.com",
  "role": "EMPLOYEE",
  "enabled": true
}
```
- **Error Response** (`400 Bad Request` — if `role: "ADMIN"`):
```json
{
  "timestamp": "2026-09-23T11:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Self-registration as ADMIN is not permitted",
  "path": "/api/auth/register",
  "correlationId": "8f3a1b2c-4d5e-6f7a-8b9c-0d1e2f3a4b5c"
}
```

---

### 1.2 User Login
- **Method**: `POST`
- **Path**: `/api/auth/login`
- **Auth Required**: No (Public)
- **Request Body**:
```json
{
  "email": "admin@fleetbilling.com",
  "password": "AdminPassword@123"
}
```
- **Success Response** (`200 OK`):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBmbGVldGJpbGxpbmcuY29tIiwicm9sZSI6IkFETUlOIn0...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "name": "System Admin",
  "email": "admin@fleetbilling.com",
  "role": "ADMIN"
}
```

---

### 1.3 Get Current User
- **Method**: `GET`
- **Path**: `/api/auth/me`
- **Auth Required**: Yes (`ADMIN`, `HR`, `EMPLOYEE`)
- **Headers**: `Authorization: Bearer <token>`
- **Success Response** (`200 OK`): Returns `UserResponse`.

---

## 2. Security Test Endpoints (`/api/admin`, `/api/hr`, `/api/employee`)

| Method | Path | Required Role | Success Response (`200 OK`) | Unauthorized Response |
|:---|:---|:---|:---|:---|
| `GET` | `/api/admin/test` | `ADMIN` | `{"role": "ADMIN", "message": "Admin endpoint accessed"}` | `403 Forbidden` |
| `GET` | `/api/hr/test` | `HR` | `{"role": "HR", "message": "HR endpoint accessed"}` | `403 Forbidden` |
| `GET` | `/api/employee/test` | `EMPLOYEE` | `{"role": "EMPLOYEE", "message": "Employee endpoint accessed"}` | `403 Forbidden` |

---

## 3. Vendor Management Endpoints (`/api/vendors`)

### 3.1 Create Vendor
- **Method**: `POST`
- **Path**: `/api/vendors`
- **Auth Required**: Yes (`ADMIN`)
- **Request Body**:
```json
{
  "code": "VEN-DELHI-001",
  "name": "Delhi Fleet Operations Ltd",
  "taxId": "27AAACD4567E1Z5",
  "contactName": "Vineet Sharma",
  "contactEmail": "contact@delhifleet.com",
  "contactPhone": "+919876543210",
  "address": "Connaught Place, New Delhi"
}
```
- **Success Response** (`201 Created`): Returns created `VendorResponse`.

### 3.2 Get Vendor List (Paginated)
- **Method**: `GET`
- **Path**: `/api/vendors?page=0&size=10`
- **Auth Required**: Yes (`ADMIN`, `HR`, `EMPLOYEE`)
- **Success Response** (`200 OK`): Returns `Page<VendorResponse>`.

---

## 4. Vehicle Management Endpoints (`/api/vehicles`)

### 4.1 Register Vehicle
- **Method**: `POST`
- **Path**: `/api/vehicles`
- **Auth Required**: Yes (`ADMIN`)
- **Request Body**:
```json
{
  "registrationNumber": "DL01AB1234",
  "vendorId": 1,
  "vehicleType": "CAB",
  "make": "Maruti",
  "model": "Dzire",
  "active": true
}
```
- **Success Response** (`201 Created`): Returns created `VehicleResponse`.

### 4.2 Activate / Deactivate Vehicle
- **Method**: `PATCH`
- **Path**: `/api/vehicles/{id}/activate` / `/api/vehicles/{id}/deactivate`
- **Auth Required**: Yes (`ADMIN`)
- **Success Response** (`200 OK`): Returns updated `VehicleResponse`.

---

## 5. Contract & Pricing Version Endpoints (`/api/contracts`)

### 5.1 Create Rate Contract
- **Method**: `POST`
- **Path**: `/api/contracts`
- **Auth Required**: Yes (`ADMIN`, `HR`)
- **Request Body**:
```json
{
  "contractCode": "CNT-2026-001",
  "name": "Delhi Cab Monthly Standard Contract",
  "vendorId": 1,
  "vehicleId": 1,
  "billingType": "PER_KM",
  "startDate": "2026-09-01",
  "endDate": "2026-12-31",
  "status": "ACTIVE"
}
```

### 5.2 Create Contract Version
- **Method**: `POST`
- **Path**: `/api/contracts/{contractId}/versions`
- **Auth Required**: Yes (`ADMIN`, `HR`)
- **Request Body**:
```json
{
  "versionNumber": 1,
  "effectiveFrom": "2026-09-01",
  "effectiveTo": "2026-12-31",
  "billingType": "PER_KM",
  "monthlyFixedFeePaisa": 500000,
  "freeKm": 100,
  "freeHours": 10,
  "overagePerKmPaisa": 1500,
  "overagePerHourPaisa": 10000,
  "nightChargePaisa": 25000,
  "waitingChargePerHourPaisa": 5000,
  "tollHandlingChargePaisa": 0
}
```

### 5.3 Create Pricing Slab
- **Method**: `POST`
- **Path**: `/api/contracts/{contractId}/versions/{versionId}/slabs`
- **Auth Required**: Yes (`ADMIN`, `HR`)
- **Request Body**:
```json
{
  "slabOrder": 1,
  "minValue": 0,
  "maxValue": 500,
  "ratePaisa": 1200,
  "unitType": "KM"
}
```

---

## 6. Trip Recording Endpoints (`/api/trips`)

### 6.1 Record Trip
- **Method**: `POST`
- **Path**: `/api/trips`
- **Auth Required**: Yes (`ADMIN`, `HR`)
- **Request Body**:
```json
{
  "externalTripId": "TRIP-2026-0901",
  "vehicleId": 1,
  "driverName": "Rajesh Kumar",
  "tripDate": "2026-09-15",
  "startTime": "2026-09-15T08:00:00",
  "endTime": "2026-09-15T18:00:00",
  "distanceKm": 150.5,
  "dutyHours": 10.0,
  "waitingHours": 2.0,
  "night": true,
  "tollAmountPaisa": 15000,
  "status": "COMPLETED"
}
```

---

## 7. Billing & Allocation Endpoints (`/api/billing`)

### 7.1 Preview Billing Calculation (Dry-Run)
- **Method**: `POST`
- **Path**: `/api/billing/preview`
- **Auth Required**: Yes (`ADMIN`, `HR`)
- **Request Body**:
```json
{
  "vehicleId": 1,
  "billingMonth": "2026-09"
}
```

### 7.2 Execute Billing Run (Idempotent)
- **Method**: `POST`
- **Path**: `/api/billing/runs`
- **Headers**: `Idempotency-Key: BILL-RUN-2026-09-001`
- **Request Body**:
```json
{
  "vehicleId": 1,
  "billingMonth": "2026-09"
}
```

### 7.3 Allocate Fixed Monthly Fee
- **Method**: `POST`
- **Path**: `/api/billing/runs/{id}/allocate-fixed-fee`
- **Headers**: `Idempotency-Key: FIX-ALLOC-2026-09-001`

---

## 8. Fraud Alert & System Endpoints (`/api/fraud-alerts`, `/api/health`)

| Method | Path | Role | Description |
|:---|:---|:---|:---|
| `GET` | `/api/fraud-alerts` | `ADMIN`, `HR`, `EMPLOYEE` | List anomaly alerts |
| `PATCH` | `/api/fraud-alerts/{id}/resolve` | `ADMIN`, `HR` | Set alert status to `RESOLVED` |
| `PATCH` | `/api/fraud-alerts/{id}/dismiss` | `ADMIN`, `HR` | Set alert status to `DISMISSED` |
| `GET` | `/api/health` | Public | Custom health check (`{"status": "UP"}`) |
| `GET` | `/actuator/health` | Public | Spring Boot Actuator status |

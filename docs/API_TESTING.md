# FleetFlow — API Testing & Postman Evidence Report

## 1. Testing Strategy & Execution Overview

The FleetFlow backend platform was verified using **JUnit 5**, **Mockito**, and manual/automated **Postman API execution**. The test suite validates:
1. End-to-end entity creation lifecycle (`Vendor` ➔ `Vehicle` ➔ `Contract` ➔ `Contract Version` ➔ `Pricing Slab` ➔ `Trip` ➔ `Billing Run` ➔ `Invoice`).
2. Security authorization enforcement (HTTP 200 vs HTTP 403 Forbidden).
3. Field validation rules (HTTP 400 Bad Request).
4. Idempotency execution protection (`Idempotency-Key` header).
5. Correlation ID propagation (`X-Correlation-ID` header).
6. High-performance Redis caching (Cache HIT vs MISS) & Graceful MySQL Fallback.

---

## 2. Tested & Verified API Payloads (Actual Working Parameters)

Below are the exact tested JSON request bodies executed during manual testing:

### 2.1 Create Vendor
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

### 2.2 Create Vehicle
```json
{
  "registrationNumber": "DL01AB1234",
  "vendorId": 2,
  "vehicleType": "CAB",
  "make": "Maruti",
  "model": "Dzire",
  "active": true
}
```

### 2.3 Create Contract
```json
{
  "contractCode": "CNT-2026-001",
  "name": "Delhi Cab Monthly Standard Contract",
  "vendorId": 2,
  "vehicleId": 3,
  "billingType": "PER_KM",
  "startDate": "2026-09-01",
  "endDate": "2026-12-31",
  "status": "ACTIVE"
}
```

### 2.4 Create Contract Version
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

### 2.5 Create Pricing Slab
```json
{
  "slabOrder": 1,
  "minValue": 0,
  "maxValue": 500,
  "ratePaisa": 1200,
  "unitType": "KM"
}
```

### 2.6 Record Trip
```json
{
  "externalTripId": "TRIP-2026-0901",
  "vehicleId": 3,
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

### 2.7 Billing Run Preview & Execution
```json
{
  "vehicleId": 3,
  "billingMonth": "2026-09"
}
```

---

## 3. Postman Evidence Verification Matrix

| Test Case # | Category | HTTP Endpoint Tested | Input Condition | Expected Result | Evidence Screenshot Path |
|:---|:---|:---|:---|:---|:---|
| **TEST-01** | Auth | `POST /api/auth/register` | Role `EMPLOYEE` | `201 Created` with User ID | `screenshots/api-testing/auth/01-register-employee.png` |
| **TEST-02** | Security | `POST /api/auth/register` | Role `ADMIN` | `400 Bad Request` ("Self-registration as ADMIN is not permitted") | `screenshots/api-testing/auth/02-admin-registration-rejected.png` |
| **TEST-03** | Auth | `POST /api/auth/login` | Valid Credentials | `200 OK` with JWT `accessToken` | `screenshots/api-testing/auth/03-admin-login-jwt.png` |
| **TEST-04** | Security | `GET /api/admin/test` | `Bearer {{adminToken}}` | `200 OK` ("Admin endpoint accessed") | `screenshots/api-testing/auth/04-admin-access-allowed.png` |
| **TEST-05** | Security | `GET /api/admin/test` | `Bearer {{employeeToken}}` | `403 Forbidden` | `screenshots/api-testing/auth/05-employee-access-forbidden.png` |
| **TEST-06** | Vendor | `POST /api/vendors` | Code: `VEN-DELHI-001` | `201 Created` with `vendorId: 2` | `screenshots/api-testing/vendors/06-create-vendor.png` |
| **TEST-07** | Vehicle | `POST /api/vehicles` | Vendor ID: `2`, Type: `CAB` | `201 Created` with `vehicleId: 3` | `screenshots/api-testing/vehicles/07-create-vehicle.png` |
| **TEST-08** | Contract | `POST /api/contracts` | Billing Type: `PER_KM` | `201 Created` with `contractId: 3` | `screenshots/api-testing/contracts/08-create-contract.png` |
| **TEST-09** | Contract | `POST /api/contracts/3/versions` | Rates in Paisa | `201 Created` with `versionId: 2` | `screenshots/api-testing/contracts/09-create-contract-version.png` |
| **TEST-10** | Trip | `POST /api/trips` | Distance: `150.5 km` | `201 Created` with `tripId: 2` | `screenshots/api-testing/trips/10-record-trip.png` |
| **TEST-11** | Billing | `POST /api/billing/preview` | Vehicle `3`, Month `2026-09` | `200 OK` with Paisa Calculation Preview | `screenshots/api-testing/billing/11-billing-preview.png` |
| **TEST-12** | Billing | `POST /api/billing/runs` | With `Idempotency-Key` | `201 Created` with Invoice Code | `screenshots/api-testing/billing/12-execute-billing-run.png` |
| **TEST-13** | Billing | `POST /api/billing/runs/5/allocate-fixed-fee` | Retainer Fee | `200 OK` (Largest Remainder Method) | `screenshots/api-testing/billing/13-fixed-fee-allocation.png` |
| **TEST-14** | Fraud | `GET /api/fraud-alerts` | Alert Register | `200 OK` with Anomaly Alerts | `screenshots/api-testing/fraud/14-fraud-alerts-list.png` |
| **TEST-15** | Tracing | `GET /api/vehicles/3` | Header `X-Correlation-ID` | Header preserved in HTTP Response | `screenshots/api-testing/error-handling/15-correlation-id.png` |
| **TEST-16** | Redis | `GET /api/vehicles/3` | 2nd Request (Cache HIT) | Served in ~2ms from Redis (`vehicles::3`) | `screenshots/api-testing/redis/16-redis-cache-hit.png` |
| **TEST-17** | Fallback | `GET /api/vehicles/3` | Redis Container Stopped | `200 OK` (Seamless Fallback to MySQL) | `screenshots/api-testing/redis/17-redis-fallback-mysql.png` |

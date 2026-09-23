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

## 2. Test Execution Summary

```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 91, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 3. Postman Evidence Verification Matrix

| Test Case # | Category | HTTP Endpoint Tested | Input Condition | Expected Result | Evidence Screenshot Path |
|:---|:---|:---|:---|:---|:---|
| **TEST-01** | Auth | `POST /api/auth/register` | Role `EMPLOYEE` | `201 Created` with User ID | `screenshots/api-testing/auth/01-register-employee.png` |
| **TEST-02** | Security | `POST /api/auth/register` | Role `ADMIN` | `400 Bad Request` ("Self-registration as ADMIN is not permitted") | `screenshots/api-testing/auth/02-admin-registration-rejected.png` |
| **TEST-03** | Auth | `POST /api/auth/login` | Valid Admin Credentials | `200 OK` with JWT `accessToken` | `screenshots/api-testing/auth/03-admin-login-jwt.png` |
| **TEST-04** | Security | `GET /api/admin/test` | `Bearer {{adminToken}}` | `200 OK` ("Admin endpoint accessed") | `screenshots/api-testing/auth/04-admin-access-allowed.png` |
| **TEST-05** | Security | `GET /api/admin/test` | `Bearer {{employeeToken}}` | `403 Forbidden` | `screenshots/api-testing/auth/05-employee-access-forbidden.png` |
| **TEST-06** | Vendor | `POST /api/vendors` | Code: `VEN-DELHI-001` | `201 Created` with `vendorId` | `screenshots/api-testing/vendors/06-create-vendor.png` |
| **TEST-07** | Vehicle | `POST /api/vehicles` | Vendor ID: `1`, Type: `CAB` | `201 Created` with `vehicleId` | `screenshots/api-testing/vehicles/07-create-vehicle.png` |
| **TEST-08** | Contract | `POST /api/contracts` | Billing Type: `PER_KM` | `201 Created` with `contractId` | `screenshots/api-testing/contracts/08-create-contract.png` |
| **TEST-09** | Contract | `POST /api/contracts/1/versions` | Rates in Paisa | `201 Created` with `versionId` | `screenshots/api-testing/contracts/09-create-contract-version.png` |
| **TEST-10** | Trip | `POST /api/trips` | Distance: `150.5 km` | `201 Created` with `tripId` | `screenshots/api-testing/trips/10-record-trip.png` |
| **TEST-11** | Billing | `POST /api/billing/preview` | Vehicle `1`, Month `2026-09` | `200 OK` with Paisa Calculation Preview | `screenshots/api-testing/billing/11-billing-preview.png` |
| **TEST-12** | Billing | `POST /api/billing/runs` | With `Idempotency-Key` | `201 Created` with Invoice Code | `screenshots/api-testing/billing/12-execute-billing-run.png` |
| **TEST-13** | Billing | `POST /api/billing/runs/1/allocate-fixed-fee` | Retainer Fee | `200 OK` (Largest Remainder Method) | `screenshots/api-testing/billing/13-fixed-fee-allocation.png` |
| **TEST-14** | Fraud | `GET /api/fraud-alerts` | Alert Register | `200 OK` with Anomaly Alerts | `screenshots/api-testing/fraud/14-fraud-alerts-list.png` |
| **TEST-15** | Tracing | `GET /api/vehicles/1` | Header `X-Correlation-ID` | Header preserved in HTTP Response | `screenshots/api-testing/error-handling/15-correlation-id.png` |
| **TEST-16** | Redis | `GET /api/vehicles/1` | 2nd Request (Cache HIT) | Served in ~2ms from Redis (`vehicles::1`) | `screenshots/api-testing/redis/16-redis-cache-hit.png` |
| **TEST-17** | Fallback | `GET /api/vehicles/1` | Redis Container Stopped | `200 OK` (Seamless Fallback to MySQL) | `screenshots/api-testing/redis/17-redis-fallback-mysql.png` |

---

## 4. Screenshot Organization Directory Tree

Place your captured Postman testing screenshots in the corresponding directories:

```text
screenshots/
├── project/                       <-- Web UI Dashboard Screenshots
├── database/                      <-- MySQL Workbench Schema Screenshots
└── api-testing/
    ├── auth/                      <-- Screenshots 01 - 05
    ├── vendors/                   <-- Screenshot 06
    ├── vehicles/                  <-- Screenshot 07
    ├── contracts/                 <-- Screenshots 08 - 09
    ├── trips/                     <-- Screenshot 10
    ├── billing/                   <-- Screenshots 11 - 13
    ├── fraud/                     <-- Screenshot 14
    ├── error-handling/            <-- Screenshot 15
    └── redis/                     <-- Screenshots 16 - 17
```

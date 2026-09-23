# FleetFlow — Project Overview & Requirements Specification

## 1. Executive Summary

**FleetFlow** (Rental Fleet Billing & Fair Cost Split) is an enterprise SaaS platform engineered for corporate transport and logistics operations. Companies that operate employee shuttle fleets or logistics services hire multiple vehicle vendors operating diverse fleets (cabs, SUVs, vans, buses). Managing vendor rate contracts, contract versioning, trip distance/hourly tracking, monthly billing execution, retainer cost allocation, and fraud monitoring requires robust software architecture.

This document details the business domain, system scope, target users, role-based access control (RBAC), and non-functional requirements.

---

## 2. Business Problem & Key Drivers

### Key Operational Challenges
1. **Diverse Rate Structures**: Vendors operate under different pricing arrangements (`PER_TRIP`, `PER_KM`, `SLAB_BASED`, `MONTHLY_FIXED`).
2. **Contract Version Discrepancies**: Transport rate contracts are updated periodically. Applying retroactively updated rates to historical trips leads to invoice disputes.
3. **Fixed Retainer Allocation**: Vehicles leased on a monthly fixed fee (e.g. ₹50,000/month) must have that retainer allocated across trips run for different corporate departments fairly, without losing rounding paisa.
4. **Duplicate Submissions & Network Failures**: Re-submitting billing runs on weak connections can trigger double payments.
5. **Trip Fraud & Anomalies**: Logged trips with overlapping times for the same vehicle or suspicious mileage need automated detection.

---

## 3. Targeted User Persona & RBAC Roles

The system enforces strict Role-Based Access Control (RBAC) via Spring Security 6 & JWT:

| Role | Target Users | System Capabilities |
|:---|:---|:---|
| `ADMIN` | Fleet Operations Directors, System Managers | Full administrative access: Vendor & Vehicle management, Contract & Pricing Slab creation, Billing execution, Fraud alert resolution, System settings. |
| `HR` | Corporate HR & Logistics Managers | Contract creation, Trip logging, Billing run previews & executions, Fixed fee allocation, Fraud alert management. |
| `EMPLOYEE` | Employees, Drivers, General Viewers | Read-only access: View assigned trips, contracts, billing run summaries, and profile details. |

---

## 4. Functional Requirements Matrix

### Module 1: Authentication & Identity Management
- **FR-1.1**: Public self-registration for standard `EMPLOYEE` users.
- **FR-1.2**: Explicit blockage of self-registration with `ADMIN` role (`HTTP 400 Bad Request`).
- **FR-1.3**: JWT bearer token generation upon successful login with 1-hour expiration (`3600s`).
- **FR-1.4**: Authenticated profile fetch via `GET /api/auth/me`.

### Module 2: Fleet & Vendor Management
- **FR-2.1**: Vendor registration with mandatory GST/Tax ID, unique code, and contact information.
- **FR-2.2**: Vehicle registration associated with a specific vendor and vehicle type (`CAB`, `SUV`, `VAN`, `BUS`).
- **FR-2.3**: Vehicle status lifecycle management (Activate / Deactivate).
- **FR-2.4**: Referential integrity checks (prevent deleting a vendor if vehicles are assigned).

### Module 3: Rate Contracts & Versioning
- **FR-3.1**: Contract creation specifying billing type and date range (`startDate`, `endDate`).
- **FR-3.2**: Version creation within contracts (`versionNumber`, `effectiveFrom`, `effectiveTo`).
- **FR-3.3**: Date overlap validation preventing two versions of the same contract from sharing effective dates.
- **FR-3.4**: Dynamic pricing slab configuration (`minValue`, `maxValue`, `ratePaisa`, `unitType`).

### Module 4: Trip Recording & Duty Logs
- **FR-4.1**: Recording trip details: vehicle ID, driver name, start/end timestamps, distance (km), duty hours, waiting hours, night trip toggle, pass-through toll amount (in paisa).
- **FR-4.2**: Trip status lifecycle (`COMPLETED`, `CANCELLED`).

### Module 5: Billing Engine & Retainer Allocation
- **FR-5.1**: Automated monthly billing calculation dry-run preview (`POST /api/billing/preview`).
- **FR-5.2**: Billing run execution creating itemized invoice records (`POST /api/billing/runs`).
- **FR-5.3**: Integer **paisa** financial precision (₹1.00 = 100 paisa) eliminating floating-point errors.
- **FR-5.4**: Fixed Monthly Retainer allocation across trips using the **Largest Remainder Method** (Hamilton/Vinton Algorithm).

### Module 6: Fraud Detection & Idempotency
- **FR-6.1**: Real-time evaluation of overlapping trip timestamps and unusual mileage anomalies.
- **FR-6.2**: Fraud alert status management (`OPEN`, `RESOLVED`, `DISMISSED`).
- **FR-6.3**: Request idempotency protection via `Idempotency-Key` headers on financial POST endpoints.

---

## 5. Non-Functional Requirements (NFRs)

- **Security**: BCrypt password encoding (strength 10), stateless JWT tokens, CORS policy enforcement, non-root Docker execution.
- **Performance**: Sub-5ms read latency for cached rate contracts via Redis; paginated database queries (`Pageable`, default size 10/20).
- **Resilience**: Redis Cache Error Handler with graceful fallback to MySQL database if Redis is unavailable.
- **Observability**: `X-Correlation-ID` header generation and propagation via SLF4J MDC context; Spring Boot Actuator health checks.
- **Data Integrity**: Database constraints (`UNIQUE` keys on registration numbers, vendor codes, contract codes).

---

## 6. End-to-End Operational Lifecycle

```text
  [ Admin / HR ]             [ Operations ]             [ Billing Engine ]          [ Audit & Finance ]
        │                           │                           │                            │
        ├─► Register Vendor         │                           │                            │
        ├─► Register Vehicle ───────┤                           │                            │
        ├─► Create Contract         │                           │                            │
        └─► Add Pricing Version     │                           │                            │
                                    ├─► Record Completed Trip   │                            │
                                    │                           ├─► Run Monthly Billing ─────┤
                                    │                           ├─► Allocate Fixed Retainer  ├─► Tax Invoice Generated
                                    │                           └─► Trigger Fraud Detection ─┴─► Review Fraud Alerts
```

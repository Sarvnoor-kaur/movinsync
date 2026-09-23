# FleetFlow — Business Logic & Calculation Engines

## 1. Domain Overview

FleetFlow implements specialized domain-driven business logic for:
1. Integer **Paisa** Financial Precision (0 Floating Point Errors).
2. Contract Versioning & Date Overlap Validation.
3. Multi-Strategy Trip Cost Calculations (`PER_KM`, `PER_TRIP`, `SLAB_BASED`, `MONTHLY_FIXED`).
4. Fixed Monthly Retainer Allocation (**Largest Remainder Method**).
5. Automated Fraud & Anomaly Detection.
6. Financial Action Idempotency.

---

## 2. Financial Precision Engine (Paisa Standard)

In financial applications, computing amounts using `float` or `double` introduces IEEE-754 rounding errors (e.g. `0.1 + 0.2 = 0.30000000000000004`).

FleetFlow stores all monetary values in 64-bit integer **Paisa** (`Long` / `BigInt`):
$$\text{Amount in Paisa} = \text{Amount in INR} \times 100$$

### Conversion Examples:
- ₹500.00 = `50000` Paisa
- ₹15.50 / km = `1550` Paisa / km
- ₹1,500.75 Toll = `150075` Paisa

---

## 3. Contract Versioning & Overlap Validation

Contracts undergo rate adjustments. A single Contract contains multiple `ContractVersion` entities.

### Date Range Validation Rule:
When creating or updating a version $[V_{\text{new}}]$ with range $[\text{effectiveFrom}, \text{effectiveTo}]$, it must NOT overlap with any existing version $[V_{\text{existing}}]$.

```text
Overlaps if: (reqStart <= exEnd) AND (exStart <= reqEnd)
```

If an overlap is detected, the system throws `BusinessException("Contract version dates overlap with an existing version")`.

---

## 4. Trip Cost Calculation Engine

When executing a monthly billing run, FleetFlow selects the active `ContractVersion` matching the trip date and evaluates charges:

$$\text{Total Trip Cost} = \text{Base Rate} + \text{Distance Overage} + \text{Hourly Overage} + \text{Night Charge} + \text{Waiting Fee} + \text{Tolls}$$

### Pricing Strategies:

#### 1. `PER_KM` Strategy:
$$\text{Overage Distance} = \max(0, \text{trip.distanceKm} - \text{version.freeKm})$$
$$\text{Distance Cost} = \text{Overage Distance} \times \text{version.overagePerKmPaisa}$$

#### 2. `SLAB_BASED` Strategy:
Finds matching `PricingSlab` where:
$$\text{fromValue} \le \text{trip.distanceKm} \le \text{toValue}$$
$$\text{Slab Cost} = \text{trip.distanceKm} \times \text{slab.ratePaisa}$$

#### 3. Ancillary Fee Additions:
- **Night Charge**: If `trip.isNightTrip = true`, add `version.nightChargePaisa`.
- **Waiting Fee**: $\text{waitingHours} \times \text{version.waitingChargePerHourPaisa}$.
- **Toll Pass-Through**: Add `trip.tollAmountPaisa` (100% pass-through).

---

## 5. Fixed Monthly Fee Allocation (Largest Remainder Method)

When a vehicle is leased on a fixed monthly retainer (e.g. ₹60,000 / month), the retainer must be allocated across all completed trips in that month proportionally based on trip distance.

### The Problem:
Simple integer division causes rounding losses where the sum of allocated trip fees does not equal the total retainer amount.

### The Algorithm (Largest Remainder Method / Hamilton-Vinton Algorithm):

1. **Calculate Total Distance**: $D_{\text{total}} = \sum \text{distance}_i$.
2. **Compute Exact Quotients & Floor Allocations**:
   $$\text{Quotient}_i = \frac{\text{FixedFeePaisa} \times \text{distance}_i}{D_{\text{total}}}$$
   $$\text{AllocatedPaisa}_i = \lfloor \text{Quotient}_i \rfloor$$
   $$\text{Remainder}_i = \text{Quotient}_i - \text{AllocatedPaisa}_i$$
3. **Compute Unallocated Paisa**:
   $$\text{UnallocatedPaisa} = \text{FixedFeePaisa} - \sum \text{AllocatedPaisa}_i$$
4. **Distribute Unallocated Paisa**: Sort trips descending by $\text{Remainder}_i$. Add `1` paisa to top $N$ trips until $\text{UnallocatedPaisa} = 0$.

$$\sum \text{FinalAllocatedPaisa}_i \equiv \text{FixedFeePaisa} \quad (\text{100\% Exact to the Paisa})$$

---

## 6. Fraud & Anomaly Detection Rules

During trip registration and billing execution, the `FraudAlertService` evaluates rules automatically:

```text
Incoming Trip / Billing ──► FraudAlertEvaluators ──► Anomalies Triggered?
                                                            │
                                           ┌────────────────┴────────────────┐
                                           ▼                                 ▼
                                          YES                                NO
                                           ▼                                 ▼
                                  Create FraudAlert                Proceed Cleanly
                                  (OPEN Status)
```

### Evaluated Fraud Rules:

| Fraud Rule | Trigger Condition | Severity |
|:---|:---|:---|
| `OVERLAPPING_TRIP` | Vehicle has two trips with overlapping `startTime` and `endTime`. | `HIGH` |
| `EXCESSIVE_DISTANCE` | Logged trip distance exceeds 800 km in a single duty log. | `MEDIUM` |
| `SPEED_ANOMALY` | Calculated average speed $\frac{\text{distanceKm}}{\text{dutyHours}} > 140 \text{ km/h}$. | `HIGH` |
| `UNUSUAL_WAITING` | Waiting hours exceed logged duty hours (`waitingHours > dutyHours`). | `LOW` |

---

## 7. Request Idempotency Mechanism

To prevent duplicate billing runs caused by network timeouts or double clicks, POST endpoints support an `Idempotency-Key` HTTP header.

```text
POST Request + Header ("Idempotency-Key: BILL-2026-09-001")
 ├─► Hash = SHA-256(Endpoint + Method + RequestBody)
 ├─► Check idempotency_records Table
 │     ├─► Key Found & COMPLETED ──► Return Stored HTTP Response (No Re-computation)
 │     └─► Key Found & IN_PROGRESS ─► Throw 409 Conflict ("Request currently processing")
 └─► Key Not Found ────────────────► Create IN_PROGRESS Record ──► Run Calculation ──► Store COMPLETED Response
```

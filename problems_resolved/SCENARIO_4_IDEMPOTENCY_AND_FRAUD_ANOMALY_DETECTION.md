# Scenario IV: Idempotent Billing Runs & Fraud Anomaly Detection

## 📌 Problem Overview & Business Scenario
In enterprise transport management, two major operational risks exist:
1. **Duplicate Payments from Network Retries**: If an HR manager double-clicks "Run Billing" or encounters a browser timeout, the backend could generate duplicate invoice records and double-charge corporate accounts.
2. **Fraudulent / Miskeyed Duty Logs**: Drivers or vendors can log invalid trips (unmapped trip records, duplicate billed trips, or impossible trip speeds like 250 km/h) to artificially inflate monthly invoices.

---

## 🛠️ How It Is Resolved in FleetFlow

### 1. Idempotency Layer (Run-Twice-Safe)
Every POST write operation accepts an optional `Idempotency-Key` HTTP header. 
- [`IdempotencyService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/IdempotencyService.java) checks the `idempotency_records` table using `REQUIRES_NEW` transactions.
- **First Call**: Saves status `PROCESSING`, calculates invoice, updates to `COMPLETED` with stored JSON.
- **Duplicate Call**: Detects key in `COMPLETED` state and returns cached response instantly without re-executing calculations.

### 2. Automated Fraud & Anomaly Detection Engine
Before a trip is billed, [`FraudDetectionService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/FraudDetectionService.java) runs rule evaluators (`FraudRule`):

```text
Incoming Duty Log ──► FraudEvaluators ──► Anomalies Detected?
                                                 ├──► YES: Log FraudAlert (HIGH/CRITICAL) & Lock Trip
                                                 └──► NO : Proceed Cleanly
```

#### Implemented Rules:
- **`AverageSpeedRule`**: Flags trips where average calculated speed exceeds $150 \text{ km/h}$.
- **`ImpossibleDistanceRule`**: Flags single duty logs exceeding $1,000 \text{ km}$.
- **`ImpossibleDurationRule`**: Flags trips lasting longer than $24 \text{ hours}$.

---

## 💡 Concrete Numerical Example

### Scenario A: Idempotency Protection
- Frontend sends: `POST /api/billing/runs` with header `Idempotency-Key: BILL-2026-SEP-V101`.
- **First Request**: Executes billing engine, generates Invoice #INV-5001, stores result.
- **Second Request (Double click)**: Finds key `BILL-2026-SEP-V101`, returns Invoice #INV-5001 without recalculation. Zero duplicate invoices generated!

### Scenario B: Fraud Catch
- Driver logs a trip: Start = `10:00 AM`, End = `11:00 AM` (1 Hour Duration), Distance = `220 km`.
- Speed = $\frac{220 \text{ km}}{1 \text{ hr}} = 220 \text{ km/h} > 150 \text{ km/h}$.
- `AverageSpeedRule` fires $\rightarrow$ Generates `FraudAlert` (Severity `HIGH`) and locks the trip for HR audit.

---

## 📂 Source Code Locations

| Component | File Path | Method / Responsibility |
|:---|:---|:---|
| **Idempotency Service** | [`IdempotencyService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/IdempotencyService.java#L61-L98) | `executeIdempotently(...)` |
| **Idempotency Entity** | [`IdempotencyRecord.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/IdempotencyRecord.java) | Database table mapping request hash |
| **Fraud Engine** | [`FraudDetectionService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/FraudDetectionService.java) | `evaluateTrips(trips)` |
| **Speed Anomaly Rule** | [`AverageSpeedRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/AverageSpeedRule.java) | Validates $\text{Speed} \le 150 \text{ km/h}$ |
| **Distance Rule** | [`ImpossibleDistanceRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/ImpossibleDistanceRule.java) | Validates $\text{Distance} \le 1000 \text{ km}$ |

# Problem 05: Automated Fraud & Anomaly Detection Engine

## ❌ What Was The Problem?
Drivers or fleet operators can attempt fraudulent claims by keying in exaggerated trip distances, impossible trip speeds, or overlapping duty logs across multiple cabs to artificially maximize monthly payouts.

---

## ✅ How Was It Resolved? (Simple Explanation)
FleetFlow incorporates an automated **Fraud Detection Engine** (`FraudDetectionService`) using the Strategy Pattern (`FraudRule`). When trips are logged or processed during billing runs, they are evaluated against active fraud constraints. Identified anomalies trigger a `FraudAlert` record with severity (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) and lock the trip from auto-approval.

---

## 💡 Evaluated Fraud Rules:
1. **`AverageSpeedRule`**: Flags trips where average speed exceeds $150 \text{ km/h}$.
   $$\text{Average Speed} = \frac{\text{Distance in Km}}{\text{Duty Hours}}$$
2. **`ImpossibleDistanceRule`**: Flags single duty logs exceeding $1,000 \text{ km}$.
3. **`ImpossibleDurationRule`**: Flags single continuous duty logs exceeding $24 \text{ hours}$.

---

## 💡 Concrete Real-World Example
- A driver logs a trip: Start = `10:00 AM`, End = `11:00 AM` (1 Hour Duration), Distance = `220 km`.
- Calculated speed: $\frac{220 \text{ km}}{1 \text{ hr}} = 220 \text{ km/h} > 150 \text{ km/h}$.
- `AverageSpeedRule` fires $\rightarrow$ Generates `FraudAlert` (Severity: `HIGH`, Status: `OPEN`) and alerts the HR transport dashboard.

---

## 📄 Source Code Implementation
- Fraud Detection Engine Core: [`FraudDetectionService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/FraudDetectionService.java)
- Speed Anomaly Rule: [`AverageSpeedRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/AverageSpeedRule.java)
- Impossible Distance Rule: [`ImpossibleDistanceRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/ImpossibleDistanceRule.java)
- Impossible Duration Rule: [`ImpossibleDurationRule.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/ImpossibleDurationRule.java)

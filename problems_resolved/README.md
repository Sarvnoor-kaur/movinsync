# 🛠️ MovInSync (FleetFlow) — Problems Resolved Directory

Welcome to the **`problems_resolved`** folder. This folder contains dedicated explanations for all the major engineering, financial, operational, and security problems solved in the MovInSync / FleetFlow project, structured into 4 key interview presentation scenarios.

---

## 📌 Master Problems Resolved Index

| Scenario # | Scenario Documentation File | Key Feature Covered | Primary Source Code File |
|---|:---|:---|:---|
| **Scenario I** | [`SCENARIO_1_CONTRACT_TYPES_AND_TIERED_SLABS.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/SCENARIO_1_CONTRACT_TYPES_AND_TIERED_SLABS.md) | **Multi-Contract Models & Tiered Slabs**: Per-km, per-trip, fixed retainer, overage rates, night, waiting, toll fees. | [`PerKmPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PerKmPricingStrategy.java) |
| **Scenario II** | [`SCENARIO_2_BEST_VALID_PRICING_OPTION_SELECTION.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/SCENARIO_2_BEST_VALID_PRICING_OPTION_SELECTION.md) | **Best Valid Pricing Option Selection**: Deterministic fare calculation and selecting the minimum-cost valid option. | [`BestPricingSelector.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/BestPricingSelector.java) |
| **Scenario III** | [`SCENARIO_3_FIXED_MONTHLY_FEE_ALLOCATION_AND_RECONCILIATION.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/SCENARIO_3_FIXED_MONTHLY_FEE_ALLOCATION_AND_RECONCILIATION.md) | **Monthly Retainer Split & Exact Reconciliation**: Splitting ₹30,000 fixed fee across trips using **Largest Remainder Method** to exact paisa. | [`FixedFeeAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/allocation/FixedFeeAllocationService.java) |
| **Scenario IV** | [`SCENARIO_4_IDEMPOTENCY_AND_FRAUD_ANOMALY_DETECTION.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/SCENARIO_4_IDEMPOTENCY_AND_FRAUD_ANOMALY_DETECTION.md) | **Idempotent Billing Runs & Fraud Checks**: Run-twice-safe billing via `Idempotency-Key` and speed/distance anomaly flagging. | [`IdempotencyService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/IdempotencyService.java) |

---

## 💡 Quick Overview Matrix

```text
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │                         FLEETFLOW SCENARIOS RESOLVED                        │
  ├──────────────────────┬───────────────────────────────┬──────────────────────┤
  │ Scenario I           │ Contract Types & Tiered Slabs │ Strategy Pattern     │
  │ Scenario II          │ Best Valid Pricing Selection  │ Deterministic Min    │
  │ Scenario III         │ Fixed Fee Proportional Split  │ Largest Remainder    │
  │ Scenario IV          │ Idempotency & Fraud Checks    │ Header SHA256 & Rules│
  └──────────────────────┴───────────────────────────────┴──────────────────────┘
```

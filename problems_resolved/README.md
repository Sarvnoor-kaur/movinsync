# 🛠️ MovInSync (FleetFlow) — Problems Resolved Directory

Welcome to the **`problems_resolved`** folder. This folder contains dedicated explanations for all the major engineering, financial, operational, and security problems solved in the MovInSync / FleetFlow project.

---

## 📌 Master Problems Resolved Index

Each problem has a dedicated document in this folder explaining **the problem statement**, **the business impact**, **the step-by-step mathematical/code solution**, **concrete examples**, and **exact source file links**.

| # | File Name | Technical Problem | Primary Source Code File |
|---|:---|:---|:---|
| **01** | [`01_FINANCIAL_ROUNDING_PAISA_PRECISION.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/01_FINANCIAL_ROUNDING_PAISA_PRECISION.md) | **Floating-Point Rounding Drift**: Eliminates off-by-one paisa errors in invoices. | [`PerKmPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PerKmPricingStrategy.java) |
| **02** | [`02_MONTHLY_RETAINER_SPLIT_LARGEST_REMAINDER.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/02_MONTHLY_RETAINER_SPLIT_LARGEST_REMAINDER.md) | **Monthly Retainer Split Mismatch**: Splits vehicle fixed retainers fairly without losing 1 paisa using the **Largest Remainder Method**. | [`FixedFeeAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/allocation/FixedFeeAllocationService.java) |
| **03** | [`03_CONTRACT_VERSIONING_OVERLAP_PREVENTION.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/03_CONTRACT_VERSIONING_OVERLAP_PREVENTION.md) | **Mid-Month Rate Contract Disputes**: Prevents retroactive billing disputes when rates change. | [`ContractVersionService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/ContractVersionService.java) |
| **04** | [`04_IDEMPOTENCY_DUPLICATE_INVOICE_PREVENTION.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/04_IDEMPOTENCY_DUPLICATE_INVOICE_PREVENTION.md) | **Duplicate Invoices from Retries**: Prevents double charges from network timeouts or double clicks using `Idempotency-Key` headers. | [`IdempotencyService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/IdempotencyService.java) |
| **05** | [`05_AUTOMATED_FRAUD_ANOMALY_DETECTION.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/05_AUTOMATED_FRAUD_ANOMALY_DETECTION.md) | **Driver Fraud & Miskeyed Logs**: Flags speed anomalies (>150 km/h) or impossible trip durations. | [`FraudDetectionService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/fraud/FraudDetectionService.java) |
| **06** | [`06_REDIS_CACHE_HIGH_AVAILABILITY.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/06_REDIS_CACHE_HIGH_AVAILABILITY.md) | **Redis Outage Service Failure**: Prevents API crashes when Redis goes offline by falling back to MySQL. | [`RedisConfig.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/config/RedisConfig.java) |
| **07** | [`07_LOG_TRACEABILITY_CORRELATION_ID.md`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/problems_resolved/07_LOG_TRACEABILITY_CORRELATION_ID.md) | **Log Traceability Across Requests**: Binds `X-Correlation-ID` headers to log entries across all services. | [`CorrelationIdFilter.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/filter/CorrelationIdFilter.java) |

---

## 💡 Quick Overview of Architecture & Solutions

```text
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │                         FLEETFLOW PROBLEMS RESOLVED                         │
  ├──────────────────────┬───────────────────────────────┬──────────────────────┤
  │ Financial Accuracy   │ Zero Rounding Errors          │ Integer Paisa Standard│
  │ Retainer Allocation  │ Reconciled Monthly Split      │ Largest Remainder    │
  │ Contract Governance  │ Date Overlap Prevention       │ Versioned Range      │
  │ Idempotency          │ Prevent Duplicate Invoices    │ SHA-256 DB Records   │
  │ Security & Fraud     │ Detect Excessive Speed/Dist   │ Rule Evaluator Engine│
  │ High Availability    │ Resilient Redis Fallback      │ Custom Cache Handler │
  │ Observability        │ End-to-End Log Correlation    │ MDC Filter           │
  └──────────────────────┴───────────────────────────────┴──────────────────────┘
```

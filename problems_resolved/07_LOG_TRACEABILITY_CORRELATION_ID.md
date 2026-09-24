# Problem 07: Observability & Log Correlation Filter

## ❌ What Was The Problem?
In a complex Spring Boot application handling authentication, vendor management, billing runs, and background tasks, tracing log statements belonging to a specific user request was tedious and difficult when debugging errors across massive log files.

---

## ✅ How Was It Resolved? (Simple Explanation)
We introduced a custom HTTP servlet filter [`CorrelationIdFilter.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/filter/CorrelationIdFilter.java):
1. Inspects each incoming HTTP request for an `X-Correlation-ID` header.
2. If absent, generates a unique UUID (e.g. `req-a1b2c3d4`).
3. Injects the ID into SLF4J MDC (Mapped Diagnostic Context).
4. Includes the correlation ID in every log statement and passes it back in the HTTP response header.

---

## 💡 Concrete Real-World Example Log Output
```text
2026-09-24 09:10:15.102 [req-8f92a10c] INFO  c.f.c.BillingController - Received request to execute billing run
2026-09-24 09:10:15.145 [req-8f92a10c] INFO  c.f.b.FixedFeeAllocationService - Allocating fixed retainer ₹30,000
2026-09-24 09:10:15.201 [req-8f92a10c] INFO  c.f.s.BillingService - Generated invoice INV-2026-001 successfully
```

---

## 📄 Source Code Implementation
- Correlation ID Filter: [`CorrelationIdFilter.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/filter/CorrelationIdFilter.java)

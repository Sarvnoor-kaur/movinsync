# Problem 04: Financial Action Idempotency (Prevent Duplicate Invoices)

## ❌ What Was The Problem?
Monthly billing execution involves running pricing strategies across hundreds of trips. If a network latency spike occurs or an HR admin double-clicks the "Execute Monthly Billing" button, multiple HTTP requests could reach the backend concurrently, triggering duplicate invoice records and double payouts.

---

## ✅ How Was It Resolved? (Simple Explanation)
We implemented database-backed HTTP Idempotency using the `Idempotency-Key` header in [`IdempotencyService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/IdempotencyService.java):

```text
POST /api/billing/runs [Header: Idempotency-Key: BILL-2026-09-V101]
 │
 ├──► Hash Fingerprint = SHA-256(Path + Method + Body)
 ├──► Lookup idempotency_records Table
 │     ├──► Status: COMPLETED ──────► Return cached HTTP response immediately (Skip computation)
 │     ├──► Status: PROCESSING ─────► Throw 409 Conflict ("Operation currently processing")
 │     └──► Not Found ──────────────► Insert PROCESSING record (REQUIRES_NEW Transaction)
 │                                          │
 │                                          ▼
 │                                   Execute Billing Engine
 │                                          │
 │                                          ▼
 └─────────────────────────────────── Update Record Status to COMPLETED + Store Response JSON
```

---

## 💡 Concrete Real-World Example
- Admin submits billing run with header `Idempotency-Key: BILL-2026-09-CAB01`.
- **Execution 1**: Creates `PROCESSING` record, calculates trip totals, generates Invoice #INV-8801, updates DB record to `COMPLETED`.
- **Execution 2 (Double click 5 seconds later)**: Reads key `BILL-2026-09-CAB01` from DB, finds status `COMPLETED`, returns stored Invoice #INV-8801 payload without modifying database state.

---

## 📄 Source Code Implementation
- Idempotency Layer Service: [`IdempotencyService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/IdempotencyService.java#L61-L98)
- Idempotency Entity: [`IdempotencyRecord.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/IdempotencyRecord.java)

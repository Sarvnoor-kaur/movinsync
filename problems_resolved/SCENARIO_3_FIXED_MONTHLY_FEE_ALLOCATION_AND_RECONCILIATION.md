# Scenario III: Splitting Fixed Monthly Fees & Exact Paisa Reconciliation

## 📌 Problem Overview & Business Scenario
Corporate fleets frequently lease cabs on a fixed monthly retainer (e.g. ₹30,000 / month per vehicle regardless of trip count). To charge internal corporate departments fairly, this ₹30,000 retainer must be distributed across all completed duty logs (including empty "dead-leg" running) based on each trip's distance share.

### The Rounding Problem:
Standard integer division drops fractional remainders. Simple division causes total allocated trip fees to sum to ₹29,998.50 instead of ₹30,000.00, resulting in un-reconciled monthly invoice audit errors.

---

## 🛠️ How It Is Resolved in FleetFlow

We implemented the **Hamilton/Vinton Largest Remainder Algorithm** in [`FixedFeeAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/allocation/FixedFeeAllocationService.java):

```text
 ┌────────────────────────────────────────────────────────────────────────┐
 │ Step 1: Calculate total weight D_total = sum(distance_i)               │
 ├────────────────────────────────────────────────────────────────────────┤
 │ Step 2: Compute exact fractional share:                                │
 │         ExactShare_i = (FixedFeePaisa * distance_i) / D_total          │
 ├────────────────────────────────────────────────────────────────────────┤
 │ Step 3: Floor each share: Floor_i = floor(ExactShare_i)                │
 │         Remainder_i = ExactShare_i - Floor_i                           │
 ├────────────────────────────────────────────────────────────────────────┤
 │ Step 4: LeftoverPaisa = FixedFeePaisa - sum(Floor_i)                   │
 ├────────────────────────────────────────────────────────────────────────┤
 │ Step 5: Sort trips descending by Remainder_i                           │
 │         Distribute +1 paisa to the top LeftoverPaisa trips             │
 └────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 Concrete Numerical Example

### Scenario:
- **Vehicle Retainer**: ₹30,000.00 (`3,000,000 paisa`)
- **Total Duty Distance**: 1,200 km across 3 trips (Trip 1 = 400 km, Trip 2 = 400 km, Trip 3 = 400 km).

### Execution:
- Trip 1: Exact Share = $3,000,000 \times \frac{400}{1200} = 1,000,000.0 \rightarrow \text{Allocated} = 1,000,000 \text{ paisa (₹10,000.00)}$
- Trip 2: Exact Share = $3,000,000 \times \frac{400}{1200} = 1,000,000.0 \rightarrow \text{Allocated} = 1,000,000 \text{ paisa (₹10,000.00)}$
- Trip 3: Exact Share = $3,000,000 \times \frac{400}{1200} = 1,000,000.0 \rightarrow \text{Allocated} = 1,000,000 \text{ paisa (₹10,000.00)}$

### Leftover Handling (Uneven Division):
If a cab retainer of ₹100.00 (`10,000 paisa`) is split across 3 equal trips (33.3333% share each):
- Trip 1 Floor = 3333 paisa, Remainder = `0.3333`
- Trip 2 Floor = 3333 paisa, Remainder = `0.3333`
- Trip 3 Floor = 3333 paisa, Remainder = `0.3333`
- Floor Sum = 9,999 paisa $\rightarrow$ Leftover = **1 paisa**.
- Leftover +1 paisa assigned to Trip 1.
- **Final Sum**: $3334 + 3333 + 3333 = 10,000 \text{ paisa (₹100.00 exact!)}$

---

## 📂 Source Code Locations

| Component | File Path | Method / Responsibility |
|:---|:---|:---|
| **Largest Remainder Allocator** | [`FixedFeeAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/allocation/FixedFeeAllocationService.java#L35-L128) | `allocate(trips, fixedFeePaisa, strategy)` |
| **Billing Allocation Orchestrator** | [`BillingAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/BillingAllocationService.java) | Integrates monthly runs with allocation results |
| **Reconciliation Assertion** | [`BillingService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/BillingService.java#L155-L162) | Asserts $\sum \text{InvoiceItems} == \text{Invoice.Total}$ |

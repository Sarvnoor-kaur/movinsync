# Problem 02: Monthly Retainer Split & Allocation Loss

## ❌ What Was The Problem?
Corporate clients often lease dedicated vehicles from transport vendors under a fixed monthly retainer contract (e.g., ₹30,000 / month per cab). To bill internal corporate departments fairly, this ₹30,000 must be allocated across all trips completed by that cab during the month based on trip distance.

Simple integer division drops fractional remainders. As a result, the sum of all allocated trip charges fails to equal the total monthly retainer fee ($\sum \text{Allocated Trip Paisa} \ne \text{Retainer Paisa}$), creating un-reconciled billing discrepancies.

---

## ✅ How Was It Resolved? (Simple Explanation)
We implemented the **Hamilton-Vinton Largest Remainder Algorithm** in [`FixedFeeAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/allocation/FixedFeeAllocationService.java):

```text
 ┌────────────────────────────────────────────────────────────────────────┐
 │ Step 1: Calculate total weight (Total Distance D_total)                │
 ├────────────────────────────────────────────────────────────────────────┤
 │ Step 2: Compute exact fractional share for each trip                   │
 │         ExactShare_i = (FixedFeePaisa * Distance_i) / D_total          │
 ├────────────────────────────────────────────────────────────────────────┤
 │ Step 3: Floor each share to integer paisa: Floor_i = floor(ExactShare) │
 │         Remainder_i = ExactShare_i - Floor_i                           │
 ├────────────────────────────────────────────────────────────────────────┤
 │ Step 4: Sum all floors: FloorSum = sum(Floor_i)                       │
 │         LeftoverPaisa = FixedFeePaisa - FloorSum                       │
 ├────────────────────────────────────────────────────────────────────────┤
 │ Step 5: Sort trips descending by Remainder_i                           │
 │         Add +1 paisa to the top LeftoverPaisa trips                    │
 └────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 Concrete Real-World Example
- **Monthly Retainer**: ₹100.00 (10,000 paisa)
- **Completed Trips**: 3 trips with distances 10 km, 10 km, and 10 km.
- **Exact Shares**:
  - Trip 1 (10km): $10000 \times \frac{10}{30} = 3333.3333... \text{ paisa} \rightarrow \text{Floor} = 3333$, Remainder = `0.3333`
  - Trip 2 (10km): $10000 \times \frac{10}{30} = 3333.3333... \text{ paisa} \rightarrow \text{Floor} = 3333$, Remainder = `0.3333`
  - Trip 3 (10km): $10000 \times \frac{10}{30} = 3333.3333... \text{ paisa} \rightarrow \text{Floor} = 3333$, Remainder = `0.3333`
- **Floor Sum**: $3333 + 3333 + 3333 = 9999 \text{ paisa}$.
- **Unallocated Leftover**: $10000 - 9999 = 1 \text{ paisa}$.
- **Distribute Leftover**: Assign +1 paisa to Trip 1.
- **Final Split**:
  - Trip 1 = 3334 paisa (₹33.34)
  - Trip 2 = 3333 paisa (₹33.33)
  - Trip 3 = 3333 paisa (₹33.33)
- **Reconciliation**: $3334 + 3333 + 3333 = 10000 \text{ paisa (₹100.00 exact)}$!

---

## 📄 Source Code Implementation
- Service Algorithm Implementation: [`FixedFeeAllocationService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/allocation/FixedFeeAllocationService.java#L35-L128)

# Problem 03: Rate Contract Versioning & Dispute Prevention

## ❌ What Was The Problem?
Transport vendors frequently renegotiate fuel prices and per-kilometer rates mid-month. Modifying rate structures directly in existing contract database records causes retroactive changes—trips logged earlier in the month get recalculated using the new rates, sparking financial disputes between vendors and corporate HR.

---

## ✅ How Was It Resolved? (Simple Explanation)
FleetFlow decouples contract definitions from rate configurations by introducing a date-ranged `ContractVersion` entity.
1. Contracts support multiple versions (`effectiveFrom` to `effectiveTo`).
2. Every trip fare calculation resolves the contract version active on the exact timestamp of the trip.
3. When creating or updating a version, the system executes date-range overlap validation:

$$\text{Overlap Condition: } (reqStart \le exEnd) \land (exStart \le reqEnd)$$

---

## 💡 Concrete Real-World Example
- **Version 1**: Effective `2026-01-01` to `2026-01-15` (₹15 / km).
- **Version 2**: Effective `2026-01-16` to `2026-01-31` (₹18 / km).
- A trip completed on **Jan 10** automatically evaluates against Version 1 (billed at ₹15/km).
- A trip completed on **Jan 20** automatically evaluates against Version 2 (billed at ₹18/km).
- If an admin attempts to insert Version 3 with dates `2026-01-12` to `2026-01-22`, the system rejects it with `BusinessException: Contract version dates overlap with an existing version`.

---

## 📄 Source Code Implementation
- Contract Version Service & Overlap Check: [`ContractVersionService.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/service/ContractVersionService.java#L32-L100)

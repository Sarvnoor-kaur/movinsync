# Scenario II: Best Valid Pricing Option Selection & Determinism

## 📌 Problem Overview & Business Scenario
When a contract allows multiple valid pricing methods (e.g. flat rate per kilometer vs. progressive tiered slabs), selecting the rate structure manually introduces human bias, inconsistency, and customer dissatisfaction if a higher-cost option is applied arbitrarily.

Furthermore, billing calculations must be **100% deterministic**—the exact same trip date, vehicle distance, and contract version must produce the exact same fare regardless of when or where the billing run is executed.

---

## 🛠️ How It Is Resolved in FleetFlow

### 1. Generating Valid Contract Options
`PricingOptionGenerator` inspects the active `ContractVersion` and generates **only** those pricing options permitted by the contract configuration (e.g., `FLAT_PER_KM` vs. `PROGRESSIVE_SLAB`). Arbitrary or unconfigured options are never generated.

### 2. Minimum-Cost Selection
`BestPricingSelector` computes the total fare for every valid option and selects the option with the lowest total charge in paisa:
```java
PricingOption best = options.stream()
        .min(Comparator.comparingLong(PricingOption::getTotalChargePaisa)
                .thenComparingInt(PricingOption::getPriority)
                .thenComparing(PricingOption::getOptionName))
        .orElseThrow();
```

### 3. Deterministic Tie-Breaking
If two valid options yield the exact same total cost, the selector applies a deterministic tie-breaker:
1. Priority order (`PROGRESSIVE_SLAB` = 1, `FLAT_PER_KM` = 2, `PER_TRIP` = 3, `FIXED_MONTHLY` = 4).
2. Option name alphabetical sorting.

---

## 💡 Concrete Numerical Example

### Given Trip: 1,500 km duty log.

- **Option A (Flat Rate)**: Contract permits flat rate of ₹15/km (`1500 paisa/km`).
  $$\text{Fare} = 1500 \text{ km} \times 1500 \text{ paisa} = 2,250,000 \text{ paisa (₹22,500.00)}$$

- **Option B (Progressive Slabs)**: Contract permits tiered slabs (0–1000 km @ ₹15/km, 1001–3000 km @ ₹13/km).
  $$\text{Slab 1} = 1001 \text{ km} \times 1500 = 1,501,500 \text{ paisa}$$
  $$\text{Slab 2} = 499 \text{ km} \times 1300 = 648,700 \text{ paisa}$$
  $$\text{Total Option B} = 1,501,500 + 648,700 = 2,150,200 \text{ paisa (₹21,502.00)}$$

### Result:
The engine compares Option A (₹22,500) and Option B (₹21,502) and deterministically selects **Option B (PROGRESSIVE_SLAB)** as the minimum-cost valid option, saving the customer ₹998.

---

## 📂 Source Code Locations

| Component | File Path | Method / Responsibility |
|:---|:---|:---|
| **Option Generator** | [`PricingOptionGenerator.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PricingOptionGenerator.java) | `generateValidOptions(trip, contractVersion, slabs)` |
| **Best Option Selector** | [`BestPricingSelector.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/BestPricingSelector.java#L30-L50) | `selectBestOption(options)` |
| **Pricing Engine Orchestrator** | [`PricingEngine.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PricingEngine.java#L42-L78) | Combines options, selects best, logs breakdown |
| **Unit Test Suite** | [`BestPricingOptionTest.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/test/java/com/fleetbilling/billing/BestPricingOptionTest.java) | Verifies option selection & determinism |

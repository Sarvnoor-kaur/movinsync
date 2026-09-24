# Scenario I: Contract Charging Models & Tiered Slabs Support

## 📌 Problem Overview & Business Scenario
Enterprise B2B fleet transport vendors operate under diverse billing models:
1. **Per-Km Billing**: Charging per kilometer driven, often with tiered rate slabs where rates decrease as distance increases.
2. **Per-Trip Billing**: Charging flat rates per completed duty log.
3. **Fixed Monthly Retainer Package**: Dedicated cab lease with free kilometer and hour allowances, charging overage per extra kilometer or hour.
4. **Ancillary / Extra Charges**: Surcharges for night driving, waiting hours, and pass-through toll fees.

Without a unified engine, managing these multi-vendor contract models leads to manual invoicing errors, missing overage calculations, and rate disputes.

---

## 🛠️ How It Is Resolved in FleetFlow

### 1. Multi-Strategy Charging Engine
We implemented the **Strategy Design Pattern** where `PricingEngine` delegates calculation to dedicated strategies based on the contract's `BillingType` enum (`PER_KM`, `PER_TRIP`, `FIXED_MONTHLY`).

### 2. Tiered Rate Slab Processing
For distance-based contracts, `PerKmPricingStrategy` consumes trip distance sequentially across ordered pricing slabs:
```java
BigDecimal inSlab = slabWidth != null ? remaining.min(slabWidth) : remaining;
long slabCharge = inSlab.multiply(BigDecimal.valueOf(slab.getRatePaisa()))
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValue();
totalPaisa += slabCharge;
remaining = remaining.subtract(inSlab);
```

### 3. Extra Charges Calculation
- **Night Charge**: Added if `trip.getNight() == true` using `version.getNightChargePaisa()`.
- **Waiting Fee**: Computed as $\text{waitingHours} \times \text{waitingChargePerHourPaisa}$ rounded to whole paisa using `RoundingMode.HALF_UP`.
- **Toll Pass-Through**: 100% pass-through of `trip.getTollAmountPaisa()`.

---

## 💡 Concrete Numerical Example

### Contract Setup:
- Slab 1 (0–100 km): ₹50 / km (`5000 paisa/km`)
- Slab 2 (101–200 km): ₹45 / km (`4500 paisa/km`)
- Slab 3 (201+ km): ₹40 / km (`4000 paisa/km`)
- Extra Charges: Night Charge = ₹500 (`50000 paisa`), Toll = ₹150 (`15000 paisa`).

### Trip: 150 km duty log at night with ₹150 toll.
1. **First 100 km** (Slab 1): $100 \times 5000 = 500,000 \text{ paisa (₹5,000.00)}$
2. **Next 50 km** (Slab 2): $50 \times 4500 = 225,000 \text{ paisa (₹2,250.00)}$
3. **Base Charge Total**: $500,000 + 225,000 = 725,000 \text{ paisa (₹7,250.00)}$
4. **Extra Charges**: Night (₹500) + Toll (₹150) = ₹650 (`65000 paisa`)
5. **Grand Total**: $725,000 + 650,000 = 790,000 \text{ paisa } (\text{₹}7,900.00 \text{ exact})$

---

## 📂 Source Code Locations

| Component | File Path | Method / Responsibility |
|:---|:---|:---|
| **Pricing Engine** | [`PricingEngine.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PricingEngine.java#L42-L80) | `calculate(trip, contractVersion)` |
| **Per-Km Tiered Slabs** | [`PerKmPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PerKmPricingStrategy.java#L28-L73) | `calculateBaseChargePaisa(...)` |
| **Per-Trip Flat Strategy** | [`PerTripPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PerTripPricingStrategy.java#L20-L32) | `calculateBaseChargePaisa(...)` |
| **Pricing Slab Entity** | [`PricingSlab.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/PricingSlab.java) | Tier range boundaries (`fromValue`, `toValue`, `ratePaisa`) |

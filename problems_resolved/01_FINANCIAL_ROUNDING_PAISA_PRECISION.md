# Problem 01: Floating-Point Financial Rounding & Paisa Precision

## ❌ What Was The Problem?
Standard primitive floating-point data types (`float` and `double`) use IEEE-754 binary floating-point representation. They cannot accurately represent base-10 decimal fractions like `0.1` or `0.2` in binary. For instance:
```java
double value = 0.1 + 0.2; // Yields 0.30000000000000004
```

In B2B enterprise fleet transport billing, processing tens of thousands of monthly trips using floating-point math leads to cumulative rounding drift. Invoices end up off by a few paisa/cents when comparing line item totals against master invoice grand totals, triggering audit rejections by corporate finance teams.

---

## ✅ How Was It Resolved? (Simple Explanation)
We completely eliminated `double` and `float` for monetary calculations across the system. 
1. All monetary fields in entities, DTOs, and database schema store money in **Integer Paisa** as `long` or `Long` values ($1 \text{ INR} = 100 \text{ Paisa}$).
2. No floating-point variables are permitted anywhere in pricing calculations.
3. Intermediate scaling and division utilize `BigDecimal` configured with `MathContext.DECIMAL128` (34 digits precision) before explicitly rounding to long integer paisa.

$$\text{Monetary Value in Paisa} = \text{Rupees} \times 100$$

---

## 💡 Concrete Real-World Example
- **Scenario**: A vehicle contract sets a rate of ₹15.50 / km. A cab completes a duty log of 10.5 km.
- **Floating-point risk**: $15.50 \times 10.5 = 162.75$, but float arithmetic can yield `162.74999999999997`.
- **FleetFlow Integer Paisa Execution**:
  $$\text{Rate} = 1550 \text{ Paisa/km}$$
  $$\text{Distance} = 1050 \text{ (scaled by 100)}$$
  $$\text{Trip Fare Paisa} = \frac{1050 \times 1550}{100} = 16275 \text{ Paisa } (\text{₹}162.75 \text{ exact})$$

---

## 📄 Source Code Implementation
- Primary calculation logic: [`PerKmPricingStrategy.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/billing/PerKmPricingStrategy.java)
- Database Entity mappings: [`PricingSlab.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/PricingSlab.java), [`ContractVersion.java`](file:///c:/Users/sarvn/OneDrive/Desktop/movinsync/fleet-billing/src/main/java/com/fleetbilling/entity/ContractVersion.java)

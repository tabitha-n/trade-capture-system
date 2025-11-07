# Step 4: Bug Investigation and Fix (TRD-2025-001)

## 1. Overview
The `calculateCashflowValue()` method in `TradeService.java` produced cashflow values **~100× larger than expected** for fixed-rate trades.

**Example:**  
- A $10,000,000 notional trade at **3.5%** with a quarterly payment produced **~$875,000** instead of the correct **$87,500**.

This issue affected financial reporting and settlement, so a minimal but targeted fix was implemented.

---

## 2. Root Cause Summary

| Issue | Description | Impact |
|-------|-------------|--------|
| Percentage Conversion Bug | Rate (e.g., `3.5`) was used directly instead of converting to decimal (`0.035`). | Cashflow values were ~100× too large. |
| Precision Bug | Arithmetic performed using `double` instead of `BigDecimal`. | Rounding and precision inconsistencies. |

> **Note on Decimals:** Intermediate calculations use **10 decimal places** (VS Code autosuggestion). Final cashflow values are rounded to **2 decimal places** for currency display.

---

## 3. Fix Summary

| Category | Before Fix | After Fix |
|----------|------------|-----------|
| Rate Handling | Raw rate used (e.g., `3.5`) | Rate divided by 100 → `0.035` (decimal) |
| Data Type | `double` arithmetic | `BigDecimal` for precise arithmetic |
| Intermediate Precision | `6` decimal places | `10` decimal places (to avoid truncation) |
| Final Rounding | No consistent rounding | `setScale(2, RoundingMode.HALF_UP)` for money |
| Default Safety | `BigDecimal.ZERO` (unscaled) | `BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)` |

---

## 4. Method Reference
**File:** `TradeService.java`  
**Method:** `calculateCashflowValue(TradeLeg leg, int monthsInterval)`

Behavior summary:
- Converts `leg.getRate()` from percent to decimal.
- Computes year fraction from `monthsInterval` (e.g., `3 / 12 = 0.25`).
- Calculates `cashflow = notional × rate × yearFraction`.
- Uses `BigDecimal` throughout with intermediate precision = 10 decimals.
- Returns final cashflow rounded to 2 decimals using `RoundingMode.HALF_UP`.

(Implementation already present in `TradeService.java`; documentation references it rather than duplicating code.)

---

## 5. Visual Formula Comparison

| Step | Before Fix (Incorrect) | After Fix (Correct) |
|------|------------------------|---------------------|
| Input Example | Notional = 10,000,000<br>Rate = 3.5<br>Months = 3 | Notional = 10,000,000<br>Rate = 3.5<br>Months = 3 |
| Formula Used | `10,000,000 × 3.5 × 3 ÷ 12` | `10,000,000 × (3.5 ÷ 100) × (3 ÷ 12)` |
| Calculation Result | `875,000.00` | `87,500.00` |
| Error Cause | Rate not converted to decimal | Rate correctly converted |
| Data Type Used | `double` | `BigDecimal` (intermediate precision = 10) |

---

## 6. Testing Summary

### Unit Test
- **Name:** `testCalculateCashflowValue_FixedLeg_ShouldPassWithoutException`  
- **Scenario:** Fixed leg, $10,000,000 notional, 3.5% rate, 3-month interval  
- **Expected Result:** `$87,500.00`  
- **Outcome:** ✅ Pass

### Integration Test
- **Name:** `testCashflowGeneration_calculateCashflowValue`  
- **Purpose:** Ensure the formula integrates correctly into the cashflow generation flow (createTrade -> cashflow saves)  
- **Outcome:** ✅ Pass — no regression detected

---

## 7. Verification Example

| Input | Output | Verified |
|-------|--------|---------|
| Notional = 10,000,000 | $87,500.00 | ✅ Correct |
| Rate = 3.5% |  |  |
| Months = 3 |  |  |

**Calculation:**  
`10,000,000 × (3.5 ÷ 100) × (3 ÷ 12) = 87,500`

---

## 8. Outcome & Notes
- Both root causes (percentage conversion and precision) were identified and fixed.  
- Intermediate precision increased to **10 decimals** to avoid truncation errors; final result is rounded to **2 decimals**.  
- All relevant unit and integration tests passed after the fix.  
- Change was minimal and localized to `calculateCashflowValue()` (no API changes).

---

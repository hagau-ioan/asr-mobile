# Yearly Screen Sorting & Future Years

## Changes Made

### 1. Sort Order: DESC (Newest First) ✅
Changed from ASC to DESC sorting in YearlyInteractor:
```kotlin
.sortedByDescending { it.year }  // 2026, 2025, 2024...
```

### 2. Comparison Logic Fixed ✅
Updated to work with DESC order:
```kotlin
// Before (ASC): compare with previous item (index - 1)
// After (DESC): compare with next item (index + 1)
if (index == yearlyData.size - 1) return@mapIndexedNotNull null
val previous = yearlyData[index + 1]
```

### 3. Future Years Support ✅
Chart automatically includes future years:
```kotlin
val maxYear = maxOf(dataYears.maxOrNull() ?: currentYear, currentYear + 1)
```

## Display Order

### Sumar Financiar (Financial Summary)
- **2027** (if data exists or in future)
- **2026** (current year)
- **2025**
- **2024**
- ... (oldest last)

### Variații An cu An (Year-over-Year Changes)
- **2027 → 2026** (if 2027 data exists)
- **2026 → 2025**
- **2025 → 2024**
- ... (oldest comparison last)

### Chart
- Shows years in **chronological order** (2024, 2025, 2026, 2027) for readability
- Includes **current year + 1** automatically
- Empty bars for years without data

## Future Proof

When 2027 data is added:
1. ✅ Will appear first in "Sumar Financiar"
2. ✅ Will create "2027 → 2026" comparison first in "Variații"
3. ✅ Will show in chart with actual values
4. ✅ 2028 will automatically be added to chart range

When 2028 data is added:
1. ✅ Will appear first in "Sumar Financiar"
2. ✅ Will create "2028 → 2027" comparison first in "Variații"
3. ✅ Will show in chart with actual values
4. ✅ 2029 will automatically be added to chart range

## Build Status
✅ Compilation successful
✅ Logic verified for DESC order
✅ Future years automatically included

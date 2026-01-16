# Number Formatting Refactoring

## Summary
Refactored the app to consistently use extension methods from `NumberExtensions.kt` for all number formatting operations.

## New Extension Methods Added

### 1. `formatForAxis(): String`
Formats numbers for chart axis labels:
- Values >= 1000: "XK+" format (e.g., 40345 → "40K+")
- Values < 1000: Integer format (e.g., 500 → "500")

**Usage:**
```kotlin
val label = value.formatForAxis()  // 40345 → "40K+"
```

### 2. `formatPercentage(): String`
Formats percentages with sign and 1 decimal place:
- Positive: "+15.7%"
- Negative: "-8.3%"
- Rounds to 1 decimal place automatically

**Usage:**
```kotlin
val percentage = difference.percentOf(total).formatPercentage()  // "+15.7%"
```

## Files Updated

### 1. NumberExtensions.kt
- Added `formatForAxis()` extension
- Added `formatPercentage()` extension

### 2. LineChart.kt
- Removed local `formatYAxisLabel()` function
- Now uses `formatForAxis()` extension
- Cleaner, more maintainable code

### 3. UtilitiesInteractor.kt
- Replaced manual percentage calculation: `(difference / previousAmount) * 100`
- Now uses: `difference.percentOf(previousAmount)`
- Applied to both total percentage and comparison percentages

### 4. UtilitiesScreen.kt
- Replaced manual rounding: `(state.totalPercentage * 10).toInt() / 10.0`
- Now uses: `state.totalPercentage.formatPercentage()`
- Applied to both summary card and comparison table

## Benefits

1. **Consistency**: All number formatting uses centralized utilities
2. **Maintainability**: Changes to formatting logic only need to be made in one place
3. **Readability**: Code is more expressive and easier to understand
4. **Reusability**: Extensions can be used throughout the app
5. **Type Safety**: Extension methods provide compile-time safety

## Existing Extensions Already in Use

The app was already using these extensions correctly:
- `formatCurrency()` - Used in HomeScreen, ExpensesScreen, CongregationsScreen, UtilitiesScreen
- `percentOfAsInt()` - Used in DonutChart for pie chart percentages
- `roundTo()` - Used internally by new `formatPercentage()` method

## Code Quality Improvements

**Before:**
```kotlin
// Manual calculation
val totalPercentage = if (previousTotal > 0) (totalDifference / previousTotal) * 100 else 0.0

// Manual rounding
val percentageText = "${if (state.totalPercentage >= 0) "+" else ""}${(state.totalPercentage * 10).toInt() / 10.0}%"

// Local function
private fun formatYAxisLabel(value: Double): String {
    return when {
        value >= 1000 -> "${(value / 1000).toInt()}K+"
        else -> value.toInt().toString()
    }
}
```

**After:**
```kotlin
// Using extension
val totalPercentage = totalDifference.percentOf(previousTotal)

// Using extension
val percentageText = state.totalPercentage.formatPercentage()

// Using extension
val label = value.formatForAxis()
```

## Date
January 16, 2026

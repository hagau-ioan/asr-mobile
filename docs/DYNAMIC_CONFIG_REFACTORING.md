# Dynamic Configuration Refactoring

## Changes Made

### 1. Simplified app_config.json
**Before**:
```json
{
  "availableYears": [2024, 2025, 2026, 2027],
  "months": [
    {"value": 1, "name": "Ianuarie"},
    ...
  ]
}
```

**After**:
```json
{
  "startYear": 2024
}
```

### 2. Dynamic Year Generation
Added utility function in `DateUtils.kt`:
```kotlin
fun getAvailableYears(startYear: Int): List<Int> {
    return (startYear..startYear + 5).toList()
}
```

**Result**: Years are now calculated as `[2024, 2025, 2026, 2027, 2028, 2029]`

### 3. Months from Translations
Months are now retrieved from string resources instead of JSON:
- Romanian: `strings.xml` (values/)
- Hungarian: `strings.xml` (values-hu/)

**Usage in screens**:
```kotlin
val months = listOf(
    1 to stringResource(Res.string.month_january),
    2 to stringResource(Res.string.month_february),
    // ... etc
)
```

### 4. Updated AppConfig Model
**Removed**:
- `availableYears: List<Int>`
- `months: List<MonthConfig>`
- `MonthConfig` data class

**Added**:
- `startYear: Int`

### 5. Updated JsonAppConfigDataSource
```kotlin
override suspend fun getAvailableYears(): List<Int> {
    val startYear = getConfig()?.startYear ?: DEFAULT_START_YEAR
    return getAvailableYears(startYear)  // Generates startYear to startYear+5
}
```

## Benefits

### 1. Multilanguage Support
- ✅ Months automatically translate based on user's language
- ✅ No need to maintain month names in JSON
- ✅ Consistent with rest of app's i18n approach

### 2. Dynamic Years
- ✅ Years automatically calculated from startYear
- ✅ Always shows 6 years (current + 5 future)
- ✅ No manual updates needed each year

### 3. Simplified Configuration
- ✅ Smaller JSON file (from 40 lines to 10 lines)
- ✅ Single source of truth for startYear
- ✅ Less maintenance overhead

### 4. Consistency
- ✅ Months use same string resources as rest of app
- ✅ Year calculation logic centralized in DateUtils
- ✅ Configuration follows DRY principle

## Configuration

To change the start year, simply update `app_config.json`:
```json
{
  "startYear": 2025  // Will generate [2025, 2026, 2027, 2028, 2029, 2030]
}
```

## Files Modified
1. `app_config.json` - Simplified configuration
2. `AppConfig.kt` - Updated model
3. `DateUtils.kt` - Added getAvailableYears()
4. `JsonAppConfigDataSource.kt` - Dynamic year generation

## Build Status
✅ BUILD SUCCESSFUL - All changes compile correctly

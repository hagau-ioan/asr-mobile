# Year Navigation for Bar Chart

## Implementation
Added navigation arrows to the bar chart allowing users to scroll through years, including future years (e.g., 2027) that don't have data yet.

## Features

### Year Range
- **Automatic range**: From earliest data year to current year + 1
- **Future years**: Includes next year (2027) even without data
- **Empty data**: Shows bars with 0 values for years without transactions

### Navigation Controls
- **Left arrow**: Navigate to previous year (disabled at first year)
- **Right arrow**: Navigate to next year (disabled at last year)
- **Year display**: Shows currently selected year between arrows
- **Visual feedback**: Selected year bars at full opacity, others at 30% opacity

### Code Changes

#### YearlyScreen.kt
- Added `selectedYearIndex` state to track current year
- Calculate `allYears` range from min data year to current year + 1
- Create `displayStats` with YearlyStat(year, 0.0, 0.0, 0.0) for missing years
- Added navigation UI with IconButtons and year label
- Pass `selectedYearIndex` to BarChart

#### BarChart.kt
- Added `selectedYearIndex` parameter (default: last year)
- Apply alpha transparency: 1.0 for selected year, 0.3 for others
- Bars fade out when not selected, highlighting current year

## User Experience
1. Chart loads showing the most recent year highlighted
2. Click left arrow to view previous years
3. Click right arrow to view future years (2027 will show empty bars)
4. Selected year is fully visible, others are dimmed
5. Navigation arrows disable at boundaries

## Build Status
✅ Full build successful
⚠️ Minor deprecation warnings (unrelated to changes)

## Future-Ready
The implementation automatically includes 2027 in the year range, so when 2027 data is added, it will display immediately without code changes.

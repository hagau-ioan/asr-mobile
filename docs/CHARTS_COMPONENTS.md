# Charts Components Implementation

## Overview
Created a dedicated charts directory with reusable chart components for the application.

## Location
`/composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/charts/`

## Components

### 1. DonutChart.kt
**Purpose**: Reusable donut/pie chart component with legend

**Features**:
- Custom Canvas-based implementation
- Automatic color assignment from predefined palette
- Percentage calculation and display
- Legend with color indicators
- Supports any data with label and value

**Usage**:
```kotlin
val items = listOf(
    DonutChartItem(label = "Category 1", value = 100.0),
    DonutChartItem(label = "Category 2", value = 200.0)
)
DonutChart(items = items)
```

**Used in**: ExpensesScreen for expense distribution visualization

### 2. LineChart.kt
**Purpose**: Reusable line chart for comparing two data series

**Features**:
- Custom Canvas-based implementation
- Dual line support (solid and dashed)
- Automatic scaling based on data
- Data points visualization
- Responsive to container size

**Usage**:
```kotlin
LineChart(
    currentYearData = listOf(100.0, 150.0, 200.0),
    previousYearData = listOf(90.0, 140.0, 180.0),
    currentYearLabel = "2026",
    previousYearLabel = "2025",
    height = 350
)
```

**Used in**: UtilitiesScreen for yearly utilities comparison

## Implementation Notes

### Why Canvas Instead of Vico?
- Vico 2.1.4 multiplatform API had compatibility issues
- Canvas provides full control and guaranteed iOS/Android compatibility
- Simpler implementation for our specific use cases
- Can be replaced with Vico later when stable multiplatform support is available

### Design Decisions
1. **Separation of Concerns**: Charts are independent, reusable components
2. **Material Design Integration**: Uses theme colors automatically
3. **Type Safety**: Dedicated data models (DonutChartItem, etc.)
4. **Minimal Dependencies**: Only uses Compose foundation APIs

## Refactoring Impact

### ExpensesScreen
- **Before**: 458 lines with embedded chart code
- **After**: ~400 lines using DonutChart component
- **Benefit**: Cleaner, more maintainable code

### UtilitiesScreen
- Implemented complete utilities comparison feature
- Month-to-month comparison table
- Yearly trend visualization with LineChart
- Follows same MVI pattern as ExpensesScreen

## Future Enhancements
1. Add axis labels and grid lines to LineChart
2. Add tooltips/hover effects
3. Add animation support
4. Consider migrating to Vico when multiplatform support stabilizes
5. Add bar chart component for other screens
6. Add interactive zoom/pan capabilities

## Related Files
- `ExpensesConstants.kt` - Chart colors and sizing constants
- `UIConstants.kt` - Shared spacing constants
- `ExpensesScreen.kt` - Uses DonutChart
- `UtilitiesScreen.kt` - Uses LineChart

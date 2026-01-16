# DataTable Component

## Overview
Reusable data table component with horizontal scroll support, consistent styling, and flexible content.

## Location
`/composeApp/src/commonMain/kotlin/com/asr/financial/presentation/ui/components/table/`

## Components

### DataTable
Main container component that provides:
- Horizontal scroll support
- Card wrapper with border
- Header row with background
- Automatic dividers between rows

### TableHeaderCell
Standard header cell with:
- Bold font weight
- Configurable width
- Text alignment support

### TableRow
Row wrapper that provides:
- Consistent padding and spacing
- Optional background color
- Automatic divider after row

### TableCell
Standard data cell with:
- Configurable width
- Text alignment support
- Color customization
- Font weight customization

## Usage Example

### Simple Table
```kotlin
DataTable(
    columns = emptyList(),
    headerContent = {
        TableHeaderCell("Name", width = 120.dp)
        TableHeaderCell("Amount", width = 100.dp, textAlign = TextAlign.End)
    }
) {
    items.forEach { item ->
        TableRow {
            TableCell(item.name, width = 120.dp)
            TableCell(item.amount.formatCurrency(), width = 100.dp, textAlign = TextAlign.End)
        }
    }
}
```

### Table with Custom Styling
```kotlin
DataTable(
    columns = emptyList(),
    headerContent = {
        Box(Modifier.width(32.dp)) // Icon space
        TableHeaderCell("Category", width = 150.dp)
        TableHeaderCell("Value", width = 120.dp, textAlign = TextAlign.End)
    }
) {
    items.forEach { item ->
        TableRow(
            backgroundColor = if (item.isHighlighted) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else Color.Transparent
        ) {
            Icon(Icons.Default.Star, null, modifier = Modifier.size(20.dp))
            TableCell(
                text = item.category,
                width = 150.dp,
                fontWeight = FontWeight.Bold
            )
            TableCell(
                text = item.value.formatCurrency(),
                width = 120.dp,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

## Features

### Horizontal Scroll
- Automatically enabled for wide tables
- Smooth scrolling experience
- Works on all platforms (Android, iOS, Desktop)

### Consistent Styling
- Uses Material Design 3 theme colors
- Consistent padding and spacing (12.dp, 8.dp)
- Automatic dividers between rows
- Header background color from theme

### Flexible Content
- Support for any composable in header and rows
- Icons, text, buttons, etc.
- Custom row backgrounds
- Conditional styling

## Used In

### CongregationsScreen
- Displays congregation donation data
- 6 columns: Icon, Name, Donated, Expected, Difference, Last Donation
- Conditional row highlighting for missing donations
- Status icons per row

### UtilitiesScreen
- Displays utility comparison data
- 6 columns: Category, Current Month, Previous Month, Difference, Variation %, Trend Icon
- Color-coded differences (red/green)
- Total row with different background

## Design Decisions

1. **Horizontal Scroll**: Essential for tables with many columns on mobile devices
2. **Card Wrapper**: Provides visual separation and elevation
3. **Reusable Cells**: Consistent styling across all tables
4. **Flexible Width**: Each column can have custom width
5. **Theme Integration**: Uses Material Design 3 colors automatically

## Future Enhancements

1. Add sorting support (click header to sort)
2. Add filtering capabilities
3. Add row selection
4. Add pagination for large datasets
5. Add sticky header support
6. Add column resizing
7. Add export to CSV functionality

## Related Files
- `CongregationsScreen.kt` - Uses DataTable for congregation data
- `UtilitiesScreen.kt` - Uses DataTable for utility comparisons
- `UIConstants.kt` - Shared spacing constants

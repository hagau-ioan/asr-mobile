# Monthly Expenses Screen Implementation

## Overview
Implemented the "Cheltuieli Lunare" (Monthly Expenses) screen following the same MVI architecture pattern used in CongregationsScreen.

## Architecture Flow

```
ExpensesScreen (UI)
    ↓
ExpensesViewModel
    ↓
ExpensesInteractor
    ↓
GetTransactionsUseCase
    ↓
TransactionRepository
    ↓
JsonTransactionDataSource
    ↓
transactions.json
```

## Files Created

### MVI Components

1. **ExpensesState.kt** - State management
   - `Loading` - Initial loading state
   - `Success` - Contains expenses list, total, and selected period
   - `Error` - Error state with message

2. **ExpensesEvent.kt** - User actions
   - `LoadData` - Initial data load
   - `FilterByPeriod` - Filter by year/month
   - `Refresh` - Refresh data

3. **ExpensesEffect.kt** - Side effects
   - `ShowToast` - Display toast messages

4. **ExpensesInteractor.kt** - Business logic
   - Filters transactions by type (EXPENSE) and period
   - Groups expenses by category
   - Calculates totals and counts
   - Sorts by amount (descending)

5. **ExpensesViewModel.kt** - ViewModel layer
   - Connects UI to Interactor
   - Manages lifecycle

### UI Components

6. **ExpensesScreen.kt** - Complete UI implementation
   - Summary card showing total expenses (red/error theme)
   - Expandable category cards
   - Transaction details within each category
   - Empty state handling
   - Loading and error states

## Features

### Summary Card
- Displays total monthly expenses
- Uses error container color (red theme) to indicate outgoing money
- Large, bold typography for emphasis

### Category Cards
- Groups expenses by category (Utilități, Materiale birou, etc.)
- Shows category name, transaction count, and total amount
- Expandable to show individual transactions
- Click to expand/collapse

### Transaction Details
- Description and date
- Individual amounts
- Sorted by date (most recent first)

### Period Filtering
- Integrated with ScreenLayout's month/year selector
- Automatically loads current month on init
- Updates when period changes

## Data Source

Uses existing transaction data from `transactions.json`:
- Filters by `type: "EXPENSE"`
- Groups by `category` field
- Displays `description`, `date`, and `amount`

## Dependency Injection

Updated `PresentationModule.kt`:
```kotlin
factory { ExpensesInteractor(get(), get()) }
viewModel { ExpensesViewModel(get()) }
```

## Design Decisions

1. **Expandable Cards** - Follows common expense tracker pattern, allows users to drill down into details without cluttering the main view

2. **Category Grouping** - Makes it easy to see spending patterns by category

3. **Red Theme** - Uses error/warning colors to visually distinguish expenses from income

4. **Minimal Code** - Reuses existing infrastructure (ScreenLayout, Transaction model, formatCurrency extension)

5. **Consistent Pattern** - Mirrors CongregationsScreen architecture for maintainability

## Testing

Build successful:
```
BUILD SUCCESSFUL in 2s
88 actionable tasks: 10 executed, 1 from cache, 77 up-to-date
```

## Next Steps

Potential enhancements:
- Add charts/graphs for visual expense breakdown
- Export functionality
- Budget tracking per category
- Receipt image attachments
- Search/filter within expenses

# UI Components Refactoring Analysis

## Duplicated Components Identified

### 1. **Period Selector Components** (CRITICAL - Used in 2+ screens)

#### a) YearSelector
**Found in:**
- `HomeScreen.kt` - Lines 100-150 (inline)
- `ExpensesScreen.kt` - Lines 260-320 (extracted function)

**Pattern:**
```kotlin
OutlinedCard with:
- Year display
- Dropdown arrow icon
- DropdownMenu with year list
- Click handler
```

**Proposed:** `PeriodYearSelector.kt`

#### b) MonthSelector
**Found in:**
- `HomeScreen.kt` - Lines 160-210 (inline)
- `ExpensesScreen.kt` - Lines 325-385 (extracted function)

**Pattern:**
```kotlin
OutlinedCard with:
- Month name display
- Dropdown arrow icon
- DropdownMenu with month list
- Click handler
```

**Proposed:** `PeriodMonthSelector.kt`

#### c) PeriodSelectorCard
**Found in:**
- `HomeScreen.kt` - Lines 80-220 (inline)
- `ExpensesScreen.kt` - Lines 204-255 (extracted function)

**Pattern:**
```kotlin
Card with:
- Title "Selectează Perioada"
- Row with YearSelector + MonthSelector
- Consistent spacing and styling
```

**Proposed:** `PeriodSelectorCard.kt`

---

### 2. **Summary Cards** (Used in multiple screens)

#### a) TotalSummaryCard
**Found in:**
- `ExpensesScreen.kt` - MonthlySummaryCard (lines 360-380)
- `HomeScreen.kt` - Similar pattern for income/expenses

**Pattern:**
```kotlin
Card with errorContainer/primaryContainer color:
- Title text
- Large amount (formatted currency)
- Optional subtitle
```

**Proposed:** `SummaryCard.kt` with variants:
- `ErrorSummaryCard` (red - expenses)
- `PrimarySummaryCard` (blue - info)
- `SuccessSummaryCard` (green - income)

---

### 3. **Loading & Error States** (Used in ALL screens)

#### a) LoadingContent
**Found in:**
- `ExpensesScreen.kt` - ExpensesLoadingContent
- `CongregationsScreen.kt` - Similar pattern
- Other screens

**Pattern:**
```kotlin
ScreenLayout with:
- Breadcrumbs
- Centered CircularProgressIndicator
```

**Proposed:** `LoadingStateContent.kt`

#### b) ErrorContent
**Found in:**
- `ExpensesScreen.kt` - ExpensesErrorContent
- `CongregationsScreen.kt` - Similar pattern

**Pattern:**
```kotlin
ScreenLayout with:
- Breadcrumbs
- Error card with message
```

**Proposed:** `ErrorStateContent.kt`

#### c) EmptyStateCard
**Found in:**
- `ExpensesScreen.kt` - EmptyStateCard
- Likely in other screens

**Pattern:**
```kotlin
Card with:
- Centered text
- Empty state message
```

**Proposed:** `EmptyStateCard.kt`

---

### 4. **Dropdown Components** (Reusable pattern)

#### a) OutlinedDropdownCard
**Pattern repeated everywhere:**
```kotlin
OutlinedCard + DropdownMenu combination
- Label text
- Selected value display
- Arrow icon
- Dropdown menu
```

**Proposed:** `OutlinedDropdownCard.kt`

---

### 5. **Chart Components** (Screen-specific but extractable)

#### a) PieChart
**Found in:**
- `ExpensesScreen.kt` - ExpensePieChart (lines 580-650)

**Pattern:**
```kotlin
Canvas-based donut chart with:
- Color-coded segments
- Legend with percentages
```

**Proposed:** `DonutChart.kt` (generic, reusable)

---

## Proposed Component Structure

```
presentation/ui/components/
├── period/
│   ├── PeriodSelectorCard.kt       ✅ Complete period selector
│   ├── YearDropdown.kt             ✅ Year selection dropdown
│   └── MonthDropdown.kt            ✅ Month selection dropdown
│
├── cards/
│   ├── SummaryCard.kt              ✅ Generic summary card
│   ├── EmptyStateCard.kt           ✅ Empty state display
│   └── CategoryCard.kt             ✅ Expandable category card
│
├── states/
│   ├── LoadingContent.kt           ✅ Loading state wrapper
│   └── ErrorContent.kt             ✅ Error state wrapper
│
├── dropdowns/
│   └── OutlinedDropdownCard.kt     ✅ Generic dropdown
│
└── charts/
    ├── DonutChart.kt               ✅ Pie/donut chart
    └── ChartLegend.kt              ✅ Chart legend
```

---

## Priority Order

### Phase 1: CRITICAL (Most Duplicated)
1. ✅ **PeriodSelectorCard** - Used in 2+ screens, 200+ lines duplicated
2. ✅ **YearDropdown** - Core component
3. ✅ **MonthDropdown** - Core component

### Phase 2: HIGH (Common Patterns)
4. ✅ **SummaryCard** - Multiple variants needed
5. ✅ **LoadingContent** - Every screen needs this
6. ✅ **ErrorContent** - Every screen needs this

### Phase 3: MEDIUM (Nice to Have)
7. ✅ **EmptyStateCard** - Simple but repeated
8. ✅ **OutlinedDropdownCard** - Generic dropdown pattern
9. ✅ **DonutChart** - Reusable visualization

---

## Benefits

### Code Reduction
- **HomeScreen**: ~150 lines → ~20 lines (period selector)
- **ExpensesScreen**: ~180 lines → ~30 lines (period selector + states)
- **Total**: ~500+ lines of duplicated code eliminated

### Consistency
- Same look and feel across all screens
- Single source of truth for styling
- Easier to maintain and update

### Testability
- Components can be tested in isolation
- Easier to write unit tests
- Better coverage

### Reusability
- New screens can use existing components
- Faster development
- Less bugs

---

## Next Steps

1. Create Phase 1 components (period selectors)
2. Update HomeScreen and ExpensesScreen to use them
3. Verify build and functionality
4. Create Phase 2 components (states and cards)
5. Update all screens
6. Create Phase 3 components (charts and dropdowns)

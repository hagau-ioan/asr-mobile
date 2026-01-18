# ASR Financial Management - Project Analysis

## Executive Summary

**ASR Financial Management** is a Kotlin Multiplatform (KMP) application for managing financial data across congregations, built for both Android and iOS. The app follows Clean Architecture principles with a strict separation between business logic (shared module) and UI (composeApp module).

---

## Architecture Overview

### Architecture Pattern
- **Clean Architecture** - Domain layer has zero dependencies on outer layers
- **MVVM + MVI** - Unidirectional data flow with States, Events, and Effects
- **Interactor Pattern** - Business logic encapsulated in Interactors
- **Two-Module Architecture** - Enforces domain purity (no Compose in shared module)

### Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Kotlin Multiplatform | 2.0.21 |
| **UI Framework** | Compose Multiplatform | 1.7.1 |
| **Coroutines** | kotlinx-coroutines | 1.9.0 |
| **Database** | SQLDelight | 2.0.2 |
| **Dependency Injection** | Koin | 4.0.0 |
| **Navigation** | Voyager | 1.1.0 |
| **Images** | Coil 3 | 3.0.4 |
| **Charts** | Custom Canvas | N/A |

---

## Project Structure

```
asr-financial/
├── shared/                          # Business logic + data (NO UI)
│   ├── src/
│   │   ├── commonMain/
│   │   │   └── kotlin/com/asr/financial/
│   │   │       ├── data/            # Data layer
│   │   │       │   ├── datasource/  # Data sources (JSON, API, DB)
│   │   │       │   ├── repository/  # Repository implementations
│   │   │       │   └── database/    # SQLDelight database
│   │   │       ├── domain/          # Domain layer (pure Kotlin)
│   │   │       │   ├── model/       # Domain models
│   │   │       │   ├── models/      # Additional domain models
│   │   │       │   ├── repository/  # Repository interfaces
│   │   │       │   └── usecase/     # Use cases (business logic)
│   │   │       ├── platform/        # Platform abstractions (expect/actual)
│   │   │       ├── di/              # Dependency injection modules
│   │   │       └── utils/           # Utility functions
│   │   ├── androidMain/             # Android platform implementations
│   │   └── iosMain/                 # iOS platform implementations
│
├── composeApp/                      # UI layer (Compose Multiplatform)
│   ├── src/
│   │   ├── commonMain/
│   │   │   └── kotlin/com/asr/financial/
│   │   │       ├── presentation/    # Presentation layer
│   │   │       │   ├── mvi/         # MVI pattern implementation
│   │   │       │   │   ├── state/    # UI States (sealed interfaces)
│   │   │       │   │   ├── event/   # UI Events (user actions)
│   │   │       │   │   ├── effect/  # UI Effects (one-time events)
│   │   │       │   │   ├── interactor/ # Business logic handlers
│   │   │       │   │   └── viewmodel/ # ViewModels (delegates to Interactors)
│   │   │       │   ├── screens/      # Screen composables
│   │   │       │   ├── navigation/  # Navigation setup
│   │   │       │   ├── theme/       # Design system (colors, typography)
│   │   │       │   └── ui/          # Reusable UI components
│   │   │       ├── di/              # Presentation DI module
│   │   │       └── platform/        # Platform-specific UI implementations
│   │   ├── androidMain/             # Android-specific UI
│   │   └── iosMain/                 # iOS-specific UI
│   │   └── composeResources/       # Resources (JSON files, strings)
│
├── androidApp/                      # Android wrapper
│   └── src/main/
│       └── kotlin/                  # MainActivity.kt
│
└── iosApp/                          # iOS Xcode project
    └── iosApp/                      # iOSApp.swift
```

---

## Clean Architecture Layers

### 1. Domain Layer (`shared/domain/`)
**Purpose**: Pure business logic, zero dependencies on outer layers

**Components**:
- **Models**: Domain entities (`Transaction`, `Congregation`, `Expense`, `AppConfig`, etc.)
- **Repository Interfaces**: Contracts for data access
- **Use Cases**: Business logic operations

**Key Files**:
- `domain/model/Transaction.kt` - Transaction domain model
- `domain/models/Congregation.kt` - Congregation domain model
- `domain/models/AppConfig.kt` - App configuration
- `domain/repository/TransactionRepository.kt` - Repository interface
- `domain/usecase/GetTransactionsUseCase.kt` - Business logic

**Principles**:
- ✅ No Android/iOS dependencies
- ✅ No Compose dependencies
- ✅ No database dependencies
- ✅ Pure Kotlin code

---

### 2. Data Layer (`shared/data/`)
**Purpose**: Data access and repository implementations

**Components**:
- **Data Sources**: JSON, API, Database implementations
- **Repository Implementations**: Concrete implementations of domain repositories
- **Database**: SQLDelight database (if used)

**Key Files**:
- `data/datasource/JsonTransactionDataSource.kt` - JSON data source
- `data/datasource/JsonAppConfigDataSource.kt` - App config data source
- `data/repository/TransactionRepositoryImpl.kt` - Repository implementation

**Data Sources**:
- `JsonTransactionDataSource` - Loads from JSON files
- `JsonAppConfigDataSource` - Loads app configuration
- `JsonCongregationDataSource` - Loads congregation data
- `JsonAsrExpenseDataSource` - Loads ASR expenses

**Current Implementation**: JSON-based (files in `composeResources/files/`)

---

### 3. Presentation Layer (`composeApp/presentation/`)
**Purpose**: UI logic and state management

**Components**:
- **MVI Pattern**: States, Events, Effects, Interactors, ViewModels
- **Screens**: Composable UI screens
- **Navigation**: Navigation graph and routes
- **Theme**: Design system
- **UI Components**: Reusable composables

---

## MVI Pattern Implementation

### Pattern Structure

```
User Action → Event → ViewModel → Interactor → Use Case → Repository → Data Source
                                                                    ↓
UI Update ← State ← Interactor ← Use Case ← Repository ← Data Source
```

### Components

#### 1. **State** (`presentation/mvi/state/`)
Sealed interfaces representing UI state

**Example**: `HomeState.kt`
```kotlin
sealed interface HomeState {
    data object Loading : HomeState
    data class Success(
        val selectedYear: Int,
        val selectedMonth: Int,
        val monthlyIncome: Double,
        val monthlyExpenses: Double,
        // ... more fields
    ) : HomeState
    data class Error(val message: String) : HomeState
}
```

**Pattern**: All screens use `Loading`, `Success`, `Error` states

---

#### 2. **Event** (`presentation/mvi/event/`)
Sealed interfaces representing user actions

**Example**: `HomeEvent.kt`
```kotlin
sealed interface HomeEvent {
    data object LoadData : HomeEvent
    data class FilterByMonth(val month: Int, val year: Int) : HomeEvent
    data class NavigateToDetails(val transactionId: String) : HomeEvent
    data object Refresh : HomeEvent
}
```

---

#### 3. **Effect** (`presentation/mvi/effect/`)
One-time events (navigation, toasts, etc.)

**Example**: `HomeEffect.kt`
```kotlin
sealed interface HomeEffect {
    data class ShowToast(val message: String) : HomeEffect
    data class NavigateToDetails(val transactionId: String) : HomeEffect
    data object ScrollToTop : HomeEffect
}
```

---

#### 4. **Interactor** (`presentation/mvi/interactor/`)
Business logic handler - processes events, emits states and effects

**Example**: `HomeInteractor.kt`
```kotlin
class HomeInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    // ... more use cases
) {
    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState = _uiState.asStateFlow()
    
    private val _uiEffectChannel = Channel<HomeEffect>(Channel.BUFFERED)
    val uiEffect: Flow<HomeEffect> = _uiEffectChannel.receiveAsFlow()
    
    suspend fun processEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadData -> loadData()
            // ... handle other events
        }
    }
}
```

**Key Points**:
- All business logic is in Interactors
- Interactors use Use Cases (from domain layer)
- Interactors emit States and Effects
- No UI logic in Interactors

---

#### 5. **ViewModel** (`presentation/mvi/viewmodel/`)
Thin wrapper that delegates to Interactor

**Example**: `HomeViewModel.kt`
```kotlin
class HomeViewModel(
    private val homeInteractor: HomeInteractor
) : ViewModel() {
    val uiState = homeInteractor.uiState
    val uiEffect = homeInteractor.uiEffect
    
    fun handleEvent(event: HomeEvent) {
        viewModelScope.launch {
            homeInteractor.processEvent(event)
        }
    }
}
```

**Pattern**: ViewModels are thin - they just delegate to Interactors

---

#### 6. **Screen** (`presentation/screens/`)
Composable UI that observes state and sends events

**Example**: `HomeScreen.kt`
```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowToast -> { /* Show toast */ }
                // ... handle effects
            }
        }
    }
    
    when (val state = uiState) {
        is HomeState.Loading -> LoadingContent()
        is HomeState.Success -> { /* Render UI */ }
        is HomeState.Error -> ErrorContent(state.message)
    }
}
```

---

## Screens & Features

### Screen Directory Structure

Each screen follows a consistent directory structure pattern:

```
presentation/screens/{screen-name}/
├── {Screen}Screen.kt          # Main screen composable
├── {Screen}Constants.kt        # Screen-specific constants
├── {Screen}Stat.kt            # Presentation data models (optional)
├── {Screen}Model.kt            # Additional data models (optional)
└── components/                 # Screen-specific reusable components
    ├── {Component}Row.kt
    ├── {Component}Card.kt
    └── ...
```

**Example: Congregations Screen**
```
presentation/screens/congregations/
├── CongregationsScreen.kt      # Main screen composable
├── CongregationsConstants.kt   # Constants (table widths, icon sizes, etc.)
├── CongregationStat.kt         # Presentation model for congregation stats
└── components/
    └── CongregationRow.kt      # Reusable row component for table
```

**Example: Expenses Screen**
```
presentation/screens/expenses/
├── ExpensesScreen.kt
├── ExpensesConstants.kt
├── ExpenseStat.kt
└── components/
    ├── ExpenseCategoryRow.kt
    ├── MonthlySummaryCard.kt
    └── YearlySummaryCard.kt
```

**Example: Utilities Screen**
```
presentation/screens/utilities/
├── UtilitiesScreen.kt
├── UtilitiesConstants.kt
├── UtilityStat.kt
├── UtilityComparison.kt
├── YearlyUtilityData.kt
└── components/
    ├── ComparisonCard.kt
    ├── ComparisonTable.kt
    ├── TrendIcon.kt
    └── YearlyComparisonCard.kt
```

### Screen File Types

#### 1. **{Screen}Screen.kt**
- Main screen composable
- Observes ViewModel state
- Handles effects
- Uses `ScreenLayout` wrapper
- Delegates rendering to success/error content composables

**Architecture Integration**:
- Imports ViewModel from `presentation.mvi.viewmodel.{Screen}ViewModel`
- Imports State from `presentation.mvi.state.{Screen}State`
- Imports Event from `presentation.mvi.event.{Screen}Event`
- Imports Effect from `presentation.mvi.effect.{Screen}Effect`
- Uses `koinViewModel()` for dependency injection
- No business logic - only UI rendering

#### 2. **{Screen}Constants.kt**
- Screen-specific constants
- UI dimensions (widths, heights, padding)
- Icon sizes
- Color values
- Other magic numbers used in the screen

**Example**:
```kotlin
object CongregationsConstants {
    const val TABLE_NAME_WIDTH_DP = 120
    const val TABLE_AMOUNT_WIDTH_DP = 100
    const val STATUS_ICON_SIZE_DP = 20
}
```

**Purpose**: Centralize all magic numbers and UI constants for maintainability

#### 3. **{Screen}Stat.kt / {Screen}Model.kt**
- Presentation data models
- Screen-specific data structures
- Not domain models (those are in `shared/domain/models/`)
- Used for UI rendering and calculations

**Example**:
```kotlin
data class CongregationStat(
    val name: String,
    val donated: Double,
    val expected: Double,
    val difference: Double,
    val lastDonation: String?,
    val isMissing: Boolean
)
```

**Architecture Note**: 
- These are **presentation models**, not domain models
- Domain models are in `shared/domain/models/`
- Presentation models are computed/transformed from domain models in Interactors
- Used only for UI rendering

#### 4. **components/** Folder
- Screen-specific reusable components
- Components only used within this screen
- If component is used across multiple screens, it should be in `presentation/ui/components/`

**Naming Convention**:
- `{Component}Row.kt` - Table/list row components
- `{Component}Card.kt` - Card components
- `{Component}Item.kt` - List item components

**Architecture Pattern**:
- Components are pure composables (no ViewModel dependency)
- Receive data as parameters
- Follow single responsibility principle
- Can use `@Composable` functions from other screens if needed

### Screen Architecture Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Screen Layer                              │
│  composeApp/presentation/screens/{screen}/                   │
│                                                              │
│  {Screen}Screen.kt                                           │
│    ↓ observes                                                │
│  {Screen}ViewModel (from mvi/viewmodel/)                     │
│    ↓ delegates to                                            │
│  {Screen}Interactor (from mvi/interactor/)                  │
│    ↓ uses                                                    │
│  Use Cases (from shared/domain/usecase/)                     │
│    ↓ calls                                                   │
│  Repository (from shared/domain/repository/)                 │
│    ↓ implemented by                                          │
│  RepositoryImpl (from shared/data/repository/)               │
│    ↓ uses                                                    │
│  DataSource (from shared/data/datasource/)                  │
└─────────────────────────────────────────────────────────────┘

Screen Files:
├── {Screen}Screen.kt          → UI rendering only
├── {Screen}Constants.kt        → UI constants
├── {Screen}Stat.kt            → Presentation models
└── components/                 → Screen-specific components
    └── {Component}Row.kt       → Reusable UI components
```

### Screen File Responsibilities

| File Type | Responsibility | Dependencies |
|-----------|--------------|--------------|
| **{Screen}Screen.kt** | UI rendering, state observation, effect handling | ViewModel, State, Event, Effect |
| **{Screen}Constants.kt** | UI constants storage | None |
| **{Screen}Stat.kt** | Presentation data models | None (pure data classes) |
| **components/{Component}.kt** | Reusable UI components | Material3, Compose |

### Main Screens

1. **Home Screen** (`screens/home/`)
   - Financial statistics dashboard
   - Monthly/yearly income/expenses
   - Missing congregations tracking
   - Period selector (month/year)
   - **Files**: `HomeScreen.kt`, `MissingCongregation.kt`

2. **Congregations Screen** (`screens/congregations/`)
   - List of congregations
   - Congregation statistics
   - Member count tracking
   - **Files**: `CongregationsScreen.kt`, `CongregationsConstants.kt`, `CongregationStat.kt`, `components/CongregationRow.kt`

3. **Expenses Screen** (`screens/expenses/`)
   - Expense tracking by category
   - Monthly/yearly summaries
   - Expense statistics
   - **Files**: `ExpensesScreen.kt`, `ExpensesConstants.kt`, `ExpenseStat.kt`, `components/ExpenseCategoryRow.kt`, `components/MonthlySummaryCard.kt`, `components/YearlySummaryCard.kt`

4. **Utilities Screen** (`screens/utilities/`)
   - Utility expenses tracking
   - Year-over-year comparisons
   - Trend analysis
   - **Files**: `UtilitiesScreen.kt`, `UtilitiesConstants.kt`, `UtilityStat.kt`, `UtilityComparison.kt`, `YearlyUtilityData.kt`, `components/ComparisonCard.kt`, `components/ComparisonTable.kt`, `components/TrendIcon.kt`, `components/YearlyComparisonCard.kt`

5. **Yearly Screen** (`screens/yearly/`)
   - Yearly financial overview
   - Year-to-year comparisons
   - Financial trends
   - **Files**: `YearlyScreen.kt`, `YearlyConstants.kt`, `YearlyStat.kt`, `YearlyComparison.kt`, `components/YearSummaryCard.kt`, `components/YearVariationCard.kt`

6. **Calculator Screen** (`screens/calculator/`)
   - Contribution calculations
   - Per-publisher calculations
   - Congregation contribution breakdown
   - **Files**: `CalculatorScreen.kt`, `CongregationContribution.kt`, `ContributionCalculation.kt`, `components/CongregationContributionRow.kt`, `components/ContributionCard.kt`

7. **ASR Expenses Screen** (`screens/asrexpenses/`)
   - ASR-specific expenses
   - Expense tracking
   - **Files**: `AsrExpensesScreen.kt`, `AsrExpensesConstants.kt`, `components/AsrExpenseRow.kt`

8. **Upload Screen** (`screens/upload/`)
   - Receipt/image upload
   - Camera capture functionality
   - **Files**: `UploadScreen.kt`, `CameraCapture.kt`

9. **Splash Screen** (`screens/splash/`)
   - App initialization
   - Loading state
   - **Files**: `SplashScreen.kt`

---

## UI Screen Patterns

### Common Screen Structure

All screens follow a consistent pattern using the `ScreenLayout` wrapper:

```kotlin
@Composable
fun {Screen}Screen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: {Screen}ViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            // Handle navigation, toasts, etc.
        }
    }
    
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(...),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        when (val state = uiState) {
            is {Screen}State.Loading -> { /* LoadingContent() */ }
            is {Screen}State.Success -> { /* Success content */ }
            is {Screen}State.Error -> { /* ErrorContent() */ }
        }
    }
}
```

### Pattern Components

#### 1. **ScreenLayout Wrapper**
**Location**: `presentation/ui/scaffold/ScreenLayout.kt`

**Purpose**: Common layout wrapper for all screens

**Features**:
- Gradient header with app info
- Breadcrumb navigation (if multiple items)
- Scrollable content area (LazyColumn)
- Responsive design support
- Refresh button support
- Menu button (for compact screens)

**Usage**:
```kotlin
ScreenLayout(
    windowSizeClass = windowSizeClass,
    breadcrumbItems = listOf(
        BreadcrumbItem("Home", Routes.HOME),
        BreadcrumbItem("Current Screen")
    ),
    selectedMonth = "Decembrie",
    selectedYear = 2025,
    showRefreshButton = true,
    isRefreshing = false,
    onNavigate = onNavigate,
    onMenuClick = onMenuClick,
    onRefreshClick = { viewModel.handleEvent(RefreshEvent) }
) {
    // LazyColumn items
    item { /* Content */ }
}
```

#### 2. **State-Based Rendering Pattern**

All screens use sealed interface states with three main states:

```kotlin
when (val state = uiState) {
    is {Screen}State.Loading -> {
        item { LoadingContent() }
    }
    
    is {Screen}State.Success -> {
        item { 
            // Success content - extracted to separate composable
            {Screen}SuccessContent(
                state = state,
                // ... other params
            )
        }
    }
    
    is {Screen}State.Error -> {
        item { ErrorContent(message = state.message) }
    }
}
```

**Benefits**:
- Consistent loading/error handling
- Type-safe state management
- Easy to test

#### 3. **Effect Handling Pattern**

One-time events (navigation, toasts) handled via effects:

```kotlin
LaunchedEffect(Unit) {
    viewModel.uiEffect.collectLatest { effect ->
        when (effect) {
            is {Screen}Effect.NavigateToDetails -> {
                onNavigate(Routes.DETAILS)
            }
            is {Screen}Effect.ShowToast -> {
                // Show toast message
            }
        }
    }
}
```

#### 4. **Period Selector Pattern**

Screens with date filtering use `PeriodSelectorCard`:

```kotlin
var selectedYear by remember { mutableStateOf(defaultYear) }
var selectedMonth by remember { mutableStateOf(defaultMonth) }
var showYearDropdown by remember { mutableStateOf(false) }
var showMonthDropdown by remember { mutableStateOf(false) }

LaunchedEffect(selectedYear, selectedMonth) {
    viewModel.handleEvent(
        {Screen}Event.FilterByPeriod(selectedYear, selectedMonth)
    )
}

item {
    PeriodSelectorCard(
        selectedYear = selectedYear,
        selectedMonth = selectedMonth,
        years = state.availableYears,
        months = months,
        showYearDropdown = showYearDropdown,
        showMonthDropdown = showMonthDropdown,
        onYearDropdownChange = { showYearDropdown = it },
        onMonthDropdownChange = { showMonthDropdown = it },
        onYearSelected = { selectedYear = it },
        onMonthSelected = { selectedMonth = it }
    )
}
```

#### 5. **Content Extraction Pattern**

Success content extracted to separate composable for readability:

```kotlin
@Composable
private fun {Screen}SuccessContent(
    state: {Screen}State.Success,
    windowSizeClass: WindowSizeClass,
    // ... other params
) {
    // All success state rendering
}
```

**Benefits**:
- Cleaner main screen composable
- Easier to test
- Better code organization

#### 6. **Breadcrumb Navigation Pattern**

Multi-level navigation shown via breadcrumbs:

```kotlin
breadcrumbItems = listOf(
    BreadcrumbItem(
        label = stringResource(Res.string.nav_home),
        route = Routes.HOME
    ),
    BreadcrumbItem(
        label = stringResource(Res.string.nav_current_screen)
        // No route = current screen (not clickable)
    )
)
```

**Behavior**:
- Single item = hidden (no breadcrumb shown)
- Multiple items = shown with navigation
- Last item = not clickable (current screen)

#### 7. **LazyColumn Content Pattern**

All screen content uses LazyColumn items:

```kotlin
ScreenLayout(...) {
    // Period selector
    item {
        PeriodSelectorCard(...)
    }
    
    // Main content
    item {
        Column {
            // Cards, tables, charts, etc.
        }
    }
    
    // Additional sections
    item {
        AnotherSection(...)
    }
}
```

**Benefits**:
- Efficient scrolling
- Easy to add/remove items
- Consistent spacing

#### 8. **Card-Based Layout Pattern**

Data displayed in Material3 Cards:

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Title", style = MaterialTheme.typography.titleMedium)
        // Content
    }
}
```

#### 9. **Responsive Design Pattern**

All screens accept `WindowSizeClass`:

```kotlin
@Composable
fun {Screen}Screen(
    windowSizeClass: WindowSizeClass,
    // ...
) {
    // Adjust layout based on window size
    when (windowSizeClass) {
        WindowSizeClass.Compact -> { /* Phone layout */ }
        WindowSizeClass.Medium -> { /* Tablet layout */ }
        WindowSizeClass.Expanded -> { /* Desktop layout */ }
    }
}
```

#### 10. **Dependency Injection Pattern**

ViewModels and platform dependencies injected via Koin:

```kotlin
@Composable
fun {Screen}Screen(
    viewModel: {Screen}ViewModel = koinViewModel(),
    clock: Clock = koinInject(),
    fileSharer: FileSharer = koinInject()
) {
    // Use injected dependencies
}
```

#### 11. **Empty State Pattern**

Screens show empty states when no data:

```kotlin
if (state.items.isEmpty()) {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EMPTY_STATE_PADDING_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.empty_state_message),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
```

#### 12. **Table/List Pattern**

Data tables use `DataTable` component:

```kotlin
DataTable(
    headers = listOf(
        TableHeaderCell("Column 1", width = 200.dp),
        TableHeaderCell("Column 2", width = 150.dp)
    ),
    rows = state.items.map { item ->
        // Row data
    }
)
```

#### 13. **Chart Integration Pattern**

Charts integrated as composables:

```kotlin
item {
    Card {
        Column {
            Text("Chart Title")
            DonutChart(
                items = chartData,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

#### 14. **Export/Share Pattern**

Screens with export functionality:

```kotlin
val scope = rememberCoroutineScope()

val onExport: () -> Unit = {
    scope.launch {
        try {
            val result = fileSharer.shareTableAsPdf(
                title = title,
                headers = headers,
                rows = rows
            )
            // Handle result
        } catch (e: Exception) {
            // Handle error
        }
    }
}

// Export button in header or action area
```

### Screen Pattern Summary

| Pattern | Purpose | Location |
|---------|---------|----------|
| **ScreenLayout** | Common wrapper | `ui/scaffold/ScreenLayout.kt` |
| **State-Based Rendering** | Loading/Success/Error | All screens |
| **Effect Handling** | One-time events | All screens |
| **Period Selector** | Date filtering | Home, Expenses, Utilities |
| **Content Extraction** | Code organization | All screens |
| **Breadcrumb Navigation** | Multi-level nav | All screens |
| **LazyColumn** | Scrollable content | All screens |
| **Card Layout** | Data presentation | All screens |
| **Responsive Design** | Window size adaptation | All screens |
| **Dependency Injection** | Koin integration | All screens |
| **Empty State** | No data handling | Most screens |
| **Data Table** | Tabular data | Congregations, Expenses |
| **Charts** | Visualizations | Expenses, Yearly, Utilities |
| **Export/Share** | Data export | Congregations, Expenses |

### Best Practices

1. ✅ **Always use ScreenLayout** for consistent structure
2. ✅ **Extract success content** to separate composable
3. ✅ **Handle all three states** (Loading, Success, Error)
4. ✅ **Use LazyColumn items** for scrollable content
5. ✅ **Follow naming conventions** ({Screen}Screen, {Screen}SuccessContent)
6. ✅ **Inject dependencies** via Koin (koinViewModel, koinInject)
7. ✅ **Use stringResource** for all text (multi-language support)
8. ✅ **Handle effects** in LaunchedEffect
9. ✅ **Use remember** for local state
10. ✅ **Pass WindowSizeClass** for responsive design

---

## Navigation

### Navigation Structure

**Routes** (`presentation/navigation/Routes.kt`):
```kotlin
object Routes {
    const val HOME = "home"
    const val CONGREGATIONS = "congregations"
    const val EXPENSES = "expenses"
    const val UTILITIES = "utilities"
    const val YEARLY = "yearly"
    const val CALCULATOR = "calculator"
    const val ASR_EXPENSES = "asr-expenses"
    const val UPLOAD = "upload"
}
```

**Navigation Graph** (`presentation/navigation/NavGraph.kt`):
- Uses Jetpack Navigation Compose
- Adaptive scaffold (drawer/rail based on screen size)
- All screens registered in NavHost

**Adaptive Navigation**:
- **Compact** (Phone): Modal drawer
- **Medium** (Tablet): Navigation rail
- **Expanded** (Desktop): Permanent drawer

---

## Platform Abstractions

### Expect/Actual Pattern

Platform-specific implementations using expect/actual:

**Platform Abstractions** (`shared/platform/`):
- `Clock.kt` - Time abstraction
- `SecureStorage.kt` - Secure storage (Android KeyStore / iOS Keychain)
- `FileHandler.kt` - File operations
- `ImageCompressor.kt` - Image compression
- `ImageCapture.kt` - Camera access
- `FileSharer.kt` - File sharing
- `Logger.kt` - Logging
- `ResourceLoader.kt` - Resource loading

**Android Implementations** (`shared/androidMain/`):
- `SecureStorage.android.kt`
- `FileHandler.android.kt`
- etc.

**iOS Implementations** (`shared/iosMain/`):
- `SecureStorage.ios.kt`
- `FileHandler.ios.kt`
- etc.

---

## Dependency Injection

### Koin Modules

**1. PlatformModule** (`shared/di/PlatformModule.kt`)
- Platform-specific implementations
- Clock, SecureStorage, FileHandler, etc.

**2. DataModule** (`shared/di/DataModule.kt`)
- Data sources (JSON implementations)
- Repository implementations

**3. DomainModule** (`shared/di/DomainModule.kt`)
- Use cases (business logic)

**4. PresentationModule** (`composeApp/di/PresentationModule.kt`)
- ResourceLoader
- Interactors
- ViewModels

**Initialization** (`composeApp/di/KoinInit.kt`):
```kotlin
fun initKoin() {
    startKoin {
        modules(
            platformModule,
            dataModule,
            domainModule,
            presentationModule
        )
    }
}
```

---

## Data Models

### Domain Models

**Transaction** (`domain/model/Transaction.kt`):
```kotlin
data class Transaction(
    val id: String,
    val type: TransactionType, // INCOME or EXPENSE
    val amount: Double,
    val description: String,
    val date: String, // YYYY-MM-DD
    val category: String,
    val congregationName: String? = null
)
```

**Congregation** (`domain/models/Congregation.kt`):
```kotlin
data class Congregation(
    val id: Long,
    val name: String,
    val location: String,
    val isActive: Boolean,
    val createdAt: Instant
)
```

**AppConfig** (`domain/models/AppConfig.kt`):
```kotlin
data class AppConfig(
    val organization: OrganizationConfig,
    val financial: FinancialConfig
)
```

---

## Data Sources

### Current Implementation: JSON Files

**Location**: `composeApp/src/commonMain/composeResources/files/`

**Files**:
- `app_config.json` - Application configuration
- `cg_donate_transactions.json` - Donation transactions
- `asr_expenses_transactions.json` - Expense transactions
- `congregations.json` - Congregation data
- `asr_expenses_last_12_months.json` - ASR expenses

**Loading**: Via `ResourceLoader` (platform abstraction)

---

## Utils & Utilities

### Shared Layer Utils (`shared/src/commonMain/kotlin/com/asr/financial/utils/`)

**Purpose**: Business logic utilities that can be used across all layers (domain, data, presentation)

#### **DateUtils.kt**
Date and time utility functions using platform `Clock` abstraction.

**Functions**:
- `getCurrentYear(clock: Clock): Int` - Get current year
- `getCurrentMonth(clock: Clock): Int` - Get current month (1-12)
- `getAvailableYears(clock: Clock): List<Int>` - Generate years from 2024 to current

**Usage**: Used in domain use cases and presentation layer for date calculations

**Example**:
```kotlin
val currentYear = getCurrentYear(clock)
val currentMonth = getCurrentMonth(clock)
```

---

### Presentation Layer Utils (`composeApp/src/commonMain/kotlin/com/asr/financial/utils/`)

**Purpose**: UI-specific utilities, extensions, and helpers for presentation layer

#### **DateTimeExtensions.kt**
Date and time extension functions and utilities for UI.

**Functions**:
- `calculateStartYearFor12Months(endMonth: Int, endYear: Int): Int` - Calculate start year for 12-month period
- `calculateStartMonthFor12Months(endMonth: Int): Int` - Calculate start month for 12-month period
- `calculatePreviousMonth(currentMonth: Int, currentYear: Int): Pair<Int, Int>` - Get previous month/year
- `getMonthsList(): List<Pair<Int, StringResource>>` - Get list of months with string resources
- `getMonthNameResource(monthNumber: Int): StringResource?` - Get month name resource by number
- `getMonthAbbreviationResource(monthNumber: Int): StringResource?` - Get month abbreviation resource
- Additional date formatting and calculation utilities

**Usage**: Used in screens for period selection, date calculations, and formatting

#### **MonthsUtils.kt**
Month-related utilities for UI.

**Functions**:
- `getMonthsList(): List<Pair<Int, StringResource>>` - List of months with string resources
- `getMonthNameResource(monthNumber: Int): StringResource?` - Get month name resource
- `getMonthAbbreviationResource(monthNumber: Int): StringResource?` - Get month abbreviation

**Usage**: Used in dropdowns, selectors, and date displays

#### **NumberExtensions.kt**
Number formatting and calculation extensions.

**Extension Functions**:
- `Double.formatDecimal(decimals: Int = 2): String` - Format with decimal places
- `Double.formatCurrency(): String` - Format as currency (RON)
- `Double.roundTo(decimals: Int = 2): Double` - Round to decimal places
- `Double.percentOf(total: Double): Double` - Calculate percentage
- `Double.percentOfAsInt(total: Double): Int` - Calculate percentage as Int
- `Double.formatForAxis(): String` - Format for chart axis labels
- Additional number manipulation functions

**Usage**: Used throughout UI for displaying numbers, currencies, percentages

**Example**:
```kotlin
val amount = 1234.56
amount.formatCurrency() // "1234.56 RON"
amount.percentOf(10000.0) // 12.35
```

#### **HtmlReportGenerator.kt**
HTML report generation utilities.

**Functions**:
- `generateCongregationsHtml(...): String` - Generate HTML report for congregations
- Additional HTML generation functions

**Usage**: Used for exporting data as HTML reports

**Data Classes**:
- `CongregationReportData` - Data structure for congregation reports

---

## UI Components

### Shared UI Components (`presentation/ui/components/`)

**Purpose**: Reusable UI components used across multiple screens

#### **Core Components**

##### **AppHeader.kt**
Gradient header component with app information.

**Features**:
- Total publishers count
- Total congregations count
- Selected month/year display
- Menu button (for compact screens)
- Refresh button (optional)
- Responsive design

**Usage**: Used in `ScreenLayout` wrapper for all screens

##### **Breadcrumb.kt**
Breadcrumb navigation component.

**Features**:
- Multi-level navigation display
- Clickable navigation items
- Current page indicator
- Responsive design

**Usage**: Used in `ScreenLayout` for navigation hierarchy

##### **TwoLevelHouseIcon.kt**
Custom icon component (two-level house icon).

**Usage**: Used in app branding/header

---

#### **Card Components** (`components/cards/`)

##### **SummaryCard.kt**
Reusable summary card component.

**Features**:
- Title and value display
- Icon support
- Color customization
- Material3 Card styling

**Usage**: Used in Home, Expenses, and other screens for displaying statistics

---

#### **Period Components** (`components/period/`)

##### **PeriodSelectorCard.kt**
Month and year selector card.

**Features**:
- Year dropdown
- Month dropdown
- Period selection
- Material3 styling

**Usage**: Used in Home, Expenses, Utilities screens for period filtering

##### **MonthDropdown.kt**
Month selection dropdown component.

**Usage**: Used within `PeriodSelectorCard`

##### **YearDropdown.kt**
Year selection dropdown component.

**Usage**: Used within `PeriodSelectorCard`

---

#### **State Components** (`components/states/`)

##### **StateComponents.kt**
Loading and error state components.

**Components**:
- `LoadingContent()` - Loading indicator
- `ErrorContent(message: String)` - Error message display

**Usage**: Used in all screens for loading/error states

**Example**:
```kotlin
when (val state = uiState) {
    is HomeState.Loading -> LoadingContent()
    is HomeState.Error -> ErrorContent(state.message)
    // ...
}
```

---

#### **Table Components** (`components/table/`)

##### **DataTable.kt**
Reusable data table component.

**Features**:
- Configurable headers
- Row rendering
- Column widths
- Material3 styling
- Responsive design

**Usage**: Used in Congregations, Expenses, Utilities screens for tabular data

**Components**:
- `DataTable` - Main table composable
- `TableHeaderCell` - Header cell component

---

### Component Architecture

#### **Component Organization**

```
presentation/ui/components/
├── AppHeader.kt              # Core header component
├── Breadcrumb.kt             # Navigation breadcrumb
├── TwoLevelHouseIcon.kt      # Custom icon
├── cards/                    # Card components
│   └── SummaryCard.kt
├── period/                   # Period selection components
│   ├── PeriodSelectorCard.kt
│   ├── MonthDropdown.kt
│   └── YearDropdown.kt
├── states/                   # State components
│   └── StateComponents.kt
└── table/                    # Table components
    └── DataTable.kt
```

#### **Component Usage Patterns**

**1. Core Layout Components**
- Used in `ScreenLayout` wrapper
- AppHeader, Breadcrumb

**2. Data Display Components**
- Used in screen success content
- SummaryCard, DataTable

**3. Input Components**
- Used for user interaction
- PeriodSelectorCard, MonthDropdown, YearDropdown

**4. State Components**
- Used for loading/error states
- LoadingContent, ErrorContent

#### **Component Design Principles**

1. **Pure Composables**: Components receive data as parameters, no ViewModel dependency
2. **Reusability**: Components can be used across multiple screens
3. **Material3**: All components use Material3 design system
4. **Responsive**: Components adapt to `WindowSizeClass`
5. **Type Safety**: Strong typing with Kotlin
6. **Accessibility**: Components follow accessibility guidelines

#### **When to Create a Component**

**Create in `presentation/ui/components/` if**:
- ✅ Used in 2+ screens
- ✅ General-purpose UI element
- ✅ Reusable across the app

**Create in `presentation/screens/{screen}/components/` if**:
- ✅ Only used in one screen
- ✅ Screen-specific functionality
- ✅ Tightly coupled to screen logic

---

### Utils & Components Summary

| Layer | Location | Purpose | Files |
|-------|----------|---------|-------|
| **Shared Utils** | `shared/utils/` | Business logic utilities | `DateUtils.kt` |
| **Presentation Utils** | `composeApp/utils/` | UI utilities & extensions | `DateTimeExtensions.kt`, `MonthsUtils.kt`, `NumberExtensions.kt`, `HtmlReportGenerator.kt` |
| **UI Components** | `presentation/ui/components/` | Reusable UI components | `AppHeader.kt`, `Breadcrumb.kt`, `SummaryCard.kt`, `PeriodSelectorCard.kt`, `DataTable.kt`, `StateComponents.kt`, etc. |
| **Screen Components** | `presentation/screens/{screen}/components/` | Screen-specific components | Various row/card components per screen |

### Charts (`presentation/screens/charts/`)

**Custom Canvas-based implementations** (no external charting library):
- `BarChart.kt` - Custom bar chart using Compose Canvas
- `DonutChart.kt` - Custom donut/pie chart using Compose Canvas
- `LineChart.kt` - Custom line chart using Compose Canvas

---

## Theme & Design System

### Theme Structure (`presentation/theme/`)

- `Color.kt` - Color scheme (light/dark)
  - Custom semantic colors: `SuccessGreen`, `SuccessGreenDark`, `WarningOrange`, `ChartSecondaryGreen`
  - Material3 color schemes for light and dark themes
- `Type.kt` - Typography definitions
- `Shape.kt` - Shape definitions (rounded corners, etc.)
- `Theme.kt` - MaterialTheme setup with `AppTheme` composable

**Usage**: `AppTheme { }` wrapper around all screens

### UI Constants (`presentation/ui/constants/`)

#### **UIConstants.kt**
Shared UI constants used across multiple screens.

**Constants**:
- **Spacing**: `CARD_PADDING_DP`, `SECTION_SPACING_DP`, `SMALL_SPACING_DP`, `TINY_SPACING_DP`, `EMPTY_STATE_PADDING_DP`
- **Defaults**: `DEFAULT_YEAR`, `DEFAULT_MONTH`, `DEFAULT_TOTAL_PUBLISHERS`, `DEFAULT_PUBLISHER_CONTRIBUTION`

**Usage**: Use for consistent spacing and default values across screens

**Example**:
```kotlin
Modifier.padding(UIConstants.CARD_PADDING_DP.dp)
```

### Responsive Design (`presentation/ui/responsive/`)

#### **WindowSizeClass.kt**
Responsive design breakpoints for different screen sizes.

**Window Size Classes**:
- `Compact` - Phone (width < 600dp)
- `Medium` - Tablet (600dp ≤ width < 840dp)
- `Expanded` - Desktop/Large Tablet (width ≥ 840dp)

**Usage**: All screens receive `WindowSizeClass` parameter and adapt layout accordingly

**Example**:
```kotlin
@Composable
fun MyScreen(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass) {
        WindowSizeClass.Compact -> { /* Phone layout */ }
        WindowSizeClass.Medium -> { /* Tablet layout */ }
        WindowSizeClass.Expanded -> { /* Desktop layout */ }
    }
}
```

### Navigation Components (`presentation/ui/navigation/`)

#### **NavigationComponents.kt**
Navigation drawer, rail, and menu components.

**Components**:
- `NavigationItem` - Navigation item data class
- `DrawerNavigationContent` - Drawer navigation content
- `AppNavigationRail` - Navigation rail for tablets
- `PermanentNavigationDrawer` - Permanent drawer for desktop

**Usage**: Used in `AdaptiveScaffold` for responsive navigation

---

## Multi-language Support

### String Resources

**Location**: `composeApp/src/commonMain/composeResources/values/`

**Languages**:
- `values/strings.xml` - Romanian (default)
- `values-hu/strings.xml` - Hungarian

**Usage**: `stringResource(Res.string.key)` in composables

---

## Key Patterns & Conventions

### 1. Naming Conventions
- **Interactors**: `{Screen}Interactor` (e.g., `HomeInteractor`)
- **ViewModels**: `{Screen}ViewModel` (e.g., `HomeViewModel`)
- **States**: `{Screen}State` (e.g., `HomeState`)
- **Events**: `{Screen}Event` (e.g., `HomeEvent`)
- **Effects**: `{Screen}Effect` (e.g., `HomeEffect`)
- **Screens**: `{Screen}Screen` (e.g., `HomeScreen`)

### 2. State Pattern
All states follow this pattern:
```kotlin
sealed interface {Screen}State {
    data object Loading : {Screen}State
    data class Success(...) : {Screen}State
    data class Error(val message: String) : {Screen}State
}
```

### 3. ViewModel Pattern
All ViewModels follow this pattern:
```kotlin
class {Screen}ViewModel(
    private val interactor: {Screen}Interactor
) : ViewModel() {
    val uiState = interactor.uiState
    val uiEffect = interactor.uiEffect
    
    fun handleEvent(event: {Screen}Event) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }
}
```

### 4. Screen Pattern
All screens follow this pattern:
```kotlin
@Composable
fun {Screen}Screen(
    viewModel: {Screen}ViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            // Handle effects
        }
    }
    
    when (val state = uiState) {
        is {Screen}State.Loading -> LoadingContent()
        is {Screen}State.Success -> { /* Render UI */ }
        is {Screen}State.Error -> ErrorContent(state.message)
    }
}
```

---

## Testing

### Test Structure
- `shared/src/commonTest/` - Shared tests
- `composeApp/src/commonTest/` - UI tests

---

## Build Configuration

### Modules
- `:shared` - Business logic module
- `:composeApp` - UI module
- `:androidApp` - Android wrapper

### Gradle Files
- `build.gradle.kts` (root) - Project configuration
- `shared/build.gradle.kts` - Shared module config
- `composeApp/build.gradle.kts` - Compose app config
- `androidApp/build.gradle.kts` - Android app config
- `gradle/libs.versions.toml` - Dependency versions

---

## Current Data Flow

### Example: Loading Transactions

1. **Screen**: `HomeScreen` calls `viewModel.handleEvent(HomeEvent.LoadData)`
2. **ViewModel**: `HomeViewModel` delegates to `HomeInteractor.processEvent()`
3. **Interactor**: `HomeInteractor` calls `GetTransactionsUseCase()`
4. **Use Case**: `GetTransactionsUseCase` calls `TransactionRepository.getAllTransactions()`
5. **Repository**: `TransactionRepositoryImpl` calls `JsonTransactionDataSource.getAll()`
6. **Data Source**: `JsonTransactionDataSource` loads JSON file via `ResourceLoader`
7. **Response**: Data flows back up through layers
8. **State**: `HomeInteractor` emits `HomeState.Success(data)`
9. **UI**: `HomeScreen` observes state and renders UI

---

## Summary

### Strengths
✅ **Clean Architecture** - Clear separation of concerns
✅ **MVI Pattern** - Unidirectional data flow
✅ **Platform Abstractions** - Easy to add new platforms
✅ **Type Safety** - Kotlin + SQLDelight
✅ **Reusable Components** - Well-structured UI components
✅ **Multi-language** - Romanian + Hungarian support
✅ **Responsive Design** - Adaptive navigation based on screen size

### Areas for Future Enhancement
- Database implementation (currently JSON-based)
- API integration (currently JSON files)
- Offline support
- Data synchronization
- More comprehensive testing

---

## Next Steps for New Features

When implementing a new feature, follow this structure:

1. **Domain Layer** (`shared/domain/`):
   - Create domain models
   - Define repository interface
   - Create use cases

2. **Data Layer** (`shared/data/`):
   - Implement data source
   - Implement repository

3. **Presentation Layer** (`composeApp/presentation/`):
   - Create State, Event, Effect
   - Create Interactor
   - Create ViewModel
   - Create Screen

4. **DI Configuration**:
   - Add to DataModule
   - Add to DomainModule
   - Add to PresentationModule

5. **Navigation**:
   - Add route to Routes.kt
   - Add screen to NavGraph.kt
   - Add navigation item if needed

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-17  
**Project**: ASR Financial Management

# Feature Implementation Guide

## Overview

This guide provides a step-by-step template for implementing new features in the ASR Financial Management app. Follow this guide to ensure consistency with the existing architecture, patterns, and conventions.

**Before starting**: Review `PROJECT_ANALYSIS.md` to understand the architecture, patterns, and existing codebase structure.

---

## Feature Implementation Checklist

### Phase 1: Planning & Design

- [ ] **Define Feature Requirements**
  - What is the feature's purpose?
  - What data does it need?
  - What user actions are required?
  - What screens/components are needed?

- [ ] **Identify Dependencies**
  - Does it need new domain models?
  - Does it need new data sources?
  - Does it need new use cases?
  - Does it reuse existing components?

- [ ] **Design UI/UX**
  - Screen layout and components
  - Navigation flow
  - State management requirements
  - Error handling scenarios

---

## Step-by-Step Implementation

### Step 1: Domain Layer (`shared/domain/`)

#### 1.1 Create Domain Models (if needed)

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/domain/models/`

**Pattern**:
```kotlin
package com.asr.financial.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class MyFeatureModel(
    val id: String,
    val name: String,
    val amount: Double,
    val date: String // Format: YYYY-MM-DD
)
```

**Checklist**:
- [ ] Use `@Serializable` if data comes from JSON
- [ ] Use `kotlinx.datetime.Instant` for timestamps
- [ ] Keep models pure (no Android/iOS dependencies)
- [ ] Add helper functions if needed (e.g., `getYear()`, `getMonth()`)

#### 1.2 Create Repository Interface

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/domain/repository/`

**Pattern**:
```kotlin
package com.asr.financial.domain.repository

import com.asr.financial.domain.models.MyFeatureModel

interface MyFeatureRepository {
    suspend fun getAll(): List<MyFeatureModel>
    suspend fun getById(id: String): MyFeatureModel?
    suspend fun getByMonth(month: Int, year: Int): List<MyFeatureModel>
}
```

**Checklist**:
- [ ] Define only what's needed (don't over-engineer)
- [ ] Use suspend functions for async operations
- [ ] Return domain models, not data models

#### 1.3 Create Use Cases

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/domain/usecase/`

**Pattern**:
```kotlin
package com.asr.financial.domain.usecase

import com.asr.financial.domain.repository.MyFeatureRepository

class GetMyFeatureUseCase(
    private val repository: MyFeatureRepository
) {
    suspend operator fun invoke(): List<MyFeatureModel> {
        return repository.getAll()
    }
}

class GetMyFeatureByMonthUseCase(
    private val repository: MyFeatureRepository
) {
    suspend operator fun invoke(month: Int, year: Int): List<MyFeatureModel> {
        return repository.getByMonth(month, year)
    }
}
```

**Checklist**:
- [ ] One use case per business operation
- [ ] Use `operator fun invoke()` for clean syntax
- [ ] Keep use cases focused and simple
- [ ] Add error handling if needed

---

### Step 2: Data Layer (`shared/data/`)

#### 2.1 Create Data Source Interface

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/`

**Pattern**:
```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.domain.models.MyFeatureModel

interface MyFeatureDataSource {
    suspend fun getAll(): List<MyFeatureModel>
    suspend fun getById(id: String): MyFeatureModel?
    suspend fun getByMonth(month: Int, year: Int): List<MyFeatureModel>
}
```

#### 2.2 Implement Data Source

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/`

**For JSON Data Source**:
```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.domain.models.MyFeatureModel
import com.asr.financial.platform.ResourceLoader
import kotlinx.serialization.json.Json

class JsonMyFeatureDataSource(
    private val resourceLoader: ResourceLoader
) : MyFeatureDataSource {
    
    private companion object {
        const val JSON_FILE = "my_feature_data.json"
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override suspend fun getAll(): List<MyFeatureModel> {
        val jsonString = resourceLoader.loadResourceAsString(JSON_FILE)
            ?: return emptyList()
        return try {
            json.decodeFromString<List<MyFeatureModel>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Implement other methods...
}
```

**Checklist**:
- [ ] Add JSON file to `composeApp/src/commonMain/composeResources/files/`
- [ ] Handle errors gracefully (return empty list, null, etc.)
- [ ] Use `ResourceLoader` for platform-agnostic resource loading

#### 2.3 Implement Repository

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/data/repository/`

**Pattern**:
```kotlin
package com.asr.financial.data.repository

import com.asr.financial.data.datasource.MyFeatureDataSource
import com.asr.financial.domain.models.MyFeatureModel
import com.asr.financial.domain.repository.MyFeatureRepository

class MyFeatureRepositoryImpl(
    private val dataSource: MyFeatureDataSource
) : MyFeatureRepository {
    
    override suspend fun getAll(): List<MyFeatureModel> {
        return dataSource.getAll()
    }
    
    // Implement other methods...
}
```

**Checklist**:
- [ ] Repository is data-source agnostic
- [ ] Can switch between JSON, API, Database without changing domain layer
- [ ] Keep implementation simple (delegate to data source)

---

### Step 3: Dependency Injection

#### 3.1 Add to DataModule

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/di/DataModule.kt`

```kotlin
val dataModule = module {
    // ... existing data sources
    
    single<MyFeatureDataSource> { JsonMyFeatureDataSource(get()) }
    
    // ... existing repositories
    
    single<MyFeatureRepository> { MyFeatureRepositoryImpl(get()) }
}
```

**Checklist**:
- [ ] Use `single` for singleton instances
- [ ] Use `get()` for dependency injection
- [ ] Order: Data sources first, then repositories

#### 3.2 Add to DomainModule

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/di/DomainModule.kt`

```kotlin
val domainModule = module {
    // ... existing use cases
    
    factory { GetMyFeatureUseCase(get()) }
    factory { GetMyFeatureByMonthUseCase(get()) }
}
```

**Checklist**:
- [ ] Use `factory` for use cases (new instance each time)
- [ ] Group related use cases together

---

### Step 4: Presentation Layer - MVI

#### 4.1 Create State

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/state/`

**Pattern**:
```kotlin
package com.asr.financial.presentation.mvi.state

sealed interface MyFeatureState {
    data object Loading : MyFeatureState
    
    data class Success(
        val items: List<MyFeatureItem>,
        val selectedYear: Int,
        val selectedMonth: Int,
        val availableYears: List<Int>,
        val isRefreshing: Boolean = false
    ) : MyFeatureState
    
    data class Error(val message: String) : MyFeatureState
}
```

**Checklist**:
- [ ] Always include `Loading`, `Success`, and `Error` states
- [ ] `Success` state should contain all data needed for UI
- [ ] Use `data object` for stateless states
- [ ] Use `data class` for states with data

#### 4.2 Create Events

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/event/`

**Pattern**:
```kotlin
package com.asr.financial.presentation.mvi.event

sealed interface MyFeatureEvent {
    data object LoadData : MyFeatureEvent
    data class FilterByPeriod(val month: Int, val year: Int) : MyFeatureEvent
    data class ItemClicked(val itemId: String) : MyFeatureEvent
    data object Refresh : MyFeatureEvent
}
```

**Checklist**:
- [ ] One event per user action
- [ ] Use `data object` for actions without parameters
- [ ] Use `data class` for actions with parameters
- [ ] Name events clearly (verb + noun)

#### 4.3 Create Effects

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/effect/`

**Pattern**:
```kotlin
package com.asr.financial.presentation.mvi.effect

sealed interface MyFeatureEffect {
    data class ShowToast(val message: String) : MyFeatureEffect
    data class NavigateToDetails(val itemId: String) : MyFeatureEffect
    data object ScrollToTop : MyFeatureEffect
}
```

**Checklist**:
- [ ] Effects are one-time events (navigation, toasts, etc.)
- [ ] Not part of state (don't store in state)
- [ ] Use `data class` for effects with parameters

#### 4.4 Create Interactor

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/interactor/`

**Pattern**:
```kotlin
package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.usecase.*
import com.asr.financial.presentation.mvi.effect.MyFeatureEffect
import com.asr.financial.presentation.mvi.event.MyFeatureEvent
import com.asr.financial.presentation.mvi.state.MyFeatureState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class MyFeatureInteractor(
    private val getMyFeatureUseCase: GetMyFeatureUseCase,
    private val getMyFeatureByMonthUseCase: GetMyFeatureByMonthUseCase
) {
    private val _uiState = MutableStateFlow<MyFeatureState>(MyFeatureState.Loading)
    val uiState = _uiState.asStateFlow()
    
    private val _uiEffectChannel = Channel<MyFeatureEffect>(Channel.BUFFERED)
    val uiEffect: Flow<MyFeatureEffect> = _uiEffectChannel.receiveAsFlow()
    
    suspend fun processEvent(event: MyFeatureEvent) {
        when (event) {
            is MyFeatureEvent.LoadData -> loadData()
            is MyFeatureEvent.FilterByPeriod -> filterByPeriod(event.month, event.year)
            is MyFeatureEvent.ItemClicked -> navigateToDetails(event.itemId)
            is MyFeatureEvent.Refresh -> refreshData()
        }
    }
    
    private suspend fun loadData() {
        _uiState.emit(MyFeatureState.Loading)
        try {
            val items = getMyFeatureUseCase()
            emitSuccessState(items)
        } catch (e: Exception) {
            _uiState.emit(MyFeatureState.Error(e.message ?: "Unknown error"))
            _uiEffectChannel.send(MyFeatureEffect.ShowToast("Failed to load data"))
        }
    }
    
    private suspend fun filterByPeriod(month: Int, year: Int) {
        try {
            val items = getMyFeatureByMonthUseCase(month, year)
            emitSuccessState(items, year, month)
        } catch (e: Exception) {
            _uiState.emit(MyFeatureState.Error(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun navigateToDetails(itemId: String) {
        _uiEffectChannel.send(MyFeatureEffect.NavigateToDetails(itemId))
    }
    
    private suspend fun refreshData() {
        val currentState = _uiState.value
        if (currentState is MyFeatureState.Success) {
            _uiState.emit(currentState.copy(isRefreshing = true))
        }
        // Refresh logic...
    }
    
    private suspend fun emitSuccessState(
        items: List<MyFeatureItem>,
        year: Int = getCurrentYear(),
        month: Int = getCurrentMonth()
    ) {
        _uiState.emit(MyFeatureState.Success(
            items = items,
            selectedYear = year,
            selectedMonth = month,
            availableYears = getAvailableYears()
        ))
    }
}
```

**Checklist**:
- [ ] All business logic in Interactor (not in ViewModel or Screen)
- [ ] Use Use Cases (from domain layer)
- [ ] Emit states and effects
- [ ] Handle errors gracefully
- [ ] Cache data if needed for performance

#### 4.5 Create ViewModel

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/viewmodel/`

**Pattern**:
```kotlin
package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.event.MyFeatureEvent
import com.asr.financial.presentation.mvi.interactor.MyFeatureInteractor
import kotlinx.coroutines.launch

class MyFeatureViewModel(
    private val interactor: MyFeatureInteractor
) : ViewModel() {
    
    val uiState = interactor.uiState
    val uiEffect = interactor.uiEffect
    
    fun handleEvent(event: MyFeatureEvent) {
        viewModelScope.launch {
            interactor.processEvent(event)
        }
    }
    
    init {
        handleEvent(MyFeatureEvent.LoadData)
    }
}
```

**Checklist**:
- [ ] ViewModel is thin (just delegates to Interactor)
- [ ] Expose `uiState` and `uiEffect` from Interactor
- [ ] Use `viewModelScope.launch` for event handling
- [ ] Optionally load data in `init` if needed

#### 4.6 Add to PresentationModule

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/di/PresentationModule.kt`

```kotlin
val presentationModule = module {
    // ... existing interactors
    
    factory { MyFeatureInteractor(get(), get()) }
    
    // ... existing view models
    
    viewModel { MyFeatureViewModel(get()) }
}
```

**Checklist**:
- [ ] Use `factory` for Interactors
- [ ] Use `viewModel` DSL for ViewModels (Koin)

---

### Step 5: Presentation Layer - Screen

#### 5.1 Create Screen Directory Structure

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/myfeature/`

**Structure**:
```
myfeature/
├── MyFeatureScreen.kt
├── MyFeatureConstants.kt
├── MyFeatureStat.kt (if needed)
└── components/
    └── MyFeatureRow.kt (if needed)
```

#### 5.2 Create Constants File

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/myfeature/MyFeatureConstants.kt`

**Pattern**:
```kotlin
package com.asr.financial.presentation.screens.myfeature

object MyFeatureConstants {
    // Table column widths
    const val TABLE_NAME_WIDTH_DP = 120
    const val TABLE_AMOUNT_WIDTH_DP = 100
    
    // Icon sizes
    const val ICON_SIZE_DP = 20
    
    // Chart dimensions (if needed)
    const val CHART_HEIGHT_DP = 350
}
```

**Checklist**:
- [ ] Centralize all magic numbers
- [ ] Use descriptive names
- [ ] Add comments if needed

#### 5.3 Create Presentation Model (if needed)

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/myfeature/MyFeatureStat.kt`

**Pattern**:
```kotlin
package com.asr.financial.presentation.screens.myfeature

data class MyFeatureStat(
    val name: String,
    val amount: Double,
    val percentage: Double,
    val isHighlighted: Boolean = false
)
```

**Checklist**:
- [ ] Only if you need screen-specific data structures
- [ ] Not domain models (those are in `shared/domain/models/`)
- [ ] Used for UI rendering and calculations

#### 5.4 Create Screen Composable

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/myfeature/MyFeatureScreen.kt`

**Pattern**:
```kotlin
package com.asr.financial.presentation.screens.myfeature

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.asr.financial.presentation.mvi.event.MyFeatureEvent
import com.asr.financial.presentation.mvi.effect.MyFeatureEffect
import com.asr.financial.presentation.mvi.state.MyFeatureState
import com.asr.financial.presentation.mvi.viewmodel.MyFeatureViewModel
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MyFeatureScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: MyFeatureViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is MyFeatureEffect.ShowToast -> {
                    // Show toast (implement toast mechanism)
                }
                is MyFeatureEffect.NavigateToDetails -> {
                    onNavigate(Routes.DETAILS)
                }
                is MyFeatureEffect.ScrollToTop -> {
                    // Scroll to top (if using LazyColumn with scroll state)
                }
            }
        }
    }
    
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_my_feature))
        ),
        selectedMonth = (uiState as? MyFeatureState.Success)?.selectedMonth?.let { 
            getMonthName(it) 
        } ?: "",
        selectedYear = (uiState as? MyFeatureState.Success)?.selectedYear ?: 2026,
        showRefreshButton = true,
        isRefreshing = (uiState as? MyFeatureState.Success)?.isRefreshing ?: false,
        onNavigate = onNavigate,
        onMenuClick = onMenuClick,
        onRefreshClick = { viewModel.handleEvent(MyFeatureEvent.Refresh) }
    ) {
        when (val state = uiState) {
            is MyFeatureState.Loading -> {
                item {
                    LoadingContent()
                }
            }
            
            is MyFeatureState.Success -> {
                item {
                    MyFeatureSuccessContent(
                        state = state,
                        onItemClick = { itemId ->
                            viewModel.handleEvent(MyFeatureEvent.ItemClicked(itemId))
                        }
                    )
                }
            }
            
            is MyFeatureState.Error -> {
                item {
                    ErrorContent(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun MyFeatureSuccessContent(
    state: MyFeatureState.Success,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Period selector (if needed)
        // Summary cards
        // Data table/list
        // Charts (if needed)
    }
}
```

**Checklist**:
- [ ] Use `ScreenLayout` wrapper
- [ ] Handle all three states (Loading, Success, Error)
- [ ] Extract success content to separate composable
- [ ] Use `stringResource` for all text (multi-language)
- [ ] Use `koinViewModel()` for dependency injection
- [ ] Handle effects in `LaunchedEffect`
- [ ] Use `collectLatest` for effects

#### 5.5 Create Screen Components (if needed)

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/myfeature/components/`

**Pattern**:
```kotlin
package com.asr.financial.presentation.screens.myfeature.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MyFeatureRow(
    item: MyFeatureItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = item.name)
            Text(text = item.amount.formatCurrency())
        }
    }
}
```

**Checklist**:
- [ ] Only create if component is screen-specific
- [ ] If used in 2+ screens, move to `presentation/ui/components/`
- [ ] Keep components pure (no ViewModel dependency)

---

### Step 6: Navigation

#### 6.1 Add Route

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/navigation/Routes.kt`

```kotlin
object Routes {
    // ... existing routes
    const val MY_FEATURE = "my-feature"
}
```

#### 6.2 Add to Navigation Graph

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/navigation/NavGraph.kt`

```kotlin
composable(Routes.MY_FEATURE) {
    MyFeatureScreen(
        windowSizeClass = windowSizeClass,
        onNavigate = { route -> navController.navigate(route) },
        onMenuClick = onMenuClick
    )
}
```

#### 6.3 Add Navigation Item (if needed)

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/ui/navigation/NavigationComponents.kt`

```kotlin
sealed class NavigationItem(
    val route: String,
    val label: StringResource,
    val icon: ImageVector
) {
    // ... existing items
    data object MyFeature : NavigationItem(
        Routes.MY_FEATURE, 
        Res.string.nav_my_feature, 
        Icons.Default.MyIcon
    )
}

val navigationItems = listOf(
    // ... existing items
    NavigationItem.MyFeature
)
```

**Checklist**:
- [ ] Add route constant
- [ ] Register screen in NavGraph
- [ ] Add navigation item if it should appear in menu
- [ ] Add string resource for navigation label

---

### Step 7: String Resources

#### 7.1 Add Strings

**Location**: `composeApp/src/commonMain/composeResources/values/strings.xml` (Romanian)

```xml
<string name="nav_my_feature">My Feature</string>
<string name="my_feature_title">My Feature Title</string>
<string name="my_feature_empty">No data available</string>
```

**Location**: `composeApp/src/commonMain/composeResources/values-hu/strings.xml` (Hungarian)

```xml
<string name="nav_my_feature">Saját Funkció</string>
<string name="my_feature_title">Saját Funkció Cím</string>
<string name="my_feature_empty">Nincs elérhető adat</string>
```

**Checklist**:
- [ ] Add all strings to both language files
- [ ] Use descriptive keys (screen_action_item format)
- [ ] Never hardcode strings in code

---

### Step 8: Testing & Validation

#### 8.1 Test Checklist

- [ ] **Domain Layer**
  - [ ] Use cases work correctly
  - [ ] Repository interface is properly defined
  - [ ] Domain models are correct

- [ ] **Data Layer**
  - [ ] Data source loads data correctly
  - [ ] Repository implementation works
  - [ ] Error handling is proper

- [ ] **Presentation Layer**
  - [ ] Screen displays correctly
  - [ ] All states work (Loading, Success, Error)
  - [ ] Effects are handled properly
  - [ ] Navigation works
  - [ ] Responsive design works (Compact, Medium, Expanded)
  - [ ] Multi-language works (Romanian, Hungarian)

- [ ] **Integration**
  - [ ] DI is configured correctly
  - [ ] Navigation is set up
  - [ ] String resources are added
  - [ ] No compilation errors
  - [ ] No runtime errors

---

## Common Patterns & Best Practices

### 1. Period Selection Pattern

If your feature needs date/month/year filtering:

```kotlin
var selectedYear by remember { mutableStateOf(defaultYear) }
var selectedMonth by remember { mutableStateOf(defaultMonth) }
var showYearDropdown by remember { mutableStateOf(false) }
var showMonthDropdown by remember { mutableStateOf(false) }

LaunchedEffect(selectedYear, selectedMonth) {
    viewModel.handleEvent(MyFeatureEvent.FilterByPeriod(selectedMonth, selectedYear))
}

item {
    PeriodSelectorCard(
        selectedYear = selectedYear,
        selectedMonth = selectedMonth,
        years = state.availableYears,
        months = months,
        // ... other params
    )
}
```

### 2. Empty State Pattern

```kotlin
if (state.items.isEmpty()) {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UIConstants.EMPTY_STATE_PADDING_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.my_feature_empty),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

### 3. Data Table Pattern

```kotlin
item {
    DataTable(
        columns = listOf(
            TableColumn("Name", TABLE_NAME_WIDTH_DP.dp),
            TableColumn("Amount", TABLE_AMOUNT_WIDTH_DP.dp)
        ),
        headerContent = {
            // Header cells
        }
    ) {
        // Row content
    }
}
```

### 4. Card Layout Pattern

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
) {
    Column(
        modifier = Modifier.padding(UIConstants.CARD_PADDING_DP.dp)
    ) {
        Text(
            text = stringResource(Res.string.card_title),
            style = MaterialTheme.typography.titleMedium
        )
        // Content
    }
}
```

---

## File Checklist

Use this checklist to ensure you've created all necessary files:

### Domain Layer
- [ ] Domain model (`shared/domain/models/`)
- [ ] Repository interface (`shared/domain/repository/`)
- [ ] Use cases (`shared/domain/usecase/`)

### Data Layer
- [ ] Data source interface (`shared/data/datasource/`)
- [ ] Data source implementation (`shared/data/datasource/`)
- [ ] Repository implementation (`shared/data/repository/`)

### Dependency Injection
- [ ] Added to DataModule
- [ ] Added to DomainModule
- [ ] Added to PresentationModule

### Presentation Layer - MVI
- [ ] State (`presentation/mvi/state/`)
- [ ] Events (`presentation/mvi/event/`)
- [ ] Effects (`presentation/mvi/effect/`)
- [ ] Interactor (`presentation/mvi/interactor/`)
- [ ] ViewModel (`presentation/mvi/viewmodel/`)

### Presentation Layer - Screen
- [ ] Screen directory (`presentation/screens/{screen}/`)
- [ ] Screen composable (`{Screen}Screen.kt`)
- [ ] Constants file (`{Screen}Constants.kt`)
- [ ] Presentation model (`{Screen}Stat.kt` - if needed)
- [ ] Screen components (`components/` - if needed)

### Navigation
- [ ] Route added to Routes.kt
- [ ] Screen registered in NavGraph.kt
- [ ] Navigation item added (if needed)

### Resources
- [ ] Strings added to `values/strings.xml` (Romanian)
- [ ] Strings added to `values-hu/strings.xml` (Hungarian)
- [ ] JSON data file added (if needed)

---

## Quick Reference

### Import Patterns

**Domain Models**:
```kotlin
import com.asr.financial.domain.models.MyModel
```

**Use Cases**:
```kotlin
import com.asr.financial.domain.usecase.GetMyFeatureUseCase
```

**MVI Components**:
```kotlin
import com.asr.financial.presentation.mvi.state.MyFeatureState
import com.asr.financial.presentation.mvi.event.MyFeatureEvent
import com.asr.financial.presentation.mvi.effect.MyFeatureEffect
import com.asr.financial.presentation.mvi.interactor.MyFeatureInteractor
import com.asr.financial.presentation.mvi.viewmodel.MyFeatureViewModel
```

**UI Components**:
```kotlin
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
```

**Utils**:
```kotlin
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getCurrentMonth
```

**Resources**:
```kotlin
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
```

---

## Troubleshooting

### Common Issues

1. **"Unresolved reference" errors**
   - Check DI configuration (all modules registered)
   - Check imports
   - Rebuild project

2. **State not updating**
   - Check if Interactor is emitting states correctly
   - Check if ViewModel is properly connected
   - Check if Screen is observing state correctly

3. **Navigation not working**
   - Check if route is added to Routes.kt
   - Check if screen is registered in NavGraph.kt
   - Check if route string matches exactly

4. **Strings not showing**
   - Check if string is added to both language files
   - Check if using `stringResource()` correctly
   - Rebuild project to regenerate resources

5. **Data not loading**
   - Check if JSON file exists and is valid
   - Check if data source is implemented correctly
   - Check if use case is calling repository correctly

---

## Final Checklist

Before considering the feature complete:

- [ ] All files created following patterns
- [ ] All DI configured correctly
- [ ] Navigation works
- [ ] Multi-language support added
- [ ] Responsive design works
- [ ] Error handling implemented
- [ ] Loading states work
- [ ] Empty states work
- [ ] No hardcoded strings
- [ ] No magic numbers (use constants)
- [ ] Code follows naming conventions
- [ ] No compilation errors
- [ ] No runtime errors
- [ ] Tested on Android
- [ ] Tested on iOS (if applicable)

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-17  
**Project**: ASR Financial Management

# PreachTrack Architecture Analysis

## MVI Pattern Structure

### 1. ViewModel
```kotlin
class ActivitiesViewModel(
    private val activityUIInteractor: ActivityUIInteractor
) : ViewModel() {
    val uiEffect = activityUIInteractor.uiEffect
    val activityListDataState = activityUIInteractor.uiState
    
    fun handleEvent(event: ActivityEvent) {
        viewModelScope.launch {
            activityUIInteractor.processEvent(event)
        }
    }
}
```

**Key Points:**
- ViewModel delegates to Interactor
- Exposes `uiState` (StateFlow) and `uiEffect` (Flow)
- Single `handleEvent()` function for all events

### 2. Interactor (UI Logic Layer)

```kotlin
class ActivityUIInteractor(private val activitiesDBUseCase: ActivitiesDBUseCase) {
    private val _uiState = MutableStateFlow<ActivityListDataState<Activity>>(ActivityListDataState())
    val uiState = _uiState.asStateFlow()
    
    private val _uiEffectChannel = Channel<ActivityEffect>(Channel.UNLIMITED)
    val uiEffect: Flow<ActivityEffect> = _uiEffectChannel.receiveAsFlow()
    
    suspend fun processEvent(event: ActivityEvent) {
        when (event) {
            is ActivityEvent.LoadActivities -> loadActivities(event.monthYear)
            is ActivityEvent.DeleteActivity -> deleteActivity(event.activity)
            is ActivityEvent.SaveActivity -> saveActivity(event.activity)
        }
    }
}
```

**Key Points:**
- Manages state with `MutableStateFlow`
- Manages effects with `Channel`
- Calls UseCases from domain layer
- Pure business logic, no Android dependencies

### 3. State (Data Classes)

```kotlin
data class ActivityListDataState<T>(
    val isLoading: Boolean = false,
    val data: List<T> = emptyList(),
    val error: ErrorTypeDataLoad = ErrorTypeDataLoad.NONE,
    val errorMsg: String = ""
)
```

**Key Points:**
- Immutable data classes
- Generic where possible
- Contains UI state (loading, data, error)

### 4. Events (Sealed Classes)

```kotlin
sealed class ActivityEvent {
    data class LoadActivities(val monthYear: Pair<String, String>?) : ActivityEvent()
    data class SaveActivity(val activity: Activity) : ActivityEvent()
    data class DeleteActivity(val activity: Activity) : ActivityEvent()
}
```

**Key Points:**
- Sealed classes for type safety
- Represents user actions
- Can carry data

### 5. Effects (Sealed Classes)

```kotlin
sealed class ActivityEffect {
    data object RequestUIEffectActivityListDisplay : ActivityEffect()
}
```

**Key Points:**
- One-time events (navigation, toasts, etc.)
- Consumed by UI
- Sent via Channel

## Theme Structure

### Colors (NEEDS IMPROVEMENT)
- Currently uses hardcoded `Color(0xFFXXXXXX)`
- ❌ NOT Material3 compliant
- ✅ We will use `MaterialTheme.colorScheme` instead

### Theme Setup
```kotlin
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = getPlatformColorScheme(isPlatformInDarkTheme()),
        typography = getPlatformTypography(),
        content = content
    )
}
```

**Key Points:**
- Platform-specific implementations (expect/actual)
- System dark mode detection
- Material3 theme

## Architecture Layers

```
UI Layer (Compose)
    ↓
ViewModel (androidx.lifecycle)
    ↓
Interactor (MVI Logic)
    ↓
UseCase (Domain - shared module)
    ↓
Repository (Data - shared module)
```

## What We'll Adopt for ASR Financial

### ✅ Keep
1. **MVI Pattern**: ViewModel → Interactor → State/Event/Effect
2. **Interactor Layer**: Separates UI logic from ViewModel
3. **Sealed Classes**: For Events and Effects
4. **StateFlow + Channel**: For state and effects
5. **Platform-specific Theme**: expect/actual pattern

### ❌ Change
1. **Hardcoded Colors**: Use Material3 ColorScheme
2. **Hardcoded Typography**: Use Material3 Typography
3. **Hardcoded Shapes**: Use Material3 Shapes

### ➕ Add
1. **Window Size Classes**: For responsive Android UI
2. **Portrait Lock**: Android + iOS configuration
3. **Mock JSON Data**: Instead of database
4. **Breadcrumb Navigation**: Adaptive UI component
5. **Sidebar/Drawer**: Responsive navigation

## Implementation Order for ASR Financial

1. Setup Material3 Theme (proper ColorScheme)
2. Create State/Event/Effect sealed classes
3. Implement Interactor pattern
4. Create ViewModels
5. Build responsive UI components
6. Add mock JSON data loading
7. Implement navigation

## Key Differences

| PreachTrack | ASR Financial |
|-------------|---------------|
| SQLDelight DB | Mock JSON files |
| Hardcoded colors | Material3 ColorScheme |
| Single screen size | Responsive (Window Size Classes) |
| Voyager navigation | Jetpack Navigation Compose |
| CocoaPods (iOS) | SPM + Direct framework |

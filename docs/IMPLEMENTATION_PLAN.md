# ASR Financial - Implementation Plan

## Overview

Implementation plan for ASR Financial KMP app following PreachTrack architecture patterns with Material3 design system, responsive Android UI, and portrait-only orientation.

## 1. Architecture Analysis (PreachTrack)

### Patterns to Adopt
- **ViewModel + Interactor** pattern (MVI)
- **State management** with sealed classes
- **UseCase + Repository** structure
- **Ktor** configuration for networking
- **Design system** organization

## 2. Project Architecture

### Shared Module (Domain + Data)

```
shared/
├── domain/
│   ├── usecases/          # Business logic
│   └── models/            # Domain entities
├── data/
│   ├── repository/        # Repository interfaces + implementations
│   └── datasource/        # Mock JSON data source
└── di/                    # Dependency injection
```

### ComposeApp Module (Presentation + UI)

```
composeApp/
├── presentation/
│   ├── viewmodel/         # ViewModels
│   ├── interactor/        # Interactors (MVI)
│   ├── state/             # UI States (sealed classes)
│   └── screens/           # Screen composables
├── theme/                 # Material3 theme (NO hardcoding)
├── navigation/            # Navigation logic
└── di/                    # Presentation DI
```

## 3. Material3 Design System

### Zero Hardcoding Rule

**Colors:**
```kotlin
// ✅ CORRECT
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.surface

// ❌ WRONG
Color(0xFF6200EE)
```

**Typography:**
```kotlin
// ✅ CORRECT
MaterialTheme.typography.headlineLarge
MaterialTheme.typography.bodyMedium

// ❌ WRONG
fontSize = 24.sp
```

**Shapes:**
```kotlin
// ✅ CORRECT
MaterialTheme.shapes.medium
MaterialTheme.shapes.large

// ❌ WRONG
RoundedCornerShape(16.dp)
```

### Dark Mode
- System-based: `isSystemInDarkTheme()`
- Dynamic color scheme
- Material3 automatic adaptation

## 4. Android Responsive Design (CRITICAL)

### Window Size Classes (Material3 2025)

```kotlin
enum class WindowSizeClass {
    Compact,    // < 600dp (phones)
    Medium,     // 600dp - 840dp (tablets, unfolded)
    Expanded    // > 840dp (large tablets, desktops)
}
```

### Breakpoints
- **Compact**: 0dp - 599dp (phone portrait)
- **Medium**: 600dp - 839dp (tablet, phone landscape)
- **Expanded**: 840dp+ (large tablet, desktop)

### Adaptive Layouts

**Navigation:**
- Compact: Bottom navigation / Navigation drawer
- Medium: Navigation rail
- Expanded: Permanent navigation drawer

**Content:**
- Compact: Single column
- Medium: Two columns
- Expanded: Multi-column with sidebar

### Implementation
```kotlin
@Composable
fun AdaptiveLayout(
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit
) {
    when (windowSizeClass) {
        Compact -> CompactLayout(content)
        Medium -> MediumLayout(content)
        Expanded -> ExpandedLayout(content)
    }
}
```

## 5. Portrait Mode Lock

### Android 16+

**AndroidManifest.xml:**
```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait"
    android:resizeableActivity="false"
    android:configChanges="orientation|screenSize">
</activity>
```

**MainActivity.kt:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}
```

### iOS 26+

**Info.plist:**
```xml
<key>UISupportedInterfaceOrientations</key>
<array>
    <string>UIInterfaceOrientationPortrait</string>
</array>
<key>UISupportedInterfaceOrientations~ipad</key>
<array>
    <string>UIInterfaceOrientationPortrait</string>
</array>
```

## 6. Concurrency (2025 Best Practices)

### Kotlin Coroutines

**Structured Concurrency:**
```kotlin
viewModelScope.launch {
    // Automatic cancellation on ViewModel clear
}
```

**Dispatchers:**
```kotlin
Dispatchers.Main.immediate  // UI updates
Dispatchers.IO              // Network/File operations
Dispatchers.Default         // CPU-intensive work
```

**Flow with StateIn:**
```kotlin
val uiState: StateFlow<UiState> = flow {
    // emit states
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = UiState.Loading
)
```

**NO Blocking:**
```kotlin
// ✅ CORRECT
suspend fun loadData() = withContext(Dispatchers.IO) {
    // network call
}

// ❌ WRONG
fun loadData() {
    runBlocking { } // NEVER block UI thread
}
```

### Swift Concurrency (iOS)

**Async/Await:**
```swift
@MainActor
class ViewModel: ObservableObject {
    @Published var state: State = .loading
    
    func loadData() async {
        // Automatically on main actor
        state = await repository.fetchData()
    }
}
```

**Actors (Swift 6):**
```swift
actor DataCache {
    private var cache: [String: Data] = [:]
    
    func get(_ key: String) -> Data? {
        cache[key]
    }
}
```

## 7. Mock Data Strategy

### JSON Files Location
```
composeApp/src/commonMain/composeResources/files/
├── mock_transactions.json
├── mock_congregations.json
└── mock_reports.json
```

### Repository Implementation
```kotlin
class MockFinancialRepository : FinancialRepository {
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun getTransactions(): List<Transaction> {
        val jsonString = loadJsonFromResources("mock_transactions.json")
        return json.decodeFromString(jsonString)
    }
}
```

### No Database/DataStore
- All data from JSON files
- In-memory state management
- Repository pattern for abstraction

## 8. UI Components

### Breadcrumb Navigation
- Adaptive: Horizontal scroll on compact, full width on expanded
- Material3 chips or text buttons
- Current location highlighted

### Sidebar/Navigation Drawer
- **Compact**: Modal drawer (swipe from edge)
- **Medium**: Navigation rail (always visible)
- **Expanded**: Permanent drawer (always visible)

### Home Screen
- **Compact**: Single column cards
- **Medium**: Two column grid
- **Expanded**: Three column grid with sidebar

### Responsive Cards
```kotlin
@Composable
fun AdaptiveCard(windowSize: WindowSizeClass) {
    Card(
        modifier = Modifier.fillMaxWidth(
            when (windowSize) {
                Compact -> 1f
                Medium -> 0.48f
                Expanded -> 0.32f
            }
        )
    ) {
        // Content
    }
}
```

## 9. State Management (MVI)

### State
```kotlin
sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val data: List<Transaction>) : HomeState
    data class Error(val message: String) : HomeState
}
```

### Event
```kotlin
sealed interface HomeEvent {
    data object LoadData : HomeEvent
    data class FilterByDate(val date: LocalDate) : HomeEvent
    data class NavigateToDetails(val id: String) : HomeEvent
}
```

### Effect
```kotlin
sealed interface HomeEffect {
    data class ShowToast(val message: String) : HomeEffect
    data class NavigateTo(val route: String) : HomeEffect
}
```

### Interactor
```kotlin
class HomeInteractor(
    private val useCase: GetTransactionsUseCase
) {
    fun handleEvent(event: HomeEvent): Flow<HomeState> = flow {
        when (event) {
            is LoadData -> {
                emit(HomeState.Loading)
                val result = useCase()
                emit(HomeState.Success(result))
            }
        }
    }
}
```

## 10. Implementation Order

1. ✅ Setup Material3 theme (colors, typography, shapes)
2. ✅ Configure portrait mode (Android + iOS)
3. ✅ Create mock JSON files
4. ✅ Implement Repository with mock data
5. ✅ Create UseCases in shared module
6. ✅ Setup MVI (State/Event/Effect)
7. ✅ Implement Interactor + ViewModel
8. ✅ Create responsive layouts (Window Size Classes)
9. ✅ Build Sidebar/Navigation
10. ✅ Build Home Screen with adaptive UI
11. ✅ Add Breadcrumb navigation
12. ✅ Test on Android (phone/tablet) + iOS

## 11. Testing Strategy

### Android
- Phone (Compact): Pixel 8
- Tablet (Medium): Pixel Tablet
- Foldable (Expanded): Pixel Fold

### iOS
- iPhone (Compact): iPhone 15 Pro
- iPad (Expanded): iPad Pro

### Orientation
- Verify portrait lock on all devices
- Test rotation attempts

## 12. Performance Guidelines

### Compose
- Use `remember` for expensive calculations
- `derivedStateOf` for computed values
- `LazyColumn` for lists
- Avoid recomposition with `key()`

### Coroutines
- Cancel jobs properly
- Use `Flow` instead of `LiveData`
- Structured concurrency always

### Memory
- No memory leaks in ViewModels
- Proper lifecycle awareness
- Clean up resources

## References

- [Material3 Design](https://m3.material.io/)
- [Window Size Classes](https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- [Swift Concurrency](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/concurrency/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

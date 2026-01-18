# Login Feature Implementation Plan

## Feature Overview

**Goal**: Implement Firebase Authentication login functionality that:
- Allows users to log in with email/password
- Persists login session until token expires
- Blocks access to app until authenticated
- Works on both Android and iOS

**Requirements**:
- ✅ Login only (no forgot password, no create account)
- ✅ Firebase Auth for both platforms
- ✅ Session persistence (handled by Firebase)
- ✅ Token expiry checking
- ✅ Navigation guard (redirect to login if not authenticated)

---

## Implementation Plan

### Phase 1: Firebase Setup & Configuration

#### 1.1 Add Firebase Dependencies

**Files to Modify**:
- `gradle/libs.versions.toml` - Add Firebase versions
- `shared/build.gradle.kts` - Add Firebase Auth dependencies
- `androidApp/build.gradle.kts` - Add Firebase Android plugin
- `iosApp/iosApp.xcodeproj/project.pbxproj` - Add Firebase via Swift Package Manager (SPM)

**Dependencies Needed**:
- Firebase Auth (Android & iOS)
- Firebase Core (Android & iOS)
- Google Services plugin (Android)

**Actions**:
- [ ] Add Firebase version to `libs.versions.toml`
- [ ] Add Firebase Auth library to shared module (Android)
- [ ] Add Google Services plugin to Android
- [ ] Add Firebase iOS SDK via Swift Package Manager in Xcode:
  - Open `iosApp/iosApp.xcodeproj` in Xcode
  - Go to **File → Add Package Dependencies...**
  - Enter URL: `https://github.com/firebase/firebase-ios-sdk`
  - Select version: **Up to Next Major Version** (e.g., `10.25.0`)
  - Select products: `FirebaseAuth`, `FirebaseCore`
  - Click **Add Package**
  - Xcode will automatically update `project.pbxproj` with package references

#### 1.2 Firebase Project Configuration

**Files to Add**:
- `androidApp/google-services.json` - Firebase config for Android
- `iosApp/iosApp/GoogleService-Info.plist` - Firebase config for iOS

**Actions**:
- [ ] Create Firebase project (if not exists)
- [ ] Enable Email/Password authentication in Firebase Console
- [ ] Download `google-services.json` for Android
  - Place in: `androidApp/` directory
- [ ] Download `GoogleService-Info.plist` for iOS
  - Place in: `iosApp/iosApp/` directory
  - Add to Xcode project (drag & drop, ensure "Copy items if needed" is checked)
- [ ] Both files are already in .gitignore

#### 1.3 Firebase Initialization

**Platform-Specific Setup**:
- Android: Initialize in `MainActivity` or Application class
- iOS: Initialize in `iOSApp.swift` (SwiftUI App)

**Actions**:
- [ ] Initialize Firebase in Android app entry point
  - Add `Firebase.initialize(context)` in `MainActivity` or Application class
- [ ] Initialize Firebase in iOS app entry point
  - Add `FirebaseApp.configure()` in `iOSApp.swift` init
  - Import `FirebaseCore` in `iOSApp.swift`
  - Example:
    ```swift
    import FirebaseCore
    
    @main
    struct iOSApp: App {
        init() {
            FirebaseApp.configure()
        }
        // ... rest of code
    }
    ```

**Note**: Firebase iOS SDK will be automatically linked when you build. No manual linking required with SPM.

---

### Phase 2: Platform Abstraction Layer

#### 2.1 Create FirebaseAuth Abstraction

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/platform/FirebaseAuth.kt`

**Interface**:
```kotlin
expect class FirebaseAuth {
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signOut()
    suspend fun getCurrentUser(): User?
    suspend fun isUserSignedIn(): Boolean
    suspend fun getAuthToken(): String?
    suspend fun refreshToken(): String?
    /**
     * Check if session is still valid (for app launch verification)
     * Firebase automatically persists sessions, but we verify on launch
     */
    suspend fun verifySession(): Boolean
}

data class AuthResult(
    val success: Boolean,
    val user: User? = null,
    val errorMessage: String? = null
)

data class User(
    val uid: String,
    val email: String?,
    val displayName: String? = null
)
```

**Actions**:
- [ ] Create expect class in `shared/src/commonMain/`
- [ ] Implement Android version in `shared/src/androidMain/`
- [ ] Implement iOS version in `shared/src/iosMain/`

#### 2.2 Android Implementation

**Location**: `shared/src/androidMain/kotlin/com/asr/financial/platform/FirebaseAuth.android.kt`

**Implementation Details**:
- Use Firebase Auth Android SDK
- Handle FirebaseAuthException
- Convert Firebase User to domain User model
- Handle token refresh

**Actions**:
- [ ] Implement FirebaseAuth for Android
- [ ] Handle authentication errors
- [ ] Implement token management

#### 2.3 iOS Implementation (SPM Integration)

**Location**: `shared/src/iosMain/kotlin/com/asr/financial/platform/FirebaseAuth.ios.kt`

**⚠️ Important: KMP + SPM Integration Challenge**

**The Issue**: When Firebase is added via SPM in Xcode, it's linked to the iOS app target. Kotlin/Native code in the shared module cannot directly access SPM packages without a bridge.

**The Solution**: Create a Swift/Objective-C bridge wrapper that Kotlin/Native can call via cinterop (similar to how `SecureStorage.ios.kt` uses iOS Security framework).

**Files to Create**:
1. **Swift Bridge** (`iosApp/iosApp/FirebaseAuthBridge.swift`):
   ```swift
   import Foundation
   import FirebaseAuth
   
   @objc public class FirebaseAuthBridge: NSObject {
       @objc public static func signIn(email: String, password: String, completion: @escaping (String?, String?) -> Void) {
           // Firebase Auth implementation
       }
       // ... other methods
   }
   ```

2. **Objective-C Header** (`iosApp/iosApp/FirebaseAuthBridge.h`):
   - Expose Swift bridge to Objective-C
   - Needed for cinterop

3. **Kotlin Implementation** (`shared/src/iosMain/kotlin/com/asr/financial/platform/FirebaseAuth.ios.kt`):
   - Use cinterop to call the bridge
   - Pattern similar to `SecureStorage.ios.kt`

**Implementation Details**:
- Create Swift/Objective-C bridge wrapper
- Use cinterop to access bridge from Kotlin
- Handle NSError conversion
- Convert Firebase User to domain User model
- Handle token refresh

**Actions**:
- [ ] Create `FirebaseAuthBridge.swift` with `@objc` annotations
- [ ] Create `FirebaseAuthBridge.h` Objective-C header
- [ ] Implement Kotlin/Native FirebaseAuth using cinterop
- [ ] Handle authentication errors (NSError → Kotlin exceptions)
- [ ] Implement token management

#### 2.4 Add to PlatformModule

**Location**: `shared/src/androidMain/kotlin/com/asr/financial/di/PlatformModule.android.kt`
**Location**: `shared/src/iosMain/kotlin/com/asr/financial/di/PlatformModule.ios.kt`

**Actions**:
- [ ] Add FirebaseAuth to Android PlatformModule
- [ ] Add FirebaseAuth to iOS PlatformModule

---

### Phase 3: Domain Layer

#### 3.1 Create Auth Domain Model

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/domain/models/AuthUser.kt`

**Model**:
```kotlin
data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val tokenExpiryTime: Long? = null // Timestamp when token expires
)
```

**Actions**:
- [ ] Create AuthUser domain model
- [ ] Add helper functions if needed (e.g., `isTokenExpired()`)

#### 3.2 Create Auth Repository Interface

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/domain/repository/AuthRepository.kt`

**Interface**:
```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun logout()
    suspend fun getCurrentUser(): AuthUser?
    suspend fun isAuthenticated(): Boolean
    suspend fun refreshAuthToken(): Result<String>
}
```

**Actions**:
- [ ] Create AuthRepository interface
- [ ] Define all required methods
- [ ] Use Result type for error handling

#### 3.3 Create Auth Use Cases

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/domain/usecase/AuthUseCases.kt`

**Use Cases**:
- `LoginUseCase` - Handle login with email/password
- `LogoutUseCase` - Handle logout
- `CheckAuthStatusUseCase` - Check if user is authenticated
- `GetCurrentUserUseCase` - Get current authenticated user
- `RefreshTokenUseCase` - Refresh auth token if expired

**Actions**:
- [ ] Create LoginUseCase
- [ ] Create LogoutUseCase
- [ ] Create CheckAuthStatusUseCase
- [ ] Create GetCurrentUserUseCase
- [ ] Create RefreshTokenUseCase (optional, for token refresh)

---

### Phase 4: Data Layer

#### 4.1 Implement Auth Repository

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/data/repository/AuthRepositoryImpl.kt`

**Implementation**:
- Use FirebaseAuth platform abstraction
- Convert platform User to domain AuthUser
- Handle token expiry checking
- Implement error handling

**Actions**:
- [ ] Implement AuthRepositoryImpl
- [ ] Map FirebaseAuth results to domain models
- [ ] Handle token expiry logic
- [ ] Implement error handling

#### 4.2 Add to DataModule

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/di/DataModule.kt`

**Actions**:
- [ ] Add AuthRepository to DataModule

#### 4.3 Add to DomainModule

**Location**: `shared/src/commonMain/kotlin/com/asr/financial/di/DomainModule.kt`

**Actions**:
- [ ] Add all Auth use cases to DomainModule

---

### Phase 5: Presentation Layer - MVI

#### 5.1 Create Login State

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/state/LoginState.kt`

**State**:
```kotlin
sealed interface LoginState {
    data object Loading : LoginState
    data class Ready(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : LoginState
    data class Error(val message: String) : LoginState
}
```

**Actions**:
- [ ] Create LoginState sealed interface
- [ ] Follow existing state patterns

#### 5.2 Create Login Events

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/event/LoginEvent.kt`

**Events**:
```kotlin
sealed interface LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent
    data class PasswordChanged(val password: String) : LoginEvent
    data object Login : LoginEvent
    data object ClearError : LoginEvent
}
```

**Actions**:
- [ ] Create LoginEvent sealed interface
- [ ] Define all user actions

#### 5.3 Create Login Effects

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/effect/LoginEffect.kt`

**Effects**:
```kotlin
sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data class ShowToast(val message: String) : LoginEffect
}
```

**Actions**:
- [ ] Create LoginEffect sealed interface
- [ ] Define navigation and toast effects

#### 5.4 Create Login Interactor

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/interactor/LoginInteractor.kt`

**Implementation**:
- Handle login logic
- Validate email/password
- Call LoginUseCase
- Emit states and effects
- Handle errors

**Actions**:
- [ ] Create LoginInteractor
- [ ] Implement login validation
- [ ] Handle authentication flow
- [ ] Emit appropriate states and effects

#### 5.5 Create Login ViewModel

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/viewmodel/LoginViewModel.kt`

**Implementation**:
- Thin wrapper around LoginInteractor
- Expose uiState and uiEffect

**Actions**:
- [ ] Create LoginViewModel
- [ ] Follow existing ViewModel pattern

#### 5.6 Add to PresentationModule

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/di/PresentationModule.kt`

**Actions**:
- [ ] Add LoginInteractor to PresentationModule
- [ ] Add LoginViewModel to PresentationModule

---

### Phase 6: Presentation Layer - Screen

#### 6.1 Create Login Screen Directory

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/login/`

**Structure**:
```
login/
├── LoginScreen.kt
├── LoginConstants.kt
└── components/ (if needed)
```

**Actions**:
- [ ] Create login directory
- [ ] Create LoginScreen.kt
- [ ] Create LoginConstants.kt

#### 6.2 Create Login Screen

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/login/LoginScreen.kt`

**UI Components**:
- Email TextField
- Password TextField (with visibility toggle)
- Login Button
- Error message display
- Loading indicator

**Design**:
- Centered layout
- Material3 design
- Brand colors/logo
- Error handling UI

**Actions**:
- [ ] Create LoginScreen composable
- [ ] Implement email/password inputs
- [ ] Add login button
- [ ] Add error display
- [ ] Add loading state
- [ ] Use stringResource for all text

#### 6.3 Create Login Constants

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/login/LoginConstants.kt`

**Constants**:
- Input field widths
- Button dimensions
- Spacing values

**Actions**:
- [ ] Create LoginConstants.kt
- [ ] Add UI dimension constants

#### 6.4 Add String Resources

**Location**: `composeApp/src/commonMain/composeResources/values/strings.xml` (Romanian)
**Location**: `composeApp/src/commonMain/composeResources/values-hu/strings.xml` (Hungarian)

**Strings Needed**:
- Login screen title
- Email label/placeholder
- Password label/placeholder
- Login button text
- Error messages (invalid email, wrong password, network error, etc.)

**Actions**:
- [ ] Add all login strings to Romanian file
- [ ] Add all login strings to Hungarian file

---

### Phase 7: Navigation & Auth Guard

#### 7.1 Add Login Route

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/navigation/Routes.kt`

**Actions**:
- [ ] Add `LOGIN = "login"` to Routes

#### 7.2 Update App.kt - Auth Check

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/App.kt`

**Flow**:
1. Show splash screen
2. Check authentication status
3. If authenticated → NavGraph (home)
4. If not authenticated → LoginScreen

**Implementation**:
```kotlin
@Composable
fun App() {
    var showSplash by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf<Boolean?>(null) }
    
    val checkAuthStatusUseCase: CheckAuthStatusUseCase = koinInject()
    
    LaunchedEffect(showSplash) {
        if (!showSplash) {
            isAuthenticated = checkAuthStatusUseCase()
        }
    }
    
    AppTheme {
        when {
            showSplash -> {
                SplashScreen(onSplashFinished = { showSplash = false })
            }
            isAuthenticated == null -> {
                // Still checking
                SplashScreen(onSplashFinished = {})
            }
            isAuthenticated == true -> {
                // Authenticated - show app
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
            else -> {
                // Not authenticated - show login
                LoginScreen(
                    onLoginSuccess = {
                        isAuthenticated = true
                    }
                )
            }
        }
    }
}
```

**Actions**:
- [ ] Update App.kt to check auth status
- [ ] Show LoginScreen if not authenticated
- [ ] Show NavGraph if authenticated
- [ ] Handle login success callback

#### 7.3 Update NavGraph - Add Login Route

**Location**: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/navigation/NavGraph.kt`

**Actions**:
- [ ] Add login route to NavGraph (if needed for navigation from login)

#### 7.4 Add Logout Functionality

**Location**: Add logout to navigation menu or settings

**Actions**:
- [ ] Add logout option to navigation drawer/menu
- [ ] Implement logout flow
- [ ] Navigate back to login after logout

---

### Phase 8: Token Expiry Handling

#### 8.1 Token Expiry Check

**Implementation**:
- Check token expiry on app launch
- Check token expiry before API calls (if needed)
- Refresh token if expired
- Logout if refresh fails

**Actions**:
- [ ] Implement token expiry checking in CheckAuthStatusUseCase
- [ ] Add token refresh logic
- [ ] Handle expired token scenarios

#### 8.2 Session Persistence

**Note**: Firebase Auth handles session persistence automatically:
- Android: Uses SharedPreferences internally
- iOS: Uses Keychain internally

**Actions**:
- [ ] Verify Firebase handles persistence (it does automatically)
- [ ] Test session persistence across app restarts

---

## File Structure Summary

### New Files to Create

**Platform Abstraction**:
- `shared/src/commonMain/kotlin/com/asr/financial/platform/FirebaseAuth.kt` (expect)
- `shared/src/androidMain/kotlin/com/asr/financial/platform/FirebaseAuth.android.kt`
- `shared/src/iosMain/kotlin/com/asr/financial/platform/FirebaseAuth.ios.kt`

**Domain Layer**:
- `shared/src/commonMain/kotlin/com/asr/financial/domain/models/AuthUser.kt`
- `shared/src/commonMain/kotlin/com/asr/financial/domain/repository/AuthRepository.kt`
- `shared/src/commonMain/kotlin/com/asr/financial/domain/usecase/AuthUseCases.kt`

**Data Layer**:
- `shared/src/commonMain/kotlin/com/asr/financial/data/repository/AuthRepositoryImpl.kt`

**Presentation Layer - MVI**:
- `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/state/LoginState.kt`
- `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/event/LoginEvent.kt`
- `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/effect/LoginEffect.kt`
- `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/interactor/LoginInteractor.kt`
- `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/mvi/viewmodel/LoginViewModel.kt`

**Presentation Layer - Screen**:
- `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/login/LoginScreen.kt`
- `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/login/LoginConstants.kt`

### Files to Modify

**Dependencies**:
- `gradle/libs.versions.toml` - Add Firebase versions
- `shared/build.gradle.kts` - Add Firebase Auth dependency (Android)
- `androidApp/build.gradle.kts` - Add Google Services plugin
- `iosApp/iosApp.xcodeproj/project.pbxproj` - Add Firebase iOS SDK via SPM (done in Xcode)

**DI Modules**:
- `shared/src/androidMain/kotlin/com/asr/financial/di/PlatformModule.android.kt` - Add FirebaseAuth
- `shared/src/iosMain/kotlin/com/asr/financial/di/PlatformModule.ios.kt` - Add FirebaseAuth
- `shared/src/commonMain/kotlin/com/asr/financial/di/DataModule.kt` - Add AuthRepository
- `shared/src/commonMain/kotlin/com/asr/financial/di/DomainModule.kt` - Add Auth use cases
- `composeApp/src/commonMain/kotlin/com/asr/financial/di/PresentationModule.kt` - Add LoginInteractor & ViewModel

**Navigation**:
- `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/navigation/Routes.kt` - Add LOGIN route
- `composeApp/src/commonMain/kotlin/com/asr/financial/App.kt` - Add auth check logic

**Resources**:
- `composeApp/src/commonMain/composeResources/values/strings.xml` - Add login strings (Romanian)
- `composeApp/src/commonMain/composeResources/values-hu/strings.xml` - Add login strings (Hungarian)

**Platform Initialization**:
- `androidApp/src/main/kotlin/com/asr/financial/android/MainActivity.kt` - Initialize Firebase
- `iosApp/iosApp/iOSApp.swift` - Initialize Firebase (add `FirebaseApp.configure()`)

---

## Implementation Order

### Recommended Sequence

1. **Phase 1**: Firebase Setup & Configuration
   - Add dependencies
   - Configure Firebase project
   - Initialize Firebase

2. **Phase 2**: Platform Abstraction Layer
   - Create FirebaseAuth expect/actual
   - Implement Android version
   - Implement iOS version

3. **Phase 3**: Domain Layer
   - Create domain models
   - Create repository interface
   - Create use cases

4. **Phase 4**: Data Layer
   - Implement repository
   - Configure DI

5. **Phase 5**: Presentation Layer - MVI
   - Create State, Event, Effect
   - Create Interactor
   - Create ViewModel

6. **Phase 6**: Presentation Layer - Screen
   - Create LoginScreen
   - Add string resources

7. **Phase 7**: Navigation & Auth Guard
   - Update App.kt
   - Add navigation routes
   - Implement auth check

8. **Phase 8**: Token Expiry Handling
   - Implement expiry checking
   - Test session persistence

---

## Key Considerations

### 1. Firebase Auth Token Management & Session Persistence

**Short Answer**: Firebase handles 2-3 month sessions automatically, but you should verify on app launch.

**How Firebase Sessions Work**:
- **ID Token**: Short-lived JWT (expires after **1 hour**)
- **Refresh Token**: Long-lived credential (can last **months** until revoked)
- **Automatic Persistence**: Firebase SDK stores refresh token securely (Keychain on iOS, KeyStore on Android)
- **Automatic Refresh**: SDK automatically refreshes ID tokens when needed

**For 2-3 Month Persistence**:
- ✅ **Firebase handles it automatically** - Refresh tokens persist until revoked
- ✅ **No additional SecureStorage needed** - Firebase uses platform secure storage
- ⚠️ **Verify on app launch** - Check `currentUser` is not null
- ⚠️ **Handle token revocation** - If user changes password/email, tokens are revoked

**What You Need to Do**:
1. **On App Launch**: Check `FirebaseAuth.isUserSignedIn()` or `getCurrentUser()`
2. **Token Refresh**: Firebase handles automatically, but you can call `refreshToken()` if needed
3. **Handle Revocation**: If `currentUser` is null unexpectedly, redirect to login

**When Sessions End**:
- User explicitly signs out
- User changes password/email (Firebase revokes tokens)
- Admin disables account
- Token is manually revoked

**Conclusion**: Firebase's built-in persistence is sufficient for 2-3 months. You don't need to store tokens in `SecureStorage` separately, but you should verify the session on app launch.

### 2. Error Handling
- Invalid email format
- Wrong password
- Network errors
- User not found
- Too many attempts
- Account disabled

### 3. Security
- Password field should be secure (password input type)
- Don't log sensitive information
- Use secure storage (Firebase handles this)

### 4. User Experience
- Show loading state during login
- Clear error messages
- Auto-focus next field
- Handle keyboard navigation
- Show success feedback

### 5. Testing
- Test successful login
- Test invalid credentials
- Test network errors
- Test token expiry
- Test session persistence
- Test logout

---

## Dependencies to Add

### Firebase Versions (to add to libs.versions.toml)
```toml
firebase-auth = "23.0.0"  # Check latest version
firebase-core = "21.0.0"  # Check latest version
google-services = "4.4.0" # Android plugin
```

### Android Dependencies (Gradle)
- Firebase Auth library (via Gradle)
- Firebase Core library (via Gradle)
- Google Services plugin (Android only)

### iOS Dependencies (Swift Package Manager)

**Why SPM?**
- ✅ Your project already uses SPM (you have `Package.swift`)
- ✅ Firebase officially supports SPM (since version 8.6.0)
- ✅ No external tools needed (integrated in Xcode)
- ✅ Simpler setup (no Podfile, no Pods directory)
- ✅ Faster builds

**Setup Steps**:
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Go to **File → Add Package Dependencies...**
3. Enter URL: `https://github.com/firebase/firebase-ios-sdk`
4. Select version: **Up to Next Major Version** (e.g., `10.25.0`)
5. Select products:
   - ✅ `FirebaseAuth`
   - ✅ `FirebaseCore`
6. Click **Add Package**
7. Xcode will automatically update `project.pbxproj` with package references

**Important Notes**:
- SPM dependencies are managed in Xcode, not in `Package.swift`
- The `Package.swift` file in your project is for creating a Swift package, not for consuming dependencies
- Firebase iOS SDK will be automatically linked when you build
- Minimum iOS version: iOS 15+ (Firebase requirement)
- Check your deployment target in Xcode project settings

**Verifying Setup**:
- Check **Project Navigator** → Your project → **Package Dependencies**
- Should see `firebase-ios-sdk` listed

**Troubleshooting**:
- **"No such module 'FirebaseAuth'"**: Clean build folder (Shift+Cmd+K) and rebuild
- **Package not resolving**: Check internet, try **File → Packages → Reset Package Caches**
- **GoogleService-Info.plist not found**: Verify file is in `iosApp/iosApp/` and added to target

**Updating Dependencies**:
- In Xcode: **File → Packages → Update to Latest Package Versions**
- Or right-click package → **Update Package**

---

## ⚠️ Important: KMP + SPM Integration

### Can KMP Access SPM Packages?

**Short Answer**: Yes, but with a bridge layer.

**Details**:
- ✅ **KMP can export to SPM**: Your project already does this (XCFrameworks)
- ✅ **KMP can consume SPM packages**: BUT requires a bridge wrapper
- ⚠️ **SPM packages in Xcode**: When you add Firebase via SPM, it's linked to the iOS app target
- ⚠️ **Kotlin/Native access**: Cannot directly import SPM packages from Kotlin code

### Solution: Swift/Objective-C Bridge

**Why a Bridge?**
- SPM packages are available to Swift/Objective-C code in the iOS app
- Kotlin/Native uses cinterop to access Objective-C APIs
- Firebase iOS SDK is Objective-C compatible, but we need a bridge to expose it

**How It Works**:
1. **Swift Bridge** (`FirebaseAuthBridge.swift`):
   - Wraps Firebase Auth calls
   - Uses `@objc` to expose to Objective-C
   - Can be called from Kotlin/Native via cinterop

2. **Kotlin/Native** (`FirebaseAuth.ios.kt`):
   - Uses cinterop (like `SecureStorage.ios.kt`)
   - Calls the Swift bridge
   - Converts Objective-C types to Kotlin types

**Pattern**: Similar to how your project already uses iOS frameworks:
- `SecureStorage.ios.kt` uses `platform.Security.*` (via cinterop)
- `FirebaseAuth.ios.kt` will use `FirebaseAuthBridge` (via cinterop)

**Alternative**: Direct cinterop with Firebase (more complex, requires cinterop configuration in `build.gradle.kts`)

**Conclusion**: SPM works with KMP, but you need a Swift/Objective-C bridge wrapper. This is a standard pattern and not a limitation.

---

## Testing Checklist

- [ ] Login with valid credentials works
- [ ] Login with invalid email shows error
- [ ] Login with wrong password shows error
- [ ] Network error handling works
- [ ] Token expiry checking works
- [ ] Session persists across app restarts
- [ ] Logout works correctly
- [ ] Navigation guard works (blocks access when not authenticated)
- [ ] Multi-language strings work
- [ ] UI is responsive (Compact, Medium, Expanded)
- [ ] Loading states work
- [ ] Error states work

---

## Next Steps

Once you approve this plan, I will:
1. Start with Phase 1 (Firebase setup)
2. Implement each phase step by step
3. Test as we go
4. Follow all existing patterns and conventions

**Ready to proceed?** Let me know if you want any changes to this plan before I start implementing.

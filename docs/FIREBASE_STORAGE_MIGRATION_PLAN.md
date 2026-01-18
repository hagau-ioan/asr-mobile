# Firebase Cloud Storage Migration Plan

## Overview

This document outlines the changes needed to migrate from local JSON files (testing mode) to Firebase Cloud Storage (production mode) while maintaining the ability to use JSON files for testing.

## Architecture Changes Required

### 1. Platform Abstraction Layer

**New File**: `shared/src/commonMain/kotlin/com/asr/financial/platform/FirebaseStorage.kt`
- Expect interface for Firebase Storage operations
- Methods: `downloadFileAsString(path: String): String?`
- Similar pattern to `FirebaseAuth` and `ResourceLoader`

**Android Implementation**: `shared/src/androidMain/kotlin/com/asr/financial/platform/FirebaseStorage.android.kt`
- Uses Firebase Storage Android SDK
- Requires `kotlinx-coroutines-play-services` for Task.await()

**iOS Implementation**: `shared/src/iosMain/kotlin/com/asr/financial/platform/FirebaseStorage.ios.kt`
- Uses Firebase Storage iOS SDK via Swift bridge
- Similar pattern to `FirebaseAuthBridge`

### 2. Firebase Storage Data Sources

Create new data source implementations that use Firebase Storage instead of ResourceLoader:

**New Files**:
- `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/FirebaseStorageTransactionDataSource.kt`
- `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/FirebaseStorageCongregationDataSource.kt`
- `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/FirebaseStorageAppConfigDataSource.kt`
- `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/FirebaseStorageAsrExpenseDataSource.kt`

Each will:
- Implement the same `*DataSource` interface
- Use `FirebaseStorage` instead of `ResourceLoader`
- Load files from Firebase Storage paths (e.g., `app/transactions.json`)

### 3. Configuration Mechanism

**Option A: Build Configuration Flag (Recommended)**
- Add a build config property to switch between modes
- Use Gradle build variants or a config file

**Option B: Runtime Configuration**
- Use a config file or environment variable
- Check at app startup

**Implementation**: Create `AppConfig.kt` or use `gradle.properties` to define:
```kotlin
object AppConfig {
    const val USE_FIREBASE_STORAGE = true // false for testing
}
```

### 4. Dependency Injection Updates

**File**: `shared/src/commonMain/kotlin/com/asr/financial/di/DataModule.kt`

Update to conditionally provide data sources:
```kotlin
val dataModule = module {
    if (AppConfig.USE_FIREBASE_STORAGE) {
        // Production: Firebase Storage
        single<TransactionDataSource> { FirebaseStorageTransactionDataSource(get()) }
        single<CongregationDataSource> { FirebaseStorageCongregationDataSource(get()) }
        single<AppConfigDataSource> { FirebaseStorageAppConfigDataSource(get(), get()) }
        single<AsrExpenseDataSource> { FirebaseStorageAsrExpenseDataSource(get()) }
    } else {
        // Testing: Local JSON files
        single<TransactionDataSource> { JsonTransactionDataSource(get()) }
        single<CongregationDataSource> { JsonCongregationDataSource(get()) }
        single<AppConfigDataSource> { JsonAppConfigDataSource(get(), get()) }
        single<AsrExpenseDataSource> { JsonAsrExpenseDataSource(get()) }
    }
    
    // Repositories remain unchanged (they use interfaces)
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    // ... etc
}
```

### 5. Dependencies Required

**File**: `gradle/libs.versions.toml`
```toml
[versions]
firebase-storage = "21.0.0"
kotlinx-coroutines-play-services = "1.9.0"

[libraries]
firebase-storage = { module = "com.google.firebase:firebase-storage-ktx", version.ref = "firebase-storage" }
kotlinx-coroutines-play-services = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services", version.ref = "kotlinx-coroutines-play-services" }
```

**File**: `shared/build.gradle.kts`
```kotlin
androidMain.dependencies {
    // ... existing
    implementation(libs.firebase.storage)
    implementation(libs.kotlinx.coroutines.play.services)
}
```

**iOS**: Add Firebase Storage via Swift Package Manager in Xcode (same as Firebase Auth)

### 6. Firebase Storage File Structure

Files should be uploaded to Firebase Storage with this structure:
```
gs://your-bucket/app/
  ├── app_config.json
  ├── congregations.json
  ├── asr_expenses_transactions.json
  ├── cg_donate_transactions.json
  └── asr_expenses_last_12_months.json
```

### 7. Security Rules

Firebase Storage security rules should allow authenticated users to read:
```javascript
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /app/{fileName} {
         allow read: if request.auth != null;
         allow write: if false; // Only admins can write via console
       }
     }
   }
```

## Implementation Steps

### Step 1: Add Dependencies
1. Update `libs.versions.toml` with Firebase Storage versions
2. Add Firebase Storage to `shared/build.gradle.kts` (Android)
3. Add Firebase Storage to Xcode project (iOS) via SPM

### Step 2: Create Platform Abstraction
1. Create `FirebaseStorage.kt` (expect)
2. Create `FirebaseStorage.android.kt` (actual)
3. Create `FirebaseStorage.ios.kt` (actual)
4. Create `FirebaseStorageBridge.swift` and `.h` (iOS)
5. Add to `PlatformModule` (Android & iOS)

### Step 3: Create Firebase Storage Data Sources
1. Create `FirebaseStorageTransactionDataSource.kt`
2. Create `FirebaseStorageCongregationDataSource.kt`
3. Create `FirebaseStorageAppConfigDataSource.kt`
4. Create `FirebaseStorageAsrExpenseDataSource.kt`

### Step 4: Add Configuration
1. Create `AppConfig.kt` with `USE_FIREBASE_STORAGE` flag
2. Update `DataModule.kt` to conditionally provide data sources

### Step 5: Update iOS Bridge
1. Add Firebase Storage initialization to `iOSApp.swift`
2. Ensure Firebase Storage is initialized before use

### Step 6: Testing
1. Test with `USE_FIREBASE_STORAGE = false` (JSON files)
2. Test with `USE_FIREBASE_STORAGE = true` (Firebase Storage)
3. Verify all data sources work correctly

## Files to Create/Modify

### New Files:
1. `shared/src/commonMain/kotlin/com/asr/financial/platform/FirebaseStorage.kt`
2. `shared/src/androidMain/kotlin/com/asr/financial/platform/FirebaseStorage.android.kt`
3. `shared/src/iosMain/kotlin/com/asr/financial/platform/FirebaseStorage.ios.kt`
4. `iosApp/iosApp/FirebaseStorageBridge.swift`
5. `iosApp/iosApp/FirebaseStorageBridge.h`
6. `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/FirebaseStorageTransactionDataSource.kt`
7. `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/FirebaseStorageCongregationDataSource.kt`
8. `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/FirebaseStorageAppConfigDataSource.kt`
9. `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/FirebaseStorageAsrExpenseDataSource.kt`
10. `composeApp/src/commonMain/kotlin/com/asr/financial/AppConfig.kt` (if doesn't exist)

### Modified Files:
1. `gradle/libs.versions.toml` - Add Firebase Storage dependencies
2. `shared/build.gradle.kts` - Add Firebase Storage dependencies
3. `shared/src/commonMain/kotlin/com/asr/financial/di/DataModule.kt` - Conditional data source injection
4. `shared/src/androidMain/kotlin/com/asr/financial/di/PlatformModule.android.kt` - Add FirebaseStorage
5. `shared/src/iosMain/kotlin/com/asr/financial/di/PlatformModule.ios.kt` - Add FirebaseStorage
6. `iosApp/iosApp/iOSApp.swift` - Initialize Firebase Storage bridge
7. `iosApp/iosApp.xcodeproj/project.pbxproj` - Add Firebase Storage SPM dependency

## Benefits

1. **Clean Architecture Maintained**: Data sources remain abstracted, repositories unchanged
2. **Easy Testing**: Switch between JSON and Firebase Storage with one flag
3. **Platform Abstraction**: Consistent pattern with existing Firebase Auth implementation
4. **No Breaking Changes**: Existing JSON data sources remain functional
5. **Future-Proof**: Easy to add caching, offline support, etc.

## Considerations

1. **Authentication Required**: Firebase Storage requires authenticated users (already have Firebase Auth)
2. **Network Dependency**: Production mode requires internet connection
3. **Error Handling**: Need robust error handling for network failures
4. **Caching**: Consider adding caching layer for offline support (future enhancement)
5. **File Paths**: Ensure Firebase Storage paths match expected file names

## Next Steps

1. Review this plan
2. Confirm Firebase Storage bucket setup
3. Upload JSON files to Firebase Storage
4. Configure security rules
5. Proceed with implementation

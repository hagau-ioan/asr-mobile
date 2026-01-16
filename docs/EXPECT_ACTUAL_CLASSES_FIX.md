# Expect/Actual Classes Warning Suppression

## Issue
Kotlin Multiplatform shows warnings for `expect`/`actual` classes:
```
'expect'/'actual' classes (including interfaces, objects, annotations, enums, 
and 'actual' typealiases) are in Beta. Consider using the '-Xexpect-actual-classes' 
flag to suppress this warning.
```

## Solution
Added `-Xexpect-actual-classes` compiler flag to both modules that use expect/actual declarations.

## Files Modified

### 1. shared/build.gradle.kts
```kotlin
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xexpect-actual-classes")  // Added
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
        iosTarget.compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")  // Added
        }
    }
}
```

### 2. composeApp/build.gradle.kts
```kotlin
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xexpect-actual-classes")  // Added
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(project(":shared"))
            binaryOption("bundleId", "com.asr.financial.ComposeApp")
        }
        iosTarget.compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")  // Added
        }
    }
}
```

## Affected Classes
The flag suppresses warnings for these expect/actual declarations:

### Platform Abstractions (shared module)
1. **Clock** - Date/time operations
2. **SecureStorage** - Keychain/KeyStore access
3. **FileHandler** - File system operations
4. **ImageCompressor** - Image compression
5. **Logger** - Platform-specific logging

Each has:
- `expect class` in `commonMain`
- `actual class` in `androidMain`
- `actual class` in `iosMain`

## Why This Flag?
- **Beta Feature**: Expect/actual classes are still in Beta in Kotlin
- **Stable Usage**: The feature is stable enough for production use
- **Clean Builds**: Suppresses warnings without affecting functionality
- **Future-Proof**: When feature becomes stable, flag can be removed

## Verification
✅ No expect/actual warnings in build output
✅ All platform abstractions compile correctly
✅ Android and iOS targets build successfully

## Build Status
```
BUILD SUCCESSFUL in 276ms
88 actionable tasks: 2 executed, 86 up-to-date
```

## References
- [Kotlin Multiplatform Expected and Actual Declarations](https://kotlinlang.org/docs/multiplatform-expect-actual.html)
- [Kotlin Compiler Options](https://kotlinlang.org/docs/compiler-reference.html)

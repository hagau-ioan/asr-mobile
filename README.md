# ASR Financial Management

A Kotlin Multiplatform (KMP) application for managing financial data across congregations, built for Android and iOS.

## Architecture

- **Kotlin Multiplatform** - Share business logic across platforms
- **Compose Multiplatform** - Shared UI layer
- **MVVM + Clean Architecture** - Separation of concerns
- **SQLDelight** - Type-safe database
- **Koin** - Dependency injection

## Project Structure

```
asr-financial/
├── shared/              # Business logic + data (no UI)
├── composeApp/          # UI layer
├── androidApp/          # Android wrapper
└── iosApp/              # iOS Xcode project
```

## Requirements

- Kotlin 2.0.21+
- Android Studio Ladybug or later
- Xcode 15+ (for iOS development)
- JDK 17+

## Documentation

See [ASR_KMP_ARCHITECTURE.md](../ASR_KMP_ARCHITECTURE.md) for detailed architecture documentation.

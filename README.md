# ASR Financial Management

A Kotlin Multiplatform (KMP) application for managing financial data across congregations, built for Android and iOS.

## Architecture

- **Kotlin Multiplatform** - Share business logic across platforms
- **Compose Multiplatform** - Shared UI layer
- **MVVM + Clean Architecture + Interactor Pattern** - Separation of concerns
- **SQLDelight** - Type-safe database
- **Koin** - Dependency injection
- **MVI Pattern** - Unidirectional data flow with States, Events, and Effects

## Project Structure

```
asr-financial/
├── shared/              # Business logic + data (no UI)
│   ├── domain/          # Pure Kotlin business logic
│   ├── data/            # Repository implementations
│   ├── platform/        # Platform abstractions (expect/actual)
│   └── di/              # Dependency injection
│
├── composeApp/          # UI layer
│   ├── presentation/    # ViewModels, Interactors, Screens
│   ├── theme/           # Design system
│   └── navigation/      # App navigation
│
├── androidApp/          # Android wrapper
└── iosApp/              # iOS Xcode project
```

## Key Features

- **Two-Module Architecture**: Enforces domain purity (no Compose in shared module)
- **Platform Abstractions**: Clock, SecureStorage, FileHandler, ImageCompressor, Logger
- **Clean Architecture**: Domain layer has zero dependencies on outer layers
- **Type-Safe Database**: SQLDelight with compile-time SQL verification
- **Secure Storage**: Android KeyStore / iOS Keychain integration
- **Image Management**: Receipt storage with compression

## Requirements

- Kotlin 2.0.21+
- Android Studio Ladybug or later
- Xcode 15+ (for iOS development)
- JDK 17+
- Gradle 8.11.1+

## Getting Started

### Android

```bash
./gradlew :androidApp:assembleDebug
```

### iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Run the project on simulator or device

## Technology Stack

| Category | Library | Version |
|----------|---------|---------|
| Kotlin | Kotlin Multiplatform | 2.0.21 |
| UI | Compose Multiplatform | 1.7.1 |
| Coroutines | kotlinx-coroutines | 1.9.0 |
| Database | SQLDelight | 2.0.2 |
| DI | Koin | 4.0.0 |
| Navigation | Voyager | 1.1.0 |
| Images | Coil 3 | 3.0.4 |
| Charts | Vico | 2.0.0 |

## Documentation

See [ASR_KMP_ARCHITECTURE.md](../ASR_KMP_ARCHITECTURE.md) for detailed architecture documentation.

## License

Proprietary - All rights reserved

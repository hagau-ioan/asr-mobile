# iOS App Setup

## Overview

The iOS app uses **Compose Multiplatform** to share the same UI code with Android. All screens and layouts from the `composeApp` module are rendered natively on iOS.

## Project Structure

```
iosApp/
├── iosApp.xcodeproj/          # Xcode project
├── iOSApp.swift               # SwiftUI app entry point
└── Info.plist                 # iOS app configuration
```

## Requirements

- macOS with Xcode 15+ installed
- iOS 14.0+ deployment target
- Apple Developer account (for physical device testing)

## How It Works

1. **Kotlin Framework**: The `composeApp` module is compiled into a native iOS framework (`ComposeApp.framework`)
2. **SwiftUI Integration**: The Swift app wraps the Compose UI using `UIViewControllerRepresentable`
3. **Shared UI**: All Compose screens from `composeApp/src/commonMain` run on iOS without modification
4. **Direct Integration**: Xcode build script automatically compiles the Kotlin framework

## Running on iOS

### Option 1: From Xcode (Recommended)

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select a simulator or device
3. Click Run (⌘R)

The build script will automatically:
- Compile the Kotlin Multiplatform code
- Generate the ComposeApp framework
- Link it to the iOS app
- Launch the app

### Option 2: From IntelliJ IDEA / Android Studio

1. In the run configurations dropdown, select `iosApp`
2. Choose your target device/simulator
3. Click Run

The IDE will build the framework and launch Xcode automatically.

## Configuration

### Team ID (Required for Physical Devices)

To run on a real iPhone/iPad:

1. Open the project in Xcode
2. Select the `iosApp` target
3. Go to **Signing & Capabilities**
4. Select your Team from the dropdown
5. Ensure Bundle Identifier is unique: `com.asr.financial`

### Build Script

The Xcode project includes a "Compile Kotlin Framework" build phase that runs:

```bash
cd "$SRCROOT/.."
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

This script:
- Detects the Xcode build configuration (Debug/Release)
- Compiles the appropriate Kotlin framework
- Signs and embeds it in the app bundle

## Troubleshooting

### "User Script Sandboxing" Error

If you see sandboxing errors:
1. In Xcode, go to Build Settings
2. Search for "User Script Sandboxing"
3. Set it to **No**
4. Run `./gradlew --stop` to restart the Gradle daemon

### Framework Not Found

If the app fails to link the framework:
1. Clean build folder: Product → Clean Build Folder (⌘⇧K)
2. Delete derived data: `rm -rf ~/Library/Developer/Xcode/DerivedData`
3. Rebuild the project

### Simulator Not Showing

Make sure you have iOS simulators installed:
```bash
xcodebuild -downloadPlatform iOS
```

## Swift Package Manager

This project uses **direct integration** (not SPM) for better IDE support and faster builds. The Kotlin framework is built on-demand during Xcode builds.

## Architecture

- **SwiftUI App**: Minimal Swift wrapper (`iOSApp.swift`)
- **Compose UI**: All screens defined in `composeApp/src/commonMain/kotlin`
- **Shared Logic**: Business logic in `shared` module
- **Platform Code**: iOS-specific implementations in `composeApp/src/iosMain/kotlin`

## Next Steps

1. Open the project in Xcode
2. Build and run on a simulator
3. All Android screens will work on iOS automatically!

For more details, see the [official Compose Multiplatform documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-create-first-app.html).

# Firebase iOS SDK Setup via Swift Package Manager

## Quick Setup (5 minutes)

### Step 1: Open Xcode Project
```bash
open iosApp/iosApp.xcodeproj
```

### Step 2: Add Firebase Package
1. In Xcode, select the **iosApp** project in the Project Navigator (left sidebar)
2. Select the **iosApp** target
3. Go to the **Package Dependencies** tab
4. Click the **+** button
5. Enter URL: `https://github.com/firebase/firebase-ios-sdk`
6. Click **Add Package**
7. Select version: **Up to Next Major Version** (e.g., `10.25.0`)
8. Select products:
   - ✅ `FirebaseAuth`
   - ✅ `FirebaseCore`
9. Click **Add Package**

### Step 3: Verify
- Check **Project Navigator** → **Package Dependencies** → Should see `firebase-ios-sdk`
- Build the project (⌘B) - should compile successfully

### Step 4: Add GoogleService-Info.plist
1. Download `GoogleService-Info.plist` from Firebase Console
2. Drag it into `iosApp/iosApp/` folder in Xcode
3. ✅ Check "Copy items if needed"
4. ✅ Ensure it's added to the `iosApp` target

## Done! ✅

The Firebase iOS SDK is now integrated. The code in `iOSApp.swift` and `FirebaseAuthBridge.swift` will work once these steps are completed.

## Troubleshooting

**"No such module 'FirebaseAuth'"**
- Clean build folder: **Product → Clean Build Folder** (Shift+⌘+K)
- Rebuild: **Product → Build** (⌘B)

**Package not resolving**
- Check internet connection
- **File → Packages → Reset Package Caches**
- Try removing and re-adding the package

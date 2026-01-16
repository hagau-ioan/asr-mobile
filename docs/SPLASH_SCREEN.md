# Splash Screen Implementation

## Overview
Fancy animated splash screen for ASR Financial Management app with smooth animations and gradient background.

## Features

### Visual Design
- **Gradient Background**: Vertical gradient from primary (slate-900) to tertiary (cyan-600)
- **Icon**: Material Icons AccountBalance (bank building) - 120dp size
- **Typography Hierarchy**:
  - "ASR" - Display Large, Bold
  - "Gestiune Financiară" - Headline Medium
  - "Târgu Mureș" - Title Large

### Animations
1. **Icon Animation**: Spring bounce effect
   - Scale from 0 to 1
   - Medium bouncy damping ratio
   - Low stiffness for smooth effect

2. **Text Fade-in**: Alpha animation
   - Duration: 1000ms
   - Delay: 300ms (after icon starts)
   - Smooth fade from 0 to 1

3. **Auto-dismiss**: 2.5 seconds total duration

## Implementation

### File Structure
```
composeApp/src/commonMain/kotlin/com/asr/financial/
├── App.kt (modified)
└── presentation/screens/splash/
    └── SplashScreen.kt (new)
```

### App.kt Integration
```kotlin
@Composable
fun App() {
    var showSplash by remember { mutableStateOf(true) }
    
    AppTheme {
        if (showSplash) {
            SplashScreen(onSplashFinished = { showSplash = false })
        } else {
            val navController = rememberNavController()
            NavGraph(navController = navController)
        }
    }
}
```

### SplashScreen.kt
- Uses Compose animations (animateFloatAsState, spring, tween)
- LaunchedEffect for auto-dismiss after 2.5s
- Callback pattern for navigation control
- Fully theme-aware (uses MaterialTheme colors)

## Theme Colors Used
- `MaterialTheme.colorScheme.primary` - Slate-900 (dark blue-gray)
- `MaterialTheme.colorScheme.tertiary` - Cyan-600 (accent)
- `MaterialTheme.colorScheme.onPrimary` - White text

## Timing
- **0ms**: Icon starts scaling (spring animation)
- **300ms**: Text starts fading in
- **1300ms**: All animations complete
- **2500ms**: Splash screen dismisses, main app loads

## Benefits
1. **Professional Look**: Smooth animations create polished experience
2. **Branding**: Clear display of app name and location
3. **Loading Time**: Masks initial app loading
4. **Theme Consistent**: Uses app's color scheme
5. **KMP Compatible**: Works on Android and iOS

## Customization Options

### Change Duration
```kotlin
delay(2500) // Modify this value in LaunchedEffect
```

### Change Animation Speed
```kotlin
// Icon animation
animationSpec = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy, // Adjust bounce
    stiffness = Spring.StiffnessLow // Adjust speed
)

// Text animation
animationSpec = tween(
    durationMillis = 1000, // Adjust fade duration
    delayMillis = 300 // Adjust delay
)
```

### Change Icon
```kotlin
Icon(
    imageVector = Icons.Default.AccountBalance, // Change icon here
    // ...
)
```

## Testing
✅ Build successful
✅ Animations smooth
✅ Auto-dismiss works
✅ Theme colors applied
✅ Text hierarchy clear

## Future Enhancements
- Add loading indicator for data fetching
- Add version number at bottom
- Add "Powered by" attribution if needed
- Implement splash screen API for Android 12+

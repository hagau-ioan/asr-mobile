# Gestionarea Versiunii Aplicației

Versiunea aplicației este centralizată și ușor de actualizat.

## Locații

### 1. Sursă de Adevăr: `gradle/libs.versions.toml`
```toml
[versions]
app-version = "1.0.0"
app-version-code = "1"
```

### 2. Cod Kotlin: `AppConfig.kt`
```kotlin
object AppConfig {
    const val VERSION_NAME: String = "1.0.0"
    const val VERSION_CODE: Int = 1
}
```

### 3. iOS: `Info.plist`
```xml
<key>CFBundleShortVersionString</key>
<string>1.0.0</string>
<key>CFBundleVersion</key>
<string>1</string>
```

## Cum să Actualizezi Versiunea

### Pas 1: Actualizează `libs.versions.toml`
```toml
app-version = "1.1.0"      # Versiune vizibilă (semantic versioning)
app-version-code = "2"     # Build number (incrementează mereu)
```

### Pas 2: Actualizează `AppConfig.kt`
```kotlin
const val VERSION_NAME: String = "1.1.0"
const val VERSION_CODE: Int = 2
```

### Pas 3: Actualizează `Info.plist` (iOS)
```xml
<string>1.1.0</string>  <!-- CFBundleShortVersionString -->
<string>2</string>       <!-- CFBundleVersion -->
```

## Utilizare în Cod

```kotlin
// În UI
Text(stringResource(Res.string.version, AppConfig.VERSION_NAME))

// În logică
if (AppConfig.VERSION_CODE > previousVersion) {
    // Migrare date
}

// Logging
Log.d("App", "Version: ${AppConfig.VERSION_NAME} (${AppConfig.VERSION_CODE})")
```

## Semantic Versioning

Format: `MAJOR.MINOR.PATCH`

- **MAJOR**: Schimbări incompatibile (breaking changes)
- **MINOR**: Funcționalități noi (backward compatible)
- **PATCH**: Bug fixes

Exemple:
- `1.0.0` → Prima versiune
- `1.1.0` → Funcționalitate nouă
- `1.1.1` → Bug fix
- `2.0.0` → Breaking change

## Version Code

- Incrementează cu 1 la fiecare build
- Folosit de store-uri pentru a determina versiunea mai nouă
- Nu poate scădea niciodată

## Automatizare Viitoare

Pentru sincronizare automată, se poate folosi:
- Gradle task care citește din `libs.versions.toml`
- Script care actualizează toate fișierele
- CI/CD pipeline care setează versiunea

## Verificare

```bash
# Android
./gradlew :androidApp:assembleDebug
# Verifică în APK: versionName și versionCode

# iOS
xcodebuild -project iosApp/iosApp.xcodeproj -showBuildSettings | grep VERSION
```

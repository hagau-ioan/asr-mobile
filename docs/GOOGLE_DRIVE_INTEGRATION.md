# Google Drive Integration

This document describes how to download JSON files from Google Drive cloud storage for the ASR Financial application.

## Overview

The application can fetch JSON data files (`transactions.json`, `congregations.json`, `config.json`) from Google Drive instead of bundled resources. This enables:
- Remote data updates without app releases
- Centralized data management
- Multiple users accessing the same data

## Google Drive API Setup

### 1. Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Google Drive API**:
   - Navigate to **APIs & Services > Library**
   - Search for "Google Drive API"
   - Click **Enable**

### 2. Configure OAuth Consent Screen

1. Go to **APIs & Services > OAuth consent screen**
2. Select **External** (or Internal for organization use)
3. Fill in the required fields:
   - App name: `ASR Financial`
   - User support email
   - Developer contact information
4. Add scopes:
   - `https://www.googleapis.com/auth/drive.readonly` (read-only access)
   - Or `https://www.googleapis.com/auth/drive.file` (access to files created by the app)

### 3. Create Credentials

#### Option A: API Key (Public Files Only)

For publicly shared files:

1. Go to **APIs & Services > Credentials**
2. Click **Create Credentials > API Key**
3. Restrict the key:
   - Application restrictions: Android/iOS apps
   - API restrictions: Google Drive API only

#### Option B: OAuth 2.0 Client ID (Private Files)

For user-specific or private files:

1. Go to **APIs & Services > Credentials**
2. Click **Create Credentials > OAuth client ID**
3. For Android:
   - Application type: **Android**
   - Package name: `com.asr.financial`
   - SHA-1 certificate fingerprint (from your keystore)
4. For iOS:
   - Application type: **iOS**
   - Bundle ID: `com.asr.financial`

#### Option C: Service Account (Server-to-Server)

For automated access without user interaction:

1. Go to **APIs & Services > Credentials**
2. Click **Create Credentials > Service Account**
3. Download the JSON key file
4. Share Drive files/folders with the service account email

## File Sharing Setup

### Make Files Accessible

1. Upload JSON files to Google Drive
2. Right-click each file > **Share**
3. Choose access level:
   - **Anyone with the link** (for API Key access)
   - **Specific service account email** (for Service Account access)
4. Copy the **File ID** from the URL:
   ```
   https://drive.google.com/file/d/FILE_ID_HERE/view
   ```

## Implementation

### Dependencies

Add Ktor client dependencies to `composeApp/build.gradle.kts`:

```kotlin
commonMain.dependencies {
    // Ktor Client (already included)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}

androidMain.dependencies {
    implementation(libs.ktor.client.okhttp)
}

iosMain.dependencies {
    implementation(libs.ktor.client.darwin)
}
```

### Google Drive API Constants

Create `GoogleDriveConfig.kt`:

```kotlin
package com.asr.financial.data.remote

object GoogleDriveConfig {
    // Google Drive API base URL
    const val BASE_URL = "https://www.googleapis.com/drive/v3"

    // File IDs from Google Drive
    const val TRANSACTIONS_FILE_ID = "your-transactions-file-id"
    const val CONGREGATIONS_FILE_ID = "your-congregations-file-id"
    const val CONFIG_FILE_ID = "your-config-file-id"

    // API Key (for public files only)
    const val API_KEY = "your-api-key"
}
```

### Google Drive Data Source

Create `GoogleDriveDataSource.kt`:

```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.data.remote.GoogleDriveConfig
import com.asr.financial.domain.model.Transaction
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

/**
 * Google Drive implementation of TransactionDataSource.
 * Downloads JSON files directly from Google Drive.
 */
class GoogleDriveTransactionDataSource(
    private val httpClient: HttpClient
) : TransactionDataSource {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun getAll(): List<Transaction> {
        return try {
            val jsonString = downloadFile(GoogleDriveConfig.TRANSACTIONS_FILE_ID)
            json.decodeFromString<List<Transaction>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getByMonth(month: Int, year: Int): List<Transaction> {
        return getAll().filter { transaction ->
            val parts = transaction.date.split("-")
            if (parts.size >= 2) {
                val txnYear = parts[0].toIntOrNull()
                val txnMonth = parts[1].toIntOrNull()
                txnYear == year && txnMonth == month
            } else {
                false
            }
        }
    }

    override suspend fun getById(id: String): Transaction? {
        return getAll().firstOrNull { it.id == id }
    }

    /**
     * Download file content from Google Drive.
     * Uses the files.get endpoint with alt=media to get raw content.
     */
    private suspend fun downloadFile(fileId: String): String {
        val response: HttpResponse = httpClient.get(
            "${GoogleDriveConfig.BASE_URL}/files/$fileId"
        ) {
            parameter("alt", "media")
            parameter("key", GoogleDriveConfig.API_KEY)
        }
        return response.bodyAsText()
    }
}
```

### HTTP Client Configuration

Create `HttpClientFactory.kt`:

```kotlin
package com.asr.financial.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
            }

            // Retry on failure
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
        }
    }
}
```

### Dependency Injection Update

Update `PresentationModule.kt` to use Google Drive:

```kotlin
val presentationModule = module {
    // HTTP Client
    single { HttpClientFactory.create() }

    // Switch from JSON resources to Google Drive
    // Option 1: Use local JSON (current)
    // single<TransactionDataSource> { JsonTransactionDataSource() }

    // Option 2: Use Google Drive
    single<TransactionDataSource> { GoogleDriveTransactionDataSource(get()) }

    // ... rest of module
}
```

## Caching Strategy

### In-Memory Cache

Add caching to avoid repeated network calls:

```kotlin
class CachedGoogleDriveTransactionDataSource(
    private val httpClient: HttpClient
) : TransactionDataSource {

    private var cachedTransactions: List<Transaction>? = null
    private var lastFetchTime: Long = 0
    private val cacheValidityMs = 5 * 60 * 1000 // 5 minutes

    override suspend fun getAll(): List<Transaction> {
        val now = Clock.System.now().toEpochMilliseconds()

        if (cachedTransactions != null && (now - lastFetchTime) < cacheValidityMs) {
            return cachedTransactions!!
        }

        return try {
            val jsonString = downloadFile(GoogleDriveConfig.TRANSACTIONS_FILE_ID)
            val transactions = json.decodeFromString<List<Transaction>>(jsonString)
            cachedTransactions = transactions
            lastFetchTime = now
            transactions
        } catch (e: Exception) {
            cachedTransactions ?: emptyList()
        }
    }

    fun invalidateCache() {
        cachedTransactions = null
        lastFetchTime = 0
    }

    // ... rest of implementation
}
```

### Disk Cache with DataStore

For persistent caching across app restarts:

```kotlin
class DiskCachedGoogleDriveDataSource(
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>
) : TransactionDataSource {

    private companion object {
        val TRANSACTIONS_KEY = stringPreferencesKey("cached_transactions")
        val LAST_FETCH_KEY = longPreferencesKey("last_fetch_time")
        const val CACHE_VALIDITY_MS = 60 * 60 * 1000L // 1 hour
    }

    override suspend fun getAll(): List<Transaction> {
        // Check disk cache first
        val cached = getCachedData()
        if (cached != null) {
            return cached
        }

        // Fetch from network
        return try {
            val jsonString = downloadFile(GoogleDriveConfig.TRANSACTIONS_FILE_ID)
            val transactions = json.decodeFromString<List<Transaction>>(jsonString)

            // Save to disk cache
            saveToDiskCache(jsonString)

            transactions
        } catch (e: Exception) {
            // Return stale cache on network error
            getCachedData(ignoreExpiry = true) ?: emptyList()
        }
    }

    private suspend fun getCachedData(ignoreExpiry: Boolean = false): List<Transaction>? {
        val prefs = dataStore.data.first()
        val lastFetch = prefs[LAST_FETCH_KEY] ?: 0
        val now = Clock.System.now().toEpochMilliseconds()

        if (!ignoreExpiry && (now - lastFetch) > CACHE_VALIDITY_MS) {
            return null
        }

        val jsonString = prefs[TRANSACTIONS_KEY] ?: return null
        return try {
            json.decodeFromString<List<Transaction>>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveToDiskCache(jsonString: String) {
        dataStore.edit { prefs ->
            prefs[TRANSACTIONS_KEY] = jsonString
            prefs[LAST_FETCH_KEY] = Clock.System.now().toEpochMilliseconds()
        }
    }
}
```

## OAuth 2.0 Authentication (Private Files)

For accessing private files, implement OAuth 2.0 flow:

### Platform-Specific OAuth

#### Android (`androidMain`)

```kotlin
// Use Google Sign-In for Android
actual class GoogleAuthProvider {
    actual suspend fun getAccessToken(): String? {
        // Implement using Google Sign-In API
        // Return the OAuth access token
    }
}
```

#### iOS (`iosMain`)

```kotlin
// Use Google Sign-In for iOS
actual class GoogleAuthProvider {
    actual suspend fun getAccessToken(): String? {
        // Implement using Google Sign-In SDK
        // Return the OAuth access token
    }
}
```

### Authenticated Requests

```kotlin
private suspend fun downloadFileAuthenticated(fileId: String): String {
    val accessToken = googleAuthProvider.getAccessToken()
        ?: throw IllegalStateException("Not authenticated")

    val response: HttpResponse = httpClient.get(
        "${GoogleDriveConfig.BASE_URL}/files/$fileId"
    ) {
        parameter("alt", "media")
        header("Authorization", "Bearer $accessToken")
    }
    return response.bodyAsText()
}
```

## Error Handling

```kotlin
sealed class GoogleDriveError : Exception() {
    data object NetworkError : GoogleDriveError()
    data object FileNotFound : GoogleDriveError()
    data object Unauthorized : GoogleDriveError()
    data object QuotaExceeded : GoogleDriveError()
    data class Unknown(override val message: String) : GoogleDriveError()
}

private suspend fun downloadFileWithErrorHandling(fileId: String): Result<String> {
    return try {
        val response: HttpResponse = httpClient.get(
            "${GoogleDriveConfig.BASE_URL}/files/$fileId"
        ) {
            parameter("alt", "media")
            parameter("key", GoogleDriveConfig.API_KEY)
        }

        when (response.status.value) {
            200 -> Result.success(response.bodyAsText())
            401, 403 -> Result.failure(GoogleDriveError.Unauthorized)
            404 -> Result.failure(GoogleDriveError.FileNotFound)
            429 -> Result.failure(GoogleDriveError.QuotaExceeded)
            else -> Result.failure(GoogleDriveError.Unknown("HTTP ${response.status.value}"))
        }
    } catch (e: Exception) {
        Result.failure(GoogleDriveError.NetworkError)
    }
}
```

## Testing

### Mock Data Source for Testing

```kotlin
class MockTransactionDataSource : TransactionDataSource {
    var mockTransactions: List<Transaction> = emptyList()
    var shouldFail: Boolean = false

    override suspend fun getAll(): List<Transaction> {
        if (shouldFail) throw Exception("Mock failure")
        return mockTransactions
    }

    override suspend fun getByMonth(month: Int, year: Int): List<Transaction> {
        return getAll().filter { /* filter logic */ }
    }

    override suspend fun getById(id: String): Transaction? {
        return getAll().firstOrNull { it.id == id }
    }
}
```

## Security Considerations

1. **Never hardcode API keys** in source code for production
   - Use BuildConfig fields
   - Use environment variables
   - Use secure storage

2. **Restrict API keys** in Google Cloud Console
   - Limit to specific apps (package name/bundle ID)
   - Limit to specific APIs

3. **Use HTTPS only** (Ktor does this by default)

4. **Validate downloaded data** before parsing

5. **Handle token refresh** for OAuth flows

## Migration Path

To switch from local JSON to Google Drive:

1. Upload JSON files to Google Drive
2. Note the File IDs
3. Create Google Cloud project and credentials
4. Implement `GoogleDriveTransactionDataSource`
5. Update DI module to inject Google Drive implementation
6. Test thoroughly
7. Keep local JSON as fallback

```kotlin
// Hybrid approach: Try Google Drive, fallback to local
class HybridTransactionDataSource(
    private val googleDriveDataSource: GoogleDriveTransactionDataSource,
    private val localDataSource: JsonTransactionDataSource
) : TransactionDataSource {

    override suspend fun getAll(): List<Transaction> {
        return try {
            googleDriveDataSource.getAll()
        } catch (e: Exception) {
            localDataSource.getAll()
        }
    }

    // ... other methods
}
```

## References

- [Google Drive API Documentation](https://developers.google.com/drive/api/v3/about-sdk)
- [Ktor Client Documentation](https://ktor.io/docs/client.html)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Google Sign-In for iOS](https://developers.google.com/identity/sign-in/ios)

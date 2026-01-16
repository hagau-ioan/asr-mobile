# Data Layer Refactoring Proposal

**Version:** 1.1
**Date:** January 2026
**Status:** Proposal

---

## Executive Summary

This document proposes a refactoring of the data layer to follow clean architecture principles. The primary data source will be **multiple JSON files in assets**, making it easy to:
- Add new data types by adding new JSON files
- Later swap to REST API or Database by changing only the DataSource implementation

---

## 1. Current Architecture (Problem)

### Current Flow

```
composeApp/ComposeJsonLoader ──┐
                               ↓
shared/MockTransactionRepository → Uses JsonLoader interface from shared
                               ↑
                               └── JsonLoader defined in shared, implemented in composeApp
```

### Current Files

| File | Location | Issue |
|------|----------|-------|
| `JsonLoader` | `shared/data/repository/` | Interface in wrong layer |
| `MockTransactionRepository` | `shared/data/repository/` | Mixes repository + data source concerns |
| `ComposeJsonLoader` | `composeApp/data/` | Implements shared interface |

### Problems

1. **Violation of Clean Architecture**: `JsonLoader` is a data source concern, not a repository concern
2. **Tight Coupling**: Repository knows about JSON loading details
3. **Hard to Swap**: Changing data source requires modifying repository
4. **Module Boundary Issues**: Interface in `shared`, implementation in `composeApp`
5. **Single File Only**: Current design only supports one JSON file

---

## 2. JSON Files Strategy

### 2.1 JSON Files Organization

All JSON data files will be stored in the Compose resources folder:

```
composeApp/src/commonMain/composeResources/files/
├── transactions.json          # All transactions (income + expenses)
├── congregations.json         # Congregation master data
├── donations.json             # Donation records
├── expenses.json              # Expense records
├── utility_expenses.json      # Utility-specific expenses
└── settings.json              # App configuration (optional)
```

### 2.2 JSON File Formats

**transactions.json** (current format - kept for compatibility)
```json
[
  {
    "id": "txn_2024_01_001",
    "type": "INCOME",
    "amount": 5000.00,
    "description": "Donație Congregația A",
    "date": "2024-01-15",
    "category": "Donații",
    "congregationName": "Congregația A"
  }
]
```

**congregations.json**
```json
[
  {
    "id": "cong_001",
    "name": "Congregația A",
    "location": "București",
    "memberCount": 120,
    "isActive": true
  }
]
```

**donations.json**
```json
[
  {
    "id": "don_001",
    "congregationId": "cong_001",
    "amount": 5000.00,
    "date": "2024-01-15",
    "type": "REGULAR",
    "notes": "Contribuție lunară"
  }
]
```

**expenses.json**
```json
[
  {
    "id": "exp_001",
    "amount": 1200.00,
    "description": "Factură electricitate",
    "date": "2024-01-10",
    "category": "Utilități",
    "receiptPath": null
  }
]
```

**utility_expenses.json**
```json
[
  {
    "id": "util_001",
    "type": "ELECTRICITY",
    "amount": 1200.00,
    "date": "2024-01-10",
    "provider": "Enel",
    "meterReading": 12345,
    "previousReading": 12100
  }
]
```

---

## 3. Proposed Architecture (Solution)

### 3.1 New Layer Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│  SHARED MODULE (Business Logic - No UI dependencies)                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  domain/                                                            │
│  ├── models/                                                        │
│  │   ├── Transaction.kt              ✓ EXISTS                       │
│  │   ├── Congregation.kt             ✓ EXISTS                       │
│  │   ├── Donation.kt                 ✓ EXISTS                       │
│  │   ├── Expense.kt                  ✓ EXISTS                       │
│  │   └── UtilityExpense.kt           ✓ EXISTS                       │
│  ├── repository/                                                    │
│  │   ├── TransactionRepository.kt    ✓ EXISTS (interface)           │
│  │   ├── CongregationRepository.kt   ✓ EXISTS (interface)           │
│  │   ├── DonationRepository.kt       ✓ EXISTS (interface)           │
│  │   ├── ExpenseRepository.kt        ✓ EXISTS (interface)           │
│  │   └── UtilityExpenseRepository.kt ✓ EXISTS (interface)           │
│  └── usecase/                                                       │
│      └── ...UseCases.kt              ✓ EXISTS                       │
│                                                                     │
│  data/                                                              │
│  ├── datasource/                                                    │
│  │   ├── TransactionDataSource.kt    ★ NEW (interface)              │
│  │   ├── CongregationDataSource.kt   ★ NEW (interface)              │
│  │   ├── DonationDataSource.kt       ★ NEW (interface)              │
│  │   ├── ExpenseDataSource.kt        ★ NEW (interface)              │
│  │   └── UtilityExpenseDataSource.kt ★ NEW (interface)              │
│  └── repository/                                                    │
│      ├── TransactionRepositoryImpl.kt ★ NEW                         │
│      ├── CongregationRepositoryImpl.kt ★ NEW                        │
│      ├── DonationRepositoryImpl.kt   ★ NEW                          │
│      ├── ExpenseRepositoryImpl.kt    ★ NEW                          │
│      └── UtilityExpenseRepositoryImpl.kt ★ NEW                      │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                               ↑
                               │ provides DataSource implementations
                               │
┌─────────────────────────────────────────────────────────────────────┐
│  COMPOSE APP MODULE (UI + JSON data sources)                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  data/                                                              │
│  ├── json/                                                          │
│  │   └── JsonResourceLoader.kt       ★ NEW (utility class)          │
│  └── datasource/                                                    │
│      ├── JsonTransactionDataSource.kt    ★ NEW                      │
│      ├── JsonCongregationDataSource.kt   ★ NEW                      │
│      ├── JsonDonationDataSource.kt       ★ NEW                      │
│      ├── JsonExpenseDataSource.kt        ★ NEW                      │
│      └── JsonUtilityExpenseDataSource.kt ★ NEW                      │
│                                                                     │
│  di/PresentationModule.kt                                           │
│  └── Provides all DataSource bindings                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 Data Flow

```
UI (Screen)
    ↓ collectAsState()
ViewModel
    ↓ delegates to
Interactor
    ↓ calls
UseCase                                   ← shared/domain/usecase/
    ↓ calls
Repository (RepositoryImpl)               ← shared/data/repository/
    ↓ calls
DataSource (interface)                    ← shared/data/datasource/
    ↓ implemented by
JsonXxxDataSource                         ← composeApp/data/datasource/
    ↓ uses
JsonResourceLoader                        ← composeApp/data/json/
    ↓ reads
xxx.json                                  ← composeApp/composeResources/files/
```

---

## 4. Implementation Details

### 4.1 JSON Resource Loader (Utility Class)

**Location:** `composeApp/src/commonMain/kotlin/com/asr/financial/data/json/JsonResourceLoader.kt`

```kotlin
package com.asr.financial.data.json

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import asr_financial.composeapp.generated.resources.Res

/**
 * Centralized utility for loading JSON files from Compose resources.
 * All JSON DataSource implementations use this loader.
 */
object JsonResourceLoader {

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    /**
     * Load and decode JSON file from resources
     * @param fileName Name of the JSON file (e.g., "transactions.json")
     * @return Decoded object of type T, or null if loading fails
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend inline fun <reified T> loadFromResources(fileName: String): T? {
        return try {
            val jsonString = Res.readBytes("files/$fileName").decodeToString()
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Load and decode JSON file, returning empty list on failure
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend inline fun <reified T> loadListFromResources(fileName: String): List<T> {
        return try {
            val jsonString = Res.readBytes("files/$fileName").decodeToString()
            json.decodeFromString<List<T>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
```

### 4.2 DataSource Interface Example: `TransactionDataSource`

**Location:** `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/TransactionDataSource.kt`

```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.domain.model.Transaction

/**
 * Data source interface for Transaction data.
 * Implementations can be JSON files, Database, or API.
 */
interface TransactionDataSource {
    suspend fun getAll(): List<Transaction>
    suspend fun getByMonth(month: Int, year: Int): List<Transaction>
    suspend fun getById(id: String): Transaction?
}
```

### 4.3 DataSource Interface: `CongregationDataSource`

**Location:** `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/CongregationDataSource.kt`

```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Congregation

/**
 * Data source interface for Congregation data.
 */
interface CongregationDataSource {
    suspend fun getAll(): List<Congregation>
    suspend fun getById(id: String): Congregation?
    suspend fun getActive(): List<Congregation>
}
```

### 4.4 DataSource Interface: `DonationDataSource`

**Location:** `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/DonationDataSource.kt`

```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Donation

/**
 * Data source interface for Donation data.
 */
interface DonationDataSource {
    suspend fun getAll(): List<Donation>
    suspend fun getByMonth(month: Int, year: Int): List<Donation>
    suspend fun getByCongregation(congregationId: String): List<Donation>
    suspend fun getById(id: String): Donation?
}
```

### 4.5 DataSource Interface: `ExpenseDataSource`

**Location:** `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/ExpenseDataSource.kt`

```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.domain.models.Expense

/**
 * Data source interface for Expense data.
 */
interface ExpenseDataSource {
    suspend fun getAll(): List<Expense>
    suspend fun getByMonth(month: Int, year: Int): List<Expense>
    suspend fun getByCategory(category: String): List<Expense>
    suspend fun getById(id: String): Expense?
}
```

### 4.6 DataSource Interface: `UtilityExpenseDataSource`

**Location:** `shared/src/commonMain/kotlin/com/asr/financial/data/datasource/UtilityExpenseDataSource.kt`

```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.domain.models.UtilityExpense

/**
 * Data source interface for UtilityExpense data.
 */
interface UtilityExpenseDataSource {
    suspend fun getAll(): List<UtilityExpense>
    suspend fun getByMonth(month: Int, year: Int): List<UtilityExpense>
    suspend fun getByType(type: String): List<UtilityExpense>
    suspend fun getById(id: String): UtilityExpense?
}
```

### 4.7 Repository Implementation Example: `TransactionRepositoryImpl`

**Location:** `shared/src/commonMain/kotlin/com/asr/financial/data/repository/TransactionRepositoryImpl.kt`

```kotlin
package com.asr.financial.data.repository

import com.asr.financial.data.datasource.TransactionDataSource
import com.asr.financial.domain.model.Transaction
import com.asr.financial.domain.repository.TransactionRepository

/**
 * Repository implementation - delegates to DataSource.
 * Data source agnostic (doesn't know if JSON, DB, or API).
 */
class TransactionRepositoryImpl(
    private val dataSource: TransactionDataSource
) : TransactionRepository {

    override suspend fun getAllTransactions(): List<Transaction> {
        return dataSource.getAll()
    }

    override suspend fun getTransactionsByMonth(month: Int, year: Int): List<Transaction> {
        return dataSource.getByMonth(month, year)
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return dataSource.getById(id)
    }
}
```

### 4.8 JSON DataSource Implementation: `JsonTransactionDataSource`

**Location:** `composeApp/src/commonMain/kotlin/com/asr/financial/data/datasource/JsonTransactionDataSource.kt`

```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.data.json.JsonResourceLoader
import com.asr.financial.domain.model.Transaction

/**
 * JSON file implementation of TransactionDataSource.
 * Loads data from transactions.json in Compose resources.
 */
class JsonTransactionDataSource : TransactionDataSource {

    private companion object {
        const val FILE_NAME = "transactions.json"
    }

    override suspend fun getAll(): List<Transaction> {
        return JsonResourceLoader.loadListFromResources(FILE_NAME)
    }

    override suspend fun getByMonth(month: Int, year: Int): List<Transaction> {
        return getAll().filter { transaction ->
            val parts = transaction.date.split("-")
            if (parts.size >= 2) {
                val txnYear = parts[0].toIntOrNull()
                val txnMonth = parts[1].toIntOrNull()
                txnYear == year && txnMonth == month
            } else false
        }
    }

    override suspend fun getById(id: String): Transaction? {
        return getAll().firstOrNull { it.id == id }
    }
}
```

### 4.9 JSON DataSource Implementation: `JsonCongregationDataSource`

**Location:** `composeApp/src/commonMain/kotlin/com/asr/financial/data/datasource/JsonCongregationDataSource.kt`

```kotlin
package com.asr.financial.data.datasource

import com.asr.financial.data.json.JsonResourceLoader
import com.asr.financial.domain.models.Congregation

/**
 * JSON file implementation of CongregationDataSource.
 */
class JsonCongregationDataSource : CongregationDataSource {

    private companion object {
        const val FILE_NAME = "congregations.json"
    }

    override suspend fun getAll(): List<Congregation> {
        return JsonResourceLoader.loadListFromResources(FILE_NAME)
    }

    override suspend fun getById(id: String): Congregation? {
        return getAll().firstOrNull { it.id == id }
    }

    override suspend fun getActive(): List<Congregation> {
        return getAll().filter { it.isActive }
    }
}
```

### 4.10 Updated DI: `DataModule.kt`

**Location:** `shared/src/commonMain/kotlin/com/asr/financial/di/DataModule.kt`

```kotlin
package com.asr.financial.di

import com.asr.financial.data.repository.*
import com.asr.financial.domain.repository.*
import org.koin.dsl.module

/**
 * Data module - provides repository implementations.
 * DataSources are provided by PresentationModule (composeApp).
 */
val dataModule = module {
    // Repositories - all delegate to their respective DataSources
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CongregationRepository> { CongregationRepositoryImpl(get()) }
    single<DonationRepository> { DonationRepositoryImpl(get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
    single<UtilityExpenseRepository> { UtilityExpenseRepositoryImpl(get()) }
}
```

### 4.11 Updated DI: `PresentationModule.kt`

**Location:** `composeApp/src/commonMain/kotlin/com/asr/financial/di/PresentationModule.kt`

```kotlin
package com.asr.financial.di

import com.asr.financial.data.datasource.*
import com.asr.financial.presentation.mvi.interactor.HomeInteractor
import com.asr.financial.presentation.mvi.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {

    // ═══════════════════════════════════════════════════════════════════
    // DATA SOURCES - JSON File Implementations
    // To swap to API or Database, change these bindings only
    // ═══════════════════════════════════════════════════════════════════
    single<TransactionDataSource> { JsonTransactionDataSource() }
    single<CongregationDataSource> { JsonCongregationDataSource() }
    single<DonationDataSource> { JsonDonationDataSource() }
    single<ExpenseDataSource> { JsonExpenseDataSource() }
    single<UtilityExpenseDataSource> { JsonUtilityExpenseDataSource() }

    // ═══════════════════════════════════════════════════════════════════
    // INTERACTORS
    // ═══════════════════════════════════════════════════════════════════
    factory { HomeInteractor(get(), get()) }
    // Add more interactors as needed...

    // ═══════════════════════════════════════════════════════════════════
    // VIEWMODELS
    // ═══════════════════════════════════════════════════════════════════
    viewModel { HomeViewModel(get()) }
    // Add more viewmodels as needed...
}
```

---

## 5. Files to Delete

After implementing the new architecture, remove these deprecated files:

| File | Reason |
|------|--------|
| `shared/data/repository/MockTransactionRepository.kt` | Replaced by `TransactionRepositoryImpl` |
| `shared/data/repository/JsonLoader.kt` | Replaced by `JsonResourceLoader` |
| `composeApp/data/ComposeJsonLoader.kt` | Replaced by `JsonResourceLoader` |

---

## 6. Migration Checklist

### Phase 1: Core Infrastructure
- [ ] Create `composeApp/data/json/JsonResourceLoader.kt`
- [ ] Create `shared/data/datasource/TransactionDataSource.kt` (interface)
- [ ] Create `shared/data/repository/TransactionRepositoryImpl.kt`
- [ ] Create `composeApp/data/datasource/JsonTransactionDataSource.kt`
- [ ] Update `shared/di/DataModule.kt`
- [ ] Update `composeApp/di/PresentationModule.kt`
- [ ] Test Transaction flow works

### Phase 2: Additional Data Sources
- [ ] Create `CongregationDataSource` interface + JSON impl + Repository impl
- [ ] Create `DonationDataSource` interface + JSON impl + Repository impl
- [ ] Create `ExpenseDataSource` interface + JSON impl + Repository impl
- [ ] Create `UtilityExpenseDataSource` interface + JSON impl + Repository impl

### Phase 3: JSON Files
- [ ] Create/update `transactions.json`
- [ ] Create `congregations.json`
- [ ] Create `donations.json`
- [ ] Create `expenses.json`
- [ ] Create `utility_expenses.json`

### Phase 4: Cleanup
- [ ] Delete `shared/data/repository/MockTransactionRepository.kt`
- [ ] Delete `shared/data/repository/JsonLoader.kt`
- [ ] Delete `composeApp/data/ComposeJsonLoader.kt`
- [ ] Run full build for Android and iOS

---

## 7. Benefits Summary

| When you want to... | You only change... |
|---------------------|-------------------|
| Add new data type | Create new JSON file + DataSource interface + JSON impl |
| Switch to REST API | Create `ApiXxxDataSource`, change DI binding |
| Switch to Database | Create `DbXxxDataSource`, change DI binding |
| Add caching | Wrap DataSource with caching decorator |
| Change JSON format | Modify only the specific `JsonXxxDataSource` |

---

## 8. Future: Switching to REST API

When ready to move from JSON files to REST API, create new implementations:

```kotlin
// Location: composeApp/data/datasource/ApiTransactionDataSource.kt

class ApiTransactionDataSource(
    private val httpClient: HttpClient
) : TransactionDataSource {

    override suspend fun getAll(): List<Transaction> {
        return httpClient.get("https://api.example.com/transactions")
            .body<List<TransactionDto>>()
            .map { it.toDomain() }
    }

    // ... other methods
}
```

**DI Change Only:**
```kotlin
// In PresentationModule.kt - change ONE line:
single<TransactionDataSource> { ApiTransactionDataSource(get()) }
```

---

## 9. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              UI LAYER                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  Screen.kt → ViewModel → Interactor → UseCase                       │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↑
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DOMAIN LAYER (shared/)                            │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  Repository Interfaces                                               │    │
│  │  ├── TransactionRepository                                          │    │
│  │  ├── CongregationRepository                                         │    │
│  │  ├── DonationRepository                                             │    │
│  │  ├── ExpenseRepository                                              │    │
│  │  └── UtilityExpenseRepository                                       │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↑
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DATA LAYER (shared/)                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  Repository Implementations                                          │    │
│  │  └── XxxRepositoryImpl(dataSource: XxxDataSource)                   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    ↑                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  DataSource Interfaces  ←── ABSTRACTION POINT                       │    │
│  │  ├── TransactionDataSource                                          │    │
│  │  ├── CongregationDataSource                                         │    │
│  │  ├── DonationDataSource                                             │    │
│  │  ├── ExpenseDataSource                                              │    │
│  │  └── UtilityExpenseDataSource                                       │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↑
┌─────────────────────────────────────────────────────────────────────────────┐
│                    JSON DATA SOURCES (composeApp/)                           │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  JsonResourceLoader (utility)                                        │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    ↑                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  JSON DataSource Implementations                                     │    │
│  │  ├── JsonTransactionDataSource      → transactions.json             │    │
│  │  ├── JsonCongregationDataSource     → congregations.json            │    │
│  │  ├── JsonDonationDataSource         → donations.json                │    │
│  │  ├── JsonExpenseDataSource          → expenses.json                 │    │
│  │  └── JsonUtilityExpenseDataSource   → utility_expenses.json         │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↑
┌─────────────────────────────────────────────────────────────────────────────┐
│                         JSON FILES (composeResources/)                       │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  files/                                                              │    │
│  │  ├── transactions.json                                              │    │
│  │  ├── congregations.json                                             │    │
│  │  ├── donations.json                                                 │    │
│  │  ├── expenses.json                                                  │    │
│  │  └── utility_expenses.json                                          │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 10. Key Design Decisions

### Why JSON Files in Assets?

1. **Simplicity**: No database setup, no API server needed
2. **Fast Iteration**: Easy to modify JSON files during development
3. **Offline First**: Data is bundled with the app
4. **Easy Testing**: JSON files can be modified for different test scenarios
5. **Future Ready**: Architecture allows easy swap to API/DB later

### Why Multiple JSON Files?

1. **Separation of Concerns**: Each data type has its own file
2. **Smaller Loads**: Only load what you need
3. **Easier Maintenance**: Changes to one type don't affect others
4. **Clear Structure**: Mirrors the domain model organization

### Why DataSource Interfaces in Shared?

1. **Clean Architecture**: Domain doesn't know about JSON
2. **Testability**: Easy to mock DataSource for tests
3. **Swappability**: Change implementation without touching domain

---

**Document Version:** 1.1
**Author:** Claude Code
**Last Updated:** January 16, 2026

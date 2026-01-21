package com.asr.financial.di

import com.asr.financial.AppConfig
import com.asr.financial.data.datasource.AppConfigDataSource
import com.asr.financial.data.datasource.AsrExpenseDataSource
import com.asr.financial.data.datasource.CongregationDataSource
import com.asr.financial.data.datasource.FirebaseStorageAppConfigDataSource
import com.asr.financial.data.datasource.FirebaseStorageAsrExpenseDataSource
import com.asr.financial.data.datasource.FirebaseStorageCongregationDataSource
import com.asr.financial.data.datasource.FirebaseStorageTransactionDataSource
import com.asr.financial.data.datasource.JsonAppConfigDataSource
import com.asr.financial.data.datasource.JsonAsrExpenseDataSource
import com.asr.financial.data.datasource.JsonCongregationDataSource
import com.asr.financial.data.datasource.JsonDecontDataSource
import com.asr.financial.data.datasource.JsonTransactionDataSource
import com.asr.financial.data.datasource.DecontDataSource
import com.asr.financial.data.datasource.FirebaseStorageDecontDataSource
import com.asr.financial.data.datasource.FirebaseStorageSituatieCurentaAsrDataSource
import com.asr.financial.data.datasource.SituatieCurentaAsrDataSource
import com.asr.financial.data.datasource.TransactionDataSource
import com.asr.financial.data.repository.AppConfigRepositoryImpl
import com.asr.financial.data.repository.AsrExpenseRepositoryImpl
import com.asr.financial.data.repository.AuthRepositoryImpl
import com.asr.financial.data.repository.CongregationInfoRepositoryImpl
import com.asr.financial.data.repository.DecontRepositoryImpl
import com.asr.financial.data.repository.CurrentAsrSituationRepositoryImpl
import com.asr.financial.data.repository.TransactionRepositoryImpl
import com.asr.financial.domain.repository.AppConfigRepository
import com.asr.financial.domain.repository.AsrExpenseRepository
import com.asr.financial.domain.repository.AuthRepository
import com.asr.financial.domain.repository.CongregationInfoRepository
import com.asr.financial.domain.repository.DecontRepository
import com.asr.financial.domain.repository.CurrentAsrSituationRepository
import com.asr.financial.domain.repository.TransactionRepository
import com.asr.financial.platform.ResourceLoader
import org.koin.dsl.module

/**
 * Data module - provides data sources and repository implementations.
 * 
 * Data sources are conditionally provided based on AppConfig.USE_FIREBASE_STORAGE:
 * - If true: Uses Firebase Cloud Storage (production)
 * - If false: Uses local JSON files (testing)
 * 
 * ResourceLoader must be provided by the presentation layer (for JSON mode).
 */
val dataModule = module {
    // Data source implementations - conditional based on configuration
    if (AppConfig.USE_FIREBASE_STORAGE) {
        // Production: Firebase Cloud Storage
        single<TransactionDataSource> { FirebaseStorageTransactionDataSource(get()) }
        single<CongregationDataSource> { FirebaseStorageCongregationDataSource(get()) }
        single<AppConfigDataSource> { FirebaseStorageAppConfigDataSource(get(), get()) }
        single<AsrExpenseDataSource> { FirebaseStorageAsrExpenseDataSource(get()) }
        single<DecontDataSource> { FirebaseStorageDecontDataSource(get()) }
        single<SituatieCurentaAsrDataSource> { FirebaseStorageSituatieCurentaAsrDataSource(get()) }
    } else {
        // Testing: Local JSON files
        single<TransactionDataSource> { JsonTransactionDataSource(get()) }
        single<CongregationDataSource> { JsonCongregationDataSource(get()) }
        single<AppConfigDataSource> { JsonAppConfigDataSource(get(), get()) }
        single<AsrExpenseDataSource> { JsonAsrExpenseDataSource(get()) }
        single<DecontDataSource> { JsonDecontDataSource(get()) }
        // Note: SituatieCurentaAsrDataSource only available via Firebase Storage (no JSON fallback)
        single<SituatieCurentaAsrDataSource> { FirebaseStorageSituatieCurentaAsrDataSource(get()) }
    }
    
    // Repository implementations (unchanged - they use interfaces)
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CongregationInfoRepository> { CongregationInfoRepositoryImpl(get()) }
    single<AppConfigRepository> { AppConfigRepositoryImpl(get()) }
    single<AsrExpenseRepository> { AsrExpenseRepositoryImpl(get()) }
    single<DecontRepository> { DecontRepositoryImpl(get()) }
    single<CurrentAsrSituationRepository> { CurrentAsrSituationRepositoryImpl(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}

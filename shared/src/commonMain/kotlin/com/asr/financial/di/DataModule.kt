package com.asr.financial.di

import com.asr.financial.data.datasource.AppConfigDataSource
import com.asr.financial.data.datasource.CongregationDataSource
import com.asr.financial.data.datasource.JsonAppConfigDataSource
import com.asr.financial.data.datasource.JsonCongregationDataSource
import com.asr.financial.data.datasource.JsonTransactionDataSource
import com.asr.financial.data.datasource.TransactionDataSource
import com.asr.financial.data.repository.AppConfigRepositoryImpl
import com.asr.financial.data.repository.CongregationInfoRepositoryImpl
import com.asr.financial.data.repository.TransactionRepositoryImpl
import com.asr.financial.domain.repository.AppConfigRepository
import com.asr.financial.domain.repository.CongregationInfoRepository
import com.asr.financial.domain.repository.TransactionRepository
import com.asr.financial.platform.ResourceLoader
import org.koin.dsl.module

/**
 * Data module - provides data sources and repository implementations.
 * ResourceLoader must be provided by the presentation layer.
 */
val dataModule = module {
    // Data source implementations (ResourceLoader provided by composeApp)
    single<TransactionDataSource> { JsonTransactionDataSource(get()) }
    single<CongregationDataSource> { JsonCongregationDataSource(get()) }
    single<AppConfigDataSource> { JsonAppConfigDataSource(get(), get()) }
    
    // Repository implementations
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CongregationInfoRepository> { CongregationInfoRepositoryImpl(get()) }
    single<AppConfigRepository> { AppConfigRepositoryImpl(get()) }
}

package com.asr.financial.di

import com.asr.financial.data.repository.AppConfigRepositoryImpl
import com.asr.financial.data.repository.CongregationInfoRepositoryImpl
import com.asr.financial.data.repository.TransactionRepositoryImpl
import com.asr.financial.domain.repository.AppConfigRepository
import com.asr.financial.domain.repository.CongregationInfoRepository
import com.asr.financial.domain.repository.TransactionRepository
import org.koin.dsl.module

/**
 * Data module - provides repository implementations.
 * DataSources are provided by PresentationModule (composeApp).
 */
val dataModule = module {
    // Repository implementations - all delegate to their respective DataSources
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CongregationInfoRepository> { CongregationInfoRepositoryImpl(get()) }
    single<AppConfigRepository> { AppConfigRepositoryImpl(get()) }
}

package com.asr.financial.di

import com.asr.financial.data.repository.MockTransactionRepository
import com.asr.financial.domain.repository.TransactionRepository
import org.koin.dsl.module

/**
 * Data module
 * Provides database and repository implementations
 */
val dataModule = module {
    // Mock repositories (JsonLoader will be provided by platform module)
    single<TransactionRepository> { MockTransactionRepository(get()) }
}

package com.asr.financial.di

import com.asr.financial.domain.usecase.GetTransactionsByMonthUseCase
import com.asr.financial.domain.usecase.GetTransactionsUseCase
import org.koin.dsl.module

/**
 * Domain module
 * Provides use cases
 */
val domainModule = module {
    // Transaction use cases
    factory { GetTransactionsUseCase(get()) }
    factory { GetTransactionsByMonthUseCase(get()) }
}

package com.asr.financial.di

import com.asr.financial.data.datasource.*
import com.asr.financial.presentation.mvi.interactor.CongregationsInteractor
import com.asr.financial.presentation.mvi.interactor.ExpensesInteractor
import com.asr.financial.presentation.mvi.interactor.HomeInteractor
import com.asr.financial.presentation.mvi.viewmodel.CongregationsViewModel
import com.asr.financial.presentation.mvi.viewmodel.ExpensesViewModel
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
    single<AppConfigDataSource> { JsonAppConfigDataSource(get()) }

    // ═══════════════════════════════════════════════════════════════════
    // INTERACTORS
    // ═══════════════════════════════════════════════════════════════════
    factory { HomeInteractor(get(), get(), get(), get(), get()) }
    factory { CongregationsInteractor(get(), get()) }
    factory { ExpensesInteractor(get(), get()) }

    // ═══════════════════════════════════════════════════════════════════
    // VIEWMODELS
    // ═══════════════════════════════════════════════════════════════════
    viewModel { HomeViewModel(get()) }
    viewModel { CongregationsViewModel(get()) }
    viewModel { ExpensesViewModel(get()) }
}

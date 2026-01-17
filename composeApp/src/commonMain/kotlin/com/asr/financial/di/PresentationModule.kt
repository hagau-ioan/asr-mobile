package com.asr.financial.di

import com.asr.financial.platform.ComposeResourceLoader
import com.asr.financial.platform.ResourceLoader
import com.asr.financial.presentation.mvi.interactor.CalculatorInteractor
import com.asr.financial.presentation.mvi.interactor.CongregationsInteractor
import com.asr.financial.presentation.mvi.interactor.ExpensesInteractor
import com.asr.financial.presentation.mvi.interactor.HomeInteractor
import com.asr.financial.presentation.mvi.interactor.UploadInteractor
import com.asr.financial.presentation.mvi.interactor.UtilitiesInteractor
import com.asr.financial.presentation.mvi.interactor.YearlyInteractor
import com.asr.financial.presentation.mvi.viewmodel.CalculatorViewModel
import com.asr.financial.presentation.mvi.viewmodel.CongregationsViewModel
import com.asr.financial.presentation.mvi.viewmodel.ExpensesViewModel
import com.asr.financial.presentation.mvi.viewmodel.HomeViewModel
import com.asr.financial.presentation.mvi.viewmodel.UploadViewModel
import com.asr.financial.presentation.mvi.viewmodel.UtilitiesViewModel
import com.asr.financial.presentation.mvi.viewmodel.YearlyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Presentation module - provides resource loader, interactors and view models.
 */
val presentationModule = module {

    // ═══════════════════════════════════════════════════════════════════
    // RESOURCE LOADER - Compose Resources Implementation
    // ═══════════════════════════════════════════════════════════════════
    single<ResourceLoader> { ComposeResourceLoader() }

    // ═══════════════════════════════════════════════════════════════════
    // INTERACTORS
    // ═══════════════════════════════════════════════════════════════════
    factory { HomeInteractor(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { CongregationsInteractor(get(), get(), get(), get(), get()) }
    factory { ExpensesInteractor(get(), get(), get(), get()) }
    factory { UtilitiesInteractor(get(), get(), get(), get()) }
    factory { YearlyInteractor(get()) }
    factory { CalculatorInteractor(get(), get(), get(), get()) }
    factory { UploadInteractor(get()) }

    // ═══════════════════════════════════════════════════════════════════
    // VIEWMODELS
    // ═══════════════════════════════════════════════════════════════════
    viewModel { HomeViewModel(get()) }
    viewModel { CongregationsViewModel(get()) }
    viewModel { ExpensesViewModel(get()) }
    viewModel { UtilitiesViewModel(get()) }
    viewModel { YearlyViewModel(get()) }
    viewModel { CalculatorViewModel(get()) }
    viewModel { UploadViewModel(get()) }
}

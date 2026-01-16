package com.asr.financial.di

import com.asr.financial.data.ComposeJsonLoader
import com.asr.financial.data.repository.JsonLoader
import com.asr.financial.presentation.mvi.interactor.HomeInteractor
import com.asr.financial.presentation.mvi.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    // JSON Loader (platform-specific)
    single<JsonLoader> { ComposeJsonLoader() }
    
    // Interactors
    factory { HomeInteractor(get(), get()) }
    
    // ViewModels
    viewModel { HomeViewModel(get()) }
}

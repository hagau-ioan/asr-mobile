package com.asr.financial.di

import com.asr.financial.presentation.screens.details.DetailsViewModel
import com.asr.financial.presentation.screens.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    // ViewModels
    viewModelOf(::HomeViewModel)

    // DetailsViewModel with parameter
    viewModel { (itemId: String) ->
        DetailsViewModel(itemId = itemId)
    }
}

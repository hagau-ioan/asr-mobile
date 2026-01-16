package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.event.HomeEvent
import com.asr.financial.presentation.mvi.interactor.HomeInteractor
import kotlinx.coroutines.launch

/**
 * Home ViewModel - Delegates to Interactor
 * Follows MVI pattern from PreachTrack
 */
class HomeViewModel(
    private val homeInteractor: HomeInteractor
) : ViewModel() {

    val uiState = homeInteractor.uiState
    val uiEffect = homeInteractor.uiEffect

    fun handleEvent(event: HomeEvent) {
        viewModelScope.launch {
            homeInteractor.processEvent(event)
        }
    }

    init {
        // Load data on init
        handleEvent(HomeEvent.LoadData)
    }
}

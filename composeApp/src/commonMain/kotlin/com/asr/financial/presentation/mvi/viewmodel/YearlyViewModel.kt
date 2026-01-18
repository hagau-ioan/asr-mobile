package com.asr.financial.presentation.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asr.financial.presentation.mvi.effect.YearlyEffect
import com.asr.financial.presentation.mvi.event.YearlyEvent
import com.asr.financial.presentation.mvi.interactor.YearlyInteractor
import com.asr.financial.presentation.mvi.state.YearlyState
import kotlinx.coroutines.flow.Flow
import com.asr.financial.presentation.ui.constants.AppConstants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Yearly Comparison Screen
 */
class YearlyViewModel(
    private val interactor: YearlyInteractor
) : ViewModel() {

    val uiState: StateFlow<YearlyState> = interactor.uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConstants.Time.STATE_FLOW_TIMEOUT_MS),
        initialValue = YearlyState.Loading
    )

    val effect: Flow<YearlyEffect> = interactor.effect

    init {
        handleEvent(YearlyEvent.LoadData)
    }

    fun handleEvent(event: YearlyEvent) {
        viewModelScope.launch {
            interactor.handleEvent(event)
        }
    }
}

package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.usecase.GetDecontUseCase
import com.asr.financial.presentation.mvi.effect.DecontEffect
import com.asr.financial.presentation.mvi.event.DecontEvent
import com.asr.financial.presentation.mvi.state.DecontState
import com.asr.financial.presentation.ui.constants.AppConstants
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Interactor for Decont Screen
 */
class DecontInteractor(
    private val getDecontUseCase: GetDecontUseCase
) {
    private val _uiState = MutableStateFlow<DecontState>(DecontState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<DecontEffect>(Channel.BUFFERED)
    val uiEffect: Flow<DecontEffect> = _uiEffectChannel.receiveAsFlow()

    suspend fun processEvent(event: DecontEvent) {
        when (event) {
            is DecontEvent.LoadDecont -> loadDecont(event.year, event.month)
        }
    }

    private suspend fun loadDecont(year: Int, month: Int) {
        _uiState.emit(DecontState.Loading)
        try {
            val decont = getDecontUseCase(year, month)
            if (decont != null) {
                val totalAmount = decont.expenses.sumOf { it.amount }
                _uiState.emit(DecontState.Success(decont, totalAmount, year, month))
            } else {
                _uiState.emit(DecontState.Error(AppConstants.ErrorMessages.DATA_NOT_FOUND))
                _uiEffectChannel.send(DecontEffect.ShowToast(AppConstants.ErrorMessages.DATA_NOT_FOUND))
            }
        } catch (e: Exception) {
            _uiState.emit(DecontState.Error(e.message ?: AppConstants.ErrorMessages.UNKNOWN_ERROR))
            _uiEffectChannel.send(DecontEffect.ShowToast(AppConstants.ErrorMessages.FAILED_TO_LOAD_DATA))
        }
    }
}

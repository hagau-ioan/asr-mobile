package com.asr.financial.presentation.mvi.state

import com.asr.financial.domain.models.Decont

/**
 * UI State for Decont Screen
 */
sealed interface DecontState {
    data object Loading : DecontState
    data class Success(
        val decont: Decont,
        val totalAmount: Double,
        val year: Int,
        val month: Int
    ) : DecontState
    data class Error(val message: String) : DecontState
}

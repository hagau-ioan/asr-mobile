package com.asr.financial.presentation.mvi.event

/**
 * UI Events for Decont Screen
 */
sealed interface DecontEvent {
    data class LoadDecont(val year: Int, val month: Int) : DecontEvent
}

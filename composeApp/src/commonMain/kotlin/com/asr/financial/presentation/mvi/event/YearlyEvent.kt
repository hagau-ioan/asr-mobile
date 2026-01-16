package com.asr.financial.presentation.mvi.event

/**
 * User events for Yearly Comparison Screen
 */
sealed class YearlyEvent {
    data object LoadData : YearlyEvent()
}

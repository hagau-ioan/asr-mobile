package com.asr.financial.presentation.mvi.state

import com.asr.financial.presentation.screens.congregations.CongregationStat

/**
 * UI State for Congregations Screen
 */
sealed interface CongregationsState {
    data object Loading : CongregationsState

    data class Success(
        val stats: List<CongregationStat>,
        val totalDonated: Double,
        val totalExpected: Double,
        val totalDifference: Double,
        val missingCount: Int,
        val selectedYear: Int,
        val selectedMonth: Int
    ) : CongregationsState

    data class Error(val message: String) : CongregationsState
}

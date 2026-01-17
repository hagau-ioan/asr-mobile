package com.asr.financial.presentation.mvi.state

import com.asr.financial.presentation.screens.utilities.UtilityComparison
import com.asr.financial.presentation.screens.utilities.YearlyUtilityData

/**
 * UI State for Utilities Screen
 */
sealed interface UtilitiesState {
    data object Loading : UtilitiesState
    data class Success(
        val comparisons: List<UtilityComparison>,
        val currentTotal: Double,
        val previousTotal: Double,
        val totalDifference: Double,
        val totalPercentage: Double,
        val yearlyData: List<YearlyUtilityData>,
        val comparisonYearlyData: List<YearlyUtilityData>,
        val selectedYear: Int,
        val selectedMonth: Int,
        val previousMonth: Int,
        val previousYear: Int,
        val comparisonYear: Int,
        val availableYears: List<Int>,
        val isRefreshing: Boolean = false
    ) : UtilitiesState
    data class Error(val message: String) : UtilitiesState
}

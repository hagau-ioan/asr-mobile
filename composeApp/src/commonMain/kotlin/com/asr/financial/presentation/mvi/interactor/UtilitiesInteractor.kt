package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.model.Transaction
import com.asr.financial.domain.model.TransactionType
import com.asr.financial.domain.usecase.GetAvailableYearsUseCase
import com.asr.financial.domain.usecase.GetTransactionsUseCase
import com.asr.financial.domain.usecase.RefreshDataUseCase
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.effect.UtilitiesEffect
import com.asr.financial.presentation.mvi.event.UtilitiesEvent
import com.asr.financial.presentation.mvi.state.UtilitiesState
import com.asr.financial.presentation.screens.utilities.UtilityComparison
import com.asr.financial.presentation.screens.utilities.YearlyUtilityData
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.percentOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class UtilitiesInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getAvailableYearsUseCase: com.asr.financial.domain.usecase.GetAvailableYearsUseCase,
    private val refreshDataUseCase: com.asr.financial.domain.usecase.RefreshDataUseCase,
    private val clock: Clock
) {
    private val _uiState = MutableStateFlow<UtilitiesState>(UtilitiesState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<UtilitiesEffect>(Channel.UNLIMITED)
    val uiEffect: Flow<UtilitiesEffect> = _uiEffectChannel.receiveAsFlow()

    private var currentYear: Int
    private var currentMonth: Int
    private var comparisonYear: Int
    private var cachedTransactions: List<Transaction> = emptyList()

    init {
        val (prevMonth, prevYear) = calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
        currentYear = prevYear
        currentMonth = prevMonth
        comparisonYear = prevYear
    }

    suspend fun processEvent(event: UtilitiesEvent) {
        when (event) {
            is UtilitiesEvent.LoadData -> loadData(currentYear, currentMonth)
            is UtilitiesEvent.FilterByPeriod -> filterByPeriod(event.year, event.month)
            is UtilitiesEvent.ChangeComparisonYear -> changeComparisonYear(event.year)
            is UtilitiesEvent.Refresh -> refreshData()
        }
    }

    private suspend fun loadData(year: Int, month: Int) {
        _uiState.emit(UtilitiesState.Loading)
        try {
            cachedTransactions = getTransactionsUseCase()
            val (prevMonth, prevYear) = calculatePreviousMonth(month, year)
            val comparisons = calculateComparisons(year, month, prevYear, prevMonth)
            val yearlyData = calculateYearlyData(year)
            val comparisonYearlyData = calculateYearlyData(comparisonYear)
            val availableYears = getAvailableYearsUseCase()
            
            val currentTotal = comparisons.sumOf { it.currentAmount }
            val previousTotal = comparisons.sumOf { it.previousAmount }
            val totalDifference = currentTotal - previousTotal
            val totalPercentage = totalDifference.percentOf(previousTotal)

            // Always emit Success, even with empty data
            _uiState.emit(
                UtilitiesState.Success(
                    comparisons = comparisons,
                    currentTotal = currentTotal,
                    previousTotal = previousTotal,
                    totalDifference = totalDifference,
                    totalPercentage = totalPercentage,
                    yearlyData = yearlyData,
                    comparisonYearlyData = comparisonYearlyData,
                    selectedYear = year,
                    selectedMonth = month,
                    previousMonth = prevMonth,
                    previousYear = prevYear,
                    comparisonYear = comparisonYear,
                    availableYears = availableYears
                )
            )
        } catch (e: Exception) {
            _uiState.emit(UtilitiesState.Error(e.message ?: "Unknown error"))
            _uiEffectChannel.send(UtilitiesEffect.ShowError("Failed to load utilities"))
        }
    }

    private suspend fun filterByPeriod(year: Int, month: Int) {
        currentYear = year
        currentMonth = month
        comparisonYear = year
        
        // Use cached data for filtering
        try {
            val (prevMonth, prevYear) = calculatePreviousMonth(month, year)
            val comparisons = calculateComparisons(year, month, prevYear, prevMonth)
            val yearlyData = calculateYearlyData(year)
            val comparisonYearlyData = calculateYearlyData(comparisonYear)
            val availableYears = getAvailableYearsUseCase()
            
            val currentTotal = comparisons.sumOf { it.currentAmount }
            val previousTotal = comparisons.sumOf { it.previousAmount }
            val totalDifference = currentTotal - previousTotal
            val totalPercentage = totalDifference.percentOf(previousTotal)

            _uiState.emit(
                UtilitiesState.Success(
                    comparisons = comparisons,
                    currentTotal = currentTotal,
                    previousTotal = previousTotal,
                    totalDifference = totalDifference,
                    totalPercentage = totalPercentage,
                    yearlyData = yearlyData,
                    comparisonYearlyData = comparisonYearlyData,
                    selectedYear = year,
                    selectedMonth = month,
                    previousMonth = prevMonth,
                    previousYear = prevYear,
                    comparisonYear = comparisonYear,
                    availableYears = availableYears
                )
            )
        } catch (e: Exception) {
            _uiState.emit(UtilitiesState.Error(e.message ?: "Unknown error"))
        }
    }

    private suspend fun changeComparisonYear(year: Int) {
        comparisonYear = year
        
        // Use cached data
        try {
            val (prevMonth, prevYear) = calculatePreviousMonth(currentMonth, currentYear)
            val comparisons = calculateComparisons(currentYear, currentMonth, prevYear, prevMonth)
            val yearlyData = calculateYearlyData(currentYear)
            val comparisonYearlyData = calculateYearlyData(comparisonYear)
            val availableYears = getAvailableYearsUseCase()
            
            val currentTotal = comparisons.sumOf { it.currentAmount }
            val previousTotal = comparisons.sumOf { it.previousAmount }
            val totalDifference = currentTotal - previousTotal
            val totalPercentage = totalDifference.percentOf(previousTotal)

            _uiState.emit(
                UtilitiesState.Success(
                    comparisons = comparisons,
                    currentTotal = currentTotal,
                    previousTotal = previousTotal,
                    totalDifference = totalDifference,
                    totalPercentage = totalPercentage,
                    yearlyData = yearlyData,
                    comparisonYearlyData = comparisonYearlyData,
                    selectedYear = currentYear,
                    selectedMonth = currentMonth,
                    previousMonth = prevMonth,
                    previousYear = prevYear,
                    comparisonYear = comparisonYear,
                    availableYears = availableYears
                )
            )
        } catch (e: Exception) {
            _uiState.emit(UtilitiesState.Error(e.message ?: "Unknown error"))
        }
    }

    private suspend fun refreshData() {
        val currentState = _uiState.value
        if (currentState is UtilitiesState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        }
        
        refreshDataUseCase()
        cachedTransactions = getTransactionsUseCase()
        val (prevMonth, prevYear) = calculatePreviousMonth(currentMonth, currentYear)
        val comparisons = calculateComparisons(currentYear, currentMonth, prevYear, prevMonth)
        val yearlyData = calculateYearlyData(currentYear)
        val comparisonYearlyData = calculateYearlyData(comparisonYear)
        val availableYears = getAvailableYearsUseCase()
        
        val currentTotal = comparisons.sumOf { it.currentAmount }
        val previousTotal = comparisons.sumOf { it.previousAmount }
        val totalDifference = currentTotal - previousTotal
        val totalPercentage = totalDifference.percentOf(previousTotal)

        _uiState.emit(
            UtilitiesState.Success(
                comparisons = comparisons,
                currentTotal = currentTotal,
                previousTotal = previousTotal,
                totalDifference = totalDifference,
                totalPercentage = totalPercentage,
                yearlyData = yearlyData,
                comparisonYearlyData = comparisonYearlyData,
                selectedYear = currentYear,
                selectedMonth = currentMonth,
                previousMonth = prevMonth,
                previousYear = prevYear,
                comparisonYear = comparisonYear,
                availableYears = availableYears
            )
        )
    }

    private suspend fun calculateComparisons(
        year: Int,
        month: Int,
        prevYear: Int,
        prevMonth: Int
    ): List<UtilityComparison> {
        val currentExpenses = cachedTransactions.filter {
            it.type == TransactionType.EXPENSE &&
            it.getYear() == year &&
            it.getMonth() == month
        }

        val previousExpenses = cachedTransactions.filter {
            it.type == TransactionType.EXPENSE &&
            it.getYear() == prevYear &&
            it.getMonth() == prevMonth
        }

        val allCategories = (currentExpenses.map { it.category } + previousExpenses.map { it.category }).distinct()

        return allCategories.map { category ->
            val currentAmount = currentExpenses
                .filter { it.category == category }
                .sumOf { it.amount }
            
            val previousAmount = previousExpenses
                .filter { it.category == category }
                .sumOf { it.amount }
            
            val difference = currentAmount - previousAmount
            val percentageChange = difference.percentOf(previousAmount)

            UtilityComparison(
                category = category,
                currentAmount = currentAmount,
                previousAmount = previousAmount,
                difference = difference,
                percentageChange = percentageChange
            )
        }
    }

    private suspend fun calculateYearlyData(year: Int): List<YearlyUtilityData> {
        return (1..12).map { month ->
            val currentYearAmount = cachedTransactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                    it.getYear() == year &&
                    it.getMonth() == month
                }
                .sumOf { it.amount }

            val previousYearAmount = cachedTransactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                    it.getYear() == year - 1 &&
                    it.getMonth() == month
                }
                .sumOf { it.amount }

            YearlyUtilityData(
                month = month,
                currentYearAmount = currentYearAmount,
                previousYearAmount = previousYearAmount
            )
        }
    }
}

package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.domain.usecase.GetAsrExpensesUseCase
import com.asr.financial.domain.usecase.GetDecontUseCase
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.effect.AsrExpensesEffect
import com.asr.financial.presentation.mvi.event.AsrExpensesEvent
import com.asr.financial.presentation.mvi.state.AsrExpensesState
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.parseDateComponentsSafe
import kotlin.concurrent.Volatile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Interactor for ASR Expenses Screen
 */
class AsrExpensesInteractor(
    private val getAsrExpensesUseCase: GetAsrExpensesUseCase,
    private val getDecontUseCase: GetDecontUseCase,
    private val clock: Clock
) {
    private val _uiState = MutableStateFlow<AsrExpensesState>(AsrExpensesState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<AsrExpensesEffect>(Channel.BUFFERED)
    val uiEffect: Flow<AsrExpensesEffect> = _uiEffectChannel.receiveAsFlow()

    @Volatile
    private var allExpenses: List<AsrExpense> = emptyList()

    suspend fun processEvent(event: AsrExpensesEvent) {
        when (event) {
            is AsrExpensesEvent.LoadData -> loadData()
            is AsrExpensesEvent.FilterByPeriod -> filterByPeriod(event.year, event.month)
        }
    }

    private suspend fun loadData() {
        _uiState.emit(AsrExpensesState.Loading)
        try {
            allExpenses = getAsrExpensesUseCase().sortedByDescending { it.date }
            
            // Find the last month with data from the last 12 months
            val (defaultYear, defaultMonth) = findLastMonthWithDataFromLast12Months(allExpenses)
            
            // Calculate available years only from expenses in the last 12 months
            val availableYears = getAvailableYearsFromLast12Months(allExpenses)
            
            // Filter expenses by the default month/year
            val filtered = allExpenses.filter { expense ->
                val dateComponents = parseDateComponentsSafe(expense.date)
                if (dateComponents == null) {
                    false
                } else {
                    val (expenseYear, expenseMonth, _) = dateComponents
                    (expenseYear == defaultYear) && (expenseMonth == defaultMonth)
                }
            }
            
            emitSuccessState(filtered, defaultYear, defaultMonth, availableYears)
        } catch (e: Exception) {
            _uiState.emit(AsrExpensesState.Error(e.message ?: com.asr.financial.presentation.ui.constants.AppConstants.ErrorMessages.UNKNOWN_ERROR))
            _uiEffectChannel.send(AsrExpensesEffect.ShowToast(com.asr.financial.presentation.ui.constants.AppConstants.ErrorMessages.FAILED_TO_LOAD_DATA))
        }
    }

    /**
     * Get available years from expenses in the last 12 months only.
     * Returns a sorted list of years (descending) that have data in the last 12 months.
     */
    private fun getAvailableYearsFromLast12Months(expenses: List<AsrExpense>): List<Int> {
        val currentYear = getCurrentYear(clock)
        val currentMonth = getCurrentMonth(clock)
        
        // Calculate the range of months to check (last 12 months)
        val monthsToCheck = mutableSetOf<Pair<Int, Int>>() // (year, month)
        for (i in 0 until 12) {
            var year = currentYear
            var month = currentMonth - i
            
            while (month <= 0) {
                month += 12
                year -= 1
            }
            
            monthsToCheck.add(year to month)
        }
        
        // Find expenses within the last 12 months and extract unique years
        return expenses
            .mapNotNull { expense ->
                val dateComponents = parseDateComponentsSafe(expense.date)
                if (dateComponents != null) {
                    val (year, month, _) = dateComponents
                    if (monthsToCheck.contains(year to month)) {
                        year
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            .distinct()
            .sortedDescending()
    }

    /**
     * Find the last month (most recent) that has data from the last 12 months.
     * Returns the year and month (1-12) of the most recent month with data.
     * If no data is found in the last 12 months, returns the most recent year/month from all expenses.
     */
    private fun findLastMonthWithDataFromLast12Months(expenses: List<AsrExpense>): Pair<Int, Int> {
        val currentYear = getCurrentYear(clock)
        val currentMonth = getCurrentMonth(clock)
        
        // Calculate the range of months to check (last 12 months)
        val monthsToCheck = mutableSetOf<Pair<Int, Int>>() // (year, month)
        for (i in 0 until 12) {
            var year = currentYear
            var month = currentMonth - i
            
            while (month <= 0) {
                month += 12
                year -= 1
            }
            
            monthsToCheck.add(year to month)
        }
        
        // Find expenses within the last 12 months and group by year/month
        val expensesByMonth = expenses
            .mapNotNull { expense ->
                val dateComponents = parseDateComponentsSafe(expense.date)
                if (dateComponents != null) {
                    val (year, month, _) = dateComponents
                    if (monthsToCheck.contains(year to month)) {
                        year to month
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            .distinct()
            .sortedWith(compareByDescending<Pair<Int, Int>> { it.first }.thenByDescending { it.second })
        
        // Return the most recent month with data from last 12 months
        if (expensesByMonth.isNotEmpty()) {
            return expensesByMonth.first()
        }
        
        // Fallback: if no data in last 12 months, return the most recent month from all expenses
        val allMonths = expenses
            .mapNotNull { expense ->
                val dateComponents = parseDateComponentsSafe(expense.date)
                if (dateComponents != null) {
                    val (year, month, _) = dateComponents
                    year to month
                } else {
                    null
                }
            }
            .distinct()
            .sortedWith(compareByDescending<Pair<Int, Int>> { it.first }.thenByDescending { it.second })
        
        return allMonths.firstOrNull() 
            ?: (com.asr.financial.presentation.ui.constants.AppConstants.Business.DEFAULT_START_YEAR to 1)
    }

    private suspend fun filterByPeriod(year: Int?, month: Int?) {
        val currentState = _uiState.value as? AsrExpensesState.Success ?: return
        
        val selectedYear = year ?: currentState.selectedYear
        val selectedMonth = month ?: currentState.selectedMonth
        
        val filtered = allExpenses.filter { expense ->
            val dateComponents = parseDateComponentsSafe(expense.date)
            if (dateComponents == null) {
                false
            } else {
                val (expenseYear, expenseMonth, _) = dateComponents
                (expenseYear == selectedYear) && (selectedMonth == 0 || expenseMonth == selectedMonth)
            }
        }
        emitSuccessState(filtered, selectedYear, selectedMonth, currentState.availableYears)
    }

    private suspend fun emitSuccessState(
        expenses: List<AsrExpense>,
        year: Int,
        month: Int,
        availableYears: List<Int>
    ) {
        val totalAmount = expenses.sumOf { it.amount }
        
        // Calculate which expenses have actual decont data (with products)
        // Only mark as decont if both: 1) text contains "Decont" AND 2) decont data exists with products
        val expensesWithDecontData = expenses.map { expense ->
            val hasDecontText = expense.description.contains("Decont", ignoreCase = true) ||
                               expense.category.contains("Decont", ignoreCase = true)
            
            if (hasDecontText) {
                // Parse date to get year and month
                val dateComponents = parseDateComponentsSafe(expense.date)
                if (dateComponents != null) {
                    val (expenseYear, expenseMonth, _) = dateComponents
                    // Check if decont data exists for this month/year and has products
                    val decont = getDecontUseCase(expenseYear, expenseMonth)
                    val hasDecontData = decont != null && decont.expenses.any { it.product_names.isNotEmpty() }
                    expense to hasDecontData
                } else {
                    expense to false
                }
            } else {
                expense to false
            }
        }
        
        // Create a set of (year, month) pairs that have decont data with products
        val monthsWithDecontData = expensesWithDecontData
            .filter { it.second }
            .mapNotNull { (expense, _) ->
                val dateComponents = parseDateComponentsSafe(expense.date)
                if (dateComponents != null) {
                    val (year, month, _) = dateComponents
                    year to month
                } else {
                    null
                }
            }
            .toSet()
        
        _uiState.emit(AsrExpensesState.Success(expenses, totalAmount, year, month, availableYears, monthsWithDecontData))
    }
}

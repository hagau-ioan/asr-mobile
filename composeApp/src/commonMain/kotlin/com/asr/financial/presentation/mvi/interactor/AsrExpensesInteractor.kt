package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.domain.usecase.GetAsrExpensesUseCase
import com.asr.financial.presentation.mvi.effect.AsrExpensesEffect
import com.asr.financial.presentation.mvi.event.AsrExpensesEvent
import com.asr.financial.presentation.mvi.state.AsrExpensesState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Interactor for ASR Expenses Screen
 */
class AsrExpensesInteractor(
    private val getAsrExpensesUseCase: GetAsrExpensesUseCase
) {
    private val _uiState = MutableStateFlow<AsrExpensesState>(AsrExpensesState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<AsrExpensesEffect>(Channel.UNLIMITED)
    val uiEffect: Flow<AsrExpensesEffect> = _uiEffectChannel.receiveAsFlow()

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
            val availableYears = allExpenses.map { it.date.substring(0, 4).toInt() }.distinct().sortedDescending()
            val defaultYear = availableYears.firstOrNull() ?: 2024
            emitSuccessState(allExpenses, defaultYear, 0, availableYears)
        } catch (e: Exception) {
            _uiState.emit(AsrExpensesState.Error(e.message ?: "Unknown error"))
            _uiEffectChannel.send(AsrExpensesEffect.ShowToast("Failed to load ASR expenses"))
        }
    }

    private suspend fun filterByPeriod(year: Int?, month: Int?) {
        val currentState = _uiState.value as? AsrExpensesState.Success ?: return
        
        val selectedYear = year ?: currentState.selectedYear
        val selectedMonth = month ?: currentState.selectedMonth
        
        val filtered = allExpenses.filter { expense ->
            val parts = expense.date.split("-")
            val expenseYear = parts[0].toInt()
            val expenseMonth = parts[1].toInt()
            
            (expenseYear == selectedYear) && (selectedMonth == 0 || expenseMonth == selectedMonth)
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
        _uiState.emit(AsrExpensesState.Success(expenses, totalAmount, year, month, availableYears))
    }
}

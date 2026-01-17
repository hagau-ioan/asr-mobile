package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.model.Transaction
import com.asr.financial.domain.model.TransactionType
import com.asr.financial.domain.usecase.GetAvailableYearsUseCase
import com.asr.financial.domain.usecase.GetTransactionsUseCase
import com.asr.financial.domain.usecase.RefreshDataUseCase
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.effect.ExpensesEffect
import com.asr.financial.presentation.mvi.event.ExpensesEvent
import com.asr.financial.presentation.mvi.state.ExpensesState
import com.asr.financial.presentation.screens.expenses.ExpenseStat
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.isWithinLastNMonths
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class ExpensesInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getAvailableYearsUseCase: com.asr.financial.domain.usecase.GetAvailableYearsUseCase,
    private val refreshDataUseCase: com.asr.financial.domain.usecase.RefreshDataUseCase,
    private val clock: Clock
) {
    private val _uiState = MutableStateFlow<ExpensesState>(ExpensesState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<ExpensesEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ExpensesEffect> = _uiEffectChannel.receiveAsFlow()

    @Volatile
    private var currentYear: Int
    @Volatile
    private var currentMonth: Int
    @Volatile
    private var cachedTransactions: List<Transaction> = emptyList()

    init {
        val (prevMonth, prevYear) = calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
        currentYear = prevYear
        currentMonth = prevMonth
    }

    suspend fun processEvent(event: ExpensesEvent) {
        when (event) {
            is ExpensesEvent.LoadData -> loadData(currentYear, currentMonth)
            is ExpensesEvent.FilterByPeriod -> filterByPeriod(event.year, event.month)
            is ExpensesEvent.Refresh -> refreshData()
        }
    }

    private suspend fun loadData(year: Int, month: Int) {
        _uiState.emit(ExpensesState.Loading)
        try {
            cachedTransactions = getTransactionsUseCase()
            val expenses = calculateExpenses(year, month)
            emitSuccessState(expenses, year, month)
        } catch (e: Exception) {
            _uiState.emit(ExpensesState.Error(e.message ?: "Unknown error"))
            _uiEffectChannel.send(ExpensesEffect.ShowToast("Failed to load expenses"))
        }
    }

    private suspend fun filterByPeriod(year: Int, month: Int) {
        currentYear = year
        currentMonth = month
        
        // Use cached data for filtering
        try {
            val expenses = calculateExpenses(year, month)
            emitSuccessState(expenses, year, month)
        } catch (e: Exception) {
            _uiState.emit(ExpensesState.Error(e.message ?: "Unknown error"))
        }
    }

    private suspend fun refreshData() {
        val currentState = _uiState.value
        if (currentState is ExpensesState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        }
        
        refreshDataUseCase()
        cachedTransactions = getTransactionsUseCase()
        val expenses = calculateExpenses(currentYear, currentMonth)
        emitSuccessState(expenses, currentYear, currentMonth)
    }

    private suspend fun calculateExpenses(year: Int, month: Int): List<ExpenseStat> {
        val filteredExpenses = cachedTransactions.filter {
            it.type == TransactionType.EXPENSE &&
            it.getYear() == year &&
            it.getMonth() == month
        }

        return filteredExpenses
            .groupBy { it.category }
            .map { (category, txns) ->
                ExpenseStat(
                    category = category,
                    amount = txns.sumOf { it.amount },
                    count = txns.size,
                    transactions = txns.sortedByDescending { it.date }
                )
            }
            .sortedByDescending { it.amount }
    }

    private suspend fun emitSuccessState(expenses: List<ExpenseStat>, year: Int, month: Int) {
        val totalExpenses = expenses.sumOf { it.amount }
        val availableYears = getAvailableYearsUseCase()
        
        // Calculate last 12 months total ending at previous month
        val currentYear = getCurrentYear(clock)
        val currentMonth = getCurrentMonth(clock)
        
        // Calculate previous month using utility
        val (endMonth, endYear) = calculatePreviousMonth(currentMonth, currentYear)
        
        val transactions = getTransactionsUseCase()
        val yearlyTotal = transactions
            .filter { transaction ->
                if (transaction.type != TransactionType.EXPENSE) return@filter false
                
                val txnYear = transaction.getYear()
                val txnMonth = transaction.getMonth()
                
                // Use utility to check if within last 12 months
                isWithinLastNMonths(
                    targetMonth = txnMonth,
                    targetYear = txnYear,
                    referenceMonth = endMonth,
                    referenceYear = endYear,
                    monthsBack = 12
                )
            }
            .sumOf { it.amount }

        _uiState.emit(
            ExpensesState.Success(
                expenses = expenses,
                totalExpenses = totalExpenses,
                yearlyTotal = yearlyTotal,
                yearlyEndMonth = endMonth,
                yearlyEndYear = endYear,
                selectedYear = year,
                selectedMonth = month,
                availableYears = availableYears
            )
        )
    }
}

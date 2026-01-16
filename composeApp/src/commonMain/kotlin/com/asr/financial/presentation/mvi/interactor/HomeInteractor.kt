package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.model.TransactionType
import com.asr.financial.domain.usecase.GetTransactionsByMonthUseCase
import com.asr.financial.domain.usecase.GetTransactionsUseCase
import com.asr.financial.presentation.mvi.effect.HomeEffect
import com.asr.financial.presentation.mvi.event.HomeEvent
import com.asr.financial.presentation.mvi.state.HomeState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Home Interactor - Handles business logic for Home screen
 * Follows MVI pattern from PreachTrack
 */
class HomeInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionsByMonthUseCase: GetTransactionsByMonthUseCase
) {
    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<HomeEffect>(Channel.UNLIMITED)
    val uiEffect: Flow<HomeEffect> = _uiEffectChannel.receiveAsFlow()

    suspend fun processEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadData -> loadTransactions()
            is HomeEvent.FilterByMonth -> filterByMonth(event.month, event.year)
            is HomeEvent.NavigateToDetails -> navigateToDetails(event.transactionId)
            is HomeEvent.Refresh -> refreshData()
        }
    }

    private suspend fun loadTransactions() {
        _uiState.emit(HomeState.Loading)
        try {
            val transactions = getTransactionsUseCase()
            val totalIncome = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val totalExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val balance = totalIncome - totalExpenses

            _uiState.emit(
                HomeState.Success(
                    transactions = transactions,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    balance = balance
                )
            )
        } catch (e: Exception) {
            _uiState.emit(HomeState.Error(e.message ?: "Unknown error"))
            _uiEffectChannel.send(HomeEffect.ShowToast("Failed to load transactions"))
        }
    }

    private suspend fun filterByMonth(month: Int, year: Int) {
        _uiState.emit(HomeState.Loading)
        try {
            val transactions = getTransactionsByMonthUseCase(month, year)
            val totalIncome = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val totalExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val balance = totalIncome - totalExpenses

            _uiState.emit(
                HomeState.Success(
                    transactions = transactions,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    balance = balance
                )
            )
        } catch (e: Exception) {
            _uiState.emit(HomeState.Error(e.message ?: "Unknown error"))
        }
    }

    private suspend fun navigateToDetails(transactionId: String) {
        _uiEffectChannel.send(HomeEffect.NavigateToDetails(transactionId))
    }

    private suspend fun refreshData() {
        loadTransactions()
        _uiEffectChannel.send(HomeEffect.ScrollToTop)
    }
}

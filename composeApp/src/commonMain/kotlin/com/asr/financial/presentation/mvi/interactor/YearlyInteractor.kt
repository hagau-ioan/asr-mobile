package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.models.TransactionType
import com.asr.financial.domain.usecase.GetTransactionsUseCase
import com.asr.financial.presentation.mvi.effect.YearlyEffect
import com.asr.financial.presentation.mvi.event.YearlyEvent
import com.asr.financial.presentation.mvi.state.YearlyState
import com.asr.financial.presentation.screens.yearly.YearlyComparison
import com.asr.financial.presentation.screens.yearly.YearlyStat
import com.asr.financial.utils.percentOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Interactor for Yearly Comparison Screen
 * Handles business logic for year-over-year financial comparison
 */
class YearlyInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) {
    private val _uiState = MutableStateFlow<YearlyState>(YearlyState.Loading)
    val uiState: Flow<YearlyState> = _uiState.asStateFlow()

    private val _effect = Channel<YearlyEffect>(Channel.BUFFERED)
    val effect: Flow<YearlyEffect> = _effect.receiveAsFlow()

    suspend fun handleEvent(event: YearlyEvent) {
        when (event) {
            is YearlyEvent.LoadData -> loadData()
        }
    }

    private suspend fun loadData() {
        try {
            _uiState.emit(YearlyState.Loading)

            val transactions = getTransactionsUseCase()
            
            // Group by year
            val yearlyData = transactions
                .groupBy { transaction ->
                    transaction.date.split("-").firstOrNull()?.toIntOrNull() ?: 0
                }
                .filter { (year, _) -> year > 0 }
                .map { (year, yearTransactions) ->
                    val donations = yearTransactions
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                    
                    val expenses = yearTransactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount }
                    
                    YearlyStat(
                        year = year,
                        totalDonations = donations,
                        totalExpenses = expenses,
                        balance = donations - expenses
                    )
                }
                .sortedByDescending { it.year }

            // Calculate year-over-year comparisons (DESC order: newest first)
            val comparisons = yearlyData.mapIndexedNotNull { index, current ->
                if (index == yearlyData.size - 1) return@mapIndexedNotNull null
                
                val previous = yearlyData[index + 1]
                
                YearlyComparison(
                    currentYear = current.year,
                    previousYear = previous.year,
                    donationsChange = current.totalDonations - previous.totalDonations,
                    donationsChangePercent = (current.totalDonations - previous.totalDonations)
                        .percentOf(previous.totalDonations),
                    expensesChange = current.totalExpenses - previous.totalExpenses,
                    expensesChangePercent = (current.totalExpenses - previous.totalExpenses)
                        .percentOf(previous.totalExpenses)
                )
            }

            _uiState.emit(
                YearlyState.Success(
                    yearlyStats = yearlyData,
                    comparisons = comparisons
                )
            )
        } catch (e: Exception) {
            val errorMessage = e.message ?: com.asr.financial.presentation.ui.constants.AppConstants.ErrorMessages.UNKNOWN_ERROR
            _uiState.emit(YearlyState.Error(errorMessage))
            _effect.send(YearlyEffect.ShowError(errorMessage))
        }
    }
}

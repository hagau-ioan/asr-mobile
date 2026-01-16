package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.model.TransactionType
import com.asr.financial.domain.usecase.GetAllCongregationsUseCase
import com.asr.financial.domain.usecase.GetTransactionsUseCase
import com.asr.financial.presentation.mvi.effect.CalculatorEffect
import com.asr.financial.presentation.mvi.event.CalculatorEvent
import com.asr.financial.presentation.mvi.state.CalculatorState
import com.asr.financial.presentation.screens.calculator.CongregationContribution
import com.asr.financial.presentation.screens.calculator.ContributionCalculation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Interactor for Calculator Screen
 * Handles business logic for contribution calculations
 */
class CalculatorInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getAllCongregationsUseCase: GetAllCongregationsUseCase
) {
    private val _uiState = MutableStateFlow<CalculatorState>(CalculatorState.Loading)
    val uiState: Flow<CalculatorState> = _uiState.asStateFlow()

    private val _effect = Channel<CalculatorEffect>(Channel.BUFFERED)
    val effect: Flow<CalculatorEffect> = _effect.receiveAsFlow()

    suspend fun handleEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.LoadData -> loadData(event.year, event.month)
            is CalculatorEvent.FilterByPeriod -> loadData(event.year, event.month)
        }
    }

    private suspend fun loadData(year: Int, month: Int) {
        try {
            _uiState.emit(CalculatorState.Loading)

            val transactions = getTransactionsUseCase()
            val congregations = getAllCongregationsUseCase()

            // Calculate monthly expenses
            val monthExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .filter { 
                    val parts = it.date.split("-")
                    parts.size >= 2 && parts[0].toIntOrNull() == year && parts[1].toIntOrNull() == month
                }
                .sumOf { it.amount }

            // Calculate yearly expenses
            val yearExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .filter { 
                    val parts = it.date.split("-")
                    parts.isNotEmpty() && parts[0].toIntOrNull() == year
                }
                .sumOf { it.amount }

            // Total publishers
            val totalPublishers = congregations.sumOf { it.memberCount }

            if (totalPublishers == 0) {
                _uiState.emit(CalculatorState.Error("Nu există vestitori înregistrați"))
                return
            }

            // Calculate contributions
            val monthlyContribution = ContributionCalculation(
                totalExpenses = monthExpenses,
                numberOfPublishers = totalPublishers,
                perPublisherAmount = monthExpenses / totalPublishers
            )

            val yearlyContribution = ContributionCalculation(
                totalExpenses = yearExpenses,
                numberOfPublishers = totalPublishers,
                perPublisherAmount = yearExpenses / totalPublishers
            )

            // Calculate per congregation
            val congregationContributions = congregations.map { congregation ->
                CongregationContribution(
                    congregationId = congregation.id,
                    congregationName = congregation.name,
                    numberOfPublishers = congregation.memberCount,
                    perPublisherAmount = monthlyContribution.perPublisherAmount,
                    totalAmount = monthlyContribution.perPublisherAmount * congregation.memberCount
                )
            }.sortedBy { it.congregationName }

            _uiState.emit(
                CalculatorState.Success(
                    monthlyContribution = monthlyContribution,
                    yearlyContribution = yearlyContribution,
                    congregationContributions = congregationContributions
                )
            )
        } catch (e: Exception) {
            _uiState.emit(CalculatorState.Error(e.message ?: "Eroare necunoscută"))
            _effect.send(CalculatorEffect.ShowError(e.message ?: "Eroare necunoscută"))
        }
    }
}

package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.model.TransactionType
import com.asr.financial.domain.usecase.GetAllCongregationsUseCase
import com.asr.financial.domain.usecase.GetPublisherExpectedContributionUseCase
import com.asr.financial.domain.usecase.GetTotalPublishersUseCase
import com.asr.financial.domain.usecase.GetTransactionsByMonthUseCase
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
    private val getAllCongregationsUseCase: GetAllCongregationsUseCase,
    private val getTotalPublishersUseCase: GetTotalPublishersUseCase,
    private val getPublisherExpectedContributionUseCase: GetPublisherExpectedContributionUseCase,
    private val getTransactionsByMonthUseCase: GetTransactionsByMonthUseCase
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

            val congregations = getAllCongregationsUseCase()
            val totalPublishers = getTotalPublishersUseCase()
            val publisherExpectedContribution = getPublisherExpectedContributionUseCase()
            
            // Calculate monthly expenses from transactions
            val monthlyTransactions = getTransactionsByMonthUseCase(month, year)
            val monthlyExpenses = monthlyTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            if (totalPublishers == 0) {
                _uiState.emit(CalculatorState.Error("Nu există vestitori înregistrați"))
                return
            }

            // Calculate contributions based on expected contribution per publisher
            val monthlyContribution = ContributionCalculation(
                totalExpenses = monthlyExpenses,
                numberOfPublishers = totalPublishers,
                perPublisherAmount = publisherExpectedContribution
            )

            val yearlyContribution = ContributionCalculation(
                totalExpenses = monthlyExpenses * 12,
                numberOfPublishers = totalPublishers,
                perPublisherAmount = publisherExpectedContribution * 12
            )

            // Calculate per congregation
            val congregationContributions = congregations.map { congregation ->
                CongregationContribution(
                    congregationId = congregation.id,
                    congregationName = congregation.name,
                    numberOfPublishers = congregation.memberCount,
                    perPublisherAmount = publisherExpectedContribution,
                    totalAmount = publisherExpectedContribution * congregation.memberCount
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

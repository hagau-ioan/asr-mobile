package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.models.TransactionType
import com.asr.financial.domain.usecase.GetAllCongregationsUseCase
import com.asr.financial.domain.usecase.GetSituatieCurentaAsrUseCase
import com.asr.financial.domain.usecase.GetTotalPublishersUseCase
import com.asr.financial.domain.usecase.GetTransactionsUseCase
import com.asr.financial.domain.usecase.IsAdminUseCase
import com.asr.financial.platform.Clock
import com.asr.financial.platform.Logger
import com.asr.financial.presentation.mvi.effect.CalculatorEffect
import com.asr.financial.presentation.mvi.event.CalculatorEvent
import com.asr.financial.presentation.mvi.interactor.CalculatorMessages
import com.asr.financial.presentation.mvi.state.CalculatorState
import com.asr.financial.presentation.screens.calculator.CongregationContribution
import com.asr.financial.presentation.screens.calculator.ContributionCalculation
import com.asr.financial.utils.divide
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.roundTo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Interactor for Calculator Screen
 * Handles business logic for contribution calculations
 * All calculations are based on the last 12 months of expenses
 */
class CalculatorInteractor(
    private val getAllCongregationsUseCase: GetAllCongregationsUseCase,
    private val getTotalPublishersUseCase: GetTotalPublishersUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getSituatieCurentaAsrUseCase: GetSituatieCurentaAsrUseCase,
    private val isAdminUseCase: IsAdminUseCase,
    private val clock: Clock,
    private val logger: Logger
) {
    private companion object {
        const val TAG = "CalculatorInteractor"
    }
    private val _uiState = MutableStateFlow<CalculatorState>(CalculatorState.Loading)
    val uiState: Flow<CalculatorState> = _uiState.asStateFlow()

    private val _effect = Channel<CalculatorEffect>(Channel.BUFFERED)
    val effect: Flow<CalculatorEffect> = _effect.receiveAsFlow()

    suspend fun handleEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.LoadData -> loadData()
            is CalculatorEvent.FilterByPeriod -> loadData() // Period filter no longer needed, always uses last 12 months
        }
    }

    private suspend fun loadData() {
        try {
            _uiState.emit(CalculatorState.Loading)

            val congregations = getAllCongregationsUseCase()
            val totalPublishers = getTotalPublishersUseCase()
            
            if (totalPublishers == 0) {
                _uiState.emit(CalculatorState.Error(CalculatorMessages.ERROR_NO_PUBLISHERS))
                return
            }

            // Get all transactions and filter for last 12 months
            val allTransactions = getTransactionsUseCase()
            val last12MonthsExpenses = getLast12MonthsExpenses(allTransactions)

            // Calculate contributions based on last 12 months expenses
            // Monthly contribution = (Total expenses last 12 months / 12) / Number of publishers
            val averageMonthlyExpenses = last12MonthsExpenses.divide(12.0)
            val monthlyPerPublisher = averageMonthlyExpenses.divide(totalPublishers.toDouble())
            
            // Yearly contribution = Total expenses last 12 months / Number of publishers
            val yearlyPerPublisher = last12MonthsExpenses.divide(totalPublishers.toDouble())

            val monthlyContribution = ContributionCalculation(
                totalExpenses = averageMonthlyExpenses,
                numberOfPublishers = totalPublishers,
                perPublisherAmount = monthlyPerPublisher.roundTo(2)
            )

            val yearlyContribution = ContributionCalculation(
                totalExpenses = last12MonthsExpenses,
                numberOfPublishers = totalPublishers,
                perPublisherAmount = yearlyPerPublisher.roundTo(2)
            )

            // Calculate per congregation using fixed monthly ceiling from JSON
            val congregationContributions = congregations.map { congregation ->
                val perPublisher = congregation.monthlyCeiling.divide(congregation.memberCount.toDouble())
                CongregationContribution(
                    congregationId = congregation.id,
                    congregationName = congregation.name,
                    numberOfPublishers = congregation.memberCount,
                    perPublisherAmount = perPublisher.roundTo(2),  // Plafon / Număr vestitori
                    totalAmount = congregation.monthlyCeiling  // Fixed ceiling from JSON
                )
            }.sortedBy { it.congregationName }

            // Load current ASR situation from Firebase Storage
            // Show whenever we have data (admin only); no filtering by current month
            // Data is cached for 12 hours
            val situatieCurentaAsr = try {
                val isAdmin = isAdminUseCase()
                if (!isAdmin) {
                    logger.debug(TAG, "situatie_curenta_asr: not shown (user is not admin)")
                    null
                } else {
                    val situatie = getSituatieCurentaAsrUseCase()
                    if (situatie == null) {
                        logger.warning(TAG, "situatie_curenta_asr: not shown (Cloud load or parse returned null)")
                        null
                    } else {
                        logger.debug(TAG, "situatie_curenta_asr: showing (year=${situatie.year}, month=${situatie.month}, endDate=${situatie.endDate})")
                        situatie
                    }
                }
            } catch (e: Exception) {
                logger.error(TAG, "situatie_curenta_asr: exception during load", e)
                null
            }

            _uiState.emit(
                CalculatorState.Success(
                    monthlyContribution = monthlyContribution,
                    yearlyContribution = yearlyContribution,
                    congregationContributions = congregationContributions,
                    situatieCurentaAsr = situatieCurentaAsr
                )
            )
        } catch (e: Exception) {
            // Use exception message as error key (if it's a known error key) or default to unknown error
            val errorKey = e.message?.takeIf { 
                it.startsWith("config_error_") || 
                it.startsWith("data_error_") || 
                it.startsWith("calculator_error_") 
            } ?: CalculatorMessages.ERROR_UNKNOWN
            _uiState.emit(CalculatorState.Error(errorKey))
            _effect.send(CalculatorEffect.ShowError(errorKey))
        }
    }

    /**
     * Get total expenses from the last 12 months
     * Based on transaction dates (YYYY-MM-DD format)
     */
    private fun getLast12MonthsExpenses(transactions: List<com.asr.financial.domain.models.Transaction>): Double {
        val currentYear = getCurrentYear(clock)
        val currentMonth = getCurrentMonth(clock)
        
        // Calculate date range for last 12 months
        val monthsToInclude = mutableListOf<Pair<Int, Int>>() // (year, month)
        
        for (i in 0 until 12) {
            var year = currentYear
            var month = currentMonth - i
            
            while (month <= 0) {
                month += 12
                year -= 1
            }
            
            monthsToInclude.add(year to month)
        }
        
        // Filter transactions from last 12 months and sum expenses
        return transactions
            .filter { transaction ->
                if (transaction.type != TransactionType.EXPENSE) return@filter false
                
                val parts = transaction.date.split("-")
                if (parts.size >= 2) {
                    val txnYear = parts[0].toIntOrNull()
                    val txnMonth = parts[1].toIntOrNull()
                    if (txnYear != null && txnMonth != null) {
                        monthsToInclude.contains(txnYear to txnMonth)
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
            .sumOf { it.amount }
    }
}

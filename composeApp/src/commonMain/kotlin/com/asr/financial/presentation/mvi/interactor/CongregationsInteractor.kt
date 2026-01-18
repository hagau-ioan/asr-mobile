package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.models.Transaction
import com.asr.financial.domain.models.TransactionType
import com.asr.financial.domain.usecase.GetAllCongregationsUseCase
import com.asr.financial.domain.usecase.GetAppConfigUseCase
import com.asr.financial.domain.usecase.GetCongregationNamesUseCase
import com.asr.financial.domain.usecase.GetTransactionsUseCase
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.effect.CongregationsEffect
import com.asr.financial.presentation.mvi.event.CongregationsEvent
import com.asr.financial.presentation.mvi.state.CongregationsState
import com.asr.financial.presentation.screens.congregations.CongregationStat
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Congregations Interactor - Handles business logic.
 * Uses UseCases for configuration data instead of hardcoded values.
 */
class CongregationsInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCongregationNamesUseCase: GetCongregationNamesUseCase,
    private val getAllCongregationsUseCase: GetAllCongregationsUseCase,
    private val getAppConfigUseCase: GetAppConfigUseCase,
    private val clock: Clock
) {
    private val _uiState = MutableStateFlow<CongregationsState>(CongregationsState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<CongregationsEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CongregationsEffect> = _uiEffectChannel.receiveAsFlow()

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

    suspend fun processEvent(event: CongregationsEvent) {
        when (event) {
            is CongregationsEvent.LoadData -> loadData(currentYear, currentMonth)
            is CongregationsEvent.FilterByPeriod -> filterByPeriod(event.year, event.month)
            is CongregationsEvent.Refresh -> refreshData()
        }
    }

    private suspend fun loadData(year: Int, month: Int) {
        _uiState.emit(CongregationsState.Loading)
        try {
            cachedTransactions = getTransactionsUseCase()
            val stats = calculateStats(year, month)
            emitSuccessState(stats, year, month)
        } catch (e: Exception) {
            _uiState.emit(CongregationsState.Error(e.message ?: com.asr.financial.presentation.ui.constants.AppConstants.ErrorMessages.UNKNOWN_ERROR))
            _uiEffectChannel.send(CongregationsEffect.ShowToast(com.asr.financial.presentation.ui.constants.AppConstants.ErrorMessages.FAILED_TO_LOAD_DATA))
        }
    }

    private suspend fun filterByPeriod(year: Int, month: Int) {
        currentYear = year
        currentMonth = month
        
        // Use cached data for filtering
        try {
            val stats = calculateStats(year, month)
            emitSuccessState(stats, year, month)
        } catch (e: Exception) {
            _uiState.emit(CongregationsState.Error(e.message ?: com.asr.financial.presentation.ui.constants.AppConstants.ErrorMessages.UNKNOWN_ERROR))
        }
    }

    private suspend fun refreshData() {
        cachedTransactions = getTransactionsUseCase()
        val stats = calculateStats(currentYear, currentMonth)
        emitSuccessState(stats, currentYear, currentMonth)
    }

    private suspend fun calculateStats(year: Int, month: Int): List<CongregationStat> {
        val filteredTransactions = cachedTransactions.filter {
            it.getYear() == year && it.getMonth() == month
        }

        // Load congregations with their ceilings
        val allCongregationsData = getAllCongregationsUseCase()
        val allCongregations = getCongregationNamesUseCase()

        val congregationDonations = filteredTransactions
            .filter { it.type == TransactionType.INCOME && it.congregationName != null }
            .groupBy { it.congregationName }

        return allCongregations.map { congName ->
            val congTransactions = congregationDonations[congName] ?: emptyList()
            val donated = congTransactions.sumOf { it.amount }
            val lastDate = congTransactions.maxByOrNull { it.date }?.date
            
            // Get the monthly ceiling for this congregation
            val congData = allCongregationsData.find { it.name == congName }
            val expected = congData?.monthlyCeiling ?: com.asr.financial.presentation.ui.constants.AppConstants.Business.DEFAULT_MONTHLY_CEILING

            CongregationStat(
                name = congName,
                donated = donated,
                expected = expected,
                difference = donated - expected,
                lastDonation = lastDate,
                isMissing = donated < expected
            )
        }.sortedWith(compareBy({ !it.isMissing }, { it.difference }))
    }

    private suspend fun emitSuccessState(stats: List<CongregationStat>, year: Int, month: Int) {
        val totalDonated = stats.sumOf { it.donated }
        val totalExpected = stats.sumOf { it.expected }
        val totalDifference = totalDonated - totalExpected
        val missingCount = stats.count { it.isMissing }

        _uiState.emit(
            CongregationsState.Success(
                stats = stats,
                totalDonated = totalDonated,
                totalExpected = totalExpected,
                totalDifference = totalDifference,
                missingCount = missingCount,
                selectedYear = year,
                selectedMonth = month
            )
        )
    }
}

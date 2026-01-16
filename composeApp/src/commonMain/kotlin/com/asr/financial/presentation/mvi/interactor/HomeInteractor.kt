package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.model.Transaction
import com.asr.financial.domain.model.TransactionType
import com.asr.financial.domain.usecase.*
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.effect.HomeEffect
import com.asr.financial.presentation.mvi.event.HomeEvent
import com.asr.financial.presentation.mvi.state.HomeState
import com.asr.financial.presentation.screens.home.MissingCongregation
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Home Interactor - Handles business logic for Home screen.
 * All calculations are done here, not in the Screen.
 * Follows MVI pattern.
 */
class HomeInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionsByMonthUseCase: GetTransactionsByMonthUseCase,
    private val getCongregationNamesUseCase: GetCongregationNamesUseCase,
    private val getAvailableYearsUseCase: GetAvailableYearsUseCase,
    private val getAppConfigUseCase: GetAppConfigUseCase,
    private val clock: Clock
) {
    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<HomeEffect>(Channel.UNLIMITED)
    val uiEffect: Flow<HomeEffect> = _uiEffectChannel.receiveAsFlow()

    private var currentYear = getCurrentYear(clock)
    private var currentMonth = getCurrentMonth(clock)

    suspend fun processEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadData -> loadData(currentYear, currentMonth)
            is HomeEvent.FilterByMonth -> filterByPeriod(event.year, event.month)
            is HomeEvent.NavigateToDetails -> navigateToDetails(event.transactionId)
            is HomeEvent.Refresh -> refreshData()
        }
    }

    private suspend fun loadData(year: Int, month: Int) {
        _uiState.emit(HomeState.Loading)
        try {
            val allTransactions = getTransactionsUseCase()
            emitSuccessState(allTransactions, year, month)
        } catch (e: Exception) {
            _uiState.emit(HomeState.Error(e.message ?: "Unknown error"))
            _uiEffectChannel.send(HomeEffect.ShowToast("Failed to load transactions"))
        }
    }

    private suspend fun filterByPeriod(year: Int, month: Int) {
        currentYear = year
        currentMonth = month

        // Don't show loading for filter changes - just update state
        try {
            val allTransactions = getTransactionsUseCase()
            emitSuccessState(allTransactions, year, month)
        } catch (e: Exception) {
            _uiState.emit(HomeState.Error(e.message ?: "Unknown error"))
        }
    }

    private suspend fun emitSuccessState(allTransactions: List<Transaction>, year: Int, month: Int) {
        // Load configuration
        val congregationNames = getCongregationNamesUseCase()
        val appConfig = getAppConfigUseCase()
        val expectedDonation = appConfig?.financial?.expectedDonationPerCongregation ?: 500.0
        val totalPublishers = appConfig?.financial?.totalPublishers ?: 785
        val congregationCount = congregationNames.size

        // Filter transactions for selected month
        val monthlyTransactions = allTransactions.filter {
            it.getYear() == year && it.getMonth() == month
        }

        // Calculate monthly statistics
        val monthlyIncome = monthlyTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val monthlyExpenses = monthlyTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val monthlyBalance = monthlyIncome - monthlyExpenses

        // Filter transactions for selected year
        val yearlyTransactions = allTransactions.filter { it.getYear() == year }

        // Calculate yearly statistics
        val yearlyIncome = yearlyTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val yearlyExpenses = yearlyTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val yearlyBalance = yearlyIncome - yearlyExpenses

        // Calculate missing congregations
        val missingCongregations = calculateMissingCongregations(
            monthlyTransactions = monthlyTransactions,
            congregationNames = congregationNames,
            expectedDonation = expectedDonation
        )

        // Calculate per publisher expense
        val perPublisherExpense = if (totalPublishers > 0) {
            yearlyExpenses / totalPublishers
        } else {
            0.0
        }

        _uiState.emit(
            HomeState.Success(
                selectedYear = year,
                selectedMonth = month,
                monthlyIncome = monthlyIncome,
                monthlyExpenses = monthlyExpenses,
                monthlyBalance = monthlyBalance,
                yearlyIncome = yearlyIncome,
                yearlyExpenses = yearlyExpenses,
                yearlyBalance = yearlyBalance,
                missingCongregations = missingCongregations,
                expectedDonationPerCongregation = expectedDonation,
                totalPublishers = totalPublishers,
                congregationCount = congregationCount,
                perPublisherExpense = perPublisherExpense
            )
        )
    }

    private fun calculateMissingCongregations(
        monthlyTransactions: List<Transaction>,
        congregationNames: List<String>,
        expectedDonation: Double
    ): List<MissingCongregation> {
        val congregationDonations = monthlyTransactions
            .filter { it.type == TransactionType.INCOME && it.congregationName != null }
            .groupBy { it.congregationName }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        return congregationNames.mapNotNull { congName ->
            val donated = congregationDonations[congName] ?: 0.0
            if (donated < expectedDonation) {
                MissingCongregation(
                    name = congName,
                    donated = donated,
                    expected = expectedDonation,
                    missing = expectedDonation - donated
                )
            } else {
                null
            }
        }
    }

    private suspend fun navigateToDetails(transactionId: String) {
        _uiEffectChannel.send(HomeEffect.NavigateToDetails(transactionId))
    }

    private suspend fun refreshData() {
        loadData(currentYear, currentMonth)
        _uiEffectChannel.send(HomeEffect.ScrollToTop)
    }
}

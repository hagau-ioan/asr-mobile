package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.domain.models.Transaction
import com.asr.financial.domain.models.TransactionType
import com.asr.financial.domain.usecase.*
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.effect.HomeEffect
import com.asr.financial.presentation.mvi.event.HomeEvent
import com.asr.financial.presentation.mvi.state.HomeState
import com.asr.financial.presentation.screens.home.MissingCongregation
import com.asr.financial.presentation.ui.constants.AppConstants
import com.asr.financial.presentation.ui.constants.UIConstants
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.divide
import com.asr.financial.utils.roundTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

/**
 * Home Interactor - Handles business logic for Home screen.
 * All calculations are done here, not in the Screen.
 * Follows MVI pattern.
 */
class HomeInteractor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCongregationNamesUseCase: GetCongregationNamesUseCase,
    private val getAllCongregationsUseCase: GetAllCongregationsUseCase,
    private val getAvailableYearsUseCase: GetAvailableYearsUseCase,
    private val getAppConfigUseCase: GetAppConfigUseCase,
    private val refreshDataUseCase: RefreshDataUseCase,
    private val observePendingNotificationUseCase: ObservePendingNotificationUseCase,
    private val getPendingNotificationUseCase: GetPendingNotificationUseCase,
    private val clearPendingNotificationUseCase: ClearPendingNotificationUseCase,
    private val clock: Clock
) {
    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<HomeEffect>(Channel.BUFFERED)
    val uiEffect: Flow<HomeEffect> = _uiEffectChannel.receiveAsFlow()
    
    // Coroutine scope for observing notification Flow
    private val interactorScope = CoroutineScope(SupervisorJob())
    
    // Observe pending notification changes and update state
    init {
        // Observe pending notification Flow and update state when it changes
        interactorScope.launch {
            observePendingNotificationUseCase().collect { notification ->
                _uiState.update { currentState ->
                    if (currentState is HomeState.Success) {
                        currentState.copy(pendingNotification = notification)
                    } else {
                        currentState
                    }
                }
            }
        }
    }

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

    suspend fun processEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadData -> loadData(currentYear, currentMonth)
            is HomeEvent.FilterByMonth -> filterByPeriod(event.year, event.month)
            is HomeEvent.NavigateToDetails -> navigateToDetails(event.transactionId)
            is HomeEvent.Refresh -> refreshData()
            is HomeEvent.DismissNotification -> dismissNotification()
        }
    }

    private suspend fun loadData(year: Int, month: Int) {
        _uiState.emit(HomeState.Loading)
        try {
            cachedTransactions = getTransactionsUseCase()
            emitSuccessState(cachedTransactions, year, month)
        } catch (e: Exception) {
            _uiState.emit(HomeState.Error(e.message ?: AppConstants.ErrorMessages.UNKNOWN_ERROR))
            _uiEffectChannel.send(HomeEffect.ShowToast(AppConstants.ErrorMessages.FAILED_TO_LOAD_TRANSACTIONS))
        }
    }

    private suspend fun filterByPeriod(year: Int, month: Int) {
        currentYear = year
        currentMonth = month

        // Use cached data for filtering
        try {
            emitSuccessState(cachedTransactions, year, month)
        } catch (e: Exception) {
            _uiState.emit(HomeState.Error(e.message ?: AppConstants.ErrorMessages.UNKNOWN_ERROR))
        }
    }

    private suspend fun emitSuccessState(allTransactions: List<Transaction>, year: Int, month: Int) {
        // Load configuration
        val allCongregationsData = getAllCongregationsUseCase()
        val congregationNames = getCongregationNamesUseCase()
        val appConfig = getAppConfigUseCase()
        val availableYears = getAvailableYearsUseCase()
        val totalPublishers = appConfig?.financial?.totalPublishers ?: UIConstants.DEFAULT_TOTAL_PUBLISHERS
        val publisherExpectedContribution = appConfig?.financial?.publisherExpectedContribution ?: UIConstants.DEFAULT_PUBLISHER_CONTRIBUTION
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
            allCongregationsData = allCongregationsData
        )

        // Calculate per publisher contribution based on last 12 months (same as CalculatorScreen)
        // Formula: (Total expenses last 12 months / 12) / Number of publishers
        val last12MonthsExpenses = getLast12MonthsExpenses(allTransactions)
        val averageMonthlyExpenses = last12MonthsExpenses.divide(12.0)
        val perPublisherContribution = if (totalPublishers > 0) {
            averageMonthlyExpenses.divide(totalPublishers.toDouble()).roundTo(2)
        } else {
            0.0
        }

        // Load pending notification from DataStore to ensure it persists after app restart
        // First try to get from current state (if already loaded), otherwise load from DataStore
        val currentPendingNotification = (_uiState.value as? HomeState.Success)?.pendingNotification
            ?: getPendingNotificationUseCase()
        
        _uiState.emit(
            HomeState.Success(
                selectedYear = year,
                selectedMonth = month,
                availableYears = availableYears,
                monthlyIncome = monthlyIncome,
                monthlyExpenses = monthlyExpenses,
                monthlyBalance = monthlyBalance,
                yearlyIncome = yearlyIncome,
                yearlyExpenses = yearlyExpenses,
                yearlyBalance = yearlyBalance,
                missingCongregations = missingCongregations,
                expectedDonationPerCongregation = allCongregationsData.sumOf { it.monthlyCeiling } / allCongregationsData.size.coerceAtLeast(1),
                totalPublishers = totalPublishers,
                congregationCount = congregationCount,
                perPublisherExpense = perPublisherContribution,
                pendingNotification = currentPendingNotification // Load from DataStore if not in state (e.g., after app restart)
            )
        )
    }

    private fun calculateMissingCongregations(
        monthlyTransactions: List<Transaction>,
        allCongregationsData: List<com.asr.financial.domain.models.CongregationInfo>
    ): List<MissingCongregation> {
        val congregationDonations = monthlyTransactions
            .filter { it.type == TransactionType.INCOME && it.congregationName != null }
            .groupBy { it.congregationName }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        return allCongregationsData.mapNotNull { congData ->
            val donated = congregationDonations[congData.name] ?: AppConstants.Defaults.ZERO_DOUBLE
            val expected = congData.monthlyCeiling
            
            if (donated < expected) {
                MissingCongregation(
                    name = congData.name,
                    donated = donated,
                    expected = expected,
                    missing = expected - donated
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
        val currentState = _uiState.value
        if (currentState is HomeState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        }
        
        refreshDataUseCase()
        loadData(currentYear, currentMonth)
        _uiEffectChannel.send(HomeEffect.ScrollToTop)
    }

    /**
     * Get total expenses from the last 12 months
     * Based on transaction dates (YYYY-MM-DD format)
     * Same calculation as in CalculatorInteractor
     */
    private fun getLast12MonthsExpenses(transactions: List<Transaction>): Double {
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
    
    private suspend fun dismissNotification() {
        clearPendingNotificationUseCase()
        // State will be updated automatically via Flow observation in init block
    }
}

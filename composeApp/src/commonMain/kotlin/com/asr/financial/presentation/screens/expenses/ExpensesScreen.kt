package com.asr.financial.presentation.screens.expenses

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import asr_financial.composeapp.generated.resources.Res
import asr_financial.composeapp.generated.resources.expenses_distribution
import asr_financial.composeapp.generated.resources.expenses_empty
import asr_financial.composeapp.generated.resources.home_month
import asr_financial.composeapp.generated.resources.home_select_period
import asr_financial.composeapp.generated.resources.home_year
import asr_financial.composeapp.generated.resources.nav_expenses
import asr_financial.composeapp.generated.resources.nav_home
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.ExpensesEvent
import com.asr.financial.presentation.mvi.state.ExpensesState
import com.asr.financial.presentation.mvi.viewmodel.ExpensesViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.charts.DonutChart
import com.asr.financial.presentation.screens.charts.DonutChartItem
import com.asr.financial.presentation.screens.expenses.components.ExpenseCategoryRow
import com.asr.financial.presentation.screens.expenses.components.MonthlySummaryCard
import com.asr.financial.presentation.screens.expenses.components.YearlySummaryCard
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.period.PeriodSelectorCard
import com.asr.financial.presentation.ui.components.period.usePeriodSelectorState
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.EMPTY_STATE_PADDING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExpensesScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: ExpensesViewModel = koinViewModel(),
    clock: Clock = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Use period selector state hook
    val periodState = usePeriodSelectorState(
        clock = clock,
        onPeriodChange = { year, month ->
            viewModel.handleEvent(ExpensesEvent.FilterByPeriod(year, month))
        },
        initialLoadEvent = null, // ExpensesScreen handles initial load differently
        skipInitialChange = true // Only trigger on user changes
    )

    // Initial load with default period
    LaunchedEffect(Unit) {
        viewModel.handleEvent(ExpensesEvent.FilterByPeriod(periodState.selectedYear, periodState.selectedMonth))
    }

    when (val state = uiState) {
        is ExpensesState.Success -> {
            ExpensesSuccessContent(
                state = state,
                windowSizeClass = windowSizeClass,
                selectedYear = periodState.selectedYear,
                selectedMonth = periodState.selectedMonth,
                showYearDropdown = periodState.showYearDropdown,
                showMonthDropdown = periodState.showMonthDropdown,
                years = state.availableYears,
                months = periodState.months,
                onYearDropdownChange = periodState.onYearDropdownChange,
                onMonthDropdownChange = periodState.onMonthDropdownChange,
                onYearSelected = periodState.onYearSelected,
                onMonthSelected = periodState.onMonthSelected,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }

        is ExpensesState.Loading -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_expenses))
                ),
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            ) {
                item {
                    LoadingContent()
                }
            }
        }

        is ExpensesState.Error -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_expenses))
                ),
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            ) {
                item {
                    ErrorContent(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun ExpensesSuccessContent(
    state: ExpensesState.Success,
    windowSizeClass: WindowSizeClass,
    selectedYear: Int,
    selectedMonth: Int,
    showYearDropdown: Boolean,
    showMonthDropdown: Boolean,
    years: List<Int>,
    months: List<Pair<Int, StringResource>>,
    onYearDropdownChange: (Boolean) -> Unit,
    onMonthDropdownChange: (Boolean) -> Unit,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val selectedMonthName = months.find { it.first == selectedMonth }?.second?.let { stringResource(it) } ?: ""

    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_expenses))
        ),
        selectedMonth = selectedMonthName,
        selectedYear = selectedYear,
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            PeriodSelectorCard(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedMonthName = selectedMonthName,
                years = years,
                months = months,
                showYearDropdown = showYearDropdown,
                showMonthDropdown = showMonthDropdown,
                onYearDropdownChange = onYearDropdownChange,
                onMonthDropdownChange = onMonthDropdownChange,
                onYearSelected = onYearSelected,
                onMonthSelected = onMonthSelected,
                title = stringResource(Res.string.home_select_period),
                yearLabel = stringResource(Res.string.home_year),
                monthLabel = stringResource(Res.string.home_month)
            )
        }

        item {
            MonthlySummaryCard(totalExpenses = state.totalExpenses)
        }

        item {
            PieChartCard(expenses = state.expenses)
        }

        state.expenses.forEach { expense ->
            item {
                ExpenseCategoryRow(expense = expense)
            }
        }

        item {
            YearlySummaryCard(
                yearlyTotal = state.yearlyTotal,
                yearlyEndMonth = state.yearlyEndMonth,
                yearlyEndYear = state.yearlyEndYear,
                months = months
            )
        }

        if (state.expenses.isEmpty()) {
            item {
                EmptyStateCard()
            }
        }
    }
}

@Composable
private fun PieChartCard(expenses: List<ExpenseStat>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text(
                text = stringResource(Res.string.expenses_distribution),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(CARD_PADDING_DP.dp))
            ExpensePieChart(expenses)
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EMPTY_STATE_PADDING_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.expenses_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExpensePieChart(expenses: List<ExpenseStat>) {
    val items = expenses.map { expense ->
        DonutChartItem(
            label = expense.category,
            value = expense.amount
        )
    }
    DonutChart(items = items)
}

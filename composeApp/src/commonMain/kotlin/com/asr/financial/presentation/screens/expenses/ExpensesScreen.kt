package com.asr.financial.presentation.screens.expenses

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.ExpensesEvent
import com.asr.financial.presentation.mvi.state.ExpensesState
import com.asr.financial.presentation.mvi.viewmodel.ExpensesViewModel
import com.asr.financial.presentation.screens.charts.DonutChart
import com.asr.financial.presentation.screens.charts.DonutChartItem
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.FULL_CIRCLE_DEGREES
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.LEGEND_ICON_SIZE_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_COLORS
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_DONUT_HOLE_RATIO
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_HEIGHT_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_SIZE_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_START_ANGLE
import com.asr.financial.presentation.screens.expenses.components.ExpenseCategoryRow
import com.asr.financial.presentation.screens.expenses.components.MonthlySummaryCard
import com.asr.financial.presentation.screens.expenses.components.YearlySummaryCard
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.period.PeriodSelectorCard
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.DEFAULT_MONTH
import com.asr.financial.presentation.ui.constants.UIConstants.DEFAULT_YEAR
import com.asr.financial.presentation.ui.constants.UIConstants.EMPTY_STATE_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SMALL_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.TINY_SPACING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getMonthsList
import com.asr.financial.utils.percentOfAsInt
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import asr_financial.composeapp.generated.resources.*
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

    val (defaultMonth, defaultYear) = remember {
        val (prevMonth, prevYear) = calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
        prevMonth to prevYear
    }

    var selectedYear by remember { mutableStateOf(defaultYear) }
    var selectedMonth by remember { mutableStateOf(defaultMonth) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    val months = remember { getMonthsList() }

    LaunchedEffect(Unit) {
        viewModel.handleEvent(ExpensesEvent.FilterByPeriod(defaultYear, defaultMonth))
    }

    LaunchedEffect(selectedYear, selectedMonth) {
        if (selectedYear != defaultYear || selectedMonth != defaultMonth) {
            viewModel.handleEvent(ExpensesEvent.FilterByPeriod(selectedYear, selectedMonth))
        }
    }

    when (val state = uiState) {
        is ExpensesState.Success -> {
            ExpensesSuccessContent(
                state = state,
                windowSizeClass = windowSizeClass,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                showYearDropdown = showYearDropdown,
                showMonthDropdown = showMonthDropdown,
                years = state.availableYears,
                months = months,
                onYearDropdownChange = { showYearDropdown = it },
                onMonthDropdownChange = { showMonthDropdown = it },
                onYearSelected = { selectedYear = it },
                onMonthSelected = { selectedMonth = it },
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }

        is ExpensesState.Loading -> {
            ExpensesLoadingContent(
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }

        is ExpensesState.Error -> {
            ExpensesErrorContent(
                message = state.message,
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
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
    months: List<Pair<Int, org.jetbrains.compose.resources.StringResource>>,
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
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
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
private fun ExpensesLoadingContent(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
            BreadcrumbItem(stringResource(Res.string.nav_expenses))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(EMPTY_STATE_PADDING_DP.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ExpensesErrorContent(
    message: String,
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
            BreadcrumbItem(stringResource(Res.string.nav_expenses))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
                    Text(
                        text = stringResource(Res.string.error),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(SMALL_SPACING_DP.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
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

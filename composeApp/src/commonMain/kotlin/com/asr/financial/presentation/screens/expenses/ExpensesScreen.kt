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
import com.asr.financial.presentation.mvi.event.ExpensesEvent
import com.asr.financial.presentation.mvi.state.ExpensesState
import com.asr.financial.presentation.mvi.viewmodel.ExpensesViewModel
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.FULL_CIRCLE_DEGREES
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.LEGEND_ICON_SIZE_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_COLORS
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_DONUT_HOLE_RATIO
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_HEIGHT_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_SIZE_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_START_ANGLE
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
import com.asr.financial.utils.calculateStartMonthFor12Months
import com.asr.financial.utils.calculateStartYearFor12Months
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getAvailableYears
import com.asr.financial.utils.getMonthsList
import com.asr.financial.platform.Clock
import com.asr.financial.utils.percentOfAsInt
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

    var selectedYear by remember { mutableStateOf(DEFAULT_YEAR) }
    var selectedMonth by remember { mutableStateOf(DEFAULT_MONTH) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    val years = remember { getAvailableYears(clock) }
    val months = remember { getMonthsList() }

    LaunchedEffect(selectedYear, selectedMonth) {
        viewModel.onEvent(ExpensesEvent.FilterByPeriod(selectedYear, selectedMonth))
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
                years = years,
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
private fun MonthlySummaryCard(totalExpenses: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text(
                text = stringResource(Res.string.expenses_total_monthly),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(SMALL_SPACING_DP.dp))
            Text(
                text = totalExpenses.formatCurrency(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
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
private fun ExpenseCategoryRow(expense: ExpenseStat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING_DP.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expense.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = expense.amount.formatCurrency(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun YearlySummaryCard(
    yearlyTotal: Double,
    yearlyEndMonth: Int,
    yearlyEndYear: Int,
    months: List<Pair<Int, org.jetbrains.compose.resources.StringResource>>
) {
    val endMonthName = months.find { it.first == yearlyEndMonth }?.second?.let { stringResource(it) } ?: ""
    val startYear = calculateStartYearFor12Months(yearlyEndMonth, yearlyEndYear)
    val startMonth = calculateStartMonthFor12Months(yearlyEndMonth)
    val startMonthName = months.find { it.first == startMonth }?.second?.let { stringResource(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text(
                text = stringResource(Res.string.expenses_total_yearly),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(SMALL_SPACING_DP.dp))
            Text(
                text = yearlyTotal.formatCurrency(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(
                    Res.string.expenses_yearly_range,
                    startMonthName,
                    startYear,
                    endMonthName,
                    yearlyEndYear
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
    val total = expenses.sumOf { it.amount }
    if (total == 0.0) return

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PIE_CHART_HEIGHT_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(PIE_CHART_SIZE_DP.dp)) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                var startAngle = PIE_CHART_START_ANGLE

                expenses.forEachIndexed { index, expense ->
                    val sweepAngle = (expense.amount / total * FULL_CIRCLE_DEGREES).toFloat()
                    val color = Color(PIE_CHART_COLORS[index % PIE_CHART_COLORS.size])

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    startAngle += sweepAngle
                }

                drawCircle(
                    color = Color.White,
                    radius = radius * PIE_CHART_DONUT_HOLE_RATIO,
                    center = center
                )
            }
        }

        Spacer(Modifier.height(CARD_PADDING_DP.dp))

        expenses.forEachIndexed { index, expense ->
            val percentage = expense.amount.percentOfAsInt(total)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TINY_SPACING_DP.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(LEGEND_ICON_SIZE_DP.dp)
                        .background(Color(PIE_CHART_COLORS[index % PIE_CHART_COLORS.size]))
                )
                Spacer(Modifier.width(SMALL_SPACING_DP.dp))
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

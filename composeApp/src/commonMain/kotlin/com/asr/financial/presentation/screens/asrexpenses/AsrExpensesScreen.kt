package com.asr.financial.presentation.screens.asrexpenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import asr_financial.composeapp.generated.resources.Res
import asr_financial.composeapp.generated.resources.asr_expenses_col_amount
import asr_financial.composeapp.generated.resources.asr_expenses_col_category
import asr_financial.composeapp.generated.resources.asr_expenses_col_date
import asr_financial.composeapp.generated.resources.asr_expenses_col_description
import asr_financial.composeapp.generated.resources.asr_expenses_empty
import asr_financial.composeapp.generated.resources.asr_expenses_note_12_months
import asr_financial.composeapp.generated.resources.asr_expenses_summary
import asr_financial.composeapp.generated.resources.asr_expenses_total_amount
import asr_financial.composeapp.generated.resources.asr_expenses_total_count
import asr_financial.composeapp.generated.resources.filter_all_months
import asr_financial.composeapp.generated.resources.home_month
import asr_financial.composeapp.generated.resources.home_select_period
import asr_financial.composeapp.generated.resources.home_year
import asr_financial.composeapp.generated.resources.nav_asr_expenses
import asr_financial.composeapp.generated.resources.nav_home
import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.presentation.mvi.event.AsrExpensesEvent
import com.asr.financial.presentation.mvi.state.AsrExpensesState
import com.asr.financial.presentation.mvi.viewmodel.AsrExpensesViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.asrexpenses.AsrExpensesConstants.COL_AMOUNT_WIDTH_DP
import com.asr.financial.presentation.screens.asrexpenses.AsrExpensesConstants.COL_CATEGORY_WIDTH_DP
import com.asr.financial.presentation.screens.asrexpenses.AsrExpensesConstants.COL_DATE_WIDTH_DP
import com.asr.financial.presentation.screens.asrexpenses.AsrExpensesConstants.COL_DESCRIPTION_WIDTH_DP
import com.asr.financial.presentation.screens.asrexpenses.components.AsrExpenseRow
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.cards.SummaryInfoCard
import com.asr.financial.presentation.ui.components.period.PeriodSelectorCard
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.constants.UIConstants.EMPTY_STATE_PADDING_DP
import com.asr.financial.presentation.ui.components.table.DataTable
import com.asr.financial.presentation.ui.components.table.TableColumn
import com.asr.financial.presentation.ui.components.table.TableHeaderCell
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getMonthsList
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AsrExpensesScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onNavigateToDecont: (Int, Int) -> Unit = { _, _ -> },
    onMenuClick: () -> Unit = {},
    viewModel: AsrExpensesViewModel = koinViewModel(),
    clock: com.asr.financial.platform.Clock = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    val months = remember { getMonthsList() }
    val (initialMonth, initialYear) = calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
    var headerMonthNum by remember { mutableIntStateOf(initialMonth) }
    var headerYear by remember { mutableIntStateOf(initialYear) }
    SideEffect {
        val (m, y) = calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
        headerMonthNum = m
        headerYear = y
    }
    val headerMonthName = months.find { it.first == headerMonthNum }?.second?.let { stringResource(it) } ?: ""

    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_asr_expenses))
        ),
        selectedMonth = headerMonthName,
        selectedYear = headerYear,
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        when (val state = uiState) {
            is AsrExpensesState.Loading -> {
                item {
                    LoadingContent()
                }
            }
            is AsrExpensesState.Success -> {
                item {
                    AsrExpensesSuccessContent(
                        expenses = state.expenses,
                        totalAmount = state.totalAmount,
                        selectedYear = state.selectedYear,
                        selectedMonth = state.selectedMonth,
                        years = state.availableYears,
                        months = getMonthsList(),
                        showYearDropdown = showYearDropdown,
                        showMonthDropdown = showMonthDropdown,
                        onYearDropdownChange = { showYearDropdown = it },
                        onMonthDropdownChange = { showMonthDropdown = it },
                        onYearSelected = { year ->
                            viewModel.handleEvent(AsrExpensesEvent.FilterByPeriod(year, null))
                        },
                        onMonthSelected = { month ->
                            viewModel.handleEvent(AsrExpensesEvent.FilterByPeriod(null, month))
                        },
                        onNavigateToDecont = onNavigateToDecont,
                        monthsWithDecontData = state.monthsWithDecontData
                    )
                }
            }
            is AsrExpensesState.Error -> {
                item {
                    ErrorContent(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun AsrExpensesSuccessContent(
    expenses: List<AsrExpense>,
    totalAmount: Double,
    selectedYear: Int,
    selectedMonth: Int,
    years: List<Int>,
    months: List<Pair<Int, StringResource>>,
    showYearDropdown: Boolean,
    showMonthDropdown: Boolean,
    onYearDropdownChange: (Boolean) -> Unit,
    onMonthDropdownChange: (Boolean) -> Unit,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onNavigateToDecont: (Int, Int) -> Unit,
    monthsWithDecontData: Set<Pair<Int, Int>> = emptySet()
) {
    val selectedMonthName = if (selectedMonth == 0) {
        stringResource(Res.string.filter_all_months)
    } else {
        months.find { it.first == selectedMonth }?.second?.let { stringResource(it) } ?: ""
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(Res.string.asr_expenses_note_12_months),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Period Selector
        PeriodSelectorCard(
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            selectedMonthName = selectedMonthName,
            years = years,
            months = listOf(0 to Res.string.filter_all_months) + months,
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

        // Summary Card
        SummaryInfoCard(
            title = stringResource(Res.string.asr_expenses_summary),
            totalItemsLabel = stringResource(Res.string.asr_expenses_total_count),
            totalItemsValue = expenses.size.toString(),
            totalAmountLabel = stringResource(Res.string.asr_expenses_total_amount),
            totalAmountValue = totalAmount.formatCurrency(),
            totalAmountColor = MaterialTheme.colorScheme.error
        )

        // Expenses Table or Empty State
        if (expenses.isEmpty()) {
            EmptyStateCard()
        } else {
            val columns = listOf(
                TableColumn(stringResource(Res.string.asr_expenses_col_date), COL_DATE_WIDTH_DP.dp),
                TableColumn(stringResource(Res.string.asr_expenses_col_category), COL_CATEGORY_WIDTH_DP.dp),
                TableColumn(stringResource(Res.string.asr_expenses_col_description), COL_DESCRIPTION_WIDTH_DP.dp),
                TableColumn(stringResource(Res.string.asr_expenses_col_amount), COL_AMOUNT_WIDTH_DP.dp, TextAlign.End)
            )

            DataTable(
                columns = columns,
                headerContent = {
                    columns.forEach { column ->
                        TableHeaderCell(
                            text = column.header,
                            width = column.width,
                            textAlign = column.textAlign
                        )
                    }
                }
            ) {
                expenses.forEach { expense ->
                    AsrExpenseRow(
                        expense = expense,
                        onDecontClick = { year, month ->
                            onNavigateToDecont(year, month)
                        },
                        monthsWithDecontData = monthsWithDecontData
                    )
                }
            }
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
                text = stringResource(Res.string.asr_expenses_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


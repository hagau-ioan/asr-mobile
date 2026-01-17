package com.asr.financial.presentation.screens.asrexpenses

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import asr_financial.composeapp.generated.resources.*
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
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.components.period.PeriodSelectorCard
import com.asr.financial.presentation.ui.components.table.DataTable
import com.asr.financial.presentation.ui.components.table.TableColumn
import com.asr.financial.presentation.ui.components.table.TableHeaderCell
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getMonthsList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AsrExpensesScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: AsrExpensesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_asr_expenses))
        ),
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
                        windowSizeClass = windowSizeClass,
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
                        onNavigate = onNavigate,
                        onMenuClick = onMenuClick
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
    windowSizeClass: WindowSizeClass,
    expenses: List<AsrExpense>,
    totalAmount: Double,
    selectedYear: Int,
    selectedMonth: Int,
    years: List<Int>,
    months: List<Pair<Int, org.jetbrains.compose.resources.StringResource>>,
    showYearDropdown: Boolean,
    showMonthDropdown: Boolean,
    onYearDropdownChange: (Boolean) -> Unit,
    onMonthDropdownChange: (Boolean) -> Unit,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
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
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.asr_expenses_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.asr_expenses_total_count),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = expenses.size.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.asr_expenses_total_amount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = totalAmount.formatCurrency(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Expenses Table
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
                AsrExpenseRow(expense)
            }
        }
    }
}


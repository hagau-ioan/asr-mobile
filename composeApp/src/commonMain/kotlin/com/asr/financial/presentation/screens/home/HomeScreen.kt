package com.asr.financial.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.HomeEvent
import com.asr.financial.presentation.mvi.state.HomeState
import com.asr.financial.presentation.mvi.viewmodel.HomeViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.period.PeriodSelectorCard
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getMonthsList
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home Screen - Displays financial statistics.
 * All calculations are done in HomeInteractor, this screen only renders state.
 */
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    clock: Clock = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Calculate previous month for default selection
    val (defaultYear, defaultMonth) = remember {
        val year = getCurrentYear(clock)
        val month = getCurrentMonth(clock)
        if (month == 1) {
            Pair(year - 1, 12)
        } else {
            Pair(year, month - 1)
        }
    }

    var selectedYear by remember { mutableStateOf(defaultYear) }
    var selectedMonth by remember { mutableStateOf(defaultMonth) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    val months = remember { getMonthsList() }

    // Notify interactor when period changes
    LaunchedEffect(selectedYear, selectedMonth) {
        viewModel.handleEvent(HomeEvent.FilterByMonth(selectedMonth, selectedYear))
    }

    val selectedMonthName = months.find { it.first == selectedMonth }?.second?.let { stringResource(it) } ?: ""
    
    // Header always shows previous month
    val (headerMonthNum, headerYear) = remember<Pair<Int, Int>> {
        calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
    }
    val headerMonth = months.find { it.first == headerMonthNum }?.second?.let { stringResource(it) } ?: ""

    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(BreadcrumbItem(stringResource(Res.string.nav_home))),
        selectedMonth = headerMonth,
        selectedYear = headerYear,
        showRefreshButton = true,
        isRefreshing = (uiState as? HomeState.Success)?.isRefreshing ?: false,
        onNavigate = onNavigate,
        onMenuClick = onMenuClick,
        onRefreshClick = { viewModel.handleEvent(HomeEvent.Refresh) }
    ) {
        // Period Selector Card
        item {
            PeriodSelectorCard(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedMonthName = selectedMonthName,
                years = (uiState as? HomeState.Success)?.availableYears ?: emptyList(),
                months = months,
                showYearDropdown = showYearDropdown,
                showMonthDropdown = showMonthDropdown,
                onYearDropdownChange = { showYearDropdown = it },
                onMonthDropdownChange = { showMonthDropdown = it },
                onYearSelected = { selectedYear = it },
                onMonthSelected = { selectedMonth = it },
                title = stringResource(Res.string.home_select_period),
                yearLabel = stringResource(Res.string.home_year),
                monthLabel = stringResource(Res.string.home_month)
            )
        }

        // Content based on state
        when (val state = uiState) {
            is HomeState.Loading -> {
                item {
                    LoadingContent()
                }
            }

            is HomeState.Success -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticsCards(
                            monthlyExpenses = state.monthlyExpenses,
                            monthlyIncome = state.monthlyIncome,
                            monthlyBalance = state.monthlyBalance,
                            missingCongregationsCount = state.missingCongregations.size,
                            totalPublishers = state.totalPublishers,
                            congregationCount = state.congregationCount,
                            publisherExpectedContribution = state.perPublisherExpense,
                            selectedMonth = selectedMonthName,
                            selectedYear = state.selectedYear
                        )

                        AnnualSummarySection(
                            yearlyIncome = state.yearlyIncome,
                            yearlyExpenses = state.yearlyExpenses,
                            yearlyBalance = state.yearlyBalance,
                            perPublisherExpense = state.perPublisherExpense,
                            totalPublishers = state.totalPublishers,
                            congregationCount = state.congregationCount,
                            selectedYear = state.selectedYear
                        )
                    }
                }

                // Missing Congregations Alert
                if (state.missingCongregations.isNotEmpty()) {
                    item {
                        MissingCongregationsAlert(
                            missingCongregations = state.missingCongregations,
                            expectedAmount = state.expectedDonationPerCongregation,
                            selectedMonth = selectedMonthName,
                            selectedYear = state.selectedYear
                        )
                    }
                }
            }

            is HomeState.Error -> {
                item {
                    ErrorContent(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun StatisticsCards(
    monthlyExpenses: Double,
    monthlyIncome: Double,
    monthlyBalance: Double,
    missingCongregationsCount: Int,
    totalPublishers: Int,
    congregationCount: Int,
    publisherExpectedContribution: Double,
    selectedMonth: String,
    selectedYear: Int
) {

    // Row 1: Expenses and Donations
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = stringResource(Res.string.stat_monthly_expenses),
            amount = monthlyExpenses.formatCurrency(),
            amountColor = MaterialTheme.colorScheme.error,
            subtitle = "$selectedMonth $selectedYear",
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = stringResource(Res.string.stat_monthly_donations),
            amount = monthlyIncome.formatCurrency(),
            amountColor = MaterialTheme.colorScheme.tertiary,
            subtitle = stringResource(Res.string.stat_balance, monthlyBalance.formatCurrency()),
            subtitleColor = if (monthlyBalance >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }

    // Row 2: Per Publisher and Missing Congregations
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = stringResource(Res.string.stat_per_publisher),
            amount = publisherExpectedContribution.formatCurrency(),
            amountColor = MaterialTheme.colorScheme.primary,
            subtitle = stringResource(Res.string.stat_for_publishers, totalPublishers),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = stringResource(Res.string.stat_missing_congregations),
            amount = missingCongregationsCount.toString(),
            amountColor = if (missingCongregationsCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
            subtitle = stringResource(Res.string.stat_of_congregations, congregationCount),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MissingCongregationsAlert(
    missingCongregations: List<MissingCongregation>,
    expectedAmount: Double,
    selectedMonth: String,
    selectedYear: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.alert_missing_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.alert_missing_description, selectedMonth, selectedYear),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(12.dp))

            missingCongregations.forEach { missing ->
                MissingCongregationCard(
                    missingCongregation = missing,
                    expectedAmount = expectedAmount
                )
            }
        }
    }
}

@Composable
private fun MissingCongregationCard(
    missingCongregation: MissingCongregation,
    expectedAmount: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = missingCongregation.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.alert_donated_amount, missingCongregation.donated.formatCurrency()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.alert_expected_amount, missingCongregation.expected.formatCurrency()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.alert_missing_amount, missingCongregation.missing.formatCurrency()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AnnualSummarySection(
    yearlyIncome: Double,
    yearlyExpenses: Double,
    yearlyBalance: Double,
    perPublisherExpense: Double,
    totalPublishers: Int,
    congregationCount: Int,
    selectedYear: Int
) {
    val avgPerCongregation = if (congregationCount > 0) totalPublishers / congregationCount else 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Annual Summary
        Card(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.annual_summary_title, selectedYear),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                SummaryRow(
                    label = stringResource(Res.string.annual_total_expenses),
                    value = yearlyExpenses.formatCurrency(),
                    valueColor = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
                
                SummaryRow(
                    label = stringResource(Res.string.annual_total_donations),
                    value = yearlyIncome.formatCurrency(),
                    valueColor = MaterialTheme.colorScheme.tertiary
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SummaryRow(
                    label = stringResource(Res.string.annual_balance),
                    value = yearlyBalance.formatCurrency(),
                    valueColor = if (yearlyBalance >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    isLarge = true
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SummaryRow(
                    label = stringResource(Res.string.annual_per_publisher),
                    value = perPublisherExpense.formatCurrency(),
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        // General Information
        Card(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.general_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                SummaryRow(
                    label = stringResource(Res.string.general_total_publishers),
                    value = totalPublishers.toString()
                )
                Spacer(Modifier.height(12.dp))
                
                SummaryRow(
                    label = stringResource(Res.string.general_total_congregations),
                    value = congregationCount.toString()
                )
                Spacer(Modifier.height(12.dp))
                
                SummaryRow(
                    label = stringResource(Res.string.general_avg_per_congregation),
                    value = avgPerCongregation.toString()
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isLarge: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = if (isLarge) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    amount: String,
    amountColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }
    }
}

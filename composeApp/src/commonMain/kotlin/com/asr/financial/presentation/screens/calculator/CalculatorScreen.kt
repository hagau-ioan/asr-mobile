package com.asr.financial.presentation.screens.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.CalculatorEvent
import com.asr.financial.presentation.mvi.state.CalculatorState
import com.asr.financial.presentation.mvi.viewmodel.CalculatorViewModel
import com.asr.financial.presentation.screens.calculator.components.CongregationContributionRow
import com.asr.financial.presentation.screens.calculator.components.ContributionCard
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getMonthName
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CalculatorScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: CalculatorViewModel = koinViewModel(),
    clock: Clock = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedYear by remember { mutableStateOf(getCurrentYear(clock)) }
    var selectedMonth by remember { mutableStateOf(getCurrentMonth(clock)) }

    LaunchedEffect(selectedYear, selectedMonth) {
        viewModel.handleEvent(CalculatorEvent.LoadData(selectedYear, selectedMonth))
    }

    when (val state = uiState) {
        is CalculatorState.Success -> {
            CalculatorSuccessContent(
                state = state,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                onYearChange = { selectedYear = it },
                onMonthChange = { selectedMonth = it },
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }
        is CalculatorState.Loading -> {
            CalculatorLoadingContent(
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }
        is CalculatorState.Error -> {
            CalculatorErrorContent(
                message = state.message,
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }
    }
}

@Composable
private fun CalculatorSuccessContent(
    state: CalculatorState.Success,
    selectedYear: Int,
    selectedMonth: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
            BreadcrumbItem(stringResource(Res.string.nav_calculator))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
                    Text(
                        text = stringResource(Res.string.calculator_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.calculator_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            PeriodSelector(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                onYearChange = onYearChange,
                onMonthChange = onMonthChange
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ContributionCard(
                        title = stringResource(Res.string.calculator_monthly_title),
                        contribution = state.monthlyContribution,
                        periodLabel = stringResource(
                            Res.string.calculator_for_month,
                            getMonthName(selectedMonth),
                            selectedYear
                        ),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    ContributionCard(
                        title = stringResource(Res.string.calculator_yearly_title),
                        contribution = state.yearlyContribution,
                        periodLabel = stringResource(Res.string.calculator_for_year, selectedYear),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(Res.string.calculator_distribution_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        state.congregationContributions.forEach { contribution ->
            item {
                CongregationContributionRow(contribution = contribution)
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedYear: Int,
    selectedMonth: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING_DP.dp),
            horizontalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.select_year),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                YearDropdown(selectedYear = selectedYear, onYearChange = onYearChange)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.select_month),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                MonthDropdown(selectedMonth = selectedMonth, onMonthChange = onMonthChange)
            }
        }
    }
}

@Composable
private fun YearDropdown(selectedYear: Int, onYearChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val years = (2024..2027).toList()

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedYear.toString())
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearChange(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MonthDropdown(selectedMonth: Int, onMonthChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val months = (1..12).toList()

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(getMonthName(selectedMonth))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(getMonthName(month)) },
                    onClick = {
                        onMonthChange(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CalculatorLoadingContent(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
            BreadcrumbItem(stringResource(Res.string.nav_calculator))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun CalculatorErrorContent(
    message: String,
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
            BreadcrumbItem(stringResource(Res.string.nav_calculator))
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
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(CARD_PADDING_DP.dp)
                )
            }
        }
    }
}

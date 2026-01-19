package com.asr.financial.presentation.screens.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.CalculatorEvent
import com.asr.financial.presentation.mvi.interactor.CalculatorMessages
import com.asr.financial.presentation.mvi.state.CalculatorState
import com.asr.financial.presentation.mvi.viewmodel.CalculatorViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.calculator.components.CongregationContributionRow
import com.asr.financial.presentation.screens.calculator.components.ContributionCard
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getMonthAbbreviationResource
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
    
    val (selectedMonth, selectedYear) = remember {
        val (prevMonth, prevYear) = calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
        prevMonth to prevYear
    }

    LaunchedEffect(Unit) {
        viewModel.handleEvent(CalculatorEvent.LoadData(selectedYear, selectedMonth))
    }

    when (val state = uiState) {
        is CalculatorState.Success -> {
            CalculatorSuccessContent(
                state = state,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }
        is CalculatorState.Loading -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_calculator))
                ),
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            ) {
                item {
                    LoadingContent()
                }
            }
        }
        is CalculatorState.Error -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_calculator))
                ),
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            ) {
                item {
                    ErrorContent(message = getErrorMessage(state.message))
                }
            }
        }
    }
}

@Composable
private fun CalculatorSuccessContent(
    state: CalculatorState.Success,
    selectedYear: Int,
    selectedMonth: Int,
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val selectedMonthName = getMonthAbbreviationResource(selectedMonth)?.let { stringResource(it) } ?: ""
    
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
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
                            selectedMonthName,
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

/**
 * Get error message from error key
 */
@Composable
private fun getErrorMessage(errorKey: String): String {
    return when (errorKey) {
        "config_error_load_failed" -> stringResource(Res.string.config_error_load_failed)
        "data_error_load_failed" -> stringResource(Res.string.data_error_load_failed)
        CalculatorMessages.ERROR_NO_PUBLISHERS -> stringResource(Res.string.calculator_error_no_publishers)
        CalculatorMessages.ERROR_UNKNOWN -> stringResource(Res.string.calculator_error_unknown)
        else -> stringResource(Res.string.calculator_error_unknown)
    }
}

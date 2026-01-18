package com.asr.financial.presentation.screens.yearly

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.YearlyEvent
import com.asr.financial.presentation.mvi.state.YearlyState
import com.asr.financial.presentation.mvi.viewmodel.YearlyViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.charts.BarChart
import com.asr.financial.presentation.screens.yearly.YearlyConstants.CHART_HEIGHT_DP
import com.asr.financial.presentation.screens.yearly.components.YearSummaryCard
import com.asr.financial.presentation.screens.yearly.components.YearVariationCard
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.EMPTY_STATE_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.getCurrentYear
import asr_financial.composeapp.generated.resources.*
import com.asr.financial.presentation.ui.constants.AppConstants
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun YearlyScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: YearlyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleEvent(YearlyEvent.LoadData)
    }

    when (val state = uiState) {
        is YearlyState.Success -> {
            YearlySuccessContent(
                state = state,
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }
        is YearlyState.Loading -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_yearly))
                ),
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            ) {
                item {
                    LoadingContent()
                }
            }
        }
        is YearlyState.Error -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_yearly))
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
private fun YearlySuccessContent(
    state: YearlyState.Success,
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_yearly))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        if (state.yearlyStats.isEmpty()) {
            item {
                EmptyYearlyCard()
            }
        } else {
            item {
                YearlyChartCard(yearlyStats = state.yearlyStats.sortedBy { it.year })
            }

            item {
                Text(
                    text = stringResource(Res.string.yearly_summary_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            state.yearlyStats.forEach { stat ->
                item {
                    YearSummaryCard(stat = stat)
                }
            }

            if (state.comparisons.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(SECTION_SPACING_DP.dp))
                    Text(
                        text = stringResource(Res.string.yearly_variations_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                state.comparisons.forEach { comparison ->
                    item {
                        YearVariationCard(comparison = comparison)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyYearlyCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EMPTY_STATE_PADDING_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.yearly_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun YearlyChartCard(
    yearlyStats: List<YearlyStat>,
    clock: Clock = koinInject()
) {
    val currentYear = remember { getCurrentYear(clock) }
    val allYears = remember(yearlyStats, currentYear) {
        val dataYears = yearlyStats.map { it.year }
        val minYear = dataYears.minOrNull() ?: currentYear
        val maxYear = dataYears.maxOrNull() ?: currentYear
        (minYear..maxYear).toList()
    }
    
    val allStats = remember(allYears, yearlyStats) {
        allYears.map { year ->
            yearlyStats.find { it.year == year } ?: YearlyStat(year, AppConstants.Defaults.ZERO_DOUBLE, AppConstants.Defaults.ZERO_DOUBLE, AppConstants.Defaults.ZERO_DOUBLE)
        }
    }
    
    var startIndex by remember { mutableStateOf((allYears.size - 3).coerceAtLeast(0)) }
    
    val displayStats = remember(allStats, startIndex) {
        allStats.drop(startIndex).take(3)
    }
    
    val displayYears = remember(allYears, startIndex) {
        allYears.drop(startIndex).take(3)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.yearly_chart_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (startIndex > 0) startIndex-- },
                        enabled = startIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.cd_previous_years))
                    }
                    
                    IconButton(
                        onClick = { if (startIndex < allYears.size - 3) startIndex++ },
                        enabled = startIndex < allYears.size - 3
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(Res.string.cd_next_years))
                    }
                }
            }
            
            Spacer(Modifier.height(SECTION_SPACING_DP.dp))
            
            BarChart(
                yearlyStats = displayStats,
                height = CHART_HEIGHT_DP
            )
        }
    }
}

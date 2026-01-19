package com.asr.financial.presentation.screens.utilities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import asr_financial.composeapp.generated.resources.Res
import asr_financial.composeapp.generated.resources.home_month
import asr_financial.composeapp.generated.resources.home_select_period
import asr_financial.composeapp.generated.resources.home_year
import asr_financial.composeapp.generated.resources.nav_home
import asr_financial.composeapp.generated.resources.nav_utilities
import asr_financial.composeapp.generated.resources.utilities_empty
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.UtilitiesEvent
import com.asr.financial.presentation.mvi.state.UtilitiesState
import com.asr.financial.presentation.mvi.viewmodel.UtilitiesViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.utilities.components.ComparisonCard
import com.asr.financial.presentation.screens.utilities.components.YearlyComparisonCard
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.period.PeriodSelectorCard
import com.asr.financial.presentation.ui.components.period.usePeriodSelectorState
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.constants.AppConstants
import com.asr.financial.presentation.ui.constants.UIConstants.EMPTY_STATE_PADDING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UtilitiesScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: UtilitiesViewModel = koinViewModel(),
    clock: Clock = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Use period selector state hook
    val periodState = usePeriodSelectorState(
        clock = clock,
        onPeriodChange = { year, month ->
            viewModel.handleEvent(UtilitiesEvent.FilterByPeriod(year, month))
        },
        initialLoadEvent = null,
        skipInitialChange = true
    )

    // Initial load with default period
    LaunchedEffect(Unit) {
        viewModel.handleEvent(UtilitiesEvent.FilterByPeriod(periodState.selectedYear, periodState.selectedMonth))
    }

    when (val state = uiState) {
        is UtilitiesState.Success -> {
            UtilitiesSuccessContent(
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
                onMenuClick = onMenuClick,
                viewModel = viewModel
            )
        }

        is UtilitiesState.Loading -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_utilities))
                ),
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            ) {
                item {
                    LoadingContent()
                }
            }
        }

        is UtilitiesState.Error -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_utilities))
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
private fun UtilitiesSuccessContent(
    state: UtilitiesState.Success,
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
    onMenuClick: () -> Unit,
    viewModel: UtilitiesViewModel
) {
    val selectedMonthName = months.find { it.first == selectedMonth }?.second?.let { stringResource(it) } ?: ""
    val previousMonthName = months.find { it.first == state.previousMonth }?.second?.let { stringResource(it) } ?: ""

    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_utilities))
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

        if (state.comparisons.all { it.currentAmount == AppConstants.Defaults.ZERO_DOUBLE && it.previousAmount == AppConstants.Defaults.ZERO_DOUBLE }) {
            item {
                EmptyUtilitiesCard()
            }
        } else {
            item {
                ComparisonCard(
                    state = state,
                    selectedMonthName = selectedMonthName,
                    previousMonthName = previousMonthName
                )
            }

            item {
                YearlyComparisonCard(
                    yearlyData = state.comparisonYearlyData,
                    comparisonYear = state.comparisonYear,
                    // Fallback to default start year if no years available (should not happen in normal operation)
                    minYear = (state.availableYears.firstOrNull() ?: AppConstants.Business.DEFAULT_START_YEAR) + 1,
                    maxYear = state.selectedYear,
                    onYearChange = { year ->
                        viewModel.handleEvent(UtilitiesEvent.ChangeComparisonYear(year))
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyUtilitiesCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EMPTY_STATE_PADDING_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.utilities_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

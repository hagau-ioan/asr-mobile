package com.asr.financial.presentation.screens.utilities

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.UtilitiesEvent
import com.asr.financial.presentation.mvi.state.UtilitiesState
import com.asr.financial.presentation.mvi.viewmodel.UtilitiesViewModel
import com.asr.financial.presentation.screens.utilities.components.ComparisonCard
import com.asr.financial.presentation.screens.utilities.components.YearlyComparisonCard
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.period.PeriodSelectorCard
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.DEFAULT_MONTH
import com.asr.financial.presentation.ui.constants.UIConstants.DEFAULT_YEAR
import com.asr.financial.presentation.ui.constants.UIConstants.EMPTY_STATE_PADDING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getAvailableYears
import com.asr.financial.utils.getMonthsList
import asr_financial.composeapp.generated.resources.*
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

    var selectedYear by remember { mutableStateOf(DEFAULT_YEAR) }
    var selectedMonth by remember { mutableStateOf(DEFAULT_MONTH) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    val years = remember { getAvailableYears(clock) }
    val months = remember { getMonthsList() }

    LaunchedEffect(selectedYear, selectedMonth) {
        viewModel.handleEvent(UtilitiesEvent.FilterByPeriod(selectedYear, selectedMonth))
    }

    when (val state = uiState) {
        is UtilitiesState.Success -> {
            UtilitiesSuccessContent(
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
                onMenuClick = onMenuClick,
                viewModel = viewModel
            )
        }

        is UtilitiesState.Loading -> {
            UtilitiesLoadingContent(
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
        }

        is UtilitiesState.Error -> {
            UtilitiesErrorContent(
                message = state.message,
                windowSizeClass = windowSizeClass,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            )
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
    months: List<Pair<Int, org.jetbrains.compose.resources.StringResource>>,
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
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
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

        if (state.comparisons.all { it.currentAmount == 0.0 && it.previousAmount == 0.0 }) {
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
private fun UtilitiesLoadingContent(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
            BreadcrumbItem(stringResource(Res.string.nav_utilities))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun UtilitiesErrorContent(
    message: String,
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
            BreadcrumbItem(stringResource(Res.string.nav_utilities))
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

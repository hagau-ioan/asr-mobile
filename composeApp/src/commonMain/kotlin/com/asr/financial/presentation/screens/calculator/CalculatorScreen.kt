package com.asr.financial.presentation.screens.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.platform.Clipboard
import com.asr.financial.platform.Clock
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getMonthsList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.koin.compose.koinInject
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
import com.asr.financial.utils.formatCurrency
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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

    val months = remember { getMonthsList() }
    val (headerMonthNum, headerYear) = calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
    val headerMonthName = months.find { it.first == headerMonthNum }?.second?.let { stringResource(it) } ?: ""

    LaunchedEffect(Unit) {
        viewModel.handleEvent(CalculatorEvent.LoadData)
    }

    when (val state = uiState) {
        is CalculatorState.Success -> {
            CalculatorSuccessContent(
                state = state,
                windowSizeClass = windowSizeClass,
                headerMonthName = headerMonthName,
                headerYear = headerYear,
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
                selectedMonth = headerMonthName,
                selectedYear = headerYear,
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
                selectedMonth = headerMonthName,
                selectedYear = headerYear,
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
    windowSizeClass: WindowSizeClass,
    headerMonthName: String,
    headerYear: Int,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_calculator))
        ),
        selectedMonth = headerMonthName,
        selectedYear = headerYear,
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
                        periodLabel = stringResource(Res.string.calculator_based_on_12_months),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    ContributionCard(
                        title = stringResource(Res.string.calculator_yearly_title),
                        contribution = state.yearlyContribution,
                        periodLabel = stringResource(Res.string.calculator_based_on_12_months),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // Situatie Curenta ASR Section
        state.situatieCurentaAsr?.let { situatie ->
            item {
                Spacer(Modifier.height(SECTION_SPACING_DP.dp))
            }
            item {
                SituatieCurentaAsrSection(situatie = situatie)
            }
        }

        item {
            Spacer(Modifier.height(SECTION_SPACING_DP.dp))
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
private fun SituatieCurentaAsrSection(
    situatie: com.asr.financial.domain.models.SituatieCurentaAsr,
    clipboard: Clipboard = koinInject()
) {
    val scope = rememberCoroutineScope()
    var showCopySuccess by remember { mutableStateOf(false) }
    
    LaunchedEffect(showCopySuccess) {
        if (showCopySuccess) {
            kotlinx.coroutines.delay(2000)
            showCopySuccess = false
        }
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.calculator_situatie_curenta_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            val textToCopy = formatSituatieForClipboard(situatie)
                            if (clipboard.copyToClipboard(textToCopy)) {
                                showCopySuccess = true
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(Res.string.calculator_situatie_copy_asr),
                        tint = if (showCopySuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.calculator_data_end_date_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = situatie.endDate,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Financial metrics (ASR only; no CG in this section)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SituatieRow(
                    label = stringResource(Res.string.calculator_incasare_asr),
                    value = situatie.incasareAsr.formatCurrency(),
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
                SituatieRow(
                    label = stringResource(Res.string.calculator_plata_asr),
                    value = situatie.plataAsr.formatCurrency(),
                    valueColor = MaterialTheme.colorScheme.error
                )
                SituatieRow(
                    label = stringResource(Res.string.calculator_total_asr),
                    value = situatie.totalAsr.formatCurrency(),
                    valueColor = MaterialTheme.colorScheme.primary,
                    isLarge = true
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Informational note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.calculator_situatie_info_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SituatieRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isLarge: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isLarge) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

/**
 * Format situatie data for clipboard (ASR only; no CG in this section)
 */
private fun formatSituatieForClipboard(situatie: com.asr.financial.domain.models.SituatieCurentaAsr): String {
    return buildString {
        appendLine("Situație Curentă ASR")
        appendLine("Date până la: ${situatie.endDate}")
        appendLine()
        appendLine("Încasare ASR: ${situatie.incasareAsr.formatCurrency()}")
        appendLine("Plată ASR: ${situatie.plataAsr.formatCurrency()}")
        appendLine("Total ASR: ${situatie.totalAsr.formatCurrency()}")
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

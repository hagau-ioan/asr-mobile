package com.asr.financial.presentation.screens.congregations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.mvi.event.CongregationsEvent
import com.asr.financial.presentation.mvi.state.CongregationsState
import com.asr.financial.presentation.mvi.viewmodel.CongregationsViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.congregations.CongregationsConstants.TABLE_AMOUNT_WIDTH_DP
import com.asr.financial.presentation.screens.congregations.CongregationsConstants.TABLE_NAME_WIDTH_DP
import com.asr.financial.presentation.screens.congregations.components.CongregationRow
import com.asr.financial.presentation.theme.SuccessGreen
import com.asr.financial.presentation.theme.SuccessGreenDark
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.cards.SummaryCard
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.components.table.DataTable
import com.asr.financial.presentation.ui.components.table.TableHeaderCell
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getMonthsList
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.platform.Clock
import com.asr.financial.platform.FileSharer
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.asr.financial.utils.CongregationReportData
import com.asr.financial.utils.generateCongregationsHtml
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CongregationsScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: CongregationsViewModel = koinViewModel(),
    fileSharer: FileSharer = koinInject(),
    clock: Clock = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    val months = getMonthsList()
    
    // Header always shows previous month
    val (headerMonthNum, headerYear) = remember<Pair<Int, Int>> {
        calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
    }
    val headerMonth = months.find { it.first == headerMonthNum }?.second?.let { stringResource(it) } ?: ""
    
    when (val state = uiState) {
        is CongregationsState.Success -> {
            val selectedMonthName = months.find { it.first == state.selectedMonth }?.second?.let { stringResource(it) } ?: ""
            val title = stringResource(Res.string.congregations_title, "$selectedMonthName ${state.selectedYear}")
            
            val onExport: () -> Unit = {
                println("Export button clicked!")
                scope.launch {
                    try {
                        println("Generating PDF...")
                        
                        val headers = listOf("Congregație", "Donat", "Așteptat", "Diferență")
                        val rows = state.stats.map { stat ->
                            listOf(
                                stat.name,
                                "${stat.donated.toInt()} RON",
                                "${stat.expected.toInt()} RON",
                                "${stat.difference.toInt()} RON"
                            )
                        } + listOf(
                            listOf(
                                "TOTAL",
                                "${state.totalDonated.toInt()} RON",
                                "${state.totalExpected.toInt()} RON",
                                "${state.totalDifference.toInt()} RON"
                            )
                        )
                        
                        val result = fileSharer.shareTableAsPdf(
                            title = title,
                            headers = headers,
                            rows = rows,
                            fileName = "congregatii_${state.selectedYear}_${state.selectedMonth}.pdf"
                        )
                        
                        println("Share result: $result")
                    } catch (e: Exception) {
                        println("Error during export: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
            
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_congregations))
                ),
                selectedMonth = headerMonth,
                selectedYear = headerYear,
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            ) {
                item {
                    CongregationsContent(
                        stats = state.stats,
                        period = "$selectedMonthName ${state.selectedYear}",
                        totalDonated = state.totalDonated,
                        totalExpected = state.totalExpected,
                        totalDifference = state.totalDifference,
                        missingCount = state.missingCount,
                        onPeriodChange = { year, month ->
                            viewModel.handleEvent(CongregationsEvent.FilterByPeriod(year, month))
                        },
                        onExportClick = onExport
                    )
                }
            }
        }
        is CongregationsState.Loading -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_congregations))
                ),
                onNavigate = onNavigate,
                onMenuClick = onMenuClick
            ) {
                item {
                    LoadingContent()
                }
            }
        }
        is CongregationsState.Error -> {
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                    BreadcrumbItem(stringResource(Res.string.nav_congregations))
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
private fun CongregationsContent(
    stats: List<CongregationStat>,
    period: String,
    totalDonated: Double,
    totalExpected: Double,
    totalDifference: Double,
    missingCount: Int,
    onPeriodChange: (Int, Int) -> Unit = { _, _ -> },
    onExportClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Title with Export button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.congregations_title, period),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onExportClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(Res.string.cd_export),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Status message
        Text(
            text = if (missingCount > 0) {
                stringResource(
                    if (missingCount == 1) Res.string.congregations_missing_count 
                    else Res.string.congregations_missing_count_plural,
                    missingCount
                    )
                } else {
                    stringResource(Res.string.congregations_all_complete)
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (missingCount > 0) MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.tertiary
            )
            
            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    label = stringResource(Res.string.congregations_total_donated),
                    value = totalDonated.formatCurrency(),
                    backgroundColor = SuccessGreen.copy(alpha = 0.1f),
                    borderColor = SuccessGreen.copy(alpha = 0.3f),
                    valueColor = SuccessGreenDark,
                    modifier = Modifier.weight(1f)
                )
                
                SummaryCard(
                    label = stringResource(Res.string.congregations_total_expected),
                    value = totalExpected.formatCurrency(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                
                SummaryCard(
                    label = stringResource(Res.string.congregations_difference),
                    value = totalDifference.formatCurrency(),
                    backgroundColor = if (totalDifference >= 0) 
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    borderColor = if (totalDifference >= 0)
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    valueColor = if (totalDifference >= 0)
                        MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Table with horizontal scroll
            DataTable(
                columns = emptyList(),
                headerContent = {
                    Box(Modifier.width(32.dp)) // Icon space
                    TableHeaderCell(
                        text = stringResource(Res.string.congregations_name),
                        width = TABLE_NAME_WIDTH_DP.dp
                    )
                    TableHeaderCell(
                        text = stringResource(Res.string.congregations_donated_amount),
                        width = TABLE_AMOUNT_WIDTH_DP.dp,
                        textAlign = TextAlign.End
                    )
                    TableHeaderCell(
                        text = stringResource(Res.string.congregations_total_expected),
                        width = TABLE_AMOUNT_WIDTH_DP.dp,
                        textAlign = TextAlign.End
                    )
                    TableHeaderCell(
                        text = stringResource(Res.string.congregations_difference),
                        width = TABLE_AMOUNT_WIDTH_DP.dp,
                        textAlign = TextAlign.End
                    )
                }
            ) {
                stats.forEach { stat ->
                    CongregationRow(stat)
                }
            }
    }
}

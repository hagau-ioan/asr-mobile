package com.asr.financial.presentation.screens.congregations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.mvi.event.CongregationsEvent
import com.asr.financial.presentation.mvi.state.CongregationsState
import com.asr.financial.presentation.mvi.viewmodel.CongregationsViewModel
import com.asr.financial.presentation.theme.SuccessGreen
import com.asr.financial.presentation.theme.SuccessGreenDark
import com.asr.financial.presentation.theme.WarningOrange
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.formatCurrency
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class CongregationStat(
    val name: String,
    val donated: Double,
    val expected: Double,
    val difference: Double,
    val lastDonation: String?,
    val isMissing: Boolean
)

@Composable
fun CongregationsScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: CongregationsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val months = listOf(
        1 to stringResource(Res.string.month_january),
        2 to stringResource(Res.string.month_february),
        3 to stringResource(Res.string.month_march),
        4 to stringResource(Res.string.month_april),
        5 to stringResource(Res.string.month_may),
        6 to stringResource(Res.string.month_june),
        7 to stringResource(Res.string.month_july),
        8 to stringResource(Res.string.month_august),
        9 to stringResource(Res.string.month_september),
        10 to stringResource(Res.string.month_october),
        11 to stringResource(Res.string.month_november),
        12 to stringResource(Res.string.month_december)
    )
    
    when (val state = uiState) {
        is CongregationsState.Success -> {
            val selectedMonthName = months.find { it.first == state.selectedMonth }?.second ?: ""
            
            ScreenLayout(
                windowSizeClass = windowSizeClass,
                breadcrumbItems = listOf(
                    BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
                    BreadcrumbItem(stringResource(Res.string.nav_congregations))
                ),
                selectedMonth = selectedMonthName,
                selectedYear = state.selectedYear,
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
                        }
                    )
                }
            }
        }
        is CongregationsState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is CongregationsState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
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
    onPeriodChange: (Int, Int) -> Unit = { _, _ -> }
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Title
        Text(
            text = stringResource(Res.string.congregations_title, period),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.width(32.dp)) // Icon
                        Text(
                            text = stringResource(Res.string.congregations_name),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = stringResource(Res.string.congregations_donated_amount),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = stringResource(Res.string.congregations_total_expected),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = stringResource(Res.string.congregations_difference),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = stringResource(Res.string.congregations_last_donation),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                    
                    HorizontalDivider()
                    
                    // Table Rows
                    stats.forEach { stat ->
                        CongregationRow(stat)
                        HorizontalDivider()
                    }
                }
            }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    backgroundColor: Color,
    borderColor: Color,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun CongregationRow(stat: CongregationStat) {
    Row(
        modifier = Modifier
            .background(
                if (stat.isMissing) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Icon
        Icon(
            imageVector = when {
                stat.isMissing -> Icons.Default.Error
                stat.difference < 0 -> Icons.Default.Warning
                else -> Icons.Default.CheckCircle
            },
            contentDescription = null,
            tint = when {
                stat.isMissing -> MaterialTheme.colorScheme.error
                stat.difference < 0 -> WarningOrange
                else -> MaterialTheme.colorScheme.tertiary
            },
            modifier = Modifier.size(20.dp)
        )
        
        // Name
        Text(
            text = stat.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (stat.isMissing) FontWeight.Bold else FontWeight.Normal,
            color = if (stat.isMissing) MaterialTheme.colorScheme.error 
                   else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(120.dp)
        )
        
        // Donated Amount
        Text(
            text = stat.donated.formatCurrency(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (stat.isMissing) FontWeight.Medium else FontWeight.Normal,
            color = if (stat.isMissing) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(100.dp)
        )
        
        // Expected Amount
        Text(
            text = stat.expected.formatCurrency(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(100.dp)
        )
        
        // Difference
        Text(
            text = "${if (stat.difference >= 0) "+" else ""}${stat.difference.formatCurrency()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = when {
                stat.difference >= 0 -> MaterialTheme.colorScheme.tertiary
                stat.isMissing -> MaterialTheme.colorScheme.error
                else -> WarningOrange
            },
            textAlign = TextAlign.End,
            modifier = Modifier.width(100.dp)
        )
        
        // Last Donation
        Text(
            text = stat.lastDonation ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(100.dp)
        )
    }
}

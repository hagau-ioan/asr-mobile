package com.asr.financial.presentation.screens.utilities.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.screens.utilities.UtilityComparison
import com.asr.financial.presentation.screens.utilities.UtilitiesConstants.TABLE_AMOUNT_WIDTH_DP
import com.asr.financial.presentation.screens.utilities.UtilitiesConstants.TABLE_CATEGORY_WIDTH_DP
import com.asr.financial.presentation.screens.utilities.UtilitiesConstants.TABLE_PERCENTAGE_WIDTH_DP
import com.asr.financial.presentation.ui.components.table.DataTable
import com.asr.financial.presentation.ui.components.table.TableCell
import com.asr.financial.presentation.ui.components.table.TableHeaderCell
import com.asr.financial.presentation.ui.components.table.TableRow
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.formatPercentage
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Table showing detailed utility comparison by category
 */
@Composable
fun ComparisonTable(
    comparisons: List<UtilityComparison>,
    selectedMonthName: String,
    selectedYear: Int,
    previousMonthName: String,
    previousYear: Int,
    totalDifference: Double,
    totalPercentage: Double,
    currentTotal: Double,
    previousTotal: Double
) {
    DataTable(
        columns = emptyList(),
        headerContent = {
            TableHeaderCell(
                text = stringResource(Res.string.utilities_comparison_title),
                width = TABLE_CATEGORY_WIDTH_DP.dp
            )
            TableHeaderCell(
                text = previousMonthName,
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End
            )
            TableHeaderCell(
                text = stringResource(Res.string.utilities_trend),
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.Center
            )
            TableHeaderCell(
                text = selectedMonthName,
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End
            )
        }
    ) {
        // Data Rows
        comparisons.forEach { comparison ->
            TableRow {
                TableCell(
                    text = comparison.category,
                    width = TABLE_CATEGORY_WIDTH_DP.dp,
                    fontWeight = FontWeight.Medium
                )
                TableCell(
                    text = comparison.previousAmount.formatCurrency(),
                    width = TABLE_AMOUNT_WIDTH_DP.dp,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TrendCell(
                    difference = comparison.difference,
                    percentage = comparison.percentageChange,
                    width = TABLE_AMOUNT_WIDTH_DP.dp
                )
                TableCell(
                    text = comparison.currentAmount.formatCurrency(),
                    width = TABLE_AMOUNT_WIDTH_DP.dp,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Total Row
        TableRow(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            TableCell(
                text = stringResource(Res.string.utilities_total),
                width = TABLE_CATEGORY_WIDTH_DP.dp,
                fontWeight = FontWeight.Bold
            )
            TableCell(
                text = previousTotal.formatCurrency(),
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold
            )
            TrendCell(
                difference = totalDifference,
                percentage = totalPercentage,
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                isBold = true
            )
            TableCell(
                text = currentTotal.formatCurrency(),
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TrendCell(
    difference: Double,
    percentage: Double,
    width: Dp,
    isBold: Boolean = false
) {
    val isDecrease = difference < 0
    val trendColor = if (isDecrease) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val trendIcon = if (isDecrease) "↓" else if (difference > 0) "↑" else "="
    
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = trendIcon,
                style = MaterialTheme.typography.titleMedium,
                color = trendColor,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = percentage.formatPercentage(),
                style = MaterialTheme.typography.bodySmall,
                color = trendColor,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

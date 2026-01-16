package com.asr.financial.presentation.screens.utilities.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                text = "$selectedMonthName $selectedYear",
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End
            )
            TableHeaderCell(
                text = "$previousMonthName $previousYear",
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End
            )
            TableHeaderCell(
                text = stringResource(Res.string.utilities_difference),
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End
            )
            TableHeaderCell(
                text = stringResource(Res.string.utilities_variation),
                width = TABLE_PERCENTAGE_WIDTH_DP.dp,
                textAlign = TextAlign.End
            )
            Box(modifier = Modifier.width(24.dp))
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
                    text = comparison.currentAmount.formatCurrency(),
                    width = TABLE_AMOUNT_WIDTH_DP.dp,
                    textAlign = TextAlign.End
                )
                TableCell(
                    text = comparison.previousAmount.formatCurrency(),
                    width = TABLE_AMOUNT_WIDTH_DP.dp,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TableCell(
                    text = "${if (comparison.difference >= 0) "+" else ""}${comparison.difference.formatCurrency()}",
                    width = TABLE_AMOUNT_WIDTH_DP.dp,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        comparison.difference > 0 -> MaterialTheme.colorScheme.error
                        comparison.difference < 0 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                TableCell(
                    text = comparison.percentageChange.formatPercentage(),
                    width = TABLE_PERCENTAGE_WIDTH_DP.dp,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        comparison.percentageChange > 0 -> MaterialTheme.colorScheme.error
                        comparison.percentageChange < 0 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                TrendIcon(comparison.difference)
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
                text = currentTotal.formatCurrency(),
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold
            )
            TableCell(
                text = previousTotal.formatCurrency(),
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold
            )
            TableCell(
                text = "${if (totalDifference >= 0) "+" else ""}${totalDifference.formatCurrency()}",
                width = TABLE_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                color = when {
                    totalDifference > 0 -> MaterialTheme.colorScheme.error
                    totalDifference < 0 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            TableCell(
                text = totalPercentage.formatPercentage(),
                width = TABLE_PERCENTAGE_WIDTH_DP.dp,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                color = when {
                    totalPercentage > 0 -> MaterialTheme.colorScheme.error
                    totalPercentage < 0 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            TrendIcon(totalDifference)
        }
    }
}

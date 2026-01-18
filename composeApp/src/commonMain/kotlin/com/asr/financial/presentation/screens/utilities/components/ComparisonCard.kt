package com.asr.financial.presentation.screens.utilities.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.mvi.state.UtilitiesState
import com.asr.financial.presentation.ui.components.cards.SummaryCard
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SMALL_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.TINY_SPACING_DP
import com.asr.financial.presentation.ui.constants.AppConstants
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.formatPercentage
import asr_financial.composeapp.generated.resources.*
import kotlin.math.abs
import org.jetbrains.compose.resources.stringResource

/**
 * Card showing month-to-month comparison with summary cards and detailed table
 */
@Composable
fun ComparisonCard(
    state: UtilitiesState.Success,
    selectedMonthName: String,
    previousMonthName: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.utilities_comparison_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(TINY_SPACING_DP.dp))
        Text(
            text = "$selectedMonthName ${state.selectedYear} vs $previousMonthName ${state.previousYear}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(SECTION_SPACING_DP.dp))

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SMALL_SPACING_DP.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Month (Left)
            SummaryCard(
                label = previousMonthName,
                value = state.previousTotal.formatCurrency(),
                backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AppConstants.UI.LOW_ALPHA),
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            )
            
            // Trend Indicator (Center)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = SMALL_SPACING_DP.dp)
            ) {
                val isDecrease = state.totalDifference < 0
                val trendColor = if (isDecrease) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                val trendIcon = if (isDecrease) "↓" else "↑"
                val trendText = if (isDecrease) 
                    stringResource(Res.string.utilities_decreased) 
                else 
                    stringResource(Res.string.utilities_increased)
                
                Text(
                    text = trendIcon,
                    style = MaterialTheme.typography.displayMedium,
                    color = trendColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = abs(state.totalDifference).formatCurrency(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = trendColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = trendText,
                    style = MaterialTheme.typography.bodySmall,
                    color = trendColor
                )
                Text(
                    text = state.totalPercentage.formatPercentage(),
                    style = MaterialTheme.typography.bodySmall,
                    color = trendColor
                )
            }
            
            // Current Month (Right)
            SummaryCard(
                label = selectedMonthName,
                value = state.currentTotal.formatCurrency(),
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = AppConstants.UI.LOW_ALPHA),
                labelColor = MaterialTheme.colorScheme.primary,
                valueColor = MaterialTheme.colorScheme.primary,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(SECTION_SPACING_DP.dp))

        // Comparison Table
        ComparisonTable(
            comparisons = state.comparisons,
            selectedMonthName = selectedMonthName,
            selectedYear = state.selectedYear,
            previousMonthName = previousMonthName,
            previousYear = state.previousYear,
            totalDifference = state.totalDifference,
            totalPercentage = state.totalPercentage,
            currentTotal = state.currentTotal,
            previousTotal = state.previousTotal
        )
    }
}

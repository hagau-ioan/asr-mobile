package com.asr.financial.presentation.screens.yearly.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.screens.yearly.YearlyComparison
import com.asr.financial.presentation.screens.yearly.YearlyConstants.TREND_ICON_SIZE_DP
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SMALL_SPACING_DP
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.formatPercentage
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun YearVariationCard(comparison: YearlyComparison) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text(
                text = "${comparison.previousYear} → ${comparison.currentYear}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(SMALL_SPACING_DP.dp))
            
            VariationRow(
                label = stringResource(Res.string.yearly_donations),
                change = comparison.donationsChange,
                percentage = comparison.donationsChangePercent,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            VariationRow(
                label = stringResource(Res.string.yearly_expenses),
                change = comparison.expensesChange,
                percentage = comparison.expensesChangePercent,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun VariationRow(
    label: String,
    change: Double,
    percentage: Double,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (change >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = if (change >= 0) color else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(TREND_ICON_SIZE_DP.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${change.formatCurrency()} (${percentage.formatPercentage()})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (change >= 0) color else MaterialTheme.colorScheme.error
            )
        }
    }
}

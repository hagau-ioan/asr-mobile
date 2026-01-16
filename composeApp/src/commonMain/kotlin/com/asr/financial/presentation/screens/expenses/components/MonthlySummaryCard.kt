package com.asr.financial.presentation.screens.expenses.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SMALL_SPACING_DP
import com.asr.financial.utils.formatCurrency
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Card displaying total monthly expenses
 */
@Composable
fun MonthlySummaryCard(totalExpenses: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text(
                text = stringResource(Res.string.expenses_total_monthly),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(SMALL_SPACING_DP.dp))
            Text(
                text = totalExpenses.formatCurrency(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

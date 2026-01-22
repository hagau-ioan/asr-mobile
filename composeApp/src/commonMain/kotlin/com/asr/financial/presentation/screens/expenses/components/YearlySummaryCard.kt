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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Card displaying yearly expenses summary (last 12 months)
 */
@Composable
fun YearlySummaryCard(
    yearlyTotal: Double,
    yearlyEndMonth: Int,
    yearlyEndYear: Int,
    months: List<Pair<Int, StringResource>>
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text(
                text = stringResource(Res.string.expenses_total_yearly),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(SMALL_SPACING_DP.dp))
            Text(
                text = yearlyTotal.formatCurrency(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(
                    Res.string.expenses_yearly_range_full_year,
                    yearlyEndYear
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

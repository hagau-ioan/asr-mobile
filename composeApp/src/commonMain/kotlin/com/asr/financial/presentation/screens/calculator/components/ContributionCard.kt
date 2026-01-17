package com.asr.financial.presentation.screens.calculator.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.screens.calculator.ContributionCalculation
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SMALL_SPACING_DP
import com.asr.financial.utils.formatCurrency
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ContributionCard(
    title: String,
    contribution: ContributionCalculation,
    periodLabel: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            
            Text(
                text = contribution.perPublisherAmount.formatCurrency(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                maxLines = 1
            )
            
            Spacer(Modifier.height(SMALL_SPACING_DP.dp))
            
            Column {
                Text(
                    text = stringResource(Res.string.calculator_total_expenses),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = contribution.totalExpenses.formatCurrency(),
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White,
                    maxLines = 1
                )
            }
            
            Spacer(Modifier.height(SMALL_SPACING_DP.dp))
            
            Column {
                Text(
                    text = stringResource(Res.string.calculator_number_publishers),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = contribution.numberOfPublishers.toString(),
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White,
                    maxLines = 1
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = SMALL_SPACING_DP.dp),
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)
            )
            
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

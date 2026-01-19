package com.asr.financial.presentation.ui.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Reusable summary info card component for displaying total items and total amount.
 * 
 * This component follows the common pattern used in AsrExpensesScreen and DecontScreen
 * where a card displays:
 * - A title
 * - Total items count (label + value)
 * - Total amount (label + value)
 * 
 * @param title Title text for the card
 * @param totalItemsLabel Label for total items (e.g., "Total tranzacții:")
 * @param totalItemsValue Value for total items (e.g., "42")
 * @param totalAmountLabel Label for total amount (e.g., "Total cheltuieli:")
 * @param totalAmountValue Value for total amount (e.g., "1,234.56 RON")
 * @param totalAmountColor Color for the total amount value (defaults to error color)
 * @param modifier Modifier for the card
 */
@Composable
fun SummaryInfoCard(
    title: String,
    totalItemsLabel: String,
    totalItemsValue: String,
    totalAmountLabel: String,
    totalAmountValue: String,
    totalAmountColor: Color = MaterialTheme.colorScheme.error,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = totalItemsLabel,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = totalItemsValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = totalAmountLabel,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = totalAmountValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = totalAmountColor
                )
            }
        }
    }
}

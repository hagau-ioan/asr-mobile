package com.asr.financial.presentation.screens.congregations.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.screens.congregations.CongregationStat
import com.asr.financial.presentation.screens.congregations.CongregationsConstants.STATUS_ICON_SIZE_DP
import com.asr.financial.presentation.screens.congregations.CongregationsConstants.TABLE_AMOUNT_WIDTH_DP
import com.asr.financial.presentation.screens.congregations.CongregationsConstants.TABLE_NAME_WIDTH_DP
import com.asr.financial.presentation.theme.WarningOrange
import com.asr.financial.presentation.ui.components.table.TableCell
import com.asr.financial.presentation.ui.components.table.TableRow
import com.asr.financial.utils.formatCurrency

/**
 * Table row displaying congregation donation statistics
 */
@Composable
fun CongregationRow(stat: CongregationStat) {
    TableRow(
        backgroundColor = if (stat.isMissing) 
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else Color.Transparent
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
            modifier = Modifier.size(STATUS_ICON_SIZE_DP.dp)
        )
        
        // Name
        TableCell(
            text = stat.name,
            width = TABLE_NAME_WIDTH_DP.dp,
            fontWeight = if (stat.isMissing) FontWeight.Bold else FontWeight.Normal,
            color = if (stat.isMissing) MaterialTheme.colorScheme.error 
                   else MaterialTheme.colorScheme.onSurface
        )
        
        // Donated Amount
        TableCell(
            text = stat.donated.formatCurrency(),
            width = TABLE_AMOUNT_WIDTH_DP.dp,
            textAlign = TextAlign.End,
            fontWeight = if (stat.isMissing) FontWeight.Medium else FontWeight.Normal,
            color = if (stat.isMissing) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.onSurface
        )
        
        // Expected Amount
        TableCell(
            text = stat.expected.formatCurrency(),
            width = TABLE_AMOUNT_WIDTH_DP.dp,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Difference
        TableCell(
            text = "${if (stat.difference >= 0) "+" else ""}${stat.difference.formatCurrency()}",
            width = TABLE_AMOUNT_WIDTH_DP.dp,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Medium,
            color = when {
                stat.difference >= 0 -> MaterialTheme.colorScheme.tertiary
                stat.isMissing -> MaterialTheme.colorScheme.error
                else -> WarningOrange
            }
        )
    }
}

package com.asr.financial.presentation.screens.asrexpenses.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.presentation.theme.SuccessGreen
import com.asr.financial.presentation.ui.components.table.TableCell
import com.asr.financial.presentation.ui.components.table.TableRow
import com.asr.financial.utils.formatCurrency
import androidx.compose.material3.MaterialTheme

/**
 * Check if an expense is a Decont item
 */
fun AsrExpense.isDecont(): Boolean {
    return description.contains("Decont", ignoreCase = true) ||
           category.contains("Decont", ignoreCase = true)
}

/**
 * Table row for displaying a single ASR expense
 * @param expense The expense to display
 * @param onDecontClick Callback when a Decont item is clicked (receives year and month)
 */
@Composable
fun AsrExpenseRow(
    expense: AsrExpense,
    onDecontClick: ((year: Int, month: Int) -> Unit)? = null
) {
    val isDecont = expense.isDecont()
    val textColor = if (isDecont) SuccessGreen else MaterialTheme.colorScheme.onSurface
    val amountColor = when {
        isDecont -> SuccessGreen
        expense.amount < 0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    val fontWeight = if (isDecont) FontWeight.Medium else null

    val modifier = if (isDecont && onDecontClick != null) {
        Modifier.clickable {
            // Parse year and month from expense date (format: YYYY-MM-DD)
            val parts = expense.date.split("-")
            if (parts.size >= 2) {
                val year = parts[0].toIntOrNull() ?: return@clickable
                val month = parts[1].toIntOrNull() ?: return@clickable
                onDecontClick(year, month)
            }
        }
    } else {
        Modifier
    }

    TableRow(modifier = modifier) {
        TableCell(
            text = expense.date,
            width = 100.dp,
            color = textColor,
            fontWeight = fontWeight
        )
        TableCell(
            text = expense.category,
            width = 180.dp,
            color = textColor,
            fontWeight = fontWeight
        )
        TableCell(
            text = expense.description,
            width = 300.dp,
            color = textColor,
            fontWeight = fontWeight
        )
        TableCell(
            text = expense.amount.formatCurrency(),
            width = 120.dp,
            textAlign = TextAlign.End,
            color = amountColor,
            fontWeight = fontWeight
        )
    }
}

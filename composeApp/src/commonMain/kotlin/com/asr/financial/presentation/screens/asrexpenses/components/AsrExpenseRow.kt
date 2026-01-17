package com.asr.financial.presentation.screens.asrexpenses.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asr.financial.domain.models.AsrExpense
import com.asr.financial.presentation.ui.components.table.TableCell
import com.asr.financial.presentation.ui.components.table.TableRow
import com.asr.financial.utils.formatCurrency
import androidx.compose.material3.MaterialTheme

/**
 * Table row for displaying a single ASR expense
 */
@Composable
fun AsrExpenseRow(expense: AsrExpense) {
    TableRow {
        TableCell(
            text = expense.date,
            width = 100.dp
        )
        TableCell(
            text = expense.category,
            width = 180.dp
        )
        TableCell(
            text = expense.description,
            width = 300.dp
        )
        TableCell(
            text = expense.amount.formatCurrency(),
            width = 120.dp,
            textAlign = TextAlign.End,
            color = if (expense.amount < 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
        )
    }
}

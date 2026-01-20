package com.asr.financial.presentation.ui.components.table

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import com.asr.financial.presentation.theme.isAppDarkMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Column definition for DataTable
 */
data class TableColumn(
    val header: String,
    val width: Dp,
    val textAlign: TextAlign = TextAlign.Start
)

/**
 * Reusable data table component with horizontal scroll
 */
@Composable
fun DataTable(
    columns: List<TableColumn>,
    modifier: Modifier = Modifier,
    headerContent: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                headerContent()
            }

            HorizontalDivider()

            // Content Rows
            content()
        }
    }
}

/**
 * Standard table header cell
 */
@Composable
fun RowScope.TableHeaderCell(
    text: String,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign,
        modifier = Modifier.width(width)
    )
}

/**
 * Standard table row wrapper
 */
@Composable
fun TableRow(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    content: @Composable RowScope.() -> Unit
) {
    val isDarkMode = isAppDarkMode()
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f)

    Row(
        modifier = modifier
            .background(backgroundColor)
            .drawBehind {
                val strokeWidth = 0.5.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

/**
 * Standard table cell
 */
@Composable
fun RowScope.TableCell(
    text: String,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = textAlign,
        color = color,
        fontWeight = fontWeight,
        modifier = Modifier.width(width)
    )
}

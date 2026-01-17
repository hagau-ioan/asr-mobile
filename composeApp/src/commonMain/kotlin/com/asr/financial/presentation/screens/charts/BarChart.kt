package com.asr.financial.presentation.screens.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import asr_financial.composeapp.generated.resources.Res
import asr_financial.composeapp.generated.resources.*
import com.asr.financial.presentation.screens.yearly.YearlyStat
import com.asr.financial.presentation.ui.constants.UIConstants.SMALL_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.TINY_SPACING_DP
import com.asr.financial.utils.formatForAxis
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

/**
 * Bar chart showing yearly financial data (donations, expenses, balance)
 */
@Composable
fun BarChart(
    yearlyStats: List<YearlyStat>,
    modifier: Modifier = Modifier,
    height: Int = 350
) {
    val donationsColor = MaterialTheme.colorScheme.tertiary
    val expensesColor = MaterialTheme.colorScheme.error
    val balanceColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()

    val dataKey = remember(yearlyStats) { yearlyStats.hashCode().toString() }
    
    var animationTriggered by rememberSaveable(dataKey) { mutableStateOf(false) }
    
    val animationProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "barChartAnimation"
    )

    LaunchedEffect(dataKey) {
        animationTriggered = true
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            val padding = 60f
            val bottomPadding = 80f
            val chartWidth = size.width - padding * 2
            val chartHeight = size.height - padding - bottomPadding

            val maxPositive = yearlyStats.maxOfOrNull {
                max(it.totalDonations, it.totalExpenses)
            }?.toFloat() ?: 0f
            
            val minNegative = yearlyStats.minOfOrNull { it.balance }?.coerceAtMost(0.0)?.toFloat() ?: 0f
            val maxNegative = kotlin.math.abs(minNegative)
            
            val maxValue = max(maxPositive, maxNegative)

            if (maxValue == 0f || yearlyStats.isEmpty()) return@Canvas

            val zeroY = padding + (chartHeight * (maxPositive / (maxPositive + maxNegative)))
            val barGroupWidth = chartWidth / yearlyStats.size
            val barWidth = barGroupWidth / 4
            val ySteps = 4

            // Draw Y-axis labels and grid (positive)
            for (i in 0..ySteps) {
                val value = (maxPositive / ySteps) * i
                val y = zeroY - (i.toFloat() / ySteps * (zeroY - padding))
                
                drawLine(
                    color = textColor.copy(alpha = 0.1f),
                    start = Offset(padding, y),
                    end = Offset(padding + chartWidth, y),
                    strokeWidth = 1f
                )
                
                val label = value.toDouble().formatForAxis()
                val textLayoutResult = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = padding - textLayoutResult.size.width - 8f,
                        y = y - textLayoutResult.size.height / 2
                    )
                )
            }
            
            // Draw Y-axis labels and grid (negative)
            if (minNegative < 0) {
                for (i in 1..2) {
                    val value = -(maxNegative / 2) * i
                    val y = zeroY + (i.toFloat() / 2 * (padding + chartHeight - zeroY))
                    
                    drawLine(
                        color = textColor.copy(alpha = 0.1f),
                        start = Offset(padding, y),
                        end = Offset(padding + chartWidth, y),
                        strokeWidth = 1f
                    )
                    
                    val label = value.toDouble().formatForAxis()
                    val textLayoutResult = textMeasurer.measure(
                        text = label,
                        style = TextStyle(
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = padding - textLayoutResult.size.width - 8f,
                            y = y - textLayoutResult.size.height / 2
                        )
                    )
                }
            }
            
            // Draw X-axis (zero line)
            drawLine(
                color = textColor.copy(alpha = 0.3f),
                start = Offset(padding, zeroY),
                end = Offset(padding + chartWidth, zeroY),
                strokeWidth = 2f
            )

            // Draw bars for each year
            yearlyStats.forEachIndexed { index, stat ->
                val x = padding + index * barGroupWidth + barGroupWidth / 2 - barWidth * 1.5f

                // Donations bar
                val donationsHeight = (stat.totalDonations / maxValue * (zeroY - padding)).toFloat() * animationProgress
                drawRect(
                    color = donationsColor,
                    topLeft = Offset(x, zeroY - donationsHeight),
                    size = Size(barWidth, donationsHeight)
                )

                // Expenses bar
                val expensesHeight = (stat.totalExpenses / maxValue * (zeroY - padding)).toFloat() * animationProgress
                drawRect(
                    color = expensesColor,
                    topLeft = Offset(x + barWidth, zeroY - expensesHeight),
                    size = Size(barWidth, expensesHeight)
                )

                // Balance bar (positive or negative)
                if (stat.balance >= 0) {
                    val balanceHeight = (stat.balance / maxValue * (zeroY - padding)).toFloat() * animationProgress
                    drawRect(
                        color = balanceColor,
                        topLeft = Offset(x + barWidth * 2, zeroY - balanceHeight),
                        size = Size(barWidth, balanceHeight)
                    )
                } else {
                    val balanceHeight = (kotlin.math.abs(stat.balance) / maxValue * (padding + chartHeight - zeroY)).toFloat() * animationProgress
                    drawRect(
                        color = balanceColor.copy(alpha = 0.7f),
                        topLeft = Offset(x + barWidth * 2, zeroY),
                        size = Size(barWidth, balanceHeight)
                    )
                }

                // X-axis label (year)
                val yearLabel = stat.year.toString()
                val textLayoutResult = textMeasurer.measure(
                    text = yearLabel,
                    style = TextStyle(
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = x + barWidth * 1.5f - textLayoutResult.size.width / 2,
                        y = padding + chartHeight + 10f
                    )
                )
            }
        }

        Spacer(Modifier.height(SMALL_SPACING_DP.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(
                color = donationsColor,
                label = stringResource(Res.string.yearly_donations)
            )
            Spacer(Modifier.width(SMALL_SPACING_DP.dp))
            LegendItem(
                color = expensesColor,
                label = stringResource(Res.string.yearly_expenses)
            )
            Spacer(Modifier.width(SMALL_SPACING_DP.dp))
            LegendItem(
                color = balanceColor,
                label = stringResource(Res.string.yearly_balance)
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TINY_SPACING_DP.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

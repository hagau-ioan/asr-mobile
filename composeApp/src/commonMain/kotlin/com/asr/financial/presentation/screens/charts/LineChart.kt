package com.asr.financial.presentation.screens.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import asr_financial.composeapp.generated.resources.*
import com.asr.financial.presentation.theme.ChartSecondaryGreen
import com.asr.financial.utils.formatForAxis
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

/**
 * Custom Canvas-based Line Chart component with axis labels
 */
@Composable
fun LineChart(
    currentYearData: List<Double>,
    previousYearData: List<Double>,
    currentYearLabel: String,
    previousYearLabel: String,
    modifier: Modifier = Modifier,
    height: Int = 350
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = if (isSystemInDarkTheme()) 
        androidx.compose.ui.graphics.Color.White 
    else 
        ChartSecondaryGreen
    val textColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()
    
    val dataKey = remember(currentYearData, previousYearData) { 
        currentYearData.hashCode() + previousYearData.hashCode() 
    }
    var animationPlayed by remember(dataKey) { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "lineChartAnimation"
    )

    LaunchedEffect(dataKey) {
        animationPlayed = true
    }
    
    val months = listOf(
        stringResource(Res.string.month_abbr_jan),
        stringResource(Res.string.month_abbr_feb),
        stringResource(Res.string.month_abbr_mar),
        stringResource(Res.string.month_abbr_apr),
        stringResource(Res.string.month_abbr_may),
        stringResource(Res.string.month_abbr_jun),
        stringResource(Res.string.month_abbr_jul),
        stringResource(Res.string.month_abbr_aug),
        stringResource(Res.string.month_abbr_sep),
        stringResource(Res.string.month_abbr_oct),
        stringResource(Res.string.month_abbr_nov),
        stringResource(Res.string.month_abbr_dec)
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        val padding = 60f
        val bottomPadding = 40f
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding - bottomPadding

        val maxValue = max(
            currentYearData.maxOrNull() ?: 0.0,
            previousYearData.maxOrNull() ?: 0.0
        )

        if (maxValue == 0.0) return@Canvas

        val xStep = chartWidth / (currentYearData.size - 1).coerceAtLeast(1)
        val ySteps = 4

        // Draw Y-axis labels and grid lines
        for (i in 0..ySteps) {
            val value = (maxValue / ySteps) * i
            val y = padding + chartHeight - (i.toFloat() / ySteps * chartHeight)
            
            // Grid line
            drawLine(
                color = textColor.copy(alpha = 0.1f),
                start = Offset(padding, y),
                end = Offset(padding + chartWidth, y),
                strokeWidth = 1f
            )
            
            // Y-axis label
            val label = value.formatForAxis()
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

        // Draw X-axis labels (months)
        currentYearData.forEachIndexed { index, _ ->
            val x = padding + index * xStep
            val y = padding + chartHeight + 10f
            
            val monthLabel = months.getOrNull(index) ?: ""
            val textLayoutResult = textMeasurer.measure(
                text = monthLabel,
                style = TextStyle(
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = x - textLayoutResult.size.width / 2,
                    y = y
                )
            )
        }

        // Draw current year line (solid) with animation
        val currentPath = Path()
        val pointsToShow = (currentYearData.size * animationProgress).toInt().coerceAtLeast(1)
        currentYearData.take(pointsToShow).forEachIndexed { index, value ->
            val x = padding + index * xStep
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            if (index == 0) {
                currentPath.moveTo(x, y)
            } else {
                currentPath.lineTo(x, y)
            }
        }
        drawPath(
            path = currentPath,
            color = primaryColor,
            style = Stroke(width = 4f)
        )

        // Draw previous year line (dashed) with animation
        val previousPath = Path()
        previousYearData.take(pointsToShow).forEachIndexed { index, value ->
            val x = padding + index * xStep
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            if (index == 0) {
                previousPath.moveTo(x, y)
            } else {
                previousPath.lineTo(x, y)
            }
        }
        drawPath(
            path = previousPath,
            color = secondaryColor,
            style = Stroke(
                width = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
            )
        )

        // Draw data points for current year
        currentYearData.forEachIndexed { index, value ->
            val x = padding + index * xStep
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        // Draw data points for previous year
        previousYearData.forEachIndexed { index, value ->
            val x = padding + index * xStep
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            drawCircle(
                color = secondaryColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}



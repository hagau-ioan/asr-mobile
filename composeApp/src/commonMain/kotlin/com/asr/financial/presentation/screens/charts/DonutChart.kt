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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.FULL_CIRCLE_DEGREES
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.LEGEND_ICON_SIZE_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_COLORS
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_DONUT_HOLE_RATIO
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_HEIGHT_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_SIZE_DP
import com.asr.financial.presentation.screens.expenses.ExpensesConstants.PIE_CHART_START_ANGLE
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SMALL_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.TINY_SPACING_DP
import com.asr.financial.utils.percentOfAsInt

/**
 * Data model for donut chart items
 */
data class DonutChartItem(
    val label: String,
    val value: Double
)

/**
 * Reusable Donut Chart component with legend
 */
@Composable
fun DonutChart(
    items: List<DonutChartItem>,
    modifier: Modifier = Modifier
) {
    val total = items.sumOf { it.value }
    if (total == 0.0) return

    val dataKey = remember(items) { items.hashCode().toString() }
    
    var animationTriggered by rememberSaveable(dataKey) { mutableStateOf(false) }
    
    val animationProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "donutChartAnimation"
    )

    LaunchedEffect(dataKey) {
        animationTriggered = true
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PIE_CHART_HEIGHT_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(PIE_CHART_SIZE_DP.dp)) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                var startAngle = PIE_CHART_START_ANGLE

                items.forEachIndexed { index, item ->
                    val sweepAngle = (item.value / total * FULL_CIRCLE_DEGREES).toFloat() * animationProgress
                    val color = Color(PIE_CHART_COLORS[index % PIE_CHART_COLORS.size])

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    startAngle += sweepAngle
                }

                drawCircle(
                    color = Color.White,
                    radius = radius * PIE_CHART_DONUT_HOLE_RATIO,
                    center = center
                )
            }
        }

        Spacer(Modifier.height(CARD_PADDING_DP.dp))

        items.forEachIndexed { index, item ->
            val percentage = item.value.percentOfAsInt(total)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TINY_SPACING_DP.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(LEGEND_ICON_SIZE_DP.dp)
                        .background(Color(PIE_CHART_COLORS[index % PIE_CHART_COLORS.size]))
                )
                Spacer(Modifier.width(SMALL_SPACING_DP.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

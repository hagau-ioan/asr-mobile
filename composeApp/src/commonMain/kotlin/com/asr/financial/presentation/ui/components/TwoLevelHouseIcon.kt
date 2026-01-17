package com.asr.financial.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill

@Composable
fun TwoLevelHouseIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    windowColor: Color = Color.White,
    doorColor: Color = Color(0xFF8B4513) // Brown door
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Roof
        val roofPath = Path().apply {
            moveTo(width * 0.3f, height * 0.25f)
            lineTo(width * 0.5f, height * 0.1f)
            lineTo(width * 0.7f, height * 0.25f)
            close()
        }
        drawPath(roofPath, color = color, style = Fill)
        
        // Second level
        drawRect(
            color = color,
            topLeft = Offset(width * 0.25f, height * 0.25f),
            size = androidx.compose.ui.geometry.Size(width * 0.5f, height * 0.3f)
        )
        
        // First level
        drawRect(
            color = color,
            topLeft = Offset(width * 0.2f, height * 0.55f),
            size = androidx.compose.ui.geometry.Size(width * 0.6f, height * 0.35f)
        )
        
        // Windows second level (white/light)
        drawRect(
            color = windowColor,
            topLeft = Offset(width * 0.3f, height * 0.3f),
            size = androidx.compose.ui.geometry.Size(width * 0.08f, height * 0.15f)
        )
        drawRect(
            color = windowColor,
            topLeft = Offset(width * 0.46f, height * 0.3f),
            size = androidx.compose.ui.geometry.Size(width * 0.08f, height * 0.15f)
        )
        drawRect(
            color = windowColor,
            topLeft = Offset(width * 0.62f, height * 0.3f),
            size = androidx.compose.ui.geometry.Size(width * 0.08f, height * 0.15f)
        )
        
        // Door (brown)
        drawRect(
            color = doorColor,
            topLeft = Offset(width * 0.44f, height * 0.65f),
            size = androidx.compose.ui.geometry.Size(width * 0.12f, height * 0.25f)
        )
        
        // Windows first level (white/light)
        drawRect(
            color = windowColor,
            topLeft = Offset(width * 0.26f, height * 0.65f),
            size = androidx.compose.ui.geometry.Size(width * 0.1f, height * 0.12f)
        )
        drawRect(
            color = windowColor,
            topLeft = Offset(width * 0.64f, height * 0.65f),
            size = androidx.compose.ui.geometry.Size(width * 0.1f, height * 0.12f)
        )
    }
}

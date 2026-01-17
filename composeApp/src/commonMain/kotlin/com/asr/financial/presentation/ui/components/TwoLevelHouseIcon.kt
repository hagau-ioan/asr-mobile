package com.asr.financial.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill

@Composable
fun TwoLevelHouseIcon(
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val isDark = isSystemInDarkTheme()
    val houseColor = color ?: if (isDark) Color.Black else Color.White
    val windowColor = if (isDark) Color.White else Color.Black
    
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
        drawPath(roofPath, color = houseColor, style = Fill)
        
        // Second level
        drawRect(
            color = houseColor,
            topLeft = Offset(width * 0.25f, height * 0.25f),
            size = androidx.compose.ui.geometry.Size(width * 0.5f, height * 0.3f)
        )
        
        // First level
        drawRect(
            color = houseColor,
            topLeft = Offset(width * 0.2f, height * 0.55f),
            size = androidx.compose.ui.geometry.Size(width * 0.6f, height * 0.35f)
        )
        
        // Windows second level
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
        
        // Door
        drawRect(
            color = windowColor,
            topLeft = Offset(width * 0.44f, height * 0.65f),
            size = androidx.compose.ui.geometry.Size(width * 0.12f, height * 0.25f)
        )
        
        // Windows first level
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

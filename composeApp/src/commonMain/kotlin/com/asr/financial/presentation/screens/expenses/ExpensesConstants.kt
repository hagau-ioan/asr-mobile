package com.asr.financial.presentation.screens.expenses

/**
 * Constants specific to Expenses Screen (pie chart visualization)
 */
object ExpensesConstants {
    // Pie chart dimensions
    const val PIE_CHART_HEIGHT_DP = 200
    const val PIE_CHART_SIZE_DP = 180
    const val PIE_CHART_START_ANGLE = -90f
    const val PIE_CHART_DONUT_HOLE_RATIO = 0.5f
    const val FULL_CIRCLE_DEGREES = 360f
    const val LEGEND_ICON_SIZE_DP = 16
    
    // Pie chart colors (18 distinct colors for categories)
    val PIE_CHART_COLORS = listOf(
        0xFFFF6B6B, // Vibrant Red
        0xFF4ECDC4, // Vibrant Teal
        0xFFFFE66D, // Vibrant Yellow
        0xFF95E1D3, // Vibrant Mint
        0xFFFF8B94, // Vibrant Pink
        0xFF6C5CE7, // Vibrant Purple
        0xFFFFA502, // Vibrant Orange
        0xFF26DE81, // Vibrant Green
        0xFFF06292, // Hot Pink
        0xFF42A5F5, // Sky Blue
        0xFFFFCA28, // Amber
        0xFF66BB6A, // Light Green
        0xFFAB47BC, // Deep Purple
        0xFFFF7043, // Deep Orange
        0xFF29B6F6, // Light Blue
        0xFFEF5350, // Red
        0xFF26C6DA, // Cyan
        0xFFEC407A  // Pink
    )
}

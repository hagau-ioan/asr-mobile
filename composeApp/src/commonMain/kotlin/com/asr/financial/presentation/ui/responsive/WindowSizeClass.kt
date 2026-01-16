package com.asr.financial.presentation.ui.responsive

import androidx.compose.runtime.Composable

/**
 * Window Size Classes for responsive design
 * Based on Material3 guidelines (2025)
 */
enum class WindowSizeClass {
    /**
     * Compact: < 600dp
     * Phones in portrait
     */
    Compact,
    
    /**
     * Medium: 600dp - 839dp
     * Tablets, phones in landscape, unfolded foldables
     */
    Medium,
    
    /**
     * Expanded: >= 840dp
     * Large tablets, desktops
     */
    Expanded
}

/**
 * Calculate window size class based on screen width
 * Platform-specific implementation
 */
@Composable
expect fun calculateWindowSizeClass(): WindowSizeClass

package com.asr.financial.presentation.ui.responsive

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

/**
 * Calculate window size class based on screen width
 * iOS implementation
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun calculateWindowSizeClass(): WindowSizeClass {
    val screenWidth = UIScreen.mainScreen.bounds.useContents { size.width }
    val screenWidthDp = screenWidth // iOS uses points which are similar to dp
    
    return when {
        screenWidthDp < 600 -> WindowSizeClass.Compact
        screenWidthDp < 840 -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }
}

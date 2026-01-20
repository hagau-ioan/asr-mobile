package com.asr.financial.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Global method to check if dark mode should be used.
 * 
 * Currently forced to always return true (dark theme).
 * To enable system theme detection, change this to:
 *   return isSystemInDarkTheme()
 */
@Composable
fun isAppDarkMode(): Boolean {
    // Force dark theme always - system theme detection commented out
    // return isSystemInDarkTheme()
    return true // Always use dark theme
}

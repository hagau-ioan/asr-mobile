package com.asr.financial

import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.asr.financial.presentation.navigation.NavGraph
import com.asr.financial.presentation.screens.splash.SplashScreen
import com.asr.financial.presentation.theme.AppTheme

@Composable
fun App() {
    var showSplash by remember { mutableStateOf(true) }
    
    AppTheme {
        if (showSplash) {
            SplashScreen(onSplashFinished = { showSplash = false })
        } else {
            val navController = rememberNavController()
            NavGraph(navController = navController)
        }
    }
}

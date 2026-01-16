package com.asr.financial

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.asr.financial.presentation.navigation.NavGraph
import com.asr.financial.presentation.theme.AsrTheme

@Composable
fun App() {
    AsrTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}

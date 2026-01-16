package com.asr.financial

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.asr.financial.presentation.navigation.NavGraph
import com.asr.financial.presentation.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}

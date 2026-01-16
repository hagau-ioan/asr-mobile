package com.asr.financial.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.asr.financial.presentation.screens.calculator.CalculatorScreen
import com.asr.financial.presentation.screens.congregations.CongregationsScreen
import com.asr.financial.presentation.screens.expenses.ExpensesScreen
import com.asr.financial.presentation.screens.home.HomeScreen
import com.asr.financial.presentation.screens.upload.UploadScreen
import com.asr.financial.presentation.screens.utilities.UtilitiesScreen
import com.asr.financial.presentation.screens.yearly.YearlyScreen
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.responsive.calculateWindowSizeClass
import com.asr.financial.presentation.ui.scaffold.AdaptiveScaffold

@Composable
fun NavGraph(
    navController: NavHostController
) {
    val windowSizeClass = calculateWindowSizeClass()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"
    
    AdaptiveScaffold(
        windowSizeClass = windowSizeClass,
        selectedRoute = currentRoute,
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) { paddingValues, onMenuClick ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable("congregations") {
                CongregationsScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable("expenses") {
                ExpensesScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable("utilities") {
                UtilitiesScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable("yearly") {
                YearlyScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable("calculator") {
                CalculatorScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable("upload") {
                UploadScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
        }
    }
}

package com.asr.financial.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.asr.financial.domain.usecase.GetCurrentUserUseCase
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.asrexpenses.AsrExpensesScreen
import com.asr.financial.presentation.screens.calculator.CalculatorScreen
import com.asr.financial.presentation.screens.congregations.CongregationsScreen
import com.asr.financial.presentation.screens.expenses.ExpensesScreen
import com.asr.financial.presentation.screens.home.HomeScreen
import com.asr.financial.presentation.screens.login.LoginScreen
import com.asr.financial.presentation.screens.upload.UploadScreen
import com.asr.financial.presentation.screens.utilities.UtilitiesScreen
import com.asr.financial.presentation.screens.yearly.YearlyScreen
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.responsive.calculateWindowSizeClass
import com.asr.financial.presentation.ui.scaffold.AdaptiveScaffold
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.HOME,
    onAuthSuccess: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val windowSizeClass = calculateWindowSizeClass()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: startDestination
    
    // Get current user email
    val getCurrentUserUseCase: GetCurrentUserUseCase = koinInject()
    var currentUserEmail by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Load user email on initial composition and refresh when route changes (especially after login)
    LaunchedEffect(currentRoute) {
        scope.launch {
            val user = getCurrentUserUseCase()
            currentUserEmail = user?.email
        }
    }
    
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
        },
        onLogout = {
            scope.launch {
                onLogout()
            }
        },
        currentUserEmail = currentUserEmail
    ) { paddingValues, onMenuClick ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigate = { route ->
                        if (route == Routes.HOME) {
                            onAuthSuccess()
                            // Refresh user email immediately after successful login (before navigation)
                            scope.launch {
                                val user = getCurrentUserUseCase()
                                currentUserEmail = user?.email
                                // Navigate after email is loaded
                                navController.navigate(route) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        } else {
                            navController.navigate(route)
                        }
                    }
                )
            }
            
            composable(Routes.HOME) {
                HomeScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable(Routes.CONGREGATIONS) {
                CongregationsScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable(Routes.EXPENSES) {
                ExpensesScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable(Routes.UTILITIES) {
                UtilitiesScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable(Routes.YEARLY) {
                YearlyScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable(Routes.CALCULATOR) {
                CalculatorScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable(Routes.ASR_EXPENSES) {
                AsrExpensesScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
            
            composable(Routes.UPLOAD) {
                UploadScreen(
                    windowSizeClass = windowSizeClass,
                    onNavigate = { route -> navController.navigate(route) },
                    onMenuClick = onMenuClick
                )
            }
        }
    }
}

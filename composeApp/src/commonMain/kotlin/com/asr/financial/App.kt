package com.asr.financial

import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.asr.financial.domain.usecase.CheckAuthStatusUseCase
import com.asr.financial.domain.usecase.LogoutUseCase
import com.asr.financial.presentation.navigation.NavGraph
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.splash.SplashScreen
import com.asr.financial.presentation.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    var showSplash by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf<Boolean?>(null) }
    val checkAuthStatusUseCase: CheckAuthStatusUseCase = koinInject()
    
    // Check authentication status during splash screen
    LaunchedEffect(Unit) {
        val startTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        // Check auth status immediately when app starts
        isAuthenticated = checkAuthStatusUseCase()
        // Ensure splash screen is visible for minimum duration (2.5 seconds)
        val elapsedTime = kotlin.time.Clock.System.now().toEpochMilliseconds() - startTime
        val remainingTime = (2500 - elapsedTime).coerceAtLeast(0)
        if (remainingTime > 0) {
            kotlinx.coroutines.delay(remainingTime)
        }
        showSplash = false
    }
    
    AppTheme {
        when {
            showSplash || isAuthenticated == null -> {
                // Show splash screen while checking auth or during splash duration
                SplashScreen(onSplashFinished = { /* Handled by LaunchedEffect */ })
            }
            isAuthenticated == true -> {
                // Authenticated - go directly to home
                val navController = rememberNavController()
                val logoutUseCase: LogoutUseCase = koinInject()
                val scope = rememberCoroutineScope()
                
                NavGraph(
                    navController = navController,
                    startDestination = Routes.HOME,
                    onAuthSuccess = { },
                    onLogout = {
                        scope.launch {
                            logoutUseCase()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
            else -> {
                // Not authenticated - show login
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = Routes.LOGIN,
                    onAuthSuccess = { }
                )
            }
        }
    }
}

package com.asr.financial

import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.asr.financial.domain.usecase.CheckAuthStatusUseCase
import com.asr.financial.domain.usecase.ConvertAndSavePendingNotificationUseCase
import com.asr.financial.domain.usecase.InitializeFcmSubscriptionsUseCase
import com.asr.financial.domain.usecase.LogoutUseCase
import com.asr.financial.domain.usecase.ObservePendingNotificationUseCase
import com.asr.financial.platform.FirebaseMessaging
import com.asr.financial.presentation.navigation.NavGraph
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.splash.SplashScreen
import com.asr.financial.presentation.theme.AppTheme
import com.asr.financial.presentation.ui.constants.AppConstants
import kotlin.time.Clock as TimeClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    var showSplash by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf<Boolean?>(null) }
    val checkAuthStatusUseCase: CheckAuthStatusUseCase = koinInject()
    
    // Check authentication status during splash screen
    LaunchedEffect(Unit) {
        val startTime = TimeClock.System.now().toEpochMilliseconds()
        // Check auth status immediately when app starts
        isAuthenticated = checkAuthStatusUseCase()
        // Ensure splash screen is visible for minimum duration
        val elapsedTime = TimeClock.System.now().toEpochMilliseconds() - startTime
        val remainingTime = (AppConstants.Time.SPLASH_SCREEN_MIN_DURATION_MS - elapsedTime).coerceAtLeast(0)
        if (remainingTime > 0) {
            delay(remainingTime)
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
                val initializeFcmSubscriptionsUseCase: InitializeFcmSubscriptionsUseCase = koinInject()
                val firebaseMessaging: FirebaseMessaging = koinInject()
                val scope = rememberCoroutineScope()
                
                // Initialize FCM subscriptions when app starts and user is authenticated
                LaunchedEffect(Unit) {
                    try {
                        initializeFcmSubscriptionsUseCase()
                    } catch (e: Exception) {
                        // Silently handle FCM subscription errors - don't block app
                    }
                }
                
                // Handle notifications received while app is in foreground
                // Automatically save to DataStore so HomeScreen can display it
                val convertAndSaveUseCase: ConvertAndSavePendingNotificationUseCase = koinInject()
                val observePendingNotificationUseCase: ObservePendingNotificationUseCase = koinInject()
                
                // Track previous notification ID to detect new notifications
                var previousNotificationId by remember { mutableStateOf<String?>(null) }
                
                LaunchedEffect(Unit) {
                    firebaseMessaging.setOnNotificationReceivedCallback { notification ->
                        // When notification is received (foreground or background),
                        // automatically convert and save it to DataStore via UseCase
                        scope.launch {
                            try {
                                convertAndSaveUseCase(notification)
                            } catch (e: Exception) {
                                // Silently handle errors - notification storage is not critical
                            }
                        }
                    }
                }
                
                // Observe pending notification changes and navigate to Home when a new notification appears
                LaunchedEffect(Unit) {
                    observePendingNotificationUseCase()
                        .map { it?.id } // Track notification ID changes
                        .distinctUntilChanged() // Only emit when ID actually changes
                        .collect { currentNotificationId ->
                            // If a new notification appears (ID changed from previous or from null)
                            if (currentNotificationId != null && currentNotificationId != previousNotificationId) {
                                // Navigate to Home screen to show the notification banner
                                val currentRoute = navController.currentBackStackEntry?.destination?.route
                                if (currentRoute != Routes.HOME) {
                                    scope.launch {
                                        navController.navigate(Routes.HOME) {
                                            // Pop back stack to root if not already at home
                                            popUpTo(Routes.HOME) { inclusive = false }
                                        }
                                    }
                                }
                                previousNotificationId = currentNotificationId
                            } else if (currentNotificationId == null) {
                                // Notification was dismissed, reset tracking
                                previousNotificationId = null
                            }
                        }
                }
                
                // Periodically verify session is still valid
                // This will detect if user was deleted from Firebase Auth
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(AppConstants.Time.AUTH_CHECK_INTERVAL_MS)
                        val stillAuthenticated = checkAuthStatusUseCase()
                        if (!stillAuthenticated) {
                            // User was deleted or session invalid, logout and navigate to login
                            scope.launch {
                                logoutUseCase()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(AppConstants.Navigation.ROOT_ROUTE_INDEX) { inclusive = true }
                                }
                            }
                            break
                        }
                    }
                }
                
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

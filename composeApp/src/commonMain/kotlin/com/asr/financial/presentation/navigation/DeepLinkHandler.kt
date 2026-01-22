package com.asr.financial.presentation.navigation

import androidx.navigation.NavHostController
import com.asr.financial.platform.PushNotification
import com.asr.financial.platform.UrlLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Handler for deep linking from push notifications.
 * Parses notification data and navigates to the appropriate screen or opens external URLs.
 */
object DeepLinkHandler {
    
    /**
     * Handle notification tap and navigate to appropriate screen or open external URL.
     * @param notification The push notification that was tapped
     * @param navController Navigation controller to perform navigation (for internal links)
     * @param urlLauncher UrlLauncher for opening external URLs
     * @param scope CoroutineScope for launching URL opening
     */
    fun handleNotificationTap(
        notification: PushNotification,
        navController: NavHostController,
        urlLauncher: UrlLauncher,
        scope: CoroutineScope
    ) {

        val url = notification.data["link"]
        val screen = notification.data["screen"]
        val type = notification.data["type"]
        
        when {
            // Handle URL-based deep linking (from backend config)
            // If URL is external (http/https), open in browser immediately and return
            !url.isNullOrBlank() -> {
                if (isExternalUrl(url)) {
                    // Open external URL in browser immediately - don't navigate in app
                    scope.launch {
                        urlLauncher.openUrl(url)
                    }
                    // Return early - don't do any internal navigation
                    return
                } else {
                    // Try internal navigation for app:// or other schemes
                    handleUrlDeepLink(url, navController)
                }
            }
            // Handle screen-based deep linking
            !screen.isNullOrBlank() -> {
                handleScreenDeepLink(screen, notification.data, navController)
            }
            // Handle type-based deep linking
            !type.isNullOrBlank() -> {
                handleTypeDeepLink(type, notification.data, navController)
            }
        }
    }
    
    /**
     * Check if URL is external (http/https) or internal (app scheme).
     */
    private fun isExternalUrl(url: String): Boolean {
        return url.startsWith("http://", ignoreCase = true) || 
               url.startsWith("https://", ignoreCase = true)
    }
    
    /**
     * Handle URL-based deep linking.
     * Example: "https://app.example.com/decont/2026/01" -> DecontRoute(2026, 1)
     */
    private fun handleUrlDeepLink(url: String, navController: NavHostController) {
        try {
            // Parse URL to extract route and parameters
            // Example formats:
            // - https://app.example.com/decont/2026/01
            // - https://app.example.com/expenses
            // - https://app.example.com/home
            
            val urlLower = url.lowercase()
            
            when {
                urlLower.contains("/decont/") -> {
                    // Extract year and month from URL
                    val parts = url.split("/")
                    val decontIndex = parts.indexOfFirst { it.lowercase() == "decont" }
                    if (decontIndex >= 0 && parts.size > decontIndex + 2) {
                        val year = parts[decontIndex + 1].toIntOrNull()
                        val month = parts[decontIndex + 2].toIntOrNull()
                        if (year != null && month != null) {
                            navController.navigate(DecontRoute(year, month))
                            return
                        }
                    }
                }
                urlLower.contains("/expenses") -> {
                    navController.navigate(Routes.EXPENSES)
                    return
                }
                urlLower.contains("/home") -> {
                    navController.navigate(Routes.HOME)
                    return
                }
                urlLower.contains("/congregations") -> {
                    navController.navigate(Routes.CONGREGATIONS)
                    return
                }
                urlLower.contains("/utilities") -> {
                    navController.navigate(Routes.UTILITIES)
                    return
                }
                urlLower.contains("/calculator") -> {
                    navController.navigate(Routes.CALCULATOR)
                    return
                }
                urlLower.contains("/asr-expenses") -> {
                    navController.navigate(Routes.ASR_EXPENSES)
                    return
                }
                urlLower.contains("/upload") -> {
                    navController.navigate(Routes.UPLOAD)
                    return
                }
            }
            
            // Default to home if URL doesn't match any pattern
            navController.navigate(Routes.HOME)
        } catch (e: Exception) {
            // On error, navigate to home
            navController.navigate(Routes.HOME)
        }
    }
    
    /**
     * Handle screen-based deep linking.
     * Example: screen="expenses" -> Routes.EXPENSES
     */
    private fun handleScreenDeepLink(screen: String, data: Map<String, String>, navController: NavHostController) {
        when (screen.lowercase()) {
            "home" -> navController.navigate(Routes.HOME)
            "expenses" -> navController.navigate(Routes.EXPENSES)
            "congregations" -> navController.navigate(Routes.CONGREGATIONS)
            "utilities" -> navController.navigate(Routes.UTILITIES)
            "yearly" -> navController.navigate(Routes.YEARLY)
            "calculator" -> navController.navigate(Routes.CALCULATOR)
            "asr-expenses" -> navController.navigate(Routes.ASR_EXPENSES)
            "upload" -> navController.navigate(Routes.UPLOAD)
            "decont" -> {
                // Extract year and month from data
                val year = data["year"]?.toIntOrNull()
                val month = data["month"]?.toIntOrNull()
                if (year != null && month != null) {
                    navController.navigate(DecontRoute(year, month))
                } else {
                    navController.navigate(Routes.HOME)
                }
            }
            else -> navController.navigate(Routes.HOME)
        }
    }
    
    /**
     * Handle type-based deep linking.
     * Example: type="expense_uploaded", expense_id="123" -> navigate to expenses
     */
    private fun handleTypeDeepLink(type: String, data: Map<String, String>, navController: NavHostController) {
        when (type.lowercase()) {
            "expense_uploaded", "decont_generated" -> {
                // Navigate to expenses or decont based on data
                val year = data["year"]?.toIntOrNull()
                val month = data["month"]?.toIntOrNull()
                if (year != null && month != null) {
                    navController.navigate(DecontRoute(year, month))
                } else {
                    navController.navigate(Routes.EXPENSES)
                }
            }
            "app_update" -> {
                navController.navigate(Routes.HOME)
            }
            else -> {
                navController.navigate(Routes.HOME)
            }
        }
    }
}

package com.asr.financial.presentation.ui.constants

/**
 * Application-wide constants
 * Centralized location for magic numbers and strings used throughout the app
 */
object AppConstants {
    // Time constants (milliseconds)
    object Time {
        const val SPLASH_SCREEN_MIN_DURATION_MS = 2500L
        const val AUTH_CHECK_INTERVAL_MS = 60 * 60_000L
        const val SPLASH_ANIMATION_DURATION_MS = 1000
        const val SPLASH_ANIMATION_DELAY_MS = 300
        const val CHART_ANIMATION_DURATION_MS = 1500
        const val BAR_CHART_ANIMATION_DURATION_MS = 1200
        const val DONUT_CHART_ANIMATION_DURATION_MS = 1000
        const val STATE_FLOW_TIMEOUT_MS = 5000L
        const val UPLOAD_TIMEOUT_MS = 10_000L // 10 seconds - timeout before allowing reset during upload
    }
    
    // Business logic constants
    object Business {
        const val MONTHS_IN_YEAR = 12
        const val DEFAULT_MONTHLY_CEILING = 500.0
        const val DEFAULT_START_YEAR = 2024
    }
    
    // UI constants
    object UI {
        const val CHART_DEFAULT_HEIGHT = 350
        const val NAVIGATION_DRAWER_WIDTH_DP = 240
        const val DRAWER_HEADER_HEIGHT_DP = 100
        const val DRAWER_ICON_SIZE_DP = 56
        const val NAVIGATION_ICON_SIZE_DP = 20
        const val NAVIGATION_SPACING_DP = 12
        const val DEFAULT_ALPHA = 0.8f
        const val LOW_ALPHA = 0.3f
    }
    
    // Navigation constants
    object Navigation {
        const val ROOT_ROUTE_INDEX = 0
    }
    
    // Error messages
    object ErrorMessages {
        const val UNKNOWN_ERROR = "Unknown error"
        const val UNKNOWN_ERROR_OCCURRED = "Unknown error occurred"
        const val FAILED_TO_LOAD_DATA = "Failed to load data"
        const val FAILED_TO_LOAD_TRANSACTIONS = "Failed to load transactions"
        const val FAILED_TO_LOAD_EXPENSES = "Failed to load expenses"
        const val FAILED_TO_LOAD_UTILITIES = "Failed to load utilities"
        const val DATA_NOT_FOUND = "Data not found"
    }
    
    // Default values
    object Defaults {
        const val ZERO_DOUBLE = 0.0
        const val ZERO_FLOAT = 0f
    }
    
    // App branding (used in splash screen before config loads)
    object Branding {
        const val APP_NAME = "ASR"
        const val APP_SUBTITLE = "Gestiune Financiară"
        const val APP_LOCATION = "Târgu Mureș"
    }
    
    // Firebase Storage paths
    object FirebaseStorage {
        const val DOCUMENTS_INCOMING_PATH_PREFIX = "documents/incoming"
    }
}

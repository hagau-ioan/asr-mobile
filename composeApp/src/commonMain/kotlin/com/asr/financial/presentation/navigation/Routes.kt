package com.asr.financial.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Centralized route constants for navigation.
 * Use these constants instead of hardcoded strings throughout the app.
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CONGREGATIONS = "congregations"
    const val EXPENSES = "expenses"
    const val UTILITIES = "utilities"
    const val YEARLY = "yearly"
    const val CALCULATOR = "calculator"
    const val ASR_EXPENSES = "asr-expenses"
    const val UPLOAD = "upload"
}

/**
 * Serializable route for Decont screen with year and month parameters
 */
@Serializable
data class DecontRoute(val year: Int, val month: Int)

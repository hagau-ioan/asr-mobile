package com.asr.financial.domain.models.access

/**
 * Application sections and functionalities that require permission checks
 */
enum class AppSection {
    // Navigation sections
    NAV_HOME,
    NAV_CONGREGATIONS,
    NAV_EXPENSES,
    NAV_UTILITIES,
    NAV_YEARLY,
    NAV_CALCULATOR,
    NAV_ASR_EXPENSES,
    NAV_UPLOAD,
    
    // Feature sections
    FEATURE_UPLOAD_DOCUMENTS,
    FEATURE_VIEW_EXPENSES,
    FEATURE_EDIT_EXPENSES,
    FEATURE_DELETE_EXPENSES,
    FEATURE_VIEW_ASR_EXPENSES,
    FEATURE_EDIT_ASR_EXPENSES,
    FEATURE_VIEW_UTILITIES,
    FEATURE_EDIT_UTILITIES,
    FEATURE_VIEW_CONGREGATIONS,
    FEATURE_EDIT_CONGREGATIONS,
    
    // Admin sections
    ADMIN_SETTINGS,
    ADMIN_USER_MANAGEMENT,
    
    // Add more sections as needed
}

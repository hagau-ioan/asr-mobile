package com.asr.financial.domain.usecase

import com.asr.financial.domain.models.access.AccessLevel
import com.asr.financial.domain.models.access.AppSection
import com.asr.financial.domain.models.access.UserRoleUtils
import com.asr.financial.domain.repository.AuthRepository

/**
 * Use case for checking if the current user has permission to access a specific app section/functionality
 * 
 * This follows Clean Architecture principles by encapsulating permission checking logic
 * and accessing user data through the repository interface.
 * 
 * The section-to-access-level mapping is defined here as business logic.
 */
class CheckPermissionUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Maps each app section to its required access level
     * Update this map to control access to different functionalities
     */
    private val sectionPermissions: Map<AppSection, AccessLevel> = mapOf(
        // Navigation sections - most are accessible to all authenticated users
        AppSection.NAV_HOME to AccessLevel.REGULAR,
        AppSection.NAV_CONGREGATIONS to AccessLevel.REGULAR,
        AppSection.NAV_EXPENSES to AccessLevel.REGULAR,
        AppSection.NAV_UTILITIES to AccessLevel.REGULAR,
        AppSection.NAV_YEARLY to AccessLevel.REGULAR,
        AppSection.NAV_CALCULATOR to AccessLevel.REGULAR,
        AppSection.NAV_ASR_EXPENSES to AccessLevel.REGULAR,
        AppSection.NAV_UPLOAD to AccessLevel.ADMIN, // Only admins can upload
        
        // Feature sections
        AppSection.FEATURE_UPLOAD_DOCUMENTS to AccessLevel.ADMIN,
        AppSection.FEATURE_VIEW_EXPENSES to AccessLevel.REGULAR,
        AppSection.FEATURE_EDIT_EXPENSES to AccessLevel.EDITOR,
        AppSection.FEATURE_DELETE_EXPENSES to AccessLevel.MANAGER,
        AppSection.FEATURE_VIEW_ASR_EXPENSES to AccessLevel.REGULAR,
        AppSection.FEATURE_EDIT_ASR_EXPENSES to AccessLevel.EDITOR,
        AppSection.FEATURE_VIEW_UTILITIES to AccessLevel.REGULAR,
        AppSection.FEATURE_EDIT_UTILITIES to AccessLevel.EDITOR,
        AppSection.FEATURE_VIEW_CONGREGATIONS to AccessLevel.REGULAR,
        AppSection.FEATURE_EDIT_CONGREGATIONS to AccessLevel.EDITOR,
        
        // Admin sections
        AppSection.ADMIN_SETTINGS to AccessLevel.ADMIN,
        AppSection.ADMIN_USER_MANAGEMENT to AccessLevel.ADMIN,
    )
    
    /**
     * Check if the current user has permission to access a specific section
     * 
     * @param section The app section or functionality to check
     * @return true if user has sufficient access level, false otherwise
     */
    suspend operator fun invoke(section: AppSection): Boolean {
        val user = authRepository.getCurrentUser()
        val requiredAccessLevel = sectionPermissions[section] 
            ?: AccessLevel.REGULAR // Default to REGULAR if section not found
        
        return UserRoleUtils.hasAccessLevel(user?.email, requiredAccessLevel)
    }
    
    /**
     * Check permission when you already have the user email
     * Useful for synchronous contexts where you can't use suspend functions
     * 
     * @param section The app section or functionality to check
     * @param userEmail User email address to determine role and access level
     * @return true if user has sufficient access level, false otherwise
     */
    fun checkPermission(section: AppSection, userEmail: String?): Boolean {
        val requiredAccessLevel = sectionPermissions[section] 
            ?: AccessLevel.REGULAR // Default to REGULAR if section not found
        
        return UserRoleUtils.hasAccessLevel(userEmail, requiredAccessLevel)
    }
}

package com.asr.financial.domain.models.access

/**
 * User roles based on email address patterns.
 * Each role maps to an AccessLevel for permission checking.
 * 
 * Role determination:
 * - ADMIN: Email contains "_asr" (case-insensitive) -> AccessLevel.ADMIN (10)
 * - REGULAR: All other authenticated users -> AccessLevel.REGULAR (1)
 * 
 * Future roles can be added here (e.g., EDITOR, MANAGER, etc.)
 */
enum class UserRole(val accessLevel: AccessLevel) {
    /**
     * Administrator role - full access including upload functionality
     * Determined by "_asr" in email address
     * Access Level: 10
     */
    ADMIN(AccessLevel.ADMIN),
    
    /**
     * Regular user role - view-only access
     * Default role for all authenticated users without special patterns
     * Access Level: 1
     */
    REGULAR(AccessLevel.REGULAR);
    
    /**
     * Get the numeric access level for this role
     */
    fun getAccessLevelValue(): Int = accessLevel.level
}

/**
 * Utility functions for user role management
 */
object UserRoleUtils {
    /**
     * Determine user role from email address
     * 
     * @param email User email address (can be null)
     * @return UserRole.ADMIN if email contains "_asr", otherwise UserRole.REGULAR
     */
    fun getRoleFromEmail(email: String?): UserRole {
        if (email == null) return UserRole.REGULAR
        
        return if (email.contains("_asr", ignoreCase = true)) {
            UserRole.ADMIN
        } else {
            UserRole.REGULAR
        }
    }
    
    /**
     * Check if user has admin access
     * 
     * @param email User email address (can be null)
     * @return true if user is ADMIN, false otherwise
     */
    fun isAdmin(email: String?): Boolean {
        return getRoleFromEmail(email) == UserRole.ADMIN
    }
    
    /**
     * Check if user has access to a specific feature based on access level
     * 
     * @param email User email address (can be null)
     * @param requiredAccessLevel Minimum access level required
     * @return true if user's access level meets or exceeds the required level
     */
    fun hasAccessLevel(email: String?, requiredAccessLevel: AccessLevel): Boolean {
        val userRole = getRoleFromEmail(email)
        return userRole.accessLevel.meets(requiredAccessLevel)
    }
    
    /**
     * Check if user has access to a specific feature
     * Currently only checks admin access, but can be extended for other roles
     * 
     * @param email User email address (can be null)
     * @param requiredRole Minimum role required for access
     * @return true if user has required access level
     * @deprecated Use hasAccessLevel instead for more granular control
     */
    @Deprecated("Use hasAccessLevel instead", ReplaceWith("hasAccessLevel(email, requiredRole.accessLevel)"))
    fun hasAccess(email: String?, requiredRole: UserRole): Boolean {
        return hasAccessLevel(email, requiredRole.accessLevel)
    }
}

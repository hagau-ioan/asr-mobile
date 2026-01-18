package com.asr.financial.domain.models.access

/**
 * Access levels for the application
 * Numeric values represent hierarchy: higher number = more access
 */
enum class AccessLevel(val level: Int) {
    /**
     * Regular user - basic view access
     * Level: 1
     */
    REGULAR(1),
    
    /**
     * Editor - can edit some content
     * Level: 2
     */
    EDITOR(2),
    
    /**
     * Manager - can manage more sections
     * Level: 3
     */
    MANAGER(3),
    
    /**
     * Administrator - full access to everything
     * Level: 10
     */
    ADMIN(10);
    
    /**
     * Check if this access level meets or exceeds the required level
     */
    fun meets(requiredLevel: AccessLevel): Boolean {
        return this.level >= requiredLevel.level
    }
}

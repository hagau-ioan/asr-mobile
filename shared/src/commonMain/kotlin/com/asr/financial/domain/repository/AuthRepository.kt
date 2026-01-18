package com.asr.financial.domain.repository

import com.asr.financial.domain.models.AuthUser

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    /**
     * Login with email and password
     * @param email User email
     * @param password User password
     * @return Result containing AuthUser on success, or error message on failure
     */
    suspend fun login(email: String, password: String): Result<AuthUser>
    
    /**
     * Logout current user
     */
    suspend fun logout()
    
    /**
     * Get current authenticated user
     * @return AuthUser if user is authenticated, null otherwise
     */
    suspend fun getCurrentUser(): AuthUser?
    
    /**
     * Check if user is authenticated
     * @return true if user is authenticated, false otherwise
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Refresh authentication token
     * @return Result containing new token on success, or error message on failure
     */
    suspend fun refreshAuthToken(): Result<String>
}

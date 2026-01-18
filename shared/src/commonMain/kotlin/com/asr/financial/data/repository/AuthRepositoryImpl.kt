package com.asr.financial.data.repository

import com.asr.financial.domain.models.AuthUser
import com.asr.financial.domain.repository.AuthRepository
import com.asr.financial.platform.FirebaseAuth
import com.asr.financial.platform.User as PlatformUser

/**
 * Implementation of AuthRepository using FirebaseAuth platform abstraction
 */
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    
    override suspend fun login(email: String, password: String): Result<AuthUser> {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password)
        
        return if (result.success && result.user != null) {
            val platformUser = result.user
            val authUser = AuthUser(
                uid = platformUser.uid,
                email = platformUser.email ?: "",
                displayName = platformUser.displayName,
                tokenExpiryTime = null // Firebase handles token expiry internally
            )
            Result.success(authUser)
        } else {
            Result.failure(Exception(result.errorMessage ?: "Authentication failed"))
        }
    }
    
    override suspend fun logout() {
        firebaseAuth.signOut()
    }
    
    override suspend fun getCurrentUser(): AuthUser? {
        val platformUser = firebaseAuth.getCurrentUser() ?: return null
        
        return AuthUser(
            uid = platformUser.uid,
            email = platformUser.email ?: "",
            displayName = platformUser.displayName,
            tokenExpiryTime = null // Firebase handles token expiry internally
        )
    }
    
    override suspend fun isAuthenticated(): Boolean {
        // Use verifySession to check with server if user still exists
        // This will return false if user was deleted from Firebase Auth
        return firebaseAuth.verifySession()
    }
    
    override suspend fun refreshAuthToken(): Result<String> {
        val token = firebaseAuth.refreshToken()
        return if (token != null) {
            Result.success(token)
        } else {
            Result.failure(Exception("Failed to refresh token"))
        }
    }
}

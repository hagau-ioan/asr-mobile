package com.asr.financial.platform

/**
 * Platform abstraction for Firebase Authentication
 * Android: Firebase Auth Android SDK
 * iOS: Firebase Auth iOS SDK (via Swift/Objective-C bridge)
 */
expect class FirebaseAuth {
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signOut()
    suspend fun getCurrentUser(): User?
    suspend fun isUserSignedIn(): Boolean
    suspend fun getAuthToken(): String?
    suspend fun refreshToken(): String?
    /**
     * Check if session is still valid (for app launch verification)
     * Firebase automatically persists sessions, but we verify on launch
     */
    suspend fun verifySession(): Boolean
}

/**
 * Result of authentication operations
 */
data class AuthResult(
    val success: Boolean,
    val user: User? = null,
    val errorMessage: String? = null
)

/**
 * Firebase User model
 */
data class User(
    val uid: String,
    val email: String?,
    val displayName: String? = null
)

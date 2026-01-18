package com.asr.financial.platform

import com.google.firebase.auth.FirebaseAuth as AndroidFirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

actual class FirebaseAuth {
    
    private val firebaseAuth: AndroidFirebaseAuth = AndroidFirebaseAuth.getInstance()
    
    private companion object {
        const val UNKNOWN_ERROR_MESSAGE = "Unknown error occurred"
    }
    
    actual suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            
            if (user != null) {
                AuthResult(
                    success = true,
                    user = user.toUser()
                )
            } else {
                AuthResult(
                    success = false,
                    errorMessage = "Authentication failed: User is null"
                )
            }
        } catch (e: FirebaseAuthException) {
            AuthResult(
                success = false,
                errorMessage = getAuthErrorMessage(e.errorCode)
            )
        } catch (e: Exception) {
            AuthResult(
                success = false,
                errorMessage = e.message ?: UNKNOWN_ERROR_MESSAGE
            )
        }
    }
    
    actual suspend fun signOut() = withContext(Dispatchers.IO) {
        firebaseAuth.signOut()
    }
    
    actual suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        firebaseAuth.currentUser?.toUser()
    }
    
    actual suspend fun isUserSignedIn(): Boolean = withContext(Dispatchers.IO) {
        firebaseAuth.currentUser != null
    }
    
    actual suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }
    
    actual suspend fun refreshToken(): String? = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.currentUser?.getIdToken(true)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }
    
    actual suspend fun verifySession(): Boolean = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            return@withContext false
        }
        
        // Force a token refresh to verify the session is still valid with the server
        // This will fail if the user has been deleted from Firebase Auth
        return@withContext try {
            val tokenResult = user.getIdToken(true).await() // Force refresh
            tokenResult != null
        } catch (e: FirebaseAuthException) {
            // If token refresh fails, the user may have been deleted
            // Sign out to clear local state
            try {
                firebaseAuth.signOut()
            } catch (signOutException: Exception) {
                // Ignore sign out errors
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun FirebaseUser.toUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName
        )
    }
    
    private fun getAuthErrorMessage(errorCode: String): String {
        return when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "login_error_invalid_email"
            "ERROR_WRONG_PASSWORD" -> "login_error_wrong_password"
            "ERROR_INVALID_CREDENTIAL" -> "login_error_invalid_credential"
            "ERROR_USER_NOT_FOUND" -> "login_error_user_not_found"
            "ERROR_USER_DISABLED" -> "login_error_user_disabled"
            "ERROR_TOO_MANY_REQUESTS" -> "login_error_too_many_requests"
            "ERROR_OPERATION_NOT_ALLOWED" -> "login_error_operation_not_allowed"
            "ERROR_NETWORK_REQUEST_FAILED" -> "login_error_network"
            else -> "login_error_unknown"
        }
    }
}

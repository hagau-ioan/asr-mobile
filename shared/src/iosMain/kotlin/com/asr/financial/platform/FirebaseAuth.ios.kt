@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.asr.financial.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * iOS implementation of FirebaseAuth using a bridge pattern.
 * 
 * This uses FirebaseAuthBridge (Swift) which must be called from the iOS app layer.
 * The bridge is accessed via a singleton pattern similar to CameraBridge.
 * 
 * Note: The Swift bridge (FirebaseAuthBridge.swift) must be added to the Xcode project
 * and the Firebase iOS SDK must be added via Swift Package Manager.
 */
actual class FirebaseAuth {
    
    actual suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseAuthBridge.signIn(email, password) { success, uid, email, displayName, errorMessage ->
                if (success && uid != null) {
                    continuation.resume(
                        AuthResult(
                            success = true,
                            user = User(
                                uid = uid,
                                email = email,
                                displayName = displayName
                            )
                        )
                    )
                } else {
                    continuation.resume(
                        AuthResult(
                            success = false,
                            errorMessage = errorMessage ?: "Authentication failed"
                        )
                    )
                }
            }
        }
    }
    
    actual suspend fun signOut() = withContext(Dispatchers.Main) {
        com.asr.financial.platform.FirebaseAuthBridge.signOut()
    }
    
    actual suspend fun getCurrentUser(): User? = withContext(Dispatchers.Main) {
        val result = com.asr.financial.platform.FirebaseAuthBridge.getCurrentUser()
        if (result == null || result.size < 3) {
            return@withContext null
        }
        
        val uid = result[0] as? String
        val email = result[1] as? String
        val displayName = result[2] as? String
        
        if (uid != null) {
            User(
                uid = uid,
                email = email,
                displayName = displayName
            )
        } else {
            null
        }
    }
    
    actual suspend fun isUserSignedIn(): Boolean = withContext(Dispatchers.Main) {
        com.asr.financial.platform.FirebaseAuthBridge.isUserSignedIn()
    }
    
    actual suspend fun getAuthToken(): String? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseAuthBridge.getAuthToken(forceRefresh = false) { token, error ->
                continuation.resume(token)
            }
        }
    }
    
    actual suspend fun refreshToken(): String? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseAuthBridge.getAuthToken(forceRefresh = true) { token, error ->
                continuation.resume(token)
            }
        }
    }
    
    actual suspend fun verifySession(): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            com.asr.financial.platform.FirebaseAuthBridge.verifySession { isValid ->
                continuation.resume(isValid)
            }
        }
    }
}

/**
 * Bridge object for iOS Firebase Auth communication.
 * This object provides static methods that will be called by the Swift bridge.
 * The Swift FirebaseAuthBridge class should call these methods.
 * 
 * Pattern similar to CameraBridge in the codebase.
 */
object FirebaseAuthBridge {
    
    // Sign in callback
    private var signInCallback: ((Boolean, String?, String?, String?, String?) -> Unit)? = null
    
    // Get auth token callback
    private var getAuthTokenCallback: ((String?, String?) -> Unit)? = null
    
    // Verify session callback
    private var verifySessionCallback: ((Boolean) -> Unit)? = null
    
    /**
     * Called from Kotlin to initiate sign in.
     * The Swift bridge should observe this and call Firebase Auth.
     */
    fun signIn(
        email: String,
        password: String,
        completion: (Boolean, String?, String?, String?, String?) -> Unit
    ) {
        signInCallback = completion
        // Post notification to Swift layer
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseAuthSignIn",
            `object` = mapOf(
                "email" to email,
                "password" to password
            )
        )
    }
    
    /**
     * Called from Kotlin to sign out.
     */
    fun signOut() {
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseAuthSignOut",
            `object` = null
        )
    }
    
    /**
     * Called from Kotlin to get current user.
     * Returns array [uid, email, displayName] or null.
     */
    fun getCurrentUser(): platform.Foundation.NSArray<platform.Foundation.NSString>? {
        // This will be populated by Swift bridge
        // For now, return null - Swift bridge should set this via setCurrentUser
        return currentUserData
    }
    
    private var currentUserData: platform.Foundation.NSArray<platform.Foundation.NSString>? = null
    
    /**
     * Called from Swift bridge to set current user data.
     */
    fun setCurrentUser(uid: String?, email: String?, displayName: String?) {
        if (uid != null) {
            currentUserData = platform.Foundation.NSArray.arrayWithObjects(
                uid as platform.Foundation.NSString,
                (email ?: "") as platform.Foundation.NSString,
                (displayName ?: "") as platform.Foundation.NSString
            )
        } else {
            currentUserData = null
        }
    }
    
    /**
     * Called from Kotlin to check if user is signed in.
     */
    fun isUserSignedIn(): Boolean {
        return currentUserData != null
    }
    
    /**
     * Called from Kotlin to get auth token.
     */
    fun getAuthToken(
        forceRefresh: Boolean,
        completion: (String?, String?) -> Unit
    ) {
        getAuthTokenCallback = completion
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseAuthGetToken",
            `object` = mapOf("forceRefresh" to forceRefresh)
        )
    }
    
    /**
     * Called from Kotlin to verify session.
     */
    fun verifySession(completion: (Boolean) -> Unit) {
        verifySessionCallback = completion
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "FirebaseAuthVerifySession",
            `object` = null
        )
    }
    
    /**
     * Called from Swift bridge when sign in completes.
     */
    fun reportSignInResult(
        success: Boolean,
        uid: String?,
        email: String?,
        displayName: String?,
        errorMessage: String?
    ) {
        signInCallback?.invoke(success, uid, email, displayName, errorMessage)
        signInCallback = null
    }
    
    /**
     * Called from Swift bridge when token is retrieved.
     */
    fun reportTokenResult(token: String?, error: String?) {
        getAuthTokenCallback?.invoke(token, error)
        getAuthTokenCallback = null
    }
    
    /**
     * Called from Swift bridge when session verification completes.
     */
    fun reportVerifySessionResult(isValid: Boolean) {
        verifySessionCallback?.invoke(isValid)
        verifySessionCallback = null
    }
}

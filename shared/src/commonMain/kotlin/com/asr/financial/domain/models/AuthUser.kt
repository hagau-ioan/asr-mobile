package com.asr.financial.domain.models

import kotlin.time.Clock

/**
 * Domain model for authenticated user
 */
data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val tokenExpiryTime: Long? = null // Timestamp when token expires (milliseconds since epoch)
) {
           /**
            * Check if the auth token is expired
            * @return true if token is expired or expiry time is not set, false otherwise
            */
           fun isTokenExpired(): Boolean {
               val currentTime = Clock.System.now().toEpochMilliseconds()
               return tokenExpiryTime == null || tokenExpiryTime < currentTime
           }
}

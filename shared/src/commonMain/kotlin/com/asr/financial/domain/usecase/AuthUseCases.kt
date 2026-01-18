package com.asr.financial.domain.usecase

import com.asr.financial.domain.models.AuthUser
import com.asr.financial.domain.repository.AuthRepository

/**
 * Use case for logging in with email and password
 */
class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {
        return repository.login(email, password)
    }
}

/**
 * Use case for logging out
 */
class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}

/**
 * Use case for checking authentication status
 */
class CheckAuthStatusUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.isAuthenticated()
    }
}

/**
 * Use case for getting current authenticated user
 */
class GetCurrentUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): AuthUser? {
        return repository.getCurrentUser()
    }
}

/**
 * Use case for refreshing authentication token
 */
class RefreshTokenUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<String> {
        return repository.refreshAuthToken()
    }
}

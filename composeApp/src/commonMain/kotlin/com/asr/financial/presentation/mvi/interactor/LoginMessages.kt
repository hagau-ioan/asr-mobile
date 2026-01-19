package com.asr.financial.presentation.mvi.interactor

/**
 * Message keys for Login screen.
 * These correspond to string resources.
 */
internal object LoginMessages {
    const val ERROR_EMPTY_EMAIL = "login_error_empty_email"
    const val ERROR_EMPTY_PASSWORD = "login_error_empty_password"
    const val ERROR_INVALID_EMAIL = "login_error_invalid_email"
    const val ERROR_WRONG_PASSWORD = "login_error_wrong_password"
    const val ERROR_INVALID_CREDENTIAL = "login_error_invalid_credential"
    const val ERROR_USER_NOT_FOUND = "login_error_user_not_found"
    const val ERROR_USER_DISABLED = "login_error_user_disabled"
    const val ERROR_TOO_MANY_REQUESTS = "login_error_too_many_requests"
    const val ERROR_OPERATION_NOT_ALLOWED = "login_error_operation_not_allowed"
    const val ERROR_NETWORK = "login_error_network"
    const val ERROR_UNKNOWN = "login_error_unknown"
}

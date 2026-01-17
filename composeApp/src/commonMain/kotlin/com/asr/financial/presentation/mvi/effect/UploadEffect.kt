package com.asr.financial.presentation.mvi.effect

/**
 * UI Effects for Upload Screen.
 * One-time events (navigation, toasts, camera launch, etc.)
 * 
 * Note: Messages are hardcoded for now due to StringResource limitations in suspend contexts.
 * TODO: Refactor to use resource IDs when Compose Resources supports suspend getString.
 */
sealed interface UploadEffect {
    data object LaunchCamera : UploadEffect
    data class ShowError(val message: String) : UploadEffect
    data class ShowSuccess(val message: String) : UploadEffect
}

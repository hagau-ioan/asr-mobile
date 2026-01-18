package com.asr.financial.presentation.mvi.state

/**
 * UI State for Upload Screen.
 * Uses String path instead of File for cross-platform compatibility.
 * 
 * Note: Error messages are hardcoded for now due to StringResource limitations.
 * TODO: Refactor to use resource IDs when Compose Resources supports better integration.
 */
sealed interface UploadState {
    data object Loading : UploadState
    data class Success(
        val currentReceiptPath: String? = null,
        val isCapturing: Boolean = false,
        val isUploading: Boolean = false,
        val uploadProgress: Float = 0f
    ) : UploadState
    data class Error(val message: String) : UploadState
}

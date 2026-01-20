package com.asr.financial.presentation.mvi.event

/**
 * UI Events for Upload Screen.
 * Represents user actions.
 */
sealed interface UploadEvent {
    data object LoadData : UploadEvent
    data object RequestCapture : UploadEvent
    data class CaptureCompleted(val success: Boolean) : UploadEvent
    data object DeleteImage : UploadEvent
    data object SendImage : UploadEvent
    data object Reset : UploadEvent
    // Cloud images events
    data object RefreshCloudImages : UploadEvent
    data class DeleteCloudImage(val remotePath: String) : UploadEvent
    data class ViewCloudImage(val remotePath: String) : UploadEvent
    data object DismissCloudImagePreview : UploadEvent
}

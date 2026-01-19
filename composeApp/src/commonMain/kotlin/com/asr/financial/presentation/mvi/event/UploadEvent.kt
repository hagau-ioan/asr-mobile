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
}

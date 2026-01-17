package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.platform.ImageCapture
import com.asr.financial.presentation.mvi.effect.UploadEffect
import com.asr.financial.presentation.mvi.event.UploadEvent
import com.asr.financial.presentation.mvi.state.UploadState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Interactor for Upload Screen.
 * Handles business logic for image capture and upload.
 * 
 * Note: Uses hardcoded strings for now. These should be moved to string resources
 * when Compose Resources provides better support for suspend contexts.
 * The strings are defined in strings.xml for reference.
 */
class UploadInteractor(
    private val imageCapture: ImageCapture
) {
    private val _uiState = MutableStateFlow<UploadState>(UploadState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<UploadEffect>(Channel.BUFFERED)
    val uiEffect: Flow<UploadEffect> = _uiEffectChannel.receiveAsFlow()

    suspend fun processEvent(event: UploadEvent) {
        when (event) {
            is UploadEvent.LoadData -> loadData()
            is UploadEvent.RequestCapture -> requestCapture()
            is UploadEvent.CaptureCompleted -> handleCaptureResult(event.success)
            is UploadEvent.DeleteImage -> deleteImage()
            is UploadEvent.SendImage -> sendImage()
        }
    }

    private suspend fun loadData() {
        try {
            val currentReceiptPath = imageCapture.getCurrentReceiptPath()
            _uiState.emit(UploadState.Success(currentReceiptPath = currentReceiptPath))
        } catch (e: Exception) {
            _uiState.emit(UploadState.Error("Eroare necunoscută")) // upload_error_unknown
        }
    }

    private suspend fun requestCapture() {
        val currentState = _uiState.value as? UploadState.Success ?: return
        _uiState.emit(currentState.copy(isCapturing = true))
        _uiEffectChannel.send(UploadEffect.LaunchCamera)
    }

    private suspend fun handleCaptureResult(success: Boolean) {
        try {
            val processedPath = imageCapture.processCapture(success)

            if (processedPath != null) {
                _uiState.emit(UploadState.Success(currentReceiptPath = processedPath))
                _uiEffectChannel.send(UploadEffect.ShowSuccess("Imagine capturată cu succes")) // upload_success_captured
            } else {
                val currentState = _uiState.value as? UploadState.Success
                _uiState.emit(currentState?.copy(isCapturing = false) ?: UploadState.Success())
                if (success) {
                    _uiEffectChannel.send(UploadEffect.ShowError("Eroare la procesarea imaginii")) // upload_error_processing
                }
            }
        } catch (e: Exception) {
            _uiState.emit(UploadState.Error("Eroare la capturare")) // upload_error_capture
            _uiEffectChannel.send(UploadEffect.ShowError("Eroare la capturare")) // upload_error_capture
        }
    }

    private suspend fun deleteImage() {
        try {
            val deleted = imageCapture.deleteCurrentReceipt()
            if (deleted) {
                _uiState.emit(UploadState.Success(currentReceiptPath = null))
                _uiEffectChannel.send(UploadEffect.ShowSuccess("Imagine ștearsă")) // upload_success_deleted
            }
        } catch (e: Exception) {
            _uiEffectChannel.send(UploadEffect.ShowError("Eroare la ștergere")) // upload_error_delete
        }
    }

    private suspend fun sendImage() {
        try {
            val currentState = _uiState.value as? UploadState.Success ?: return
            val path = currentState.currentReceiptPath ?: return

            // Show the image path
            _uiEffectChannel.send(UploadEffect.ShowSuccess("Cale: $path")) // upload_path_prefix
            
            // Wait 2 seconds then delete
            kotlinx.coroutines.delay(2000)
            
            val deleted = imageCapture.deleteCurrentReceipt()
            if (deleted) {
                _uiState.emit(UploadState.Success(currentReceiptPath = null))
            }
        } catch (e: Exception) {
            _uiEffectChannel.send(UploadEffect.ShowError("Eroare la trimitere")) // upload_error_send
        }
    }
}

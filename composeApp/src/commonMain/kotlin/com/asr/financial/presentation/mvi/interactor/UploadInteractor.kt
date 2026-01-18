package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.platform.Clock
import com.asr.financial.platform.FirebaseStorage
import com.asr.financial.platform.ImageCapture
import com.asr.financial.presentation.mvi.effect.UploadEffect
import com.asr.financial.presentation.mvi.event.UploadEvent
import com.asr.financial.presentation.mvi.state.UploadState
import com.asr.financial.utils.formatTimestampForFileName
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Interactor for Upload Screen.
 * Handles business logic for image capture and upload.
 */
class UploadInteractor(
    private val imageCapture: ImageCapture,
    private val clock: Clock,
    private val firebaseStorage: FirebaseStorage
) {
    private val _uiState = MutableStateFlow<UploadState>(UploadState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<UploadEffect>(Channel.BUFFERED)
    val uiEffect: Flow<UploadEffect> = _uiEffectChannel.receiveAsFlow()
    
    // Coroutine scope for launching progress updates from non-suspend callbacks
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
        } catch (_: Exception) {
            _uiState.emit(UploadState.Error(UploadMessages.ERROR_UNKNOWN))
        }
    }

    private suspend fun requestCapture() {
        val currentState = _uiState.value as? UploadState.Success ?: return
        _uiState.emit(currentState.copy(isCapturing = true))
        _uiEffectChannel.send(UploadEffect.LaunchCamera)
    }

    private suspend fun handleCaptureResult(success: Boolean) {
        try {
            val currentState = _uiState.value as? UploadState.Success ?: UploadState.Success()
            
            if (!success) {
                // User cancelled or capture failed
                _uiState.emit(currentState.copy(isCapturing = false))
                return
            }

            val processedPath = imageCapture.processCapture(success)

            if (processedPath != null) {
                // Rename file with timestamp
                val timestamp = formatTimestampForFileName(clock)
                val renamedPath = imageCapture.renameReceiptWithTimestamp(timestamp)
                
                if (renamedPath != null) {
                    // Success: Update state with renamed image path and reset capturing flag
                    _uiState.emit(UploadState.Success(
                        currentReceiptPath = renamedPath,
                        isCapturing = false
                    ))
                    _uiEffectChannel.send(UploadEffect.ShowSuccess(UploadMessages.SUCCESS_CAPTURED))
                } else {
                    // Rename failed, but keep the processed path
                    _uiState.emit(UploadState.Success(
                        currentReceiptPath = processedPath,
                        isCapturing = false
                    ))
                    _uiEffectChannel.send(UploadEffect.ShowSuccess(UploadMessages.SUCCESS_CAPTURED))
                }
            } else {
                // Processing failed: Reset capturing flag but keep existing state
                _uiState.emit(currentState.copy(isCapturing = false))
                _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_PROCESSING))
            }
        } catch (_: Exception) {
            // Exception during processing: Try to preserve existing state
            val currentState = _uiState.value as? UploadState.Success
            if (currentState != null) {
                _uiState.emit(currentState.copy(isCapturing = false))
            } else {
                _uiState.emit(UploadState.Error(UploadMessages.ERROR_CAPTURE))
            }
            _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_CAPTURE))
        }
    }

    private suspend fun deleteImage() {
        try {
            val deleted = imageCapture.deleteCurrentReceipt()
            if (deleted) {
                _uiState.emit(UploadState.Success(currentReceiptPath = null))
                _uiEffectChannel.send(UploadEffect.ShowSuccess(UploadMessages.SUCCESS_DELETED))
            }
        } catch (_: Exception) {
            _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_DELETE))
        }
    }

    private suspend fun sendImage() {
        try {
            val currentState = _uiState.value as? UploadState.Success ?: return
            val localPath = currentState.currentReceiptPath ?: return

            // Get current year and month for Firebase Storage path
            val year = getCurrentYear(clock)
            val month = getCurrentMonth(clock).toString().padStart(2, '0')
            
            // Extract filename from local path (should be receipt_YYYYMMDD_HHmmss.jpg)
            val fileName = localPath.substringAfterLast("/")
            
            // Build Firebase Storage path: documents/incoming/YYYY/MM/filename
            val remotePath = "${com.asr.financial.presentation.ui.constants.AppConstants.FirebaseStorage.DOCUMENTS_INCOMING_PATH_PREFIX}/$year/$month/$fileName"

            // Update state to show uploading
            _uiState.emit(currentState.copy(
                isUploading = true,
                uploadProgress = 0f
            ))

            // Upload file with progress tracking
            val uploadSuccess = firebaseStorage.uploadFile(
                localPath = localPath,
                remotePath = remotePath,
                onProgress = { progress ->
                    // Launch coroutine to emit state update since onProgress is not a suspend function
                    scope.launch {
                        _uiState.emit(currentState.copy(
                            isUploading = true,
                            uploadProgress = progress
                        ))
                    }
                }
            )

            if (uploadSuccess) {
                // Upload successful: Delete local file and show success
                imageCapture.deleteCurrentReceipt()
                _uiState.emit(UploadState.Success(
                    currentReceiptPath = null,
                    isUploading = false,
                    uploadProgress = 0f
                ))
                _uiEffectChannel.send(UploadEffect.ShowSuccess(UploadMessages.SUCCESS_SENT))
            } else {
                // Upload failed: Delete local file anyway and show error
                imageCapture.deleteCurrentReceipt()
                _uiState.emit(UploadState.Success(
                    currentReceiptPath = null,
                    isUploading = false,
                    uploadProgress = 0f
                ))
                _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_SEND))
            }
        } catch (_: Exception) {
            // Exception during upload: Delete local file and show error
            try {
                imageCapture.deleteCurrentReceipt()
            } catch (_: Exception) {
                // Ignore delete errors
            }
            val currentState = _uiState.value as? UploadState.Success
            _uiState.emit(currentState?.copy(
                isUploading = false,
                uploadProgress = 0f
            ) ?: UploadState.Success())
            _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_SEND))
        }
    }
}

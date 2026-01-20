package com.asr.financial.presentation.mvi.interactor

import com.asr.financial.platform.Clock
import com.asr.financial.platform.FileHandler
import com.asr.financial.platform.FirebaseStorage
import com.asr.financial.platform.ImageCapture
import com.asr.financial.presentation.mvi.effect.UploadEffect
import com.asr.financial.presentation.mvi.event.UploadEvent
import com.asr.financial.presentation.mvi.state.UploadState
import com.asr.financial.presentation.ui.constants.AppConstants
import com.asr.financial.utils.formatTimestampForFileName
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    private val firebaseStorage: FirebaseStorage,
    private val fileHandler: FileHandler
) {
    private val _uiState = MutableStateFlow<UploadState>(UploadState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEffectChannel = Channel<UploadEffect>(Channel.BUFFERED)
    val uiEffect: Flow<UploadEffect> = _uiEffectChannel.receiveAsFlow()
    
    // Coroutine scope for launching progress updates from non-suspend callbacks
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Session-based counter to prevent duplicate filenames when images are taken quickly
    // Resets when entering the screen (in loadData)
    private var sessionCounter = 0
    
    // Reference to the current upload job for cancellation
    private var currentUploadJob: kotlinx.coroutines.Job? = null

    /**
     * Builds the Firebase Storage path for the current year/month.
     */
    private fun buildCurrentMonthCloudPath(): String {
        val year = getCurrentYear(clock)
        val month = getCurrentMonth(clock).toString().padStart(2, '0')
        return "${AppConstants.FirebaseStorage.DOCUMENTS_INCOMING_PATH_PREFIX}/$year/$month"
    }

    suspend fun processEvent(event: UploadEvent) {
        when (event) {
            is UploadEvent.LoadData -> loadData()
            is UploadEvent.RequestCapture -> requestCapture()
            is UploadEvent.CaptureCompleted -> handleCaptureResult(event.success)
            is UploadEvent.DeleteImage -> deleteImage()
            is UploadEvent.SendImage -> sendImage()
            is UploadEvent.Reset -> reset()
            is UploadEvent.RefreshCloudImages -> refreshCloudImages()
            is UploadEvent.DeleteCloudImage -> deleteCloudImage(event.remotePath)
            is UploadEvent.ViewCloudImage -> viewCloudImage(event.remotePath)
            is UploadEvent.DismissCloudImagePreview -> dismissCloudImagePreview()
        }
    }

    private suspend fun loadData() {
        try {
            // Reset session counter when entering the screen
            sessionCounter = 0
            
            // Clean up any leftover temp files from previous sessions
            fileHandler.cleanupTempFiles("firebase_")
            
            val currentReceiptPath = imageCapture.getCurrentReceiptPath()
            _uiState.emit(UploadState.Success(currentReceiptPath = currentReceiptPath))
            // Load cloud images after initial state is set
            refreshCloudImages()
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
                // Increment session counter for this capture
                sessionCounter++

                // Rename file with timestamp and session counter to prevent duplicates
                val timestamp = formatTimestampForFileName(clock)
                val timestampWithCounter = "${timestamp}_${sessionCounter.toString().padStart(3, '0')}"
                val renamedPath = imageCapture.renameReceiptWithTimestamp(timestampWithCounter)

                // Use renamed path if successful, otherwise keep the processed path
                val finalPath = renamedPath ?: processedPath
                val existingCloudImages = currentState.cloudImages

                _uiState.emit(UploadState.Success(
                    currentReceiptPath = finalPath,
                    isCapturing = false,
                    cloudImages = existingCloudImages
                ))
                _uiEffectChannel.send(UploadEffect.ShowSuccess(UploadMessages.SUCCESS_CAPTURED))
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
            val currentState = _uiState.value as? UploadState.Success
            val deleted = imageCapture.deleteCurrentReceipt()
            if (deleted) {
                _uiState.emit(UploadState.Success(
                    currentReceiptPath = null,
                    cloudImages = currentState?.cloudImages ?: emptyList()
                ))
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

            // Extract filename from local path (should be receipt_YYYYMMDD_HHmmss.jpg)
            val fileName = localPath.substringAfterLast("/")

            // Build Firebase Storage path: documents/incoming/YYYY/MM/filename
            val remotePath = "${buildCurrentMonthCloudPath()}/$fileName"

            // Update state to show uploading
            _uiState.emit(currentState.copy(
                isUploading = true,
                uploadProgress = 0f,
                canResetDuringUpload = false
            ))

            // Start timeout coroutine to enable reset button after timeout
            val timeoutJob = scope.launch {
                delay(AppConstants.Time.UPLOAD_TIMEOUT_MS)
                // Enable reset button after timeout if upload is still in progress
                val state = _uiState.value as? UploadState.Success
                if (state?.isUploading == true) {
                    _uiState.emit(state.copy(canResetDuringUpload = true))
                }
            }

            // Create upload job that can be cancelled
            val uploadJob = scope.launch {
                try {
                    // Upload file with progress tracking
                    val uploadSuccess = firebaseStorage.uploadFile(
                        localPath = localPath,
                        remotePath = remotePath,
                        onProgress = { progress ->
                            // Launch coroutine to emit state update since onProgress is not a suspend function
                            scope.launch {
                                val state = _uiState.value as? UploadState.Success
                                _uiState.emit(state?.copy(
                                    isUploading = true,
                                    uploadProgress = progress
                                ) ?: currentState.copy(
                                    isUploading = true,
                                    uploadProgress = progress
                                ))
                            }
                        }
                    )
                    
                    // Cancel timeout job since upload completed
                    timeoutJob.cancel()
                    
                    // Handle upload result
                    if (uploadSuccess) {
                        // Upload successful: Delete local file and show success
                        imageCapture.deleteCurrentReceipt()
                        val existingCloudImages = (_uiState.value as? UploadState.Success)?.cloudImages ?: emptyList()
                        _uiState.emit(UploadState.Success(
                            currentReceiptPath = null,
                            isCapturing = false,
                            isUploading = false,
                            uploadProgress = 0f,
                            canResetDuringUpload = false,
                            cloudImages = existingCloudImages
                        ))
                        _uiEffectChannel.send(UploadEffect.ShowSuccess(UploadMessages.SUCCESS_SENT))
                        // Refresh cloud images list after successful upload
                        refreshCloudImages()
                    } else {
                        // Upload failed: Delete local file anyway and show error
                        imageCapture.deleteCurrentReceipt()
                        _uiState.emit(UploadState.Success(
                            currentReceiptPath = null,
                            isCapturing = false,
                            isUploading = false,
                            uploadProgress = 0f,
                            canResetDuringUpload = false
                        ))
                        _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_SEND))
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // Upload was cancelled - this is expected when reset is pressed
                    timeoutJob.cancel()
                    throw e // Re-throw to propagate cancellation
                } catch (_: Exception) {
                    // Exception during upload: Delete local file and show error
                    timeoutJob.cancel()
                    try {
                        imageCapture.deleteCurrentReceipt()
                    } catch (_: Exception) {
                        // Ignore delete errors
                    }
                    val state = _uiState.value as? UploadState.Success
                    _uiState.emit(state?.copy(
                        isCapturing = false,
                        isUploading = false,
                        uploadProgress = 0f,
                        canResetDuringUpload = false
                    ) ?: UploadState.Success())
                    _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_SEND))
                } finally {
                    // Clear upload job reference
                    currentUploadJob = null
                }
            }
            
            // Store upload job reference for cancellation
            currentUploadJob = uploadJob
            uploadJob.join() // Wait for upload to complete or be cancelled
            
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Upload was cancelled - this is expected when reset is pressed
            // State will be reset by the reset() function
            currentUploadJob = null
            throw e // Re-throw to propagate cancellation
        } catch (_: Exception) {
            // Other exceptions during upload setup
            currentUploadJob = null
            val currentState = _uiState.value as? UploadState.Success
            _uiState.emit(currentState?.copy(
                isCapturing = false,
                isUploading = false,
                uploadProgress = 0f,
                canResetDuringUpload = false
            ) ?: UploadState.Success())
            _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_SEND))
        }
    }

    private suspend fun reset() {
        try {
            val currentState = _uiState.value as? UploadState.Success
            val wasUploading = currentState?.isUploading == true

            // Cancel ongoing upload if it exists
            currentUploadJob?.cancel()
            currentUploadJob = null

            // Cancel any ongoing upload by resetting state
            // Delete current receipt if exists
            try {
                imageCapture.deleteCurrentReceipt()
            } catch (_: Exception) {
                // Ignore delete errors
            }

            // Reset to initial state and reset session counter, but preserve cloud images
            sessionCounter = 0
            
            // Clean up any temp files
            fileHandler.cleanupTempFiles("firebase_")
            
            _uiState.emit(UploadState.Success(
                currentReceiptPath = null,
                isCapturing = false,
                isUploading = false,
                uploadProgress = 0f,
                canResetDuringUpload = false,
                cloudImages = currentState?.cloudImages ?: emptyList()
            ))

            // If reset was pressed during upload, show error message
            if (wasUploading) {
                _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_UPLOAD_CANCELLED))
            }
        } catch (_: Exception) {
            // If reset fails, try to at least set a clean state
            _uiState.emit(UploadState.Success())
        }
    }

    private suspend fun refreshCloudImages() {
        try {
            val currentState = _uiState.value as? UploadState.Success ?: return

            // Set loading state
            _uiState.emit(currentState.copy(isLoadingCloudImages = true))

            // Fetch cloud images from current year/month path
            val cloudImages = firebaseStorage.listFiles(buildCurrentMonthCloudPath())

            // Update state with cloud images
            val updatedState = _uiState.value as? UploadState.Success ?: currentState
            _uiState.emit(updatedState.copy(
                cloudImages = cloudImages,
                isLoadingCloudImages = false
            ))
        } catch (_: Exception) {
            val currentState = _uiState.value as? UploadState.Success
            _uiState.emit(currentState?.copy(isLoadingCloudImages = false) ?: UploadState.Success())
            _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_LOAD_CLOUD_IMAGES))
        }
    }

    private suspend fun deleteCloudImage(remotePath: String) {
        try {
            val currentState = _uiState.value as? UploadState.Success ?: return

            // Set deleting state
            _uiState.emit(currentState.copy(isDeletingCloudImage = true))

            // Delete from Firebase Storage
            val deleted = firebaseStorage.deleteFile(remotePath)

            if (deleted) {
                // Remove from local list
                val updatedImages = currentState.cloudImages.filter { it.path != remotePath }
                _uiState.emit(currentState.copy(
                    cloudImages = updatedImages,
                    isDeletingCloudImage = false
                ))
                _uiEffectChannel.send(UploadEffect.ShowSuccess(UploadMessages.SUCCESS_CLOUD_IMAGE_DELETED))
                // Refresh cloud images list to ensure consistency
                refreshCloudImages()
            } else {
                _uiState.emit(currentState.copy(isDeletingCloudImage = false))
                _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_DELETE_CLOUD_IMAGE))
            }
        } catch (_: Exception) {
            val currentState = _uiState.value as? UploadState.Success
            _uiState.emit(currentState?.copy(isDeletingCloudImage = false) ?: UploadState.Success())
            _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_DELETE_CLOUD_IMAGE))
        }
    }

    private suspend fun viewCloudImage(remotePath: String) {
        try {
            val currentState = _uiState.value as? UploadState.Success ?: return

            // Clean up ALL temp files (including any leftover from crashes)
            fileHandler.cleanupTempFiles("firebase_")

            // Set downloading state
            _uiState.emit(currentState.copy(isDownloadingForPreview = true))

            // Download to temp location
            val localPath = firebaseStorage.downloadFileToTemp(remotePath)

            if (localPath != null) {
                val updatedState = _uiState.value as? UploadState.Success ?: currentState
                _uiState.emit(updatedState.copy(
                    cloudImagePreviewPath = localPath,
                    isDownloadingForPreview = false
                ))
            } else {
                _uiState.emit(currentState.copy(isDownloadingForPreview = false))
                _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_DOWNLOAD_PREVIEW))
            }
        } catch (_: Exception) {
            val currentState = _uiState.value as? UploadState.Success
            _uiState.emit(currentState?.copy(isDownloadingForPreview = false) ?: UploadState.Success())
            _uiEffectChannel.send(UploadEffect.ShowError(UploadMessages.ERROR_DOWNLOAD_PREVIEW))
        }
    }

    private suspend fun dismissCloudImagePreview() {
        val currentState = _uiState.value as? UploadState.Success ?: return
        
        // Clean up ALL temp files (including any leftover from crashes)
        fileHandler.cleanupTempFiles("firebase_")
        
        _uiState.emit(currentState.copy(cloudImagePreviewPath = null))
    }
}

package com.asr.financial.presentation.screens.upload

import androidx.compose.runtime.*

/**
 * iOS implementation of CameraCapture.
 * The actual camera UI must be handled in SwiftUI layer using UIImagePickerController.
 *
 * This implementation provides a callback mechanism that the iOS native code can use
 * to trigger camera capture and report results back.
 */
@Composable
actual fun CameraCapture(
    onCaptureResult: (Boolean) -> Unit,
    content: @Composable (launchCamera: () -> Unit) -> Unit
) {
    DisposableEffect(Unit) {
        CameraBridge.captureCallback = onCaptureResult
        onDispose {
            CameraBridge.captureCallback = null
        }
    }

    content {
        CameraBridge.requestCapture()
    }
}

/**
 * Bridge object for iOS camera communication.
 * SwiftUI layer should observe cameraRequested and present UIImagePickerController.
 * When capture completes, call reportCaptureResult(success).
 */
object CameraBridge {
    internal var captureCallback: ((Boolean) -> Unit)? = null
    private var _cameraRequested = mutableStateOf(false)

    val cameraRequested: State<Boolean> get() = _cameraRequested

    internal fun requestCapture() {
        _cameraRequested.value = true
        // Post notification to SwiftUI
        platform.Foundation.NSNotificationCenter.defaultCenter.postNotificationName(
            "LaunchCamera",
            `object` = null
        )
    }

    /**
     * Call this from Swift when camera capture completes.
     */
    fun reportCaptureResult(success: Boolean) {
        _cameraRequested.value = false
        captureCallback?.invoke(success)
    }

    /**
     * Call this from Swift when camera is dismissed without capture.
     */
    fun cancelCapture() {
        _cameraRequested.value = false
        captureCallback?.invoke(false)
    }
}

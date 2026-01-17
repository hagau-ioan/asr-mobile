package com.asr.financial.presentation.screens.upload

import androidx.compose.runtime.Composable

/**
 * Composable wrapper providing platform-specific camera capture functionality.
 *
 * @param onCaptureResult Callback when camera capture completes (true for success, false for cancel/failure)
 * @param content Composable content that receives a launch callback to trigger camera capture
 */
@Composable
expect fun CameraCapture(
    onCaptureResult: (Boolean) -> Unit,
    content: @Composable (launchCamera: () -> Unit) -> Unit
)

package com.asr.financial.presentation.screens.upload

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.asr.financial.platform.ImageCapture
import org.koin.compose.koinInject

@Composable
actual fun CameraCapture(
    onCaptureResult: (Boolean) -> Unit,
    content: @Composable (launchCamera: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val imageCapture: ImageCapture = koinInject()

    var pendingCameraLaunch by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        onCaptureResult(success)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingCameraLaunch = true
        } else {
            onCaptureResult(false)
        }
    }

    LaunchedEffect(pendingCameraLaunch) {
        if (pendingCameraLaunch) {
            pendingCameraLaunch = false
            val uri = imageCapture.getReceiptUri()
            cameraLauncher.launch(uri)
        }
    }

    content {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            val uri = imageCapture.getReceiptUri()
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

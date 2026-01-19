package com.asr.financial.presentation.screens.upload

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max
import kotlin.math.min
import coil3.compose.rememberAsyncImagePainter
import com.asr.financial.presentation.mvi.effect.UploadEffect
import com.asr.financial.presentation.mvi.event.UploadEvent
import com.asr.financial.presentation.mvi.interactor.UploadMessages
import com.asr.financial.presentation.mvi.state.UploadState
import com.asr.financial.presentation.mvi.viewmodel.UploadViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import asr_financial.composeapp.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UploadScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: UploadViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var launchCameraRequested by remember { mutableStateOf(false) }

    CameraCapture(
        onCaptureResult = { success ->
            viewModel.handleEvent(UploadEvent.CaptureCompleted(success))
        }
    ) { launchCamera ->
        LaunchedEffect(launchCameraRequested) {
            if (launchCameraRequested) {
                launchCamera()
                // Reset flag so LaunchedEffect can trigger again for next capture
                launchCameraRequested = false
            }
        }

        // Pre-compute string resources in composable context
        val defaultErrorMessage = stringResource(Res.string.upload_error_unknown)
        val errorUnknown = stringResource(Res.string.upload_error_unknown)
        val errorCapture = stringResource(Res.string.upload_error_capture)
        val errorProcessing = stringResource(Res.string.upload_error_processing)
        val errorDelete = stringResource(Res.string.upload_error_delete)
        val errorSend = stringResource(Res.string.upload_error_send)
        val errorUploadCancelled = stringResource(Res.string.upload_error_upload_cancelled)
        val successCaptured = stringResource(Res.string.upload_success_captured)
        val successDeleted = stringResource(Res.string.upload_success_deleted)
        val successSent = stringResource(Res.string.upload_success_sent)
        
        val errorMessages = remember(errorUnknown, errorCapture, errorProcessing, errorDelete, errorSend, errorUploadCancelled) {
            mapOf(
                UploadMessages.ERROR_UNKNOWN to errorUnknown,
                UploadMessages.ERROR_CAPTURE to errorCapture,
                UploadMessages.ERROR_PROCESSING to errorProcessing,
                UploadMessages.ERROR_DELETE to errorDelete,
                UploadMessages.ERROR_SEND to errorSend,
                UploadMessages.ERROR_UPLOAD_CANCELLED to errorUploadCancelled
            )
        }
        val successMessages = remember(successCaptured, successDeleted, successSent) {
            mapOf(
                UploadMessages.SUCCESS_CAPTURED to successCaptured,
                UploadMessages.SUCCESS_DELETED to successDeleted,
                UploadMessages.SUCCESS_SENT to successSent
            )
        }

        LaunchedEffect(Unit) {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    is UploadEffect.LaunchCamera -> {
                        launchCameraRequested = true
                    }
                    is UploadEffect.ShowError -> {
                        val message = errorMessages[effect.message] ?: defaultErrorMessage
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                    is UploadEffect.ShowSuccess -> {
                        val message = successMessages[effect.message] ?: effect.message
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }

        Box {
            when (val state = uiState) {
                is UploadState.Success -> {
                    UploadSuccessContent(
                        state = state,
                        windowSizeClass = windowSizeClass,
                        onNavigate = onNavigate,
                        onMenuClick = onMenuClick,
                        onCaptureClick = { viewModel.handleEvent(UploadEvent.RequestCapture) },
                        onDeleteClick = { viewModel.handleEvent(UploadEvent.DeleteImage) },
                        onSendClick = { viewModel.handleEvent(UploadEvent.SendImage) },
                        onResetClick = { viewModel.handleEvent(UploadEvent.Reset) }
                    )
                }
                is UploadState.Loading -> {
                    ScreenLayout(
                        windowSizeClass = windowSizeClass,
                        breadcrumbItems = listOf(
                            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                            BreadcrumbItem(stringResource(Res.string.nav_upload))
                        ),
                        onNavigate = onNavigate,
                        onMenuClick = onMenuClick
                    ) {
                        item {
                            LoadingContent()
                        }
                    }
                }
                is UploadState.Error -> {
                    UploadErrorContent(
                        message = state.message,
                        windowSizeClass = windowSizeClass,
                        onNavigate = onNavigate,
                        onMenuClick = onMenuClick,
                        onRetryClick = { viewModel.handleEvent(UploadEvent.LoadData) }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun UploadSuccessContent(
    state: UploadState.Success,
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSendClick: () -> Unit,
    onResetClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_upload))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            val isDarkTheme = isSystemInDarkTheme()
            // Parent card: use secondaryContainer with low alpha for light mode (grey tint)
            // This is different from the tips section which uses primaryContainer
            val parentCardColor = if (isDarkTheme) {
                CardDefaults.cardColors()
            } else {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = parentCardColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CARD_PADDING_DP.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.upload_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(SECTION_SPACING_DP.dp))

                    if (state.currentReceiptPath != null) {
                        var showFullScreenImage by remember { mutableStateOf(false) }
                        
                        Image(
                            painter = rememberAsyncImagePainter(state.currentReceiptPath),
                            contentDescription = stringResource(Res.string.cd_receipt_preview),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showFullScreenImage = true },
                            contentScale = ContentScale.Fit // Maintains aspect ratio, click to see full screen
                        )
                        
                        // Full-screen image viewer dialog
                        if (showFullScreenImage) {
                            FullScreenImageViewer(
                                imagePath = state.currentReceiptPath,
                                onDismiss = { showFullScreenImage = false }
                            )
                        }

                        Spacer(Modifier.height(SECTION_SPACING_DP.dp))

                        // Show upload progress if uploading
                        if (state.isUploading) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    progress = { state.uploadProgress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(Res.string.upload_uploading),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(SECTION_SPACING_DP.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (state.isUploading) {
                                // Show reset button when uploading (enabled after timeout)
                                Button(
                                    onClick = onResetClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.canResetDuringUpload,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(Res.string.upload_reset))
                                }
                            } else {
                                // Normal buttons when not uploading
                                Button(
                                    onClick = onDeleteClick,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(Res.string.upload_delete))
                                }

                                Button(
                                    onClick = onSendClick,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(Res.string.upload_send))
                                }
                            }
                        }
                    } else {
                        // Show description
                        Text(
                            text = stringResource(Res.string.upload_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(SECTION_SPACING_DP.dp))

                        // Photo guidelines section - darker background for light mode
                        val notesCardColor = if (isDarkTheme) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = notesCardColor
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.upload_guidelines_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = stringResource(Res.string.upload_guideline_light),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = stringResource(Res.string.upload_guideline_surface),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = stringResource(Res.string.upload_guideline_preview),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(SECTION_SPACING_DP.dp))

                        Text(
                            text = stringResource(Res.string.upload_no_image),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(SECTION_SPACING_DP.dp))

                        Button(
                            onClick = onCaptureClick,
                            enabled = !state.isCapturing
                        ) {
                            if (state.isCapturing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.upload_capture))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadErrorContent(
    message: String,
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_upload))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(CARD_PADDING_DP.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(onClick = onRetryClick) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenImageViewer(
    imagePath: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // Reset zoom when dialog is dismissed
    LaunchedEffect(Unit) {
        scale = 1f
        offset = Offset.Zero
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            // Zoomable full-screen image (behind buttons)
            Image(
                painter = rememberAsyncImagePainter(imagePath),
                contentDescription = "Full screen receipt image",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        // Pinch-to-zoom (works on Android, limited on iOS)
                        detectTransformGestures { _, pan, zoom, _ ->
                            // On iOS, zoom might be 1f even with pinch, so we check if it's actually changing
                            if (zoom != 1f) {
                                scale = max(1f, min(scale * zoom, 5f)) // Limit zoom between 1x and 5x
                            }
                            // Pan works on both platforms
                            offset += pan
                        }
                    }
                    .pointerInput(Unit) {
                        // Double-tap to zoom (works on both Android and iOS)
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                // Double-tap to zoom in/out
                                if (scale > 1f) {
                                    // Reset zoom
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    // Zoom in to 2x at tap location
                                    scale = 2f
                                    offset = Offset(
                                        -tapOffset.x * (scale - 1f),
                                        -tapOffset.y * (scale - 1f)
                                    )
                                }
                            }
                        )
                    },
                contentScale = ContentScale.Fit
            )
            
            // Top right corner controls (close button and zoom controls)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                val zoomOutButtonColor = if (scale > 1f) {
                    surfaceVariantColor
                } else {
                    surfaceVariantColor.copy(alpha = 0.5f)
                }
                
                // Zoom in button
                FloatingActionButton(
                    onClick = {
                        scale = min(scale * 1.5f, 5f) // Zoom in by 1.5x, max 5x
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = surfaceVariantColor,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom in",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Zoom out button
                FloatingActionButton(
                    onClick = {
                        if (scale > 1f) {
                            scale = max(scale / 1.5f, 1f) // Zoom out by 1.5x, min 1x
                            if (scale == 1f) {
                                offset = Offset.Zero // Reset offset when zoomed out completely
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = zoomOutButtonColor,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom out",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

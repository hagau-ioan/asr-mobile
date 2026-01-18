package com.asr.financial.presentation.screens.upload

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        val successCaptured = stringResource(Res.string.upload_success_captured)
        val successDeleted = stringResource(Res.string.upload_success_deleted)
        val successSent = stringResource(Res.string.upload_success_sent)
        
        val errorMessages = remember(errorUnknown, errorCapture, errorProcessing, errorDelete, errorSend) {
            mapOf(
                UploadMessages.ERROR_UNKNOWN to errorUnknown,
                UploadMessages.ERROR_CAPTURE to errorCapture,
                UploadMessages.ERROR_PROCESSING to errorProcessing,
                UploadMessages.ERROR_DELETE to errorDelete,
                UploadMessages.ERROR_SEND to errorSend
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
                        onSendClick = { viewModel.handleEvent(UploadEvent.SendImage) }
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
    onSendClick: () -> Unit
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
            Card(modifier = Modifier.fillMaxWidth()) {
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
                        Image(
                            painter = rememberAsyncImagePainter(state.currentReceiptPath),
                            contentDescription = stringResource(Res.string.cd_receipt_preview),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )

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
                            Button(
                                onClick = onDeleteClick,
                                modifier = Modifier.weight(1f),
                                enabled = !state.isUploading,
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
                                modifier = Modifier.weight(1f),
                                enabled = !state.isUploading
                            ) {
                                if (state.isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(Res.string.upload_send))
                            }
                        }
                    } else {
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


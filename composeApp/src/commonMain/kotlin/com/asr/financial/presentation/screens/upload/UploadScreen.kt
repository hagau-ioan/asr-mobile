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
import com.asr.financial.presentation.mvi.state.UploadState
import com.asr.financial.presentation.mvi.viewmodel.UploadViewModel
import com.asr.financial.presentation.ui.components.BreadcrumbItem
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
                launchCameraRequested = false
                launchCamera()
            }
        }

        LaunchedEffect(Unit) {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    is UploadEffect.LaunchCamera -> {
                        launchCameraRequested = true
                    }
                    is UploadEffect.ShowError -> {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                    is UploadEffect.ShowSuccess -> {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
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
                    UploadLoadingContent(
                        windowSizeClass = windowSizeClass,
                        onNavigate = onNavigate,
                        onMenuClick = onMenuClick
                    )
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
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
private fun UploadLoadingContent(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
            BreadcrumbItem(stringResource(Res.string.nav_upload))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
            BreadcrumbItem(stringResource(Res.string.nav_home), "home"),
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

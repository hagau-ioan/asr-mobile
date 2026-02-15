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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max
import kotlin.math.min
import coil3.compose.rememberAsyncImagePainter
import com.asr.financial.platform.CloudStorageFile
import com.asr.financial.presentation.mvi.effect.UploadEffect
import com.asr.financial.presentation.mvi.event.UploadEvent
import com.asr.financial.presentation.mvi.interactor.UploadMessages
import com.asr.financial.presentation.mvi.state.UploadState
import com.asr.financial.presentation.mvi.viewmodel.UploadViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.dialogs.DeleteConfirmationDialog
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.components.table.DataTable
import com.asr.financial.presentation.ui.components.table.TableCell
import com.asr.financial.presentation.ui.components.table.TableColumn
import com.asr.financial.presentation.ui.components.table.TableHeaderCell
import com.asr.financial.presentation.ui.components.table.TableRow
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getMonthsList
import asr_financial.composeapp.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UploadScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: UploadViewModel = koinViewModel(),
    clock: com.asr.financial.platform.Clock = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var launchCameraRequested by remember { mutableStateOf(false) }

    val months = remember { getMonthsList() }
    val (headerMonthNum, headerYear) = calculatePreviousMonth(getCurrentMonth(clock), getCurrentYear(clock))
    val headerMonthName = months.find { it.first == headerMonthNum }?.second?.let { stringResource(it) } ?: ""

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
        val errorLoadCloud = stringResource(Res.string.upload_error_load_cloud)
        val errorDeleteCloud = stringResource(Res.string.upload_error_delete_cloud)
        val errorDownloadPreview = stringResource(Res.string.upload_error_download_preview)
        val successCaptured = stringResource(Res.string.upload_success_captured)
        val successDeleted = stringResource(Res.string.upload_success_deleted)
        val successSent = stringResource(Res.string.upload_success_sent)
        val successCloudDeleted = stringResource(Res.string.upload_success_cloud_deleted)

        val errorMessages = remember(errorUnknown, errorCapture, errorProcessing, errorDelete, errorSend, errorUploadCancelled, errorLoadCloud, errorDeleteCloud, errorDownloadPreview) {
            mapOf(
                UploadMessages.ERROR_UNKNOWN to errorUnknown,
                UploadMessages.ERROR_CAPTURE to errorCapture,
                UploadMessages.ERROR_PROCESSING to errorProcessing,
                UploadMessages.ERROR_DELETE to errorDelete,
                UploadMessages.ERROR_SEND to errorSend,
                UploadMessages.ERROR_UPLOAD_CANCELLED to errorUploadCancelled,
                UploadMessages.ERROR_LOAD_CLOUD_IMAGES to errorLoadCloud,
                UploadMessages.ERROR_DELETE_CLOUD_IMAGE to errorDeleteCloud,
                UploadMessages.ERROR_DOWNLOAD_PREVIEW to errorDownloadPreview
            )
        }
        val successMessages = remember(successCaptured, successDeleted, successSent, successCloudDeleted) {
            mapOf(
                UploadMessages.SUCCESS_CAPTURED to successCaptured,
                UploadMessages.SUCCESS_DELETED to successDeleted,
                UploadMessages.SUCCESS_SENT to successSent,
                UploadMessages.SUCCESS_CLOUD_IMAGE_DELETED to successCloudDeleted
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
                        headerMonthName = headerMonthName,
                        headerYear = headerYear,
                        onNavigate = onNavigate,
                        onMenuClick = onMenuClick,
                        onCaptureClick = { viewModel.handleEvent(UploadEvent.RequestCapture) },
                        onDeleteClick = { viewModel.handleEvent(UploadEvent.DeleteImage) },
                        onSendClick = { viewModel.handleEvent(UploadEvent.SendImage) },
                        onResetClick = { viewModel.handleEvent(UploadEvent.Reset) },
                        onRefreshCloudImages = { viewModel.handleEvent(UploadEvent.RefreshCloudImages) },
                        onDeleteCloudImage = { path -> viewModel.handleEvent(UploadEvent.DeleteCloudImage(path)) },
                        onViewCloudImage = { path -> viewModel.handleEvent(UploadEvent.ViewCloudImage(path)) },
                        onDismissCloudImagePreview = { viewModel.handleEvent(UploadEvent.DismissCloudImagePreview) }
                    )
                }
                is UploadState.Loading -> {
                    ScreenLayout(
                        windowSizeClass = windowSizeClass,
                        breadcrumbItems = listOf(
                            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
                            BreadcrumbItem(stringResource(Res.string.nav_upload))
                        ),
                        selectedMonth = headerMonthName,
                        selectedYear = headerYear,
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
                        headerMonthName = headerMonthName,
                        headerYear = headerYear,
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
    headerMonthName: String,
    headerYear: Int,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSendClick: () -> Unit,
    onResetClick: () -> Unit,
    onRefreshCloudImages: () -> Unit,
    onDeleteCloudImage: (String) -> Unit,
    onViewCloudImage: (String) -> Unit,
    onDismissCloudImagePreview: () -> Unit
) {
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_upload))
        ),
        selectedMonth = headerMonthName,
        selectedYear = headerYear,
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

                        // Photo guidelines section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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

                                GuidelineRow(stringResource(Res.string.upload_guideline_light))
                                GuidelineRow(stringResource(Res.string.upload_guideline_surface))
                                GuidelineRow(stringResource(Res.string.upload_guideline_preview))
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

        // Cloud images table section
        item {
            Spacer(Modifier.height(SECTION_SPACING_DP.dp))

            CloudImagesSection(
                cloudImages = state.cloudImages,
                isLoading = state.isLoadingCloudImages,
                isDeleting = state.isDeletingCloudImage,
                onRefresh = onRefreshCloudImages,
                onDeleteImage = onDeleteCloudImage,
                onViewImage = onViewCloudImage
            )
        }
    }

    // Cloud image preview dialog
    if (state.cloudImagePreviewPath != null) {
        FullScreenImageViewer(
            imagePath = state.cloudImagePreviewPath,
            onDismiss = onDismissCloudImagePreview
        )
    }

    // Show loading overlay when downloading for preview
    if (state.isDownloadingForPreview) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(Res.string.upload_downloading_preview))
                }
            }
        }
    }
}

@Composable
private fun CloudImagesSection(
    cloudImages: List<CloudStorageFile>,
    isLoading: Boolean,
    isDeleting: Boolean,
    onRefresh: () -> Unit,
    onDeleteImage: (String) -> Unit,
    onViewImage: (String) -> Unit
) {
    var pendingDeleteFile by remember { mutableStateOf<CloudStorageFile?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING_DP.dp)
        ) {
            // Header with title and refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.upload_cloud_images_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading && !isDeleting
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.upload_refresh_cloud_images)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.upload_cloud_images_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(SECTION_SPACING_DP.dp))

            if (isLoading && cloudImages.isEmpty()) {
                // Show loading indicator only if no cached data
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (cloudImages.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.upload_no_cloud_images),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Cloud images table
                CloudImagesTable(
                    cloudImages = cloudImages,
                    isDeleting = isDeleting,
                    onRequestDelete = { file -> pendingDeleteFile = file },
                    onViewImage = onViewImage
                )
            }
        }
    }

    pendingDeleteFile?.let { file ->
        DeleteConfirmationDialog(
            title = stringResource(Res.string.delete_confirm_title),
            message = stringResource(Res.string.delete_confirm_message, file.name),
            confirmText = stringResource(Res.string.delete_confirm_delete),
            cancelText = stringResource(Res.string.delete_confirm_cancel),
            isProcessing = isDeleting,
            onConfirm = {
                onDeleteImage(file.path)
                pendingDeleteFile = null
            },
            onDismiss = { pendingDeleteFile = null }
        )
    }
}

// Table column widths (fixed columns only)
private const val COL_DATE_WIDTH_DP = 50
private const val COL_ACTION_WIDTH_DP = 44

@Composable
private fun CloudImagesTable(
    cloudImages: List<CloudStorageFile>,
    isDeleting: Boolean,
    onRequestDelete: (CloudStorageFile) -> Unit,
    onViewImage: (String) -> Unit
) {
    // Pre-compute string resources
    val colNameText = stringResource(Res.string.upload_col_name)
    val colDateText = stringResource(Res.string.upload_col_date)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Calculate available width: total width - row padding (12dp on each side = 24dp) - spacing (8dp between columns * 2 = 16dp) - fixed columns
        val rowPadding = 24.dp // 12.dp on each side from Row padding
        val totalSpacing = 16.dp // 8.dp spacing between 3 columns (2 gaps)
        val fixedColumnsWidth = COL_DATE_WIDTH_DP.dp + COL_ACTION_WIDTH_DP.dp
        val nameColumnWidth = (maxWidth - rowPadding - totalSpacing - fixedColumnsWidth).coerceAtLeast(100.dp)

        DataTable(
            columns = listOf(
                TableColumn(colNameText, nameColumnWidth),
                TableColumn(colDateText, COL_DATE_WIDTH_DP.dp),
                TableColumn("", COL_ACTION_WIDTH_DP.dp, TextAlign.Center)
            ),
            headerContent = {
                // Name column header - calculated width
                Box(modifier = Modifier.width(nameColumnWidth)) {
                    Text(
                        text = colNameText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Date column header - fixed width
                TableHeaderCell(
                    text = colDateText,
                    width = COL_DATE_WIDTH_DP.dp
                )
                
                // Delete column header - fixed width
                Box(
                    modifier = Modifier.width(COL_ACTION_WIDTH_DP.dp)
                )
            }
        ) {
            cloudImages.forEach { file ->
                TableRow(
                    modifier = Modifier.clickable(enabled = !isDeleting) {
                        onViewImage(file.path)
                    }
                ) {
                    // Name cell - calculated width
                    Box(modifier = Modifier.width(nameColumnWidth)) {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Date cell - fixed width
                    TableCell(
                        text = formatTimestamp(file.updatedTimeMillis),
                        width = COL_DATE_WIDTH_DP.dp
                    )

                    // Delete action cell - fixed width
                    Box(
                        modifier = Modifier.width(COL_ACTION_WIDTH_DP.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { onRequestDelete(file) },
                            enabled = !isDeleting,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.upload_delete_cloud_image),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format timestamp to a readable date string
 */
@Suppress("DEPRECATION")
private fun formatTimestamp(timestampMillis: Long): String {
    if (timestampMillis == 0L) return "-"
    return try {
        val instant = Instant.fromEpochMilliseconds(timestampMillis)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${localDateTime.monthNumber.toString().padStart(2, '0')}"
    } catch (_: Exception) {
        "-"
    }
}

@Composable
private fun GuidelineRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = stringResource(Res.string.symbol_bullet),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UploadErrorContent(
    message: String,
    windowSizeClass: WindowSizeClass,
    headerMonthName: String,
    headerYear: Int,
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
        selectedMonth = headerMonthName,
        selectedYear = headerYear,
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
                contentDescription = stringResource(Res.string.cd_full_screen_image),
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
                        contentDescription = stringResource(Res.string.cd_close),
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
                        contentDescription = stringResource(Res.string.cd_zoom_in),
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
                        contentDescription = stringResource(Res.string.cd_zoom_out),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


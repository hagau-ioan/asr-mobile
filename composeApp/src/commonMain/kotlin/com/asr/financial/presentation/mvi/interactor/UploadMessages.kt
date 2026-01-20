package com.asr.financial.presentation.mvi.interactor

/**
 * Message keys for Upload screen.
 * These correspond to string resources.
 */
internal object UploadMessages {
    const val SUCCESS_CAPTURED = "upload_success_captured"
    const val SUCCESS_DELETED = "upload_success_deleted"
    const val SUCCESS_SENT = "upload_success_sent"
    const val ERROR_UNKNOWN = "upload_error_unknown"
    const val ERROR_CAPTURE = "upload_error_capture"
    const val ERROR_PROCESSING = "upload_error_processing"
    const val ERROR_DELETE = "upload_error_delete"
    const val ERROR_SEND = "upload_error_send"
    const val ERROR_UPLOAD_CANCELLED = "upload_error_upload_cancelled"
    // Cloud images messages
    const val SUCCESS_CLOUD_IMAGE_DELETED = "upload_success_cloud_deleted"
    const val ERROR_LOAD_CLOUD_IMAGES = "upload_error_load_cloud"
    const val ERROR_DELETE_CLOUD_IMAGE = "upload_error_delete_cloud"
    const val ERROR_DOWNLOAD_PREVIEW = "upload_error_download_preview"
}

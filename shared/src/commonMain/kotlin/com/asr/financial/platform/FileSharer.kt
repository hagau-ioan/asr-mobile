package com.asr.financial.platform

/**
 * Platform-specific file sharing
 */
expect class FileSharer {
    /**
     * Share table as PDF
     * @param title Table title
     * @param headers Column headers
     * @param rows Table rows
     * @param fileName Suggested file name
     */
    suspend fun shareTableAsPdf(
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        fileName: String
    ): Boolean
}

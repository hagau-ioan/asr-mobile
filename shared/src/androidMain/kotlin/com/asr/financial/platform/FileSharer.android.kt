package com.asr.financial.platform

import android.content.Context

/**
 * Android implementation of FileSharer
 */
actual class FileSharer(private val context: Context) {
    private val pdfGenerator = PdfGenerator(context)
    
    actual suspend fun shareTableAsPdf(
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        fileName: String
    ): Boolean {
        return pdfGenerator.shareTableAsPdf(title, headers, rows, fileName)
    }
}

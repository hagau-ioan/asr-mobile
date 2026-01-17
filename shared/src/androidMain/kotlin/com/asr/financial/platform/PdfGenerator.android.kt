package com.asr.financial.platform

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Android PDF generator using native Android PDF API
 */
class PdfGenerator(private val context: Context) {
    
    suspend fun shareTableAsPdf(
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        fileName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            val paint = Paint().apply {
                textSize = 12f
                isAntiAlias = true
            }
            
            val boldPaint = Paint().apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            
            val startX = 50f
            val startY = 80f
            val colWidth = 130f
            val rowHeight = 25f
            
            // Title
            canvas.drawText(title, startX, 50f, boldPaint)
            
            // Draw table
            val numCols = headers.size
            val numRows = rows.size + 1 // +1 for header
            
            // Draw grid
            for (i in 0..numRows) {
                val y = startY + (i * rowHeight)
                canvas.drawLine(startX, y, startX + (numCols * colWidth), y, borderPaint)
            }
            
            for (i in 0..numCols) {
                val x = startX + (i * colWidth)
                canvas.drawLine(x, startY, x, startY + (numRows * rowHeight), borderPaint)
            }
            
            // Headers
            headers.forEachIndexed { index, header ->
                canvas.drawText(header, startX + (index * colWidth) + 5f, startY + 17f, boldPaint)
            }
            
            // Rows
            rows.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, cell ->
                    canvas.drawText(
                        cell,
                        startX + (colIndex * colWidth) + 5f,
                        startY + ((rowIndex + 2) * rowHeight) - 8f,
                        paint
                    )
                }
            }
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(intent, "Partajează PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

package com.asr.financial.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.UIKit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.*

/**
 * iOS implementation of FileSharer
 */
@OptIn(ExperimentalForeignApi::class)
actual class FileSharer {
    actual suspend fun shareTableAsPdf(
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        fileName: String
    ): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                // Use Documents directory instead of tmp
                val documentsPath = NSSearchPathForDirectoriesInDomains(
                    NSDocumentDirectory,
                    NSUserDomainMask,
                    true
                ).firstOrNull() as? String ?: NSTemporaryDirectory()
                
                val filePath = "$documentsPath/$fileName"
                
                // Generate PDF
                generatePdf(filePath, title, headers, rows)
                
                // Verify file exists
                val fileManager = NSFileManager.defaultManager
                val fileUrl = NSURL.fileURLWithPath(filePath)
                
                if (!fileManager.fileExistsAtPath(filePath)) {
                    return@withContext false
                }
                
                // Get root view controller
                val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                
                if (rootViewController != null) {
                    // Create activity view controller (share sheet)
                    val activityViewController = UIActivityViewController(
                        activityItems = listOf(fileUrl),
                        applicationActivities = null
                    )
                    
                    // Present share sheet
                    rootViewController.presentViewController(
                        activityViewController,
                        animated = true,
                        completion = null
                    )
                    
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    private fun generatePdf(
        path: String,
        title: String,
        headers: List<String>,
        rows: List<List<String>>
    ) {
        // Create PDF context
        UIGraphicsBeginPDFContextToFile(path, CGRectMake(0.0, 0.0, 595.0, 842.0), null)
        UIGraphicsBeginPDFPage()
        
        val context = UIGraphicsGetCurrentContext()
        
        // Draw title
        val titleAttributes = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(16.0)
        )
        val titleString = title as NSString
        titleString.drawAtPoint(
            CGPointMake(50.0, 50.0),
            withAttributes = titleAttributes
        )
        
        // Draw table (simplified - just text)
        var y = 100.0
        val textAttributes = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.systemFontOfSize(12.0)
        )
        
        // Headers
        var x = 50.0
        headers.forEach { header ->
            (header as NSString).drawAtPoint(
                CGPointMake(x, y),
                withAttributes = textAttributes
            )
            x += 130.0
        }
        y += 25.0
        
        // Rows
        rows.forEach { row ->
            x = 50.0
            row.forEach { cell ->
                (cell as NSString).drawAtPoint(
                    CGPointMake(x, y),
                    withAttributes = textAttributes
                )
                x += 130.0
            }
            y += 20.0
        }
        
        UIGraphicsEndPDFContext()
    }
}

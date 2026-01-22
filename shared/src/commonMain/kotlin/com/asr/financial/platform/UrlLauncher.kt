package com.asr.financial.platform

/**
 * Platform abstraction for opening external URLs.
 * Android: Uses Intent with ACTION_VIEW
 * iOS: Uses UIApplication.shared.open()
 */
expect class UrlLauncher {
    /**
     * Open an external URL in the default browser or appropriate app.
     * @param url The URL to open (must be a valid HTTP/HTTPS URL)
     * @return true if the URL was opened successfully, false otherwise
     */
    suspend fun openUrl(url: String): Boolean
}

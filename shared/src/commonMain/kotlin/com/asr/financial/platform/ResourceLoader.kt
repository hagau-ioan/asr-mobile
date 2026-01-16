package com.asr.financial.platform

/**
 * Platform-specific resource loader for JSON files.
 * Actual implementations are provided by the presentation layer (composeApp).
 */
interface ResourceLoader {
    /**
     * Load a resource file as a string
     * @param fileName Name of the file (e.g., "transactions.json")
     * @return File content as string, or null if loading fails
     */
    suspend fun loadResourceAsString(fileName: String): String?
}

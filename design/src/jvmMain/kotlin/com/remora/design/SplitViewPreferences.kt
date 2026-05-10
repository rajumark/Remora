package com.remora.design

import androidx.compose.runtime.*
import java.io.File
import java.io.IOException

/**
 * Cross-platform file-based preferences manager for split view position
 */
object SplitViewPreferences {
    private const val DEFAULT_RATIO = 0.4f
    private const val PREFS_FILE_NAME = "remora_preferences.txt"
    private const val SPLIT_RATIO_KEY = "split_ratio"
    
    /**
     * Get the app support directory for storing preferences
     */
    private fun getAppSupportDirectory(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        val appName = "Remora"
        
        val baseDir = when {
            os.contains("win") -> {
                val localAppData = System.getenv("LOCALAPPDATA")
                if (localAppData != null) File(localAppData) else File(userHome, "AppData/Local")
            }
            os.contains("mac") -> {
                File(userHome, "Library/Application Support")
            }
            else -> { // Linux/Unix
                val xdgDataHome = System.getenv("XDG_DATA_HOME")
                if (xdgDataHome != null) File(xdgDataHome) else File(userHome, ".local/share")
            }
        }
        
        return File(baseDir, appName).apply { if (!exists()) mkdirs() }
    }
    
    /**
     * Get the stored split ratio, or default if not found
     */
    fun getStoredRatio(): Float {
        return try {
            val prefsFile = File(getAppSupportDirectory(), PREFS_FILE_NAME)
            if (!prefsFile.exists()) {
                return DEFAULT_RATIO
            }
            
            val lines = prefsFile.readLines()
            val ratioLine = lines.find { it.startsWith("$SPLIT_RATIO_KEY=") }
            ratioLine?.substringAfter("=")?.toFloatOrNull() ?: DEFAULT_RATIO
        } catch (e: Exception) {
            println("Failed to read split ratio: ${e.message}")
            DEFAULT_RATIO
        }
    }
    
    /**
     * Store the split ratio to file
     */
    fun storeRatio(ratio: Float) {
        try {
            val appDir = getAppSupportDirectory()
            val prefsFile = File(appDir, PREFS_FILE_NAME)
            
            // Read existing preferences
            val existingLines = if (prefsFile.exists()) {
                prefsFile.readLines().toMutableList()
            } else {
                mutableListOf()
            }
            
            // Remove existing split ratio entry if present
            existingLines.removeAll { it.startsWith("$SPLIT_RATIO_KEY=") }
            
            // Add new split ratio entry
            existingLines.add("$SPLIT_RATIO_KEY=$ratio")
            
            // Write all preferences back to file
            prefsFile.writeText(existingLines.joinToString("\n"))
        } catch (e: Exception) {
            println("Failed to store split ratio: ${e.message}")
        }
    }
}

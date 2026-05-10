package com.remora.apps

import androidx.compose.runtime.*

/**
 * Shared state manager for apps module
 */
object AppsStateManager {
    /**
     * Currently selected package name
     */
    var selectedPackage by mutableStateOf<String?>(null)
        private set
    
    /**
     * Update the selected package
     */
    fun selectPackage(packageName: String?) {
        selectedPackage = packageName
    }
    
    /**
     * Clear the selected package
     */
    fun clearSelection() {
        selectedPackage = null
    }
}

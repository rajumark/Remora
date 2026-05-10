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
     * Current package filter option
     */
    var currentFilter by mutableStateOf(PackageFilterOption.ALL_APPS)
        private set
    
    /**
     * Whether AppsRightPage should be visible
     */
    var isRightPageVisible by mutableStateOf(false)
        private set
    
    /**
     * Update the selected package
     */
    fun selectPackage(packageName: String?) {
        selectedPackage = packageName
        if (packageName != null) {
            isRightPageVisible = true
        }
    }
    
    /**
     * Clear the selected package
     */
    fun clearSelection() {
        selectedPackage = null
    }
    
    /**
     * Update the package filter
     */
    fun setFilter(filter: PackageFilterOption) {
        currentFilter = filter
    }
    
    /**
     * Set right page visibility
     */
    fun toggleRightPageVisibility(visible: Boolean) {
        isRightPageVisible = visible
    }
}

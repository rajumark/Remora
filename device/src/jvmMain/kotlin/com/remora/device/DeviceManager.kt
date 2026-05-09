package com.remora.device

import com.remora.adb.AdbManager
import androidx.compose.runtime.*

/**
 * Manages device-related state and higher-level device logic.
 */
object DeviceManager {
    /**
     * Observable state for the list of connected devices
     */
    var connectedDevices by mutableStateOf(emptyList<String>())
        private set

    /**
     * Observable state for the currently selected device
     */
    var selectedDevice by mutableStateOf<String?>(null)

    /**
     * Update the list of devices and handle auto-selection
     */
    suspend fun refreshDevices() {
        AdbManager.getDevices().onSuccess { devices ->
            if (connectedDevices != devices) {
                connectedDevices = devices
                // Auto-selection logic
                if (selectedDevice == null || !devices.contains(selectedDevice)) {
                    selectedDevice = devices.firstOrNull()
                }
            }
        }.onFailure {
            println("Failed to fetch devices: ${it.message}")
        }
    }
}

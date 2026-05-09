package com.remora.device

import com.remora.adb.AdbManager
import com.remora.adb.Device
import androidx.compose.runtime.*

/**
 * Manages device-related state and higher-level device logic.
 */
object DeviceManager {
    /**
     * Observable state for the list of connected devices
     */
    var connectedDevices by mutableStateOf(emptyList<Device>())
        private set

    /**
     * Observable state for the currently selected device
     */
    var selectedDevice by mutableStateOf<Device?>(null)

    /**
     * Update the list of devices and handle auto-selection
     */
    suspend fun refreshDevices() {
        AdbManager.getDevices().onSuccess { devices ->
            if (connectedDevices != devices) {
                connectedDevices = devices
                // Auto-selection logic
                if (selectedDevice == null || !devices.any { it.serial == selectedDevice?.serial }) {
                    selectedDevice = devices.firstOrNull()
                } else {
                    // Update the selected device with fresh info if it still exists
                    selectedDevice = devices.find { it.serial == selectedDevice?.serial }
                }
            }
        }.onFailure {
            println("Failed to fetch devices: ${it.message}")
        }
    }
}

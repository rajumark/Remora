package com.remora.adb

/**
 * Represents a connected Android device
 * @param serial The unique serial number of the device
 * @param osVersion The Android version (e.g., "13", "14")
 * @param model The model name of the device (optional)
 */
data class Device(
    val serial: String,
    val osVersion: String = "Unknown",
    val model: String = "Unknown Device"
)

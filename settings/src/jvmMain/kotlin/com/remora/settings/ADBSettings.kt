package com.remora.settings

import com.remora.adb.AdbManager

object ADBSettings {
    fun getSettingsList(): List<SettingsBox> {
        try {
            val pinList = SettingsPinDatabase.getPinList()
            return listSettings.map {
                SettingsBox(it, pinList.contains(it))
            }.sortedByDescending { it.isPined }
        } catch (e: Exception) {
            return emptyList()
        }
    }

    suspend fun openSettingByName(serial: String, intent: String): Result<Unit> = runCatching {
        val adbPath = AdbManager.initializeAdb().getOrThrow()
        val adbExecutable = if (AdbManager.getCurrentPlatform() == "windows") {
            java.io.File(adbPath, "platform-tools/adb.exe")
        } else {
            java.io.File(adbPath, "platform-tools/adb")
        }

        val process = ProcessBuilder(
            adbExecutable.absolutePath,
            "-s",
            serial,
            "shell",
            "am",
            "start",
            "-a",
            intent
        ).start()
        process.waitFor()
    }
}

data class SettingsBox(
    val intent: String,
    val isPined: Boolean
)

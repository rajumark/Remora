package com.remora.adb

import java.io.*
import java.util.zip.ZipInputStream
import androidx.compose.runtime.*

/**
 * Manages ADB (Android Debug Bridge) functionality including extraction and command execution
 */
object AdbManager {
    
    private var isInitialized = false
    
    /**
     * Observable state for the current ADB path
     */
    var adbPath by mutableStateOf<String?>(null)
        private set

    /**
     * Get the platform-specific application support directory
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
     * Initialize ADB by extracting the appropriate platform tools to a persistent directory
     */
    suspend fun initializeAdb(): Result<String> = runCatching {
        if (isInitialized && adbPath != null) {
            return@runCatching adbPath!!
        }
        
        val platform = getCurrentPlatform()
        val appDir = getAppSupportDirectory()
        val binDir = File(appDir, "bin")
        val adbExecutableName = if (platform == "windows") "adb.exe" else "adb"
        val adbFile = File(binDir, "platform-tools/$adbExecutableName")
        
        // If ADB already exists, just return the path
        if (adbFile.exists()) {
            val path = binDir.absolutePath
            adbPath = path
            isInitialized = true
            return@runCatching path
        }
        
        // Ensure bin directory exists
        binDir.mkdirs()
        
        val resourceName = "/adb/platform-tools-latest-$platform.zip"
        
        // Get resource stream
        val resourceStream = this::class.java.getResourceAsStream(resourceName)
            ?: throw IllegalStateException("ADB tools not found for platform: $platform")
        
        // Extract zip file to persistent directory
        resourceStream.use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val file = File(binDir, entry.name)
                        file.parentFile.mkdirs()
                        
                        FileOutputStream(file).use { output ->
                            zipInputStream.copyTo(output)
                        }
                        
                        // Make executable files executable on Unix systems
                        if (platform != "windows" && (entry.name.endsWith("adb") || entry.name.endsWith("fastboot"))) {
                            file.setExecutable(true, false)
                        }
                    }
                    entry = zipInputStream.nextEntry
                }
            }
        }
        
        val path = binDir.absolutePath
        adbPath = path
        isInitialized = true
        path
    }
    
    /**
     * Execute ADB version command
     */
    suspend fun getAdbVersion(): Result<String> = runCatching {
        val adbPath = initializeAdb().getOrThrow()
        val adbExecutable = if (getCurrentPlatform() == "windows") {
            File(adbPath, "platform-tools/adb.exe")
        } else {
            File(adbPath, "platform-tools/adb")
        }
        
        if (!adbExecutable.exists()) {
            throw IllegalStateException("ADB executable not found at: ${adbExecutable.absolutePath}")
        }
        
        val process = ProcessBuilder(adbExecutable.absolutePath, "version").start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val error = process.errorStream.bufferedReader().use { it.readText() }
        
        val exitCode = process.waitFor()
        
        if (exitCode != 0) {
            throw RuntimeException("ADB command failed with exit code $exitCode. Error: $error")
        }
        
        output.trim()
    }
    
    /**
     * Get current platform name
     */
    fun getCurrentPlatform(): String {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> "windows"
            os.contains("mac") -> "darwin"
            os.contains("nix") || os.contains("nux") || os.contains("aix") -> "linux"
            else -> throw UnsupportedOperationException("Unsupported platform: $os")
        }
    }
    
    /**
     * Clean up extracted ADB files (Used only if full reset is needed)
     */
    fun cleanup() {
        adbPath?.let { path ->
            try {
                File(path).deleteRecursively()
            } catch (e: Exception) {
                println("Warning: Failed to cleanup ADB files: ${e.message}")
            }
        }
        adbPath = null
        isInitialized = false
    }

    /**
     * Get a property from a specific device
     */
    suspend fun getDeviceProperty(serial: String, property: String): String = runCatching {
        val adbPath = initializeAdb().getOrThrow()
        val adbExecutable = if (getCurrentPlatform() == "windows") {
            File(adbPath, "platform-tools/adb.exe")
        } else {
            File(adbPath, "platform-tools/adb")
        }
        
        val process = ProcessBuilder(adbExecutable.absolutePath, "-s", serial, "shell", "getprop", property).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        process.waitFor()
        output
    }.getOrDefault("Unknown")

    /**
     * Get list of connected devices using ADB
     */
    suspend fun getDevices(): Result<List<Device>> = runCatching {
        val adbPath = initializeAdb().getOrThrow()
        val adbExecutable = if (getCurrentPlatform() == "windows") {
            File(adbPath, "platform-tools/adb.exe")
        } else {
            File(adbPath, "platform-tools/adb")
        }
        
        if (!adbExecutable.exists()) {
            throw IllegalStateException("ADB executable not found at: ${adbExecutable.absolutePath}")
        }
        
        val process = ProcessBuilder(adbExecutable.absolutePath, "devices").start()
        val output = process.inputStream.bufferedReader().use { it.readLines() }
        val exitCode = process.waitFor()
        
        if (exitCode != 0) {
            val error = process.errorStream.bufferedReader().use { it.readText() }
            throw RuntimeException("ADB devices command failed with exit code $exitCode. Error: $error")
        }
        
        val serials = output.drop(1) // Drop "List of devices attached"
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.contains("\t") }
            .map { it.split("\t")[0] }

        serials.map { serial ->
            val osVersion = getDeviceProperty(serial, "ro.build.version.release")
            val apiLevel = getDeviceProperty(serial, "ro.build.version.sdk")
            val model = getDeviceProperty(serial, "ro.product.model")
            val isEmulator = serial.startsWith("emulator-") || 
                            getDeviceProperty(serial, "ro.kernel.qemu") == "1" ||
                            model.contains("sdk_gphone", ignoreCase = true)
            
            Device(serial, osVersion, apiLevel, model, isEmulator)
        }
    }

    /**
     * Get list of installed packages from a device using pm list command
     */
    suspend fun getInstalledPackages(serial: String, filter: String = ""): Result<List<String>> = runCatching {
        val adbPath = initializeAdb().getOrThrow()
        val adbExecutable = if (getCurrentPlatform() == "windows") {
            File(adbPath, "platform-tools/adb.exe")
        } else {
            File(adbPath, "platform-tools/adb")
        }
        
        if (!adbExecutable.exists()) {
            throw IllegalStateException("ADB executable not found at: ${adbExecutable.absolutePath}")
        }
        
        val command = if (filter.isEmpty()) {
            listOf("shell", "pm", "list", "packages")
        } else {
            listOf("shell", "pm", "list", "packages", filter)
        }
        
        val process = ProcessBuilder(adbExecutable.absolutePath, "-s", serial, *command.toTypedArray()).start()
        val output = process.inputStream.bufferedReader().use { it.readLines() }
        val exitCode = process.waitFor()
        
        if (exitCode != 0) {
            val error = process.errorStream.bufferedReader().use { it.readText() }
            throw RuntimeException("PM list command failed with exit code $exitCode. Error: $error")
        }
        
        output.map { it.trim() }
            .filter { it.startsWith("package:") }
            .map { it.substringAfter("package:") }
    }
}

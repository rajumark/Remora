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
     * Get basic info for a specific package using dumpsys package command
     */
    suspend fun getPackageBasicInfo(serial: String, packageName: String): Map<String, String> = runCatching {
        val adbPath = initializeAdb().getOrThrow()
        val adbExecutable = if (getCurrentPlatform() == "windows") {
            File(adbPath, "platform-tools/adb.exe")
        } else {
            File(adbPath, "platform-tools/adb")
        }

        val process = ProcessBuilder(adbExecutable.absolutePath, "-s", serial, "shell", "dumpsys", "package", packageName).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor()

        val result = mutableMapOf<String, String>()

        output.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("versionName=") && !trimmed.startsWith("baseVersionName=") -> {
                    val value = trimmed.substringAfter("versionName=", "Unknown")
                    if (value.isNotEmpty() && value != "Unknown") result["versionName"] = value
                }
                trimmed.startsWith("versionCode=") && trimmed.contains("minSdk=") -> {
                    val value = trimmed.substringAfter("versionCode=", "").substringBefore(" ").trim()
                    if (value.isNotEmpty()) result["versionCode"] = value
                }
                trimmed.startsWith("firstInstallTime=") -> {
                    val value = trimmed.substringAfter("firstInstallTime=", "Unknown")
                    if (value.isNotEmpty() && value != "Unknown") result["firstInstallTime"] = value
                }
                trimmed.startsWith("lastUpdateTime=") -> {
                    val value = trimmed.substringAfter("lastUpdateTime=", "Unknown")
                    if (value.isNotEmpty() && value != "Unknown") result["lastUpdateTime"] = value
                }
            }
        }

        result
    }.getOrDefault(emptyMap())

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

    private fun getAdbExecutable(): File {
        val adbPath = adbPath ?: throw IllegalStateException("ADB not initialized")
        val exeName = if (getCurrentPlatform() == "windows") "platform-tools/adb.exe" else "platform-tools/adb"
        return File(adbPath, exeName)
    }

    fun startApp(serial: String, packageName: String): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1").start()
        process.waitFor()
    }

    fun forceStopApp(serial: String, packageName: String): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "am", "force-stop", packageName).start()
        process.waitFor()
    }

    fun restartApp(serial: String, packageName: String): Result<Unit> = runCatching {
        forceStopApp(serial, packageName).getOrThrow()
        startApp(serial, packageName).getOrThrow()
    }

    fun clearAppData(serial: String, packageName: String): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "pm", "clear", packageName).start()
        process.waitFor()
    }

    fun enableDisableApp(serial: String, packageName: String, makeEnable: Boolean): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        val command = if (makeEnable) {
            listOf(adb.absolutePath, "-s", serial, "shell", "pm", "enable", packageName)
        } else {
            listOf(adb.absolutePath, "-s", serial, "shell", "pm", "disable-user", packageName)
        }
        val process = ProcessBuilder(command).start()
        process.waitFor()
    }

    fun uninstallApp(serial: String, packageName: String): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "uninstall", packageName).start()
        process.waitFor()
    }

    fun openAppSettings(serial: String, packageName: String): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "am", "start", "-a", "android.settings.APPLICATION_DETAILS_SETTINGS", "-d", "package:$packageName").start()
        process.waitFor()
    }

    fun pressHome(serial: String): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "input", "keyevent", "KEYCODE_HOME").start()
        process.waitFor()
    }

    fun openUrl(serial: String, url: String): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "am", "start", "-a", "android.intent.action.VIEW", "-d", url).start()
        process.waitFor()
    }

    fun downloadApk(serial: String, packageName: String, outputDir: File): Result<Unit> = runCatching {
        val adb = getAdbExecutable()

        // Get APK path(s)
        val pathProcess = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "pm", "path", packageName).start()
        val paths = pathProcess.inputStream.bufferedReader().use { it.readLines() }
            .map { it.trim() }
            .filter { it.startsWith("package:") }
            .mapNotNull { it.split(":").getOrNull(1)?.trim() }
        pathProcess.waitFor()

        if (paths.isEmpty()) throw IllegalStateException("No APK path found for $packageName")

        paths.forEach { apkPath ->
            val pullProcess = ProcessBuilder(adb.absolutePath, "-s", serial, "pull", apkPath, outputDir.absolutePath).start()
            pullProcess.waitFor()
        }
    }

    /**
     * Get all permissions for a package using dumpsys package command.
     * Returns a map with keys: requested_permissions, install_permissions, runtime_permissions
     */
    suspend fun getAllRuntimePermissions(serial: String, packageName: String): Map<String, List<PermissionInfo>> = runCatching {
        val adb = getAdbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "dumpsys", "package", packageName).start()
        val reader = process.inputStream.bufferedReader()
        val permissionsMap = mutableMapOf<String, MutableList<PermissionInfo>>()
        var currentGroup: String? = null

        reader.useLines { lines ->
            lines.forEach { line ->
                when {
                    line.contains("requested permissions:") -> currentGroup = "requested_permissions"
                    line.contains("install permissions:") -> currentGroup = "install_permissions"
                    line.contains("runtime permissions:") -> currentGroup = "runtime_permissions"
                    line.isBlank() -> currentGroup = null
                    currentGroup != null -> {
                        val permission = line.extractPermission() ?: return@forEach
                        val granted = when (currentGroup) {
                            "runtime_permissions", "install_permissions" -> line.contains("granted=true")
                            else -> false
                        }
                        permissionsMap.getOrPut(currentGroup!!) { mutableListOf() }.add(PermissionInfo(permission, granted))
                    }
                }
            }
        }

        process.waitFor()
        permissionsMap
    }.getOrDefault(emptyMap())

    /**
     * Grant permissions to a package
     */
    fun grantPermissions(serial: String, packageName: String, permissions: List<String>): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        permissions.forEach { permission ->
            val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "pm", "grant", packageName, permission).start()
            process.waitFor()
        }
    }

    /**
     * Revoke permissions from a package
     */
    fun revokePermissions(serial: String, packageName: String, permissions: List<String>): Result<Unit> = runCatching {
        val adb = getAdbExecutable()
        permissions.forEach { permission ->
            val process = ProcessBuilder(adb.absolutePath, "-s", serial, "shell", "pm", "revoke", packageName, permission).start()
            process.waitFor()
        }
    }

    /**
     * Extract permission name from a line of dumpsys output
     */
    private fun String.extractPermission(): String? {
        val trimmed = trim()
        // Lines typically look like: "  android.permission.CAMERA: granted=true"
        // or: "  android.permission.CAMERA" for requested permissions
        return when {
            trimmed.startsWith("android.permission.") -> {
                trimmed.substringBefore(":").trim()
            }
            trimmed.startsWith("com.") || trimmed.startsWith("org.") -> {
                // Some custom permissions
                trimmed.substringBefore(":").trim()
            }
            else -> null
        }
    }
}

/**
 * Data class representing a permission with its grant status
 */
data class PermissionInfo(
    val permission: String,
    val granted: Boolean
)

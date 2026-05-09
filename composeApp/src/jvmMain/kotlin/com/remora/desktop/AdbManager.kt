package com.remora.desktop

import java.io.*
import java.util.zip.ZipInputStream
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

/**
 * Manages ADB (Android Debug Bridge) functionality including extraction and command execution
 */
object AdbManager {
    
    private var extractedAdbPath: String? = null
    private var isInitialized = false
    
    /**
     * Initialize ADB by extracting the appropriate platform tools
     */
    suspend fun initializeAdb(): Result<String> = runCatching {
        if (isInitialized && extractedAdbPath != null) {
            return@runCatching extractedAdbPath!!
        }
        
        val platform = getCurrentPlatform()
        val resourceName = "/adb/platform-tools-latest-$platform.zip"
        
        // Get resource stream
        val resourceStream = this::class.java.getResourceAsStream(resourceName)
            ?: throw IllegalStateException("ADB tools not found for platform: $platform")
        
        // Create temporary directory for ADB tools
        val tempDir = createTempDirectory("adb-platform-tools")
        extractedAdbPath = tempDir.toString()
        
        // Extract zip file
        resourceStream.use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val filePath = tempDir.resolve(entry.name).toString()
                        val file = File(filePath)
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
        
        isInitialized = true
        extractedAdbPath!!
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
     * Clean up extracted ADB files
     */
    fun cleanup() {
        extractedAdbPath?.let { path ->
            try {
                File(path).deleteRecursively()
            } catch (e: Exception) {
                println("Warning: Failed to cleanup ADB files: ${e.message}")
            }
        }
        extractedAdbPath = null
        isInitialized = false
    }
}

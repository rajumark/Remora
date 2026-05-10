package com.remora.apps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remora.adb.AdbManager
import com.remora.device.DeviceManager
import org.koin.compose.koinInject
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class BasicInfoItem(
    val label: String,
    val value: String
)

@Composable
fun AppsRightBasicPage() {
    val adbManager = koinInject<AdbManager>()
    val deviceManager = koinInject<DeviceManager>()
    val selectedPackage = AppsStateManager.selectedPackage

    var infoItems by remember { mutableStateOf(listOf<BasicInfoItem>()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPackage, deviceManager.selectedDevice) {
        val pkg = selectedPackage
        val device = deviceManager.selectedDevice
        if (pkg != null && device != null) {
            isLoading = true
            val info = adbManager.getPackageBasicInfo(device.serial, pkg)
            val items = mutableListOf<BasicInfoItem>()

            info["versionName"]?.let { items.add(BasicInfoItem("Version Name", it)) }
            info["versionCode"]?.let { items.add(BasicInfoItem("Version Code", it)) }
            info["firstInstallTime"]?.let { items.add(BasicInfoItem("Install Time", formatTimestamp(it))) }
            info["lastUpdateTime"]?.let { items.add(BasicInfoItem("Last Update Time", formatTimestamp(it))) }

            infoItems = items
            isLoading = false
        } else {
            infoItems = emptyList()
        }
    }

    fun getDeviceAndPkg(): Pair<String, String>? {
        val device = deviceManager.selectedDevice ?: return null
        val pkg = selectedPackage ?: return null
        return device.serial to pkg
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            selectedPackage == null -> {
                Text(
                    text = "No package selected",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            infoItems.isEmpty() -> {
                Text(
                    text = "No basic info available",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    infoItems.forEachIndexed { index, item ->
                        InfoRow(item)
                        if (index < infoItems.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row 1: Core app actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionButton("Open") {
                            getDeviceAndPkg()?.let { (s, p) -> adbManager.startApp(s, p) }
                        }
                        ActionButton("Force Stop") {
                            getDeviceAndPkg()?.let { (s, p) -> adbManager.forceStopApp(s, p) }
                        }
                        ActionButton("Restart") {
                            getDeviceAndPkg()?.let { (s, p) -> adbManager.restartApp(s, p) }
                        }
                        ActionButton("Uninstall") {
                            getDeviceAndPkg()?.let { (s, p) -> adbManager.uninstallApp(s, p) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Data & state actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionButton("Clear Data") {
                            getDeviceAndPkg()?.let { (s, p) -> adbManager.clearAppData(s, p) }
                        }
                        ActionButton("Enable") {
                            getDeviceAndPkg()?.let { (s, p) -> adbManager.enableDisableApp(s, p, true) }
                        }
                        ActionButton("Disable") {
                            getDeviceAndPkg()?.let { (s, p) -> adbManager.enableDisableApp(s, p, false) }
                        }
                        ActionButton("Home") {
                            deviceManager.selectedDevice?.let { adbManager.pressHome(it.serial) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: Info & external actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionButton("Copy") {
                            selectedPackage?.let {
                                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(it), null)
                            }
                        }
                        ActionButton("App Info") {
                            getDeviceAndPkg()?.let { (s, p) -> adbManager.openAppSettings(s, p) }
                        }
                        ActionButton("Play Store") {
                            getDeviceAndPkg()?.let { (s, p) ->
                                adbManager.openUrl(s, "https://play.google.com/store/apps/details?id=$p")
                            }
                        }
                        ActionButton("Desktop") {
                            selectedPackage?.let {
                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().browse(URI("https://play.google.com/store/apps/details?id=$it"))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 4: Download & find
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionButton("Find Online") {
                            selectedPackage?.let {
                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().browse(URI("https://www.google.com/search?q=download+$it+APK"))
                                }
                            }
                        }
                        ActionButton("Download APK") {
                            getDeviceAndPkg()?.let { (s, p) ->
                                val downloadDir = File(System.getProperty("user.home"), "Downloads/$p").apply { mkdirs() }
                                adbManager.downloadApk(s, p, downloadDir)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Text(text)
    }
}

@Composable
private fun InfoRow(item: BasicInfoItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = item.value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTimestamp(value: String): String {
    return try {
        val numeric = value.toLong()
        val date = Date(if (numeric > 1000000000000) numeric else numeric * 1000)
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
    } catch (_: NumberFormatException) {
        value
    }
}

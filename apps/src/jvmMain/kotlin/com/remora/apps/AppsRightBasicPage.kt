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

            info["versionName"]?.let {
                items.add(BasicInfoItem("Version Name", it))
            }
            info["versionCode"]?.let {
                items.add(BasicInfoItem("Version Code", it))
            }
            info["firstInstallTime"]?.let {
                items.add(BasicInfoItem("Install Time", formatTimestamp(it)))
            }
            info["lastUpdateTime"]?.let {
                items.add(BasicInfoItem("Last Update Time", formatTimestamp(it)))
            }

            infoItems = items
            isLoading = false
        } else {
            infoItems = emptyList()
        }
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlineButtonWithText("Open")
                        OutlineButtonWithText("Stop")
                        OutlineButtonWithText("Restart")
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlineButtonWithText(text: String) {
    OutlinedButton(onClick = {}) {
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

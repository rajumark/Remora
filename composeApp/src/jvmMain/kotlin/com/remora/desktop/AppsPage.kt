package com.remora.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import com.remora.adb.AdbManager
import com.remora.device.DeviceManager
import com.remora.device.DeviceResources

@Composable
fun AppsPage() {
    val adbPath = AdbManager.adbPath
    val selectedDevice = DeviceManager.selectedDevice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Apps",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Connection Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ADB Path: ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    Text(adbPath ?: "Initializing...", style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active Device: ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    Text(selectedDevice ?: "No device selected", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (selectedDevice == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(DeviceResources.ic_mobile),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Please connect or select a device to manage apps",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            // Placeholder for actual app list logic
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Fetching apps for $selectedDevice...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

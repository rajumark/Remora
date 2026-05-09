package com.remora.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.remora.adb.AdbManager
import com.remora.device.DeviceManager
import org.koin.compose.koinInject

/**
 * A terminal interface for executing shell commands on connected devices.
 */
@Composable
fun TerminalPage(
    adbManager: AdbManager = koinInject(),
    deviceManager: DeviceManager = koinInject()
) {
    val selectedDevice = deviceManager.selectedDevice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Terminal",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Device: ${selectedDevice?.serial ?: "No device connected"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black)
                ) {
                    Text(
                        text = "$ adb shell\n# Shell ready for ${selectedDevice?.serial ?: "unknown"}",
                        color = Color.Green,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

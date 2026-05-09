package com.remora.device

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import com.remora.device.Res

import org.koin.compose.koinInject

/**
 * A self-contained UI component for selecting connected Android devices.
 * Handles its own dropdown state and interacts with [DeviceManager] for data.
 */
@Composable
fun DeviceSelector(
    modifier: Modifier = Modifier,
    deviceManager: DeviceManager = koinInject()
) {
    var showDeviceDropdown by remember { mutableStateOf(false) }
    val devices = deviceManager.connectedDevices
    val selectedDevice = deviceManager.selectedDevice

    Box(modifier = modifier) {
        TextButton(
            onClick = { if (devices.isNotEmpty()) showDeviceDropdown = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = devices.isNotEmpty()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(Res.drawable.ic_mobile),
                    contentDescription = null,
                    tint = if (devices.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedDevice ?: "No device",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (devices.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (devices.size > 1) {
                        Text(
                            text = "${devices.size} devices available",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Dropdown for selecting between multiple devices
        DropdownMenu(
            expanded = showDeviceDropdown,
            onDismissRequest = { showDeviceDropdown = false },
            modifier = Modifier.width(208.dp)
        ) {
            devices.forEach { deviceId ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = deviceId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (deviceId == selectedDevice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        deviceManager.selectedDevice = deviceId
                        showDeviceDropdown = false
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(Res.drawable.ic_mobile),
                            contentDescription = null,
                            tint = if (deviceId == selectedDevice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                )
            }
        }
    }
}

package com.remora.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import remora.composeapp.generated.resources.Res
import remora.composeapp.generated.resources.ic_dashboard
import remora.composeapp.generated.resources.ic_settings
import remora.composeapp.generated.resources.ic_profile
import remora.composeapp.generated.resources.ic_design
import remora.composeapp.generated.resources.ic_help
import remora.composeapp.generated.resources.ic_mobile

data class NavigationItem(
    val destination: String,
    val label: String,
    val iconResource: DrawableResource
)

private val navigationItems = listOf(
    NavigationItem(
        destination = "Dashboard",
        label = "Dashboard",
        iconResource = Res.drawable.ic_dashboard
    ),
    NavigationItem(
        destination = "Settings",
        label = "Settings",
        iconResource = Res.drawable.ic_settings
    ),
    NavigationItem(
        destination = "Design",
        label = "Design",
        iconResource = Res.drawable.ic_design
    ),
    NavigationItem(
        destination = "Apps",
        label = "Apps",
        iconResource = Res.drawable.ic_profile
    ),
    NavigationItem(
        destination = "Help",
        label = "Help",
        iconResource = Res.drawable.ic_help
    )
)

@Composable
fun AppNavigationDrawer(
    modifier: Modifier = Modifier,
    selectedDestination: String = "Dashboard",
    onNavigationItemClick: (String) -> Unit = {}
) {
    var devices by remember { mutableStateOf(emptyList<String>()) }
    var selectedDevice by remember { mutableStateOf<String?>(null) }
    var showDeviceDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            AdbManager.getDevices().onSuccess { newDevices ->
                // Check if device list has changed to avoid unnecessary updates and flickering
                if (devices != newDevices) {
                    devices = newDevices
                    
                    // Auto-selection logic: 
                    // If the currently selected device is no longer in the list, or if no device is selected,
                    // automatically pick the first available device.
                    if (selectedDevice == null || !newDevices.contains(selectedDevice)) {
                        selectedDevice = newDevices.firstOrNull()
                    }
                }
            }.onFailure {
                println("Failed to fetch devices: ${it.message}")
            }
            delay(2000)
        }
    }

    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Device Selection Section
        Box {
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
                            selectedDevice = deviceId
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Navigation Items
        navigationItems.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(painterResource(item.iconResource), contentDescription = null) },
                label = { Text(item.label) },
                selected = selectedDestination == item.destination,
                onClick = { onNavigationItemClick(item.destination) }
            )
        }
    }
}

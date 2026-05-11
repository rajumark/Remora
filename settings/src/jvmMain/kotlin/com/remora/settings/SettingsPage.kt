package com.remora.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.remora.adb.AdbManager
import com.remora.device.DeviceManager

@Composable
fun SettingsPage(
    adbManager: AdbManager = koinInject(),
    deviceManager: DeviceManager = koinInject()
) {
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var selectedDevice by remember { mutableStateOf(deviceManager.selectedDevice) }

    LaunchedEffect(Unit) {
        selectedDevice = deviceManager.selectedDevice
    }

    val allSettings = remember { SettingsDataList }
    val pinList = remember { mutableStateOf(SettingsPinDatabase.getPinList()) }

    val filteredSettings = remember(searchQuery, allSettings) {
        if (searchQuery.isBlank()) {
            allSettings
        } else {
            allSettings.filter { it.text.lowercase().contains(searchQuery.lowercase()) }
        }
    }

    val pinnedSettings = remember(filteredSettings, pinList.value) {
        filteredSettings.filter { pinList.value.contains(it.id) }
    }

    val otherSettings = remember(filteredSettings, pinList.value) {
        filteredSettings.filter { !pinList.value.contains(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Android Settings",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search settings") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (pinnedSettings.isNotEmpty()) {
                item {
                    Text(
                        text = "Pinned",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(pinnedSettings) { setting ->
                    SettingItemCard(
                        setting = setting,
                        device = selectedDevice,
                        onTogglePin = {
                            if (pinList.value.contains(setting.id)) {
                                SettingsPinDatabase.unPinSetting(setting.id)
                            } else {
                                SettingsPinDatabase.pinSetting(setting.id)
                            }
                            pinList.value = SettingsPinDatabase.getPinList()
                        },
                        isPinned = pinList.value.contains(setting.id)
                    )
                }
            }

            if (otherSettings.isNotEmpty()) {
                item {
                    Text(
                        text = "Other Settings",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(otherSettings) { setting ->
                    SettingItemCard(
                        setting = setting,
                        device = selectedDevice,
                        onTogglePin = {
                            if (pinList.value.contains(setting.id)) {
                                SettingsPinDatabase.unPinSetting(setting.id)
                            } else {
                                SettingsPinDatabase.pinSetting(setting.id)
                            }
                            pinList.value = SettingsPinDatabase.getPinList()
                        },
                        isPinned = pinList.value.contains(setting.id)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingItemCard(
    setting: SettingItem,
    device: com.remora.adb.Device?,
    onTogglePin: () -> Unit,
    isPinned: Boolean,
    scope: kotlinx.coroutines.CoroutineScope = rememberCoroutineScope()
) {
    Card(
        onClick = {
            device?.let {
                scope.launch {
                    ADBSettings.openSettingByName(it.serial, setting.intent)
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = setting.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onTogglePin) {
                Text(
                    text = if (isPinned) "★" else "☆",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

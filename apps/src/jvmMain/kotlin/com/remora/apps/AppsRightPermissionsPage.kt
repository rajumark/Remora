package com.remora.apps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.remora.adb.AdbManager
import com.remora.adb.PermissionInfo
import com.remora.device.DeviceManager
import com.remora.apps.AppsResources
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.interaction.MutableInteractionSource

private enum class PermissionFilter {
    RUNTIME, REQUESTED, INSTALL
}

@Composable
fun AppsRightPermissionsPage() {
    val adbManager = koinInject<AdbManager>()
    val deviceManager = koinInject<DeviceManager>()
    val selectedPackage = AppsStateManager.selectedPackage
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var permissionsMap by remember { mutableStateOf<Map<String, List<PermissionInfo>>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(PermissionFilter.RUNTIME) }

    // Helper to get device and package
    fun getDeviceAndPkg(): Pair<String, String>? {
        val device = deviceManager.selectedDevice ?: return null
        val pkg = selectedPackage ?: return null
        return device.serial to pkg
    }

    // Load permissions when package or device changes
    LaunchedEffect(selectedPackage, deviceManager.selectedDevice) {
        val pkg = selectedPackage
        val device = deviceManager.selectedDevice
        if (pkg != null && device != null) {
            isLoading = true
            permissionsMap = adbManager.getAllRuntimePermissions(device.serial, pkg)
            isLoading = false
        } else {
            permissionsMap = emptyMap()
        }
    }

    // Get current filtered list
    val currentPermissions = when (selectedFilter) {
        PermissionFilter.REQUESTED -> permissionsMap["requested_permissions"].orEmpty()
        PermissionFilter.INSTALL -> permissionsMap["install_permissions"].orEmpty()
        PermissionFilter.RUNTIME -> permissionsMap["runtime_permissions"].orEmpty()
    }

    val filteredPermissions = currentPermissions.filter {
        it.permission.contains(searchQuery, ignoreCase = true)
    }

    fun refreshPermissions() {
        val (serial, pkg) = getDeviceAndPkg() ?: return
        isLoading = true
        coroutineScope.launch {
            permissionsMap = adbManager.getAllRuntimePermissions(serial, pkg)
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            selectedPackage == null -> {
                EmptyState("No package selected")
            }
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            permissionsMap.isEmpty() -> {
                EmptyState("No permissions available")
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top action bar
                    PermissionActionBar(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onGrantAll = {
                            getDeviceAndPkg()?.let { (serial, pkg) ->
                                val perms = filteredPermissions.map { it.permission }
                                adbManager.grantPermissions(serial, pkg, perms)
                                refreshPermissions()
                            }
                        },
                        onRevokeAll = {
                            getDeviceAndPkg()?.let { (serial, pkg) ->
                                val perms = filteredPermissions.map { it.permission }
                                adbManager.revokePermissions(serial, pkg, perms)
                                refreshPermissions()
                            }
                        },
                        onRestart = {
                            getDeviceAndPkg()?.let { (serial, pkg) ->
                                adbManager.restartApp(serial, pkg)
                            }
                        },
                        onAppInfo = {
                            getDeviceAndPkg()?.let { (serial, pkg) ->
                                adbManager.openAppSettings(serial, pkg)
                            }
                        },
                        onRefresh = { refreshPermissions() }
                    )

                    // Filter chips
                    PermissionFilterRow(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it },
                        counts = mapOf(
                            PermissionFilter.REQUESTED to permissionsMap["requested_permissions"].orEmpty().size,
                            PermissionFilter.INSTALL to permissionsMap["install_permissions"].orEmpty().size,
                            PermissionFilter.RUNTIME to permissionsMap["runtime_permissions"].orEmpty().size
                        )
                    )

                    // Permission count indicator
                    Text(
                        text = "${filteredPermissions.size} permissions",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Permissions list
                    if (filteredPermissions.isEmpty() && searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No permissions found for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(filteredPermissions, key = { it.permission }) { permission ->
                                PermissionListItem(
                                    permission = permission,
                                    canToggle = selectedFilter == PermissionFilter.RUNTIME,
                                    onToggle = { granted ->
                                        getDeviceAndPkg()?.let { (serial, pkg) ->
                                            if (granted) {
                                                adbManager.grantPermissions(serial, pkg, listOf(permission.permission))
                                            } else {
                                                adbManager.revokePermissions(serial, pkg, listOf(permission.permission))
                                            }
                                            refreshPermissions()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionActionBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onGrantAll: () -> Unit,
    onRevokeAll: () -> Unit,
    onRestart: () -> Unit,
    onAppInfo: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search permissions...") },
            singleLine = true,
            shape = CircleShape,
            leadingIcon = {
                Icon(
                    painterResource(AppsResources.ic_search),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") }
                    ) {
                        Icon(
                            painterResource(AppsResources.ic_close),
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onGrantAll, modifier = Modifier.weight(1f)) {
                Text("Grant All")
            }
            OutlinedButton(onClick = onRevokeAll, modifier = Modifier.weight(1f)) {
                Text("Revoke All")
            }
            OutlinedButton(onClick = onRestart, modifier = Modifier.weight(1f)) {
                Text("Restart")
            }
            OutlinedButton(onClick = onAppInfo, modifier = Modifier.weight(1f)) {
                Text("App Info")
            }
            OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun PermissionFilterRow(
    selectedFilter: PermissionFilter,
    onFilterSelected: (PermissionFilter) -> Unit,
    counts: Map<PermissionFilter, Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PermissionFilterChip(
            label = "Runtime",
            count = counts[PermissionFilter.RUNTIME] ?: 0,
            selected = selectedFilter == PermissionFilter.RUNTIME,
            onClick = { onFilterSelected(PermissionFilter.RUNTIME) }
        )
        PermissionFilterChip(
            label = "Requested",
            count = counts[PermissionFilter.REQUESTED] ?: 0,
            selected = selectedFilter == PermissionFilter.REQUESTED,
            onClick = { onFilterSelected(PermissionFilter.REQUESTED) }
        )
        PermissionFilterChip(
            label = "Install",
            count = counts[PermissionFilter.INSTALL] ?: 0,
            selected = selectedFilter == PermissionFilter.INSTALL,
            onClick = { onFilterSelected(PermissionFilter.INSTALL) }
        )
    }
}

@Composable
private fun PermissionFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .height(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
            if (count > 0) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PermissionListItem(
    permission: PermissionInfo,
    canToggle: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = canToggle,
                onClick = { onToggle(!permission.granted) }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Checkbox (only shown for runtime permissions, read-only for others)
            if (canToggle) {
                Checkbox(
                    checked = permission.granted,
                    onCheckedChange = { onToggle(it) }
                )
            } else {
                // Read-only indicator - show a small dot for granted permissions
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (permission.granted) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {}
                    }
                }
            }

            Text(
                text = permission.permission,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        // Status text
        val statusText = when {
            !canToggle && permission.granted -> "Granted"
            !canToggle && !permission.granted -> "Not Granted"
            else -> ""
        }
        if (statusText.isNotEmpty()) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = if (permission.granted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}


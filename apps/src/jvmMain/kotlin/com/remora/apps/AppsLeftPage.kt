package com.remora.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import com.remora.adb.AdbManager
import com.remora.device.DeviceManager
import com.remora.apps.AppsResources
import com.remora.apps.Res
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.painterResource
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.awt.Desktop

@Composable
fun AppsLeftPage() {
    val deviceManager = koinInject<DeviceManager>()
    val adbManager = koinInject<AdbManager>()
    val listState = rememberLazyListState()
    
    var packages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var isScreenVisible by remember { mutableStateOf(true) }
    
    val filteredPackages = packages.filter { 
        it.contains(searchQuery.text, ignoreCase = true) 
    }
    
    // Function to refresh packages
    suspend fun refreshPackages() {
        val selectedDevice = deviceManager.selectedDevice
        if (selectedDevice != null) {
            adbManager.getInstalledPackages(
                serial = selectedDevice.serial,
                filter = AppsStateManager.currentFilter.adbFlag
            )
                .onSuccess { newPackages ->
                    // Smart diffing - only update if list changed
                    if (packages != newPackages) {
                        packages = newPackages
                        // Clear selected package if not found in new list
                        if (AppsStateManager.selectedPackage != null && 
                            AppsStateManager.selectedPackage !in newPackages) {
                            AppsStateManager.selectPackage(null)
                        }
                        println("Packages updated: ${newPackages.size} packages")
                    }
                }
                .onFailure { 
                    if (error == null) { // Only set error if not already set to avoid flicker
                        error = it.message
                    }
                }
        }
    }
    
    // Load packages when device or filter changes
    LaunchedEffect(deviceManager.selectedDevice, AppsStateManager.currentFilter) {
        val selectedDevice = deviceManager.selectedDevice
        if (selectedDevice != null) {
            isLoading = true
            error = null
            refreshPackages()
            isLoading = false
        } else {
            packages = emptyList()
            error = null
        }
    }
    
    // 5-second auto-refresh when screen is visible
    LaunchedEffect(isScreenVisible, deviceManager.selectedDevice) {
        if (!isScreenVisible || deviceManager.selectedDevice == null) return@LaunchedEffect
        
        while (isScreenVisible && deviceManager.selectedDevice != null) {
            kotlinx.coroutines.delay(5000) // 5 seconds
            if (isScreenVisible && deviceManager.selectedDevice != null) {
                refreshPackages()
            }
        }
    }
    
    // Lifecycle callbacks to track screen visibility
    DisposableEffect(Unit) {
        isScreenVisible = true
        onDispose {
            isScreenVisible = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        // Search field with tune button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search packages...") },
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
                    if (searchQuery.text.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = TextFieldValue("") }
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
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { 
                    println("Tune button clicked, showing dialog")
                    showFilterDialog = true 
                }
            ) {
                Icon(
                    painterResource(AppsResources.ic_tune),
                    contentDescription = "Filter options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Filter name and count display
        Text(
            text = "${AppsStateManager.currentFilter.displayName} (${filteredPackages.size})",
            modifier = Modifier.padding(start = 32.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Content area
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                deviceManager.selectedDevice == null -> {
                    Text(
                        text = "No device selected",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                packages.isEmpty() -> {
                    Text(
                        text = "No packages found",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                filteredPackages.isEmpty() && searchQuery.text.isNotEmpty() -> {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No packages found for \"${searchQuery.text}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { searchQuery = TextFieldValue("") }
                            ) {
                                Text("Clear filter")
                            }
                            TextButton(
                                onClick = {
                                    try {
                                        val encodedQuery = URLEncoder.encode("Android App ${searchQuery.text}", StandardCharsets.UTF_8.toString())
                                        val searchUrl = "https://www.google.com/search?q=$encodedQuery"
                                        
                                        if (Desktop.isDesktopSupported()) {
                                            Desktop.getDesktop().browse(URI(searchUrl))
                                        }
                                    } catch (e: Exception) {
                                        println("Failed to open browser: ${e.message}")
                                    }
                                }
                            ) {
                                Text("Find online")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredPackages) { packageName ->
                            val isSelected = AppsStateManager.selectedPackage == packageName
                            val interactionSource = remember { MutableInteractionSource() }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { AppsStateManager.selectPackage(packageName) }
                                    .background(
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                            ) {
                                Text(
                                    text = packageName,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    
                    VerticalScrollbar(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(listState)
                    )
                }
            }
        }
        
        // Filter options dialog
        FilterOptionsDialog(
            isVisible = showFilterDialog,
            onDismiss = { 
                println("Dialog dismissed")
                showFilterDialog = false 
            },
            onFilterSelected = { filter ->
                println("Filter selected: ${filter.displayName}")
                AppsStateManager.setFilter(filter)
                showFilterDialog = false
            },
            currentFilter = AppsStateManager.currentFilter
        )
        
        // Debug info
        println("Current state: showFilterDialog=$showFilterDialog, packages=${packages.size}, currentFilter=${AppsStateManager.currentFilter.displayName}")
    }
}

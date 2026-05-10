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
    
    val filteredPackages = packages.filter { 
        it.contains(searchQuery.text, ignoreCase = true) 
    }
    
    LaunchedEffect(deviceManager.selectedDevice) {
        val selectedDevice = deviceManager.selectedDevice
        if (selectedDevice != null) {
            isLoading = true
            error = null
            adbManager.getInstalledPackages(selectedDevice.serial)
                .onSuccess { 
                    packages = it 
                }
                .onFailure { 
                    error = it.message 
                }
            isLoading = false
        } else {
            packages = emptyList()
            error = null
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
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                        contentPadding = PaddingValues(16.dp),
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
    }
}

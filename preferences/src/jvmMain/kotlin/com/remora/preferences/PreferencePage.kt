package com.remora.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PreferencePage(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .padding(32.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            var selectedTab by remember { mutableStateOf("Theme") }
            
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("Theme") },
                        selected = selectedTab == "Theme",
                        onClick = { selectedTab = "Theme" },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    
                    // Future items will be added here
                }
                
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(40.dp)
                ) {
                    when (selectedTab) {
                        "Theme" -> ThemePreferencePage()
                    }
                }
            }
        }
    }
}

@Composable
fun ThemePreferencePage() {
    var selectedTheme by remember { mutableStateOf("System") }
    
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Choose how Remora looks to you. Select a theme or let it follow your system settings.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeOption(
                label = "System",
                description = "Follow system appearance",
                selected = selectedTheme == "System",
                onClick = { selectedTheme = "System" }
            )
            ThemeOption(
                label = "Light",
                description = "Always use light theme",
                selected = selectedTheme == "Light",
                onClick = { selectedTheme = "Light" }
            )
            ThemeOption(
                label = "Dark",
                description = "Always use dark theme",
                selected = selectedTheme == "Dark",
                onClick = { selectedTheme = "Dark" }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}

package com.remora.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.compose.koinInject

private val seedColors = listOf(
    0xFF6750A4, // Purple
    0xFF006C4C, // Green
    0xFF984061, // Pink
    0xFF0061A4, // Blue
    0xFF7D5260, // Reddish
    0xFF6B5E40, // Olive
    0xFF4E6078, // Slate
    0xFF006A6A, // Teal
    0xFF625B71, // Gray-ish
    0xFFA03D2A, // Orange-ish
    0xFF595B71, // Indigo-ish
    0xFF006874  // Cyan-ish
)

@Composable
fun PreferencePage(
    onDismissRequest: () -> Unit,
    preferenceStore: PreferenceStore = koinInject()
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
            var selectedTab by remember { mutableStateOf("Appearance") }
            
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
                        label = { Text("Appearance") },
                        selected = selectedTab == "Appearance",
                        onClick = { selectedTab = "Appearance" },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
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
                        "Appearance" -> AppearancePreferencePage(preferenceStore)
                    }
                }
            }
        }
    }
}

@Composable
fun AppearancePreferencePage(preferenceStore: PreferenceStore) {
    val selectedTheme by preferenceStore.theme.collectAsState()
    val selectedSeedColor by preferenceStore.seedColor.collectAsState()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Theme Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleLarge
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    ThemeCard(
                        theme = theme,
                        selected = selectedTheme == theme,
                        onClick = { preferenceStore.setTheme(theme) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Color Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = "Choose a seed color to generate your unique Material 3 color scheme.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(seedColors) { colorValue ->
                    ColorCircle(
                        color = Color(colorValue),
                        selected = selectedSeedColor == colorValue,
                        onClick = { preferenceStore.setSeedColor(colorValue) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Text(text = theme.name, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Surface(
                modifier = Modifier.size(16.dp),
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 4.dp
            ) {}
        }
    }
}

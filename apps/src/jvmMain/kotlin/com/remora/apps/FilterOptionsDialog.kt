package com.remora.apps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.remora.apps.AppsResources
import com.remora.apps.Res
import org.jetbrains.compose.resources.painterResource

/**
 * Package filter options
 */
@OptIn(ExperimentalStdlibApi::class)
enum class PackageFilterOption(val displayName: String, val adbFlag: String) {
    ALL_APPS("All Apps", ""),
    USER_APPS("User Apps", "-3"),
    SYSTEM_APPS("System Apps", "-s"),
    DISABLED_APPS("Disabled Apps", "-d"),
    ENABLED_APPS("Enabled Apps", "-e")
}

/**
 * Custom popup dialog for package filter options
 */
@Composable
fun FilterOptionsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onFilterSelected: (PackageFilterOption) -> Unit,
    currentFilter: PackageFilterOption
) {
    if (!isVisible) return
    
    Dialog(
        onDismissRequest = onDismiss,
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
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Package Filter Options",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            painterResource(AppsResources.ic_close),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Radio button options
                PackageFilterOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onFilterSelected(option)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentFilter == option,
                            onClick = { 
                                onFilterSelected(option)
                                onDismiss()
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

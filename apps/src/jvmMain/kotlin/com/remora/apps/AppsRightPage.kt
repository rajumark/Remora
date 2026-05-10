package com.remora.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remora.apps.AppsResources
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppsRightPage() {
    val selectedPackage = AppsStateManager.selectedPackage
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedPackage ?: "No package selected",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedPackage != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                IconButton(
                    onClick = {
                        AppsStateManager.selectPackage(null)
                        AppsStateManager.toggleRightPageVisibility(false)
                    }
                ) {
                    Icon(
                        painterResource(AppsResources.ic_close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rest of the content can go here
            Text(
                text = "Package details will appear here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

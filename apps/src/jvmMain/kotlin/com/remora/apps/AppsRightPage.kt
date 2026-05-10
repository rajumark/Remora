package com.remora.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remora.apps.AppsResources
import org.jetbrains.compose.resources.painterResource
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun AppsRightPage() {
    val selectedPackage = AppsStateManager.selectedPackage
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Basic", "Permissions", "SharedPreferences", "Files", "Manifest")

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val packageInteractionSource = remember { MutableInteractionSource() }
                val isPackageHovered by packageInteractionSource.collectIsHoveredAsState()

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .hoverable(packageInteractionSource),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedPackage ?: "No package selected",
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedPackage != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    if (isPackageHovered && selectedPackage != null) {
                        OutlinedButton(
                            onClick = {
                                val selection = StringSelection(selectedPackage)
                                Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
                            }
                        ) {
                            Text("Copy")
                        }
                    }
                }

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

            // Scrollable tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> AppsRightBasicPage()
                    1 -> AppsRightPermissionsPage()
                    2 -> AppsRightSharedPreferencePage()
                    3 -> AppsRightFilesPage()
                    4 -> AppsRightManifestPage()
                }
            }
        }
    }
}

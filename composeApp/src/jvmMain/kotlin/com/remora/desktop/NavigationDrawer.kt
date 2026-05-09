package com.remora.desktop

import com.remora.adb.AdbManager
import com.remora.device.DeviceManager
import com.remora.device.DeviceSelector

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

data class NavigationItem(
    val destination: String,
    val label: String,
    val iconResource: DrawableResource
)

private val navigationItems = listOf(
    NavigationItem(
        destination = "Apps",
        label = "Apps",
        iconResource = Res.drawable.ic_profile
    ),
    NavigationItem(
        destination = "Terminal",
        label = "Terminal",
        iconResource = Res.drawable.ic_dashboard
    ),
    NavigationItem(
        destination = "Design",
        label = "Design",
        iconResource = Res.drawable.ic_design
    ),
    NavigationItem(
        destination = "Settings",
        label = "Settings",
        iconResource = Res.drawable.ic_settings
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
    selectedDestination: String = "Apps",
    onNavigationItemClick: (String) -> Unit = {},
    onPreferencesClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Device Selection Section (Now modularized)
        DeviceSelector()

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

        Spacer(Modifier.weight(1f))

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            icon = { Icon(painterResource(Res.drawable.ic_settings), contentDescription = null) },
            label = { Text("Preferences") },
            selected = false,
            onClick = onPreferencesClick
        )
    }
}

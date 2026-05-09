package com.remora.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import remora.composeapp.generated.resources.Res
import remora.composeapp.generated.resources.ic_dashboard
import remora.composeapp.generated.resources.ic_settings
import remora.composeapp.generated.resources.ic_profile
import remora.composeapp.generated.resources.ic_help

@Composable
fun AppNavigationDrawer(
    modifier: Modifier = Modifier,
    selectedDestination: String = "Dashboard",
    onNavigationItemClick: (String) -> Unit = {}
) {
    Card(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Navigation Items
            NavigationDrawerItem(
                icon = { Icon(painterResource(Res.drawable.ic_dashboard), contentDescription = null) },
                label = { Text("Dashboard") },
                selected = selectedDestination == "Dashboard",
                onClick = { onNavigationItemClick("Dashboard") }
            )
            NavigationDrawerItem(
                icon = { Icon(painterResource(Res.drawable.ic_settings), contentDescription = null) },
                label = { Text("Settings") },
                selected = selectedDestination == "Settings",
                onClick = { onNavigationItemClick("Settings") }
            )
            NavigationDrawerItem(
                icon = { Icon(painterResource(Res.drawable.ic_profile), contentDescription = null) },
                label = { Text("Profile") },
                selected = selectedDestination == "Profile",
                onClick = { onNavigationItemClick("Profile") }
            )
            NavigationDrawerItem(
                icon = { Icon(painterResource(Res.drawable.ic_help), contentDescription = null) },
                label = { Text("Help") },
                selected = selectedDestination == "Help",
                onClick = { onNavigationItemClick("Help") }
            )
        }
    }
}

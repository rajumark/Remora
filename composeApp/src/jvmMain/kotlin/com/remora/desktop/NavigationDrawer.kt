package com.remora.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import remora.composeapp.generated.resources.Res
import remora.composeapp.generated.resources.ic_dashboard
import remora.composeapp.generated.resources.ic_settings
import remora.composeapp.generated.resources.ic_profile
import remora.composeapp.generated.resources.ic_design
import remora.composeapp.generated.resources.ic_help
import remora.composeapp.generated.resources.ic_mobile

data class NavigationItem(
    val destination: String,
    val label: String,
    val iconResource: DrawableResource
)

private val navigationItems = listOf(
    NavigationItem(
        destination = "Dashboard",
        label = "Dashboard",
        iconResource = Res.drawable.ic_dashboard
    ),
    NavigationItem(
        destination = "Settings",
        label = "Settings",
        iconResource = Res.drawable.ic_settings
    ),
    NavigationItem(
        destination = "Design",
        label = "Design",
        iconResource = Res.drawable.ic_design
    ),
    NavigationItem(
        destination = "Apps",
        label = "Apps",
        iconResource = Res.drawable.ic_profile
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
    selectedDestination: String = "Dashboard",
    onNavigationItemClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Text Button at top
        TextButton(
            onClick = { /* Handle click */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                 
            ) {
                Icon(
                    painterResource(Res.drawable.ic_mobile),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Poco X5 Pro",
                    maxLines = 1
                )
            }
        }

        // Navigation Items
        navigationItems.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(painterResource(item.iconResource), contentDescription = null) },
                label = { Text(item.label) },
                selected = selectedDestination == item.destination,
                onClick = { onNavigationItemClick(item.destination) }
            )
        }
    }
}

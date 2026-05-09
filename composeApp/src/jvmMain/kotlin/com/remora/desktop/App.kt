package com.remora.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource

import remora.composeapp.generated.resources.Res
import remora.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    var selectedDestination by remember { mutableStateOf("Dashboard") }
    var isSidebarVisible by remember { mutableStateOf(true) }
    
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Navigation Menu
                if (isSidebarVisible) {
                    AppNavigationDrawer(
                        selectedDestination = selectedDestination,
                        onNavigationItemClick = { destination ->
                            selectedDestination = destination
                            println("Navigating to: $destination")
                        }
                    )
                }
                
                // Main Content
                when (selectedDestination) {
                    "Dashboard" -> DashboardPage()
                    "Settings" -> SettingsPage()
                    "Design" -> DesignPage()
                    "Profile" -> ProfilePage()
                    "Help" -> HelpPage()
                    else -> DashboardPage()
                }
            }
        }
    }
}
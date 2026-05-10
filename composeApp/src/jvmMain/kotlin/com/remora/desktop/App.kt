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

import com.remora.adb.AdbManager
import com.remora.device.DeviceManager
import com.remora.apps.AppsPage
import com.remora.settings.SettingsPage
import com.remora.design.DesignPage
import com.remora.help.HelpPage
import com.remora.terminal.TerminalPage
import com.remora.preferences.PreferencePage
import com.remora.preferences.PreferenceStore
import com.remora.preferences.AppTheme
import androidx.compose.foundation.isSystemInDarkTheme

import org.koin.compose.koinInject

@Composable
@Preview
fun App(
    adbManager: AdbManager = koinInject(),
    deviceManager: DeviceManager = koinInject(),
    preferenceStore: PreferenceStore = koinInject()
) {
    val selectedTheme by preferenceStore.theme.collectAsState()
    val seedColorValue by preferenceStore.seedColor.collectAsState()
    
    val isDark = when (selectedTheme) {
        AppTheme.System -> isSystemInDarkTheme()
        AppTheme.Light -> false
        AppTheme.Dark -> true
    }
    
    val seedColor = Color(seedColorValue)
    val colorScheme = preferenceStore.generateColorScheme(seedColor, isDark)

    var selectedDestination by remember { mutableStateOf("Apps") }
    var isSidebarVisible by remember { mutableStateOf(true) }
    var showPreferences by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Initialize ADB once at startup
        adbManager.initializeAdb()
        // Poll for devices every 2 seconds
        while (true) {
            deviceManager.refreshDevices()
            kotlinx.coroutines.delay(2000)
        }
    }
    
    MaterialTheme(colorScheme = colorScheme) {
        if (showPreferences) {
            PreferencePage(onDismissRequest = { showPreferences = false })
        }

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
                        },
                        onPreferencesClick = { showPreferences = true }
                    )
                }
                
                // Main Content
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    when (selectedDestination) {
                        "Apps" -> AppsPage()
                        "Terminal" -> TerminalPage()
                        "Design" -> DesignPage()
                        "Settings" -> SettingsPage()
                        "Help" -> HelpPage()
                        else -> AppsPage()
                    }
                }
            }
        }
    }
}
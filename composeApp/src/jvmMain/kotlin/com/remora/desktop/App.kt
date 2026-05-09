package com.remora.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    
    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Row(modifier = Modifier.fillMaxSize()) {
                // Navigation Menu
                AppNavigationDrawer(
                    selectedDestination = selectedDestination,
                    onNavigationItemClick = { destination ->
                        selectedDestination = destination
                        println("Navigating to: $destination")
                    }
                )
                
                // Main Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$selectedDestination screen",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}
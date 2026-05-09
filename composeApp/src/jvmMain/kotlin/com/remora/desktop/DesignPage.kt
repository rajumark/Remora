package com.remora.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.selection.SelectionContainer
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun DesignPage() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Typography", "Colors", "Buttons", "Cards", "Components", "Debug")

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> TypographyTab()
            1 -> ColorsTab()
            2 -> ButtonsTab()
            3 -> CardsTab()
            4 -> ComponentsTab()
            5 -> DebugTab()
        }
    }
}
}

fun copyToClipboard(text: String) {
    val selection = StringSelection(text)
    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
}

@Composable
fun CopyButton(code: String) {
    TextButton(
        onClick = { copyToClipboard(code) },
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text("Copy Code")
    }
}

@Composable
fun TypographyTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Typography Styles", style = MaterialTheme.typography.headlineSmall)

        val typographyStyles = listOf(
            "displayLarge" to MaterialTheme.typography.displayLarge,
            "displayMedium" to MaterialTheme.typography.displayMedium,
            "displaySmall" to MaterialTheme.typography.displaySmall,
            "headlineLarge" to MaterialTheme.typography.headlineLarge,
            "headlineMedium" to MaterialTheme.typography.headlineMedium,
            "headlineSmall" to MaterialTheme.typography.headlineSmall,
            "titleLarge" to MaterialTheme.typography.titleLarge,
            "titleMedium" to MaterialTheme.typography.titleMedium,
            "titleSmall" to MaterialTheme.typography.titleSmall,
            "bodyLarge" to MaterialTheme.typography.bodyLarge,
            "bodyMedium" to MaterialTheme.typography.bodyMedium,
            "bodySmall" to MaterialTheme.typography.bodySmall,
            "labelLarge" to MaterialTheme.typography.labelLarge,
            "labelMedium" to MaterialTheme.typography.labelMedium,
            "labelSmall" to MaterialTheme.typography.labelSmall
        )

        typographyStyles.forEach { (name, style) ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text("$name:", style = MaterialTheme.typography.labelMedium)
                Text("Sample Text", style = style)
                CopyButton("MaterialTheme.typography.$name")
            }
        }
    }
}

@Composable
fun ColorsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Color Scheme", style = MaterialTheme.typography.headlineSmall)

        val colors = MaterialTheme.colorScheme
        val colorList = listOf(
            "primary" to colors.primary,
            "onPrimary" to colors.onPrimary,
            "primaryContainer" to colors.primaryContainer,
            "onPrimaryContainer" to colors.onPrimaryContainer,
            "secondary" to colors.secondary,
            "onSecondary" to colors.onSecondary,
            "secondaryContainer" to colors.secondaryContainer,
            "onSecondaryContainer" to colors.onSecondaryContainer,
            "tertiary" to colors.tertiary,
            "onTertiary" to colors.onTertiary,
            "tertiaryContainer" to colors.tertiaryContainer,
            "onTertiaryContainer" to colors.onTertiaryContainer,
            "error" to colors.error,
            "onError" to colors.onError,
            "errorContainer" to colors.errorContainer,
            "onErrorContainer" to colors.onErrorContainer,
            "background" to colors.background,
            "onBackground" to colors.onBackground,
            "surface" to colors.surface,
            "onSurface" to colors.onSurface,
            "surfaceVariant" to colors.surfaceVariant,
            "onSurfaceVariant" to colors.onSurfaceVariant,
            "surfaceBright" to colors.surfaceBright,
            "surfaceDim" to colors.surfaceDim,
            "surfaceContainer" to colors.surfaceContainer,
            "surfaceContainerHigh" to colors.surfaceContainerHigh,
            "surfaceContainerHighest" to colors.surfaceContainerHighest,
            "surfaceContainerLow" to colors.surfaceContainerLow,
            "surfaceContainerLowest" to colors.surfaceContainerLowest,
            "inverseSurface" to colors.inverseSurface,
            "inverseOnSurface" to colors.inverseOnSurface,
            "inversePrimary" to colors.inversePrimary,
            "outline" to colors.outline,
            "outlineVariant" to colors.outlineVariant,
            "scrim" to colors.scrim
        )

        colorList.forEach { (name, color) ->
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 16.dp)
                ) {
                    Surface(
                        color = color,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                }
                CopyButton("MaterialTheme.colorScheme.$name")
            }
        }
    }
}

@Composable
fun ButtonsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Button Types", style = MaterialTheme.typography.headlineSmall)

        // Filled Button
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filled Button", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {}) { Text("Filled") }
            CopyButton("Button(onClick = { }) { Text(\"Filled\") }")
        }

        // Outlined Button
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Outlined Button", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = {}) { Text("Outlined") }
            CopyButton("OutlinedButton(onClick = { }) { Text(\"Outlined\") }")
        }

        // Text Button
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Text Button", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = {}) { Text("Text") }
            CopyButton("TextButton(onClick = { }) { Text(\"Text\") }")
        }

        // Elevated Button
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Elevated Button", style = MaterialTheme.typography.titleMedium)
            ElevatedButton(onClick = {}) { Text("Elevated") }
            CopyButton("ElevatedButton(onClick = { }) { Text(\"Elevated\") }")
        }

        // Filled Tonal Button
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filled Tonal Button", style = MaterialTheme.typography.titleMedium)
            FilledTonalButton(onClick = {}) { Text("Tonal") }
            CopyButton("FilledTonalButton(onClick = { }) { Text(\"Tonal\") }")
        }
    }
}

@Composable
fun CardsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Card Variations", style = MaterialTheme.typography.headlineSmall)

        // Basic Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Basic Card", style = MaterialTheme.typography.titleMedium)
                Text("Default Material Card")
                CopyButton("Card { /* content */ }")
            }
        }

        // Elevated Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Elevated Card", style = MaterialTheme.typography.titleMedium)
                Text("Card with custom elevation")
                CopyButton("Card(elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) { }")
            }
        }

        // Outlined Card
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Outlined Card", style = MaterialTheme.typography.titleMedium)
                Text("Card with outline border")
                CopyButton("OutlinedCard { /* content */ }")
            }
        }

        // Clickable Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = {}
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Clickable Card", style = MaterialTheme.typography.titleMedium)
                Text("Card that responds to clicks")
                CopyButton("Card(onClick = { }) { /* content */ }")
            }
        }
    }
}

@Composable
fun ComponentsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Other Components", style = MaterialTheme.typography.headlineSmall)

        // Chips
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Chips", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("Assist") })
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Filter") }
                )
            }
            CopyButton("AssistChip(onClick = { }, label = { Text(\"Assist\") })")
        }

        // Progress Indicators
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Progress", style = MaterialTheme.typography.titleMedium)
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            CopyButton("CircularProgressIndicator()")
        }

        // Switches
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Switches", style = MaterialTheme.typography.titleMedium)
            var checked by remember { mutableStateOf(true) }
            Switch(checked = checked, onCheckedChange = { checked = it })
            CopyButton("Switch(checked = checked, onCheckedChange = { checked = it })")
        }

        // Checkboxes
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Checkboxes", style = MaterialTheme.typography.titleMedium)
            var checked by remember { mutableStateOf(true) }
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            CopyButton("Checkbox(checked = checked, onCheckedChange = { checked = it })")
        }

        // Radio Buttons
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Radio Buttons", style = MaterialTheme.typography.titleMedium)
            var selected by remember { mutableStateOf(true) }
            RadioButton(selected = selected, onClick = { selected = !selected })
            CopyButton("RadioButton(selected = selected, onClick = { })")
        }

        // Slider
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Slider", style = MaterialTheme.typography.titleMedium)
            var value by remember { mutableStateOf(0.5f) }
            Slider(value = value, onValueChange = { value = it })
            CopyButton("Slider(value = value, onValueChange = { value = it })")
        }
    }
}

@Composable
fun DebugTab() {
    var adbPath by remember { mutableStateOf("Loading...") }
    var adbVersion by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        AdbManager.initializeAdb().onSuccess { path ->
            val platform = AdbManager.getCurrentPlatform()
            val executable = if (platform == "windows") "platform-tools/adb.exe" else "platform-tools/adb"
            adbPath = java.io.File(path, executable).absolutePath
        }.onFailure {
            adbPath = "Error: ${it.message}"
        }

        AdbManager.getAdbVersion().onSuccess {
            adbVersion = it
        }.onFailure {
            adbVersion = "Error: ${it.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Debug Information", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ADB Path", style = MaterialTheme.typography.titleMedium)
                SelectionContainer {
                    Text(adbPath, style = MaterialTheme.typography.bodyMedium)
                }
                CopyButton(adbPath)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ADB Version", style = MaterialTheme.typography.titleMedium)
                SelectionContainer {
                    Text(adbVersion, style = MaterialTheme.typography.bodyMedium)
                }
                CopyButton(adbVersion)
            }
        }
    }
}

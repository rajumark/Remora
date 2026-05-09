package com.remora.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

import com.remora.desktop.di.appModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Remora",
            state = rememberWindowState(width = 1280.dp, height = 720.dp)
        ) {
            App()
        }
    }
}
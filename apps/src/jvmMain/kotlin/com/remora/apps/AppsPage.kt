package com.remora.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import com.remora.adb.AdbManager
import com.remora.device.DeviceManager
import com.remora.design.SplitView

@Composable
fun AppsPage(
    adbManager: AdbManager = koinInject(),
    deviceManager: DeviceManager = koinInject()
) {
    val isRightPageVisible = AppsStateManager.isRightPageVisible
    
    SplitView(
        modifier = Modifier.fillMaxSize(),
        initialLeftWidthRatio = 0.4f,
        leftContent = {
            AppsLeftPage()
        },
        rightContent = {
            if (isRightPageVisible) {
                AppsRightPage()
            }
        }
    )
}

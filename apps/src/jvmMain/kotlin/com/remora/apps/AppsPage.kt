package com.remora.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    SplitView(
        modifier = Modifier.fillMaxSize(),
        initialLeftWidthRatio = 0.3f,
        leftContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Text("Left Panel")
            }
        },
        rightContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 4.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Text("Right Panel")
            }
        }
    )
}

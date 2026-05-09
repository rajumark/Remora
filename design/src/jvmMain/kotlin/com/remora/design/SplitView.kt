package com.remora.design

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.awt.awtEventOrNull
import java.awt.Cursor
import androidx.compose.ui.input.pointer.PointerInputScope

@Composable
fun SplitView(
    modifier: Modifier = Modifier,
    initialLeftWidthRatio: Float = 0.3f,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit
) {
    var leftWidthRatio by remember { mutableStateOf(initialLeftWidthRatio) }
    var isDragging by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        val density = LocalDensity.current

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left Panel
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * leftWidthRatio)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                leftContent()
            }

            // Divider Handle
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(
                        if (isDragging) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) 
                        else 
                            MaterialTheme.colorScheme.outlineVariant
                    )
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                event.awtEventOrNull?.let { awtEvent ->
                                    awtEvent.component?.cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { 
                                isDragging = true
                            },
                            onDragEnd = { 
                                isDragging = false
                            },
                            onDragCancel = { 
                                isDragging = false
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                // Calculate the new ratio based on the total width
                                val totalWidthPx = with(density) { totalWidth.toPx() }
                                val ratioChange = dragAmount / totalWidthPx
                                val newRatio = (leftWidthRatio + ratioChange).coerceIn(0.1f, 0.9f)
                                leftWidthRatio = newRatio
                            }
                        )
                    },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(
                        if (isDragging)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        CircleShape
                    )
            )
        }

        // Right Panel
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * (1f - leftWidthRatio))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                rightContent()
            }
        }
    }
}

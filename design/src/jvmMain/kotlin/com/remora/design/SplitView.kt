package com.remora.design

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.awt.awtEventOrNull
import java.awt.Cursor

@Composable
fun SplitView(
    modifier: Modifier = Modifier,
    initialLeftWidthRatio: Float = 0.3f,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit
) {
    var leftWidthRatio by remember { mutableStateOf(initialLeftWidthRatio) }
    var isDragging by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

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
            ) {
                leftContent()
            }

            // Divider Handle
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .hoverable(interactionSource)
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
                            onHorizontalDrag = { _, dragAmount ->
                                val totalWidthPx = with(density) { totalWidth.toPx() }
                                val ratioChange = dragAmount / totalWidthPx
                                val newRatio =
                                    (leftWidthRatio + ratioChange).coerceIn(0.1f, 0.9f)

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
                            when {
                                isDragging || isHovered -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            },
                            CircleShape
                        )
                )
            }

            // Right Panel
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * (1f - leftWidthRatio))
            ) {
                rightContent()
            }
        }
    }
}

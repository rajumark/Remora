package com.remora.device

import org.jetbrains.compose.resources.DrawableResource

/**
 * Public accessor for device-specific resources.
 * This bypasses the 'internal' visibility of the generated Res class.
 */
object DeviceResources {
    val ic_mobile: DrawableResource get() = Res.drawable.ic_mobile
}

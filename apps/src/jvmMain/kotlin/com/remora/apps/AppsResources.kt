package com.remora.apps

import org.jetbrains.compose.resources.DrawableResource

/**
 * Public accessor for apps-specific resources.
 * This bypasses the 'internal' visibility of the generated Res class.
 */
object AppsResources {
    val ic_search: DrawableResource get() = Res.drawable.ic_search
    val ic_close: DrawableResource get() = Res.drawable.ic_close
}

package com.remora.preferences

data class AccentColor(
    val name: String,
    val color: Long
)

val accentColors = listOf(
    AccentColor("Midnight", 0xFF1E293B),
    AccentColor("Sierra Blue", 0xFF007AFF),
    AccentColor("Alpine Green", 0xFF28CD41),
    AccentColor("Deep Purple", 0xFF5856D6),
    AccentColor("Rose Gold", 0xFFFF2D55),
    AccentColor("Amber", 0xFFFF9500),
    AccentColor("Teal Mist", 0xFF5AC8FA),
    AccentColor("Sunlight", 0xFFFFCC00),
    AccentColor("Amethyst", 0xFFAF52DE),
    AccentColor("Crimson", 0xFFFF3B30),
    AccentColor("Slate", 0xFF8E8E93),
    AccentColor("Desert", 0xFFA2845E)
)

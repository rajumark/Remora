package com.remora.preferences

import java.util.prefs.Preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamicColorScheme

enum class AppTheme {
    System, Light, Dark
}

class PreferenceStore {
    private val prefs = Preferences.userRoot().node("com.remora.desktop")

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme

    private val _seedColor = MutableStateFlow(loadSeedColor())
    val seedColor: StateFlow<Long> = _seedColor

    fun setTheme(theme: AppTheme) {
        prefs.put("theme", theme.name)
        prefs.flush()
        _theme.value = theme
    }

    fun setSeedColor(color: Long) {
        prefs.putLong("seed_color", color)
        prefs.flush()
        _seedColor.value = color
    }

    private fun loadTheme(): AppTheme {
        val name = prefs.get("theme", AppTheme.System.name)
        return try {
            AppTheme.valueOf(name)
        } catch (e: Exception) {
            AppTheme.System
        }
    }

    private fun loadSeedColor(): Long {
        // Default Material 3 blue seed color
        return prefs.getLong("seed_color", 0xFF6750A4)
    }

    /**
     * Generates a full Material 3 ColorScheme based on a seed color using the MaterialKolor library.
     */
    fun generateColorScheme(seedColor: Color, isDark: Boolean): ColorScheme {
        return dynamicColorScheme(
            seedColor = seedColor,
            isDark = isDark,
            isAmoled = false
        )
    }
}

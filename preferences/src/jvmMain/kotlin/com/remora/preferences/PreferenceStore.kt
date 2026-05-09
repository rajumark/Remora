package com.remora.preferences

import com.russhwolf.multiplatformsettings.Settings
import com.russhwolf.multiplatformsettings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppTheme {
    System, Light, Dark
}

class PreferenceStore {
    private val settings: Settings = Settings()

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme

    private val _seedColor = MutableStateFlow(loadSeedColor())
    val seedColor: StateFlow<Long> = _seedColor

    fun setTheme(theme: AppTheme) {
        settings["theme"] = theme.name
        _theme.value = theme
    }

    fun setSeedColor(color: Long) {
        settings["seed_color"] = color
        _seedColor.value = color
    }

    private fun loadTheme(): AppTheme {
        val name = settings.getString("theme", AppTheme.System.name)
        return try {
            AppTheme.valueOf(name)
        } catch (e: Exception) {
            AppTheme.System
        }
    }

    private fun loadSeedColor(): Long {
        // Default Material 3 blue seed color
        return settings.getLong("seed_color", 0xFF6750A4)
    }
}

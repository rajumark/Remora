package com.remora.settings

import java.util.prefs.Preferences

object SettingsPinDatabase {
    private const val PINNED_SETTINGS_KEY = "pinned_settings"
    private val prefs: Preferences = Preferences.userNodeForPackage(SettingsPinDatabase::class.java)

    fun pinSetting(settingId: String) {
        val currentList = getPinList().toMutableList()
        if (!currentList.contains(settingId)) {
            currentList.add(settingId)
            prefs.put(PINNED_SETTINGS_KEY, currentList.joinToString(","))
            prefs.flush()
        }
    }

    fun getPinList(): List<String> {
        val storedValue = prefs.get(PINNED_SETTINGS_KEY, "")
        return if (storedValue.isEmpty()) {
            emptyList()
        } else {
            storedValue.split(",").filter { it.isNotEmpty() }
        }
    }

    fun unPinSetting(settingId: String) {
        val currentList = getPinList().toMutableList()
        currentList.remove(settingId)
        prefs.put(PINNED_SETTINGS_KEY, currentList.joinToString(","))
        prefs.flush()
    }
}

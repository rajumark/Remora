package com.remora.desktop.di

import org.koin.dsl.module
import com.remora.adb.AdbManager
import com.remora.device.DeviceManager
import com.remora.preferences.PreferenceStore

/**
 * Global Koin module for providing core dependencies.
 */
val appModule = module {
    // Providing our managers as singletons.
    // Even though they are Kotlin objects, using DI allows us to 
    // swap them for mocks in tests or different implementations later.
    single { AdbManager }
    single { DeviceManager }
    single { PreferenceStore() }
}

This is a Kotlin Multiplatform project targeting Desktop (JVM).

## Project Structure

```
Remora/
├── composeApp/          # Main Desktop Application
│   └── src/jvmMain/    # JVM-specific UI and app entry point
│
├── adb/                # Core ADB Management Module
│   └── src/jvmMain/    # ADB tool extraction, command execution
│
├── device/             # Device Management Module
│   └── src/jvmMain/    # Device discovery, selection state
│
├── apps/               # Apps Feature Module
│   └── src/jvmMain/    # App listing, app management UI
│
├── settings/           # Settings Feature Module
│   └── src/jvmMain/    # Android settings launcher UI
│
├── design/             # Design Feature Module
│   └── src/jvmMain/    # Design-related features
│
├── terminal/           # Terminal Feature Module
│   └── src/jvmMain/    # Terminal/ADB shell features
│
├── help/               # Help Feature Module
│   └── src/jvmMain/    # Help documentation
│
└── preferences/        # Preferences Feature Module
    └── src/jvmMain/    # Application preferences
```

## Module Dependencies

```
composeApp (Main Application)
    ├── adb (Core)
    ├── device (Core)
    ├── apps
    ├── settings
    ├── design
    ├── terminal
    ├── help
    └── preferences

Feature Modules (apps, settings, design, terminal, help, preferences)
    ├── adb (Core)
    └── device (Core)
```

### Module Descriptions

**Core Modules:**
- **adb** - Manages ADB tool extraction, path management, and command execution
- **device** - Manages device discovery, connection state, and device selection

**Feature Modules:**
- **apps** - App listing, installation, management, and app-specific operations
- **settings** - Android settings launcher for connected devices
- **design** - Design-related features and tools
- **terminal** - Terminal and ADB shell functionality
- **help** - Help documentation and user guidance
- **preferences** - Application preferences and configuration

**Application Module:**
- **composeApp** - Main desktop application that integrates all features

---

## Source Code Organization

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

---

### macOS Installation Note

After installing Remora.app, you may need to remove the quarantine attribute to open it:

```shell
sudo -s
xattr -rd com.apple.quarantine /Applications/Remora.app
```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
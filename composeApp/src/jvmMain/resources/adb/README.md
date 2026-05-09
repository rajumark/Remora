# ADB Platform Tools Distribution

This folder contains Android Debug Bridge (ADB) command-line tools for distribution with the Remora desktop application.

## Files
- `platform-tools-latest-windows.zip` - Windows ADB tools
- `platform-tools-latest-darwin.zip` - macOS ADB tools  
- `platform-tools-latest-linux.zip` - Linux ADB tools

## Usage in Application

These zip files are packaged as resources and can be extracted at runtime:

```kotlin
// Example runtime extraction code
val resourceStream = this::class.java.getResourceAsStream("/adb/platform-tools-latest-${getCurrentPlatform()}.zip")
// Extract and use ADB tools
```

## Platform Detection

The application should detect the current platform and extract the appropriate zip file:
- Windows → platform-tools-latest-windows.zip
- macOS → platform-tools-latest-darwin.zip
- Linux → platform-tools-latest-linux.zip

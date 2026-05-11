# Material Design Guidelines for Remora Project

## Core Principles

### Use Only Standard Material Design Components
- **NO custom UI components** - Always use built-in Material Design components
- **NO custom colors** - Use default MaterialTheme colors
- **NO custom styling** - Use default Material Design styling 

### Approved Material Design Components

#### Navigation
- `PermanentNavigationDrawer` - For desktop navigation
- `NavigationDrawerItem` - For navigation menu items
- `BottomNavigationBar` - For mobile bottom navigation

#### Layout
- `Row`, `Column`, `Box` - For basic layout
- `LazyColumn`, `LazyRow` - For scrolling lists
- `Card` - Only default styling, no custom colors

#### Input
- `Button`, `TextButton`, `OutlinedButton` - Use default styling
- `TextField` - Use default styling only
- `Checkbox`, `RadioButton`, `Switch` - Default styling

#### Display
- `Text` - Use MaterialTheme.typography only
- `Icon` - Use default icons only
- `Image` - Default loading behavior

#### Containers
- `Surface` - Default colors only
- `Scaffold` - For screen structure
- `TopAppBar` - Default styling

### Typography
- Use only `MaterialTheme.typography` values:
  - `displayLarge`, `displayMedium`, `displaySmall`
  - `headlineLarge`, `headlineMedium`, `headlineSmall`
  - `titleLarge`, `titleMedium`, `titleSmall`
  - `bodyLarge`, `bodyMedium`, `bodySmall`
  - `labelLarge`, `labelMedium`, `labelSmall`

### Colors
- **NEVER** specify custom colors
- Use only `MaterialTheme.colorScheme`:
  - `primary`, `onPrimary`
  - `secondary`, `onSecondary`
  - `surface`, `onSurface`
  - `background`, `onBackground`
  - `error`, `onError`

### Spacing
- Use Material Design spacing values:
- `4.dp`, `8.dp`, `16.dp`, `24.dp`, `32.dp`

### Examples of What NOT to Do

❌ **WRONG:**
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = Color.Blue // Custom color
    )
) { /* content */ }

Text(
    text = "Hello",
    color = Color.Red, // Custom color
    fontSize = 20.sp  // Custom font size
)
```

✅ **CORRECT:**
```kotlin
Card { /* content */ } // Default styling

Text(
    text = "Hello",
    style = MaterialTheme.typography.bodyLarge // Default typography
)
```

### Project-Specific Rules
1. Always prefer built-in Material Design components
2. Never create custom composables for standard UI elements
3. Use default MaterialTheme values for colors, typography, and shapes
4. Keep UI simple and consistent with Material Design guidelines

---

**Remember: When in doubt, use the default Material Design component without any customization.**

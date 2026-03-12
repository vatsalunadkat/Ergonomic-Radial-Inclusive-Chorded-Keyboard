# ERICK - Application Context & Architecture

**Version**: 0.3.0-alpha  
**Last Updated**: March 13, 2026  
**Project**: Ergonomic Radial Inclusive Controller Keyboard (ERICK)

## Executive Summary

ERICK is a cross-platform chorded keyboard system that enables text input through dual joystick movements (touch or physical controller). The application uses Kotlin Multiplatform to share core keyboard logic between Android and iOS implementations. Both platforms feature fully functional keyboard implementations:
- **Android**: Input Method Editor (IME) service with Jetpack Compose UI
- **iOS**: Custom Keyboard Extension with SwiftUI interface

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 Platform Layer (UI/OS)                   │
│  ┌─────────────────────┐      ┌─────────────────────┐  │
│  │   Android IME       │      │   iOS Extension     │  │
│  │  - Activities       │      │  - Keyboard Ext.    │  │
│  │  - IME Service      │      │  - SwiftUI Views    │  │
│  │  - Compose UI       │      │  - App Group Prefs  │  │
│  └──────────┬──────────┘      └──────────┬──────────┘  │
└─────────────┼─────────────────────────────┼─────────────┘
              │                             │
              └──────────┬──────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│          Shared Module (Kotlin Multiplatform)            │
│  ┌─────────────────────────────────────────────────┐    │
│  │  KeyboardStateMachine                           │    │
│  │  - State tracking (idle, first stick, complete) │    │
│  │  - Chord validation                             │    │
│  │  - Event processing                             │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │  KeyboardLogic                                  │    │
│  │  - Direction mapping                            │    │
│  │  - Character resolution                          │    │
│  │  - Chord combinations                           │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │  KeyboardContracts                              │    │
│  │  - Interfaces and data classes                  │    │
│  │  - Platform abstractions                        │    │
│  └─────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
```

### Technology Stack

**Cross-Platform:**
- Kotlin Multiplatform (KMP) - Shared business logic
- Coroutines - Async operations and state management

**Android:**
- Kotlin - Primary language
- Jetpack Compose - Modern declarative UI
- DataStore - Type-safe preferences storage
- Android IME Framework - Custom keyboard implementation
- Material Design 3 - UI components

**iOS:**
- Swift with KMP interop via XCFramework
- SwiftUI for modern declarative UI
- Custom Keyboard Extension
- App Groups for shared preferences

## Core Components

### 1. Shared Module (Kotlin Multiplatform)

Located in `android/shared/src/commonMain/kotlin/`

#### KeyboardStateMachine
**Purpose**: Manages the state transitions of joystick inputs to form complete chords.

**States**:
- `Idle`: No joystick active
- `FirstStickActive`: One joystick moved (awaiting second)
- `ChordComplete`: Both joysticks registered (ready to output character)

**Key Methods**:
- `processInput(leftDirection, rightDirection)`: Processes joystick positions
- `reset()`: Returns to idle state
- `getCurrentChord()`: Returns the active chord combination

#### KeyboardLogic
**Purpose**: Translates chord combinations into characters.

**Responsibilities**:
- Direction enumeration (UP, DOWN, LEFT, RIGHT, CENTER)
- Chord-to-character mapping
- Special character handling
- Layout switching support

#### KeyboardContracts
**Purpose**: Defines interfaces and data structures shared between platforms.

**Key Interfaces**:
- `KeyboardActionDelegate`: Platform callback for key events
- `ChordDefinition`: Data class for chord configurations
- `KeyboardLayout`: Interface for different layout modes

### 2. Android Implementation

Located in `android/app/src/main/java/com/vatoo/erick/`

#### MyInputMethodService
**Purpose**: Android IME service that integrates the keyboard with the Android OS.

**Responsibilities**:
- IME lifecycle management (onCreate, onDestroy)
- Input view creation and management
- Connection to text fields
- Integration with KeyboardStateMachine
- Observer pattern for preferences (layout type, left-handed mode)
- Coroutine scope management for async operations

**Key Features**:
- Uses custom keyboard layout (XML + programmatic views)
- Handles touch events from JoystickView
- Dispatches characters to active input connections
- Launches settings activity
- Reacts to preference changes at runtime

#### JoystickView
**Purpose**: Custom Android View for rendering and handling touch-based joystick input.

**Features**:
- Circular touch area with visual feedback
- 8-way directional detection
- Return-to-center animation
- Configurable sensitivity and dead zones
- Real-time position tracking
- Preview text display (right joystick)

**Technical Details**:
- Extends `View`
- Custom `onDraw()` for rendering
- `onTouchEvent()` for input handling
- Callback interface for direction changes

#### MainActivity
**Purpose**: Onboarding and IME enablement UI.

**Features**:
- **Guided Setup Flow**:
  1. Check if ERICK is enabled in system settings
  2. Check if ERICK is selected as current keyboard
  3. Provide direct links to system settings
- Helper functions to detect IME status
- Jetpack Compose UI for modern, responsive design
- Navigation to settings

#### SettingsActivity / SettingsScreen
**Purpose**: Configuration UI for keyboard preferences.

**Settings Available**:
- **Layout Type**: Logical (A–Z), Efficiency
- **Theme**: Dark mode toggle
- **Accessibility**:
  - Colorblind mode
  - Left-handed mode (swaps dial roles)

**Technical Details**:
- Built with Jetpack Compose
- Material Design 3 components
- Persists to DataStore
- Changes apply at runtime without restart

#### PreferencesManager (DataStore)
**Purpose**: Type-safe, asynchronous persistence of user settings.

**Stored Preferences**:
- `layout_type: String` ("logical" | "efficiency")
- `dark_theme: Boolean`
- `colorblind_mode: Boolean`
- `left_handed_mode: Boolean`

**Technical Approach**:
- Uses Preferences DataStore (key-value)
- Coroutine-based async reads/writes
- Flow-based reactive updates

### 3. iOS Implementation

Located in `ios/ERICK/`

#### KeyboardViewController
**Purpose**: iOS Custom Keyboard Extension controller that integrates with the iOS input system.

**Responsibilities**:
- Implements `KeyboardActionDelegate` from SharedKeyboard framework
- Manages `KeyboardStateMachine` instance
- Handles touch callbacks from SwiftUI joysticks
- Commits text via `textDocumentProxy`
- Updates preview text in ViewModel

**Key Features**:
- UIHostingController to embed SwiftUI views
- Keyboard height constraint (280pt)
- Globe button for keyboard switching (Apple requirement)
- Settings button to configure preferences

#### JoystickView (SwiftUI)
**Purpose**: SwiftUI view for touch-based joystick input.

**Features**:
- Drag gesture with thumb clamping
- Spring animation for return-to-center
- Visual feedback with colors matching Android
- Preview text display (right joystick)
- Configurable for left/right side

#### SettingsView
**Purpose**: SwiftUI settings screen accessible from keyboard and main app.

**Settings Available**:
- Layout Type (Logical, Efficiency)
- Dark Theme toggle
- Colorblind Mode toggle
- Left-Handed Mode toggle

**Technical Details**:
- Uses `@AppStorage` with App Group suite
- Shared between main app and keyboard extension
- Real-time updates via UserDefaults observers

#### SharedKeyboard.xcframework
**Purpose**: Pre-compiled Kotlin Multiplatform framework containing shared logic.

**Contents**:
- `KeyboardStateMachine`
- `KeyboardLogic`
- `KeyboardFactory`
- `KeyboardActionDelegate` protocol
- `Direction`, `KeyboardMode`, `LayoutType`, `InputAction` types

### 4. Data Flow

#### Input Flow (Touch to Character)

```
User Touch on JoystickView
    ↓
JoystickView detects direction
    ↓
Callback to MyInputMethodService
    ↓
Dispatch to KeyboardStateMachine.processInput()
    ↓
State machine checks current state:
  - Idle → FirstStickActive (store direction)
  - FirstStickActive → ChordComplete (both directions recorded)
    ↓
KeyboardLogic.resolveChord(leftDir, rightDir)
    ↓
Character returned (e.g., "A", "5", "?")
    ↓
MyInputMethodService.getCurrentInputConnection().commitText()
    ↓
Character appears in text field
```

#### Settings Flow

```
User opens Settings (from MainActivity or IME settings button)
    ↓
SettingsActivity/SettingsScreen rendered (Compose)
    ↓
User changes setting (toggle/radio button)
    ↓
SettingsViewModel updates state
    ↓
LayoutPreferences writes to DataStore
    ↓
Flow emits new preference value
    ↓
MyInputMethodService observes Flow
    ↓
Keyboard behavior updates (layout/theme/etc.)
```

## Chord Input System

### Chord Definition

A "chord" is a combination of two joystick directions (left stick + right stick) that maps to a character.

**Example Chords**:
- Left: UP, Right: UP → "A"
- Left: RIGHT, Right: UP → "5"
- Left: CENTER, Right: DOWN → "backspace"

### Layout Modes

1. **Logical Mode (A–Z)**: Characters organized alphabetically
   - Easy to learn and memorize
   - Groups: N=a-e, NE=f-j, E=k-o, SE=p-t, S=u-y, SW=z/symbols, W=1-5, NW=6-0

2. **Efficiency Mode**: Optimized for English letter frequency
   - Most common letters on easiest chords
   - Groups: E=e,h,t,r,m, S=a,d,o,l,w, N=i,c,n,u,f, etc.
   - Based on research optimization for typing speed

### Left-Handed Mode

When enabled, the roles of the two dials are swapped:
- **Normal Mode**: Left dial = letter groups (direction), Right dial = color/position
- **Left-Handed Mode**: Left dial = color/position, Right dial = letter groups (direction)
- Single-swipe utility functions (Space, Enter, Backspace) move to the LEFT dial
- Chord output remains identical (same chord = same character)

### State Machine Logic

The state machine prevents accidental inputs and ensures proper chord completion:

1. **Idle State**: Waiting for input
   - Both joysticks at center
   - No pending chord

2. **FirstStickActive**: One joystick moved
   - Records which joystick moved and in what direction
   - Waits for second joystick
   - Timeout (if implemented) returns to Idle

3. **ChordComplete**: Both joysticks have moved
   - Chord is resolved to character
   - Character is output
   - State resets to Idle (or waits for joysticks to return to center)

## Configuration and Preferences

### Android DataStore Schema

```kotlin
// Key definitions (PreferencesManager.kt)
private val LAYOUT_TYPE_KEY = stringPreferencesKey("layout_type")
private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
private val COLORBLIND_MODE_KEY = booleanPreferencesKey("colorblind_mode")
private val LEFT_HANDED_MODE_KEY = booleanPreferencesKey("left_handed_mode")

// Default values
layoutType: "logical"
darkTheme: false
colorblindMode: false
leftHandedMode: false
```

### iOS App Group UserDefaults

```swift
// App Group: group.com.vatoo.erick
// Keys (SettingsView.swift)
@AppStorage("layout_type") var layoutType: String = "logical"
@AppStorage("dark_theme") var darkTheme: Bool = false
@AppStorage("colorblind_mode") var colorblindMode: Bool = false
@AppStorage("left_handed_mode") var leftHandedMode: Bool = false
```

### Build Configuration

**Android App Module** (`android/app/build.gradle.kts`):
- compileSdk: 36
- minSdk: 24
- targetSdk: 36
- Dependencies: Compose, DataStore, Material3, Coroutines

**Shared Module** (`android/shared/build.gradle.kts`):
- Kotlin Multiplatform plugin
- Android library target (minSdk 24, compileSdk 36)
- iOS targets: iosX64, iosArm64, iosSimulatorArm64
- XCFramework output: "SharedKeyboard"

## Development Workflow

### Adding a New Feature

1. **Shared Logic** (if cross-platform):
   - Add to `shared/commonMain/kotlin/`
   - Write platform-agnostic code
   - Define interfaces in KeyboardContracts if platform-specific implementation needed

2. **Android Implementation**:
   - Add UI in `app/src/main/` (Compose or XML)
   - Wire up to shared logic in MyInputMethodService or relevant Activity
   - Add preferences to LayoutPreferences if persistent

3. **Testing**:
   - Unit tests for shared module
   - Android instrumentation tests for UI
   - Manual testing on physical device or emulator

### Version History

- **v0.3.0-alpha** (Current):
  - iOS keyboard extension fully implemented
  - Efficiency layout (optimized for English frequency)
  - Left-handed mode (swaps dial roles)
  - Onboarding flow for both platforms
  - Settings sync via App Group (iOS)

- **v0.2.1-alpha**:
  - Kotlin Multiplatform shared module
  - Settings UI with DataStore
  - JoystickView touch input
  - Onboarding flow (Android)
  - ERICK logo and branding

- **v0.1.x**:
  - Initial Android IME prototype
  - Basic XML keyboard layout
  - Simple touch input

## Future Architecture Considerations

### Physical Controller Support
- Detect gamepad connection (Android: InputManager, iOS: GCController)
- Map analog stick inputs to same state machine
- Handle button inputs for additional chords (triggers, bumpers)

### Cloud Sync
- Backend service for settings sync
- Authentication (Google, Apple Sign-In)
- Conflict resolution for multi-device users

### Analytics
- Typing speed tracking
- Common chord patterns
- Error rate analysis
- Privacy-preserving (local-first, opt-in)

## Key Files Reference

### Configuration Files
- `android/gradle/libs.versions.toml` - Dependency versions
- `android/app/build.gradle.kts` - App module build config
- `android/shared/build.gradle.kts` - KMP shared module config
- `android/settings.gradle.kts` - Module declarations
- `ios/ERICK/ERICK.xcodeproj/project.pbxproj` - Xcode project config

### Android Source Files
- `android/shared/src/commonMain/kotlin/KeyboardStateMachine.kt` - Core state logic
- `android/shared/src/commonMain/kotlin/KeyboardLogic.kt` - Chord mapping & layouts
- `android/shared/src/commonMain/kotlin/KeyboardContracts.kt` - Interfaces and types
- `android/app/src/main/java/com/vatoo/erick/MyInputMethodService.kt` - IME service
- `android/app/src/main/java/com/vatoo/erick/JoystickView.kt` - Touch input view
- `android/app/src/main/java/com/vatoo/erick/MainActivity.kt` - Onboarding UI
- `android/app/src/main/java/com/vatoo/erick/SettingsScreen.kt` - Settings UI
- `android/app/src/main/java/com/vatoo/erick/PreferencesManager.kt` - DataStore wrapper

### iOS Source Files
- `ios/ERICK/ErickKeyBoard/KeyboardViewController.swift` - Keyboard extension controller
- `ios/ERICK/ErickKeyBoard/JoystickView.swift` - SwiftUI joystick
- `ios/ERICK/ErickKeyBoard/SettingsView.swift` - In-keyboard settings
- `ios/ERICK/ERICK/ContentView.swift` - Main app onboarding
- `ios/ERICK/ERICK/SettingsView.swift` - Main app settings
- `ios/ERICK/SharedKeyboard.xcframework/` - KMP compiled framework

### Resource Files
- `android/app/src/main/res/xml/method.xml` - IME metadata
- `android/app/src/main/res/layout/keyboard_simple.xml` - Keyboard layout
- `android/app/src/main/res/values/strings.xml` - String resources
- `ios/ERICK/ErickKeyBoard/Info.plist` - Keyboard extension config

## Troubleshooting & Common Issues

### Issue: Shared module not compiling
**Solution**: Ensure Kotlin Multiplatform plugin is installed and Android Studio is updated.

### Issue: IME not showing in system settings
**Solution**: Check AndroidManifest.xml has proper IME service declaration with intent filter.

### Issue: DataStore not persisting
**Solution**: Verify DataStore context is application context, not activity context.

### Issue: JoystickView not responding
**Solution**: Check touch event handling in onTouchEvent() and ensure view is clickable/focusable.

---

**Document Maintained By**: ERICK Development Team  
**For Questions**: See [GitHub Issues](https://github.com/vatsalunadkat/Ergonomic-Radial-Inclusive-Chorded-Keyboard/issues)

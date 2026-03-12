# ERICK - iOS

This folder contains the iOS implementation of ERICK (Ergonomic Radial Inclusive Controller Keyboard).

## Overview

ERICK iOS is a custom keyboard extension that enables chord-based text input using touch joysticks. The app uses a pre-compiled Kotlin Multiplatform framework (`SharedKeyboard.xcframework`) to share keyboard logic with the Android implementation.

## Features

- **Chorded Keyboard Input**: Two SwiftUI joysticks for chord-based text entry
- **Multiple Layouts**:
  - **Logical (A–Z)**: Alphabetically organized, easy to learn
  - **Efficiency**: Optimized for English letter frequency
- **Accessibility Options**:
  - **Colorblind Mode**: Adjusted color scheme for color vision deficiency
  - **Left-Handed Mode**: Swaps left/right dial roles
- **Single-Swipe Utilities**: Space, Enter, Backspace, Shift, Caps Lock via single-joystick gestures
- **Dark Theme**: System-integrated dark mode support
- **Onboarding Flow**: Guided setup for enabling the keyboard extension

## Setup

### Requirements

- macOS with Xcode 15.0 or later
- iOS 14.0 or later target
- Swift 5.0+
- Apple Developer account (for device testing)

### Getting Started

1. Open `ios/ERICK/ERICK.xcodeproj` in Xcode
2. Select your development team in Signing & Capabilities for both targets:
   - `ERICK` (main app)
   - `ErickKeyBoard` (keyboard extension)
3. Build and run on simulator or device
4. Follow the in-app onboarding to enable ERICK as a keyboard

### Enabling the Keyboard

1. Open **Settings** → **General** → **Keyboard** → **Keyboards**
2. Tap **Add New Keyboard...**
3. Select **ERICK - ErickKeyBoard**
4. (Optional) Enable **Allow Full Access** if prompted

## Project Structure

```
ios/
├── ERICK/                        # Xcode project folder
│   ├── ERICK/                    # Main app target
│   │   ├── ERICKApp.swift        # App entry point
│   │   ├── ContentView.swift     # Main app UI (onboarding)
│   │   ├── SettingsView.swift    # Settings screen
│   │   ├── ERICK.entitlements    # App capabilities (App Group)
│   │   └── Assets.xcassets/      # App icons and images
│   ├── ErickKeyBoard/            # Keyboard extension target
│   │   ├── KeyboardViewController.swift  # IME controller
│   │   ├── JoystickView.swift            # SwiftUI joystick component
│   │   ├── SettingsView.swift            # In-keyboard settings
│   │   ├── Info.plist                    # Extension configuration
│   │   └── ErickKeyBoard.entitlements    # Extension capabilities
│   ├── SharedKeyboard.xcframework/       # KMP compiled framework
│   └── ERICK.xcodeproj/          # Xcode project file
└── README.md                     # This file
```

## Key Components

### Keyboard Extension
- **KeyboardViewController**: UIInputViewController subclass that implements `KeyboardActionDelegate` and integrates with `KeyboardStateMachine` from the shared framework
- **JoystickView**: SwiftUI view with drag gesture, thumb clamping, spring return-to-center animation, and preview text display
- **KeyboardContainerView**: Horizontal layout of left/right joysticks with settings button

### Main App
- **ContentView**: Onboarding UI with instructions for enabling the keyboard
- **SettingsView**: Configuration for layout, theme, and accessibility options

### Shared Framework (KMP)
The `SharedKeyboard.xcframework` contains pre-compiled Kotlin Multiplatform code:
- **KeyboardStateMachine**: Tracks joystick states and fires chords/single-swipes
- **KeyboardLogic**: Direction mapping, chord-to-character resolution
- **KeyboardFactory**: Factory for creating state machine instances
- Imported via `import SharedKeyboard` in Swift files

### Settings & Preferences
Settings are stored in App Group UserDefaults (`group.com.vatoo.erick`) for sharing between app and extension:
- `layout_type`: "logical" or "efficiency"
- `dark_theme`: Boolean
- `colorblind_mode`: Boolean
- `left_handed_mode`: Boolean

## Building

1. Open `ERICK.xcodeproj` in Xcode
2. Select the `ERICK` scheme for the main app or `ErickKeyBoard` for the extension
3. Choose a simulator or connected device
4. Press **Cmd+B** to build or **Cmd+R** to run

### Building for Device

1. Ensure your Apple Developer account is configured
2. Set the development team for both targets
3. Connect your iOS device
4. Select the device and press **Cmd+R**

## Testing the Keyboard

1. Build and run the main app
2. Follow the onboarding instructions to enable ERICK
3. Open any text input field (e.g., Notes, Messages, Safari)
4. Tap the globe icon to switch to ERICK
5. Use the touch joysticks to input text via chord combinations:
   - **Left joystick**: Select letter group (direction)
   - **Right joystick**: Select position within group (color)
   - **Single swipe (right only)**: Utility keys (Space, Enter, Backspace, etc.)

## Architecture Notes

**SwiftUI Integration**: The keyboard extension uses `UIHostingController` to embed SwiftUI views within the `UIInputViewController`.

**KMP Framework**: The `SharedKeyboard.xcframework` is a fat framework containing arm64 (device) and x86_64 + arm64 (simulator) slices. It's pre-compiled from the Android shared module.

**Text Input**: Characters are committed via `textDocumentProxy.insertText()` and actions via `textDocumentProxy.deleteBackward()`, `adjustTextPosition()`, etc.

**Keyboard Switching**: The globe button calls `advanceToNextInputMode()` as required by Apple for third-party keyboards.

**App Group**: Both the main app and keyboard extension share the `group.com.vatoo.erick` App Group for settings synchronization.

## Troubleshooting

**Keyboard not appearing in list**: Ensure the keyboard extension is properly signed and the App Group is configured on both targets

**SharedKeyboard import errors**: Verify that `SharedKeyboard.xcframework` is properly linked in Build Phases → Link Binary With Libraries

**Settings not syncing**: Check that both targets have the same App Group identifier in their entitlements

**Simulator issues**: If the keyboard doesn't appear on simulator, try resetting the simulator (Device → Erase All Content and Settings)

## Updating the Shared Framework

When the Kotlin Multiplatform shared module is updated:

1. Build the iOS framework from the Android project:
   ```bash
   cd android
   ./gradlew :shared:assembleSharedKeyboardXCFramework
   ```
2. Copy the generated `SharedKeyboard.xcframework` to `ios/ERICK/`
3. Clean and rebuild the Xcode project

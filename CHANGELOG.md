# Changelog

All notable changes to the ERICK (Ergonomic Radial Inclusive Controller Keyboard) project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0-alpha] - 2026-03-13

### Added
- **Left-Handed Mode**: Swaps the roles of left and right dials
  - Normal: Left dial = letter groups, Right dial = position
  - Left-handed: Left dial = position, Right dial = letter groups
  - Single-swipe utilities fire from left dial in left-handed mode
  - Runtime toggleable without restart
- **Efficiency Layout**: New keyboard layout optimized for English letter frequency
  - Common letters (e, t, a, o, i, n) on easiest chord positions
  - Based on research optimization algorithms
- **iOS Keyboard Extension**: Fully functional custom keyboard for iOS
  - SwiftUI-based JoystickView with spring animations
  - Integration with SharedKeyboard.xcframework (KMP)
  - Globe button for keyboard switching (Apple requirement)
  - Settings accessible from keyboard surface
- **iOS Onboarding Flow**: Guided setup UI for enabling keyboard extension
- **iOS Settings Screen**: Configuration for layout, theme, and accessibility
- **App Group Support (iOS)**: Shared preferences between app and keyboard extension
- **Demo Assets**: Added typing demo GIFs and onboarding flow recordings

### Changed
- **Shared Module**: Updated `KeyboardStateMachine` with left-handed mode support
  - Added `setLeftHandedMode()` and `isLeftHandedMode()` methods
  - Modified `fireChord()` and `getPreviewText()` to swap dial roles
  - Modified `handleTouch()` to fire single-swipes from correct dial
- **Android Settings**: Enhanced settings screen with new accessibility options
- **Documentation**: Reorganized demo files into `documentation/demo files/` folder

### Technical Details
- **Shared Module Changes**:
  - `KeyboardStateMachine.kt`: Added `isLeftHandedMode` state variable
  - `KeyboardLogic.kt`: Already supports both Logical and Efficiency layouts
- **Android Changes**:
  - `MyInputMethodService.kt`: Observes `leftHandedMode` preference
  - `PreferencesManager.kt`: Already had left-handed mode support
  - `SettingsScreen.kt`: Already had left-handed mode toggle
- **iOS Implementation**:
  - `KeyboardViewController.swift`: Full IME integration
  - `JoystickView.swift`: SwiftUI joystick with drag gesture
  - `SettingsView.swift`: App Group UserDefaults storage

## [0.2.1-alpha] - 2026-03-08

### Added
- **Kotlin Multiplatform (KMP) Shared Module**: Core keyboard logic now shared between Android and iOS
  - `KeyboardStateMachine`: State management for chord input processing
  - `KeyboardLogic`: Character resolution and chord mapping
  - `KeyboardContracts`: Platform-agnostic interfaces and contracts
- **JoystickView**: Custom Android view for touch-based joystick input with visual feedback
- **Settings UI**: Jetpack Compose-based settings screen with preferences for:
  - Keyboard layout selection (Logical, Efficiency)
  - Theme options (Dark mode toggle)
  - Colorblind mode
  - Left-handed mode
- **DataStore Integration**: Type-safe, async preferences storage replacing SharedPreferences
- **Onboarding Flow**: Guided setup UI in MainActivity to help users enable and select the IME
- **ERICK Logo**: Professional branding assets integrated throughout the app
- **Settings Button**: Quick access to settings from the IME keyboard view

### Changed
- **Repository Structure**: Reorganized project into platform-specific folders
  - Moved Android app to `android/` directory
  - Created `ios/` directory for iOS development
  - Separated documentation into `documentation/` folder
- **IME Service**: Refactored `MyInputMethodService` to use shared `KeyboardStateMachine`
- **MainActivity**: Enhanced with Compose UI and IME status checking
- **SettingsActivity**: Completely rewritten using Jetpack Compose and Material Design 3
- **Keyboard Layout**: Updated XML layout with better spacing and settings button integration

### Improved
- **Code Reusability**: Shared module enables code reuse between Android and iOS implementations
- **User Experience**: Onboarding flow makes IME setup much clearer for new users
- **Architecture**: Clean separation of platform-specific and shared code
- **Documentation**: Added comprehensive README files for Android and iOS folders
- **Build System**: Updated Gradle configuration with version catalog and KMP support

### Fixed
- Array out-of-bounds safety with `getOrNull()` in keyboard logic
- Coroutine lifecycle management in IME service
- Layout inconsistencies in keyboard view

### Technical Details
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36
- **Kotlin**: 1.9+
- **Gradle**: 8.0+
- **Dependencies Added**:
  - Jetpack Compose BOM and Material3
  - DataStore Preferences
  - Kotlinx Coroutines Core
  - Material Icons Extended

## [0.1.0] - Sprint 1 (Previous)

### Added
- Initial Android IME implementation
- Basic keyboard layout with XML views
- Touch-based input support
- Simple notepad integration
- Blue key background styling

### Features from Initial Release
- Basic chord input system
- Two-joystick input method
- Simple mode for accessibility

---

## Upcoming in Future Releases

### [Planned for 0.4.0]
- Physical controller support (gamepad input)
- Enhanced haptic feedback options
- Typing speed analytics

### [Planned for 0.5.0]
- Word prediction and autocorrect
- Tutorial/learning game mode
- Custom chord mapping

### [Planned for 1.0.0]
- Production-ready stability
- Multi-language support
- Cloud settings sync
- App Store and Play Store releases

---

**Note**: Alpha versions are pre-release builds for testing and development. Features and APIs may change without notice.

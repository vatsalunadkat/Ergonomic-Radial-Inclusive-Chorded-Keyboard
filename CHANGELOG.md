# Changelog

All notable changes to the ERICK (Ergonomic Radial Inclusive Controller Keyboard) project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.0-alpha] - 2026-03-20

### Added
- **Word Prediction & Autocorrect** (`WordPredictionEngine`):
  - Trie-based dictionary with ~700 words across 4 frequency tiers
  - Prefix completions sorted by frequency (e.g., "ca" → "can", "call", "case")
  - Spelling corrections using Levenshtein edit distance (max distance 2)
  - Bigram next-word predictions (~70 common pairs, e.g., "my" → "name")
  - Default sentence-starter suggestions ("I", "The", "Hello") when no context exists
  - Always-on suggestion bar — predictions appear immediately on keyboard open
  - Smart space insertion when accepting next-word suggestions
- **Accelerating Backspace**: Hold backspace to delete at increasing speed (character → word-level)
- **Physical Gaming Controller Input**:
  - Android: `InputManager` polling via `Handler` for analog stick directions
  - iOS: `GCController` with `DisplayLink` polling for gamepad input
  - Maps analog stick axes to the same 8-directional state machine

### Changed
- Suggestion bar now appears at the same height as the preview capsule (Android)
- `KeyboardStateMachine` expanded with word buffer tracking, suggestion orchestration, and last-completed-word state

## [0.3.0-alpha] - 2026-03-15

### Added
- **iOS Keyboard Extension**: Fully functional Custom Keyboard Extension
  - `KeyboardViewController` (UIInputViewController) hosting SwiftUI UI
  - SwiftUI JoystickView, KeyboardPreviewBar, KeyboardSuggestionBar
  - SharedKeyboard.xcframework integration for shared KMP logic
  - Settings via App Group UserDefaults (`group.com.vatoo.erick`)
  - Physical controller support via GCController
- **Custom Layout Creator**: Full chord-to-character editor
  - Interactive chord editor with visual color indicators per character
  - Save/load custom layouts to DataStore (Android) and UserDefaults (iOS)
- **Preview Bar**: Animated capsule showing color-coded characters as chords are formed
- **Light / Dark Mode**: System-respecting theme with manual override
- **Accessibility Fonts**: OpenDyslexic and Atkinson Hyperlegible font options
- **Shift / CapsLock Indicators**: Visual state display in keyboard UI
- **Colorblind Mode**: 6 palettes — Protanopia, Deuteranopia, Tritanopia, Achromatopsia, High Contrast, Default
- **Left-Handed Mode**: Mirrors joystick positions for left-hand dominant users
- **Efficiency Layout**: Letter-frequency-optimized chord assignments for faster typing

### Changed
- Settings screen expanded with font picker, colorblind palette selector, custom layout editor
- Keyboard XML layout updated with preview capsule and suggestion bar containers
- `KeyboardContracts` extended with `PlatformSettings`, prediction callbacks, and controller interfaces

### Improved
- Updated documentation and website to reflect dual-platform support
- Color-coded indicators in chord editors for easier layout customization

## [0.2.1-alpha] - 2026-03-08

### Added
- **Kotlin Multiplatform (KMP) Shared Module**: Core keyboard logic shared between Android and iOS
  - `KeyboardStateMachine`: State management for chord input processing
  - `KeyboardLogic`: Character resolution and chord mapping
  - `KeyboardContracts`: Platform-agnostic interfaces and contracts
- **JoystickView**: Custom Android view for touch-based joystick input with visual feedback
- **Settings UI**: Jetpack Compose settings with layout, theme, colorblind, left-handed options
- **DataStore Integration**: Type-safe, async preferences storage
- **Onboarding Flow**: Guided setup UI to help users enable and select the IME
- **ERICK Logo**: Professional branding assets integrated throughout the app
- **Settings Button**: Quick access to settings from the IME keyboard view

### Changed
- Repository reorganized into `android/`, `ios/`, `documentation/` directories
- `MyInputMethodService` refactored to use shared `KeyboardStateMachine`
- `SettingsActivity` rewritten with Jetpack Compose and Material Design 3

### Fixed
- Array out-of-bounds safety with `getOrNull()` in keyboard logic
- Coroutine lifecycle management in IME service
- Layout inconsistencies in keyboard view

### Technical Details
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36
- **Kotlin**: 1.9+
- **Gradle**: 8.0+

## [0.1.0] - Sprint 1

### Added
- Initial Android IME implementation
- Basic keyboard layout with XML views
- Touch-based chord input system with two-joystick method
- Simple mode for accessibility

---

## Upcoming in Future Releases

### [Planned for 0.5.0]
- Multi-language support (extended dictionaries, character sets)
- Mini typing game for chord learning and speed practice

### [Planned for 1.0.0]
- Production-ready stability
- Cloud settings sync
- Typing speed analytics and personal bests
- App Store and Play Store releases

---

**Note**: Alpha versions are pre-release builds for testing and development. Features and APIs may change without notice.

# iOS Keyboard Regression Checklist

## Layout Switching

- Open the keyboard extension settings.
- Switch `Logical (A-Z)` to `Efficiency`.
- Close settings.
- Confirm the left dial labels change immediately without touching either joystick.
- Switch back to `Logical (A-Z)` and confirm the dial updates immediately again.

## Efficiency Mapping Smoke Tests

- In efficiency mode, test `Left NW + Right N` and confirm preview and committed output are both `h`.
- Test `Left NW + Right W` and confirm preview and committed output are both `0`.
- Test `Left NW + Right NW` and confirm preview and committed output are both `c`.
- Test `Left N + Right W` and confirm preview and committed output are both `4`.
- Test `Left N + Right NW` and confirm preview and committed output are both `k`.
- Test `Left W + Right SW` and confirm preview and committed output are both `2`.
- Test `Left W + Right NW` and confirm preview and committed output are both `z`.

## Right-Only Actions

- Swipe only the right dial to `SW` and confirm shift toggles immediately.
- Swipe only the right dial to `NW` and confirm caps lock toggles immediately.
- Swipe only the right dial to `E` and confirm a space is inserted.
- Swipe only the right dial to `W` and confirm backspace deletes one character.

## Physical Controller Lifecycle

- Connect a physical game controller before opening the main app.
- Confirm the controller can move both on-screen sticks inside the app.
- Switch from the app to another app with a text field and activate the ERICK keyboard.
- Confirm the controller still moves the keyboard sticks and can enter text.
- Switch to a different text field in the same app and confirm controller input still works.
- Return to the ERICK app and confirm the controller still works without reconnecting.
- Background the ERICK app, foreground it again, then test controller input one more time.

## Pass Criteria

- Preview highlight, left-dial labels, and committed output must all match.
- Layout changes must be visible immediately after closing settings.
- A connected physical controller must keep working across app switches, input-field switches, and app foreground/background transitions.
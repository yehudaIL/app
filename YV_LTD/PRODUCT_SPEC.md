# Y.V L.T.D. — Product Specification

## Product vision
Y.V L.T.D. is a Hebrew-first Android field-intelligence dashboard that turns a phone's available sensors and connectivity APIs into understandable, time-based insights. It combines device diagnostics, nearby-radio observations, environmental measurements, and local trend detection in one polished app.

The app must never imply that it can detect every device, identify people, locate arbitrary phones, inspect cellular traffic, or see through walls. It only reports information Android and the device hardware legitimately expose.

## Target platform
- Android 13+ initially; degrade gracefully on older supported versions if added later.
- Kotlin, Android Studio, Jetpack Compose, Material 3.
- Hebrew-first with RTL support; English can be added later.
- Local-first architecture; core measurements and history work offline.
- No root required.
- No account required for MVP.

## Main navigation
1. ראשי / Dashboard — overall status, latest observations, quick scan actions.
2. Radar — Wi-Fi and Bluetooth observations supported by OS permissions and hardware.
3. סביבה / Environment — available ambient-light, proximity, accelerometer, gyroscope, and other sensors; microphone-based sound-level estimation only after explicit user action and clear permission.
4. תובנות / Intelligence — transparent rules-based insights from the user's own stored measurements.
5. היסטוריה / Time Machine — charts, saved sessions, comparisons, and export.
6. הגדרות / Settings — permissions, privacy, units, language, data deletion, diagnostics.

## MVP scope (first installable milestone)
- Premium dark dashboard branded Y.V L.T.D.
- Live sensor viewer using SensorManager; show only sensors actually present.
- Wi-Fi scan/results screen using supported Android APIs, with clear scan throttling and location/nearby-device permission handling as required by Android version.
- Bluetooth LE scan screen using supported APIs and runtime permissions; do not claim all Bluetooth devices are discoverable.
- Local measurement sessions saved in Room.
- History list and simple time-series charts.
- Basic local insight rules, e.g. Wi-Fi signal was weaker in this saved session than in your previous session. Each insight must show the measurements behind it.
- Permission education screens and graceful empty/error states.
- Export a simple text/CSV report from user-selected sessions.

## Later phases
- Room-level Wi-Fi signal mapping based on manually named measurement spots; do not pretend GPS or radio strength provides precise indoor positioning.
- More advanced trend detection and user-configurable alerts.
- Optional AI explanation layer only if a real model/service is configured; never fake AI responses.
- Optional widgets and scheduled summaries, subject to Android background-execution limits.

## Technical architecture
- UI: Jetpack Compose + Material 3, single-activity, Navigation Compose.
- Layers: ui, domain, data, platform.
- Persistence: Room; DataStore for preferences.
- Sensor access: SensorManager behind repository interfaces.
- Wi-Fi and Bluetooth: platform adapters with version-aware permission checks and lifecycle-safe scans.
- Charts: lightweight custom Compose charts or a maintained chart library after dependency review.
- Background work: WorkManager only for user-requested periodic summaries; no continuous hidden scanning.
- Tests: unit tests for insight rules, repository tests, and UI smoke tests.

## Privacy and safety requirements
- Explain every permission at the point it is needed.
- Scan only after a user action or an explicitly enabled, visible feature.
- No hidden/background radio scanning in the MVP.
- Store observations locally by default.
- Provide a clear delete-all-app-data action.
- Never collect or transmit scan results to a server in the MVP.
- Do not identify device owners or infer personal identities.
- Respect Android scan throttling and manufacturer-specific limitations.

## Visual direction
- Dark graphite background, restrained cyan/ice-blue accents, high-contrast readable text.
- Premium technical aesthetic, not cluttered or imitation hacker graphics.
- Dashboard cards, subtle motion, clear status labels, accessible touch targets.
- Hebrew RTL layout must be tested on a real Android 13 device.

## Definition of done for MVP
- Builds as a debug APK in Android Studio/Gradle.
- Installs and launches on an Android 13 device.
- Missing sensors and denied permissions do not crash the app.
- Wi-Fi/Bluetooth screens clearly communicate platform limitations.
- Sensor values update while the screen is active.
- Sessions persist across app restarts.
- History and insight calculations are reproducible from stored data.
- Offline dashboard, sensors, and history remain usable without internet.

## Build sequence
1. Inspect the existing repository and preserve unrelated projects.
2. Add the Android app skeleton and branding.
3. Implement navigation, dashboard, and sensor inventory.
4. Add Wi-Fi/Bluetooth user-initiated scans with permission flows.
5. Add Room history, comparisons, and explainable insights.
6. Run Gradle tests/build and verify installation on target hardware.
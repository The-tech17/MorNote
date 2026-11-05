# Implementation Plan - Multi-option Theme Mode

Enable three theme options: Light, Dark, and System Default. The selected mode will be persisted using Jetpack DataStore and applied across the entire application.

## User Review Required

> [!IMPORTANT]
> The implementation introduces `androidx.datastore:datastore-preferences` as a new dependency.
> The UI in `SettingsScreen` will be updated to use radio buttons for theme selection.

## Proposed Changes

### Dependencies

#### [MODIFY] [libs.versions.toml](file:///E:/AndroidStudioProjects/MorNote/gradle/libs.versions.toml)
- Add `androidx-datastore-preferences = "1.2.1"`.

#### [MODIFY] [build.gradle.kts](file:///E:/AndroidStudioProjects/MorNote/app/build.gradle.kts)
- Add `implementation(libs.androidx.datastore.preferences)`.

---

### Data Layer

#### [NEW] [ThemeMode.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/data/ThemeMode.kt)
- Define `enum class ThemeMode { LIGHT, DARK, SYSTEM }`.

#### [NEW] [ThemeRepository.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/data/ThemeRepository.kt)
- Implement `ThemeRepository` using `DataStore<Preferences>`.
- Expose `val themeMode: Flow<ThemeMode>`.
- Implement `suspend fun setThemeMode(mode: ThemeMode)`.

---

### Application & ViewModel

#### [MODIFY] [MorphedNotesApp.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/MorphedNotesApp.kt)
- Initialize `ThemeRepository`.

#### [MODIFY] [AppViewModelFactory.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/AppViewModelFactory.kt)
- Include `ThemeRepository` in the constructor.
- Add support for `SettingsViewModel`.

#### [NEW] [SettingsViewModel.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/ui/settings/SettingsViewModel.kt)
- Expose `themeMode` from repository.
- Provide `onThemeModeSelected(mode: ThemeMode)` function.

---

### UI Layer

#### [MODIFY] [SettingsScreen.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/ui/settings/SettingsScreen.kt)
- Update UI to display radio buttons for Light, Dark, and System theme.
- Connect to `SettingsViewModel`.

#### [MODIFY] [MainActivity.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/MainActivity.kt)
- Collect `themeMode` from `ThemeRepository`.
- Determine `darkTheme` boolean based on the selected mode:
    - `SYSTEM` -> `isSystemInDarkTheme()`
    - `DARK` -> `true`
    - `LIGHT` -> `false`
- Pass `darkTheme` to `MorNoteTheme`.

## Verification Plan

### Automated Tests
- N/A (Manual verification is more suitable for UI theme changes in this context, but unit tests for `ThemeRepository` could be added if requested).

### Manual Verification
1. Open Settings Screen.
2. Select "Dark" mode -> Verify app theme changes to dark immediately.
3. Select "Light" mode -> Verify app theme changes to light immediately.
4. Select "System Default" -> Verify app theme follows system setting.
5. Restart the app -> Verify the last selected theme is persisted.

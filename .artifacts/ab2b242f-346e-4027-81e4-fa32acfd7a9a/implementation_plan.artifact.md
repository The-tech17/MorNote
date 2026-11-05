# Implementation Plan - Build AI Vault

This plan outlines the steps to implement the AI Vault feature in MorNote, including a new bottom sheet UI, AI settings, backend integration via Cloud Functions, and an improved AI result screen.

## User Review Required

> [!IMPORTANT]
> - The backend integration assumes a Firebase Cloud Function is (or will be) deployed to handle the AI processing.
> - "Private AI" mode will continue to use the direct Gemini integration for now, while "Cloud AI" will route through the backend.

## Proposed Changes

### 1. Project Configuration & Dependencies

#### [MODIFY] [libs.versions.toml](file:///E:/AndroidStudioProjects/MorNote/gradle/libs.versions.toml)
- Add `firebase-functions-ktx` dependency.

#### [MODIFY] [app/build.gradle.kts](file:///E:/AndroidStudioProjects/MorNote/app/build.gradle.kts)
- Add `libs.firebase.functions.ktx` to dependencies.

### 2. Data & Settings Layer

#### [NEW] [AiSettingsRepository.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/data/AiSettingsRepository.kt)
- Create a DataStore-backed repository to store AI Mode (Private/Cloud) and Provider (OpenAI/Gemini/Claude).

#### [MODIFY] [MorphedNotesApp.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/MorphedNotesApp.kt)
- Initialize `AiSettingsRepository`.

#### [MODIFY] [AppViewModelFactory.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/AppViewModelFactory.kt)
- Update factory to provide `AiSettingsRepository` to ViewModels.

### 3. AI Vault UI (Milestone 1)

#### [MODIFY] [AiVaultBottomSheet.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/ui/detail/AiVaultBottomSheet.kt)
- Redesign to match the requested UI.
- Update `AiAction` to include `GrammarFix` and `Translate`.
- Implement the branded security confirmation screen ("🔒 AI Vault").

#### [MODIFY] [NoteDetailScreen.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/ui/detail/NoteDetailScreen.kt)
- Replace FAB with a "✨ AI Vault" text button at the bottom of the note content.
- Update the AI Result UI to a more sophisticated dialog/screen with "Replace Note", "Copy", and "Insert Below" options (Milestone 4).

### 4. Settings UI (Milestone 2)

#### [MODIFY] [SettingsScreen.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/ui/settings/SettingsScreen.kt)
- Add "AI Vault" section with Mode and Provider selection.

#### [MODIFY] [SettingsViewModel.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/ui/settings/SettingsViewModel.kt)
- Add logic to read/write AI settings.

### 5. Backend & AI Logic (Milestone 3)

#### [MODIFY] [NoteDetailViewModel.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/NoteDetailViewModel.kt)
- Update `performAiAction` to check AI settings and route request to either `AiAssistantManager` (Private) or a new `BackendAiManager` (Cloud).

#### [NEW] [BackendAiManager.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/data/BackendAiManager.kt)
- Implement calls to Firebase Cloud Functions for AI processing.

## Verification Plan

### Automated Tests
- Unit tests for `AiSettingsRepository`.
- Unit tests for `NoteDetailViewModel` action routing logic.

### Manual Verification
- Deploy the app and verify the "AI Vault" button appears at the bottom of a note.
- Verify the redesigned Bottom Sheet and security confirmation.
- Change AI settings in the Settings screen and verify they persist.
- Trigger an AI action and verify the new AI Result UI appears with correct options.
- Verify "Replace Note", "Copy", and "Insert Below" work as expected.

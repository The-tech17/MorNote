# Task: Build AI Vault

## Task 1: AI Vault UI & Infrastructure
- [x] Redesign `AiVaultBottomSheet.kt` with "🔒 AI Vault" branded security screen
- [x] Update `AiAction` to include `GrammarFix` and `Translate`
- [x] Create `AiSettingsRepository` for AI configuration persistence
- [x] Add AI configuration options to `SettingsScreen.kt` and `SettingsViewModel.kt`
- [x] Update `NoteDetailScreen.kt` with new "✨ AI Vault" entry point and sophisticated AI Result dialog

## Task 2: Backend Integration logic
- [x] Create `BackendAiManager` to handle Cloud Function calls
- [x] Update `NoteDetailViewModel` to route actions based on user settings
- [x] Implement generic error handling for AI failures (via try-catch in managers)

## Task 3: Refinement & Verification
- [x] Verify "Replace", "Copy", and "Insert Below" functionality
- [x] Test AI Mode switching (Private vs Cloud)
- [x] Final UI Polish

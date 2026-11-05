# Fix warnings in NoteRepository.kt and Converters.kt

The goal is to address unused function warnings reported by static analysis in `NoteRepository.kt` and `Converters.kt`.

## Proposed Changes

### [app]

#### [MODIFY] [NoteRepository.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/NoteRepository.kt)
- Remove the unused function `updateTags`.

#### [MODIFY] [Converters.kt](file:///E:/AndroidStudioProjects/MorNote/app/src/main/java/com/sanjana/mornote/Converters.kt)
- Suppress "unused" warnings for `fromList` and `toList` using `@Suppress("unused")`. These functions are used by Room's annotation processor, but static analysis tools may flag them as unused in code.

## Verification Plan

### Automated Tests
- Run a build to ensure no regression.
- Since these are just warning fixes (one removal of dead code, one suppression of false positive), functional tests should still pass.

### Manual Verification
- Verify that the warnings are gone in the IDE.

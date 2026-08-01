# Large Refactor Work Package — 0.7.0

## Delivered
- Android `EditorViewModel` with lifecycle-aware `StateFlow` for navigation, search and paging state.
- Activity recreation no longer resets Var/Object queries or pages.
- Var/Object search limits raised from 50 to 5,000 before paging, removing misleading truncated totals.
- Shared `NamedBrowser` search/paging engine.
- Search and pagination added to backups, export history and crash logs.
- Pure `ExportPayload`/`ExportKind` model with safe file-name normalization and empty-output rejection.
- Module/rule consistency from 0.6 retained; only VERIFIED rules are writable.
- Version bumped to 0.7.0-large-refactor.

## Verification
- Pure Kotlin compilation/tests for Paging, NamedBrowser, ExportPayload, module consistency and reducer.
- Structural checks for ViewModel wiring, lifecycle collection and no INTERNET permission.

## Remaining external blocker
A real APK still requires Android SDK plus `gradle-wrapper.jar`; neither exists in this execution environment.

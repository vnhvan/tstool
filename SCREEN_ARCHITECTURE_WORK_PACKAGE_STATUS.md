# Screen Architecture Work Package — v0.9.0

## Completed

- Reduced `MainActivity.kt` from 481 lines to 60 lines.
- Added `OfflineEditorApp` scaffold with five real screens: Editor, Analyzer, Backup, Restore, Settings.
- Added bottom navigation driven by `EditorSession.screen`.
- Added `DocumentLaunchers` coordinator for open, compare and three export MIME types.
- All ContentResolver reads/writes in the coordinator run on `Dispatchers.IO`.
- Added `WorkspaceState` and moved backup/history/crash search + paging state into the ViewModel reducer.
- Editor screen keeps verified Sound Volume edit, Var browser and Object browser.
- Analyzer screen keeps diagnostics, analysis export, save comparison and full diff export.
- Backup screen keeps create/open/inspect/export/delete, history search and pagination.
- Restore screen keeps verified staging, restore-ready export, root read-only probe and dynamic module catalog.
- Settings screen keeps backup capacity, candidate visibility and root-probe opt-in.
- Crash logs remain searchable, pageable and individually exportable from the app shell.

## Safety retained

- No INTERNET permission.
- Only the verified Sound Volume rule is writable.
- Coin, TCash and Factory Slots remain non-writable.
- Root access remains read-only and disabled by default.
- File input remains bounded to 64 MB.
- Newer async operations still invalidate older results.

## Verification completed in this environment

- Pure Kotlin reducer/concurrency/bounded-input checks: PASS.
- Screen architecture structural checks: PASS.
- MainActivity line count: 60.
- Manifest Internet-permission check: PASS.

## Environment limitation

An APK was not built because this environment still lacks Android SDK and `gradle-wrapper.jar`. Android compilation and LDPlayer runtime testing remain required.

# v0.12.0 — Edit Preview & Repository Work Package

## Completed

- Added Android-free `BackupRepository`, `HistoryRepository`, and `RestoreRepository` contracts.
- Updated `EditorViewModel` to depend on repository interfaces rather than concrete Android stores.
- Kept `BackupStore`, `HistoryStore`, and `RestoreStagingStore` as production adapters.
- Added a two-step verified-edit workflow: prepare/verify, review diff and risk, then confirm export.
- Added discard/reset for an edit that has not been exported.
- Added `EditRiskAssessor` with NORMAL, CAUTION, and HIGH classifications.
- Added detailed edit preview UI showing the exact Var/Object diff.
- Added streaming SHA-256 functions for `InputStream` and `File`.
- Removed full-file `readBytes()` hashing from backup listing and post-write verification.
- Added tests for risk assessment, streaming hash parity, and fake repository usage.

## Safety state

Only VERIFIED edit rules can reach `SafeEditEngine`. Coin, TCash, Factory Slot, and blocked modules remain non-writable.

## Build limitation

Source checks pass, but an APK was not built because Android SDK and `gradle-wrapper.jar` are unavailable in this environment.

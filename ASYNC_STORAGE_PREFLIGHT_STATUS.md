# v0.11.0 Async Storage & Module Preflight

## Implemented
- Backup create, inspect, open, export and delete run through cancellable ViewModel operations.
- Restore staging create, verify/export and clear run through cancellable ViewModel operations.
- Root read-only probe is owned by ViewModel state and runs on Dispatchers.IO.
- Backup, persisted history, staging metadata, inspection and root results live in StorageState.
- Export lifecycle tracks READY, WRITING, COMPLETED, CANCELLED and FAILED.
- Android document cancellation is reported without creating history.
- Module preflight checks variable presence, current value, source encodability and confidence before enabling edits.
- Candidate and blocked modules show their lock reason and current variable value when present.
- Project Doctor paths were updated for the split-screen architecture.

## Safety retained
- No INTERNET permission.
- Only VERIFIED edit rules can write.
- Coin, TCash and Factory Slot remain locked.
- Backup and restore SHA/container verification remain enabled.
- Operations use LatestOperationGate and can be cancelled.
- File input remains limited to 64 MiB.

## Verification
- Pure Kotlin checks: PASS.
- Structural checks: PASS_V011_STRUCTURE.
- Project Doctor passes all source checks; Android SDK and gradle-wrapper.jar remain unavailable in this environment.

## Build status
No APK was built because Android SDK and gradle-wrapper.jar are unavailable.

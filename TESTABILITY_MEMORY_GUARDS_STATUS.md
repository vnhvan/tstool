# v0.13.0 Testability & Memory Guards

## Added
- Complete fake BackupRepository and RestoreRepository contract tests.
- SaveMemoryProfile for explicit retained container/XML memory accounting.
- Editor UI memory visibility and warning when both retained buffers are >= 8 MiB.
- Project Doctor checks for repository abstractions, fake tests, and memory visibility.
- Version 0.13.0-testability-memory-guards.

## Safety retained
- Only VERIFIED edit rules are writable.
- Two-step diff preview remains mandatory.
- Storage and parsing remain cancellable/background operations.
- No INTERNET permission.

## Environment limitation
- Android SDK is unavailable in this environment.
- gradle-wrapper.jar could not be fetched because outbound DNS/network access is unavailable.
- Therefore no APK/runtime Android claim is made.

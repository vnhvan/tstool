# Async UI Work Package — v0.8.0

## Completed

- Moved opened document, diagnostics and comparison diff into `EditorViewModel` state.
- Added `viewModelScope` processing for decode, analysis, diagnostics and save comparison.
- Added latest-operation-wins generation gate so stale asynchronous results cannot overwrite a newer document.
- Added explicit cancellation and busy UI with an operation label.
- Moved document reads to `Dispatchers.IO`.
- Added bounded streaming input (`LimitedInput`) with a 64 MB hard limit and 32 KiB buffer.
- Added a second 64 MB guard in `SaveRepository`.
- Extracted reusable paging and storage row composables from `MainActivity`.
- Added unit tests for operation gating and bounded input.

## Safety properties

- Starting a newer open/compare/diagnostics operation cancels the previous job.
- A canceled or stale job cannot publish its result.
- Empty and oversized files are rejected before decode.
- Coin, TCash and Factory Slot remain non-writable.
- No Internet permission was introduced.

## Verification performed in this environment

- `PASS_ASYNC_IO_GUARDS`
- `PASS_EDITOR_SESSION`
- `PASS_MAIN_STRUCTURE`
- MainActivity braces and parentheses are balanced.
- No unbounded `InputStream.readBytes()` remains in MainActivity.

## Environment limitation

The Android SDK and Gradle wrapper JAR are still unavailable here, so an actual Android APK build and emulator instrumentation run have not been completed.

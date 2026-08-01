# Township Offline Save Editor

Current source version: **0.9.0-screen-architecture**.

The app is an offline clean-room save editor foundation with verified codec round-trip, analysis, diagnostics, backup/history, safe edit rules, restore staging, crash logging, async cancellation and five separated Compose screens. It requests no Internet permission.

See `SCREEN_ARCHITECTURE_WORK_PACKAGE_STATUS.md` for the latest work package.

# Township Offline Save Editor — Integration Work Package

Offline Android source for analyzing, comparing, backing up, safely editing and staging Township `mGameInfo` saves.

## Current workflow

1. Open binary `mGameInfo` or decoded XML.
2. Validate container and XML structure.
3. Analyze Vars and Objects.
4. Search Vars/Objects or compare two saves.
5. Create verified internal/external backup.
6. Run diagnostics.
7. Apply a VERIFIED edit through Safe Edit Engine.
8. Encode and reopen the result.
9. Create restore staging.
10. Export a restore-ready copy after SHA-256 and size verification.

## Module policy

- Sound Volume — VERIFIED and writable.
- Coin — CANDIDATE/read-only.
- TCash — CANDIDATE/read-only.
- Cow Factory Slots — CANDIDATE/read-only.
- Barn/Warehouse — BLOCKED.

A candidate rule cannot be used unless code explicitly opts into experimental mode. The production UI does not do this.

## Build

Open this directory in Android Studio with JDK 17, install Android SDK 35, sync Gradle, run unit tests, then build the debug APK.

Expected APK path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The included wrapper properties are present, but `gradle-wrapper.jar` is not available in the current build environment.

## LDPlayer safety

Force-stop Township and copy the entire saves directory before replacing any file. The app intentionally does not write directly into `/data/data/com.playrix.township.vn/saves` yet.

## Architecture work package 0.5.0

- ModuleCatalog tập trung, kiểm tra ID trùng và chỉ trả module VERIFIED cho luồng ghi.
- AppScreen model chuẩn bị tách giao diện thành Editor/Analyzer/Backup/Restore/Settings.
- Crash logs có thể xuất từng file ngoại tuyến với kiểm tra đường dẫn.
- ExportNames chuẩn hóa tên file xuất.

## v0.8.0 async/UI hardening

The editor now processes decode, diagnostics and save comparisons through `EditorViewModel` on background dispatchers. File reads are bounded to 64 MB, stale asynchronous operations are discarded, and the user can cancel an active operation. See `ASYNC_UI_WORK_PACKAGE_STATUS.md`.

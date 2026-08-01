# Build attempt and feature-parity assessment — v0.13.1

## Build attempt in the current environment

The project source was inspected again on 2026-07-30.

Detected:
- Java 21 is installed; project targets Java/Kotlin 17.
- No Android SDK, `android.jar`, `sdkmanager`, `adb`, or `aapt2` is installed.
- No system Gradle installation is present.
- `gradle-wrapper.properties` targets Gradle 8.9, but `gradle-wrapper.jar` is absent.
- No cached Android Gradle Plugin or AndroidX artifacts are available.

Result: no real APK was produced in this container. This is an environment limitation, not proof that the source compiles successfully.

## New reproducible build path

Added `.github/workflows/android-build.yml`:
- JDK 17
- Android SDK 35 / Build Tools 35.0.0
- Gradle 8.9 without relying on the missing wrapper JAR
- `testDebugUnitTest`
- `assembleDebug`
- uploads the real `app-debug.apk` only if the build succeeds

Added `scripts/build_local.sh` for Android Studio or a machine with Android SDK and Gradle 8.9.

## Feature parity with Chuck's Tool v4.27

### Implemented or substantially equivalent
- Offline opening of binary `mGameInfo` and decoded XML.
- Verified 0x79 codec and LZ4/XML payload handling.
- Read-only Var/Object analysis and search.
- Semantic comparison of two saves.
- Diagnostics and report export.
- Internal backup, duplicate detection, inspection, export and deletion.
- Restore staging and verification.
- Edit-rule registry, module status and preflight.
- Two-step change preview, risk warning, round-trip verification and export history.
- Offline architecture with no INTERNET permission.

### Partially equivalent
- Module UI/catalog: architecture exists, but only verified rules appear as writable.
- Save write pipeline: implemented for verified `mGameInfo` rules, but still needs an actual Android build and device writeback validation for this exact source revision.
- Root handling: read-only probe only; no automatic direct replacement in Township data.

### Not yet equivalent
- Most original feature modules: Barn/Warehouse, Coin, TCash, Helicopter, mining groups, decorations, skins, stickers, profile catalogs, Zoo, Regatta, Perk, Booster, Airport, Community, Expansion, Like and Golden Ticket.
- `LocalInfo` AES processing.
- PLXE processing for `GlobalVars` and `StartupConsts`.
- Original online authentication, entitlement, encrypted secure tables, native anti-tamper and heartbeat. These are intentionally excluded from the clean-room offline application.
- Original app's full catalog and server-dependent behavior.

### Writable modules in this build
- Sound Volume: VERIFIED and writable.
- Coin: candidate/read-only.
- TCash: candidate/read-only.
- Cow Factory Slots: candidate/read-only.
- Barn/Warehouse: blocked.

## Honest conclusion

The new application is not yet feature-equivalent to the original APK. It is currently a strong offline save-inspection and safety platform with one verified writable module. The next major milestone is obtaining a real CI/Android Studio build, installing it, opening the provided fixture saves, exporting a Sound edit, and confirming Township accepts the result. Only after that should additional modules be enabled one by one from isolated before/after fixtures.

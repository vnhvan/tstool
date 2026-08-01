# Hardening Work Package

## Added
- Full Settings UI for backup retention, Candidate module visibility and read-only root probe gate.
- Reusable paging engine for large Var/Object result sets.
- Offline crash reporter retaining at most 10 local stack traces.
- Custom Application registration for crash logger initialization.
- Root probe disabled by default and controlled by persisted settings.
- Candidate modules can be hidden without changing registry data.
- Project Doctor script checks JDK, Android SDK, Gradle wrapper and accidental INTERNET permission.
- Version bumped to `0.4.0-hardening` (`versionCode=30`).

## Verification performed
- Existing integration regression jar: PASS (`diag=0/1/11`, `diff=1`, `vars=1299`).
- Paging engine compiled with Kotlin/JVM 1.9.0.
- Pagination boundary behavior checked for 53 records / 20 per page.
- Manifest verified to contain no INTERNET permission.
- Application crash reporter registration verified.

## Known build blocker
The source archive intentionally does not claim a successful Android APK build. This environment has no Android SDK and the inherited project lacks `gradle/wrapper/gradle-wrapper.jar`. Open with Android Studio or regenerate the wrapper before building.

## Safety boundary
- No direct game-folder write.
- No root write/delete operation.
- Coin, TCash and Factory candidate writes remain locked.
- Restore is staging/export only and remains SHA-256 verified.

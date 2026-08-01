# Phase 3 build status

## Completed

- Kotlin codec retained and hardened with container-size checks.
- Plain decoded XML is accepted in read-only mode.
- XML variable parser now tolerates attribute order and whitespace differences.
- Golden fixtures are included from files already accepted by Township on LDPlayer.
- Unit tests cover original decode, sound=100 decode, XML-level round trip, known round-trip equality, and truncated input rejection.
- Java/Kotlin target standardized at 17 for Android Gradle Plugin 8.7.3.
- Gradle Wrapper properties and launcher scripts added.

## Remaining build prerequisite

`gradle-wrapper.jar` and Android SDK dependencies cannot be downloaded in the current offline execution environment. Android Studio can sync the project and obtain these dependencies on a connected development machine.

## Recommended build

1. Open this folder in Android Studio.
2. Select JDK 17 in Gradle settings.
3. Allow Gradle sync.
4. Run `testDebugUnitTest`.
5. Build `app-debug.apk`.

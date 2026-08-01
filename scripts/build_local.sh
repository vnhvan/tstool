#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is missing. Install JDK 17." >&2; exit 2
fi
JAVA_MAJOR="$(java -version 2>&1 | awk -F'[\".]' '/version/ {print $2; exit}')"
if [[ "$JAVA_MAJOR" != "17" ]]; then
  echo "WARNING: JDK 17 is recommended; detected Java $JAVA_MAJOR." >&2
fi
if [[ -z "${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}" ]]; then
  echo "ERROR: ANDROID_HOME or ANDROID_SDK_ROOT is not set." >&2; exit 3
fi
if command -v gradle >/dev/null 2>&1; then
  GRADLE=(gradle)
elif [[ -f gradle/wrapper/gradle-wrapper.jar ]]; then
  chmod +x ./gradlew
  GRADLE=(./gradlew)
else
  echo "ERROR: Gradle 8.9 or gradle-wrapper.jar is required." >&2; exit 4
fi
"${GRADLE[@]}" --no-daemon testDebugUnitTest assembleDebug
APK="app/build/outputs/apk/debug/app-debug.apk"
[[ -f "$APK" ]] || { echo "ERROR: APK was not produced." >&2; exit 5; }
sha256sum "$APK" | tee "$APK.sha256"
echo "APK: $ROOT/$APK"

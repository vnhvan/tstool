#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
fail=0

check_absent() {
  local pattern="$1" path="$2" label="$3" hits
  hits="$(mktemp)"
  if grep -R -n --include='*.kt' -E "$pattern" "$ROOT/$path" >"$hits" 2>/dev/null; then
    echo "[FAIL] $label"; cat "$hits"; fail=1
  else
    echo "[OK] $label"
  fi
  rm -f "$hits"
}

check_present() {
  local pattern="$1" file="$2" label="$3"
  if grep -q -E "$pattern" "$ROOT/$file"; then echo "[OK] $label"; else echo "[FAIL] $label"; fail=1; fi
}

check_absent 'document\.source([^K]|$)' 'app/src' 'No obsolete SaveDocument.source references'
check_absent 'document\.coin([^A-Za-z0-9_]|$)' 'app/src' 'No invalid SaveDocument.coin references'
check_absent 'org\.junit\.Assert\.assertFailsWith' 'app/src/test' 'No invalid JUnit assertFailsWith imports'
check_absent 'Chọn mGameInfo\.xml' 'app/src/main/java/com/offline/saveeditor/ui/screens' 'Coin screen does not require file picker'
check_absent 'attachRootSaveAccess|prepareExperimentalCoin' 'app/src/main/java' 'Old Coin workspace flow is disconnected'
check_present 'android:process=":coin_worker"' 'app/src/main/AndroidManifest.xml' 'Coin worker uses isolated Android process'
check_present 'CoinWorkerClient' 'app/src/main/java/com/offline/saveeditor/state/EditorViewModel.kt' 'Coin worker connected to ViewModel'
check_present 'CoinXmlBytes' 'app/src/main/java/com/offline/saveeditor/coin/CoinFileProcessor.kt' 'Coin uses byte-level XML field access'
check_present 'copyTownshipSaveTo' 'app/src/main/java/com/offline/saveeditor/root/RootSaveAccess.kt' 'Root file copy avoids stdout save transfer'
check_present 'replaceTownshipSaveFrom' 'app/src/main/java/com/offline/saveeditor/root/RootSaveAccess.kt' 'Atomic verified root replacement available'
check_present 'withTimeout\(45_000\)' 'app/src/main/java/com/offline/saveeditor/state/EditorViewModel.kt' 'Coin worker read timeout enabled'
check_present 'AppScreen.START_DATE -> StartDateScreen' 'app/src/main/java/com/offline/saveeditor/ui/AppShell.kt' 'Start-date screen connected'
check_present 'android:process=":start_date_worker"' 'app/src/main/AndroidManifest.xml' 'gameStartDate worker uses isolated process'
check_present 'gameStartDate' 'app/src/main/java/com/offline/saveeditor/startdate/StartDateXmlBytes.kt' 'gameStartDate byte field access present'
check_present 'attachStartDateWorker' 'app/src/main/java/com/offline/saveeditor/MainActivity.kt' 'Start-date worker attached'
check_present 'Asia/Bangkok' 'app/src/main/java/com/offline/saveeditor/startdate/StartDateTime.kt' 'Month/year conversion uses UTC+7'

exit "$fail"

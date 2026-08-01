#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
fail=0

check_absent() {
  local pattern="$1"
  local path="$2"
  local label="$3"
  local hits
  hits="$(mktemp)"
  if grep -R -n --include='*.kt' -E "$pattern" "$ROOT/$path" >"$hits" 2>/dev/null; then
    echo "[FAIL] $label"
    cat "$hits"
    fail=1
  else
    echo "[OK] $label"
  fi
  rm -f "$hits"
}

check_present() {
  local pattern="$1"
  local file="$2"
  local label="$3"
  if grep -q -E "$pattern" "$ROOT/$file"; then
    echo "[OK] $label"
  else
    echo "[FAIL] $label"
    fail=1
  fi
}

check_absent 'document\.source([^K]|$)' 'app/src' 'No obsolete SaveDocument.source references'
check_absent 'document\.coin([^A-Za-z0-9_]|$)' 'app/src' 'No invalid SaveDocument.coin references'
check_absent 'org\.junit\.Assert\.assertFailsWith' 'app/src/test' 'No invalid JUnit assertFailsWith imports'
check_absent 'Chọn mGameInfo\.xml' 'app/src/main/java/com/offline/saveeditor/ui/screens' 'Coin screen does not require file picker'
check_present 'RootSaveAccess' 'app/src/main/java/com/offline/saveeditor/state/EditorViewModel.kt' 'Direct root access connected to EditorViewModel'
check_present 'document\.fields\.coin' 'app/src/main/java/com/offline/saveeditor/state/EditorViewModel.kt' 'Coin verification uses SaveFields contract'

exit "$fail"

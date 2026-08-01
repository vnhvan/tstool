#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
fail=0
check_absent() {
  local pattern="$1"; local path="$2"; local label="$3"
  if grep -R -n --include='*.kt' "$pattern" "$ROOT/$path" >/tmp/source_contract_hits 2>/dev/null; then
    echo "[FAIL] $label"
    cat /tmp/source_contract_hits
    fail=1
  else
    echo "[OK] $label"
  fi
}
check_absent 'document\.source\([^K]\|$\)' 'app/src' 'No obsolete SaveDocument.source references'
check_absent 'org\.junit\.Assert\.assertFailsWith' 'app/src/test' 'No invalid JUnit assertFailsWith imports'
exit "$fail"

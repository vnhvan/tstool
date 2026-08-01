#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${TMPDIR:-/tmp}/township_offline_checks"
rm -rf "$OUT" && mkdir -p "$OUT/stubs/com/offline/saveeditor/model" "$OUT/stubs/com/offline/saveeditor/diagnostics" "$OUT/stubs/com/offline/saveeditor/analysis" "$OUT/stubs/com/offline/saveeditor/export" "$OUT/stubs/com/offline/saveeditor/state" "$OUT/stubs/com/offline/saveeditor/edit"
cat > "$OUT/stubs/com/offline/saveeditor/model/SaveDocument.kt" <<'STUB'
package com.offline.saveeditor.model
class SaveDocument
STUB
cat > "$OUT/stubs/com/offline/saveeditor/diagnostics/DiagnosticsReport.kt" <<'STUB'
package com.offline.saveeditor.diagnostics
class DiagnosticsReport
STUB
cat > "$OUT/stubs/com/offline/saveeditor/analysis/SaveDiff.kt" <<'STUB'
package com.offline.saveeditor.analysis
class SaveDiff
STUB
cat > "$OUT/stubs/com/offline/saveeditor/export/ExportPayload.kt" <<'STUB'
package com.offline.saveeditor.export
class ExportPayload(val safeName: String = "test.bin")
STUB
cat > "$OUT/stubs/com/offline/saveeditor/edit/PendingEditPreview.kt" <<'STUB'
package com.offline.saveeditor.edit
class PendingEditPreview
STUB
cat > "$OUT/stubs/com/offline/saveeditor/state/StorageState.kt" <<'STUB'
package com.offline.saveeditor.state
enum class ExportPhase { IDLE, READY, WRITING, COMPLETED, CANCELLED, FAILED }
data class StorageState(val exportPhase: ExportPhase = ExportPhase.IDLE, val lastExportName: String? = null)
STUB
cat > "$OUT/Check.kt" <<'CHECK'
import com.offline.saveeditor.concurrency.LatestOperationGate
import com.offline.saveeditor.io.LimitedInput
import com.offline.saveeditor.state.*
import java.io.ByteArrayInputStream
fun main() {
  val gate=LatestOperationGate(); val first=gate.begin(); val second=gate.begin()
  check(!gate.isCurrent(first) && gate.isCurrent(second)); gate.invalidate(); check(!gate.isCurrent(second))
  val bytes=ByteArray(8193){(it%251).toByte()}; check(LimitedInput.read(ByteArrayInputStream(bytes),9000).contentEquals(bytes))
  check(runCatching { LimitedInput.read(ByteArrayInputStream(ByteArray(11)),10) }.isFailure)
  val state=EditorReducer.reduce(EditorSession(varPage=5), EditorAction.SearchVars("coin")); check(state.varPage==0)
  val busy=EditorReducer.reduce(state, EditorAction.Busy(true,"work")); check(busy.busy && busy.operationLabel=="work")
  val backupSearch=EditorReducer.reduce(state, EditorAction.SearchBackups("abc")); check(backupSearch.workspace.backup.query=="abc" && backupSearch.workspace.backup.page==0)
  val moved=EditorReducer.reduce(backupSearch, EditorAction.BackupPage(4)); check(moved.workspace.backup.page==4)
  val rule=com.offline.saveeditor.edit.EditRule("sound","Sound","soundVolume",com.offline.saveeditor.edit.EditConfidence.VERIFIED,0,100)
  check(com.offline.saveeditor.edit.EditRiskAssessor.assess(5,95,rule).level==com.offline.saveeditor.edit.EditRiskLevel.HIGH)
  val hashBytes=ByteArray(200000){(it%251).toByte()}
  check(com.offline.saveeditor.util.sha256(hashBytes)==com.offline.saveeditor.util.sha256(ByteArrayInputStream(hashBytes),4096))
  println("Pure Kotlin checks: PASS")
}
CHECK
kotlinc \
  "$OUT/stubs/com/offline/saveeditor/model/SaveDocument.kt" \
  "$OUT/stubs/com/offline/saveeditor/diagnostics/DiagnosticsReport.kt" \
  "$OUT/stubs/com/offline/saveeditor/analysis/SaveDiff.kt" \
  "$OUT/stubs/com/offline/saveeditor/export/ExportPayload.kt" \
  "$OUT/stubs/com/offline/saveeditor/edit/PendingEditPreview.kt" \
  "$OUT/stubs/com/offline/saveeditor/state/StorageState.kt" \
  "$ROOT/app/src/main/java/com/offline/saveeditor/concurrency/LatestOperationGate.kt" \
  "$ROOT/app/src/main/java/com/offline/saveeditor/io/LimitedInput.kt" \
  "$ROOT/app/src/main/java/com/offline/saveeditor/edit/EditRule.kt" \
  "$ROOT/app/src/main/java/com/offline/saveeditor/edit/EditRisk.kt" \
  "$ROOT/app/src/main/java/com/offline/saveeditor/util/Hashing.kt" \
  "$ROOT/app/src/main/java/com/offline/saveeditor/navigation/AppScreen.kt" \
  "$ROOT/app/src/main/java/com/offline/saveeditor/state/WorkspaceState.kt" \
  "$ROOT/app/src/main/java/com/offline/saveeditor/state/EditorSession.kt" \
  "$OUT/Check.kt" -include-runtime -d "$OUT/check.jar"
java -jar "$OUT/check.jar"

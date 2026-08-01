#!/usr/bin/env bash
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
fail=0
ok(){ echo "[OK] $1"; }
miss(){ echo "[MISSING] $1"; fail=1; }
check_file(){ [ -f "$ROOT/$1" ] && ok "$2" || miss "$2"; }
check_text(){ grep -Rqs "$2" "$ROOT/$1" && ok "$3" || miss "$3"; }
command -v java >/dev/null 2>&1 && ok Java || miss Java
command -v javac >/dev/null 2>&1 && ok Javac || miss Javac
check_file gradle/wrapper/gradle-wrapper.properties "gradle-wrapper.properties"
check_file gradle/wrapper/gradle-wrapper.jar "gradle-wrapper.jar"
if [ -n "${ANDROID_HOME:-}" ] && [ -d "$ANDROID_HOME" ]; then ok "ANDROID_HOME=$ANDROID_HOME"; else miss "Android SDK / ANDROID_HOME"; fi
MANIFEST="$ROOT/app/src/main/AndroidManifest.xml"
grep -q 'android.permission.INTERNET' "$MANIFEST" && { echo '[FAIL] INTERNET permission exists'; fail=1; } || ok "No INTERNET permission"
check_text app/src/main/AndroidManifest.xml 'OfflineSaveEditorApp' "Crash reporter Application registered"
check_text app/src/main/java/com/offline/saveeditor/MainActivity.kt 'collectAsStateWithLifecycle' "Lifecycle-aware state collection"
check_text app/src/main/java/com/offline/saveeditor/ui/screens 'NamedBrowser.browse' "Backup/history pagination"
check_text app/src/main/java/com/offline/saveeditor/ui/AppShell.kt 'NamedBrowser.browse' "Crash pagination"
check_text app/src/main/java/com/offline/saveeditor/ui/launcher/DocumentLaunchers.kt 'LimitedInput::read' "Bounded stream input"
check_text app/src/main/java/com/offline/saveeditor/ui/AppShell.kt 'BusyPanel' "Cancellable busy UI"
check_text app/src/main/java/com/offline/saveeditor/state/EditorViewModel.kt 'createBackup' "Async backup operations"
check_text app/src/main/java/com/offline/saveeditor/state/EditorViewModel.kt 'createRestoreStage' "Async restore operations"
check_text app/src/main/java/com/offline/saveeditor/state/EditorViewModel.kt 'runRootProbe' "Async root probe"
check_file app/src/main/java/com/offline/saveeditor/edit/ModulePreflight.kt "Module preflight"
check_text app/src/main/java/com/offline/saveeditor/ui/launcher/DocumentLaunchers.kt 'onExportCancelled' "Export cancellation state"
check_file app/src/main/java/com/offline/saveeditor/storage/StorageRepositories.kt "Storage repository contracts"
check_file app/src/main/java/com/offline/saveeditor/edit/EditRisk.kt "Edit risk assessor"
check_file app/src/main/java/com/offline/saveeditor/ui/components/EditPreviewCard.kt "Edit diff preview UI"
check_text app/src/main/java/com/offline/saveeditor/storage/BackupStore.kt 'sha256(temporary)' "Streaming backup verification"
check_text app/src/main/java/com/offline/saveeditor/state/EditorViewModel.kt 'confirmPendingEditExport' "Two-step edit confirmation"
grep -Rqs "interface BackupRepository" "$ROOT/app/src/main/java" && echo "[OK] Backup repository abstraction" || echo "[MISSING] Backup repository abstraction"
grep -Rqs "SaveMemoryProfile" "$ROOT/app/src/main/java" && echo "[OK] Document memory visibility" || echo "[MISSING] Document memory visibility"
grep -Rqs "RepositoryFakesTest" "$ROOT/app/src/test" && echo "[OK] Backup/restore fake repository tests" || echo "[MISSING] Backup/restore fake repository tests"
exit $fail

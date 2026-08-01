# Integration Work Package Status

## Completed in this package

- Safe Edit Engine shared by every editor module.
- Explicit confidence gates: VERIFIED, CANDIDATE, BLOCKED.
- Candidate modules are read-only by default.
- A prepared edit must change exactly the expected Var and no Object.
- Encoded output is reopened and the edited value is checked again.
- Diagnostics Engine combines container, structure, analyzer, Knowledge Base and module status checks.
- Diagnostics TXT export.
- Restore staging integrated into the Compose UI.
- Restore-ready export only after byte-for-byte verification.
- Read-only root probe for `/data/data/com.playrix.township.vn/saves` with timeout.
- Persistent application settings foundation.
- Main Sound editor now uses Safe Edit Engine instead of a one-off path.

## Verified locally

Using the real decoded save fixture:

- 1,299 Var entries detected.
- Diagnostics: 0 errors, 1 warning, 11 informational entries.
- Sound edit produces exactly one Var diff and zero Object diffs.
- Coin editing is rejected by default because it is still CANDIDATE.
- Restore verification accepts an identical copy.
- New root/settings classes compile with Android stubs.

Output:

```text
PASS integration work package
diag=0/1/11, diff=1, vars=1299
PASS root/settings syntax
```

## Safety boundaries

- No INTERNET permission.
- Root feature is detection/listing only.
- No direct write to Township save directory.
- Coin, TCash and Factory Slot remain locked for writing.
- Only Sound Volume is VERIFIED.

## Not verified in this environment

- Full Android Gradle build.
- APK installation and Compose rendering on LDPlayer.
- Real `su` behavior on LDPlayer.
- Direct restore into the Township package directory.

# Phase 5 status

## Added

- Variable browser for every `<Var>` entry in decoded XML.
- Case-insensitive search by variable name or value.
- Result limiting to prevent UI stalls on large saves.
- Module registry with VERIFIED / READ_ONLY / EXPERIMENTAL / BLOCKED states.
- In-memory edit-history model with bounded retention.
- Unit tests for variable enumeration/search and history capacity.
- Existing safe flow retained: backup, preview, encode, decode verification, export.

## Current module states

- Sound Volume: VERIFIED.
- Coin: READ_ONLY.
- TCash: READ_ONLY.
- Factory Slot: EXPERIMENTAL.
- Barn/Warehouse: BLOCKED pending integrity analysis.

## Build limitation

The project still requires Android Studio/Android SDK and dependency resolution to produce an APK. The source is structured for direct Android Studio import with JDK 17.

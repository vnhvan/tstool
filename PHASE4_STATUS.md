# Phase 4 status

## Verified

- The Python codec round-trips all eight original samples.
- Township accepted both a no-change round-trip file and a Sound Volume 100 file on LDPlayer.
- Kotlin parser reads the verified fixtures and locates Coin, TCash and Sound Volume.

## Implemented in source

- Offline file picker.
- Kotlin mGameInfo codec.
- Structure validation.
- Backup export.
- Editable Sound Volume with range checks.
- Change preview and report.
- Encode → decode → byte-equality verification before export.

## Not yet verified here

- Full Android Gradle build, because this runtime has no Android SDK or dependency cache.
- Direct root access to the game's save directory.
- Coin/TCash editing semantics.
- Barn/Warehouse integrity processors.

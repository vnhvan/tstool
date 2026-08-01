# Architecture Work Package

## Added
- Central `ModuleCatalog` with duplicate-id validation, visibility filtering and writable-module discovery.
- App screen model for the next navigation refactor.
- Per-file offline crash-log export with path validation.
- Export filename sanitizer foundation.
- Version 0.5.0-architecture.
- Unit tests for module registration and screen fallback.

## Safety
Only VERIFIED modules are returned by `writable()`. Coin, TCash and Factory Slot remain non-writable.

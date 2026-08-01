# Changelog

## v0.1.3
- Thêm `ContainerInspector` để phân loại và kiểm tra file trước khi decode.
- Phát hiện file rỗng, XML bị cắt, sai magic 0x79, header ngắn và payload vượt kích thước.
- Thêm `BackupInspection` với SHA, loại container và kết luận an toàn.
- Thêm nút Kiểm tra và Xuất lại cho từng backup.
- Tăng độ bền khi hoàn tất backup nếu `renameTo()` thất bại.
- Thêm unit test cho các trường hợp file lỗi.

## v0.1.2
- Tự động backup, chống trùng và giới hạn 25 backup.

## 0.4.0-hardening
- Added persisted Settings UI.
- Added reusable pagination for Var and Object browsers.
- Added offline crash logging with retention.
- Added root probe settings gate; disabled by default.
- Added Candidate module visibility control.
- Added Project Doctor build diagnostics.
- Registered custom Application and kept manifest without INTERNET permission.

## 0.7.0-large-refactor
- Added lifecycle-aware EditorViewModel.
- Added backup/history/crash search and pagination.
- Raised analyzer browser cap from 50 to 5,000 results.
- Added pure export payload validation and filename sanitization.

## 0.10.0-dynamic-editor

- Added registry-driven dynamic editor UI for VERIFIED edit rules.
- Moved safe edit + encode + reopen verification to cancellable ViewModel background work.
- Added pending-export reducer state.
- Added export audit metadata and automatic persistent edit history after successful output writes.
- Added reducer regression test for one-shot pending export consumption.

## 0.12.0-edit-preview-repositories
- Added repository interfaces for backup, history, and restore storage.
- Added two-step edit review/confirmation and discard/reset.
- Added edit risk assessment and exact diff preview.
- Added streaming SHA-256 for files and input streams.
- Removed repeated full-file reads for backup hash calculation.
- Added pure Kotlin regression checks for risk and streaming hashes.

## 0.13.1-build-ready-ci
- Added reproducible GitHub Actions Android build using JDK 17, SDK 35 and Gradle 8.9.
- Added local build preflight script with explicit SDK/Gradle checks and APK SHA-256 output.
- Added honest feature-parity and build-attempt assessment.

# Release checklist v0.1.3

## Build
- [ ] Android Studio dùng JDK 17.
- [ ] Gradle Sync thành công.
- [ ] `./gradlew testDebugUnitTest` thành công.
- [ ] `./gradlew assembleDebug` tạo `app-debug.apk`.

## LDPlayer
- [ ] Cài APK trên LDPlayer 9 / Android 9.
- [ ] Mở save gốc và kiểm tra Coin, TCash, Sound.
- [ ] Tạo backup nội bộ.
- [ ] Nút Kiểm tra báo HỢP LỆ.
- [ ] Xuất lại backup và so SHA-256 với nguồn.
- [ ] Thử file XML bị cắt; ứng dụng phải từ chối.
- [ ] Thử file sai header; ứng dụng phải từ chối.
- [ ] Tạo Sound 100, thay thủ công vào game và xác minh.

## Không được bật trong v0.1.3
- Ghi trực tiếp bằng root.
- Restore trực tiếp vào thư mục game.
- Ghi Coin/TCash khi chưa có golden dataset cô lập.

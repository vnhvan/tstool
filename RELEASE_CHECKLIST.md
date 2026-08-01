# Checklist kiểm thử APK v0.1

- [ ] Gradle Sync không lỗi.
- [ ] `testDebugUnitTest` đạt.
- [ ] Build APK debug thành công.
- [ ] APK cài được trên LDPlayer 9 Android 9.
- [ ] Ứng dụng không yêu cầu quyền Internet.
- [ ] Chọn được `mGameInfo_original.xml`.
- [ ] Coin/TCash/Sound hiển thị đúng fixture.
- [ ] Tạo backup nội bộ thành công.
- [ ] Đóng/mở ứng dụng, backup và lịch sử vẫn còn.
- [ ] Mở lại backup nội bộ thành công.
- [ ] Xuất backup gốc có SHA-256 giống đầu vào.
- [ ] Xuất XML giải mã thành công.
- [ ] Chỉnh Sound 0 → 100 và báo cáo chỉ có một thay đổi.
- [ ] File xuất có kích thước 300.008 byte đối với fixture chuẩn.
- [ ] File xuất decode lại đúng Sound 100.
- [ ] Township mở file xuất không lỗi và không rollback.

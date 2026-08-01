# Core Work Package — trạng thái

## Đã hoàn thành trong gói này

- Save Analyzer đọc toàn bộ `<Var>` và `<Object>`.
- Lập snapshot ổn định cho một save.
- Phát hiện tên biến trùng.
- Thống kê số Var, Object, Object có name và BID/ID.
- Object Browser tìm theo name, BID/ID, data và mọi thuộc tính.
- Full Diff Engine so sánh Var và Object giữa hai save.
- Báo cáo full diff dạng TXT.
- Knowledge Base ban đầu cho Sound, Coin, TCash, Mine Depth và Cow Factory Slot.
- Báo cáo phân tích save dạng TXT.
- Restore Pipeline: kiểm tra nguồn, tạo kế hoạch, xác minh file đã ghi byte-for-byte.
- Restore Staging Store: ghi file tạm nội bộ, đọc lại, kiểm SHA-256, rồi mới đổi tên thành staging chính thức.
- Giao diện đã thêm báo cáo phân tích, Object Browser và chọn save thứ hai để so sánh.

## Kết quả trên fixture thật

- 1.299 Var.
- 658 Object.
- 0 tên Var trùng.
- So sánh original và Sound100: đúng 1 thay đổi Var (`soundVolume`, 0 -> 100), 0 Object thay đổi.
- Restore verification: bản sao nguyên vẹn đạt; bản thiếu 1 byte bị từ chối.

## Giới hạn còn lại

- Chưa compile APK thật trong môi trường này vì thiếu Android SDK/AndroidX cache.
- Restore staging chưa ghi trực tiếp vào `/data/data/com.playrix.township.vn/saves`.
- Coin/TCash vẫn chỉ đọc cho tới khi có dữ liệu mẫu độc lập.
- Object identity hiện ưu tiên BID, rồi name, rồi index; một số object không có khóa ổn định có thể tạo diff nhiễu.

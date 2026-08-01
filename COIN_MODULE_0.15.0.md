# Coin Experimental — v0.15.0

## Đã triển khai

- Coin xuất hiện ở Home và Modules với trạng thái `Experimental`.
- Màn hình Coin riêng.
- Chọn `mGameInfo.xml` nhị phân.
- Đọc giá trị hiện tại từ biến `money`.
- Nhập Coin mới trong khoảng `0–2.000.000.000`.
- Safe Edit chỉ cho phép đúng Coin chạy bằng `allowCandidate=true`.
- Xác minh diff chỉ có một Var `money`, không thay Object.
- Encode save và mở lại để xác minh giá trị.
- Xem trước thay đổi/rủi ro trước khi xuất.
- Chỉ ghi lịch sử sau khi Android xuất file thành công.

## Trạng thái

Coin vẫn là `Experimental`. Chỉ chuyển `Verified` sau khi:

1. Township mở file đã chỉnh.
2. Coin hiển thị đúng.
3. Thực hiện giao dịch Coin bình thường.
4. Force stop và mở lại, Coin vẫn duy trì.

## Thử nghiệm đề nghị

- Backup toàn bộ thư mục save.
- Thử tăng Coin một lượng nhỏ, ví dụ `+100`.
- Không thử ngay giá trị cực lớn.

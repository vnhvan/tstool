# Build APK bằng Codemagic — không cần thư mục `.github`

Gói này dùng file `codemagic.yaml` nằm ngay tại thư mục gốc. Không cần upload thư mục ẩn `.github`.

## A. Upload project lên GitHub

1. Tạo repository GitHub mới, nên chọn **Private**.
2. Trong repository chọn **Add file → Upload files**.
3. Giải nén gói ZIP này.
4. Mở thư mục `ChucksTool_CodeMagic_Build` và kéo **toàn bộ nội dung bên trong** lên GitHub.
5. Kiểm tra tại trang gốc repository phải thấy trực tiếp:

```text
app/
gradle/
scripts/
tools/
build.gradle.kts
settings.gradle.kts
gradle.properties
codemagic.yaml
```

Không upload nguyên file ZIP. Không để project lồng thêm một thư mục con.

## B. Kết nối Codemagic

1. Truy cập `https://codemagic.io` và đăng nhập bằng GitHub.
2. Chọn **Add application**.
3. Chọn **GitHub**, cấp quyền cho Codemagic và chọn repository vừa tạo.
4. Chọn loại project **Android** hoặc **Other** nếu Codemagic tự nhận diện không đúng.
5. Sau khi thêm ứng dụng, chọn **Check for configuration file** hoặc **Start new build**.
6. Chọn branch `main` và workflow:

```text
Build Android Debug APK
```

7. Bấm **Start new build**.

## C. Tải APK

Khi build thành công, mở build vừa chạy và tải trong mục **Artifacts**:

```text
app-debug.apk
app-debug.apk.sha256
```

APK được tạo tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## D. Khi build lỗi

Mở bước màu đỏ, sao chép log từ dòng `FAILURE: Build failed with an exception.` đến cuối và gửi lại.

Các lỗi thường cần gửi đầy đủ:

- `Unresolved reference`
- `Android resource linking failed`
- `Could not resolve ...`
- `Manifest merger failed`
- `Compilation error`

## Ghi chú

- Workflow tự tải Gradle 8.9 nên không cần `gradle-wrapper.jar`.
- Codemagic cung cấp Android SDK trên máy build.
- Đây là debug APK, Android tự ký bằng debug keystore.
- Không cần cấu hình keystore để cài thử trên LDPlayer.

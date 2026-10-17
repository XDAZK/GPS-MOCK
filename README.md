# GPS Mock 📍🛰️

> Ứng dụng giả lập vị trí GPS mạnh mẽ trên Android với đầy đủ tính năng lưu tọa độ giả, lập lịch trình tự động chuyển vị trí, điều khiển bằng Joystick và quy trình tự động hóa GitHub Actions CI/CD xuất file Release APK.
> Dựa trên kiến trúc và cảm hứng từ [Sriharan-S/gps-mock](https://github.com/Sriharan-S/gps-mock).

---

## 🌟 Tính Năng Chính

### 1. 🗺️ Bản đồ tương tác & Giả lập thời gian thực
- Bản đồ tương tác thế giới (OpenStreetMap / Leaflet), hỗ trợ tìm kiếm địa điểm, địa chỉ và tọa độ toàn cầu.
- Di chuyển ghim trên bản đồ để chọn vị trí giả lập hoặc nhập vĩ độ / kinh độ chính xác đến 6 chữ số thập phân.
- Động cơ **Android TestProvider** kết hợp **Foreground Service** giúp vị trí giả luôn hoạt động bền bỉ ở chế độ chạy ngầm ngay cả khi tắt màn hình hoặc chuyển sang app khác.
- Thông báo tiện ích cho phép theo dõi tọa độ, tạm dừng hoặc dừng giả lập tức thì.

### 2. 📌 Quản lý & Lưu tọa độ giả (Saved Coordinates)
- Lưu các địa điểm thường dùng với tên tùy chỉnh, ghi chú và nhãn phân loại: **Favorites**, **Vietnam**, **Global**, **Gaming**, **Work**, **Custom**.
- Dịch chuyển tức thì (Teleport) đến bất kỳ điểm nào đã lưu chỉ với 1 chạm.
- Tích hợp sẵn danh mục các địa danh nổi tiếng (Hồ Hoàn Kiếm, Chợ Bến Thành, Times Square, Eiffel Tower, Shibuya Crossing, Sydney Opera House...).
- Lưu trữ cục bộ an toàn bằng **Room Database**.

### 3. ⏱️ Lập lịch trình tự động chuyển vị trí (Route & Timed Schedules)
- **Chế độ Di chuyển liên tục (Continuous Path)**:
  - Chọn nhiều điểm dừng (Point A → Point B → Point C...).
  - Tự động nội suy tọa độ mượt mà dựa theo tốc độ tùy chỉnh (Đi bộ 5 km/h, Xe đạp 15 km/h, Xe hơi 60 km/h, hoặc tùy chỉnh).
  - Tự động tính góc xoay hướng đi (bearing) theo cung đường.
- **Chế độ Nhảy theo thời gian (Timed Hop Schedule)**:
  - Dừng tại mỗi địa điểm trong thời gian quy định (ví dụ: dừng 30 giây tại Điểm 1, sau đó tự động nhảy đến Điểm 2).
- **Chế độ lặp linh hoạt**: Lặp vòng tròn (`LOOP`), chạy khứ hồi đảo chiều (`PING_PONG`), hoặc chạy 1 lượt (`ONCE`).

### 4. 🕹️ Cần điều khiển ảo (Floating Joystick)
- Bật/tắt cần điều khiển nổi trên màn hình để di chuyển vị trí theo hướng mong muốn (N, NE, E, SE, S, SW, W, NW).
- Thanh trượt điều chỉnh tốc độ tức thì.

### 5. 🔬 Mô phỏng chân thực (Realism Mode)
- **Tùy chọn GPS Micro-Jitter**: Tạo dao động vi mô tự nhiên (~0.5m) giúp tránh các thuật toán phát hiện vị trí tĩnh bất thường của game và ứng dụng tracking.
- Tùy chỉnh độ chính xác (Accuracy: 1m - 15m).

---

## 🛠️ Hướng Dẫn Bật Vị Trí Giả Trên Android

Để điện thoại cho phép GPS Mock ghi đè tọa độ hệ thống:

1. Mở **Cài đặt** trên điện thoại > **Thông tin điện thoại** (About phone).
2. Nhấn liên tục **7 lần** vào **Số hiệu bản dựng** (Build number) cho đến khi hiện thông báo *"Bạn đã là nhà phát triển"*.
3. Quay lại Cài đặt > **Hệ thống** > **Tùy chọn cho nhà phát triển** (Developer options).
4. Tìm kiếm mục **Chọn ứng dụng vị trí giả** (Select mock location app).
5. Chọn **GPS Mock**.
6. Mở lại ứng dụng GPS Mock và bắt đầu sử dụng!

---

## 🚀 GitHub Actions CI/CD (Tự Động Build Release APK)

Dự án đã được tích hợp sẵn workflow CI/CD tại `.github/workflows/build-release.yml`.

### Cách hoạt động:
- **Tự động build APK**: Khi bạn push code lên nhánh `main` hoặc `master`, hoặc tạo Pull Request, GitHub Actions sẽ tự động kiểm tra cú pháp và build APK.
- **Tự động tạo Release**: Khi bạn push tag phiên bản (ví dụ: `git tag v1.0.0 && git push origin v1.0.0`) hoặc kích hoạt thủ công qua nút **Run workflow** (Actions tab), hệ thống sẽ:
  1. Biên dịch file `GPS-Mock-release.apk`.
  2. Ký số APK bằng keystore tự tạo hoặc GitHub Secrets nếu được cấu hình.
  3. Tải file APK lên mục Artifacts và tạo **GitHub Release** đính kèm file APK sẵn sàng tải về cài đặt.

### Tùy chọn cấu hình Secrets (Tùy chọn):
Nếu bạn muốn dùng Keystore riêng của mình, thêm vào **Settings > Secrets and variables > Actions**:
- `RELEASE_KEYSTORE_BASE64`: Nội dung file keystore `.jks` đã mã hóa base64 (`base64 -w 0 my-upload-key.jks`).
- `STORE_PASSWORD`: Mật khẩu keystore.
- `KEY_PASSWORD`: Mật khẩu key alias.

*(Nếu không thiết lập Secrets, CI/CD sẽ tự động khởi tạo keystore chuẩn để luôn xuất ra file APK ký số hợp lệ).*

---

## 🏗️ Cấu Trúc Mã Nguồn

```
app/
 ├── src/main/java/com/example/
 │    ├── data/
 │    │    ├── dao/                # Room DAOs (SavedLocationDao, RouteScheduleDao)
 │    │    ├── model/              # Entity models (SavedLocation, RouteSchedule, Waypoint)
 │    │    ├── repository/         # Repository pattern
 │    │    ├── AppDatabase.kt      # Room Database
 │    │    └── PresetLocations.kt  # Danh sách địa điểm & lịch trình mẫu
 │    ├── service/
 │    │    ├── MockLocationService.kt           # Foreground Service điều khiển luồng mock
 │    │    ├── MockLocationProviderHelper.kt    # Tương tác với LocationManager TestProvider
 │    │    ├── MockEngineState.kt               # StateFlow trạng thái thời gian thực
 │    │    └── LocationMath.kt                  # Tính toán khoảng cách Haversine & góc xoay
 │    ├── ui/
 │    │    ├── components/         # Joystick, CoordinateInputDialog, SaveDialog, RouteDialog
 │    │    ├── screens/            # MapScreen, SavedLocationsScreen, RouteScheduleScreen, SettingsScreen
 │    │    ├── theme/              # Cyber Dark Material 3 Palette
 │    │    └── viewmodel/          # GpsMockViewModel
 │    └── MainActivity.kt          # Bottom Navigation & quyền hệ thống
 └── .github/workflows/build-release.yml  # GitHub Actions CI/CD
```

# QuanLyOto

## Khởi tạo cơ sở dữ liệu SQL Server

Để tạo lại database và nạp dữ liệu mẫu, chạy lần lượt hai file trong SQL Server Management Studio:

1. `database/QuanLyGaraOto.sql` — xóa database `a1` nếu đã tồn tại, sau đó tạo lại database và toàn bộ bảng.
2. `database/DuLieuMau.sql` — nạp dữ liệu mẫu vào database `a1`.

**Cảnh báo:** Bước 1 xóa toàn bộ dữ liệu hiện có trong database `a1`. Chỉ chạy khi bạn muốn tạo lại database; hãy sao lưu dữ liệu cần giữ trước. Chỉ chạy file dữ liệu mẫu sau khi tạo schema thành công. File dữ liệu mẫu không cho phép chạy lặp để tránh trùng dữ liệu.

Bộ dữ liệu mẫu mở rộng bao gồm khách hàng và nhiều xe, tài khoản/nhân viên/lịch làm việc, phụ tùng/tồn kho, dịch vụ/khuyến mãi, tiếp nhận/kiểm tra/sửa chữa, hóa đơn và giao dịch thanh toán, bảo hành/đổi trả/bàn giao. Dữ liệu có nhiều trạng thái và mốc thời gian để thử báo cáo, công nợ, tồn kho và các luồng xử lý.

Tài khoản demo: `NV001` đến `NV008` dùng mật khẩu `123456`; `NV009` và `NV010` dùng mật khẩu `654321`.

Các ca mẫu: `CA001` 07:00–11:00, `CA002` 13:00–17:00, `CA003` 18:00–22:00.

Nếu không muốn xóa database hiện có, không chạy script tạo lại ở trên. Với database cũ, chạy `database/Migration_ChiTietHoaDon.sql` một lần để cập nhật cấu trúc chi tiết phiếu sửa chữa và hóa đơn.

Với database đang dùng cột `NhanVien.chucVu` dạng văn bản, chạy `database/Migration_ChucVu.sql` một lần trên database `a1` trước khi mở ứng dụng. Migration giữ lại nhân viên hiện có, tạo danh mục chức vụ từ dữ liệu cũ và chuyển sang khóa ngoại `maChucVu`. Không chạy `DuLieuMau.sql` trên schema cũ; file này sẽ báo nếu thiếu bảng/cột mới.

Với database đang dùng chưa có cột `PhieuTiepNhan.tienDo`, chạy `database/Migration_TienDoPhieuTiepNhan.sql` một lần. Migration giữ nguyên dữ liệu và xác định tiến độ ban đầu từ các phiếu kiểm tra, sửa chữa, hóa đơn và trạng thái thanh toán hiện có.

Sau đó chạy `database/Migration_TrangThaiPhieuKiemTra.sql` để thêm trạng thái cho phiếu kiểm tra và chuẩn hóa trạng thái phiếu sửa chữa về `Chờ sửa chữa` / `Đã sửa chữa`.

Các bảng chính sách bảo hành và phiếu yêu cầu bảo hành chỉ được tạo trong script khởi tạo database mới; nếu đang dùng database cũ, hãy tạo lại database và nạp lại dữ liệu mẫu để sử dụng đầy đủ các chức năng này.

Các ràng buộc `NOT NULL` mới cũng chỉ áp dụng khi khởi tạo schema mới. Để tạo lại toàn bộ database `a1`, chạy `QuanLyGaraOto.sql` rồi `DuLieuMau.sql` theo đúng thứ tự.

## Cấu hình kết nối cho các DAO đã tách khỏi `BaseDAO`/`ConnectDB`

Các DAO `CaLamViecDAO`, `ChinhSachBaoHanhDAO`, `PhieuYeuCauBaoHanhDAO`, `NhanVienDAO`, `LichLamViecDAO`, `KhachHangDAO`, `PhieuTiepNhanDAO`, `PhieuKiemTraDAO`, `TaiKhoanDAO` và `PhieuBanGiaoDAO` mở kết nối riêng cho từng thao tác, mỗi DAO tự đọc cấu hình từ `database.properties` ở thư mục gốc. File này được Git bỏ qua để thông tin đăng nhập không bị commit. Nếu cần cấu hình lại, sao chép `database.properties.example` thành `database.properties` rồi nhập đúng URL, user và mật khẩu SQL Server.

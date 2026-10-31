# QuanLyOto

## Khởi tạo cơ sở dữ liệu SQL Server

Để tạo database mới và nạp dữ liệu mẫu, chạy lần lượt hai file trong SQL Server Management Studio:

1. `database/QuanLyGaraOto.sql` — tạo database `a1` và toàn bộ bảng.
2. `database/DuLieuMau.sql` — nạp dữ liệu mẫu vào database `a1`.

Chỉ chạy file dữ liệu mẫu sau khi tạo schema thành công. File dữ liệu mẫu không cho phép chạy lặp để tránh trùng dữ liệu.

Nếu database `a1` đã có schema cũ, không chạy lại file tạo database. Chạy `database/Migration_ChiTietHoaDon.sql` một lần để cập nhật cấu trúc chi tiết phiếu sửa chữa và hóa đơn.

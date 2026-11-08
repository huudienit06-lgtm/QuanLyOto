-- Dữ liệu mẫu mở rộng cho schema hiện tại trong QuanLyGaraOto.sql.
-- Chỉ chạy một lần trên database a1 mới; không xóa dữ liệu đang có.
USE a1;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.ChucVu', N'U') IS NULL
        OR COL_LENGTH(N'dbo.NhanVien', N'maChucVu') IS NULL
    BEGIN
        ;THROW 50002, N'Schema chua cap nhat ChucVu. Hay tao database moi hoac chay Migration_ChucVu.sql truoc.', 1;
    END;

    IF EXISTS (SELECT 1 FROM dbo.NhanVien WHERE maNV LIKE 'NV%')
    BEGIN
        ;THROW 50001, N'Dữ liệu mẫu đã tồn tại hoặc mã nhân viên NV đã được sử dụng. Hãy dùng database mới để nạp lại bộ dữ liệu này.', 1;
    END;

    INSERT INTO dbo.KhachHang (maKH, hoTen, soDT, email, diaChi, congNo) VALUES
    ('KH001', N'Nguyễn Minh Anh', '0901000001', 'minhanh.demo@example.com', N'Quận 1, TP. Hồ Chí Minh', 0),
    ('KH002', N'Trần Quốc Bảo', '0901000002', 'quocbao.demo@example.com', N'Thủ Đức, TP. Hồ Chí Minh', 0),
    ('KH003', N'Lê Thu Hà', '0901000003', 'thuha.demo@example.com', N'Hải Châu, Đà Nẵng', 369600),
    ('KH004', N'Phạm Đức Long', '0901000004', 'duclong.demo@example.com', N'Ninh Kiều, Cần Thơ', 0),
    ('KH005', N'Võ Ngọc Mai', '0901000005', 'ngocmai.demo@example.com', N'Ba Đình, Hà Nội', 918000),
    ('KH006', N'Đặng Hoàng Nam', '0901000006', 'hoangnam.demo@example.com', N'Biên Hòa, Đồng Nai', 2041200),
    ('KH007', N'Bùi Thanh Tâm', '0901000007', 'thanhtam.demo@example.com', N'Thuận An, Bình Dương', 0),
    ('KH008', N'Đỗ Khánh Linh', '0901000008', 'khanhlinh.demo@example.com', N'Quận 7, TP. Hồ Chí Minh', 0),
    ('KH009', N'Phan Gia Huy', '0901000009', 'giahuy.demo@example.com', N'Long Biên, Hà Nội', 0),
    ('KH010', N'Ngô Thùy Dương', '0901000010', 'thuyduong.demo@example.com', N'Sơn Trà, Đà Nẵng', 0);

    INSERT INTO dbo.ChucVu (maChucVu, tenChucVu) VALUES
    ('CV_QL', N'Quản lý'),
    ('CV_CVDV', N'Cố vấn dịch vụ'),
    ('CV_LT', N'Lễ tân'),
    ('CV_KTV', N'Kỹ thuật viên');

    INSERT INTO dbo.NhanVien (maNV, hoTen, maChucVu, soDT, email, trangThai) VALUES
    ('NV001', N'Nguyễn Thị Lan', 'CV_LT', '0902000001', 'lan.demo@example.com', N'Đang làm việc'),
    ('NV002', N'Trần Văn Minh', 'CV_LT', '0902000002', 'minh.demo@example.com', N'Đang làm việc'),
    ('NV003', N'Lê Hoàng Phúc', 'CV_KTV', '0902000003', 'phuc.demo@example.com', N'Đang làm việc'),
    ('NV004', N'Phạm Văn Hùng', 'CV_KTV', '0902000004', 'hung.demo@example.com', N'Đang làm việc'),
    ('NV005', N'Võ Thành Đạt', 'CV_KTV', '0902000005', 'dat.demo@example.com', N'Đang làm việc'),
    ('NV006', N'Đặng Quốc Việt', 'CV_KTV', '0902000006', 'viet.demo@example.com', N'Đang làm việc'),
    ('NV007', N'Bùi Minh Tâm', 'CV_KTV', '0902000007', 'tam.demo@example.com', N'Đang làm việc'),
    ('NV008', N'Hoàng Gia Bảo', 'CV_KTV', '0902000008', 'bao.demo@example.com', N'Đang làm việc'),
    ('NV009', N'Nguyễn Hoàng Phúc', 'CV_QL', '0902000009', 'quanly1.demo@example.com', N'Đang làm việc'),
    ('NV010', N'Trần Quốc Khánh', 'CV_QL', '0902000010', 'quanly2.demo@example.com', N'Đang làm việc');

    -- Tài khoản demo dùng mật khẩu rõ theo cơ chế đăng nhập hiện tại của ứng dụng.
    INSERT INTO dbo.TaiKhoan (maNV, tenDangNhap, matKhau, vaiTro, trangThai) VALUES
    ('NV001', 'NV001', '123456', N'Nhân viên', N'Hoạt động'),
    ('NV002', 'NV002', '123456', N'Nhân viên', N'Hoạt động'),
    ('NV003', 'NV003', '123456', N'Nhân viên', N'Hoạt động'),
    ('NV004', 'NV004', '123456', N'Nhân viên', N'Hoạt động'),
    ('NV005', 'NV005', '123456', N'Nhân viên', N'Hoạt động'),
    ('NV006', 'NV006', '123456', N'Nhân viên', N'Hoạt động'),
    ('NV007', 'NV007', '123456', N'Nhân viên', N'Hoạt động'),
    ('NV008', 'NV008', '123456', N'Nhân viên', N'Hoạt động'),
    ('NV009', 'NV009', '654321', N'Quản lý', N'Hoạt động'),
    ('NV010', 'NV010', '654321', N'Quản lý', N'Hoạt động');

    INSERT INTO dbo.Xe (maXe, bienSo, hangXe, dongXe, namSanXuat, soKhung, soMay, maKH) VALUES
    ('X001', '51A-10001', N'Toyota', N'Vios', 2021, 'DMVIN001', 'DMMOT001', 'KH001'),
    ('X002', '51B-10002', N'Honda', N'City', 2020, 'DMVIN002', 'DMMOT002', 'KH002'),
    ('X003', '43A-10003', N'Hyundai', N'Accent', 2022, 'DMVIN003', 'DMMOT003', 'KH003'),
    ('X004', '65A-10004', N'Mazda', N'CX-5', 2019, 'DMVIN004', 'DMMOT004', 'KH004'),
    ('X005', '30F-10005', N'Ford', N'Ranger', 2023, 'DMVIN005', 'DMMOT005', 'KH005'),
    ('X006', '60A-10006', N'Kia', N'Seltos', 2021, 'DMVIN006', 'DMMOT006', 'KH006'),
    ('X007', '61A-10007', N'Mitsubishi', N'Xforce', 2024, 'DMVIN007', 'DMMOT007', 'KH007'),
    ('X008', '51H-10008', N'Mercedes-Benz', N'C200', 2020, 'DMVIN008', 'DMMOT008', 'KH008'),
    ('X009', '29A-10009', N'VinFast', N'VF 8', 2023, 'DMVIN009', 'DMMOT009', 'KH009'),
    ('X010', '43A-10010', N'Toyota', N'Corolla Cross', 2022, 'DMVIN010', 'DMMOT010', 'KH010'),
    ('X011', '51A-10011', N'Toyota', N'Vios', 2018, 'DMVIN011', 'DMMOT011', 'KH001'),
    ('X012', '51B-10012', N'Honda', N'CR-V', 2024, 'DMVIN012', 'DMMOT012', 'KH006');

    INSERT INTO dbo.CaLamViec (maCa, gioBatDau, gioKetThuc) VALUES
    ('CA001', '07:00', '11:00'),
    ('CA002', '13:00', '17:00'),
    ('CA003', '18:00', '22:00');

    INSERT INTO dbo.LichLamViec (maLich, maCa, maNV, ngayLamViec, trangThai) VALUES
    ('LLV001', 'CA001', 'NV001', '2026-10-30', N'Có mặt'),
    ('LLV002', 'CA002', 'NV002', '2026-10-30', N'Có mặt'),
    ('LLV003', 'CA003', 'NV003', '2026-10-30', N'Có mặt'),
    ('LLV004', 'CA001', 'NV004', '2026-10-30', N'Có mặt'),
    ('LLV005', 'CA001', 'NV005', '2026-10-31', N'Có mặt'),
    ('LLV006', 'CA002', 'NV006', '2026-10-31', N'Có mặt'),
    ('LLV007', 'CA003', 'NV007', '2026-10-31', N'Có mặt'),
    ('LLV008', 'CA001', 'NV008', '2026-10-31', N'Vắng mặt'),
    ('LLV009', 'CA001', 'NV001', '2026-11-02', N'Đã phân công'),
    ('LLV010', 'CA002', 'NV002', '2026-11-02', N'Đã phân công'),
    ('LLV011', 'CA003', 'NV003', '2026-11-02', N'Đã phân công'),
    ('LLV012', 'CA001', 'NV004', '2026-11-02', N'Đã phân công'),
    ('LLV013', 'CA001', 'NV005', '2026-11-03', N'Đã phân công'),
    ('LLV014', 'CA002', 'NV006', '2026-11-03', N'Đã phân công'),
    ('LLV015', 'CA003', 'NV009', '2026-11-03', N'Đã phân công');

    INSERT INTO dbo.PhuTung
        (maPhuTung, tenPhuTung, donViTinh, giaNhap, giaBan, soLuongTon, nhaCungCap, trangThai)
    VALUES
    ('PT001', N'Lọc dầu động cơ', N'Cái', 120000, 180000, 19, N'Minh Phát Auto', N'Còn hàng'),
    ('PT002', N'Lọc gió động cơ', N'Cái', 150000, 220000, 14, N'Thành Công Parts', N'Còn hàng'),
    ('PT003', N'Bugi đánh lửa', N'Cái', 80000, 130000, 11, N'Minh Phát Auto', N'Còn hàng'),
    ('PT004', N'Má phanh trước', N'Bộ', 450000, 650000, 9, N'An Phú Garage Supply', N'Còn hàng'),
    ('PT005', N'Ắc quy 12V', N'Cái', 1200000, 1550000, 8, N'Thành Công Parts', N'Còn hàng'),
    ('PT006', N'Dầu động cơ tổng hợp 5W-30', N'Can', 240000, 350000, 11, N'Dầu nhớt Việt', N'Còn hàng'),
    ('PT007', N'Máy phát điện ô tô', N'Cái', 650000, 900000, 9, N'An Phú Garage Supply', N'Còn hàng'),
    ('PT008', N'Cảm biến áp suất lốp', N'Cái', 300000, 450000, 5, N'Điện xe Thành Đạt', N'Còn hàng'),
    ('PT009', N'Cầu chì ô tô', N'Bộ', 70000, 120000, 5, N'Điện xe Thành Đạt', N'Còn hàng'),
    ('PT010', N'Nước làm mát động cơ', N'Can', 180000, 280000, 5, N'Dầu nhớt Việt', N'Còn hàng'),
    ('PT011', N'Gạt mưa trước', N'Bộ', 160000, 240000, 4, N'Minh Phát Auto', N'Ngừng kinh doanh'),
    ('PT012', N'Bơm nhiên liệu', N'Cái', 600000, 850000, 0, N'Thành Công Parts', N'Hết hàng');

    INSERT INTO dbo.ChinhSachBaoHanh
        (maCSBH, tenChinhSach, thoiHanBaoHanh, donViThoiGian,
         dieuKienBaoHanh, truongHopTuChoi, noiDungBaoHanh, ngayTao, trangThai)
    VALUES
    ('CSBH001', N'Bảo hành lọc dầu và lọc gió', 6, 'THANG',
     N'Có hóa đơn mua hàng; phụ tùng được gara lắp đặt và sử dụng đúng hướng dẫn.',
     N'Từ chối nếu hư hỏng do lắp sai, tai nạn hoặc tự ý tháo sửa.',
     N'Kiểm tra lỗi kỹ thuật và đổi phụ tùng tương đương nếu đủ điều kiện.', '2026-01-05', 1),
    ('CSBH002', N'Bảo hành má phanh', 3, 'THANG',
     N'Áp dụng với lỗi vật liệu hoặc lỗi sản xuất, cần xuất trình hóa đơn.',
     N'Không áp dụng với hao mòn tự nhiên, quá tải hoặc sử dụng sai cách.',
     N'Kiểm tra hệ thống phanh và sửa chữa hoặc đổi phụ tùng theo kết quả.', '2026-01-05', 1),
    ('CSBH003', N'Bảo hành ắc quy', 12, 'THANG',
     N'Bình còn tem, có hóa đơn và hệ thống sạc xe hoạt động đúng thông số.',
     N'Từ chối nếu bình cạn do xe lâu ngày không sử dụng hoặc đấu nối sai.',
     N'Đo kiểm bình và hệ thống sạc; sửa chữa hoặc đổi bình đủ điều kiện.', '2026-01-05', 1),
    ('CSBH004', N'Bảo hành phụ tùng điện', 30, 'NGAY',
     N'Phụ tùng còn nguyên tem và được lắp đặt tại gara.',
     N'Từ chối nếu có dấu hiệu ngấm nước, chập điện do đấu nối ngoài.',
     N'Kiểm tra và sửa chữa lỗi kỹ thuật được xác nhận.', '2026-02-01', 1),
    ('CSBH005', N'Chính sách cũ đã ngừng áp dụng', 1, 'NAM',
     N'Chính sách lưu để tra cứu giao dịch cũ.',
     N'Không áp dụng cho giao dịch phát sinh mới.',
     N'Liên hệ quản lý để được hướng dẫn.', '2025-01-01', 0);

    INSERT INTO dbo.ChinhSachBaoHanh_PhuTung (maCSBH, maPhuTung) VALUES
    ('CSBH001', 'PT001'), ('CSBH001', 'PT002'),
    ('CSBH002', 'PT004'), ('CSBH003', 'PT005'),
    ('CSBH004', 'PT007'), ('CSBH004', 'PT008'),
    ('CSBH005', 'PT011');

    INSERT INTO dbo.DichVu (maDV, tenDV, moTaDV, giaDV) VALUES
    ('DMDV001', N'Thay dầu động cơ', N'Thay dầu và kiểm tra rò rỉ dầu động cơ.', 250000),
    ('DMDV002', N'Bảo dưỡng hệ thống phanh', N'Kiểm tra, vệ sinh và bảo dưỡng phanh.', 400000),
    ('DMDV003', N'Vệ sinh lọc gió', N'Vệ sinh lọc gió và kiểm tra đường nạp.', 150000),
    ('DMDV004', N'Kiểm tra hệ thống điện', N'Kiểm tra ắc quy, máy phát và hệ thống điện.', 200000),
    ('DMDV005', N'Cân chỉnh góc lái', N'Kiểm tra và cân chỉnh góc đặt bánh xe.', 500000),
    ('DMDV006', N'Sửa chữa hệ thống truyền động', N'Chẩn đoán và sửa chữa cụm truyền động.', 1200000),
    ('DMDV007', N'Kiểm tra xe tổng quát', N'Kiểm tra các hạng mục an toàn và vận hành.', 180000),
    ('DMDV008', N'Vệ sinh kim phun', N'Vệ sinh kim phun và kiểm tra áp suất nhiên liệu.', 300000),
    ('DMDV009', N'Chẩn đoán lỗi bằng máy', N'Đọc mã lỗi và lập biên bản kiểm tra.', 350000),
    ('DMDV010', N'Vá lốp', N'Kiểm tra và vá lốp không săm.', 100000);

    INSERT INTO dbo.ChuongTrinhKhuyenMai
        (maKM, tenKM, tileGiam, ngayBatDau, ngayKetThuc, dieuKienApDung, trangThai)
    VALUES
    ('KM001', N'Khách hàng mới', 5, '2026-01-01', '2026-12-31', N'Giảm 5% cho khách hàng lần đầu sử dụng dịch vụ.', N'Tạm ngưng'),
    ('KM002', N'Ưu đãi bảo dưỡng định kỳ', 10, '2026-01-01', '2026-12-31', N'Áp dụng cho hóa đơn có dịch vụ bảo dưỡng.', N'Đang áp dụng'),
    ('KM003', N'Ưu đãi tháng 11', 7, '2026-11-01', '2026-11-30', N'Áp dụng trong tháng 11 năm 2026.', N'Đang áp dụng'),
    ('KM004', N'Tri ân khách hàng', 8, '2026-10-01', '2026-12-31', N'Áp dụng cho khách hàng hiện hữu.', N'Đang áp dụng'),
    ('KM005', N'Khách hàng thân thiết', 12, '2026-01-01', '2026-12-31', N'Áp dụng theo chương trình khách hàng thân thiết.', N'Đang áp dụng'),
    ('KM006', N'Chương trình năm sau', 15, '2027-01-01', '2027-03-31', N'Chương trình tương lai để kiểm thử ngày hiệu lực.', N'Chưa áp dụng'),
    ('KM007', N'Ưu đãi đã kết thúc', 5, '2026-09-01', '2026-09-30', N'Chương trình đã hết hạn.', N'Hết hạn');

    INSERT INTO dbo.PhieuTiepNhan
        (maPhieuTiepNhan, maXe, maKH, maNV, ngayLap, trangThai, tienDo, ghiChu)
    VALUES
    ('PTN001', 'X001', 'KH001', 'NV001', '2026-10-28T08:15:00', N'Hoàn thành', N'Hoàn thành', N'Động cơ có tiếng gõ khi khởi động nguội.'),
    ('PTN002', 'X002', 'KH002', 'NV002', '2026-10-28T09:10:00', N'Hoàn thành', N'Hoàn thành', N'Phanh trước phát tiếng kêu khi giảm tốc.'),
    ('PTN003', 'X003', 'KH003', 'NV001', '2026-10-29T08:40:00', N'Hoàn thành', N'Đã lập hóa đơn', N'Bảo dưỡng định kỳ, xe hao nhiên liệu.'),
    ('PTN004', 'X004', 'KH004', 'NV002', '2026-10-29T10:20:00', N'Hoàn thành', N'Hoàn thành', N'Xe khó khởi động, đèn báo ắc quy sáng.'),
    ('PTN005', 'X005', 'KH005', 'NV001', '2026-10-29T13:00:00', N'Hoàn thành', N'Đã lập hóa đơn', N'Xe rung khi chạy tốc độ cao.'),
    ('PTN006', 'X006', 'KH006', 'NV002', '2026-10-30T08:30:00', N'Hoàn thành', N'Đã lập hóa đơn', N'Tiếng ồn ở hệ thống truyền động.'),
    ('PTN007', 'X007', 'KH007', 'NV001', '2025-12-12T09:00:00', N'Hoàn thành', N'Hoàn thành', N'Kiểm tra xe trước chuyến đi dài.'),
    ('PTN008', 'X008', 'KH008', 'NV002', '2026-11-01T10:00:00', N'Đang sửa chữa', N'Đã lập phiếu sửa chữa', N'Đèn cảnh báo động cơ bật ngắt quãng.'),
    ('PTN009', 'X009', 'KH009', 'NV001', '2026-11-02T08:45:00', N'Đang kiểm tra', N'Đã lập phiếu kiểm tra', N'Cần kiểm tra hệ thống phanh và lốp.'),
    ('PTN010', 'X010', 'KH010', 'NV002', '2026-11-02T09:30:00', N'Đã tiếp nhận', N'Đã tiếp nhận', N'Khách đặt lịch kiểm tra tổng quát.');

    INSERT INTO dbo.PhieuKiemTra
        (maPhieuKiemTra, maPhieuTiepNhan, maKH, maXe, maNV, trangThai, trangThaiBanDau, vanDeHuHong)
    VALUES
    ('PKT001', 'PTN001', 'KH001', 'X001', 'NV003', N'Đã kiểm tra', N'Động cơ hoạt động, có tiếng gõ nhẹ khi nguội.', N'Lọc dầu cũ; cần thay dầu và kiểm tra áp suất bôi trơn.'),
    ('PKT002', 'PTN002', 'KH002', 'X002', 'NV004', N'Đã kiểm tra', N'Phanh vẫn hoạt động nhưng má phanh mòn.', N'Cần bảo dưỡng cụm phanh và thay má phanh trước.'),
    ('PKT003', 'PTN003', 'KH003', 'X003', 'NV005', N'Đã kiểm tra', N'Lọc gió bẩn, xe đến hạn bảo dưỡng.', N'Cần thay lọc gió, thay dầu và vệ sinh lọc.'),
    ('PKT004', 'PTN004', 'KH004', 'X004', 'NV006', N'Đã kiểm tra', N'Ắc quy yếu, điện áp sạc chưa ổn định.', N'Cần kiểm tra hệ thống điện và thay ắc quy.'),
    ('PKT005', 'PTN005', 'KH005', 'X005', 'NV007', N'Đã kiểm tra', N'Vô lăng rung nhẹ khi chạy nhanh.', N'Cần cân chỉnh góc lái và kiểm tra lốp.'),
    ('PKT006', 'PTN006', 'KH006', 'X006', 'NV008', N'Đã kiểm tra', N'Tiếng ồn phát ra từ cụm truyền động.', N'Cần tháo kiểm tra và thay dầu cụm truyền động.'),
    ('PKT007', 'PTN007', 'KH007', 'X007', 'NV003', N'Đã kiểm tra', N'Xe vận hành bình thường, không có lỗi nghiêm trọng.', N'Cần kiểm tra tổng quát và thay bugi đến hạn.'),
    ('PKT008', 'PTN008', 'KH008', 'X008', 'NV004', N'Đã kiểm tra', N'Đèn báo lỗi động cơ xuất hiện ngắt quãng.', N'Đang chờ đọc mã lỗi và xác định nguyên nhân.'),
    ('PKT009', 'PTN009', 'KH009', 'X009', 'NV005', N'Đã kiểm tra', N'Chưa ghi nhận tình trạng ban đầu.', N'Chờ kỹ thuật viên kiểm tra xe.');

    INSERT INTO dbo.PhieuSuaChua
        (maPhieuSuaChua, maPhieuKiemTra, maXe, maKH, maNV, ngayBatDau, ngayHoanThanhDuKien, trangThai)
    VALUES
    ('PSC001', 'PKT001', 'X001', 'KH001', 'NV003', '2026-10-28T08:45:00', '2026-10-28T12:00:00', N'Đã sửa chữa'),
    ('PSC002', 'PKT002', 'X002', 'KH002', 'NV004', '2026-10-28T09:30:00', '2026-10-28T15:00:00', N'Đã sửa chữa'),
    ('PSC003', 'PKT003', 'X003', 'KH003', 'NV005', '2026-10-29T09:00:00', '2026-10-29T16:00:00', N'Đã sửa chữa'),
    ('PSC004', 'PKT004', 'X004', 'KH004', 'NV006', '2026-10-29T10:45:00', '2026-10-29T17:00:00', N'Đã sửa chữa'),
    ('PSC005', 'PKT005', 'X005', 'KH005', 'NV007', '2026-10-29T13:30:00', '2026-10-30T10:00:00', N'Đã sửa chữa'),
    ('PSC006', 'PKT006', 'X006', 'KH006', 'NV008', '2026-10-30T09:00:00', '2026-10-30T16:00:00', N'Đã sửa chữa'),
    ('PSC007', 'PKT007', 'X007', 'KH007', 'NV003', '2025-12-12T09:30:00', '2025-12-12T13:00:00', N'Đã sửa chữa'),
    ('PSC008', 'PKT008', 'X008', 'KH008', 'NV004', '2026-11-01T10:30:00', '2026-11-04T17:00:00', N'Đã sửa chữa');

    INSERT INTO dbo.ChiTietPhieuSuaChua (maPhieuSuaChua, maPhuTung, maDV, soLuong, donGia) VALUES
    ('PSC001', 'PT001', NULL, 1, 180000), ('PSC001', NULL, 'DMDV001', 1, 250000),
    ('PSC002', 'PT004', NULL, 1, 650000), ('PSC002', NULL, 'DMDV002', 1, 400000),
    ('PSC003', 'PT002', NULL, 1, 220000), ('PSC003', NULL, 'DMDV001', 1, 250000),
    ('PSC003', NULL, 'DMDV003', 1, 150000),
    ('PSC004', 'PT005', NULL, 1, 1550000), ('PSC004', NULL, 'DMDV004', 1, 200000),
    ('PSC005', 'PT006', NULL, 1, 350000), ('PSC005', NULL, 'DMDV005', 1, 500000),
    ('PSC006', 'PT007', NULL, 1, 900000), ('PSC006', NULL, 'DMDV006', 1, 1200000),
    ('PSC007', 'PT003', NULL, 1, 130000), ('PSC007', NULL, 'DMDV007', 1, 180000),
    ('PSC008', NULL, 'DMDV009', 1, 350000);

    INSERT INTO dbo.HoaDon
        (maHoaDon, maPhieuSuaChua, maNV, maKM, ngayLap,
         tienGiamGia, thueVAT, soTien, trangThai)
    VALUES
    ('HD001', 'PSC001', 'NV001', 'KM004', '2026-10-28T12:00:00', 37152, 34400, 427248, N'Đã thanh toán'),
    ('HD002', 'PSC002', 'NV002', 'KM002', '2026-10-28T15:00:00', 113400, 84000, 1020600, N'Đã thanh toán'),
    ('HD003', 'PSC003', 'NV001', NULL, '2026-10-29T16:00:00', 0, 49600, 669600, N'Thanh toán một phần'),
    ('HD004', 'PSC004', 'NV002', 'KM005', '2026-10-29T17:00:00', 226800, 140000, 1663200, N'Đã thanh toán'),
    ('HD005', 'PSC005', 'NV001', NULL, '2026-10-30T10:00:00', 0, 68000, 918000, N'Chưa thanh toán'),
    ('HD006', 'PSC006', 'NV002', 'KM002', '2026-10-30T16:00:00', 226800, 168000, 2041200, N'Chưa thanh toán'),
    ('HD007', 'PSC007', 'NV001', NULL, '2025-12-12T13:00:00', 0, 24800, 334800, N'Đã thanh toán'),
    ('HD008', NULL, 'NV002', NULL, '2026-10-31T14:00:00', 0, 36000, 486000, N'Đã thanh toán');

    INSERT INTO dbo.ChiTietHoaDon
        (maHoaDon, maPhieuSuaChua, maPhuTung, maDV, soLuong, donGia)
    VALUES
    ('HD001', 'PSC001', 'PT001', NULL, 1, 180000),
    ('HD001', 'PSC001', NULL, 'DMDV001', 1, 250000),
    ('HD002', 'PSC002', 'PT004', NULL, 1, 650000),
    ('HD002', 'PSC002', NULL, 'DMDV002', 1, 400000),
    ('HD003', 'PSC003', 'PT002', NULL, 1, 220000),
    ('HD003', 'PSC003', NULL, 'DMDV001', 1, 250000),
    ('HD003', 'PSC003', NULL, 'DMDV003', 1, 150000),
    ('HD004', 'PSC004', 'PT005', NULL, 1, 1550000),
    ('HD004', 'PSC004', NULL, 'DMDV004', 1, 200000),
    ('HD005', 'PSC005', 'PT006', NULL, 1, 350000),
    ('HD005', 'PSC005', NULL, 'DMDV005', 1, 500000),
    ('HD006', 'PSC006', 'PT007', NULL, 1, 900000),
    ('HD006', 'PSC006', NULL, 'DMDV006', 1, 1200000),
    ('HD007', 'PSC007', 'PT003', NULL, 1, 130000),
    ('HD007', 'PSC007', NULL, 'DMDV007', 1, 180000),
    ('HD008', NULL, 'PT008', NULL, 1, 450000);

    INSERT INTO dbo.PhieuYeuCauBaoHanh
        (maPhieuYCBH, maHoaDon, maPhuTung, soLuong, ngayTiepNhan,
         trangThai, lyDo, ghiChuXuLy, ngayXuLy, ngayHoanThanh)
    VALUES
    ('YCBH001', 'HD001', 'PT001', 1, '2026-10-30T09:00:00',
     N'Chờ xử lý', N'Lọc dầu có dấu hiệu rò rỉ trong thời hạn bảo hành.', NULL, NULL, NULL),
    ('YCBH002', 'HD002', 'PT004', 1, '2026-10-30T10:30:00',
     N'Chấp nhận bảo hành', N'Má phanh phát tiếng kêu bất thường sau khi lắp.', N'Đã xác nhận tiếp nhận để kiểm tra.', '2026-10-30T11:00:00', NULL),
    ('YCBH003', 'HD004', 'PT005', 1, '2026-10-31T09:15:00',
     N'Hoàn thành bảo hành', N'Ắc quy không giữ điện dù hệ thống sạc bình thường.', N'Đã đổi bình và kiểm tra lại hệ thống sạc.', '2026-10-31T10:00:00', '2026-10-31T12:00:00'),
    ('YCBH004', 'HD001', 'PT001', 1, '2026-11-01T08:45:00',
     N'Từ chối bảo hành', N'Vỏ phụ tùng bị nứt do tác động bên ngoài.', N'Từ chối theo điều kiện loại trừ của chính sách.', '2026-11-01T09:30:00', NULL);

    -- Yêu cầu bảo hành đã hoàn thành trả một bình về kho.
    UPDATE dbo.PhuTung SET soLuongTon = soLuongTon + 1 WHERE maPhuTung = 'PT005';

    INSERT INTO dbo.GiaoDichThanhToan
        (maGD, maHoaDon, phuongThucThanhToan, thoiGianGD, trangThaiGD, soTienThanhToan)
    VALUES
    ('GD001', 'HD001', N'Tiền mặt', '2026-10-28T12:10:00', N'Thành công', 427248),
    ('GD002', 'HD002', N'Chuyển khoản / QR', '2026-10-28T15:10:00', N'Thành công', 500000),
    ('GD003', 'HD002', N'Tiền mặt', '2026-10-28T15:20:00', N'Thành công', 520600),
    ('GD004', 'HD003', N'Chuyển khoản / QR', '2026-10-29T16:10:00', N'Thành công', 300000),
    ('GD005', 'HD005', N'Chuyển khoản / QR', '2026-10-30T10:10:00', N'Thất bại', 0),
    ('GD006', 'HD004', N'Chuyển khoản / QR', '2026-10-29T17:10:00', N'Thành công', 1663200),
    ('GD007', 'HD007', N'Tiền mặt', '2025-12-12T13:10:00', N'Thành công', 334800),
    ('GD008', 'HD008', N'Chuyển khoản / QR', '2026-10-31T14:10:00', N'Thành công', 486000);

    INSERT INTO dbo.PhieuBaoHanh
        (maPhieuBaoHanh, maNV, maKH, maXe, ngayHetHanBaoHanh, noiDungBaoHanh, dieuKienBaoHanh)
    VALUES
    ('PBH001', 'NV003', 'KH001', 'X001', '2027-10-28', N'Bảo hành công việc thay dầu và kiểm tra rò rỉ.', N'Áp dụng khi bảo dưỡng đúng lịch.'),
    ('PBH002', 'NV004', 'KH002', 'X002', '2027-01-28', N'Bảo hành má phanh đã thay.', N'Không áp dụng với hao mòn tự nhiên.'),
    ('PBH003', 'NV005', 'KH003', 'X003', '2027-04-29', N'Bảo hành lọc gió và công việc bảo dưỡng.', N'Giữ hóa đơn và sử dụng phụ tùng đúng hướng dẫn.'),
    ('PBH004', 'NV006', 'KH004', 'X004', '2027-10-29', N'Bảo hành bình ắc quy đã thay.', N'Áp dụng khi hệ thống sạc hoạt động đúng thông số.'),
    ('PBH005', 'NV007', 'KH005', 'X005', '2026-01-12', N'Bảo hành dịch vụ cân chỉnh góc lái.', N'Liên hệ gara nếu hiện tượng rung tái diễn.');

    INSERT INTO dbo.PhieuBanGiao
        (maPhieuBanGiao, maXe, maKH, tenKhachHang, maNV,
         tinhTrangXeHienTai, tenNguoiNhan, soDienThoaiNguoiNhan)
    VALUES
    ('PBG001', 'X001', 'KH001', N'Nguyễn Minh Anh', 'NV001', N'Đã thay dầu, động cơ hoạt động ổn định.', N'Nguyễn Minh Anh', '0901000001'),
    ('PBG002', 'X002', 'KH002', N'Trần Quốc Bảo', 'NV002', N'Đã thay má phanh, phanh hoạt động tốt.', N'Trần Quốc Bảo', '0901000002'),
    ('PBG003', 'X003', 'KH003', N'Lê Thu Hà', 'NV001', N'Đã bảo dưỡng và thay lọc gió.', N'Lê Thu Hà', '0901000003'),
    ('PBG004', 'X004', 'KH004', N'Phạm Đức Long', 'NV002', N'Đã thay ắc quy và kiểm tra hệ thống điện.', N'Phạm Đức Long', '0901000004'),
    ('PBG005', 'X007', 'KH007', N'Bùi Thanh Tâm', 'NV001', N'Xe đã kiểm tra tổng quát và thay bugi.', N'Bùi Thanh Tâm', '0901000007');

    INSERT INTO dbo.PhieuDoiTra (maPhieuDoiTra, maHoaDon, lyDoDoiTra, hinhThucXuLy) VALUES
    ('PDT001', 'HD001', N'Khách yêu cầu kiểm tra lại phụ tùng sau khi phát hiện rò rỉ.', N'Kiểm tra bảo hành'),
    ('PDT002', 'HD002', N'Khách phản ánh tiếng kêu ở cụm phanh.', N'Tiếp nhận bảo hành'),
    ('PDT003', 'HD007', N'Khách cần điều chỉnh lịch kiểm tra sau sửa chữa.', N'Điều chỉnh lịch hẹn'),
    ('PDT004', 'HD008', N'Khách mua nhầm loại cảm biến áp suất lốp.', N'Đổi phụ tùng');

    INSERT INTO dbo.PhieuNhapXuatKho (maPhieuNhapXuat, maNV, ngayLapPhieu, kieuPhieu) VALUES
    ('PN001', 'NV009', '2026-10-01T08:00:00', N'Nhập'),
    ('PN002', 'NV010', '2026-10-05T09:00:00', N'Nhập'),
    ('PN003', 'NV009', '2026-10-10T10:00:00', N'Nhập'),
    ('PX001', 'NV003', '2026-10-28T08:30:00', N'Xuất'),
    ('PX002', 'NV004', '2026-10-29T08:30:00', N'Xuất'),
    ('PX003', 'NV009', '2026-10-31T13:00:00', N'Xuất');

    INSERT INTO dbo.ChiTietPhieuNhap
        (maPhieuNhapXuat, maPhuTung, soLuong, donGia, ghiChu)
    VALUES
    ('PN001', 'PT001', 20, 120000, N'Tồn đầu kỳ và nhập bổ sung tháng 10.'),
    ('PN001', 'PT002', 15, 150000, N'Tồn đầu kỳ tháng 10.'),
    ('PN001', 'PT003', 12, 80000, N'Tồn đầu kỳ tháng 10.'),
    ('PN001', 'PT004', 10, 450000, N'Tồn đầu kỳ tháng 10.'),
    ('PN002', 'PT005', 8, 1200000, N'Tồn đầu kỳ tháng 10.'),
    ('PN002', 'PT006', 12, 240000, N'Tồn đầu kỳ tháng 10.'),
    ('PN002', 'PT007', 10, 650000, N'Tồn đầu kỳ tháng 10.'),
    ('PN002', 'PT008', 6, 300000, N'Tồn đầu kỳ tháng 10.'),
    ('PN003', 'PT009', 5, 70000, N'Tồn đầu kỳ tháng 10.'),
    ('PN003', 'PT010', 5, 180000, N'Tồn đầu kỳ tháng 10.'),
    ('PN003', 'PT011', 4, 160000, N'Tồn đầu kỳ tháng 10.'),
    ('PN003', 'PT012', 3, 600000, N'Tồn đầu kỳ tháng 10.'),
    ('PX001', 'PT001', 1, 120000, N'Xuất lắp cho phiếu sửa chữa PSC001.'),
    ('PX001', 'PT004', 1, 450000, N'Xuất lắp cho phiếu sửa chữa PSC002.'),
    ('PX002', 'PT002', 1, 150000, N'Xuất lắp cho phiếu sửa chữa PSC003.'),
    ('PX002', 'PT005', 1, 1200000, N'Xuất lắp cho phiếu sửa chữa PSC004; đã hoàn kho theo bảo hành.'),
    ('PX002', 'PT006', 1, 240000, N'Xuất lắp cho phiếu sửa chữa PSC005.'),
    ('PX003', 'PT003', 1, 80000, N'Xuất lắp cho phiếu sửa chữa PSC007.'),
    ('PX003', 'PT007', 1, 650000, N'Xuất lắp cho phiếu sửa chữa PSC006.'),
    ('PX003', 'PT008', 1, 300000, N'Xuất bán lẻ theo hóa đơn HD008.'),
    ('PX003', 'PT012', 3, 600000, N'Xuất hết hàng tồn không còn đủ điều kiện kinh doanh.');

    UPDATE ptn
    SET tienDo = CASE
        WHEN EXISTS (
            SELECT 1
            FROM dbo.PhieuKiemTra pkt
            JOIN dbo.PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra
            JOIN dbo.HoaDon hd ON hd.maPhieuSuaChua = psc.maPhieuSuaChua
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
        ) AND NOT EXISTS (
            SELECT 1
            FROM dbo.PhieuKiemTra pkt
            JOIN dbo.PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra
            JOIN dbo.HoaDon hd ON hd.maPhieuSuaChua = psc.maPhieuSuaChua
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
                AND hd.trangThai <> N'Đã thanh toán'
        ) THEN N'Hoàn thành'
        WHEN EXISTS (
            SELECT 1
            FROM dbo.PhieuKiemTra pkt
            JOIN dbo.PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra
            JOIN dbo.HoaDon hd ON hd.maPhieuSuaChua = psc.maPhieuSuaChua
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
        ) THEN N'Đã lập hóa đơn'
        WHEN EXISTS (
            SELECT 1
            FROM dbo.PhieuKiemTra pkt
            JOIN dbo.PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
        ) THEN N'Đã lập phiếu sửa chữa'
        WHEN EXISTS (
            SELECT 1
            FROM dbo.PhieuKiemTra pkt
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
        ) THEN N'Đã lập phiếu kiểm tra'
        ELSE N'Đã tiếp nhận'
    END
    FROM dbo.PhieuTiepNhan ptn;

    COMMIT TRANSACTION;
    PRINT N'Đã nạp bộ dữ liệu mẫu mở rộng thành công.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

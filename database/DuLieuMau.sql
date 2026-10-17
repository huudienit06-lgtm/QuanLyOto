USE a1;

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (SELECT 1 FROM NhanVien WHERE maNV LIKE 'DMNV%')
    BEGIN
        THROW 50001, N'Dữ liệu mẫu đã tồn tại. Không chạy file này lần thứ hai nếu chưa xóa dữ liệu mẫu cũ.', 1;
    END;

    INSERT INTO KhachHang (maKH, hoTen, soDT, email, diaChi, congNo) VALUES
    ('DMKH01', N'Nguyễn Minh Anh', '0901234567', 'minhanh@example.com', N'Quận 1, TP. Hồ Chí Minh', 0),
    ('DMKH02', N'Trần Quốc Bảo', '0912345678', 'quocbao@example.com', N'Thủ Đức, TP. Hồ Chí Minh', 500000),
    ('DMKH03', N'Lê Thu Hà', '0923456789', 'thuha@example.com', N'Quận Hải Châu, Đà Nẵng', 0),
    ('DMKH04', N'Phạm Đức Long', '0934567890', 'duclong@example.com', N'Quận Ninh Kiều, Cần Thơ', 1200000),
    ('DMKH05', N'Võ Ngọc Mai', '0945678901', 'ngocmai@example.com', N'Quận Ba Đình, Hà Nội', 0);

    INSERT INTO NhanVien (maNV, hoTen, chucVu, soDT, email, trangThai) VALUES
    ('DMNV01', N'Nguyễn Thị Lan', N'Lễ tân', '0901111001', 'lan.dm@example.com', N'Đang làm việc'),
    ('DMNV02', N'Trần Thị Hoa', N'Lễ tân', '0901111002', 'hoa.dm@example.com', N'Đang làm việc'),
    ('DMNV03', N'Lê Văn Nam', N'Lễ tân', '0901111003', 'nam.dm@example.com', N'Đang làm việc'),
    ('DMNV04', N'Phạm Văn Hùng', N'Kỹ thuật viên', '0901111004', 'hung.dm@example.com', N'Đang làm việc'),
    ('DMNV05', N'Võ Thành Đạt', N'Kỹ thuật viên', '0901111005', 'dat.dm@example.com', N'Đang làm việc'),
    ('DMNV06', N'Đặng Quốc Việt', N'Kỹ thuật viên', '0901111006', 'viet.dm@example.com', N'Đang làm việc'),
    ('DMNV07', N'Bùi Minh Tâm', N'Kỹ thuật viên', '0901111007', 'tam.dm@example.com', N'Đang làm việc'),
    ('DMNV08', N'Hoàng Gia Bảo', N'Kỹ thuật viên', '0901111008', 'baogh.dm@example.com', N'Đang làm việc'),
    ('DMNV09', N'Nguyễn Hoàng Phúc', N'Quản lý', '0901111009', 'phuc.dm@example.com', N'Đang làm việc'),
    ('DMNV10', N'Trần Quốc Khánh', N'Quản lý', '0901111010', 'khanh.dm@example.com', N'Đang làm việc');

    INSERT INTO TaiKhoan (maNV, tenDangNhap, matKhau, vaiTro, trangThai) VALUES
    ('DMNV01', 'DMNV01', '123456', N'Nhân viên', N'Hoạt động'),
    ('DMNV02', 'DMNV02', '123456', N'Nhân viên', N'Hoạt động'),
    ('DMNV03', 'DMNV03', '123456', N'Nhân viên', N'Hoạt động'),
    ('DMNV04', 'DMNV04', '123456', N'Nhân viên', N'Hoạt động'),
    ('DMNV05', 'DMNV05', '123456', N'Nhân viên', N'Hoạt động'),
    ('DMNV06', 'DMNV06', '123456', N'Nhân viên', N'Hoạt động'),
    ('DMNV07', 'DMNV07', '123456', N'Nhân viên', N'Hoạt động'),
    ('DMNV08', 'DMNV08', '123456', N'Nhân viên', N'Hoạt động'),
    ('DMNV09', 'DMNV09', '654321', N'Quản lý', N'Hoạt động'),
    ('DMNV10', 'DMNV10', '654321', N'Quản lý', N'Hoạt động');

    INSERT INTO Xe (maXe, bienSo, hangXe, dongXe, namSanXuat, soKhung, soMay, maKH) VALUES
    ('DMXE01', '51A-12345', N'Toyota', N'Vios', 2021, 'DMVIN001', 'DMMOT001', 'DMKH01'),
    ('DMXE02', '51B-23456', N'Honda', N'City', 2020, 'DMVIN002', 'DMMOT002', 'DMKH02'),
    ('DMXE03', '43A-34567', N'Hyundai', N'Accent', 2022, 'DMVIN003', 'DMMOT003', 'DMKH03'),
    ('DMXE04', '65A-45678', N'Mazda', N'CX-5', 2019, 'DMVIN004', 'DMMOT004', 'DMKH04'),
    ('DMXE05', '30F-56789', N'Ford', N'Ranger', 2023, 'DMVIN005', 'DMMOT005', 'DMKH05');

    INSERT INTO CaLamViec (maCa, gioBatDau, gioKetThuc) VALUES
    ('DMCA01', '07:00', '11:00'),
    ('DMCA02', '11:00', '15:00'),
    ('DMCA03', '15:00', '19:00'),
    ('DMCA04', '07:00', '15:00'),
    ('DMCA05', '11:00', '19:00');

    INSERT INTO LichLamViec (maLich, maCa, maNV, ngayLamViec, trangThai) VALUES
    ('DMLL01', 'DMCA01', 'DMNV01', '2026-10-19', N'Đã phân công'),
    ('DMLL02', 'DMCA02', 'DMNV02', '2026-10-19', N'Đã phân công'),
    ('DMLL03', 'DMCA03', 'DMNV04', '2026-10-19', N'Đã phân công'),
    ('DMLL04', 'DMCA04', 'DMNV05', '2026-10-20', N'Đã phân công'),
    ('DMLL05', 'DMCA05', 'DMNV09', '2026-10-20', N'Đã phân công');

    INSERT INTO PhuTung (maPhuTung, tenPhuTung, donViTinh, giaNhap, giaBan, soLuongTon, nhaCungCap, trangThai) VALUES
    ('DMPT01', N'Lọc dầu động cơ', N'Cái', 120000, 180000, 30, N'Phụ tùng Minh Phát', N'Đang kinh doanh'),
    ('DMPT02', N'Lọc gió động cơ', N'Cái', 150000, 220000, 25, N'Phụ tùng Thành Công', N'Đang kinh doanh'),
    ('DMPT03', N'Bugi đánh lửa', N'Cái', 80000, 130000, 40, N'Phụ tùng Minh Phát', N'Đang kinh doanh'),
    ('DMPT04', N'Má phanh trước', N'Bộ', 450000, 650000, 15, N'Phụ tùng An Phú', N'Đang kinh doanh'),
    ('DMPT05', N'Ắc quy 12V', N'Bình', 1200000, 1550000, 10, N'Phụ tùng Thành Công', N'Đang kinh doanh');

    INSERT INTO DichVu (maDV, tenDV, moTaDV, giaDV) VALUES
    ('DMDV01', N'Thay dầu động cơ', N'Thay dầu và kiểm tra mức dầu động cơ.', 250000),
    ('DMDV02', N'Bảo dưỡng hệ thống phanh', N'Kiểm tra và bảo dưỡng hệ thống phanh.', 400000),
    ('DMDV03', N'Vệ sinh lọc gió', N'Vệ sinh hoặc thay lọc gió động cơ.', 150000),
    ('DMDV04', N'Kiểm tra hệ thống điện', N'Kiểm tra ắc quy và hệ thống điện xe.', 200000),
    ('DMDV05', N'Cân chỉnh góc lái', N'Kiểm tra và cân chỉnh góc đặt bánh xe.', 500000);

    INSERT INTO ChuongTrinhKhuyenMai (maKM, tenKM, tileGiam, ngayBatDau, ngayKetThuc, dieuKienApDung, trangThai) VALUES
    ('DMKM01', N'Ưu đãi khách hàng mới', 5, '2026-01-01', '2026-12-31', N'Giảm 5 phần trăm cho khách hàng mới.', N'Đang áp dụng'),
    ('DMKM02', N'Ưu đãi bảo dưỡng', 10, '2026-01-01', '2026-12-31', N'Áp dụng cho dịch vụ bảo dưỡng.', N'Đang áp dụng'),
    ('DMKM03', N'Ưu đãi thay dầu', 7, '2026-06-01', '2026-12-31', N'Áp dụng khi thay dầu động cơ.', N'Đang áp dụng'),
    ('DMKM04', N'Ưu đãi tháng tri ân', 8, '2026-10-01', '2026-10-31', N'Áp dụng trong tháng tri ân khách hàng.', N'Đang áp dụng'),
    ('DMKM05', N'Ưu đãi khách hàng thân thiết', 12, '2026-01-01', '2026-12-31', N'Áp dụng cho khách hàng thân thiết.', N'Đang áp dụng');

    INSERT INTO PhieuTiepNhan (maPhieuTiepNhan, maXe, maKH, maNV, ngayLap, trangThai, ghiChu) VALUES
    ('DMTN01', 'DMXE01', 'DMKH01', 'DMNV01', '2026-10-15T08:30:00', N'Đã tiếp nhận', N'Khách báo động cơ có tiếng ồn.'),
    ('DMTN02', 'DMXE02', 'DMKH02', 'DMNV02', '2026-10-15T09:15:00', N'Đã tiếp nhận', N'Kiểm tra phanh trước.'),
    ('DMTN03', 'DMXE03', 'DMKH03', 'DMNV03', '2026-10-16T10:00:00', N'Đang sửa chữa', N'Xe cần bảo dưỡng định kỳ.'),
    ('DMTN04', 'DMXE04', 'DMKH04', 'DMNV01', '2026-10-16T13:20:00', N'Đang sửa chữa', N'Kiểm tra hệ thống điện.'),
    ('DMTN05', 'DMXE05', 'DMKH05', 'DMNV02', '2026-10-17T08:45:00', N'Chờ thanh toán', N'Kiểm tra tổng quát trước chuyến đi.');

    INSERT INTO PhieuKiemTra (maPhieuKiemTra, maPhieuTiepNhan, maKH, maXe, maNV, trangThaiBanDau, vanDeHuHong) VALUES
    ('DMKT01', 'DMTN01', 'DMKH01', 'DMXE01', 'DMNV04', N'Động cơ hoạt động, có tiếng ồn nhẹ.', N'Cần thay dầu và kiểm tra lọc dầu.'),
    ('DMKT02', 'DMTN02', 'DMKH02', 'DMXE02', 'DMNV05', N'Phanh hoạt động nhưng má phanh mòn.', N'Cần thay má phanh trước.'),
    ('DMKT03', 'DMTN03', 'DMKH03', 'DMXE03', 'DMNV06', N'Lọc gió bẩn, xe đến kỳ bảo dưỡng.', N'Cần vệ sinh lọc gió và thay dầu.'),
    ('DMKT04', 'DMTN04', 'DMKH04', 'DMXE04', 'DMNV07', N'Ắc quy yếu, hệ thống điện cần kiểm tra.', N'Cần kiểm tra và thay ắc quy nếu cần.'),
    ('DMKT05', 'DMTN05', 'DMKH05', 'DMXE05', 'DMNV08', N'Xe hoạt động bình thường.', N'Cân chỉnh góc lái và kiểm tra tổng quát.');

    INSERT INTO PhieuSuaChua (maPhieuSuaChua, maPhieuKiemTra, maXe, maKH, maNV, ngayBatDau, ngayHoanThanhDuKien) VALUES
    ('DMSC01', 'DMKT01', 'DMXE01', 'DMKH01', 'DMNV04', '2026-10-15T09:00:00', '2026-10-15T12:00:00'),
    ('DMSC02', 'DMKT02', 'DMXE02', 'DMKH02', 'DMNV05', '2026-10-15T10:00:00', '2026-10-15T15:00:00'),
    ('DMSC03', 'DMKT03', 'DMXE03', 'DMKH03', 'DMNV06', '2026-10-16T10:30:00', '2026-10-16T16:00:00'),
    ('DMSC04', 'DMKT04', 'DMXE04', 'DMKH04', 'DMNV07', '2026-10-16T14:00:00', '2026-10-17T12:00:00'),
    ('DMSC05', 'DMKT05', 'DMXE05', 'DMKH05', 'DMNV08', '2026-10-17T09:00:00', '2026-10-17T14:00:00');

    INSERT INTO ChiTietPhieuSuaChua (maPhieuSuaChua, maPhuTung, maDV, soLuong, donGia, tienCong) VALUES
    ('DMSC01', 'DMPT01', 'DMDV01', 1, 180000, 100000),
    ('DMSC02', 'DMPT04', 'DMDV02', 1, 650000, 250000),
    ('DMSC03', 'DMPT02', 'DMDV03', 1, 220000, 80000),
    ('DMSC04', 'DMPT05', 'DMDV04', 1, 1550000, 120000),
    ('DMSC05', 'DMPT03', 'DMDV05', 1, 130000, 200000);

    INSERT INTO HoaDon (maHoaDon, maPhieuSuaChua, maNV, maKM, ngayLap, tienGiamGia, thueVAT, soTien, trangThai) VALUES
    ('DMHD01', 'DMSC01', 'DMNV01', 'DMKM03', '2026-10-15T12:00:00', 37100, 8, 532332, N'Đã thanh toán'),
    ('DMHD02', 'DMSC02', 'DMNV02', 'DMKM02', '2026-10-15T15:00:00', 130000, 8, 1263600, N'Đã thanh toán'),
    ('DMHD03', 'DMSC03', 'DMNV03', 'DMKM01', '2026-10-16T16:00:00', 22500, 8, 461700, N'Đã thanh toán'),
    ('DMHD04', 'DMSC04', 'DMNV01', 'DMKM04', '2026-10-17T12:00:00', 149600, 8, 1858032, N'Chưa thanh toán'),
    ('DMHD05', 'DMSC05', 'DMNV02', 'DMKM05', '2026-10-17T14:00:00', 99600, 8, 788832, N'Đã thanh toán');

    INSERT INTO ChiTietHoaDon (maHoaDon, maPhieuSuaChua, maPhuTung, maDV, soLuong, donGia) VALUES
    ('DMHD01', 'DMSC01', 'DMPT01', 'DMDV01', 1, 180000),
    ('DMHD02', 'DMSC02', 'DMPT04', 'DMDV02', 1, 650000),
    ('DMHD03', 'DMSC03', 'DMPT02', 'DMDV03', 1, 220000),
    ('DMHD04', 'DMSC04', 'DMPT05', 'DMDV04', 1, 1550000),
    ('DMHD05', 'DMSC05', 'DMPT03', 'DMDV05', 1, 130000);

    INSERT INTO GiaoDichThanhToan (maGD, maHoaDon, phuongThucThanhToan, thoiGianGD, trangThaiGD) VALUES
    ('DMGD01', 'DMHD01', N'Tiền mặt', '2026-10-15T12:10:00', N'Thành công'),
    ('DMGD02', 'DMHD02', N'Chuyển khoản', '2026-10-15T15:10:00', N'Thành công'),
    ('DMGD03', 'DMHD03', N'Tiền mặt', '2026-10-16T16:10:00', N'Thành công'),
    ('DMGD04', 'DMHD04', N'Tiền mặt', '2026-10-17T12:10:00', N'Chờ thanh toán'),
    ('DMGD05', 'DMHD05', N'Chuyển khoản', '2026-10-17T14:10:00', N'Thành công');

    INSERT INTO PhieuBaoHanh (maPhieuBaoHanh, maNV, maKH, maXe, ngayHetHanBaoHanh, noiDungBaoHanh, dieuKienBaoHanh) VALUES
    ('DMBH01', 'DMNV04', 'DMKH01', 'DMXE01', '2027-10-15', N'Bảo hành công việc thay dầu.', N'Áp dụng khi bảo dưỡng đúng lịch.'),
    ('DMBH02', 'DMNV05', 'DMKH02', 'DMXE02', '2027-10-15', N'Bảo hành má phanh đã thay.', N'Không áp dụng với hao mòn tự nhiên.'),
    ('DMBH03', 'DMNV06', 'DMKH03', 'DMXE03', '2027-10-16', N'Bảo hành phụ tùng lọc gió.', N'Giữ hóa đơn để được bảo hành.'),
    ('DMBH04', 'DMNV07', 'DMKH04', 'DMXE04', '2027-10-17', N'Bảo hành ắc quy đã thay.', N'Áp dụng theo điều kiện nhà sản xuất.'),
    ('DMBH05', 'DMNV08', 'DMKH05', 'DMXE05', '2027-10-17', N'Bảo hành công việc cân chỉnh.', N'Liên hệ gara nếu lỗi tái diễn.');

    INSERT INTO PhieuDoiTra (maPhieuDoiTra, maHoaDon, lyDoDoiTra, hinhThucXuLy) VALUES
    ('DMDT01', 'DMHD01', N'Khách báo phụ tùng không phù hợp.', N'Đổi phụ tùng'),
    ('DMDT02', 'DMHD02', N'Khách yêu cầu kiểm tra lại má phanh.', N'Kiểm tra lại'),
    ('DMDT03', 'DMHD03', N'Khách muốn đổi lịch bảo dưỡng.', N'Điều chỉnh dịch vụ'),
    ('DMDT04', 'DMHD04', N'Phụ tùng giao nhầm quy cách.', N'Đổi phụ tùng'),
    ('DMDT05', 'DMHD05', N'Khách yêu cầu kiểm tra lại góc lái.', N'Kiểm tra lại');

    INSERT INTO PhieuNhapXuatKho (maPhieuNhapXuat, maNV, ngayLapPhieu, kieuPhieu) VALUES
    ('DMPNK01', 'DMNV09', '2026-10-01T08:00:00', N'Nhập'),
    ('DMPNK02', 'DMNV10', '2026-10-03T09:00:00', N'Nhập'),
    ('DMPNK03', 'DMNV09', '2026-10-08T10:00:00', N'Xuất'),
    ('DMPNK04', 'DMNV10', '2026-10-12T11:00:00', N'Nhập'),
    ('DMPNK05', 'DMNV09', '2026-10-16T13:00:00', N'Xuất');

    INSERT INTO ChiTietPhieuNhap (maPhieuNhapXuat, maPhuTung, soLuong, donGia, ghiChu) VALUES
    ('DMPNK01', 'DMPT01', 20, 120000, N'Nhập hàng tháng 10.'),
    ('DMPNK02', 'DMPT02', 15, 150000, N'Nhập hàng tháng 10.'),
    ('DMPNK03', 'DMPT03', 2, 80000, N'Xuất dùng cho phiếu sửa chữa DMSC05.'),
    ('DMPNK04', 'DMPT04', 10, 450000, N'Nhập bổ sung hàng tồn.'),
    ('DMPNK05', 'DMPT05', 1, 1200000, N'Xuất dùng cho phiếu sửa chữa DMSC04.');

    COMMIT TRANSACTION;
    PRINT N'Đã thêm dữ liệu mẫu thành công.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;

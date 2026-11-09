-- Chạy file này trước DuLieuMau.sql trên SQL Server.
-- CẢNH BÁO: Script xóa và tạo lại database a1, bao gồm toàn bộ dữ liệu hiện có.
USE master;
GO

IF DB_ID(N'a1') IS NOT NULL
BEGIN
    ALTER DATABASE a1 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE a1;
END;

CREATE DATABASE a1;
GO

USE a1;
GO

CREATE TABLE KhachHang (
    maKH VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDT VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    diaChi NVARCHAR(255),
    congNo FLOAT NOT NULL DEFAULT 0
);

CREATE TABLE Xe (
    maXe VARCHAR(20) PRIMARY KEY,
    bienSo VARCHAR(20) NOT NULL UNIQUE,
    hangXe NVARCHAR(50) NOT NULL,
    dongXe NVARCHAR(50) NOT NULL,
    namSanXuat INT NOT NULL,
    soKhung VARCHAR(50) NOT NULL,
    soMay VARCHAR(50) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    CONSTRAINT FK_Xe_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH) ON DELETE CASCADE
);

-- Chức vụ là danh mục riêng; NhanVien tham chiếu bằng maChucVu.
CREATE TABLE dbo.ChucVu (
    maChucVu VARCHAR(20) PRIMARY KEY,
    tenChucVu NVARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE dbo.NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    maChucVu VARCHAR(20) NOT NULL,
    soDT VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    trangThai NVARCHAR(20) NOT NULL,
    CONSTRAINT FK_NhanVien_ChucVu FOREIGN KEY (maChucVu) REFERENCES dbo.ChucVu(maChucVu)
);

CREATE TABLE TaiKhoan (
    maNV VARCHAR(20) PRIMARY KEY,
    tenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    matKhau VARCHAR(255) NOT NULL,
    vaiTro NVARCHAR(50) NOT NULL,
    trangThai NVARCHAR(20) NOT NULL,
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV) ON DELETE CASCADE
);

CREATE TABLE CaLamViec (
    maCa VARCHAR(20) PRIMARY KEY,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL
);

CREATE TABLE LichLamViec (
    maLich VARCHAR(20) PRIMARY KEY,
    maCa VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayLamViec DATE NOT NULL,
    trangThai NVARCHAR(20) NOT NULL,
    CONSTRAINT FK_LichLamViec_CaLamViec FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa),
    CONSTRAINT FK_LichLamViec_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV) 
);

CREATE TABLE PhuTung (
    maPhuTung VARCHAR(20) PRIMARY KEY,
    tenPhuTung NVARCHAR(100) NOT NULL,
    donViTinh NVARCHAR(20) NOT NULL,
    giaNhap FLOAT NOT NULL,
    giaBan FLOAT NOT NULL,
    soLuongTon INT NOT NULL DEFAULT 0,
    nhaCungCap NVARCHAR(100),
    trangThai NVARCHAR(20) NOT NULL
);

CREATE TABLE ChinhSachBaoHanh (
    maCSBH VARCHAR(10) PRIMARY KEY,
    tenChinhSach NVARCHAR(150) NOT NULL,
    thoiHanBaoHanh INT NOT NULL,
    donViThoiGian VARCHAR(10) NOT NULL,
    dieuKienBaoHanh NVARCHAR(1000),
    truongHopTuChoi NVARCHAR(1000),
    noiDungBaoHanh NVARCHAR(1000),
    ngayTao DATETIME2 NOT NULL CONSTRAINT DF_CSBH_ngayTao DEFAULT SYSDATETIME(),
    trangThai BIT NOT NULL CONSTRAINT DF_CSBH_trangThai DEFAULT 1,
    CONSTRAINT CK_CSBH_thoiHan CHECK (thoiHanBaoHanh > 0),
    CONSTRAINT CK_CSBH_donVi CHECK (donViThoiGian IN ('NGAY', 'THANG', 'NAM'))
);

CREATE TABLE ChinhSachBaoHanh_PhuTung (
    maCSBH VARCHAR(10) NOT NULL,
    maPhuTung VARCHAR(20) NOT NULL,
    CONSTRAINT PK_CSBH_PhuTung PRIMARY KEY (maCSBH, maPhuTung),
    CONSTRAINT FK_CSBHPT_ChinhSach FOREIGN KEY (maCSBH)
        REFERENCES ChinhSachBaoHanh(maCSBH) ON DELETE CASCADE,
    CONSTRAINT FK_CSBHPT_PhuTung FOREIGN KEY (maPhuTung)
        REFERENCES PhuTung(maPhuTung)
);

CREATE INDEX IX_CSBHPT_PhuTung ON ChinhSachBaoHanh_PhuTung(maPhuTung, maCSBH);

CREATE TABLE DichVu (
    maDV VARCHAR(20) PRIMARY KEY,
    tenDV NVARCHAR(100) NOT NULL,
    moTaDV NVARCHAR(MAX),
    giaDV FLOAT NOT NULL
);

CREATE TABLE PhieuTiepNhan (
    maPhieuTiepNhan VARCHAR(20) PRIMARY KEY,
    maXe VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayLap DATETIME NOT NULL,
    trangThai NVARCHAR(20) NOT NULL,
    tienDo NVARCHAR(40) NOT NULL
        CONSTRAINT DF_PTN_tienDo DEFAULT N'Đã tiếp nhận',
    ghiChu NVARCHAR(MAX),
    CONSTRAINT CK_PTN_tienDo CHECK (tienDo IN (
        N'Đã tiếp nhận', N'Đã lập phiếu kiểm tra', N'Đã lập phiếu sửa chữa',
        N'Đã lập hóa đơn', N'Hoàn thành'
    )),
    CONSTRAINT FK_PTN_Xe FOREIGN KEY (maXe) REFERENCES Xe(maXe),
    CONSTRAINT FK_PTN_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PTN_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE PhieuKiemTra (
    maPhieuKiemTra VARCHAR(20) PRIMARY KEY,
    maPhieuTiepNhan VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    maXe VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    trangThai NVARCHAR(20) NOT NULL
        CONSTRAINT DF_PKT_trangThai DEFAULT N'Chờ kiểm tra',
    trangThaiBanDau NVARCHAR(MAX) NOT NULL,
    vanDeHuHong NVARCHAR(MAX) NOT NULL,
    CONSTRAINT CK_PKT_trangThai CHECK (trangThai IN (N'Chờ kiểm tra', N'Đã kiểm tra')),
    CONSTRAINT FK_PKT_PhieuTiepNhan FOREIGN KEY (maPhieuTiepNhan) REFERENCES PhieuTiepNhan(maPhieuTiepNhan),
    CONSTRAINT FK_PKT_KH FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PKT_Xe FOREIGN KEY (maXe) REFERENCES Xe(maXe),
    CONSTRAINT FK_PKT_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE PhieuSuaChua (
    maPhieuSuaChua VARCHAR(20) PRIMARY KEY,
    maPhieuKiemTra VARCHAR(20) NOT NULL,
    maXe VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayBatDau DATETIME NOT NULL,
    ngayHoanThanhDuKien DATETIME NOT NULL,
    trangThai NVARCHAR(20) NOT NULL CONSTRAINT DF_PSC_trangThai DEFAULT N'Chờ sửa chữa',
    CONSTRAINT CK_PSC_trangThai CHECK (trangThai IN (N'Chờ sửa chữa', N'Đã sửa chữa')),
    CONSTRAINT FK_PSC_PKT FOREIGN KEY (maPhieuKiemTra) REFERENCES PhieuKiemTra(maPhieuKiemTra),
    CONSTRAINT FK_PSC_Xe FOREIGN KEY (maXe) REFERENCES Xe(maXe),
    CONSTRAINT FK_PSC_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PSC_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE ChiTietPhieuSuaChua (
    maChiTiet BIGINT IDENTITY(1,1) PRIMARY KEY,
    maPhieuSuaChua VARCHAR(20) NOT NULL,
    maPhuTung VARCHAR(20),
    maDV VARCHAR(20),
    soLuong INT NOT NULL DEFAULT 1,
    donGia DECIMAL(18,2) NOT NULL DEFAULT 0,
    CONSTRAINT CK_CTPSC_MotLoai CHECK (
        (maPhuTung IS NOT NULL AND maDV IS NULL)
        OR (maPhuTung IS NULL AND maDV IS NOT NULL)
    ),
    CONSTRAINT FK_CTPSC_PhieuSuaChua FOREIGN KEY (maPhieuSuaChua) REFERENCES PhieuSuaChua(maPhieuSuaChua) ON DELETE CASCADE,
    CONSTRAINT FK_CTPSC_PhuTung FOREIGN KEY (maPhuTung) REFERENCES PhuTung(maPhuTung),
    CONSTRAINT FK_CTPSC_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV)
);

CREATE TABLE ChuongTrinhKhuyenMai (
    maKM VARCHAR(20) PRIMARY KEY,
    tenKM NVARCHAR(100) NOT NULL,
    tileGiam FLOAT NOT NULL,
    ngayBatDau DATE NOT NULL,
    ngayKetThuc DATE NOT NULL,
    dieuKienApDung NVARCHAR(MAX),
    trangThai NVARCHAR(20) NOT NULL
);

CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    maPhieuSuaChua VARCHAR(20) NULL,
    maNV VARCHAR(20) NOT NULL,
    maKM VARCHAR(20) NULL,
    ngayLap DATETIME NOT NULL,
    tienGiamGia DECIMAL(18,2) NOT NULL DEFAULT 0,
    thueVAT DECIMAL(18,2) NOT NULL DEFAULT 0,
    soTien DECIMAL(18,2) NOT NULL DEFAULT 0,
    trangThai NVARCHAR(20) NOT NULL,
    CONSTRAINT FK_HD_PhieuSuaChua FOREIGN KEY (maPhieuSuaChua) REFERENCES PhieuSuaChua(maPhieuSuaChua),
    CONSTRAINT FK_HD_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HD_ChuongTrinhKM FOREIGN KEY (maKM) REFERENCES ChuongTrinhKhuyenMai(maKM)
);
-- Bổ sung mã khách hàng trực tiếp vào hóa đơn
ALTER TABLE HoaDon
ADD maKH VARCHAR(20) NULL;
-- Tạo khóa ngoại đến bảng KhachHang
ALTER TABLE HoaDon
ADD CONSTRAINT FK_HoaDon_KhachHang
FOREIGN KEY (maKH) REFERENCES KhachHang(maKH);

CREATE TABLE ChiTietHoaDon (
    maChiTietHD BIGINT IDENTITY(1,1) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL,
    maPhieuSuaChua VARCHAR(20) NULL, 
    maPhuTung VARCHAR(20) NULL,
    maDV VARCHAR(20) NULL,
    soLuong INT NOT NULL DEFAULT 1,
    donGia DECIMAL(18,2) NOT NULL DEFAULT 0,
    CONSTRAINT CK_CTHD_MotLoai CHECK (
        (maPhuTung IS NOT NULL AND maDV IS NULL)
        OR (maPhuTung IS NULL AND maDV IS NOT NULL)
    ),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_PhieuSuaChua FOREIGN KEY (maPhieuSuaChua) REFERENCES PhieuSuaChua(maPhieuSuaChua),
    CONSTRAINT FK_CTHD_PhuTung FOREIGN KEY (maPhuTung) REFERENCES PhuTung(maPhuTung),
    CONSTRAINT FK_CTHD_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV)
);

CREATE TABLE PhieuYeuCauBaoHanh (
    maPhieuYCBH VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL,
    maPhuTung VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    ngayTiepNhan DATETIME2 NOT NULL CONSTRAINT DF_PYCBH_ngayTiepNhan DEFAULT SYSDATETIME(),
    trangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_PYCBH_trangThai DEFAULT N'Chờ xử lý',
    lyDo NVARCHAR(1000) NOT NULL,
    ghiChuXuLy NVARCHAR(1000),
    ngayXuLy DATETIME2,
    ngayHoanThanh DATETIME2,
    CONSTRAINT CK_PYCBH_soLuong CHECK (soLuong > 0),
    CONSTRAINT CK_PYCBH_trangThai CHECK (
        trangThai IN (N'Chờ xử lý', N'Chấp nhận bảo hành',
                      N'Hoàn thành bảo hành', N'Từ chối bảo hành')
    ),
    CONSTRAINT FK_PYCBH_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_PYCBH_PhuTung FOREIGN KEY (maPhuTung) REFERENCES PhuTung(maPhuTung)
);

CREATE INDEX IX_PYCBH_trangThai_ngayTiepNhan
    ON PhieuYeuCauBaoHanh(trangThai, ngayTiepNhan DESC);

CREATE TABLE GiaoDichThanhToan (
    maGD VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL,
    phuongThucThanhToan NVARCHAR(50) NOT NULL,
    thoiGianGD DATETIME NOT NULL CONSTRAINT DF_GDTT_thoiGian DEFAULT GETDATE(),
    trangThaiGD NVARCHAR(20) NOT NULL,
    soTienThanhToan DECIMAL(18,2) NOT NULL DEFAULT 0,
    CONSTRAINT FK_GDTT_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon)
);

CREATE TABLE PhieuBaoHanh (
    maPhieuBaoHanh VARCHAR(20) PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    maXe VARCHAR(20) NOT NULL,
    ngayHetHanBaoHanh DATE NOT NULL,
    noiDungBaoHanh NVARCHAR(MAX) NOT NULL,
    dieuKienBaoHanh NVARCHAR(MAX),
    CONSTRAINT FK_PBH_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PBH_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PBH_Xe FOREIGN KEY (maXe) REFERENCES Xe(maXe)
);

CREATE TABLE PhieuBanGiao (
    maPhieuBanGiao VARCHAR(20) PRIMARY KEY,
    maXe VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    tenKhachHang NVARCHAR(100) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    tinhTrangXeHienTai NVARCHAR(MAX) NOT NULL,
    tenNguoiNhan NVARCHAR(100) NOT NULL,
    soDienThoaiNguoiNhan VARCHAR(15) NOT NULL,
    CONSTRAINT FK_PBG_Xe FOREIGN KEY (maXe) REFERENCES Xe(maXe),
    CONSTRAINT FK_PBG_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PBG_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL,
    lyDoDoiTra NVARCHAR(MAX) NOT NULL,
    hinhThucXuLy NVARCHAR(50) NOT NULL,
    CONSTRAINT FK_PDT_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon)
);

CREATE TABLE PhieuNhapXuatKho (
    maPhieuNhapXuat VARCHAR(20) PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    ngayLapPhieu DATETIME NOT NULL,
    kieuPhieu NVARCHAR(20) NOT NULL,
    CONSTRAINT FK_PNXK_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE ChiTietPhieuNhap (
    maPhieuNhapXuat VARCHAR(20) NOT NULL,
    maPhuTung VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGia FLOAT NOT NULL,
    ghiChu NVARCHAR(MAX),
    PRIMARY KEY (maPhieuNhapXuat, maPhuTung),
    CONSTRAINT FK_CTPN_PhieuNhapXuat FOREIGN KEY (maPhieuNhapXuat) REFERENCES PhieuNhapXuatKho(maPhieuNhapXuat) ON DELETE CASCADE,
    CONSTRAINT FK_CTPN_PhuTung FOREIGN KEY (maPhuTung) REFERENCES PhuTung(maPhuTung)
);

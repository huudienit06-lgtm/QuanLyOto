CREATE DATABASE a1
USE a1
GO
--
CREATE TABLE KhachHang (
    maKH VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDT VARCHAR(15),
    email VARCHAR(100),
    diaChi NVARCHAR(255),
    congNo FLOAT DEFAULT 0
);

CREATE TABLE Xe (
    maXe VARCHAR(20) PRIMARY KEY,
    bienSo VARCHAR(20) NOT NULL UNIQUE,
    hangXe NVARCHAR(50),
    dongXe NVARCHAR(50),
    namSanXuat INT,
    soKhung VARCHAR(50),
    soMay VARCHAR(50),
    maKH VARCHAR(20) NOT NULL,
    CONSTRAINT FK_Xe_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH) ON DELETE CASCADE
);

CREATE TABLE NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50),
    soDT VARCHAR(15),
    email VARCHAR(100),
    trangThai NVARCHAR(20)
);

CREATE TABLE TaiKhoan (
    maNV VARCHAR(20) PRIMARY KEY,
    tenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    matKhau VARCHAR(255) NOT NULL,
    vaiTro NVARCHAR(50),
    trangThai NVARCHAR(20),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV) ON DELETE CASCADE
);

CREATE TABLE CaLamViec (
    maCa VARCHAR(20) PRIMARY KEY,
    gioBatDau TIME,
    gioKetThuc TIME
);

CREATE TABLE LichLamViec (
    maLich VARCHAR(20) PRIMARY KEY,
    maCa VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayLamViec DATE NOT NULL,
    trangThai NVARCHAR(20),
    CONSTRAINT FK_LichLamViec_CaLamViec FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa),
    CONSTRAINT FK_LichLamViec_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV) 
);

CREATE TABLE PhuTung (
    maPhuTung VARCHAR(20) PRIMARY KEY,
    tenPhuTung NVARCHAR(100) NOT NULL,
    donViTinh NVARCHAR(20),
    giaNhap FLOAT,
    giaBan FLOAT,
    soLuongTon INT DEFAULT 0,
    nhaCungCap NVARCHAR(100),
    trangThai NVARCHAR(20)
);

CREATE TABLE DichVu (
    maDV VARCHAR(20) PRIMARY KEY,
    tenDV NVARCHAR(100) NOT NULL,
    moTaDV NVARCHAR(MAX),
    giaDV FLOAT
);

CREATE TABLE PhieuTiepNhan (
    maPhieuTiepNhan VARCHAR(20) PRIMARY KEY,
    maXe VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayLap DATETIME NOT NULL,
    trangThai NVARCHAR(20),
    ghiChu NVARCHAR(MAX),
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
    trangThaiBanDau NVARCHAR(MAX),
    vanDeHuHong NVARCHAR(MAX),
    CONSTRAINT FK_PKT_PhieuTiepNhan FOREIGN KEY (maPhieuTiepNhan) REFERENCES PhieuTiepNhan(maPhieuTiepNhan),
    CONSTRAINT FK_PKT_KH FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PKT_Xe FOREIGN KEY (maXe) REFERENCES Xe(maXe),
    CONSTRAINT FK_PKT_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE PhieuSuaChua (
    maPhieuSuaChua VARCHAR(20) PRIMARY KEY,
    maPhieuKiemTra VARCHAR(20),
    maXe VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayBatDau DATETIME,
    ngayHoanThanhDuKien DATETIME,
    CONSTRAINT FK_PSC_PKT FOREIGN KEY (maPhieuKiemTra) REFERENCES PhieuKiemTra(maPhieuKiemTra),
    CONSTRAINT FK_PSC_Xe FOREIGN KEY (maXe) REFERENCES Xe(maXe),
    CONSTRAINT FK_PSC_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PSC_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE ChiTietPhieuSuaChua (
    maPhieuSuaChua VARCHAR(20) NOT NULL,
    maPhuTung VARCHAR(20) NOT NULL,
    maDV VARCHAR(20) NOT NULL,
    soLuong INT DEFAULT 1,
    donGia DECIMAL(18,2),
    tienCong DECIMAL(18,2),
    PRIMARY KEY (maPhieuSuaChua, maDV), -- Khóa chính phức hợp gồm mã phiếu sửa chữa và mã dịch vụ
    CONSTRAINT FK_CTPSC_PhieuSuaChua FOREIGN KEY (maPhieuSuaChua) REFERENCES PhieuSuaChua(maPhieuSuaChua) ON DELETE CASCADE,
    CONSTRAINT FK_CTPSC_PhuTung FOREIGN KEY (maPhuTung) REFERENCES PhuTung(maPhuTung),
    CONSTRAINT FK_CTPSC_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV)
);

CREATE TABLE ChuongTrinhKhuyenMai (
    maKM VARCHAR(20) PRIMARY KEY,
    tenKM NVARCHAR(100) NOT NULL,
    tileGiam FLOAT,
    ngayBatDau DATE,
    ngayKetThuc DATE,
    dieuKienApDung NVARCHAR(MAX),
    trangThai NVARCHAR(20)
);

CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    maPhieuSuaChua VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    maKM VARCHAR(20),
    ngayLap DATETIME NOT NULL,
    tienGiamGia FLOAT DEFAULT 0,
    thueVAT FLOAT DEFAULT 0,
    soTien FLOAT,
    trangThai NVARCHAR(20),
    CONSTRAINT FK_HD_PhieuSuaChua FOREIGN KEY (maPhieuSuaChua) REFERENCES PhieuSuaChua(maPhieuSuaChua),
    CONSTRAINT FK_HD_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HD_ChuongTrinhKM FOREIGN KEY (maKM) REFERENCES ChuongTrinhKhuyenMai(maKM)
);

CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(20) NOT NULL,
    maPhieuSuaChua VARCHAR(20) NOT NULL, -- Thêm liên kết tới phiếu sửa chữa
    maPhuTung VARCHAR(20) NOT NULL,
    maDV VARCHAR(20) NOT NULL,
    soLuong INT,
    donGia DECIMAL(18,2),
    PRIMARY KEY (maHoaDon, maPhieuSuaChua), -- Khóa chính phức hợp gồm mã hóa đơn và mã phiếu sửa chữa
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_PhieuSuaChua FOREIGN KEY (maPhieuSuaChua) REFERENCES PhieuSuaChua(maPhieuSuaChua),
    CONSTRAINT FK_CTHD_PhuTung FOREIGN KEY (maPhuTung) REFERENCES PhuTung(maPhuTung),
    CONSTRAINT FK_CTHD_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV)
);

CREATE TABLE GiaoDichThanhToan (
    maGD VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL,
    phuongThucThanhToan NVARCHAR(50),
    thoiGianGD DATETIME,
    trangThaiGD NVARCHAR(20),
    CONSTRAINT FK_GDTT_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon)
);

CREATE TABLE PhieuBaoHanh (
    maPhieuBaoHanh VARCHAR(20) PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    maXe VARCHAR(20) NOT NULL,
    ngayHetHanBaoHanh DATE,
    noiDungBaoHanh NVARCHAR(MAX),
    dieuKienBaoHanh NVARCHAR(MAX),
    CONSTRAINT FK_PBH_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PBH_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PBH_Xe FOREIGN KEY (maXe) REFERENCES Xe(maXe)
);

CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL,
    lyDoDoiTra NVARCHAR(MAX),
    hinhThucXuLy NVARCHAR(50),
    CONSTRAINT FK_PDT_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon)
);

CREATE TABLE PhieuNhapXuatKho (
    maPhieuNhapXuat VARCHAR(20) PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    ngayLapPhieu DATETIME,
    kieuPhieu NVARCHAR(20),
    CONSTRAINT FK_PNXK_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE ChiTietPhieuNhap (
    maPhieuNhapXuat VARCHAR(20) NOT NULL,
    maPhuTung VARCHAR(20) NOT NULL,
    soLuong INT,
    donGia FLOAT,
    ghiChu NVARCHAR(MAX),
    PRIMARY KEY (maPhieuNhapXuat, maPhuTung),
    CONSTRAINT FK_CTPN_PhieuNhapXuat FOREIGN KEY (maPhieuNhapXuat) REFERENCES PhieuNhapXuatKho(maPhieuNhapXuat) ON DELETE CASCADE,
    CONSTRAINT FK_CTPN_PhuTung FOREIGN KEY (maPhuTung) REFERENCES PhuTung(maPhuTung)
);


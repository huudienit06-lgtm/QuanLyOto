package entity;

import java.sql.Timestamp;

public class PhieuSuaChua {
    private String maPhieuSuaChua;
    private PhieuKiemTra maPhieuKiemTra;
    private Xe maXe;
    private KhachHang maKH;
    private NhanVien maNV;
    private Timestamp ngayBatDau;
    private Timestamp ngayHoanThanhDuKien;
    private String trangThai;

    public PhieuSuaChua() {
    }

    public String getMaPhieuSuaChua() {
        return maPhieuSuaChua;
    }

    public void setMaPhieuSuaChua(String maPhieuSuaChua) {
        this.maPhieuSuaChua = maPhieuSuaChua;
    }

    public PhieuKiemTra getMaPhieuKiemTra() {
        return maPhieuKiemTra;
    }

    public void setMaPhieuKiemTra(PhieuKiemTra maPhieuKiemTra) {
        this.maPhieuKiemTra = maPhieuKiemTra;
    }

    public Xe getMaXe() {
        return maXe;
    }

    public void setMaXe(Xe maXe) {
        this.maXe = maXe;
    }

    public KhachHang getMaKH() {
        return maKH;
    }

    public void setMaKH(KhachHang maKH) {
        this.maKH = maKH;
    }

    public NhanVien getMaNV() {
        return maNV;
    }

    public void setMaNV(NhanVien maNV) {
        this.maNV = maNV;
    }

    public Timestamp getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Timestamp ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public Timestamp getNgayHoanThanhDuKien() {
        return ngayHoanThanhDuKien;
    }

    public void setNgayHoanThanhDuKien(Timestamp ngayHoanThanhDuKien) {
        this.ngayHoanThanhDuKien = ngayHoanThanhDuKien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}

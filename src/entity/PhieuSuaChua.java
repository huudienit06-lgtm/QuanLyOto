package entity;

import java.sql.Timestamp;

public class PhieuSuaChua {
    private String maPhieuSuaChua;
    private String maPhieuKiemTra;
    private String maXe;
    private String maKH;
    private String maNV;
    private Timestamp ngayBatDau;
    private Timestamp ngayHoanThanhDuKien;

    public PhieuSuaChua() {
    }

    public String getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(String maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }
    public String getMaPhieuKiemTra() { return maPhieuKiemTra; }
    public void setMaPhieuKiemTra(String maPhieuKiemTra) { this.maPhieuKiemTra = maPhieuKiemTra; }
    public String getMaXe() { return maXe; }
    public void setMaXe(String maXe) { this.maXe = maXe; }
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public Timestamp getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Timestamp ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public Timestamp getNgayHoanThanhDuKien() { return ngayHoanThanhDuKien; }
    public void setNgayHoanThanhDuKien(Timestamp ngayHoanThanhDuKien) { this.ngayHoanThanhDuKien = ngayHoanThanhDuKien; }
}

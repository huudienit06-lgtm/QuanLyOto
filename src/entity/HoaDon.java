package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class HoaDon {
    private String maHoaDon;
    private String maPhieuSuaChua;
    private String maNV;
    private String maKM;
    private Timestamp ngayLap;
    private BigDecimal tienGiamGia;
    private BigDecimal thueVAT;
    private BigDecimal soTien;
    private String trangThai;

    public HoaDon() {
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public String getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(String maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getMaKM() { return maKM; }
    public void setMaKM(String maKM) { this.maKM = maKM; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }
    public BigDecimal getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(BigDecimal tienGiamGia) { this.tienGiamGia = tienGiamGia; }
    public BigDecimal getThueVAT() { return thueVAT; }
    public void setThueVAT(BigDecimal thueVAT) { this.thueVAT = thueVAT; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

package entity;

import java.math.BigDecimal;

public class ChiTietHoaDon {
    private String maHoaDon;
    private String maPhieuSuaChua;
    private String maPhuTung;
    private String maDV;
    private Integer soLuong;
    private BigDecimal donGia;

    public ChiTietHoaDon() {
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public String getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(String maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }
    public String getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(String maPhuTung) { this.maPhuTung = maPhuTung; }
    public String getMaDV() { return maDV; }
    public void setMaDV(String maDV) { this.maDV = maDV; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}

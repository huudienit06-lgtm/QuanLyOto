package entity;

import java.math.BigDecimal;

public class ChiTietPhieuSuaChua {
    private Long maChiTiet;
    private String maPhieuSuaChua;
    private String maPhuTung;
    private String maDV;
    private Integer soLuong;
    private BigDecimal donGia;

    public ChiTietPhieuSuaChua() {
    }

    public Long getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(Long maChiTiet) { this.maChiTiet = maChiTiet; }
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

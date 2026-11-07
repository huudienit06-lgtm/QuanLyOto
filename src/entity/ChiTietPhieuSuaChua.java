package entity;

import java.math.BigDecimal;

public class ChiTietPhieuSuaChua {
    private Long maChiTiet;
    private String maPhieuSuaChua;
    private PhuTung maPhuTung;
    private DichVu maDV;
    private Integer soLuong;
    private BigDecimal donGia;

    public ChiTietPhieuSuaChua() {
    }

    public Long getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(Long maChiTiet) { this.maChiTiet = maChiTiet; }
    public String getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(String maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }
    public PhuTung getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(PhuTung maPhuTung) { this.maPhuTung = maPhuTung; }
    public DichVu getMaDV() { return maDV; }
    public void setMaDV(DichVu dichVu) { this.maDV = dichVu; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}

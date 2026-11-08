package entity;

public class ChiTietPhieuNhap {
    private String maPhieuNhapXuat;
    private String maPhuTung;
    private Integer soLuong;
    private Double donGia;
    private String ghiChu;

    public ChiTietPhieuNhap() {
    }

    public String getMaPhieuNhapXuat() { return maPhieuNhapXuat; }
    public void setMaPhieuNhapXuat(String maPhieuNhapXuat) { this.maPhieuNhapXuat = maPhieuNhapXuat; }
    public String getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(String maPhuTung) { this.maPhuTung = maPhuTung; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public Double getDonGia() { return donGia; }
    public void setDonGia(Double donGia) { this.donGia = donGia; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}

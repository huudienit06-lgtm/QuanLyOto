package entity;

public class ChiTietPhieuNhap {
    private PhieuNhapXuatKho phieuNhapXuatKho; 
    private PhuTung phuTung;                   
    private Integer soLuong;
    private Double donGia;
    private String ghiChu;

    public ChiTietPhieuNhap() {}

    public PhieuNhapXuatKho getPhieuNhapXuatKho() { return phieuNhapXuatKho; }
    public void setPhieuNhapXuatKho(PhieuNhapXuatKho phieuNhapXuatKho) { this.phieuNhapXuatKho = phieuNhapXuatKho; }
    
    public PhuTung getPhuTung() { return phuTung; }
    public void setPhuTung(PhuTung phuTung) { this.phuTung = phuTung; }
    
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    
    public Double getDonGia() { return donGia; }
    public void setDonGia(Double donGia) { this.donGia = donGia; }
    
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
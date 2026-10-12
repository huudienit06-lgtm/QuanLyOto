package entity;

import java.sql.Timestamp;

public class PhieuTiepNhan {
    private String maPhieuTiepNhan;
    private String maXe;
    private String maKH;
    private String maNV;
    private Timestamp ngayLap;
    private String trangThai;
    private String ghiChu;

    public PhieuTiepNhan() {
    }

    public String getMaPhieuTiepNhan() { return maPhieuTiepNhan; }
    public void setMaPhieuTiepNhan(String maPhieuTiepNhan) { this.maPhieuTiepNhan = maPhieuTiepNhan; }
    public String getMaXe() { return maXe; }
    public void setMaXe(String maXe) { this.maXe = maXe; }
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}

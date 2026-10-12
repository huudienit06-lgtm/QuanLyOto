package entity;

import java.sql.Date;

public class ChuongTrinhKhuyenMai {
    private String maKM;
    private String tenKM;
    private Double tileGiam;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private String dieuKienApDung;
    private String trangThai;

    public ChuongTrinhKhuyenMai() {
    }

    public String getMaKM() { return maKM; }
    public void setMaKM(String maKM) { this.maKM = maKM; }
    public String getTenKM() { return tenKM; }
    public void setTenKM(String tenKM) { this.tenKM = tenKM; }
    public Double getTileGiam() { return tileGiam; }
    public void setTileGiam(Double tileGiam) { this.tileGiam = tileGiam; }
    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public String getDieuKienApDung() { return dieuKienApDung; }
    public void setDieuKienApDung(String dieuKienApDung) { this.dieuKienApDung = dieuKienApDung; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

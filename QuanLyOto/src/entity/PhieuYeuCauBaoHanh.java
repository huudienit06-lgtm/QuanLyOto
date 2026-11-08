package entity;

import java.sql.Timestamp;
import java.util.Objects;

public class PhieuYeuCauBaoHanh {
    private String maPhieuYCBH;
    private HoaDon hoaDon;
    private PhuTung phuTung;
    private int soLuong;
    private Timestamp ngayTiepNhan;
    private String trangThai;
    private String lyDo;
    private String ghiChuXuLy;
    private Timestamp ngayXuLy;
    private Timestamp ngayHoanThanh;

    public PhieuYeuCauBaoHanh() {
    }

    public PhieuYeuCauBaoHanh(String maPhieuYCBH) {
        this.maPhieuYCBH = maPhieuYCBH;
    }

    public PhieuYeuCauBaoHanh(String maPhieuYCBH, HoaDon hoaDon, PhuTung phuTung, int soLuong,
            Timestamp ngayTiepNhan, String trangThai, String lyDo, String ghiChuXuLy,
            Timestamp ngayXuLy, Timestamp ngayHoanThanh) {
        this.maPhieuYCBH = maPhieuYCBH;
        this.hoaDon = hoaDon;
        this.phuTung = phuTung;
        this.soLuong = soLuong;
        this.ngayTiepNhan = ngayTiepNhan;
        this.trangThai = trangThai;
        this.lyDo = lyDo;
        this.ghiChuXuLy = ghiChuXuLy;
        this.ngayXuLy = ngayXuLy;
        this.ngayHoanThanh = ngayHoanThanh;
    }

    public String getMaPhieuYCBH() { return maPhieuYCBH; }
    public void setMaPhieuYCBH(String maPhieuYCBH) { this.maPhieuYCBH = maPhieuYCBH; }
    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }
    public PhuTung getPhuTung() { return phuTung; }
    public void setPhuTung(PhuTung phuTung) { this.phuTung = phuTung; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public Timestamp getNgayTiepNhan() { return ngayTiepNhan; }
    public void setNgayTiepNhan(Timestamp ngayTiepNhan) { this.ngayTiepNhan = ngayTiepNhan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public String getGhiChuXuLy() { return ghiChuXuLy; }
    public void setGhiChuXuLy(String ghiChuXuLy) { this.ghiChuXuLy = ghiChuXuLy; }
    public Timestamp getNgayXuLy() { return ngayXuLy; }
    public void setNgayXuLy(Timestamp ngayXuLy) { this.ngayXuLy = ngayXuLy; }
    public Timestamp getNgayHoanThanh() { return ngayHoanThanh; }
    public void setNgayHoanThanh(Timestamp ngayHoanThanh) { this.ngayHoanThanh = ngayHoanThanh; }

    @Override
    public String toString() {
        return maPhieuYCBH + " - " + trangThai;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PhieuYeuCauBaoHanh other)) return false;
        return maPhieuYCBH != null && maPhieuYCBH.equals(other.maPhieuYCBH);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPhieuYCBH);
    }
}

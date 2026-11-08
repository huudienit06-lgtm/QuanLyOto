package entity;

import java.sql.Date;
import java.util.Objects;

public class LichLamViec {
    private String maLich;
    private CaLamViec caLamViec;
    private NhanVien nhanVien;
    private Date ngayLamViec;
    private String trangThai;

    public LichLamViec() {
    }

    public LichLamViec(String maLich) {
        this.maLich = maLich;
    }

    public LichLamViec(String maLich, CaLamViec caLamViec, NhanVien nhanVien,
            Date ngayLamViec, String trangThai) {
        this.maLich = maLich;
        this.caLamViec = caLamViec;
        this.nhanVien = nhanVien;
        this.ngayLamViec = ngayLamViec;
        this.trangThai = trangThai;
    }

    public String getMaLich() { return maLich; }
    public void setMaLich(String maLich) { this.maLich = maLich; }
    public CaLamViec getCaLamViec() { return caLamViec; }
    public void setCaLamViec(CaLamViec caLamViec) { this.caLamViec = caLamViec; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public Date getNgayLamViec() { return ngayLamViec; }
    public void setNgayLamViec(Date ngayLamViec) { this.ngayLamViec = ngayLamViec; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return maLich + " - " + ngayLamViec + " (" + trangThai + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof LichLamViec other)) return false;
        return maLich != null && maLich.equals(other.maLich);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maLich);
    }
}

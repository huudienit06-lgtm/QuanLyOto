package entity;

import java.sql.Date;

public class LichLamViec {
    private String maLich;
    private String maCa;
    private String maNV;
    private Date ngayLamViec;
    private String trangThai;

    public LichLamViec() {
    }

    public String getMaLich() { return maLich; }
    public void setMaLich(String maLich) { this.maLich = maLich; }
    public String getMaCa() { return maCa; }
    public void setMaCa(String maCa) { this.maCa = maCa; }
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public Date getNgayLamViec() { return ngayLamViec; }
    public void setNgayLamViec(Date ngayLamViec) { this.ngayLamViec = ngayLamViec; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

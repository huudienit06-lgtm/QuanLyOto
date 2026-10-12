package entity;

import java.sql.Timestamp;

public class PhieuNhapXuatKho {
    private String maPhieuNhapXuat;
    private String maNV;
    private Timestamp ngayLapPhieu;
    private String kieuPhieu;

    public PhieuNhapXuatKho() {
    }

    public String getMaPhieuNhapXuat() { return maPhieuNhapXuat; }
    public void setMaPhieuNhapXuat(String maPhieuNhapXuat) { this.maPhieuNhapXuat = maPhieuNhapXuat; }
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public Timestamp getNgayLapPhieu() { return ngayLapPhieu; }
    public void setNgayLapPhieu(Timestamp ngayLapPhieu) { this.ngayLapPhieu = ngayLapPhieu; }
    public String getKieuPhieu() { return kieuPhieu; }
    public void setKieuPhieu(String kieuPhieu) { this.kieuPhieu = kieuPhieu; }
}

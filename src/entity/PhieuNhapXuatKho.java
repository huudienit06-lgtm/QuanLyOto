package entity;

import java.sql.Timestamp;

public class PhieuNhapXuatKho {
    private String maPhieuNhapXuat;
    private NhanVien nhanVien; 
    private Timestamp ngayLapPhieu;
    private String kieuPhieu;

    public PhieuNhapXuatKho() {}

    public PhieuNhapXuatKho(String maPhieuNhapXuat) {
        this.maPhieuNhapXuat = maPhieuNhapXuat;
    }

    public String getMaPhieuNhapXuat() { return maPhieuNhapXuat; }
    public void setMaPhieuNhapXuat(String maPhieuNhapXuat) { this.maPhieuNhapXuat = maPhieuNhapXuat; }
    
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    
    public Timestamp getNgayLapPhieu() { return ngayLapPhieu; }
    public void setNgayLapPhieu(Timestamp ngayLapPhieu) { this.ngayLapPhieu = ngayLapPhieu; }
    
    public String getKieuPhieu() { return kieuPhieu; }
    public void setKieuPhieu(String kieuPhieu) { this.kieuPhieu = kieuPhieu; }

    public void setGhiChu(String trim) {
        throw new UnsupportedOperationException("Unimplemented method 'setGhiChu'");
    }
}
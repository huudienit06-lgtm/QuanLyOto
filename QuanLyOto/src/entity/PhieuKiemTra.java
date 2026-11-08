package entity;

import java.util.Objects;

public class PhieuKiemTra {
    public static final String TRANG_THAI_CHO_KIEM_TRA = "Chờ kiểm tra";
    public static final String TRANG_THAI_DA_KIEM_TRA = "Đã kiểm tra";

    private String maPhieuKiemTra;
    private PhieuTiepNhan phieuTiepNhan;
    private KhachHang khachHang;
    private Xe xe;
    private NhanVien nhanVien;
    private String trangThai;
    private String trangThaiBanDau;
    private String vanDeHuHong;

    public PhieuKiemTra() {
    }

    public PhieuKiemTra(String maPhieuKiemTra) {
        this.maPhieuKiemTra = maPhieuKiemTra;
    }

    public PhieuKiemTra(String maPhieuKiemTra, PhieuTiepNhan phieuTiepNhan, KhachHang khachHang,
            Xe xe, NhanVien nhanVien, String trangThaiBanDau, String vanDeHuHong) {
        this(maPhieuKiemTra, phieuTiepNhan, khachHang, xe, nhanVien,
            TRANG_THAI_CHO_KIEM_TRA, trangThaiBanDau, vanDeHuHong);
        }

        public PhieuKiemTra(String maPhieuKiemTra, PhieuTiepNhan phieuTiepNhan, KhachHang khachHang,
            Xe xe, NhanVien nhanVien, String trangThai, String trangThaiBanDau, String vanDeHuHong) {
        this.maPhieuKiemTra = maPhieuKiemTra;
        this.phieuTiepNhan = phieuTiepNhan;
        this.khachHang = khachHang;
        this.xe = xe;
        this.nhanVien = nhanVien;
        this.trangThai = trangThai;
        this.trangThaiBanDau = trangThaiBanDau;
        this.vanDeHuHong = vanDeHuHong;
    }

    public String getMaPhieuKiemTra() { return maPhieuKiemTra; }
    public void setMaPhieuKiemTra(String maPhieuKiemTra) { this.maPhieuKiemTra = maPhieuKiemTra; }
    public PhieuTiepNhan getPhieuTiepNhan() { return phieuTiepNhan; }
    public void setPhieuTiepNhan(PhieuTiepNhan phieuTiepNhan) { this.phieuTiepNhan = phieuTiepNhan; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public Xe getXe() { return xe; }
    public void setXe(Xe xe) { this.xe = xe; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTrangThaiBanDau() { return trangThaiBanDau; }
    public void setTrangThaiBanDau(String trangThaiBanDau) { this.trangThaiBanDau = trangThaiBanDau; }
    public String getVanDeHuHong() { return vanDeHuHong; }
    public void setVanDeHuHong(String vanDeHuHong) { this.vanDeHuHong = vanDeHuHong; }

    @Override
    public String toString() {
        return maPhieuKiemTra + " - "
                + (phieuTiepNhan == null ? "" : phieuTiepNhan.getMaPhieuTiepNhan());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PhieuKiemTra other)) return false;
        return maPhieuKiemTra != null && maPhieuKiemTra.equals(other.maPhieuKiemTra);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPhieuKiemTra);
    }
}

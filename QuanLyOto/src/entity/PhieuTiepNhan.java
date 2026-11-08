package entity;

import java.sql.Timestamp;
import java.util.Objects;

public class PhieuTiepNhan {
    public static final String TIEN_DO_DA_TIEP_NHAN = "Đã tiếp nhận";
    public static final String TIEN_DO_DA_LAP_PHIEU_KIEM_TRA = "Đã lập phiếu kiểm tra";
    public static final String TIEN_DO_DA_LAP_PHIEU_SUA_CHUA = "Đã lập phiếu sửa chữa";
    public static final String TIEN_DO_DA_LAP_HOA_DON = "Đã lập hóa đơn";
    public static final String TIEN_DO_HOAN_THANH = "Hoàn thành";

    private String maPhieuTiepNhan;
    private Xe xe;
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private Timestamp ngayLap;
    private String trangThai;
    private String tienDo;
    private String ghiChu;

    public PhieuTiepNhan() {
    }

    public PhieuTiepNhan(String maPhieuTiepNhan) {
        this.maPhieuTiepNhan = maPhieuTiepNhan;
    }

    public PhieuTiepNhan(String maPhieuTiepNhan, Xe xe, KhachHang khachHang, NhanVien nhanVien,
            Timestamp ngayLap, String trangThai, String ghiChu) {
        this(maPhieuTiepNhan, xe, khachHang, nhanVien, ngayLap, trangThai,
            TIEN_DO_DA_TIEP_NHAN, ghiChu);
        }

        public PhieuTiepNhan(String maPhieuTiepNhan, Xe xe, KhachHang khachHang, NhanVien nhanVien,
            Timestamp ngayLap, String trangThai, String tienDo, String ghiChu) {
        this.maPhieuTiepNhan = maPhieuTiepNhan;
        this.xe = xe;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.tienDo = tienDo;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieuTiepNhan() { return maPhieuTiepNhan; }
    public void setMaPhieuTiepNhan(String maPhieuTiepNhan) { this.maPhieuTiepNhan = maPhieuTiepNhan; }
    public Xe getXe() { return xe; }
    public void setXe(Xe xe) { this.xe = xe; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTienDo() { return tienDo; }
    public void setTienDo(String tienDo) { this.tienDo = tienDo; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return maPhieuTiepNhan + " - "
                + (xe == null ? "" : xe.getBienSo() == null ? xe.getMaXe() : xe.getBienSo())
                + " (" + trangThai + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PhieuTiepNhan other)) return false;
        return maPhieuTiepNhan != null && maPhieuTiepNhan.equals(other.maPhieuTiepNhan);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPhieuTiepNhan);
    }
}

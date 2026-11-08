package entity;

import java.util.Objects;

public class TaiKhoan {
    private NhanVien nhanVien;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private String trangThai;
    private transient String hoTenNhanVien;

    public TaiKhoan() {
    }

    public TaiKhoan(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public TaiKhoan(NhanVien nhanVien, String tenDangNhap, String matKhau, String vaiTro,
            String trangThai, String hoTenNhanVien) {
        this.nhanVien = nhanVien;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
        this.hoTenNhanVien = hoTenNhanVien;
    }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getHoTenNhanVien() { return hoTenNhanVien; }
    public void setHoTenNhanVien(String hoTenNhanVien) { this.hoTenNhanVien = hoTenNhanVien; }

    @Override
    public String toString() {
        return (nhanVien == null ? "" : nhanVien.getMaNV()) + " - " + tenDangNhap;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof TaiKhoan other)) return false;
        return nhanVien != null && other.nhanVien != null
                && nhanVien.getMaNV() != null && nhanVien.getMaNV().equals(other.nhanVien.getMaNV());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nhanVien == null ? null : nhanVien.getMaNV());
    }
}

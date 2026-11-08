package entity;

import java.util.Objects;

public class PhieuBanGiao {
    private String maPhieuBanGiao;
    private Xe xe;
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private String tinhTrangXeHienTai;
    private String tenNguoiNhan;
    private String soDienThoaiNguoiNhan;

    public PhieuBanGiao() {
    }

    public PhieuBanGiao(String maPhieuBanGiao) {
        this.maPhieuBanGiao = maPhieuBanGiao;
    }

    public PhieuBanGiao(String maPhieuBanGiao, Xe xe, KhachHang khachHang,
            NhanVien nhanVien, String tinhTrangXeHienTai, String tenNguoiNhan, String soDienThoaiNguoiNhan) {
        this.maPhieuBanGiao = maPhieuBanGiao;
        this.xe = xe;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.tinhTrangXeHienTai = tinhTrangXeHienTai;
        this.tenNguoiNhan = tenNguoiNhan;
        this.soDienThoaiNguoiNhan = soDienThoaiNguoiNhan;
    }

    public String getMaPhieuBanGiao() {
        return maPhieuBanGiao;
    }

    public void setMaPhieuBanGiao(String maPhieuBanGiao) {
        this.maPhieuBanGiao = maPhieuBanGiao;
    }

    public Xe getXe() {
        return xe;
    }

    public void setXe(Xe xe) {
        this.xe = xe;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getTinhTrangXeHienTai() {
        return tinhTrangXeHienTai;
    }

    public void setTinhTrangXeHienTai(String tinhTrangXeHienTai) {
        this.tinhTrangXeHienTai = tinhTrangXeHienTai;
    }

    public String getTenNguoiNhan() {
        return tenNguoiNhan;
    }

    public void setTenNguoiNhan(String tenNguoiNhan) {
        this.tenNguoiNhan = tenNguoiNhan;
    }

    public String getSoDienThoaiNguoiNhan() {
        return soDienThoaiNguoiNhan;
    }

    public void setSoDienThoaiNguoiNhan(String soDienThoaiNguoiNhan) {
        this.soDienThoaiNguoiNhan = soDienThoaiNguoiNhan;
    }

    @Override
    public String toString() {
        return maPhieuBanGiao + " - "
                + (khachHang == null ? "" : khachHang.getHoTen());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof PhieuBanGiao other))
            return false;
        return maPhieuBanGiao != null && maPhieuBanGiao.equals(other.maPhieuBanGiao);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPhieuBanGiao);
    }
}

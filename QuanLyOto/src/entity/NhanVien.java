package entity;

import java.util.Objects;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private ChucVu chucVu;
    private String soDT;
    private String email;
    private String trangThai;

    public NhanVien() {
    }

    public NhanVien(String maNV) {
        this.maNV = maNV;
    }

    public NhanVien(String maNV, String hoTen, ChucVu chucVu, String soDT, String email, String trangThai) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
        this.soDT = soDT;
        this.email = email;
        this.trangThai = trangThai;
    }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public ChucVu getChucVu() { return chucVu; }
    public void setChucVu(ChucVu chucVu) { this.chucVu = chucVu; }
    public String getSoDT() { return soDT; }
    public void setSoDT(String soDT) { this.soDT = soDT; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return maNV + " - " + hoTen;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof NhanVien other)) return false;
        return maNV != null && maNV.equals(other.maNV);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maNV);
    }
}

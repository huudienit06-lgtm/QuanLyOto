package entity;

import java.util.Objects;

public class ChucVu {
    private String maChucVu;
    private String tenChucVu;

    public ChucVu() {
    }

    public ChucVu(String maChucVu) {
        this.maChucVu = maChucVu;
    }

    public ChucVu(String maChucVu, String tenChucVu) {
        this.maChucVu = maChucVu;
        this.tenChucVu = tenChucVu;
    }

    public String getMaChucVu() { return maChucVu; }
    public void setMaChucVu(String maChucVu) { this.maChucVu = maChucVu; }
    public String getTenChucVu() { return tenChucVu; }
    public void setTenChucVu(String tenChucVu) { this.tenChucVu = tenChucVu; }

    @Override
    public String toString() {
        return tenChucVu == null ? maChucVu : tenChucVu;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ChucVu other)) return false;
        return maChucVu != null && maChucVu.equals(other.maChucVu);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maChucVu);
    }
}
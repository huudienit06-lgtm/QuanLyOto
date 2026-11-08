package entity;

import java.sql.Time;
import java.util.Objects;

public class CaLamViec {
    private String maCa;
    private Time gioBatDau;
    private Time gioKetThuc;

    public CaLamViec() {
    }

    public CaLamViec(String maCa, Time gioBatDau, Time gioKetThuc) {
        this.maCa = maCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public CaLamViec(String maCa) {
        this(maCa, null, null);
    }

    public String getMaCa() { return maCa; }
    public void setMaCa(String maCa) { this.maCa = maCa; }
    public Time getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(Time gioBatDau) { this.gioBatDau = gioBatDau; }
    public Time getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(Time gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    @Override
    public String toString() {
        return maCa + " (" + gioBatDau + " - " + gioKetThuc + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CaLamViec other)) return false;
        return maCa != null && maCa.equals(other.maCa);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maCa);
    }
}

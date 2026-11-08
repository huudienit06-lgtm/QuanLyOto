package entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChinhSachBaoHanh {
    private String maCSBH;
    private String tenChinhSach;
    private int thoiHanBaoHanh;
    private String donViThoiGian;
    private String dieuKienBaoHanh;
    private String truongHopTuChoi;
    private String noiDungBaoHanh;
    private Timestamp ngayTao;
    private boolean trangThai;
    private List<PhuTung> dsPhuTung = new ArrayList<>();

    public ChinhSachBaoHanh() {
    }

    public ChinhSachBaoHanh(String maCSBH, String tenChinhSach, int thoiHanBaoHanh, String donViThoiGian, String dieuKienBaoHanh, String truongHopTuChoi, String noiDungBaoHanh) {
        this.maCSBH = maCSBH;
        this.tenChinhSach = tenChinhSach;
        this.thoiHanBaoHanh = thoiHanBaoHanh;
        this.donViThoiGian = donViThoiGian;
        this.dieuKienBaoHanh = dieuKienBaoHanh;
        this.truongHopTuChoi = truongHopTuChoi;
        this.noiDungBaoHanh = noiDungBaoHanh;
    }

    public ChinhSachBaoHanh(String maCSBH) {
        this.maCSBH = maCSBH;
    }

    public ChinhSachBaoHanh(String maCSBH, String tenChinhSach, int thoiHanBaoHanh, String donViThoiGian,
            String dieuKienBaoHanh, String truongHopTuChoi, String noiDungBaoHanh,
            Timestamp ngayTao, boolean trangThai, List<PhuTung> dsPhuTung) {
        this(maCSBH, tenChinhSach, thoiHanBaoHanh, donViThoiGian,
                dieuKienBaoHanh, truongHopTuChoi, noiDungBaoHanh);
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
        setDsPhuTung(dsPhuTung);
    }

    public String getMaCSBH() { return maCSBH; }
    public void setMaCSBH(String maCSBH) { this.maCSBH = maCSBH; }
    public String getTenChinhSach() { return tenChinhSach; }
    public void setTenChinhSach(String tenChinhSach) { this.tenChinhSach = tenChinhSach; }
    public int getThoiHanBaoHanh() { return thoiHanBaoHanh; }
    public void setThoiHanBaoHanh(int thoiHanBaoHanh) { this.thoiHanBaoHanh = thoiHanBaoHanh; }
    public String getDonViThoiGian() { return donViThoiGian; }
    public void setDonViThoiGian(String donViThoiGian) { this.donViThoiGian = donViThoiGian; }
    public String getDieuKienBaoHanh() { return dieuKienBaoHanh; }
    public void setDieuKienBaoHanh(String dieuKienBaoHanh) { this.dieuKienBaoHanh = dieuKienBaoHanh; }
    public String getTruongHopTuChoi() { return truongHopTuChoi; }
    public void setTruongHopTuChoi(String truongHopTuChoi) { this.truongHopTuChoi = truongHopTuChoi; }
    public String getNoiDungBaoHanh() { return noiDungBaoHanh; }
    public void setNoiDungBaoHanh(String noiDungBaoHanh) { this.noiDungBaoHanh = noiDungBaoHanh; }
    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public List<PhuTung> getDsPhuTung() { return new ArrayList<>(dsPhuTung); }
    public void setDsPhuTung(List<PhuTung> dsPhuTung) {
        this.dsPhuTung = dsPhuTung == null ? new ArrayList<>() : new ArrayList<>(dsPhuTung);
    }

    @Override
    public String toString() {
        return maCSBH + " - " + tenChinhSach;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ChinhSachBaoHanh other)) return false;
        return maCSBH != null && maCSBH.equals(other.maCSBH);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maCSBH);
    }
}

package entity;

import java.sql.Date;

public class PhieuBaoHanh {
    private String maPhieuBaoHanh;
    private String maNV;
    private String maKH;
    private String maXe;
    private Date ngayHetHanBaoHanh;
    private String noiDungBaoHanh;
    private String dieuKienBaoHanh;

    public PhieuBaoHanh() {
    }

    public String getMaPhieuBaoHanh() { return maPhieuBaoHanh; }
    public void setMaPhieuBaoHanh(String maPhieuBaoHanh) { this.maPhieuBaoHanh = maPhieuBaoHanh; }
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getMaXe() { return maXe; }
    public void setMaXe(String maXe) { this.maXe = maXe; }
    public Date getNgayHetHanBaoHanh() { return ngayHetHanBaoHanh; }
    public void setNgayHetHanBaoHanh(Date ngayHetHanBaoHanh) { this.ngayHetHanBaoHanh = ngayHetHanBaoHanh; }
    public String getNoiDungBaoHanh() { return noiDungBaoHanh; }
    public void setNoiDungBaoHanh(String noiDungBaoHanh) { this.noiDungBaoHanh = noiDungBaoHanh; }
    public String getDieuKienBaoHanh() { return dieuKienBaoHanh; }
    public void setDieuKienBaoHanh(String dieuKienBaoHanh) { this.dieuKienBaoHanh = dieuKienBaoHanh; }
}

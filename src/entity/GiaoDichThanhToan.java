package entity;

import java.sql.Timestamp;

public class GiaoDichThanhToan {
    private String maGD;
    private String maHoaDon;
    private String phuongThucThanhToan;
    private Timestamp thoiGianGD;
    private String trangThaiGD;

    public GiaoDichThanhToan() {
    }

    public String getMaGD() { return maGD; }
    public void setMaGD(String maGD) { this.maGD = maGD; }
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public Timestamp getThoiGianGD() { return thoiGianGD; }
    public void setThoiGianGD(Timestamp thoiGianGD) { this.thoiGianGD = thoiGianGD; }
    public String getTrangThaiGD() { return trangThaiGD; }
    public void setTrangThaiGD(String trangThaiGD) { this.trangThaiGD = trangThaiGD; }
}

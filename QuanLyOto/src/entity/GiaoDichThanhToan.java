package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

public class GiaoDichThanhToan {
    // Hằng số để tránh "magic string" rải rác ở DAO/GUI.
    public static final String TT_THANH_CONG = "Thành công";
    public static final String TT_THAT_BAI = "Thất bại";
    public static final String TT_CHO_XU_LY = "Chờ xử lý";

    public static final String PT_TIEN_MAT = "Tiền mặt";
    public static final String PT_CHUYEN_KHOAN = "Chuyển khoản / QR";

    private String maGD;
    private HoaDon hoaDon;
    private String phuongThucThanhToan;
    private Timestamp thoiGianGD;
    private String trangThaiGD;
    private BigDecimal soTienThanhToan;

    public GiaoDichThanhToan() {
    }

    public GiaoDichThanhToan(String maGD, HoaDon hoaDon,
            String phuongThucThanhToan, Timestamp thoiGianGD,
            String trangThaiGD, BigDecimal soTienThanhToan) {
        setMaGD(maGD);
        setHoaDon(hoaDon);
        setPhuongThucThanhToan(phuongThucThanhToan);
        setThoiGianGD(thoiGianGD);
        setTrangThaiGD(trangThaiGD);
        setSoTienThanhToan(soTienThanhToan);
    }

    public String getMaGD() {
        return maGD;
    }

    public void setMaGD(String maGD) {
        this.maGD = maGD == null ? null : maGD.trim();
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan == null ? null : phuongThucThanhToan.trim();
    }

    public Timestamp getThoiGianGD() {
        return thoiGianGD;
    }

    public void setThoiGianGD(Timestamp thoiGianGD) {
        this.thoiGianGD = thoiGianGD;
    }

    public String getTrangThaiGD() {
        return trangThaiGD;
    }

    public void setTrangThaiGD(String trangThaiGD) {
        this.trangThaiGD = trangThaiGD == null ? null : trangThaiGD.trim();
    }

    public BigDecimal getSoTienThanhToan() {
        return soTienThanhToan;
    }

    public void setSoTienThanhToan(BigDecimal soTienThanhToan) {
        if (soTienThanhToan != null && soTienThanhToan.signum() < 0)
            throw new IllegalArgumentException("Số tiền thanh toán không được âm.");
        this.soTienThanhToan = soTienThanhToan;
    }

    /** Tiện ích: lấy mã hóa đơn mà không cần kiểm tra null hoaDon. */
    public String getMaHoaDon() {
        return hoaDon == null ? null : hoaDon.getMaHoaDon();
    }

    public boolean isThanhCong() {
        return TT_THANH_CONG.equalsIgnoreCase(trangThaiGD);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GiaoDichThanhToan))
            return false;
        return Objects.equals(maGD, ((GiaoDichThanhToan) o).maGD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maGD);
    }

    @Override
    public String toString() {
        return "GiaoDichThanhToan [maGD=" + maGD + ", maHoaDon=" + getMaHoaDon()
                + ", phuongThuc=" + phuongThucThanhToan + ", thoiGian=" + thoiGianGD
                + ", trangThai=" + trangThaiGD + ", soTien=" + soTienThanhToan + "]";
    }
}
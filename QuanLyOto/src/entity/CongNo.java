package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/*
 * Công nợ của một khách hàng.
 * Không có bảng CongNo trong SQL: tổng nợ được tính từ HoaDon - GiaoDichThanhToan
 * theo từng khách hàng (xem CongNoDao), nên lớp này chỉ là đối tượng dữ liệu dẫn xuất.
 * Khóa ngoại tới KhachHang được biểu diễn bằng đối tượng KhachHang.
 */
public class CongNo {
    // Giá trị lọc / trạng thái dùng chung cho giao diện và DAO
    public static final String TAT_CA = "Tất cả";
    public static final String CON_NO = "Còn nợ";
    public static final String DA_TT = "Đã thanh toán";

    private KhachHang khachHang;
    private BigDecimal tongNo = BigDecimal.ZERO;
    private int soHDNo;
    private LocalDateTime ngayCapNhat;

    public CongNo() {
    }

    public CongNo(KhachHang khachHang, BigDecimal tongNo, int soHDNo, LocalDateTime ngayCapNhat) {
        this.khachHang = khachHang;
        setTongNo(tongNo);
        this.soHDNo = soHDNo;
        this.ngayCapNhat = ngayCapNhat;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public BigDecimal getTongNo() {
        return tongNo;
    }

    public void setTongNo(BigDecimal tongNo) {
        this.tongNo = tongNo == null ? BigDecimal.ZERO : tongNo;
    }

    public int getSoHDNo() {
        return soHDNo;
    }

    public void setSoHDNo(int soHDNo) {
        this.soHDNo = soHDNo;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    // Trạng thái suy ra từ tổng nợ
    public String getTrangThai() {
        return tongNo.signum() > 0 ? CON_NO : DA_TT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CongNo)) {
            return false;
        }
        return Objects.equals(khachHang, ((CongNo) o).khachHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(khachHang);
    }

    @Override
    public String toString() {
        return "CongNo [khachHang=" + khachHang + ", tongNo=" + tongNo + ", soHDNo=" + soHDNo + "]";
    }

    // Một hóa đơn còn nợ của khách hàng (hiển thị trong khung chi tiết)
    public static class HoaDonNo {
        private String maHoaDon;
        private KhachHang khachHang;
        private LocalDateTime ngayLap;
        private BigDecimal soTien = BigDecimal.ZERO;
        private BigDecimal daTra = BigDecimal.ZERO;
        private BigDecimal conPhaiTra = BigDecimal.ZERO;

        public HoaDonNo() {
        }

        public HoaDonNo(String maHoaDon, KhachHang khachHang, LocalDateTime ngayLap,
                BigDecimal soTien, BigDecimal daTra, BigDecimal conPhaiTra) {
            this.maHoaDon = maHoaDon;
            this.khachHang = khachHang;
            this.ngayLap = ngayLap;
            this.soTien = soTien == null ? BigDecimal.ZERO : soTien;
            this.daTra = daTra == null ? BigDecimal.ZERO : daTra;
            this.conPhaiTra = conPhaiTra == null ? BigDecimal.ZERO : conPhaiTra;
        }

        public String getMaHoaDon() {
            return maHoaDon;
        }

        public KhachHang getKhachHang() {
            return khachHang;
        }

        public LocalDateTime getNgayLap() {
            return ngayLap;
        }

        public BigDecimal getSoTien() {
            return soTien;
        }

        public BigDecimal getDaTra() {
            return daTra;
        }

        public BigDecimal getConPhaiTra() {
            return conPhaiTra;
        }
    }
}
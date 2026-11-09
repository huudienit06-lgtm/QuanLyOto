package dao;

import connectDB.ConnectDB;
import entity.ChiTietHoaDon;
import entity.DichVu;
import entity.GiaoDichThanhToan;
import entity.PhuTung;
import entity.HoaDon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimKiemHoaDonDAO {
    private static final String SQL_TIM_KIEM = """
            SELECT hd.maHoaDon, hd.maPhieuSuaChua, hd.maNV,
                   kh.hoTen AS tenKhachHang, x.bienSo, hd.maKM,
                   hd.ngayLap, hd.soTien, hd.trangThai
            FROM HoaDon hd
            LEFT JOIN PhieuSuaChua p
                ON hd.maPhieuSuaChua = p.maPhieuSuaChua
            LEFT JOIN KhachHang kh
                ON kh.maKH = COALESCE(hd.maKH, p.maKH)
            LEFT JOIN Xe x
                ON p.maXe = x.maXe
            WHERE (? = '' OR hd.maHoaDon LIKE ?)
              AND (? = '' OR hd.maPhieuSuaChua LIKE ?)
              AND (? = '' OR hd.maNV LIKE ?)
              AND (? = '' OR ISNULL(hd.maKM, '') LIKE ?)
              AND (? = '' OR COALESCE(hd.maKH, p.maKH) LIKE ? OR kh.hoTen LIKE ?)
              AND (? = '' OR p.maXe LIKE ? OR x.bienSo LIKE ?)
              AND (? IS NULL OR (hd.ngayLap >= ? AND hd.ngayLap < DATEADD(DAY, 1, ?)))
              AND (? IS NULL OR hd.trangThai = ?)
            ORDER BY hd.ngayLap DESC
            """;

    // thueVAT, tienGiamGia, soTien lấy từ hóa đơn
    private static final String SQL_HOA_DON = """
            SELECT maHoaDon, thueVAT, tienGiamGia, soTien, trangThai
            FROM HoaDon
            WHERE maHoaDon = ?
            """;

    // maKH, maXe lấy qua phiếu sửa chữa
    private static final String SQL_MA_KH_MA_XE = """
                SELECT
                COALESCE(hd.maKH, p.maKH) AS maKH,
                p.maXe
                FROM HoaDon hd
                LEFT JOIN PhieuSuaChua p
                ON hd.maPhieuSuaChua = p.maPhieuSuaChua
                WHERE hd.maHoaDon = ?
            """;

    // Dịch vụ xếp trước, phụ tùng xếp sau
    private static final String SQL_CHI_TIET = """
            SELECT maHoaDon, maDV, maPhuTung, soLuong, donGia
            FROM ChiTietHoaDon
            WHERE maHoaDon = ?
            ORDER BY CASE WHEN maDV IS NOT NULL THEN 0 ELSE 1 END
            """;

    // Tên dịch vụ / phụ tùng của các dòng trong hóa đơn
    private static final String SQL_TEN_HANG = """
            SELECT dv.maDV AS ma, dv.tenDV AS ten
            FROM ChiTietHoaDon ct JOIN DichVu dv ON ct.maDV = dv.maDV
            WHERE ct.maHoaDon = ?
            UNION
            SELECT pt.maPhuTung, pt.tenPhuTung
            FROM ChiTietHoaDon ct JOIN PhuTung pt ON ct.maPhuTung = pt.maPhuTung
            WHERE ct.maHoaDon = ?
            """;

    private static final String SQL_GIAO_DICH_THANH_CONG = """
            SELECT maHoaDon, soTienThanhToan, trangThaiGD
            FROM GiaoDichThanhToan
            WHERE maHoaDon = ?
              AND trangThaiGD = N'Thành công'
            """;

    // Dùng chung kết nối của ứng dụng, KHÔNG đóng sau khi dùng xong
    public boolean kiemTraKetNoi() {
        try {
            Connection conn = ConnectDB.getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Tìm kiếm hóa đơn theo các điều kiện (chuỗi rỗng / null = bỏ qua điều kiện).
     * Kết quả là dữ liệu ghép từ nhiều bảng (tên khách hàng, biển số xe...) nên mỗi
     * dòng trả về là một mảng 9 phần tử:
     * 0 maHoaDon, 1 maPhieuSuaChua, 2 maNV, 3 tenKhachHang, 4 bienSo, 5 maKM,
     * 6 ngayLap (Timestamp), 7 soTien (BigDecimal), 8 trangThai
     */
    public List<Object[]> timKiem(String maHoaDon, String maPhieuSuaChua, String maNV, String maKM,
            String khachHang, String xe, LocalDate ngayLap, String trangThai) throws SQLException {
        List<Object[]> ds = new ArrayList<>();
        Connection conn = ConnectDB.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(SQL_TIM_KIEM)) {
            // Thứ tự tham số khớp với SQL_TIM_KIEM: giá trị rồi các mẫu LIKE
            int i = 1;
            i = datThamSoChuoi(ps, i, maHoaDon, 1);
            i = datThamSoChuoi(ps, i, maPhieuSuaChua, 1);
            i = datThamSoChuoi(ps, i, maNV, 1);
            i = datThamSoChuoi(ps, i, maKM, 1);
            i = datThamSoChuoi(ps, i, khachHang, 2); // mã hoặc tên khách hàng
            i = datThamSoChuoi(ps, i, xe, 2); // mã xe hoặc biển số xe

            // Ngày lập: (IS NULL, >=, <) cùng một mốc ngày
            Timestamp mocNgay = ngayLap == null ? null : Timestamp.valueOf(ngayLap.atStartOfDay());
            for (int k = 0; k < 3; k++) {
                ps.setTimestamp(i++, mocNgay);
            }
            // Trạng thái: (IS NULL, =)
            for (int k = 0; k < 2; k++) {
                ps.setString(i++, trangThai);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[] {
                            rs.getString("maHoaDon"),
                            rs.getString("maPhieuSuaChua"),
                            rs.getString("maNV"),
                            rs.getString("tenKhachHang"),
                            rs.getString("bienSo"),
                            rs.getString("maKM"),
                            rs.getTimestamp("ngayLap"),
                            rs.getBigDecimal("soTien"),
                            rs.getString("trangThai")
                    });
                }
            }
        }
        return ds;
    }

    // Lấy thông tin tiền của hóa đơn (entity HoaDon); không có thì trả về null
    public HoaDon layHoaDon(String maHD) throws SQLException {
        Connection conn = ConnectDB.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_HOA_DON)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setMaHoaDon(rs.getString("maHoaDon"));
                    hd.setThueVAT(rs.getBigDecimal("thueVAT"));
                    hd.setTienGiamGia(rs.getBigDecimal("tienGiamGia"));
                    hd.setSoTien(rs.getBigDecimal("soTien"));
                    hd.setTrangThai(rs.getString("trangThai"));
                    return hd;
                }
            }
        }
        return null;
    }

    // Trả về { maKH, maXe } của hóa đơn (có thể null nếu hóa đơn không có phiếu sửa
    // chữa)
    public String[] layMaKhachHangVaXe(String maHD) throws SQLException {
        Connection conn = ConnectDB.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_MA_KH_MA_XE)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[] { rs.getString("maKH"), rs.getString("maXe") };
                }
            }
        }
        return new String[] { null, null };
    }

    // Các dòng chi tiết của hóa đơn (entity ChiTietHoaDon)
    public List<ChiTietHoaDon> layChiTiet(String maHD) throws SQLException {
        List<ChiTietHoaDon> ds = new ArrayList<>();
        Connection conn = ConnectDB.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_CHI_TIET)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon ct = new ChiTietHoaDon();
                    HoaDon hoaDon = new HoaDon();
                    hoaDon.setMaHoaDon(rs.getString("maHoaDon"));
                    ct.setHoaDon(hoaDon);
                    if (rs.getString("maDV") != null) {
                        DichVu dichVu = new DichVu();
                        dichVu.setMaDV(rs.getString("maDV"));
                        ct.setDichVu(dichVu);
                    }
                    if (rs.getString("maPhuTung") != null) {
                        PhuTung phuTung = new PhuTung();
                        phuTung.setMaPhuTung(rs.getString("maPhuTung"));
                        ct.setPhuTung(phuTung);
                    }
                    ct.setSoLuong(rs.getInt("soLuong"));
                    ct.setDonGia(rs.getBigDecimal("donGia"));
                    ds.add(ct);
                }
            }
        }
        return ds;
    }

    // Bảng tra tên: mã dịch vụ / mã phụ tùng -> tên
    public Map<String, String> layTenHang(String maHD) throws SQLException {
        Map<String, String> map = new HashMap<>();
        Connection conn = ConnectDB.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_TEN_HANG)) {
            ps.setString(1, maHD);
            ps.setString(2, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("ma"), rs.getString("ten"));
                }
            }
        }
        return map;
    }

    // Các giao dịch thanh toán thành công của hóa đơn (entity GiaoDichThanhToan)
    public List<GiaoDichThanhToan> layGiaoDichThanhCong(String maHD) throws SQLException {
        List<GiaoDichThanhToan> ds = new ArrayList<>();
        Connection conn = ConnectDB.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_GIAO_DICH_THANH_CONG)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GiaoDichThanhToan gd = new GiaoDichThanhToan();
                    HoaDon hoaDon = new HoaDon();
                    hoaDon.setMaHoaDon(rs.getString("maHoaDon"));
                    gd.setHoaDon(hoaDon);
                    gd.setSoTienThanhToan(rs.getBigDecimal("soTienThanhToan"));
                    gd.setTrangThaiGD(rs.getString("trangThaiGD"));
                    ds.add(gd);
                }
            }
        }
        return ds;
    }

    // Gán tham số chuỗi: giá trị gốc, rồi soLike mẫu "%giá trị%". Trả về vị trí
    // tham số kế tiếp.
    private int datThamSoChuoi(PreparedStatement ps, int i, String giaTri, int soLike) throws SQLException {
        String gt = giaTri == null ? "" : giaTri;
        ps.setString(i++, gt);
        for (int k = 0; k < soLike; k++) {
            ps.setString(i++, "%" + gt + "%");
        }
        return i;
    }
}
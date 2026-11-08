package dao;

import connectDB.ConnectDB;
import entity.GiaoDichThanhToan;
import entity.HoaDon;
import entity.PhieuTiepNhan;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ThanhToanDAO {
    public static final String LOAI_DV = "DV";
    public static final String LOAI_PT = "PT";

    // ===================== DTO =====================
    public static class ThongTinHoaDon {
        public String maHoaDon, maKhachHang, tenKhachHang, maPhieuSuaChua, maNhanVien, maXe;
        public Timestamp ngayLap;
        /** HoaDon.soTien: tổng thanh toán sau VAT và sau giảm giá. */
        public BigDecimal tongTien, thueVAT, tienGiamGia;
    }

    public static class ChiTiet {
        public String loai, tenHang;
        public int soLuong;
        public BigDecimal donGia = BigDecimal.ZERO;
        public BigDecimal thanhTien = BigDecimal.ZERO;
    }

    // ===================== Kết nối =====================
    /** Không đóng connection dùng chung (singleton) của ConnectDB. */
    private Connection layKetNoi() throws SQLException {
        ConnectDB.getInstance().connect();
        return ConnectDB.getConnection();
    }

    // ===================== Hóa đơn =====================
    /** Trả về null nếu không tìm thấy hóa đơn. */
    public ThongTinHoaDon layHoaDon(String maHD) throws SQLException {
        String sql = """
                SELECT
                hd.maHoaDon,
                psc.maKH,
                kh.hoTen AS tenKH,
                hd.maPhieuSuaChua,
                hd.ngayLap,
                hd.soTien,
                hd.thueVAT,
                hd.tienGiamGia,
                hd.maNV,
                psc.maXe
                FROM HoaDon hd
                LEFT JOIN PhieuSuaChua psc
                    ON hd.maPhieuSuaChua = psc.maPhieuSuaChua
                LEFT JOIN KhachHang kh
                    ON psc.maKH = kh.maKH
                WHERE hd.maHoaDon = ?
                """;

        Connection conn = layKetNoi();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                ThongTinHoaDon hd = new ThongTinHoaDon();

                hd.maHoaDon = rs.getString("maHoaDon");
                hd.maKhachHang = rs.getString("maKH");
                hd.tenKhachHang = rs.getString("tenKH");
                hd.maPhieuSuaChua = rs.getString("maPhieuSuaChua");
                hd.ngayLap = rs.getTimestamp("ngayLap");
                hd.tongTien = rs.getBigDecimal("soTien");
                hd.thueVAT = rs.getBigDecimal("thueVAT");
                hd.tienGiamGia = rs.getBigDecimal("tienGiamGia");
                hd.maNhanVien = trim(rs.getString("maNV"));
                hd.maXe = trim(rs.getString("maXe"));

                return hd;
            }
        }
    }

    public List<ChiTiet> layChiTiet(String maHD) throws SQLException {
        List<ChiTiet> ds = new ArrayList<>();
        String sql = """
                SELECT CASE WHEN ct.maDV IS NOT NULL THEN 'DV' ELSE 'PT' END AS loai,
                       COALESCE(dv.tenDV, pt.tenPhuTung) AS tenHang,
                       ct.soLuong, ct.donGia,
                       CAST(ct.soLuong AS DECIMAL(18, 2)) * ct.donGia AS thanhTien
                FROM ChiTietHoaDon ct
                LEFT JOIN DichVu dv ON ct.maDV = dv.maDV
                LEFT JOIN PhuTung pt ON ct.maPhuTung = pt.maPhuTung
                WHERE ct.maHoaDon = ?
                ORDER BY CASE WHEN ct.maDV IS NOT NULL THEN 0 ELSE 1 END,
                         COALESCE(dv.tenDV, pt.tenPhuTung)
                """;
        Connection conn = layKetNoi();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTiet ct = new ChiTiet();
                    ct.loai = rs.getString("loai");
                    ct.tenHang = nvl(rs.getString("tenHang"));
                    ct.soLuong = rs.getInt("soLuong");
                    ct.donGia = bd(rs.getBigDecimal("donGia"));
                    ct.thanhTien = bd(rs.getBigDecimal("thanhTien"));
                    ds.add(ct);
                }
            }
        }
        return ds;
    }

    // ===================== Lịch sử thanh toán =====================
    public List<GiaoDichThanhToan> layLichSu(String maHD) throws SQLException {
        List<GiaoDichThanhToan> ds = new ArrayList<>();
        String sql = """
                SELECT maGD, maHoaDon, phuongThucThanhToan, thoiGianGD, trangThaiGD, soTienThanhToan
                FROM GiaoDichThanhToan
                WHERE maHoaDon = ?
                ORDER BY thoiGianGD DESC
                """;
        Connection conn = layKetNoi();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Khóa ngoại dạng đối tượng: chỉ cần mã hóa đơn, không nạp lại cả hóa đơn.
                    HoaDon hd = new HoaDon();
                    hd.setMaHoaDon(rs.getString("maHoaDon"));

                    GiaoDichThanhToan gd = new GiaoDichThanhToan();
                    gd.setMaGD(rs.getString("maGD"));
                    gd.setHoaDon(hd);
                    gd.setPhuongThucThanhToan(rs.getString("phuongThucThanhToan"));
                    gd.setThoiGianGD(rs.getTimestamp("thoiGianGD"));
                    gd.setTrangThaiGD(rs.getString("trangThaiGD"));
                    gd.setSoTienThanhToan(rs.getBigDecimal("soTienThanhToan"));
                    ds.add(gd);
                }
            }
        }
        return ds;
    }

    // ===================== Ghi nhận thanh toán =====================
    /**
     * Ghi một giao dịch thanh toán thành công trong một transaction.
     * Kiểm tra lại số tiền còn nợ ở phía CSDL để tránh thanh toán vượt (vd: 2 máy
     * cùng thu).
     *
     * @return mã giao dịch vừa tạo
     */
    public String ghiNhanThanhToan(GiaoDichThanhToan gd) throws SQLException {
        if (gd == null || gd.getMaHoaDon() == null || gd.getMaHoaDon().isEmpty())
            throw new SQLException("Giao dịch chưa gắn với hóa đơn nào.");
        final String maHD = gd.getMaHoaDon();
        final BigDecimal soTien = gd.getSoTienThanhToan();
        if (soTien == null || soTien.signum() <= 0)
            throw new SQLException("Số tiền thanh toán phải lớn hơn 0.");
        Connection conn = layKetNoi();
        boolean autoCu = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            BigDecimal tong = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT soTien FROM HoaDon WITH (UPDLOCK, ROWLOCK) WHERE maHoaDon = ?")) {
                ps.setString(1, maHD);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        tong = bd(rs.getBigDecimal(1));
                }
            }
            if (tong == null)
                throw new SQLException("Không tìm thấy hóa đơn " + maHD + ".");

            BigDecimal da = BigDecimal.ZERO;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ISNULL(SUM(soTienThanhToan), 0) FROM GiaoDichThanhToan "
                            + "WHERE maHoaDon = ? AND trangThaiGD = ?")) {
                ps.setString(1, maHD);
                ps.setString(2, GiaoDichThanhToan.TT_THANH_CONG);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        da = bd(rs.getBigDecimal(1));
                }
            }
            if (soTien.compareTo(tong.subtract(da)) > 0)
                throw new SQLException("Số tiền thanh toán vượt quá số còn nợ của hóa đơn.");

            String maGD = taoMaGD(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO GiaoDichThanhToan "
                            + "(maGD, maHoaDon, phuongThucThanhToan, thoiGianGD, trangThaiGD, soTienThanhToan) "
                            + "VALUES (?, ?, ?, ?, ?, ?)")) {
                gd.setMaGD(maGD);
                gd.setThoiGianGD(new Timestamp(System.currentTimeMillis()));
                gd.setTrangThaiGD(GiaoDichThanhToan.TT_THANH_CONG);
                ps.setString(1, gd.getMaGD());
                ps.setString(2, maHD);
                ps.setString(3, gd.getPhuongThucThanhToan());
                ps.setTimestamp(4, gd.getThoiGianGD());
                ps.setString(5, gd.getTrangThaiGD());
                ps.setBigDecimal(6, soTien);
                ps.executeUpdate();
            }
            // Cập nhật trạng thái hóa đơn sau khi đã ghi nhận giao dịch.
            BigDecimal daThanhToanMoi = da.add(soTien);

            String trangThai;
            if (daThanhToanMoi.compareTo(tong) >= 0) {
                trangThai = "Đã thanh toán";
            } else if (daThanhToanMoi.signum() > 0) {
                trangThai = "Thanh toán một phần";
            } else {
                trangThai = "Chưa thanh toán";
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE HoaDon SET trangThai = ? WHERE maHoaDon = ?")) {
                ps.setString(1, trangThai);
                ps.setString(2, maHD);

                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Không thể cập nhật trạng thái hóa đơn.");
                }
            }

            if (daThanhToanMoi.compareTo(tong) >= 0) {
                String sqlTienDo = "UPDATE ptn SET tienDo = ? "
                        + "FROM PhieuTiepNhan ptn "
                        + "JOIN PhieuKiemTra pkt ON pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan "
                        + "JOIN PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra "
                        + "JOIN HoaDon hd ON hd.maPhieuSuaChua = psc.maPhieuSuaChua "
                        + "WHERE hd.maHoaDon = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlTienDo)) {
                    ps.setString(1, PhieuTiepNhan.TIEN_DO_HOAN_THANH);
                    ps.setString(2, maHD);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return maGD;
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ignore) {
            }
            throw ex;
        } finally {
            try {
                conn.setAutoCommit(autoCu);
            } catch (SQLException ignore) {
            }
        }
    }

    /** Sinh mã dạng GD001, GD002... (gọi trong transaction đang mở). */
    private String taoMaGD(Connection conn) throws SQLException {
        String sql = "SELECT ISNULL(MAX(CAST(SUBSTRING(maGD, 3, 20) AS INT)), 0) + 1 "
                + "FROM GiaoDichThanhToan WITH (UPDLOCK, HOLDLOCK) WHERE maGD LIKE 'GD%'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            int so = rs.next() ? rs.getInt(1) : 1;
            return String.format("GD%03d", so);
        }
    }

    // ===================== Tiện ích =====================
    private static BigDecimal bd(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }
}
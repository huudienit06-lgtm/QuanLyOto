
package dao;

import connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import util.MaTuDong;

public class ThanhToanDAO {

    public static class ThongTinHoaDon {
        public String maHoaDon;
        public Timestamp ngayLap;
        public String maPhieuSuaChua;
        public BigDecimal tongTien;
        public BigDecimal tienGiamGia;
        public BigDecimal thueVAT;
        public String trangThai;

        public String maKhachHang;
        public String tenKhachHang;
        public String maNV;
        public String maXe;

        public ThongTinHoaDon(
                String maHoaDon,
                Timestamp ngayLap,
                String maPhieuSuaChua,
                BigDecimal tongTien,
                BigDecimal tienGiamGia,
                BigDecimal thueVAT,
                String trangThai,
                String maKhachHang,
                String tenKhachHang, String maNV, String maXe) {

            this.maHoaDon = maHoaDon;
            this.ngayLap = ngayLap;
            this.maPhieuSuaChua = maPhieuSuaChua;
            this.tongTien = tongTien;
            this.trangThai = trangThai;
            this.maKhachHang = maKhachHang;
            this.tenKhachHang = tenKhachHang;
            this.maNV = maNV;
            this.maXe = maXe;
            this.tienGiamGia = tienGiamGia;
            this.thueVAT = thueVAT;
        }
    }

    public static class GiaoDich {
        public String maGD;
        public String maHoaDon;
        public String phuongThucThanhToan;
        public Timestamp thoiGianGD;
        public String trangThaiGD;
        public BigDecimal soTienThanhToan;

        public GiaoDich(
                String maGD,
                String maHoaDon,
                String phuongThucThanhToan,
                Timestamp thoiGianGD,
                String trangThaiGD,
                BigDecimal soTienThanhToan) {
            this.maGD = maGD;
            this.maHoaDon = maHoaDon;
            this.phuongThucThanhToan = phuongThucThanhToan;
            this.thoiGianGD = thoiGianGD;
            this.trangThaiGD = trangThaiGD;
            this.soTienThanhToan = soTienThanhToan;
        }
    }

    private Connection getConnection() throws SQLException {
        ConnectDB.getInstance().connect();
        Connection conn = ConnectDB.getConnection();

        if (conn == null) {
            throw new SQLException("Không thể kết nối SQL Server.");
        }
        return conn;
    }

    public ThongTinHoaDon layHoaDon(String maHD) throws SQLException {
        String sql = "SELECT hd.maHoaDon, hd.ngayLap, hd.maPhieuSuaChua, " +
                "hd.soTien, hd.tienGiamGia, hd.thueVAT, hd.trangThai, " +
                "hd.maNV AS maNV, " +
                "psc.maXe AS maXe, " +
                "kh.maKH AS maKhachHang, " +
                "kh.hoTen AS tenKhachHang " +
                "FROM HoaDon hd " +
                "LEFT JOIN PhieuSuaChua psc " +
                "ON hd.maPhieuSuaChua = psc.maPhieuSuaChua " +
                "LEFT JOIN KhachHang kh " +
                "ON psc.maKH = kh.maKH " +
                "WHERE hd.maHoaDon = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ThongTinHoaDon(
                            rs.getString("maHoaDon"),
                            rs.getTimestamp("ngayLap"),
                            rs.getString("maPhieuSuaChua"),
                            rs.getBigDecimal("soTien"),
                            rs.getBigDecimal("tienGiamGia"),
                            rs.getBigDecimal("thueVAT"),
                            rs.getString("trangThai"),
                            rs.getString("maKhachHang"),
                            rs.getString("tenKhachHang"),
                            rs.getString("maNV"),
                            rs.getString("maXe"));
                }
            }
        }

        return null;
    }

    // Lấy lịch sử giao dịch của một hóa đơn.
    public List<GiaoDich> layLichSu(String maHD) throws SQLException {
        List<GiaoDich> ds = new ArrayList<>();

        String sql = "SELECT maGD, maHoaDon, phuongThucThanhToan, " +
                "thoiGianGD, trangThaiGD, soTienThanhToan " +
                "FROM GiaoDichThanhToan " +
                "WHERE maHoaDon = ? ORDER BY thoiGianGD DESC";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new GiaoDich(
                            rs.getString("maGD"),
                            rs.getString("maHoaDon"),
                            rs.getString("phuongThucThanhToan"),
                            rs.getTimestamp("thoiGianGD"),
                            rs.getString("trangThaiGD"),
                            rs.getBigDecimal("soTienThanhToan")));
                }
            }
        }

        return ds;
    }

    // Ghi nhận một giao dịch thanh toán.
    public boolean ghiNhanThanhToan(
            String maHD,
            BigDecimal soTien,
            String phuongThuc) throws SQLException {

        if (maHD == null || maHD.trim().isEmpty()) {
            throw new SQLException("Mã hóa đơn không được để trống.");
        }

        if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Số tiền thanh toán phải lớn hơn 0.");
        }

        if (phuongThuc == null || phuongThuc.trim().isEmpty()) {
            throw new SQLException("Vui lòng chọn phương thức thanh toán.");
        }

        try (Connection conn = getConnection()) {
            boolean autoCommitCu = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                BigDecimal tongTien;

                // Khóa dòng hóa đơn trong lúc kiểm tra thanh toán.
                String sqlHoaDon = "SELECT soTien FROM HoaDon WITH (UPDLOCK, HOLDLOCK) " +
                        "WHERE maHoaDon = ?";

                try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
                    ps.setString(1, maHD);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Không tìm thấy hóa đơn " + maHD);
                        }
                        tongTien = rs.getBigDecimal("soTien");
                    }
                }

                if (tongTien == null ||
                        tongTien.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("Tổng tiền hóa đơn không hợp lệ.");
                }

                BigDecimal daThanhToan = BigDecimal.ZERO;

                String sqlTong = "SELECT COALESCE(SUM(soTienThanhToan), 0) AS daThanhToan " +
                        "FROM GiaoDichThanhToan " +
                        "WHERE maHoaDon = ? AND trangThaiGD = N'Thành công'";

                try (PreparedStatement ps = conn.prepareStatement(sqlTong)) {
                    ps.setString(1, maHD);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            daThanhToan = rs.getBigDecimal("daThanhToan");
                        }
                    }
                }

                if (daThanhToan == null) {
                    daThanhToan = BigDecimal.ZERO;
                }

                BigDecimal conLai = tongTien.subtract(daThanhToan);

                if (conLai.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new SQLException("Hóa đơn đã được thanh toán đủ.");
                }

                if (soTien.compareTo(conLai) > 0) {
                    throw new SQLException(
                            "Số tiền thanh toán vượt số tiền còn lại: " + conLai);
                }

                List<String> danhSachMaGD = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT maGD FROM GiaoDichThanhToan WITH (UPDLOCK, HOLDLOCK)");
                        ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        danhSachMaGD.add(rs.getString("maGD"));
                    }
                }
                String maGD = MaTuDong.sinhMa(
                        MaTuDong.TIEN_TO_GIAO_DICH,
                        danhSachMaGD,
                        ma -> ma);

                String sqlInsert = "INSERT INTO GiaoDichThanhToan " +
                        "(maGD, maHoaDon, phuongThucThanhToan, " +
                        "trangThaiGD, soTienThanhToan) " +
                        "VALUES (?, ?, ?, N'Thành công', ?)";

                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setString(1, maGD);
                    ps.setString(2, maHD);
                    ps.setString(3, phuongThuc);
                    ps.setBigDecimal(4, soTien);
                    ps.executeUpdate();
                }

                BigDecimal tongDaTra = daThanhToan.add(soTien);
                String trangThaiHD = tongDaTra.compareTo(tongTien) >= 0
                        ? "Đã thanh toán"
                        : "Thanh toán một phần";

                String sqlUpdate = "UPDATE HoaDon SET trangThai = ? WHERE maHoaDon = ?";

                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setString(1, trangThaiHD);
                    ps.setString(2, maHD);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                throw new SQLException(
                        "Không thể lưu giao dịch thanh toán.", e);

            } finally {
                conn.setAutoCommit(autoCommitCu);
            }
        }
    }
}

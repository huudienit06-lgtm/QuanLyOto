package dao;

import entity.HoaDon;
import entity.PhieuYeuCauBaoHanh;
import entity.PhuTung;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PhieuYeuCauBaoHanhDAO {
    public static final String CHO_XU_LY = "Chờ xử lý";
    public static final String CHAP_NHAN = "Chấp nhận bảo hành";
    public static final String HOAN_THANH = "Hoàn thành bảo hành";
    public static final String TU_CHOI = "Từ chối bảo hành";

    public record YeuCauHienThi(String maPhieuYCBH, String maHoaDon, String maPhuTung,
            String tenPhuTung, String tenKhachHang, int soLuong,
            Timestamp ngayTiepNhan, String trangThai, String lyDo, String ghiChuXuLy,
            Timestamp ngayXuLy, Timestamp ngayHoanThanh) {
    }

    public record LuaChonHoaDon(String maHoaDon, String tenKhachHang, Timestamp ngayLap) {
        @Override
        public String toString() {
            return maHoaDon + " - " + tenKhachHang;
        }
    }

    public record LuaChonPhuTung(String maPhuTung, String tenPhuTung, int soLuongHoaDon,
            int thoiHanBaoHanh, String donViThoiGian) {
        @Override
        public String toString() {
            if (thoiHanBaoHanh <= 0) {
                return maPhuTung + " - " + tenPhuTung;
            }
            String donVi = switch (donViThoiGian) {
                case "NGAY" -> "ngày";
                case "NAM" -> "năm";
                default -> "tháng";
            };
            return maPhuTung + " - " + tenPhuTung + " (bảo hành "
                    + thoiHanBaoHanh + " " + donVi + ")";
        }
    }

    public String layMaTiepTheo() throws SQLException {
        int maLonNhat = 0;
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement("""
                SELECT maPhieuYCBH
                FROM PhieuYeuCauBaoHanh
                WHERE maPhieuYCBH LIKE 'YCBH%'
                """);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString(1);
                try {
                    maLonNhat = Math.max(maLonNhat, Integer.parseInt(ma.substring(4)));
                } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                    // Bỏ qua mã không theo mẫu YCBH + số.
                }
            }
        }
        return dinhDangMaMoi(maLonNhat);
    }

    public List<YeuCauHienThi> timKiem(String tuKhoa) throws SQLException {
        String sql = """
                SELECT yc.maPhieuYCBH, yc.maHoaDon, yc.maPhuTung,
                       pt.tenPhuTung, kh.hoTen AS tenKhachHang, yc.soLuong,
                       yc.ngayTiepNhan, yc.trangThai, yc.lyDo, yc.ghiChuXuLy,
                       yc.ngayXuLy, yc.ngayHoanThanh
                FROM PhieuYeuCauBaoHanh yc
                JOIN PhuTung pt ON pt.maPhuTung = yc.maPhuTung
                JOIN HoaDon hd ON hd.maHoaDon = yc.maHoaDon
                LEFT JOIN PhieuSuaChua psc ON psc.maPhieuSuaChua = hd.maPhieuSuaChua
                LEFT JOIN KhachHang kh ON kh.maKH = psc.maKH
                WHERE ? = '' OR yc.maPhieuYCBH LIKE ? OR yc.maHoaDon LIKE ?
                      OR yc.maPhuTung LIKE ? OR pt.tenPhuTung LIKE ?
                      OR kh.hoTen LIKE ? OR yc.lyDo LIKE ? OR yc.trangThai LIKE ?
                ORDER BY CASE yc.trangThai
                    WHEN N'Chờ xử lý' THEN 1
                    WHEN N'Chấp nhận bảo hành' THEN 2
                    WHEN N'Hoàn thành bảo hành' THEN 3
                    WHEN N'Từ chối bảo hành' THEN 4
                    ELSE 5
                END, yc.ngayTiepNhan DESC, yc.maPhieuYCBH
                """;
        String keyword = tuKhoa == null ? "" : tuKhoa.trim();
        List<YeuCauHienThi> dsYeuCau = new ArrayList<>();
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyword);
            for (int i = 2; i <= 8; i++) {
                ps.setString(i, "%" + keyword + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsYeuCau.add(new YeuCauHienThi(
                            rs.getString("maPhieuYCBH"),
                            rs.getString("maHoaDon"),
                            rs.getString("maPhuTung"),
                            rs.getString("tenPhuTung"),
                            rs.getString("tenKhachHang"),
                            rs.getInt("soLuong"),
                            rs.getTimestamp("ngayTiepNhan"),
                            rs.getString("trangThai"),
                            rs.getString("lyDo"),
                            rs.getString("ghiChuXuLy"),
                            rs.getTimestamp("ngayXuLy"),
                            rs.getTimestamp("ngayHoanThanh")));
                }
            }
        }
        return dsYeuCau;
    }

    public List<LuaChonHoaDon> layHoaDonCoBaoHanh() throws SQLException {
        String sql = """
                SELECT DISTINCT hd.maHoaDon, kh.hoTen, hd.ngayLap
                FROM HoaDon hd
                JOIN PhieuSuaChua psc ON psc.maPhieuSuaChua = hd.maPhieuSuaChua
                JOIN KhachHang kh ON kh.maKH = psc.maKH
                WHERE EXISTS (
                    SELECT 1
                    FROM ChiTietHoaDon cthd
                    JOIN ChinhSachBaoHanh_PhuTung csp ON csp.maPhuTung = cthd.maPhuTung
                    JOIN ChinhSachBaoHanh cs ON cs.maCSBH = csp.maCSBH AND cs.trangThai = 1
                    WHERE cthd.maHoaDon = hd.maHoaDon
                )
                ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC
                """;
        List<LuaChonHoaDon> dsHoaDon = new ArrayList<>();
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dsHoaDon.add(new LuaChonHoaDon(
                        rs.getString("maHoaDon"), rs.getString("hoTen"), rs.getTimestamp("ngayLap")));
            }
        }
        return dsHoaDon;
    }

    public List<LuaChonPhuTung> layPhuTungTheoHoaDon(String maHoaDon) throws SQLException {
        String sql = """
                SELECT cthd.maPhuTung, pt.tenPhuTung, SUM(cthd.soLuong) AS soLuongHoaDon,
                       cs.thoiHanBaoHanh, cs.donViThoiGian
                FROM ChiTietHoaDon cthd
                JOIN HoaDon hd ON hd.maHoaDon = cthd.maHoaDon
                JOIN PhuTung pt ON pt.maPhuTung = cthd.maPhuTung
                CROSS APPLY (
                    SELECT TOP (1) cs.thoiHanBaoHanh, cs.donViThoiGian
                    FROM ChinhSachBaoHanh_PhuTung csp
                    JOIN ChinhSachBaoHanh cs ON cs.maCSBH = csp.maCSBH
                    WHERE csp.maPhuTung = cthd.maPhuTung AND cs.trangThai = 1
                    ORDER BY cs.ngayTao DESC, cs.maCSBH DESC
                ) cs
                WHERE cthd.maHoaDon = ?
                GROUP BY cthd.maPhuTung, pt.tenPhuTung, cs.thoiHanBaoHanh, cs.donViThoiGian
                ORDER BY pt.tenPhuTung, cthd.maPhuTung
                """;
        List<LuaChonPhuTung> dsPhuTung = new ArrayList<>();
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsPhuTung.add(new LuaChonPhuTung(
                            rs.getString("maPhuTung"),
                            rs.getString("tenPhuTung"),
                            rs.getInt("soLuongHoaDon"),
                            rs.getInt("thoiHanBaoHanh"),
                            rs.getString("donViThoiGian")));
                }
            }
        }
        return dsPhuTung;
    }

    public void them(PhieuYeuCauBaoHanh yeuCau) throws SQLException {
        kiemTraYeuCau(yeuCau);
        giaoDich(con -> {
            kiemTraPhuTungDuocBaoHanh(con, maHoaDon(yeuCau), maPhuTung(yeuCau),
                    yeuCau.getSoLuong(), true);
            String maPhieu = taoMaMoi(con);
            String sql = """
                    INSERT INTO PhieuYeuCauBaoHanh
                        (maPhieuYCBH, maHoaDon, maPhuTung, soLuong, trangThai, lyDo)
                    VALUES (?, ?, ?, ?, N'Chờ xử lý', ?)
                    """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maPhieu);
                ps.setString(2, maHoaDon(yeuCau));
                ps.setString(3, maPhuTung(yeuCau));
                ps.setInt(4, yeuCau.getSoLuong());
                ps.setString(5, yeuCau.getLyDo());
                ps.executeUpdate();
            }
            yeuCau.setMaPhieuYCBH(maPhieu);
            yeuCau.setTrangThai(CHO_XU_LY);
            return null;
        });
    }

    public void capNhatChoXuLy(PhieuYeuCauBaoHanh yeuCau) throws SQLException {
        kiemTraYeuCau(yeuCau);
        giaoDich(con -> {
            try (PreparedStatement ps = con.prepareStatement("""
                    SELECT maHoaDon, maPhuTung
                    FROM PhieuYeuCauBaoHanh WITH (UPDLOCK, HOLDLOCK)
                    WHERE maPhieuYCBH = ? AND trangThai = N'Chờ xử lý'
                    """)) {
                ps.setString(1, yeuCau.getMaPhieuYCBH());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Chỉ có thể cập nhật yêu cầu đang chờ xử lý.");
                    }
                    HoaDon hoaDon = new HoaDon();
                    hoaDon.setMaHoaDon(rs.getString("maHoaDon"));
                    PhuTung phuTung = new PhuTung();
                    phuTung.setMaPhuTung(rs.getString("maPhuTung"));
                    yeuCau.setHoaDon(hoaDon);
                    yeuCau.setPhuTung(phuTung);
                }
            }
            kiemTraPhuTungDuocBaoHanh(con, maHoaDon(yeuCau), maPhuTung(yeuCau),
                    yeuCau.getSoLuong(), false);
            try (PreparedStatement ps = con.prepareStatement("""
                    UPDATE PhieuYeuCauBaoHanh
                    SET soLuong = ?, lyDo = ?
                    WHERE maPhieuYCBH = ? AND trangThai = N'Chờ xử lý'
                    """)) {
                ps.setInt(1, yeuCau.getSoLuong());
                ps.setString(2, yeuCau.getLyDo());
                ps.setString(3, yeuCau.getMaPhieuYCBH());
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Yêu cầu đã được xử lý hoặc không còn tồn tại.");
                }
            }
            return null;
        });
    }

    public boolean xoaChoXuLy(String maPhieuYCBH) throws SQLException {
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement(
                "DELETE FROM PhieuYeuCauBaoHanh WHERE maPhieuYCBH = ? AND trangThai = N'Chờ xử lý'")) {
            ps.setString(1, maPhieuYCBH);
            return ps.executeUpdate() > 0;
        }
    }

    public void chapNhan(String maPhieuYCBH, String ghiChuXuLy) throws SQLException {
        giaoDich(con -> {
            String maHoaDon;
            String maPhuTung;
            int soLuong;
            Timestamp ngayTiepNhan;
            try (PreparedStatement ps = con.prepareStatement("""
                    SELECT maHoaDon, maPhuTung, soLuong, ngayTiepNhan
                    FROM PhieuYeuCauBaoHanh WITH (UPDLOCK, HOLDLOCK)
                    WHERE maPhieuYCBH = ? AND trangThai = N'Chờ xử lý'
                    """)) {
                ps.setString(1, maPhieuYCBH);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Chỉ có thể chấp nhận yêu cầu đang chờ xử lý.");
                    }
                    maHoaDon = rs.getString("maHoaDon");
                    maPhuTung = rs.getString("maPhuTung");
                    soLuong = rs.getInt("soLuong");
                    ngayTiepNhan = rs.getTimestamp("ngayTiepNhan");
                }
            }
            kiemTraPhuTungDuocBaoHanhTaiThoiDiem(con, maHoaDon, maPhuTung, ngayTiepNhan);
            try (PreparedStatement ps = con.prepareStatement("""
                    UPDATE PhuTung
                    SET soLuongTon = soLuongTon + ?
                    WHERE maPhuTung = ? AND soLuongTon <= 2147483647 - ?
                    """)) {
                ps.setInt(1, soLuong);
                ps.setString(2, maPhuTung);
                ps.setInt(3, soLuong);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Không thể cập nhật tồn kho phụ tùng " + maPhuTung
                            + "; hãy kiểm tra mã phụ tùng và giới hạn số lượng.");
                }
            }
            capNhatTrangThai(con, maPhieuYCBH, CHO_XU_LY, CHAP_NHAN, ghiChuXuLy, false);
            return null;
        });
    }

    public void tuChoi(String maPhieuYCBH, String ghiChuXuLy) throws SQLException {
        giaoDich(con -> {
            capNhatTrangThai(con, maPhieuYCBH, CHO_XU_LY, TU_CHOI, ghiChuXuLy, false);
            return null;
        });
    }

    public void hoanThanh(String maPhieuYCBH, String ghiChuXuLy) throws SQLException {
        giaoDich(con -> {
            capNhatTrangThai(con, maPhieuYCBH, CHAP_NHAN, HOAN_THANH, ghiChuXuLy, true);
            return null;
        });
    }

    private void capNhatTrangThai(Connection con, String maPhieuYCBH, String trangThaiCu,
            String trangThaiMoi, String ghiChuXuLy, boolean hoanThanh) throws SQLException {
        String sql = hoanThanh
                ? """
                  UPDATE PhieuYeuCauBaoHanh
                  SET trangThai = ?, ghiChuXuLy = ?, ngayHoanThanh = SYSDATETIME()
                  WHERE maPhieuYCBH = ? AND trangThai = ?
                  """
                : """
                  UPDATE PhieuYeuCauBaoHanh
                  SET trangThai = ?, ghiChuXuLy = ?, ngayXuLy = SYSDATETIME()
                  WHERE maPhieuYCBH = ? AND trangThai = ?
                  """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setString(2, ghiChuXuLy);
            ps.setString(3, maPhieuYCBH);
            ps.setString(4, trangThaiCu);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Không thể chuyển trạng thái yêu cầu bảo hành.");
            }
        }
    }

    private void kiemTraPhuTungDuocBaoHanh(Connection con, String maHoaDon, String maPhuTung,
            int soLuong, boolean batBuocConHan) throws SQLException {
        String sql = batBuocConHan
                ? """
                  SELECT SUM(cthd.soLuong) AS soLuongHoaDon
                  FROM ChiTietHoaDon cthd
                  JOIN HoaDon hd ON hd.maHoaDon = cthd.maHoaDon
                  CROSS APPLY (
                      SELECT TOP (1) cs.thoiHanBaoHanh, cs.donViThoiGian
                      FROM ChinhSachBaoHanh_PhuTung csp
                      JOIN ChinhSachBaoHanh cs ON cs.maCSBH = csp.maCSBH
                      WHERE csp.maPhuTung = cthd.maPhuTung AND cs.trangThai = 1
                      ORDER BY cs.ngayTao DESC, cs.maCSBH DESC
                  ) cs
                  WHERE cthd.maHoaDon = ? AND cthd.maPhuTung = ?
                    AND CAST(GETDATE() AS DATE) <= CAST(
                        CASE cs.donViThoiGian
                            WHEN 'NGAY' THEN DATEADD(DAY, cs.thoiHanBaoHanh, hd.ngayLap)
                            WHEN 'NAM' THEN DATEADD(YEAR, cs.thoiHanBaoHanh, hd.ngayLap)
                            ELSE DATEADD(MONTH, cs.thoiHanBaoHanh, hd.ngayLap)
                        END AS DATE)
                  GROUP BY cthd.maHoaDon, cthd.maPhuTung
                  """
                : """
                  SELECT SUM(soLuong) AS soLuongHoaDon
                  FROM ChiTietHoaDon
                  WHERE maHoaDon = ? AND maPhuTung = ?
                  GROUP BY maHoaDon, maPhuTung
                  """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maPhuTung);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(batBuocConHan
                            ? "Phụ tùng không thuộc hóa đơn hoặc không còn hạn/chính sách bảo hành."
                            : "Phụ tùng không thuộc hóa đơn đã chọn.");
                }
                if (soLuong > rs.getInt("soLuongHoaDon")) {
                    throw new SQLException("Số lượng yêu cầu vượt quá số lượng phụ tùng trên hóa đơn.");
                }
            }
        }
    }

    private void kiemTraPhuTungDuocBaoHanhTaiThoiDiem(
            Connection con, String maHoaDon, String maPhuTung, Timestamp ngayTiepNhan) throws SQLException {
        String sql = """
                SELECT TOP (1) 1
                FROM ChiTietHoaDon cthd
                JOIN HoaDon hd ON hd.maHoaDon = cthd.maHoaDon
                JOIN ChinhSachBaoHanh_PhuTung csp ON csp.maPhuTung = cthd.maPhuTung
                JOIN ChinhSachBaoHanh cs ON cs.maCSBH = csp.maCSBH
                WHERE cthd.maHoaDon = ? AND cthd.maPhuTung = ?
                  AND CAST(? AS DATE) <= CAST(
                      CASE cs.donViThoiGian
                          WHEN 'NGAY' THEN DATEADD(DAY, cs.thoiHanBaoHanh, hd.ngayLap)
                          WHEN 'NAM' THEN DATEADD(YEAR, cs.thoiHanBaoHanh, hd.ngayLap)
                          ELSE DATEADD(MONTH, cs.thoiHanBaoHanh, hd.ngayLap)
                      END AS DATE)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maPhuTung);
            ps.setTimestamp(3, ngayTiepNhan);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không thể chấp nhận: phụ tùng không có bảo hành hợp lệ tại ngày tiếp nhận.");
                }
            }
        }
    }

    private String taoMaMoi(Connection con) throws SQLException {
        String sql = """
                SELECT maPhieuYCBH
                FROM PhieuYeuCauBaoHanh WITH (UPDLOCK, HOLDLOCK)
                WHERE maPhieuYCBH LIKE 'YCBH%'
                """;
        int maLonNhat = 0;
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString(1);
                try {
                    maLonNhat = Math.max(maLonNhat, Integer.parseInt(ma.substring(4)));
                } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                    // Bỏ qua mã không theo mẫu YCBH + số.
                }
            }
        }
        return dinhDangMaMoi(maLonNhat);
    }

    private String dinhDangMaMoi(int maLonNhat) throws SQLException {
        if (maLonNhat >= 999) {
            throw new SQLException("Đã hết dải mã phiếu yêu cầu bảo hành tự động.");
        }
        return String.format("YCBH%03d", maLonNhat + 1);
    }

    private void kiemTraYeuCau(PhieuYeuCauBaoHanh yeuCau) throws SQLException {
        if (yeuCau == null) {
            throw new SQLException("Thông tin yêu cầu bảo hành không được để trống.");
        }
        if (maHoaDon(yeuCau) == null || maHoaDon(yeuCau).isBlank()
                || maPhuTung(yeuCau) == null || maPhuTung(yeuCau).isBlank()) {
            throw new SQLException("Cần chọn hóa đơn và phụ tùng được yêu cầu bảo hành.");
        }

        if (yeuCau.getSoLuong() <= 0) {
            throw new SQLException("Số lượng yêu cầu phải lớn hơn 0.");
        }
        if (yeuCau.getLyDo() == null || yeuCau.getLyDo().isBlank()) {
            throw new SQLException("Lý do yêu cầu bảo hành không được để trống.");
        }
        if (yeuCau.getLyDo() != null && yeuCau.getLyDo().length() > 1000) {
            throw new SQLException("Lý do không được vượt quá 1000 ký tự.");
        }
    }

    private String maHoaDon(PhieuYeuCauBaoHanh yeuCau) {
        return yeuCau.getHoaDon() == null ? null : yeuCau.getHoaDon().getMaHoaDon();
    }

    private String maPhuTung(PhieuYeuCauBaoHanh yeuCau) {
        return yeuCau.getPhuTung() == null ? null : yeuCau.getPhuTung().getMaPhuTung();
    }

    private Connection ketNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }

    @FunctionalInterface
    private interface TacVu<R> {
        R chay(Connection con) throws SQLException;
    }

    private <R> R giaoDich(TacVu<R> tacVu) throws SQLException {
        try (Connection con = ketNoi()) {
            boolean autoCommit = con.getAutoCommit();
            try {
                con.setAutoCommit(false);
                R ketQua = tacVu.chay(con);
                con.commit();
                return ketQua;
            } catch (SQLException | RuntimeException exception) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        }
    }
}

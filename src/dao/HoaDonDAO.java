package dao;

import connectDB.ConnectDB;
import entity.HoaDon;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.MaTuDong;

public class HoaDonDAO extends BaseDAO<HoaDon> {
    // DỮ LIỆU TRẢ VỀ CHO GIAO DIỆN
    public record PhuTung(String ma, String ten, BigDecimal giaBan) {
        @Override
        public String toString() {
            return ma.isEmpty() ? ten : ma + " - " + ten;
        }
    }

    public record KhuyenMai(String ma, String ten, BigDecimal tile) {
        @Override
        public String toString() {
            return ma.isEmpty() ? ten : ten + " (" + ma + ") - " + tile.stripTrailingZeros().toPlainString() + "%";
        }
    }

    /** Một dòng chi tiết: dịch vụ hoặc phụ tùng. */
    public record DongChiTiet(boolean dichVu, String ma, String ten, int soLuong, BigDecimal donGia) {
        public BigDecimal thanhTien() {
            return donGia.multiply(BigDecimal.valueOf(soLuong));
        }
    }

    public record Phieu(String ma, String maKH, String tenKH, String soDT,
            String maXe, String bienSo, String tenXe,
            String maNV, String tenNV, Timestamp ngayBatDau) {
    }

    public HoaDonDAO() {
        super(HoaDon.class, "HoaDon", "maHoaDon");
    }

    // MÃ HÓA ĐƠN: Sinh mã tiếp theo: HD001, HD002...
    public String taoMaHoaDonMoi() throws SQLException {
        return MaTuDong.sinhMa(
                MaTuDong.TIEN_TO_HOA_DON,
                findAll(),
                HoaDon::getMaHoaDon);
    }

    // LƯU / XÓA (CÓ TRANSACTION): Lưu hóa đơn và toàn bộ chi tiết trong cùng một
    // transaction.
    public void luuHoaDon(String maHD, String maPSC, String maNV, String maKM, String loaiHoaDon,
            LocalDate ngayLap, BigDecimal tienGiam, BigDecimal tienVAT, BigDecimal soTien, List<DongChiTiet> chiTiet)
            throws SQLException {
        String sqlHD = """
                INSERT INTO HoaDon
                    (maHoaDon, maPhieuSuaChua, maNV, maKM,
                     ngayLap, tienGiamGia, thueVAT, soTien, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, N'Chưa thanh toán')
                """;
        String sqlCT = """
                INSERT INTO ChiTietHoaDon
                    (maHoaDon, maPhieuSuaChua, maPhuTung, maDV, soLuong, donGia)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        giaoDich(c -> {
            try (PreparedStatement ps = c.prepareStatement(sqlHD)) {
                ps.setString(1, maHD);
                ps.setString(2, maPSC);
                ps.setString(3, maNV);
                ps.setString(4, maKM);
                ps.setTimestamp(5, Timestamp.valueOf(ngayLap.atStartOfDay()));
                ps.setBigDecimal(6, tienGiam);
                ps.setBigDecimal(7, tienVAT);
                ps.setBigDecimal(8, soTien);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(sqlCT)) {
                for (DongChiTiet d : chiTiet) {
                    ps.setString(1, maHD);
                    ps.setString(2, maPSC);
                    ps.setString(3, d.dichVu() ? null : d.ma());
                    ps.setString(4, d.dichVu() ? d.ma() : null);
                    ps.setInt(5, d.soLuong());
                    ps.setBigDecimal(6, d.donGia());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return null;
        });
    }

    /** Xóa chi tiết trước rồi mới xóa hóa đơn để không vướng khóa ngoại. */
    public boolean xoaHoaDon(String maHD) throws SQLException {
        return giaoDich(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
                ps.setString(1, maHD);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM HoaDon WHERE maHoaDon = ?")) {
                ps.setString(1, maHD);
                return ps.executeUpdate() > 0;
            }
        });
    }

    // DANH MỤC
    public List<PhuTung> layDanhSachPhuTung() throws SQLException {
        String sql = "SELECT maPhuTung, tenPhuTung, giaBan FROM PhuTung ORDER BY maPhuTung";
        List<PhuTung> ds = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new PhuTung(rs.getString(1), rs.getString(2), khacNull(rs.getBigDecimal(3))));
            }
        }
        return ds;
    }

    public List<KhuyenMai> layKhuyenMaiDangApDung() throws SQLException {
        String sql = """
                SELECT maKM, tenKM, tileGiam
                FROM ChuongTrinhKhuyenMai
                WHERE trangThai = N'Đang áp dụng'
                  AND ngayBatDau <= CAST(GETDATE() AS DATE)
                  AND ngayKetThuc >= CAST(GETDATE() AS DATE)
                  AND tileGiam BETWEEN 0 AND 100
                ORDER BY maKM
                """;
        List<KhuyenMai> ds = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BigDecimal tile = rs.getBigDecimal("tileGiam");
                if (tile != null) {
                    ds.add(new KhuyenMai(rs.getString("maKM"), rs.getString("tenKM"), tile));
                }
            }
        }
        return ds;
    }

    // PHIẾU SỬA CHỮA
    private static final String SQL_PHIEU = """
            SELECT p.maPhieuSuaChua, p.maKH, kh.hoTen, kh.soDT,
                   p.maXe, x.bienSo, x.hangXe, x.dongXe,
                   p.maNV, nv.hoTen AS tenNV, p.ngayBatDau
            FROM PhieuSuaChua p
            JOIN KhachHang kh ON p.maKH = kh.maKH
            JOIN Xe x ON p.maXe = x.maXe
            JOIN NhanVien nv ON p.maNV = nv.maNV
            """;

    /** Trả về null nếu không có phiếu. */
    public Phieu layPhieu(String maPhieu) throws SQLException {
        List<Phieu> ds = truyVanPhieu("WHERE p.maPhieuSuaChua = ?", maPhieu);
        return ds.isEmpty() ? null : ds.get(0);
    }

    /** Tìm theo mã phiếu, mã KH, mã xe, tên KH, SĐT hoặc biển số. */
    public List<Phieu> timPhieu(String tuKhoa) throws SQLException {
        String m = "%" + tuKhoa + "%";
        return truyVanPhieu("""
                WHERE p.maPhieuSuaChua LIKE ? OR p.maKH LIKE ? OR p.maXe LIKE ?
                   OR kh.hoTen LIKE ? OR kh.soDT LIKE ? OR x.bienSo LIKE ?
                """, m, m, m, m, m, m);
    }

    private List<Phieu> truyVanPhieu(String dieuKien, String... thamSo) throws SQLException {
        List<Phieu> ds = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                SQL_PHIEU + dieuKien + " ORDER BY p.ngayBatDau DESC")) {
            for (int i = 0; i < thamSo.length; i++) {
                ps.setString(i + 1, thamSo[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String hang = rs.getString("hangXe");
                    String dong = rs.getString("dongXe");
                    String tenXe = ((hang == null ? "" : hang) + " " + (dong == null ? "" : dong)).trim();
                    ds.add(new Phieu(
                            rs.getString("maPhieuSuaChua"), rs.getString("maKH"),
                            rs.getString("hoTen"), rs.getString("soDT"),
                            rs.getString("maXe"), rs.getString("bienSo"), tenXe,
                            rs.getString("maNV"), rs.getString("tenNV"),
                            rs.getTimestamp("ngayBatDau")));
                }
            }
        }
        return ds;
    }

    /** Dịch vụ xếp trước, phụ tùng xếp sau. */
    public List<DongChiTiet> layChiTietPhieu(String maPhieu) throws SQLException {
        String sql = """
                SELECT ct.maDV, dv.tenDV, ct.maPhuTung, pt.tenPhuTung,
                       ct.donGia, ct.soLuong
                FROM ChiTietPhieuSuaChua ct
                LEFT JOIN DichVu dv ON ct.maDV = dv.maDV
                LEFT JOIN PhuTung pt ON ct.maPhuTung = pt.maPhuTung
                WHERE ct.maPhieuSuaChua = ?
                ORDER BY CASE WHEN ct.maDV IS NULL THEN 1 ELSE 0 END, ct.maChiTiet
                """;
        List<DongChiTiet> ds = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean laDV = rs.getString("maDV") != null;
                    ds.add(new DongChiTiet(
                            laDV,
                            rs.getString(laDV ? "maDV" : "maPhuTung"),
                            rs.getString(laDV ? "tenDV" : "tenPhuTung"),
                            rs.getInt("soLuong"),
                            khacNull(rs.getBigDecimal("donGia"))));
                }
            }
        }
        return ds;
    }

    // TIỆN ÍCH NỘI BỘ
    private Connection conn() throws SQLException {
        ConnectDB.getInstance().connect();
        Connection c = ConnectDB.getConnection();
        if (c == null) {
            throw new SQLException("Không thể kết nối SQL Server.");
        }
        return c;
    }

    private static BigDecimal khacNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @FunctionalInterface
    private interface TacVu<R> {
        R chay(Connection c) throws SQLException;
    }

    private <R> R giaoDich(TacVu<R> tacVu) throws SQLException {
        Connection c = conn();
        boolean tuDong = c.getAutoCommit();
        try {
            c.setAutoCommit(false);
            R kq = tacVu.chay(c);
            c.commit();
            return kq;
        } catch (SQLException | RuntimeException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(tuDong);
        }
    }
}
package dao;

import entity.ChinhSachBaoHanh;
import entity.PhuTung;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChinhSachBaoHanhDAO {
    public record PhuTungBaoHanh(String maPhuTung, String tenPhuTung, int soLuong,
            String tenChinhSach, int thoiHan, String donViThoiGian,
            String noiDungBaoHanh, String dieuKienBaoHanh, String truongHopTuChoi) {
    }

    public List<ChinhSachBaoHanh> timKiem(String tuKhoa) throws SQLException {
        String sql = """
                SELECT maCSBH, tenChinhSach, thoiHanBaoHanh, donViThoiGian,
                       dieuKienBaoHanh, truongHopTuChoi, noiDungBaoHanh, ngayTao, trangThai
                FROM ChinhSachBaoHanh cs
                WHERE ? = '' OR maCSBH LIKE ? OR tenChinhSach LIKE ?
                      OR dieuKienBaoHanh LIKE ? OR truongHopTuChoi LIKE ?
                      OR noiDungBaoHanh LIKE ?
                      OR EXISTS (
                          SELECT 1
                          FROM ChinhSachBaoHanh_PhuTung csp
                          JOIN PhuTung pt ON pt.maPhuTung = csp.maPhuTung
                          WHERE csp.maCSBH = cs.maCSBH
                            AND (pt.maPhuTung LIKE ? OR pt.tenPhuTung LIKE ?)
                      )
                ORDER BY ngayTao DESC, maCSBH
                """;
        String tuKhoaTim = tuKhoa == null ? "" : tuKhoa.trim();
        List<ChinhSachBaoHanh> dsChinhSach = new ArrayList<>();
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tuKhoaTim);
            for (int i = 2; i <= 8; i++) {
                ps.setString(i, "%" + tuKhoaTim + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsChinhSach.add(taoChinhSach(rs));
                }
            }
        }
        napPhuTungApDung(dsChinhSach);
        return dsChinhSach;
    }

    public String layMaTiepTheo() throws SQLException {
        try (Connection con = ketNoi()) {
            return layMaTiepTheo(con, false);
        }
    }

    public List<PhuTung> layDanhSachPhuTung() throws SQLException {
        String sql = """
                SELECT maPhuTung, tenPhuTung, donViTinh, giaNhap, giaBan,
                       soLuongTon, nhaCungCap, trangThai
                FROM PhuTung
                ORDER BY tenPhuTung, maPhuTung
                """;
        List<PhuTung> dsPhuTung = new ArrayList<>();
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dsPhuTung.add(new PhuTung(
                        rs.getString("maPhuTung"),
                        rs.getString("tenPhuTung"),
                        rs.getString("donViTinh"),
                        rs.getDouble("giaNhap"),
                        rs.getDouble("giaBan"),
                        rs.getInt("soLuongTon"),
                        rs.getString("nhaCungCap"),
                        rs.getString("trangThai")));
            }
        }
        return dsPhuTung;
    }

    public void luu(ChinhSachBaoHanh chinhSach) throws SQLException {
        luu(chinhSach, chinhSach == null || chinhSach.getMaCSBH() == null
                || chinhSach.getMaCSBH().isBlank());
    }

    public void luu(ChinhSachBaoHanh chinhSach, boolean themMoi) throws SQLException {
        kiemTraDuLieu(chinhSach);
        List<PhuTung> dsPhuTung = chinhSach.getDsPhuTung();
        Set<String> dsMaPhuTung = new LinkedHashSet<>();
        for (PhuTung phuTung : dsPhuTung) {
            dsMaPhuTung.add(phuTung == null ? null : phuTung.getMaPhuTung());
        }
        if (dsMaPhuTung.size() != dsPhuTung.size()) {
            throw new SQLException("Danh sách phụ tùng áp dụng bị trùng.");
        }
        if (dsMaPhuTung.isEmpty()) {
            throw new SQLException("Vui lòng chọn ít nhất một phụ tùng áp dụng.");
        }
        if (dsMaPhuTung.contains(null) || dsMaPhuTung.contains("")) {
            throw new SQLException("Mã phụ tùng áp dụng không hợp lệ.");
        }

        try (Connection con = ketNoi()) {
            boolean autoCommit = con.getAutoCommit();
            int isolation = con.getTransactionIsolation();
            try {
                con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                con.setAutoCommit(false);
                String maCSBH = chinhSach.getMaCSBH();
                if (themMoi) {
                    maCSBH = taoMaMoi(con);
                    chinhSach.setMaCSBH(maCSBH);
                    themChinhSach(con, chinhSach);
                } else {
                    if (maCSBH == null || maCSBH.isBlank()) {
                        throw new SQLException("Mã chính sách cần cập nhật không được để trống.");
                    }
                    capNhatChinhSach(con, chinhSach);
                }

                if (chinhSach.isTrangThai()) {
                    kiemTraPhuTungDaCoChinhSach(con, maCSBH, dsMaPhuTung);
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChinhSachBaoHanh_PhuTung WHERE maCSBH = ?")) {
                    ps.setString(1, maCSBH);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO ChinhSachBaoHanh_PhuTung (maCSBH, maPhuTung) VALUES (?, ?)")) {
                    for (String maPhuTung : dsMaPhuTung) {
                        ps.setString(1, maCSBH);
                        ps.setString(2, maPhuTung);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
            } catch (SQLException | RuntimeException exception) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            } finally {
                con.setAutoCommit(autoCommit);
                con.setTransactionIsolation(isolation);
            }
        }
    }

    public boolean xoa(String maCSBH) throws SQLException {
        try (Connection con = ketNoi()) {
            boolean autoCommit = con.getAutoCommit();
            try {
                con.setAutoCommit(false);
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChinhSachBaoHanh_PhuTung WHERE maCSBH = ?")) {
                    ps.setString(1, maCSBH);
                    ps.executeUpdate();
                }
                int soDong;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChinhSachBaoHanh WHERE maCSBH = ?")) {
                    ps.setString(1, maCSBH);
                    soDong = ps.executeUpdate();
                }
                con.commit();
                return soDong > 0;
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

    public List<PhuTungBaoHanh> layPhuTungBaoHanhTheoPhieu(String maPhieuSuaChua) throws SQLException {
        String sql = """
                WITH PhuTungTrongPhieu AS (
                    SELECT maPhuTung, SUM(soLuong) AS soLuong
                    FROM ChiTietPhieuSuaChua
                    WHERE maPhieuSuaChua = ? AND maPhuTung IS NOT NULL
                    GROUP BY maPhuTung
                )
                SELECT pt.maPhuTung, pt.tenPhuTung, p.soLuong,
                       cs.tenChinhSach, cs.thoiHanBaoHanh, cs.donViThoiGian,
                       cs.noiDungBaoHanh, cs.dieuKienBaoHanh, cs.truongHopTuChoi
                FROM PhuTungTrongPhieu p
                JOIN PhuTung pt ON pt.maPhuTung = p.maPhuTung
                CROSS APPLY (
                    SELECT TOP (1) cs.tenChinhSach, cs.thoiHanBaoHanh,
                           cs.donViThoiGian, cs.noiDungBaoHanh,
                           cs.dieuKienBaoHanh, cs.truongHopTuChoi
                    FROM ChinhSachBaoHanh_PhuTung csp
                    JOIN ChinhSachBaoHanh cs ON cs.maCSBH = csp.maCSBH
                    WHERE csp.maPhuTung = p.maPhuTung AND cs.trangThai = 1
                    ORDER BY cs.ngayTao DESC, cs.maCSBH DESC
                ) cs
                ORDER BY pt.tenPhuTung, pt.maPhuTung
                """;
        List<PhuTungBaoHanh> dsBaoHanh = new ArrayList<>();
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieuSuaChua);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsBaoHanh.add(new PhuTungBaoHanh(
                            rs.getString("maPhuTung"),
                            rs.getString("tenPhuTung"),
                            rs.getInt("soLuong"),
                            rs.getString("tenChinhSach"),
                            rs.getInt("thoiHanBaoHanh"),
                            rs.getString("donViThoiGian"),
                            rs.getString("noiDungBaoHanh"),
                            rs.getString("dieuKienBaoHanh"),
                            rs.getString("truongHopTuChoi")));
                }
            }
        }
        return dsBaoHanh;
    }

    private void napPhuTungApDung(List<ChinhSachBaoHanh> dsChinhSach) throws SQLException {
        if (dsChinhSach.isEmpty()) {
            return;
        }
        java.util.Map<String, ChinhSachBaoHanh> chinhSachTheoMa = new java.util.HashMap<>();
        for (ChinhSachBaoHanh chinhSach : dsChinhSach) {
            chinhSach.setDsPhuTung(new ArrayList<>());
            chinhSachTheoMa.put(chinhSach.getMaCSBH(), chinhSach);
        }
        String sql = """
                SELECT csp.maCSBH, pt.maPhuTung, pt.tenPhuTung, pt.donViTinh,
                       pt.giaNhap, pt.giaBan, pt.soLuongTon, pt.nhaCungCap, pt.trangThai
                FROM ChinhSachBaoHanh_PhuTung csp
                JOIN PhuTung pt ON pt.maPhuTung = csp.maPhuTung
                ORDER BY csp.maCSBH, pt.maPhuTung
                """;
        try (Connection con = ketNoi();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChinhSachBaoHanh chinhSach = chinhSachTheoMa.get(rs.getString("maCSBH"));
                if (chinhSach != null) {
                    List<PhuTung> dsPhuTung = chinhSach.getDsPhuTung();
                    dsPhuTung.add(new PhuTung(
                            rs.getString("maPhuTung"),
                            rs.getString("tenPhuTung"),
                            rs.getString("donViTinh"),
                            rs.getDouble("giaNhap"),
                            rs.getDouble("giaBan"),
                            rs.getInt("soLuongTon"),
                            rs.getString("nhaCungCap"),
                            rs.getString("trangThai")));
                    chinhSach.setDsPhuTung(dsPhuTung);
                }
            }
        }
    }

    private ChinhSachBaoHanh taoChinhSach(ResultSet rs) throws SQLException {
        return new ChinhSachBaoHanh(
                rs.getString("maCSBH"),
                rs.getString("tenChinhSach"),
                rs.getInt("thoiHanBaoHanh"),
                rs.getString("donViThoiGian"),
                rs.getString("dieuKienBaoHanh"),
                rs.getString("truongHopTuChoi"),
                rs.getString("noiDungBaoHanh"),
                rs.getTimestamp("ngayTao"),
                rs.getBoolean("trangThai"),
                List.of());
    }

    private void themChinhSach(Connection con, ChinhSachBaoHanh chinhSach) throws SQLException {
        String sql = """
                INSERT INTO ChinhSachBaoHanh
                    (maCSBH, tenChinhSach, thoiHanBaoHanh, donViThoiGian,
                     dieuKienBaoHanh, truongHopTuChoi, noiDungBaoHanh, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, chinhSach.getMaCSBH());
            ganThamSo(ps, chinhSach, 2);
            ps.executeUpdate();
        }
    }

    private void capNhatChinhSach(Connection con, ChinhSachBaoHanh chinhSach) throws SQLException {
        String sql = """
                UPDATE ChinhSachBaoHanh
                SET tenChinhSach = ?, thoiHanBaoHanh = ?, donViThoiGian = ?,
                    dieuKienBaoHanh = ?, truongHopTuChoi = ?, noiDungBaoHanh = ?, trangThai = ?
                WHERE maCSBH = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ganThamSo(ps, chinhSach, 1);
            ps.setString(8, chinhSach.getMaCSBH());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Không tìm thấy chính sách cần cập nhật: " + chinhSach.getMaCSBH());
            }
        }
    }

    private void ganThamSo(PreparedStatement ps, ChinhSachBaoHanh chinhSach, int viTri)
            throws SQLException {
        int i = viTri;
        ps.setString(i++, chinhSach.getTenChinhSach().trim());
        ps.setInt(i++, chinhSach.getThoiHanBaoHanh());
        ps.setString(i++, chinhSach.getDonViThoiGian());
        ps.setString(i++, chinhSach.getDieuKienBaoHanh());
        ps.setString(i++, chinhSach.getTruongHopTuChoi());
        ps.setString(i++, chinhSach.getNoiDungBaoHanh());
        ps.setBoolean(i, chinhSach.isTrangThai());
    }

    private void kiemTraPhuTungDaCoChinhSach(
            Connection con, String maCSBH, Set<String> dsMaPhuTung) throws SQLException {
        String sql = """
                SELECT TOP (1) cs.tenChinhSach
                FROM ChinhSachBaoHanh_PhuTung csp WITH (UPDLOCK, HOLDLOCK)
                JOIN ChinhSachBaoHanh cs WITH (UPDLOCK, HOLDLOCK) ON cs.maCSBH = csp.maCSBH
                WHERE csp.maPhuTung = ? AND cs.trangThai = 1 AND cs.maCSBH <> ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String maPhuTung : dsMaPhuTung) {
                ps.setString(1, maPhuTung);
                ps.setString(2, maCSBH);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("Phụ tùng " + maPhuTung
                                + " đã thuộc chính sách đang áp dụng \""
                                + rs.getString("tenChinhSach") + "\".");
                    }
                }
            }
        }
    }

    private String taoMaMoi(Connection con) throws SQLException {
        return layMaTiepTheo(con, true);
    }

    private String layMaTiepTheo(Connection con, boolean khoa) throws SQLException {
        String sql = "SELECT maCSBH FROM ChinhSachBaoHanh"
                + (khoa ? " WITH (UPDLOCK, HOLDLOCK)" : "")
                + " WHERE maCSBH LIKE 'CSBH%'";
        int maLonNhat = 0;
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString(1);
                try {
                    maLonNhat = Math.max(maLonNhat, Integer.parseInt(ma.substring(4)));
                } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                    // Bỏ qua mã không theo mẫu CSBH + số.
                }
            }
        }
        if (maLonNhat >= 999) {
            throw new SQLException("Đã hết dải mã chính sách bảo hành tự động.");
        }
        return String.format("CSBH%03d", maLonNhat + 1);
    }

    private void kiemTraDuLieu(ChinhSachBaoHanh chinhSach) throws SQLException {
        if (chinhSach == null) {
            throw new SQLException("Thông tin chính sách bảo hành không được để trống.");
        }
        if (chinhSach.getTenChinhSach() == null || chinhSach.getTenChinhSach().isBlank()) {
            throw new SQLException("Tên chính sách không được để trống.");
        }
        if (chinhSach.getThoiHanBaoHanh() <= 0) {
            throw new SQLException("Thời hạn bảo hành phải lớn hơn 0.");
        }
        String donVi = chinhSach.getDonViThoiGian();
        if (!"NGAY".equals(donVi) && !"THANG".equals(donVi) && !"NAM".equals(donVi)) {
            throw new SQLException("Đơn vị thời gian bảo hành không hợp lệ.");
        }
        if (chinhSach.getTenChinhSach().length() > 150) {
            throw new SQLException("Tên chính sách không được vượt quá 150 ký tự.");
        }
        if (chinhSach.getMaCSBH() != null && chinhSach.getMaCSBH().length() > 10) {
            throw new SQLException("Mã chính sách không được vượt quá 10 ký tự.");
        }
        for (String noiDung : List.of(
                chinhSach.getDieuKienBaoHanh() == null ? "" : chinhSach.getDieuKienBaoHanh(),
                chinhSach.getTruongHopTuChoi() == null ? "" : chinhSach.getTruongHopTuChoi(),
                chinhSach.getNoiDungBaoHanh() == null ? "" : chinhSach.getNoiDungBaoHanh())) {
            if (noiDung.length() > 1000) {
                throw new SQLException("Nội dung chính sách không được vượt quá 1000 ký tự.");
            }
        }
    }

    private Connection ketNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

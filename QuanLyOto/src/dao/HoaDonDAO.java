package dao;

import connectDB.ConnectDB;
import dao.ChinhSachBaoHanhDAO.PhuTungBaoHanh;
import entity.ChuongTrinhKhuyenMai;
import entity.HoaDon;
import entity.NhanVien;
import entity.PhieuSuaChua;
import entity.PhieuTiepNhan;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import util.MaTuDong;

public class HoaDonDAO {
    // Hằng dùng chung cho màn hình lập hóa đơn
    public static final String SUA_CHUA = "Hóa đơn sửa chữa";
    public static final String MUA_PHU_TUNG = "Hóa đơn mua phụ tùng";
    public static final String DICH_VU = "Dịch vụ";
    public static final String PHU_TUNG = "Phụ tùng";
    public static final String CHUA_THANH_TOAN = "Chưa thanh toán";
    public static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final BigDecimal VAT = new BigDecimal("0.08");

    private final ChinhSachBaoHanhDAO chinhSachBaoHanhDAO = new ChinhSachBaoHanhDAO();

    public boolean insert(HoaDon hoaDon) throws SQLException {
        String sql = """
                INSERT INTO HoaDon (
                    maHoaDon, maPhieuSuaChua, maNV, maKM,
                    ngayLap, tienGiamGia, thueVAT, soTien, trangThai
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        if (hoaDon.getNhanVien() == null) {
            throw new SQLException("Hóa đơn phải có nhân viên lập.");
        }

        try (Connection con = conn();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hoaDon.getMaHoaDon());

            ps.setString(2, hoaDon.getPhieuSuaChua() == null
                    ? null
                    : hoaDon.getPhieuSuaChua().getMaPhieuSuaChua());

            ps.setString(3, hoaDon.getNhanVien().getMaNV());

            ps.setString(4, hoaDon.getChuongTrinhKhuyenMai() == null
                    ? null
                    : hoaDon.getChuongTrinhKhuyenMai().getMaKM());

            ps.setTimestamp(5, hoaDon.getNgayLap());
            ps.setBigDecimal(6, hoaDon.getTienGiamGia());
            ps.setBigDecimal(7, hoaDon.getThueVAT());
            ps.setBigDecimal(8, hoaDon.getSoTien());
            ps.setString(9, hoaDon.getTrangThai());

            return ps.executeUpdate() > 0;
        }
    }

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

    // MÃ HÓA ĐƠN: Sinh mã tiếp theo: HD001, HD002...
    public String taoMaHoaDonMoi() throws SQLException {
        return MaTuDong.sinhMa(
                MaTuDong.TIEN_TO_HOA_DON,
                findAll(),
                HoaDon::getMaHoaDon);
    }

    private HoaDon mapHoaDon(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();

        hd.setMaHoaDon(rs.getString("maHoaDon"));
        hd.setNgayLap(rs.getTimestamp("ngayLap"));
        hd.setTienGiamGia(rs.getBigDecimal("tienGiamGia"));
        hd.setThueVAT(rs.getBigDecimal("thueVAT"));
        hd.setSoTien(rs.getBigDecimal("soTien"));
        hd.setTrangThai(rs.getString("trangThai"));

        // Ánh xạ phiếu sửa chữa
        String maPSC = rs.getString("maPhieuSuaChua");
        if (maPSC != null) {
            PhieuSuaChua phieu = new PhieuSuaChua();
            phieu.setMaPhieuSuaChua(maPSC);
            hd.setPhieuSuaChua(phieu);
        }

        // Ánh xạ nhân viên
        String maNV = rs.getString("maNV");
        if (maNV != null) {
            NhanVien nv = new NhanVien();
            nv.setMaNV(maNV);
            hd.setNhanVien(nv);
        }

        // Ánh xạ chương trình khuyến mãi
        String maKM = rs.getString("maKM");
        if (maKM != null) {
            ChuongTrinhKhuyenMai km = new ChuongTrinhKhuyenMai();
            km.setMaKM(maKM);
            hd.setChuongTrinhKhuyenMai(km);
        }

        return hd;
    }

    public List<HoaDon> findAll() throws SQLException {
        String sql = """
                SELECT maHoaDon, maPhieuSuaChua, maNV, maKM,
                       ngayLap, tienGiamGia, thueVAT, soTien, trangThai
                FROM HoaDon
                ORDER BY ngayLap DESC
                """;

        List<HoaDon> ds = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ds.add(mapHoaDon(rs));
            }
        }

        return ds;
    }

    public HoaDon findById(Object... keyValues) throws SQLException {
        if (keyValues == null || keyValues.length != 1) {
            throw new SQLException("Hóa đơn cần một giá trị khóa chính.");
        }

        String sql = """
                SELECT maHoaDon, maPhieuSuaChua, maNV, maKM,
                       ngayLap, tienGiamGia, thueVAT, soTien, trangThai
                FROM HoaDon
                WHERE maHoaDon = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, keyValues[0]);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapHoaDon(rs) : null;
            }
        }
    }

    public List<HoaDon> search(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String sql = """
                SELECT maHoaDon, maPhieuSuaChua, maNV, maKM,
                       ngayLap, tienGiamGia, thueVAT, soTien, trangThai
                FROM HoaDon
                WHERE maHoaDon LIKE ?
                   OR maPhieuSuaChua LIKE ?
                   OR maNV LIKE ?
                   OR maKM LIKE ?
                   OR trangThai LIKE ?
                ORDER BY ngayLap DESC
                """;

        String tuKhoa = "%" + keyword.trim() + "%";
        List<HoaDon> ds = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, tuKhoa);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapHoaDon(rs));
                }
            }
        }

        return ds;
    }

    public List<HoaDon> findByField(String fieldName, Object value)
            throws SQLException {

        String column;

        switch (fieldName) {
            case "maHoaDon" -> column = "maHoaDon";
            case "phieuSuaChua" -> column = "maPhieuSuaChua";
            case "nhanVien" -> column = "maNV";
            case "chuongTrinhKhuyenMai" -> column = "maKM";
            case "ngayLap" -> column = "ngayLap";
            case "tienGiamGia" -> column = "tienGiamGia";
            case "thueVAT" -> column = "thueVAT";
            case "soTien" -> column = "soTien";
            case "trangThai" -> column = "trangThai";
            default -> throw new SQLException(
                    "Không tìm thấy thuộc tính " + fieldName + " trong HoaDon.");
        }

        // Nếu truyền đối tượng quan hệ thì lấy mã khóa ngoại
        if (value instanceof PhieuSuaChua p) {
            value = p.getMaPhieuSuaChua();
        } else if (value instanceof NhanVien nv) {
            value = nv.getMaNV();
        } else if (value instanceof ChuongTrinhKhuyenMai km) {
            value = km.getMaKM();
        }

        String sql = """
                SELECT maHoaDon, maPhieuSuaChua, maNV, maKM,
                       ngayLap, tienGiamGia, thueVAT, soTien, trangThai
                FROM HoaDon
                WHERE
                """ + column + (value == null ? " IS NULL" : " = ?");

        List<HoaDon> ds = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (value != null) {
                ps.setObject(1, value);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapHoaDon(rs));
                }
            }
        }

        return ds;
    }

    public boolean update(HoaDon hd) throws SQLException {
        String sql = """
                UPDATE HoaDon
                SET maPhieuSuaChua = ?,
                    maNV = ?,
                    maKM = ?,
                    ngayLap = ?,
                    tienGiamGia = ?,
                    thueVAT = ?,
                    soTien = ?,
                    trangThai = ?
                WHERE maHoaDon = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1,
                    hd.getPhieuSuaChua() == null
                            ? null
                            : hd.getPhieuSuaChua().getMaPhieuSuaChua());

            ps.setString(2,
                    hd.getNhanVien() == null
                            ? null
                            : hd.getNhanVien().getMaNV());

            ps.setString(3,
                    hd.getChuongTrinhKhuyenMai() == null
                            ? null
                            : hd.getChuongTrinhKhuyenMai().getMaKM());

            ps.setTimestamp(4, hd.getNgayLap());
            ps.setBigDecimal(5, hd.getTienGiamGia());
            ps.setBigDecimal(6, hd.getThueVAT());
            ps.setBigDecimal(7, hd.getSoTien());
            ps.setString(8, hd.getTrangThai());
            ps.setString(9, hd.getMaHoaDon());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Object... keyValues) throws SQLException {
        if (keyValues == null || keyValues.length != 1) {
            throw new SQLException("Hóa đơn cần một giá trị khóa chính.");
        }

        return xoaHoaDon(String.valueOf(keyValues[0]));
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
            if (maPSC != null && !maPSC.isBlank()) {
                String sqlTienDo = "UPDATE ptn SET tienDo = ? "
                        + "FROM PhieuTiepNhan ptn "
                        + "JOIN PhieuKiemTra pkt ON pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan "
                        + "JOIN PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra "
                        + "WHERE psc.maPhieuSuaChua = ?";
                try (PreparedStatement ps = c.prepareStatement(sqlTienDo)) {
                    ps.setString(1, PhieuTiepNhan.TIEN_DO_DA_LAP_HOA_DON);
                    ps.setString(2, maPSC);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Không cập nhật được tiến độ phiếu tiếp nhận.");
                    }
                }
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
            JOIN PhieuKiemTra pkt ON pkt.maPhieuKiemTra = p.maPhieuKiemTra
            JOIN PhieuTiepNhan ptn ON ptn.maPhieuTiepNhan = pkt.maPhieuTiepNhan
            """;

    /** Trả về null nếu không có phiếu. */
    public Phieu layPhieu(String maPhieu) throws SQLException {
        List<Phieu> ds = truyVanPhieu(
            "WHERE p.maPhieuSuaChua = ? AND p.trangThai = N'Đã sửa chữa' "
                + "AND ptn.tienDo = N'Đã lập phiếu sửa chữa'",
            maPhieu);
        return ds.isEmpty() ? null : ds.get(0);
    }

    /** Tìm theo mã phiếu, mã KH, mã xe, tên KH, SĐT hoặc biển số. */
    public List<Phieu> timPhieu(String tuKhoa) throws SQLException {
        String m = "%" + tuKhoa + "%";
        return truyVanPhieu("""
                     WHERE (p.maPhieuSuaChua LIKE ? OR p.maKH LIKE ? OR p.maXe LIKE ?
                         OR kh.hoTen LIKE ? OR kh.soDT LIKE ? OR x.bienSo LIKE ?)
                        AND p.trangThai = N'Đã sửa chữa'
                        AND ptn.tienDo = N'Đã lập phiếu sửa chữa'
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

    public record LichSuHoaDon(String ma, LocalDate ngayLap, String maNV,
            String maPhieu, BigDecimal tongThanhToan, String trangThai) {
    }

    // THÔNG TIN ĐẦY ĐỦ CỦA MỘT HÓA ĐƠN
    public record ThongTinHoaDon(
            String maHoaDon,
            LocalDate ngayLap,
            String maNV,
            String tenNV,
            String maPhieu,
            String maKH,
            String tenKH,
            String soDT,
            String maXe,
            String bienSo,
            String tenXe,
            String maKM,
            String tenKM,
            BigDecimal tileGiam,
            BigDecimal tienGiam,
            BigDecimal tienVAT,
            BigDecimal soTien,
            String trangThai) {
    }

    // LẤY THÔNG TIN CHUNG CỦA HÓA ĐƠN THEO MÃ
    public ThongTinHoaDon layThongTinHoaDon(String maHD) throws SQLException {
        String sql = """
                SELECT
                    hd.maHoaDon,
                    hd.ngayLap,
                    hd.maNV,
                    nv.hoTen AS tenNV,
                    hd.maPhieuSuaChua,
                    p.maKH,
                    kh.hoTen AS tenKH,
                    kh.soDT,
                    p.maXe,
                    x.bienSo,
                    x.hangXe,
                    x.dongXe,
                    hd.maKM,
                    km.tenKM,
                    km.tileGiam,
                    hd.tienGiamGia,
                    hd.thueVAT,
                    hd.soTien,
                    hd.trangThai
                FROM HoaDon hd
                LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV
                LEFT JOIN PhieuSuaChua p
                    ON hd.maPhieuSuaChua = p.maPhieuSuaChua
                LEFT JOIN KhachHang kh ON p.maKH = kh.maKH
                LEFT JOIN Xe x ON p.maXe = x.maXe
                LEFT JOIN ChuongTrinhKhuyenMai km ON hd.maKM = km.maKM
                WHERE hd.maHoaDon = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hangXe = rs.getString("hangXe");
                    String dongXe = rs.getString("dongXe");

                    String tenXe = ((hangXe == null ? "" : hangXe) + " "
                            + (dongXe == null ? "" : dongXe)).trim();

                    java.sql.Date ngay = rs.getDate("ngayLap");

                    return new ThongTinHoaDon(
                            rs.getString("maHoaDon"),
                            ngay == null ? null : ngay.toLocalDate(),
                            rs.getString("maNV"),
                            rs.getString("tenNV"),
                            rs.getString("maPhieuSuaChua"),
                            rs.getString("maKH"),
                            rs.getString("tenKH"),
                            rs.getString("soDT"),
                            rs.getString("maXe"),
                            rs.getString("bienSo"),
                            tenXe,
                            rs.getString("maKM"),
                            rs.getString("tenKM"),
                            khacNull(rs.getBigDecimal("tileGiam")),
                            khacNull(rs.getBigDecimal("tienGiamGia")),
                            khacNull(rs.getBigDecimal("thueVAT")),
                            khacNull(rs.getBigDecimal("soTien")),
                            rs.getString("trangThai"));
                }
            }
        }

        return null;
    }

    // LẤY CHI TIẾT DỊCH VỤ / PHỤ TÙNG CỦA HÓA ĐƠN
    public List<DongChiTiet> layChiTietHoaDon(String maHD) throws SQLException {
        String sql = """
                SELECT
                    ct.maDV,
                    dv.tenDV,
                    ct.maPhuTung,
                    pt.tenPhuTung,
                    ct.soLuong,
                    ct.donGia
                FROM ChiTietHoaDon ct
                LEFT JOIN DichVu dv ON ct.maDV = dv.maDV
                LEFT JOIN PhuTung pt ON ct.maPhuTung = pt.maPhuTung
                WHERE ct.maHoaDon = ?
                ORDER BY
                    CASE WHEN ct.maDV IS NULL THEN 1 ELSE 0 END
                """;

        List<DongChiTiet> ds = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, maHD);

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

    public List<LichSuHoaDon> layLichSuHoaDon() throws SQLException {
        String sql = """
                SELECT
                    hd.maHoaDon,
                    hd.ngayLap,
                    hd.maNV,
                    hd.maPhieuSuaChua,
                    hd.soTien,
                    CASE
                        WHEN ISNULL(gd.daThanhToan, 0) <= 0
                            THEN N'Chưa thanh toán'
                        WHEN ISNULL(gd.daThanhToan, 0) >= hd.soTien
                            THEN N'Đã thanh toán'
                        ELSE N'Thanh toán một phần'
                    END AS trangThai
                FROM HoaDon hd
                LEFT JOIN (
                    SELECT
                        maHoaDon,
                        SUM(soTienThanhToan) AS daThanhToan
                    FROM GiaoDichThanhToan
                    WHERE trangThaiGD = ?
                    GROUP BY maHoaDon
                ) gd ON hd.maHoaDon = gd.maHoaDon
                ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC
                """;

        List<LichSuHoaDon> ds = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, entity.GiaoDichThanhToan.TT_THANH_CONG);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date ngay = rs.getDate("ngayLap");

                    ds.add(new LichSuHoaDon(
                            rs.getString("maHoaDon"),
                            ngay == null ? null : ngay.toLocalDate(),
                            rs.getString("maNV"),
                            rs.getString("maPhieuSuaChua"),
                            rs.getBigDecimal("soTien"),
                            rs.getString("trangThai")));
                }
            }
        }

        return ds;
    }

    public record TongKet(BigDecimal dichVu, BigDecimal phuTung, BigDecimal tongHoaDon,
            BigDecimal tienVAT, BigDecimal tienGiam, BigDecimal tongThanhToan) {
        public static final TongKet RONG = new TongKet(BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public record DuLieuKhoiTao(String maHoaDon, List<PhuTung> dsPhuTung,
            List<KhuyenMai> dsKhuyenMai, List<LichSuHoaDon> dsLichSu) {
    }

    public record PhieuDayDu(Phieu phieu, List<DongChiTiet> dsChiTiet,
            List<PhuTungBaoHanh> dsBaoHanh) {
    }

    public record HoaDonDaLuu(ThongTinHoaDon hd, List<DongChiTiet> dsChiTiet,
            List<PhuTungBaoHanh> dsBaoHanh) {
    }

    // Dữ liệu cần để mở màn hình: mã mới, danh mục phụ tùng, khuyến mãi, lịch sử.
    public DuLieuKhoiTao taiDuLieuBanDau() throws SQLException {
        return new DuLieuKhoiTao(
                taoMaHoaDonMoi(),
                layDanhSachPhuTung(),
                layKhuyenMaiDangApDung(),
                layLichSuHoaDon());
    }

    // Phiếu sửa chữa kèm chi tiết và phụ tùng được bảo hành; null nếu không có
    // phiếu.
    public PhieuDayDu taiPhieu(String maPhieu) throws SQLException {
        Phieu p = layPhieu(maPhieu);
        if (p == null) {
            return null;
        }
        return new PhieuDayDu(p,
                layChiTietPhieu(maPhieu),
                chinhSachBaoHanhDAO.layPhuTungBaoHanhTheoPhieu(maPhieu));
    }

    // Hóa đơn đã lưu kèm chi tiết và bảo hành (nếu là hóa đơn sửa chữa).
    public HoaDonDaLuu taiHoaDon(String maHD) throws SQLException {
        ThongTinHoaDon hd = layThongTinHoaDon(maHD);
        if (hd == null) {
            throw new SQLException("Không tìm thấy hóa đơn " + maHD);
        }
        List<DongChiTiet> dsChiTiet = layChiTietHoaDon(maHD);
        List<PhuTungBaoHanh> dsBaoHanh = coPhieu(hd.maPhieu())
                ? chinhSachBaoHanhDAO.layPhuTungBaoHanhTheoPhieu(hd.maPhieu())
                : List.of();
        return new HoaDonDaLuu(hd, dsChiTiet, dsBaoHanh);
    }

    // Hóa đơn đang lập: VAT 8% trên tiền hàng, sau đó trừ khuyến mãi theo tỉ lệ.
    public TongKet tinhTong(List<DongChiTiet> dsChiTiet, KhuyenMai km) {
        BigDecimal dichVu = tongDichVu(dsChiTiet);
        BigDecimal phuTung = tongPhuTung(dsChiTiet);

        BigDecimal truocThue = dichVu.add(phuTung);
        BigDecimal tienVAT = truocThue.multiply(VAT).setScale(0, RoundingMode.HALF_UP);
        BigDecimal tamTinh = truocThue.add(tienVAT);

        BigDecimal tile = km == null ? BigDecimal.ZERO : km.tile();
        BigDecimal tienGiam = tamTinh.multiply(tile)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .min(tamTinh);
        return new TongKet(dichVu, phuTung, tamTinh, tienVAT, tienGiam, tamTinh.subtract(tienGiam));
    }

    // Hóa đơn đã lưu: dùng đúng số liệu đã lưu, không tính lại.
    public TongKet tongKetHoaDonCu(ThongTinHoaDon hd, List<DongChiTiet> dsChiTiet) {
        BigDecimal dichVu = tongDichVu(dsChiTiet);
        BigDecimal phuTung = tongPhuTung(dsChiTiet);
        return new TongKet(dichVu, phuTung, dichVu.add(phuTung).add(hd.tienVAT()),
                hd.tienVAT(), hd.tienGiam(), hd.soTien());
    }

    private BigDecimal tongDichVu(List<DongChiTiet> ds) {
        BigDecimal tong = BigDecimal.ZERO;
        for (DongChiTiet d : ds) {
            if (d.dichVu()) {
                tong = tong.add(d.thanhTien());
            }
        }
        return tong;
    }

    private BigDecimal tongPhuTung(List<DongChiTiet> ds) {
        BigDecimal tong = BigDecimal.ZERO;
        for (DongChiTiet d : ds) {
            if (!d.dichVu()) {
                tong = tong.add(d.thanhTien());
            }
        }
        return tong;
    }

    // Vị trí dòng phụ tùng có mã đã cho, -1 nếu chưa có.
    public int timDongPhuTung(List<DongChiTiet> ds, String maPhuTung) {
        for (int i = 0; i < ds.size(); i++) {
            DongChiTiet d = ds.get(i);
            if (!d.dichVu() && maPhuTung.equals(d.ma())) {
                return i;
            }
        }
        return -1;
    }

    // Đơn giá hợp lệ (số, không âm) hoặc null.
    public BigDecimal docDonGia(String text) {
        try {
            BigDecimal donGia = new BigDecimal(text.trim());
            return donGia.signum() < 0 ? null : donGia;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static boolean coPhieu(String maPhieu) {
        return maPhieu != null && !maPhieu.isBlank();
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
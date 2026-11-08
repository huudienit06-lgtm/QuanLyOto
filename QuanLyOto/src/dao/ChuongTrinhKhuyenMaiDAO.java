package dao;

import connectDB.ConnectDB;
import entity.ChuongTrinhKhuyenMai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.MaTuDong;

public class ChuongTrinhKhuyenMaiDAO {

    // Lấy danh sách tất cả chương trình khuyến mãi
    public List<ChuongTrinhKhuyenMai> getAll() throws SQLException {
        List<ChuongTrinhKhuyenMai> ds = new ArrayList<>();

        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();

        String sql = "SELECT * FROM ChuongTrinhKhuyenMai";

        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ChuongTrinhKhuyenMai km = new ChuongTrinhKhuyenMai();

                km.setMaKM(rs.getString("maKM"));
                km.setTenKM(rs.getString("tenKM"));
                km.setTileGiam(rs.getDouble("tileGiam"));
                km.setNgayBatDau(rs.getDate("ngayBatDau"));
                km.setNgayKetThuc(rs.getDate("ngayKetThuc"));
                km.setDieuKienApDung(rs.getString("dieuKienApDung"));
                km.setTrangThai(rs.getString("trangThai"));

                ds.add(km);
            }
        }

        return ds;
    }

    // Tìm chương trình khuyến mãi theo mã
    public ChuongTrinhKhuyenMai getById(String maKM) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();

        String sql = "SELECT * FROM ChuongTrinhKhuyenMai WHERE maKM = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKM);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChuongTrinhKhuyenMai km = new ChuongTrinhKhuyenMai();

                    km.setMaKM(rs.getString("maKM"));
                    km.setTenKM(rs.getString("tenKM"));
                    km.setTileGiam(rs.getDouble("tileGiam"));
                    km.setNgayBatDau(rs.getDate("ngayBatDau"));
                    km.setNgayKetThuc(rs.getDate("ngayKetThuc"));
                    km.setDieuKienApDung(rs.getString("dieuKienApDung"));
                    km.setTrangThai(rs.getString("trangThai"));

                    return km;
                }
            }
        }

        return null;
    }

    // Thêm chương trình khuyến mãi
    public boolean insert(ChuongTrinhKhuyenMai km) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();

        String sql = "INSERT INTO ChuongTrinhKhuyenMai "
                + "(maKM, tenKM, tileGiam, ngayBatDau, ngayKetThuc, dieuKienApDung, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, km.getMaKM());
            ps.setString(2, km.getTenKM());
            ps.setObject(3, km.getTileGiam());
            ps.setDate(4, km.getNgayBatDau());
            ps.setDate(5, km.getNgayKetThuc());
            ps.setString(6, km.getDieuKienApDung());
            ps.setString(7, km.getTrangThai());

            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật chương trình khuyến mãi
    public boolean update(ChuongTrinhKhuyenMai km) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();

        String sql = "UPDATE ChuongTrinhKhuyenMai SET "
                + "tenKM = ?, tileGiam = ?, ngayBatDau = ?, "
                + "ngayKetThuc = ?, dieuKienApDung = ?, trangThai = ? "
                + "WHERE maKM = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, km.getTenKM());
            ps.setObject(2, km.getTileGiam());
            ps.setDate(3, km.getNgayBatDau());
            ps.setDate(4, km.getNgayKetThuc());
            ps.setString(5, km.getDieuKienApDung());
            ps.setString(6, km.getTrangThai());
            ps.setString(7, km.getMaKM());

            return ps.executeUpdate() > 0;
        }
    }

    // Xóa chương trình khuyến mãi theo mã
    public boolean delete(String maKM) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();

        String sql = "DELETE FROM ChuongTrinhKhuyenMai WHERE maKM = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKM);

            return ps.executeUpdate() > 0;
        }
    }

    // ================= XỬ LÝ NGHIỆP VỤ CHO MÀN HÌNH QUẢN LÝ KHUYẾN MÃI
    // =================
    // (không dùng Swing; KhuyenMaiController gọi, FrmKhuyenMai chỉ hiển thị)

    // Trạng thái (cột trangThai NVARCHAR(20))
    public static final String TT_DANG = "Đang áp dụng";
    public static final String TT_CHUA = "Chưa áp dụng";
    public static final String TT_HET = "Hết hạn";
    public static final String TT_TAM = "Tạm ngưng";
    public static final String TT_TAT_CA = "Tất cả"; // chỉ dùng cho bộ lọc

    // Tên ô nhập bị lỗi, để giao diện đưa con trỏ về đúng ô
    public static final String TRUONG_TEN = "ten";
    public static final String TRUONG_TILE = "tile";

    private static final String SQL_TAI = """
            SELECT maKM, tenKM, tileGiam, ngayBatDau, ngayKetThuc, dieuKienApDung, trangThai
            FROM ChuongTrinhKhuyenMai
            ORDER BY ngayBatDau DESC
            """;

    // Một dòng của bảng ChuongTrinhKhuyenMai (dùng BigDecimal/LocalDate cho dễ xử
    // lý)
    public static class KhuyenMai {
        public String maKM;
        public String tenKM;
        public BigDecimal tileGiam;
        public LocalDate ngayBatDau;
        public LocalDate ngayKetThuc;
        public String dieuKienApDung;
        public String trangThai;

        public KhuyenMai(String ma, String ten, BigDecimal tile, LocalDate bd, LocalDate kt,
                String dk, String tt) {
            this.maKM = ma;
            this.tenKM = ten;
            this.tileGiam = tile;
            this.ngayBatDau = bd;
            this.ngayKetThuc = kt;
            this.dieuKienApDung = dk;
            this.trangThai = tt;
        }
    }

    // Điều kiện lọc; trường nào null/rỗng thì không lọc theo trường đó.
    public record TieuChiLoc(String ma, String ten, String trangThai,
            BigDecimal tileTu, BigDecimal tileDen, LocalDate tuNgay, LocalDate denNgay) {
    }

    // Dữ liệu nhập sai; truong cho biết ô cần đưa con trỏ về (có thể null).
    public static class LoiNhapLieu extends Exception {
        private final String truong;

        public LoiNhapLieu(String message, String truong) {
            super(message);
            this.truong = truong;
        }

        public String truong() {
            return truong;
        }
    }

    // TRUY VẤN
    public List<KhuyenMai> layDanhSachKhuyenMai() throws SQLException {
        List<KhuyenMai> ds = new ArrayList<>();

        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();

        try (PreparedStatement ps = con.prepareStatement(SQL_TAI);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new KhuyenMai(
                        rs.getString("maKM"),
                        rs.getString("tenKM"),
                        rs.getBigDecimal("tileGiam"),
                        rs.getDate("ngayBatDau").toLocalDate(),
                        rs.getDate("ngayKetThuc").toLocalDate(),
                        rs.getString("dieuKienApDung"),
                        rs.getString("trangThai")));
            }
        }
        return ds;
    }

    public boolean them(KhuyenMai km) throws SQLException {
        return insert(toEntity(km));
    }

    public boolean capNhat(KhuyenMai km) throws SQLException {
        return update(toEntity(km));
    }

    public boolean xoa(String maKM) throws SQLException {
        return delete(maKM);
    }

    private static ChuongTrinhKhuyenMai toEntity(KhuyenMai k) {
        ChuongTrinhKhuyenMai km = new ChuongTrinhKhuyenMai();
        km.setMaKM(k.maKM);
        km.setTenKM(k.tenKM);
        km.setTileGiam(k.tileGiam.doubleValue());
        km.setNgayBatDau(Date.valueOf(k.ngayBatDau));
        km.setNgayKetThuc(Date.valueOf(k.ngayKetThuc));
        km.setDieuKienApDung(k.dieuKienApDung == null || k.dieuKienApDung.isEmpty() ? null : k.dieuKienApDung);
        km.setTrangThai(k.trangThai);
        return km;
    }

    // TRẠNG THÁI THEO NGÀY
    public static String tinhTrangThai(LocalDate bd, LocalDate kt) {
        LocalDate homNay = LocalDate.now();
        if (homNay.isBefore(bd))
            return TT_CHUA;
        if (homNay.isAfter(kt))
            return TT_HET;
        return TT_DANG;
    }

    // Tự chuyển trạng thái theo ngày hiện tại (khuyến mãi "Tạm ngưng" giữ nguyên)
    public void capNhatTrangThaiTheoNgay(List<KhuyenMai> ds) {
        for (KhuyenMai km : ds) {
            if (!TT_TAM.equals(km.trangThai)) {
                km.trangThai = tinhTrangThai(km.ngayBatDau, km.ngayKetThuc);
            }
        }
    }

    // TÌM KIẾM / MÃ MỚI
    public List<KhuyenMai> loc(List<KhuyenMai> ds, TieuChiLoc tc) {
        String ma = tc.ma() == null ? "" : tc.ma().trim().toLowerCase();
        String ten = tc.ten() == null ? "" : tc.ten().trim().toLowerCase();

        List<KhuyenMai> kq = new ArrayList<>();
        for (KhuyenMai km : ds) {
            if (!km.maKM.toLowerCase().contains(ma) || !km.tenKM.toLowerCase().contains(ten))
                continue;
            if (tc.trangThai() != null && !TT_TAT_CA.equals(tc.trangThai())
                    && !km.trangThai.equals(tc.trangThai()))
                continue;
            if (tc.tileTu() != null && km.tileGiam.compareTo(tc.tileTu()) < 0)
                continue;
            if (tc.tileDen() != null && km.tileGiam.compareTo(tc.tileDen()) > 0)
                continue;
            // Khuyến mãi có hiệu lực giao với khoảng ngày đang lọc
            if (tc.tuNgay() != null && km.ngayKetThuc.isBefore(tc.tuNgay()))
                continue;
            if (tc.denNgay() != null && km.ngayBatDau.isAfter(tc.denNgay()))
                continue;
            kq.add(km);
        }
        return kq;
    }

    public KhuyenMai timTheoMa(List<KhuyenMai> ds, String ma) {
        for (KhuyenMai km : ds) {
            if (km.maKM.equalsIgnoreCase(ma))
                return km;
        }
        return null;
    }

    // Sinh mã dạng KM001, KM002... theo số lớn nhất hiện có
    public String taoMaMoi(List<KhuyenMai> ds) {
        return MaTuDong.sinhMa(
                MaTuDong.TIEN_TO_KHUYEN_MAI,
                ds,
                km -> km.maKM);
    }

    // Đọc số từ chuỗi (chấp nhận dấu phẩy); rỗng -> null; sai ->
    // NumberFormatException.
    public static BigDecimal docSo(String text) {
        String s = text == null ? "" : text.trim();
        return s.isEmpty() ? null : new BigDecimal(s.replace(',', '.'));
    }

    // KIỂM TRA DỮ LIỆU NHẬP
    // Trả về khuyến mãi hợp lệ hoặc ném LoiNhapLieu.
    public KhuyenMai kiemTra(String maNhap, String tenNhap, String tileNhap,
            LocalDate bd, LocalDate kt, boolean tamNgung, String dieuKien,
            boolean themMoi, List<KhuyenMai> dsHienCo) throws LoiNhapLieu {
        String ma = maNhap.trim();
        String ten = tenNhap.trim();
        String tileStr = tileNhap.trim().replace(',', '.');

        // Mã và tên
        if (ma.isEmpty())
            throw new LoiNhapLieu("Vui lòng nhập mã khuyến mãi.", null);
        if (ma.length() > 20 || !ma.matches("[A-Za-z0-9_-]+"))
            throw new LoiNhapLieu("Mã khuyến mãi tối đa 20 ký tự, chỉ gồm chữ không dấu, số, '-' hoặc '_'.", null);
        if (themMoi && timTheoMa(dsHienCo, ma) != null)
            throw new LoiNhapLieu("Mã khuyến mãi \"" + ma + "\" đã tồn tại.", null);
        if (ten.isEmpty())
            throw new LoiNhapLieu("Vui lòng nhập tên khuyến mãi.", TRUONG_TEN);
        if (ten.length() > 100)
            throw new LoiNhapLieu("Tên khuyến mãi tối đa 100 ký tự.", null);

        // Tỉ lệ giảm
        BigDecimal tile;
        try {
            tile = new BigDecimal(tileStr);
        } catch (NumberFormatException ex) {
            throw new LoiNhapLieu("Tỉ lệ giảm phải là số (ví dụ 10 hoặc 7.5).", TRUONG_TILE);
        }
        if (tile.signum() <= 0 || tile.compareTo(new BigDecimal("100")) > 0)
            throw new LoiNhapLieu("Tỉ lệ giảm phải lớn hơn 0 và không vượt quá 100%.", TRUONG_TILE);
        if (tile.stripTrailingZeros().scale() > 2)
            throw new LoiNhapLieu("Tỉ lệ giảm chỉ được tối đa 2 chữ số thập phân.", TRUONG_TILE);

        // Ngày áp dụng
        if (bd == null || kt == null)
            throw new LoiNhapLieu("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.", null);
        if (kt.isBefore(bd))
            throw new LoiNhapLieu("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.", null);

        String trangThai = tamNgung ? TT_TAM : tinhTrangThai(bd, kt);
        return new KhuyenMai(ma, ten, tile.setScale(2, RoundingMode.HALF_UP), bd, kt,
                dieuKien == null ? "" : dieuKien.trim(), trangThai);
    }
}
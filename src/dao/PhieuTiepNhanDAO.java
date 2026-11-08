package dao;

import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuTiepNhan;
import entity.Xe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PhieuTiepNhanDAO {

    private static final String BASE = "SELECT t.maPhieuTiepNhan,t.maXe,t.maKH,t.maNV,t.ngayLap,t.trangThai,t.ghiChu,"
            + "x.bienSo, k.hoTen AS tenKH, n.hoTen AS tenNV FROM PhieuTiepNhan t "
            + "LEFT JOIN Xe x ON x.maXe=t.maXe LEFT JOIN KhachHang k ON k.maKH=t.maKH "
            + "LEFT JOIN NhanVien n ON n.maNV=t.maNV ";

    private PhieuTiepNhan map(ResultSet rs) throws SQLException {
        PhieuTiepNhan p = new PhieuTiepNhan();
        p.setMaPhieuTiepNhan(rs.getString("maPhieuTiepNhan"));
        p.setMaXe(rs.getString("maXe"));
        p.setMaKH(rs.getString("maKH"));
        p.setMaNV(rs.getString("maNV"));
        p.setNgayLap(rs.getTimestamp("ngayLap"));
        p.setTrangThai(rs.getString("trangThai"));
        p.setGhiChu(rs.getString("ghiChu"));
        p.setBienSo(rs.getString("bienSo"));
        p.setTenKH(rs.getString("tenKH"));
        p.setTenNV(rs.getString("tenNV"));
        return p;
    }

    public List<PhieuTiepNhan> getAll() throws SQLException {
        return timKiem("", "");
    }

    public List<PhieuTiepNhan> timKiem(String kw, String trangThai) throws SQLException {
        String sql = BASE + "WHERE (t.maPhieuTiepNhan LIKE ? OR ISNULL(x.bienSo,'') LIKE ? OR ISNULL(k.hoTen,'') LIKE ?)"
                + (trangThai.isEmpty() ? "" : " AND t.trangThai = ?") + " ORDER BY t.ngayLap DESC, t.maPhieuTiepNhan DESC";
        List<PhieuTiepNhan> ds = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            String k = DbHelper.like(kw);
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            if (!trangThai.isEmpty()) ps.setNString(4, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(map(rs));
            }
        }
        return ds;
    }

    public PhieuTiepNhan getById(String ma) throws SQLException {
        try (PreparedStatement ps = DbHelper.get().prepareStatement(BASE + "WHERE t.maPhieuTiepNhan = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Các phiếu tiếp nhận chưa có phiếu kiểm tra. */
    public List<PhieuTiepNhan> getChuaKiemTra() throws SQLException {
        String sql = BASE + "WHERE NOT EXISTS (SELECT 1 FROM PhieuKiemTra c WHERE c.maPhieuTiepNhan = t.maPhieuTiepNhan) "
                + "ORDER BY t.ngayLap DESC, t.maPhieuTiepNhan DESC";
        List<PhieuTiepNhan> ds = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(map(rs));
        }
        return ds;
    }

    /** Danh sách nhân viên {maNV, hoTen} để chọn người lập phiếu. */
    public List<String[]> getNhanVien() throws SQLException {
        List<String[]> ds = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement("SELECT maNV, hoTen FROM NhanVien ORDER BY maNV");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(new String[]{rs.getString(1), rs.getString(2)});
        }
        return ds;
    }

    /** @return mã phiếu vừa tạo */
    public String insert(PhieuTiepNhan p) throws SQLException {
        String ma = DbHelper.nextId("PhieuTiepNhan", "maPhieuTiepNhan", "PTN");
        String sql = "INSERT INTO PhieuTiepNhan(maPhieuTiepNhan,maXe,maKH,maNV,ngayLap,trangThai,ghiChu) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setString(2, p.getMaXe());
            ps.setString(3, p.getMaKH());
            ps.setString(4, p.getMaNV());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.setNString(6, p.getTrangThai());
            ps.setNString(7, p.getGhiChu());
            ps.executeUpdate();
        }
        return ma;
    }

    public boolean updateTrangThai(String ma, String trangThai) throws SQLException {
        try (PreparedStatement ps = DbHelper.get().prepareStatement(
                "UPDATE PhieuTiepNhan SET trangThai=? WHERE maPhieuTiepNhan=?")) {
            ps.setNString(1, trangThai);
            ps.setString(2, ma);
            return ps.executeUpdate() > 0;
        }
    }

    /** Ném SQLException (23000) nếu phiếu đã có phiếu kiểm tra. */
    public boolean delete(String ma) throws SQLException {
        try (PreparedStatement ps = DbHelper.get().prepareStatement("DELETE FROM PhieuTiepNhan WHERE maPhieuTiepNhan=?")) {
            ps.setString(1, ma);
            return ps.executeUpdate() > 0;
        }
public class PhieuTiepNhanDAO {
    public List<PhieuTiepNhan> findAll() throws SQLException {
        String sql = """
                SELECT ptn.maPhieuTiepNhan, ptn.ngayLap, ptn.trangThai, ptn.ghiChu,
                       xe.maXe AS xe_maXe, xe.bienSo, xe.hangXe, xe.dongXe,
                       xe.namSanXuat, xe.soKhung, xe.soMay, xe.maKH AS xe_maKH,
                       kh.maKH AS kh_maKH, kh.hoTen AS kh_hoTen, kh.soDT AS kh_soDT,
                       kh.email AS kh_email, kh.diaChi AS kh_diaChi, kh.congNo AS kh_congNo,
                       nv.maNV AS nv_maNV, nv.hoTen AS nv_hoTen, nv.chucVu, nv.soDT AS nv_soDT,
                       nv.email AS nv_email, nv.trangThai AS nv_trangThai
                FROM PhieuTiepNhan ptn
                JOIN Xe xe ON xe.maXe = ptn.maXe
                JOIN KhachHang kh ON kh.maKH = ptn.maKH
                JOIN NhanVien nv ON nv.maNV = ptn.maNV
                ORDER BY ptn.ngayLap DESC
                """;
        List<PhieuTiepNhan> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public boolean insert(PhieuTiepNhan phieu) throws SQLException {
        String sql = "INSERT INTO PhieuTiepNhan "
                + "(maPhieuTiepNhan, maXe, maKH, maNV, ngayLap, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(PhieuTiepNhan phieu) throws SQLException {
        String sql = "UPDATE PhieuTiepNhan SET maXe = ?, maKH = ?, maNV = ?, ngayLap = ?, "
                + "trangThai = ?, ghiChu = ? WHERE maPhieuTiepNhan = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, layMaXe(phieu));
            statement.setString(2, layMaKhachHang(phieu));
            statement.setString(3, layMaNhanVien(phieu));
            statement.setTimestamp(4, phieu.getNgayLap());
            statement.setString(5, phieu.getTrangThai());
            statement.setString(6, phieu.getGhiChu());
            statement.setString(7, phieu.getMaPhieuTiepNhan());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maPhieuTiepNhan) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM PhieuTiepNhan WHERE maPhieuTiepNhan = ?")) {
            statement.setString(1, maPhieuTiepNhan);
            return statement.executeUpdate() > 0;
        }
    }

    private PhieuTiepNhan mapRow(ResultSet resultSet) throws SQLException {
        return new PhieuTiepNhan(
                resultSet.getString("maPhieuTiepNhan"),
                new Xe(
                        resultSet.getString("xe_maXe"),
                        resultSet.getString("bienSo"),
                        resultSet.getString("hangXe"),
                        resultSet.getString("dongXe"),
                        resultSet.getObject("namSanXuat") == null ? null : resultSet.getInt("namSanXuat"),
                        resultSet.getString("soKhung"),
                        resultSet.getString("soMay"),
                        resultSet.getString("xe_maKH")),
                new KhachHang(
                        resultSet.getString("kh_maKH"),
                        resultSet.getString("kh_hoTen"),
                        resultSet.getString("kh_soDT"),
                        resultSet.getString("kh_email"),
                        resultSet.getString("kh_diaChi"),
                        resultSet.getDouble("kh_congNo")),
                new NhanVien(
                        resultSet.getString("nv_maNV"),
                        resultSet.getString("nv_hoTen"),
                        resultSet.getString("chucVu"),
                        resultSet.getString("nv_soDT"),
                        resultSet.getString("nv_email"),
                        resultSet.getString("nv_trangThai")),
                resultSet.getTimestamp("ngayLap"),
                resultSet.getString("trangThai"),
                resultSet.getString("ghiChu"));
    }

    private void ganThamSo(PreparedStatement statement, PhieuTiepNhan phieu) throws SQLException {
        statement.setString(1, phieu.getMaPhieuTiepNhan());
        statement.setString(2, layMaXe(phieu));
        statement.setString(3, layMaKhachHang(phieu));
        statement.setString(4, layMaNhanVien(phieu));
        statement.setTimestamp(5, phieu.getNgayLap());
        statement.setString(6, phieu.getTrangThai());
        statement.setString(7, phieu.getGhiChu());
    }

    private String layMaXe(PhieuTiepNhan phieu) throws SQLException {
        if (phieu.getXe() == null || phieu.getXe().getMaXe() == null) {
            throw new SQLException("Phiếu tiếp nhận phải có xe.");
        }
        return phieu.getXe().getMaXe();
    }

    private String layMaKhachHang(PhieuTiepNhan phieu) throws SQLException {
        if (phieu.getKhachHang() == null || phieu.getKhachHang().getMaKH() == null) {
            throw new SQLException("Phiếu tiếp nhận phải có khách hàng.");
        }
        return phieu.getKhachHang().getMaKH();
    }

    private String layMaNhanVien(PhieuTiepNhan phieu) throws SQLException {
        if (phieu.getNhanVien() == null || phieu.getNhanVien().getMaNV() == null) {
            throw new SQLException("Phiếu tiếp nhận phải có nhân viên lập phiếu.");
        }
        return phieu.getNhanVien().getMaNV();
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

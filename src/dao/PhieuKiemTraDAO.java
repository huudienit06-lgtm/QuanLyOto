package dao;

import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuKiemTra;
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
import java.util.ArrayList;
import java.util.List;

public class PhieuKiemTraDAO {

    private static final String BASE = "SELECT c.maPhieuKiemTra,c.maPhieuTiepNhan,c.maKH,c.maXe,c.maNV,"
            + "c.trangThaiBanDau,c.vanDeHuHong, x.bienSo, k.hoTen AS tenKH, n.hoTen AS tenNV FROM PhieuKiemTra c "
            + "LEFT JOIN Xe x ON x.maXe=c.maXe LEFT JOIN KhachHang k ON k.maKH=c.maKH "
            + "LEFT JOIN NhanVien n ON n.maNV=c.maNV ";

    private PhieuKiemTra map(ResultSet rs) throws SQLException {
        PhieuKiemTra p = new PhieuKiemTra();
        p.setMaPhieuKiemTra(rs.getString("maPhieuKiemTra"));
        p.setMaPhieuTiepNhan(rs.getString("maPhieuTiepNhan"));
        p.setMaKH(rs.getString("maKH"));
        p.setMaXe(rs.getString("maXe"));
        p.setMaNV(rs.getString("maNV"));
        p.setTrangThaiBanDau(rs.getString("trangThaiBanDau"));
        p.setVanDeHuHong(rs.getString("vanDeHuHong"));
        p.setBienSo(rs.getString("bienSo"));
        p.setTenKH(rs.getString("tenKH"));
        p.setTenNV(rs.getString("tenNV"));
        return p;
    }

    public List<PhieuKiemTra> timKiem(String kw) throws SQLException {
        String sql = BASE + "WHERE c.maPhieuKiemTra LIKE ? OR c.maPhieuTiepNhan LIKE ? OR ISNULL(x.bienSo,'') LIKE ? "
                + "OR ISNULL(k.hoTen,'') LIKE ? ORDER BY c.maPhieuKiemTra DESC";
        List<PhieuKiemTra> ds = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            String k = DbHelper.like(kw);
            for (int i = 1; i <= 4; i++) ps.setString(i, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(map(rs));
            }
        }
        return ds;
    }

    public List<PhieuKiemTra> getAll() throws SQLException {
        return timKiem("");
    }

    /** maKH, maXe được lấy từ phiếu tiếp nhận tương ứng. @return mã phiếu vừa tạo */
    public String insert(PhieuKiemTra p) throws SQLException {
        String ma = DbHelper.nextId("PhieuKiemTra", "maPhieuKiemTra", "PKT");
        String sql = "INSERT INTO PhieuKiemTra(maPhieuKiemTra,maPhieuTiepNhan,maKH,maXe,maNV,trangThaiBanDau,vanDeHuHong) "
                + "VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setString(2, p.getMaPhieuTiepNhan());
            ps.setString(3, p.getMaKH());
            ps.setString(4, p.getMaXe());
            ps.setString(5, p.getMaNV());
            ps.setNString(6, p.getTrangThaiBanDau());
            ps.setNString(7, p.getVanDeHuHong());
            ps.executeUpdate();
        }
        return ma;
    }

    public boolean update(PhieuKiemTra p) throws SQLException {
        String sql = "UPDATE PhieuKiemTra SET maNV=?, trangThaiBanDau=?, vanDeHuHong=? WHERE maPhieuKiemTra=?";
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, p.getMaNV());
            ps.setNString(2, p.getTrangThaiBanDau());
            ps.setNString(3, p.getVanDeHuHong());
            ps.setString(4, p.getMaPhieuKiemTra());
            return ps.executeUpdate() > 0;
        }
    }

    /** Ném SQLException (23000) nếu phiếu đã có phiếu sửa chữa. */
    public boolean delete(String ma) throws SQLException {
        try (PreparedStatement ps = DbHelper.get().prepareStatement("DELETE FROM PhieuKiemTra WHERE maPhieuKiemTra=?")) {
            ps.setString(1, ma);
            return ps.executeUpdate() > 0;
        }
public class PhieuKiemTraDAO {
    public List<PhieuKiemTra> findAll() throws SQLException {
        String sql = "SELECT maPhieuKiemTra, maPhieuTiepNhan, maKH, maXe, maNV, "
                + "trangThaiBanDau, vanDeHuHong FROM PhieuKiemTra ORDER BY maPhieuKiemTra";
        List<PhieuKiemTra> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public boolean insert(PhieuKiemTra phieu) throws SQLException {
        String sql = "INSERT INTO PhieuKiemTra "
                + "(maPhieuKiemTra, maPhieuTiepNhan, maKH, maXe, maNV, trangThaiBanDau, vanDeHuHong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(PhieuKiemTra phieu) throws SQLException {
        String sql = "UPDATE PhieuKiemTra SET maPhieuTiepNhan = ?, maKH = ?, maXe = ?, maNV = ?, "
                + "trangThaiBanDau = ?, vanDeHuHong = ? WHERE maPhieuKiemTra = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phieu.getPhieuTiepNhan() == null
                    ? null : phieu.getPhieuTiepNhan().getMaPhieuTiepNhan());
            statement.setString(2, phieu.getKhachHang() == null ? null : phieu.getKhachHang().getMaKH());
            statement.setString(3, phieu.getXe() == null ? null : phieu.getXe().getMaXe());
            statement.setString(4, phieu.getNhanVien() == null ? null : phieu.getNhanVien().getMaNV());
            statement.setString(5, phieu.getTrangThaiBanDau());
            statement.setString(6, phieu.getVanDeHuHong());
            statement.setString(7, phieu.getMaPhieuKiemTra());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maPhieuKiemTra) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM PhieuKiemTra WHERE maPhieuKiemTra = ?")) {
            statement.setString(1, maPhieuKiemTra);
            return statement.executeUpdate() > 0;
        }
    }

    private PhieuKiemTra mapRow(ResultSet resultSet) throws SQLException {
        return new PhieuKiemTra(
                resultSet.getString("maPhieuKiemTra"),
                new PhieuTiepNhan(resultSet.getString("maPhieuTiepNhan")),
                new KhachHang(resultSet.getString("maKH")),
                new Xe(resultSet.getString("maXe")),
                new NhanVien(resultSet.getString("maNV")),
                resultSet.getString("trangThaiBanDau"),
                resultSet.getString("vanDeHuHong"));
    }

    private void ganThamSo(PreparedStatement statement, PhieuKiemTra phieu) throws SQLException {
        statement.setString(1, phieu.getMaPhieuKiemTra());
        statement.setString(2, phieu.getPhieuTiepNhan() == null
                ? null : phieu.getPhieuTiepNhan().getMaPhieuTiepNhan());
        statement.setString(3, phieu.getKhachHang() == null ? null : phieu.getKhachHang().getMaKH());
        statement.setString(4, phieu.getXe() == null ? null : phieu.getXe().getMaXe());
        statement.setString(5, phieu.getNhanVien() == null ? null : phieu.getNhanVien().getMaNV());
        statement.setString(6, phieu.getTrangThaiBanDau());
        statement.setString(7, phieu.getVanDeHuHong());
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

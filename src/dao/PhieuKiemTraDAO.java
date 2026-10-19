package dao;

import entity.PhieuKiemTra;

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
    }
}

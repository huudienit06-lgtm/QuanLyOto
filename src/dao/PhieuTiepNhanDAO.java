package dao;

import entity.PhieuTiepNhan;

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
    }
}

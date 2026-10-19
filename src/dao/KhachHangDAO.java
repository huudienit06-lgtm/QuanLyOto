package dao;

import entity.KhachHang;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    private KhachHang map(ResultSet rs) throws SQLException {
        return new KhachHang(rs.getString("maKH"), rs.getString("hoTen"), rs.getString("soDT"),
                rs.getString("email"), rs.getString("diaChi"), rs.getDouble("congNo"), rs.getString("trangThai"));
    }

    public List<KhachHang> getAll() throws SQLException {
        return timKeyword("", "");
    }

    /** Tìm nhanh theo mã / tên / SĐT, lọc theo trạng thái ("" = tất cả). */
    public List<KhachHang> timKeyword(String kw, String trangThai) throws SQLException {
        String sql = "SELECT * FROM KhachHang WHERE (maKH LIKE ? OR hoTen LIKE ? OR soDT LIKE ?)"
                + (trangThai.isEmpty() ? "" : " AND trangThai = ?") + " ORDER BY maKH";
        List<KhachHang> ds = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            String k = DbHelper.like(kw);
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            if (!trangThai.isEmpty()) ps.setString(4, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(map(rs));
            }
        }
        return ds;
    }

    public KhachHang getById(String ma) throws SQLException {
        try (PreparedStatement ps = DbHelper.get().prepareStatement("SELECT * FROM KhachHang WHERE maKH = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean existsSoDT(String soDT, String excludeMaKH) throws SQLException {
        String sql = "SELECT COUNT(*) FROM KhachHang WHERE soDT = ? AND maKH <> ?";
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, soDT);
            ps.setString(2, excludeMaKH == null ? "" : excludeMaKH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean insert(KhachHang k) throws SQLException {
        k.setMaKH(DbHelper.nextId("KhachHang", "maKH", "KH"));
        String sql = "INSERT INTO KhachHang(maKH,hoTen,soDT,email,diaChi,congNo,trangThai) VALUES(?,?,?,?,?,0,?)";
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, k.getMaKH());
            ps.setNString(2, k.getHoTen());
            ps.setString(3, k.getSoDT());
            ps.setString(4, k.getEmail());
            ps.setNString(5, k.getDiaChi());
            ps.setNString(6, k.getTrangThai());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(KhachHang k) throws SQLException {
        String sql = "UPDATE KhachHang SET hoTen=?, soDT=?, email=?, diaChi=?, trangThai=? WHERE maKH=?";
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setNString(1, k.getHoTen());
            ps.setString(2, k.getSoDT());
            ps.setString(3, k.getEmail());
            ps.setNString(4, k.getDiaChi());
            ps.setNString(5, k.getTrangThai());
            ps.setString(6, k.getMaKH());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @return 1 = đã xóa hẳn; 2 = khách đã có xe/phiếu liên quan nên chuyển sang "Ngừng hoạt động".
     */
    public int delete(String ma) throws SQLException {
        try (PreparedStatement ps = DbHelper.get().prepareStatement("DELETE FROM KhachHang WHERE maKH=?")) {
            ps.setString(1, ma);
            ps.executeUpdate();
            return 1;
        } catch (SQLException e) {
            if (!"23000".equals(e.getSQLState())) throw e; // 23000 = vi phạm khóa ngoại
        }
        try (PreparedStatement ps = DbHelper.get().prepareStatement(
                "UPDATE KhachHang SET trangThai = N'Ngừng hoạt động' WHERE maKH=?")) {
            ps.setString(1, ma);
            ps.executeUpdate();
            return 2;
        }
    }

    /** Tìm kiếm chuyên sâu. Mỗi dòng: maKH, hoTen, soDT, email, soXe, trangThai. */
    public List<Object[]> timKiem(String ma, String ten, String sdt, String email, String diaChi) throws SQLException {
        String sql = "SELECT k.maKH,k.hoTen,k.soDT,k.email,"
                + "(SELECT COUNT(*) FROM Xe x WHERE x.maKH=k.maKH) AS soXe,k.trangThai "
                + "FROM KhachHang k WHERE k.maKH LIKE ? AND k.hoTen LIKE ? AND k.soDT LIKE ? "
                + "AND ISNULL(k.email,'') LIKE ? AND ISNULL(k.diaChi,'') LIKE ? ORDER BY k.maKH";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, DbHelper.like(ma));
            ps.setString(2, DbHelper.like(ten));
            ps.setString(3, DbHelper.like(sdt));
            ps.setString(4, DbHelper.like(email));
            ps.setString(5, DbHelper.like(diaChi));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4) == null ? "" : rs.getString(4), rs.getInt(5), rs.getString(6)});
            }
        }
        return rows;
    }

    /** Thống kê khách hàng. Mỗi dòng: maKH, hoTen, soDT, soXe, soLanTiepNhan, congNo(Double), trangThai. */
    public List<Object[]> thongKe(String kw, String trangThai, boolean chiCongNo) throws SQLException {
        String sql = "SELECT k.maKH,k.hoTen,k.soDT,"
                + "(SELECT COUNT(*) FROM Xe x WHERE x.maKH=k.maKH),"
                + "(SELECT COUNT(*) FROM PhieuTiepNhan t WHERE t.maKH=k.maKH),"
                + "ISNULL(k.congNo,0),k.trangThai FROM KhachHang k "
                + "WHERE (k.maKH LIKE ? OR k.hoTen LIKE ? OR k.soDT LIKE ?)"
                + (trangThai.isEmpty() ? "" : " AND k.trangThai = ?")
                + (chiCongNo ? " AND ISNULL(k.congNo,0) > 0" : "") + " ORDER BY k.maKH";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            String k = DbHelper.like(kw);
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            if (!trangThai.isEmpty()) ps.setString(4, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getInt(4), rs.getInt(5), rs.getDouble(6), rs.getString(7)});
            }
        }
        return rows;
    }

    /**
     * Lịch sử sửa chữa theo khách hàng và/hoặc xe ("" = bỏ qua điều kiện).
     * Mỗi dòng: maPhieuSuaChua, bienSo, tenKH, ngayBatDau(Timestamp), ngayHoanThanhDuKien(Timestamp), tongTienCong(Double).
     */
    public List<Object[]> lichSuSuaChua(String maKH, String maXe) throws SQLException {
        String sql = "SELECT s.maPhieuSuaChua, x.bienSo, k.hoTen, s.ngayBatDau, s.ngayHoanThanhDuKien, "
                + "ISNULL((SELECT SUM(c.tienCong) FROM ChiTietPhieuSuaChua c WHERE c.maPhieuSuaChua=s.maPhieuSuaChua),0) "
                + "FROM PhieuSuaChua s JOIN Xe x ON x.maXe=s.maXe JOIN KhachHang k ON k.maKH=s.maKH "
                + "WHERE (?='' OR s.maKH=?) AND (?='' OR s.maXe=?) ORDER BY s.ngayBatDau DESC, s.maPhieuSuaChua DESC";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, maKH);
            ps.setString(2, maKH);
            ps.setString(3, maXe);
            ps.setString(4, maXe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getTimestamp(4), rs.getTimestamp(5), rs.getDouble(6)});
            }
        }
        return rows;
    }
}

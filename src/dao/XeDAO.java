package dao;

import entity.Xe;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class XeDAO {

    /** Trạng thái = trạng thái của phiếu tiếp nhận gần nhất (nếu chưa từng tiếp nhận -> "Chưa tiếp nhận"). */
    private static final String LATEST_TN = "(SELECT TOP 1 {col} FROM PhieuTiepNhan t WHERE t.maXe=x.maXe "
            + "ORDER BY t.ngayLap DESC, t.maPhieuTiepNhan DESC)";
    private static final String LATEST_STATUS = LATEST_TN.replace("{col}", "t.trangThai");
    private static final String LATEST_DATE = LATEST_TN.replace("{col}", "CONVERT(varchar(10), t.ngayLap, 103)");

    private static final String BASE = "SELECT x.maXe,x.bienSo,x.hangXe,x.dongXe,x.namSanXuat,x.soKhung,x.soMay,x.maKH,"
            + "k.hoTen AS tenChuXe, ISNULL(" + LATEST_STATUS + ", N'Chưa tiếp nhận') AS trangThai, "
            + LATEST_DATE + " AS ngayNhan "
            + "FROM Xe x LEFT JOIN KhachHang k ON k.maKH = x.maKH ";

    private Xe map(ResultSet rs) throws SQLException {
        Xe x = new Xe(rs.getString("maXe"), rs.getString("bienSo"), rs.getString("hangXe"), rs.getString("dongXe"),
                rs.getInt("namSanXuat"), rs.getString("soKhung"), rs.getString("soMay"), rs.getString("maKH"));
        x.setTenChuXe(rs.getString("tenChuXe"));
        x.setTrangThai(rs.getString("trangThai"));
        x.setNgayNhan(rs.getString("ngayNhan"));
        return x;
    }

    private List<Xe> query(String sql, List<Object> params) throws SQLException {
        List<Xe> ds = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(map(rs));
            }
        }
        return ds;
    }

    public List<Xe> getAll() throws SQLException {
        return query(BASE + " ORDER BY x.maXe", new ArrayList<>());
    }

    public List<Xe> timKeyword(String kw) throws SQLException {
        String k = DbHelper.like(kw);
        return query(BASE + " WHERE x.maXe LIKE ? OR x.bienSo LIKE ? OR ISNULL(x.hangXe,'') LIKE ? "
                + "OR ISNULL(x.dongXe,'') LIKE ? OR ISNULL(k.hoTen,'') LIKE ? ORDER BY x.maXe", List.of(k, k, k, k, k));
    }

    public List<Xe> getByKhachHang(String maKH) throws SQLException {
        return query(BASE + " WHERE x.maKH = ? ORDER BY x.maXe", List.of(maKH));
    }

    public Xe getById(String maXe) throws SQLException {
        List<Xe> ds = query(BASE + " WHERE x.maXe = ?", List.of(maXe));
        return ds.isEmpty() ? null : ds.get(0);
    }

    public Xe getByBienSo(String bienSo) throws SQLException {
        List<Xe> ds = query(BASE + " WHERE x.bienSo = ?", List.of(bienSo));
        return ds.isEmpty() ? null : ds.get(0);
    }

    /** Tìm kiếm chuyên sâu. trangThai = "" là tất cả; ngay = null là bỏ qua. */
    public List<Xe> timKiem(String bienSo, String maXe, String hang, String dong, String tenKH,
                            String trangThai, java.sql.Date ngay) throws SQLException {
        StringBuilder sb = new StringBuilder(BASE).append(" WHERE x.bienSo LIKE ? AND x.maXe LIKE ? ")
                .append("AND ISNULL(x.hangXe,'') LIKE ? AND ISNULL(x.dongXe,'') LIKE ? AND ISNULL(k.hoTen,'') LIKE ?");
        List<Object> p = new ArrayList<>(List.of(DbHelper.like(bienSo), DbHelper.like(maXe), DbHelper.like(hang),
                DbHelper.like(dong), DbHelper.like(tenKH)));
        if (!trangThai.isEmpty()) {
            sb.append(" AND ISNULL(").append(LATEST_STATUS).append(", N'Chưa tiếp nhận') = ?");
            p.add(trangThai);
        }
        if (ngay != null) {
            sb.append(" AND EXISTS (SELECT 1 FROM PhieuTiepNhan t WHERE t.maXe=x.maXe AND CAST(t.ngayLap AS date) = ?)");
            p.add(ngay);
        }
        sb.append(" ORDER BY x.maXe");
        return query(sb.toString(), p);
    }

    public List<String> getHang() throws SQLException {
        List<String> ds = new ArrayList<>();
        try (PreparedStatement ps = DbHelper.get().prepareStatement(
                "SELECT DISTINCT hangXe FROM Xe WHERE hangXe IS NOT NULL ORDER BY hangXe");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(rs.getString(1));
        }
        return ds;
    }

    public boolean existsBienSo(String bienSo, String excludeMaXe) throws SQLException {
        try (PreparedStatement ps = DbHelper.get().prepareStatement(
                "SELECT COUNT(*) FROM Xe WHERE bienSo = ? AND maXe <> ?")) {
            ps.setString(1, bienSo);
            ps.setString(2, excludeMaXe == null ? "" : excludeMaXe);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean insert(Xe x) throws SQLException {
        x.setMaXe(DbHelper.nextId("Xe", "maXe", "XE"));
        String sql = "INSERT INTO Xe(maXe,bienSo,hangXe,dongXe,namSanXuat,soKhung,soMay,maKH) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, x.getMaXe());
            ps.setString(2, x.getBienSo());
            ps.setNString(3, x.getHangXe());
            ps.setNString(4, x.getDongXe());
            ps.setInt(5, x.getNamSanXuat());
            ps.setString(6, x.getSoKhung());
            ps.setString(7, x.getSoMay());
            ps.setString(8, x.getMaKH());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Xe x) throws SQLException {
        String sql = "UPDATE Xe SET bienSo=?, hangXe=?, dongXe=?, namSanXuat=?, soKhung=?, soMay=?, maKH=? WHERE maXe=?";
        try (PreparedStatement ps = DbHelper.get().prepareStatement(sql)) {
            ps.setString(1, x.getBienSo());
            ps.setNString(2, x.getHangXe());
            ps.setNString(3, x.getDongXe());
            ps.setInt(4, x.getNamSanXuat());
            ps.setString(5, x.getSoKhung());
            ps.setString(6, x.getSoMay());
            ps.setString(7, x.getMaKH());
            ps.setString(8, x.getMaXe());
            return ps.executeUpdate() > 0;
        }
    }

    /** Ném SQLException (SQLState 23000) nếu xe đã có phiếu tiếp nhận/sửa chữa. */
    public boolean delete(String maXe) throws SQLException {
        try (PreparedStatement ps = DbHelper.get().prepareStatement("DELETE FROM Xe WHERE maXe=?")) {
            ps.setString(1, maXe);
            return ps.executeUpdate() > 0;
        }
    }
}

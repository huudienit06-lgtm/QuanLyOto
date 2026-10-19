package dao;

import connectDB.ConnectDB;
import entity.LichLamViec;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LichLamViecDAO extends BaseDAO<LichLamViec> {
    public LichLamViecDAO() {
        super(LichLamViec.class, "LichLamViec", "maLich");
    }

    public List<PhanCong> layPhanCong(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String sql = "SELECT ll.maLich, ll.maCa, ll.maNV, nv.hoTen, ll.ngayLamViec, ll.trangThai "
                + "FROM LichLamViec ll JOIN NhanVien nv ON nv.maNV = ll.maNV "
                + "WHERE ll.ngayLamViec >= ? AND ll.ngayLamViec < ? "
                + "ORDER BY ll.ngayLamViec, ll.maCa, nv.hoTen";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(tuNgay));
            statement.setDate(2, Date.valueOf(denNgay));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<PhanCong> danhSach = new ArrayList<>();
                while (resultSet.next()) {
                    danhSach.add(new PhanCong(
                            resultSet.getString("maLich"),
                            resultSet.getString("maCa"),
                            resultSet.getString("maNV"),
                            resultSet.getString("hoTen"),
                            resultSet.getDate("ngayLamViec").toLocalDate(),
                            resultSet.getString("trangThai")));
                }
                return danhSach;
            }
        }
    }

    public boolean themPhanCong(String maLich, String maCa, String maNV, LocalDate ngayLamViec)
            throws SQLException {
        String sql = "INSERT INTO LichLamViec (maLich, maCa, maNV, ngayLamViec, trangThai) "
                + "VALUES (?, ?, ?, ?, N'Đã phân công')";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maLich);
            statement.setString(2, maCa);
            statement.setString(3, maNV);
            statement.setDate(4, Date.valueOf(ngayLamViec));
            return statement.executeUpdate() > 0;
        }
    }

    public boolean capNhatPhanCong(String maLich, String maCa, String maNV, LocalDate ngayLamViec)
            throws SQLException {
        String sql = "UPDATE LichLamViec SET maCa = ?, maNV = ?, ngayLamViec = ? WHERE maLich = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maCa);
            statement.setString(2, maNV);
            statement.setDate(3, Date.valueOf(ngayLamViec));
            statement.setString(4, maLich);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean xoaPhanCong(String maLich) throws SQLException {
        String sql = "DELETE FROM LichLamViec WHERE maLich = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maLich);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean capNhatTrangThai(String maLich, boolean coMat) throws SQLException {
        String sql = "UPDATE LichLamViec SET trangThai = ? WHERE maLich = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, coMat ? "Có mặt" : "Vắng mặt");
            statement.setString(2, maLich);
            return statement.executeUpdate() > 0;
        }
    }

    private Connection moKetNoi() throws SQLException {
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }
        return connection;
    }

    public record PhanCong(String maLich, String maCa, String maNV, String hoTen,
            LocalDate ngayLamViec, String trangThai) {
    }
}

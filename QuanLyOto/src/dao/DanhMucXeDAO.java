package dao;

import connectDB.ConnectDB;
import java.sql.*;
import java.util.*;

public class DanhMucXeDAO {
    private Connection moKetNoi() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }
        return connection;
    }

    public Map<String, Set<String>> findAll() throws SQLException {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        String sql = "SELECT h.tenHangXe, d.tenDongXe FROM HangXe h LEFT JOIN DongXe d "
                + "ON d.tenHangXe = h.tenHangXe ORDER BY h.tenHangXe, d.tenDongXe";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Set<String> models = result.computeIfAbsent(rs.getString(1), key -> new LinkedHashSet<>());
                if (rs.getString(2) != null) {
                    models.add(rs.getString(2));
                }
            }
        }
        return result;
    }

    public void themHangXe(String ten) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO HangXe (tenHangXe) VALUES (?)")) {
            statement.setNString(1, ten);
            statement.executeUpdate();
        }
    }

    public void themDongXe(String hang, String dong) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO DongXe (tenHangXe, tenDongXe) VALUES (?, ?)")) {
            statement.setNString(1, hang);
            statement.setNString(2, dong);
            statement.executeUpdate();
        }
    }
}

package dao;

import connectDB.ConnectDB;
import entity.DongXe;
import entity.HangXe;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DanhMucXeDAO {
    private Connection moKetNoi() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }
        return connection;
    }

    public List<HangXe> findAllHangXe() throws SQLException {
        List<HangXe> danhSach = new ArrayList<>();
        String sql = "SELECT tenHangXe FROM HangXe ORDER BY tenHangXe";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(new HangXe(resultSet.getString("tenHangXe")));
            }
        }
        return danhSach;
    }

    public List<DongXe> findDongXeByHang(String tenHangXe) throws SQLException {
        List<DongXe> danhSach = new ArrayList<>();
        String sql = "SELECT tenHangXe, tenDongXe FROM DongXe "
                + "WHERE tenHangXe = ? ORDER BY tenDongXe";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setNString(1, tenHangXe);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new DongXe(
                            resultSet.getString("tenHangXe"),
                            resultSet.getString("tenDongXe")));
                }
            }
        }
        return danhSach;
    }

    public Map<String, Set<String>> findAll() throws SQLException {
        Map<String, Set<String>> danhMuc = new LinkedHashMap<>();
        String sql = "SELECT h.tenHangXe, d.tenDongXe FROM HangXe h "
                + "LEFT JOIN DongXe d ON d.tenHangXe = h.tenHangXe "
                + "ORDER BY h.tenHangXe, d.tenDongXe";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Set<String> cacDongXe = danhMuc.computeIfAbsent(
                        resultSet.getString("tenHangXe"), key -> new LinkedHashSet<>());
                String tenDongXe = resultSet.getString("tenDongXe");
                if (tenDongXe != null) {
                    cacDongXe.add(tenDongXe);
                }
            }
        }
        return danhMuc;
    }

    public void themHangXe(HangXe hangXe) throws SQLException {
        String sql = "INSERT INTO HangXe (tenHangXe) VALUES (?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setNString(1, hangXe.getTenHangXe());
            statement.executeUpdate();
        }
    }

    public boolean capNhatHangXe(String tenHangXeCu, HangXe hangXe) throws SQLException {
        String sql = "UPDATE HangXe SET tenHangXe = ? WHERE tenHangXe = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setNString(1, hangXe.getTenHangXe());
            statement.setNString(2, tenHangXeCu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean xoaHangXe(String tenHangXe) throws SQLException {
        String sql = "DELETE FROM HangXe WHERE tenHangXe = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setNString(1, tenHangXe);
            return statement.executeUpdate() > 0;
        }
    }

    public void themDongXe(DongXe dongXe) throws SQLException {
        String sql = "INSERT INTO DongXe (tenHangXe, tenDongXe) VALUES (?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setNString(1, dongXe.getTenHangXe());
            statement.setNString(2, dongXe.getTenDongXe());
            statement.executeUpdate();
        }
    }

    public boolean capNhatDongXe(String tenDongXeCu, DongXe dongXe) throws SQLException {
        String sql = "UPDATE DongXe SET tenDongXe = ? "
                + "WHERE tenHangXe = ? AND tenDongXe = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setNString(1, dongXe.getTenDongXe());
            statement.setNString(2, dongXe.getTenHangXe());
            statement.setNString(3, tenDongXeCu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean xoaDongXe(String tenHangXe, String tenDongXe) throws SQLException {
        String sql = "DELETE FROM DongXe WHERE tenHangXe = ? AND tenDongXe = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setNString(1, tenHangXe);
            statement.setNString(2, tenDongXe);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean coDongXe(String tenHangXe) throws SQLException {
        return tonTai("SELECT 1 FROM DongXe WHERE tenHangXe = ?", tenHangXe, null);
    }

    public boolean coXe(String tenHangXe, String tenDongXe) throws SQLException {
        return tonTai("SELECT 1 FROM Xe WHERE hangXe = ? AND dongXe = ?", tenHangXe, tenDongXe);
    }

    private boolean tonTai(String sql, String giaTriThuNhat, String giaTriThuHai)
            throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setNString(1, giaTriThuNhat);
            if (giaTriThuHai != null) {
                statement.setNString(2, giaTriThuHai);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}

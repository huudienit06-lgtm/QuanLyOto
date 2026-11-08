package dao;

import entity.ChucVu;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChucVuDAO {
    public List<ChucVu> findAll() throws SQLException {
        String sql = "SELECT maChucVu, tenChucVu FROM ChucVu ORDER BY tenChucVu";
        List<ChucVu> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public ChucVu findById(String maChucVu) throws SQLException {
        String sql = "SELECT maChucVu, tenChucVu FROM ChucVu WHERE maChucVu = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maChucVu);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public boolean insert(ChucVu chucVu) throws SQLException {
        String sql = "INSERT INTO ChucVu (maChucVu, tenChucVu) VALUES (?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chucVu.getMaChucVu());
            statement.setString(2, chucVu.getTenChucVu());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(ChucVu chucVu) throws SQLException {
        String sql = "UPDATE ChucVu SET tenChucVu = ? WHERE maChucVu = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chucVu.getTenChucVu());
            statement.setString(2, chucVu.getMaChucVu());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maChucVu) throws SQLException {
        String sql = "DELETE FROM ChucVu WHERE maChucVu = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maChucVu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean daDuocSuDung(String maChucVu) throws SQLException {
        String sql = "SELECT 1 FROM NhanVien WHERE maChucVu = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maChucVu);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private ChucVu mapRow(ResultSet resultSet) throws SQLException {
        return new ChucVu(resultSet.getString("maChucVu"), resultSet.getString("tenChucVu"));
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}
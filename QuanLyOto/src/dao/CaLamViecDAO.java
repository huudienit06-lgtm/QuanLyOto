package dao;

import entity.CaLamViec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CaLamViecDAO {
    public List<CaLamViec> findAll() throws SQLException {
        String sql = "SELECT maCa, gioBatDau, gioKetThuc FROM CaLamViec";
        List<CaLamViec> danhSachCa = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSachCa.add(new CaLamViec(
                        resultSet.getString("maCa"),
                        resultSet.getTime("gioBatDau"),
                        resultSet.getTime("gioKetThuc")));
            }
        }
        danhSachCa.sort(Comparator.comparing(CaLamViec::getGioBatDau,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return danhSachCa;
    }

    public CaLamViec findById(String maCa) throws SQLException {
        String sql = "SELECT maCa, gioBatDau, gioKetThuc FROM CaLamViec WHERE maCa = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maCa);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new CaLamViec(
                            resultSet.getString("maCa"),
                            resultSet.getTime("gioBatDau"),
                            resultSet.getTime("gioKetThuc"));
                }
            }
        }
        return null;
    }

    public boolean insert(CaLamViec caLamViec) throws SQLException {
        String sql = "INSERT INTO CaLamViec (maCa, gioBatDau, gioKetThuc) VALUES (?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, caLamViec.getMaCa());
            statement.setTime(2, caLamViec.getGioBatDau());
            statement.setTime(3, caLamViec.getGioKetThuc());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(CaLamViec caLamViec) throws SQLException {
        String sql = "UPDATE CaLamViec SET gioBatDau = ?, gioKetThuc = ? WHERE maCa = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTime(1, caLamViec.getGioBatDau());
            statement.setTime(2, caLamViec.getGioKetThuc());
            statement.setString(3, caLamViec.getMaCa());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maCa) throws SQLException {
        String sql = "DELETE FROM CaLamViec WHERE maCa = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maCa);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean daDuocPhanCong(String maCa) throws SQLException {
        String sql = "SELECT 1 FROM LichLamViec WHERE maCa = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maCa);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

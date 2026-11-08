package dao;

import entity.KhachHang;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {
    public List<KhachHang> findAll() throws SQLException {
        String sql = "SELECT maKH, hoTen, soDT, email, diaChi, congNo FROM KhachHang ORDER BY maKH";
        List<KhachHang> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public KhachHang findById(String maKH) throws SQLException {
        String sql = "SELECT maKH, hoTen, soDT, email, diaChi, congNo FROM KhachHang WHERE maKH = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKH);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public boolean insert(KhachHang khachHang) throws SQLException {
        String sql = "INSERT INTO KhachHang (maKH, hoTen, soDT, email, diaChi, congNo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, khachHang);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(KhachHang khachHang) throws SQLException {
        String sql = "UPDATE KhachHang SET hoTen = ?, soDT = ?, email = ?, diaChi = ?, congNo = ? WHERE maKH = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, khachHang.getHoTen());
            statement.setString(2, khachHang.getSoDT());
            statement.setString(3, khachHang.getEmail());
            statement.setString(4, khachHang.getDiaChi());
            statement.setObject(5, khachHang.getCongNo());
            statement.setString(6, khachHang.getMaKH());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maKH) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM KhachHang WHERE maKH = ?")) {
            statement.setString(1, maKH);
            return statement.executeUpdate() > 0;
        }
    }

    private KhachHang mapRow(ResultSet resultSet) throws SQLException {
        return new KhachHang(
                resultSet.getString("maKH"),
                resultSet.getString("hoTen"),
                resultSet.getString("soDT"),
                resultSet.getString("email"),
                resultSet.getString("diaChi"),
                resultSet.getDouble("congNo"));
    }

    private void ganThamSo(PreparedStatement statement, KhachHang khachHang) throws SQLException {
        int index = 1;
        statement.setString(index++, khachHang.getMaKH());
        statement.setString(index++, khachHang.getHoTen());
        statement.setString(index++, khachHang.getSoDT());
        statement.setString(index++, khachHang.getEmail());
        statement.setString(index++, khachHang.getDiaChi());
        statement.setObject(index, khachHang.getCongNo());
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

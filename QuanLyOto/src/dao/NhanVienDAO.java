package dao;

import entity.ChucVu;
import entity.NhanVien;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {
    public List<NhanVien> findAll() throws SQLException {
        String sql = "SELECT nv.maNV, nv.hoTen, cv.maChucVu, cv.tenChucVu, nv.soDT, nv.email, nv.trangThai "
            + "FROM NhanVien nv JOIN ChucVu cv ON cv.maChucVu = nv.maChucVu ORDER BY nv.maNV";
        List<NhanVien> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public NhanVien findById(String maNV) throws SQLException {
        String sql = "SELECT nv.maNV, nv.hoTen, cv.maChucVu, cv.tenChucVu, nv.soDT, nv.email, nv.trangThai "
            + "FROM NhanVien nv JOIN ChucVu cv ON cv.maChucVu = nv.maChucVu WHERE nv.maNV = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maNV);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public boolean insert(NhanVien nhanVien) throws SQLException {
        String sql = "INSERT INTO NhanVien (maNV, hoTen, maChucVu, soDT, email, trangThai) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nhanVien.getMaNV());
            statement.setString(2, nhanVien.getHoTen());
            statement.setString(3, nhanVien.getChucVu().getMaChucVu());
            statement.setString(4, nhanVien.getSoDT());
            statement.setString(5, nhanVien.getEmail());
            statement.setString(6, nhanVien.getTrangThai());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(NhanVien nhanVien) throws SQLException {
        String sql = "UPDATE NhanVien SET hoTen = ?, maChucVu = ?, soDT = ?, email = ?, trangThai = ? "
            + "WHERE maNV = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nhanVien.getHoTen());
            statement.setString(2, nhanVien.getChucVu().getMaChucVu());
            statement.setString(3, nhanVien.getSoDT());
            statement.setString(4, nhanVien.getEmail());
            statement.setString(5, nhanVien.getTrangThai());
            statement.setString(6, nhanVien.getMaNV());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maNV) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM NhanVien WHERE maNV = ?")) {
            statement.setString(1, maNV);
            return statement.executeUpdate() > 0;
        }
    }

    private NhanVien mapRow(ResultSet resultSet) throws SQLException {
        return new NhanVien(
                resultSet.getString("maNV"),
                resultSet.getString("hoTen"),
                new ChucVu(resultSet.getString("maChucVu"), resultSet.getString("tenChucVu")),
                resultSet.getString("soDT"),
                resultSet.getString("email"),
                resultSet.getString("trangThai"));
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

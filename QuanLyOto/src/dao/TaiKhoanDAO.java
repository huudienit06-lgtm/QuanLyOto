package dao;

import entity.NhanVien;
import entity.TaiKhoan;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {
    public List<TaiKhoan> findAll() throws SQLException {
        String sql = "SELECT tk.maNV, tk.tenDangNhap, tk.matKhau, tk.vaiTro, tk.trangThai, nv.hoTen "
                + "FROM TaiKhoan tk JOIN NhanVien nv ON nv.maNV = tk.maNV ORDER BY tk.maNV";
        List<TaiKhoan> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public TaiKhoan findById(String maNV) throws SQLException {
        String sql = "SELECT tk.maNV, tk.tenDangNhap, tk.matKhau, tk.vaiTro, tk.trangThai, nv.hoTen "
                + "FROM TaiKhoan tk JOIN NhanVien nv ON nv.maNV = tk.maNV WHERE tk.maNV = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maNV);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public boolean insert(TaiKhoan taiKhoan) throws SQLException {
        String sql = "INSERT INTO TaiKhoan (maNV, tenDangNhap, matKhau, vaiTro, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maNV(taiKhoan));
            statement.setString(2, taiKhoan.getTenDangNhap());
            statement.setString(3, taiKhoan.getMatKhau());
            statement.setString(4, taiKhoan.getVaiTro());
            statement.setString(5, taiKhoan.getTrangThai());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(TaiKhoan taiKhoan) throws SQLException {
        String sql = "UPDATE TaiKhoan SET tenDangNhap = ?, matKhau = ?, vaiTro = ?, trangThai = ? WHERE maNV = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, taiKhoan.getTenDangNhap());
            statement.setString(2, taiKhoan.getMatKhau());
            statement.setString(3, taiKhoan.getVaiTro());
            statement.setString(4, taiKhoan.getTrangThai());
            statement.setString(5, maNV(taiKhoan));
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maNV) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM TaiKhoan WHERE maNV = ?")) {
            statement.setString(1, maNV);
            return statement.executeUpdate() > 0;
        }
    }

    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT tk.maNV, tk.tenDangNhap, tk.vaiTro, tk.trangThai, nv.hoTen "
                + "FROM TaiKhoan tk JOIN NhanVien nv ON nv.maNV = tk.maNV "
                + "WHERE tk.tenDangNhap = ? AND tk.matKhau = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tenDangNhap);
            statement.setString(2, matKhau);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new TaiKhoan(
                        new NhanVien(resultSet.getString("maNV"), resultSet.getString("hoTen"),
                                null, null, null, null),
                        resultSet.getString("tenDangNhap"),
                        null,
                        resultSet.getString("vaiTro"),
                        resultSet.getString("trangThai"),
                        resultSet.getString("hoTen"));
            }
        }
    }

    private TaiKhoan mapRow(ResultSet resultSet) throws SQLException {
        return new TaiKhoan(
                new NhanVien(resultSet.getString("maNV"), resultSet.getString("hoTen"),
                        null, null, null, null),
                resultSet.getString("tenDangNhap"),
                resultSet.getString("matKhau"),
                resultSet.getString("vaiTro"),
                resultSet.getString("trangThai"),
                null);
    }

    private String maNV(TaiKhoan taiKhoan) {
        return taiKhoan.getNhanVien() == null ? null : taiKhoan.getNhanVien().getMaNV();
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

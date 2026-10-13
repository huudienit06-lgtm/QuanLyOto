package dao;

import connectDB.ConnectDB;
import entity.TaiKhoan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO extends BaseDAO<TaiKhoan> {
    public TaiKhoanDAO() {
        super(TaiKhoan.class, "TaiKhoan", "maNV");
    }

    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT tk.maNV, tk.tenDangNhap, tk.vaiTro, tk.trangThai, nv.hoTen "
                + "FROM TaiKhoan tk JOIN NhanVien nv ON nv.maNV = tk.maNV "
                + "WHERE tk.tenDangNhap = ? AND tk.matKhau = ?";
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu để xác thực tài khoản.");
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tenDangNhap);
            statement.setString(2, matKhau);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                TaiKhoan taiKhoan = new TaiKhoan();
                taiKhoan.setMaNV(resultSet.getString("maNV"));
                taiKhoan.setTenDangNhap(resultSet.getString("tenDangNhap"));
                taiKhoan.setVaiTro(resultSet.getString("vaiTro"));
                taiKhoan.setTrangThai(resultSet.getString("trangThai"));
                taiKhoan.setHoTenNhanVien(resultSet.getString("hoTen"));
                return taiKhoan;
            }
        }
    }
}

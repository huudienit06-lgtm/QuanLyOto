package dao;

import connectDB.ConnectDB;
import entity.ChiTietPhieuSuaChua;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuSuaChuaDAO {
    private static final String COT_DU_LIEU = "maChiTiet, maPhieuSuaChua, maPhuTung, maDV, soLuong, donGia";

    public List<ChiTietPhieuSuaChua> findAll() throws SQLException {
        String sql = "SELECT " + COT_DU_LIEU + " FROM ChiTietPhieuSuaChua ORDER BY maChiTiet";
        List<ChiTietPhieuSuaChua> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(anhXa(resultSet));
            }
        }
        return danhSach;
    }

    public List<ChiTietPhieuSuaChua> findByField(String fieldName, Object value) throws SQLException {
        if (!"maPhieuSuaChua".equals(fieldName)) {
            throw new SQLException("Không hỗ trợ tìm chi tiết theo thuộc tính: " + fieldName);
        }
        if (value == null || value.toString().isBlank()) {
            throw new SQLException("Mã phiếu sửa chữa không được để trống.");
        }
        String sql = "SELECT " + COT_DU_LIEU
                + " FROM ChiTietPhieuSuaChua WHERE maPhieuSuaChua = ? ORDER BY maChiTiet";
        List<ChiTietPhieuSuaChua> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value.toString().trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(anhXa(resultSet));
                }
            }
        }
        return danhSach;
    }

    public boolean insert(ChiTietPhieuSuaChua chiTiet) throws SQLException {
        kiemTraDuLieu(chiTiet);
        String sql = "INSERT INTO ChiTietPhieuSuaChua "
                + "(maPhieuSuaChua, maPhuTung, maDV, soLuong, donGia) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chiTiet.getMaPhieuSuaChua().trim());
            statement.setString(2, chiTiet.getMaPhuTung());
            statement.setString(3, chiTiet.getMaDV());
            statement.setInt(4, chiTiet.getSoLuong());
            statement.setBigDecimal(5, chiTiet.getDonGia());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Long maChiTiet) throws SQLException {
        if (maChiTiet == null || maChiTiet <= 0) {
            throw new SQLException("Mã chi tiết không hợp lệ.");
        }
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM ChiTietPhieuSuaChua WHERE maChiTiet = ?")) {
            statement.setLong(1, maChiTiet);
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

    private ChiTietPhieuSuaChua anhXa(ResultSet resultSet) throws SQLException {
        ChiTietPhieuSuaChua chiTiet = new ChiTietPhieuSuaChua();
        chiTiet.setMaChiTiet(resultSet.getLong("maChiTiet"));
        chiTiet.setMaPhieuSuaChua(resultSet.getString("maPhieuSuaChua"));
        chiTiet.setMaPhuTung(resultSet.getString("maPhuTung"));
        chiTiet.setMaDV(resultSet.getString("maDV"));
        chiTiet.setSoLuong(resultSet.getInt("soLuong"));
        chiTiet.setDonGia(resultSet.getBigDecimal("donGia"));
        return chiTiet;
    }

    private void kiemTraDuLieu(ChiTietPhieuSuaChua chiTiet) throws SQLException {
        if (chiTiet == null || chiTiet.getMaPhieuSuaChua() == null
                || chiTiet.getMaPhieuSuaChua().isBlank()) {
            throw new SQLException("Mã phiếu sửa chữa không được để trống.");
        }
        boolean coPhuTung = chiTiet.getMaPhuTung() != null && !chiTiet.getMaPhuTung().isBlank();
        boolean coDichVu = chiTiet.getMaDV() != null && !chiTiet.getMaDV().isBlank();
        if (coPhuTung == coDichVu) {
            throw new SQLException("Chi tiết phải có đúng một mã phụ tùng hoặc mã dịch vụ.");
        }
        if (chiTiet.getSoLuong() == null || chiTiet.getSoLuong() <= 0) {
            throw new SQLException("Số lượng phải lớn hơn 0.");
        }
        if (chiTiet.getDonGia() == null || chiTiet.getDonGia().signum() < 0) {
            throw new SQLException("Đơn giá không được để trống hoặc âm.");
        }
    }
}

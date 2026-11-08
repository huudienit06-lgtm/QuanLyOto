package dao;

import connectDB.ConnectDB;
import entity.ChiTietPhieuNhap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuNhapDAO {
    
    public ChiTietPhieuNhapDAO() {
    }

    // Hàm gọi từ PhieuNhapXuatKhoDAO (chiTietDAO.findByField("maPhieuNhapXuat", ...))
    public List<ChiTietPhieuNhap> findByField(String fieldName, Object value) throws SQLException {
        List<ChiTietPhieuNhap> ds = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietPhieuNhap WHERE " + fieldName + " = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setObject(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                    ct.setMaPhieuNhapXuat(rs.getString("maPhieuNhapXuat"));
                    ct.setMaPhuTung(rs.getString("maPhuTung"));
                    ct.setSoLuong(rs.getInt("soLuong"));
                    ct.setDonGia(rs.getDouble("donGia"));
                    ct.setGhiChu(rs.getString("ghiChu"));
                    ds.add(ct);
                }
            }
        }
        return ds;
    }

    // Hàm gọi từ PhieuNhapXuatKhoDAO (chiTietDAO.deleteById(maPhieu, maPhuTung))
    public boolean deleteById(Object... keyValues) throws SQLException {
        String sql = "DELETE FROM ChiTietPhieuNhap WHERE maPhieuNhapXuat = ? AND maPhuTung = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setObject(1, keyValues[0]);
            stmt.setObject(2, keyValues[1]);
            return stmt.executeUpdate() > 0;
        }
    }
}
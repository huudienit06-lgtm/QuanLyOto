package dao;

import connectDB.ConnectDB;
import entity.Xe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class XeDAO {

    // 1. Lấy danh sách tất cả các xe có trong CSDL
    public List<Xe> getAllCacXe() {
        List<Xe> danhSachXe = new ArrayList<>();
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();
        
        try {
            String sql = "SELECT * FROM Xe"; // Tên bảng Xe trong database của bạn
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            
            while (rs.next()) {
                String maXe = rs.getString("maXe");
                String bienSo = rs.getString("bienSo");
                String hangXe = rs.getString("hangXe");
                String dongXe = rs.getString("dongXe");
                String maKhachHang = rs.getString("maKhachHang");
                
                Xe xe = new Xe(maXe, bienSo, hangXe, dongXe, maKhachHang);
                danhSachXe.add(xe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachXe;
    }

    // 2. Thêm một xe mới vào CSDL
    public boolean createXe(Xe xe) {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();
        PreparedStatement pstmt = null;
        int n = 0;
        
        try {
            String sql = "INSERT INTO Xe (maXe, bienSo, hangXe, dongXe, maKhachHang) VALUES (?, ?, ?, ?, ?)";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, xe.getMaXe());
            pstmt.setString(2, xe.getBienSo());
            pstmt.setString(3, xe.getHangXe());
            pstmt.setString(4, xe.getDongXe());
            pstmt.setString(5, xe.getMaKhachHang());
            
            n = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // 3. Cập nhật thông tin xe
    public boolean updateXe(Xe xe) {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();
        PreparedStatement pstmt = null;
        int n = 0;
        
        try {
            String sql = "UPDATE Xe SET bienSo = ?, hangXe = ?, dongXe = ?, maKhachHang = ? WHERE maXe = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, xe.getBienSo());
            pstmt.setString(2, xe.getHangXe());
            pstmt.setString(3, xe.getDongXe());
            pstmt.setString(4, xe.getMaKhachHang());
            pstmt.setString(5, xe.getMaXe());
            
            n = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // 4. Xóa xe theo mã xe
    public boolean deleteXe(String maXe) {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();
        PreparedStatement pstmt = null;
        int n = 0;
        
        try {
            String sql = "DELETE FROM Xe WHERE maXe = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maXe);
            
            n = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
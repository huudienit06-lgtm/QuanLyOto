package dao;

import entity.PhuTung;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import connectDB.ConnectDB;
/**
 * Lớp DAO quản lý thao tác Cơ sở dữ liệu cho Phụ tùng
 * Kế thừa các hàm truy vấn tự động bằng Reflection từ BaseDAO<PhuTung>
 */
public class PhuTungDAO extends BaseDAO<PhuTung> {

    /**
     * Khởi tạo DAO, cấu hình ánh xạ với CSDL
     * - Class entity: PhuTung.class
     * - Tên bảng SQL: "PhuTung"
     * - Khóa chính: "maPhuTung"
     */
    public PhuTungDAO() {
        super(PhuTung.class, "PhuTung", "maPhuTung");
    }
    // CÁC HÀM TƯƠNG TÁC VỚI GIAO DIỆN FRMPHUTUNG
    /**
     * 1. Lấy toàn bộ danh sách phụ tùng để đổ lên bảng
     * Sử dụng hàm findAll() tích hợp sẵn của BaseDAO
     */
    public List<PhuTung> layTatCaPhuTung() {
        try {
            return findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
    /**
     * 2. Thêm phụ tùng mới vào kho
     * Sử dụng hàm insert() tích hợp sẵn của BaseDAO
     */
    public boolean themPhuTung(PhuTung pt) {
        try {
            return insert(pt);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 3. Cập nhật thông tin và giá bán phụ tùng
     * Sử dụng hàm update() tích hợp sẵn của BaseDAO
     */
    public boolean capNhatPhuTung(PhuTung pt) {
        try {
            return update(pt);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 4. Xóa phụ tùng khỏi hệ thống
     * Sử dụng hàm deleteById() tích hợp sẵn của BaseDAO
     */
    public boolean xoaPhuTung(String maPhuTung) {
        try {
            return deleteById(maPhuTung);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 5. Hàm truy vấn đặc thù: Lấy danh sách Nhà cung cấp không trùng lặp
     * Hàm này phục vụ việc đổ dữ liệu tự động vào JComboBox trên Giao diện
     */
public List<String> layDanhSachNhaCungCap() {
    List<String> dsNCC = new ArrayList<>();
    String sql = "SELECT DISTINCT nhaCungCap FROM PhuTung WHERE nhaCungCap IS NOT NULL AND nhaCungCap != ''";
    
    try {
        // Tự mở kết nối CSDL trực tiếp
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
         
        while (rs.next()) {
            dsNCC.add(rs.getString("nhaCungCap"));
        }
        
        // Giải phóng tài nguyên
        rs.close();
        statement.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return dsNCC;
}
}

package dao;

import entity.HoaDon;
import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class HoaDonDAO extends BaseDAO<HoaDon> {

    public HoaDonDAO() {
        super(HoaDon.class, "HoaDon", "maHoaDon");
    }

    /**
     * Sinh mã hóa đơn tiếp theo theo số thứ tự lớn nhất trong CSDL.
     * Ví dụ: HD001, HD002, HD003...
     */
    public String taoMaHoaDonMoi() {
        String sql = """
                SELECT ISNULL(
                    MAX(TRY_CONVERT(INT, SUBSTRING(maHoaDon, 3, 20))),
                    0
                ) + 1 AS soTiepTheo
                FROM HoaDon
                WHERE maHoaDon LIKE 'HD%'
                """;

        try (
                Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int soTiepTheo = rs.getInt("soTiepTheo");
                return String.format("HD%03d", soTiepTheo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Không thể phát sinh mã hóa đơn: " + e.getMessage());
        }

        return "";
    }
}

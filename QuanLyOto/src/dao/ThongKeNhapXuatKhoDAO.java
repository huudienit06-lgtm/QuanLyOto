package dao;

import connectDB.ConnectDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThongKeNhapXuatKhoDAO {
    public List<ThongKePhuTung> layThongKe(
            LocalDate tuNgay, LocalDate denNgay, String kieuPhieu) throws SQLException {
        String sql = "SELECT pt.maPhuTung, pt.tenPhuTung, pt.donViTinh, SUM(ct.soLuong) AS tongSoLuong "
                + "FROM PhieuNhapXuatKho pnx "
                + "JOIN ChiTietPhieuNhap ct ON ct.maPhieuNhapXuat = pnx.maPhieuNhapXuat "
                + "JOIN PhuTung pt ON pt.maPhuTung = ct.maPhuTung "
                + "WHERE pnx.kieuPhieu = ? AND pnx.ngayLapPhieu >= ? AND pnx.ngayLapPhieu < ? "
                + "GROUP BY pt.maPhuTung, pt.tenPhuTung, pt.donViTinh "
                + "ORDER BY pt.tenPhuTung, pt.maPhuTung";
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu để tải thống kê nhập xuất kho.");
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, kieuPhieu);
            statement.setDate(2, Date.valueOf(tuNgay));
            statement.setDate(3, Date.valueOf(denNgay));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ThongKePhuTung> danhSach = new ArrayList<>();
                while (resultSet.next()) {
                    danhSach.add(new ThongKePhuTung(
                            resultSet.getString("maPhuTung"),
                            resultSet.getString("tenPhuTung"),
                            resultSet.getString("donViTinh"),
                            resultSet.getLong("tongSoLuong")));
                }
                return danhSach;
            }
        }
    }

    public record ThongKePhuTung(String maPhuTung, String tenPhuTung, String donViTinh, long soLuong) {
    }
}

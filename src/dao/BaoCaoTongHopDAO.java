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

public class BaoCaoTongHopDAO {
    private static final String SQL_BAO_CAO =
            "WITH KyBaoCao AS (SELECT CAST(? AS DATE) AS tuKyTruoc, "
                    + "CAST(? AS DATE) AS tuKyBaoCao, CAST(? AS DATE) AS denKyBaoCao) "
                    + "SELECT N'Doanh thu' AS nhom, N'Tổng doanh thu' AS chiTieu, "
                    + "COALESCE(SUM(CASE WHEN hd.ngayLap >= ky.tuKyTruoc AND hd.ngayLap < ky.tuKyBaoCao "
                    + "THEN COALESCE(hd.soTien, 0) ELSE 0 END), 0) AS kyTruoc, "
                    + "COALESCE(SUM(CASE WHEN hd.ngayLap >= ky.tuKyBaoCao AND hd.ngayLap < ky.denKyBaoCao "
                    + "THEN COALESCE(hd.soTien, 0) ELSE 0 END), 0) AS kyBaoCao "
                    + "FROM HoaDon hd CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Doanh thu', N'Số hóa đơn', "
                    + "SUM(CASE WHEN hd.ngayLap >= ky.tuKyTruoc AND hd.ngayLap < ky.tuKyBaoCao THEN 1 ELSE 0 END), "
                    + "SUM(CASE WHEN hd.ngayLap >= ky.tuKyBaoCao AND hd.ngayLap < ky.denKyBaoCao THEN 1 ELSE 0 END) "
                    + "FROM HoaDon hd CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Sửa chữa', N'Số phiếu sửa chữa', "
                    + "SUM(CASE WHEN ps.ngayBatDau >= ky.tuKyTruoc AND ps.ngayBatDau < ky.tuKyBaoCao THEN 1 ELSE 0 END), "
                    + "SUM(CASE WHEN ps.ngayBatDau >= ky.tuKyBaoCao AND ps.ngayBatDau < ky.denKyBaoCao THEN 1 ELSE 0 END) "
                    + "FROM PhieuSuaChua ps CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Sửa chữa', N'Phiếu đã hoàn thành', "
                    + "SUM(CASE WHEN ps.ngayBatDau >= ky.tuKyTruoc AND ps.ngayBatDau < ky.tuKyBaoCao "
                    + "AND ps.trangThai = N'Hoàn thành' THEN 1 ELSE 0 END), "
                    + "SUM(CASE WHEN ps.ngayBatDau >= ky.tuKyBaoCao AND ps.ngayBatDau < ky.denKyBaoCao "
                    + "AND ps.trangThai = N'Hoàn thành' THEN 1 ELSE 0 END) "
                    + "FROM PhieuSuaChua ps CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Khách hàng', N'Khách hàng sử dụng dịch vụ', "
                    + "COUNT(DISTINCT CASE WHEN ps.ngayBatDau >= ky.tuKyTruoc AND ps.ngayBatDau < ky.tuKyBaoCao "
                    + "THEN ps.maKH END), "
                    + "COUNT(DISTINCT CASE WHEN ps.ngayBatDau >= ky.tuKyBaoCao AND ps.ngayBatDau < ky.denKyBaoCao "
                    + "THEN ps.maKH END) "
                    + "FROM PhieuSuaChua ps CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Khách hàng', N'Phiếu tiếp nhận', "
                    + "SUM(CASE WHEN pt.ngayLap >= ky.tuKyTruoc AND pt.ngayLap < ky.tuKyBaoCao THEN 1 ELSE 0 END), "
                    + "SUM(CASE WHEN pt.ngayLap >= ky.tuKyBaoCao AND pt.ngayLap < ky.denKyBaoCao THEN 1 ELSE 0 END) "
                    + "FROM PhieuTiepNhan pt CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Nhân viên', N'Nhân viên được phân công', "
                    + "COUNT(DISTINCT CASE WHEN ll.ngayLamViec >= ky.tuKyTruoc AND ll.ngayLamViec < ky.tuKyBaoCao "
                    + "THEN ll.maNV END), "
                    + "COUNT(DISTINCT CASE WHEN ll.ngayLamViec >= ky.tuKyBaoCao AND ll.ngayLamViec < ky.denKyBaoCao "
                    + "THEN ll.maNV END) "
                    + "FROM LichLamViec ll CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Nhân viên', N'Số ca được phân công', "
                    + "SUM(CASE WHEN ll.ngayLamViec >= ky.tuKyTruoc AND ll.ngayLamViec < ky.tuKyBaoCao THEN 1 ELSE 0 END), "
                    + "SUM(CASE WHEN ll.ngayLamViec >= ky.tuKyBaoCao AND ll.ngayLamViec < ky.denKyBaoCao THEN 1 ELSE 0 END) "
                    + "FROM LichLamViec ll CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Phụ tùng', N'Số lượng phụ tùng đã xuất', "
                    + "COALESCE(SUM(CASE WHEN hd.ngayLap >= ky.tuKyTruoc AND hd.ngayLap < ky.tuKyBaoCao "
                    + "THEN COALESCE(ct.soLuong, 0) ELSE 0 END), 0), "
                    + "COALESCE(SUM(CASE WHEN hd.ngayLap >= ky.tuKyBaoCao AND hd.ngayLap < ky.denKyBaoCao "
                    + "THEN COALESCE(ct.soLuong, 0) ELSE 0 END), 0) "
                    + "FROM ChiTietHoaDon ct JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Phụ tùng', N'Giá trị phụ tùng đã xuất', "
                    + "COALESCE(SUM(CASE WHEN hd.ngayLap >= ky.tuKyTruoc AND hd.ngayLap < ky.tuKyBaoCao "
                    + "THEN COALESCE(ct.soLuong, 0) * COALESCE(ct.donGia, 0) ELSE 0 END), 0), "
                    + "COALESCE(SUM(CASE WHEN hd.ngayLap >= ky.tuKyBaoCao AND hd.ngayLap < ky.denKyBaoCao "
                    + "THEN COALESCE(ct.soLuong, 0) * COALESCE(ct.donGia, 0) ELSE 0 END), 0) "
                    + "FROM ChiTietHoaDon ct JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon CROSS JOIN KyBaoCao ky";

    public List<ChiTieuBaoCao> layBaoCao(LocalDate tuKyTruoc, LocalDate tuKyBaoCao,
            LocalDate denKyBaoCao) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }

        try (PreparedStatement statement = connection.prepareStatement(SQL_BAO_CAO)) {
            statement.setDate(1, Date.valueOf(tuKyTruoc));
            statement.setDate(2, Date.valueOf(tuKyBaoCao));
            statement.setDate(3, Date.valueOf(denKyBaoCao));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ChiTieuBaoCao> danhSach = new ArrayList<>();
                while (resultSet.next()) {
                    danhSach.add(new ChiTieuBaoCao(
                            resultSet.getString("nhom"),
                            resultSet.getString("chiTieu"),
                            resultSet.getDouble("kyTruoc"),
                            resultSet.getDouble("kyBaoCao")));
                }
                return danhSach;
            }
        }
    }

    public record ChiTieuBaoCao(String nhom, String chiTieu, double kyTruoc, double kyBaoCao) {
    }
}

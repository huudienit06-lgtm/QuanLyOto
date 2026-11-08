package dao;

import connectDB.ConnectDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Timestamp;
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
                    + "FROM ChiTietHoaDon ct JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Công nợ', N'Công nợ còn lại của hóa đơn trong kỳ', "
                    + "COALESCE(SUM(CASE WHEN hd.ngayLap >= ky.tuKyTruoc AND hd.ngayLap < ky.tuKyBaoCao "
                    + "THEN CASE WHEN hd.soTien - COALESCE(tt.daThu, 0) > 0 "
                    + "THEN hd.soTien - COALESCE(tt.daThu, 0) ELSE 0 END ELSE 0 END), 0), "
                    + "COALESCE(SUM(CASE WHEN hd.ngayLap >= ky.tuKyBaoCao AND hd.ngayLap < ky.denKyBaoCao "
                    + "THEN CASE WHEN hd.soTien - COALESCE(tt.daThu, 0) > 0 "
                    + "THEN hd.soTien - COALESCE(tt.daThu, 0) ELSE 0 END ELSE 0 END), 0) "
                    + "FROM HoaDon hd "
                    + "LEFT JOIN (SELECT maHoaDon, SUM(soTienThanhToan) AS daThu "
                    + "FROM GiaoDichThanhToan "
                    + "WHERE trangThaiGD IS NULL OR trangThaiGD = N'Thành công' GROUP BY maHoaDon) tt "
                    + "ON tt.maHoaDon = hd.maHoaDon CROSS JOIN KyBaoCao ky "
                    + "UNION ALL "
                    + "SELECT N'Kho', N'Số lượng phụ tùng nhập kho', "
                    + "COALESCE(SUM(CASE WHEN pnx.ngayLapPhieu >= ky.tuKyTruoc "
                    + "AND pnx.ngayLapPhieu < ky.tuKyBaoCao THEN COALESCE(ct.soLuong, 0) ELSE 0 END), 0), "
                    + "COALESCE(SUM(CASE WHEN pnx.ngayLapPhieu >= ky.tuKyBaoCao "
                    + "AND pnx.ngayLapPhieu < ky.denKyBaoCao THEN COALESCE(ct.soLuong, 0) ELSE 0 END), 0) "
                    + "FROM PhieuNhapXuatKho pnx JOIN ChiTietPhieuNhap ct "
                    + "ON ct.maPhieuNhapXuat = pnx.maPhieuNhapXuat "
                    + "CROSS JOIN KyBaoCao ky WHERE pnx.kieuPhieu = N'Nhập' "
                    + "UNION ALL "
                    + "SELECT N'Kho', N'Số lượng phụ tùng xuất kho', "
                    + "COALESCE(SUM(CASE WHEN pnx.ngayLapPhieu >= ky.tuKyTruoc "
                    + "AND pnx.ngayLapPhieu < ky.tuKyBaoCao THEN COALESCE(ct.soLuong, 0) ELSE 0 END), 0), "
                    + "COALESCE(SUM(CASE WHEN pnx.ngayLapPhieu >= ky.tuKyBaoCao "
                    + "AND pnx.ngayLapPhieu < ky.denKyBaoCao THEN COALESCE(ct.soLuong, 0) ELSE 0 END), 0) "
                    + "FROM PhieuNhapXuatKho pnx JOIN ChiTietPhieuNhap ct "
                    + "ON ct.maPhieuNhapXuat = pnx.maPhieuNhapXuat "
                    + "CROSS JOIN KyBaoCao ky WHERE pnx.kieuPhieu = N'Xuất'";

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

    public List<DiemXuHuong> layXuHuong(
            LocalDate tuNgay, LocalDate denNgay, String nhom, String doPhanGiai) throws SQLException {
        String phanNhom = switch (doPhanGiai) {
            case "HOUR" -> "DATEADD(HOUR, DATEDIFF(HOUR, 0, %s), 0)";
            case "DAY" -> "DATEADD(DAY, DATEDIFF(DAY, 0, %s), 0)";
            case "MONTH" -> "DATEADD(MONTH, DATEDIFF(MONTH, 0, %s), 0)";
            default -> throw new IllegalArgumentException("Độ phân giải thời gian không hợp lệ.");
        };
        String sql = taoSqlXuHuong(nhom, phanNhom);
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu để tải biểu đồ xu hướng.");
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            Timestamp batDau = Timestamp.valueOf(tuNgay.atStartOfDay());
            Timestamp ketThuc = Timestamp.valueOf(denNgay.atStartOfDay());
            statement.setTimestamp(1, batDau);
            statement.setTimestamp(2, ketThuc);
            if ("Kho".equals(nhom)) {
                statement.setTimestamp(3, batDau);
                statement.setTimestamp(4, ketThuc);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<DiemXuHuong> danhSach = new ArrayList<>();
                while (resultSet.next()) {
                    Timestamp moc = resultSet.getTimestamp("mocThoiGian");
                    if (moc != null) {
                        danhSach.add(new DiemXuHuong(
                                resultSet.getString("chiTieu"),
                                moc.toLocalDateTime(),
                                resultSet.getDouble("giaTri")));
                    }
                }
                return danhSach;
            }
        }
    }

    private String taoSqlXuHuong(String nhom, String phanNhom) {
        return switch (nhom) {
            case "Doanh thu" -> {
                String moc = phanNhom.formatted("hd.ngayLap");
                yield "SELECT " + moc + " AS mocThoiGian, "
                        + "SUM(COALESCE(hd.soTien, 0)) AS giaTri, N'Tổng doanh thu' AS chiTieu "
                        + "FROM HoaDon hd WHERE hd.ngayLap >= ? AND hd.ngayLap < ? "
                        + "GROUP BY " + moc;
            }
            case "Sửa chữa" -> {
                String moc = phanNhom.formatted("ps.ngayBatDau");
                yield "SELECT " + moc + " AS mocThoiGian, COUNT(*) AS giaTri, "
                        + "N'Số phiếu sửa chữa' AS chiTieu FROM PhieuSuaChua ps "
                        + "WHERE ps.ngayBatDau >= ? AND ps.ngayBatDau < ? GROUP BY " + moc;
            }
            case "Khách hàng" -> {
                String moc = phanNhom.formatted("ps.ngayBatDau");
                yield "SELECT " + moc + " AS mocThoiGian, COUNT(DISTINCT ps.maKH) AS giaTri, "
                        + "N'Khách hàng sử dụng dịch vụ' AS chiTieu FROM PhieuSuaChua ps "
                        + "WHERE ps.ngayBatDau >= ? AND ps.ngayBatDau < ? GROUP BY " + moc;
            }
            case "Nhân viên" -> {
                String moc = phanNhom.formatted("ll.ngayLamViec");
                yield "SELECT " + moc + " AS mocThoiGian, COUNT(DISTINCT ll.maNV) AS giaTri, "
                        + "N'Nhân viên được phân công' AS chiTieu FROM LichLamViec ll "
                        + "WHERE ll.ngayLamViec >= ? AND ll.ngayLamViec < ? GROUP BY " + moc;
            }
            case "Phụ tùng" -> {
                String moc = phanNhom.formatted("hd.ngayLap");
                yield "SELECT " + moc + " AS mocThoiGian, SUM(COALESCE(ct.soLuong, 0)) AS giaTri, "
                        + "N'Số lượng phụ tùng đã xuất' AS chiTieu "
                        + "FROM ChiTietHoaDon ct JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon "
                        + "WHERE ct.maPhuTung IS NOT NULL AND hd.ngayLap >= ? AND hd.ngayLap < ? "
                        + "GROUP BY " + moc;
            }
            case "Công nợ" -> {
                String moc = phanNhom.formatted("hd.ngayLap");
                yield "SELECT " + moc + " AS mocThoiGian, "
                        + "SUM(CASE WHEN hd.soTien - COALESCE(tt.daThu, 0) > 0 "
                        + "THEN hd.soTien - COALESCE(tt.daThu, 0) ELSE 0 END) AS giaTri, "
                        + "N'Công nợ còn lại của hóa đơn trong kỳ' AS chiTieu "
                        + "FROM HoaDon hd LEFT JOIN ("
                        + "SELECT maHoaDon, SUM(soTienThanhToan) AS daThu "
                        + "FROM GiaoDichThanhToan "
                        + "WHERE trangThaiGD IS NULL OR trangThaiGD = N'Thành công' GROUP BY maHoaDon"
                        + ") tt ON tt.maHoaDon = hd.maHoaDon "
                        + "WHERE hd.ngayLap >= ? AND hd.ngayLap < ? GROUP BY " + moc;
            }
            case "Kho" -> {
                String mocNhap = phanNhom.formatted("pnx.ngayLapPhieu");
                String mocXuat = phanNhom.formatted("pnx.ngayLapPhieu");
                yield "SELECT " + mocNhap + " AS mocThoiGian, "
                        + "SUM(COALESCE(ct.soLuong, 0)) AS giaTri, N'Số lượng phụ tùng nhập kho' AS chiTieu "
                        + "FROM PhieuNhapXuatKho pnx JOIN ChiTietPhieuNhap ct "
                        + "ON ct.maPhieuNhapXuat = pnx.maPhieuNhapXuat "
                        + "WHERE pnx.kieuPhieu = N'Nhập' AND pnx.ngayLapPhieu >= ? "
                        + "AND pnx.ngayLapPhieu < ? GROUP BY " + mocNhap
                        + " UNION ALL SELECT " + mocXuat + " AS mocThoiGian, "
                        + "SUM(COALESCE(ct.soLuong, 0)) AS giaTri, N'Số lượng phụ tùng xuất kho' AS chiTieu "
                        + "FROM PhieuNhapXuatKho pnx JOIN ChiTietPhieuNhap ct "
                        + "ON ct.maPhieuNhapXuat = pnx.maPhieuNhapXuat "
                        + "WHERE pnx.kieuPhieu = N'Xuất' AND pnx.ngayLapPhieu >= ? "
                        + "AND pnx.ngayLapPhieu < ? GROUP BY " + mocXuat;
            }
            default -> throw new IllegalArgumentException("Nhóm báo cáo không hợp lệ: " + nhom);
        };
    }

    public record ChiTieuBaoCao(String nhom, String chiTieu, double kyTruoc, double kyBaoCao) {
    }

    public record DiemXuHuong(String chiTieu, LocalDateTime mocThoiGian, double giaTri) {
    }
}

package dao;

import connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoSuaChuaDAO {
    private static final String TAT_CA = "Tất cả";

    private static final String SQL_TONG_QUAN = "SELECT COUNT(*) AS tongPhieu, "
            + "COALESCE(SUM(CASE WHEN ps.trangThai = N'Hoàn thành' THEN 1 ELSE 0 END), 0) AS daHoanThanh, "
            + "COALESCE(SUM(CASE WHEN ps.trangThai <> N'Hoàn thành' THEN 1 ELSE 0 END), 0) AS dangXuLy, "
            + "COALESCE(SUM(COALESCE(hd.tongTien, 0)), 0) AS doanhThu "
            + "FROM PhieuSuaChua ps "
            + "LEFT JOIN (SELECT maPhieuSuaChua, SUM(COALESCE(soTien, 0)) AS tongTien "
            + "           FROM HoaDon GROUP BY maPhieuSuaChua) hd "
            + "ON hd.maPhieuSuaChua = ps.maPhieuSuaChua "
            + "WHERE ps.ngayBatDau >= ? AND ps.ngayBatDau < ? "
            + "AND (? IS NULL OR ps.trangThai = ?) "
            + "AND (? IS NULL OR ps.maNV = ?)";

    public ThongKeTongQuan layTongQuan(LocalDate tuNgay, LocalDate denNgay,
            String trangThai, String maNV) throws SQLException {
        try (PreparedStatement statement = moStatement(SQL_TONG_QUAN)) {
            datKhoangThoiGian(statement, tuNgay, denNgay);
            datBoLoc(statement, 3, trangThai);
            datBoLoc(statement, 5, maNV);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Không nhận được dữ liệu tổng quan sửa chữa.");
                }
                return new ThongKeTongQuan(
                        resultSet.getInt("tongPhieu"),
                        resultSet.getInt("daHoanThanh"),
                        resultSet.getInt("dangXuLy"),
                        resultSet.getBigDecimal("doanhThu"));
            }
        }
    }

    public List<ThongKeBieuDo> layTheoBieuDo(LocalDate ngayMoc, String ky,
            String trangThai, String maNV) throws SQLException {
        LocalDate tuNgay;
        LocalDate denNgay;
        String nhan;
        String nhom;
        if ("Tuần".equals(ky)) {
            tuNgay = ngayMoc.minusDays(6);
            denNgay = ngayMoc.plusDays(1);
            nhan = "CONVERT(varchar(10), CONVERT(date, ps.ngayBatDau), 23)";
            nhom = "CONVERT(date, ps.ngayBatDau)";
        } else if ("Tháng".equals(ky)) {
            tuNgay = ngayMoc.withDayOfMonth(1);
            denNgay = tuNgay.plusMonths(1);
            nhan = "CONVERT(varchar(2), DAY(ps.ngayBatDau))";
            nhom = "DAY(ps.ngayBatDau)";
        } else {
            tuNgay = ngayMoc.withDayOfYear(1);
            denNgay = tuNgay.plusYears(1);
            nhan = "CONVERT(varchar(2), MONTH(ps.ngayBatDau))";
            nhom = "MONTH(ps.ngayBatDau)";
        }
        String sql = "SELECT " + nhan + " AS nhan, COUNT(*) AS soLuong "
                + "FROM PhieuSuaChua ps WHERE ps.ngayBatDau >= ? AND ps.ngayBatDau < ? "
                + "AND (? IS NULL OR ps.trangThai = ?) AND (? IS NULL OR ps.maNV = ?) "
                + "GROUP BY " + nhom + " ORDER BY " + nhom;
        List<ThongKeBieuDo> danhSach = new ArrayList<>();
        try (PreparedStatement statement = moStatement(sql)) {
            statement.setDate(1, Date.valueOf(tuNgay));
            statement.setDate(2, Date.valueOf(denNgay));
            datBoLoc(statement, 3, trangThai);
            datBoLoc(statement, 5, maNV);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new ThongKeBieuDo(
                            resultSet.getString("nhan"),
                            resultSet.getInt("soLuong")));
                }
            }
        }
        return danhSach;
    }

    public List<ThongKeSuDung> layTopSuDung(LocalDate ngayMoc, String ky,
            String loai, String trangThai, String maNV) throws SQLException {
        LocalDate tuNgay;
        LocalDate denNgay;
        if ("Ngày".equals(ky)) {
            tuNgay = ngayMoc;
            denNgay = ngayMoc.plusDays(1);
        } else if ("Tháng".equals(ky)) {
            tuNgay = ngayMoc.withDayOfMonth(1);
            denNgay = tuNgay.plusMonths(1);
        } else {
            tuNgay = ngayMoc.withDayOfYear(1);
            denNgay = tuNgay.plusYears(1);
        }

        StringBuilder sql = new StringBuilder(
                "SELECT TOP 5 "
                + "CASE WHEN ct.maPhuTung IS NULL THEN N'Dịch vụ' ELSE N'Phụ tùng' END AS loai, "
                + "COALESCE(pt.maPhuTung, dv.maDV) AS ma, "
                + "COALESCE(pt.tenPhuTung, dv.tenDV) AS ten, "
                + "SUM(ct.soLuong) AS soLuong "
                + "FROM PhieuSuaChua ps "
                + "JOIN ChiTietPhieuSuaChua ct ON ct.maPhieuSuaChua = ps.maPhieuSuaChua "
                + "LEFT JOIN PhuTung pt ON pt.maPhuTung = ct.maPhuTung "
                + "LEFT JOIN DichVu dv ON dv.maDV = ct.maDV "
                + "WHERE ps.ngayBatDau >= ? AND ps.ngayBatDau < ? "
                + "AND (? IS NULL OR ps.trangThai = ?) "
                + "AND (? IS NULL OR ps.maNV = ?) ");
        if ("Dịch vụ".equals(loai)) {
            sql.append("AND ct.maDV IS NOT NULL ");
        } else if ("Phụ tùng".equals(loai)) {
            sql.append("AND ct.maPhuTung IS NOT NULL ");
        }
        sql.append("GROUP BY CASE WHEN ct.maPhuTung IS NULL THEN N'Dịch vụ' ELSE N'Phụ tùng' END, "
                + "COALESCE(pt.maPhuTung, dv.maDV), COALESCE(pt.tenPhuTung, dv.tenDV) "
                + "ORDER BY SUM(ct.soLuong) DESC, ten");

        List<ThongKeSuDung> danhSach = new ArrayList<>();
        try (PreparedStatement statement = moStatement(sql.toString())) {
            statement.setDate(1, Date.valueOf(tuNgay));
            statement.setDate(2, Date.valueOf(denNgay));
            datBoLoc(statement, 3, trangThai);
            datBoLoc(statement, 5, maNV);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new ThongKeSuDung(
                            ky,
                            resultSet.getString("loai"),
                            resultSet.getString("ma"),
                            resultSet.getString("ten"),
                            resultSet.getInt("soLuong")));
                }
            }
        }
        return danhSach;
    }

    private PreparedStatement moStatement(String sql) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu để thống kê sửa chữa.");
        }
        return connection.prepareStatement(sql);
    }

    private void datKhoangThoiGian(PreparedStatement statement, LocalDate tuNgay,
            LocalDate denNgay) throws SQLException {
        if (tuNgay == null || denNgay == null || denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Khoảng thời gian thống kê không hợp lệ.");
        }
        statement.setDate(1, Date.valueOf(tuNgay));
        statement.setDate(2, Date.valueOf(denNgay.plusDays(1)));
    }

    private void datBoLoc(PreparedStatement statement, int viTri, String giaTri)
            throws SQLException {
        String boLoc = giaTri == null || giaTri.isBlank()
                || TAT_CA.equals(giaTri)
                || giaTri.startsWith(TAT_CA + " ")
                ? null
                : giaTri.trim();
        statement.setString(viTri, boLoc);
        statement.setString(viTri + 1, boLoc);
    }

    public record ThongKeTongQuan(int tongPhieu, int daHoanThanh, int dangXuLy,
            BigDecimal doanhThu) {
    }

    public record ThongKeBieuDo(String nhan, int soLuong) {
    }

    public record ThongKeSuDung(String ky, String loai, String ma, String ten, int soLuong) {
    }

}

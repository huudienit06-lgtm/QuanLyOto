package dao;

import connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    private static final String SQL_CHI_TIET = "SELECT ps.maPhieuSuaChua, ps.maXe, kh.hoTen AS tenKhachHang, "
            + "nv.hoTen AS tenNhanVien, ps.ngayBatDau, ps.trangThai, "
            + "COALESCE(hd.tongTien, 0) AS tongTien "
            + "FROM PhieuSuaChua ps "
            + "LEFT JOIN KhachHang kh ON kh.maKH = ps.maKH "
            + "LEFT JOIN NhanVien nv ON nv.maNV = ps.maNV "
            + "LEFT JOIN (SELECT maPhieuSuaChua, SUM(COALESCE(soTien, 0)) AS tongTien "
            + "           FROM HoaDon GROUP BY maPhieuSuaChua) hd "
            + "ON hd.maPhieuSuaChua = ps.maPhieuSuaChua "
            + "WHERE ps.ngayBatDau >= ? AND ps.ngayBatDau < ? "
            + "AND (? IS NULL OR ps.trangThai = ?) "
            + "AND (? IS NULL OR ps.maNV = ?) "
            + "ORDER BY ps.ngayBatDau DESC, ps.maPhieuSuaChua";

    private static final String SQL_THEO_TRANG_THAI = "SELECT ps.trangThai, COUNT(*) AS soLuong "
            + "FROM PhieuSuaChua ps "
            + "WHERE ps.ngayBatDau >= ? AND ps.ngayBatDau < ? "
            + "AND (? IS NULL OR ps.trangThai = ?) "
            + "AND (? IS NULL OR ps.maNV = ?) "
            + "GROUP BY ps.trangThai ORDER BY ps.trangThai";

    private static final String SQL_THEO_THANG = "SELECT YEAR(ps.ngayBatDau) AS nam, MONTH(ps.ngayBatDau) AS thang, "
            + "COUNT(*) AS soLuong "
            + "FROM PhieuSuaChua ps "
            + "WHERE ps.ngayBatDau >= ? AND ps.ngayBatDau < ? "
            + "AND (? IS NULL OR ps.trangThai = ?) "
            + "AND (? IS NULL OR ps.maNV = ?) "
            + "GROUP BY YEAR(ps.ngayBatDau), MONTH(ps.ngayBatDau) "
            + "ORDER BY nam, thang";

    private static final String SQL_NHAN_VIEN = "SELECT maNV, hoTen FROM NhanVien ORDER BY hoTen, maNV";

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

    public List<ThongKePhieuSuaChua> layChiTiet(LocalDate tuNgay, LocalDate denNgay,
            String trangThai, String maNV) throws SQLException {
        List<ThongKePhieuSuaChua> danhSach = new ArrayList<>();
        try (PreparedStatement statement = moStatement(SQL_CHI_TIET)) {
            datKhoangThoiGian(statement, tuNgay, denNgay);
            datBoLoc(statement, 3, trangThai);
            datBoLoc(statement, 5, maNV);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new ThongKePhieuSuaChua(
                            resultSet.getString("maPhieuSuaChua"),
                            resultSet.getString("maXe"),
                            resultSet.getString("tenKhachHang"),
                            resultSet.getString("tenNhanVien"),
                            resultSet.getTimestamp("ngayBatDau"),
                            resultSet.getString("trangThai"),
                            resultSet.getBigDecimal("tongTien")));
                }
            }
        }
        return danhSach;
    }

    public List<ThongKeTrangThai> layTheoTrangThai(LocalDate tuNgay, LocalDate denNgay,
            String trangThai, String maNV) throws SQLException {
        List<ThongKeTrangThai> danhSach = new ArrayList<>();
        try (PreparedStatement statement = moStatement(SQL_THEO_TRANG_THAI)) {
            datKhoangThoiGian(statement, tuNgay, denNgay);
            datBoLoc(statement, 3, trangThai);
            datBoLoc(statement, 5, maNV);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new ThongKeTrangThai(
                            resultSet.getString("trangThai"),
                            resultSet.getInt("soLuong")));
                }
            }
        }
        return danhSach;
    }

    public List<ThongKeTheoThang> layTheoThang(LocalDate tuNgay, LocalDate denNgay,
            String trangThai, String maNV) throws SQLException {
        List<ThongKeTheoThang> danhSach = new ArrayList<>();
        try (PreparedStatement statement = moStatement(SQL_THEO_THANG)) {
            datKhoangThoiGian(statement, tuNgay, denNgay);
            datBoLoc(statement, 3, trangThai);
            datBoLoc(statement, 5, maNV);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new ThongKeTheoThang(
                            resultSet.getInt("nam"),
                            resultSet.getInt("thang"),
                            resultSet.getInt("soLuong")));
                }
            }
        }
        return danhSach;
    }

    public List<NhanVienThongKe> layDanhSachNhanVien() throws SQLException {
        List<NhanVienThongKe> danhSach = new ArrayList<>();
        try (PreparedStatement statement = moStatement(SQL_NHAN_VIEN);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(new NhanVienThongKe(
                        resultSet.getString("maNV"),
                        resultSet.getString("hoTen")));
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

    public record ThongKePhieuSuaChua(String maPhieuSuaChua, String maXe,
            String tenKhachHang, String tenNhanVien, Timestamp ngayBatDau,
            String trangThai, BigDecimal tongTien) {
    }

    public record ThongKeTrangThai(String trangThai, int soLuong) {
    }

    public record ThongKeTheoThang(int nam, int thang, int soLuong) {
    }

    public record NhanVienThongKe(String maNV, String hoTen) {
    }

}

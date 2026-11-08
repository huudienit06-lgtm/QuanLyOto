package dao;

import connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ThongKeCongNoDAO {
    private static final String SQL_KHACH_HANG = "SELECT maKH, hoTen, soDT FROM KhachHang ORDER BY maKH";
    private static final String SQL_HOA_DON = "SELECT hd.maHoaDon, hd.ngayLap, hd.soTien, psc.maKH, "
            + "COALESCE(tt.daThanhToan, 0) AS daThanhToan "
            + "FROM HoaDon hd LEFT JOIN PhieuSuaChua psc "
            + "ON hd.maPhieuSuaChua = psc.maPhieuSuaChua "
            + "LEFT JOIN (SELECT maHoaDon, SUM(soTienThanhToan) AS daThanhToan "
            + "FROM GiaoDichThanhToan WHERE trangThaiGD = N'Thành công' "
            + "GROUP BY maHoaDon) tt ON tt.maHoaDon = hd.maHoaDon "
            + "ORDER BY hd.ngayLap DESC";
    private static final String SQL_GIAO_DICH = "SELECT thoiGianGD, phuongThucThanhToan, soTienThanhToan, trangThaiGD "
            + "FROM GiaoDichThanhToan WHERE maHoaDon = ? ORDER BY thoiGianGD";

    public DuLieuCongNo layDuLieu() throws SQLException {
        Connection connection = ketNoi();
        List<KhachHangData> khachHang = new ArrayList<>();
        List<HoaDonData> hoaDon = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_KHACH_HANG);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                khachHang.add(new KhachHangData(resultSet.getString("maKH"),
                        resultSet.getString("hoTen"), resultSet.getString("soDT")));
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(SQL_HOA_DON);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Timestamp ngayLap = resultSet.getTimestamp("ngayLap");
                if (ngayLap == null) {
                    continue;
                }
                long tong = laySo(resultSet.getBigDecimal("soTien"));
                long daThanhToan = laySo(resultSet.getBigDecimal("daThanhToan"));
                hoaDon.add(new HoaDonData(resultSet.getString("maHoaDon"),
                        ngayLap.toLocalDateTime().toLocalDate(), resultSet.getString("maKH"),
                        tong, Math.max(0L, tong - daThanhToan)));
            }
        }
        return new DuLieuCongNo(khachHang, hoaDon);
    }

    public List<GiaoDichData> layGiaoDich(String maHoaDon) throws SQLException {
        Connection connection = ketNoi();
        List<GiaoDichData> giaoDich = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GIAO_DICH)) {
            statement.setString(1, maHoaDon);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Timestamp thoiGian = resultSet.getTimestamp("thoiGianGD");
                    giaoDich.add(new GiaoDichData(thoiGian == null ? null : thoiGian.toLocalDateTime(),
                            resultSet.getString("phuongThucThanhToan"),
                            laySo(resultSet.getBigDecimal("soTienThanhToan")),
                            resultSet.getString("trangThaiGD")));
                }
            }
        }
        return giaoDich;
    }

    private Connection ketNoi() throws SQLException {
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối SQL Server.");
        }
        return connection;
    }

    private static long laySo(BigDecimal so) {
        return so == null ? 0L : so.longValue();
    }

    public record DuLieuCongNo(List<KhachHangData> khachHang, List<HoaDonData> hoaDon) {
    }

    public record KhachHangData(String ma, String ten, String sdt) {
    }

    public record HoaDonData(String ma, LocalDate ngay, String maKH, long tong, long conNo) {
    }

    public record GiaoDichData(LocalDateTime thoiGian, String phuongThuc, long soTien, String trangThai) {
    }
}
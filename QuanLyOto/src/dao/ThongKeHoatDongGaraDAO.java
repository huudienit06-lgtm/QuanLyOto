package dao;

import connectDB.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ThongKeHoatDongGaraDAO {
    private static final String SQL_THONG_KE = """
            SELECT
                (SELECT COUNT(*)
                 FROM PhieuTiepNhan
                 WHERE trangThai <> N'Đã hủy') AS xeDaTiepNhan,
                (SELECT COUNT(*)
                 FROM PhieuTiepNhan ptn
                 WHERE ptn.trangThai IN (N'Đã tiếp nhận', N'Đang kiểm tra')
                   AND NOT EXISTS (
                       SELECT 1
                       FROM PhieuKiemTra pkt
                       WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
                   )) AS xeChoKiemTra,
                (SELECT COUNT(*)
                 FROM PhieuSuaChua
                 WHERE trangThai = N'Chờ xử lý') AS xeChoSuaChua,
                (SELECT COUNT(*)
                 FROM PhieuSuaChua psc
                 WHERE psc.trangThai = N'Hoàn thành'
                   AND (
                       NOT EXISTS (
                           SELECT 1
                           FROM HoaDon hd
                           WHERE hd.maPhieuSuaChua = psc.maPhieuSuaChua
                       )
                       OR EXISTS (
                           SELECT 1
                           FROM HoaDon hd
                           WHERE hd.maPhieuSuaChua = psc.maPhieuSuaChua
                             AND hd.trangThai <> N'Đã thanh toán'
                       )
                   )) AS xeChoThanhToan
            """;

    private static final String SQL_THONG_KE_QUAN_LY = """
            SELECT
                (SELECT COUNT(DISTINCT CASE
                            WHEN ll.trangThai = N'Có mặt' THEN ll.maNV
                        END)
                 FROM LichLamViec ll
                 JOIN CaLamViec ca ON ca.maCa = ll.maCa
                 WHERE ll.ngayLamViec = CAST(GETDATE() AS DATE)
                   AND CAST(GETDATE() AS TIME) >= ca.gioBatDau
                   AND CAST(GETDATE() AS TIME) < ca.gioKetThuc) AS nhanVienCoMat,
                (SELECT COUNT(DISTINCT ll.maNV)
                 FROM LichLamViec ll
                 JOIN CaLamViec ca ON ca.maCa = ll.maCa
                 WHERE ll.ngayLamViec = CAST(GETDATE() AS DATE)
                   AND CAST(GETDATE() AS TIME) >= ca.gioBatDau
                   AND CAST(GETDATE() AS TIME) < ca.gioKetThuc) AS nhanVienDuocPhanCong,
                (SELECT COUNT(*)
                 FROM PhieuTiepNhan
                 WHERE ngayLap >= CAST(GETDATE() AS DATE)
                   AND ngayLap < DATEADD(DAY, 1, CAST(GETDATE() AS DATE))) AS phieuTiepNhanHomNay,
                (SELECT COUNT(DISTINCT maXe)
                 FROM PhieuSuaChua
                 WHERE trangThai = N'Đang sửa chữa') AS xeDangSuaChua,
                (SELECT COUNT(*)
                 FROM HoaDon) AS tongHoaDon
            """;

    public ThongKeHoatDong layThongKe() throws SQLException {
        try (PreparedStatement statement = moStatement(SQL_THONG_KE);
                ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException("Không nhận được kết quả thống kê hoạt động gara.");
            }
            return new ThongKeHoatDong(
                    resultSet.getInt("xeDaTiepNhan"),
                    resultSet.getInt("xeChoKiemTra"),
                    resultSet.getInt("xeChoSuaChua"),
                    resultSet.getInt("xeChoThanhToan"));
        }
    }

    public ThongKeQuanLy layThongKeQuanLy() throws SQLException {
        try (PreparedStatement statement = moStatement(SQL_THONG_KE_QUAN_LY);
                ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException("Không nhận được kết quả thống kê dành cho quản lý.");
            }
            return new ThongKeQuanLy(
                    resultSet.getInt("nhanVienCoMat"),
                    resultSet.getInt("nhanVienDuocPhanCong"),
                    resultSet.getInt("phieuTiepNhanHomNay"),
                    resultSet.getInt("xeDangSuaChua"),
                    resultSet.getInt("tongHoaDon"));
        }
    }

    private PreparedStatement moStatement(String sql) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu để tải thống kê hoạt động gara.");
        }
        return connection.prepareStatement(sql);
    }

    public record ThongKeHoatDong(
            int xeDaTiepNhan,
            int xeChoKiemTra,
            int xeChoSuaChua,
            int xeChoThanhToan) {
    }

    public record ThongKeQuanLy(
            int nhanVienCoMat,
            int nhanVienDuocPhanCong,
            int phieuTiepNhanHomNay,
            int xeDangSuaChua,
            int tongHoaDon) {
    }

}

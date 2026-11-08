package dao;

import connectDB.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO của màn hình Thống kê doanh thu: chỉ đọc dữ liệu từ SQL Server và đổ vào
 * các Entity NhanVien, KhachHang, HoaDon.
 */
public class ThongKeDoanhThuDAO {

    // Nhân viên có lập hóa đơn
    private static final String SQL_NHAN_VIEN = "SELECT DISTINCT nv.maNV, nv.hoTen " +
            "FROM NhanVien nv " +
            "INNER JOIN HoaDon hd ON nv.maNV = hd.maNV " +
            "WHERE nv.maNV IS NOT NULL " +
            "ORDER BY nv.maNV";

    // Danh sách khách hàng
    private static final String SQL_KHACH_HANG = "SELECT maKH, hoTen, soDT FROM KhachHang ORDER BY maKH";

    // Hóa đơn kèm khách hàng (qua phiếu sửa chữa)
    private static final String SQL_HOA_DON = "SELECT hd.maHoaDon, hd.ngayLap, hd.maNV, hd.soTien, " +
            "       psc.maKH AS maKH, " +
            "       kh.hoTen AS tenKhachHang, " +
            "       kh.soDT AS soDT " +
            "FROM HoaDon hd " +
            "LEFT JOIN PhieuSuaChua psc ON hd.maPhieuSuaChua = psc.maPhieuSuaChua " +
            "LEFT JOIN KhachHang kh ON psc.maKH = kh.maKH " +
            "ORDER BY hd.ngayLap DESC";

    // Tổng tiền đã thu thành công của từng hóa đơn
    private static final String SQL_TONG_DA_THU = "SELECT maHoaDon, SUM(soTienThanhToan) AS tongDaThu " +
            "FROM GiaoDichThanhToan " +
            "WHERE trangThaiGD = N'Thành công' " +
            "GROUP BY maHoaDon";

    // Lịch sử giao dịch của một hóa đơn
    private static final String SQL_GIAO_DICH = "SELECT thoiGianGD, phuongThucThanhToan, soTienThanhToan, trangThaiGD "
            + "FROM GiaoDichThanhToan WHERE maHoaDon = ? ORDER BY thoiGianGD";

    private Connection layKetNoi() throws SQLException {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối SQL Server.");
        }
        return con;
    }

    private static long laySo(BigDecimal so) {
        return so == null ? 0L : so.longValue();
    }

    // Nhân viên có lập hóa đơn
    public List<NhanVien> layDanhSachNhanVien() throws SQLException {
        List<NhanVien> ds = new ArrayList<>();
        Connection con = layKetNoi();
        try (PreparedStatement ps = con.prepareStatement(SQL_NHAN_VIEN);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ten = rs.getString("hoTen");
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(ten == null ? "" : ten);
                ds.add(nv);
            }
        }
        return ds;
    }

    public List<KhachHang> layDanhSachKhachHang() throws SQLException {
        List<KhachHang> ds = new ArrayList<>();
        Connection con = layKetNoi();
        try (PreparedStatement ps = con.prepareStatement(SQL_KHACH_HANG);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sdt = rs.getString("soDT");
                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                kh.setHoTen(rs.getString("hoTen"));
                kh.setSoDT(sdt == null ? "" : sdt);
                ds.add(kh);
            }
        }
        return ds;
    }

    // Hóa đơn (bỏ qua hóa đơn không có ngày lập). Khách hàng null nếu hóa đơn chưa
    // gắn khách.
    public List<HoaDon> layDanhSachHoaDon() throws SQLException {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = layKetNoi();
        try (PreparedStatement ps = con.prepareStatement(SQL_HOA_DON);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ngayLap = rs.getTimestamp("ngayLap");
                if (ngayLap == null) {
                    continue;
                }
                HoaDon hd = new HoaDon();
                hd.setMaHoaDon(rs.getString("maHoaDon"));
                hd.setNgayLap(ngayLap);
                hd.setSoTien(rs.getBigDecimal("soTien"));

                String maNV = rs.getString("maNV");
                if (maNV != null) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(maNV);
                    hd.setNhanVien(nv);
                }

                String maKH = rs.getString("maKH");
                if (maKH != null) {
                    String sdt = rs.getString("soDT");
                    KhachHang kh = new KhachHang();
                    kh.setMaKH(maKH);
                    kh.setHoTen(rs.getString("tenKhachHang"));
                    kh.setSoDT(sdt == null ? "" : sdt);
                    hd.setKhachHang(kh);
                }
                ds.add(hd);
            }
        }
        return ds;
    }

    // maHoaDon -> tổng tiền đã thu thành công
    public Map<String, Long> layTongDaThuTheoHoaDon() throws SQLException {
        Map<String, Long> ds = new HashMap<>();
        Connection con = layKetNoi();
        try (PreparedStatement ps = con.prepareStatement(SQL_TONG_DA_THU);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.put(rs.getString("maHoaDon"), laySo(rs.getBigDecimal("tongDaThu")));
            }
        }
        return ds;
    }

    // Mỗi dòng: {Timestamp thoiGian, String phuongThuc, Long soTien, String
    // trangThai}
    public List<Object[]> layLichSuGiaoDich(String maHoaDon) throws SQLException {
        List<Object[]> ds = new ArrayList<>();
        Connection con = layKetNoi();
        try (PreparedStatement ps = con.prepareStatement(SQL_GIAO_DICH)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[] {
                            rs.getTimestamp("thoiGianGD"),
                            rs.getString("phuongThucThanhToan"),
                            laySo(rs.getBigDecimal("soTienThanhToan")),
                            rs.getString("trangThaiGD") });
                }
            }
        }
        return ds;
    }
}
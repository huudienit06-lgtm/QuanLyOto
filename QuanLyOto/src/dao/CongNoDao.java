package dao;

import connectDB.ConnectDB;
import entity.CongNo;
import entity.KhachHang;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CongNoDao {
    // Danh sách công nợ theo khách hàng
    private static final String SQL_CONG_NO = "SELECT kh.maKH, kh.hoTen, kh.soDT, " +
            "ISNULL(cn.tongNo, 0) AS tongNo, ISNULL(cn.soHDNo, 0) AS soHDNo, " +
            "cn.ngayCapNhat AS ngayCapNhat " +
            "FROM KhachHang kh " +
            "OUTER APPLY (SELECT " +
            " SUM(CASE WHEN hd.soTien-ISNULL(tt.daTra,0)>0 " +
            " THEN hd.soTien-ISNULL(tt.daTra,0) ELSE 0 END) AS tongNo, " +
            " SUM(CASE WHEN hd.maHoaDon IS NOT NULL AND hd.soTien-ISNULL(tt.daTra,0)>0 " +
            " THEN 1 ELSE 0 END) AS soHDNo, MAX(hd.ngayLap) AS ngayCapNhat " +
            " FROM PhieuSuaChua p JOIN HoaDon hd ON hd.maPhieuSuaChua=p.maPhieuSuaChua " +
            " OUTER APPLY (SELECT SUM(g.soTienThanhToan) AS daTra FROM GiaoDichThanhToan g " +
            " WHERE g.maHoaDon=hd.maHoaDon " +
            " AND (g.trangThaiGD IS NULL OR g.trangThaiGD=N'Thành công')) tt " +
            " WHERE p.maKH=kh.maKH) cn " +
            "WHERE ISNULL(cn.tongNo, 0) > 0 " +
            "AND (? = '' OR kh.maKH LIKE ? OR kh.hoTen LIKE ? OR kh.soDT LIKE ? " +
            " OR EXISTS (SELECT 1 FROM PhieuSuaChua p2 JOIN HoaDon h2 ON h2.maPhieuSuaChua=p2.maPhieuSuaChua " +
            " WHERE p2.maKH=kh.maKH AND h2.maHoaDon LIKE ?)) " +
            "AND (? = N'Tất cả' OR (? = N'Còn nợ' AND ISNULL(cn.tongNo,0) > 0) " +
            " OR (? = N'Đã thanh toán' AND ISNULL(cn.tongNo,0) <= 0)) " +
            "AND (? = N'Tất cả' OR EXISTS (SELECT 1 FROM PhieuSuaChua p3 " +
            " JOIN HoaDon h3 ON h3.maPhieuSuaChua=p3.maPhieuSuaChua WHERE p3.maKH=kh.maKH " +
            " AND ((? = N'Hôm nay' AND CAST(h3.ngayLap AS DATE)=CAST(GETDATE() AS DATE)) " +
            " OR (? = N'7 ngày gần nhất' AND h3.ngayLap >= DATEADD(DAY,-7,GETDATE())) " +
            " OR (? = N'30 ngày gần nhất' AND h3.ngayLap >= DATEADD(DAY,-30,GETDATE()))))) " +
            "ORDER BY cn.ngayCapNhat DESC, kh.maKH";

    // Thông tin một khách hàng
    private static final String SQL_KHACH_HANG = "SELECT maKH, hoTen, soDT, diaChi FROM KhachHang WHERE maKH = ?";

    // Hóa đơn còn nợ của một khách hàng
    private static final String SQL_HOA_DON_NO = "SELECT hd.maHoaDon, hd.ngayLap, hd.soTien, ISNULL(tt.daTra,0) AS daTra, "
            + "hd.soTien-ISNULL(tt.daTra,0) AS conPhaiTra "
            + "FROM PhieuSuaChua psc JOIN HoaDon hd ON hd.maPhieuSuaChua=psc.maPhieuSuaChua "
            + "OUTER APPLY (SELECT SUM(g.soTienThanhToan) daTra FROM GiaoDichThanhToan g "
            + "WHERE g.maHoaDon=hd.maHoaDon AND (g.trangThaiGD IS NULL OR g.trangThaiGD=N'Thành công')) tt "
            + "WHERE psc.maKH=? AND hd.soTien-ISNULL(tt.daTra,0)>0 ORDER BY hd.ngayLap";

    private Connection layKetNoi() throws SQLException {
        ConnectDB.getInstance().connect();
        return ConnectDB.getConnection();
    }

    // Lấy danh sách công nợ theo từ khóa, khoảng nợ, trạng thái, thời gian phát
    // sinh
    public List<CongNo> layDanhSachCongNo(String tuKhoa, String nhomNo, String trangThai, String thoiGian)
            throws SQLException {
        List<CongNo> dsCongNo = new ArrayList<>();
        String like = "%" + tuKhoa + "%";

        try (Connection con = layKetNoi(); PreparedStatement ps = con.prepareStatement(SQL_CONG_NO)) {
            int i = 1;
            ps.setString(i++, tuKhoa);
            for (int k = 0; k < 4; k++) {
                ps.setString(i++, like);
            }
            for (int k = 0; k < 3; k++) {
                ps.setString(i++, trangThai);
            }
            for (int k = 0; k < 4; k++) {
                ps.setString(i++, thoiGian);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal no = nz(rs.getBigDecimal("tongNo"));
                    if (!thuocKhoangNo(no, nhomNo)) {
                        continue;
                    }
                    KhachHang kh = new KhachHang(rs.getString("maKH"));
                    kh.setHoTen(rs.getString("hoTen"));
                    kh.setSoDT(rs.getString("soDT"));
                    dsCongNo.add(new CongNo(kh, no, rs.getInt("soHDNo"),
                            toLocal(rs.getTimestamp("ngayCapNhat"))));
                }
            }
        }
        return dsCongNo;
    }

    // Lấy thông tin một khách hàng (không có thì trả về null)
    public KhachHang layKhachHang(String maKH) throws SQLException {
        try (Connection con = layKetNoi(); PreparedStatement ps = con.prepareStatement(SQL_KHACH_HANG)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KhachHang kh = new KhachHang(rs.getString("maKH"), rs.getString("hoTen"),
                            rs.getString("soDT"));
                    kh.setDiaChi(rs.getString("diaChi"));
                    return kh;
                }
            }
        }
        return null;
    }

    // Lấy các hóa đơn còn nợ của một khách hàng
    public List<CongNo.HoaDonNo> layHoaDonNo(KhachHang khachHang) throws SQLException {
        List<CongNo.HoaDonNo> dsHoaDon = new ArrayList<>();
        try (Connection con = layKetNoi(); PreparedStatement ps = con.prepareStatement(SQL_HOA_DON_NO)) {
            ps.setString(1, khachHang.getMaKH());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsHoaDon.add(new CongNo.HoaDonNo(
                            rs.getString("maHoaDon"), khachHang, toLocal(rs.getTimestamp("ngayLap")),
                            nz(rs.getBigDecimal("soTien")), nz(rs.getBigDecimal("daTra")),
                            nz(rs.getBigDecimal("conPhaiTra"))));
                }
            }
        }
        return dsHoaDon;
    }

    // Lọc theo khoảng nợ (chỉ áp dụng cho khách còn nợ): <1tr, 1-5tr, 5-10tr,
    // >=10tr
    private boolean thuocKhoangNo(BigDecimal no, String nhomNo) {
        if (CongNo.TAT_CA.equals(nhomNo)) {
            return true;
        }
        if (no.signum() <= 0) {
            return false;
        }
        BigDecimal mot = new BigDecimal("1000000");
        BigDecimal nam = mot.multiply(BigDecimal.valueOf(5));
        BigDecimal muoi = mot.multiply(BigDecimal.TEN);
        switch (nhomNo) {
            case "Dưới 1 triệu":
                return no.compareTo(mot) < 0;
            case "1 - 5 triệu":
                return no.compareTo(mot) >= 0 && no.compareTo(nam) < 0;
            case "5 - 10 triệu":
                return no.compareTo(nam) >= 0 && no.compareTo(muoi) < 0;
            case "Trên 10 triệu":
                return no.compareTo(muoi) >= 0;
            default:
                return true;
        }
    }

    private static BigDecimal nz(BigDecimal so) {
        return so == null ? BigDecimal.ZERO : so;
    }

    private static LocalDateTime toLocal(Timestamp thoiGian) {
        return thoiGian == null ? null : thoiGian.toLocalDateTime();
    }
}
package dao;

import entity.PhieuNhapXuatKho;
import entity.ChiTietPhieuNhap;
import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PhieuNhapXuatKhoDAO extends BaseDAO<PhieuNhapXuatKho> {
    
    public PhieuNhapXuatKhoDAO() {
        super(PhieuNhapXuatKho.class, "PhieuNhapXuatKho", "maPhieuNhapXuat");
    }

    public boolean luuGiaoDichKho(PhieuNhapXuatKho phieu, List<ChiTietPhieuNhap> chiTietList, boolean isNhapKho) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            if (con == null) {
                throw new SQLException("Không thể kết nối cơ sở dữ liệu để lưu giao dịch kho.");
            }
            con.setAutoCommit(false);

            try (PreparedStatement stmtChiTiet = con.prepareStatement(
                    "INSERT INTO ChiTietPhieuNhap "
                            + "(maPhieuNhapXuat, maPhuTung, soLuong, donGia, ghiChu) VALUES (?, ?, ?, ?, ?)");
                    PreparedStatement stmtCapNhatTon = con.prepareStatement(
                            "UPDATE PhuTung SET soLuongTon = soLuongTon "
                                    + (isNhapKho ? "+" : "-") + " ? WHERE maPhuTung = ?")) {
                if (!insert(con, phieu)) {
                    throw new SQLException("Không thể lưu thông tin phiếu nhập xuất kho.");
                }

                for (ChiTietPhieuNhap ct : chiTietList) {
                    stmtChiTiet.setString(1, ct.getMaPhieuNhapXuat());
                    stmtChiTiet.setString(2, ct.getMaPhuTung());
                    stmtChiTiet.setInt(3, ct.getSoLuong());
                    stmtChiTiet.setDouble(4, ct.getDonGia());
                    stmtChiTiet.setString(5, ct.getGhiChu());
                    stmtChiTiet.addBatch();

                    stmtCapNhatTon.setInt(1, ct.getSoLuong());
                    stmtCapNhatTon.setString(2, ct.getMaPhuTung());
                    stmtCapNhatTon.addBatch();
                }

                stmtChiTiet.executeBatch();
                stmtCapNhatTon.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
package dao;

import entity.PhieuNhapXuatKho;
import entity.ChiTietPhieuNhap;
import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapXuatKhoDAO extends BaseDAO<PhieuNhapXuatKho> {
    
    private final ChiTietPhieuNhapDAO chiTietDAO = new ChiTietPhieuNhapDAO();

    public PhieuNhapXuatKhoDAO() {
        super(PhieuNhapXuatKho.class, "PhieuNhapXuatKho", "maPhieuNhapXuat");
    }

    public List<PhieuNhapXuatKho> layTatCaPhieu() {
        try {
            return findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<ChiTietPhieuNhap> layChiTietPhieu(String maPhieuNhapXuat) {
        try {
            return chiTietDAO.findByField("maPhieuNhapXuat", maPhieuNhapXuat);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public double tinhTongTienPhieu(String maPhieuNhapXuat) {
        List<ChiTietPhieuNhap> ds = layChiTietPhieu(maPhieuNhapXuat);
        double tongTien = 0;
        if (ds != null) {
            for (ChiTietPhieuNhap ct : ds) {
                if (ct.getSoLuong() != null && ct.getDonGia() != null) {
                    tongTien += ct.getSoLuong() * ct.getDonGia();
                }
            }
        }
        return tongTien;
    }

    public boolean xoaPhieu(String maPhieuNhapXuat) {
        try {
            List<ChiTietPhieuNhap> dsChiTiet = layChiTietPhieu(maPhieuNhapXuat);
            for (ChiTietPhieuNhap ct : dsChiTiet) {
                chiTietDAO.deleteById(ct.getMaPhieuNhapXuat(), ct.getMaPhuTung());
            }
            return deleteById(maPhieuNhapXuat);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean luuGiaoDichKho(PhieuNhapXuatKho phieu, List<ChiTietPhieuNhap> chiTietList, boolean isNhapKho) {
        Connection con = null;
        try {
            ConnectDB.getInstance().connect();
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

            if (con == null || con.isClosed()) return false;

            con.setAutoCommit(false);

            if (!insert(con, phieu)) {
                throw new SQLException("Không thể lưu thông tin phiếu.");
            }

            String sqlChiTiet = "INSERT INTO ChiTietPhieuNhap (maPhieuNhapXuat, maPhuTung, soLuong, donGia, ghiChu) VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateTon = "UPDATE PhuTung SET soLuongTon = soLuongTon " + (isNhapKho ? "+" : "-") + " ? WHERE maPhuTung = ?";

            try (PreparedStatement stmtChiTiet = con.prepareStatement(sqlChiTiet);
                 PreparedStatement stmtTon = con.prepareStatement(sqlUpdateTon)) {
                
                for (ChiTietPhieuNhap ct : chiTietList) {
                    stmtChiTiet.setString(1, ct.getMaPhieuNhapXuat());
                    stmtChiTiet.setString(2, ct.getMaPhuTung());
                    stmtChiTiet.setInt(3, ct.getSoLuong());
                    stmtChiTiet.setDouble(4, ct.getDonGia());
                    stmtChiTiet.setString(5, ct.getGhiChu() != null ? ct.getGhiChu() : "");
                    stmtChiTiet.addBatch();

                    stmtTon.setInt(1, ct.getSoLuong());
                    stmtTon.setString(2, ct.getMaPhuTung());
                    stmtTon.addBatch();
                }

                stmtChiTiet.executeBatch();
                stmtTon.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();
                }
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
                if (con != null && !con.isClosed()) con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
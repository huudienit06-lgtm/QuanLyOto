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
        PreparedStatement stmtPhieu = null;
        PreparedStatement stmtChiTiet = null;
        PreparedStatement stmtCapNhatTon = null;

        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bật chế độ Transaction (Tắt tự động lưu)

            // 1. LƯU THÔNG TIN PHIẾU
            String sqlPhieu = "INSERT INTO PhieuNhapXuatKho (maPhieuNhapXuat, maNV, ngayLapPhieu, kieuPhieu) VALUES (?, ?, ?, ?)";
            stmtPhieu = con.prepareStatement(sqlPhieu);
            stmtPhieu.setString(1, phieu.getMaPhieuNhapXuat());
            stmtPhieu.setString(2, phieu.getMaNV());
            stmtPhieu.setTimestamp(3, phieu.getNgayLapPhieu());
            stmtPhieu.setString(4, phieu.getKieuPhieu());
            stmtPhieu.executeUpdate();

            // 2. LƯU CHI TIẾT VÀ CẬP NHẬT TỒN KHO
            String sqlChiTiet = "INSERT INTO ChiTietPhieuNhap (maPhieuNhapXuat, maPhuTung, soLuong, donGia, ghiChu) VALUES (?, ?, ?, ?, ?)";
            stmtChiTiet = con.prepareStatement(sqlChiTiet);

            // Xác định phép toán (Nhập là cộng kho, Xuất là trừ kho)
            String toanTu = isNhapKho ? "+" : "-";
            String sqlCapNhatTon = "UPDATE PhuTung SET soLuongTon = soLuongTon " + toanTu + " ? WHERE maPhuTung = ?";
            stmtCapNhatTon = con.prepareStatement(sqlCapNhatTon);

            for (ChiTietPhieuNhap ct : chiTietList) {
                // Thêm vào hàng đợi (Batch) của Chi Tiết
                stmtChiTiet.setString(1, ct.getMaPhieuNhapXuat());
                stmtChiTiet.setString(2, ct.getMaPhuTung());
                stmtChiTiet.setInt(3, ct.getSoLuong());
                stmtChiTiet.setDouble(4, ct.getDonGia());
                stmtChiTiet.setString(5, ct.getGhiChu());
                stmtChiTiet.addBatch();

                // Thêm vào hàng đợi (Batch) của Cập nhật tồn kho
                stmtCapNhatTon.setInt(1, ct.getSoLuong());
                stmtCapNhatTon.setString(2, ct.getMaPhuTung());
                stmtCapNhatTon.addBatch();
            }

            // Thực thi lưu hàng loạt cho tốc độ nhanh nhất
            stmtChiTiet.executeBatch();
            stmtCapNhatTon.executeBatch();

            // 3. XÁC NHẬN LƯU VĨNH VIỄN
            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback(); // Hoàn tác 100% nếu có bất kỳ lỗi gì
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (con != null) con.setAutoCommit(true); // Trả lại AutoCommit cho các hàm BaseDAO khác
                if (stmtPhieu != null) stmtPhieu.close();
                if (stmtChiTiet != null) stmtChiTiet.close();
                if (stmtCapNhatTon != null) stmtCapNhatTon.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
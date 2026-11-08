package dao;

import entity.NhanVien;
import entity.PhieuNhapXuatKho;
import entity.ChiTietPhieuNhap;
import entity.PhuTung;
import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.MaTuDong;

public class PhieuNhapXuatKhoDAO extends BaseDAO<PhieuNhapXuatKho> {

    private final ChiTietPhieuNhapDAO chiTietDAO = new ChiTietPhieuNhapDAO();

    public PhieuNhapXuatKhoDAO() {
        super(PhieuNhapXuatKho.class, "PhieuNhapXuatKho", "maPhieuNhapXuat");
    }

    // --- OVERRIDE CÁC HÀM CỦA BASEDAO ĐỂ TRÁNH LỖI REFLECTION KHI DÙNG OBJECT ---
    @Override
    public List<PhieuNhapXuatKho> findAll() throws SQLException {
        List<PhieuNhapXuatKho> list = new ArrayList<>();
        String sql = "SELECT * FROM PhieuNhapXuatKho";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                PhieuNhapXuatKho phieu = new PhieuNhapXuatKho();
                phieu.setMaPhieuNhapXuat(rs.getString("maPhieuNhapXuat"));
                phieu.setNhanVien(new NhanVien(rs.getString("maNV")));
                phieu.setNgayLapPhieu(rs.getTimestamp("ngayLapPhieu"));
                phieu.setKieuPhieu(rs.getString("kieuPhieu"));
                list.add(phieu);
            }
        }
        return list;
    }

    // Ghi đè hàm insert truyền vào Connection để dùng SQL thuần
    public boolean insert(Connection con, PhieuNhapXuatKho phieu) throws SQLException {
        String sql = "INSERT INTO PhieuNhapXuatKho (maPhieuNhapXuat, maNV, ngayLapPhieu, kieuPhieu) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, phieu.getMaPhieuNhapXuat());
            stmt.setString(2, phieu.getNhanVien() != null ? phieu.getNhanVien().getMaNV() : null);
            stmt.setTimestamp(3, phieu.getNgayLapPhieu());
            stmt.setString(4, phieu.getKieuPhieu());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<PhieuNhapXuatKho> layTatCaPhieu() {
        try {
            return findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String taoMaPhieuMoi(boolean isNhapKho) throws SQLException {
        return MaTuDong.sinhMa(
                isNhapKho ? MaTuDong.TIEN_TO_PHIEU_NHAP_KHO : MaTuDong.TIEN_TO_PHIEU_XUAT_KHO,
                findAll(),
                PhieuNhapXuatKho::getMaPhieuNhapXuat);
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
                chiTietDAO.deleteById(ct.getPhieuNhapXuatKho().getMaPhieuNhapXuat(), ct.getPhuTung().getMaPhuTung());
            }
            return deleteById(maPhieuNhapXuat);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean luuGiaoDichKho(PhieuNhapXuatKho phieu, List<ChiTietPhieuNhap> chiTietList, boolean isNhapKho) throws SQLException {
        validateChiTiet(chiTietList);
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu để lưu giao dịch kho.");
        }
        boolean autoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            if (!insert(con, phieu)) {
                throw new SQLException("Không thể lưu thông tin phiếu nhập xuất kho.");
            }
            for (ChiTietPhieuNhap chiTiet : chiTietList) {
                insertChiTiet(con, chiTiet);
                capNhatTonKho(con, chiTiet.getPhuTung().getMaPhuTung(), (isNhapKho ? 1 : -1) * chiTiet.getSoLuong());
            }
            con.commit();
            return true;
        } catch (SQLException exception) {
            try { con.rollback(); } catch (SQLException rollbackException) { exception.addSuppressed(rollbackException); }
            throw exception;
        } finally {
            try { con.setAutoCommit(autoCommit); } 
            catch (SQLException restoreException) { if (con.getAutoCommit() != autoCommit) throw restoreException; }
        }
    }

    public boolean capNhatGiaoDichKho(String maPhieuNhapXuat, List<ChiTietPhieuNhap> chiTietMoi, boolean isNhapKho) throws SQLException {
        validateChiTiet(chiTietMoi);
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu để cập nhật giao dịch kho.");
        }
        boolean autoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            boolean isNhapCu = layLoaiPhieu(con, maPhieuNhapXuat);
            List<ChiTietPhieuNhap> chiTietCu = layChiTietPhieu(con, maPhieuNhapXuat);
            Map<String, Integer> thayDoiTonKho = new HashMap<>();
            
            for (ChiTietPhieuNhap chiTiet : chiTietCu) {
                themThayDoiTonKho(thayDoiTonKho, chiTiet.getPhuTung().getMaPhuTung(), (isNhapCu ? -1 : 1) * chiTiet.getSoLuong());
            }
            for (ChiTietPhieuNhap chiTiet : chiTietMoi) {
                themThayDoiTonKho(thayDoiTonKho, chiTiet.getPhuTung().getMaPhuTung(), (isNhapKho ? 1 : -1) * chiTiet.getSoLuong());
            }
            for (Map.Entry<String, Integer> thayDoi : thayDoiTonKho.entrySet()) {
                capNhatTonKho(con, thayDoi.getKey(), thayDoi.getValue());
            }

            try (PreparedStatement statement = con.prepareStatement("DELETE FROM ChiTietPhieuNhap WHERE maPhieuNhapXuat = ?")) {
                statement.setString(1, maPhieuNhapXuat);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = con.prepareStatement("UPDATE PhieuNhapXuatKho SET kieuPhieu = ? WHERE maPhieuNhapXuat = ?")) {
                statement.setString(1, isNhapKho ? "Nhập" : "Xuất");
                statement.setString(2, maPhieuNhapXuat);
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Không tìm thấy phiếu cần cập nhật: " + maPhieuNhapXuat);
                }
            }
            for (ChiTietPhieuNhap chiTiet : chiTietMoi) {
                chiTiet.setPhieuNhapXuatKho(new PhieuNhapXuatKho(maPhieuNhapXuat));
                insertChiTiet(con, chiTiet);
            }
            con.commit();
            return true;
        } catch (SQLException exception) {
            try { con.rollback(); } catch (SQLException rollbackException) { exception.addSuppressed(rollbackException); }
            throw exception;
        } finally {
            try { con.setAutoCommit(autoCommit); } 
            catch (SQLException restoreException) { if (con.getAutoCommit() != autoCommit) throw restoreException; }
        }
    }

    private boolean layLoaiPhieu(Connection con, String maPhieuNhapXuat) throws SQLException {
        try (PreparedStatement statement = con.prepareStatement("SELECT kieuPhieu FROM PhieuNhapXuatKho WHERE maPhieuNhapXuat = ?")) {
            statement.setString(1, maPhieuNhapXuat);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new SQLException("Không tìm thấy phiếu cần cập nhật: " + maPhieuNhapXuat);
                String kieuPhieu = resultSet.getString("kieuPhieu");
                if ("Nhập".equalsIgnoreCase(kieuPhieu)) return true;
                if ("Xuất".equalsIgnoreCase(kieuPhieu)) return false;
                throw new SQLException("Loại phiếu không hợp lệ: " + kieuPhieu);
            }
        }
    }

    private List<ChiTietPhieuNhap> layChiTietPhieu(Connection con, String maPhieuNhapXuat) throws SQLException {
        List<ChiTietPhieuNhap> danhSach = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(
                "SELECT maPhieuNhapXuat, maPhuTung, soLuong, donGia, ghiChu FROM ChiTietPhieuNhap WHERE maPhieuNhapXuat = ?")) {
            statement.setString(1, maPhieuNhapXuat);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ChiTietPhieuNhap chiTiet = new ChiTietPhieuNhap();
                    chiTiet.setPhieuNhapXuatKho(new PhieuNhapXuatKho(resultSet.getString("maPhieuNhapXuat")));
                    chiTiet.setPhuTung(new PhuTung(resultSet.getString("maPhuTung")));
                    chiTiet.setSoLuong(resultSet.getInt("soLuong"));
                    chiTiet.setDonGia(resultSet.getDouble("donGia"));
                    chiTiet.setGhiChu(resultSet.getString("ghiChu"));
                    danhSach.add(chiTiet);
                }
            }
        }
        return danhSach;
    }

    private void insertChiTiet(Connection con, ChiTietPhieuNhap chiTiet) throws SQLException {
        try (PreparedStatement statement = con.prepareStatement(
                "INSERT INTO ChiTietPhieuNhap (maPhieuNhapXuat, maPhuTung, soLuong, donGia, ghiChu) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, chiTiet.getPhieuNhapXuatKho().getMaPhieuNhapXuat());
            statement.setString(2, chiTiet.getPhuTung().getMaPhuTung());
            statement.setInt(3, chiTiet.getSoLuong());
            statement.setDouble(4, chiTiet.getDonGia());
            statement.setString(5, chiTiet.getGhiChu());
            statement.executeUpdate();
        }
    }

    private void capNhatTonKho(Connection con, String maPhuTung, int thayDoi) throws SQLException {
        if (thayDoi == 0) return;
        String sql = thayDoi < 0
                ? "UPDATE PhuTung SET soLuongTon = soLuongTon + ? WHERE maPhuTung = ? AND soLuongTon >= ?"
                : "UPDATE PhuTung SET soLuongTon = soLuongTon + ? WHERE maPhuTung = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, thayDoi);
            statement.setString(2, maPhuTung);
            if (thayDoi < 0) statement.setInt(3, -thayDoi);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Không đủ tồn kho hoặc không tìm thấy phụ tùng " + maPhuTung);
            }
        }
    }

    private void themThayDoiTonKho(Map<String, Integer> thayDoiTonKho, String maPhuTung, int thayDoi) {
        thayDoiTonKho.merge(maPhuTung, thayDoi, Integer::sum);
    }

    private void validateChiTiet(List<ChiTietPhieuNhap> chiTietList) throws SQLException {
        if (chiTietList == null || chiTietList.isEmpty()) {
            throw new SQLException("Phiếu giao dịch phải có ít nhất một phụ tùng.");
        }
        for (ChiTietPhieuNhap chiTiet : chiTietList) {
            if (chiTiet.getPhuTung() == null || chiTiet.getPhuTung().getMaPhuTung() == null || chiTiet.getPhuTung().getMaPhuTung().isBlank()
                    || chiTiet.getSoLuong() == null || chiTiet.getSoLuong() <= 0
                    || chiTiet.getDonGia() == null || chiTiet.getDonGia() <= 0) {
                throw new SQLException("Thông tin chi tiết phụ tùng không hợp lệ.");
            }
        }
    }
}
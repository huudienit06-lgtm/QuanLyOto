package dao;

import connectDB.ConnectDB;
import entity.PhuTung;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhuTungDAO { 

    public PhuTungDAO() {
    }
    public List<PhuTung> layTatCaPhuTung() {
        try {
            return findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public boolean themPhuTung(PhuTung pt) {
        try { return insert(pt); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatPhuTung(PhuTung pt) {
        try { return update(pt); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean xoaPhuTung(String maPhuTung) {
        try { return deleteById(maPhuTung); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public PhuTung layPhuTungTheoMa(String maPT) {
        try { return findById(maPT); } catch (SQLException e) { e.printStackTrace(); return null; }
    }

    public List<String> layDanhSachNhaCungCap() {
        List<String> dsNCC = new ArrayList<>();
        String sql = "SELECT DISTINCT nhaCungCap FROM PhuTung WHERE nhaCungCap IS NOT NULL AND nhaCungCap != ''";
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                dsNCC.add(rs.getString("nhaCungCap"));
            }
            rs.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsNCC;
    }

    // -- CÁC HÀM GỐC TỪ BASEDAO ĐƯỢC MANG VÀO ĐÂY --

    public List<PhuTung> findAll() throws SQLException {
        List<PhuTung> ds = new ArrayList<>();
        String sql = "SELECT * FROM PhuTung";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        }
        return ds;
    }

    public PhuTung findById(Object... keyValues) throws SQLException {
        String sql = "SELECT * FROM PhuTung WHERE maPhuTung = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setObject(1, keyValues[0]);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean insert(PhuTung pt) throws SQLException {
        String sql = "INSERT INTO PhuTung (maPhuTung, tenPhuTung, donViTinh, giaNhap, giaBan, soLuongTon, nhaCungCap, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, pt.getMaPhuTung());
            stmt.setString(2, pt.getTenPhuTung());
            stmt.setString(3, pt.getDonViTinh());
            stmt.setDouble(4, pt.getGiaNhap());
            stmt.setDouble(5, pt.getGiaBan());
            stmt.setInt(6, pt.getSoLuongTon());
            stmt.setString(7, pt.getNhaCungCap());
            stmt.setString(8, pt.getTrangThai());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean update(PhuTung pt) throws SQLException {
        String sql = "UPDATE PhuTung SET tenPhuTung = ?, donViTinh = ?, giaNhap = ?, giaBan = ?, soLuongTon = ?, nhaCungCap = ?, trangThai = ? WHERE maPhuTung = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, pt.getTenPhuTung());
            stmt.setString(2, pt.getDonViTinh());
            stmt.setDouble(3, pt.getGiaNhap());
            stmt.setDouble(4, pt.getGiaBan());
            stmt.setInt(5, pt.getSoLuongTon());
            stmt.setString(6, pt.getNhaCungCap());
            stmt.setString(7, pt.getTrangThai());
            stmt.setString(8, pt.getMaPhuTung());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Object... keyValues) throws SQLException {
        String sql = "DELETE FROM PhuTung WHERE maPhuTung = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setObject(1, keyValues[0]);
            return stmt.executeUpdate() > 0;
        }
    }

    private PhuTung mapRow(ResultSet rs) throws SQLException {
        PhuTung pt = new PhuTung();
        pt.setMaPhuTung(rs.getString("maPhuTung"));
        pt.setTenPhuTung(rs.getString("tenPhuTung"));
        pt.setDonViTinh(rs.getString("donViTinh"));
        pt.setGiaNhap(rs.getDouble("giaNhap"));
        pt.setGiaBan(rs.getDouble("giaBan"));
        pt.setSoLuongTon(rs.getInt("soLuongTon"));
        pt.setNhaCungCap(rs.getString("nhaCungCap"));
        pt.setTrangThai(rs.getString("trangThai"));
        return pt;
    }
}
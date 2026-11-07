package dao;

import connectDB.ConnectDB;
import entity.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {
    
    public DichVuDAO() {
    }

    public List<DichVu> findAll() throws SQLException {
        List<DichVu> ds = new ArrayList<>();
        String sql = "SELECT * FROM DichVu";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                DichVu dv = new DichVu();
                dv.setMaDV(rs.getString("maDV"));
                dv.setTenDV(rs.getString("tenDV"));
                dv.setGiaDV(rs.getDouble("giaDV"));
                dv.setMoTaDV(rs.getString("moTaDV"));
                ds.add(dv);
            }
        }
        return ds;
    }

    public DichVu findById(Object... keyValues) throws SQLException {
        String sql = "SELECT * FROM DichVu WHERE maDV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setObject(1, keyValues[0]);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DichVu dv = new DichVu();
                    dv.setMaDV(rs.getString("maDV"));
                    dv.setTenDV(rs.getString("tenDV"));
                    dv.setGiaDV(rs.getDouble("giaDV"));
                    dv.setMoTaDV(rs.getString("moTaDV"));
                    return dv;
                }
            }
        }
        return null;
    }

    public boolean insert(DichVu dv) throws SQLException {
        String sql = "INSERT INTO DichVu (maDV, tenDV, giaDV, moTaDV) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dv.getMaDV());
            stmt.setString(2, dv.getTenDV());
            stmt.setDouble(3, dv.getGiaDV());
            stmt.setString(4, dv.getMoTaDV());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean update(DichVu dv) throws SQLException {
        String sql = "UPDATE DichVu SET tenDV = ?, giaDV = ?, moTaDV = ? WHERE maDV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dv.getTenDV());
            stmt.setDouble(2, dv.getGiaDV());
            stmt.setString(3, dv.getMoTaDV());
            stmt.setString(4, dv.getMaDV());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Object... keyValues) throws SQLException {
        String sql = "DELETE FROM DichVu WHERE maDV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setObject(1, keyValues[0]);
            return stmt.executeUpdate() > 0;
        }
    }
}
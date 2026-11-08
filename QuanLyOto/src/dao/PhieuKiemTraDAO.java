package dao;

import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuKiemTra;
import entity.PhieuTiepNhan;
import entity.Xe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuKiemTraDAO {
    public List<PhieuKiemTra> findAll() throws SQLException {
        String sql = "SELECT pkt.maPhieuKiemTra, pkt.maPhieuTiepNhan, pkt.maKH, pkt.maXe, pkt.maNV, "
                + "pkt.trangThai, pkt.trangThaiBanDau, pkt.vanDeHuHong, nv.hoTen "
                + "FROM PhieuKiemTra pkt JOIN NhanVien nv ON nv.maNV = pkt.maNV "
                + "ORDER BY pkt.maPhieuKiemTra";
        List<PhieuKiemTra> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public PhieuKiemTra findById(String maPhieuKiemTra) throws SQLException {
        String sql = "SELECT pkt.maPhieuKiemTra, pkt.maPhieuTiepNhan, pkt.maKH, pkt.maXe, pkt.maNV, "
                + "pkt.trangThai, pkt.trangThaiBanDau, pkt.vanDeHuHong, nv.hoTen "
                + "FROM PhieuKiemTra pkt JOIN NhanVien nv ON nv.maNV = pkt.maNV "
                + "WHERE pkt.maPhieuKiemTra = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuKiemTra);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public List<PhieuKiemTra> findAllDuDieuKienLapPhieuSuaChua() throws SQLException {
        String sql = "SELECT pkt.maPhieuKiemTra, pkt.maPhieuTiepNhan, pkt.maKH, pkt.maXe, pkt.maNV, "
                + "pkt.trangThai, pkt.trangThaiBanDau, pkt.vanDeHuHong, nv.hoTen "
                + "FROM PhieuKiemTra pkt JOIN PhieuTiepNhan ptn "
                + "ON ptn.maPhieuTiepNhan = pkt.maPhieuTiepNhan "
                + "JOIN NhanVien nv ON nv.maNV = pkt.maNV "
                + "WHERE pkt.trangThai = ? AND ptn.tienDo = ? "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuSuaChua psc "
                + "WHERE psc.maPhieuKiemTra = pkt.maPhieuKiemTra) "
                + "ORDER BY pkt.maPhieuKiemTra";
        List<PhieuKiemTra> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PhieuKiemTra.TRANG_THAI_DA_KIEM_TRA);
            statement.setString(2, PhieuTiepNhan.TIEN_DO_DA_LAP_PHIEU_KIEM_TRA);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(mapRow(resultSet));
                }
            }
        }
        return danhSach;
    }

    public boolean insert(PhieuKiemTra phieu) throws SQLException {
        String sql = "INSERT INTO PhieuKiemTra "
                + "(maPhieuKiemTra, maPhieuTiepNhan, maKH, maXe, maNV, trangThai, trangThaiBanDau, vanDeHuHong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(PhieuKiemTra phieu) throws SQLException {
        String sql = "UPDATE PhieuKiemTra SET maPhieuTiepNhan = ?, maKH = ?, maXe = ?, maNV = ?, "
                + "trangThai = ?, trangThaiBanDau = ?, vanDeHuHong = ? WHERE maPhieuKiemTra = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phieu.getPhieuTiepNhan() == null
                    ? null
                    : phieu.getPhieuTiepNhan().getMaPhieuTiepNhan());
            statement.setString(2, phieu.getKhachHang() == null ? null : phieu.getKhachHang().getMaKH());
            statement.setString(3, phieu.getXe() == null ? null : phieu.getXe().getMaXe());
            statement.setString(4, phieu.getNhanVien() == null ? null : phieu.getNhanVien().getMaNV());
            statement.setString(5, phieu.getTrangThai());
            statement.setString(6, phieu.getTrangThaiBanDau());
            statement.setString(7, phieu.getVanDeHuHong());
            statement.setString(8, phieu.getMaPhieuKiemTra());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maPhieuKiemTra) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM PhieuKiemTra WHERE maPhieuKiemTra = ?")) {
            statement.setString(1, maPhieuKiemTra);
            return statement.executeUpdate() > 0;
        }
    }

    private PhieuKiemTra mapRow(ResultSet resultSet) throws SQLException {
        NhanVien nhanVien = new NhanVien(resultSet.getString("maNV"));
        nhanVien.setHoTen(resultSet.getString("hoTen"));
        return new PhieuKiemTra(
                resultSet.getString("maPhieuKiemTra"),
                new PhieuTiepNhan(resultSet.getString("maPhieuTiepNhan")),
                new KhachHang(resultSet.getString("maKH")),
                new Xe(resultSet.getString("maXe")),
                nhanVien,
                resultSet.getString("trangThai"),
                resultSet.getString("trangThaiBanDau"),
                resultSet.getString("vanDeHuHong"));
    }

    private void ganThamSo(PreparedStatement statement, PhieuKiemTra phieu) throws SQLException {
        statement.setString(1, phieu.getMaPhieuKiemTra());
        statement.setString(2, phieu.getPhieuTiepNhan() == null
                ? null
                : phieu.getPhieuTiepNhan().getMaPhieuTiepNhan());
        statement.setString(3, phieu.getKhachHang() == null ? null : phieu.getKhachHang().getMaKH());
        statement.setString(4, phieu.getXe() == null ? null : phieu.getXe().getMaXe());
        statement.setString(5, phieu.getNhanVien() == null ? null : phieu.getNhanVien().getMaNV());
        statement.setString(6, phieu.getTrangThai());
        statement.setString(7, phieu.getTrangThaiBanDau());
        statement.setString(8, phieu.getVanDeHuHong());
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

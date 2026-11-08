package dao;

import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuBanGiao;
import entity.Xe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuBanGiaoDAO {
    public List<PhieuBanGiao> findAll() throws SQLException {
        String sql = "SELECT p.maPhieuBanGiao, p.maXe, p.maKH, kh.hoTen AS hoTenKhachHang, p.maNV, "
                + "p.tinhTrangXeHienTai, p.tenNguoiNhan, p.soDienThoaiNguoiNhan "
                + "FROM PhieuBanGiao p JOIN KhachHang kh ON kh.maKH = p.maKH "
                + "ORDER BY p.maPhieuBanGiao";
        List<PhieuBanGiao> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public PhieuBanGiao findById(String maPhieuBanGiao) throws SQLException {
        String sql = "SELECT p.maPhieuBanGiao, p.maXe, p.maKH, kh.hoTen AS hoTenKhachHang, p.maNV, "
                + "p.tinhTrangXeHienTai, p.tenNguoiNhan, p.soDienThoaiNguoiNhan "
                + "FROM PhieuBanGiao p JOIN KhachHang kh ON kh.maKH = p.maKH "
                + "WHERE p.maPhieuBanGiao = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuBanGiao);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public boolean insert(PhieuBanGiao phieu) throws SQLException {
        String sql = "INSERT INTO PhieuBanGiao (maPhieuBanGiao, maXe, maKH, tenKhachHang, maNV, "
                + "tinhTrangXeHienTai, tenNguoiNhan, soDienThoaiNguoiNhan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu, true);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(PhieuBanGiao phieu) throws SQLException {
        String sql = "UPDATE PhieuBanGiao SET maXe = ?, maKH = ?, tenKhachHang = ?, maNV = ?, "
                + "tinhTrangXeHienTai = ?, tenNguoiNhan = ?, soDienThoaiNguoiNhan = ? "
                + "WHERE maPhieuBanGiao = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu, false);
            statement.setString(8, phieu.getMaPhieuBanGiao());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maPhieuBanGiao) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM PhieuBanGiao WHERE maPhieuBanGiao = ?")) {
            statement.setString(1, maPhieuBanGiao);
            return statement.executeUpdate() > 0;
        }
    }

    private PhieuBanGiao mapRow(ResultSet resultSet) throws SQLException {
        return new PhieuBanGiao(
                resultSet.getString("maPhieuBanGiao"),
                new Xe(resultSet.getString("maXe")),
                new KhachHang(resultSet.getString("maKH"), resultSet.getString("hoTenKhachHang"), null),
                new NhanVien(resultSet.getString("maNV")),
                resultSet.getString("tinhTrangXeHienTai"),
                resultSet.getString("tenNguoiNhan"),
                resultSet.getString("soDienThoaiNguoiNhan"));
    }

    private void ganThamSo(PreparedStatement statement, PhieuBanGiao phieu, boolean includeKey)
            throws SQLException {
        int index = 1;
        if (includeKey) {
            statement.setString(index++, phieu.getMaPhieuBanGiao());
        }
        statement.setString(index++, phieu.getXe() == null ? null : phieu.getXe().getMaXe());
        statement.setString(index++, phieu.getKhachHang() == null
                ? null
                : phieu.getKhachHang().getMaKH());
        statement.setString(index++, phieu.getKhachHang() == null
                ? null
                : phieu.getKhachHang().getHoTen());
        statement.setString(index++, phieu.getNhanVien() == null
                ? null
                : phieu.getNhanVien().getMaNV());
        statement.setString(index++, phieu.getTinhTrangXeHienTai());
        statement.setString(index++, phieu.getTenNguoiNhan());
        statement.setString(index, phieu.getSoDienThoaiNguoiNhan());
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

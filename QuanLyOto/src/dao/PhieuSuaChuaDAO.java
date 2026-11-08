package dao;

import connectDB.ConnectDB;
import entity.PhieuSuaChua;
import entity.ChiTietPhieuSuaChua;
import entity.Xe;
import entity.PhieuKiemTra;
import entity.KhachHang;
import entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuSuaChuaDAO {
    private static final String CAU_LENH_LAY_DANH_SACH = """
            SELECT maPhieuSuaChua, maPhieuKiemTra, maXe, maKH, maNV,
                   ngayBatDau, ngayHoanThanhDuKien, trangThai
            FROM PhieuSuaChua
            ORDER BY maPhieuSuaChua
            """;

    public List<PhieuSuaChua> findAll() throws SQLException {
        List<PhieuSuaChua> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(CAU_LENH_LAY_DANH_SACH);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(anhXa(resultSet));
            }
        }
        return danhSach;
    }

    public PhieuSuaChua findById(String maPhieuSuaChua) throws SQLException {
        kiemTraMa(maPhieuSuaChua);
        String sql = "SELECT maPhieuSuaChua, maPhieuKiemTra, maXe, maKH, maNV, "
                + "ngayBatDau, ngayHoanThanhDuKien, trangThai "
                + "FROM PhieuSuaChua WHERE maPhieuSuaChua = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuSuaChua.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? anhXa(resultSet) : null;
            }
        }
    }

    public boolean insert(PhieuSuaChua phieu) throws SQLException {
        kiemTraDuLieu(phieu);
        String sql = "INSERT INTO PhieuSuaChua "
                + "(maPhieuSuaChua, maPhieuKiemTra, maXe, maKH, maNV, "
                + "ngayBatDau, ngayHoanThanhDuKien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu, true);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean insertWithDetails(PhieuSuaChua phieu, List<ChiTietPhieuSuaChua> danhSachChiTiet)
            throws SQLException {
        kiemTraDuLieu(phieu);
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            throw new SQLException("Phiếu sửa chữa phải có ít nhất một chi tiết.");
        }
        String sqlPhieu = "INSERT INTO PhieuSuaChua "
                + "(maPhieuSuaChua, maPhieuKiemTra, maXe, maKH, maNV, "
                + "ngayBatDau, ngayHoanThanhDuKien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlChiTiet = "INSERT INTO ChiTietPhieuSuaChua "
                + "(maPhieuSuaChua, maPhuTung, maDV, soLuong, donGia) VALUES (?, ?, ?, ?, ?)";
        Connection connection = moKetNoi();
        try (PreparedStatement phieuStatement = connection.prepareStatement(sqlPhieu);
                PreparedStatement chiTietStatement = connection.prepareStatement(sqlChiTiet)) {
            connection.setAutoCommit(false);
            ganThamSo(phieuStatement, phieu, true);
            phieuStatement.executeUpdate();
            for (ChiTietPhieuSuaChua chiTiet : danhSachChiTiet) {
                kiemTraChiTiet(chiTiet);
                chiTietStatement.setString(1, chiTiet.getMaPhieuSuaChua().trim());
                chiTietStatement.setString(2, chiTiet.getMaPhuTung());
                chiTietStatement.setString(3, chiTiet.getMaDV());
                chiTietStatement.setInt(4, chiTiet.getSoLuong());
                chiTietStatement.setBigDecimal(5, chiTiet.getDonGia());
                chiTietStatement.addBatch();
            }
            chiTietStatement.executeBatch();
            connection.commit();
            return true;
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                exception.addSuppressed(rollbackException);
            }
            throw exception;
        } finally {
            connection.close();
        }
    }

    private void kiemTraChiTiet(ChiTietPhieuSuaChua chiTiet) throws SQLException {
        boolean coPhuTung = chiTiet.getMaPhuTung() != null && !chiTiet.getMaPhuTung().isBlank();
        boolean coDichVu = chiTiet.getMaDV() != null && !chiTiet.getMaDV().isBlank();
        if (coPhuTung == coDichVu || chiTiet.getSoLuong() == null || chiTiet.getSoLuong() <= 0
                || chiTiet.getDonGia() == null || chiTiet.getDonGia().signum() < 0) {
            throw new SQLException("Chi tiết sửa chữa không hợp lệ.");
        }
    }

    public boolean update(PhieuSuaChua phieu) throws SQLException {
        kiemTraDuLieu(phieu);
        String sql = "UPDATE PhieuSuaChua SET maPhieuKiemTra = ?, maXe = ?, maKH = ?, maNV = ?, "
                + "ngayBatDau = ?, ngayHoanThanhDuKien = ?, trangThai = ? "
                + "WHERE maPhieuSuaChua = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phieu.getMaPhieuKiemTra().getMaPhieuKiemTra());
            statement.setString(2, phieu.getMaXe().getMaXe());
            statement.setString(3, phieu.getMaKH().getMaKH());
            statement.setString(4, phieu.getMaNV().getMaNV());
            statement.setTimestamp(5, phieu.getNgayBatDau());
            statement.setTimestamp(6, phieu.getNgayHoanThanhDuKien());
            statement.setString(7, phieu.getTrangThai());
            statement.setString(8, phieu.getMaPhieuSuaChua().trim());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maPhieuSuaChua) throws SQLException {
        kiemTraMa(maPhieuSuaChua);
        String maPSC = maPhieuSuaChua.trim();
        try (Connection connection = moKetNoi();
                PreparedStatement kiemTraHoaDon = connection.prepareStatement(
                        "SELECT COUNT(*) FROM HoaDon WHERE maPhieuSuaChua = ?");
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM PhieuSuaChua WHERE maPhieuSuaChua = ?")) {
            kiemTraHoaDon.setString(1, maPSC);
            try (ResultSet resultSet = kiemTraHoaDon.executeQuery()) {
                resultSet.next();
                if (resultSet.getInt(1) > 0) {
                    throw new SQLException(
                            "Không thể xóa phiếu đã lập hóa đơn. Hãy xóa hóa đơn liên quan trước.");
                }
            }
            statement.setString(1, maPSC);
            return statement.executeUpdate() > 0;
        }
    }

    private Connection moKetNoi() throws SQLException {
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }
        return connection;
    }

    private PhieuSuaChua anhXa(ResultSet resultSet) throws SQLException {
        PhieuSuaChua phieu = new PhieuSuaChua();
        phieu.setMaPhieuSuaChua(resultSet.getString("maPhieuSuaChua"));

        PhieuKiemTra phieuKiemTra = new PhieuKiemTra();
        phieuKiemTra.setMaPhieuKiemTra(resultSet.getString("maPhieuKiemTra"));
        phieu.setMaPhieuKiemTra(phieuKiemTra);

        Xe xe = new Xe();
        xe.setMaXe(resultSet.getString("maXe"));
        phieu.setMaXe(xe);

        KhachHang khachHang = new KhachHang();
        khachHang.setMaKH(resultSet.getString("maKH"));
        phieu.setMaKH(khachHang);

        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNV(resultSet.getString("maNV"));
        phieu.setMaNV(nhanVien);

        phieu.setNgayBatDau(resultSet.getTimestamp("ngayBatDau"));
        phieu.setNgayHoanThanhDuKien(resultSet.getTimestamp("ngayHoanThanhDuKien"));
        phieu.setTrangThai(resultSet.getString("trangThai"));
        return phieu;
    }

    private void ganThamSo(PreparedStatement statement, PhieuSuaChua phieu, boolean includeKey)
            throws SQLException {
        int index = 1;
        if (includeKey) {
            statement.setString(index++, phieu.getMaPhieuSuaChua().trim());
        }
        statement.setString(index++, phieu.getMaPhieuKiemTra().getMaPhieuKiemTra());
        statement.setString(index++, phieu.getMaXe().getMaXe());
        statement.setString(index++, phieu.getMaKH().getMaKH());
        statement.setString(index++, phieu.getMaNV().getMaNV());
        statement.setTimestamp(index++, phieu.getNgayBatDau());
        statement.setTimestamp(index++, phieu.getNgayHoanThanhDuKien());
        statement.setString(index, phieu.getTrangThai());
    }

    private void kiemTraDuLieu(PhieuSuaChua phieu) throws SQLException {
        if (phieu == null) {
            throw new SQLException("Thông tin phiếu sửa chữa không được để trống.");
        }
        kiemTraMa(phieu.getMaPhieuSuaChua());
        kiemTraBatBuoc(phieu.getMaPhieuKiemTra().getMaPhieuKiemTra(), "Mã phiếu kiểm tra");
        kiemTraBatBuoc(phieu.getMaXe().getMaXe(), "Mã xe");
        kiemTraBatBuoc(phieu.getMaKH().getMaKH(), "Mã khách hàng");
        kiemTraBatBuoc(phieu.getMaNV().getMaNV(), "Mã nhân viên");
        if (phieu.getNgayBatDau() == null || phieu.getNgayHoanThanhDuKien() == null) {
            throw new SQLException("Ngày bắt đầu và ngày hoàn thành dự kiến không được để trống.");
        }
    }

    private void kiemTraBatBuoc(String giaTri, String tenTruong) throws SQLException {
        if (giaTri == null || giaTri.isBlank()) {
            throw new SQLException(tenTruong + " không được để trống.");
        }
    }

    private void kiemTraMa(String maPhieuSuaChua) throws SQLException {
        if (maPhieuSuaChua == null || maPhieuSuaChua.isBlank()) {
            throw new SQLException("Mã phiếu sửa chữa không được để trống.");
        }
    }
}

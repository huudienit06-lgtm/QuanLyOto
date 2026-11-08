package dao;

import entity.ChucVu;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuTiepNhan;
import entity.Xe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuTiepNhanDAO {
    public List<PhieuTiepNhan> findAll() throws SQLException {
        String sql = """
                SELECT ptn.maPhieuTiepNhan, ptn.ngayLap, ptn.trangThai, ptn.tienDo, ptn.ghiChu,
                       xe.maXe AS xe_maXe, xe.bienSo, xe.hangXe, xe.dongXe,
                       xe.namSanXuat, xe.soKhung, xe.soMay, xe.maKH AS xe_maKH,
                       kh.maKH AS kh_maKH, kh.hoTen AS kh_hoTen, kh.soDT AS kh_soDT,
                       kh.email AS kh_email, kh.diaChi AS kh_diaChi, kh.congNo AS kh_congNo,
                       nv.maNV AS nv_maNV, nv.hoTen AS nv_hoTen,
                       cv.maChucVu AS nv_maChucVu, cv.tenChucVu AS nv_tenChucVu, nv.soDT AS nv_soDT,
                       nv.email AS nv_email, nv.trangThai AS nv_trangThai
                FROM PhieuTiepNhan ptn
                JOIN Xe xe ON xe.maXe = ptn.maXe
                JOIN KhachHang kh ON kh.maKH = ptn.maKH
                JOIN NhanVien nv ON nv.maNV = ptn.maNV
                JOIN ChucVu cv ON cv.maChucVu = nv.maChucVu
                ORDER BY ptn.ngayLap DESC
                """;
        List<PhieuTiepNhan> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public boolean insert(PhieuTiepNhan phieu) throws SQLException {
        String sql = "INSERT INTO PhieuTiepNhan "
            + "(maPhieuTiepNhan, maXe, maKH, maNV, ngayLap, trangThai, tienDo, ghiChu) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(PhieuTiepNhan phieu) throws SQLException {
        String sql = "UPDATE PhieuTiepNhan SET maXe = ?, maKH = ?, maNV = ?, ngayLap = ?, "
            + "trangThai = ?, tienDo = ?, ghiChu = ? WHERE maPhieuTiepNhan = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, layMaXe(phieu));
            statement.setString(2, layMaKhachHang(phieu));
            statement.setString(3, layMaNhanVien(phieu));
            statement.setTimestamp(4, phieu.getNgayLap());
            statement.setString(5, phieu.getTrangThai());
            statement.setString(6, phieu.getTienDo());
            statement.setString(7, phieu.getGhiChu());
            statement.setString(8, phieu.getMaPhieuTiepNhan());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean capNhatTienDo(String maPhieuTiepNhan, String tienDo) throws SQLException {
        String sql = "UPDATE PhieuTiepNhan SET tienDo = ? WHERE maPhieuTiepNhan = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tienDo);
            statement.setString(2, maPhieuTiepNhan);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean capNhatTienDoTheoPhieuKiemTra(String maPhieuKiemTra, String tienDo)
            throws SQLException {
        String sql = "UPDATE ptn SET tienDo = ? "
                + "FROM PhieuTiepNhan ptn JOIN PhieuKiemTra pkt "
                + "ON pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan "
                + "WHERE pkt.maPhieuKiemTra = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tienDo);
            statement.setString(2, maPhieuKiemTra);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maPhieuTiepNhan) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM PhieuTiepNhan WHERE maPhieuTiepNhan = ?")) {
            statement.setString(1, maPhieuTiepNhan);
            return statement.executeUpdate() > 0;
        }
    }

    private PhieuTiepNhan mapRow(ResultSet resultSet) throws SQLException {
        return new PhieuTiepNhan(
                resultSet.getString("maPhieuTiepNhan"),
                new Xe(
                        resultSet.getString("xe_maXe"),
                        resultSet.getString("bienSo"),
                        resultSet.getString("hangXe"),
                        resultSet.getString("dongXe"),
                        resultSet.getObject("namSanXuat") == null ? null : resultSet.getInt("namSanXuat"),
                        resultSet.getString("soKhung"),
                        resultSet.getString("soMay"),
                        resultSet.getString("xe_maKH")),
                new KhachHang(
                        resultSet.getString("kh_maKH"),
                        resultSet.getString("kh_hoTen"),
                        resultSet.getString("kh_soDT"),
                        resultSet.getString("kh_email"),
                        resultSet.getString("kh_diaChi"),
                        resultSet.getDouble("kh_congNo")),
                new NhanVien(
                        resultSet.getString("nv_maNV"),
                        resultSet.getString("nv_hoTen"),
                        new ChucVu(resultSet.getString("nv_maChucVu"), resultSet.getString("nv_tenChucVu")),
                        resultSet.getString("nv_soDT"),
                        resultSet.getString("nv_email"),
                        resultSet.getString("nv_trangThai")),
                resultSet.getTimestamp("ngayLap"),
                resultSet.getString("trangThai"),
                resultSet.getString("tienDo"),
                resultSet.getString("ghiChu"));
    }

    private void ganThamSo(PreparedStatement statement, PhieuTiepNhan phieu) throws SQLException {
        statement.setString(1, phieu.getMaPhieuTiepNhan());
        statement.setString(2, layMaXe(phieu));
        statement.setString(3, layMaKhachHang(phieu));
        statement.setString(4, layMaNhanVien(phieu));
        statement.setTimestamp(5, phieu.getNgayLap());
        statement.setString(6, phieu.getTrangThai());
        statement.setString(7, phieu.getTienDo());
        statement.setString(8, phieu.getGhiChu());
    }

    private String layMaXe(PhieuTiepNhan phieu) throws SQLException {
        if (phieu.getXe() == null || phieu.getXe().getMaXe() == null) {
            throw new SQLException("Phiếu tiếp nhận phải có xe.");
        }
        return phieu.getXe().getMaXe();
    }

    private String layMaKhachHang(PhieuTiepNhan phieu) throws SQLException {
        if (phieu.getKhachHang() == null || phieu.getKhachHang().getMaKH() == null) {
            throw new SQLException("Phiếu tiếp nhận phải có khách hàng.");
        }
        return phieu.getKhachHang().getMaKH();
    }

    private String layMaNhanVien(PhieuTiepNhan phieu) throws SQLException {
        if (phieu.getNhanVien() == null || phieu.getNhanVien().getMaNV() == null) {
            throw new SQLException("Phiếu tiếp nhận phải có nhân viên lập phiếu.");
        }
        return phieu.getNhanVien().getMaNV();
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }
}

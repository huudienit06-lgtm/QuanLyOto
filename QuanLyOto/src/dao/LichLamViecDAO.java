package dao;

import entity.CaLamViec;
import entity.LichLamViec;
import entity.NhanVien;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LichLamViecDAO {
    public List<LichLamViec> findAll() throws SQLException {
        String sql = "SELECT maLich, maCa, maNV, ngayLamViec, trangThai FROM LichLamViec ORDER BY ngayLamViec DESC";
        List<LichLamViec> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(new LichLamViec(
                        resultSet.getString("maLich"),
                        new CaLamViec(resultSet.getString("maCa")),
                        new NhanVien(resultSet.getString("maNV")),
                        resultSet.getDate("ngayLamViec"),
                        resultSet.getString("trangThai")));
            }
        }
        return danhSach;
    }

    public List<PhanCong> layPhanCong(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String sql = "SELECT ll.maLich, ll.maCa, ll.maNV, nv.hoTen, ll.ngayLamViec, ll.trangThai "
                + "FROM LichLamViec ll JOIN NhanVien nv ON nv.maNV = ll.maNV "
                + "WHERE ll.ngayLamViec >= ? AND ll.ngayLamViec < ? "
                + "ORDER BY ll.ngayLamViec, ll.maCa, nv.hoTen";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(tuNgay));
            statement.setDate(2, Date.valueOf(denNgay));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<PhanCong> danhSach = new ArrayList<>();
                while (resultSet.next()) {
                    danhSach.add(new PhanCong(
                            resultSet.getString("maLich"),
                            resultSet.getString("maCa"),
                            resultSet.getString("maNV"),
                            resultSet.getString("hoTen"),
                            resultSet.getDate("ngayLamViec").toLocalDate(),
                            resultSet.getString("trangThai")));
                }
                return danhSach;
            }
        }
    }

    public List<NhanVienTrongCa> layNhanVienTrongCa(LocalDateTime thoiDiem) throws SQLException {
        String sql = "SELECT ll.ngayLamViec, ll.maNV, nv.hoTen, ca.gioBatDau, ca.gioKetThuc "
                + "FROM LichLamViec ll "
                + "JOIN NhanVien nv ON nv.maNV = ll.maNV "
                + "JOIN CaLamViec ca ON ca.maCa = ll.maCa "
                + "WHERE ll.ngayLamViec >= ? AND ll.ngayLamViec < ? "
                + "AND ll.trangThai <> N'Vắng mặt' "
                + "ORDER BY nv.hoTen, ll.maNV";
        LocalDate homNay = thoiDiem.toLocalDate();
        LocalTime gioHienTai = thoiDiem.toLocalTime();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(homNay.minusDays(1)));
            statement.setDate(2, Date.valueOf(homNay.plusDays(1)));
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, NhanVienTrongCa> nhanVienTrongCa = new LinkedHashMap<>();
                while (resultSet.next()) {
                    LocalDate ngayLamViec = resultSet.getDate("ngayLamViec").toLocalDate();
                    LocalTime gioBatDau = resultSet.getTime("gioBatDau").toLocalTime();
                    LocalTime gioKetThuc = resultSet.getTime("gioKetThuc").toLocalTime();
                    if (laCaDangDienRa(ngayLamViec, gioBatDau, gioKetThuc, homNay, gioHienTai)) {
                        String maNV = resultSet.getString("maNV");
                        nhanVienTrongCa.putIfAbsent(maNV,
                                new NhanVienTrongCa(maNV, resultSet.getString("hoTen")));
                    }
                }
                return new ArrayList<>(nhanVienTrongCa.values());
            }
        }
    }

    private boolean laCaDangDienRa(
            LocalDate ngayLamViec, LocalTime gioBatDau, LocalTime gioKetThuc,
            LocalDate homNay, LocalTime gioHienTai) {
        if (gioBatDau.isBefore(gioKetThuc)) {
            return ngayLamViec.equals(homNay)
                    && !gioHienTai.isBefore(gioBatDau)
                    && gioHienTai.isBefore(gioKetThuc);
        }
        return (ngayLamViec.equals(homNay) && !gioHienTai.isBefore(gioBatDau))
                || (ngayLamViec.plusDays(1).equals(homNay) && gioHienTai.isBefore(gioKetThuc));
    }

    public boolean themPhanCong(String maLich, String maCa, String maNV, LocalDate ngayLamViec)
            throws SQLException {
        String sql = "INSERT INTO LichLamViec (maLich, maCa, maNV, ngayLamViec, trangThai) "
                + "VALUES (?, ?, ?, ?, N'Đã phân công')";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maLich);
            statement.setString(2, maCa);
            statement.setString(3, maNV);
            statement.setDate(4, Date.valueOf(ngayLamViec));
            return statement.executeUpdate() > 0;
        }
    }

    public boolean daTrungPhanCong(String maCa, String maNV, LocalDate ngayLamViec,
            String maLichBoQua) throws SQLException {
        String sql = "SELECT 1 FROM LichLamViec WHERE maCa = ? AND maNV = ? AND ngayLamViec = ?";
        if (maLichBoQua != null) {
            sql += " AND maLich <> ?";
        }
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maCa);
            statement.setString(2, maNV);
            statement.setDate(3, Date.valueOf(ngayLamViec));
            if (maLichBoQua != null) {
                statement.setString(4, maLichBoQua);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean capNhatPhanCong(String maLich, String maCa, String maNV, LocalDate ngayLamViec)
            throws SQLException {
        String sql = "UPDATE LichLamViec SET maCa = ?, maNV = ?, ngayLamViec = ? WHERE maLich = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maCa);
            statement.setString(2, maNV);
            statement.setDate(3, Date.valueOf(ngayLamViec));
            statement.setString(4, maLich);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean xoaPhanCong(String maLich) throws SQLException {
        String sql = "DELETE FROM LichLamViec WHERE maLich = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maLich);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean capNhatTrangThai(String maLich, boolean coMat) throws SQLException {
        String sql = "UPDATE LichLamViec SET trangThai = ? WHERE maLich = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, coMat ? "Có mặt" : "Vắng mặt");
            statement.setString(2, maLich);
            return statement.executeUpdate() > 0;
        }
    }

    private Connection moKetNoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String password = "123456";
        return DriverManager.getConnection(url, user, password);
    }

    public record PhanCong(String maLich, String maCa, String maNV, String hoTen,
            LocalDate ngayLamViec, String trangThai) {
    }

    public record NhanVienTrongCa(String maNV, String hoTen) {
    }

}

package controller;

import dao.BaoCaoSuaChuaDAO;
import dao.BaoCaoSuaChuaDAO.ThongKeBieuDo;
import dao.BaoCaoSuaChuaDAO.ThongKeSuDung;
import dao.BaoCaoSuaChuaDAO.ThongKeTongQuan;
import gui.FrmThongKeSuaChua;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class ThongKeSuaChuaController {
    private final FrmThongKeSuaChua view;
    private final BaoCaoSuaChuaDAO baoCaoSuaChuaDAO = new BaoCaoSuaChuaDAO();

    public ThongKeSuaChuaController(FrmThongKeSuaChua view) {
        this.view = view;
    }

    public void khoiTao() {
        view.layNutThongKe().addActionListener(event -> thongKe());
        view.layNutLamMoi().addActionListener(event -> {
            view.lamMoiBieuMau();
            thongKe();
        });
        view.lamMoiBieuMau();
        thongKe();
    }

    private void thongKe() {
        try {
            LocalDate tuNgay = view.layTuNgay();
            LocalDate denNgay = view.layDenNgay();
            if (denNgay.isBefore(tuNgay)) {
                throw new IllegalArgumentException("Đến ngày phải bằng hoặc sau Từ ngày.");
            }

            String kySuDung = view.layKySuDung();
            String kyBieuDo = view.layKyBieuDo();
            ThongKeTongQuan tongQuan = baoCaoSuaChuaDAO.layTongQuan(
                    tuNgay, denNgay, null, null);
            List<ThongKeSuDung> dichVuHot = baoCaoSuaChuaDAO.layTopSuDung(
                    denNgay, kySuDung, "Dịch vụ", null, null);
            List<ThongKeSuDung> phuTungHot = baoCaoSuaChuaDAO.layTopSuDung(
                    denNgay, kySuDung, "Phụ tùng", null, null);
            List<ThongKeBieuDo> bieuDo = baoCaoSuaChuaDAO.layTheoBieuDo(
                    denNgay, kyBieuDo, null, null);

            view.capNhatTongQuan(tongQuan);
            view.capNhatBangHot(dichVuHot, phuTungHot);
            view.capNhatBieuDo(bieuDo);
            view.thongBao("Đã cập nhật thống kê.");
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải thống kê sửa chữa: " + exception.getMessage());
        }
    }
}
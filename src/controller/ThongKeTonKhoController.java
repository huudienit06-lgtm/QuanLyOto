package controller;

import dao.PhuTungDAO;
import dao.ThongKeNhapXuatKhoDAO;
import entity.PhuTung;
import gui.FrmThongKeTonKho;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;

public class ThongKeTonKhoController {
    private final FrmThongKeTonKho view;
    private final PhuTungDAO phuTungDAO = new PhuTungDAO();
    private final ThongKeNhapXuatKhoDAO thongKeNhapXuatKhoDAO = new ThongKeNhapXuatKhoDAO();

    public ThongKeTonKhoController(FrmThongKeTonKho view) {
        this.view = view;
    }

    public void khoiTao() {
        view.getBtnLoc().addActionListener(e -> {
            String tieuChi = view.getCboLocPhuTung().getSelectedItem().toString();
            taiDuLieuVaoBang(tieuChi);
            taiThongKeNhapXuat("Nhập", view.getCboKyNhap(), view.getModelThongKeNhap());
            taiThongKeNhapXuat("Xuất", view.getCboKyXuat(), view.getModelThongKeXuat());
        });

        view.getCboKyNhap().addActionListener(e -> taiThongKeNhapXuat("Nhập", view.getCboKyNhap(), view.getModelThongKeNhap()));
        view.getCboKyXuat().addActionListener(e -> taiThongKeNhapXuat("Xuất", view.getCboKyXuat(), view.getModelThongKeXuat()));

        napDuLieuBanDau();
    }

    private void napDuLieuBanDau() {
        taiDuLieuVaoBang("Tất cả phụ tùng");
        taiThongKeNhapXuat("Nhập", view.getCboKyNhap(), view.getModelThongKeNhap());
        taiThongKeNhapXuat("Xuất", view.getCboKyXuat(), view.getModelThongKeXuat());
    }

    private void taiThongKeNhapXuat(String kieuPhieu, JComboBox<String> cboKy, DefaultTableModel model) {
        String ky = (String) cboKy.getSelectedItem();
        if (ky == null) return;
        LocalDate homNay = LocalDate.now();
        LocalDate tuNgay, denNgay;
        switch (ky) {
            case "Tuần này" -> { tuNgay = homNay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); denNgay = tuNgay.plusWeeks(1); }
            case "Tháng này" -> { tuNgay = homNay.withDayOfMonth(1); denNgay = tuNgay.plusMonths(1); }
            case "Năm nay" -> { tuNgay = homNay.withDayOfYear(1); denNgay = tuNgay.plusYears(1); }
            default -> throw new IllegalArgumentException("Kỳ thống kê không hợp lệ: " + ky);
        }
        try {
            List<ThongKeNhapXuatKhoDAO.ThongKePhuTung> danhSach = thongKeNhapXuatKhoDAO.layThongKe(tuNgay, denNgay, kieuPhieu);
            model.setRowCount(0);
            for (ThongKeNhapXuatKhoDAO.ThongKePhuTung phuTung : danhSach) {
                model.addRow(new Object[] { phuTung.maPhuTung(), phuTung.tenPhuTung(), phuTung.donViTinh() == null ? "—" : phuTung.donViTinh(), phuTung.soLuong() });
            }
        } catch (SQLException exception) { exception.printStackTrace(); }
    }

    private void taiDuLieuVaoBang(String tieuChiLoc) {
        view.getModelTonKho().setRowCount(0);
        int canNhapThemCount = 0; int tongSoLoai = 0;
        try {
            List<PhuTung> dsPhuTung = phuTungDAO.findAll();
            if (dsPhuTung != null && !dsPhuTung.isEmpty()) {
                Collections.sort(dsPhuTung, Comparator.comparingInt(PhuTung::getSoLuongTon));
                for (PhuTung pt : dsPhuTung) {
                    int sl = pt.getSoLuongTon();
                    String trangThai = "Đủ tồn";
                    if (sl < 5) { trangThai = "Sắp hết - Cần nhập thêm"; canNhapThemCount++; } 
                    else if (sl <= 10) { trangThai = "Tồn thấp - Theo dõi"; }

                    boolean isMatch = true;
                    if (tieuChiLoc.equals("Sắp hết (< 5)") && sl >= 5) isMatch = false;
                    if (tieuChiLoc.equals("Tồn thấp (5 - 10)") && (sl < 5 || sl > 10)) isMatch = false;
                    if (tieuChiLoc.equals("Đủ tồn (> 10)") && sl <= 10) isMatch = false;

                    if (isMatch) {
                        String donViTinh = pt.getDonViTinh() != null ? pt.getDonViTinh() : "—";
                        view.getModelTonKho().addRow(new Object[]{ pt.getMaPhuTung(), pt.getTenPhuTung(), donViTinh, sl, trangThai });
                        tongSoLoai++;
                    }
                }
            }
            view.getLblPhuDeBang().setText(tongSoLoai + " loại phụ tùng - Sắp xếp số lượng tồn từ thấp đến cao");
            view.getLblCanNhapThem().setText(canNhapThemCount + " loại cần nhập thêm ngay!");
            view.getLblFooterLeft().setText("Hiển thị: " + tongSoLoai + " / " + (dsPhuTung != null ? dsPhuTung.size() : 0) + " loại phụ tùng");
            view.getLblFooterRight().setText("Kiểm kê: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")));
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
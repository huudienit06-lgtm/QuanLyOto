package controller;

import dao.PhuTungDAO;
import entity.PhuTung;
import gui.FrmPhuTung;
import util.MaTuDong;

import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhuTungController {
    private final FrmPhuTung view;
    private final PhuTungDAO dao;

    public PhuTungController(FrmPhuTung view) {
        this.view = view;
        this.dao = new PhuTungDAO();
    }

    public void khoiTao() {
        // Gắn sự kiện (Listener) cho các nút trên giao diện
        view.getBtnThem().addActionListener(e -> themPhuTung());
        view.getBtnCapNhat().addActionListener(e -> capNhatPhuTung());
        view.getBtnXoa().addActionListener(e -> xoaPhuTung());
        view.getBtnLamMoi().addActionListener(e -> view.lamMoiForm());
        view.getBtnTaiLai().addActionListener(e -> {
            view.getTxtTimMa().setText("");
            view.getTxtTimTen().setText("");
            if(view.getCboTimNCC().getItemCount() > 0) view.getCboTimNCC().setSelectedIndex(0);
            view.locDanhSachPhuTung();
            napDanhSachPhuTung();
        });

        // Gắn sự kiện Click JTable
        view.getTblPhuTung().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && view.getTblPhuTung().getSelectedRow() >= 0) {
                view.hienThiPhuTungDaChon();
            }
        });

        napDanhSachPhuTung();
        napNhaCungCap();
    }

    public void napDanhSachPhuTung() {
        try {
            List<PhuTung> ds = dao.findAll();
            view.hienThiDanhSach(ds);
            view.getTxtMaPT().setText(phatSinhMaPT());
            view.setThongBao("Đã tải " + ds.size() + " phụ tùng");
        } catch (SQLException e) {
            view.hienThiLoi("Lỗi tải phụ tùng: " + e.getMessage());
        }
    }

    public void napNhaCungCap() {
        List<String> nccs = dao.layDanhSachNhaCungCap();
        view.getCboNhaCungCap().removeAllItems();
        view.getCboTimNCC().removeAllItems();
        view.getCboTimNCC().addItem("Tất cả");
        for(String ncc : nccs) {
            view.getCboNhaCungCap().addItem(ncc);
            view.getCboTimNCC().addItem(ncc);
        }
    }

    private void themPhuTung() {
        try {
            PhuTung pt = view.docThongTinPhuTung();
            if (dao.insert(pt)) {
                napDanhSachPhuTung();
                napNhaCungCap();
                view.lamMoiForm();
                view.setThongBao("Đã thêm phụ tùng thành công!");
            }
        } catch (IllegalArgumentException e) {
            view.hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            view.hienThiLoi("Lỗi thêm phụ tùng: " + e.getMessage());
        }
    }

    private void capNhatPhuTung() {
        try {
            PhuTung pt = view.docThongTinPhuTung();
            if (dao.update(pt)) {
                napDanhSachPhuTung();
                napNhaCungCap();
                view.lamMoiForm();
                view.setThongBao("Đã cập nhật phụ tùng thành công!");
            } else {
                view.hienThiLoi("Không tìm thấy phụ tùng để cập nhật.");
            }
        } catch (IllegalArgumentException e) {
            view.hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            view.hienThiLoi("Lỗi cập nhật: " + e.getMessage());
        }
    }

    private void xoaPhuTung() {
        String ma = view.getTxtMaPT().getText().trim();
        if (ma.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc muốn xóa phụ tùng " + ma + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (dao.deleteById(ma)) {
                    napDanhSachPhuTung();
                    napNhaCungCap();
                    view.lamMoiForm();
                    view.setThongBao("Đã xóa phụ tùng thành công!");
                } else {
                    view.hienThiLoi("Không thể xóa (Đã phát sinh giao dịch kho)");
                }
            } catch (SQLException e) {
                view.hienThiLoi("Lỗi xóa: " + e.getMessage());
            }
        }
    }

    public String phatSinhMaPT() {
        List<String> danhSachMa = new ArrayList<>();
        for (int i = 0; i < view.getModelPhuTung().getRowCount(); i++) {
            danhSachMa.add(view.getModelPhuTung().getValueAt(i, 0).toString());
        }
        return MaTuDong.sinhMa(MaTuDong.TIEN_TO_PHU_TUNG, danhSachMa, ma -> ma);
    }
}
package controller;

import dao.DichVuDAO;
import entity.DichVu;
import gui.FrmDichVu;
import util.MaTuDong;

import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuController {
    private final FrmDichVu view;
    private final DichVuDAO dichVuDao;

    public DichVuController(FrmDichVu view) {
        this.view = view;
        this.dichVuDao = new DichVuDAO();
    }

    public void khoiTao() {
        // Gắn sự kiện (Listener) cho các nút trên giao diện
        view.getBtnAdd().addActionListener(e -> themDichVu());
        view.getBtnUpdate().addActionListener(e -> capNhatDichVu());
        view.getBtnDel().addActionListener(e -> xoaDichVu());
        view.getBtnReset().addActionListener(e -> view.lamMoiForm());
        
        // Gắn sự kiện Click JTable
        view.getTblDichVu().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && view.getTblDichVu().getSelectedRow() >= 0) {
                view.hienThiDichVuDaChon();
            }
        });

        // Gắn sự kiện tải lại danh sách
        view.getBtnTaiLai().addActionListener(e -> {
            view.getTxtLocMaDichVu().setText("");
            view.getTxtLocTenDichVu().setText("");
            napDanhSachDichVu();
        });

        napDanhSachDichVu();
    }

    public void napDanhSachDichVu() {
        try {
            view.hienThiDanhSach(dichVuDao.findAll());
            view.getTxtMaDichVu().setText(phatSinhMaDV());
            view.setThongBao("Đã tải " + view.getModelDichVu().getRowCount() + " dịch vụ");
        } catch (SQLException e) {
            view.hienThiLoi("Không thể tải danh sách dịch vụ: " + e.getMessage());
        }
    }

    private void themDichVu() {
        try {
            DichVu dichVu = view.docThongTinDichVu();
            if (dichVuDao.insert(dichVu)) {
                napDanhSachDichVu();
                view.lamMoiForm();
                view.setThongBao("Đã thêm dịch vụ " + dichVu.getMaDV());
            }
        } catch (IllegalArgumentException e) {
            view.hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            view.hienThiLoi("Không thể thêm dịch vụ: " + e.getMessage());
        }
    }

    private void capNhatDichVu() {
        try {
            DichVu dichVu = view.docThongTinDichVu();
            if (dichVuDao.update(dichVu)) {
                napDanhSachDichVu();
                view.lamMoiForm();
                view.setThongBao("Đã cập nhật dịch vụ " + dichVu.getMaDV());
            } else {
                view.hienThiLoi("Không tìm thấy dịch vụ cần cập nhật");
            }
        } catch (IllegalArgumentException e) {
            view.hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            view.hienThiLoi("Không thể cập nhật dịch vụ: " + e.getMessage());
        }
    }

    private void xoaDichVu() {
        String maDV = view.getTxtMaDichVu().getText().trim();
        if (maDV.isEmpty()) {
            view.hienThiLoi("Mã dịch vụ không được để trống");
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(view, "Bạn có chắc muốn xóa dịch vụ " + maDV + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) return;

        try {
            if (dichVuDao.deleteById(maDV)) {
                napDanhSachDichVu();
                view.lamMoiForm();
                view.setThongBao("Đã xóa dịch vụ " + maDV);
            } else {
                view.hienThiLoi("Không tìm thấy dịch vụ cần xóa");
            }
        } catch (SQLException e) {
            view.hienThiLoi("Không thể xóa dịch vụ: " + e.getMessage());
        }
    }

    public String phatSinhMaDV() {
        List<String> danhSachMa = new ArrayList<>();
        for (int i = 0; i < view.getModelDichVu().getRowCount(); i++) {
            danhSachMa.add(view.getModelDichVu().getValueAt(i, 0).toString());
        }
        return MaTuDong.sinhMa(MaTuDong.TIEN_TO_DICH_VU, danhSachMa, ma -> ma);
    }
}
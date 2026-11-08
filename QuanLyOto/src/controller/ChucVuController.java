package controller;

import dao.ChucVuDAO;
import entity.ChucVu;
import gui.FrmChucVu;
import java.sql.SQLException;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import util.MaTuDong;

public final class ChucVuController {
    private final FrmChucVu view;
    private final ChucVuDAO chucVuDAO = new ChucVuDAO();
    private List<ChucVu> danhSachChucVu = List.of();

    public ChucVuController(FrmChucVu view) {
        this.view = view;
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themChucVu());
        view.getBtnCapNhat().addActionListener(event -> capNhatChucVu());
        view.getBtnXoa().addActionListener(event -> xoaChucVu());
        view.getBtnLamMoi().addActionListener(event -> view.lamMoiForm(phatSinhMaChucVu()));
        view.getBtnTaiLai().addActionListener(event -> {
            view.getTxtTimKiem().setText("");
            taiDanhSach();
        });
        view.getTblChucVu().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                view.hienThiChucVuDuocChon();
            }
        });
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) { view.locDanhSach(); }
            @Override
            public void removeUpdate(DocumentEvent event) { view.locDanhSach(); }
            @Override
            public void changedUpdate(DocumentEvent event) { view.locDanhSach(); }
        });
        taiDanhSach();
    }

    private void taiDanhSach() {
        try {
            danhSachChucVu = chucVuDAO.findAll();
            view.hienThiDanhSach(danhSachChucVu);
            view.lamMoiForm(phatSinhMaChucVu());
            view.thongBao("Đã tải " + view.getTblChucVu().getRowCount() + " chức vụ.");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách chức vụ: " + exception.getMessage());
        }
    }

    private void themChucVu() {
        try {
            ChucVu chucVu = view.layChucVuNhap();
            if (chucVuDAO.insert(chucVu)) {
                taiDanhSach();
                view.thongBao("Đã thêm chức vụ " + chucVu.getTenChucVu() + ".");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể thêm chức vụ: " + exception.getMessage());
        }
    }

    private void capNhatChucVu() {
        try {
            ChucVu chucVu = view.layChucVuNhap();
            if (chucVuDAO.update(chucVu)) {
                taiDanhSach();
                view.thongBao("Đã cập nhật chức vụ " + chucVu.getTenChucVu() + ".");
            } else {
                view.hienThiLoi("Không tìm thấy chức vụ cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật chức vụ: " + exception.getMessage());
        }
    }

    private void xoaChucVu() {
        String maChucVu = view.getMaChucVuDangChon();
        if (maChucVu == null) {
            view.hienThiLoi("Hãy chọn chức vụ cần xóa.");
            return;
        }
        try {
            if (chucVuDAO.daDuocSuDung(maChucVu)) {
                view.hienThiLoi("Không thể xóa chức vụ đang được gán cho nhân viên.");
                return;
            }
            if (!view.xacNhanXoa(maChucVu)) {
                return;
            }
            if (chucVuDAO.deleteById(maChucVu)) {
                taiDanhSach();
                view.thongBao("Đã xóa chức vụ " + maChucVu + ".");
            } else {
                view.hienThiLoi("Không tìm thấy chức vụ cần xóa.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa chức vụ: " + exception.getMessage());
        }
    }

    private String phatSinhMaChucVu() {
        return MaTuDong.sinhMa(
                MaTuDong.TIEN_TO_CHUC_VU, danhSachChucVu, ChucVu::getMaChucVu);
    }
}
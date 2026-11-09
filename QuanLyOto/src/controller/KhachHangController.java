package controller;

import dao.KhachHangDAO;
import entity.KhachHang;
import gui.FrmKhachHang;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public final class KhachHangController {
    private final FrmKhachHang view;
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    public KhachHangController(FrmKhachHang view) {
        this.view = view;
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themKhachHang());
        view.getBtnCapNhat().addActionListener(event -> capNhatKhachHang());
        view.getBtnXoa().addActionListener(event -> xoaKhachHang());
        view.getBtnLamMoi().addActionListener(event -> lamMoiForm());
        view.getBtnTaiLai().addActionListener(event -> {
            lamMoiForm();
            taiDanhSachKhachHang();
        });
        view.getTblKhachHang().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && view.getTblKhachHang().getSelectedRow() >= 0) {
                view.hienThiKhachHangDangChon();
            }
        });
        bindDocument(view.getTxtLocMaKH());
        bindDocument(view.getTxtLocHoTen());
        bindDocument(view.getTxtLocSoDienThoai());
        bindDocument(view.getTxtLocEmail());
        taiDanhSachKhachHang();
        lamMoiForm();
    }

    private void bindDocument(JTextComponent field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                view.locDanhSachKhachHang();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                view.locDanhSachKhachHang();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                view.locDanhSachKhachHang();
            }
        });
    }

    private void taiDanhSachKhachHang() {
        try {
            List<KhachHang> danhSach = khachHangDAO.findAll();
            view.hienThiDanhSach(danhSach);
            view.locDanhSachKhachHang();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách khách hàng: " + exception.getMessage());
        }
    }

    private void themKhachHang() {
        if (!view.validateForm()) {
            return;
        }
        KhachHang khachHang = view.taoKhachHang();
        if (khachHang.getCongNo() == null) {
            khachHang.setCongNo(0D);
        }
        try {
            if (new HashSet<>(khachHangDAO.findAll()).contains(new KhachHang(khachHang.getMaKH()))) {
                view.hienThiLoi("Mã khách hàng đã tồn tại.");
                return;
            }
            if (!khachHangDAO.insert(khachHang)) {
                view.hienThiLoi("Không thêm được khách hàng vào cơ sở dữ liệu.");
                return;
            }
            if (view.isCheDoTaoMoi()) {
                view.dongDialogSauKhiTao(khachHang.getMaKH());
                return;
            }
            taiDanhSachKhachHang();
            lamMoiForm();
            view.thongBaoThanhCong("Đã thêm khách hàng thành công.", "Thêm khách hàng");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể thêm khách hàng: " + exception.getMessage());
        }
    }

    private void capNhatKhachHang() {
        if (view.getMaKHDangChon() == null) {
            view.hienThiLoi("Vui lòng chọn một khách hàng trước khi cập nhật.");
            return;
        }
        if (!view.validateForm()) {
            return;
        }
        try {
            if (!khachHangDAO.update(view.taoKhachHang())) {
                view.hienThiLoi("Không cập nhật được khách hàng trong cơ sở dữ liệu.");
                return;
            }
            taiDanhSachKhachHang();
            lamMoiForm();
            view.thongBaoThanhCong("Đã cập nhật khách hàng thành công.", "Cập nhật khách hàng");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật khách hàng: " + exception.getMessage());
        }
    }

    private void xoaKhachHang() {
        String maKH = view.getMaKHDangChon();
        if (maKH == null) {
            view.hienThiLoi("Vui lòng chọn một khách hàng để xóa.");
            return;
        }
        if (JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa khách hàng có mã: " + maKH + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!khachHangDAO.deleteById(maKH)) {
                view.hienThiLoi("Không xóa được khách hàng khỏi cơ sở dữ liệu.");
                return;
            }
            taiDanhSachKhachHang();
            lamMoiForm();
            view.thongBaoThanhCong("Đã xóa khách hàng thành công.", "Xóa khách hàng");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa khách hàng: " + exception.getMessage());
        }
    }

    private void lamMoiForm() {
        try {
            String maMoi = util.MaTuDong.sinhMa(util.MaTuDong.TIEN_TO_KHACH_HANG,
                    khachHangDAO.findAll(), KhachHang::getMaKH);
            view.lamMoiBieuMau(maMoi);
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể phát sinh mã khách hàng: " + exception.getMessage());
            view.lamMoiBieuMau("");
        }
    }
}

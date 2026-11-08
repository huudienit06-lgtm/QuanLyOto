package controller;

import dao.ChucVuDAO;
import dao.NhanVienDAO;
import entity.ChucVu;
import entity.NhanVien;
import gui.FrmNhanVien;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public final class NhanVienController {
    private final FrmNhanVien view;
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final ChucVuDAO chucVuDAO = new ChucVuDAO();

    public NhanVienController(FrmNhanVien view) {
        this.view = view;
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themNhanVien());
        view.getBtnCapNhat().addActionListener(event -> capNhatNhanVien());
        view.getBtnXoa().addActionListener(event -> xoaNhanVien());
        view.getBtnLamMoi().addActionListener(event -> lamMoiForm());
        view.getBtnTaiLai().addActionListener(event -> {
            taiDanhSachChucVu();
            taiDanhSachNhanVien();
        });
        view.getTblNhanVien().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                view.hienThiNhanVienDangChon();
            }
        });
        bindDocument(view.getTxtLocMaNV());
        bindDocument(view.getTxtLocHoTen());
        bindDocument(view.getTxtLocSoDienThoai());
        bindDocument(view.getTxtLocEmail());
        taiDanhSachChucVu();
        lamMoiForm();
    }

    private void bindDocument(JTextComponent field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) { view.locDanhSachNhanVien(); }
            @Override
            public void removeUpdate(DocumentEvent event) { view.locDanhSachNhanVien(); }
            @Override
            public void changedUpdate(DocumentEvent event) { view.locDanhSachNhanVien(); }
        });
    }

    private void taiDanhSachNhanVien() {
        try {
            view.hienThiDanhSach(nhanVienDAO.findAll());
            view.locDanhSachNhanVien();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách nhân viên: " + exception.getMessage());
        }
    }

    private void taiDanhSachChucVu() {
        try {
            List<ChucVu> danhSachChucVu = chucVuDAO.findAll();
            view.hienThiDanhSachChucVu(danhSachChucVu);
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách chức vụ: " + exception.getMessage());
        }
    }

    private void themNhanVien() {
        if (!view.validateForm()) {
            return;
        }
        NhanVien nhanVien = view.taoNhanVien();
        try {
            if (new HashSet<>(nhanVienDAO.findAll()).contains(new NhanVien(nhanVien.getMaNV()))) {
                view.hienThiLoi("Mã nhân viên đã tồn tại.");
                return;
            }
            if (!nhanVienDAO.insert(nhanVien)) {
                view.hienThiLoi("Không thêm được nhân viên vào cơ sở dữ liệu.");
                return;
            }
            taiDanhSachNhanVien();
            lamMoiForm();
            view.thongBaoThanhCong("Đã thêm nhân viên thành công.", "Thêm nhân viên");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể thêm nhân viên: " + exception.getMessage());
        }
    }

    private void capNhatNhanVien() {
        String maNV = view.getMaNVDangChon();
        if (maNV == null) {
            view.hienThiLoi("Vui lòng chọn hoặc tìm một nhân viên trước khi cập nhật.");
            return;
        }
        if (!view.validateForm()) {
            return;
        }
        NhanVien nhanVien = view.taoNhanVien();
        nhanVien.setMaNV(maNV);
        try {
            if (!nhanVienDAO.update(nhanVien)) {
                view.hienThiLoi("Không cập nhật được nhân viên trong cơ sở dữ liệu.");
                return;
            }
            taiDanhSachNhanVien();
            view.chonNhanVienTheoMa(maNV);
            view.thongBaoThanhCong("Đã cập nhật nhân viên thành công.", "Cập nhật nhân viên");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật nhân viên: " + exception.getMessage());
        }
    }

    private void xoaNhanVien() {
        String maNV = view.getMaNVDangChon();
        if (maNV == null) {
            view.hienThiLoi("Vui lòng chọn một nhân viên để xóa.");
            return;
        }
        if (JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa nhân viên có mã: " + maNV + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!nhanVienDAO.deleteById(maNV)) {
                view.hienThiLoi("Không xóa được nhân viên khỏi cơ sở dữ liệu.");
                return;
            }
            taiDanhSachNhanVien();
            lamMoiForm();
            view.thongBaoThanhCong("Đã xóa nhân viên thành công.", "Xóa nhân viên");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa nhân viên: " + exception.getMessage());
        }
    }

    private void lamMoiForm() {
        try {
            List<NhanVien> danhSach = nhanVienDAO.findAll();
            view.lamMoiBieuMau(util.MaTuDong.sinhMa(
                    util.MaTuDong.TIEN_TO_NHAN_VIEN, danhSach, NhanVien::getMaNV));
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể phát sinh mã nhân viên: " + exception.getMessage());
            view.lamMoiBieuMau("");
        }
        taiDanhSachNhanVien();
    }
}

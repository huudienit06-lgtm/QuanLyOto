package controller;

import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import gui.FrmQuanLyTaiKhoan;
import java.sql.SQLException;
import java.util.HashSet;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class TaiKhoanController {
    private final FrmQuanLyTaiKhoan view;
    private final TaiKhoanDAO taiKhoanDAO;

    public TaiKhoanController(FrmQuanLyTaiKhoan view) {
        this(view, new TaiKhoanDAO());
    }

    TaiKhoanController(FrmQuanLyTaiKhoan view, TaiKhoanDAO taiKhoanDAO) {
        this.view = view;
        this.taiKhoanDAO = taiKhoanDAO;
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themTaiKhoan());
        view.getBtnCapNhat().addActionListener(event -> capNhatTaiKhoan());
        view.getBtnXoa().addActionListener(event -> xoaTaiKhoan());
        view.getBtnDatLaiMatKhau().addActionListener(event -> datLaiMatKhau());
        view.getBtnLamMoi().addActionListener(event -> lamMoiForm());
        view.getBtnTaiLai().addActionListener(event -> taiDanhSachTaiKhoan());
        view.getChkHienMatKhau().addActionListener(event -> view.capNhatHienThiMatKhau());
        view.getTblTaiKhoan().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                hienThiTaiKhoanDangChon();
            }
        });
        themBoLangNgheTimKiem(view.getTxtLocMaNV());
        themBoLangNgheTimKiem(view.getTxtLocTenDangNhap());

        taiDanhSachTaiKhoan();
        lamMoiForm();
    }

    private void themBoLangNgheTimKiem(javax.swing.JTextField txtTimKiem) {
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                view.locDanhSachTaiKhoan();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                view.locDanhSachTaiKhoan();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                view.locDanhSachTaiKhoan();
            }
        });
    }

    private void taiDanhSachTaiKhoan() {
        try {
            view.hienThiDanhSachTaiKhoan(taiKhoanDAO.findAll());
        } catch (SQLException exception) {
            view.hienThiLoi("Lỗi dữ liệu",
                    "Không thể tải danh sách tài khoản: " + exception.getMessage());
        }
    }

    private void hienThiTaiKhoanDangChon() {
        String maNV = view.getMaNVDangChon();
        if (maNV == null) {
            return;
        }
        try {
            TaiKhoan taiKhoan = taiKhoanDAO.findById(maNV);
            if (taiKhoan != null) {
                view.hienThiTaiKhoan(taiKhoan);
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Lỗi dữ liệu",
                    "Không thể lấy thông tin tài khoản: " + exception.getMessage());
        }
    }

    private void lamMoiForm() {
        view.lamMoiForm();
    }

    private void themTaiKhoan() {
        if (!view.validateForm(true)) {
            return;
        }

        String maNV = view.getMaNVNhap();
        try {
            if (new HashSet<>(taiKhoanDAO.findAll()).contains(new TaiKhoan(new NhanVien(maNV)))) {
                view.hienThiCanhBao("Tài khoản đã tồn tại",
                        "Mã nhân viên này đã có tài khoản. Vui lòng chọn nhân viên khác hoặc cập nhật tài khoản hiện có.");
                return;
            }

            TaiKhoan taiKhoan = new TaiKhoan(
                    new NhanVien(maNV),
                    view.getTenDangNhapNhap(),
                    view.getMatKhauNhap(),
                    view.getVaiTroNhap(),
                    view.getTrangThaiNhap(),
                    null);
            if (taiKhoanDAO.insert(taiKhoan)) {
                view.thongBao("Đã tạo tài khoản cho nhân viên " + maNV);
                view.hienThiThanhCong("Tạo tài khoản thành công.");
                taiDanhSachTaiKhoan();
                lamMoiForm();
            } else {
                view.hienThiThatBai("Không thể tạo tài khoản.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Lỗi dữ liệu", "Không thể tạo tài khoản: " + exception.getMessage());
        }
    }

    private void capNhatTaiKhoan() {
        String maNV = view.getMaNVNhap();
        if (maNV.isEmpty()) {
            view.hienThiCanhBao("Thiếu thông tin", "Vui lòng chọn tài khoản cần cập nhật.");
            return;
        }
        if (!view.validateForm(false)) {
            return;
        }

        try {
            TaiKhoan taiKhoan = taiKhoanDAO.findById(maNV);
            if (taiKhoan == null) {
                view.hienThiCanhBao("Không tìm thấy", "Không tìm thấy tài khoản để cập nhật.");
                return;
            }

            taiKhoan.setTenDangNhap(view.getTenDangNhapNhap());
            taiKhoan.setVaiTro(view.getVaiTroNhap());
            taiKhoan.setTrangThai(view.getTrangThaiNhap());
            String matKhau = view.getMatKhauNhap();
            if (!matKhau.isEmpty()) {
                taiKhoan.setMatKhau(matKhau);
            }

            if (taiKhoanDAO.update(taiKhoan)) {
                view.thongBao("Đã cập nhật tài khoản: " + taiKhoan.getTenDangNhap());
                view.hienThiThanhCong("Cập nhật tài khoản thành công.");
                taiDanhSachTaiKhoan();
                lamMoiForm();
            } else {
                view.hienThiThatBai("Không thể cập nhật tài khoản.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Lỗi dữ liệu", "Không thể cập nhật tài khoản: " + exception.getMessage());
        }
    }

    private void xoaTaiKhoan() {
        String maNV = view.getMaNVNhap();
        if (maNV.isEmpty()) {
            view.hienThiCanhBao("Thiếu thông tin", "Vui lòng chọn tài khoản cần xóa.");
            return;
        }
        if (!view.xacNhanXoa(maNV)) {
            return;
        }

        try {
            if (taiKhoanDAO.deleteById(maNV)) {
                view.thongBao("Đã xóa tài khoản: " + maNV);
                view.hienThiThanhCong("Xóa tài khoản thành công.");
                taiDanhSachTaiKhoan();
                lamMoiForm();
            } else {
                view.hienThiThatBai("Không thể xóa tài khoản.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Lỗi dữ liệu", "Không thể xóa tài khoản: " + exception.getMessage());
        }
    }

    private void datLaiMatKhau() {
        String maNV = view.getMaNVNhap();
        if (maNV.isEmpty()) {
            view.hienThiCanhBao("Thiếu thông tin",
                    "Vui lòng chọn tài khoản cần đặt lại mật khẩu.");
            return;
        }
        if (!view.xacNhanDatLaiMatKhau(maNV)) {
            return;
        }

        try {
            TaiKhoan taiKhoan = taiKhoanDAO.findById(maNV);
            if (taiKhoan == null) {
                view.hienThiCanhBao("Không tìm thấy",
                        "Không tìm thấy tài khoản cần đặt lại mật khẩu.");
                return;
            }
            taiKhoan.setMatKhau("123456");
            if (taiKhoanDAO.update(taiKhoan)) {
                view.thongBao("Đã đặt lại mật khẩu cho: " + taiKhoan.getTenDangNhap());
                view.hienThiThanhCong(
                        "Đặt lại mật khẩu thành công. Mật khẩu mặc định mới: 123456");
                view.xoaMatKhauNhap();
            } else {
                view.hienThiThatBai("Không thể đặt lại mật khẩu.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Lỗi dữ liệu",
                    "Không thể đặt lại mật khẩu: " + exception.getMessage());
        }
    }
}

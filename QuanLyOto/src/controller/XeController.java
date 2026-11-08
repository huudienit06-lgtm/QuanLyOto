package controller;

import dao.KhachHangDAO;
import dao.XeDAO;
import entity.Xe;
import gui.FrmXe;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import util.MaTuDong;

public class XeController {
    private final FrmXe view;
    private final XeDAO xeDao;
    private final KhachHangDAO khachHangDao;

    public XeController(FrmXe view) {
        this.view = view;
        this.xeDao = new XeDAO();
        this.khachHangDao = new KhachHangDAO();
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themXe());
        view.getBtnSua().addActionListener(event -> capNhatXe());
        view.getBtnXoa().addActionListener(event -> xoaXe());
        view.getBtnLamMoi().addActionListener(event -> lamMoiForm());
        view.getBtnTaiLai().addActionListener(event -> {
            try {
                view.xoaBoLocXe();
                napDanhSachXe();
            } catch (SQLException exception) {
                view.hienThiLoi("Không thể tải danh sách xe: " + exception.getMessage());
            }
        });
        view.getTblXe().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && view.getTblXe().getSelectedRow() >= 0) {
                view.hienThiXeDaChon();
            }
        });
        napDuLieu();
    }

    private void napDuLieu() {
        try {
            view.capNhatDanhSachKhachHang(khachHangDao.findAll());
            view.hienThiDanhSach(xeDao.findAll());
            view.locDanhSach();
            lamMoiForm();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải dữ liệu quản lý xe: " + exception.getMessage());
        }
    }

    private void napDanhSachXe() throws SQLException {
        view.hienThiDanhSach(xeDao.findAll());
        view.locDanhSach();
    }

    private void themXe() {
        try {
            Xe xe = view.docThongTinXe();
            if (xeDao.insert(xe)) {
                napDanhSachXe();
                lamMoiForm();
                view.setThongBao("Đã thêm xe " + xe.getMaXe());
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể thêm xe: " + exception.getMessage());
        }
    }

    private void capNhatXe() {
        try {
            Xe xe = view.docThongTinXe();
            if (xeDao.update(xe)) {
                napDanhSachXe();
                view.setThongBao("Đã cập nhật xe " + xe.getMaXe());
            } else {
                view.hienThiLoi("Không tìm thấy xe cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật xe: " + exception.getMessage());
        }
    }

    private void xoaXe() {
        String maXe = view.getTxtMaXe().getText().trim();
        if (maXe.isEmpty()) {
            view.hienThiLoi("Hãy chọn xe trong bảng hoặc nhập mã xe cần xóa.");
            return;
        }
        if (JOptionPane.showConfirmDialog(view, "Bạn có chắc muốn xóa xe " + maXe + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (xeDao.deleteById(maXe)) {
                napDanhSachXe();
                lamMoiForm();
                view.setThongBao("Đã xóa xe " + maXe);
            } else {
                view.hienThiLoi("Không tìm thấy xe cần xóa.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa xe: " + exception.getMessage());
        }
    }

    private void lamMoiForm() {
        try {
            List<Xe> danhSachXe = xeDao.findAll();
            List<String> danhSachMa = new ArrayList<>();
            for (Xe xe : danhSachXe) {
                danhSachMa.add(xe.getMaXe());
            }
            view.setMaXe(MaTuDong.sinhMa(MaTuDong.TIEN_TO_XE, danhSachMa, ma -> ma));
            view.lamMoiForm();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể phát sinh mã xe: " + exception.getMessage());
        }
    }
}
package controller;

import dao.DanhMucXeDAO;
import entity.DongXe;
import entity.HangXe;
import gui.FrmHangXe;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class DanhMucXeController {
    private final FrmHangXe view;
    private final DanhMucXeDAO danhMucXeDAO;
    private List<HangXe> danhSachHangXe = List.of();

    public DanhMucXeController(FrmHangXe view) {
        this(view, new DanhMucXeDAO());
    }

    DanhMucXeController(FrmHangXe view, DanhMucXeDAO danhMucXeDAO) {
        this.view = view;
        this.danhMucXeDAO = danhMucXeDAO;
    }

    public void khoiTao() {
        view.getBtnThemHangXe().addActionListener(event -> themHangXe());
        view.getBtnCapNhatHangXe().addActionListener(event -> capNhatHangXe());
        view.getBtnXoaHangXe().addActionListener(event -> xoaHangXe());
        view.getBtnLamMoiHangXe().addActionListener(event -> view.lamMoiFormHangXe());
        view.getBtnThemDongXe().addActionListener(event -> themDongXe());
        view.getBtnCapNhatDongXe().addActionListener(event -> capNhatDongXe());
        view.getBtnXoaDongXe().addActionListener(event -> xoaDongXe());
        view.getBtnLamMoiDongXe().addActionListener(event -> view.lamMoiFormDongXe());
        view.getTxtTimKiemHangXe().getDocument().addDocumentListener(taoBoLangNgheTimKiem());
        view.getTblHangXe().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                chonHangXeTrenBang();
            }
        });
        view.getTblDongXe().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                chonDongXeTrenBang();
            }
        });
        taiDanhSach(null);
    }

    private void taiDanhSach(String tenHangXeCanChon) {
        try {
            danhSachHangXe = danhMucXeDAO.findAllHangXe();
            locDanhSachHangXe();
            if (tenHangXeCanChon == null && !danhSachHangXe.isEmpty()) {
                tenHangXeCanChon = danhSachHangXe.get(0).getTenHangXe();
            }
            hienThiHangXeDuocChon(tenHangXeCanChon);
            view.hienThiThongBao("Đã tải " + danhSachHangXe.size() + " hãng xe.");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách hãng xe: " + exception.getMessage());
        }
    }

    private void locDanhSachHangXe() {
        String tuKhoa = view.getTimKiemHangXe().toLowerCase(Locale.ROOT);
        List<HangXe> ketQua = new ArrayList<>();
        for (HangXe hangXe : danhSachHangXe) {
            if (hangXe.getTenHangXe().toLowerCase(Locale.ROOT).contains(tuKhoa)) {
                ketQua.add(hangXe);
            }
        }
        view.hienThiDanhSachHangXe(ketQua);
    }

    private void chonHangXeTrenBang() {
        int dongDuocChon = view.getTblHangXe().getSelectedRow();
        if (dongDuocChon < 0) {
            return;
        }
        String tenHangXe = String.valueOf(view.getTblHangXe().getValueAt(dongDuocChon, 0));
        hienThiHangXeDuocChon(tenHangXe);
    }

    private void hienThiHangXeDuocChon(String tenHangXe) {
        view.chonHangXe(tenHangXe);
        view.hienThiDanhSachDongXe(List.of());
        if (tenHangXe == null) {
            view.getTblDongXe().clearSelection();
            return;
        }
        try {
            view.hienThiDanhSachDongXe(danhMucXeDAO.findDongXeByHang(tenHangXe));
            view.getTblDongXe().clearSelection();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải các dòng xe thuộc hãng " + tenHangXe + ": "
                    + exception.getMessage());
        }
    }

    private void chonDongXeTrenBang() {
        int dongDuocChon = view.getTblDongXe().getSelectedRow();
        if (dongDuocChon < 0) {
            return;
        }
        view.chonDongXe(String.valueOf(view.getTblDongXe().getValueAt(dongDuocChon, 0)));
    }

    private void themHangXe() {
        try {
            HangXe hangXe = new HangXe(view.layTenHangXeNhap());
            danhMucXeDAO.themHangXe(hangXe);
            view.thongBaoDanhMucThayDoi();
            view.getTxtTimKiemHangXe().setText("");
            taiDanhSach(hangXe.getTenHangXe());
            view.hienThiThongBao("Đã thêm hãng xe " + hangXe.getTenHangXe() + ".");
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể thêm hãng xe: " + exception.getMessage());
        }
    }

    private void capNhatHangXe() {
        String tenHangXeCu = view.getTenHangXeDangChon();
        if (tenHangXeCu == null) {
            view.hienThiLoi("Hãy chọn hãng xe cần cập nhật.");
            return;
        }
        try {
            HangXe hangXe = new HangXe(view.layTenHangXeNhap());
            if (danhMucXeDAO.capNhatHangXe(tenHangXeCu, hangXe)) {
                view.thongBaoDanhMucThayDoi();
                view.getTxtTimKiemHangXe().setText("");
                taiDanhSach(hangXe.getTenHangXe());
                view.hienThiThongBao("Đã cập nhật hãng xe " + hangXe.getTenHangXe() + ".");
            } else {
                view.hienThiLoi("Không tìm thấy hãng xe cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật hãng xe: " + exception.getMessage());
        }
    }

    private void xoaHangXe() {
        String tenHangXe = view.getTenHangXeDangChon();
        if (tenHangXe == null) {
            view.hienThiLoi("Hãy chọn hãng xe cần xóa.");
            return;
        }
        if (!view.xacNhanXoa("Bạn có chắc muốn xóa hãng xe " + tenHangXe + "?")) {
            return;
        }
        try {
            if (danhMucXeDAO.coDongXe(tenHangXe)) {
                view.hienThiLoi("Hãy xóa các dòng xe thuộc hãng này trước.");
                return;
            }
            if (danhMucXeDAO.xoaHangXe(tenHangXe)) {
                view.thongBaoDanhMucThayDoi();
                taiDanhSach(null);
                view.hienThiThongBao("Đã xóa hãng xe " + tenHangXe + ".");
            } else {
                view.hienThiLoi("Không tìm thấy hãng xe cần xóa.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa hãng xe: " + exception.getMessage());
        }
    }

    private void themDongXe() {
        String tenHangXe = view.getTenHangXeDangChon();
        if (tenHangXe == null) {
            view.hienThiLoi("Hãy chọn hãng xe trước khi thêm dòng xe.");
            return;
        }
        try {
            DongXe dongXe = new DongXe(tenHangXe, view.layTenDongXeNhap());
            danhMucXeDAO.themDongXe(dongXe);
            view.thongBaoDanhMucThayDoi();
            hienThiHangXeDuocChon(tenHangXe);
            view.hienThiThongBao("Đã thêm dòng xe " + dongXe.getTenDongXe() + ".");
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể thêm dòng xe: " + exception.getMessage());
        }
    }

    private void capNhatDongXe() {
        String tenHangXe = view.getTenHangXeDangChon();
        String tenDongXeCu = view.getTenDongXeDangChon();
        if (tenHangXe == null || tenDongXeCu == null) {
            view.hienThiLoi("Hãy chọn dòng xe cần cập nhật.");
            return;
        }
        try {
            DongXe dongXe = new DongXe(tenHangXe, view.layTenDongXeNhap());
            if (danhMucXeDAO.capNhatDongXe(tenDongXeCu, dongXe)) {
                view.thongBaoDanhMucThayDoi();
                hienThiHangXeDuocChon(tenHangXe);
                view.hienThiThongBao("Đã cập nhật dòng xe " + dongXe.getTenDongXe() + ".");
            } else {
                view.hienThiLoi("Không tìm thấy dòng xe cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật dòng xe: " + exception.getMessage());
        }
    }

    private void xoaDongXe() {
        String tenHangXe = view.getTenHangXeDangChon();
        String tenDongXe = view.getTenDongXeDangChon();
        if (tenHangXe == null || tenDongXe == null) {
            view.hienThiLoi("Hãy chọn dòng xe cần xóa.");
            return;
        }
        if (!view.xacNhanXoa("Bạn có chắc muốn xóa dòng xe " + tenDongXe + "?")) {
            return;
        }
        try {
            if (danhMucXeDAO.coXe(tenHangXe, tenDongXe)) {
                view.hienThiLoi("Không thể xóa dòng xe đang được sử dụng trong danh sách xe.");
                return;
            }
            if (danhMucXeDAO.xoaDongXe(tenHangXe, tenDongXe)) {
                view.thongBaoDanhMucThayDoi();
                hienThiHangXeDuocChon(tenHangXe);
                view.hienThiThongBao("Đã xóa dòng xe " + tenDongXe + ".");
            } else {
                view.hienThiLoi("Không tìm thấy dòng xe cần xóa.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa dòng xe: " + exception.getMessage());
        }
    }

    private DocumentListener taoBoLangNgheTimKiem() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSachHangXe();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSachHangXe();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSachHangXe();
            }
        };
    }
}

package controller;

import dao.KhachHangDAO;
import dao.LichLamViecDAO;
import dao.PhieuKiemTraDAO;
import dao.PhieuTiepNhanDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.PhieuKiemTra;
import entity.PhieuTiepNhan;
import entity.Xe;
import gui.FrmPhieuKiemTra;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import util.MaTuDong;

public final class PhieuKiemTraController {
    private final FrmPhieuKiemTra view;
    private final PhieuKiemTraDAO phieuKiemTraDAO = new PhieuKiemTraDAO();
    private final PhieuTiepNhanDAO phieuTiepNhanDAO = new PhieuTiepNhanDAO();
    private final LichLamViecDAO lichLamViecDAO = new LichLamViecDAO();
    private final XeDAO xeDAO = new XeDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private List<PhieuKiemTra> danhSachPhieuKiemTra = new ArrayList<>();

    public PhieuKiemTraController(FrmPhieuKiemTra view) {
        this.view = view;
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themPhieuKiemTra());
        view.getBtnSua().addActionListener(event -> capNhatPhieuKiemTra());
        view.getBtnXoa().addActionListener(event -> xoaPhieuKiemTra());
        view.getBtnLamMoi().addActionListener(event -> lamMoiForm());
        view.getBtnTaiLai().addActionListener(event -> taiLaiDuLieu());
        view.getCboMaPTN().addActionListener(event -> view.capNhatThongTinTheoPhieuTiepNhan());
        view.getTblPhieuKiemTra().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                hienThiPhieuDuocChon();
            }
        });
        view.getTxtLocMaPKT().getDocument().addDocumentListener(
                taoBoLangNgheTimKiem(() -> view.locDanhSach()));
        view.getTxtLocMaPTN().getDocument().addDocumentListener(
                taoBoLangNgheTimKiem(() -> view.locDanhSach()));
        view.getTxtLocMaXe().getDocument().addDocumentListener(
                taoBoLangNgheTimKiem(() -> view.locDanhSach()));

        napDanhSachNhanVienTrongCa();
        napDanhSachPhieuKiemTra();
        lamMoiForm();
    }

    private javax.swing.event.DocumentListener taoBoLangNgheTimKiem(Runnable capNhat) {
        return new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                capNhat.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                capNhat.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                capNhat.run();
            }
        };
    }

    private void taiLaiDuLieu() {
        view.xoaBoLoc();
        napDanhSachNhanVienTrongCa();
        napDanhSachPhieuKiemTra();
        lamMoiForm();
    }

    private void napDanhSachNhanVienTrongCa() {
        view.hienThiDanhSachNhanVienTrongCa(List.of());
        try {
            view.hienThiDanhSachNhanVienTrongCa(
                    lichLamViecDAO.layNhanVienTrongCa(LocalDateTime.now()));
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách nhân viên đang trong ca: "
                    + exception.getMessage());
        }
    }

    private void napDanhSachPhieuTiepNhan() {
        try {
                List<PhieuTiepNhan> danhSachPhieuTiepNhan = phieuTiepNhanDAO.findAll().stream()
                    .filter(phieu -> "Đã tiếp nhận".equals(phieu.getTrangThai())
                        && PhieuTiepNhan.TIEN_DO_DA_TIEP_NHAN.equals(phieu.getTienDo()))
                    .toList();
            Map<String, String> bienSoTheoMaXe = new HashMap<>();
            for (Xe xe : xeDAO.findAll()) {
                bienSoTheoMaXe.put(xe.getMaXe(), xe.getBienSo());
            }
            Map<String, String> tenKhachHangTheoMaKH = new HashMap<>();
            for (KhachHang khachHang : khachHangDAO.findAll()) {
                tenKhachHangTheoMaKH.put(khachHang.getMaKH(), khachHang.getHoTen());
            }
            view.hienThiDanhSachPhieuTiepNhan(
                    danhSachPhieuTiepNhan, bienSoTheoMaXe, tenKhachHangTheoMaKH);
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách phiếu tiếp nhận: " + exception.getMessage());
        }
    }

    private void napDanhSachPhieuKiemTra() {
        try {
            danhSachPhieuKiemTra = phieuKiemTraDAO.findAll();
            view.hienThiDanhSach(danhSachPhieuKiemTra);
            view.thongBao("Đã tải " + danhSachPhieuKiemTra.size() + " phiếu kiểm tra");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách phiếu kiểm tra: " + exception.getMessage());
        }
    }

    private void themPhieuKiemTra() {
        try {
            PhieuKiemTra phieu = view.layPhieuKiemTraNhap();
            if (new HashSet<>(phieuKiemTraDAO.findAll())
                    .contains(new PhieuKiemTra(phieu.getMaPhieuKiemTra()))) {
                view.hienThiLoi("Mã phiếu kiểm tra đã tồn tại.");
                return;
            }
            if (phieuKiemTraDAO.insert(phieu)) {
                phieuTiepNhanDAO.capNhatTienDo(
                        phieu.getPhieuTiepNhan().getMaPhieuTiepNhan(),
                        PhieuTiepNhan.TIEN_DO_DA_LAP_PHIEU_KIEM_TRA);
                napDanhSachPhieuKiemTra();
                lamMoiForm();
                view.thongBao("Đã thêm phiếu kiểm tra " + phieu.getMaPhieuKiemTra());
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể thêm phiếu kiểm tra: " + exception.getMessage());
        }
    }

    private void capNhatPhieuKiemTra() {
        try {
            PhieuKiemTra phieu = view.layPhieuKiemTraNhap();
            if (phieuKiemTraDAO.update(phieu)) {
                napDanhSachPhieuKiemTra();
                view.thongBao("Đã cập nhật phiếu kiểm tra " + phieu.getMaPhieuKiemTra());
            } else {
                view.hienThiLoi("Không tìm thấy phiếu kiểm tra cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật phiếu kiểm tra: " + exception.getMessage());
        }
    }

    private void xoaPhieuKiemTra() {
        String maPhieu = view.getMaPhieuNhap().trim();
        if (maPhieu.isEmpty()) {
            view.hienThiLoi("Hãy chọn phiếu kiểm tra cần xóa.");
            return;
        }
        if (!view.xacNhanXoa(maPhieu)) {
            return;
        }

        try {
            if (phieuKiemTraDAO.deleteById(maPhieu)) {
                napDanhSachPhieuKiemTra();
                lamMoiForm();
                view.thongBao("Đã xóa phiếu kiểm tra " + maPhieu);
            } else {
                view.hienThiLoi("Không tìm thấy phiếu kiểm tra cần xóa.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa phiếu kiểm tra: " + exception.getMessage());
        }
    }

    private void hienThiPhieuDuocChon() {
        String maPhieu = view.getMaPhieuDangChon();
        if (maPhieu == null) {
            return;
        }
        for (PhieuKiemTra phieu : danhSachPhieuKiemTra) {
            if (maPhieu.equals(phieu.getMaPhieuKiemTra())) {
                view.hienThiPhieuKiemTra(phieu);
                return;
            }
        }
    }

    private void lamMoiForm() {
        String maMoi = "";
        try {
            maMoi = MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_PHIEU_KIEM_TRA,
                    phieuKiemTraDAO.findAll(),
                    PhieuKiemTra::getMaPhieuKiemTra);
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể phát sinh mã phiếu kiểm tra: " + exception.getMessage());
        }
        napDanhSachPhieuTiepNhan();
        view.lamMoiForm(maMoi);
    }
}

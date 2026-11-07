package controller;

import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.PhieuBanGiaoDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuBanGiao;
import entity.TaiKhoan;
import entity.Xe;
import gui.FrmBanGiaoXe;
import java.sql.SQLException;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import util.MaTuDong;

public final class BanGiaoXeController {
    private final FrmBanGiaoXe view;
    private final TaiKhoan taiKhoanHienTai;
    private final PhieuBanGiaoDAO phieuBanGiaoDAO = new PhieuBanGiaoDAO();
    private final XeDAO xeDAO = new XeDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public BanGiaoXeController(FrmBanGiaoXe view, TaiKhoan taiKhoanHienTai) {
        this.view = view;
        this.taiKhoanHienTai = taiKhoanHienTai;
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themPhieu());
        view.getBtnCapNhat().addActionListener(event -> capNhatPhieu());
        view.getBtnXoa().addActionListener(event -> xoaPhieu());
        view.getBtnLamMoi().addActionListener(event -> lamMoiForm());
        view.getBtnTaiLai().addActionListener(event -> taiLaiDuLieu());
        view.getCboXe().addActionListener(event -> capNhatThongTinKhachHang());
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                view.locDanhSach();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                view.locDanhSach();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                view.locDanhSach();
            }
        });
        view.getTblBanGiao().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                hienThiPhieuDangChon();
            }
        });
        taiLaiDuLieu();
    }

    private void taiLaiDuLieu() {
        taiDanhMuc();
        taiDanhSachPhieu();
        lamMoiForm();
    }

    private void taiDanhMuc() {
        try {
            List<Xe> danhSachXe = xeDAO.findAll();
            List<NhanVien> danhSachNhanVien = nhanVienDAO.findAll();
            view.hienThiDanhMuc(danhSachXe, danhSachNhanVien);
            capNhatThongTinKhachHang();
        } catch (SQLException exception) {
            view.thongBao("Không tải được danh mục xe/nhân viên");
            view.hienThiLoi("Không thể tải danh mục bàn giao: " + exception.getMessage());
        }
    }

    private void taiDanhSachPhieu() {
        try {
            List<PhieuBanGiao> danhSach = phieuBanGiaoDAO.findAll();
            view.hienThiDanhSachPhieu(danhSach);
            view.thongBao("Đã tải " + danhSach.size() + " phiếu bàn giao");
        } catch (SQLException exception) {
            view.thongBao("Không tải được danh sách phiếu");
            view.hienThiLoi("Không thể tải danh sách phiếu bàn giao: " + exception.getMessage());
        }
    }

    private void capNhatThongTinKhachHang() {
        Xe xe = view.getXeDuocChon();
        if (xe == null) {
            view.hienThiThongTinKhachHang(null);
            return;
        }
        try {
            KhachHang khachHang = khachHangDAO.findById(xe.getMaKH());
            view.hienThiThongTinKhachHang(khachHang);
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải thông tin khách hàng: " + exception.getMessage());
        }
    }

    private void themPhieu() {
        if (!view.xacThucForm()) {
            return;
        }
        try {
            PhieuBanGiao phieu = taoPhieuTuForm();
            if (phieuBanGiaoDAO.findAll().contains(new PhieuBanGiao(phieu.getMaPhieuBanGiao()))) {
                view.hienThiLoi("Mã phiếu bàn giao đã tồn tại.");
                return;
            }
            if (!phieuBanGiaoDAO.insert(phieu)) {
                throw new SQLException("Không có bản ghi nào được thêm.");
            }
            view.thongBao("Đã lập phiếu bàn giao " + phieu.getMaPhieuBanGiao());
            taiDanhSachPhieu();
            lamMoiForm();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể lập phiếu bàn giao: " + exception.getMessage());
        }
    }

    private void capNhatPhieu() {
        if (!view.xacThucForm()) {
            return;
        }
        try {
            if (!phieuBanGiaoDAO.update(taoPhieuTuForm())) {
                throw new SQLException("Không tìm thấy phiếu cần cập nhật.");
            }
            view.thongBao("Đã cập nhật phiếu bàn giao");
            taiDanhSachPhieu();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật phiếu bàn giao: " + exception.getMessage());
        }
    }

    private void xoaPhieu() {
        String maPhieu = view.getMaPhieuNhap();
        if (maPhieu.isEmpty()) {
            view.hienThiLoi("Vui lòng chọn phiếu cần xóa.");
            return;
        }
        try {
            if (!phieuBanGiaoDAO.deleteById(maPhieu)) {
                throw new SQLException("Không tìm thấy phiếu cần xóa.");
            }
            view.thongBao("Đã xóa phiếu bàn giao");
            taiDanhSachPhieu();
            lamMoiForm();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa phiếu bàn giao: " + exception.getMessage());
        }
    }

    private PhieuBanGiao taoPhieuTuForm() throws SQLException {
        Xe xe = view.getXeDuocChon();
        KhachHang khachHang = khachHangDAO.findById(xe.getMaKH());
        NhanVien nhanVien = view.getNhanVienDuocChon();
        return new PhieuBanGiao(
                view.getMaPhieuNhap(),
                xe,
                khachHang,
                nhanVien,
                view.getTinhTrangXeNhap(),
                view.getTenNguoiNhanNhap(),
                view.getSoDienThoaiNguoiNhanNhap());
    }

    private void hienThiPhieuDangChon() {
        String maPhieu = view.getMaPhieuDangChon();
        if (maPhieu == null) {
            return;
        }
        try {
            PhieuBanGiao phieu = phieuBanGiaoDAO.findById(maPhieu);
            if (phieu != null) {
                view.hienThiPhieu(phieu);
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải chi tiết phiếu: " + exception.getMessage());
        }
    }

    private void lamMoiForm() {
        String maPhieuMoi = "";
        try {
            maPhieuMoi = MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_PHIEU_BAN_GIAO,
                    phieuBanGiaoDAO.findAll(),
                    PhieuBanGiao::getMaPhieuBanGiao);
        } catch (SQLException exception) {
            view.thongBao("Không thể phát sinh mã phiếu bàn giao");
            view.hienThiLoi("Không thể phát sinh mã phiếu bàn giao: " + exception.getMessage());
        }
        String maNhanVien = taiKhoanHienTai == null || taiKhoanHienTai.getNhanVien() == null
                ? null
                : taiKhoanHienTai.getNhanVien().getMaNV();
        view.lamMoiBieuMau(maPhieuMoi, maNhanVien);
    }
}

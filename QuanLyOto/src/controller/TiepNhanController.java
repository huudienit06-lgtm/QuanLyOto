package controller;

import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.PhieuTiepNhanDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.Xe;
import gui.FrmTiepNhan;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.MaTuDong;

public final class TiepNhanController {
    private final FrmTiepNhan view;
    private final TaiKhoan taiKhoanHienTai;
    private final PhieuTiepNhanDAO phieuTiepNhanDAO = new PhieuTiepNhanDAO();
    private final XeDAO xeDAO = new XeDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final Map<String, KhachHang> khachHangTheoMa = new HashMap<>();
    private final Map<String, NhanVien> nhanVienTheoMa = new HashMap<>();
    private final Map<String, PhieuTiepNhan> phieuTheoMa = new HashMap<>();
    private NhanVien nhanVienDangNhap;
    private PhieuTiepNhan phieuDangChon;

    public TiepNhanController(FrmTiepNhan view, TaiKhoan taiKhoanHienTai) {
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
        view.getTxtTimKiem().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                view.locDanhSach();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                view.locDanhSach();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                view.locDanhSach();
            }
        });
        view.getTblTiepNhan().getSelectionModel().addListSelectionListener(event -> {
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
            List<KhachHang> danhSachKhachHang = khachHangDAO.findAll();

            view.hienThiDanhSachXe(danhSachXe);
            nhanVienTheoMa.clear();
            for (NhanVien nhanVien : danhSachNhanVien) {
                nhanVienTheoMa.put(nhanVien.getMaNV(), nhanVien);
            }
            String maNV = taiKhoanHienTai == null || taiKhoanHienTai.getNhanVien() == null
                    ? null : taiKhoanHienTai.getNhanVien().getMaNV();
            nhanVienDangNhap = timNhanVien(danhSachNhanVien, maNV);
            khachHangTheoMa.clear();
            for (KhachHang khachHang : danhSachKhachHang) {
                khachHangTheoMa.put(khachHang.getMaKH(), khachHang);
            }
            capNhatThongTinKhachHang();
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh mục xe, khách hàng, nhân viên: "
                    + exception.getMessage());
        }
    }

    private void taiDanhSachPhieu() {
        try {
            List<PhieuTiepNhan> danhSach = phieuTiepNhanDAO.findAll();
            phieuTheoMa.clear();
            for (PhieuTiepNhan phieu : danhSach) {
                phieuTheoMa.put(phieu.getMaPhieuTiepNhan(), phieu);
            }
            view.hienThiDanhSachPhieu(danhSach);
            view.thongBao("Đã tải " + danhSach.size() + " phiếu tiếp nhận");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách phiếu tiếp nhận: " + exception.getMessage());
        }
    }

    private void capNhatThongTinKhachHang() {
        Xe xe = view.getXeDuocChon();
        view.hienThiThongTinKhachHang(xe == null ? null : khachHangTheoMa.get(xe.getMaKH()));
    }

    private void themPhieu() {
        try {
            PhieuTiepNhan phieu = taoPhieuTuForm(false);
            if (phieuTheoMa.containsKey(phieu.getMaPhieuTiepNhan())) {
                view.hienThiLoi("Mã phiếu tiếp nhận đã tồn tại.");
                return;
            }
            if (phieuTiepNhanDAO.insert(phieu)) {
                taiDanhSachPhieu();
                lamMoiForm();
                view.thongBao("Đã lập phiếu tiếp nhận " + phieu.getMaPhieuTiepNhan());
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể lập phiếu tiếp nhận: " + exception.getMessage());
        }
    }

    private void capNhatPhieu() {
        try {
            PhieuTiepNhan phieu = taoPhieuTuForm(true);
            if (phieuTiepNhanDAO.update(phieu)) {
                taiDanhSachPhieu();
                view.thongBao("Đã cập nhật phiếu tiếp nhận " + phieu.getMaPhieuTiepNhan());
            } else {
                view.hienThiLoi("Không tìm thấy phiếu tiếp nhận cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật phiếu tiếp nhận: " + exception.getMessage());
        }
    }

    private void xoaPhieu() {
        String maPhieu = view.getMaPhieuNhap();
        if (maPhieu.isEmpty()) {
            view.hienThiLoi("Hãy chọn phiếu tiếp nhận cần xóa.");
            return;
        }
        if (!view.xacNhanXoa(maPhieu)) {
            return;
        }
        try {
            if (phieuTiepNhanDAO.deleteById(maPhieu)) {
                taiDanhSachPhieu();
                lamMoiForm();
                view.thongBao("Đã xóa phiếu tiếp nhận " + maPhieu);
            } else {
                view.hienThiLoi("Không tìm thấy phiếu tiếp nhận cần xóa.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa phiếu tiếp nhận. Phiếu có thể đang được phiếu kiểm tra sử dụng: "
                    + exception.getMessage());
        }
    }

    private PhieuTiepNhan taoPhieuTuForm(boolean capNhat) {
        String maPhieu = view.getMaPhieuNhap();
        Xe xe = view.getXeDuocChon();
        String trangThai = view.getTrangThaiNhap();
        if (maPhieu.isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không được để trống.");
        }
        if (maPhieu.length() > 20) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không được vượt quá 20 ký tự.");
        }
        if (xe == null) {
            throw new IllegalArgumentException("Hãy chọn xe cần tiếp nhận.");
        }
        if (nhanVienDangNhap == null) {
            throw new IllegalArgumentException(
                    "Không xác định được nhân viên đang đăng nhập. Hãy đăng nhập lại trước khi lập phiếu.");
        }
        KhachHang khachHang = khachHangTheoMa.get(xe.getMaKH());
        if (khachHang == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy khách hàng sở hữu xe đã chọn. Hãy tải lại danh mục.");
        }
        if (trangThai.isEmpty()) {
            throw new IllegalArgumentException("Trạng thái tiếp nhận không được để trống.");
        }
        if (trangThai.length() > 20) {
            throw new IllegalArgumentException("Trạng thái tiếp nhận không được vượt quá 20 ký tự.");
        }
        if (capNhat && phieuDangChon == null) {
            throw new IllegalArgumentException("Hãy chọn phiếu tiếp nhận cần cập nhật.");
        }
        String ghiChu = view.getGhiChuNhap();
        Timestamp ngayLap = capNhat
                ? phieuDangChon.getNgayLap()
                : Timestamp.valueOf(LocalDateTime.now());
        String tienDo = capNhat && phieuDangChon != null
            ? phieuDangChon.getTienDo()
            : PhieuTiepNhan.TIEN_DO_DA_TIEP_NHAN;
        return new PhieuTiepNhan(maPhieu, xe, khachHang, nhanVienDangNhap, ngayLap,
            trangThai, tienDo, ghiChu.isEmpty() ? null : ghiChu);
    }

    private void hienThiPhieuDangChon() {
        String maPhieu = view.getMaPhieuDangChon();
        phieuDangChon = maPhieu == null ? null : phieuTheoMa.get(maPhieu);
        if (phieuDangChon != null) {
            view.hienThiPhieu(phieuDangChon, nhanVienDangNhap);
        }
    }

    private void lamMoiForm() {
        phieuDangChon = null;
        String maMoi = "";
        try {
            maMoi = MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_PHIEU_TIEP_NHAN,
                    phieuTiepNhanDAO.findAll(),
                    PhieuTiepNhan::getMaPhieuTiepNhan);
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể phát sinh mã phiếu tiếp nhận: " + exception.getMessage());
        }
        view.lamMoiBieuMau(maMoi, nhanVienDangNhap);
    }

    private NhanVien timNhanVien(List<NhanVien> danhSach, String maNV) {
        if (maNV == null || maNV.isBlank()) {
            return null;
        }
        return danhSach.stream()
                .filter(nhanVien -> maNV.trim().equals(nhanVien.getMaNV()))
                .findFirst()
                .orElse(null);
    }
}

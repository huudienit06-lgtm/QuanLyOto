package controller;

import dao.ChiTietPhieuSuaChuaDAO;
import dao.DichVuDAO;
import dao.PhieuKiemTraDAO;
import dao.PhieuSuaChuaDAO;
import dao.PhieuTiepNhanDAO;
import dao.PhuTungDAO;
import dao.XeDAO;
import entity.ChiTietPhieuSuaChua;
import entity.DichVu;
import entity.PhieuKiemTra;
import entity.PhieuSuaChua;
import entity.PhieuTiepNhan;
import entity.PhuTung;
import entity.TaiKhoan;
import entity.Xe;
import gui.FrmPhieuSuaChua;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.MaTuDong;

public final class PhieuSuaChuaController {
    private final FrmPhieuSuaChua view;
    private final TaiKhoan taiKhoanHienTai;
    private final PhieuSuaChuaDAO phieuSuaChuaDAO = new PhieuSuaChuaDAO();
    private final ChiTietPhieuSuaChuaDAO chiTietDAO = new ChiTietPhieuSuaChuaDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final PhuTungDAO phuTungDAO = new PhuTungDAO();
    private final PhieuKiemTraDAO phieuKiemTraDAO = new PhieuKiemTraDAO();
    private final PhieuTiepNhanDAO phieuTiepNhanDAO = new PhieuTiepNhanDAO();
    private final XeDAO xeDAO = new XeDAO();

    public PhieuSuaChuaController(FrmPhieuSuaChua view, TaiKhoan taiKhoanHienTai) {
        this.view = view;
        this.taiKhoanHienTai = taiKhoanHienTai;
    }

    public void khoiTao() {
        view.layNutThemPhieu().addActionListener(event -> themPhieu());
        view.layNutCapNhatPhieu().addActionListener(event -> capNhatPhieu());
        view.layNutXoaPhieu().addActionListener(event -> xoaPhieu());
        view.layNutLamMoiPhieu().addActionListener(event -> lamMoiForm());
        view.layNutTaiLai().addActionListener(event -> {
            view.xoaBoLoc();
            taiLaiDuLieu();
        });
        view.layNutThemChiTiet().addActionListener(event -> themChiTiet());
        view.layNutXoaChiTiet().addActionListener(event -> xoaChiTiet());
        view.layBangPhieuSuaChua().getSelectionModel().addListSelectionListener(event -> {
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
            view.hienThiDanhSachDichVu(dichVuDAO.findAll());
            view.hienThiDanhSachPhuTung(phuTungDAO.findAll());
                List<PhieuKiemTra> danhSachPhieuKiemTra =
                    phieuKiemTraDAO.findAllDuDieuKienLapPhieuSuaChua();
            Map<String, String> bienSoTheoMaXe = new HashMap<>();
            for (Xe xe : xeDAO.findAll()) {
                bienSoTheoMaXe.put(xe.getMaXe(), xe.getBienSo());
            }
            view.hienThiDanhSachPhieuKiemTra(danhSachPhieuKiemTra, bienSoTheoMaXe);
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải danh mục phiếu sửa chữa", exception.getMessage());
        }
    }

    private void taiDanhSachPhieu() {
        try {
            List<PhieuSuaChua> danhSach = phieuSuaChuaDAO.findAll();
            view.hienThiDanhSachPhieuSuaChua(danhSach);
            view.thongBao("Đã tải " + danhSach.size() + " phiếu sửa chữa");
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải danh sách phiếu sửa chữa", exception.getMessage());
        }
    }

    private void themPhieu() {
        try {
            PhieuSuaChua phieu = view.layPhieuNhap();
            if (phieuSuaChuaDAO.insert(phieu)) {
                phieuTiepNhanDAO.capNhatTienDoTheoPhieuKiemTra(
                        phieu.getMaPhieuKiemTra().getMaPhieuKiemTra(),
                        PhieuTiepNhan.TIEN_DO_DA_LAP_PHIEU_SUA_CHUA);
                taiDanhMuc();
                taiDanhSachPhieu();
                lamMoiForm();
                view.thongBaoThanhCong("Đã tạo phiếu " + phieu.getMaPhieuSuaChua());
            }
        } catch (IllegalArgumentException exception) {
            view.baoLoi("Dữ liệu phiếu sửa chữa không hợp lệ", exception.getMessage());
        } catch (SQLException exception) {
            view.baoLoi("Không thể tạo phiếu sửa chữa", exception.getMessage());
        }
    }

    private void capNhatPhieu() {
        try {
            if (!phieuSuaChuaDAO.update(view.layPhieuNhap())) {
                view.baoLoi("Không tìm thấy phiếu cần cập nhật", null);
                return;
            }
            taiDanhSachPhieu();
            view.thongBaoThanhCong("Đã cập nhật phiếu sửa chữa");
        } catch (IllegalArgumentException exception) {
            view.baoLoi("Dữ liệu phiếu sửa chữa không hợp lệ", exception.getMessage());
        } catch (SQLException exception) {
            view.baoLoi("Không thể cập nhật phiếu sửa chữa", exception.getMessage());
        }
    }

    private void xoaPhieu() {
        String maPSC = view.layMaPhieuNhap();
        if (!view.xacNhanXoaPhieu(maPSC)) {
            return;
        }
        try {
            if (!phieuSuaChuaDAO.deleteById(maPSC)) {
                view.baoLoi("Không tìm thấy phiếu cần xóa", null);
                return;
            }
            taiDanhSachPhieu();
            lamMoiForm();
            view.thongBaoThanhCong("Đã xóa phiếu " + maPSC);
        } catch (SQLException exception) {
            view.baoLoi("Không thể xóa phiếu sửa chữa", exception.getMessage());
        }
    }

    private void hienThiPhieuDangChon() {
        view.hienThiPhieuDangChon();
        String maPSC = view.layMaPhieuNhap();
        if (maPSC.isEmpty()) {
            return;
        }
        try {
            PhieuSuaChua phieu = phieuSuaChuaDAO.findById(maPSC);
            if (phieu != null && phieu.getMaPhieuKiemTra() != null) {
                PhieuKiemTra phieuKiemTra = phieuKiemTraDAO.findById(
                        phieu.getMaPhieuKiemTra().getMaPhieuKiemTra());
                if (phieuKiemTra != null) {
                    view.chonPhieuKiemTraDangChon(phieuKiemTra);
                }
            }
            List<ChiTietPhieuSuaChua> danhSach = chiTietDAO.findByField("maPhieuSuaChua", maPSC);
            Map<String, PhuTung> phuTungTheoMa = new HashMap<>();
            Map<String, DichVu> dichVuTheoMa = new HashMap<>();
            for (ChiTietPhieuSuaChua chiTiet : danhSach) {
                if (chiTiet.getMaPhuTung() != null) {
                    PhuTung phuTung = phuTungDAO.findById(chiTiet.getMaPhuTung());
                    if (phuTung != null) {
                        phuTungTheoMa.put(phuTung.getMaPhuTung(), phuTung);
                    }
                }
                if (chiTiet.getMaDV() != null) {
                    DichVu dichVu = dichVuDAO.findById(chiTiet.getMaDV());
                    if (dichVu != null) {
                        dichVuTheoMa.put(dichVu.getMaDV(), dichVu);
                    }
                }
            }
            view.hienThiChiTiet(danhSach, phuTungTheoMa, dichVuTheoMa);
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải chi tiết phiếu sửa chữa", exception.getMessage());
        }
    }

    private void themChiTiet() {
        try {
            if (chiTietDAO.insert(view.layChiTietNhap())) {
                hienThiPhieuDangChon();
                view.thongBaoThanhCong("Đã thêm chi tiết vào phiếu");
            }
        } catch (IllegalArgumentException exception) {
            view.baoLoi("Dữ liệu chi tiết không hợp lệ", exception.getMessage());
        } catch (SQLException exception) {
            view.baoLoi("Không thể thêm chi tiết", exception.getMessage());
        }
    }

    private void xoaChiTiet() {
        if (!view.xacNhanXoaChiTiet()) {
            return;
        }
        try {
            if (!chiTietDAO.deleteById(view.layMaChiTietDangChon())) {
                view.baoLoi("Không tìm thấy chi tiết cần xóa", null);
                return;
            }
            hienThiPhieuDangChon();
            view.lamMoiFormChiTiet();
            view.thongBaoThanhCong("Đã xóa chi tiết");
        } catch (SQLException exception) {
            view.baoLoi("Không thể xóa chi tiết", exception.getMessage());
        }
    }

    private void lamMoiForm() {
        try {
            String maMoi = MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_PHIEU_SUA_CHUA,
                    phieuSuaChuaDAO.findAll(),
                    PhieuSuaChua::getMaPhieuSuaChua);
            String maNV = taiKhoanHienTai == null || taiKhoanHienTai.getNhanVien() == null
                    ? ""
                    : taiKhoanHienTai.getNhanVien().getMaNV();
            view.lamMoiForm(maMoi, maNV);
            view.lamMoiFormChiTiet();
        } catch (SQLException exception) {
            view.baoLoi("Không thể phát sinh mã phiếu sửa chữa", exception.getMessage());
            view.lamMoiForm("", "");
        }
    }
}
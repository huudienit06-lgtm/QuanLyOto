package controller;

import dao.PhieuYeuCauBaoHanhDAO;
import dao.PhieuYeuCauBaoHanhDAO.LuaChonHoaDon;
import dao.PhieuYeuCauBaoHanhDAO.LuaChonPhuTung;
import dao.PhieuYeuCauBaoHanhDAO.YeuCauHienThi;
import entity.PhieuYeuCauBaoHanh;
import gui.FrmTiepNhanBaoHanh;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;

public final class TiepNhanBaoHanhController {
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FrmTiepNhanBaoHanh view;
    private final PhieuYeuCauBaoHanhDAO phieuYeuCauBaoHanhDAO = new PhieuYeuCauBaoHanhDAO();
    private final List<YeuCauHienThi> dsYeuCau = new ArrayList<>();
    private boolean dangNapHoaDon;
    private boolean dangTaoMoi = true;
    private boolean baoHanhConHan;

    public TiepNhanBaoHanhController(FrmTiepNhanBaoHanh view) {
        this.view = view;
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themYeuCau());
        view.getBtnCapNhat().addActionListener(event -> capNhatYeuCau());
        view.getBtnXoa().addActionListener(event -> xoaYeuCau());
        view.getBtnChapNhan().addActionListener(event -> chapNhanYeuCau());
        view.getBtnHoanThanh().addActionListener(event -> hoanThanhYeuCau());
        view.getBtnTuChoi().addActionListener(event -> tuChoiYeuCau());
        view.getBtnLamMoi().addActionListener(event -> lamMoiForm());
        view.getBtnTimKiem().addActionListener(event -> taiDanhSachYeuCau());
        view.getBtnTaiLai().addActionListener(event -> {
            view.xoaTuKhoa();
            taiDanhSachHoaDon();
            taiDanhSachYeuCau();
        });
        view.getCboHoaDon().addActionListener(event -> {
            if (!dangNapHoaDon) {
                taiPhuTungTheoHoaDon();
            }
        });
        view.getCboPhuTung().addActionListener(event -> capNhatGioiHanSoLuong());
        view.getTblYeuCau().getSelectionModel().addListSelectionListener(this::xuLyChonYeuCau);
        view.getTxtTimKiem().addActionListener(event -> taiDanhSachYeuCau());
        view.capNhatTrangThaiNut(true, false, false);
        view.lamMoiBieuMau(layMaYeuCauMoi());
        taiDanhSachHoaDon();
        taiDanhSachYeuCau();
    }

    private void taiDanhSachHoaDon() {
        dangNapHoaDon = true;
        try {
            view.hienThiDanhSachHoaDon(phieuYeuCauBaoHanhDAO.layHoaDonCoBaoHanh());
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải hóa đơn có phụ tùng được bảo hành", exception);
        } finally {
            dangNapHoaDon = false;
        }
        taiPhuTungTheoHoaDon();
    }

    private void taiPhuTungTheoHoaDon() {
        view.hienThiDanhSachPhuTung(List.of());
        LuaChonHoaDon hoaDon = view.getHoaDonDuocChon();
        if (hoaDon == null) {
            capNhatGioiHanSoLuong();
            return;
        }
        try {
            view.hienThiDanhSachPhuTung(
                    phieuYeuCauBaoHanhDAO.layPhuTungTheoHoaDon(hoaDon.maHoaDon()));
            capNhatGioiHanSoLuong();
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải phụ tùng được bảo hành của hóa đơn", exception);
        }
    }

    private void capNhatGioiHanSoLuong() {
        LuaChonPhuTung phuTung = view.getPhuTungDuocChon();
        view.capNhatGioiHanSoLuong(phuTung == null ? 1 : Math.max(1, phuTung.soLuongHoaDon()));
        capNhatKetQuaBaoHanh();
    }

    private void capNhatKetQuaBaoHanh() {
        LuaChonHoaDon hoaDon = view.getHoaDonDuocChon();
        LuaChonPhuTung phuTung = view.getPhuTungDuocChon();
        baoHanhConHan = false;
        String ketQua;
        if (hoaDon == null || phuTung == null) {
            ketQua = "Chọn hóa đơn và phụ tùng";
        } else if (hoaDon.ngayLap() == null || phuTung.thoiHanBaoHanh() <= 0) {
            ketQua = "Không có dữ liệu thời hạn bảo hành";
        } else {
            LocalDate ngayHetHan = hoaDon.ngayLap().toLocalDateTime().toLocalDate();
            ngayHetHan = switch (phuTung.donViThoiGian()) {
                case "NGAY" -> ngayHetHan.plusDays(phuTung.thoiHanBaoHanh());
                case "NAM" -> ngayHetHan.plusYears(phuTung.thoiHanBaoHanh());
                default -> ngayHetHan.plusMonths(phuTung.thoiHanBaoHanh());
            };
            baoHanhConHan = !LocalDate.now().isAfter(ngayHetHan);
            ketQua = (baoHanhConHan ? "Còn hạn đến " : "Đã hết hạn từ ")
                    + ngayHetHan.format(DINH_DANG_NGAY);
        }
        view.hienThiKetQuaBaoHanh(ketQua, baoHanhConHan);
        view.capNhatTrangThaiNut(dangTaoMoi, false, false);
    }

    private void taiDanhSachYeuCau() {
        try {
            dsYeuCau.clear();
            dsYeuCau.addAll(phieuYeuCauBaoHanhDAO.timKiem(view.getTuKhoa()));
            view.hienThiDanhSachYeuCau(dsYeuCau);
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải danh sách yêu cầu bảo hành", exception);
        }
    }

    private void xuLyChonYeuCau(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        int row = view.getDongDangChon();
        if (row < 0 || row >= dsYeuCau.size()) {
            return;
        }
        YeuCauHienThi yeuCau = dsYeuCau.get(row);
        dangTaoMoi = false;
        view.chonHoaDon(yeuCau.maHoaDon());
        LuaChonPhuTung phuTung = view.timPhuTung(yeuCau.maPhuTung());
        if (phuTung == null) {
            phuTung = new LuaChonPhuTung(
                    yeuCau.maPhuTung(), yeuCau.tenPhuTung(), yeuCau.soLuong(), 0, "");
            view.themPhuTung(phuTung);
        }
        boolean choXuLy = PhieuYeuCauBaoHanhDAO.CHO_XU_LY.equals(yeuCau.trangThai());
        boolean daChapNhan = PhieuYeuCauBaoHanhDAO.CHAP_NHAN.equals(yeuCau.trangThai());
        view.hienThiYeuCau(yeuCau, phuTung, choXuLy, daChapNhan);
        capNhatKetQuaBaoHanh();
        view.capNhatTrangThaiNut(false, choXuLy, daChapNhan);
    }

    private void themYeuCau() {
        if (!view.kiemTraThem(baoHanhConHan)) {
            return;
        }
        PhieuYeuCauBaoHanh yeuCau = view.taoYeuCauNhap(view.getMaYeuCauMoi());
        try {
            phieuYeuCauBaoHanhDAO.them(yeuCau);
            view.xoaTuKhoa();
            taiDanhSachYeuCau();
            view.chonYeuCau(yeuCau.getMaPhieuYCBH());
            view.thongBao("Đã tiếp nhận yêu cầu " + yeuCau.getMaPhieuYCBH()
                    + " với trạng thái Chờ xử lý.", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            view.baoLoi("Không thể tiếp nhận yêu cầu bảo hành", exception);
        }
    }

    private void capNhatYeuCau() {
        YeuCauHienThi yeuCauDangChon = layYeuCauDangChon();
        if (yeuCauDangChon == null) {
            return;
        }
        if (!PhieuYeuCauBaoHanhDAO.CHO_XU_LY.equals(yeuCauDangChon.trangThai())) {
            view.hienThiLoi("Chỉ có thể cập nhật yêu cầu đang chờ xử lý.");
            return;
        }
        if (!view.kiemTraLyDo()) {
            return;
        }
        PhieuYeuCauBaoHanh yeuCau = view.taoYeuCauNhap(yeuCauDangChon.maPhieuYCBH());
        try {
            phieuYeuCauBaoHanhDAO.capNhatChoXuLy(yeuCau);
            taiDanhSachYeuCau();
            view.chonYeuCau(yeuCau.getMaPhieuYCBH());
            view.thongBao("Đã cập nhật yêu cầu bảo hành.", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            view.baoLoi("Không thể cập nhật yêu cầu bảo hành", exception);
        }
    }

    private void xoaYeuCau() {
        YeuCauHienThi yeuCau = layYeuCauDangChon();
        if (yeuCau == null) {
            return;
        }
        if (!PhieuYeuCauBaoHanhDAO.CHO_XU_LY.equals(yeuCau.trangThai())) {
            view.hienThiLoi("Chỉ có thể xóa yêu cầu đang chờ xử lý.");
            return;
        }
        if (!view.xacNhanXoa(yeuCau.maPhieuYCBH())) {
            return;
        }
        try {
            if (!phieuYeuCauBaoHanhDAO.xoaChoXuLy(yeuCau.maPhieuYCBH())) {
                throw new SQLException("Yêu cầu đã được xử lý hoặc không còn tồn tại.");
            }
            lamMoiForm();
            taiDanhSachYeuCau();
        } catch (SQLException exception) {
            view.baoLoi("Không thể xóa yêu cầu bảo hành", exception);
        }
    }

    private void chapNhanYeuCau() {
        YeuCauHienThi yeuCau = layYeuCauDangChon();
        if (yeuCau == null || !xacNhanTrangThai(yeuCau, PhieuYeuCauBaoHanhDAO.CHO_XU_LY,
                "Chấp nhận yêu cầu này? Số lượng phụ tùng sẽ được cộng vào tồn kho.")) {
            return;
        }
        try {
            phieuYeuCauBaoHanhDAO.chapNhan(
                    yeuCau.maPhieuYCBH(), view.getGhiChuXuLyNhap());
            taiDanhSachYeuCau();
            view.chonYeuCau(yeuCau.maPhieuYCBH());
            view.thongBao("Đã chấp nhận bảo hành và cập nhật tồn kho.",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            view.baoLoi("Không thể chấp nhận yêu cầu bảo hành", exception);
        }
    }

    private void tuChoiYeuCau() {
        YeuCauHienThi yeuCau = layYeuCauDangChon();
        if (yeuCau == null || !xacNhanTrangThai(
                yeuCau, PhieuYeuCauBaoHanhDAO.CHO_XU_LY, "Từ chối yêu cầu bảo hành này?")) {
            return;
        }
        if (!view.kiemTraGhiChu("Vui lòng ghi lý do từ chối trong phần Ghi chú xử lý.")) {
            return;
        }
        try {
            phieuYeuCauBaoHanhDAO.tuChoi(
                    yeuCau.maPhieuYCBH(), view.getGhiChuXuLyNhap());
            taiDanhSachYeuCau();
            view.chonYeuCau(yeuCau.maPhieuYCBH());
            view.thongBao("Đã từ chối yêu cầu bảo hành.",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            view.baoLoi("Không thể từ chối yêu cầu bảo hành", exception);
        }
    }

    private void hoanThanhYeuCau() {
        YeuCauHienThi yeuCau = layYeuCauDangChon();
        if (yeuCau == null || !xacNhanTrangThai(yeuCau, PhieuYeuCauBaoHanhDAO.CHAP_NHAN,
                "Đánh dấu yêu cầu này đã hoàn thành bảo hành?")) {
            return;
        }
        if (!view.kiemTraGhiChu("Vui lòng nhập ghi chú kết quả bảo hành.")) {
            return;
        }
        try {
            phieuYeuCauBaoHanhDAO.hoanThanh(
                    yeuCau.maPhieuYCBH(), view.getGhiChuXuLyNhap());
            taiDanhSachYeuCau();
            view.chonYeuCau(yeuCau.maPhieuYCBH());
            view.thongBao("Đã hoàn thành yêu cầu bảo hành.",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            view.baoLoi("Không thể hoàn thành yêu cầu bảo hành", exception);
        }
    }

    private boolean xacNhanTrangThai(
            YeuCauHienThi yeuCau, String trangThaiCanCo, String loiNhac) {
        if (!trangThaiCanCo.equals(yeuCau.trangThai())) {
            view.hienThiLoi("Yêu cầu không ở trạng thái phù hợp để thực hiện thao tác này.");
            return false;
        }
        return view.xacNhan(loiNhac);
    }

    private YeuCauHienThi layYeuCauDangChon() {
        int row = view.getDongDangChon();
        if (row < 0 || row >= dsYeuCau.size()) {
            view.hienThiLoi("Vui lòng chọn một yêu cầu bảo hành.");
            return null;
        }
        return dsYeuCau.get(row);
    }

    private void lamMoiForm() {
        dangTaoMoi = true;
        baoHanhConHan = false;
        view.lamMoiBieuMau(layMaYeuCauMoi());
        taiDanhSachHoaDon();
    }

    private String layMaYeuCauMoi() {
        try {
            return phieuYeuCauBaoHanhDAO.layMaTiepTheo();
        } catch (SQLException exception) {
            view.baoLoi("Không thể tạo mã yêu cầu bảo hành", exception);
            return "";
        }
    }
}

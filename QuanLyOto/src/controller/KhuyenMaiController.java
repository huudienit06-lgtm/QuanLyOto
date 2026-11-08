package controller;

import dao.ChuongTrinhKhuyenMaiDAO;
import dao.ChuongTrinhKhuyenMaiDAO.KhuyenMai;
import dao.ChuongTrinhKhuyenMaiDAO.LoiNhapLieu;
import dao.ChuongTrinhKhuyenMaiDAO.TieuChiLoc;
import gui.FrmKhuyenMai;
import gui.FrmKhuyenMai.DlgKhuyenMai;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller: gắn các sự kiện của FrmKhuyenMai (và hộp thoại thông tin) vào xử
 * lý. Truy vấn, tính trạng thái, lọc, kiểm tra dữ liệu nằm ở
 * ChuongTrinhKhuyenMaiDAO; việc hiển thị nằm ở FrmKhuyenMai.
 */
public class KhuyenMaiController {
    private final FrmKhuyenMai view;
    private final ChuongTrinhKhuyenMaiDAO dao = new ChuongTrinhKhuyenMaiDAO();

    private final List<KhuyenMai> dsKM = new ArrayList<>(); // toàn bộ dữ liệu
    private List<KhuyenMai> dsHienThi = new ArrayList<>(); // sau khi lọc (khớp dòng trên bảng)

    public KhuyenMaiController(FrmKhuyenMai view) {
        this.view = view;
        dangKySuKien();
        lamMoiDuLieu();
    }

    // ĐĂNG KÝ SỰ KIỆN
    private void dangKySuKien() {
        view.khiNhanTim(this::timKiem);
        view.khiNhapEnter(this::timKiem);
        view.khiNhanLamMoi(() -> {
            view.xoaBoLoc();
            timKiem();
        });
        view.khiNhanThemMoi(() -> moKhungThongTin(null));
        view.khiChonDong(row -> {
            if (row >= 0 && row < dsHienThi.size()) {
                moKhungThongTin(dsHienThi.get(row));
            }
        });
    }

    // TẢI / TÌM KIẾM
    // Tải lại dữ liệu từ CSDL rồi áp dụng lại bộ lọc hiện tại
    private void lamMoiDuLieu() {
        taiDanhSachKhuyenMai();
        timKiem();
    }

    private void taiDanhSachKhuyenMai() {
        dsKM.clear();
        try {
            dsKM.addAll(dao.layDanhSachKhuyenMai());
        } catch (SQLException e) {
            view.baoLoiCSDL("Không thể tải danh sách khuyến mãi từ CSDL!\n" + e.getMessage());
        }
    }

    private void timKiem() {
        // Kiểm tra điều kiện lọc
        BigDecimal tileTu, tileDen;
        try {
            tileTu = ChuongTrinhKhuyenMaiDAO.docSo(view.layTileTu());
            tileDen = ChuongTrinhKhuyenMaiDAO.docSo(view.layTileDen());
        } catch (NumberFormatException ex) {
            view.canhBao("Tỉ lệ giảm lọc phải là số.");
            return;
        }

        LocalDate tuNgay = view.layLocTuNgay();
        LocalDate denNgay = view.layLocDenNgay();
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            view.canhBao("Khoảng ngày lọc không hợp lệ (đến ngày < từ ngày).");
            return;
        }

        // Cập nhật trạng thái theo ngày rồi lọc
        dao.capNhatTrangThaiTheoNgay(dsKM);
        dsHienThi = dao.loc(dsKM, new TieuChiLoc(
                view.layMaLoc(), view.layTenLoc(), view.layTrangThaiLoc(),
                tileTu, tileDen, tuNgay, denNgay));
        view.hienThiDanhSach(dsHienThi, dsKM.size());
    }

    // HỘP THOẠI THÔNG TIN
    // km == null: thêm mới; khác null: xem / sửa / xóa khuyến mãi đó
    private void moKhungThongTin(KhuyenMai km) {
        boolean themMoi = (km == null);
        DlgKhuyenMai dlg = view.taoHopThoai(km, themMoi ? dao.taoMaMoi(dsKM) : null);

        dlg.khiNhanLuu(() -> luu(dlg, themMoi));
        dlg.khiNhanXoa(() -> xoa(dlg));
        dlg.khiNhanDong(dlg::dong);
        dlg.hienThi();
    }

    private void luu(DlgKhuyenMai dlg, boolean themMoi) {
        KhuyenMai ban;
        try {
            ban = dao.kiemTra(dlg.layMa(), dlg.layTen(), dlg.layTile(),
                    dlg.layNgayBatDau(), dlg.layNgayKetThuc(), dlg.laTamNgung(),
                    dlg.layDieuKien(), themMoi, dsKM);
        } catch (LoiNhapLieu e) {
            dlg.canhBao(e.getMessage());
            dlg.datFocus(e.truong());
            return;
        }

        if (luuVaoCSDL(ban, themMoi)) {
            dlg.dong();
            lamMoiDuLieu();
        }
    }

    private void xoa(DlgKhuyenMai dlg) {
        String ma = dlg.layMa();
        if (dlg.xacNhanXoa(ma) && xoaKhoiCSDL(ma)) {
            dlg.dong();
            lamMoiDuLieu();
        }
    }

    // CSDL
    private boolean luuVaoCSDL(KhuyenMai km, boolean themMoi) {
        try {
            boolean ok = themMoi ? dao.them(km) : dao.capNhat(km);
            if (ok) {
                view.thongTin(themMoi ? "Thêm khuyến mãi thành công!" : "Cập nhật khuyến mãi thành công!");
                return true;
            }
            view.canhBao(themMoi ? "Không thêm được khuyến mãi." : "Không tìm thấy khuyến mãi cần cập nhật.");
            return false;
        } catch (SQLException e) {
            view.baoLoiCSDL("Lỗi khi lưu khuyến mãi vào CSDL!\n" + e.getMessage());
            return false;
        }
    }

    private boolean xoaKhoiCSDL(String maKM) {
        try {
            if (dao.xoa(maKM)) {
                view.thongTin("Xóa khuyến mãi thành công!");
                return true;
            }
            view.canhBao("Không tìm thấy khuyến mãi cần xóa trong CSDL.");
            return false;
        } catch (SQLException e) {
            view.baoLoiCSDL("Không thể xóa khuyến mãi khỏi CSDL!\n"
                    + "Có thể khuyến mãi đang được tham chiếu bởi hóa đơn.\n"
                    + e.getMessage());
            return false;
        }
    }
}
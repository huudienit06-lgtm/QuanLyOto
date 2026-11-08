package controller;

import dao.ChinhSachBaoHanhDAO.PhuTungBaoHanh;
import dao.HoaDonDAO.DongChiTiet;
import dao.HoaDonDAO.KhuyenMai;
import dao.HoaDonDAO.LichSuHoaDon;
import dao.HoaDonDAO.PhuTung;
import dao.HoaDonDAO.ThongTinHoaDon;
import dao.HoaDonDAO;
import dao.HoaDonDAO.HoaDonDaLuu;
import dao.HoaDonDAO.PhieuDayDu;
import dao.HoaDonDAO.TongKet;
import gui.FrmHoaDon;
import gui.FrmThanhToan;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static dao.HoaDonDAO.CHUA_THANH_TOAN;
import static dao.HoaDonDAO.DINH_DANG_NGAY;
import static dao.HoaDonDAO.MUA_PHU_TUNG;
import static dao.HoaDonDAO.SUA_CHUA;
import static dao.HoaDonDAO.coPhieu;

/**
 * Controller: gắn các sự kiện của FrmHoaDon vào xử lý. Việc tính toán và truy
 * vấn dữ liệu nằm ở HoaDonDAO; việc hiển thị nằm ở FrmHoaDon.
 */
public class HoaDonController {
    public static final String MSG_CHON_PHIEU = "Chọn phiếu sửa chữa để kiểm tra bảo hành.";
    public static final String MSG_KHONG_BAO_HANH = "Phiếu này không có phụ tùng thuộc chính sách đang áp dụng.";
    public static final String MSG_HD_MUA_PHU_TUNG = "Hóa đơn mua phụ tùng không có phiếu sửa chữa.";

    private final FrmHoaDon view;
    private final HoaDonDAO dao = new HoaDonDAO();

    // Số liệu tổng kết đang hiển thị
    private TongKet tongKet = TongKet.RONG;

    // Mã hóa đơn vừa lưu thành công, dùng cho nút Thanh toán.
    private String maHoaDonVuaLuu;
    // Đang đổ dữ liệu hóa đơn cũ vào form: tạm tắt các tính toán/xóa tự động.
    private boolean dangNapHoaDonCu;
    // Form đang hiển thị một hóa đơn đã lưu (không cho lưu lại).
    private boolean dangXemHoaDonCu;
    // Đang chọn dòng lịch sử bằng code: không tải lại hóa đơn.
    private boolean dangDongBoLichSu;

    public HoaDonController(FrmHoaDon view) {
        this.view = view;
        dangKySuKien();
        capNhatLoaiHoaDon();
    }

    // ĐĂNG KÝ SỰ KIỆN
    private void dangKySuKien() {
        view.khiDoiLoaiHoaDon(this::capNhatLoaiHoaDon);
        view.khiNhanThem(this::lamMoiToanBo);
        view.khiNhanTimPhieu(this::moCuaSoTimPhieu);
        view.khiNhapMaPhieu(this::napThongTinPhieu);
        view.khiNhanThemPhuTung(this::themPhuTung);
        view.khiNhanLamMoiNhapLieu(view::lamMoiNhapLieu);
        view.khiNhanXoaDong(this::xoaDongChiTiet);
        view.khiNhanLamMoi(this::xacNhanLamMoi);
        view.khiNhanLuu(this::luuHoaDon);
        view.khiNhanThanhToan(this::moFormThanhToan);

        // Bảng chi tiết hoặc khuyến mãi đổi thì tính lại tổng
        view.khiDoiChiTiet(this::tinhTongNeuDangLap);
        view.khiDoiKhuyenMai(this::tinhTongNeuDangLap);

        view.khiChonLichSu(this::xemHoaDonLichSu);
    }

    // TẢI DỮ LIỆU
    public void taiDuLieuBanDauAsync() {
        chayNen(dao::taiDuLieuBanDau,
                d -> view.napDuLieuBanDau(d.maHoaDon(), d.dsPhuTung(), d.dsKhuyenMai(), d.dsLichSu()),
                "Không thể tải dữ liệu ban đầu của hóa đơn");
    }

    private <T> void chayNen(Callable<T> viec, Consumer<T> khiXong, String tieuDeLoi) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return viec.call();
            }

            @Override
            protected void done() {
                try {
                    khiXong.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.baoLoi(tieuDeLoi, ex);
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() == null ? ex : ex.getCause();
                    view.baoLoi(tieuDeLoi, goc instanceof Exception e ? e : new Exception(goc));
                }
            }
        }.execute();
    }

    private void capNhatMaHoaDon() {
        try {
            String ma = dao.taoMaHoaDonMoi();
            if (!ma.isEmpty()) {
                view.datMaHoaDon(ma);
            }
        } catch (SQLException ex) {
            view.baoLoi("Không thể phát sinh mã hóa đơn", ex);
        }
    }

    // LOẠI HÓA ĐƠN
    private void capNhatLoaiHoaDon() {
        view.hienThiNguon(view.layLoaiHoaDon());

        // Đang nạp hóa đơn cũ: chỉ đổi giao diện nguồn, không xóa dữ liệu vừa tải
        if (!dangNapHoaDonCu) {
            maHoaDonVuaLuu = null;
            view.xoaKhuyenMaiNgungApDung();
            view.xoaThongTinKhachVaXe();
            view.xoaChiTiet();
            view.xoaBaoHanh(MSG_CHON_PHIEU);
            view.lamMoiNhapLieu();
            tinhTongHoaDon();
        }
        view.capNhatBoCuc();
    }

    // PHIẾU SỬA CHỮA
    private void moCuaSoTimPhieu() {
        view.moCuaSoTimPhieu(this::napThongTinPhieu);
    }

    // Điền thông tin khách, xe và chi tiết từ phiếu sửa chữa.
    private boolean napThongTinPhieu(String maPhieu) {
        try {
            PhieuDayDu kq = dao.taiPhieu(maPhieu);
            if (kq == null) {
                view.canhBao("Không tìm thấy phiếu sửa chữa: " + maPhieu);
                return false;
            }
            view.hienThiPhieu(kq.phieu());
            view.datChiTiet(kq.dsChiTiet());
            view.hienThiBaoHanh(kq.dsBaoHanh(), MSG_KHONG_BAO_HANH);

            if (kq.dsChiTiet().isEmpty()) {
                view.canhBao("Phiếu sửa chữa chưa có dịch vụ hoặc phụ tùng.");
            }
            return true;
        } catch (SQLException ex) {
            view.baoLoi("Không thể tải thông tin phiếu hoặc kiểm tra bảo hành", ex);
            return false;
        }
    }

    // XEM LẠI HÓA ĐƠN TỪ LỊCH SỬ
    private void xemHoaDonLichSu(String maHD) {
        if (dangDongBoLichSu) {
            return;
        }
        chayNen(() -> dao.taiHoaDon(maHD), this::hienThiHoaDonCu, "Không thể tải thông tin hóa đơn");
    }

    private void hienThiHoaDonCu(HoaDonDaLuu cu) {
        ThongTinHoaDon hd = cu.hd();
        boolean laSuaChua = coPhieu(hd.maPhieu());

        dangNapHoaDonCu = true;
        try {
            dangXemHoaDonCu = true;
            maHoaDonVuaLuu = null; // hóa đơn cũ không dùng để thanh toán lại từ màn này

            view.hienThiThongTinHoaDon(hd, laSuaChua);
            view.datChiTiet(cu.dsChiTiet());
            view.chonKhuyenMaiDaLuu(hd.maKM(), hd.tenKM(), hd.tileGiam());

            tongKet = dao.tongKetHoaDonCu(hd, cu.dsChiTiet());
            view.hienThiTong(tongKet);

            List<PhuTungBaoHanh> dsBaoHanh = cu.dsBaoHanh();
            view.hienThiBaoHanh(dsBaoHanh, laSuaChua ? MSG_KHONG_BAO_HANH : MSG_HD_MUA_PHU_TUNG);
            view.capNhatBoCuc();
        } finally {
            dangNapHoaDonCu = false;
        }
    }

    // CHI TIẾT HÓA ĐƠN
    private void themPhuTung() {
        PhuTung pt = view.layPhuTungDangChon();
        if (pt == null || pt.ma().isEmpty()) {
            view.canhBao("Vui lòng chọn phụ tùng.");
            return;
        }

        var donGia = dao.docDonGia(view.layDonGiaNhap());
        if (donGia == null) {
            view.canhBao("Đơn giá phải là số hợp lệ và không âm.");
            return;
        }

        int soLuongThem = view.laySoLuong();
        List<DongChiTiet> ds = view.layChiTietTuBang();
        int row = dao.timDongPhuTung(ds, pt.ma());

        if (row >= 0) {
            // Đã có: cộng số lượng, giữ đơn giá cũ của dòng
            view.capNhatSoLuongDong(row, ds.get(row).soLuong() + soLuongThem);
        } else {
            view.themDongChiTiet(new DongChiTiet(false, pt.ma(), pt.ten(), soLuongThem, donGia));
        }
        view.lamMoiNhapLieu();
    }

    private void xoaDongChiTiet() {
        if (!view.xoaDongChiTietDangChon()) {
            view.canhBao("Vui lòng chọn dòng cần xóa.");
        }
    }

    // TÍNH TIỀN
    private void tinhTongNeuDangLap() {
        if (!dangNapHoaDonCu) {
            tinhTongHoaDon();
        }
    }

    private void tinhTongHoaDon() {
        tongKet = dao.tinhTong(view.layChiTietTuBang(), view.layKhuyenMaiDangChon());
        view.hienThiTong(tongKet);
    }

    // LƯU / THANH TOÁN
    private void luuHoaDon() {
        if (dangXemHoaDonCu) {
            view.canhBao("Đây là hóa đơn đã lưu. Nhấn \"Thêm\" để lập hóa đơn mới.");
            return;
        }

        String loai = view.layLoaiHoaDon();
        boolean laSuaChua = SUA_CHUA.equals(loai);
        if (!laSuaChua && !MUA_PHU_TUNG.equals(loai)) {
            view.canhBao("Vui lòng chọn loại hóa đơn.");
            return;
        }

        String maHD = view.layMaHoaDon();
        if (maHD.isEmpty() || maHD.equalsIgnoreCase("Tự động")) {
            view.canhBao("Mã hóa đơn chưa được tạo.");
            capNhatMaHoaDon();
            return;
        }

        // Người lập hóa đơn lấy từ tài khoản đăng nhập
        String maNV = view.layMaNhanVien();
        if (maNV.isEmpty()) {
            view.canhBao("Chưa có thông tin nhân viên đang đăng nhập. "
                    + "Vui lòng đăng nhập lại hoặc kiểm tra cấu hình tài khoản.");
            return;
        }

        String maPhieu = laSuaChua ? view.layMaPhieu() : null;
        if (laSuaChua && maPhieu.isEmpty()) {
            view.canhBao("Vui lòng nhập mã phiếu sửa chữa.");
            return;
        }

        List<DongChiTiet> dsChiTiet = view.layChiTietTuBang();
        if (dsChiTiet.isEmpty()) {
            view.canhBao("Vui lòng thêm chi tiết hóa đơn.");
            return;
        }

        LocalDate ngayLap;
        try {
            ngayLap = LocalDate.parse(view.layNgayLap(), DINH_DANG_NGAY);
        } catch (DateTimeParseException ex) {
            view.canhBao("Ngày lập hóa đơn không hợp lệ.");
            return;
        }

        KhuyenMai km = view.layKhuyenMaiDangChon();
        String maKM = (km == null || km.ma().isEmpty()) ? null : km.ma();
        try {
            dao.luuHoaDon(maHD, maPhieu, maNV, maKM, loai, ngayLap,
                    tongKet.tienGiam(), tongKet.tienVAT(), tongKet.tongThanhToan(), dsChiTiet);
            maHoaDonVuaLuu = maHD;
            themVaoDauLichSu(new LichSuHoaDon(maHD, ngayLap, maNV, maPhieu,
                    tongKet.tongThanhToan(), CHUA_THANH_TOAN));
            view.thongBaoThanhCong("Lưu hóa đơn thành công!\nMã hóa đơn: " + maHD);
        } catch (SQLException ex) {
            view.baoLoi("Không thể lưu hóa đơn", ex);
        }
    }

    private void moFormThanhToan() {
        if (maHoaDonVuaLuu == null) {
            view.canhBao("Vui lòng lưu hóa đơn trước khi thanh toán!");
            return;
        }

        FrmThanhToan frm = new FrmThanhToan(maHoaDonVuaLuu);

        // Đảm bảo cửa sổ được đóng hoàn toàn khi người dùng thoát.
        frm.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        // Khi đóng cửa sổ thanh toán, tải lại lịch sử từ database.
        frm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                taiLaiLichSuAsync();
            }
        });

        frm.setLocationRelativeTo(view);
        frm.setVisible(true);
    }

    // Tải lại lịch sử hóa đơn sau khi đóng cửa sổ thanh toán.
    private void taiLaiLichSuAsync() {
        chayNen(
                dao::layLichSuHoaDon,
                dsLichSu -> {
                    dangDongBoLichSu = true;
                    try {
                        view.capNhatLichSu(dsLichSu);
                    } finally {
                        dangDongBoLichSu = false;
                    }
                },
                "Không thể cập nhật lịch sử hóa đơn sau khi thanh toán");
    }

    // LÀM MỚI
    private void xacNhanLamMoi() {
        if (view.xacNhan("Bạn có muốn làm mới dữ liệu đang nhập không?", "Xác nhận làm mới")) {
            lamMoiToanBo();
        }
    }

    // Đưa form về trạng thái lập hóa đơn mới.
    private void lamMoiToanBo() {
        dangXemHoaDonCu = false;
        view.datLaiForm(); // khuyến mãi, ngày lập, loại hóa đơn về mặc định
        capNhatLoaiHoaDon(); // xóa phiếu, bảng chi tiết, ô phụ tùng

        dangDongBoLichSu = true;
        try {
            view.boChonLichSu();
        } finally {
            dangDongBoLichSu = false;
        }

        capNhatMaHoaDon();
    }

    // Đưa hóa đơn lên đầu lịch sử và chọn dòng đó (không tải lại hóa đơn).
    private void themVaoDauLichSu(LichSuHoaDon h) {
        dangDongBoLichSu = true;
        try {
            view.themVaoDauLichSu(h);
        } finally {
            dangDongBoLichSu = false;
        }
    }
}
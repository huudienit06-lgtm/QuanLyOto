package controller;

import dao.ChinhSachBaoHanhDAO;
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
import dao.TimKiemHoaDonDAO;
import entity.GiaoDichThanhToan;
import gui.FrmHoaDon;
import gui.FrmThanhToan;
import java.awt.Color;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;

import static dao.HoaDonDAO.CHUA_THANH_TOAN;
import static dao.HoaDonDAO.DINH_DANG_NGAY;
import static dao.HoaDonDAO.MUA_PHU_TUNG;
import static dao.HoaDonDAO.SUA_CHUA;
import static dao.HoaDonDAO.coPhieu;

public class HoaDonController {
    public static final String MSG_CHON_PHIEU = "Chọn phiếu sửa chữa để kiểm tra bảo hành.";
    public static final String MSG_KHONG_BAO_HANH = "Phiếu này không có phụ tùng thuộc chính sách đang áp dụng.";
    public static final String MSG_HD_MUA_PHU_TUNG = "Hóa đơn mua phụ tùng không có phiếu sửa chữa.";
    private int lanYeuCauBaoHanh = 0;

    private final FrmHoaDon view;
    private final HoaDonDAO dao = new HoaDonDAO();
    private final ChinhSachBaoHanhDAO baoHanhDAO = new ChinhSachBaoHanhDAO();

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
        view.khiDoiChiTiet(this::xuLyKhiDoiChiTiet);
        view.khiDoiKhuyenMai(this::tinhTongNeuDangLap);
        view.khiChonLichSu(maHD -> moKhungThongTin(maHD));
    }

    private void xuLyKhiDoiChiTiet() {
        tinhTongNeuDangLap();
        if (dangNapHoaDonCu) {
            return;
        }
        capNhatBaoHanhTheoChiTiet();
    }

    private void capNhatBaoHanhTheoChiTiet() {
        int lanHienTai = ++lanYeuCauBaoHanh;
        List<DongChiTiet> dsChiTiet = new ArrayList<>(
                view.layChiTietTuBang());
        new SwingWorker<List<PhuTungBaoHanh>, Void>() {
            @Override
            protected List<PhuTungBaoHanh> doInBackground() throws Exception {
                return baoHanhDAO.layPhuTungBaoHanhTheoDanhSach(dsChiTiet);
            }

            @Override
            protected void done() {
                if (lanHienTai != lanYeuCauBaoHanh) {
                    return;
                }
                try {
                    List<PhuTungBaoHanh> dsBaoHanh = get();
                    view.hienThiBaoHanh(
                            dsBaoHanh,
                            MSG_KHONG_BAO_HANH);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() != null
                            ? ex.getCause()
                            : ex;
                    view.xoaBaoHanh("Không thể tải thông tin bảo hành.");
                    view.baoLoi(
                            "Không thể kiểm tra chính sách bảo hành",
                            goc instanceof Exception
                                    ? (Exception) goc
                                    : new Exception(goc));
                }
            }
        }.execute();
    }

    private static final NumberFormat NF_TIEN = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    // Bấm một hóa đơn trong lịch sử: hiện khung thông tin, không đổ lên form lập
    private void moKhungThongTin(String maHD) {
        if (dangDongBoLichSu) {
            return;
        }
        try {
            HoaDonDaLuu cu = dao.taiHoaDon(maHD);
            if (cu == null) {
                view.canhBao("Không tìm thấy hóa đơn " + maHD + ".");
                return;
            }
            ThongTinHoaDon hd = cu.hd();
            TongKet tk = dao.tongKetHoaDonCu(hd, cu.dsChiTiet());
            TimKiemHoaDonDAO daoTK = new TimKiemHoaDonDAO();
            String[] khXe = daoTK.layMaKhachHangVaXe(maHD);
            BigDecimal tongGiaoDich = BigDecimal.ZERO;
            for (GiaoDichThanhToan gd : daoTK.layGiaoDichThanhCong(maHD)) {
                if (gd.getSoTienThanhToan() != null) {
                    tongGiaoDich = tongGiaoDich.add(gd.getSoTienThanhToan());
                }
            }

            // Bảng dịch vụ, phụ tùng
            // Bảng dịch vụ, phụ tùng và thông tin bảo hành
            DefaultTableModel modelDong = view.taoModelDongHang();
            BigDecimal tongChuaVAT = BigDecimal.ZERO;
            int stt = 1;

            // Lấy chính sách bảo hành của các phụ tùng trong hóa đơn
            List<PhuTungBaoHanh> dsBaoHanh = baoHanhDAO.layPhuTungBaoHanhTheoDanhSach(cu.dsChiTiet());

            // Ánh xạ mã phụ tùng với thông tin bảo hành
            Map<String, PhuTungBaoHanh> baoHanhTheoMa = new HashMap<>();

            for (PhuTungBaoHanh bh : dsBaoHanh) {
                baoHanhTheoMa.put(bh.maPhuTung(), bh);
            }

            // Đổ từng dòng chi tiết vào bảng
            for (DongChiTiet d : cu.dsChiTiet()) {
                tongChuaVAT = tongChuaVAT.add(d.thanhTien());
                String thongTinBaoHanh = "—";
                // Chỉ phụ tùng mới có thông tin bảo hành
                if (!d.dichVu()) {
                    PhuTungBaoHanh bh = baoHanhTheoMa.get(d.ma());
                    if (bh != null) {
                        thongTinBaoHanh = bh.thoiHan() + " "
                                + switch (bh.donViThoiGian().toUpperCase()) {
                                    case "NGAY" -> "ngày";
                                    case "THANG" -> "tháng";
                                    case "NAM" -> "năm";
                                    default -> bh.donViThoiGian();
                                };
                    } else {
                        thongTinBaoHanh = "---";
                    }
                }
                modelDong.addRow(new Object[] {
                        stt++, d.ma(), d.ten(), d.soLuong(), tien(d.donGia()), tien(d.thanhTien()), thongTinBaoHanh
                });
            }

            String trangThai = view.layTrangThaiLichSu(maHD);
            boolean daTT = "Đã thanh toán".equals(trangThai);
            boolean motPhan = "Thanh toán một phần".equals(trangThai);
            boolean chuaTT = "Chưa thanh toán".equals(trangThai);
            BigDecimal daTra = daTT ? tk.tongThanhToan() : (motPhan ? tongGiaoDich : BigDecimal.ZERO);
            BigDecimal conLai = tk.tongThanhToan().subtract(daTra).max(BigDecimal.ZERO);

            Color chu = new Color(40, 50, 65), xanh = new Color(22, 130, 70);
            Color do_ = new Color(220, 53, 69), nut = new Color(37, 99, 235);

            String[][] thongTin = {
                    { "Khách hàng", hienThi(hd.tenKH()) },
                    { "Mã khách hàng", hienThi(hd.maKH()) },
                    { "Ngày lập", hd.ngayLap() == null ? "---" : hd.ngayLap().format(DINH_DANG_NGAY) },
                    { "Biển số xe", hienThi(hd.bienSo()) },
                    { "Mã xe", hienThi(khXe == null ? null : khXe[1]) },
                    { "Mã nhân viên", hienThi(hd.maNV()) },
                    { "Phiếu sửa chữa", hienThi(hd.maPhieu()) },
                    { "Mã khuyến mãi", hienThi(hd.maKM()) },
                    { "Trạng thái", trangThai } };

            List<Object[]> dongTien = new ArrayList<>();
            dongTien.add(new Object[] { "Tổng hóa đơn (chưa gồm VAT)", tien(tongChuaVAT) + " đ", chu, true });
            dongTien.add(new Object[] { "Tiền thuế (VAT)", tien(tk.tienVAT()) + " đ", chu, false });
            dongTien.add(new Object[] { "Tiền giảm", "- " + tien(tk.tienGiam()) + " đ", do_, false });
            dongTien.add(new Object[] { "TỔNG THANH TOÁN", tien(tk.tongThanhToan()) + " đ", nut, true });
            if (daTT || motPhan) {
                dongTien.add(new Object[] { "Đã thanh toán", tien(daTra) + " đ", xanh, true });
            }
            if (motPhan || chuaTT) {
                dongTien.add(new Object[] { "Còn lại", tien(conLai) + " đ", do_, true });
            }

            view.hienThiKhungThongTin(maHD, thongTin, modelDong, dongTien,
                    chuaTT || motPhan, () -> moThanhToanHoaDon(maHD));
        } catch (Exception ex) {
            view.baoLoi("Không thể tải thông tin hóa đơn", ex);
        }
    }

    private void moThanhToanHoaDon(String maHD) {
        FrmThanhToan frm = new FrmThanhToan(maHD);
        frm.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        frm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                taiLaiLichSuAsync();
            }
        });
        frm.setLocationRelativeTo(view);
        frm.setVisible(true);
    }

    private static String tien(BigDecimal so) {
        return so == null ? "0" : NF_TIEN.format(so);
    }

    private static String hienThi(String s) {
        return (s == null || s.isBlank()) ? "---" : s;
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
            capNhatBaoHanhTheoChiTiet();
            if (kq.dsChiTiet().isEmpty()) {
                view.canhBao("Phiếu sửa chữa chưa có dịch vụ hoặc phụ tùng.");
            }
            return true;
        } catch (SQLException ex) {
            view.baoLoi("Không thể tải thông tin phiếu hoặc kiểm tra bảo hành", ex);
            return false;
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
        moThanhToanHoaDon(maHoaDonVuaLuu);
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
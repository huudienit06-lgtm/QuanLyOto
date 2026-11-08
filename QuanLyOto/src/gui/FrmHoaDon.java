package gui;

import controller.HoaDonController;
import dao.ChinhSachBaoHanhDAO.PhuTungBaoHanh;
import dao.HoaDonDAO.DongChiTiet;
import dao.HoaDonDAO.KhuyenMai;
import dao.HoaDonDAO.LichSuHoaDon;
import dao.HoaDonDAO.Phieu;
import dao.HoaDonDAO.PhuTung;
import dao.HoaDonDAO.ThongTinHoaDon;
import dao.HoaDonDAO.TongKet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static controller.HoaDonController.MSG_CHON_PHIEU;
import static dao.HoaDonDAO.DICH_VU;
import static dao.HoaDonDAO.DINH_DANG_NGAY;
import static dao.HoaDonDAO.MUA_PHU_TUNG;
import static dao.HoaDonDAO.PHU_TUNG;
import static dao.HoaDonDAO.SUA_CHUA;
import static dao.HoaDonDAO.coPhieu;

/**
 * View: chỉ dựng giao diện và hiển thị dữ liệu. Các sự kiện của nút/ô nhập
 * được HoaDonController đăng ký qua các phương thức khiXxx(...); không truy
 * cập DAO và không có nghiệp vụ ở đây.
 */
public class FrmHoaDon extends JPanel {
    private static final String THE_TRONG = "trong";
    private static final String THE_SUA_CHUA = "suachua";
    private static final String THE_PHU_TUNG = "phutung";
    private static final Color MAU_CHU = new Color(40, 50, 65);
    private static final Color MAU_PHU = new Color(100, 112, 130);
    private static final Color MAU_NEN_TRANG = new Color(244, 247, 251);
    private static final Color MAU_NEN_O = new Color(247, 249, 252);
    private static final Color MAU_NEN_TIEU_DE = new Color(240, 245, 255);
    private static final Color MAU_DONG_CHAN = new Color(246, 248, 252);
    private static final Color MAU_CHON = new Color(219, 234, 254);
    private static final Color MAU_NUT = new Color(37, 99, 235);
    private static final Color MAU_NUT_DAM = new Color(29, 78, 216);
    private static final Color MAU_VIEN = new Color(220, 225, 232);
    private static final Color MAU_DO = new Color(220, 53, 69);
    private static final Color MAU_DO_DAM = new Color(185, 28, 40);
    private static final Color MAU_XANH_LA = new Color(22, 130, 70);
    private static final Color MAU_XANH_LA_DAM = new Color(17, 105, 55);
    private static final Font FONT_THUONG = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_DAM = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NHO_DAM = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_GIA_TRI = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_NHAN_TONG = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_TONG = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_TIEU_DE = new Font("Segoe UI", Font.BOLD, 24);
    private static final NumberFormat DINH_DANG_TIEN = NumberFormat
            .getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private final HoaDonController controller;

    // Thông tin hóa đơn (nhân viên là người đang đăng nhập, dùng cho cả hai loại)
    private final JTextField txtMaHoaDon = new JTextField("Tự động");
    private final JTextField txtNgayLap = new JTextField(LocalDate.now().format(DINH_DANG_NGAY));
    private final JTextField txtMaNhanVienLapHoaDon = new JTextField();
    private final JTextField txtTenNhanVienLapHoaDon = new JTextField();
    private final JComboBox<String> cboLoaiHoaDon = new JComboBox<>(new String[] {
            "-- Chọn loại hóa đơn --", SUA_CHUA, MUA_PHU_TUNG });

    // Phiếu sửa chữa
    private final JTextField txtMaPhieuSuaChua = new JTextField();
    private final JTextField txtMaKhachHang = new JTextField();
    private final JTextField txtTenKhachHang = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtBienSoXe = new JTextField();
    private final JTextField txtTenXe = new JTextField();
    // Các ô tự điền từ phiếu (chỉ đọc).
    private final JTextField[] oTuDien = { txtMaKhachHang, txtTenKhachHang, txtSoDienThoai, txtBienSoXe, txtTenXe };

    // Phụ tùng và khuyến mãi
    private final JComboBox<PhuTung> cboPhuTung = new JComboBox<>();
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JTextField txtDonGia = new JTextField("0");
    private final JComboBox<KhuyenMai> cboKhuyenMai = new JComboBox<>();
    // Khuyến mãi đã ngừng áp dụng, thêm tạm vào combo khi xem hóa đơn cũ.
    private KhuyenMai khuyenMaiNgungApDung;

    // Các nút (sự kiện do controller đăng ký)
    private final JButton btnThemHoaDon = taoNut("Thêm");
    private final JButton btnTimPhieu = taoNutPhu("Tìm phiếu");
    private final JButton btnThemPhuTung = taoNut("Thêm vào hóa đơn");
    private final JButton btnLamMoiNhapLieu = taoNutPhu("Làm mới nhập liệu");
    private final JButton btnXoaDong = taoNutMau("Xóa dòng", MAU_DO, MAU_DO_DAM, Color.WHITE, MAU_DO_DAM);
    private final JButton btnLamMoi = taoNutPhu("Làm mới");
    private final JButton btnLuu = taoNut("Lưu hóa đơn");
    private final JButton btnThanhToan = taoNutMau("Thanh toán", MAU_XANH_LA, MAU_XANH_LA_DAM,
            Color.WHITE, MAU_XANH_LA);

    // Tổng kết
    private final JLabel lblTienDichVu = new JLabel("0 đ");
    private final JLabel lblTienPhuTung = new JLabel("0 đ");
    private final JLabel lblTongHoaDon = new JLabel("0 đ");
    private final JLabel lblTienVAT = new JLabel("0 đ");
    private final JLabel lblTienGiam = new JLabel("0 đ");
    private final JLabel lblTongThanhToan = new JLabel("0 đ");

    // Khu vực "Nguồn hóa đơn"
    private final CardLayout cardNguon = new CardTheoThe();
    private final JPanel pnlNguon = new JPanel(cardNguon);
    private The theNguon;

    // Bảng chi tiết: Loại, Mã, Tên, Số lượng, Đơn giá (BigDecimal), Thành tiền
    // (BigDecimal)
    private final DefaultTableModel modelChiTiet = new MoHinhBang(
            "Loại", "Mã", "Tên dịch vụ / phụ tùng", "Số lượng", "Đơn giá", "Thành tiền");
    private final JTable tblChiTiet = new BangSoc(modelChiTiet);

    // Bảng bảo hành
    private final DefaultTableModel modelBaoHanh = new MoHinhBang(
            "Phụ tùng", "Số lượng", "Chính sách", "Thời hạn bảo hành");
    private final List<PhuTungBaoHanh> dsBaoHanhHienThi = new ArrayList<>();
    private final JTable tblBaoHanh = new JTable(modelBaoHanh) {
        @Override
        public String getToolTipText(MouseEvent event) {
            int row = rowAtPoint(event.getPoint());
            if (row < 0) {
                return null;
            }
            int modelRow = convertRowIndexToModel(row);
            return modelRow < dsBaoHanhHienThi.size()
                    ? taoThongTinBaoHanhTooltip(dsBaoHanhHienThi.get(modelRow))
                    : null;
        }
    };
    private final JLabel lblTinhTrangBaoHanh = new JLabel(MSG_CHON_PHIEU);

    // Lịch sử hóa đơn
    private final DefaultTableModel modelLichSu = new MoHinhBang(
            "Mã hóa đơn", "Ngày lập", "Mã NV", "Mã phiếu sửa chữa", "Tổng thanh toán", "Trạng thái");
    private final JTable tblLichSu = new BangSoc(modelLichSu);
    private final JLabel lblSoLichSu = new JLabel("0 hóa đơn");

    // KHỞI TẠO
    public FrmHoaDon() {
        setLayout(new BorderLayout(0, 12));
        setBackground(MAU_NEN_TRANG);
        setBorder(new EmptyBorder(14, 18, 14, 18));
        kieuCacOComponent();
        cauHinhBangChiTiet();
        cauHinhBangBaoHanh();
        cauHinhBangLichSu();
        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoVungCuon(), BorderLayout.CENTER);
        caiDatSuKienHienThi();
        // Controller đăng ký mọi sự kiện nghiệp vụ lên form này
        controller = new HoaDonController(this);
    }

    public void setNhanVienLapHoaDon(String maNV, String tenNV) {
        txtMaNhanVienLapHoaDon.setText(nz(maNV).trim());
        txtTenNhanVienLapHoaDon.setText(nz(tenNV).trim());
    }

    public void taiDuLieuBanDauAsync() {
        controller.taiDuLieuBanDauAsync();
    }

    // ĐĂNG KÝ SỰ KIỆN (controller gọi)
    public void khiDoiLoaiHoaDon(Runnable r) {
        cboLoaiHoaDon.addActionListener(e -> r.run());
    }

    public void khiNhanThem(Runnable r) {
        btnThemHoaDon.addActionListener(e -> r.run());
    }

    public void khiNhanTimPhieu(Runnable r) {
        btnTimPhieu.addActionListener(e -> r.run());
    }

    // Nhập mã phiếu rồi nhấn Enter
    public void khiNhapMaPhieu(Consumer<String> c) {
        txtMaPhieuSuaChua.addActionListener(e -> {
            String ma = txtMaPhieuSuaChua.getText().trim();
            if (!ma.isEmpty()) {
                c.accept(ma);
            }
        });
    }

    public void khiNhanThemPhuTung(Runnable r) {
        btnThemPhuTung.addActionListener(e -> r.run());
    }

    public void khiNhanLamMoiNhapLieu(Runnable r) {
        btnLamMoiNhapLieu.addActionListener(e -> r.run());
    }

    public void khiNhanXoaDong(Runnable r) {
        btnXoaDong.addActionListener(e -> r.run());
    }

    public void khiNhanLamMoi(Runnable r) {
        btnLamMoi.addActionListener(e -> r.run());
    }

    public void khiNhanLuu(Runnable r) {
        btnLuu.addActionListener(e -> r.run());
    }

    public void khiNhanThanhToan(Runnable r) {
        btnThanhToan.addActionListener(e -> r.run());
    }

    public void khiDoiChiTiet(Runnable r) {
        modelChiTiet.addTableModelListener(e -> r.run());
    }

    public void khiDoiKhuyenMai(Runnable r) {
        cboKhuyenMai.addActionListener(e -> r.run());
    }

    // Chọn một hóa đơn trong lịch sử, truyền mã hóa đơn
    public void khiChonLichSu(Consumer<String> c) {
        tblLichSu.getSelectionModel().addListSelectionListener(e -> {
            int row = tblLichSu.getSelectedRow();
            if (e.getValueIsAdjusting() || row < 0) {
                return;
            }
            c.accept(modelLichSu.getValueAt(tblLichSu.convertRowIndexToModel(row), 0).toString());
        });
    }

    // ĐỌC DỮ LIỆU TỪ FORM (cho controller)
    public String layLoaiHoaDon() {
        return (String) cboLoaiHoaDon.getSelectedItem();
    }

    public String layMaHoaDon() {
        return txtMaHoaDon.getText().trim();
    }

    public String layMaNhanVien() {
        return txtMaNhanVienLapHoaDon.getText().trim();
    }

    public String layMaPhieu() {
        return txtMaPhieuSuaChua.getText().trim();
    }

    public String layNgayLap() {
        return txtNgayLap.getText().trim();
    }

    public KhuyenMai layKhuyenMaiDangChon() {
        return (KhuyenMai) cboKhuyenMai.getSelectedItem();
    }

    public PhuTung layPhuTungDangChon() {
        return (PhuTung) cboPhuTung.getSelectedItem();
    }

    public String layDonGiaNhap() {
        return txtDonGia.getText().trim();
    }

    public int laySoLuong() {
        return (Integer) spnSoLuong.getValue();
    }

    public List<DongChiTiet> layChiTietTuBang() {
        List<DongChiTiet> ds = new ArrayList<>();
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            ds.add(new DongChiTiet(
                    DICH_VU.equals(modelChiTiet.getValueAt(i, 0)),
                    (String) modelChiTiet.getValueAt(i, 1),
                    (String) modelChiTiet.getValueAt(i, 2),
                    (Integer) modelChiTiet.getValueAt(i, 3),
                    (BigDecimal) modelChiTiet.getValueAt(i, 4)));
        }
        return ds;
    }

    // HIỂN THỊ DỮ LIỆU (controller gọi)
    public void napDuLieuBanDau(String maHoaDon, List<PhuTung> dsPhuTung,
            List<KhuyenMai> dsKhuyenMai, List<LichSuHoaDon> dsLichSu) {
        datMaHoaDon(maHoaDon);

        cboPhuTung.removeAllItems();
        cboPhuTung.addItem(new PhuTung("", "-- Chọn phụ tùng --", BigDecimal.ZERO));
        dsPhuTung.forEach(cboPhuTung::addItem);

        cboKhuyenMai.removeAllItems();
        cboKhuyenMai.addItem(new KhuyenMai("", "-- Không áp dụng khuyến mãi --", BigDecimal.ZERO));
        dsKhuyenMai.forEach(cboKhuyenMai::addItem);
        cboKhuyenMai.setSelectedIndex(0);

        modelLichSu.setRowCount(0);
        dsLichSu.forEach(h -> modelLichSu.addRow(dongLichSu(h)));
    }

    public void datMaHoaDon(String ma) {
        txtMaHoaDon.setText(ma == null || ma.isEmpty() ? "Tự động" : ma);
    }

    // Đổi thẻ nguồn hóa đơn theo loại hóa đơn.
    public void hienThiNguon(String loai) {
        if (SUA_CHUA.equals(loai)) {
            cardNguon.show(pnlNguon, THE_SUA_CHUA);
            theNguon.datTieuDe("PHIẾU SỬA CHỮA");
        } else if (MUA_PHU_TUNG.equals(loai)) {
            cardNguon.show(pnlNguon, THE_PHU_TUNG);
            theNguon.datTieuDe("CHỌN PHỤ TÙNG");
        } else {
            cardNguon.show(pnlNguon, THE_TRONG);
            theNguon.datTieuDe("NGUỒN HÓA ĐƠN");
        }
    }

    public void capNhatBoCuc() {
        pnlNguon.revalidate();
        revalidate();
        repaint();
    }

    public void hienThiPhieu(Phieu p) {
        txtMaPhieuSuaChua.setText(p.ma());
        txtMaKhachHang.setText(p.maKH());
        txtTenKhachHang.setText(p.tenKH());
        txtSoDienThoai.setText(p.soDT());
        txtBienSoXe.setText(p.bienSo());
        txtTenXe.setText(p.tenXe());
    }

    // Hiển thị thông tin một hóa đơn đã lưu (hóa đơn, phiếu, khách hàng, xe).
    public void hienThiThongTinHoaDon(ThongTinHoaDon hd, boolean laSuaChua) {
        txtMaHoaDon.setText(hd.maHoaDon());
        txtNgayLap.setText(hd.ngayLap() == null ? "" : hd.ngayLap().format(DINH_DANG_NGAY));
        txtMaNhanVienLapHoaDon.setText(nz(hd.maNV()));
        txtTenNhanVienLapHoaDon.setText(nz(hd.tenNV()));
        cboLoaiHoaDon.setSelectedItem(laSuaChua ? SUA_CHUA : MUA_PHU_TUNG);

        txtMaPhieuSuaChua.setText(nz(hd.maPhieu()));
        txtMaKhachHang.setText(nz(hd.maKH()));
        txtTenKhachHang.setText(nz(hd.tenKH()));
        txtSoDienThoai.setText(nz(hd.soDT()));
        txtBienSoXe.setText(nz(hd.bienSo()));
        txtTenXe.setText(nz(hd.tenXe()));
    }

    public void xoaThongTinKhachVaXe() {
        for (JTextField t : oTuDien) {
            t.setText("");
        }
    }

    // Bảng chi tiết
    public void xoaChiTiet() {
        modelChiTiet.setRowCount(0);
    }

    public void datChiTiet(List<DongChiTiet> ds) {
        modelChiTiet.setRowCount(0);
        ds.forEach(this::themDongChiTiet);
    }

    public void themDongChiTiet(DongChiTiet d) {
        modelChiTiet.addRow(new Object[] {
                d.dichVu() ? DICH_VU : PHU_TUNG,
                d.ma(), d.ten(), d.soLuong(), d.donGia(), d.thanhTien() });
    }

    // Đổi số lượng của một dòng, giữ đơn giá và tính lại thành tiền.
    public void capNhatSoLuongDong(int row, int soLuongMoi) {
        BigDecimal donGia = (BigDecimal) modelChiTiet.getValueAt(row, 4);
        modelChiTiet.setValueAt(soLuongMoi, row, 3);
        modelChiTiet.setValueAt(donGia.multiply(BigDecimal.valueOf(soLuongMoi)), row, 5);
    }

    // Xóa dòng đang chọn; false nếu chưa chọn dòng nào.
    public boolean xoaDongChiTietDangChon() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            return false;
        }
        modelChiTiet.removeRow(tblChiTiet.convertRowIndexToModel(row));
        return true;
    }

    // Khuyến mãi
    // Chọn khuyến mãi đã áp dụng cho hóa đơn cũ; nếu đã ngừng áp dụng thì thêm tạm
    // vào combo.
    public void chonKhuyenMaiDaLuu(String maKM, String tenKM, BigDecimal tile) {
        if (maKM == null || maKM.isBlank()) {
            if (cboKhuyenMai.getItemCount() > 0) {
                cboKhuyenMai.setSelectedIndex(0);
            }
            return;
        }
        for (int i = 0; i < cboKhuyenMai.getItemCount(); i++) {
            KhuyenMai km = cboKhuyenMai.getItemAt(i);
            if (km.ma().equals(maKM)) {
                cboKhuyenMai.setSelectedItem(km);
                return;
            }
        }
        xoaKhuyenMaiNgungApDung();
        khuyenMaiNgungApDung = new KhuyenMai(maKM,
                tenKM == null ? "Khuyến mãi đã ngừng áp dụng" : tenKM, tile);
        cboKhuyenMai.addItem(khuyenMaiNgungApDung);
        cboKhuyenMai.setSelectedItem(khuyenMaiNgungApDung);
    }

    public void xoaKhuyenMaiNgungApDung() {
        if (khuyenMaiNgungApDung != null) {
            cboKhuyenMai.removeItem(khuyenMaiNgungApDung);
            khuyenMaiNgungApDung = null;
        }
    }

    // Tổng kết
    public void hienThiTong(TongKet tk) {
        lblTienDichVu.setText(dinhDangTien(tk.dichVu()));
        lblTienPhuTung.setText(dinhDangTien(tk.phuTung()));
        lblTienVAT.setText(dinhDangTien(tk.tienVAT()));
        lblTongHoaDon.setText(dinhDangTien(tk.tongHoaDon()));
        lblTienGiam.setText(dinhDangTien(tk.tienGiam()));
        lblTongThanhToan.setText(dinhDangTien(tk.tongThanhToan()));
    }

    // Bảo hành
    public void hienThiBaoHanh(List<PhuTungBaoHanh> ds, String thongBaoRong) {
        modelBaoHanh.setRowCount(0);
        dsBaoHanhHienThi.clear();
        dsBaoHanhHienThi.addAll(ds);
        for (PhuTungBaoHanh b : ds) {
            modelBaoHanh.addRow(new Object[] {
                    b.tenPhuTung(), b.soLuong(), b.tenChinhSach(), hienThiThoiHanBaoHanh(b) });
        }
        lblTinhTrangBaoHanh.setText(ds.isEmpty()
                ? thongBaoRong
                : ds.size() + " phụ tùng được áp dụng bảo hành");
    }

    public void xoaBaoHanh(String thongBao) {
        hienThiBaoHanh(List.of(), thongBao);
    }

    private void capNhatThoiHanBaoHanhTrongBang() {
        for (int row = 0; row < dsBaoHanhHienThi.size(); row++) {
            modelBaoHanh.setValueAt(hienThiThoiHanBaoHanh(dsBaoHanhHienThi.get(row)), row, 3);
        }
    }

    private String hienThiThoiHanBaoHanh(PhuTungBaoHanh b) {
        String donVi = switch (b.donViThoiGian()) {
            case "NGAY" -> "ngày";
            case "NAM" -> "năm";
            default -> "tháng";
        };
        String hienThi = b.thoiHan() + " " + donVi;
        try {
            LocalDate ngayLap = LocalDate.parse(txtNgayLap.getText().trim(), DINH_DANG_NGAY);
            LocalDate ngayHetHan = switch (b.donViThoiGian()) {
                case "NGAY" -> ngayLap.plusDays(b.thoiHan());
                case "NAM" -> ngayLap.plusYears(b.thoiHan());
                default -> ngayLap.plusMonths(b.thoiHan());
            };
            return hienThi + " (đến " + ngayHetHan.format(DINH_DANG_NGAY) + ")";
        } catch (DateTimeParseException ex) {
            return hienThi;
        }
    }

    private String taoThongTinBaoHanhTooltip(PhuTungBaoHanh b) {
        StringBuilder sb = new StringBuilder("<html><b>").append(thoatHtml(b.tenChinhSach())).append("</b>");
        themDongTooltip(sb, "Nội dung", b.noiDungBaoHanh());
        themDongTooltip(sb, "Điều kiện", b.dieuKienBaoHanh());
        themDongTooltip(sb, "Từ chối", b.truongHopTuChoi());
        return sb.append("</html>").toString();
    }

    private void themDongTooltip(StringBuilder sb, String nhan, String noiDung) {
        if (noiDung != null && !noiDung.isBlank()) {
            sb.append("<br>").append(nhan).append(": ").append(thoatHtml(noiDung));
        }
    }

    private String thoatHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Xóa phần nhập phiếu/phụ tùng, giữ nguyên bảng chi tiết.
    public void lamMoiNhapLieu() {
        txtMaPhieuSuaChua.setText("");
        if (cboPhuTung.getItemCount() > 0) {
            cboPhuTung.setSelectedIndex(0);
        } else {
            txtDonGia.setText("0");
        }
        spnSoLuong.setValue(1);
    }

    // Đưa các ô chung về mặc định của hóa đơn mới (khuyến mãi, ngày lập, loại hóa
    // đơn).
    public void datLaiForm() {
        if (cboKhuyenMai.getItemCount() > 0) {
            cboKhuyenMai.setSelectedIndex(0);
        }
        txtNgayLap.setText(LocalDate.now().format(DINH_DANG_NGAY));
        cboLoaiHoaDon.setSelectedIndex(0);
    }

    // Lịch sử
    public void boChonLichSu() {
        tblLichSu.clearSelection();
    }

    private Object[] dongLichSu(LichSuHoaDon h) {
        return new Object[] {
                h.ma(),
                h.ngayLap() == null ? "" : h.ngayLap().format(DINH_DANG_NGAY),
                h.maNV(),
                coPhieu(h.maPhieu()) ? h.maPhieu() : "—",
                h.tongThanhToan(),
                h.trangThai() };
    }

    // Đưa hóa đơn lên đầu lịch sử (thay dòng cũ nếu trùng mã) và chọn dòng đó.
    public void themVaoDauLichSu(LichSuHoaDon h) {
        for (int i = modelLichSu.getRowCount() - 1; i >= 0; i--) {
            if (h.ma().equals(modelLichSu.getValueAt(i, 0))) {
                modelLichSu.removeRow(i);
            }
        }
        modelLichSu.insertRow(0, dongLichSu(h));
        tblLichSu.setRowSelectionInterval(0, 0);
        tblLichSu.scrollRectToVisible(tblLichSu.getCellRect(0, 0, true));
    }

    // Cập nhật lại toàn bộ bảng lịch sử hóa đơn.
    public void capNhatLichSu(List<LichSuHoaDon> dsLichSu) {
        modelLichSu.setRowCount(0);

        if (dsLichSu != null) {
            for (LichSuHoaDon h : dsLichSu) {
                modelLichSu.addRow(dongLichSu(h));
            }
        }
    }

    // HỘP THOẠI VÀ CỬA SỔ PHỤ
    public void canhBao(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    public void baoLoi(String tieuDe, Exception ex) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void thongBaoThanhCong(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean xacNhan(String msg, String tieuDe) {
        return JOptionPane.showConfirmDialog(this, msg, tieuDe,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // Cửa sổ tìm phiếu: khiChon trả true thì đóng cửa sổ.
    public void moCuaSoTimPhieu(Predicate<String> khiChon) {
        JFrame win = new JFrame("Tìm phiếu sửa chữa");
        win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        win.setSize(1000, 480);
        win.setMinimumSize(new Dimension(800, 400));
        win.setLocationRelativeTo(this);
        win.setContentPane(new FrmTimPhieuSuaChua(maPhieu -> {
            if (khiChon.test(maPhieu)) {
                win.dispose();
            }
        }));
        win.setVisible(true);
    }

    // DỰNG GIAO DIỆN
    // Khung chung
    private JScrollPane taoVungCuon() {
        // Ẩn thanh cuộn nhưng vẫn cuộn được bằng con lăn chuột
        JScrollPane sp = new JScrollPane(taoNoiDung(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        return sp;
    }

    private JPanel taoTieuDe() {
        JLabel lblTieuDe = new JLabel("LẬP HÓA ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(FONT_TIEU_DE);
        lblTieuDe.setForeground(MAU_CHU);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnThemHoaDon);

        // Khối trống bên trái cùng bề rộng với cụm nút để tiêu đề nằm chính giữa
        JPanel pnlCanBang = new JPanel();
        pnlCanBang.setOpaque(false);
        pnlCanBang.setPreferredSize(pnlNut.getPreferredSize());

        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 4, 0));
        pnl.add(pnlCanBang, BorderLayout.WEST);
        pnl.add(lblTieuDe, BorderLayout.CENTER);
        pnl.add(pnlNut, BorderLayout.EAST);
        return pnl;
    }

    private JPanel taoNoiDung() {
        JPanel pnl = new TrangCuon();
        pnl.setLayout(new GridBagLayout());
        pnl.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridy = 0;
        g.weighty = 1;

        g.gridx = 0;
        g.weightx = 0.35;
        g.insets = new Insets(0, 0, 0, 14);
        pnl.add(taoCotTrai(), g);

        g.gridx = 1;
        g.weightx = 0.65;
        g.insets = new Insets(0, 0, 0, 0);
        pnl.add(taoCotPhai(), g);

        // Lịch sử hóa đơn nằm dưới hai cột, chiếm cả bề ngang
        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 0, 0, 0);
        pnl.add(taoTheLichSu(), g);
        return pnl;
    }

    private JPanel taoCotTrai() {
        JPanel col = new CotNoiDung(430, 380);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.anchor = GridBagConstraints.NORTH;

        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        col.add(taoTheThongTinHoaDon(), g);

        // Thẻ Nguồn hóa đơn chiếm hết chiều cao còn lại -> đáy bằng khung Tổng kết
        g.gridy = 1;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(12, 0, 0, 0);
        col.add(taoTheNguon(), g);
        return col;
    }

    private JPanel taoCotPhai() {
        JPanel col = new CotNoiDung(760, 560);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;

        g.gridy = 0;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        col.add(taoTheChiTiet(), g);

        g.gridy = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 0, 0, 0);
        col.add(taoTheTongKet(), g);
        return col;
    }

    private The taoTheThongTinHoaDon() {
        The the = new The("THÔNG TIN HÓA ĐƠN");
        themDongKep(the.noiDung, 0, "Mã hóa đơn:", txtMaHoaDon, 0.4, "Ngày lập:", txtNgayLap, 0.6);
        themDongKep(the.noiDung, 1, "Mã NV:", txtMaNhanVienLapHoaDon, 0.4, "Tên NV:", txtTenNhanVienLapHoaDon, 0.6);
        themDong(the.noiDung, 2, "Loại hóa đơn:", cboLoaiHoaDon, 92);
        return the;
    }

    private The taoTheNguon() {
        theNguon = new The("NGUỒN HÓA ĐƠN");
        theNguon.noiDung.setLayout(new BorderLayout());

        pnlNguon.setOpaque(false);
        pnlNguon.add(taoTheTrong(), THE_TRONG);
        pnlNguon.add(neoTren(taoTheSuaChua()), THE_SUA_CHUA);
        pnlNguon.add(neoTren(taoThePhuTung()), THE_PHU_TUNG);

        theNguon.noiDung.add(pnlNguon, BorderLayout.CENTER);
        return theNguon;
    }

    // Hiển thị khi chưa chọn loại hóa đơn.
    private JPanel taoTheTrong() {
        JLabel lbl = new JLabel(
                "<html><div style='text-align:center'>Chọn loại hóa đơn ở trên<br>"
                        + "để bắt đầu lập hóa đơn.</div></html>",
                SwingConstants.CENTER);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(300, 120));
        pnl.add(lbl, BorderLayout.CENTER);
        return pnl;
    }

    // Hóa đơn sửa chữa: nhập mã phiếu, hệ thống tự điền khách hàng và xe.
    private JPanel taoTheSuaChua() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        JPanel dongMaPhieu = new JPanel(new BorderLayout(8, 0));
        dongMaPhieu.setOpaque(false);
        dongMaPhieu.add(txtMaPhieuSuaChua, BorderLayout.CENTER);
        dongMaPhieu.add(btnTimPhieu, BorderLayout.EAST);

        themDong(pnl, 0, "Mã phiếu:", dongMaPhieu);
        themTieuMuc(pnl, 1, "KHÁCH HÀNG VÀ XE");
        themDong(pnl, 2, "Mã khách hàng:", txtMaKhachHang);
        themDong(pnl, 3, "Tên khách hàng:", txtTenKhachHang);
        themDong(pnl, 4, "Số điện thoại:", txtSoDienThoai);
        themDong(pnl, 5, "Biển số xe:", txtBienSoXe);
        themDong(pnl, 6, "Tên xe:", txtTenXe);
        return pnl;
    }

    // Hóa đơn mua phụ tùng: chọn phụ tùng, số lượng, đơn giá rồi thêm vào bảng.
    private JPanel taoThePhuTung() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        themDong(pnl, 0, "Phụ tùng:", cboPhuTung);
        themDong(pnl, 1, "Số lượng:", spnSoLuong);
        themDong(pnl, 2, "Đơn giá:", txtDonGia);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 3;
        g.gridwidth = 2;
        g.weightx = 1;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(10, 0, 0, 0);
        pnl.add(btnThemPhuTung, g);
        return pnl;
    }

    private JPanel neoTren(JPanel p) {
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(p, BorderLayout.NORTH);
        return w;
    }

    private The taoTheChiTiet() {
        The the = new The("CHI TIẾT HÓA ĐƠN");
        the.noiDung.setLayout(new BorderLayout(0, 10));

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        kieuKhungCuon(scroll, 500, 120, 300, 100);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnLamMoiNhapLieu);
        pnlNut.add(btnXoaDong);

        JPanel pnlGiua = new JPanel(new BorderLayout(0, 10));
        pnlGiua.setOpaque(false);
        pnlGiua.add(scroll, BorderLayout.CENTER);
        pnlGiua.add(taoTheBaoHanh(), BorderLayout.SOUTH);

        the.noiDung.add(pnlGiua, BorderLayout.CENTER);
        the.noiDung.add(pnlNut, BorderLayout.SOUTH);
        return the;
    }

    private JPanel taoTheBaoHanh() {
        JLabel lblTieuDe = new JLabel("PHỤ TÙNG ĐƯỢC BẢO HÀNH");
        lblTieuDe.setFont(FONT_DAM);
        lblTieuDe.setForeground(MAU_NUT_DAM);

        lblTinhTrangBaoHanh.setFont(FONT_THUONG);
        lblTinhTrangBaoHanh.setForeground(MAU_PHU);
        lblTinhTrangBaoHanh.setToolTipText("Thời hạn bảo hành được tính từ ngày lập hóa đơn.");

        JPanel pnlTieuDe = new JPanel(new BorderLayout(8, 0));
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(lblTieuDe, BorderLayout.WEST);
        pnlTieuDe.add(lblTinhTrangBaoHanh, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(tblBaoHanh);
        kieuKhungCuon(scroll, 500, 112, 300, 80);

        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);
        pnl.add(pnlTieuDe, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    // Tổng kết
    private The taoTheTongKet() {
        The the = new The("TỔNG KẾT HÓA ĐƠN");
        the.noiDung.setLayout(new BorderLayout(0, 12));
        the.noiDung.setBorder(new EmptyBorder(6, 0, 12, 12));

        JPanel pnlHaiNua = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHaiNua.setOpaque(false);
        pnlHaiNua.add(taoNuaTraiTongKet());
        pnlHaiNua.add(taoNuaPhaiTongKet());

        the.noiDung.add(pnlHaiNua, BorderLayout.CENTER);
        the.noiDung.add(taoNutThaoTac(), BorderLayout.SOUTH);
        return the;
    }

    // Nửa trái: tiền dịch vụ, tiền phụ tùng và khuyến mãi.
    private JPanel taoNuaTraiTongKet() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, MAU_VIEN),
                new EmptyBorder(0, 0, 0, 12)));
        themDongTien(pnl, 0, "Tiền dịch vụ:", lblTienDichVu);
        themDongTien(pnl, 1, "Tiền phụ tùng:", lblTienPhuTung);

        JLabel lblKM = new JLabel("Khuyến mãi:");
        lblKM.setFont(FONT_THUONG);
        lblKM.setForeground(MAU_PHU);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.gridy = 2;
        g.insets = new Insets(10, 12, 2, 0);
        pnl.add(lblKM, g);
        g.gridy = 3;
        g.insets = new Insets(2, 12, 6, 0);
        pnl.add(cboKhuyenMai, g);
        return pnl;
    }

    // Nửa phải: tổng hóa đơn, thuế, giảm giá và tổng thanh toán.
    private JPanel taoNuaPhaiTongKet() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        themDongTien(pnl, 0, "Tổng hóa đơn (đã gồm VAT):", lblTongHoaDon);
        themDongTien(pnl, 1, "Tiền thuế (VAT 8%):", lblTienVAT);
        themDongTien(pnl, 2, "Tiền giảm:", lblTienGiam);

        GridBagConstraints gs = new GridBagConstraints();
        gs.gridy = 3;
        gs.gridwidth = 2;
        gs.weightx = 1;
        gs.fill = GridBagConstraints.HORIZONTAL;
        gs.insets = new Insets(6, 12, 6, 0);
        pnl.add(new JSeparator(), gs);

        JLabel lblNhanTong = new JLabel("Tổng thanh toán:");
        lblNhanTong.setFont(FONT_NHAN_TONG);
        lblNhanTong.setForeground(MAU_CHU);

        GridBagConstraints gt = new GridBagConstraints();
        gt.gridy = 4;
        gt.gridx = 0;
        gt.weightx = 1;
        gt.anchor = GridBagConstraints.WEST;
        gt.insets = new Insets(4, 12, 4, 8);
        pnl.add(lblNhanTong, gt);

        gt.gridx = 1;
        gt.weightx = 0;
        gt.anchor = GridBagConstraints.EAST;
        gt.insets = new Insets(4, 0, 4, 0);
        pnl.add(lblTongThanhToan, gt);
        return pnl;
    }

    private JPanel taoNutThaoTac() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnl.setOpaque(false);
        pnl.add(btnLamMoi);
        pnl.add(btnLuu);
        pnl.add(btnThanhToan);
        return pnl;
    }

    // Lịch sử hóa đơn
    private The taoTheLichSu() {
        The the = new The("LỊCH SỬ HÓA ĐƠN");
        the.themBenPhaiTieuDe(lblSoLichSu);
        the.noiDung.setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(tblLichSu,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        kieuKhungCuon(scroll, 500, 160, 300, 120);
        scroll.getVerticalScrollBar().setUnitIncrement(30);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); // ẩn thanh cuộn, vẫn lăn chuột được

        the.noiDung.add(scroll, BorderLayout.CENTER);
        return the;
    }

    // Thành phần form dùng chung
    private void themDong(JPanel pnl, int hang, String nhan, JComponent o) {
        themDong(pnl, hang, nhan, o, 122);
    }

    private void themDong(JPanel pnl, int hang, String nhan, JComponent o, int rongNhan) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 0, 4, 10);
        pnl.add(taoNhan(nhan, rongNhan), g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        pnl.add(o, g);
    }

    private void themDongKep(JPanel pnl, int hang, String nhan1, JComponent o1, double w1,
            String nhan2, JComponent o2, double w2) {
        o1.setPreferredSize(new Dimension(10, 32));
        o2.setPreferredSize(new Dimension(10, 32));

        JPanel dong = new JPanel(new GridBagLayout());
        dong.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.insets = new Insets(0, 0, 0, 10);
        dong.add(taoNhan(nhan1, 78), g);

        g.gridx = 1;
        g.weightx = w1;
        g.insets = new Insets(0, 0, 0, 14);
        dong.add(o1, g);

        g.gridx = 2;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 0, 8);
        dong.add(taoNhan(nhan2, 78), g);

        g.gridx = 3;
        g.weightx = w2;
        g.insets = new Insets(0, 0, 0, 0);
        dong.add(o2, g);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = hang;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);
        pnl.add(dong, gc);
    }

    private JLabel taoNhan(String nhan, int rong) {
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);
        lbl.setPreferredSize(new Dimension(rong, 32));
        return lbl;
    }

    private void themTieuMuc(JPanel pnl, int hang, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_NHO_DAM);
        lbl.setForeground(MAU_NUT_DAM);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                new EmptyBorder(0, 0, 4, 0)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(12, 0, 4, 0);
        pnl.add(lbl, g);
    }

    private void themDongTien(JPanel pnl, int hang, String nhan, JLabel giaTri) {
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.insets = new Insets(5, 12, 5, 0);
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        pnl.add(lbl, g);

        g.gridx = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        pnl.add(giaTri, g);
    }

    // KIỂU DÁNG
    private void kieuCacOComponent() {
        for (JTextField t : new JTextField[] {
                txtMaHoaDon, txtNgayLap, txtMaNhanVienLapHoaDon, txtTenNhanVienLapHoaDon }) {
            kieuO(t, true);
        }
        for (JTextField t : oTuDien) {
            kieuO(t, true);
        }
        kieuO(txtMaPhieuSuaChua, false);
        kieuO(txtDonGia, false);
        txtMaPhieuSuaChua.setToolTipText("Nhập mã phiếu sửa chữa rồi nhấn Enter");

        for (JComboBox<?> c : new JComboBox<?>[] { cboLoaiHoaDon, cboPhuTung, cboKhuyenMai }) {
            c.setFont(FONT_THUONG);
            c.setBackground(Color.WHITE);
            c.setPreferredSize(new Dimension(120, 32));
        }
        cboKhuyenMai.setMaximumRowCount(8);
        cboKhuyenMai.setToolTipText("Chọn chương trình khuyến mãi");

        spnSoLuong.setFont(FONT_THUONG);
        spnSoLuong.setPreferredSize(new Dimension(120, 32));

        lblSoLichSu.setFont(FONT_THUONG);
        lblSoLichSu.setForeground(MAU_PHU);

        // Các nhãn số tiền ở khu tổng kết
        for (JLabel lbl : new JLabel[] {
                lblTienDichVu, lblTienPhuTung, lblTongHoaDon, lblTienVAT, lblTienGiam }) {
            lbl.setFont(FONT_GIA_TRI);
            lbl.setForeground(MAU_CHU);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            lbl.setPreferredSize(new Dimension(160, 28));
        }
        lblTienGiam.setForeground(MAU_DO);
        lblTongHoaDon.setForeground(MAU_NUT);

        lblTongThanhToan.setFont(FONT_TONG);
        lblTongThanhToan.setForeground(MAU_XANH_LA);
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongThanhToan.setPreferredSize(new Dimension(200, 34));
    }

    private void kieuO(JTextField txt, boolean chiDoc) {
        txt.setFont(FONT_THUONG);
        txt.setForeground(MAU_CHU);
        txt.setEditable(!chiDoc);
        txt.setBackground(chiDoc ? MAU_NEN_O : Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAU_VIEN),
                new EmptyBorder(4, 8, 4, 8)));
        txt.setPreferredSize(new Dimension(120, 32));
    }

    private void kieuKhungCuon(JScrollPane sp, int rong, int cao, int rongMin, int caoMin) {
        sp.setBorder(BorderFactory.createLineBorder(MAU_VIEN));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(rong, cao));
        sp.setMinimumSize(new Dimension(rongMin, caoMin));
    }

    // Kiểu chung cho cả ba bảng: font, dòng kẻ, màu chọn, tiêu đề cột, bề rộng cột.
    private void kieuBang(JTable t, int caoDong, int... rong) {
        t.setFont(FONT_THUONG);
        t.setRowHeight(caoDong);
        t.setShowVerticalLines(false);
        t.setGridColor(MAU_VIEN);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(MAU_CHON);
        t.setSelectionForeground(MAU_CHU);

        JTableHeader header = t.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(FONT_DAM);
        header.setBackground(MAU_NEN_TIEU_DE);
        header.setForeground(MAU_CHU);
        header.setPreferredSize(new Dimension(0, 34));

        for (int i = 0; i < rong.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }
    }

    private void datRenderer(JTable t, TableCellRenderer... renderer) {
        for (int i = 0; i < renderer.length; i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(renderer[i]);
        }
    }

    private void cauHinhBangChiTiet() {
        kieuBang(tblChiTiet, 30, 80, 90, 280, 70, 120, 130);
        TableCellRenderer giua = rendererChu(SwingConstants.CENTER, 0, 0);
        TableCellRenderer trai = rendererChu(SwingConstants.LEFT, 10, 6);
        TableCellRenderer tien = rendererTien(SwingConstants.RIGHT, 6, 12);
        datRenderer(tblChiTiet, giua, giua, trai, giua, tien, tien);
    }

    private void cauHinhBangBaoHanh() {
        kieuBang(tblBaoHanh, 28, 190, 75, 190, 190);
    }

    private void cauHinhBangLichSu() {
        kieuBang(tblLichSu, 30, 130, 110, 100, 170, 160, 140);
        TableCellRenderer giua = rendererChu(SwingConstants.CENTER, 0, 0);
        TableCellRenderer tien = rendererTien(SwingConstants.CENTER, 6, 6);
        datRenderer(tblLichSu, giua, giua, giua, giua, tien, giua);

        modelLichSu.addTableModelListener(
                e -> lblSoLichSu.setText(modelLichSu.getRowCount() + " hóa đơn"));
    }

    private static DefaultTableCellRenderer rendererChu(int canh, int trai, int phai) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(canh);
        r.setBorder(new EmptyBorder(0, trai, 0, phai));
        return r;
    }

    private static DefaultTableCellRenderer rendererTien(int canh, int trai, int phai) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object v) {
                setText(v instanceof BigDecimal b ? dinhDangTien(b) : "");
            }
        };
        r.setHorizontalAlignment(canh);
        r.setBorder(new EmptyBorder(0, trai, 0, phai));
        return r;
    }

    // Sự kiện chỉ phục vụ hiển thị (không có nghiệp vụ nên để trong view)
    private void caiDatSuKienHienThi() {
        txtNgayLap.getDocument().addDocumentListener(khiDoi(this::capNhatThoiHanBaoHanhTrongBang));
        cboPhuTung.addActionListener(e -> capNhatDonGiaPhuTung());
    }

    private static DocumentListener khiDoi(Runnable viec) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                viec.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                viec.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                viec.run();
            }
        };
    }

    private void capNhatDonGiaPhuTung() {
        PhuTung pt = (PhuTung) cboPhuTung.getSelectedItem();
        txtDonGia.setText(pt == null ? "0" : pt.giaBan().toPlainString());
    }

    // TIỆN ÍCH
    private static String dinhDangTien(BigDecimal tien) {
        return DINH_DANG_TIEN.format(tien) + " đ";
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    // Nút chính: nền xanh, chữ trắng.
    private JButton taoNut(String text) {
        return taoNutMau(text, MAU_NUT, MAU_NUT_DAM, Color.WHITE, MAU_NUT_DAM);
    }

    // Nút phụ: nền trắng, viền xám, chữ tối.
    private JButton taoNutPhu(String text) {
        return taoNutMau(text, Color.WHITE, new Color(241, 245, 249), MAU_CHU, new Color(180, 190, 205));
    }

    private JButton taoNutMau(String text, Color nen, Color nenKhiRe, Color chu, Color vien) {
        JButton btn = new JButton(text);

        // BasicButtonUI để Look and Feel không ghi đè màu nền
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFont(FONT_DAM);
        btn.setForeground(chu);
        btn.setBackground(nen);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(vien, 1),
                BorderFactory.createEmptyBorder(4, 14, 4, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Bề rộng theo chữ, tối thiểu 90px, cao đều 34px
        int rong = Math.max(90, btn.getFontMetrics(FONT_DAM).stringWidth(text) + 40);
        btn.setPreferredSize(new Dimension(rong, 34));
        btn.setMinimumSize(new Dimension(rong, 34));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(nenKhiRe);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(nen);
            }
        });
        return btn;
    }

    // LỚP LỒNG HỖ TRỢ GIAO DIỆN
    private static class MoHinhBang extends DefaultTableModel {
        MoHinhBang(String... tenCot) {
            super(tenCot, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private static class BangSoc extends JTable {
        BangSoc(TableModel model) {
            super(model);
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : MAU_DONG_CHAN);
                c.setForeground(MAU_CHU);
            }
            return c;
        }
    }

    // Thẻ (card) có thanh tiêu đề, dùng cho mọi khu vực của màn hình.
    private static class The extends JPanel {
        final JPanel noiDung = new JPanel(new GridBagLayout());
        private final JLabel lblTieuDe = new JLabel();
        private final JPanel pnlDau = new JPanel(new BorderLayout(8, 0));

        The(String tieuDe) {
            super(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(MAU_VIEN));

            lblTieuDe.setFont(FONT_DAM);
            lblTieuDe.setForeground(MAU_NUT_DAM);
            datTieuDe(tieuDe);

            pnlDau.setBackground(MAU_NEN_TIEU_DE);
            pnlDau.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                    new EmptyBorder(8, 12, 8, 12)));
            pnlDau.add(lblTieuDe, BorderLayout.WEST);

            noiDung.setBackground(Color.WHITE);
            noiDung.setBorder(new EmptyBorder(10, 12, 12, 12));

            add(pnlDau, BorderLayout.NORTH);
            add(noiDung, BorderLayout.CENTER);
        }

        void datTieuDe(String tieuDe) {
            lblTieuDe.setText(tieuDe);
        }

        void themBenPhaiTieuDe(JComponent c) {
            pnlDau.add(c, BorderLayout.EAST);
        }
    }

    private static class CardTheoThe extends CardLayout {
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets in = parent.getInsets();
                for (Component c : parent.getComponents()) {
                    if (c.isVisible()) {
                        Dimension d = c.getPreferredSize();
                        return new Dimension(d.width + in.left + in.right,
                                d.height + in.top + in.bottom);
                    }
                }
                return super.preferredLayoutSize(parent);
            }
        }
    }

    // Panel cuộn: co giãn theo chiều ngang, tự giãn đầy khung khi cửa sổ cao.
    private static class TrangCuon extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return 120;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            Container cha = getParent();
            return cha instanceof JViewport && cha.getHeight() > getPreferredSize().height;
        }
    }

    private static class CotNoiDung extends JPanel {
        private final int rongUaThich;
        private final int rongToiThieu;

        CotNoiDung(int rongUaThich, int rongToiThieu) {
            super(new GridBagLayout());
            setOpaque(false);
            this.rongUaThich = rongUaThich;
            this.rongToiThieu = rongToiThieu;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width, rongUaThich), d.height);
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            return new Dimension(Math.max(d.width, rongToiThieu), d.height);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BaseFrame frame = new BaseFrame();
            FrmHoaDon frmHoaDon = new FrmHoaDon();
            frame.setContent(frmHoaDon);
            frmHoaDon.taiDuLieuBanDauAsync();
            frame.setVisible(true);
        });
    }
}
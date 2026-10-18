package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FrmThanhToan extends JFrame {

    // ================== MÀU SẮC ==================
    private static final Color BRAND = new Color(0x2150E8);
    private static final Color BLUE_DARK = new Color(0x1D4ED8);
    private static final Color TEXT = new Color(0x17233B);
    private static final Color MUTED = new Color(0x64748B);
    private static final Color LINE = new Color(0xDBE2EE);
    private static final Color SOFT = new Color(0xF4F7FC);
    private static final Color FIELD_BG = new Color(0xF1F5F9);
    private static final Color GREEN = new Color(0x15803D);
    private static final Color ORANGE = new Color(0xB45309);

    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private static final String CARD_TIEN_MAT = "TIEN_MAT";
    private static final String CARD_CHUYEN_KHOAN = "CHUYEN_KHOAN";

    // ================== DỮ LIỆU HÓA ĐƠN ==================
    private String maHoaDon;
    private BigDecimal tongTien = BigDecimal.ZERO;
    private BigDecimal daThanhToan = BigDecimal.ZERO;

    private boolean coGiaoDich = false;
    private final List<String> lichSuIn = new ArrayList<>();

    // ================== THÀNH PHẦN GIAO DIỆN ==================
    // Các ô thông tin hóa đơn: chỉ đọc, sau này dữ liệu tự cập nhật từ CSDL
    private JTextField txtMaHoaDon;
    private JTextField txtNgayLap;
    private JTextField txtMaKhachHang;
    private JTextField txtTenKhachHang;
    private JTextField txtMaPhieuSuaChua;
    private JTextField txtTrangThaiHoaDon;

    private JLabel lblTongTien;
    private JLabel lblDaThanhToan;
    private JLabel lblConPhaiTra;
    private JLabel lblTienThua;
    private JLabel lblHuongDan;
    private JLabel lblTrangThaiGiaoDich;
    private JLabel lblMoTaGiaoDich;

    private JToggleButton btnTienMat;
    private JToggleButton btnChuyenKhoan;
    private JPanel pnlKhuVucPhuongThuc;
    private CardLayout cardPhuongThuc;

    private JTextField txtSoTienThanhToan;
    private JTextField txtTienKhachDua;
    private JButton btnThanhToanDu;

    private DefaultListModel<String> modelLichSu;
    private JList<String> listLichSu;
    private JScrollPane cuonChinh;

    private JButton btnXacNhan;
    private JButton btnDong;

    // ================== CONSTRUCTOR ==================
    public FrmThanhToan(String maHoaDon) {
        this.maHoaDon = maHoaDon == null ? "" : maHoaDon.trim();

        setTitle("Thanh toán hóa đơn - AUTOGARA PRO");
        // Vùng làm việc của màn hình (đã trừ thanh taskbar) để cửa sổ không bị thấp hơn
        // màn hình
        Rectangle man = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setSize(Math.min(1100, man.width), Math.min(800, man.height));
        setMinimumSize(new Dimension(
                Math.min(1000, man.width), Math.min(560, man.height)));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        khoiTaoGiaoDien();
        ganSuKien();

        // Tự nạp hóa đơn khi mở form (hiện là dữ liệu mẫu)
        taiDuLieuMau();
    }

    // ================== KHỞI TẠO GIAO DIỆN ==================
    private void khoiTaoGiaoDien() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(SOFT);
        root.setBorder(new EmptyBorder(14, 18, 14, 18));
        setContentPane(root);

        root.add(taoTieuDe(), BorderLayout.NORTH);

        // Nội dung cuộn được bằng con lăn chuột, nhưng ẩn thanh cuộn
        JPanel haiCot = new JPanel(new GridLayout(1, 2, 16, 0));
        haiCot.setOpaque(false);
        haiCot.add(taoCotTrai());
        haiCot.add(taoCotPhai());

        // Hai cột + trạng thái giao dịch + nút cùng nằm trong vùng cuộn
        ViewCuon noiDung = new ViewCuon(new BorderLayout(0, 16));
        noiDung.setOpaque(false);
        noiDung.setBorder(new EmptyBorder(0, 0, 20, 0));
        noiDung.add(haiCot, BorderLayout.CENTER);
        noiDung.add(taoKhuVucNut(), BorderLayout.SOUTH);

        cuonChinh = new JScrollPane(noiDung,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cuonChinh.setBorder(null);
        cuonChinh.setOpaque(false);
        cuonChinh.getViewport().setOpaque(false);

        // Khi ẩn thanh cuộn, Swing không tự cuộn bằng con lăn -> tự xử lý
        cuonChinh.setWheelScrollingEnabled(false);
        cuonChinh.addMouseWheelListener(e -> {
            JViewport vp = cuonChinh.getViewport();
            Component view = vp.getView();
            if (view == null) {
                return;
            }
            int toiDa = Math.max(0, view.getHeight() - vp.getHeight());
            Point p = vp.getViewPosition();
            int y = p.y + e.getUnitsToScroll() * 16;
            y = Math.max(0, Math.min(toiDa, y));
            vp.setViewPosition(new Point(p.x, y));
        });

        root.add(cuonChinh, BorderLayout.CENTER);
    }

    // ================== TIÊU ĐỀ ==================
    private JPanel taoTieuDe() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("THANH TOÁN HÓA ĐƠN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT);

        panel.add(title, BorderLayout.CENTER);
        return panel;
    }

    // ================== CỘT TRÁI ==================
    private JPanel taoCotTrai() {
        JPanel cot = new JPanel(new BorderLayout(0, 16));
        cot.setOpaque(false);

        // ---- 1. Thông tin hóa đơn ----
        JPanel thongTin = taoCard();
        thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
        thongTin.add(taoTieuDeMuc("1. Thông tin hóa đơn"));
        thongTin.add(Box.createVerticalStrut(14));

        txtMaHoaDon = taoOChiDoc();
        txtNgayLap = taoOChiDoc();
        txtMaKhachHang = taoOChiDoc();
        txtTenKhachHang = taoOChiDoc();
        txtMaPhieuSuaChua = taoOChiDoc();
        txtTrangThaiHoaDon = taoOChiDoc();

        JPanel luoi = new JPanel(new GridLayout(0, 2, 14, 14));
        luoi.setOpaque(false);
        luoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        luoi.add(taoO("Mã hóa đơn", txtMaHoaDon));
        luoi.add(taoO("Ngày lập", txtNgayLap));
        luoi.add(taoO("Mã khách hàng", txtMaKhachHang));
        luoi.add(taoO("Tên khách hàng", txtTenKhachHang));
        luoi.add(taoO("Mã phiếu sửa chữa", txtMaPhieuSuaChua));
        luoi.add(taoO("Trạng thái hóa đơn", txtTrangThaiHoaDon));
        thongTin.add(luoi);

        // ---- 2. Lịch sử thanh toán ----
        JPanel lichSu = taoCard();
        lichSu.setLayout(new BorderLayout(0, 12));

        // Chỉnh chiều cao khung lịch sử ngắn lại
        lichSu.setPreferredSize(new Dimension(450, 230));
        lichSu.add(taoTieuDeMuc("2. Lịch sử thanh toán"), BorderLayout.NORTH);

        modelLichSu = new DefaultListModel<>();
        listLichSu = new JList<>(modelLichSu);
        listLichSu.setFont(FONT);
        listLichSu.setFixedCellHeight(54);
        listLichSu.setBackground(Color.WHITE);
        listLichSu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listLichSu.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lb.setBorder(new EmptyBorder(6, 12, 6, 12));
                lb.setFont(FONT);
                return lb;
            }
        });

        JScrollPane scroll = new JScrollPane(listLichSu);
        scroll.setBorder(new LineBorder(LINE));
        scroll.addMouseWheelListener(e -> {
            if (cuonChinh != null && !scroll.getVerticalScrollBar().isVisible()) {
                cuonChinh.dispatchEvent(
                        SwingUtilities.convertMouseEvent(scroll, e, cuonChinh));
            }
        });
        lichSu.add(scroll, BorderLayout.CENTER);

        JLabel ghiChu = new JLabel("Lịch sử đang sử dụng dữ liệu mẫu.");
        ghiChu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ghiChu.setForeground(MUTED);
        lichSu.add(ghiChu, BorderLayout.SOUTH);

        cot.add(thongTin, BorderLayout.NORTH);
        cot.add(lichSu, BorderLayout.CENTER);
        return cot;
    }

    // ================== CỘT PHẢI ==================
    private JPanel taoCotPhai() {
        // ---- 3. Tổng quan thanh toán ----
        JPanel tongQuan = taoCard();
        tongQuan.setLayout(new BoxLayout(tongQuan, BoxLayout.Y_AXIS));
        tongQuan.add(taoTieuDeMuc("3. Tổng quan thanh toán"));
        tongQuan.add(Box.createVerticalStrut(14));

        lblTongTien = taoGiaTri("0 ₫");
        lblDaThanhToan = taoGiaTri("0 ₫");
        lblConPhaiTra = taoGiaTriNoiBat("0 ₫");

        tongQuan.add(taoDongSoTien("Tổng tiền hóa đơn", lblTongTien));
        tongQuan.add(Box.createVerticalStrut(10));
        tongQuan.add(taoDongSoTien("Đã thanh toán", lblDaThanhToan));
        tongQuan.add(Box.createVerticalStrut(14));

        JPanel conNo = new JPanel();
        conNo.setLayout(new BoxLayout(conNo, BoxLayout.Y_AXIS));
        conNo.setBackground(new Color(0xEFF6FF));
        conNo.setBorder(new EmptyBorder(12, 14, 12, 14));
        conNo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMoTaNo = taoNhan("CÒN PHẢI THANH TOÁN");
        lblMoTaNo.setForeground(BLUE_DARK);
        lblMoTaNo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblConPhaiTra.setAlignmentX(Component.LEFT_ALIGNMENT);
        conNo.add(lblMoTaNo);
        conNo.add(Box.createVerticalStrut(6));
        conNo.add(lblConPhaiTra);
        tongQuan.add(conNo);

        // ---- 4. Phương thức thanh toán (chia 2 nửa) ----
        JPanel phuongThuc = taoCard();
        phuongThuc.setLayout(new BoxLayout(phuongThuc, BoxLayout.Y_AXIS));
        phuongThuc.add(taoTieuDeMuc("4. Phương thức thanh toán"));
        phuongThuc.add(Box.createVerticalStrut(12));

        btnTienMat = taoToggle("Tiền mặt");
        btnChuyenKhoan = taoToggle("Chuyển khoản / QR");
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(btnTienMat);
        nhom.add(btnChuyenKhoan);
        btnTienMat.setSelected(true);

        JPanel hangToggle = new JPanel(new GridLayout(1, 2, 10, 0));
        hangToggle.setOpaque(false);
        hangToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangToggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        hangToggle.add(btnTienMat);
        hangToggle.add(btnChuyenKhoan);
        phuongThuc.add(hangToggle);

        lblHuongDan = new JLabel("Tiền mặt: nhận tiền trực tiếp tại quầy.");
        lblHuongDan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHuongDan.setForeground(MUTED);
        lblHuongDan.setAlignmentX(Component.LEFT_ALIGNMENT);
        phuongThuc.add(Box.createVerticalStrut(10));
        phuongThuc.add(lblHuongDan);

        // ---- 5. Số tiền thanh toán lần này ----
        JPanel soTien = taoCard();
        soTien.setLayout(new BoxLayout(soTien, BoxLayout.Y_AXIS));
        soTien.add(taoTieuDeMuc("5. Số tiền thanh toán lần này"));
        soTien.add(Box.createVerticalStrut(12));

        btnThanhToanDu = new JButton("Điền đủ số còn nợ");
        btnThanhToanDu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnThanhToanDu.setForeground(BRAND);
        btnThanhToanDu.setContentAreaFilled(false);
        btnThanhToanDu.setBorderPainted(false);
        btnThanhToanDu.setFocusPainted(false);
        btnThanhToanDu.setMargin(new Insets(0, 0, 0, 0));
        btnThanhToanDu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel hangNhan = new JPanel(new BorderLayout());
        hangNhan.setOpaque(false);
        hangNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangNhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        hangNhan.add(taoNhan("Số tiền khách muốn thanh toán (nhập tay)"), BorderLayout.WEST);
        hangNhan.add(btnThanhToanDu, BorderLayout.EAST);
        soTien.add(hangNhan);
        soTien.add(Box.createVerticalStrut(5));

        txtSoTienThanhToan = taoTextField();
        soTien.add(txtSoTienThanhToan);
        soTien.add(Box.createVerticalStrut(12));

        // Khu vực đổi theo phương thức: tiền mặt / chuyển khoản
        cardPhuongThuc = new CardLayout();
        pnlKhuVucPhuongThuc = new JPanel(cardPhuongThuc);
        pnlKhuVucPhuongThuc.setOpaque(false);
        pnlKhuVucPhuongThuc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlTienMat = new JPanel();
        pnlTienMat.setOpaque(false);
        pnlTienMat.setLayout(new BoxLayout(pnlTienMat, BoxLayout.Y_AXIS));

        JLabel lblKhachDua = taoNhan("Tiền khách đưa");
        lblKhachDua.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtTienKhachDua = taoTextField();
        lblTienThua = taoGiaTri("0 ₫");
        lblTienThua.setForeground(GREEN);

        JPanel hangTienThua = taoDongSoTien("Tiền thừa trả khách", lblTienThua);
        hangTienThua.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlTienMat.add(lblKhachDua);
        pnlTienMat.add(Box.createVerticalStrut(5));
        pnlTienMat.add(txtTienKhachDua);
        pnlTienMat.add(Box.createVerticalStrut(12));
        pnlTienMat.add(hangTienThua);

        JPanel pnlChuyenKhoan = new JPanel(new BorderLayout());
        pnlChuyenKhoan.setOpaque(false);
        JLabel lblGoiY = new JLabel("<html>Nhấn <b>Xác nhận thanh toán</b> để hiển thị mã QR "
                + "theo đúng số tiền đã nhập.<br>Khi khách chuyển khoản thành công, "
                + "hóa đơn sẽ được cập nhật.</html>");
        lblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblGoiY.setForeground(MUTED);
        lblGoiY.setVerticalAlignment(SwingConstants.TOP);
        pnlChuyenKhoan.add(lblGoiY, BorderLayout.NORTH);

        pnlKhuVucPhuongThuc.add(pnlTienMat, CARD_TIEN_MAT);
        pnlKhuVucPhuongThuc.add(pnlChuyenKhoan, CARD_CHUYEN_KHOAN);
        soTien.add(pnlKhuVucPhuongThuc);

        // ---- Ghép cột phải ----
        JPanel cot = new JPanel(new GridBagLayout());
        cot.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 16, 0);
        cot.add(tongQuan, c);

        c.gridy = 1;
        cot.add(phuongThuc, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        cot.add(soTien, c);

        JPanel filler = new JPanel();
        filler.setOpaque(false);
        c.gridy = 3;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        cot.add(filler, c);

        return cot;
    }

    // ================== TRẠNG THÁI GIAO DỊCH ==================
    private JPanel taoTrangThaiGiaoDich() {
        JPanel panel = taoCard();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = taoTieuDeMuc("6. Trạng thái giao dịch");

        lblTrangThaiGiaoDich = new JLabel("● Chưa có giao dịch");
        lblTrangThaiGiaoDich.setFont(FONT_BOLD);
        lblTrangThaiGiaoDich.setForeground(ORANGE);
        lblTrangThaiGiaoDich.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMoTaGiaoDich = new JLabel("Chưa có giao dịch được xác nhận.");
        lblMoTaGiaoDich.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMoTaGiaoDich.setForeground(MUTED);
        lblMoTaGiaoDich.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(lblTrangThaiGiaoDich);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblMoTaGiaoDich);
        return panel;
    }

    // ================== KHU VỰC NÚT ==================
    private JPanel taoKhuVucNut() {
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.setOpaque(false);

        btnDong = taoButton("Đóng", false);
        btnXacNhan = taoButton("Xác nhận thanh toán", true);

        // Kích thước cố định để nút luôn hiển thị đủ chiều cao
        btnDong.setPreferredSize(new Dimension(110, 44));
        btnXacNhan.setPreferredSize(new Dimension(210, 44));

        nut.add(btnDong);
        nut.add(btnXacNhan);

        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        footer.add(taoTrangThaiGiaoDich(), BorderLayout.CENTER);
        footer.add(nut, BorderLayout.SOUTH);
        return footer;
    }

    // ================== SỰ KIỆN ==================
    private void ganSuKien() {
        btnDong.addActionListener(e -> dispose());
        btnXacNhan.addActionListener(e -> xacNhanThanhToan());

        btnThanhToanDu.addActionListener(e -> {
            BigDecimal conNo = tinhConNo();
            if (conNo.compareTo(BigDecimal.ZERO) > 0) {
                txtSoTienThanhToan.setText(conNo.toPlainString());
            }
        });

        java.awt.event.ItemListener doiPhuongThuc = e -> capNhatPhuongThuc();
        btnTienMat.addItemListener(doiPhuongThuc);
        btnChuyenKhoan.addItemListener(doiPhuongThuc);

        khiThayDoi(txtTienKhachDua, this::capNhatTienThua);
        khiThayDoi(txtSoTienThanhToan, this::capNhatTienThua);

        capNhatPhuongThuc();
    }

    private void khiThayDoi(JTextField field, Runnable hanhDong) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                hanhDong.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                hanhDong.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                hanhDong.run();
            }
        });
    }

    private boolean laTienMat() {
        return btnTienMat.isSelected();
    }

    private void capNhatPhuongThuc() {
        capNhatMauToggle(btnTienMat);
        capNhatMauToggle(btnChuyenKhoan);

        if (laTienMat()) {
            cardPhuongThuc.show(pnlKhuVucPhuongThuc, CARD_TIEN_MAT);
            lblHuongDan.setText("Tiền mặt: nhận tiền trực tiếp tại quầy.");
        } else {
            cardPhuongThuc.show(pnlKhuVucPhuongThuc, CARD_CHUYEN_KHOAN);
            lblHuongDan.setText("Chuyển khoản / QR: khách quét mã QR để thanh toán.");
        }
        capNhatTienThua();
    }

    // ================== NẠP DỮ LIỆU MẪU ==================

    private void taiDuLieuMau() {
        // Thông tin hóa đơn để trống, chờ nhận dữ liệu hóa đơn thực tế
        maHoaDon = "";
        txtMaHoaDon.setText("");
        txtNgayLap.setText("");
        txtMaKhachHang.setText("");
        txtTenKhachHang.setText("");
        txtMaPhieuSuaChua.setText("");
        txtTrangThaiHoaDon.setText("");

        // Tổng tiền hóa đơn chưa có dữ liệu
        tongTien = BigDecimal.ZERO;
        daThanhToan = BigDecimal.ZERO;

        modelLichSu.clear();
        coGiaoDich = false;
        lichSuIn.clear();

        capNhatTongQuan();

        // Giữ nguyên chức năng chọn phương thức và nhập tiền
        txtSoTienThanhToan.setEnabled(true);
        txtTienKhachDua.setEnabled(true);
        btnThanhToanDu.setEnabled(true);
        btnTienMat.setEnabled(true);
        btnChuyenKhoan.setEnabled(true);
        btnXacNhan.setEnabled(true);

        datTrangThaiGiaoDich(
                "Chưa có hóa đơn",
                ORANGE,
                "Thông tin hóa đơn sẽ được cập nhật khi có dữ liệu.");
    }

    // ================== XÁC NHẬN THANH TOÁN ==================
    private void xacNhanThanhToan() {
        BigDecimal conNo = tinhConNo();
        BigDecimal soTien = docSoTien(txtSoTienThanhToan.getText());
        if (maHoaDon.isEmpty()
                || tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            baoLoi("Chưa có thông tin hóa đơn để thanh toán.");
            return;
        }
        if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
            baoLoi("Vui lòng nhập số tiền khách muốn thanh toán.");
            txtSoTienThanhToan.requestFocus();
            return;
        }

        if (soTien.compareTo(conNo) > 0) {
            baoLoi("Số tiền thanh toán không được lớn hơn số tiền còn nợ ("
                    + dinhDangTien(conNo) + ").");
            txtSoTienThanhToan.requestFocus();
            return;
        }

        if (laTienMat()) {
            thanhToanTienMat(soTien);
        } else {
            thanhToanChuyenKhoan(soTien);
        }
    }

    private void thanhToanTienMat(BigDecimal soTien) {
        BigDecimal tienKhachDua = docSoTien(txtTienKhachDua.getText());

        if (tienKhachDua == null || tienKhachDua.compareTo(soTien) < 0) {
            baoLoi("Tiền khách đưa phải lớn hơn hoặc bằng số tiền thanh toán.");
            txtTienKhachDua.requestFocus();
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận thanh toán tiền mặt hóa đơn " + maHoaDon
                        + "\nvới số tiền " + dinhDangTien(soTien) + "?",
                "Xác nhận thanh toán",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        BigDecimal tienThua = tienKhachDua.subtract(soTien);
        ghiNhanThanhToan(soTien, "Tiền mặt");

        JOptionPane.showMessageDialog(
                this,
                "Thanh toán tiền mặt thành công!\n"
                        + "Số tiền thanh toán: " + dinhDangTien(soTien) + "\n"
                        + "Tiền thừa trả khách: " + dinhDangTien(tienThua),
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);

        hoiInHoaDon();
    }

    private void thanhToanChuyenKhoan(BigDecimal soTien) {
        boolean daThanhToanQR = hienThiQR(soTien);

        if (!daThanhToanQR) {
            datTrangThaiGiaoDich("Đã hủy giao dịch chuyển khoản", ORANGE,
                    "Chưa ghi nhận khoản thanh toán nào.");
            return;
        }

        ghiNhanThanhToan(soTien, "Chuyển khoản / QR");

        JOptionPane.showMessageDialog(
                this,
                "Thanh toán chuyển khoản thành công!\n"
                        + "Số tiền: " + dinhDangTien(soTien),
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);

        hoiInHoaDon();
    }

    /**
     * Cập nhật số tiền, trạng thái hóa đơn và lịch sử sau khi thanh toán thành
     * công.
     */
    private void ghiNhanThanhToan(BigDecimal soTien, String phuongThuc) {
        daThanhToan = daThanhToan.add(soTien);

        String gio = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));

        if (!coGiaoDich) {
            modelLichSu.clear();
            coGiaoDich = true;
        }

        modelLichSu.add(0, "<html><b>" + dinhDangTien(soTien) + "</b> - "
                + phuongThuc + "<br><span style='color:#64748B'>"
                + gio + "</span></html>");
        lichSuIn.add(gio + " - " + phuongThuc + ": " + dinhDangTien(soTien));

        txtSoTienThanhToan.setText("");
        txtTienKhachDua.setText("");

        capNhatTongQuan();

        boolean hetNo = tinhConNo().compareTo(BigDecimal.ZERO) <= 0;
        datTrangThaiGiaoDich("Thanh toán thành công", GREEN,
                "Đã ghi nhận " + dinhDangTien(soTien) + " bằng " + phuongThuc
                        + " lúc " + gio + (hetNo ? ". Hóa đơn đã thanh toán đủ." : "."));
    }

    // ================== HỎI IN HÓA ĐƠN ==================
    private void hoiInHoaDon() {
        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn in hóa đơn không?",
                "In hóa đơn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (luaChon == JOptionPane.YES_OPTION) {
            inHoaDon();
        }
    }

    private void inHoaDon() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Hoa don " + maHoaDon);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pf, int pageIndex) {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2 = (Graphics2D) g;
                g2.translate(pf.getImageableX(), pf.getImageableY());
                g2.setColor(Color.BLACK);

                int w = (int) pf.getImageableWidth();
                int y = 30;

                Font tieuDe = new Font("Segoe UI", Font.BOLD, 18);
                Font thuong = new Font("Segoe UI", Font.PLAIN, 12);
                Font dam = new Font("Segoe UI", Font.BOLD, 12);

                g2.setFont(tieuDe);
                String ten = "AUTOGARA PRO";
                g2.drawString(ten, (w - g2.getFontMetrics().stringWidth(ten)) / 2, y);
                y += 26;

                String ten2 = "HÓA ĐƠN THANH TOÁN";
                g2.drawString(ten2, (w - g2.getFontMetrics().stringWidth(ten2)) / 2, y);
                y += 30;

                g2.setFont(thuong);
                String[] thongTin = {
                        "Mã hóa đơn: " + txtMaHoaDon.getText(),
                        "Ngày lập: " + txtNgayLap.getText(),
                        "Khách hàng: " + txtTenKhachHang.getText()
                                + " (" + txtMaKhachHang.getText() + ")",
                        "Phiếu sửa chữa: " + txtMaPhieuSuaChua.getText()
                };
                for (String dong : thongTin) {
                    g2.drawString(dong, 0, y);
                    y += 20;
                }

                y += 6;
                g2.drawLine(0, y, w, y);
                y += 22;

                g2.setFont(dam);
                g2.drawString("Lịch sử thanh toán", 0, y);
                y += 20;

                g2.setFont(thuong);
                for (String dong : lichSuIn) {
                    g2.drawString(dong, 0, y);
                    y += 20;
                }

                y += 6;
                g2.drawLine(0, y, w, y);
                y += 22;

                g2.setFont(dam);
                g2.drawString("Tổng tiền: " + dinhDangTien(tongTien), 0, y);
                y += 20;
                g2.drawString("Đã thanh toán: " + dinhDangTien(daThanhToan), 0, y);
                y += 20;
                g2.drawString("Còn phải trả: " + dinhDangTien(tinhConNo()), 0, y);
                y += 20;
                g2.drawString("Trạng thái: " + txtTrangThaiHoaDon.getText(), 0, y);
                y += 36;

                g2.setFont(thuong);
                String camOn = "Cảm ơn quý khách!";
                g2.drawString(camOn, (w - g2.getFontMetrics().stringWidth(camOn)) / 2, y);

                return PAGE_EXISTS;
            }
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                baoLoi("Không thể in hóa đơn: " + ex.getMessage());
            }
        }
    }

    // ================== CỬA SỔ QR CHUYỂN KHOẢN ==================
    /** Trả về true khi khách đã chuyển khoản thành công. */
    private boolean hienThiQR(BigDecimal soTien) {
        final boolean[] ketQua = { false };

        JDialog dlg = new JDialog(this, "Thanh toán chuyển khoản", true);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 32, 20, 32));

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("QUÉT MÃ QR ĐỂ THANH TOÁN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        QrPanel qr = new QrPanel(maHoaDon + "|" + soTien.toPlainString());
        qr.setBorder(new CompoundBorder(
                new LineBorder(LINE, 1, true), new EmptyBorder(10, 10, 10, 10)));
        qr.setAlignmentX(Component.CENTER_ALIGNMENT);
        qr.setMaximumSize(qr.getPreferredSize());

        JLabel nhanTien = new JLabel("Số tiền cần chuyển");
        nhanTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nhanTien.setForeground(MUTED);
        nhanTien.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tien = new JLabel(dinhDangTien(soTien));
        tien.setFont(new Font("Segoe UI", Font.BOLD, 26));
        tien.setForeground(BLUE_DARK);
        tien.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noiDung = new JLabel("Nội dung: Thanh toan " + maHoaDon);
        noiDung.setFont(FONT);
        noiDung.setForeground(TEXT);
        noiDung.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ghiChu = new JLabel("(Mã QR minh họa - sẽ thay bằng mã QR thật khi tích hợp ngân hàng)");
        ghiChu.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        ghiChu.setForeground(MUTED);
        ghiChu.setAlignmentX(Component.CENTER_ALIGNMENT);

        giua.add(title);
        giua.add(Box.createVerticalStrut(16));
        giua.add(qr);
        giua.add(Box.createVerticalStrut(16));
        giua.add(nhanTien);
        giua.add(Box.createVerticalStrut(4));
        giua.add(tien);
        giua.add(Box.createVerticalStrut(8));
        giua.add(noiDung);
        giua.add(Box.createVerticalStrut(6));
        giua.add(ghiChu);

        JButton btnHuy = taoButton("Hủy", false);
        JButton btnDaChuyen = taoButton("Khách đã chuyển khoản", true);

        btnHuy.addActionListener(e -> dlg.dispose());
        btnDaChuyen.addActionListener(e -> {
            ketQua[0] = true;
            dlg.dispose();
        });

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        nut.setOpaque(false);
        nut.add(btnHuy);
        nut.add(btnDaChuyen);

        root.add(giua, BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        return ketQua[0];
    }

    /**
     * Panel nội dung: luôn vừa chiều ngang cửa sổ, chiều dọc cuộn được bằng con
     * lăn.
     */
    private static class ViewCuon extends JPanel implements Scrollable {
        ViewCuon(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 40, 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            // Cửa sổ cao hơn nội dung thì giãn đầy khung, thấp hơn thì cho cuộn
            Container cha = getParent();
            return cha instanceof JViewport
                    && cha.getHeight() > getPreferredSize().height;
        }
    }

    /** Mã QR minh họa (chưa phải mã QR thật). */
    private static class QrPanel extends JPanel {
        private static final int N = 29;
        private final boolean[][] o = new boolean[N][N];

        QrPanel(String seed) {
            setPreferredSize(new Dimension(260, 260));
            setBackground(Color.WHITE);

            Random r = new Random(seed.hashCode());
            for (int y = 0; y < N; y++) {
                for (int x = 0; x < N; x++) {
                    o[y][x] = r.nextInt(100) < 48;
                }
            }
            veOGoc(0, 0);
            veOGoc(N - 7, 0);
            veOGoc(0, N - 7);
        }

        private void veOGoc(int ox, int oy) {
            for (int dy = -1; dy <= 7; dy++) {
                for (int dx = -1; dx <= 7; dx++) {
                    int x = ox + dx;
                    int y = oy + dy;
                    if (x < 0 || y < 0 || x >= N || y >= N) {
                        continue;
                    }
                    if (dx >= 0 && dx <= 6 && dy >= 0 && dy <= 6) {
                        boolean vien = dx == 0 || dx == 6 || dy == 0 || dy == 6;
                        boolean loi = dx >= 2 && dx <= 4 && dy >= 2 && dy <= 4;
                        o[y][x] = vien || loi;
                    } else {
                        o[y][x] = false;
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Insets in = getInsets();
            int w = getWidth() - in.left - in.right;
            int h = getHeight() - in.top - in.bottom;
            int s = Math.min(w, h) / N;
            int ox = in.left + (w - s * N) / 2;
            int oy = in.top + (h - s * N) / 2;

            g.setColor(Color.BLACK);
            for (int y = 0; y < N; y++) {
                for (int x = 0; x < N; x++) {
                    if (o[y][x]) {
                        g.fillRect(ox + x * s, oy + y * s, s, s);
                    }
                }
            }
        }
    }

    // ================== CẬP NHẬT SỐ TIỀN ==================
    private BigDecimal tinhConNo() {
        BigDecimal conNo = tongTien.subtract(daThanhToan);
        return conNo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : conNo;
    }

    private void capNhatTongQuan() {
        BigDecimal conNo = tinhConNo();

        lblTongTien.setText(dinhDangTien(tongTien));
        lblDaThanhToan.setText(dinhDangTien(daThanhToan));
        lblConPhaiTra.setText(dinhDangTien(conNo));

        boolean coHoaDon = maHoaDon != null
                && !maHoaDon.trim().isEmpty()
                && tongTien.compareTo(BigDecimal.ZERO) > 0;

        boolean hetNo = conNo.compareTo(BigDecimal.ZERO) <= 0;

        if (!coHoaDon) {
            txtTrangThaiHoaDon.setText("");
            txtTrangThaiHoaDon.setForeground(MUTED);
        } else if (hetNo) {
            txtTrangThaiHoaDon.setText("Đã thanh toán");
            txtTrangThaiHoaDon.setForeground(GREEN);
        } else if (daThanhToan.compareTo(BigDecimal.ZERO) > 0) {
            txtTrangThaiHoaDon.setText("Thanh toán một phần");
            txtTrangThaiHoaDon.setForeground(ORANGE);
        } else {
            txtTrangThaiHoaDon.setText("Chưa thanh toán");
            txtTrangThaiHoaDon.setForeground(ORANGE);
        }

        txtSoTienThanhToan.setEnabled(true);
        txtTienKhachDua.setEnabled(true);
        btnThanhToanDu.setEnabled(true);
        btnTienMat.setEnabled(true);
        btnChuyenKhoan.setEnabled(true);
        btnXacNhan.setEnabled(true);

        capNhatTienThua();
    }

    private void capNhatTienThua() {
        if (lblTienThua == null || txtTienKhachDua == null || txtSoTienThanhToan == null) {
            return;
        }

        if (!laTienMat()) {
            lblTienThua.setText("0 ₫");
            return;
        }

        BigDecimal tienKhachDua = docSoTien(txtTienKhachDua.getText());
        BigDecimal soTien = docSoTien(txtSoTienThanhToan.getText());

        if (tienKhachDua == null || soTien == null) {
            lblTienThua.setText("0 ₫");
            return;
        }

        BigDecimal tienThua = tienKhachDua.subtract(soTien);
        if (tienThua.compareTo(BigDecimal.ZERO) < 0) {
            tienThua = BigDecimal.ZERO;
        }
        lblTienThua.setText(dinhDangTien(tienThua));
    }

    private void datTrangThaiGiaoDich(String tieuDe, Color mau, String moTa) {
        lblTrangThaiGiaoDich.setText("● " + tieuDe);
        lblTrangThaiGiaoDich.setForeground(mau);
        lblMoTaGiaoDich.setText(moTa);
    }

    // ================== HÀM DỰNG THÀNH PHẦN ==================
    private JPanel taoCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(LINE, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JLabel taoTieuDeMuc(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel taoNhan(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(MUTED);
        return label;
    }

    private JLabel taoGiaTri(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(TEXT);
        return label;
    }

    private JLabel taoGiaTriNoiBat(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 25));
        label.setForeground(BLUE_DARK);
        return label;
    }

    /** Một ô thông tin: nhãn phía trên, ô chữ phía dưới (cùng căn lề trái). */
    private JPanel taoO(String nhan, JTextField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lbl = taoNhan(nhan);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        return panel;
    }

    private JPanel taoDongSoTien(String nhan, JLabel giaTri) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(taoNhan(nhan), BorderLayout.WEST);
        panel.add(giaTri, BorderLayout.EAST);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private JTextField taoTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT);
        field.setPreferredSize(new Dimension(100, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setBorder(new CompoundBorder(
                new LineBorder(LINE, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    /** Ô chỉ đọc: không cho nhập, dữ liệu sẽ tự cập nhật từ CSDL. */
    private JTextField taoOChiDoc() {
        JTextField field = taoTextField();
        field.setEditable(false);
        field.setFont(FONT_BOLD);
        field.setForeground(TEXT);
        field.setBackground(FIELD_BG);
        field.setCursor(Cursor.getDefaultCursor());
        return field;
    }

    /** Nền nút tự vẽ (giữ đúng màu trên mọi Look and Feel). */
    private static class NenNutUI extends BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(b.isEnabled() ? b.getBackground() : new Color(0xE2E8F0));
            g2.fillRect(0, 0, b.getWidth(), b.getHeight());
            g2.dispose();
            super.paint(g, c);
        }
    }

    private JButton taoButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setUI(new NenNutUI());

        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(false);

        if (primary) {
            button.setBackground(BRAND);
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(11, 18, 11, 18));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(TEXT);
            button.setBorder(new CompoundBorder(
                    new LineBorder(LINE, 1, true),
                    new EmptyBorder(10, 17, 10, 17)));
        }
        return button;
    }

    private JToggleButton taoToggle(String text) {
        JToggleButton button = new JToggleButton(text);
        button.setUI(new NenNutUI());

        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(false);
        capNhatMauToggle(button);
        return button;
    }

    private void capNhatMauToggle(JToggleButton button) {
        if (button == null) {
            return;
        }
        if (button.isSelected()) {
            button.setBackground(BRAND);
            button.setForeground(Color.WHITE);
            button.setBorder(new CompoundBorder(
                    new LineBorder(BRAND, 1, true),
                    new EmptyBorder(12, 10, 12, 10)));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(TEXT);
            button.setBorder(new CompoundBorder(
                    new LineBorder(LINE, 1, true),
                    new EmptyBorder(12, 10, 12, 10)));
        }
    }

    // ================== XỬ LÝ SỐ TIỀN ==================
    private BigDecimal docSoTien(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            String value = text.trim()
                    .replace("₫", "")
                    .replace("VND", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", "");

            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String dinhDangTien(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("vi-VN"));
        symbols.setGroupingSeparator('.');

        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        format.setGroupingUsed(true);

        return format.format(value) + " ₫";
    }

    private void baoLoi(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
    }

    // ================== CHẠY THỬ GIAO DIỆN ==================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrmThanhToan frame = new FrmThanhToan("HD001");
            frame.setVisible(true);
        });
    }
}
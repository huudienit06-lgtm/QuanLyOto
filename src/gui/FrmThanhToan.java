package gui;

import connectDB.ConnectDB;
import dao.ThanhToanDAO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.print.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FrmThanhToan extends JFrame {
    private static final String TEN_GARA = "AUTOGARA PRO";
    private static final String DIA_CHI_GARA = "Địa chỉ: 12 Nguyễn Văn Bảo, P.4, Q. Gò Vấp, TP. Hồ Chí Minh";
    private static final String LIEN_HE_GARA = "Điện thoại: 028 1234 5678  |  MST: 0312345678";
    // Đúng theo các thuộc tính trong ThanhToanDAO.ThongTinHoaDon.
    private static final String COT_MA_NV = "maNV";
    private static final String BANG_PHIEU = "PhieuSuaChua";
    private static final String COT_MA_PHIEU = "maPhieuSuaChua";
    private static final String COT_MA_XE = "maXe";
    private static final Color BRAND = new Color(0x2150E8);
    private static final Color BLUE_DARK = new Color(0x1D4ED8);
    private static final Color TEXT = new Color(0x17233B);
    private static final Color MUTED = new Color(0x64748B);
    private static final Color LINE = new Color(0xDBE2EE);
    private static final Color LINE_SOFT = new Color(0xEEF2F7);
    private static final Color SOFT = new Color(0xF4F7FC);
    private static final Color GREEN = new Color(0x15803D);
    private static final Color GREEN_SOFT = new Color(0xF0FDF4);
    private static final Color ORANGE = new Color(0xB45309);
    private static final Color ORANGE_SOFT = new Color(0xFFF7ED);
    private static final Color RED = new Color(0xDC2626);
    private static final Color BLUE_SOFT = new Color(0xEFF6FF);
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final String CARD_TIEN_MAT = "TIEN_MAT";
    private static final String CARD_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    private static final String LOAI_DV = "DV";

    private String maHoaDon;
    private String maNhanVien = "";
    private String maXe = "";
    // HoaDon.soTien là tổng thanh toán sau VAT và sau giảm giá.
    private BigDecimal tongTien = BigDecimal.ZERO;
    private BigDecimal daThanhToan = BigDecimal.ZERO;
    private BigDecimal tienDV = BigDecimal.ZERO;
    private BigDecimal tienPT = BigDecimal.ZERO;
    private BigDecimal tienThue = BigDecimal.ZERO;
    private BigDecimal tienGiamGia = BigDecimal.ZERO;
    private final List<ChiTiet> chiTietHD = new ArrayList<>();

    private JTextField txtMaHoaDon, txtNgayLap, txtMaKhachHang, txtTenKhachHang;
    private JTextField txtMaPhieuSuaChua, txtMaNhanVien, txtTrangThaiHoaDon;
    private JLabel lblPill, lblTienDV, lblTienPT, lblTongHoaDon, lblThue, lblGiamGia;
    private JLabel lblTongThanhToan, lblDaThanhToan, lblConPhaiTra, lblNhanTienThua, lblTienThua;
    private JLabel lblHuongDan, lblThongBao;
    private JProgressBar progDaTra;

    private JToggleButton btnTienMat, btnChuyenKhoan;
    private JPanel pnlKhuVucPhuongThuc;
    private CardLayout cardPhuongThuc;
    private JTextField txtSoTienThanhToan, txtTienKhachDua;
    private JButton btnThanhToanDu, btnMotNua, btnVuaDu;
    private JButton[] btnMenhGia;
    private final List<JButton> cacNutNhanh = new ArrayList<>();
    private DefaultListModel<String> modelLichSu;
    private JButton btnXacNhan, btnDong;
    private final ThanhToanDAO thanhToanDAO = new ThanhToanDAO();

    public FrmThanhToan(String maHoaDon) {
        this.maHoaDon = maHoaDon == null ? "" : maHoaDon.trim();
        setTitle("THANH TOÁN HÓA ĐƠN");
        Rectangle man = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1180, man.width), Math.min(820, man.height));
        setMinimumSize(new Dimension(Math.min(1000, man.width), Math.min(560, man.height)));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        khoiTaoGiaoDien();
        ganSuKien();
        taiHoaDonTuCSDL(this.maHoaDon);
    }

    private void khoiTaoGiaoDien() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(SOFT);
        root.setBorder(new EmptyBorder(14, 20, 14, 20));
        setContentPane(root);
        root.add(taoTieuDe(), BorderLayout.NORTH);

        JPanel haiCot = new JPanel(new GridBagLayout());
        haiCot.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.gridx = 0;
        g.weightx = .40;
        g.insets = new Insets(0, 0, 0, 16);
        haiCot.add(taoCotTrai(), g);
        g.gridx = 1;
        g.weightx = .60;
        g.insets = new Insets(0, 0, 0, 0);
        haiCot.add(taoCotPhai(), g);

        ViewCuon noiDung = new ViewCuon(new BorderLayout());
        noiDung.setOpaque(false);
        noiDung.add(haiCot, BorderLayout.CENTER);
        JScrollPane cuon = new JScrollPane(noiDung,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cuon.setBorder(null);
        cuon.setOpaque(false);
        cuon.getViewport().setOpaque(false);
        cuon.getVerticalScrollBar().setUnitIncrement(18);
        root.add(cuon, BorderLayout.CENTER);
        root.add(taoKhuVucNut(), BorderLayout.SOUTH);
    }

    private JPanel taoTieuDe() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("THANH TOÁN HÓA ĐƠN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT);
        p.add(title, BorderLayout.CENTER);

        // Giữ chỗ bên phải để tiêu đề luôn nằm chính giữa.
        lblPill = new JLabel(" ");
        lblPill.setOpaque(true);
        lblPill.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPill.setBorder(new EmptyBorder(7, 16, 7, 16));
        lblPill.setVisible(false);

        JPanel benPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        benPhai.setOpaque(false);
        benPhai.setPreferredSize(new Dimension(160, 40));
        benPhai.add(lblPill);
        p.add(benPhai, BorderLayout.EAST);

        // Tạo khoảng trống tương ứng bên trái để tiêu đề cân giữa.
        JPanel benTrai = new JPanel();
        benTrai.setOpaque(false);
        benTrai.setPreferredSize(new Dimension(160, 40));
        p.add(benTrai, BorderLayout.WEST);
        return p;
    }

    private void datPill(String text, Color fg, Color bg) {
        lblPill.setText(text);
        lblPill.setForeground(fg);
        lblPill.setBackground(bg);
        lblPill.setVisible(text != null && !text.trim().isEmpty());
    }

    private JPanel taoCotTrai() {
        JPanel cot = new JPanel(new BorderLayout(0, 14));
        cot.setOpaque(false);

        JPanel thongTin = taoCard();
        thongTin.setLayout(new BorderLayout(0, 10));
        thongTin.add(taoTieuDeMuc("1. Thông tin hóa đơn"), BorderLayout.NORTH);
        txtMaHoaDon = taoOChiDoc();
        txtNgayLap = taoOChiDoc();
        txtMaKhachHang = taoOChiDoc();
        txtTenKhachHang = taoOChiDoc();
        txtMaPhieuSuaChua = taoOChiDoc();
        txtMaNhanVien = taoOChiDoc();
        txtTrangThaiHoaDon = taoOChiDoc();

        JPanel luoi = new JPanel(new GridBagLayout());
        luoi.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 12, 12);

        c.gridy = 0;
        c.gridx = 0;
        c.gridwidth = 1;
        luoi.add(taoO("Mã hóa đơn", txtMaHoaDon), c);
        c.gridx = 1;
        c.insets = new Insets(0, 0, 12, 0);
        luoi.add(taoO("Ngày lập", txtNgayLap), c);

        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 12, 12);
        luoi.add(taoO("Mã khách hàng", txtMaKhachHang), c);
        c.gridx = 1;
        c.insets = new Insets(0, 0, 12, 0);
        luoi.add(taoO("Khách hàng", txtTenKhachHang), c);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 12, 12);
        luoi.add(taoO("Mã nhân viên", txtMaNhanVien), c);
        c.gridx = 1;
        c.insets = new Insets(0, 0, 12, 0);
        luoi.add(taoO("Phiếu sửa chữa", txtMaPhieuSuaChua), c);

        c.gridy = 4;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(0, 0, 0, 0);
        luoi.add(taoO("Trạng thái hóa đơn", txtTrangThaiHoaDon), c);

        thongTin.add(luoi, BorderLayout.CENTER);

        JPanel lichSu = taoCard();
        lichSu.setLayout(new BorderLayout(0, 10));
        lichSu.add(taoTieuDeMuc("2. Lịch sử thanh toán"), BorderLayout.NORTH);
        modelLichSu = new DefaultListModel<>();
        JList<String> list = new JList<>(modelLichSu);
        list.setFont(FONT);
        list.setFixedCellHeight(52);
        list.setBackground(Color.WHITE);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                    boolean selected, boolean focus) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(l, value, index, selected, focus);
                lb.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINE_SOFT),
                        new EmptyBorder(6, 8, 6, 8)));
                lb.setFont(FONT);
                return lb;
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(new LineBorder(LINE));
        scroll.setPreferredSize(new Dimension(100, 90));
        lichSu.add(scroll, BorderLayout.CENTER);
        cot.add(thongTin, BorderLayout.NORTH);
        cot.add(lichSu, BorderLayout.CENTER);
        return cot;
    }

    private JPanel taoO(String nhan, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.add(taoNhan(nhan), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        p.setBorder(new MatteBorder(0, 0, 1, 0, LINE_SOFT));
        return p;
    }

    private JPanel taoCotPhai() {
        JPanel tongQuan = taoCard();
        tongQuan.setLayout(new BoxLayout(tongQuan, BoxLayout.Y_AXIS));
        tongQuan.add(taoTieuDeMuc("3. Tổng quan thanh toán"));
        tongQuan.add(Box.createVerticalStrut(10));

        lblTienDV = taoGiaTri("0 ₫");
        lblTienPT = taoGiaTri("0 ₫");
        lblTongHoaDon = taoGiaTri("0 ₫");
        lblTongHoaDon.setFont(FONT_BOLD);
        lblThue = taoGiaTri("0 ₫");
        lblGiamGia = taoGiaTri("0 ₫");
        lblGiamGia.setForeground(RED);
        lblTongThanhToan = taoGiaTri("0 ₫");
        lblTongThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongThanhToan.setForeground(BRAND);
        lblDaThanhToan = taoGiaTri("0 ₫");
        lblDaThanhToan.setForeground(GREEN);
        lblConPhaiTra = taoGiaTriNoiBat("0 ₫");

        tongQuan.add(taoDongSoTien("Tiền dịch vụ", lblTienDV));
        tongQuan.add(Box.createVerticalStrut(6));
        tongQuan.add(taoDongSoTien("Tiền phụ tùng", lblTienPT));
        tongQuan.add(Box.createVerticalStrut(8));
        tongQuan.add(taoDuongKe());
        tongQuan.add(Box.createVerticalStrut(8));
        tongQuan.add(taoDongSoTien("Tổng hóa đơn (đã gồm VAT)", lblTongHoaDon));
        tongQuan.add(Box.createVerticalStrut(6));
        tongQuan.add(taoDongSoTien("Tiền thuế (VAT)", lblThue));
        tongQuan.add(Box.createVerticalStrut(6));
        tongQuan.add(taoDongSoTien("Tiền giảm", lblGiamGia));
        tongQuan.add(Box.createVerticalStrut(8));
        tongQuan.add(taoDuongKe());
        tongQuan.add(Box.createVerticalStrut(8));
        tongQuan.add(taoDongSoTien("Tổng thanh toán", lblTongThanhToan));
        tongQuan.add(Box.createVerticalStrut(6));
        tongQuan.add(taoDongSoTien("Đã thanh toán", lblDaThanhToan));
        tongQuan.add(Box.createVerticalStrut(8));

        progDaTra = new JProgressBar(0, 100);
        progDaTra.setBorderPainted(false);
        progDaTra.setForeground(GREEN);
        progDaTra.setBackground(LINE);
        progDaTra.setPreferredSize(new Dimension(100, 6));
        progDaTra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progDaTra.setAlignmentX(Component.LEFT_ALIGNMENT);
        tongQuan.add(progDaTra);
        tongQuan.add(Box.createVerticalStrut(10));

        JPanel conNo = new JPanel(new BorderLayout(8, 0));
        conNo.setBackground(BLUE_SOFT);
        conNo.setBorder(new EmptyBorder(10, 14, 10, 14));
        conNo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel moTa = taoNhan("CÒN PHẢI THANH TOÁN");
        moTa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        moTa.setForeground(BLUE_DARK);
        conNo.add(moTa, BorderLayout.WEST);
        conNo.add(lblConPhaiTra, BorderLayout.EAST);
        conNo.setMaximumSize(new Dimension(Integer.MAX_VALUE, conNo.getPreferredSize().height));
        tongQuan.add(conNo);

        JPanel thanhToan = taoCard();
        thanhToan.setLayout(new BoxLayout(thanhToan, BoxLayout.Y_AXIS));
        thanhToan.add(taoTieuDeMuc("4. Thanh toán"));
        thanhToan.add(Box.createVerticalStrut(10));
        thanhToan.add(taoNhanTruong("Phương thức thanh toán"));
        thanhToan.add(Box.createVerticalStrut(6));

        btnTienMat = taoToggle("Tiền mặt");
        btnChuyenKhoan = taoToggle("Chuyển khoản / QR");
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(btnTienMat);
        nhom.add(btnChuyenKhoan);
        btnTienMat.setSelected(true);
        JPanel hangToggle = new JPanel(new GridLayout(1, 2, 10, 0));
        hangToggle.setOpaque(false);
        hangToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangToggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        hangToggle.add(btnTienMat);
        hangToggle.add(btnChuyenKhoan);
        thanhToan.add(hangToggle);

        lblHuongDan = new JLabel("Tiền mặt: nhận tiền trực tiếp tại quầy.");
        lblHuongDan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHuongDan.setForeground(MUTED);
        lblHuongDan.setAlignmentX(Component.LEFT_ALIGNMENT);
        thanhToan.add(Box.createVerticalStrut(5));
        thanhToan.add(lblHuongDan);
        thanhToan.add(Box.createVerticalStrut(12));
        thanhToan.add(taoNhanTruong("Số tiền thanh toán lần này"));
        thanhToan.add(Box.createVerticalStrut(5));
        txtSoTienThanhToan = taoTextField();
        thanhToan.add(txtSoTienThanhToan);

        btnThanhToanDu = taoChip("Đủ số còn nợ");
        btnMotNua = taoChip("50% số còn nợ");
        thanhToan.add(Box.createVerticalStrut(8));
        thanhToan.add(taoHangChip(btnThanhToanDu, btnMotNua));
        thanhToan.add(Box.createVerticalStrut(12));

        cardPhuongThuc = new CardLayout();
        pnlKhuVucPhuongThuc = new JPanel(cardPhuongThuc);
        pnlKhuVucPhuongThuc.setOpaque(false);
        pnlKhuVucPhuongThuc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlTienMat = new JPanel();
        pnlTienMat.setOpaque(false);
        pnlTienMat.setLayout(new BoxLayout(pnlTienMat, BoxLayout.Y_AXIS));
        txtTienKhachDua = taoTextField();
        btnVuaDu = taoChip("Vừa đủ");
        String[] menhGia = { "50000", "100000", "200000", "500000" };
        btnMenhGia = new JButton[menhGia.length];
        JPanel hangChipTien = taoHangChip(btnVuaDu);
        for (int i = 0; i < menhGia.length; i++) {
            btnMenhGia[i] = taoChip(dinhDangTien(new BigDecimal(menhGia[i])).replace(" ₫", ""));
            btnMenhGia[i].setActionCommand(menhGia[i]);
            hangChipTien.add(btnMenhGia[i]);
        }

        lblNhanTienThua = taoNhan("Tiền thừa trả khách");
        lblNhanTienThua.setFont(FONT_BOLD);
        lblNhanTienThua.setForeground(TEXT);
        lblTienThua = taoGiaTri("0 ₫");
        lblTienThua.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTienThua.setForeground(GREEN);
        JPanel hangTienThua = new JPanel(new BorderLayout(8, 0));
        hangTienThua.setBackground(GREEN_SOFT);
        hangTienThua.setBorder(new EmptyBorder(9, 14, 9, 14));
        hangTienThua.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangTienThua.add(lblNhanTienThua, BorderLayout.WEST);
        hangTienThua.add(lblTienThua, BorderLayout.EAST);
        hangTienThua.setMaximumSize(new Dimension(Integer.MAX_VALUE, hangTienThua.getPreferredSize().height));

        pnlTienMat.add(taoNhanTruong("Tiền khách đưa"));
        pnlTienMat.add(Box.createVerticalStrut(5));
        pnlTienMat.add(txtTienKhachDua);
        pnlTienMat.add(Box.createVerticalStrut(6));
        pnlTienMat.add(hangChipTien);
        pnlTienMat.add(Box.createVerticalStrut(10));
        pnlTienMat.add(hangTienThua);

        JPanel pnlChuyenKhoan = new JPanel(new BorderLayout());
        pnlChuyenKhoan.setOpaque(false);
        pnlKhuVucPhuongThuc.add(pnlTienMat, CARD_TIEN_MAT);
        pnlKhuVucPhuongThuc.add(pnlChuyenKhoan, CARD_CHUYEN_KHOAN);
        thanhToan.add(pnlKhuVucPhuongThuc);

        JPanel cot = new JPanel(new GridBagLayout());
        cot.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 14, 0);
        cot.add(tongQuan, c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        cot.add(thanhToan, c);
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        c.gridy = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        cot.add(filler, c);
        return cot;
    }

    private JPanel taoKhuVucNut() {
        lblThongBao = new JLabel(" ");
        lblThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblThongBao.setForeground(MUTED);
        btnDong = taoButton("Đóng", false);
        btnXacNhan = taoButton("Xác nhận hóa đơn", true);
        btnDong.setPreferredSize(new Dimension(110, 46));
        btnXacNhan.setPreferredSize(new Dimension(230, 46));
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.setOpaque(false);
        nut.add(btnDong);
        nut.add(btnXacNhan);
        JPanel footer = taoCard();
        footer.setLayout(new BorderLayout(16, 0));
        footer.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true),
                new EmptyBorder(10, 16, 10, 12)));
        footer.add(lblThongBao, BorderLayout.CENTER);
        footer.add(nut, BorderLayout.EAST);
        return footer;
    }

    private void datThongBao(String text, Color color) {
        lblThongBao.setText(text == null || text.isEmpty() ? " " : text);
        lblThongBao.setForeground(color);
    }

    private void ganSuKien() {
        btnDong.addActionListener(e -> dispose());
        btnXacNhan.addActionListener(e -> xacNhanThanhToan());
        btnThanhToanDu.addActionListener(e -> {
            BigDecimal conNo = tinhConNo();
            if (conNo.signum() > 0) {
                txtSoTienThanhToan.setText(conNo.stripTrailingZeros().toPlainString());
                txtSoTienThanhToan.requestFocusInWindow();
                txtSoTienThanhToan.selectAll();
            } else {
                txtSoTienThanhToan.setText("0");
                baoLoi("Hóa đơn đã được thanh toán đủ.");
            }
            capNhatTienThua();
        });
        btnMotNua.addActionListener(e -> {
            BigDecimal conNo = tinhConNo();
            if (conNo.signum() > 0) {
                txtSoTienThanhToan.setText(
                        conNo.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP).toPlainString());
                txtSoTienThanhToan.requestFocusInWindow();
                txtSoTienThanhToan.selectAll();
            }
        });
        btnVuaDu.addActionListener(e -> {
            BigDecimal soTien = docSoTien(txtSoTienThanhToan.getText());
            if (soTien == null || soTien.signum() <= 0)
                soTien = tinhConNo();
            txtTienKhachDua.setText(soTien.stripTrailingZeros().toPlainString());
        });
        for (JButton b : btnMenhGia) {
            b.addActionListener(e -> txtTienKhachDua.setText(b.getActionCommand()));
        }
        java.awt.event.ItemListener doiPhuongThuc = e -> capNhatPhuongThuc();
        btnTienMat.addItemListener(doiPhuongThuc);
        btnChuyenKhoan.addItemListener(doiPhuongThuc);
        khiThayDoi(txtTienKhachDua, this::capNhatTienThua);
        khiThayDoi(txtSoTienThanhToan, this::capNhatTienThua);
        txtSoTienThanhToan.addActionListener(e -> {
            if (btnXacNhan.isEnabled())
                xacNhanThanhToan();
        });
        capNhatPhuongThuc();
    }

    private void khiThayDoi(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            public void changedUpdate(DocumentEvent e) {
                action.run();
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
            lblHuongDan.setText("Chuyển khoản / QR: xác nhận sau khi khách hoàn tất chuyển khoản.");
        }
        capNhatTienThua();
    }

    private void xoaDuLieu() {
        tongTien = daThanhToan = tienDV = tienPT = tienThue = tienGiamGia = BigDecimal.ZERO;
        chiTietHD.clear();
        maNhanVien = maXe = "";
        txtMaHoaDon.setText("");
        txtNgayLap.setText("");
        txtMaKhachHang.setText("");
        txtTenKhachHang.setText("");
        txtMaPhieuSuaChua.setText("");
        txtMaNhanVien.setText("");
        txtTrangThaiHoaDon.setText("");
        modelLichSu.clear();
    }

    private void taiHoaDonTuCSDL(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            xoaDuLieu();
            capNhatTongQuan();
            datThongBao("Vui lòng mở thanh toán từ một hóa đơn đã lưu.", ORANGE);
            btnXacNhan.setEnabled(false);
            return;
        }
        try {
            ThanhToanDAO.ThongTinHoaDon hd = thanhToanDAO.layHoaDon(maHD.trim());
            if (hd == null) {
                maHoaDon = "";
                xoaDuLieu();
                capNhatTongQuan();
                btnXacNhan.setEnabled(false);
                datThongBao("Mã hóa đơn " + maHD + " không tồn tại trong cơ sở dữ liệu.", ORANGE);
                return;
            }

            maHoaDon = hd.maHoaDon;
            tongTien = bd(hd.tongTien);
            tienThue = bd(hd.thueVAT);
            tienGiamGia = bd(hd.tienGiamGia);

            txtMaHoaDon.setText(nvl(hd.maHoaDon));
            txtMaKhachHang.setText(nvl(hd.maKhachHang));
            txtTenKhachHang.setText(nvl(hd.tenKhachHang));
            txtMaPhieuSuaChua.setText(nvl(hd.maPhieuSuaChua));
            txtNgayLap.setText(hd.ngayLap == null ? ""
                    : hd.ngayLap.toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            chiTietHD.clear();
            chiTietHD.addAll(layChiTiet());
            tienDV = BigDecimal.ZERO;
            tienPT = BigDecimal.ZERO;
            for (ChiTiet ct : chiTietHD) {
                if (LOAI_DV.equals(ct.loai))
                    tienDV = tienDV.add(ct.thanhTien);
                else
                    tienPT = tienPT.add(ct.thanhTien);
            }

            layMaNhanVienMaXe();
            txtMaNhanVien.setText(maNhanVien);

            List<ThanhToanDAO.GiaoDich> ds = thanhToanDAO.layLichSu(maHoaDon);
            modelLichSu.clear();
            daThanhToan = BigDecimal.ZERO;
            for (ThanhToanDAO.GiaoDich gd : ds) {
                if ("Thành công".equalsIgnoreCase(gd.trangThaiGD) && gd.soTienThanhToan != null) {
                    daThanhToan = daThanhToan.add(gd.soTienThanhToan);
                }
                String gio = gd.thoiGianGD == null ? ""
                        : gd.thoiGianGD.toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
                String tien = gd.soTienThanhToan == null ? "Chưa có số tiền" : dinhDangTien(gd.soTienThanhToan);
                modelLichSu.addElement("<html><b>" + tien + "</b> - " + nvl(gd.phuongThucThanhToan)
                        + "<br><span style='color:#64748B'>" + gio + " | " + nvl(gd.trangThaiGD) + "</span></html>");
            }
            capNhatTongQuan();
            datThongBao(" ", MUTED);
        } catch (SQLException ex) {
            ex.printStackTrace();
            datThongBao("Không thể đọc dữ liệu hóa đơn từ SQL Server.", RED);
            JOptionPane.showMessageDialog(this,
                    "Không thể tải thông tin hóa đơn.\nChi tiết: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
            btnXacNhan.setEnabled(false);
        }
    }

    private static class ChiTiet {
        String loai, tenHang;
        int soLuong;
        BigDecimal donGia = BigDecimal.ZERO;
        BigDecimal thanhTien = BigDecimal.ZERO;
    }

    private List<ChiTiet> layChiTiet() throws SQLException {
        List<ChiTiet> ds = new ArrayList<>();
        String sql = """
                SELECT CASE WHEN ct.maDV IS NOT NULL THEN 'DV' ELSE 'PT' END AS loai,
                       COALESCE(dv.tenDV, pt.tenPhuTung) AS tenHang,
                       ct.soLuong, ct.donGia,
                       CAST(ct.soLuong AS DECIMAL(18, 2)) * ct.donGia AS thanhTien
                FROM ChiTietHoaDon ct
                LEFT JOIN DichVu dv ON ct.maDV = dv.maDV
                LEFT JOIN PhuTung pt ON ct.maPhuTung = pt.maPhuTung
                WHERE ct.maHoaDon = ?
                ORDER BY CASE WHEN ct.maDV IS NOT NULL THEN 0 ELSE 1 END,
                         COALESCE(dv.tenDV, pt.tenPhuTung)
                """;
        ConnectDB.getInstance().connect();
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTiet ct = new ChiTiet();
                    ct.loai = rs.getString("loai");
                    ct.tenHang = nvl(rs.getString("tenHang"));
                    ct.soLuong = rs.getInt("soLuong");
                    ct.donGia = bd(rs.getBigDecimal("donGia"));
                    ct.thanhTien = bd(rs.getBigDecimal("thanhTien"));
                    ds.add(ct);
                }
            }
        }
        return ds;
    }

    private void layMaNhanVienMaXe() {
        maNhanVien = "";
        maXe = "";
        try {
            ConnectDB.getInstance().connect();
            Connection conn = ConnectDB.getConnection();
            String sqlNV = "SELECT " + COT_MA_NV + " FROM HoaDon WHERE maHoaDon = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        maNhanVien = nvl(rs.getString(1)).trim();
                }
            }
            String maPhieu = txtMaPhieuSuaChua.getText().trim();
            if (!maPhieu.isEmpty()) {
                String sqlXe = "SELECT " + COT_MA_XE + " FROM " + BANG_PHIEU
                        + " WHERE " + COT_MA_PHIEU + " = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlXe)) {
                    ps.setString(1, maPhieu);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            maXe = nvl(rs.getString(1)).trim();
                    }
                }
            }
        } catch (Exception ex) {
            // Nếu thiếu dữ liệu phụ, giữ mã nhân viên/mã xe trống.
        }
    }

    private void xacNhanThanhToan() {
        BigDecimal soTien = docSoTien(txtSoTienThanhToan.getText());
        if (maHoaDon == null || maHoaDon.isEmpty() || tongTien.signum() <= 0) {
            baoLoi("Chưa có thông tin hóa đơn để thanh toán.");
            return;
        }
        if (soTien == null || soTien.signum() <= 0) {
            baoLoi("Vui lòng nhập số tiền khách muốn thanh toán.");
            txtSoTienThanhToan.requestFocus();
            return;
        }
        BigDecimal conNo = tinhConNo();
        if (soTien.compareTo(conNo) > 0) {
            int luaChon = JOptionPane.showConfirmDialog(this,
                    "Số tiền nhập (" + dinhDangTien(soTien) + ") lớn hơn số còn nợ ("
                            + dinhDangTien(conNo) + ").\nTự động đổi về số còn nợ?",
                    "Điều chỉnh số tiền", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (luaChon == JOptionPane.YES_OPTION) {
                txtSoTienThanhToan.setText(conNo.stripTrailingZeros().toPlainString());
                txtSoTienThanhToan.requestFocusInWindow();
                txtSoTienThanhToan.selectAll();
            }
            return;
        }

        DuLieuHoaDon d = new DuLieuHoaDon();
        d.maHoaDon = maHoaDon;
        d.ngayLap = txtNgayLap.getText();
        d.tenKhach = txtTenKhachHang.getText();
        d.maKhach = txtMaKhachHang.getText();
        d.maPhieu = txtMaPhieuSuaChua.getText();
        d.maXe = maXe;
        d.maNhanVien = maNhanVien;
        d.chiTiet = new ArrayList<>(chiTietHD);
        d.tienDV = tienDV;
        d.tienPT = tienPT;
        d.thue = tienThue;
        d.giamGia = tienGiamGia;
        d.tongHoaDon = tongTien.add(tienGiamGia);
        d.tongTien = tongTien;
        d.daTraTruoc = daThanhToan;
        d.lanNay = soTien;
        d.conLai = tongTien.subtract(daThanhToan).subtract(soTien).max(BigDecimal.ZERO);
        d.ngayIn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        if (laTienMat()) {
            BigDecimal khachDua = docSoTien(txtTienKhachDua.getText());
            if (khachDua == null || khachDua.compareTo(soTien) < 0) {
                baoLoi("Tiền khách đưa phải lớn hơn hoặc bằng số tiền thanh toán.");
                txtTienKhachDua.requestFocus();
                return;
            }
            d.phuongThuc = "Tiền mặt";
            d.khachDua = khachDua;
            d.tienThua = khachDua.subtract(soTien);
        } else {
            if (!hienThiQR(soTien)) {
                datThongBao("Đã hủy giao dịch chuyển khoản. Chưa ghi nhận thanh toán.", ORANGE);
                return;
            }
            d.phuongThuc = "Chuyển khoản / QR";
        }

        if (!ghiNhanThanhToan(soTien, d.phuongThuc))
            return;
        JOptionPane.showMessageDialog(this,
                "Thanh toán thành công!\nSố tiền: " + dinhDangTien(soTien)
                        + "\nPhương thức: " + d.phuongThuc,
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        int chon = JOptionPane.showOptionDialog(this, "Bạn có muốn in hóa đơn không?",
                "In hóa đơn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new Object[] { "In hóa đơn", "Không" }, "In hóa đơn");
        if (chon == 0)
            hienThiHoaDon(d);
    }

    private boolean ghiNhanThanhToan(BigDecimal soTien, String phuongThuc) {
        try {
            thanhToanDAO.ghiNhanThanhToan(maHoaDon, soTien, phuongThuc);
            txtSoTienThanhToan.setText("");
            txtTienKhachDua.setText("");
            taiHoaDonTuCSDL(maHoaDon);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Thanh toán thất bại: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static class DuLieuHoaDon {
        String maHoaDon, ngayLap, tenKhach, maKhach, maPhieu, phuongThuc, ngayIn, maXe, maNhanVien;
        List<ChiTiet> chiTiet;
        BigDecimal tienDV, tienPT, thue, giamGia, tongHoaDon, tongTien, daTraTruoc, lanNay, conLai;
        BigDecimal khachDua, tienThua;
    }

    private void hienThiHoaDon(DuLieuHoaDon d) {
        JDialog dlg = new JDialog(this, "Hóa đơn thanh toán - " + d.maHoaDon, true);
        JPanel giay = taoHoaDonIn(d);
        JPanel vien = new JPanel(new BorderLayout());
        vien.setBorder(new LineBorder(LINE));
        vien.add(giay, BorderLayout.CENTER);
        JPanel nen = new JPanel(new GridBagLayout());
        nen.setBackground(new Color(0xE5E9F0));
        nen.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.weighty = 1;
        nen.add(vien, c);
        JScrollPane cuon = new JScrollPane(nen);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(20);

        JButton btnQuay = taoButton("Quay lại", false);
        JButton btnXacNhanIn = taoButton("Xác nhận in", true);
        btnQuay.addActionListener(e -> dlg.dispose());
        btnXacNhanIn.addActionListener(e -> {
            inKhung(giay, d.maHoaDon);
            dlg.dispose();
        });
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.setOpaque(false);
        nut.add(btnQuay);
        nut.add(btnXacNhanIn);
        JPanel duoi = new JPanel(new BorderLayout());
        duoi.setBackground(Color.WHITE);
        duoi.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, LINE),
                new EmptyBorder(10, 16, 10, 12)));
        duoi.add(nut, BorderLayout.EAST);
        JPanel root = new JPanel(new BorderLayout());
        root.add(cuon, BorderLayout.CENTER);
        root.add(duoi, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        Rectangle man = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        dlg.setSize(Math.min(960, man.width), Math.min(900, man.height));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel taoHoaDonIn(DuLieuHoaDon d) {
        final int W = 760;
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(30, 36, 30, 36));

        JLabel ten = new JLabel(TEN_GARA);
        ten.setFont(new Font("Segoe UI", Font.BOLD, 24));
        ten.setForeground(BRAND);
        JLabel dc = new JLabel(DIA_CHI_GARA);
        dc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dc.setForeground(MUTED);
        JLabel lh = new JLabel(LIEN_HE_GARA);
        lh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lh.setForeground(MUTED);
        JPanel gara = new JPanel();
        gara.setOpaque(false);
        gara.setLayout(new BoxLayout(gara, BoxLayout.Y_AXIS));
        gara.add(ten);
        gara.add(Box.createVerticalStrut(3));
        gara.add(dc);
        gara.add(lh);

        JLabel so = new JLabel("Số: " + d.maHoaDon, SwingConstants.RIGHT);
        so.setFont(FONT_BOLD);
        JLabel in = new JLabel("Ngày in: " + d.ngayIn, SwingConstants.RIGHT);
        in.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        in.setForeground(MUTED);
        JPanel soBox = new JPanel();
        soBox.setOpaque(false);
        soBox.setLayout(new BoxLayout(soBox, BoxLayout.Y_AXIS));
        so.setAlignmentX(Component.RIGHT_ALIGNMENT);
        in.setAlignmentX(Component.RIGHT_ALIGNMENT);
        soBox.add(so);
        soBox.add(Box.createVerticalStrut(3));
        soBox.add(in);
        JPanel dau = new JPanel(new BorderLayout(10, 0));
        dau.setOpaque(false);
        dau.add(gara, BorderLayout.WEST);
        dau.add(soBox, BorderLayout.EAST);
        themVaoGiay(p, dau);
        p.add(Box.createVerticalStrut(10));
        JPanel ke = new JPanel();
        ke.setBackground(BRAND);
        ke.setPreferredSize(new Dimension(10, 3));
        ke.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        ke.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(ke);
        p.add(Box.createVerticalStrut(16));

        JLabel tieuDe = new JLabel("HÓA ĐƠN THANH TOÁN", SwingConstants.CENTER);
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 22));
        tieuDe.setForeground(TEXT);
        themVaoGiay(p, tieuDe);
        JLabel phu = new JLabel("(Hóa đơn dịch vụ sửa chữa & bảo dưỡng xe)", SwingConstants.CENTER);
        phu.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        phu.setForeground(MUTED);
        themVaoGiay(p, phu);
        p.add(Box.createVerticalStrut(14));

        String trangThai = d.conLai.signum() <= 0 ? "Đã thanh toán đủ" : "Thanh toán một phần";
        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.weightx = .5;
        g.insets = new Insets(2, 0, 2, 12);
        String[][] cap = {
                { "Khách hàng", d.tenKhach, "Ngày lập", d.ngayLap },
                { "Mã khách hàng", d.maKhach, "Phiếu sửa chữa", d.maPhieu },
                { "Mã xe", d.maXe, "Mã nhân viên", d.maNhanVien },
                { "Phương thức", d.phuongThuc, "Trạng thái", trangThai }
        };
        for (int i = 0; i < cap.length; i++) {
            g.gridy = i;
            g.gridx = 0;
            info.add(dongNhanGiaTri(cap[i][0], cap[i][1]), g);
            g.gridx = 1;
            info.add(dongNhanGiaTri(cap[i][2], cap[i][3]), g);
        }
        info.setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, LINE),
                new EmptyBorder(8, 0, 8, 0)));
        themVaoGiay(p, info);
        p.add(Box.createVerticalStrut(14));

        JLabel mucCT = new JLabel("CHI TIẾT DỊCH VỤ, PHỤ TÙNG");
        mucCT.setFont(FONT_BOLD);
        mucCT.setForeground(TEXT);
        themVaoGiay(p, mucCT);
        p.add(Box.createVerticalStrut(6));

        JPanel bang = new JPanel(new GridBagLayout());
        bang.setBackground(Color.WHITE);
        bang.setBorder(new MatteBorder(1, 1, 0, 0, TEXT));
        int[] rong = { 44, 270, 54, 60, 112, 130 };
        double[] gian = { 0, 1, 0, 0, 0, 0 };
        String[] td = { "STT", "Tên hàng hóa, dịch vụ", "ĐVT", "SL", "Đơn giá", "Thành tiền" };
        int[] can = { SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER,
                SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.RIGHT };
        GridBagConstraints b = new GridBagConstraints();
        b.fill = GridBagConstraints.BOTH;
        b.gridy = 0;
        for (int i = 0; i < td.length; i++) {
            b.gridx = i;
            b.weightx = gian[i];
            bang.add(oBang(td[i], can[i], true, new Color(0xEAF0FF), rong[i]), b);
        }
        if (d.chiTiet == null || d.chiTiet.isEmpty()) {
            b.gridy = 1;
            b.gridx = 0;
            b.gridwidth = 6;
            b.weightx = 1;
            bang.add(oBang("Hóa đơn chưa có chi tiết dịch vụ / phụ tùng.",
                    SwingConstants.CENTER, false, Color.WHITE, 100), b);
        } else {
            int stt = 1;
            for (ChiTiet ct : d.chiTiet) {
                b.gridy = stt;
                b.gridwidth = 1;
                String tenHang = "<html><div style='width:" + (rong[1] - 14) + "px'>"
                        + nvl(ct.tenHang).replace("&", "&amp;").replace("<", "&lt;") + "</div></html>";
                String[] gt = { String.valueOf(stt), tenHang, LOAI_DV.equals(ct.loai) ? "Lần" : "Cái",
                        String.valueOf(ct.soLuong), dinhDangSo(ct.donGia), dinhDangSo(ct.thanhTien) };
                for (int i = 0; i < gt.length; i++) {
                    b.gridx = i;
                    b.weightx = gian[i];
                    bang.add(oBang(gt[i], can[i], false, Color.WHITE, rong[i]), b);
                }
                stt++;
            }
        }
        themVaoGiay(p, bang);
        p.add(Box.createVerticalStrut(14));

        JPanel tien = new JPanel(new GridBagLayout());
        tien.setOpaque(false);
        GridBagConstraints t = new GridBagConstraints();
        t.fill = GridBagConstraints.HORIZONTAL;
        t.gridy = 0;
        themDongTien(tien, t, "Tiền dịch vụ", dinhDangTien(d.tienDV), false, TEXT);
        themDongTien(tien, t, "Tiền phụ tùng", dinhDangTien(d.tienPT), false, TEXT);
        themDongTien(tien, t, "Tổng hóa đơn (đã gồm VAT)", dinhDangTien(d.tongHoaDon), true, TEXT);
        themDongTien(tien, t, "Tiền thuế (VAT)", dinhDangTien(d.thue), false, TEXT);
        themDongTien(tien, t, "Tiền giảm", d.giamGia.signum() > 0
                ? "- " + dinhDangTien(d.giamGia)
                : dinhDangTien(d.giamGia), false,
                d.giamGia.signum() > 0 ? RED : TEXT);
        themDongTien(tien, t, "TỔNG THANH TOÁN", dinhDangTien(d.tongTien), true, BRAND);
        themDongTien(tien, t, "Đã thanh toán trước", dinhDangTien(d.daTraTruoc), false, TEXT);
        themDongTien(tien, t, "Thanh toán lần này", dinhDangTien(d.lanNay), true, GREEN);
        themDongTien(tien, t, "Còn lại", dinhDangTien(d.conLai), true, d.conLai.signum() > 0 ? RED : TEXT);
        tien.setPreferredSize(new Dimension(330, tien.getPreferredSize().height));

        StringBuilder ghiChu = new StringBuilder("<html><div style='width:300px'>");
        ghiChu.append("<b>Số tiền thanh toán bằng chữ:</b><br><i>")
                .append(docSoThanhChu(d.lanNay.longValue())).append("</i>");
        if (d.khachDua != null) {
            ghiChu.append("<br><br>Tiền khách đưa: <b>").append(dinhDangTien(d.khachDua))
                    .append("</b><br>Tiền thừa trả khách: <b>").append(dinhDangTien(d.tienThua)).append("</b>");
        }
        ghiChu.append("</div></html>");
        JLabel lblGhiChu = new JLabel(ghiChu.toString());
        lblGhiChu.setFont(FONT);
        lblGhiChu.setForeground(TEXT);
        lblGhiChu.setVerticalAlignment(SwingConstants.TOP);
        JPanel duoiBang = new JPanel(new BorderLayout(16, 0));
        duoiBang.setOpaque(false);
        duoiBang.add(lblGhiChu, BorderLayout.CENTER);
        duoiBang.add(tien, BorderLayout.EAST);
        themVaoGiay(p, duoiBang);
        p.add(Box.createVerticalStrut(26));

        JPanel ky = new JPanel(new GridLayout(1, 3, 10, 0));
        ky.setOpaque(false);
        ky.add(khungKy("KHÁCH HÀNG"));
        ky.add(khungKy("NGƯỜI THU TIỀN"));
        ky.add(khungKy("QUẢN LÝ"));
        themVaoGiay(p, ky);
        p.add(Box.createVerticalStrut(18));
        JLabel cam = new JLabel("Cảm ơn quý khách đã sử dụng dịch vụ của " + TEN_GARA + "!",
                SwingConstants.CENTER);
        cam.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        cam.setForeground(MUTED);
        themVaoGiay(p, cam);

        p.setSize(W, 10);
        Dimension kich = new Dimension(W, p.getPreferredSize().height);
        p.setPreferredSize(kich);
        p.setMinimumSize(kich);
        p.setMaximumSize(kich);
        p.setSize(kich);
        return p;
    }

    private void themVaoGiay(JPanel parent, JComponent child) {
        child.setAlignmentX(Component.LEFT_ALIGNMENT);
        child.setMaximumSize(new Dimension(Integer.MAX_VALUE, child.getPreferredSize().height));
        parent.add(child);
    }

    private JPanel dongNhanGiaTri(String nhan, String giaTri) {
        JPanel dong = new JPanel(new BorderLayout(6, 0));
        dong.setOpaque(false);
        JLabel l = new JLabel(nhan + ":");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(MUTED);
        l.setPreferredSize(new Dimension(110, 20));
        JLabel v = new JLabel(giaTri == null || giaTri.isEmpty() ? "-" : giaTri);
        v.setFont(FONT_BOLD);
        v.setForeground(TEXT);
        dong.add(l, BorderLayout.WEST);
        dong.add(v, BorderLayout.CENTER);
        return dong;
    }

    private JLabel oBang(String text, int canh, boolean dam, Color nen, int rong) {
        JLabel l = new JLabel(text, canh);
        l.setFont(new Font("Segoe UI", dam ? Font.BOLD : Font.PLAIN, 12));
        l.setForeground(TEXT);
        l.setOpaque(true);
        l.setBackground(nen);
        l.setVerticalAlignment(SwingConstants.CENTER);
        l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 1, TEXT),
                new EmptyBorder(6, 6, 6, 6)));
        l.setPreferredSize(new Dimension(rong, l.getPreferredSize().height));
        return l;
    }

    private void themDongTien(JPanel panel, GridBagConstraints c, String nhan, String gt,
            boolean dam, Color mau) {
        Font f = new Font("Segoe UI", dam ? Font.BOLD : Font.PLAIN, dam ? 14 : 13);
        JLabel l = new JLabel(nhan);
        l.setFont(f);
        l.setForeground(dam ? mau : MUTED);
        JLabel v = new JLabel(gt, SwingConstants.RIGHT);
        v.setFont(f);
        v.setForeground(mau);
        JPanel dong = new JPanel(new BorderLayout(8, 0));
        dong.setOpaque(false);
        dong.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINE_SOFT),
                new EmptyBorder(5, 0, 5, 0)));
        dong.add(l, BorderLayout.WEST);
        dong.add(v, BorderLayout.EAST);
        c.gridx = 0;
        c.weightx = 1;
        panel.add(dong, c);
        c.gridy++;
    }

    private JPanel khungKy(String chucDanh) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(chucDanh, SwingConstants.CENTER);
        a.setFont(FONT_BOLD);
        a.setForeground(TEXT);
        a.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel b = new JLabel("(Ký, ghi rõ họ tên)", SwingConstants.CENTER);
        b.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        b.setForeground(MUTED);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(a);
        p.add(Box.createVerticalStrut(2));
        p.add(b);
        p.add(Box.createVerticalStrut(64));
        return p;
    }

    private void inKhung(JComponent giay, String maHD) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Hoa don thanh toan - " + maHD);
        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        paper.setSize(595, 842);
        paper.setImageableArea(30, 30, 535, 782);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            double scale = pageFormat.getImageableWidth() / giay.getWidth();
            double pageHeight = pageFormat.getImageableHeight() / scale;
            int pages = (int) Math.ceil(giay.getHeight() / pageHeight);
            if (pageIndex >= pages)
                return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2.scale(scale, scale);
                g2.setClip(0, 0, giay.getWidth(), (int) Math.ceil(pageHeight));
                g2.translate(0, -pageIndex * pageHeight);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                giay.printAll(g2);
            } finally {
                g2.dispose();
            }
            return Printable.PAGE_EXISTS;
        }, pf);
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                baoLoi("Không thể in hóa đơn: " + ex.getMessage());
            }
        }
    }

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
        qr.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(10, 10, 10, 10)));
        qr.setAlignmentX(Component.CENTER_ALIGNMENT);
        qr.setMaximumSize(qr.getPreferredSize());
        JLabel nhan = new JLabel("Số tiền cần chuyển");
        nhan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nhan.setForeground(MUTED);
        nhan.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tien = new JLabel(dinhDangTien(soTien));
        tien.setFont(new Font("Segoe UI", Font.BOLD, 26));
        tien.setForeground(BLUE_DARK);
        tien.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel noiDung = new JLabel("Nội dung: Thanh toan " + maHoaDon);
        noiDung.setFont(FONT);
        noiDung.setForeground(TEXT);
        noiDung.setAlignmentX(Component.CENTER_ALIGNMENT);
        giua.add(title);
        giua.add(Box.createVerticalStrut(16));
        giua.add(qr);
        giua.add(Box.createVerticalStrut(16));
        giua.add(nhan);
        giua.add(Box.createVerticalStrut(4));
        giua.add(tien);
        giua.add(Box.createVerticalStrut(8));
        giua.add(noiDung);

        JButton huy = taoButton("Hủy", false);
        JButton daChuyen = taoButton("Khách đã chuyển khoản", true);
        huy.addActionListener(e -> dlg.dispose());
        daChuyen.addActionListener(e -> {
            ketQua[0] = true;
            dlg.dispose();
        });
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        nut.setOpaque(false);
        nut.add(huy);
        nut.add(daChuyen);
        root.add(giua, BorderLayout.CENTER);
        root.add(nut, BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.pack();
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        return ketQua[0];
    }

    private static class QrPanel extends JPanel {
        private static final int N = 29;
        private final boolean[][] cells = new boolean[N][N];

        QrPanel(String seed) {
            setPreferredSize(new Dimension(260, 260));
            setBackground(Color.WHITE);
            Random random = new Random(seed.hashCode());
            for (int y = 0; y < N; y++)
                for (int x = 0; x < N; x++)
                    cells[y][x] = random.nextInt(100) < 48;
            veOGoc(0, 0);
            veOGoc(N - 7, 0);
            veOGoc(0, N - 7);
        }

        private void veOGoc(int ox, int oy) {
            for (int dy = -1; dy <= 7; dy++)
                for (int dx = -1; dx <= 7; dx++) {
                    int x = ox + dx, y = oy + dy;
                    if (x < 0 || y < 0 || x >= N || y >= N)
                        continue;
                    if (dx >= 0 && dx <= 6 && dy >= 0 && dy <= 6) {
                        boolean border = dx == 0 || dx == 6 || dy == 0 || dy == 6;
                        boolean core = dx >= 2 && dx <= 4 && dy >= 2 && dy <= 4;
                        cells[y][x] = border || core;
                    } else
                        cells[y][x] = false;
                }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Insets in = getInsets();
            int w = getWidth() - in.left - in.right, h = getHeight() - in.top - in.bottom;
            int s = Math.min(w, h) / N;
            int ox = in.left + (w - s * N) / 2, oy = in.top + (h - s * N) / 2;
            g.setColor(Color.BLACK);
            for (int y = 0; y < N; y++)
                for (int x = 0; x < N; x++)
                    if (cells[y][x])
                        g.fillRect(ox + x * s, oy + y * s, s, s);
        }
    }

    private BigDecimal tinhConNo() {
        return tongTien.subtract(daThanhToan).max(BigDecimal.ZERO);
    }

    private void capNhatTongQuan() {
        BigDecimal conNo = tinhConNo();
        lblTienDV.setText(dinhDangTien(tienDV));
        lblTienPT.setText(dinhDangTien(tienPT));
        // Tổng hóa đơn trước giảm giá = tổng thanh toán đã lưu + tiền giảm.
        BigDecimal tongHoaDon = tongTien.add(tienGiamGia);
        lblTongHoaDon.setText(dinhDangTien(tongHoaDon));
        lblThue.setText(dinhDangTien(tienThue));
        lblGiamGia.setText(tienGiamGia.signum() > 0 ? "- " + dinhDangTien(tienGiamGia)
                : dinhDangTien(tienGiamGia));
        lblTongThanhToan.setText(dinhDangTien(tongTien));
        lblDaThanhToan.setText(dinhDangTien(daThanhToan));
        lblConPhaiTra.setText(dinhDangTien(conNo));

        boolean coHoaDon = maHoaDon != null && !maHoaDon.trim().isEmpty() && tongTien.signum() > 0;
        boolean hetNo = conNo.signum() <= 0;
        int percent = 0;
        if (coHoaDon) {
            percent = daThanhToan.multiply(BigDecimal.valueOf(100))
                    .divide(tongTien, 0, RoundingMode.DOWN).intValue();
            percent = Math.max(0, Math.min(100, percent));
        }
        progDaTra.setValue(percent);

        if (!coHoaDon) {
            txtTrangThaiHoaDon.setText("");
            datPill("", MUTED, SOFT);
        } else if (hetNo) {
            txtTrangThaiHoaDon.setText("Đã thanh toán");
            txtTrangThaiHoaDon.setForeground(GREEN);
            datPill("● Đã thanh toán đủ", GREEN, GREEN_SOFT);
        } else if (daThanhToan.signum() > 0) {
            txtTrangThaiHoaDon.setText("Thanh toán một phần");
            txtTrangThaiHoaDon.setForeground(ORANGE);
            datPill("", MUTED, SOFT);
        } else {
            txtTrangThaiHoaDon.setText("Chưa thanh toán");
            txtTrangThaiHoaDon.setForeground(ORANGE);
            datPill("● Chưa thanh toán", ORANGE, ORANGE_SOFT);
        }

        boolean choPhep = coHoaDon && !hetNo;
        txtSoTienThanhToan.setEnabled(choPhep);
        txtTienKhachDua.setEnabled(choPhep);
        for (JButton b : cacNutNhanh)
            b.setEnabled(choPhep);
        btnTienMat.setEnabled(choPhep);
        btnChuyenKhoan.setEnabled(choPhep);
        btnXacNhan.setEnabled(choPhep);
        if (!choPhep) {
            txtSoTienThanhToan.setText("");
            txtTienKhachDua.setText("");
        } else if (txtSoTienThanhToan.getText().trim().isEmpty()) {
            txtSoTienThanhToan.setText(conNo.stripTrailingZeros().toPlainString());
        }
        capNhatTienThua();
    }

    private void capNhatTienThua() {
        if (lblTienThua == null || txtTienKhachDua == null || txtSoTienThanhToan == null)
            return;
        BigDecimal soTien = docSoTien(txtSoTienThanhToan.getText());
        BigDecimal khachDua = docSoTien(txtTienKhachDua.getText());
        if (!laTienMat() || khachDua == null || soTien == null) {
            lblNhanTienThua.setText("Tiền thừa trả khách");
            lblTienThua.setText("0 ₫");
            lblTienThua.setForeground(GREEN);
            return;
        }
        BigDecimal chenh = khachDua.subtract(soTien);
        if (chenh.signum() < 0) {
            lblNhanTienThua.setText("Khách còn thiếu");
            lblTienThua.setText(dinhDangTien(chenh.negate()));
            lblTienThua.setForeground(RED);
        } else {
            lblNhanTienThua.setText("Tiền thừa trả khách");
            lblTienThua.setText(dinhDangTien(chenh));
            lblTienThua.setForeground(GREEN);
        }
    }

    private static class ViewCuon extends JPanel implements Scrollable {
        ViewCuon(LayoutManager layout) {
            super(layout);
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 18;
        }

        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return Math.max(r.height - 40, 18);
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            Container parent = getParent();
            return parent instanceof JViewport && parent.getHeight() > getPreferredSize().height;
        }
    }

    private JPanel taoCard() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JLabel taoTieuDeMuc(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel taoNhan(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED);
        return l;
    }

    private JLabel taoNhanTruong(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel taoGiaTri(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD);
        l.setForeground(TEXT);
        return l;
    }

    private JLabel taoGiaTriNoiBat(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        l.setForeground(BLUE_DARK);
        return l;
    }

    private JComponent taoDuongKe() {
        JPanel p = new JPanel();
        p.setBackground(LINE);
        p.setPreferredSize(new Dimension(10, 1));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JPanel taoDongSoTien(String nhan, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = taoNhan(nhan);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(l, BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private JTextField taoTextField() {
        JTextField f = new JTextField();
        f.setFont(FONT);
        f.setPreferredSize(new Dimension(100, 40));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JTextField taoOChiDoc() {
        JTextField f = new JTextField();
        f.setEditable(false);
        f.setFont(FONT_BOLD);
        f.setForeground(TEXT);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(0, 0, 8, 0));
        f.setPreferredSize(new Dimension(100, 26));
        f.setCursor(Cursor.getDefaultCursor());
        return f;
    }

    private JPanel taoHangChip(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JButton b : buttons)
            p.add(b);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return p;
    }

    private JButton taoChip(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBackground(BLUE_SOFT);
        b.setForeground(BLUE_DARK);
        b.setBorder(new CompoundBorder(new LineBorder(new Color(0xBFD3FA), 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        cacNutNhanh.add(b);
        return b;
    }

    private JButton taoButton(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (primary) {
            b.setBackground(BRAND);
            b.setForeground(Color.WHITE);
            b.setBorder(new EmptyBorder(11, 18, 11, 18));
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(TEXT);
            b.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true),
                    new EmptyBorder(10, 17, 10, 17)));
        }
        return b;
    }

    private JToggleButton taoToggle(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        capNhatMauToggle(b);
        return b;
    }

    private void capNhatMauToggle(JToggleButton b) {
        if (b == null)
            return;
        if (b.isSelected()) {
            b.setBackground(BRAND);
            b.setForeground(Color.WHITE);
            b.setBorder(new CompoundBorder(new LineBorder(BRAND, 1, true),
                    new EmptyBorder(12, 10, 12, 10)));
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(TEXT);
            b.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true),
                    new EmptyBorder(12, 10, 12, 10)));
        }
    }

    private BigDecimal docSoTien(String text) {
        if (text == null || text.trim().isEmpty())
            return null;
        try {
            String value = text.trim().replace("₫", "").replace("VND", "")
                    .replace(" ", "").replace(".", "").replace(",", "");
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String dinhDangSo(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("vi-VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        format.setGroupingUsed(true);
        return format.format(value == null ? BigDecimal.ZERO : value);
    }

    private String dinhDangTien(BigDecimal value) {
        return dinhDangSo(value) + " ₫";
    }

    private void baoLoi(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private static BigDecimal bd(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

    private static final String[] CH = { "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };

    private String docBaSo(int n, boolean dayDu) {
        int tram = n / 100, chuc = n % 100 / 10, dv = n % 10;
        StringBuilder sb = new StringBuilder();
        if (tram > 0 || dayDu)
            sb.append(CH[tram]).append(" trăm ");
        if (chuc > 1) {
            sb.append(CH[chuc]).append(" mươi ");
            if (dv == 1)
                sb.append("mốt");
            else if (dv == 5)
                sb.append("lăm");
            else if (dv > 0)
                sb.append(CH[dv]);
        } else if (chuc == 1) {
            sb.append("mười ");
            if (dv == 5)
                sb.append("lăm");
            else if (dv > 0)
                sb.append(CH[dv]);
        } else if (dv > 0) {
            if (tram > 0 || dayDu)
                sb.append("lẻ ");
            sb.append(CH[dv]);
        }
        return sb.toString().trim();
    }

    private String docSoThanhChu(long so) {
        if (so <= 0)
            return "Không đồng";
        String[] bac = { "", " nghìn", " triệu", " tỷ" };
        int[] nhom = new int[4];
        long x = so;
        for (int i = 0; i < 3; i++) {
            nhom[i] = (int) (x % 1000);
            x /= 1000;
        }
        nhom[3] = (int) Math.min(x, 999999);
        StringBuilder sb = new StringBuilder();
        boolean daCo = false;
        for (int i = 3; i >= 0; i--) {
            if (nhom[i] == 0)
                continue;
            String dang = i == 3 && nhom[i] > 999 ? String.valueOf(nhom[i]) : docBaSo(nhom[i], daCo);
            sb.append(dang).append(bac[i]).append(' ');
            daCo = true;
        }
        String kq = sb.toString().trim();
        return Character.toUpperCase(kq.charAt(0)) + kq.substring(1) + " đồng chẵn.";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrmThanhToan frame = new FrmThanhToan("HD001");
            frame.setVisible(true);
        });
    }
}

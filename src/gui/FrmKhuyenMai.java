package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import connectDB.ConnectDB;

public class FrmKhuyenMai extends JPanel {

    // =====================================================================
    // 1. HẰNG SỐ, MODEL, THUỘC TÍNH
    // =====================================================================
    private static final Color BLUE = new Color(25, 130, 235);
    private static final Color BLUE_DAM = new Color(25, 118, 210);
    private static final Color NAVY = new Color(20, 59, 105);
    private static final Color NAVY_DAM = new Color(25, 55, 90);
    private static final Color BG = new Color(244, 249, 255);
    private static final Color BORDER = new Color(205, 224, 246);
    private static final Font F_TEXT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    // Trạng thái (cột trangThai NVARCHAR(20))
    private static final String TT_DANG = "Đang áp dụng";
    private static final String TT_CHUA = "Chưa áp dụng";
    private static final String TT_HET = "Hết hạn";
    private static final String TT_TAM = "Tạm ngưng";
    private static final String TT_CHUA_XAC_DINH = "—"; // ngày chưa đủ / không hợp lệ

    private static final int W_O = 200; // chiều rộng chung của các ô trong khung tìm kiếm
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String SQL_TAI = """
            SELECT maKM, tenKM, tileGiam, ngayBatDau, ngayKetThuc, dieuKienApDung, trangThai
            FROM ChuongTrinhKhuyenMai
            ORDER BY ngayBatDau DESC
            """;
    private static final String SQL_THEM = """
            INSERT INTO ChuongTrinhKhuyenMai
                (maKM, tenKM, tileGiam, ngayBatDau, ngayKetThuc, dieuKienApDung, trangThai)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String SQL_CAP_NHAT = """
            UPDATE ChuongTrinhKhuyenMai
            SET tenKM = ?, tileGiam = ?, ngayBatDau = ?, ngayKetThuc = ?,
                dieuKienApDung = ?, trangThai = ?
            WHERE maKM = ?
            """;
    private static final String SQL_XOA = "DELETE FROM ChuongTrinhKhuyenMai WHERE maKM = ?";

    /** Một dòng của bảng ChuongTrinhKhuyenMai. */
    private static class KhuyenMai {
        String maKM;
        String tenKM;
        BigDecimal tileGiam;
        LocalDate ngayBatDau;
        LocalDate ngayKetThuc;
        String dieuKienApDung;
        String trangThai;

        KhuyenMai(String ma, String ten, BigDecimal tile, LocalDate bd, LocalDate kt, String dk, String tt) {
            this.maKM = ma;
            this.tenKM = ten;
            this.tileGiam = tile;
            this.ngayBatDau = bd;
            this.ngayKetThuc = kt;
            this.dieuKienApDung = dk;
            this.trangThai = tt;
        }
    }

    private final List<KhuyenMai> dsKM = new ArrayList<>(); // toàn bộ dữ liệu
    private final List<KhuyenMai> dsHienThi = new ArrayList<>(); // dữ liệu sau khi lọc (khớp dòng trên bảng)

    // Khung tìm kiếm
    private JTextField txtTimMa, txtTimTen, txtTileTu, txtTileDen;
    private JDateChooser txtLocTuNgay, txtLocDenNgay;
    private JComboBox<String> cboTimTrangThai;

    // Bảng
    private JTable tblKhuyenMai;
    private DefaultTableModel model;
    private JLabel lblTong;

    // =====================================================================
    // 2. KHỞI TẠO & GIAO DIỆN CHÍNH
    // =====================================================================
    public FrmKhuyenMai() {
        setLayout(new BorderLayout(0, 8));
        setBackground(BG);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(0, 16, 16, 16));
        body.add(buildSearchPanel(), BorderLayout.NORTH);
        body.add(buildTablePanel(), BorderLayout.CENTER);

        add(buildTitle(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        lamMoiDuLieu();
    }

    private JPanel buildTitle() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        p.setBackground(BG);
        JLabel title = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(NAVY);
        p.add(title);
        return p;
    }

    /**
     * Khung trắng bo viền có tiêu đề; benPhai (có thể null) đặt cùng hàng tiêu đề,
     * sát phải.
     */
    private JPanel khung(String tieuDe, Component benPhai) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(10, 14, 12, 14)));

        JLabel heading = new JLabel(tieuDe);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        heading.setForeground(NAVY);

        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.setBorder(new EmptyBorder(0, 0, 10, 0));
        dau.add(heading, BorderLayout.WEST);
        if (benPhai != null) {
            dau.add(benPhai, BorderLayout.EAST);
        }
        p.add(dau, BorderLayout.NORTH);
        return p;
    }

    // ---------- Khung tìm kiếm / lọc ----------
    private JPanel buildSearchPanel() {
        JPanel panel = khung("Tìm kiếm / Lọc", null);

        txtTimMa = oNhap(W_O);
        txtTimTen = oNhap(W_O);
        cboTimTrangThai = new JComboBox<>(new String[] { "Tất cả", TT_DANG, TT_CHUA, TT_HET, TT_TAM });
        kichThuoc(cboTimTrangThai, W_O, 30);
        cboTimTrangThai.setFont(F_TEXT);
        cboTimTrangThai.setBackground(Color.WHITE);
        txtTileTu = oNhap(90);
        txtTileDen = oNhap(90);
        txtLocTuNgay = oChonNgay(W_O);
        txtLocDenNgay = oChonNgay(W_O);
        // Ngày bắt đầu lọc: không cho chọn ngày trước hôm nay
        LocalDate homNay = LocalDate.now();
        txtLocTuNgay.setMinSelectableDate(Date.valueOf(homNay));

        // Ngày kết thúc lọc: chỉ chọn ngày từ ngày bắt đầu trở đi
        txtLocDenNgay.setEnabled(false);

        txtLocTuNgay.addPropertyChangeListener("date", e -> {
            LocalDate tuNgay = layNgay(txtLocTuNgay);
            LocalDate denNgay = layNgay(txtLocDenNgay);

            if (tuNgay != null) {
                txtLocDenNgay.setMinSelectableDate(Date.valueOf(tuNgay));
                txtLocDenNgay.setEnabled(true);

                // Nếu ngày kết thúc cũ nhỏ hơn ngày bắt đầu mới thì xóa
                if (denNgay != null && denNgay.isBefore(tuNgay)) {
                    txtLocDenNgay.setDate(null);
                }
            } else {
                txtLocDenNgay.setDate(null);
                txtLocDenNgay.setEnabled(false);
            }
        });

        Component[][] luoi = {
                { nhan("Mã khuyến mãi:"), txtTimMa, nhan("Tên khuyến mãi:"), txtTimTen,
                        nhan("Trạng thái:"), cboTimTrangThai },
                { nhan("Tỉ lệ giảm (%):"), taoOTileGiam(), nhan("Hiệu lực từ ngày:"), txtLocTuNgay,
                        nhan("Đến ngày:"), txtLocDenNgay } };

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 10);
        for (int y = 0; y < luoi.length; y++) {
            for (int x = 0; x < luoi[y].length; x++) {
                boolean laO = (x % 2 == 1);
                c.gridx = x;
                c.gridy = y;
                c.weightx = laO ? 1 : 0; // nhãn giữ nguyên, ô nhập giãn đều nhau
                c.fill = laO ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
                form.add(luoi[y][x], c);
            }
        }

        // Hai nút ở hàng riêng bên dưới các trường tìm kiếm
        GridBagConstraints cn = new GridBagConstraints();
        cn.gridx = 0;
        cn.gridy = luoi.length;
        cn.gridwidth = 6;
        cn.weightx = 1;
        cn.anchor = GridBagConstraints.EAST;
        cn.fill = GridBagConstraints.HORIZONTAL;
        cn.insets = new Insets(8, 0, 0, 0);
        form.add(taoNutTimKiem(), cn);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    /** Ô lọc tỉ lệ giảm: hai ô giãn đều, dấu "–" ở giữa. */
    private JPanel taoOTileGiam() {
        JPanel tile = new JPanel(new GridBagLayout());
        tile.setOpaque(false);

        JLabel gach = nhan("–");
        gach.setHorizontalAlignment(SwingConstants.CENTER);
        gach.setPreferredSize(new Dimension(24, 30));

        GridBagConstraints ct = new GridBagConstraints();
        ct.fill = GridBagConstraints.HORIZONTAL;
        ct.weightx = 1;
        tile.add(txtTileTu, ct);
        ct.weightx = 0;
        tile.add(gach, ct);
        ct.weightx = 1;
        tile.add(txtTileDen, ct);

        kichThuoc(tile, W_O, 30);
        return tile;
    }

    private JPanel taoNutTimKiem() {
        JButton btnTim = nutChinh("Tìm kiếm", BLUE_DAM);
        JButton btnLamMoi = nutThuong("Làm mới");
        btnLamMoi.setForeground(NAVY_DAM);
        kichThuoc(btnTim, 130, 34);
        kichThuoc(btnLamMoi, 130, 34);

        btnTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> {
            txtTimMa.setText("");
            txtTimTen.setText("");
            cboTimTrangThai.setSelectedIndex(0);
            txtTileTu.setText("");
            txtTileDen.setText("");

            txtLocTuNgay.setDate(null);
            txtLocDenNgay.setDate(null);
            txtLocDenNgay.setEnabled(false);

            timKiem();
        });
        for (JTextField t : new JTextField[] { txtTimMa, txtTimTen, txtTileTu, txtTileDen }) {
            t.addActionListener(e -> timKiem());
        }

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setOpaque(false);
        nut.add(btnTim);
        nut.add(btnLamMoi);
        return nut;
    }

    // ---------- Danh sách khuyến mãi ----------
    private JPanel buildTablePanel() {
        JButton btnThem = nutChinh("+  Thêm mới", BLUE_DAM);
        btnThem.addActionListener(e -> moKhungThongTin(null));

        JPanel panel = khung("Danh sách khuyến mãi", btnThem);
        panel.add(taoBang(), BorderLayout.CENTER);
        panel.add(taoChanBang(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane taoBang() {
        String[] cot = { "Mã KM", "Tên chương trình khuyến mãi", "Tỉ lệ giảm", "Ngày bắt đầu", "Ngày kết thúc",
                "Điều kiện áp dụng", "Trạng thái" };
        model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKhuyenMai = new JTable(model);
        tblKhuyenMai.setRowHeight(34);
        tblKhuyenMai.setFont(F_TEXT);
        tblKhuyenMai.getTableHeader().setFont(F_BOLD);
        tblKhuyenMai.getTableHeader().setBackground(new Color(236, 245, 255));
        tblKhuyenMai.getTableHeader().setForeground(NAVY);
        tblKhuyenMai.getTableHeader().setReorderingAllowed(false);
        tblKhuyenMai.setSelectionBackground(new Color(214, 235, 255));
        tblKhuyenMai.setSelectionForeground(Color.BLACK);
        tblKhuyenMai.setGridColor(new Color(225, 235, 246));
        tblKhuyenMai.setShowVerticalLines(false);
        tblKhuyenMai.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblKhuyenMai.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        int[] rong = { 80, 260, 85, 105, 105, 300, 115 };
        for (int i = 0; i < rong.length; i++) {
            tblKhuyenMai.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        DefaultTableCellRenderer giua = new DefaultTableCellRenderer();
        giua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[] { 0, 2, 3, 4 }) {
            tblKhuyenMai.getColumnModel().getColumn(i).setCellRenderer(giua);
        }
        tblKhuyenMai.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());

        // Nhấn vào dòng -> mở khung thông tin riêng
        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhuyenMai.rowAtPoint(e.getPoint());
                if (row >= 0 && row < dsHienThi.size()) {
                    moKhungThongTin(dsHienThi.get(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblKhuyenMai);
        scroll.setBorder(new LineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel taoChanBang() {
        lblTong = new JLabel("Tổng số bản ghi: 0");
        lblTong.setFont(F_TEXT);
        lblTong.setForeground(NAVY);

        JPanel chan = new JPanel(new BorderLayout());
        chan.setBackground(Color.WHITE);
        chan.setBorder(new EmptyBorder(8, 0, 0, 0));
        chan.add(lblTong, BorderLayout.WEST);
        return chan;
    }

    /** Cột trạng thái: chữ đậm, căn giữa, màu theo từng trạng thái. */
    private static class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(F_BOLD);
            setForeground(mauTrangThai(String.valueOf(v)));
            return this;
        }
    }

    /** Nhãn trạng thái trong hộp thoại: chỉ là chữ đậm có màu, không khung. */
    private static class Badge extends JLabel {
        Badge() {
            setFont(F_BOLD);
        }

        void setTrangThai(String tt) {
            setText(tt);
            setForeground(mauTrangThai(tt));
        }
    }

    private static Color mauTrangThai(String tt) {
        if (TT_DANG.equals(tt))
            return new Color(22, 140, 70);
        if (TT_CHUA.equals(tt))
            return new Color(25, 110, 200);
        if (TT_TAM.equals(tt))
            return new Color(210, 120, 10);
        return new Color(140, 140, 140); // Hết hạn / chưa xác định
    }

    // =====================================================================
    // 3. HỘP THOẠI THÔNG TIN KHUYẾN MÃI
    // =====================================================================
    /** km == null: thêm mới; khác null: xem / sửa / xóa khuyến mãi đó. */
    private void moKhungThongTin(KhuyenMai km) {
        new DlgKhuyenMai(km).setVisible(true);
    }

    private class DlgKhuyenMai extends JDialog {
        private final KhuyenMai km;
        private final boolean themMoi;

        private final JTextField txtMa = oNhap(310);
        private final JTextField txtTen = oNhap(310);
        private final JTextField txtTile = oNhap(135);
        private final JDateChooser dcBatDau = oChonNgay(150);
        private final JDateChooser dcKetThuc = oChonNgay(150);
        private final JTextArea txtDieuKien = new JTextArea(4, 28);
        private final JCheckBox chkTamNgung = new JCheckBox("Tạm ngưng áp dụng");
        private final Badge lblTrangThai = new Badge();

        DlgKhuyenMai(KhuyenMai km) {
            super(SwingUtilities.getWindowAncestor(FrmKhuyenMai.this),
                    km == null ? "Thêm khuyến mãi" : "Thông tin khuyến mãi",
                    Dialog.ModalityType.APPLICATION_MODAL);
            this.km = km;
            this.themMoi = (km == null);
            setResizable(false);

            napDuLieu();
            ganSuKien();
            capNhatTrangThai();
            dungGiaoDien();

            pack();
            setLocationRelativeTo(FrmKhuyenMai.this);
            txtTen.requestFocusInWindow();
        }

        // ---------- Dữ liệu ban đầu ----------
        private void napDuLieu() {
            txtDieuKien.setFont(F_TEXT);
            txtDieuKien.setLineWrap(true);
            txtDieuKien.setWrapStyleWord(true);
            chkTamNgung.setFont(F_TEXT);
            chkTamNgung.setOpaque(false);

            if (themMoi) {
                LocalDate homNay = LocalDate.now();
                txtMa.setText(taoMaMoi());
                ganNgay(dcBatDau, homNay);
                dcBatDau.setMinSelectableDate(Date.valueOf(homNay)); // không chọn ngày bắt đầu trước hôm nay
                dcKetThuc.setDate(null);
            } else {
                txtMa.setText(km.maKM);
                txtMa.setEditable(false);
                txtMa.setBackground(new Color(238, 242, 247));
                txtTen.setText(km.tenKM);
                txtTile.setText(km.tileGiam.stripTrailingZeros().toPlainString());
                ganNgay(dcBatDau, km.ngayBatDau);
                ganNgay(dcKetThuc, km.ngayKetThuc);
                txtDieuKien.setText(km.dieuKienApDung == null ? "" : km.dieuKienApDung);
                chkTamNgung.setSelected(TT_TAM.equals(km.trangThai));
            }
            txtDieuKien.setCaretPosition(0);

            // Ngày kết thúc chỉ chọn được khi đã có ngày bắt đầu, và không trước ngày bắt
            // đầu
            LocalDate bd = layNgay(dcBatDau);
            if (bd != null) {
                dcKetThuc.setMinSelectableDate(Date.valueOf(bd));
                dcKetThuc.setEnabled(true);
            }
        }

        private void ganSuKien() {
            chkTamNgung.addActionListener(e -> capNhatTrangThai());
            dcKetThuc.addPropertyChangeListener("date", e -> capNhatTrangThai());
            dcBatDau.addPropertyChangeListener("date", e -> {
                LocalDate bd = layNgay(dcBatDau);
                LocalDate kt = layNgay(dcKetThuc);

                if (bd != null) {
                    dcKetThuc.setMinSelectableDate(Date.valueOf(bd));
                    dcKetThuc.setEnabled(true);
                    if (kt != null && kt.isBefore(bd)) {
                        dcKetThuc.setDate(null);
                    }
                } else {
                    dcKetThuc.setDate(null);
                    dcKetThuc.setEnabled(false);
                }
                capNhatTrangThai();
            });
        }

        /** Trạng thái hiển thị do hệ thống tự tính theo ngày (trừ khi tạm ngưng). */
        private void capNhatTrangThai() {
            LocalDate bd = layNgay(dcBatDau);
            LocalDate kt = layNgay(dcKetThuc);

            String tt;
            if (chkTamNgung.isSelected()) {
                tt = TT_TAM;
            } else if (bd == null || kt == null || kt.isBefore(bd)) {
                tt = TT_CHUA_XAC_DINH;
            } else {
                tt = tinhTrangThai(bd, kt);
            }
            lblTrangThai.setTrangThai(tt);
        }

        // ---------- Giao diện ----------
        private void dungGiaoDien() {
            JLabel tieuDe = new JLabel(themMoi ? "Thêm khuyến mãi mới" : "Thông tin khuyến mãi");
            tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 16));
            tieuDe.setForeground(NAVY);
            tieuDe.setOpaque(true);
            tieuDe.setBackground(new Color(236, 245, 255));
            tieuDe.setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton btnLuu = nutChinh(themMoi ? "Lưu" : "Lưu thay đổi");
            JButton btnXoa = nutThuong("Xóa");
            JButton btnDong = nutThuong("Đóng");
            btnXoa.setForeground(new Color(200, 50, 50));
            btnXoa.setVisible(!themMoi);

            btnLuu.addActionListener(e -> luu());
            btnXoa.addActionListener(e -> xoa());
            btnDong.addActionListener(e -> dispose());

            JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            nut.setBackground(Color.WHITE);
            nut.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
            nut.add(btnLuu);
            nut.add(btnXoa);
            nut.add(btnDong);

            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            add(tieuDe, BorderLayout.NORTH);
            add(taoForm(), BorderLayout.CENTER);
            add(nut, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(btnLuu);
        }

        private JPanel taoForm() {
            JPanel tileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            tileRow.setOpaque(false);
            tileRow.add(txtTile);
            tileRow.add(nhan("%  (0 < tỉ lệ ≤ 100)"));

            JPanel ngayRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            ngayRow.setOpaque(false);
            ngayRow.add(dcBatDau);
            ngayRow.add(nhan("đến"));
            ngayRow.add(dcKetThuc);

            JPanel trangThaiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
            trangThaiRow.setOpaque(false);
            trangThaiRow.add(lblTrangThai);
            trangThaiRow.add(chkTamNgung);

            JScrollPane spDieuKien = new JScrollPane(txtDieuKien);
            spDieuKien.setPreferredSize(new Dimension(310, 90));

            String[] nhanTruong = { "Mã khuyến mãi:", "Tên khuyến mãi:", "Tỉ lệ giảm:", "Thời gian áp dụng:",
                    "Điều kiện áp dụng:", "Trạng thái:" };
            Component[] oTruong = { txtMa, txtTen, tileRow, ngayRow, spDieuKien, trangThaiRow };

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(new EmptyBorder(16, 20, 8, 20));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(6, 0, 6, 12);
            for (int i = 0; i < nhanTruong.length; i++) {
                c.gridy = i;
                c.gridx = 0;
                c.anchor = (i == 4) ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
                form.add(nhan(nhanTruong[i]), c);
                c.gridx = 1;
                c.anchor = GridBagConstraints.WEST;
                form.add(oTruong[i], c);
            }
            return form;
        }

        // ---------- Hành động ----------
        private void luu() {
            KhuyenMai ban = docVaKiemTra();
            if (ban != null && luuVaoCSDL(ban, themMoi)) {
                dispose();
                lamMoiDuLieu();
            }
        }

        private void xoa() {
            int ans = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa khuyến mãi " + km.maKM + "?\n",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (ans == JOptionPane.YES_OPTION && xoaKhoiCSDL(km.maKM)) {
                dispose();
                lamMoiDuLieu();
            }
        }

        /**
         * Kiểm tra dữ liệu theo ràng buộc của bảng; trả về khuyến mãi mới, hoặc null
         * nếu có lỗi.
         */
        private KhuyenMai docVaKiemTra() {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String tileStr = txtTile.getText().trim().replace(',', '.');

            if (ma.isEmpty())
                return loi("Vui lòng nhập mã khuyến mãi.", null);
            if (ma.length() > 20 || !ma.matches("[A-Za-z0-9_-]+"))
                return loi("Mã khuyến mãi tối đa 20 ký tự, chỉ gồm chữ không dấu, số, '-' hoặc '_'.", null);
            if (themMoi && timTheoMa(ma) != null)
                return loi("Mã khuyến mãi \"" + ma + "\" đã tồn tại.", null);
            if (ten.isEmpty())
                return loi("Vui lòng nhập tên khuyến mãi.", txtTen);
            if (ten.length() > 100)
                return loi("Tên khuyến mãi tối đa 100 ký tự.", null);

            BigDecimal tile;
            try {
                tile = new BigDecimal(tileStr);
            } catch (NumberFormatException ex) {
                return loi("Tỉ lệ giảm phải là số (ví dụ 10 hoặc 7.5).", txtTile);
            }
            if (tile.signum() <= 0 || tile.compareTo(new BigDecimal("100")) > 0)
                return loi("Tỉ lệ giảm phải lớn hơn 0 và không vượt quá 100%.", txtTile);
            if (tile.stripTrailingZeros().scale() > 2)
                return loi("Tỉ lệ giảm chỉ được tối đa 2 chữ số thập phân.", txtTile);

            LocalDate bd = layNgay(dcBatDau);
            LocalDate kt = layNgay(dcKetThuc);
            if (bd == null || kt == null)
                return loi("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.", null);
            if (kt.isBefore(bd))
                return loi("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.", null);

            String trangThai = chkTamNgung.isSelected() ? TT_TAM : tinhTrangThai(bd, kt);
            return new KhuyenMai(ma, ten, tile.setScale(2, RoundingMode.HALF_UP), bd, kt,
                    txtDieuKien.getText().trim(), trangThai);
        }

        private KhuyenMai loi(String message, Component focus) {
            canhBao(this, message);
            if (focus != null) {
                focus.requestFocus();
            }
            return null;
        }
    }

    // =====================================================================
    // 4. NGHIỆP VỤ: TÌM KIẾM / TRẠNG THÁI / MÃ
    // =====================================================================
    /** Tải lại dữ liệu từ CSDL rồi áp dụng lại bộ lọc hiện tại. */
    private void lamMoiDuLieu() {
        taiDanhSachKhuyenMai();
        timKiem();
    }

    private void timKiem() {
        String ma = txtTimMa.getText().trim().toLowerCase();
        String ten = txtTimTen.getText().trim().toLowerCase();
        String tt = (String) cboTimTrangThai.getSelectedItem();

        BigDecimal tileTu, tileDen;
        try {
            tileTu = docSo(txtTileTu);
            tileDen = docSo(txtTileDen);
        } catch (NumberFormatException ex) {
            canhBao(this, "Tỉ lệ giảm lọc phải là số.");
            return;
        }

        LocalDate tuNgay = layNgay(txtLocTuNgay);
        LocalDate denNgay = layNgay(txtLocDenNgay);
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            canhBao(this, "Khoảng ngày lọc không hợp lệ (đến ngày < từ ngày).");
            return;
        }

        capNhatTrangThaiTheoNgay();
        dsHienThi.clear();
        model.setRowCount(0);

        for (KhuyenMai km : dsKM) {
            if (!km.maKM.toLowerCase().contains(ma) || !km.tenKM.toLowerCase().contains(ten))
                continue;
            if (!"Tất cả".equals(tt) && !km.trangThai.equals(tt))
                continue;
            if (tileTu != null && km.tileGiam.compareTo(tileTu) < 0)
                continue;
            if (tileDen != null && km.tileGiam.compareTo(tileDen) > 0)
                continue;
            // Khuyến mãi có hiệu lực giao với khoảng ngày đang lọc
            if (tuNgay != null && km.ngayKetThuc.isBefore(tuNgay))
                continue;
            if (denNgay != null && km.ngayBatDau.isAfter(denNgay))
                continue;

            dsHienThi.add(km);
            model.addRow(new Object[] {
                    km.maKM,
                    km.tenKM,
                    km.tileGiam.stripTrailingZeros().toPlainString() + "%",
                    km.ngayBatDau.format(DF),
                    km.ngayKetThuc.format(DF),
                    km.dieuKienApDung == null ? "" : km.dieuKienApDung.replace('\n', ' '),
                    km.trangThai });
        }
        lblTong.setText("Tổng số bản ghi: " + dsHienThi.size() + " / " + dsKM.size());
        tblKhuyenMai.clearSelection();
    }

    /**
     * Tự chuyển trạng thái theo ngày hiện tại (khuyến mãi "Tạm ngưng" giữ nguyên).
     */
    private void capNhatTrangThaiTheoNgay() {
        for (KhuyenMai km : dsKM) {
            if (!TT_TAM.equals(km.trangThai)) {
                km.trangThai = tinhTrangThai(km.ngayBatDau, km.ngayKetThuc);
            }
        }
    }

    private String tinhTrangThai(LocalDate bd, LocalDate kt) {
        LocalDate homNay = LocalDate.now();
        if (homNay.isBefore(bd))
            return TT_CHUA;
        if (homNay.isAfter(kt))
            return TT_HET;
        return TT_DANG;
    }

    private KhuyenMai timTheoMa(String ma) {
        for (KhuyenMai km : dsKM) {
            if (km.maKM.equalsIgnoreCase(ma))
                return km;
        }
        return null;
    }

    /** Sinh mã dạng KM001, KM002... theo số lớn nhất hiện có. */
    private String taoMaMoi() {
        int max = 0;
        for (KhuyenMai km : dsKM) {
            try {
                max = Math.max(max, Integer.parseInt(km.maKM.substring(2)));
            } catch (Exception ignored) {
                // mã không theo dạng KMxxx -> bỏ qua
            }
        }
        return String.format("KM%03d", max + 1);
    }

    // =====================================================================
    // 5. TRUY CẬP CSDL
    // =====================================================================
    private void taiDanhSachKhuyenMai() {
        dsKM.clear();

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_TAI);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsKM.add(new KhuyenMai(
                        rs.getString("maKM"),
                        rs.getString("tenKM"),
                        rs.getBigDecimal("tileGiam"),
                        rs.getDate("ngayBatDau").toLocalDate(),
                        rs.getDate("ngayKetThuc").toLocalDate(),
                        rs.getString("dieuKienApDung"),
                        rs.getString("trangThai")));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách khuyến mãi từ CSDL!\n" + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Thêm mới (themMoi = true) hoặc cập nhật khuyến mãi. Trả về true nếu thành
     * công.
     */
    private boolean luuVaoCSDL(KhuyenMai km, boolean themMoi) {
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(themMoi ? SQL_THEM : SQL_CAP_NHAT)) {

            int i = 1;
            if (themMoi) {
                ps.setString(i++, km.maKM);
            }
            ps.setNString(i++, km.tenKM);
            ps.setBigDecimal(i++, km.tileGiam);
            ps.setDate(i++, Date.valueOf(km.ngayBatDau));
            ps.setDate(i++, Date.valueOf(km.ngayKetThuc));
            if (km.dieuKienApDung == null || km.dieuKienApDung.isEmpty()) {
                ps.setNull(i++, Types.NVARCHAR);
            } else {
                ps.setNString(i++, km.dieuKienApDung);
            }
            ps.setNString(i++, km.trangThai);
            if (!themMoi) {
                ps.setString(i, km.maKM);
            }

            if (ps.executeUpdate() > 0) {
                thongBao(themMoi ? "Thêm khuyến mãi thành công!" : "Cập nhật khuyến mãi thành công!",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
            thongBao(themMoi ? "Không thêm được khuyến mãi." : "Không tìm thấy khuyến mãi cần cập nhật.",
                    JOptionPane.WARNING_MESSAGE);
            return false;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lưu khuyến mãi vào CSDL!\n" + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean xoaKhoiCSDL(String maKM) {
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_XOA)) {

            ps.setString(1, maKM);

            if (ps.executeUpdate() > 0) {
                thongBao("Xóa khuyến mãi thành công!", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
            thongBao("Không tìm thấy khuyến mãi cần xóa trong CSDL.", JOptionPane.WARNING_MESSAGE);
            return false;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa khuyến mãi khỏi CSDL!\n"
                            + "Có thể khuyến mãi đang được tham chiếu bởi hóa đơn.\n"
                            + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // =====================================================================
    // 6. TIỆN ÍCH
    // =====================================================================

    // ---------- Ngày / số ----------
    private JDateChooser oChonNgay(int width) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("dd/MM/yyyy");
        chooser.setFont(F_TEXT);
        chooser.setDate(null);
        kichThuoc(chooser, width, 30);
        return chooser;
    }

    private LocalDate layNgay(JDateChooser chooser) {
        java.util.Date date = chooser.getDate();
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void ganNgay(JDateChooser chooser, LocalDate date) {
        chooser.setDate(date == null ? null
                : java.util.Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * Đọc số từ ô nhập; để trống trả về null; sai định dạng ném
     * NumberFormatException.
     */
    private BigDecimal docSo(JTextField field) {
        String s = field.getText().trim();
        return s.isEmpty() ? null : new BigDecimal(s.replace(',', '.'));
    }

    // ---------- Thành phần giao diện ----------
    private JLabel nhan(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_TEXT);
        l.setForeground(NAVY);
        return l;
    }

    private JTextField oNhap(int width) {
        JTextField t = new JTextField();
        t.setFont(F_TEXT);
        kichThuoc(t, width, 30);
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(2, 6, 2, 6)));
        return t;
    }

    private void kichThuoc(JComponent c, int w, int h) {
        Dimension d = new Dimension(w, h);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
    }

    private JButton nutChinh(String text) {
        return nutChinh(text, BLUE);
    }

    private JButton nutChinh(String text, Color nen) {
        return taoNut(text, F_BOLD, Color.WHITE, nen, new Color(20, 100, 200));
    }

    private JButton nutThuong(String text) {
        return taoNut(text, F_TEXT, NAVY, Color.WHITE, BORDER);
    }

    private JButton taoNut(String text, Font font, Color chu, Color nen, Color vien) {
        JButton b = new JButton(text);
        b.setFont(font);
        b.setForeground(chu);
        b.setBackground(nen);

        // Buộc nút hiển thị màu nền đã thiết lập
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.setFocusPainted(false);

        b.setBorder(BorderFactory.createCompoundBorder(new LineBorder(vien, 1), new EmptyBorder(7, 15, 7, 15)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ---------- Thông báo ----------
    private void canhBao(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void thongBao(String message, int loai) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", loai);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BaseFrame frame = new BaseFrame();
            frame.setContent(new FrmKhuyenMai());
            frame.setVisible(true);
        });
    }
}
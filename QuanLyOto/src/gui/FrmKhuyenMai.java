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
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.IntConsumer;

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
import javax.swing.table.JTableHeader;
import com.toedter.calendar.JDateChooser;
import controller.KhuyenMaiController;
import dao.ChuongTrinhKhuyenMaiDAO.KhuyenMai;

import static dao.ChuongTrinhKhuyenMaiDAO.TRUONG_TEN;
import static dao.ChuongTrinhKhuyenMaiDAO.TRUONG_TILE;
import static dao.ChuongTrinhKhuyenMaiDAO.TT_CHUA;
import static dao.ChuongTrinhKhuyenMaiDAO.TT_DANG;
import static dao.ChuongTrinhKhuyenMaiDAO.TT_HET;
import static dao.ChuongTrinhKhuyenMaiDAO.TT_TAM;
import static dao.ChuongTrinhKhuyenMaiDAO.TT_TAT_CA;
import static dao.ChuongTrinhKhuyenMaiDAO.tinhTrangThai;

/**
 * View: chỉ dựng giao diện và hiển thị dữ liệu. Sự kiện của nút/bảng/hộp thoại
 * được KhuyenMaiController đăng ký qua các phương thức khiXxx(...); không truy
 * cập CSDL và không có nghiệp vụ ở đây.
 */
public class FrmKhuyenMai extends JPanel {
    private static final Color BLUE_DAM = new Color(25, 118, 210);
    private static final Color NAVY = new Color(20, 59, 105);
    private static final Color NAVY_DAM = new Color(25, 55, 90);
    private static final Color BG = new Color(244, 249, 255);
    private static final Color BORDER = new Color(205, 224, 246);
    private static final Font F_TEXT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    // Chiều rộng chung của các ô trong khung tìm kiếm
    private static final int W_O_NHAP = 200;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String TT_CHUA_XAC_DINH = "—"; // ngày chưa đủ / không hợp lệ

    // Khung tìm kiếm
    private JTextField txtTimMa, txtTimTen, txtTileTu, txtTileDen;
    private JDateChooser dchLocTuNgay, dchLocDenNgay;
    private JComboBox<String> cmbTimTrangThai;

    // Các nút (sự kiện do controller đăng ký)
    private final JButton btnTim = nutChinh("Tìm kiếm");
    private final JButton btnLamMoiLoc = nutThuong("Làm mới");
    private final JButton btnThemMoi = nutChinh("+  Thêm mới");

    // Bảng
    private JTable tblKhuyenMai;
    private DefaultTableModel modelKhuyenMai;
    private JLabel lblTong;

    private final KhuyenMaiController controller;

    // 2. KHỞI TẠO
    public FrmKhuyenMai() {
        setLayout(new BorderLayout(0, 8));
        setBackground(BG);

        JPanel pnlBody = new JPanel(new BorderLayout(0, 12));
        pnlBody.setBackground(BG);
        pnlBody.setBorder(new EmptyBorder(0, 16, 16, 16));
        pnlBody.add(buildSearchPanel(), BorderLayout.NORTH);
        pnlBody.add(buildTablePanel(), BorderLayout.CENTER);

        add(buildTitle(), BorderLayout.NORTH);
        add(pnlBody, BorderLayout.CENTER);

        // Controller đăng ký sự kiện và tải dữ liệu lần đầu
        controller = new KhuyenMaiController(this);
    }

    // 3. ĐĂNG KÝ SỰ KIỆN (controller gọi)
    public void khiNhanTim(Runnable r) {
        btnTim.addActionListener(e -> r.run());
    }

    public void khiNhanLamMoi(Runnable r) {
        btnLamMoiLoc.addActionListener(e -> r.run());
    }

    public void khiNhanThemMoi(Runnable r) {
        btnThemMoi.addActionListener(e -> r.run());
    }

    // Nhấn Enter trong các ô nhập tìm kiếm
    public void khiNhapEnter(Runnable r) {
        for (JTextField txt : new JTextField[] { txtTimMa, txtTimTen, txtTileTu, txtTileDen }) {
            txt.addActionListener(e -> r.run());
        }
    }

    // Nhấn vào dòng -> truyền chỉ số dòng trên bảng
    public void khiChonDong(IntConsumer c) {
        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhuyenMai.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    c.accept(row);
                }
            }
        });
    }

    // 4. ĐỌC DỮ LIỆU TỪ KHUNG TÌM KIẾM (cho controller)
    public String layMaLoc() {
        return txtTimMa.getText().trim();
    }

    public String layTenLoc() {
        return txtTimTen.getText().trim();
    }

    public String layTrangThaiLoc() {
        return (String) cmbTimTrangThai.getSelectedItem();
    }

    public String layTileTu() {
        return txtTileTu.getText();
    }

    public String layTileDen() {
        return txtTileDen.getText();
    }

    public LocalDate layLocTuNgay() {
        return layNgay(dchLocTuNgay);
    }

    public LocalDate layLocDenNgay() {
        return layNgay(dchLocDenNgay);
    }

    // 5. HIỂN THỊ (controller gọi)
    public void xoaBoLoc() {
        txtTimMa.setText("");
        txtTimTen.setText("");
        cmbTimTrangThai.setSelectedIndex(0);
        txtTileTu.setText("");
        txtTileDen.setText("");
        dchLocTuNgay.setDate(null);
        dchLocDenNgay.setDate(null);
    }

    public void hienThiDanhSach(List<KhuyenMai> ds, int tong) {
        modelKhuyenMai.setRowCount(0);
        for (KhuyenMai km : ds) {
            modelKhuyenMai.addRow(new Object[] {
                    km.maKM,
                    km.tenKM,
                    km.tileGiam.stripTrailingZeros().toPlainString() + "%",
                    km.ngayBatDau.format(DF),
                    km.ngayKetThuc.format(DF),
                    km.dieuKienApDung == null ? "" : km.dieuKienApDung.replace('\n', ' '),
                    km.trangThai });
        }
        lblTong.setText("Tổng số bản ghi: " + ds.size() + " / " + tong);
        tblKhuyenMai.clearSelection();
    }

    // km == null: hộp thoại thêm mới (maMoi là mã vừa sinh); khác null: xem / sửa /
    // xóa
    public DlgKhuyenMai taoHopThoai(KhuyenMai km, String maMoi) {
        return new DlgKhuyenMai(km, maMoi);
    }

    // Thông báo
    public void canhBao(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    public void thongTin(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void baoLoiCSDL(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
    }

    // 6. XÂY DỰNG GIAO DIỆN CHÍNH
    // Tiêu đề
    private JPanel buildTitle() {
        JPanel pnlTieuDe = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        pnlTieuDe.setBackground(BG);

        JLabel lblTieuDe = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTieuDe.setForeground(NAVY);
        pnlTieuDe.add(lblTieuDe);
        return pnlTieuDe;
    }

    private JPanel khung(String tieuDe, Component benPhai) {
        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(10, 14, 12, 14)));

        JLabel lblHeading = new JLabel(tieuDe);
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeading.setForeground(NAVY);

        JPanel pnlDau = new JPanel(new BorderLayout());
        pnlDau.setOpaque(false);
        pnlDau.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlDau.add(lblHeading, BorderLayout.WEST);
        if (benPhai != null) {
            pnlDau.add(benPhai, BorderLayout.EAST);
        }
        pnlKhung.add(pnlDau, BorderLayout.NORTH);
        return pnlKhung;
    }

    // Khung tìm kiếm / lọc
    private JPanel buildSearchPanel() {
        JPanel pnlTimKiem = khung("Tìm kiếm / Lọc", null);

        txtTimMa = oNhap(W_O_NHAP);
        txtTimTen = oNhap(W_O_NHAP);
        cmbTimTrangThai = new JComboBox<>(new String[] { TT_TAT_CA, TT_DANG, TT_CHUA, TT_HET, TT_TAM });
        kichThuoc(cmbTimTrangThai, W_O_NHAP, 30);
        cmbTimTrangThai.setFont(F_TEXT);
        cmbTimTrangThai.setBackground(Color.WHITE);
        txtTileTu = oNhap(90);
        txtTileDen = oNhap(90);

        // Hai ô ngày lọc chỉ chọn từ hôm nay trở đi
        Date homNay = Date.valueOf(LocalDate.now());
        dchLocTuNgay = oChonNgay(W_O_NHAP);
        dchLocDenNgay = oChonNgay(W_O_NHAP);
        dchLocTuNgay.setMinSelectableDate(homNay);
        dchLocDenNgay.setMinSelectableDate(homNay);

        // "Đến ngày" luôn >= "Hiệu lực từ ngày"; xóa "từ ngày" thì quay về mốc hôm nay
        dchLocTuNgay.addPropertyChangeListener("date", e -> {
            LocalDate tu = layNgay(dchLocTuNgay);
            LocalDate den = layNgay(dchLocDenNgay);

            dchLocDenNgay.setMinSelectableDate(Date.valueOf(tu != null ? tu : LocalDate.now()));
            if (tu != null && den != null && den.isBefore(tu)) {
                dchLocDenNgay.setDate(null);
            }
        });

        // Lưới nhãn + ô nhập: cột chẵn là nhãn, cột lẻ là ô nhập
        Component[][] luoi = {
                { nhan("Mã khuyến mãi:"), txtTimMa, nhan("Tên khuyến mãi:"), txtTimTen,
                        nhan("Trạng thái:"), cmbTimTrangThai },
                { nhan("Tỉ lệ giảm (%):"), taoOTileGiam(), nhan("Hiệu lực từ ngày:"), dchLocTuNgay,
                        nhan("Đến ngày:"), dchLocDenNgay } };

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
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
                pnlForm.add(luoi[y][x], c);
            }
        }

        // Hai nút ở hàng riêng bên dưới các trường tìm kiếm
        GridBagConstraints cNut = new GridBagConstraints();
        cNut.gridx = 0;
        cNut.gridy = luoi.length;
        cNut.gridwidth = 6;
        cNut.weightx = 1;
        cNut.anchor = GridBagConstraints.EAST;
        cNut.fill = GridBagConstraints.HORIZONTAL;
        cNut.insets = new Insets(8, 0, 0, 0);
        pnlForm.add(taoNutTimKiem(), cNut);

        pnlTimKiem.add(pnlForm, BorderLayout.CENTER);
        return pnlTimKiem;
    }

    // Ô lọc tỉ lệ giảm: hai ô giãn đều, dấu "–" ở giữa
    private JPanel taoOTileGiam() {
        JPanel pnlTile = new JPanel(new GridBagLayout());
        pnlTile.setOpaque(false);

        JLabel lblGach = nhan("–");
        lblGach.setHorizontalAlignment(SwingConstants.CENTER);
        lblGach.setPreferredSize(new Dimension(24, 30));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        pnlTile.add(txtTileTu, c);
        c.weightx = 0;
        pnlTile.add(lblGach, c);
        c.weightx = 1;
        pnlTile.add(txtTileDen, c);

        kichThuoc(pnlTile, W_O_NHAP, 30);
        return pnlTile;
    }

    private JPanel taoNutTimKiem() {
        btnLamMoiLoc.setForeground(NAVY_DAM);
        kichThuoc(btnTim, 130, 34);
        kichThuoc(btnLamMoiLoc, 130, 34);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnTim);
        pnlNut.add(btnLamMoiLoc);
        return pnlNut;
    }

    // Danh sách khuyến mãi
    private JPanel buildTablePanel() {
        JPanel pnlDanhSach = khung("Danh sách khuyến mãi", btnThemMoi);
        pnlDanhSach.add(taoBang(), BorderLayout.CENTER);
        pnlDanhSach.add(taoChanBang(), BorderLayout.SOUTH);
        return pnlDanhSach;
    }

    private JScrollPane taoBang() {
        String[] cot = { "Mã KM", "Tên chương trình khuyến mãi", "Tỉ lệ giảm", "Ngày bắt đầu", "Ngày kết thúc",
                "Điều kiện áp dụng", "Trạng thái" };
        modelKhuyenMai = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKhuyenMai = new JTable(modelKhuyenMai);
        tblKhuyenMai.setRowHeight(34);
        tblKhuyenMai.setFont(F_TEXT);
        tblKhuyenMai.setSelectionBackground(new Color(214, 235, 255));
        tblKhuyenMai.setSelectionForeground(Color.BLACK);
        tblKhuyenMai.setGridColor(new Color(225, 235, 246));
        tblKhuyenMai.setShowVerticalLines(false);
        tblKhuyenMai.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblKhuyenMai.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JTableHeader tbhKhuyenMai = tblKhuyenMai.getTableHeader();
        tbhKhuyenMai.setFont(F_BOLD);
        tbhKhuyenMai.setBackground(new Color(236, 245, 255));
        tbhKhuyenMai.setForeground(NAVY);
        tbhKhuyenMai.setReorderingAllowed(false);

        // Độ rộng cột
        int[] rong = { 80, 260, 85, 105, 105, 300, 115 };
        for (int i = 0; i < rong.length; i++) {
            tblKhuyenMai.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        // Căn giữa các cột mã, tỉ lệ, ngày; cột trạng thái tô màu
        DefaultTableCellRenderer rdrGiua = new DefaultTableCellRenderer();
        rdrGiua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[] { 0, 2, 3, 4 }) {
            tblKhuyenMai.getColumnModel().getColumn(i).setCellRenderer(rdrGiua);
        }
        tblKhuyenMai.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());

        JScrollPane scrBang = new JScrollPane(tblKhuyenMai);
        scrBang.setBorder(new LineBorder(BORDER));
        scrBang.getViewport().setBackground(Color.WHITE);
        return scrBang;
    }

    private JPanel taoChanBang() {
        lblTong = new JLabel("Tổng số bản ghi: 0");
        lblTong.setFont(F_TEXT);
        lblTong.setForeground(NAVY);

        JPanel pnlChan = new JPanel(new BorderLayout());
        pnlChan.setBackground(Color.WHITE);
        pnlChan.setBorder(new EmptyBorder(8, 0, 0, 0));
        pnlChan.add(lblTong, BorderLayout.WEST);
        return pnlChan;
    }

    // Cột trạng thái: chữ đậm, căn giữa, màu theo từng trạng thái
    private static class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row,
                int col) {
            super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(F_BOLD);
            setForeground(mauTrangThai(String.valueOf(val)));
            return this;
        }
    }

    private static Color mauTrangThai(String trangThai) {
        if (TT_DANG.equals(trangThai))
            return new Color(22, 140, 70);
        if (TT_CHUA.equals(trangThai))
            return new Color(25, 110, 200);
        if (TT_TAM.equals(trangThai))
            return new Color(210, 120, 10);
        return new Color(140, 140, 140); // Hết hạn / chưa xác định
    }

    // 7. HỘP THOẠI THÔNG TIN KHUYẾN MÃI
    // Chỉ dựng giao diện; nút Lưu / Xóa / Đóng do controller đăng ký.
    public class DlgKhuyenMai extends JDialog {
        private final KhuyenMai km;
        private final boolean themMoi;
        private final String maMoi;

        private final JTextField txtMa = oNhap(310);
        private final JTextField txtTen = oNhap(310);
        private final JTextField txtTile = oNhap(135);
        private final JDateChooser dchBatDau = oChonNgay(150);
        private final JDateChooser dchKetThuc = oChonNgay(150);
        private final JTextArea txaDieuKien = new JTextArea(4, 28);
        private final JCheckBox chkTamNgung = new JCheckBox("Tạm ngưng áp dụng");
        private final JLabel lblTrangThai = new JLabel();

        private JButton btnLuu, btnXoa, btnDong;

        DlgKhuyenMai(KhuyenMai km, String maMoi) {
            super(SwingUtilities.getWindowAncestor(FrmKhuyenMai.this),
                    km == null ? "Thêm khuyến mãi" : "Thông tin khuyến mãi",
                    Dialog.ModalityType.APPLICATION_MODAL);
            this.km = km;
            this.themMoi = (km == null);
            this.maMoi = maMoi;
            setResizable(false);

            napDuLieu();
            ganSuKienHienThi();
            capNhatTrangThai();
            dungGiaoDien();

            pack();
            setLocationRelativeTo(FrmKhuyenMai.this);
            txtTen.requestFocusInWindow();
        }

        // Đăng ký sự kiện (controller gọi)
        public void khiNhanLuu(Runnable r) {
            btnLuu.addActionListener(e -> r.run());
        }

        public void khiNhanXoa(Runnable r) {
            btnXoa.addActionListener(e -> r.run());
        }

        public void khiNhanDong(Runnable r) {
            btnDong.addActionListener(e -> r.run());
        }

        // Đọc dữ liệu từ form (controller gọi)
        public String layMa() {
            return txtMa.getText();
        }

        public String layTen() {
            return txtTen.getText();
        }

        public String layTile() {
            return txtTile.getText();
        }

        public LocalDate layNgayBatDau() {
            return layNgay(dchBatDau);
        }

        public LocalDate layNgayKetThuc() {
            return layNgay(dchKetThuc);
        }

        public String layDieuKien() {
            return txaDieuKien.getText();
        }

        public boolean laTamNgung() {
            return chkTamNgung.isSelected();
        }

        // Hiển thị (controller gọi)
        public void hienThi() {
            setVisible(true);
        }

        public void dong() {
            dispose();
        }

        public void canhBao(String message) {
            JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
        }

        // Đưa con trỏ về ô sai (truong có thể null)
        public void datFocus(String truong) {
            if (TRUONG_TEN.equals(truong)) {
                txtTen.requestFocus();
            } else if (TRUONG_TILE.equals(truong)) {
                txtTile.requestFocus();
            }
        }

        public boolean xacNhanXoa(String maKM) {
            return JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa khuyến mãi " + maKM + "?\n",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
        }

        // Dữ liệu ban đầu
        private void napDuLieu() {
            txaDieuKien.setFont(F_TEXT);
            txaDieuKien.setLineWrap(true);
            txaDieuKien.setWrapStyleWord(true);
            chkTamNgung.setFont(F_TEXT);
            chkTamNgung.setOpaque(false);
            lblTrangThai.setFont(F_BOLD);
            txtMa.setEditable(false);
            txtMa.setBackground(new Color(238, 242, 247));

            if (themMoi) {
                LocalDate homNay = LocalDate.now();
                txtMa.setText(maMoi);
                ganNgay(dchBatDau, homNay);
                dchBatDau.setMinSelectableDate(Date.valueOf(homNay)); // không chọn ngày bắt đầu trước hôm nay
            } else {
                txtMa.setText(km.maKM);
                txtTen.setText(km.tenKM);
                txtTile.setText(km.tileGiam.stripTrailingZeros().toPlainString());
                ganNgay(dchBatDau, km.ngayBatDau);
                ganNgay(dchKetThuc, km.ngayKetThuc);
                txaDieuKien.setText(km.dieuKienApDung == null ? "" : km.dieuKienApDung);
                chkTamNgung.setSelected(TT_TAM.equals(km.trangThai));
            }
            txaDieuKien.setCaretPosition(0);

            // Ngày kết thúc không được trước ngày bắt đầu (ngày bắt đầu luôn có giá trị ban
            // đầu)
            dchKetThuc.setMinSelectableDate(Date.valueOf(layNgay(dchBatDau)));
        }

        // Sự kiện chỉ phục vụ hiển thị (không có nghiệp vụ nên để trong view)
        private void ganSuKienHienThi() {
            chkTamNgung.addActionListener(e -> capNhatTrangThai());
            dchKetThuc.addPropertyChangeListener("date", e -> capNhatTrangThai());
            dchBatDau.addPropertyChangeListener("date", e -> {
                LocalDate bd = layNgay(dchBatDau);
                LocalDate kt = layNgay(dchKetThuc);

                // Ngày kết thúc chỉ chọn được khi đã có ngày bắt đầu
                dchKetThuc.setEnabled(bd != null);
                if (bd == null) {
                    dchKetThuc.setDate(null);
                } else {
                    dchKetThuc.setMinSelectableDate(Date.valueOf(bd));
                    if (kt != null && kt.isBefore(bd)) {
                        dchKetThuc.setDate(null);
                    }
                }
                capNhatTrangThai();
            });
        }

        // Trạng thái hiển thị do hệ thống tự tính theo ngày (trừ khi tạm ngưng)
        private void capNhatTrangThai() {
            LocalDate bd = layNgay(dchBatDau);
            LocalDate kt = layNgay(dchKetThuc);

            String trangThai;
            if (chkTamNgung.isSelected()) {
                trangThai = TT_TAM;
            } else if (bd == null || kt == null || kt.isBefore(bd)) {
                trangThai = TT_CHUA_XAC_DINH;
            } else {
                trangThai = tinhTrangThai(bd, kt);
            }
            lblTrangThai.setText(trangThai);
            lblTrangThai.setForeground(mauTrangThai(trangThai));
        }

        // Giao diện
        private void dungGiaoDien() {
            JLabel lblTieuDe = new JLabel(themMoi ? "Thêm khuyến mãi mới" : "Thông tin khuyến mãi");
            lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTieuDe.setForeground(NAVY);
            lblTieuDe.setOpaque(true);
            lblTieuDe.setBackground(new Color(236, 245, 255));
            lblTieuDe.setBorder(new EmptyBorder(10, 10, 10, 10));

            btnLuu = nutChinh(themMoi ? "Lưu" : "Lưu thay đổi");
            btnXoa = nutThuong("Xóa");
            btnDong = nutThuong("Đóng");
            btnXoa.setForeground(new Color(200, 50, 50));
            btnXoa.setVisible(!themMoi);

            JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            pnlNut.setBackground(Color.WHITE);
            pnlNut.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
            pnlNut.add(btnLuu);
            pnlNut.add(btnXoa);
            pnlNut.add(btnDong);

            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            add(lblTieuDe, BorderLayout.NORTH);
            add(taoForm(), BorderLayout.CENTER);
            add(pnlNut, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(btnLuu);
        }

        private JPanel taoForm() {
            JPanel pnlTile = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            pnlTile.setOpaque(false);
            pnlTile.add(txtTile);
            pnlTile.add(nhan("%  (0 < tỉ lệ ≤ 100)"));

            JPanel pnlNgay = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            pnlNgay.setOpaque(false);
            pnlNgay.add(dchBatDau);
            pnlNgay.add(nhan("đến"));
            pnlNgay.add(dchKetThuc);

            JPanel pnlTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
            pnlTrangThai.setOpaque(false);
            pnlTrangThai.add(lblTrangThai);
            pnlTrangThai.add(chkTamNgung);

            JScrollPane scrDieuKien = new JScrollPane(txaDieuKien);
            scrDieuKien.setPreferredSize(new Dimension(310, 90));

            String[] nhanTruong = { "Mã khuyến mãi:", "Tên khuyến mãi:", "Tỉ lệ giảm:", "Thời gian áp dụng:",
                    "Điều kiện áp dụng:", "Trạng thái:" };
            Component[] oTruong = { txtMa, txtTen, pnlTile, pnlNgay, scrDieuKien, pnlTrangThai };

            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setBackground(Color.WHITE);
            pnlForm.setBorder(new EmptyBorder(16, 20, 8, 20));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(6, 0, 6, 12);
            for (int i = 0; i < nhanTruong.length; i++) {
                c.gridy = i;
                c.gridx = 0;
                c.anchor = (i == 4) ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
                pnlForm.add(nhan(nhanTruong[i]), c);
                c.gridx = 1;
                c.anchor = GridBagConstraints.WEST;
                pnlForm.add(oTruong[i], c);
            }
            return pnlForm;
        }
    }

    // 8. TIỆN ÍCH GIAO DIỆN
    // Ngày
    private JDateChooser oChonNgay(int width) {
        JDateChooser dch = new JDateChooser();
        dch.setDateFormatString("dd/MM/yyyy");
        dch.setFont(F_TEXT);
        kichThuoc(dch, width, 30);
        return dch;
    }

    private LocalDate layNgay(JDateChooser dch) {
        java.util.Date date = dch.getDate();
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void ganNgay(JDateChooser dch, LocalDate date) {
        dch.setDate(date == null ? null
                : java.util.Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    // Thành phần giao diện
    private JLabel nhan(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(F_TEXT);
        lbl.setForeground(NAVY);
        return lbl;
    }

    private JTextField oNhap(int width) {
        JTextField txt = new JTextField();
        txt.setFont(F_TEXT);
        kichThuoc(txt, width, 30);
        txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(2, 6, 2, 6)));
        return txt;
    }

    private void kichThuoc(JComponent comp, int w, int h) {
        Dimension d = new Dimension(w, h);
        comp.setPreferredSize(d);
        comp.setMinimumSize(d);
    }

    private JButton nutChinh(String text) {
        return taoNut(text, F_BOLD, Color.WHITE, BLUE_DAM, new Color(20, 100, 200));
    }

    private JButton nutThuong(String text) {
        return taoNut(text, F_TEXT, NAVY, Color.WHITE, BORDER);
    }

    private JButton taoNut(String text, Font font, Color chu, Color nen, Color vien) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setForeground(chu);
        btn.setBackground(nen);

        // Buộc nút hiển thị màu nền đã thiết lập
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);

        btn.setBorder(BorderFactory.createCompoundBorder(new LineBorder(vien, 1), new EmptyBorder(7, 15, 7, 15)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BaseFrame frame = new BaseFrame();
            frame.setContent(new FrmKhuyenMai());
            frame.setVisible(true);
        });
    }
}
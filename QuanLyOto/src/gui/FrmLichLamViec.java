package gui;

import controller.LichLamViecController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện quản lý lịch phân công ca theo tuần và điểm danh nhân viên.
 */
public class FrmLichLamViec extends JPanel {

    private static final String CHUA_PHAN_CONG = "Chưa phân công";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Bảng dữ liệu ─────────────────────────────────────────────────────────
    private final DefaultTableModel modelLichTuan = new DefaultTableModel(
            new Object[]{"Ca làm việc", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5",
                    "Thứ 6", "Thứ 7", "Chủ nhật"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel modelDiemDanh = new DefaultTableModel(
            new Object[]{"Mã nhân viên", "Họ và tên", "Có mặt"}, 0) {
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 2 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2;
        }
    };
    private final JTable tblLichTuan = new JTable(modelLichTuan);
    private final JTable tblDiemDanh = new JTable(modelDiemDanh);

    // ── Các thành phần nhập liệu ──────────────────────────────────────────────
    private final JComboBox<String> cboNhanVien  = new JComboBox<>();
    private final JComboBox<String> cboCaLamViec = new JComboBox<>();

    // ── Các nút bấm ──────────────────────────────────────────────────────────
    private final JButton btnThem       = new JButton("Thêm phân công");
    private final JButton btnSua        = new JButton("Sửa phân công");
    private final JButton btnXoa        = new JButton("Xóa phân công");
    private final JButton btnLamMoi     = new JButton("Làm mới");
    private final JButton btnTuanTruoc  = new JButton("‹ Tuần trước");
    private final JButton btnTuanKeTiep = new JButton("Tuần kế tiếp ›");

    // ── Các nhãn thông tin ───────────────────────────────────────────────────
    private final JLabel lblCaHienTai        = new JLabel("");
    private final JLabel lblTrangThaiDiemDanh = new JLabel("");
    private final JLabel lblTuanHienThi       = new JLabel("");

    // ── Các thành phần khác ──────────────────────────────────────────────────
    private final DatePickerPanel pnlChonNgayLamViec = new DatePickerPanel(LocalDate.now());
    private final LichCaCellRenderer rendererLichCa  = new LichCaCellRenderer();
    private final LichLamViecController controller     = new LichLamViecController(this);
    private final LocalDate ngayDauTuanBanDau = LocalDate.now().with(DayOfWeek.MONDAY);
    private LocalDate ngayDauTuan = ngayDauTuanBanDau;
    private JScrollPane scrNoiDung;

    public FrmLichLamViec() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setOpaque(true);

        // Áp dụng style cho combobox
        cboNhanVien.setFont(UIConfig.FONT_REGULAR);
        cboCaLamViec.setFont(UIConfig.FONT_REGULAR);

        // Áp dụng style cho các nút
        styleButtonPrimary(btnThem);
        styleButtonSuccess(btnSua);
        styleButtonDanger(btnXoa);
        styleButtonSecondary(btnLamMoi);
        styleButtonSecondary(btnTuanTruoc);
        styleButtonSecondary(btnTuanKeTiep);

        // Áp dụng style cho nhãn
        lblCaHienTai.setFont(UIConfig.FONT_BOLD);
        lblCaHienTai.setForeground(UIConfig.COLOR_MUTED);
        lblTrangThaiDiemDanh.setFont(UIConfig.FONT_REGULAR);
        lblTrangThaiDiemDanh.setForeground(UIConfig.COLOR_MUTED);
        lblTuanHienThi.setFont(UIConfig.FONT_REGULAR);
        lblTuanHienThi.setForeground(UIConfig.COLOR_MUTED);

        cauHinhBangLich();
        cauHinhBangDiemDanh();
        capNhatHienThiTuan(ngayDauTuanBanDau, LocalDate.now());

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(createFormPhanCong(), BorderLayout.NORTH);
        pnlNoiDung.add(createLichTuanCard(), BorderLayout.CENTER);
        pnlNoiDung.add(createDiemDanhCard(), BorderLayout.SOUTH);

        scrNoiDung = new JScrollPane(pnlNoiDung);
        scrNoiDung.setBorder(null);
        scrNoiDung.setOpaque(false);
        scrNoiDung.getViewport().setOpaque(false);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);

        add(createPageHeader("LỊCH CA LÀM VIỆC",
                "Theo dõi lịch tuần, ca đang diễn ra và điểm danh nhân viên",
                scrNoiDung), BorderLayout.CENTER);
        controller.khoiTao();
    }

    // ── Helper: style nút ────────────────────────────────────────────────────
    private void styleButtonPrimary(JButton btn) {
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(UIConfig.COLOR_PRIMARY);
        btn.setForeground(UIConfig.COLOR_SURFACE);
        btn.setOpaque(true); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setMargin(new Insets(6, 14, 6, 14));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void styleButtonSuccess(JButton btn) {
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(UIConfig.COLOR_SUCCESS);
        btn.setForeground(UIConfig.COLOR_SURFACE);
        btn.setOpaque(true); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setMargin(new Insets(6, 14, 6, 14));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void styleButtonDanger(JButton btn) {
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(UIConfig.COLOR_DANGER);
        btn.setForeground(UIConfig.COLOR_SURFACE);
        btn.setOpaque(true); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setMargin(new Insets(6, 14, 6, 14));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void styleButtonSecondary(JButton btn) {
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(UIConfig.COLOR_SECONDARY);
        btn.setForeground(UIConfig.COLOR_TEXT);
        btn.setOpaque(true); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setMargin(new Insets(6, 14, 6, 14));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    // ── Helper: tạo card panel ───────────────────────────────────────────────
    private JPanel createCardPanel(java.awt.LayoutManager layout) {
        JPanel pnl = new JPanel(layout);
        pnl.setBackground(UIConfig.COLOR_SURFACE);
        pnl.setOpaque(true);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return pnl;
    }

    // ── Helper: tạo nhãn tiêu đề section ─────────────────────────────────────
    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConfig.FONT_SECTION);
        lbl.setForeground(UIConfig.COLOR_TITLE);
        return lbl;
    }

    // ── Helper: tạo header trang ─────────────────────────────────────────────
    private JPanel createPageHeader(String title, String subtitle, JScrollPane content) {
        JPanel pnlPage = new JPanel(new BorderLayout(0, 14));
        pnlPage.setOpaque(false);
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 4));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSubtitle, BorderLayout.CENTER);
        pnlPage.add(pnlHeader, BorderLayout.NORTH);
        pnlPage.add(content, BorderLayout.CENTER);
        return pnlPage;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        controller.khiViewDuocHienThi();
    }

    @Override
    public void removeNotify() {
        controller.khiViewBiAn();
        super.removeNotify();
    }

    // ── Tạo form phân công ────────────────────────────────────────────────────
    private JPanel createFormPhanCong() {
        JPanel pnlPhanCong = createCardPanel(new BorderLayout(0, 10));
        pnlPhanCong.add(createSectionTitle("Thông tin phân công ca"), BorderLayout.NORTH);

        JPanel pnlFields = new JPanel(new java.awt.GridBagLayout());
        pnlFields.setOpaque(false);
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 8);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        JLabel lblNhanVien = new JLabel("Nhân viên:");
        lblNhanVien.setFont(UIConfig.FONT_REGULAR);
        lblNhanVien.setForeground(UIConfig.COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlFields.add(lblNhanVien, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        pnlFields.add(cboNhanVien, gbc);

        JLabel lblCa = new JLabel("Ca làm việc:");
        lblCa.setFont(UIConfig.FONT_REGULAR);
        lblCa.setForeground(UIConfig.COLOR_TEXT);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        pnlFields.add(lblCa, gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1;
        pnlFields.add(cboCaLamViec, gbc);

        JLabel lblNgay = new JLabel("Ngày làm việc:");
        lblNgay.setFont(UIConfig.FONT_REGULAR);
        lblNgay.setForeground(UIConfig.COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlFields.add(lblNgay, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        pnlFields.add(pnlChonNgayLamViec, gbc);
        gbc.gridwidth = 1;

        pnlPhanCong.add(pnlFields, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        pnlPhanCong.add(pnlButtons, BorderLayout.SOUTH);

        return pnlPhanCong;
    }

    // ── Tạo card lịch tuần ────────────────────────────────────────────────────
    private JPanel createLichTuanCard() {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.add(createSectionTitle("Lịch nhân viên theo ca trong tuần"), BorderLayout.WEST);
        JPanel pnlDieuHuong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlDieuHuong.setOpaque(false);
        pnlDieuHuong.add(btnTuanTruoc);
        pnlDieuHuong.add(btnTuanKeTiep);
        pnlHeader.add(pnlDieuHuong, BorderLayout.EAST);
        pnlCard.add(pnlHeader, BorderLayout.NORTH);

        JScrollPane scrLich = new JScrollPane(tblLichTuan);
        scrLich.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrLich.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(scrLich, BorderLayout.CENTER);

        JPanel pnlInfo = new JPanel(new BorderLayout(8, 0));
        pnlInfo.setOpaque(false);
        pnlInfo.add(lblTuanHienThi, BorderLayout.WEST);
        pnlInfo.add(lblCaHienTai, BorderLayout.EAST);
        pnlCard.add(pnlInfo, BorderLayout.SOUTH);

        return pnlCard;
    }

    // ── Tạo card điểm danh ────────────────────────────────────────────────────
    private JPanel createDiemDanhCard() {
        JPanel pnlDiemDanh = createCardPanel(new BorderLayout(0, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.add(createSectionTitle("Điểm danh nhân viên trong ca hiện tại"), BorderLayout.WEST);
        pnlHeader.add(lblTrangThaiDiemDanh, BorderLayout.EAST);
        pnlDiemDanh.add(pnlHeader, BorderLayout.NORTH);

        JScrollPane scrDiemDanh = new JScrollPane(tblDiemDanh);
        scrDiemDanh.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrDiemDanh.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        scrDiemDanh.setPreferredSize(new java.awt.Dimension(700, 112));
        pnlDiemDanh.add(scrDiemDanh, BorderLayout.CENTER);

        return pnlDiemDanh;
    }

    // ── Cấu hình bảng lịch tuần ───────────────────────────────────────────────
    private void cauHinhBangLich() {
        tblLichTuan.setAutoCreateRowSorter(false);
        tblLichTuan.setFont(UIConfig.FONT_REGULAR);
        tblLichTuan.setForeground(UIConfig.COLOR_TEXT);
        tblLichTuan.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblLichTuan.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblLichTuan.setCellSelectionEnabled(true);
        tblLichTuan.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblLichTuan.setRowHeight(62);
        tblLichTuan.setShowVerticalLines(true);
        tblLichTuan.setShowHorizontalLines(true);
        tblLichTuan.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblLichTuan.setFillsViewportHeight(true);
        tblLichTuan.setDefaultRenderer(Object.class, rendererLichCa);
        tblLichTuan.getColumnModel().getColumn(0).setPreferredWidth(145);
        tblLichTuan.getColumnModel().getColumn(0).setMinWidth(130);
        for (int column = 1; column < tblLichTuan.getColumnCount(); column++) {
            tblLichTuan.getColumnModel().getColumn(column).setPreferredWidth(135);
            tblLichTuan.getColumnModel().getColumn(column).setMinWidth(105);
        }
        tblLichTuan.getTableHeader().setReorderingAllowed(false);
        tblLichTuan.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblLichTuan.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblLichTuan.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblLichTuan.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));
    }

    // ── Cấu hình bảng điểm danh ──────────────────────────────────────────────
    private void cauHinhBangDiemDanh() {
        tblDiemDanh.setFont(UIConfig.FONT_REGULAR);
        tblDiemDanh.setForeground(UIConfig.COLOR_TEXT);
        tblDiemDanh.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblDiemDanh.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblDiemDanh.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblDiemDanh.setRowHeight(28);
        tblDiemDanh.setFillsViewportHeight(true);
        tblDiemDanh.getTableHeader().setReorderingAllowed(false);
        tblDiemDanh.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblDiemDanh.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblDiemDanh.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblDiemDanh.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 32));
        tblDiemDanh.getColumnModel().getColumn(0).setPreferredWidth(115);
        tblDiemDanh.getColumnModel().getColumn(1).setPreferredWidth(210);
        tblDiemDanh.getColumnModel().getColumn(2).setPreferredWidth(85);
        tblDiemDanh.getColumnModel().getColumn(2)
                .setCellEditor(new DefaultCellEditor(new javax.swing.JCheckBox()));
    }

    // ── Các phương thức public cho controller ─────────────────────────────────
    public void hienThiLuaChon(List<String> nhanVien, List<String> caLamViec) {
        cboNhanVien.removeAllItems();
        cboNhanVien.addItem("Chọn nhân viên");
        nhanVien.forEach(cboNhanVien::addItem);
        cboCaLamViec.removeAllItems();
        cboCaLamViec.addItem("Chọn ca làm việc");
        caLamViec.forEach(cboCaLamViec::addItem);
    }

    public void capNhatHienThiTuan(LocalDate ngayDauTuan, LocalDate ngayDuocChon) {
        this.ngayDauTuan = ngayDauTuan;
        pnlChonNgayLamViec.setSelectedDate(ngayDuocChon);
        Object[] tieuDeCot = new Object[8];
        tieuDeCot[0] = "Ca làm việc";
        String[] tenThu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        for (int index = 0; index < tenThu.length; index++) {
            LocalDate ngay = ngayDauTuan.plusDays(index);
            tieuDeCot[index + 1] = "<html><center>" + tenThu[index] + "<br>"
                    + ngay.format(DINH_DANG_NGAY) + "</center></html>";
        }
        modelLichTuan.setColumnIdentifiers(tieuDeCot);
        lblTuanHienThi.setText("Tuần: " + ngayDauTuan.format(DINH_DANG_NGAY) + " - "
                + ngayDauTuan.plusDays(6).format(DINH_DANG_NGAY));
    }

    public void hienThiLichTuan(Object[][] cacDong) {
        modelLichTuan.setRowCount(0);
        for (Object[] dong : cacDong) {
            modelLichTuan.addRow(dong);
        }
        tblLichTuan.setDefaultRenderer(Object.class, rendererLichCa);
        tblLichTuan.repaint();
    }

    public void hienThiDiemDanh(Object[][] cacDong, String thongBao) {
        modelDiemDanh.setRowCount(0);
        for (Object[] dong : cacDong) {
            modelDiemDanh.addRow(dong);
        }
        lblTrangThaiDiemDanh.setText(thongBao);
    }

    public void capNhatCaHienTai(String thongTin, boolean dangTrongCa, int chiSoCa, int cotNgay) {
        rendererLichCa.setCaHienTai(cotNgay >= 0 ? chiSoCa : -1, cotNgay);
        tblLichTuan.repaint();
        lblCaHienTai.setText(thongTin);
        lblCaHienTai.setForeground(dangTrongCa ? UIConfig.COLOR_SUCCESS : UIConfig.COLOR_MUTED);
    }

    public void capNhatTrangThaiNut(boolean ngayDuocSua, boolean coPhanCongDangChon) {
        btnThem.setEnabled(ngayDuocSua);
        btnSua.setEnabled(ngayDuocSua && coPhanCongDangChon);
        btnXoa.setEnabled(ngayDuocSua && coPhanCongDangChon);
    }

    public void chonNhanVien(String maNV) {
        cboNhanVien.setSelectedIndex(0);
        for (int index = 1; index < cboNhanVien.getItemCount(); index++) {
            if (cboNhanVien.getItemAt(index).startsWith(maNV + " - ")) {
                cboNhanVien.setSelectedIndex(index);
                break;
            }
        }
    }

    public void datLaiLuaChon() {
        cboNhanVien.setSelectedIndex(0);
        cboCaLamViec.setSelectedIndex(0);
    }

    public void thongBao(String noiDung, String tieuDe, int loai) {
        JOptionPane.showMessageDialog(this, noiDung, tieuDe, loai);
    }

    public boolean xacNhanXoa() {
        return JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phân công đã chọn?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public int chonPhanCong(List<String> luaChon) {
        Object luaChonNhanVien = JOptionPane.showInputDialog(this,
                "Chọn phân công cần thao tác:", "Chọn phân công",
                JOptionPane.QUESTION_MESSAGE, null, luaChon.toArray(), luaChon.get(0));
        return luaChonNhanVien == null ? -1 : luaChon.indexOf(luaChonNhanVien.toString());
    }

    public void hienThiLoiCSDL(String thongBao, Exception exception) {
        thongBao(thongBao + "\n" + exception.getMessage(),
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    public void hienThiLoi(String thongBao) {
        thongBao(thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void setTrangThai(String thongBao) {
        lblTrangThaiDiemDanh.setText(thongBao);
    }

    public LocalDate getNgayDuocChon() {
        return pnlChonNgayLamViec.getSelectedDate();
    }

    public int getChiSoCaDuocChon() {
        return cboCaLamViec.getSelectedIndex() - 1;
    }

    public void setSelectedShiftIndex(int chiSoCa) {
        cboCaLamViec.setSelectedIndex(chiSoCa + 1);
    }

    public String getMaNhanVienDuocChon() {
        String selected = (String) cboNhanVien.getSelectedItem();
        if (selected == null || cboNhanVien.getSelectedIndex() <= 0) {
            return null;
        }
        int separator = selected.indexOf(" - ");
        return separator > 0 ? selected.substring(0, separator) : null;
    }

    // ── Getter cho controller ─────────────────────────────────────────────────
    public JTable getTblLichTuan()              { return tblLichTuan; }
    public JTable getTblDiemDanh()              { return tblDiemDanh; }
    public DefaultTableModel getModelDiemDanh() { return modelDiemDanh; }
    public JButton getBtnThem()                 { return btnThem; }
    public JButton getBtnSua()                  { return btnSua; }
    public JButton getBtnXoa()                  { return btnXoa; }
    public JButton getBtnLamMoi()               { return btnLamMoi; }
    public JButton getBtnTuanTruoc()            { return btnTuanTruoc; }
    public JButton getBtnTuanKeTiep()           { return btnTuanKeTiep; }
    public DatePickerPanel getPnlChonNgayLamViec() { return pnlChonNgayLamViec; }
    public JScrollPane getScrNoiDung()          { return scrNoiDung; }

    // ── Lớp nội: DatePickerPanel ──────────────────────────────────────────────
    public static final class DatePickerPanel extends JPanel {
        private static final String[] TEN_NGAY_TRONG_TUAN = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        private static final Locale LOCALE_VI = Locale.forLanguageTag("vi-VN");
        private static final DateTimeFormatter DINH_DANG_NGAY_DAY_DU =
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", LOCALE_VI);
        private static final DateTimeFormatter DINH_DANG_THANG =
                DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_VI);

        private final JTextField txtNgay      = new JTextField(18);
        private final JButton    btnMoLich    = new JButton("▣");
        private final JButton    btnThangTruoc = new JButton("‹");
        private final JButton    btnThangSau   = new JButton("›");
        private final JButton[]  btnNgay       = new JButton[42];
        private final JPopupMenu popLich       = new JPopupMenu();
        private final JPanel     pnlLich       = new JPanel(new BorderLayout(0, 8));
        private final JLabel     lblThang      = new JLabel("");
        private LocalDate ngayDuocChon;
        private LocalDate thangDangHienThi;

        private DatePickerPanel(LocalDate ngayBanDau) {
            super(new BorderLayout(6, 0));
            ngayDuocChon      = ngayBanDau;
            thangDangHienThi  = ngayBanDau.withDayOfMonth(1);
            setOpaque(false);

            // Style txtNgay
            txtNgay.setFont(UIConfig.FONT_REGULAR);
            txtNgay.setForeground(UIConfig.COLOR_TEXT);
            txtNgay.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIConfig.COLOR_BORDER, 1),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtNgay.setEditable(false);
            txtNgay.setFocusable(false);

            // Style btnMoLich
            btnMoLich.setFont(UIConfig.FONT_BOLD);
            btnMoLich.setBackground(UIConfig.COLOR_SECONDARY);
            btnMoLich.setForeground(UIConfig.COLOR_TEXT);
            btnMoLich.setOpaque(true);
            btnMoLich.setBorderPainted(false);
            btnMoLich.setFocusPainted(false);
            btnMoLich.setMargin(new Insets(4, 8, 4, 8));
            btnMoLich.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnMoLich.setToolTipText("Mở lịch chọn ngày");

            // Style lblThang
            lblThang.setFont(UIConfig.FONT_SECTION);
            lblThang.setForeground(UIConfig.COLOR_TITLE);

            add(txtNgay, BorderLayout.CENTER);
            add(btnMoLich, BorderLayout.EAST);
            popLich.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConfig.COLOR_BORDER),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            popLich.add(pnlLich);
            cauHinhLich();
            setSelectedDate(ngayBanDau);
        }

        private void cauHinhLich() {
            pnlLich.setOpaque(false);
            pnlLich.setPreferredSize(new java.awt.Dimension(280, 250));
            JPanel pnlDieuHuongThang = new JPanel(new BorderLayout(8, 0));
            pnlDieuHuongThang.setOpaque(false);
            cauHinhNutThang(btnThangTruoc);
            cauHinhNutThang(btnThangSau);
            lblThang.setHorizontalAlignment(SwingConstants.CENTER);
            pnlDieuHuongThang.add(btnThangTruoc, BorderLayout.WEST);
            pnlDieuHuongThang.add(lblThang, BorderLayout.CENTER);
            pnlDieuHuongThang.add(btnThangSau, BorderLayout.EAST);
            pnlLich.add(pnlDieuHuongThang, BorderLayout.NORTH);

            JPanel pnlNgay = new JPanel(new GridLayout(7, 7, 3, 3));
            pnlNgay.setOpaque(false);
            for (String tenNgay : TEN_NGAY_TRONG_TUAN) {
                JLabel lblNgay = new JLabel(tenNgay);
                lblNgay.setFont(UIConfig.FONT_REGULAR);
                lblNgay.setForeground(UIConfig.COLOR_MUTED);
                lblNgay.setHorizontalAlignment(SwingConstants.CENTER);
                pnlNgay.add(lblNgay);
            }
            for (int index = 0; index < btnNgay.length; index++) {
                btnNgay[index] = new JButton();
                btnNgay[index].setFont(UIConfig.FONT_REGULAR);
                btnNgay[index].setFocusPainted(false);
                btnNgay[index].setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                btnNgay[index].setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                pnlNgay.add(btnNgay[index]);
            }
            pnlLich.add(pnlNgay, BorderLayout.CENTER);
        }

        private void capNhatLich() {
            lblThang.setText(thangDangHienThi.format(DINH_DANG_THANG));
            LocalDate ngayDauO = thangDangHienThi.withDayOfMonth(1).with(DayOfWeek.MONDAY);
            LocalDate homNay   = LocalDate.now();
            for (int index = 0; index < btnNgay.length; index++) {
                LocalDate ngay   = ngayDauO.plusDays(index);
                JButton btn      = btnNgay[index];
                boolean cungThang = ngay.getMonth() == thangDangHienThi.getMonth();
                boolean dangChon  = ngay.equals(ngayDuocChon);
                btn.setText(String.valueOf(ngay.getDayOfMonth()));
                btn.putClientProperty("ngay", ngay);
                btn.setForeground(dangChon ? Color.WHITE
                        : cungThang ? UIConfig.COLOR_TEXT : UIConfig.COLOR_MUTED);
                btn.setBackground(dangChon ? UIConfig.COLOR_PRIMARY
                        : ngay.equals(homNay) ? UIConfig.COLOR_SELECTION : Color.WHITE);
                btn.setOpaque(true);
                btn.setToolTipText(ngay.format(DINH_DANG_NGAY_DAY_DU));
            }
            pnlLich.revalidate();
            pnlLich.repaint();
            popLich.pack();
        }

        public void setSelectedDate(LocalDate ngay) {
            ngayDuocChon     = ngay;
            thangDangHienThi = ngay.withDayOfMonth(1);
            txtNgay.setText(ngay.format(DINH_DANG_NGAY_DAY_DU));
            capNhatLich();
        }

        public void capNhatThang(int soThang) {
            thangDangHienThi = thangDangHienThi.plusMonths(soThang);
            capNhatLich();
        }

        private void cauHinhNutThang(JButton btnThang) {
            btnThang.setFont(UIConfig.FONT_SECTION);
            btnThang.setForeground(UIConfig.COLOR_TITLE);
            btnThang.setBackground(Color.WHITE);
            btnThang.setFocusPainted(false);
            btnThang.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }

        public JButton   getBtnMoLich()     { return btnMoLich; }
        public JButton   getBtnThangTruoc() { return btnThangTruoc; }
        public JButton   getBtnThangSau()   { return btnThangSau; }
        public JButton[] getBtnNgay()       { return btnNgay.clone(); }
        public JPopupMenu getPopLich()      { return popLich; }
        public LocalDate getNgayDuocChon()  { return ngayDuocChon; }
        public LocalDate getSelectedDate()  { return ngayDuocChon; }
        public LocalDate getNgay(JButton btn) { return (LocalDate) btn.getClientProperty("ngay"); }
        public void hienThiLich() {
            capNhatLich();
            popLich.show(this, 0, getHeight());
        }
    }

    // ── Lớp nội: renderer ô lịch ca ──────────────────────────────────────────
    private final class LichCaCellRenderer extends DefaultTableCellRenderer {
        private final JTextArea txaNhanVien = new JTextArea();
        private int caHienTai     = -1;
        private int cotNgayHienTai = -1;

        private LichCaCellRenderer() {
            txaNhanVien.setLineWrap(true);
            txaNhanVien.setWrapStyleWord(true);
            txaNhanVien.setFont(UIConfig.FONT_REGULAR);
            txaNhanVien.setForeground(UIConfig.COLOR_TEXT);
            txaNhanVien.setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        private void setCaHienTai(int caHienTai, int cotNgayHienTai) {
            this.caHienTai      = caHienTai;
            this.cotNgayHienTai = cotNgayHienTai;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text          = value == null ? "" : value.toString();
            boolean laCaHienTai  = row == caHienTai;
            boolean laNgayHienTai = column == cotNgayHienTai;
            boolean laNgayQuaKhu = column > 0
                    && ngayDauTuan.plusDays(column - 1L).isBefore(LocalDate.now());
            if (column == 0) {
                JLabel lblCa = (JLabel) super.getTableCellRendererComponent(
                        table, text, isSelected, hasFocus, row, column);
                lblCa.setHorizontalAlignment(SwingConstants.CENTER);
                lblCa.setVerticalAlignment(SwingConstants.CENTER);
                lblCa.setFont(UIConfig.FONT_BOLD);
                lblCa.setForeground(laCaHienTai ? UIConfig.COLOR_SUCCESS : UIConfig.COLOR_TITLE);
                lblCa.setBackground(isSelected ? UIConfig.COLOR_SELECTION
                    : laCaHienTai ? UIConfig.COLOR_SELECTION : UIConfig.COLOR_TABLE_HEADER);
                lblCa.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                if (laCaHienTai) {
                    lblCa.setText("<html><center>" + text.replace("\n", "<br>")
                            + "<br><b>● ĐANG DIỄN RA</b></center></html>");
                }
                return lblCa;
            }
            txaNhanVien.setText(text);
            if (laNgayQuaKhu) {
                txaNhanVien.setBackground(UIConfig.COLOR_SECONDARY);
            } else if (isSelected) {
                txaNhanVien.setBackground(UIConfig.COLOR_SELECTION);
            } else if (laCaHienTai && laNgayHienTai) {
                txaNhanVien.setBackground(UIConfig.COLOR_SELECTION);
            } else if (laCaHienTai) {
                txaNhanVien.setBackground(UIConfig.COLOR_TABLE_HEADER);
            } else {
                txaNhanVien.setBackground(UIConfig.COLOR_SURFACE);
            }
            txaNhanVien.setForeground(laNgayQuaKhu || CHUA_PHAN_CONG.equals(text)
                    ? UIConfig.COLOR_MUTED : UIConfig.COLOR_TEXT);
            txaNhanVien.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
            return txaNhanVien;
        }
    }
}

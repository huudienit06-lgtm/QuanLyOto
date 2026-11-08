package gui;

import controller.ChucVuController;
import entity.ChucVu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class FrmChucVu extends JPanel {
    private final JTextField txtMaChucVu = taoONhap(16);
    private final JTextField txtTenChucVu = taoONhap(24);
    private final JTextField txtTimKiem = taoONhap(18);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final JButton btnThem = new JButton("Thêm");
    private final JButton btnCapNhat = new JButton("Cập nhật");
    private final JButton btnXoa = new JButton("Xóa");
    private final JButton btnLamMoi = new JButton("Làm mới");
    private final JButton btnTaiLai = new JButton("Tải lại");

    private final DefaultTableModel modelChucVu = new DefaultTableModel(
            new Object[] { "Mã chức vụ", "Tên chức vụ" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblChucVu = new JTable(modelChucVu);
    private final TableRowSorter<DefaultTableModel> sorterChucVu = new TableRowSorter<>(modelChucVu);
    private final ChucVuController controller = new ChucVuController(this);

    public FrmChucVu() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(22, 28, 22, 28));
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        configureTable();
        styleButton(btnThem, UIConfig.COLOR_SUCCESS, Color.WHITE);
        styleButton(btnCapNhat, UIConfig.COLOR_PRIMARY, Color.WHITE);
        styleButton(btnXoa, UIConfig.COLOR_DANGER, Color.WHITE);
        styleButton(btnLamMoi, UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
        styleButton(btnTaiLai, UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
        txtMaChucVu.setEditable(false);
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);
        controller.khoiTao();
    }

    private JPanel createHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 5));
        pnlHeader.setOpaque(false);
        JLabel lblTieuDe = new JLabel("QUẢN LÝ CHỨC VỤ");
        lblTieuDe.setFont(UIConfig.FONT_TITLE);
        lblTieuDe.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblMoTa = new JLabel("Quản lý danh mục chức vụ áp dụng cho nhân viên");
        lblMoTa.setFont(UIConfig.FONT_REGULAR);
        lblMoTa.setForeground(UIConfig.COLOR_MUTED);
        pnlHeader.add(lblTieuDe, BorderLayout.NORTH);
        pnlHeader.add(lblMoTa, BorderLayout.CENTER);
        return pnlHeader;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormCard(), BorderLayout.NORTH);
        pnlContent.add(createTableCard(), BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormCard() {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 12));
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã chức vụ *", txtMaChucVu, 0);
        addField(pnlFields, "Tên chức vụ *", txtTenChucVu, 2);
        JPanel pnlButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnThem);
        pnlButtons.add(btnCapNhat);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        pnlCard.add(createSectionTitle("Thông tin chức vụ"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createTableCard() {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 10));
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.add(createSectionTitle("Danh sách chức vụ"), BorderLayout.WEST);
        JPanel pnlTimKiem = new JPanel(new BorderLayout(8, 0));
        pnlTimKiem.setOpaque(false);
        JLabel lblTimKiem = new JLabel("Tìm kiếm");
        lblTimKiem.setFont(UIConfig.FONT_REGULAR);
        lblTimKiem.setForeground(UIConfig.COLOR_MUTED);
        pnlTimKiem.add(lblTimKiem, BorderLayout.WEST);
        pnlTimKiem.add(txtTimKiem, BorderLayout.CENTER);
        pnlTimKiem.add(btnTaiLai, BorderLayout.EAST);
        pnlHeader.add(pnlTimKiem, BorderLayout.EAST);
        JScrollPane scrTable = new JScrollPane(tblChucVu);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(pnlHeader, BorderLayout.NORTH);
        pnlCard.add(scrTable, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel createStatusBar() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private void configureTable() {
        tblChucVu.setRowSorter(sorterChucVu);
        tblChucVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChucVu.setRowHeight(30);
        tblChucVu.setFont(UIConfig.FONT_REGULAR);
        tblChucVu.setForeground(UIConfig.COLOR_TEXT);
        tblChucVu.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblChucVu.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblChucVu.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblChucVu.setFillsViewportHeight(true);
        tblChucVu.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblChucVu.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblChucVu.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblChucVu.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
    }

    private JPanel createCardPanel(java.awt.LayoutManager layout) {
        JPanel pnlCard = new JPanel(layout);
        pnlCard.setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return pnlCard;
    }

    private JLabel createSectionTitle(String text) {
        JLabel lblTieuDe = new JLabel(text);
        lblTieuDe.setFont(UIConfig.FONT_SECTION);
        lblTieuDe.setForeground(UIConfig.COLOR_TITLE);
        return lblTieuDe;
    }

    private void addField(JPanel pnlFields, String text, JTextField txtField, int column) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = 0;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lblField = new JLabel(text);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_MUTED);
        pnlFields.add(lblField, gbcLabel);
        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = 0;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlFields.add(txtField, gbcInput);
    }

    private static JTextField taoONhap(int columns) {
        JTextField txtField = new JTextField(columns);
        txtField.setFont(UIConfig.FONT_REGULAR);
        txtField.setForeground(UIConfig.COLOR_TEXT);
        txtField.setBackground(UIConfig.COLOR_SURFACE);
        txtField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1), new EmptyBorder(6, 9, 6, 9)));
        return txtField;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(UIConfig.FONT_BOLD);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(6, 14, 6, 14));
    }

    public void hienThiDanhSach(List<ChucVu> danhSach) {
        modelChucVu.setRowCount(0);
        for (ChucVu chucVu : danhSach) {
            modelChucVu.addRow(new Object[] { chucVu.getMaChucVu(), chucVu.getTenChucVu() });
        }
    }

    public ChucVu layChucVuNhap() {
        String maChucVu = txtMaChucVu.getText().trim();
        String tenChucVu = txtTenChucVu.getText().trim();
        if (maChucVu.isEmpty()) {
            throw new IllegalArgumentException("Mã chức vụ không được để trống.");
        }
        if (maChucVu.length() > 20) {
            throw new IllegalArgumentException("Mã chức vụ không được vượt quá 20 ký tự.");
        }
        if (tenChucVu.isEmpty()) {
            throw new IllegalArgumentException("Tên chức vụ không được để trống.");
        }
        if (tenChucVu.length() > 50) {
            throw new IllegalArgumentException("Tên chức vụ không được vượt quá 50 ký tự.");
        }
        return new ChucVu(maChucVu, tenChucVu);
    }

    public void hienThiChucVuDuocChon() {
        int viewRow = tblChucVu.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = tblChucVu.convertRowIndexToModel(viewRow);
        txtMaChucVu.setText(String.valueOf(modelChucVu.getValueAt(modelRow, 0)));
        txtTenChucVu.setText(String.valueOf(modelChucVu.getValueAt(modelRow, 1)));
        txtMaChucVu.setEditable(false);
        btnThem.setEnabled(false);
        btnCapNhat.setEnabled(true);
        btnXoa.setEnabled(true);
        thongBao("Đang chọn chức vụ " + txtTenChucVu.getText() + ".");
    }

    public void lamMoiForm(String maMoi) {
        tblChucVu.clearSelection();
        txtMaChucVu.setText(maMoi);
        txtTenChucVu.setText("");
        btnThem.setEnabled(true);
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);
        thongBao("Sẵn sàng.");
    }

    public void locDanhSach() {
        String tuKhoa = txtTimKiem.getText().trim();
        sorterChucVu.setRowFilter(tuKhoa.isEmpty()
                ? null : RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa)));
        lblThongBao.setText("Hiển thị " + sorterChucVu.getViewRowCount()
                + " / " + modelChucVu.getRowCount() + " chức vụ.");
    }

    public String getMaChucVuDangChon() {
        int viewRow = tblChucVu.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = tblChucVu.convertRowIndexToModel(viewRow);
        return String.valueOf(modelChucVu.getValueAt(modelRow, 0));
    }

    public boolean xacNhanXoa(String maChucVu) {
        return JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa chức vụ " + maChucVu + "?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void thongBao(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
    }

    public void hienThiLoi(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        JOptionPane.showMessageDialog(this, message, "Quản lý chức vụ", JOptionPane.ERROR_MESSAGE);
    }

    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnCapNhat() { return btnCapNhat; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JButton getBtnTaiLai() { return btnTaiLai; }
    public JTable getTblChucVu() { return tblChucVu; }
    public JTextField getTxtTimKiem() { return txtTimKiem; }
}
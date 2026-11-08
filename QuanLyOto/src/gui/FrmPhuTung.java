package gui;

import controller.PhuTungController;
import entity.PhuTung;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmPhuTung extends JPanel {
    private static final Color PALE_BLUE = UIConfig.COLOR_BACKGROUND;
    private static final Color TEXT = UIConfig.COLOR_TEXT;
    private static final Color MUTED = UIConfig.COLOR_MUTED;
    private static final Color BORDER = UIConfig.COLOR_BORDER;

    private final JTextField txtMaPT = new JTextField(16);
    private final JTextField txtTenPT = new JTextField(16);
    private final JComboBox<String> cboDonViTinh = new JComboBox<>(new String[]{"Cái", "Bộ", "Chiếc", "Lít", "Hộp"});
    private final JTextField txtGiaNhap = new JTextField(16);
    private final JTextField txtGiaBan = new JTextField(16);
    private final JTextField txtSoLuongTon = new JTextField(16);
    private final JComboBox<String> cboNhaCungCap = new JComboBox<>();
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{"Còn hàng", "Hết hàng", "Ngừng kinh doanh"});

    private final JTextField txtTimMa = new JTextField(10);
    private final JTextField txtTimTen = new JTextField(12);
    private final JComboBox<String> cboTimNCC = new JComboBox<>(new String[]{"Tất cả"});
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final DefaultTableModel modelPhuTung = new DefaultTableModel(
            new Object[] { "MÃ PT", "TÊN PHỤ TÙNG", "ĐVT", "GIÁ NHẬP", "GIÁ BÁN", "TỒN KHO", "NHÀ CUNG CẤP", "TRẠNG THÁI" }, 0);
    private final JTable tblPhuTung = new JTable(modelPhuTung);

    private JButton btnThem = new JButton();
    private JButton btnCapNhat = new JButton();
    private JButton btnXoa = new JButton();
    private JButton btnLamMoi;
    private JButton btnTaiLai;

    private final DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private final TableRowSorter<DefaultTableModel> sortedPhuTung = new TableRowSorter<>(modelPhuTung);

    // [MVC] Khởi tạo Controller điều phối
    private final PhuTungController controller = new PhuTungController(this);

    public FrmPhuTung() {
        setLayout(new BorderLayout(0, 18));
        setBackground(PALE_BLUE);
        setBorder(new EmptyBorder(22, 32, 22, 32));

        add(createHeading(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        tblPhuTung.setRowSorter(sortedPhuTung);
        tblPhuTung.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhuTung.setRowHeight(30);
        tblPhuTung.setFont(UIConfig.FONT_REGULAR);
        tblPhuTung.setForeground(TEXT);
        tblPhuTung.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblPhuTung.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblPhuTung.setSelectionForeground(TEXT);
        tblPhuTung.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblPhuTung.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblPhuTung.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblPhuTung.getTableHeader().setPreferredSize(new Dimension(0, 34));
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tblPhuTung.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tblPhuTung.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tblPhuTung.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        
        // [MVC] Kích hoạt sự kiện từ Controller
        controller.khoiTao();
    }

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel(new BorderLayout());
        pnlHeading.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ PHỤ TÙNG");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        return pnlHeading;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormPanel(), BorderLayout.NORTH);
        pnlContent.add(createTableCard("Danh sách phụ tùng", createSearchToolbar(), new JScrollPane(tblPhuTung)), BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormPanel() {
        JPanel pnlFormCard = createCardPanel();
        pnlFormCard.setLayout(new BorderLayout(0, 12));

        JLabel lblFormTitle = createSectionTitle("Thông tin phụ tùng");
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        
        cboNhaCungCap.setEditable(true);
        cboDonViTinh.setEditable(true);
        txtSoLuongTon.setEditable(false);
        txtSoLuongTon.setBackground(UIConfig.COLOR_SECONDARY);

        addField(pnlFields, "Mã phụ tùng", txtMaPT, 0, 0);
        addField(pnlFields, "Tên phụ tùng", txtTenPT, 2, 0);
        addField(pnlFields, "Đơn vị tính", cboDonViTinh, 4, 0);
        addField(pnlFields, "Giá nhập", txtGiaNhap, 0, 1);
        addField(pnlFields, "Giá bán", txtGiaBan, 2, 1);
        addField(pnlFields, "Số lượng tồn", txtSoLuongTon, 4, 1);
        addField(pnlFields, "Nhà cung cấp", cboNhaCungCap, 0, 2);
        addField(pnlFields, "Trạng thái", cboTrangThai, 2, 2);

        txtMaPT.setEditable(false);
        txtMaPT.setBackground(UIConfig.COLOR_SECONDARY);
        txtMaPT.setFocusable(false);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        
        btnThem = createButton("Thêm mới", new ImageIcon(getClass().getResource("/resources/icons/plus.png")), UIConfig.COLOR_SUCCESS, Color.WHITE);
        btnCapNhat = createButton("Cập nhật", new ImageIcon(getClass().getResource("/resources/icons/update.png")), UIConfig.COLOR_PRIMARY, Color.WHITE);
        btnXoa = createButton("Xóa", new ImageIcon(getClass().getResource("/resources/icons/trash.png")), UIConfig.COLOR_DANGER, Color.WHITE);
        btnLamMoi = createButton("Làm mới", new ImageIcon(getClass().getResource("/resources/icons/reset.png")), UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);

        pnlButtons.add(btnThem, buttonConstraints(0));
        pnlButtons.add(btnCapNhat, buttonConstraints(1));
        pnlButtons.add(btnXoa, buttonConstraints(2));
        pnlButtons.add(btnLamMoi, buttonConstraints(3));

        JPanel pnlLower = new JPanel(new BorderLayout());
        pnlLower.setOpaque(false);
        pnlLower.add(pnlButtons, BorderLayout.WEST);

        pnlFormCard.add(lblFormTitle, BorderLayout.NORTH);
        pnlFormCard.add(pnlFields, BorderLayout.CENTER);
        pnlFormCard.add(pnlLower, BorderLayout.SOUTH);
        return pnlFormCard;
    }

    private JPanel createStatusBar() {
        JPanel pnlStatusBar = new JPanel(new BorderLayout());
        pnlStatusBar.setOpaque(false);
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(MUTED);
        pnlStatusBar.add(lblThongBao, BorderLayout.WEST);
        return pnlStatusBar;
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);
        JPanel pnlLoc = new JPanel(new GridLayout(1, 4, 8, 0));
        pnlLoc.setOpaque(false);
        
        cboTimNCC.addActionListener(e -> locDanhSachPhuTung());

        pnlLoc.add(createSearchField("Mã phụ tùng", txtTimMa));
        pnlLoc.add(createSearchField("Tên phụ tùng", txtTimTen));
        
        JPanel pnlCboLoc = new JPanel(new BorderLayout(0, 3));
        pnlCboLoc.setOpaque(false);
        JLabel lblNhaCungCap = new JLabel("Nhà cung cấp");
        lblNhaCungCap.setFont(UIConfig.FONT_REGULAR);
        lblNhaCungCap.setForeground(UIConfig.COLOR_MUTED);
        pnlCboLoc.add(lblNhaCungCap, BorderLayout.NORTH);
        pnlCboLoc.add(cboTimNCC, BorderLayout.CENTER);
        pnlLoc.add(pnlCboLoc);
        
        btnTaiLai = new JButton("Tải lại");
        btnTaiLai.setFont(UIConfig.FONT_BOLD);
        btnTaiLai.setBackground(UIConfig.COLOR_SECONDARY);
        btnTaiLai.setForeground(UIConfig.COLOR_TEXT);
        btnTaiLai.setOpaque(true);
        btnTaiLai.setBorderPainted(false);
        btnTaiLai.setFocusPainted(false);
        pnlSearch.add(pnlLoc, BorderLayout.CENTER);
        pnlSearch.add(btnTaiLai, BorderLayout.EAST);
        
        themBoLangNghe(txtTimMa);
        themBoLangNghe(txtTimTen);
        return pnlSearch;
    }

    private JPanel createSearchField(String labelText, JTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new JLabel(labelText);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_MUTED);
        pnlField.add(lblField, BorderLayout.NORTH);
        pnlField.add(txtField, BorderLayout.CENTER);
        return pnlField;
    }

    private void themBoLangNghe(JTextField txtField) {
        txtField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { locDanhSachPhuTung(); }
            @Override public void removeUpdate(DocumentEvent event) { locDanhSachPhuTung(); }
            @Override public void changedUpdate(DocumentEvent event) { locDanhSachPhuTung(); }
        });
    }

    public void locDanhSachPhuTung() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        themBoLoc(filters, txtTimMa.getText(), 0);
        themBoLoc(filters, txtTimTen.getText(), 1);
        
        String timNcc = (String) cboTimNCC.getSelectedItem();
        if (timNcc != null && !timNcc.equals("Tất cả")) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(timNcc), 6));
        }

        sortedPhuTung.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblThongBao.setText("Hiển thị " + sortedPhuTung.getViewRowCount() + " / " + modelPhuTung.getRowCount() + " phụ tùng");
    }

    private void themBoLoc(List<RowFilter<Object, Object>> filters, String value, int column) {
        String key = value.trim();
        if (!key.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(key), column));
        }
    }

    private void addField(JPanel pnlFields, String label, Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column; gbcLabel.gridy = row; gbcLabel.anchor = GridBagConstraints.WEST; gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lblField = new JLabel(label);
        lblField.setFont(UIConfig.FONT_REGULAR); lblField.setForeground(TEXT);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1; gbcInput.gridy = row; gbcInput.weightx = 1; gbcInput.fill = GridBagConstraints.HORIZONTAL; gbcInput.insets = new Insets(5, 0, 5, 18);
        if (component instanceof JTextField textField) styleTextField(textField);
        if (component instanceof JComboBox<?> comboBox) comboBox.setFont(UIConfig.FONT_REGULAR);
        pnlFields.add(component, gbcInput);
    }

    private JButton createButton(String text, ImageIcon icon, Color background, Color foreground) {
        JButton btn = new JButton(text);
        btn.setFont(UIConfig.FONT_BOLD); btn.setBackground(background); btn.setForeground(foreground);
        Image image = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        btn.setIcon(new ImageIcon(image)); btn.setIconTextGap(3); btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 13, 8, 13)); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, btn.getPreferredSize().height));
        return btn;
    }

    private GridBagConstraints buttonConstraints(int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(0, 0, 0, 8);
        return gbc;
    }

    private void styleTextField(JTextField txtField) {
        txtField.setFont(UIConfig.FONT_REGULAR); txtField.setForeground(TEXT);
        txtField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 9, 6, 9)));
    }

    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text); lbl.setFont(UIConfig.FONT_SECTION); lbl.setForeground(UIConfig.COLOR_TITLE);
        return lbl;
    }

    private JPanel createCardPanel() {
        JPanel pnlCard = new JPanel(); pnlCard.setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));
        return pnlCard;
    }

    private JPanel createTableCard(String title, JPanel toolbar, JScrollPane scrTable) {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 10));
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.add(createSectionTitle(title), BorderLayout.WEST);
        pnlHeader.add(toolbar, BorderLayout.EAST);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(pnlHeader, BorderLayout.NORTH);
        pnlCard.add(scrTable, BorderLayout.CENTER);
        return pnlCard;
    }

    public void hienThiDanhSach(List<PhuTung> ds) {
        modelPhuTung.setRowCount(0);
        for (PhuTung pt : ds) {
            modelPhuTung.addRow(new Object[] {
                pt.getMaPhuTung(), pt.getTenPhuTung(), pt.getDonViTinh(),
                df.format(pt.getGiaNhap()), df.format(pt.getGiaBan()),
                pt.getSoLuongTon(), pt.getNhaCungCap(), pt.getTrangThai()
            });
        }
    }

    public void hienThiLoi(String message) {
        setThongBao(message);
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        JOptionPane.showMessageDialog(this, message, "Quản lý phụ tùng", JOptionPane.ERROR_MESSAGE);
    }

    public void setThongBao(String msg) {
        lblThongBao.setText(msg);
        lblThongBao.setForeground(MUTED);
    }

    public PhuTung docThongTinPhuTung() {
        String ma = txtMaPT.getText().trim();
        String ten = txtTenPT.getText().trim();
        if (ten.isEmpty()) throw new IllegalArgumentException("Tên phụ tùng không được để trống");
        
        double giaNhap = parseMoney(txtGiaNhap.getText().trim(), "Giá nhập");
        double giaBan = parseMoney(txtGiaBan.getText().trim(), "Giá bán");
        
        int soLuong = 0;
        if (!txtSoLuongTon.getText().trim().isEmpty()) {
            try { soLuong = Integer.parseInt(txtSoLuongTon.getText().trim()); }
            catch (Exception e) { throw new IllegalArgumentException("Số lượng tồn phải là số"); }
        }

        String dvt = (String) cboDonViTinh.getSelectedItem();
        String ncc = (String) cboNhaCungCap.getSelectedItem();
        String trangThai = (String) cboTrangThai.getSelectedItem();

        return new PhuTung(ma, ten, dvt, giaNhap, giaBan, soLuong, ncc, trangThai);
    }

    private double parseMoney(String text, String fieldName) {
        if (text.isEmpty()) throw new IllegalArgumentException(fieldName + " không được để trống");
        try {
            double value = Double.parseDouble(text.replaceAll("[^0-9]", ""));
            if (value < 0) throw new IllegalArgumentException(fieldName + " không được âm");
            return value;
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " phải là số hợp lệ");
        }
    }

    public void lamMoiForm() {
        txtMaPT.setText(controller.phatSinhMaPT());
        txtTenPT.setText(""); txtGiaNhap.setText(""); txtGiaBan.setText(""); txtSoLuongTon.setText("0");
        cboDonViTinh.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        tblPhuTung.clearSelection();
        txtTenPT.requestFocusInWindow();
        btnThem.setEnabled(true); btnCapNhat.setEnabled(false); btnXoa.setEnabled(false);
    }

    public void hienThiPhuTungDaChon() {
        int selectedRow = tblPhuTung.getSelectedRow();
        if (selectedRow < 0) return;
        int modelRow = tblPhuTung.convertRowIndexToModel(selectedRow);
        
        txtMaPT.setText(cellText(modelPhuTung.getValueAt(modelRow, 0)));
        txtTenPT.setText(cellText(modelPhuTung.getValueAt(modelRow, 1)));
        cboDonViTinh.setSelectedItem(cellText(modelPhuTung.getValueAt(modelRow, 2)));
        txtGiaNhap.setText(cellText(modelPhuTung.getValueAt(modelRow, 3)).replaceAll("[^0-9]", ""));
        txtGiaBan.setText(cellText(modelPhuTung.getValueAt(modelRow, 4)).replaceAll("[^0-9]", ""));
        txtSoLuongTon.setText(cellText(modelPhuTung.getValueAt(modelRow, 5)));
        cboNhaCungCap.setSelectedItem(cellText(modelPhuTung.getValueAt(modelRow, 6)));
        cboTrangThai.setSelectedItem(cellText(modelPhuTung.getValueAt(modelRow, 7)));

        btnThem.setEnabled(false); btnCapNhat.setEnabled(true); btnXoa.setEnabled(true);
    }

    private String cellText(Object value) { return value == null ? "" : value.toString(); }

    // --- CÁC HÀM GETTER CHO CONTROLLER ---
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnCapNhat() { return btnCapNhat; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JButton getBtnTaiLai() { return btnTaiLai; }
    public JTable getTblPhuTung() { return tblPhuTung; }
    public DefaultTableModel getModelPhuTung() { return modelPhuTung; }
    public JTextField getTxtMaPT() { return txtMaPT; }
    public JTextField getTxtTimMa() { return txtTimMa; }
    public JTextField getTxtTimTen() { return txtTimTen; }
    public JComboBox<String> getCboNhaCungCap() { return cboNhaCungCap; }
    public JComboBox<String> getCboTimNCC() { return cboTimNCC; }
}
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import dao.DichVuDAO;
import entity.DichVu;
import util.MaTuDong;

public class FrmDichVu extends JPanel {
    private static final Color PALE_BLUE = new Color(242, 247, 255);
    private static final Color TEXT = new Color(47, 62, 84);
    private static final Color MUTED = new Color(112, 128, 150);
    private static final Color BORDER = new Color(221, 231, 245);

    private final JTextField txtMaDichVu = new JTextField(16);
    private final JTextField txtTenDichVu = new JTextField(16);
    private final JTextField txtGiaTien = new JTextField(16);
    private final JTextField txtMoTa = new JTextField(16);
    private final JTextField txtLocMaDichVu = new JTextField(10);
    private final JTextField txtLocTenDichVu = new JTextField(12);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final DefaultTableModel modelDichVu = new DefaultTableModel(
            new Object[] {
                    "MÃ DỊCH VỤ",
                    "TÊN DỊCH VỤ",
                    "GIÁ TIỀN",
                    "MÔ TẢ"
            },
            0);
    private final JTable tblDichVu = new JTable(modelDichVu);

    private JButton btnAdd = new JButton();
    private JButton btnUpdate = new JButton();
    private JButton btnDel = new JButton();

    private final DichVuDAO dichVuDao = new DichVuDAO();
    private final DecimalFormat df = new DecimalFormat("#,### VNĐ");

    private final TableRowSorter<DefaultTableModel> sortedDichVu = new TableRowSorter<>(modelDichVu);

    public FrmDichVu() {
        setLayout(new BorderLayout(0, 18));
        setBackground(PALE_BLUE);
        setBorder(new EmptyBorder(22, 32, 22, 32));

        add(createHeading(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        tblDichVu.setRowSorter(sortedDichVu);
        tblDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDichVu.setRowHeight(30);
        tblDichVu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDichVu.setForeground(TEXT);
        tblDichVu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDichVu.getTableHeader().setForeground(new Color(32, 55, 92));
        tblDichVu.getTableHeader().setBackground(new Color(239, 245, 253));
        tblDichVu.getTableHeader().setPreferredSize(new Dimension(0, 34));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        tblDichVu.getColumnModel().getColumn(2).setCellRenderer(renderer);
        tblDichVu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblDichVu.getSelectedRow() >= 0) {
                hienThiDichVuDaChon();
            }
        });
        napDanhSachDichVu();
    }

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel(new BorderLayout());
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ DỊCH VỤ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblTitle.setForeground(new Color(32, 55, 92));

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        return pnlHeading;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormPanel(), BorderLayout.NORTH);
        pnlContent.add(
                FormUI.createTableCard(
                        "Danh sách dịch vụ",
                        createSearchToolbar(),
                        new AppScrollPane(tblDichVu)),
                BorderLayout.CENTER);
        return pnlContent;

    }

    private JPanel createFormPanel() {
        JPanel pnlFormCard = createCardPanel();
        pnlFormCard.setLayout(new BorderLayout(0, 12));

        JLabel lblFormTitle = createSectionTitle("Thông tin dịch vụ");
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã dịch vụ", txtMaDichVu, 0, 0);
        addField(pnlFields, "Tên dịch vụ", txtTenDichVu, 2, 0);
        addField(pnlFields, "Giá tiền", txtGiaTien, 0, 1);
        addField(pnlFields, "Mô tả", txtMoTa, 2, 1);

        txtMaDichVu.setEditable(false);
        txtMaDichVu.setBackground(new Color(240, 240, 240));
        txtMaDichVu.setFocusable(false);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        btnAdd = createButton(
                "Thêm mới",
                new ImageIcon(getClass().getResource("/resources/icons/plus.png")),
                new Color(0x33cc33),
                new Color(0xffffff));

        btnAdd.addActionListener(e -> themDichVu());
        btnUpdate = createButton(
                "Cập nhật",
                new ImageIcon(getClass().getResource("/resources/icons/update.png")),
                new Color(0x3366cc),
                new Color(0xffffff));
        btnUpdate.addActionListener(e -> capNhatDichVu());
        btnDel = createButton(
                "Xóa",
                new ImageIcon(getClass().getResource("/resources/icons/trash.png")),
                new Color(0xff0033),
                new Color(0xffffff));
        btnDel.addActionListener(e -> xoaDichVu());
        JButton btnReset = createButton(
                "Làm mới",
                new ImageIcon(getClass().getResource("/resources/icons/reset.png")),
                new Color(0x555555),
                new Color(0xffffff));
        btnReset.addActionListener(e -> lamMoiForm());
        pnlButtons.add(btnAdd, buttonConstraints(0));
        pnlButtons.add(btnUpdate, buttonConstraints(1));
        pnlButtons.add(btnDel, buttonConstraints(2));
        pnlButtons.add(btnReset, buttonConstraints(3));

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
        lblThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBao.setForeground(MUTED);
        pnlStatusBar.add(lblThongBao, BorderLayout.WEST);
        return pnlStatusBar;
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);
        JPanel pnlLoc = new JPanel(new GridLayout(1, 4, 8, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã dịch vụ", txtLocMaDichVu));
        pnlLoc.add(createSearchField("Tên dịch vụ", txtLocTenDichVu));
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTaiLai.addActionListener(e -> {
            txtLocMaDichVu.setText("");
            txtLocTenDichVu.setText("");
            napDanhSachDichVu();
        });
        pnlSearch.add(pnlLoc, BorderLayout.CENTER);
        pnlSearch.add(btnTaiLai, BorderLayout.EAST);
        themBoLangNghe(txtLocMaDichVu);
        themBoLangNghe(txtLocTenDichVu);

        return pnlSearch;
    }

    private JPanel createSearchField(String labelText, JTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new AppLabel(labelText, AppLabel.Style.MUTED);
        pnlField.add(lblField, BorderLayout.NORTH);
        pnlField.add(txtField, BorderLayout.CENTER);
        return pnlField;
    }

    private void themBoLangNghe(JTextField txtField) {
        txtField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSachDichVu();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSachDichVu();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSachDichVu();
            }
        });
    }

    private void locDanhSachDichVu() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        themBoLoc(filters, txtLocMaDichVu.getText(), 0);
        themBoLoc(filters, txtLocTenDichVu.getText(), 1);
        sortedDichVu.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblThongBao
                .setText("Hiển thị " + sortedDichVu.getViewRowCount() + " / " + modelDichVu.getRowCount() + " dịch vụ");
    }

    private void themBoLoc(List<RowFilter<Object, Object>> filters, String value, int column) {
        String key = value.trim();
        if (!key.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(key), column));
        }
    }

    private void addField(JPanel pnlFields, String label, Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblField.setForeground(TEXT);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        if (component instanceof JTextField) {
            styleTextField((JTextField) component);
        } else if (component instanceof JComboBox<?>) {
            component.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        pnlFields.add(component, gbcInput);
    }

    private JButton createButton(String text, ImageIcon icon, Color background, Color foreground) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(background);
        btn.setForeground(foreground);
        Image image = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        btn.setIcon(new ImageIcon(image));
        btn.setIconTextGap(3);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 13, 8, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, btn.getPreferredSize().height));

        return btn;
    }

    private GridBagConstraints buttonConstraints(int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        return gbc;
    }

    private void styleTextField(JTextField txtField) {
        txtField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtField.setForeground(TEXT);
        txtField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 9, 6, 9)));
    }

    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(32, 55, 92));
        return lbl;
    }

    private JPanel createCardPanel() {
        JPanel pnlCard = new JPanel();
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        return pnlCard;
    }

    private void napDanhSachDichVu() {
        try {
            hienThiDanhSach(dichVuDao.findAll());
            txtMaDichVu.setText(phatSinhMaDV());
            lblThongBao.setText("Đã tải " + modelDichVu.getRowCount() + " dịch vụ");
        } catch (SQLException e) {
            hienThiLoi("Không thể tải danh sách dịch vụ: " + e.getMessage());
        }
    }

    private void hienThiDanhSach(List<DichVu> danhSachDichVu) {
        modelDichVu.setRowCount(0);
        for (DichVu dichVu : danhSachDichVu) {
            modelDichVu.addRow(new Object[] {
                    dichVu.getMaDV(),
                    dichVu.getTenDV(),
                    df.format(dichVu.getGiaDV()),
                    dichVu.getMoTaDV()
            });
        }
    }

    private void hienThiLoi(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(new Color(190, 55, 55));
        JOptionPane.showMessageDialog(this, message, "Quản lý dịch vụ", JOptionPane.ERROR_MESSAGE);
    }

    private DichVu docThongTinDichVu() {
        String maDV = txtMaDichVu.getText().trim();
        String tenDV = txtTenDichVu.getText().trim();
        String giaDVText = txtGiaTien.getText().trim();

        if (maDV == null || maDV.isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống");
        }
        if (tenDV == null || tenDV.isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống");
        }
        Double giaDV = null;
        if (!giaDVText.isEmpty()) {
            try {
                giaDV = Double.valueOf(giaDVText);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Giá tiền phải là số");
            }
            if (giaDV < 0) {
                throw new IllegalArgumentException("Giá tiền không được âm");
            }
        }
        DichVu dichVu = new DichVu();
        dichVu.setMaDV(maDV);
        dichVu.setTenDV(tenDV);
        dichVu.setGiaDV(giaDV);
        dichVu.setMoTaDV(giaTriRongLaNull(txtMoTa.getText()));

        return dichVu;
    }

    private String giaTriRongLaNull(String text) {
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void themDichVu() {
        try {
            DichVu dichVu = docThongTinDichVu();
            if (dichVuDao.insert(dichVu)) {
                napDanhSachDichVu();
                lamMoiForm();
                lblThongBao.setText("Đã thêm dịch vụ " + dichVu.getMaDV());
            }
        } catch (IllegalArgumentException e) {
            hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            hienThiLoi("Không thể thêm dịch vụ: " + e.getMessage());
        }
    }

    private void capNhatDichVu() {
        try {
            DichVu dichVu = docThongTinDichVu();
            if (dichVuDao.update(dichVu)) {
                napDanhSachDichVu();
                lblThongBao.setText("Đã cập nhật dịch vụ " + dichVu.getMaDV());
            } else {
                hienThiLoi("Không tìm thấy dịch vụ cần cập nhật");
            }
        } catch (IllegalArgumentException e) {
            hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            hienThiLoi("Không thể cập nhật dịch vụ: " + e.getMessage());
        }
    }

    private void xoaDichVu() {
        String maDV = txtMaDichVu.getText().trim();
        if (maDV == null || maDV.isEmpty()) {
            hienThiLoi("Mã dịch vụ không được để trống");
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa dịch vụ " + maDV + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (dichVuDao.deleteById(maDV)) {
                napDanhSachDichVu();
                lamMoiForm();
                lblThongBao.setText("Đã xóa dịch vụ " + maDV);
            } else {
                hienThiLoi("Không tìm thấy dịch vụ cần xóa");
            }
        } catch (SQLException e) {
            hienThiLoi("Không thể xóa dịch vụ " + e.getMessage());
        }
    }

    private void lamMoiForm() {
        txtMaDichVu.setText(phatSinhMaDV());
        txtTenDichVu.setText("");
        txtGiaTien.setText("");
        txtMoTa.setText("");
        tblDichVu.clearSelection();

        txtTenDichVu.requestFocusInWindow();

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDel.setEnabled(false);
    }

    private void hienThiDichVuDaChon() {
        int selectedRow = tblDichVu.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = tblDichVu.convertRowIndexToModel(selectedRow);
        txtMaDichVu.setText(cellText(modelDichVu.getValueAt(modelRow, 0)));
        txtTenDichVu.setText(cellText(modelDichVu.getValueAt(modelRow, 1)));
        String giaStr = cellText(modelDichVu.getValueAt(modelRow, 2));
        txtGiaTien.setText(cellText(giaStr.replaceAll("[^0-9]", "")));
        txtMoTa.setText(cellText(modelDichVu.getValueAt(modelRow, 3)));

        btnAdd.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnDel.setEnabled(true);
    }

    private String cellText(Object value) {
        return value == null ? "" : value.toString();
    }

    private String phatSinhMaDV() {
        List<String> danhSachMa = new ArrayList<>();
        for (int i = 0; i < modelDichVu.getRowCount(); i++) {
            danhSachMa.add(cellText(modelDichVu.getValueAt(i, 0)));
        }
        return MaTuDong.sinhMa(
                MaTuDong.TIEN_TO_DICH_VU,
                danhSachMa,
                ma -> ma);
    }
}

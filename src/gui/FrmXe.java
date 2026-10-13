package gui;

import dao.KhachHangDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.Xe;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class FrmXe extends JPanel {
    private static final Color BLUE = new Color(36, 99, 235);
    private static final Color PALE_BLUE = new Color(242, 247, 255);
    private static final Color TEXT = new Color(47, 62, 84);
    private static final Color MUTED = new Color(112, 128, 150);
    private static final Color BORDER = new Color(221, 231, 245);

    private final XeDAO xeDAO = new XeDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    private final JTextField txtMaXe = new JTextField(16);
    private final JTextField txtBienSo = new JTextField(16);
    private final JTextField txtHangXe = new JTextField(16);
    private final JTextField txtDongXe = new JTextField(16);
    private final JTextField txtNamSanXuat = new JTextField(16);
    private final JTextField txtSoKhung = new JTextField(16);
    private final JTextField txtSoMay = new JTextField(16);
    private final JComboBox<KhachHang> cboKhachHang = new JComboBox<>();
    private final JTextField txtTimKiem = new JTextField(22);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");
    private final DefaultTableModel modelXe = new DefaultTableModel(
            new Object[] {"Mã xe", "Biển số", "Hãng xe", "Dòng xe", "Năm SX", "Số khung", "Số máy", "Mã KH"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblXe = new JTable(modelXe);

    public FrmXe() {
        setLayout(new BorderLayout(0, 16));
        setBackground(PALE_BLUE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        add(createHeading(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        configureComponents();
        napKhachHang();
        napDanhSachXe();
    }

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 5));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ XE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblTitle.setForeground(new Color(32, 55, 92));
        JLabel lblSubtitle = new JLabel("Thông tin xe và danh sách xe đã tiếp nhận");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormPanel(), BorderLayout.NORTH);
        pnlContent.add(createTablePanel(), BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormPanel() {
        JPanel pnlFormCard = createCardPanel();
        pnlFormCard.setLayout(new BorderLayout(0, 12));

        JLabel lblFormTitle = createSectionTitle("Thông tin xe");
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã xe *", txtMaXe, 0, 0);
        addField(pnlFields, "Biển số *", txtBienSo, 2, 0);
        addField(pnlFields, "Hãng xe", txtHangXe, 0, 1);
        addField(pnlFields, "Dòng xe", txtDongXe, 2, 1);
        addField(pnlFields, "Năm sản xuất", txtNamSanXuat, 0, 2);
        addField(pnlFields, "Khách hàng *", cboKhachHang, 2, 2);
        addField(pnlFields, "Số khung", txtSoKhung, 0, 3);
        addField(pnlFields, "Số máy", txtSoMay, 2, 3);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton btnThem = createButton("Thêm xe", BLUE, Color.WHITE);
        JButton btnSua = createButton("Cập nhật", new Color(38, 132, 93), Color.WHITE);
        JButton btnXoa = createButton("Xóa xe", new Color(210, 65, 65), Color.WHITE);
        JButton btnLamMoi = createButton("Làm mới", new Color(235, 241, 250), TEXT);
        btnThem.addActionListener(event -> themXe());
        btnSua.addActionListener(event -> capNhatXe());
        btnXoa.addActionListener(event -> xoaXe());
        btnLamMoi.addActionListener(event -> lamMoiForm());
        pnlButtons.add(btnThem, buttonConstraints(0));
        pnlButtons.add(btnSua, buttonConstraints(1));
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

    private JPanel createTablePanel() {
        JPanel pnlTableCard = createCardPanel();
        pnlTableCard.setLayout(new BorderLayout(0, 12));

        JPanel pnlTableHeader = new JPanel(new BorderLayout(12, 0));
        pnlTableHeader.setOpaque(false);
        JLabel lblTableTitle = createSectionTitle("Danh sách xe đã tiếp nhận");
        JPanel pnlSearch = new JPanel(new GridBagLayout());
        pnlSearch.setOpaque(false);
        JButton btnTimKiem = createButton("Tìm kiếm", BLUE, Color.WHITE);
        JButton btnTaiLai = createButton("Tải lại", new Color(235, 241, 250), TEXT);
        btnTimKiem.addActionListener(event -> timKiemXe());
        btnTaiLai.addActionListener(event -> {
            txtTimKiem.setText("");
            napDanhSachXe();
        });
        txtTimKiem.addActionListener(event -> timKiemXe());

        GridBagConstraints gbcSearchField = new GridBagConstraints();
        gbcSearchField.gridx = 0;
        gbcSearchField.gridy = 0;
        gbcSearchField.weightx = 1;
        gbcSearchField.fill = GridBagConstraints.HORIZONTAL;
        gbcSearchField.insets = new Insets(0, 0, 0, 8);
        pnlSearch.add(txtTimKiem, gbcSearchField);
        GridBagConstraints gbcSearchButton = new GridBagConstraints();
        gbcSearchButton.gridx = 1;
        gbcSearchButton.gridy = 0;
        gbcSearchButton.insets = new Insets(0, 0, 0, 8);
        pnlSearch.add(btnTimKiem, gbcSearchButton);
        GridBagConstraints gbcReloadButton = new GridBagConstraints();
        gbcReloadButton.gridx = 2;
        gbcReloadButton.gridy = 0;
        pnlSearch.add(btnTaiLai, gbcReloadButton);

        pnlTableHeader.add(lblTableTitle, BorderLayout.WEST);
        pnlTableHeader.add(pnlSearch, BorderLayout.EAST);

        JScrollPane scrXe = new JScrollPane(tblXe);
        scrXe.setBorder(BorderFactory.createLineBorder(BORDER));
        scrXe.getViewport().setBackground(Color.WHITE);

        tblXe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblXe.setRowHeight(30);
        tblXe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblXe.setForeground(TEXT);
        tblXe.setGridColor(new Color(232, 238, 247));
        tblXe.setSelectionBackground(new Color(225, 239, 255));
        tblXe.setSelectionForeground(TEXT);
        tblXe.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblXe.getTableHeader().setForeground(new Color(32, 55, 92));
        tblXe.getTableHeader().setBackground(new Color(239, 245, 253));
        tblXe.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tblXe.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblXe.getSelectedRow() >= 0) {
                hienThiXeDaChon();
            }
        });

        pnlTableCard.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableCard.add(scrXe, BorderLayout.CENTER);
        return pnlTableCard;
    }

    private JPanel createStatusBar() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        lblThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBao.setForeground(MUTED);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private JPanel createCardPanel() {
        JPanel pnlCard = new JPanel();
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        return pnlCard;
    }

    private JLabel createSectionTitle(String text) {
        JLabel lblTitle = new JLabel(text);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(32, 55, 92));
        return lblTitle;
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

    private void configureComponents() {
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimKiem.setToolTipText("Tìm theo mã xe, biển số, hãng, dòng xe, số khung hoặc số máy");
        cboKhachHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang) {
                    KhachHang khachHang = (KhachHang) value;
                    setText(khachHang.getMaKH() + " - " + khachHang.getHoTen());
                }
                return this;
            }
        });
    }

    private void napKhachHang() {
        try {
            List<KhachHang> danhSachKhachHang = khachHangDAO.findAll();
            cboKhachHang.setModel(new DefaultComboBoxModel<>(danhSachKhachHang.toArray(new KhachHang[0])));
            if (danhSachKhachHang.isEmpty()) {
                hienThiLoi("Chưa có khách hàng. Hãy thêm khách hàng trước khi nhập xe.");
            }
        } catch (SQLException exception) {
            hienThiLoi("Không thể tải danh sách khách hàng: " + exception.getMessage());
        }
    }

    private void napDanhSachXe() {
        try {
            hienThiDanhSach(xeDAO.findAll());
            lblThongBao.setText("Đã tải " + modelXe.getRowCount() + " xe");
        } catch (SQLException exception) {
            hienThiLoi("Không thể tải danh sách xe: " + exception.getMessage());
        }
    }

    private void hienThiDanhSach(List<Xe> danhSachXe) {
        modelXe.setRowCount(0);
        for (Xe xe : danhSachXe) {
            modelXe.addRow(new Object[] {
                    xe.getMaXe(), xe.getBienSo(), xe.getHangXe(), xe.getDongXe(),
                    xe.getNamSanXuat(), xe.getSoKhung(), xe.getSoMay(), xe.getMaKH()
            });
        }
    }

    private void themXe() {
        try {
            Xe xe = docThongTinXe();
            if (xeDAO.insert(xe)) {
                napDanhSachXe();
                lamMoiForm();
                lblThongBao.setText("Đã thêm xe " + xe.getMaXe());
            }
        } catch (IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            hienThiLoi("Không thể thêm xe: " + exception.getMessage());
        }
    }

    private void capNhatXe() {
        try {
            Xe xe = docThongTinXe();
            if (xeDAO.update(xe)) {
                napDanhSachXe();
                lblThongBao.setText("Đã cập nhật xe " + xe.getMaXe());
            } else {
                hienThiLoi("Không tìm thấy xe cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            hienThiLoi("Không thể cập nhật xe: " + exception.getMessage());
        }
    }

    private void xoaXe() {
        String maXe = txtMaXe.getText().trim();
        if (maXe.isEmpty()) {
            hienThiLoi("Hãy chọn xe trong bảng hoặc nhập mã xe cần xóa.");
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa xe " + maXe + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (xeDAO.deleteById(maXe)) {
                napDanhSachXe();
                lamMoiForm();
                lblThongBao.setText("Đã xóa xe " + maXe);
            } else {
                hienThiLoi("Không tìm thấy xe cần xóa.");
            }
        } catch (SQLException exception) {
            hienThiLoi("Không thể xóa xe: " + exception.getMessage());
        }
    }

    private void timKiemXe() {
        try {
            List<Xe> ketQua = xeDAO.search(txtTimKiem.getText());
            hienThiDanhSach(ketQua);
            lblThongBao.setText("Tìm thấy " + ketQua.size() + " xe");
        } catch (SQLException exception) {
            hienThiLoi("Không thể tìm kiếm xe: " + exception.getMessage());
        }
    }

    private Xe docThongTinXe() {
        String maXe = txtMaXe.getText().trim();
        String bienSo = txtBienSo.getText().trim();
        KhachHang khachHang = (KhachHang) cboKhachHang.getSelectedItem();
        if (maXe.isEmpty() || bienSo.isEmpty()) {
            throw new IllegalArgumentException("Mã xe và biển số không được để trống.");
        }
        if (khachHang == null) {
            throw new IllegalArgumentException("Hãy chọn khách hàng sở hữu xe.");
        }

        Integer namSanXuat = null;
        String namSanXuatText = txtNamSanXuat.getText().trim();
        if (!namSanXuatText.isEmpty()) {
            try {
                namSanXuat = Integer.valueOf(namSanXuatText);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Năm sản xuất phải là số nguyên.");
            }
        }
        return new Xe(
                maXe,
                bienSo,
                giaTriRongLaNull(txtHangXe.getText()),
                giaTriRongLaNull(txtDongXe.getText()),
                namSanXuat,
                giaTriRongLaNull(txtSoKhung.getText()),
                giaTriRongLaNull(txtSoMay.getText()),
                khachHang.getMaKH());
    }

    private String giaTriRongLaNull(String value) {
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private void hienThiXeDaChon() {
        int selectedRow = tblXe.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = tblXe.convertRowIndexToModel(selectedRow);
        txtMaXe.setText(String.valueOf(modelXe.getValueAt(modelRow, 0)));
        txtBienSo.setText(cellText(modelXe.getValueAt(modelRow, 1)));
        txtHangXe.setText(cellText(modelXe.getValueAt(modelRow, 2)));
        txtDongXe.setText(cellText(modelXe.getValueAt(modelRow, 3)));
        txtNamSanXuat.setText(cellText(modelXe.getValueAt(modelRow, 4)));
        txtSoKhung.setText(cellText(modelXe.getValueAt(modelRow, 5)));
        txtSoMay.setText(cellText(modelXe.getValueAt(modelRow, 6)));

        String maKhachHang = cellText(modelXe.getValueAt(modelRow, 7));
        for (int index = 0; index < cboKhachHang.getItemCount(); index++) {
            KhachHang khachHang = cboKhachHang.getItemAt(index);
            if (khachHang.getMaKH().equals(maKhachHang)) {
                cboKhachHang.setSelectedIndex(index);
                break;
            }
        }
    }

    private String cellText(Object value) {
        return value == null ? "" : value.toString();
    }

    private void lamMoiForm() {
        txtMaXe.setText("");
        txtBienSo.setText("");
        txtHangXe.setText("");
        txtDongXe.setText("");
        txtNamSanXuat.setText("");
        txtSoKhung.setText("");
        txtSoMay.setText("");
        if (cboKhachHang.getItemCount() > 0) {
            cboKhachHang.setSelectedIndex(0);
        }
        tblXe.clearSelection();
        txtMaXe.requestFocusInWindow();
    }

    private JButton createButton(String text, Color background, Color foreground) {
        JButton btnButton = new JButton(text);
        btnButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnButton.setBackground(background);
        btnButton.setForeground(foreground);
        btnButton.setFocusPainted(false);
        btnButton.setBorder(new EmptyBorder(8, 13, 8, 13));
        btnButton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return btnButton;
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

    private void hienThiLoi(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(new Color(190, 55, 55));
        JOptionPane.showMessageDialog(this, message, "Quản lý xe", JOptionPane.ERROR_MESSAGE);
    }
}

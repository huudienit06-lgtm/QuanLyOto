package gui;

import dao.KhachHangDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.Xe;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;

public class FrmXe extends JPanel {
    private final XeDAO xeDAO = new XeDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    private final AppTextField txtMaXe = new AppTextField(16);
    private final AppTextField txtBienSo = new AppTextField(16);
    private final AppTextField txtHangXe = new AppTextField(16);
    private final AppTextField txtDongXe = new AppTextField(16);
    private final AppTextField txtNamSanXuat = new AppTextField(16);
    private final AppTextField txtSoKhung = new AppTextField(16);
    private final AppTextField txtSoMay = new AppTextField(16);
    private final AppComboBox<KhachHang> cboKhachHang = new AppComboBox<>();
    private final AppTextField txtTimKiem = new AppTextField(22);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");
    private final DefaultTableModel modelXe = new DefaultTableModel(
            new Object[] {"Mã xe", "Biển số", "Hãng xe", "Dòng xe", "Năm SX", "Số khung", "Số máy", "Mã KH"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final AppTable tblXe = new AppTable(modelXe);

    public FrmXe() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
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
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new AppLabel("Thông tin xe và danh sách xe đã tiếp nhận", AppLabel.Style.MUTED);

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
        AppButton btnThem = new AppButton("Thêm xe", AppButton.Style.PRIMARY);
        AppButton btnSua = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
        AppButton btnXoa = new AppButton("Xóa xe", AppButton.Style.DANGER);
        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
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
        AppButton btnTimKiem = new AppButton("Tìm kiếm", AppButton.Style.PRIMARY);
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
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

        AppScrollPane scrXe = new AppScrollPane(tblXe);

        tblXe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private JPanel createCardPanel() {
        JPanel pnlCard = AppPanel.card(new BorderLayout());
        return pnlCard;
    }

    private JLabel createSectionTitle(String text) {
        JLabel lblTitle = new JLabel(text);
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        return lblTitle;
    }

    private void addField(JPanel pnlFields, String label, Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lblField = new JLabel(label);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_TEXT);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlFields.add(component, gbcInput);
    }

    private void configureComponents() {
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

    private GridBagConstraints buttonConstraints(int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        return gbc;
    }

    private void hienThiLoi(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(new Color(190, 55, 55));
        JOptionPane.showMessageDialog(this, message, "Quản lý xe", JOptionPane.ERROR_MESSAGE);
    }
}

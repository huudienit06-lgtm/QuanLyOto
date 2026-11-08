package gui;

import controller.TaiKhoanController;
import entity.NhanVien;
import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmQuanLyTaiKhoan extends JPanel {
    private final JTextField txtMaNV = new JTextField(16);
    private final JTextField txtTenDangNhap = new JTextField(16);
    private final JTextField txtLocMaNV = new JTextField(10);
    private final JTextField txtLocTenDangNhap = new JTextField(12);
    private final JPasswordField pwdMatKhau = new JPasswordField(16);
    private final JPasswordField pwdXacNhanMatKhau = new JPasswordField(16);
    private final char kyTuCheMatKhau = pwdMatKhau.getEchoChar();
    private final JCheckBox chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
    private final JComboBox<String> cboVaiTro = new JComboBox<>();
    private final JComboBox<String> cboTrangThai = new JComboBox<>();
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final JButton btnThem = new JButton("Thêm tài khoản");
    private final JButton btnCapNhat = new JButton("Cập nhật");
    private final JButton btnXoa = new JButton("Xóa");
    private final JButton btnDatLaiMatKhau = new JButton("Đặt lại mật khẩu");
    private final JButton btnLamMoi = new JButton("Làm mới");
    private final JButton btnTaiLai = new JButton("Tải lại");

    private final DefaultTableModel modelTaiKhoan = new DefaultTableModel(
            new Object[] {"Mã NV", "Họ tên", "Tên đăng nhập", "Vai trò", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblTaiKhoan = new JTable(modelTaiKhoan);
    private final TableRowSorter<DefaultTableModel> sorterTaiKhoan = new TableRowSorter<>(modelTaiKhoan);
    private final TaiKhoanController controller;

    public FrmQuanLyTaiKhoan() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);

        JSplitPane splContent = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createFormCard(),
            createTableCard("Danh sách tài khoản", createSearchToolbar(), new JScrollPane(tblTaiKhoan))
        );
        splContent.setBorder(null);
        splContent.setResizeWeight(0.42);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        SwingUtilities.invokeLater(() -> splContent.setDividerLocation(0.42));

        pnlContent.add(splContent, BorderLayout.CENTER);
        JPanel pnlPage = new JPanel(new BorderLayout(0, 16));
        pnlPage.setBackground(UIConfig.COLOR_BACKGROUND);
        pnlPage.setBorder(new EmptyBorder(22, 28, 22, 28));
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 5));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ TÀI KHOẢN");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new JLabel(
            "Quản lý người dùng, vai trò và trạng thái tài khoản trong hệ thống gara");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSubtitle, BorderLayout.CENTER);
        pnlPage.add(pnlHeader, BorderLayout.NORTH);
        pnlPage.add(pnlContent, BorderLayout.CENTER);
        add(pnlPage, BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        khoiTaoGiaoDien();
        controller = new TaiKhoanController(this);
        controller.khoiTao();
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);

        JPanel pnlLoc = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã nhân viên", txtLocMaNV));
        pnlLoc.add(createSearchField("Tên đăng nhập", txtLocTenDangNhap));

        pnlSearch.add(pnlLoc, BorderLayout.CENTER);
        pnlSearch.add(btnTaiLai, BorderLayout.EAST);
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

    private JPanel createStatusPanel() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private void khoiTaoGiaoDien() {
        JTextField[] textFields = { txtMaNV, txtTenDangNhap, txtLocMaNV, txtLocTenDangNhap };
        for (JTextField textField : textFields) {
            textField.setFont(UIConfig.FONT_REGULAR);
            textField.setForeground(UIConfig.COLOR_TEXT);
            textField.setBackground(UIConfig.COLOR_SURFACE);
            textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIConfig.COLOR_BORDER, 1), new EmptyBorder(6, 9, 6, 9)));
        }
        JPasswordField[] passwordFields = { pwdMatKhau, pwdXacNhanMatKhau };
        for (JPasswordField passwordField : passwordFields) {
            passwordField.setFont(UIConfig.FONT_REGULAR);
            passwordField.setForeground(UIConfig.COLOR_TEXT);
            passwordField.setBackground(UIConfig.COLOR_SURFACE);
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIConfig.COLOR_BORDER, 1), new EmptyBorder(6, 9, 6, 9)));
        }
        cboVaiTro.setFont(UIConfig.FONT_REGULAR);
        cboTrangThai.setFont(UIConfig.FONT_REGULAR);
        chkHienMatKhau.setFont(UIConfig.FONT_REGULAR);
        chkHienMatKhau.setForeground(UIConfig.COLOR_TEXT);
        chkHienMatKhau.setOpaque(false);
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        JButton[] buttons = { btnThem, btnCapNhat, btnXoa, btnDatLaiMatKhau, btnLamMoi, btnTaiLai };
        Color[] mauNen = { UIConfig.COLOR_PRIMARY, UIConfig.COLOR_SUCCESS, UIConfig.COLOR_DANGER,
                UIConfig.COLOR_SECONDARY, UIConfig.COLOR_SECONDARY, UIConfig.COLOR_SECONDARY };
        for (int index = 0; index < buttons.length; index++) {
            buttons[index].setFont(UIConfig.FONT_BOLD);
            buttons[index].setBackground(mauNen[index]);
            buttons[index].setForeground(index < 3 ? Color.WHITE : UIConfig.COLOR_TEXT);
            buttons[index].setOpaque(true);
            buttons[index].setBorderPainted(false);
            buttons[index].setFocusPainted(false);
        }
        tblTaiKhoan.setFont(UIConfig.FONT_REGULAR);
        tblTaiKhoan.setForeground(UIConfig.COLOR_TEXT);
        tblTaiKhoan.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblTaiKhoan.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblTaiKhoan.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblTaiKhoan.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblTaiKhoan.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblTaiKhoan.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblTaiKhoan.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        cboVaiTro.setModel(new DefaultComboBoxModel<>(new String[] {
                "Chọn vai trò", "Quản lý", "Nhân viên", "Lễ tân", "Kỹ thuật viên", "Thu ngân"
        }));
        cboTrangThai.setModel(new DefaultComboBoxModel<>(new String[] {
                "Chọn trạng thái", "Hoạt động", "Không hoạt động", "Tạm khóa"
        }));
        tblTaiKhoan.setRowSorter(sorterTaiKhoan);
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.setRowHeight(30);
        tblTaiKhoan.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTaiKhoan.setFillsViewportHeight(true);
        int[] doRongCot = {110, 190, 170, 170, 130};
        for (int cot = 0; cot < doRongCot.length; cot++) {
            tblTaiKhoan.getColumnModel().getColumn(cot).setPreferredWidth(doRongCot[cot]);
        }
    }

    private JPanel createFormCard() {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 6));
        pnlCard.setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIConfig.COLOR_BORDER, 1), new EmptyBorder(14, 16, 14, 16)));
        pnlCard.setLayout(new BorderLayout(0, 6));

        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã nhân viên *", txtMaNV, 0, 0);
        addField(pnlFields, "Tên đăng nhập *", txtTenDangNhap, 2, 0);
        addField(pnlFields, "Mật khẩu *", pwdMatKhau, 0, 1);
        addField(pnlFields, "Xác nhận mật khẩu *", pwdXacNhanMatKhau, 2, 1);
        addField(pnlFields, "Vai trò", cboVaiTro, 0, 2);
        addField(pnlFields, "Trạng thái", cboTrangThai, 2, 2);
        addField(pnlFields, "Bảo mật", chkHienMatKhau, 0, 3);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton[] buttons = { btnThem, btnCapNhat, btnXoa, btnDatLaiMatKhau, btnLamMoi };
        for (int index = 0; index < buttons.length; index++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = index;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 8);
            pnlButtons.add(buttons[index], gbc);
        }
        JLabel lblSection = new JLabel("Thông tin tài khoản");
        lblSection.setFont(UIConfig.FONT_SECTION);
        lblSection.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblSection, BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createTableCard(String title, JPanel toolbar, JScrollPane scrTable) {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 12));
        pnlCard.setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1), new EmptyBorder(14, 16, 14, 16)));
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(toolbar, BorderLayout.EAST);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(pnlHeader, BorderLayout.NORTH);
        pnlCard.add(scrTable, BorderLayout.CENTER);
        return pnlCard;
    }

    private void addField(JPanel pnlFields, String text, java.awt.Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lblField = new JLabel(text);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_MUTED);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlFields.add(component, gbcInput);
    }

    public void hienThiDanhSachTaiKhoan(List<TaiKhoan> danhSach) {
        modelTaiKhoan.setRowCount(0);
        for (TaiKhoan taiKhoan : danhSach) {
            NhanVien nhanVien = taiKhoan.getNhanVien();
            modelTaiKhoan.addRow(new Object[] {
                    nhanVien == null ? "" : nhanVien.getMaNV(),
                    nhanVien == null ? "" : nhanVien.getHoTen(),
                    taiKhoan.getTenDangNhap(),
                    taiKhoan.getVaiTro(),
                    taiKhoan.getTrangThai()
            });
        }
        thongBao("Đã tải " + modelTaiKhoan.getRowCount() + " tài khoản");
    }

    public void locDanhSachTaiKhoan() {
        String maNV = txtLocMaNV.getText().trim().toLowerCase();
        String tenDangNhap = txtLocTenDangNhap.getText().trim().toLowerCase();
        sorterTaiKhoan.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String rowMaNV = String.valueOf(entry.getValue(0)).trim().toLowerCase();
                String rowTenDangNhap = String.valueOf(entry.getValue(2)).trim().toLowerCase();
                return (maNV.isEmpty() || rowMaNV.contains(maNV))
                        && (tenDangNhap.isEmpty() || rowTenDangNhap.contains(tenDangNhap));
            }
        });
    }

    public String getMaNVDangChon() {
        int rowView = tblTaiKhoan.getSelectedRow();
        if (rowView < 0) {
            return null;
        }
        int rowModel = tblTaiKhoan.convertRowIndexToModel(rowView);
        return String.valueOf(modelTaiKhoan.getValueAt(rowModel, 0));
    }

    public void hienThiTaiKhoan(TaiKhoan taiKhoan) {
        txtMaNV.setEditable(false);
        txtMaNV.setText(taiKhoan.getNhanVien().getMaNV());
        txtTenDangNhap.setText(taiKhoan.getTenDangNhap());
        pwdMatKhau.setText(taiKhoan.getMatKhau());
        pwdXacNhanMatKhau.setText(taiKhoan.getMatKhau());
        capNhatHienThiMatKhau();
        cboVaiTro.setSelectedItem(taiKhoan.getVaiTro() != null ? taiKhoan.getVaiTro() : "Chọn vai trò");
        cboTrangThai.setSelectedItem(taiKhoan.getTrangThai() != null ? taiKhoan.getTrangThai() : "Chọn trạng thái");
        thongBao("Đang chỉnh sửa tài khoản: " + taiKhoan.getTenDangNhap());
    }

    public void lamMoiForm() {
        txtMaNV.setEditable(true);
        txtMaNV.setText("");
        txtTenDangNhap.setText("");
        pwdMatKhau.setText("");
        pwdXacNhanMatKhau.setText("");
        chkHienMatKhau.setSelected(false);
        capNhatHienThiMatKhau();
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        tblTaiKhoan.clearSelection();
        thongBao("Sẵn sàng");
    }

    public void capNhatHienThiMatKhau() {
        char kyTuHienThi = chkHienMatKhau.isSelected() ? (char) 0 : kyTuCheMatKhau;
        pwdMatKhau.setEchoChar(kyTuHienThi);
        pwdXacNhanMatKhau.setEchoChar(kyTuHienThi);
    }

    public boolean validateForm(boolean batBuocMatKhau) {
        String maNV = getMaNVNhap();
        String tenDangNhap = getTenDangNhapNhap();
        String matKhau = getMatKhauNhap();
        String xacNhanMatKhau = new String(pwdXacNhanMatKhau.getPassword()).trim();

        if (maNV.isEmpty()) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập mã nhân viên.");
            return false;
        }
        if (tenDangNhap.isEmpty()) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập tên đăng nhập.");
            return false;
        }
        if (cboVaiTro.getSelectedIndex() <= 0) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng chọn vai trò cho tài khoản.");
            return false;
        }
        if (cboTrangThai.getSelectedIndex() <= 0) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng chọn trạng thái tài khoản.");
            return false;
        }
        if (batBuocMatKhau && matKhau.isEmpty()) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập mật khẩu.");
            return false;
        }
        if (!matKhau.isEmpty() && !matKhau.equals(xacNhanMatKhau)) {
            hienThiCanhBao("Sai thông tin", "Mật khẩu xác nhận không khớp.");
            return false;
        }
        if (!matKhau.isEmpty() && matKhau.length() < 6) {
            hienThiCanhBao("Sai thông tin", "Mật khẩu phải có ít nhất 6 ký tự.");
            return false;
        }
        return true;
    }

    public String getMaNVNhap() { return txtMaNV.getText().trim(); }
    public String getTenDangNhapNhap() { return txtTenDangNhap.getText().trim(); }
    public String getMatKhauNhap() { return new String(pwdMatKhau.getPassword()).trim(); }
    public String getVaiTroNhap() { return String.valueOf(cboVaiTro.getSelectedItem()); }
    public String getTrangThaiNhap() { return String.valueOf(cboTrangThai.getSelectedItem()); }

    public void xoaMatKhauNhap() {
        pwdMatKhau.setText("");
        pwdXacNhanMatKhau.setText("");
    }

    public boolean xacNhanXoa(String maNV) {
        return JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa tài khoản của nhân viên " + maNV + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public boolean xacNhanDatLaiMatKhau(String maNV) {
        return JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn đặt lại mật khẩu cho tài khoản " + maNV + " về mặc định không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    public void hienThiLoi(String tieuDe, String message) {
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        lblThongBao.setText("Lỗi dữ liệu");
        JOptionPane.showMessageDialog(this, message, tieuDe, JOptionPane.ERROR_MESSAGE);
    }

    public void hienThiCanhBao(String tieuDe, String message) {
        JOptionPane.showMessageDialog(this, message, tieuDe, JOptionPane.WARNING_MESSAGE);
    }

    public void hienThiThanhCong(String message) {
        JOptionPane.showMessageDialog(this, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public void hienThiThatBai(String message) {
        JOptionPane.showMessageDialog(this, message, "Thất bại", JOptionPane.ERROR_MESSAGE);
    }

    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnCapNhat() { return btnCapNhat; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnDatLaiMatKhau() { return btnDatLaiMatKhau; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JButton getBtnTaiLai() { return btnTaiLai; }
    public JCheckBox getChkHienMatKhau() { return chkHienMatKhau; }
    public JTextField getTxtLocMaNV() { return txtLocMaNV; }
    public JTextField getTxtLocTenDangNhap() { return txtLocTenDangNhap; }
    public javax.swing.JTable getTblTaiKhoan() { return tblTaiKhoan; }
}

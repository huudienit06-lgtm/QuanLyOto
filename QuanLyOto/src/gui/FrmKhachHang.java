package gui;

import controller.KhachHangController;
import entity.KhachHang;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmKhachHang extends JPanel {
    private final boolean cheDoTaoMoi;
    private final KhachHangController controller;
    private javax.swing.JDialog dialogTaoKhachHang;
    private String maKHVuaTao;

    public FrmKhachHang() {
        this(false);
    }

    private final JTextField txtMaKH = createTextField(16);
    private final JTextField txtHoTen = createTextField(16);
    private final JTextField txtSoDienThoai = createTextField(16);
    private final JTextField txtEmail = createTextField(16);
    private final JTextField txtDiaChi = createTextField(16);
    private final JTextField txtCongNo = createTextField(16);
    private final JTextField txtLocMaKH = createTextField(10);
    private final JTextField txtLocHoTen = createTextField(12);
    private final JTextField txtLocSoDienThoai = createTextField(12);
    private final JTextField txtLocEmail = createTextField(12);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");
    private String maKHDangChon;

    private final DefaultTableModel modelKhachHang = new DefaultTableModel(
            new Object[] { "Mã khách hàng", "Họ và tên", "Số điện thoại", "Email", "Địa chỉ", "Công nợ" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblKhachHang = new JTable(modelKhachHang);
    private final TableRowSorter<DefaultTableModel> sorterKhachHang = new TableRowSorter<>(modelKhachHang);

    private final JButton btnThem = createButton(
            "Thêm mới", "/resources/icons/plus.png", UIConfig.COLOR_PRIMARY, Color.WHITE);
    private final JButton btnCapNhat = createButton(
            "Cập nhật", "/resources/icons/update.png", UIConfig.COLOR_SUCCESS, Color.WHITE);
    private final JButton btnXoa = createButton(
            "Xóa", "/resources/icons/trash.png", UIConfig.COLOR_DANGER, Color.WHITE);
    private final JButton btnLamMoi = createButton(
            "Làm mới", "/resources/icons/reset.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
    private final JButton btnTaiLai = createButton(
            "Tải lại", "/resources/icons/reload.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);

    public FrmKhachHang(boolean cheDoTaoMoi) {
        this.cheDoTaoMoi = cheDoTaoMoi;
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        if (cheDoTaoMoi) {
            add(createHeaderPanel(), BorderLayout.NORTH);
            add(createFormCard(), BorderLayout.CENTER);
            add(createStatusPanel(), BorderLayout.SOUTH);
        } else {
            add(createHeaderPanel(), BorderLayout.NORTH);
            JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
            pnlContent.setOpaque(false);
            JSplitPane splContent = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    createFormCard(),
                    createTableCard());
            splContent.setBorder(null);
            splContent.setResizeWeight(0.38);
            splContent.setDividerSize(7);
            splContent.setEnabled(false);
            SwingUtilities.invokeLater(() -> splContent.setDividerLocation(0.38));
            pnlContent.add(splContent, BorderLayout.CENTER);
            add(pnlContent, BorderLayout.CENTER);
            add(createStatusPanel(), BorderLayout.SOUTH);
        }
        txtMaKH.setEditable(false);
        txtCongNo.setEditable(false);
        txtCongNo.setFocusable(false);
        tblKhachHang.setFont(UIConfig.FONT_REGULAR);
        tblKhachHang.setForeground(UIConfig.COLOR_TEXT);
        tblKhachHang.setRowHeight(30);
        tblKhachHang.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblKhachHang.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblKhachHang.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblKhachHang.setRowSorter(sorterKhachHang);
        tblKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblKhachHang.setFillsViewportHeight(true);
        tblKhachHang.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblKhachHang.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblKhachHang.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblKhachHang.getTableHeader().setPreferredSize(new Dimension(0, 34));
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        if (cheDoTaoMoi) {
            btnCapNhat.setVisible(false);
            btnXoa.setVisible(false);
            btnLamMoi.setVisible(false);
            btnTaiLai.setVisible(false);
        }
        controller = new KhachHangController(this);
        controller.khoiTao();
    }

    public boolean isCheDoTaoMoi() {
        return cheDoTaoMoi;
    }

    public void ganDialogTaoKhachHang(javax.swing.JDialog dialog) {
        this.dialogTaoKhachHang = dialog;
    }

    public void dongDialogSauKhiTao(String maKH) {
        this.maKHVuaTao = maKH;

        if (dialogTaoKhachHang != null) {
            dialogTaoKhachHang.dispose();
        }
    }

    public String getMaKHVuaTao() {
        return maKHVuaTao;
    }

    public static String moDialogTaoMoi(Component parent) {
        java.awt.Window owner = SwingUtilities.getWindowAncestor(parent);
        javax.swing.JDialog dialog = new javax.swing.JDialog(
                owner,
                "TẠO KHÁCH HÀNG",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        FrmKhachHang form = new FrmKhachHang(true);
        form.ganDialogTaoKhachHang(dialog);
        dialog.setContentPane(form);
        dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(850, 430);
        dialog.setMinimumSize(new Dimension(750, 380));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return form.getMaKHVuaTao();
    }

    private JPanel createHeaderPanel() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 4));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);

        JLabel lblSubtitle = new JLabel("Thông tin khách hàng và dữ liệu giao dịch của gara");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel createFormCard() {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 12));

        JLabel lblSection = new JLabel("Thông tin khách hàng");
        lblSection.setFont(UIConfig.FONT_SECTION);
        lblSection.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblSection, BorderLayout.NORTH);

        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã khách hàng *", txtMaKH, 0, 0);
        addField(pnlFields, "Họ và tên *", txtHoTen, 2, 0);
        addField(pnlFields, "Số điện thoại *", txtSoDienThoai, 4, 0);
        addField(pnlFields, "Email", txtEmail, 0, 1);
        addField(pnlFields, "Địa chỉ", txtDiaChi, 2, 1);
        addField(pnlFields, "Công nợ", txtCongNo, 4, 1);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton[] buttons = { btnThem, btnCapNhat, btnXoa, btnLamMoi };
        for (int i = 0; i < buttons.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 8);
            pnlButtons.add(buttons[i], gbc);
        }

        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createTableCard() {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách khách hàng");
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        pnlHeader.add(createSearchToolbar(), BorderLayout.EAST);
        pnlCard.add(pnlHeader, BorderLayout.NORTH);

        JScrollPane scrTable = new JScrollPane(tblKhachHang);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(scrTable, BorderLayout.CENTER);

        return pnlCard;
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);
        JPanel pnlLoc = new JPanel(new GridLayout(1, 4, 8, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã khách hàng", txtLocMaKH));
        pnlLoc.add(createSearchField("Họ và tên", txtLocHoTen));
        pnlLoc.add(createSearchField("Số điện thoại", txtLocSoDienThoai));
        pnlLoc.add(createSearchField("Email", txtLocEmail));
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

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(UIConfig.COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)));
        return card;
    }

    private JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UIConfig.FONT_REGULAR);
        tf.setForeground(UIConfig.COLOR_TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                new EmptyBorder(6, 8, 6, 8)));
        return tf;
    }

    private JButton createButton(String text, String iconPath, Color background, Color foreground) {
        JButton btn = new JButton(text);
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        ImageIcon icon = getScaledIcon(iconPath, 16, 16);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(6);
        }
        return btn;
    }

    private ImageIcon getScaledIcon(String path, int width, int height) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon originalIcon = new ImageIcon(url);
                Image img = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void addField(JPanel pnlFields, String labelText, Component component, int col, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = col;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConfig.FONT_REGULAR);
        lbl.setForeground(UIConfig.COLOR_TEXT);
        pnlFields.add(lbl, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = col + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1.0;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlFields.add(component, gbcInput);
    }

    public boolean validateForm() {
        if (!FormValidator.isValidIdentifier(txtMaKH.getText().trim())) {
            hienThiLoi("Mã khách hàng không được để trống và chỉ được chứa chữ, số, gạch ngang hoặc gạch dưới.");
            txtMaKH.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(txtMaKH.getText().trim(), 20)) {
            hienThiLoi("Mã khách hàng không được vượt quá 20 ký tự.");
            txtMaKH.requestFocus();
            return false;
        }
        if (!FormValidator.isValidFullName(txtHoTen.getText().trim())) {
            hienThiLoi("Họ và tên không hợp lệ.");
            txtHoTen.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(txtHoTen.getText().trim(), 100)) {
            hienThiLoi("Họ và tên không được vượt quá 100 ký tự.");
            txtHoTen.requestFocus();
            return false;
        }
        if (txtSoDienThoai.getText().trim().isEmpty()) {
            hienThiLoi("Số điện thoại không được để trống.");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalPhoneNumber(txtSoDienThoai.getText().trim())) {
            hienThiLoi("Số điện thoại phải có định dạng Việt Nam, ví dụ 0912345678 hoặc +84912345678.");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(txtSoDienThoai.getText().trim(), 15)) {
            hienThiLoi("Số điện thoại không được vượt quá 15 ký tự.");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalEmail(txtEmail.getText().trim())) {
            hienThiLoi("Email không hợp lệ.");
            txtEmail.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(txtEmail.getText().trim(), 100)) {
            hienThiLoi("Email không được vượt quá 100 ký tự.");
            txtEmail.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(txtDiaChi.getText().trim(), 255)) {
            hienThiLoi("Địa chỉ không được vượt quá 255 ký tự.");
            txtDiaChi.requestFocus();
            return false;
        }
        return true;
    }

    public void hienThiDanhSach(List<KhachHang> danhSach) {
        modelKhachHang.setRowCount(0);
        for (KhachHang khachHang : danhSach) {
            modelKhachHang.addRow(new Object[] {
                    khachHang.getMaKH(), khachHang.getHoTen(), khachHang.getSoDT(),
                    khachHang.getEmail(), khachHang.getDiaChi(), khachHang.getCongNo()
            });
        }
    }

    public void locDanhSachKhachHang() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        themBoLoc(filters, txtLocMaKH.getText(), 0);
        themBoLoc(filters, txtLocHoTen.getText(), 1);
        themBoLoc(filters, txtLocSoDienThoai.getText(), 2);
        themBoLoc(filters, txtLocEmail.getText(), 3);
        sorterKhachHang.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblThongBao.setText("Hiển thị " + sorterKhachHang.getViewRowCount() + " / "
                + modelKhachHang.getRowCount() + " khách hàng");
    }

    private void themBoLoc(List<RowFilter<Object, Object>> filters, String giaTri, int cot) {
        String tuKhoa = giaTri.trim();
        if (!tuKhoa.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa), cot));
        }
    }

    public void hienThiKhachHangDangChon() {
        int viewRow = tblKhachHang.getSelectedRow();
        int modelRow = tblKhachHang.convertRowIndexToModel(viewRow);
        maKHDangChon = giaTriChuoi(modelKhachHang.getValueAt(modelRow, 0));
        txtMaKH.setText(maKHDangChon);
        txtMaKH.setEnabled(false);
        txtHoTen.setText(giaTriChuoi(modelKhachHang.getValueAt(modelRow, 1)));
        txtSoDienThoai.setText(giaTriChuoi(modelKhachHang.getValueAt(modelRow, 2)));
        txtEmail.setText(giaTriChuoi(modelKhachHang.getValueAt(modelRow, 3)));
        txtDiaChi.setText(giaTriChuoi(modelKhachHang.getValueAt(modelRow, 4)));
        txtCongNo.setText(giaTriChuoi(modelKhachHang.getValueAt(modelRow, 5)));
    }

    private String giaTriChuoi(Object giaTri) {
        return giaTri == null ? "" : giaTri.toString();
    }

    public KhachHang taoKhachHang() {
        return new KhachHang(
                maKHDangChon == null ? txtMaKH.getText().trim() : maKHDangChon,
                txtHoTen.getText().trim(),
                txtSoDienThoai.getText().trim(),
                txtEmail.getText().trim(),
                txtDiaChi.getText().trim(),
                parseCongNo());
    }

    private Double parseCongNo() {
        try {
            String giaTri = txtCongNo.getText().trim();
            return giaTri.isEmpty() ? 0D : Double.valueOf(giaTri);
        } catch (NumberFormatException exception) {
            return 0D;
        }
    }

    public void lamMoiBieuMau(String maMoi) {
        tblKhachHang.clearSelection();
        maKHDangChon = null;
        txtMaKH.setEnabled(true);
        txtMaKH.setText(maMoi);
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");
        txtCongNo.setText("0");
        txtLocMaKH.setText("");
        txtLocHoTen.setText("");
        txtLocSoDienThoai.setText("");
        txtLocEmail.setText("");
    }

    public String getMaKHDangChon() {
        return maKHDangChon;
    }

    public JButton getBtnThem() {
        return btnThem;
    }

    public JButton getBtnCapNhat() {
        return btnCapNhat;
    }

    public JButton getBtnXoa() {
        return btnXoa;
    }

    public JButton getBtnLamMoi() {
        return btnLamMoi;
    }

    public JButton getBtnTaiLai() {
        return btnTaiLai;
    }

    public JTable getTblKhachHang() {
        return tblKhachHang;
    }

    public JTextField getTxtLocMaKH() {
        return txtLocMaKH;
    }

    public JTextField getTxtLocHoTen() {
        return txtLocHoTen;
    }

    public JTextField getTxtLocSoDienThoai() {
        return txtLocSoDienThoai;
    }

    public JTextField getTxtLocEmail() {
        return txtLocEmail;
    }

    public void hienThiLoi(String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void thongBaoThanhCong(String noiDung, String tieuDe) {
        JOptionPane.showMessageDialog(this, noiDung, tieuDe, JOptionPane.INFORMATION_MESSAGE);
    }
}

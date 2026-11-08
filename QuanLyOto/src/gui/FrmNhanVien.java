package gui;

import controller.NhanVienController;
import entity.ChucVu;
import entity.NhanVien;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmNhanVien extends JPanel {
    private final NhanVienController controller = new NhanVienController(this);
    private final javax.swing.JTextField txtMaNV = new javax.swing.JTextField(16);
    private final javax.swing.JTextField txtHoTen = new javax.swing.JTextField(16);
    private final javax.swing.JTextField txtSoDienThoai = new javax.swing.JTextField(16);
    private final javax.swing.JTextField txtEmail = new javax.swing.JTextField(16);
    private final javax.swing.JTextField txtLocMaNV = new javax.swing.JTextField(10);
    private final javax.swing.JTextField txtLocHoTen = new javax.swing.JTextField(12);
    private final javax.swing.JTextField txtLocSoDienThoai = new javax.swing.JTextField(12);
    private final javax.swing.JTextField txtLocEmail = new javax.swing.JTextField(12);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");
    private final javax.swing.JComboBox<ChucVu> cboChucVu = new javax.swing.JComboBox<>();
    private final javax.swing.JComboBox<String> cboTrangThai = new javax.swing.JComboBox<>();
    private String maNVDangChon;
    private final DefaultTableModel modelNhanVien = new DefaultTableModel(
            new Object[] { "Mã nhân viên", "Họ và tên", "Chức vụ", "Số điện thoại", "Email", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final javax.swing.JTable tblNhanVien = new javax.swing.JTable(modelNhanVien);
    private final TableRowSorter<DefaultTableModel> sorterNhanVien = new TableRowSorter<>(modelNhanVien);
    private final JButton btnThem = new JButton("Thêm nhân viên");
    private final JButton btnCapNhat = new JButton("Cập nhật");
    private final JButton btnXoa = new JButton("Xóa");
    private final JButton btnLamMoi = new JButton("Làm mới");
    private final JButton btnTaiLai = new JButton("Tải lại");

    public FrmNhanVien() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        Color[] mauNut = { UIConfig.COLOR_PRIMARY, UIConfig.COLOR_SUCCESS,
            UIConfig.COLOR_DANGER, UIConfig.COLOR_SECONDARY, UIConfig.COLOR_SECONDARY };
        JButton[] buttons = { btnThem, btnCapNhat, btnXoa, btnLamMoi, btnTaiLai };
        for (int index = 0; index < buttons.length; index++) {
            buttons[index].setFont(UIConfig.FONT_BOLD);
            buttons[index].setForeground(index == 3 || index == 4 ? UIConfig.COLOR_TEXT : Color.WHITE);
            buttons[index].setBackground(mauNut[index]);
            buttons[index].setOpaque(true);
            buttons[index].setBorderPainted(false);
            buttons[index].setFocusPainted(false);
        }
        javax.swing.JTextField[] textFields = {
            txtMaNV, txtHoTen, txtSoDienThoai, txtEmail,
            txtLocMaNV, txtLocHoTen, txtLocSoDienThoai, txtLocEmail
        };
        for (javax.swing.JTextField textField : textFields) {
            textField.setFont(UIConfig.FONT_REGULAR);
            textField.setForeground(UIConfig.COLOR_TEXT);
            textField.setBackground(UIConfig.COLOR_SURFACE);
            textField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1), new EmptyBorder(6, 9, 6, 9)));
        }
        cboChucVu.setFont(UIConfig.FONT_REGULAR);
        cboTrangThai.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        txtMaNV.setEditable(false);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);
        JSplitPane splContent = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            createFormCard(), createTableCard("Danh sách nhân viên", createSearchToolbar(),
                new JScrollPane(tblNhanVien)));
        splContent.setBorder(null);
        splContent.setResizeWeight(0.38);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        SwingUtilities.invokeLater(() -> splContent.setDividerLocation(0.38));
        pnlContent.add(splContent, BorderLayout.CENTER);

        JPanel pnlPage = new JPanel(new BorderLayout(0, 16));
        pnlPage.setBackground(UIConfig.COLOR_BACKGROUND);
        pnlPage.setBorder(new EmptyBorder(22, 28, 22, 28));
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 5));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new JLabel("Thông tin nhân viên và danh sách nhân sự của gara");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSubtitle, BorderLayout.CENTER);
        pnlPage.add(pnlHeader, BorderLayout.NORTH);
        pnlPage.add(pnlContent, BorderLayout.CENTER);
        add(pnlPage, BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        tblNhanVien.setRowSorter(sorterNhanVien);
        tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNhanVien.setRowHeight(30);
        tblNhanVien.setFont(UIConfig.FONT_REGULAR);
        tblNhanVien.setForeground(UIConfig.COLOR_TEXT);
        tblNhanVien.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblNhanVien.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblNhanVien.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblNhanVien.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblNhanVien.setFillsViewportHeight(true);
        tblNhanVien.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblNhanVien.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblNhanVien.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblNhanVien.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        int[] doRongCot = { 105, 180, 145, 145, 220, 140 };
        for (int cot = 0; cot < doRongCot.length; cot++) {
            tblNhanVien.getColumnModel().getColumn(cot).setPreferredWidth(doRongCot[cot]);
        }
        controller.khoiTao();
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);
        JPanel pnlLoc = new JPanel(new GridLayout(1, 4, 8, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã nhân viên", txtLocMaNV));
        pnlLoc.add(createSearchField("Họ và tên", txtLocHoTen));
        pnlLoc.add(createSearchField("Số điện thoại", txtLocSoDienThoai));
        pnlLoc.add(createSearchField("Email", txtLocEmail));
        pnlSearch.add(pnlLoc, BorderLayout.CENTER);
        pnlSearch.add(btnTaiLai, BorderLayout.EAST);
        return pnlSearch;
    }

    private JPanel createSearchField(String labelText, JTextField txtLocMaNV2) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new JLabel(labelText);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_MUTED);
        pnlField.add(lblField, BorderLayout.NORTH);
        pnlField.add(txtLocMaNV2, BorderLayout.CENTER);
        return pnlField;
    }

    private JPanel createStatusPanel() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private JPanel createFormCard() {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 6));
        pnlCard.setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIConfig.COLOR_BORDER, 1), new EmptyBorder(14, 16, 14, 16)));
        pnlCard.setLayout(new BorderLayout(0, 6));
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã nhân viên (tự động)", txtMaNV, 0, 0);
        addField(pnlFields, "Họ và tên *", txtHoTen, 2, 0);
        addField(pnlFields, "Chức vụ", cboChucVu, 4, 0);
        addField(pnlFields, "Số điện thoại", txtSoDienThoai, 0, 1);
        addField(pnlFields, "Email", txtEmail, 2, 1);
        addField(pnlFields, "Trạng thái", cboTrangThai, 4, 1);

        cboTrangThai.setModel(new DefaultComboBoxModel<>(
                new String[] { "Đang làm việc", "Tạm nghỉ", "Đã nghỉ việc" }));

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton[] buttons = { btnThem, btnCapNhat, btnXoa, btnLamMoi };
        for (int index = 0; index < buttons.length; index++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = index;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 8);
            pnlButtons.add(buttons[index], gbc);
        }
        JLabel lblSection = new JLabel("Thông tin nhân viên");
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

    public boolean validateForm() {
        String maNV = txtMaNV.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String soDienThoai = txtSoDienThoai.getText().trim();
        String email = txtEmail.getText().trim();
        ChucVu chucVu = (ChucVu) cboChucVu.getSelectedItem();
        String trangThai = (String) cboTrangThai.getSelectedItem();

        if (!FormValidator.isValidIdentifier(maNV)) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtMaNV.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(maNV, 20)) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên không được vượt quá 20 ký tự.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtMaNV.requestFocus();
            return false;
        }
        if (!FormValidator.isValidFullName(hoTen)) {
            JOptionPane.showMessageDialog(this, "Họ và tên không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtHoTen.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(hoTen, 100)) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được vượt quá 100 ký tự.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtHoTen.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalPhoneNumber(soDienThoai)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải có định dạng Việt Nam, ví dụ 0912345678 hoặc +84912345678.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(soDienThoai, 15)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được vượt quá 15 ký tự.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalEmail(email)) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(email, 100)) {
            JOptionPane.showMessageDialog(this, "Email không được vượt quá 100 ký tự.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        if (chucVu == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chức vụ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            cboChucVu.requestFocus();
            return false;
        }
        if (trangThai == null || trangThai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            cboTrangThai.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(trangThai, 20)) {
            JOptionPane.showMessageDialog(this, "Trạng thái không được vượt quá 20 ký tự.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            cboTrangThai.requestFocus();
            return false;
        }

        return true;
    }

    public void hienThiDanhSach(List<NhanVien> danhSachNhanVien) {
        modelNhanVien.setRowCount(0);
        for (NhanVien nhanVien : danhSachNhanVien) {
            modelNhanVien.addRow(new Object[] {
                    nhanVien.getMaNV(), nhanVien.getHoTen(), nhanVien.getChucVu(),
                    nhanVien.getSoDT(), nhanVien.getEmail(), nhanVien.getTrangThai()
            });
        }
    }

    public void hienThiDanhSachChucVu(List<ChucVu> danhSachChucVu) {
        cboChucVu.setModel(new DefaultComboBoxModel<>(danhSachChucVu.toArray(ChucVu[]::new)));
    }

    public void locDanhSachNhanVien() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        themBoLoc(filters, txtLocMaNV.getText(), 0);
        themBoLoc(filters, txtLocHoTen.getText(), 1);
        themBoLoc(filters, txtLocSoDienThoai.getText(), 3);
        themBoLoc(filters, txtLocEmail.getText(), 4);
        sorterNhanVien.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblThongBao.setText("Hiển thị " + sorterNhanVien.getViewRowCount() + " / "
                + modelNhanVien.getRowCount() + " nhân viên");
    }

    private void themBoLoc(List<RowFilter<Object, Object>> filters, String giaTri, int cot) {
        String tuKhoa = giaTri.trim();
        if (!tuKhoa.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa), cot));
        }
    }

    public void hienThiNhanVienDangChon() {
        int viewRow = tblNhanVien.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = tblNhanVien.convertRowIndexToModel(viewRow);
        maNVDangChon = modelNhanVien.getValueAt(modelRow, 0).toString();
        txtMaNV.setText(maNVDangChon);
        txtMaNV.setEnabled(false);
        txtHoTen.setText(giaTriChuoi(modelNhanVien.getValueAt(modelRow, 1)));
        cboChucVu.setSelectedItem(modelNhanVien.getValueAt(modelRow, 2));
        txtSoDienThoai.setText(giaTriChuoi(modelNhanVien.getValueAt(modelRow, 3)));
        txtEmail.setText(giaTriChuoi(modelNhanVien.getValueAt(modelRow, 4)));
        cboTrangThai.setSelectedItem(modelNhanVien.getValueAt(modelRow, 5));
    }

    private String giaTriChuoi(Object giaTri) {
        return giaTri == null ? "" : giaTri.toString();
    }

    public void lamMoiBieuMau(String maMoi) {
        tblNhanVien.clearSelection();
        maNVDangChon = null;
        txtMaNV.setText(maMoi);
        txtMaNV.setEnabled(true);
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        if (cboChucVu.getItemCount() > 0) {
            cboChucVu.setSelectedIndex(0);
        } else {
            cboChucVu.setSelectedItem(null);
        }
        cboTrangThai.setSelectedIndex(0);
        txtLocMaNV.setText("");
        txtLocHoTen.setText("");
        txtLocSoDienThoai.setText("");
        txtLocEmail.setText("");
    }

    public void chonNhanVienTheoMa(String maNV) {
        for (int modelRow = 0; modelRow < modelNhanVien.getRowCount(); modelRow++) {
            if (maNV.equals(modelNhanVien.getValueAt(modelRow, 0))) {
                int viewRow = tblNhanVien.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    tblNhanVien.setRowSelectionInterval(viewRow, viewRow);
                }
                return;
            }
        }
    }

    public NhanVien taoNhanVien() {
        return new NhanVien(
                txtMaNV.getText().trim(),
                txtHoTen.getText().trim(),
                (ChucVu) cboChucVu.getSelectedItem(),
                txtSoDienThoai.getText().trim(),
                txtEmail.getText().trim(),
                (String) cboTrangThai.getSelectedItem());
    }

    public String getMaNVDangChon() {
        return maNVDangChon;
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

    public javax.swing.JTable getTblNhanVien() {
        return tblNhanVien;
    }

    public javax.swing.JTextField getTxtLocMaNV() {
        return txtLocMaNV;
    }

    public javax.swing.JTextField getTxtLocHoTen() {
        return txtLocHoTen;
    }

    public javax.swing.JTextField getTxtLocSoDienThoai() {
        return txtLocSoDienThoai;
    }

    public javax.swing.JTextField getTxtLocEmail() {
        return txtLocEmail;
    }

    public void hienThiLoi(String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void thongBaoThanhCong(String noiDung, String tieuDe) {
        javax.swing.JOptionPane.showMessageDialog(this, noiDung, tieuDe, javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

}

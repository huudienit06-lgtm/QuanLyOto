package gui;

import controller.NhanVienController;
import entity.NhanVien;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.RowFilter;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.SwingUtilities;

public class FrmNhanVien extends JPanel {
    private final NhanVienController controller = new NhanVienController(this);
    private final AppTextField txtMaNV = new AppTextField(16);
    private final AppTextField txtHoTen = new AppTextField(16);
    private final AppTextField txtSoDienThoai = new AppTextField(16);
    private final AppTextField txtEmail = new AppTextField(16);
    private final AppTextField txtLocMaNV = new AppTextField(10);
    private final AppTextField txtLocHoTen = new AppTextField(12);
    private final AppTextField txtLocSoDienThoai = new AppTextField(12);
    private final AppTextField txtLocEmail = new AppTextField(12);
    private final JLabel lblThongBao = new AppLabel("Sẵn sàng", AppLabel.Style.MUTED);
    private final AppComboBox<String> cboChucVu = new AppComboBox<>();
    private final AppComboBox<String> cboTrangThai = new AppComboBox<>();
    private String maNVDangChon;
    private final DefaultTableModel modelNhanVien = new DefaultTableModel(
            new Object[] {"Mã nhân viên", "Họ và tên", "Chức vụ", "Số điện thoại", "Email", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final javax.swing.JTable tblNhanVien = FormUI.createTable(modelNhanVien);
    private final TableRowSorter<DefaultTableModel> sorterNhanVien = new TableRowSorter<>(modelNhanVien);
    private final AppButton btnThem = new AppButton("Thêm nhân viên");
    private final AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
    private final AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);

    public FrmNhanVien() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        txtMaNV.setEditable(false);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);
        JSplitPane splContent = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createFormCard(), FormUI.createTableCard("Danh sách nhân viên", createSearchToolbar(),
                        new AppScrollPane(tblNhanVien)));
        splContent.setBorder(null);
        splContent.setResizeWeight(0.38);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        SwingUtilities.invokeLater(() -> splContent.setDividerLocation(0.38));
        pnlContent.add(splContent, BorderLayout.CENTER);

        add(FormUI.createPage("QUẢN LÝ NHÂN VIÊN",
                "Thông tin nhân viên và danh sách nhân sự của gara", pnlContent), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        tblNhanVien.setRowSorter(sorterNhanVien);
        tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNhanVien.setRowHeight(30);
        tblNhanVien.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblNhanVien.setFillsViewportHeight(true);
        int[] doRongCot = {105, 180, 145, 145, 220, 140};
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

    private JPanel createSearchField(String labelText, AppTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new AppLabel(labelText, AppLabel.Style.MUTED);
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

    private JPanel createFormCard() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.setLayout(new BorderLayout(0, 6));
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Mã nhân viên (tự động)", txtMaNV, 0, 0);
        FormUI.addField(pnlFields, "Họ và tên *", txtHoTen, 2, 0);
        FormUI.addField(pnlFields, "Chức vụ", cboChucVu, 4, 0);
        FormUI.addField(pnlFields, "Số điện thoại", txtSoDienThoai, 0, 1);
        FormUI.addField(pnlFields, "Email", txtEmail, 2, 1);
        FormUI.addField(pnlFields, "Trạng thái", cboTrangThai, 4, 1);

        cboChucVu.setModel(new DefaultComboBoxModel<>(
                new String[] {"Chọn chức vụ", "Quản lý", "Cố vấn dịch vụ", "Lễ tân", "Kỹ thuật viên", "Thu ngân"}));
        cboTrangThai.setModel(new DefaultComboBoxModel<>(
                new String[] {"Đang làm việc", "Tạm nghỉ", "Đã nghỉ việc"}));

        JPanel pnlButtons = FormUI.createButtonRow(btnThem, btnCapNhat, btnXoa, btnLamMoi);
        pnlCard.add(FormUI.createSectionTitle("Thông tin nhân viên"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }
    public boolean validateForm() {
        String maNV = txtMaNV.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String soDienThoai = txtSoDienThoai.getText().trim();
        String email = txtEmail.getText().trim();
        String chucVu  = (String) cboChucVu.getSelectedItem();
        String trangThai = (String) cboTrangThai.getSelectedItem();

        if (!FormValidator.isValidIdentifier(maNV)) {
            FormUI.showError("Mã nhân viên không được để trống.");
            txtMaNV.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(maNV, 20)) {
            FormUI.showError("Mã nhân viên không được vượt quá 20 ký tự.");
            txtMaNV.requestFocus();
            return false;
        }
        if (!FormValidator.isValidFullName(hoTen)) {
            FormUI.showError("Họ và tên không hợp lệ.");
            txtHoTen.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(hoTen, 100)) {
            FormUI.showError("Họ và tên không được vượt quá 100 ký tự.");
            txtHoTen.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalPhoneNumber(soDienThoai)) {
            FormUI.showError("Số điện thoại phải có định dạng Việt Nam, ví dụ 0912345678 hoặc +84912345678.");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(soDienThoai, 15)) {
            FormUI.showError("Số điện thoại không được vượt quá 15 ký tự.");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalEmail(email)) {
            FormUI.showError("Email không hợp lệ.");
            txtEmail.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(email, 100)) {
            FormUI.showError("Email không được vượt quá 100 ký tự.");
            txtEmail.requestFocus();
            return false;
        }
        if (chucVu == null || chucVu.equals("Chọn chức vụ")) {
            FormUI.showError("Vui lòng chọn chức vụ.");
            cboChucVu.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(chucVu, 50)) {
            FormUI.showError("Chức vụ không được vượt quá 50 ký tự.");
            cboChucVu.requestFocus();
            return false;
        }
        if (trangThai == null || trangThai.isEmpty()) {
            FormUI.showError("Vui lòng chọn trạng thái.");
            cboTrangThai.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(trangThai, 20)) {
            FormUI.showError("Trạng thái không được vượt quá 20 ký tự.");
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
        cboChucVu.setSelectedIndex(0);
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
                (String) cboChucVu.getSelectedItem(),
                txtSoDienThoai.getText().trim(),
                txtEmail.getText().trim(),
                (String) cboTrangThai.getSelectedItem());
    }

    public String getMaNVDangChon() {
        return maNVDangChon;
    }

    public AppButton getBtnThem() { return btnThem; }
    public AppButton getBtnCapNhat() { return btnCapNhat; }
    public AppButton getBtnXoa() { return btnXoa; }
    public AppButton getBtnLamMoi() { return btnLamMoi; }
    public AppButton getBtnTaiLai() { return btnTaiLai; }
    public javax.swing.JTable getTblNhanVien() { return tblNhanVien; }
    public AppTextField getTxtLocMaNV() { return txtLocMaNV; }
    public AppTextField getTxtLocHoTen() { return txtLocHoTen; }
    public AppTextField getTxtLocSoDienThoai() { return txtLocSoDienThoai; }
    public AppTextField getTxtLocEmail() { return txtLocEmail; }

    public void hienThiLoi(String noiDung) {
        FormUI.showError(noiDung);
    }

    public void thongBaoThanhCong(String noiDung, String tieuDe) {
        javax.swing.JOptionPane.showMessageDialog(this, noiDung, tieuDe, javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

}

package gui;

import dao.NhanVienDAO;
import entity.NhanVien;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class FrmNhanVien extends JPanel implements ActionListener {
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final AppTextField txtMaNV = new AppTextField(16);
    private final AppTextField txtHoTen = new AppTextField(16);
    private final AppTextField txtSoDienThoai = new AppTextField(16);
    private final AppTextField txtEmail = new AppTextField(16);
    private final AppTextField txtTimKiem = new AppTextField(18);
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

    public FrmNhanVien() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormCard(), BorderLayout.NORTH);
        pnlContent.add(FormUI.createTableCard("Danh sách nhân viên", createSearchToolbar(),
                new AppScrollPane(tblNhanVien)), BorderLayout.CENTER);

        add(FormUI.createPage("QUẢN LÝ NHÂN VIÊN",
                "Thông tin nhân viên và danh sách nhân sự của gara", pnlContent), BorderLayout.CENTER);
        add(FormUI.createStatus("Sẵn sàng"), BorderLayout.SOUTH);

        tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNhanVien.setRowHeight(26);
        tblNhanVien.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblNhanVien.getSelectedRow() >= 0) {
                hienThiNhanVienDangChon();
            }
        });
        taiDanhSachNhanVien();
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlSearch.setOpaque(false);

        AppButton btnTimKiem = new AppButton("Tìm kiếm");
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTimKiem.addActionListener(this);
        btnTaiLai.addActionListener(this);
        txtTimKiem.setToolTipText("Tìm theo mã, họ tên, chức vụ, số điện thoại hoặc email");
        txtTimKiem.addActionListener(event -> timKiemNhanVien());
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTimKiem);
        pnlSearch.add(btnTaiLai);
        return pnlSearch;
    }

    private JPanel createFormCard() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.setLayout(new BorderLayout(0, 6));
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Mã nhân viên *", txtMaNV, 0, 0);
        FormUI.addField(pnlFields, "Họ và tên *", txtHoTen, 2, 0);
        FormUI.addField(pnlFields, "Chức vụ", cboChucVu, 4, 0);
        FormUI.addField(pnlFields, "Số điện thoại", txtSoDienThoai, 0, 1);
        FormUI.addField(pnlFields, "Email", txtEmail, 2, 1);
        FormUI.addField(pnlFields, "Trạng thái", cboTrangThai, 4, 1);

        cboChucVu.setModel(new DefaultComboBoxModel<>(
                new String[] {"Chọn chức vụ", "Quản lý", "Cố vấn dịch vụ", "Lễ tân", "Kỹ thuật viên", "Thu ngân"}));
        cboTrangThai.setModel(new DefaultComboBoxModel<>(
                new String[] {"Đang làm việc", "Tạm nghỉ", "Đã nghỉ việc"}));

        AppButton btnThem = new AppButton("Thêm nhân viên");
        AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
        AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
        btnThem.addActionListener(this);
        btnCapNhat.addActionListener(this);
        btnXoa.addActionListener(this);
        btnLamMoi.addActionListener(this);
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
        if (!FormValidator.isValidFullName(hoTen)) {
            FormUI.showError("Họ và tên không hợp lệ.");
            txtHoTen.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalPhoneNumber(soDienThoai)) {
            FormUI.showError("Số điện thoại không hợp lệ.");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalEmail(email)) {
            FormUI.showError("Email không hợp lệ.");
            txtEmail.requestFocus();
            return false;
        }
        if (chucVu == null || chucVu.equals("Chọn chức vụ")) {
            FormUI.showError("Vui lòng chọn chức vụ.");
            cboChucVu.requestFocus();
            return false;
        }
        if (trangThai == null || trangThai.isEmpty()) {
            FormUI.showError("Vui lòng chọn trạng thái.");
            cboTrangThai.requestFocus();
            return false;
        }

        return true;
    }

    private void themNhanVien() {
        if (!validateForm()) {
            return;
        }

        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNV(txtMaNV.getText().trim());
        nhanVien.setHoTen(txtHoTen.getText().trim());
        nhanVien.setChucVu((String) cboChucVu.getSelectedItem());
        nhanVien.setSoDT(txtSoDienThoai.getText().trim());
        nhanVien.setEmail(txtEmail.getText().trim());
        nhanVien.setTrangThai((String) cboTrangThai.getSelectedItem());

        try {
            if (!nhanVienDAO.insert(nhanVien)) {
                FormUI.showError("Không thêm được nhân viên vào cơ sở dữ liệu.");
                return;
            }
            taiDanhSachNhanVien();
            lamMoiForm();
            JOptionPane.showMessageDialog(this, "Đã thêm nhân viên thành công.",
                    "Thêm nhân viên", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            FormUI.showError("Không thể thêm nhân viên: " + exception.getMessage());
        }
    }

    private void taiDanhSachNhanVien() {
        try {
            hienThiDanhSach(nhanVienDAO.findAll());
        } catch (SQLException exception) {
            FormUI.showError("Không thể tải danh sách nhân viên: " + exception.getMessage());
        }
    }

    private void hienThiDanhSach(List<NhanVien> danhSachNhanVien) {
        modelNhanVien.setRowCount(0);
        for (NhanVien nhanVien : danhSachNhanVien) {
            modelNhanVien.addRow(new Object[] {
                    nhanVien.getMaNV(), nhanVien.getHoTen(), nhanVien.getChucVu(),
                    nhanVien.getSoDT(), nhanVien.getEmail(), nhanVien.getTrangThai()
            });
        }
    }

    private void timKiemNhanVien() {
        try {
            List<NhanVien> ketQua = nhanVienDAO.search(txtTimKiem.getText());
            maNVDangChon = null;
            lamMoiThongTinNhanVien();
            hienThiDanhSach(ketQua);
            if (ketQua.size() == 1) {
                int viewRow = tblNhanVien.convertRowIndexToView(0);
                tblNhanVien.setRowSelectionInterval(viewRow, viewRow);
            } else if (ketQua.isEmpty()) {
                FormUI.showError("Không tìm thấy nhân viên phù hợp.");
            }
        } catch (SQLException exception) {
            FormUI.showError("Không thể tìm kiếm nhân viên: " + exception.getMessage());
        }
    }

    private void hienThiNhanVienDangChon() {
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

    private void lamMoiForm() {
        tblNhanVien.clearSelection();
        maNVDangChon = null;
        lamMoiThongTinNhanVien();
        txtTimKiem.setText("");
        taiDanhSachNhanVien();
    }

    private void lamMoiThongTinNhanVien() {
        txtMaNV.setText("");
        txtMaNV.setEnabled(true);
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        cboChucVu.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
    }

    public void capNhatNhanVien() {
        if (maNVDangChon == null) {
            FormUI.showError("Vui lòng chọn hoặc tìm một nhân viên trước khi cập nhật.");
            return;
        }
        if (!validateForm()) {
            return;
        }

        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNV(maNVDangChon);
        nhanVien.setHoTen(txtHoTen.getText().trim());
        nhanVien.setChucVu((String) cboChucVu.getSelectedItem());
        nhanVien.setSoDT(txtSoDienThoai.getText().trim());
        nhanVien.setEmail(txtEmail.getText().trim());
        nhanVien.setTrangThai((String) cboTrangThai.getSelectedItem());

        try {
            if (!nhanVienDAO.update(nhanVien)) {
                FormUI.showError("Không cập nhật được nhân viên trong cơ sở dữ liệu.");
                return;
            }
            taiDanhSachNhanVien();
            hienThiNhanVienTheoMa(nhanVien.getMaNV());
            JOptionPane.showMessageDialog(this, "Đã cập nhật nhân viên thành công.",
                    "Cập nhật nhân viên", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            FormUI.showError("Không thể cập nhật nhân viên: " + exception.getMessage());
        }
    }

    private void hienThiNhanVienTheoMa(String maNV) {
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

    public void xoaNhanVien() {
        int selectedRow = tblNhanVien.getSelectedRow();
        if (selectedRow == -1) {
            FormUI.showError("Vui lòng chọn một nhân viên để xóa.");
            return;
        }
        String maNV = (String) modelNhanVien.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhân viên có mã: " + maNV + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (!nhanVienDAO.deleteById(maNV)) {
                FormUI.showError("Không xóa được nhân viên khỏi cơ sở dữ liệu.");
                return;
            }
            taiDanhSachNhanVien();
            lamMoiForm();
            JOptionPane.showMessageDialog(this, "Đã xóa nhân viên thành công.",
                    "Xóa nhân viên", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            FormUI.showError("Không thể xóa nhân viên: " + exception.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if ("Thêm nhân viên".equals(actionCommand)) {
            themNhanVien();
        } 
        else if ("Làm mới".equals(actionCommand)) {
            lamMoiForm();
        }
        else if("Cập nhật".equals(actionCommand)) {
            capNhatNhanVien();
        } else if ("Tìm kiếm".equals(actionCommand)) {
            timKiemNhanVien();
        } else if ("Tải lại".equals(actionCommand)) {
            lamMoiForm();
        } else if ("Xóa".equals(actionCommand)) {
            xoaNhanVien();
        }
    }
}

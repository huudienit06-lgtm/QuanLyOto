package gui;

import dao.KhachHangDAO;
import entity.KhachHang;
import gui.AppButton.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import util.MaTuDong;

public class FrmKhachHang extends JPanel implements ActionListener {
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final AppTextField txtMaKH = new AppTextField(16);
    private final AppTextField txtHoTen = new AppTextField(16);
    private final AppTextField txtSoDienThoai = new AppTextField(16);
    private final AppTextField txtEmail = new AppTextField(16);
    private final AppTextField txtDiaChi = new AppTextField(16);
    private final AppTextField txtCongNo = new AppTextField(16);
    private final AppTextField txtLocMaKH = new AppTextField(10);
    private final AppTextField txtLocHoTen = new AppTextField(12);
    private final AppTextField txtLocSoDienThoai = new AppTextField(12);
    private final AppTextField txtLocEmail = new AppTextField(12);
    private final JLabel lblThongBao = new AppLabel("Sẵn sàng", AppLabel.Style.MUTED);
    private String maKHDangChon;

    private final DefaultTableModel modelKhachHang = new DefaultTableModel(
            new Object[] { "Mã khách hàng", "Họ và tên", "Số điện thoại", "Email", "Địa chỉ", "Công nợ" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final javax.swing.JTable tblKhachHang = FormUI.createTable(modelKhachHang);
    private final TableRowSorter<DefaultTableModel> sorterKhachHang = new TableRowSorter<>(modelKhachHang);

    public FrmKhachHang() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);
        JSplitPane splContent = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createFormCard(),
                FormUI.createTableCard("Danh sách khách hàng", createSearchToolbar(),
                        new AppScrollPane(tblKhachHang)));
        splContent.setBorder(null);
        splContent.setResizeWeight(0.38);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        SwingUtilities.invokeLater(() -> splContent.setDividerLocation(0.38));
        pnlContent.add(splContent, BorderLayout.CENTER);

        add(FormUI.createPage("QUẢN LÝ KHÁCH HÀNG",
                "Thông tin khách hàng và dữ liệu giao dịch của gara", pnlContent), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        txtMaKH.setEditable(false);
        txtCongNo.setEditable(false);
        txtCongNo.setFocusable(false);
        tblKhachHang.setRowSorter(sorterKhachHang);
        tblKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblKhachHang.setFillsViewportHeight(true);
        tblKhachHang.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblKhachHang.getSelectedRow() >= 0) {
                hienThiKhachHangDangChon();
            }
        });
        taiDanhSachKhachHang();
        lamMoiForm();
    }

    private JPanel createFormCard() {
        AppPanel pnlCard = FormUI.createCard();
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Mã khách hàng *", txtMaKH, 0, 0);
        FormUI.addField(pnlFields, "Họ và tên *", txtHoTen, 2, 0);
        FormUI.addField(pnlFields, "Số điện thoại", txtSoDienThoai, 4, 0);
        FormUI.addField(pnlFields, "Email", txtEmail, 0, 1);
        FormUI.addField(pnlFields, "Địa chỉ", txtDiaChi, 2, 1);
        FormUI.addField(pnlFields, "Công nợ", txtCongNo, 4, 1);

        AppButton btnThem = new AppButton("Thêm mới");
        btnThem.setPreferredSize(new Dimension(100, btnThem.getPreferredSize().height));
        AppButton btnCapNhat = new AppButton("Cập nhật", Style.SUCCESS);
        btnCapNhat.setPreferredSize(new Dimension(100, btnCapNhat.getPreferredSize().height));
        AppButton btnXoa = new AppButton("Xóa", Style.DANGER);
        btnXoa.setPreferredSize(new Dimension(100, btnXoa.getPreferredSize().height));
        AppButton btnLamMoi = new AppButton("Làm mới", Style.SECONDARY);
        btnLamMoi.setPreferredSize(new Dimension(100, btnLamMoi.getPreferredSize().height));
        btnThem.addActionListener(this);
        btnCapNhat.addActionListener(this);
        btnXoa.addActionListener(this);
        btnLamMoi.addActionListener(this);

        pnlCard.add(FormUI.createSectionTitle("Thông tin khách hàng"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(FormUI.createButtonRow(btnThem, btnCapNhat, btnXoa, btnLamMoi), BorderLayout.SOUTH);
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
        AppButton btnTaiLai = new AppButton("Tải lại", Style.SECONDARY);
        btnTaiLai.addActionListener(this);
        pnlSearch.add(pnlLoc, BorderLayout.CENTER);
        pnlSearch.add(btnTaiLai, BorderLayout.EAST);
        themBoLangNghe(txtLocMaKH);
        themBoLangNghe(txtLocHoTen);
        themBoLangNghe(txtLocSoDienThoai);
        themBoLangNghe(txtLocEmail);
        return pnlSearch;
    }

    private JPanel createSearchField(String labelText, AppTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        pnlField.add(new AppLabel(labelText, AppLabel.Style.MUTED), BorderLayout.NORTH);
        pnlField.add(txtField, BorderLayout.CENTER);
        return pnlField;
    }

    private void themBoLangNghe(AppTextField txtField) {
        txtField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSachKhachHang();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSachKhachHang();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSachKhachHang();
            }
        });
    }

    private JPanel createStatusPanel() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private boolean validateForm() {
        if (!FormValidator.isValidIdentifier(txtMaKH.getText().trim())) {
            FormUI.showError("Mã khách hàng không được để trống và chỉ được chứa chữ, số, gạch ngang hoặc gạch dưới.");
            txtMaKH.requestFocus();
            return false;
        }
        if (!FormValidator.isValidFullName(txtHoTen.getText().trim())) {
            FormUI.showError("Họ và tên không hợp lệ.");
            txtHoTen.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalPhoneNumber(txtSoDienThoai.getText().trim())) {
            FormUI.showError("Số điện thoại không hợp lệ.");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!FormValidator.isValidOptionalEmail(txtEmail.getText().trim())) {
            FormUI.showError("Email không hợp lệ.");
            txtEmail.requestFocus();
            return false;
        }
        return true;
    }

    private void taiDanhSachKhachHang() {
        try {
            hienThiDanhSach(khachHangDAO.findAll());
            locDanhSachKhachHang();
        } catch (SQLException exception) {
            FormUI.showError("Không thể tải danh sách khách hàng: " + exception.getMessage());
        }
    }

    private void hienThiDanhSach(List<KhachHang> danhSach) {
        modelKhachHang.setRowCount(0);
        for (KhachHang khachHang : danhSach) {
            modelKhachHang.addRow(new Object[] {
                    khachHang.getMaKH(), khachHang.getHoTen(), khachHang.getSoDT(),
                    khachHang.getEmail(), khachHang.getDiaChi(), khachHang.getCongNo()
            });
        }
    }

    private void locDanhSachKhachHang() {
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

    private void hienThiKhachHangDangChon() {
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

    private KhachHang taoKhachHang() {
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKH(maKHDangChon == null ? txtMaKH.getText().trim() : maKHDangChon);
        khachHang.setHoTen(txtHoTen.getText().trim());
        khachHang.setSoDT(txtSoDienThoai.getText().trim());
        khachHang.setEmail(txtEmail.getText().trim());
        khachHang.setDiaChi(txtDiaChi.getText().trim());
        khachHang.setCongNo(parseCongNo());
        return khachHang;
    }

    private Double parseCongNo() {
        try {
            String giaTri = txtCongNo.getText().trim();
            return giaTri.isEmpty() ? 0D : Double.valueOf(giaTri);
        } catch (NumberFormatException exception) {
            return 0D;
        }
    }

    private void themKhachHang() {
        if (!validateForm()) {
            return;
        }
        KhachHang khachHang = taoKhachHang();
        if (khachHang.getCongNo() == null) {
            khachHang.setCongNo(0D);
        }
        try {
            if (!khachHangDAO.insert(khachHang)) {
                FormUI.showError("Không thêm được khách hàng vào cơ sở dữ liệu.");
                return;
            }
            taiDanhSachKhachHang();
            lamMoiForm();
            JOptionPane.showMessageDialog(this, "Đã thêm khách hàng thành công.",
                    "Thêm khách hàng", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            FormUI.showError("Không thể thêm khách hàng: " + exception.getMessage());
        }
    }

    private void capNhatKhachHang() {
        if (maKHDangChon == null) {
            FormUI.showError("Vui lòng chọn một khách hàng trước khi cập nhật.");
            return;
        }
        if (!validateForm()) {
            return;
        }
        try {
            if (!khachHangDAO.update(taoKhachHang())) {
                FormUI.showError("Không cập nhật được khách hàng trong cơ sở dữ liệu.");
                return;
            }
            taiDanhSachKhachHang();
            lamMoiForm();
            JOptionPane.showMessageDialog(this, "Đã cập nhật khách hàng thành công.",
                    "Cập nhật khách hàng", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            FormUI.showError("Không thể cập nhật khách hàng: " + exception.getMessage());
        }
    }

    private void xoaKhachHang() {
        int selectedRow = tblKhachHang.getSelectedRow();
        if (selectedRow < 0) {
            FormUI.showError("Vui lòng chọn một khách hàng để xóa.");
            return;
        }
        int modelRow = tblKhachHang.convertRowIndexToModel(selectedRow);
        String maKH = giaTriChuoi(modelKhachHang.getValueAt(modelRow, 0));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng có mã: " + maKH + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!khachHangDAO.deleteById(maKH)) {
                FormUI.showError("Không xóa được khách hàng khỏi cơ sở dữ liệu.");
                return;
            }
            taiDanhSachKhachHang();
            lamMoiForm();
            JOptionPane.showMessageDialog(this, "Đã xóa khách hàng thành công.",
                    "Xóa khách hàng", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            FormUI.showError("Không thể xóa khách hàng: " + exception.getMessage());
        }
    }

    private void lamMoiForm() {
        tblKhachHang.clearSelection();
        maKHDangChon = null;
        txtMaKH.setEnabled(true);
        try {
            txtMaKH.setText(MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_KHACH_HANG,
                    khachHangDAO.findAll(),
                    KhachHang::getMaKH));
        } catch (SQLException exception) {
            txtMaKH.setText("");
            FormUI.showError("Không thể phát sinh mã khách hàng: " + exception.getMessage());
        }
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

    @Override
    public void actionPerformed(ActionEvent event) {
        switch (event.getActionCommand()) {
            case "Thêm mới" -> themKhachHang();
            case "Cập nhật" -> capNhatKhachHang();
            case "Xóa" -> xoaKhachHang();
            case "Làm mới", "Tải lại" -> {
                lamMoiForm();
                taiDanhSachKhachHang();
            }
            default -> {
            }
        }
    }
}

package gui;

import controller.BanGiaoXeController;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuBanGiao;
import entity.TaiKhoan;
import entity.Xe;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmBanGiaoXe extends JPanel {
    private final AppTextField txtMaPhieuBanGiao = new AppTextField(14);
    private final AppComboBox<Xe> cboXe = new AppComboBox<>();
    private final AppTextField txtMaKH = new AppTextField(14);
    private final AppTextField txtTenKhachHang = new AppTextField(14);
    private final AppComboBox<NhanVien> cboNhanVien = new AppComboBox<>();
    private final AppTextField txtTenNguoiNhan = new AppTextField(14);
    private final AppTextField txtSoDienThoaiNguoiNhan = new AppTextField(14);
    private final JTextArea txaTinhTrangXe = new JTextArea(5, 20);
    private final AppTextField txtTimKiem = new AppTextField(20);
    private final AppLabel lblThongBao = new AppLabel("Sẵn sàng", AppLabel.Style.MUTED);
    private final AppButton btnThem = new AppButton("Lập phiếu", AppButton.Style.SUCCESS);
    private final AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.PRIMARY);
    private final AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);

    private final DefaultTableModel modelBanGiao = new DefaultTableModel(
            new Object[] {
                    "Mã phiếu", "Biển số", "Mã khách hàng", "Tên khách hàng",
                    "Nhân viên", "Người nhận", "Số điện thoại"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblBanGiao = FormUI.createTable(modelBanGiao);
    private final TableRowSorter<DefaultTableModel> sorterBanGiao = new TableRowSorter<>(modelBanGiao);
    private final BanGiaoXeController controller;

    public FrmBanGiaoXe() {
        this(null);
    }

    public FrmBanGiaoXe(TaiKhoan taiKhoanHienTai) {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormCard(), BorderLayout.NORTH);
        pnlContent.add(FormUI.createTableCard(
                "Danh sách phiếu bàn giao",
                createSearchToolbar(),
                new JScrollPane(tblBanGiao)),
                BorderLayout.CENTER);

        add(FormUI.createPage(
                "PHIẾU BÀN GIAO XE",
                "Xác nhận tình trạng xe và thông tin người nhận sau khi hoàn tất dịch vụ",
                pnlContent),
                BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);
        configureComponents();
        controller = new BanGiaoXeController(this, taiKhoanHienTai);
        controller.khoiTao();
    }

    private JPanel createFormCard() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.setLayout(new BorderLayout(0, 10));

        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Mã phiếu (tự động)", txtMaPhieuBanGiao, 0, 0);
        FormUI.addField(pnlFields, "Xe bàn giao *", cboXe, 2, 0);
        FormUI.addField(pnlFields, "Mã khách hàng", txtMaKH, 0, 1);
        FormUI.addField(pnlFields, "Tên khách hàng", txtTenKhachHang, 2, 1);
        FormUI.addField(pnlFields, "Nhân viên lập phiếu *", cboNhanVien, 0, 2);
        FormUI.addField(pnlFields, "Tên người nhận *", txtTenNguoiNhan, 2, 2);
        FormUI.addField(pnlFields, "Số điện thoại *", txtSoDienThoaiNguoiNhan, 0, 3);

        JPanel pnlCondition = new JPanel(new BorderLayout(0, 5));
        pnlCondition.setOpaque(false);
        pnlCondition.add(new AppLabel("Tình trạng xe hiện tại *", AppLabel.Style.MUTED), BorderLayout.NORTH);
        txaTinhTrangXe.setLineWrap(true);
        txaTinhTrangXe.setWrapStyleWord(true);
        pnlCondition.add(new JScrollPane(txaTinhTrangXe), BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(new GridLayout(1, 2, 24, 0));
        pnlBody.setOpaque(false);
        pnlBody.add(pnlFields);
        pnlBody.add(pnlCondition);

        btnThem.setPreferredSize(new Dimension(100, btnThem.getPreferredSize().height));
        btnCapNhat.setPreferredSize(new Dimension(100, btnCapNhat.getPreferredSize().height));
        btnXoa.setPreferredSize(new Dimension(100, btnXoa.getPreferredSize().height));
        btnLamMoi.setPreferredSize(new Dimension(100, btnLamMoi.getPreferredSize().height));

        pnlCard.add(FormUI.createSectionTitle("Thông tin bàn giao"), BorderLayout.NORTH);
        pnlCard.add(pnlBody, BorderLayout.CENTER);
        pnlCard.add(FormUI.createButtonRow(btnThem, btnCapNhat, btnXoa, btnLamMoi), BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createSearchToolbar() {
        JPanel pnlToolbar = new JPanel(new BorderLayout(8, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.add(new AppLabel("Tìm kiếm", AppLabel.Style.MUTED), BorderLayout.WEST);
        pnlToolbar.add(txtTimKiem, BorderLayout.CENTER);
        pnlToolbar.add(btnTaiLai, BorderLayout.EAST);
        return pnlToolbar;
    }

    private JPanel createStatusPanel() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private void configureComponents() {
        txtMaPhieuBanGiao.setEditable(false);
        txtMaPhieuBanGiao.setToolTipText("Mã phiếu duy nhất, ví dụ: PBG001");
        txtMaKH.setEditable(false);
        txtTenKhachHang.setEditable(false);
        txtMaKH.setFocusable(false);
        txtTenKhachHang.setFocusable(false);
        tblBanGiao.setRowSorter(sorterBanGiao);
        tblBanGiao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBanGiao.setFillsViewportHeight(true);
    }

    public void hienThiDanhMuc(List<Xe> danhSachXe, List<NhanVien> danhSachNhanVien) {
        cboXe.setModel(new DefaultComboBoxModel<>(danhSachXe.toArray(new Xe[0])));
        cboXe.setRenderer(new ComboBoxRenderer<Xe>() {
            @Override
            protected String getText(Xe xe) {
                return xe.getMaXe() + " - " + xe.getBienSo();
            }
        });
        cboNhanVien.setModel(new DefaultComboBoxModel<>(danhSachNhanVien.toArray(new NhanVien[0])));
        cboNhanVien.setRenderer(new ComboBoxRenderer<NhanVien>() {
            @Override
            protected String getText(NhanVien nhanVien) {
                return nhanVien.toString();
            }
        });
    }

    public void hienThiThongTinKhachHang(KhachHang khachHang) {
        txtMaKH.setText(khachHang == null ? "" : khachHang.getMaKH());
        txtTenKhachHang.setText(khachHang == null ? "" : khachHang.getHoTen());
    }

    public void hienThiDanhSachPhieu(List<PhieuBanGiao> danhSach) {
        modelBanGiao.setRowCount(0);
        for (PhieuBanGiao phieu : danhSach) {
            modelBanGiao.addRow(new Object[] {
                    phieu.getMaPhieuBanGiao(),
                    phieu.getXe() == null ? "" : phieu.getXe().getMaXe(),
                    phieu.getKhachHang() == null ? "" : phieu.getKhachHang().getMaKH(),
                    phieu.getKhachHang() == null ? "" : phieu.getKhachHang().getHoTen(),
                    phieu.getNhanVien() == null ? "" : phieu.getNhanVien().getMaNV(),
                    phieu.getTenNguoiNhan(),
                    phieu.getSoDienThoaiNguoiNhan()
            });
        }
        locDanhSach();
    }

    public void hienThiPhieu(PhieuBanGiao phieu) {
        txtMaPhieuBanGiao.setText(phieu.getMaPhieuBanGiao());
        chonXe(phieu.getXe() == null ? null : phieu.getXe().getMaXe());
        chonNhanVien(phieu.getNhanVien() == null ? null : phieu.getNhanVien().getMaNV());
        txaTinhTrangXe.setText(phieu.getTinhTrangXeHienTai());
        txtTenNguoiNhan.setText(phieu.getTenNguoiNhan());
        txtSoDienThoaiNguoiNhan.setText(phieu.getSoDienThoaiNguoiNhan());
    }

    public void lamMoiBieuMau(String maPhieuMoi, String maNhanVienMacDinh) {
        txtMaPhieuBanGiao.setText(maPhieuMoi);
        if (cboXe.getItemCount() > 0) {
            cboXe.setSelectedIndex(0);
        } else {
            hienThiThongTinKhachHang(null);
        }
        if (maNhanVienMacDinh != null) {
            chonNhanVien(maNhanVienMacDinh.trim());
        } else if (cboNhanVien.getItemCount() > 0) {
            cboNhanVien.setSelectedIndex(0);
        }
        txtTenNguoiNhan.setText("");
        txtSoDienThoaiNguoiNhan.setText("");
        txaTinhTrangXe.setText("");
        tblBanGiao.clearSelection();
        thongBao("Sẵn sàng lập phiếu bàn giao");
    }

    public void locDanhSach() {
        String keyword = txtTimKiem.getText().trim();
        sorterBanGiao.setRowFilter(keyword.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword)));
    }

    public boolean xacThucForm() {
        if (txtMaPhieuBanGiao.getText().trim().isEmpty()
                || cboXe.getSelectedItem() == null
                || cboNhanVien.getSelectedItem() == null
                || txtTenNguoiNhan.getText().trim().isEmpty()
                || txtSoDienThoaiNguoiNhan.getText().trim().isEmpty()
                || txaTinhTrangXe.getText().trim().isEmpty()) {
            FormUI.showError("Vui lòng nhập đầy đủ các trường bắt buộc.");
            return false;
        }
        if (!FormValidator.isValidPhoneNumber(txtSoDienThoaiNguoiNhan.getText().trim())) {
            FormUI.showError(
                    "Số điện thoại người nhận không hợp lệ. Ví dụ: 0912345678 hoặc +84912345678.");
            txtSoDienThoaiNguoiNhan.requestFocus();
            return false;
        }
        if (!FormValidator.isWithinMaxLength(txtTenNguoiNhan.getText().trim(), 100)) {
            FormUI.showError("Tên người nhận không được vượt quá 100 ký tự.");
            txtTenNguoiNhan.requestFocus();
            return false;
        }
        return true;
    }

    public String getMaPhieuNhap() {
        return txtMaPhieuBanGiao.getText().trim();
    }

    public Xe getXeDuocChon() {
        return (Xe) cboXe.getSelectedItem();
    }

    public NhanVien getNhanVienDuocChon() {
        return (NhanVien) cboNhanVien.getSelectedItem();
    }

    public String getTenNguoiNhanNhap() {
        return txtTenNguoiNhan.getText().trim();
    }

    public String getSoDienThoaiNguoiNhanNhap() {
        return txtSoDienThoaiNguoiNhan.getText().trim();
    }

    public String getTinhTrangXeNhap() {
        return txaTinhTrangXe.getText().trim();
    }

    public String getMaPhieuDangChon() {
        int viewRow = tblBanGiao.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblBanGiao.convertRowIndexToModel(viewRow);
        Object value = modelBanGiao.getValueAt(modelRow, 0);
        return value == null ? null : value.toString();
    }

    public void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    public void hienThiLoi(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        lblThongBao.setText(message);
        FormUI.showError(message);
    }

    public AppButton getBtnThem() {
        return btnThem;
    }

    public AppButton getBtnCapNhat() {
        return btnCapNhat;
    }

    public AppButton getBtnXoa() {
        return btnXoa;
    }

    public AppButton getBtnLamMoi() {
        return btnLamMoi;
    }

    public AppButton getBtnTaiLai() {
        return btnTaiLai;
    }

    public AppComboBox<Xe> getCboXe() {
        return cboXe;
    }

    public AppTextField getTxtTimKiem() {
        return txtTimKiem;
    }

    public JTable getTblBanGiao() {
        return tblBanGiao;
    }

    private void chonXe(String maXe) {
        if (maXe == null) {
            return;
        }
        for (int index = 0; index < cboXe.getItemCount(); index++) {
            if (maXe.equals(cboXe.getItemAt(index).getMaXe())) {
                cboXe.setSelectedIndex(index);
                return;
            }
        }
    }

    private void chonNhanVien(String maNV) {
        if (maNV == null) {
            return;
        }
        for (int index = 0; index < cboNhanVien.getItemCount(); index++) {
            if (maNV.equals(cboNhanVien.getItemAt(index).getMaNV())) {
                cboNhanVien.setSelectedIndex(index);
                return;
            }
        }
    }

    private abstract static class ComboBoxRenderer<T> extends DefaultListCellRenderer {
        @Override
        @SuppressWarnings("unchecked")
        public Component getListCellRendererComponent(
                javax.swing.JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setText(value == null ? "Chọn..." : getText((T) value));
            return this;
        }

        protected abstract String getText(T value);
    }
}

package gui;

import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.PhieuBanGiaoDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuBanGiao;
import entity.Xe;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import util.MaTuDong;

public class FrmBanGiaoXe extends JPanel {
    private final PhieuBanGiaoDAO phieuBanGiaoDAO = new PhieuBanGiaoDAO();
    private final XeDAO xeDAO = new XeDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private final AppTextField txtMaPhieuBanGiao = new AppTextField(14);
    private final AppComboBox<Xe> cboXe = new AppComboBox<>();
    private final AppTextField txtMaKH = new AppTextField(14);
    private final AppTextField txtTenKhachHang = new AppTextField(14);
    private final AppComboBox<NhanVien> cboNhanVien = new AppComboBox<>();
    private final AppTextField txtTenNguoiNhan = new AppTextField(14);
    private final AppTextField txtSoDienThoaiNguoiNhan = new AppTextField(14);
    private final JTextArea txaTinhTrangXe = new JTextArea(5, 20);
    private final AppTextField txtTimKiem = new AppTextField(20);
    private final JLabel lblThongBao = new AppLabel("Sẵn sàng", AppLabel.Style.MUTED);

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

    public FrmBanGiaoXe() {
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
        napDuLieuDanhMuc();
        napDanhSachPhieu();
        lamMoiForm();
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

        AppButton btnThem = new AppButton("Lập phiếu", AppButton.Style.SUCCESS);
        btnThem.setPreferredSize(new Dimension(100, btnThem.getPreferredSize().height));
        AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.PRIMARY);
        btnCapNhat.setPreferredSize(new Dimension(100, btnCapNhat.getPreferredSize().height));
        AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
        btnXoa.setPreferredSize(new Dimension(100, btnXoa.getPreferredSize().height));
        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
        btnLamMoi.setPreferredSize(new Dimension(100, btnLamMoi.getPreferredSize().height));

        btnThem.addActionListener(event -> themPhieu());
        btnCapNhat.addActionListener(event -> capNhatPhieu());
        btnXoa.addActionListener(event -> xoaPhieu());
        btnLamMoi.addActionListener(event -> lamMoiForm());

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
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTaiLai.addActionListener(event -> napDanhSachPhieu());
        pnlToolbar.add(btnTaiLai, BorderLayout.EAST);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSach();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSach();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSach();
            }
        });
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
        tblBanGiao.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblBanGiao.getSelectedRow() >= 0) {
                hienThiPhieuDangChon();
            }
        });
        cboXe.addActionListener(event -> capNhatThongTinKhachHang());
    }

    private void napDuLieuDanhMuc() {
        try {
            List<Xe> danhSachXe = xeDAO.findAll();
            cboXe.setModel(new DefaultComboBoxModel<>(danhSachXe.toArray(new Xe[0])));
            cboXe.setRenderer(new ComboBoxRenderer<Xe>() {
                @Override
                protected String getText(Xe xe) {
                    return xe.getMaXe() + " - " + xe.getBienSo();
                }
            });

            List<NhanVien> danhSachNhanVien = nhanVienDAO.findAll();
            cboNhanVien.setModel(new DefaultComboBoxModel<>(danhSachNhanVien.toArray(new NhanVien[0])));
            cboNhanVien.setRenderer(new ComboBoxRenderer<NhanVien>() {
                @Override
                protected String getText(NhanVien nhanVien) {
                    return nhanVien.getMaNV() + " - " + nhanVien.getHoTen();
                }
            });
            capNhatThongTinKhachHang();
        } catch (SQLException exception) {
            lblThongBao.setText("Không tải được danh mục xe/nhân viên");
            FormUI.showError("Không thể tải danh mục bàn giao: " + exception.getMessage());
        }
    }

    private void capNhatThongTinKhachHang() {
        Xe xe = (Xe) cboXe.getSelectedItem();
        if (xe == null) {
            txtMaKH.setText("");
            txtTenKhachHang.setText("");
            return;
        }
        txtMaKH.setText(xe.getMaKH());
        try {
            KhachHang khachHang = khachHangDAO.findById(xe.getMaKH());
            txtTenKhachHang.setText(khachHang == null ? "" : khachHang.getHoTen());
            if (khachHang != null && txtTenNguoiNhan.getText().trim().isEmpty()) {
                txtTenNguoiNhan.setText(khachHang.getHoTen());
                txtSoDienThoaiNguoiNhan.setText(khachHang.getSoDT());
            }
        } catch (SQLException exception) {
            FormUI.showError("Không thể tải thông tin khách hàng: " + exception.getMessage());
        }
    }

    private void napDanhSachPhieu() {
        try {
            modelBanGiao.setRowCount(0);
            for (PhieuBanGiao phieu : phieuBanGiaoDAO.findAll()) {
                modelBanGiao.addRow(new Object[] {
                        phieu.getMaPhieuBanGiao(), phieu.getMaXe(), phieu.getMaKH(),
                        phieu.getTenKhachHang(), phieu.getMaNV(), phieu.getTenNguoiNhan(),
                        phieu.getSoDienThoaiNguoiNhan()
                });
            }
            lblThongBao.setText("Đã tải " + modelBanGiao.getRowCount() + " phiếu bàn giao");
        } catch (SQLException exception) {
            lblThongBao.setText("Không tải được danh sách phiếu");
            FormUI.showError("Không thể tải danh sách phiếu bàn giao: " + exception.getMessage());
        }
    }

    private void locDanhSach() {
        String keyword = txtTimKiem.getText().trim();
        sorterBanGiao.setRowFilter(keyword.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword)));
    }

    private boolean validateForm() {
        if (txtMaPhieuBanGiao.getText().trim().isEmpty()
                || cboXe.getSelectedItem() == null
                || cboNhanVien.getSelectedItem() == null
                || txtTenNguoiNhan.getText().trim().isEmpty()
                || txtSoDienThoaiNguoiNhan.getText().trim().isEmpty()
                || txaTinhTrangXe.getText().trim().isEmpty()) {
            FormUI.showError("Vui lòng nhập đầy đủ các trường bắt buộc.");
            return false;
        }
        if (!txtSoDienThoaiNguoiNhan.getText().trim().matches("\\d{9,15}")) {
            FormUI.showError("Số điện thoại người nhận phải gồm 9 đến 15 chữ số.");
            return false;
        }
        return true;
    }

    private PhieuBanGiao layPhieuTuForm() {
        Xe xe = (Xe) cboXe.getSelectedItem();
        NhanVien nhanVien = (NhanVien) cboNhanVien.getSelectedItem();
        PhieuBanGiao phieu = new PhieuBanGiao();
        phieu.setMaPhieuBanGiao(txtMaPhieuBanGiao.getText().trim());
        phieu.setMaXe(xe.getMaXe());
        phieu.setMaKH(txtMaKH.getText().trim());
        phieu.setTenKhachHang(txtTenKhachHang.getText().trim());
        phieu.setMaNV(nhanVien.getMaNV());
        phieu.setTinhTrangXeHienTai(txaTinhTrangXe.getText().trim());
        phieu.setTenNguoiNhan(txtTenNguoiNhan.getText().trim());
        phieu.setSoDienThoaiNguoiNhan(txtSoDienThoaiNguoiNhan.getText().trim());
        return phieu;
    }

    private void themPhieu() {
        if (!validateForm()) {
            return;
        }
        try {
            if (!phieuBanGiaoDAO.insert(layPhieuTuForm())) {
                throw new SQLException("Không có bản ghi nào được thêm.");
            }
            lblThongBao.setText("Đã lập phiếu bàn giao " + txtMaPhieuBanGiao.getText().trim());
            napDanhSachPhieu();
            lamMoiForm();
        } catch (SQLException exception) {
            FormUI.showError("Không thể lập phiếu bàn giao: " + exception.getMessage());
        }
    }

    private void capNhatPhieu() {
        if (!validateForm()) {
            return;
        }
        try {
            if (!phieuBanGiaoDAO.update(layPhieuTuForm())) {
                throw new SQLException("Không tìm thấy phiếu cần cập nhật.");
            }
            lblThongBao.setText("Đã cập nhật phiếu bàn giao");
            napDanhSachPhieu();
        } catch (SQLException exception) {
            FormUI.showError("Không thể cập nhật phiếu bàn giao: " + exception.getMessage());
        }
    }

    private void xoaPhieu() {
        String maPhieu = txtMaPhieuBanGiao.getText().trim();
        if (maPhieu.isEmpty()) {
            FormUI.showError("Vui lòng chọn phiếu cần xóa.");
            return;
        }
        try {
            if (!phieuBanGiaoDAO.deleteById(maPhieu)) {
                throw new SQLException("Không tìm thấy phiếu cần xóa.");
            }
            lblThongBao.setText("Đã xóa phiếu bàn giao");
            napDanhSachPhieu();
            lamMoiForm();
        } catch (SQLException exception) {
            FormUI.showError("Không thể xóa phiếu bàn giao: " + exception.getMessage());
        }
    }

    private void hienThiPhieuDangChon() {
        int viewRow = tblBanGiao.getSelectedRow();
        int modelRow = tblBanGiao.convertRowIndexToModel(viewRow);
        String maPhieu = modelBanGiao.getValueAt(modelRow, 0).toString();
        try {
            PhieuBanGiao phieu = phieuBanGiaoDAO.findById(maPhieu);
            if (phieu == null) {
                return;
            }
            txtMaPhieuBanGiao.setText(phieu.getMaPhieuBanGiao());
            chonXe(phieu.getMaXe());
            chonNhanVien(phieu.getMaNV());
            txaTinhTrangXe.setText(phieu.getTinhTrangXeHienTai());
            txtTenNguoiNhan.setText(phieu.getTenNguoiNhan());
            txtSoDienThoaiNguoiNhan.setText(phieu.getSoDienThoaiNguoiNhan());
        } catch (SQLException exception) {
            FormUI.showError("Không thể tải chi tiết phiếu: " + exception.getMessage());
        }
    }

    private void chonXe(String maXe) {
        for (int index = 0; index < cboXe.getItemCount(); index++) {
            if (cboXe.getItemAt(index).getMaXe().equals(maXe)) {
                cboXe.setSelectedIndex(index);
                return;
            }
        }
    }

    private void chonNhanVien(String maNV) {
        for (int index = 0; index < cboNhanVien.getItemCount(); index++) {
            if (cboNhanVien.getItemAt(index).getMaNV().equals(maNV)) {
                cboNhanVien.setSelectedIndex(index);
                return;
            }
        }
    }

    private void lamMoiForm() {
        try {
            txtMaPhieuBanGiao.setText(MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_PHIEU_BAN_GIAO,
                    phieuBanGiaoDAO.findAll(),
                    PhieuBanGiao::getMaPhieuBanGiao));
        } catch (SQLException exception) {
            txtMaPhieuBanGiao.setText("");
            lblThongBao.setText("Không thể phát sinh mã phiếu bàn giao");
            FormUI.showError("Không thể phát sinh mã phiếu bàn giao: " + exception.getMessage());
        }
        if (cboXe.getItemCount() > 0) {
            cboXe.setSelectedIndex(0);
        }
        if (cboNhanVien.getItemCount() > 0) {
            cboNhanVien.setSelectedIndex(0);
        }
        txtTenNguoiNhan.setText("");
        txtSoDienThoaiNguoiNhan.setText("");
        txaTinhTrangXe.setText("");
        tblBanGiao.clearSelection();
        lblThongBao.setText("Sẵn sàng lập phiếu bàn giao");
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

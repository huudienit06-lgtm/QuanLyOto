package gui;

import controller.PhieuKiemTraController;
import dao.LichLamViecDAO;
import entity.PhieuKiemTra;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmPhieuKiemTra extends JPanel {
    private final PhieuKiemTraController controller = new PhieuKiemTraController(this);
    private final Map<String, String> bienSoTheoMaXe = new HashMap<>();
    private final Map<String, String> tenKhachHangTheoMaKH = new HashMap<>();

    private final AppTextField txtMaPKT = new AppTextField(16);
    private final javax.swing.JComboBox<PhieuTiepNhan> cboMaPTN = new javax.swing.JComboBox<>();
    private final AppTextField txtMaKH = new AppTextField(16);
    private final AppTextField txtMaXe = new AppTextField(16);
    private final AppTextField txtBienSo = new AppTextField(16);
    private final javax.swing.JComboBox<LichLamViecDAO.NhanVienTrongCa> cboNhanVienKiemTra =
            new javax.swing.JComboBox<>();
    private final javax.swing.JTextArea txaTrangThai = new javax.swing.JTextArea(2, 20);
    private final javax.swing.JTextArea txaVanDe = new javax.swing.JTextArea(3, 20);
    private final AppTextField txtLocMaPKT = new AppTextField(12);
    private final AppTextField txtLocMaPTN = new AppTextField(12);
    private final AppTextField txtLocMaXe = new AppTextField(12);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");
    private final AppButton btnThem = new AppButton("Thêm phiếu", AppButton.Style.PRIMARY);
    private final AppButton btnSua = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
    private final AppButton btnXoa = new AppButton("Xóa phiếu", AppButton.Style.DANGER);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);

    private final DefaultTableModel modelPhieuKiemTra = new DefaultTableModel(
            new Object[] {
                    "Mã phiếu kiểm tra", "Mã phiếu tiếp nhận", "Mã khách hàng", "Mã xe",
                    "Mã nhân viên", "Trạng thái ban đầu", "Vấn đề hư hỏng"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final AppTable tblPhieuKiemTra = new AppTable(modelPhieuKiemTra);
    private final TableRowSorter<DefaultTableModel> sorterPhieuKiemTra =
            new TableRowSorter<>(modelPhieuKiemTra);

    public FrmPhieuKiemTra() {
        this(null);
    }

    public FrmPhieuKiemTra(TaiKhoan taiKhoanHienTai) {
        setLayout(new BorderLayout(0, 8));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(14, 22, 14, 22));

        add(createHeading(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        cauHinhThanhPhan();
        controller.khoiTao();
    }

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 5));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("PHIẾU KIỂM TRA");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new AppLabel(
                "Lập phiếu kiểm tra tình trạng xe và tra cứu các phiếu đã lập",
                AppLabel.Style.MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);

        JPanel pnlForm = createFormPanel();  //form thông tin phiếu kiểm tra
        int chieuCaoForm = pnlForm.getPreferredSize().height;
        pnlForm.setMinimumSize(new Dimension(0, chieuCaoForm));
        javax.swing.JSplitPane splContent = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT, pnlForm, createTablePanel());
        splContent.setBorder(null);
        splContent.setResizeWeight(0.0);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        javax.swing.SwingUtilities.invokeLater(() -> splContent.setDividerLocation(chieuCaoForm));
        pnlContent.add(splContent, BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormPanel() {
        JPanel pnlFormCard = AppPanel.card(new BorderLayout(0, 6));
        pnlFormCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConfig.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        // vùng nhập thông tin phiếu kiểm tra
        JLabel lblFormTitle = createSectionTitle("Thông tin phiếu kiểm tra");
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã phiếu kiểm tra (tự động)", txtMaPKT, 0, 0);
        addField(pnlFields, "Phiếu tiếp nhận *", cboMaPTN, 2, 0);
        addField(pnlFields, "Mã khách hàng", txtMaKH, 0, 1);
        addField(pnlFields, "Mã xe", txtMaXe, 2, 1);
        addField(pnlFields, "Nhân viên kiểm tra *", cboNhanVienKiemTra, 0, 2);
        addField(pnlFields, "Biển số xe", txtBienSo, 2, 2);
        addLongField(pnlFields, "Trạng thái xe ban đầu *", txaTrangThai, 0, 3);
        addLongField(pnlFields, "Vấn đề hư hỏng *", txaVanDe, 2, 3);

        // vùng nút thao tác phiếu kiểm tra
        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnThem, buttonConstraints(0));
        pnlButtons.add(btnSua, buttonConstraints(1));
        pnlButtons.add(btnXoa, buttonConstraints(2));
        pnlButtons.add(btnLamMoi, buttonConstraints(3));

        //vùng tổng hợp chứa form và nút thao tác
        JPanel pnlFieldsAndButtons = new JPanel();
        pnlFieldsAndButtons.setOpaque(false);
        pnlFieldsAndButtons.setLayout(new javax.swing.BoxLayout(
                pnlFieldsAndButtons, javax.swing.BoxLayout.Y_AXIS));
        pnlFields.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlFields.getPreferredSize().height));
        pnlButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlButtons.getPreferredSize().height));
        pnlFieldsAndButtons.add(pnlFields);
        pnlFieldsAndButtons.add(javax.swing.Box.createVerticalStrut(8));
        pnlFieldsAndButtons.add(pnlButtons);

        pnlFormCard.add(lblFormTitle, BorderLayout.NORTH);
        pnlFormCard.add(pnlFieldsAndButtons, BorderLayout.CENTER);
        return pnlFormCard;
    }

    //vùng bảng danh sách phiếu kiểm tra
    private JPanel createTablePanel() {
        JPanel pnlTableCard = AppPanel.card(new BorderLayout(0, 8));
        JPanel pnlTableHeader = new JPanel(new BorderLayout(8, 8));
        pnlTableHeader.setOpaque(false);
        JPanel pnlTitleBar = new JPanel(new BorderLayout());
        pnlTitleBar.setOpaque(false);
        pnlTitleBar.add(createSectionTitle("Danh sách phiếu kiểm tra"), BorderLayout.WEST);
        pnlTitleBar.add(btnTaiLai, BorderLayout.EAST);

        //vùng tìm kiếm theo thuộc tính
        JPanel pnlLoc = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã phiếu kiểm tra", txtLocMaPKT));
        pnlLoc.add(createSearchField("Mã phiếu tiếp nhận", txtLocMaPTN));
        pnlLoc.add(createSearchField("Mã xe", txtLocMaXe));
        pnlTableHeader.add(pnlTitleBar, BorderLayout.NORTH);
        pnlTableHeader.add(pnlLoc, BorderLayout.CENTER);

        tblPhieuKiemTra.setRowSorter(sorterPhieuKiemTra);
        tblPhieuKiemTra.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhieuKiemTra.setFillsViewportHeight(true);
        tblPhieuKiemTra.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        pnlTableCard.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableCard.add(new AppScrollPane(tblPhieuKiemTra), BorderLayout.CENTER);
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

    //tạo title cho các section trong form
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
        gbcLabel.insets = new Insets(2, 0, 2, 8);
        JLabel lblField = new JLabel(label);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_TEXT);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(2, 0, 2, 12);
        pnlFields.add(component, gbcInput);
    }

    private void addLongField(
            JPanel pnlFields, String label, javax.swing.JTextArea txaField, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.gridwidth = 2;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(4, 0, 2, column == 0 ? 12 : 0);
        JLabel lblField = new JLabel(label);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_TEXT);
        pnlFields.add(lblField, gbcLabel);

        txaField.setLineWrap(true);
        txaField.setWrapStyleWord(true);
        txaField.setRows(3);
        txaField.setFont(UIConfig.FONT_REGULAR);
        txaField.setForeground(UIConfig.COLOR_TEXT);
        txaField.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
        javax.swing.JScrollPane scrField = new AppScrollPane(txaField);
        scrField.setPreferredSize(new Dimension(0, 108));
        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column;
        gbcInput.gridy = row + 1;
        gbcInput.gridwidth = 2;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(0, 0, 2, column == 0 ? 12 : 0);
        pnlFields.add(scrField, gbcInput);
    }

    private GridBagConstraints buttonConstraints(int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.insets = new Insets(3, 0, 2, 8);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void cauHinhThanhPhan() {
        txtMaPKT.setEditable(false);
        for (AppTextField txtField : new AppTextField[] {txtMaKH, txtMaXe, txtBienSo}) {
            txtField.setEditable(false);
            txtField.setBackground(UIConfig.COLOR_BACKGROUND);
        }
        cboMaPTN.setFont(UIConfig.FONT_REGULAR);
        cboNhanVienKiemTra.setFont(UIConfig.FONT_REGULAR);
        cboNhanVienKiemTra.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lblItem = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof LichLamViecDAO.NhanVienTrongCa nhanVien) {
                    lblItem.setText(nhanVien.hoTen() + " (" + nhanVien.maNV() + ")");
                }
                return lblItem;
            }
        });
        cboMaPTN.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lblItem = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhieuTiepNhan phieuTiepNhan) {
                    lblItem.setText(thongTinPhieuTiepNhan(phieuTiepNhan));
                }
                return lblItem;
            }
        });
        tblPhieuKiemTra.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblPhieuKiemTra.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblPhieuKiemTra.getColumnModel().getColumn(5).setPreferredWidth(180);
        tblPhieuKiemTra.getColumnModel().getColumn(6).setPreferredWidth(220);
    }

    public void hienThiDanhSachNhanVienTrongCa(
            List<LichLamViecDAO.NhanVienTrongCa> danhSach) {
        cboNhanVienKiemTra.removeAllItems();
        for (LichLamViecDAO.NhanVienTrongCa nhanVien : danhSach) {
            cboNhanVienKiemTra.addItem(nhanVien);
        }
        cboNhanVienKiemTra.setToolTipText(cboNhanVienKiemTra.getItemCount() == 0
                ? "Không có nhân viên được phân công trong ca hiện tại."
                : "Chọn nhân viên được phân công trong ca hiện tại.");
    }

    public void hienThiDanhSachPhieuTiepNhan(
            List<PhieuTiepNhan> danhSach,
            Map<String, String> bienSoTheoMaXeMoi,
            Map<String, String> tenKhachHangTheoMaKHMoi) {
        bienSoTheoMaXe.clear();
        bienSoTheoMaXe.putAll(bienSoTheoMaXeMoi);
        tenKhachHangTheoMaKH.clear();
        tenKhachHangTheoMaKH.putAll(tenKhachHangTheoMaKHMoi);
        cboMaPTN.removeAllItems();
        for (PhieuTiepNhan phieuTiepNhan : danhSach) {
            cboMaPTN.addItem(phieuTiepNhan);
        }
    }

    public void capNhatThongTinTheoPhieuTiepNhan() {
        PhieuTiepNhan phieuTiepNhan = (PhieuTiepNhan) cboMaPTN.getSelectedItem();
        if (phieuTiepNhan == null) {
            txtMaKH.setText("");
            txtMaXe.setText("");
            txtBienSo.setText("");
            return;
        }
        txtMaKH.setText(phieuTiepNhan.getKhachHang().getMaKH());
        txtMaXe.setText(phieuTiepNhan.getXe().getMaXe());
        txtBienSo.setText(giaTriThongTin(bienSoTheoMaXe, phieuTiepNhan.getXe().getMaXe()));
    }

    private String thongTinPhieuTiepNhan(PhieuTiepNhan phieuTiepNhan) {
        String bienSo = giaTriThongTin(bienSoTheoMaXe, phieuTiepNhan.getXe().getMaXe());
        String tenKhachHang = giaTriThongTin(
                tenKhachHangTheoMaKH, phieuTiepNhan.getKhachHang().getMaKH());
        return phieuTiepNhan.getMaPhieuTiepNhan()
                + " - Biển số: " + bienSo
                + " - Khách hàng: " + tenKhachHang;
    }

    private String giaTriThongTin(Map<String, String> thongTinTheoMa, String ma) {
        if (ma == null) {
            return "";
        }
        return thongTinTheoMa.getOrDefault(ma, "");
    }

    public void hienThiDanhSachPhieuKiemTra(List<PhieuKiemTra> danhSach) {
        modelPhieuKiemTra.setRowCount(0);
        for (PhieuKiemTra phieu : danhSach) {
            modelPhieuKiemTra.addRow(new Object[] {
                    phieu.getMaPhieuKiemTra(),
                    phieu.getPhieuTiepNhan().getMaPhieuTiepNhan(),
                    phieu.getKhachHang().getMaKH(),
                    phieu.getXe().getMaXe(),
                    phieu.getNhanVien().getMaNV(),
                    phieu.getTrangThaiBanDau(),
                    phieu.getVanDeHuHong()
            });
        }
        locDanhSach();
    }

    public PhieuKiemTra layPhieuKiemTraNhap() {
        String maPKT = txtMaPKT.getText().trim();
        PhieuTiepNhan phieuTiepNhan = (PhieuTiepNhan) cboMaPTN.getSelectedItem();
        String trangThai = txaTrangThai.getText().trim();
        String vanDe = txaVanDe.getText().trim();
        if (maPKT.isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu kiểm tra không được để trống.");
        }
        if (maPKT.length() > 20) {
            throw new IllegalArgumentException("Mã phiếu kiểm tra không được vượt quá 20 ký tự.");
        }
        if (phieuTiepNhan == null) {
            throw new IllegalArgumentException("Hãy chọn phiếu tiếp nhận.");
        }
        LichLamViecDAO.NhanVienTrongCa nhanVien =
                (LichLamViecDAO.NhanVienTrongCa) cboNhanVienKiemTra.getSelectedItem();
        if (nhanVien == null) {
            throw new IllegalArgumentException("Không có nhân viên đang trong ca để lập phiếu kiểm tra.");
        }
        if (trangThai.isEmpty()) {
            throw new IllegalArgumentException("Trạng thái xe ban đầu không được để trống.");
        }
        if (vanDe.isEmpty()) {
            throw new IllegalArgumentException("Vấn đề hư hỏng không được để trống.");
        }

        return new PhieuKiemTra(
                maPKT,
                phieuTiepNhan,
                phieuTiepNhan.getKhachHang(),
                phieuTiepNhan.getXe(),
                new entity.NhanVien(nhanVien.maNV()),
                trangThai,
                vanDe);
    }

    public void hienThiPhieuDuocChon(PhieuKiemTra phieu) {
        txtMaPKT.setText(phieu.getMaPhieuKiemTra());
        String maPTN = phieu.getPhieuTiepNhan().getMaPhieuTiepNhan();
        for (int index = 0; index < cboMaPTN.getItemCount(); index++) {
            PhieuTiepNhan phieuTiepNhan = cboMaPTN.getItemAt(index);
            if (phieuTiepNhan.getMaPhieuTiepNhan().equals(maPTN)) {
                cboMaPTN.setSelectedItem(phieuTiepNhan);
                break;
            }
        }
        String maNV = phieu.getNhanVien().getMaNV();
        boolean timThayNhanVien = false;
        for (int index = 0; index < cboNhanVienKiemTra.getItemCount(); index++) {
            LichLamViecDAO.NhanVienTrongCa nhanVien = cboNhanVienKiemTra.getItemAt(index);
            if (nhanVien.maNV().equals(maNV)) {
                cboNhanVienKiemTra.setSelectedItem(nhanVien);
                timThayNhanVien = true;
                break;
            }
        }
        if (!timThayNhanVien) {
            cboNhanVienKiemTra.setSelectedItem(null);
        }
        txaTrangThai.setText(phieu.getTrangThaiBanDau());
        txaVanDe.setText(phieu.getVanDeHuHong());
        thongBao("Đang chọn phiếu kiểm tra " + txtMaPKT.getText());
    }

    public void lamMoiBieuMau(String maMoi) {
        txtMaPKT.setText(maMoi);
        if (cboMaPTN.getItemCount() > 0) {
            cboMaPTN.setSelectedIndex(0);
        } else {
            capNhatThongTinTheoPhieuTiepNhan();
        }
        if (cboNhanVienKiemTra.getItemCount() > 0) {
            cboNhanVienKiemTra.setSelectedIndex(0);
        } else {
            cboNhanVienKiemTra.setSelectedItem(null);
        }
        txaTrangThai.setText("");
        txaVanDe.setText("");
        tblPhieuKiemTra.clearSelection();
        thongBao("Sẵn sàng");
    }

    private JPanel createSearchField(String label, AppTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new JLabel(label);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_MUTED);
        pnlField.add(lblField, BorderLayout.NORTH);
        pnlField.add(txtField, BorderLayout.CENTER);
        return pnlField;
    }

    public void locDanhSach() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        themBoLoc(filters, txtLocMaPKT.getText(), 0);
        themBoLoc(filters, txtLocMaPTN.getText(), 1);
        themBoLoc(filters, txtLocMaXe.getText(), 3);
        sorterPhieuKiemTra.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblThongBao.setText("Hiển thị " + sorterPhieuKiemTra.getViewRowCount()
                + " / " + modelPhieuKiemTra.getRowCount() + " phiếu kiểm tra");
    }

    private void themBoLoc(List<RowFilter<Object, Object>> filters, String giaTri, int cot) {
        String tuKhoa = giaTri.trim();
        if (!tuKhoa.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa), cot));
        }
    }

    public void xoaBoLoc() {
        txtLocMaPKT.setText("");
        txtLocMaPTN.setText("");
        txtLocMaXe.setText("");
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }

    public String getMaPhieuNhap() {
        return txtMaPKT.getText();
    }

    public String getMaPhieuDangChon() {
        int viewRow = tblPhieuKiemTra.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblPhieuKiemTra.convertRowIndexToModel(viewRow);
        return text(modelPhieuKiemTra.getValueAt(modelRow, 0));
    }

    public boolean xacNhanXoa(String maPhieu) {
        return JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa phiếu kiểm tra " + maPhieu + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    public void hienThiLoi(String message) {
        lblThongBao.setForeground(new java.awt.Color(190, 55, 55));
        lblThongBao.setText(message);
        JOptionPane.showMessageDialog(this, message, "Phiếu kiểm tra", JOptionPane.ERROR_MESSAGE);
    }

    public AppButton getBtnThem() { return btnThem; }
    public AppButton getBtnSua() { return btnSua; }
    public AppButton getBtnXoa() { return btnXoa; }
    public AppButton getBtnLamMoi() { return btnLamMoi; }
    public AppButton getBtnTaiLai() { return btnTaiLai; }
    public javax.swing.JComboBox<PhieuTiepNhan> getCboMaPTN() { return cboMaPTN; }
    public AppTable getTblPhieuKiemTra() { return tblPhieuKiemTra; }
    public AppTextField getTxtLocMaPKT() { return txtLocMaPKT; }
    public AppTextField getTxtLocMaPTN() { return txtLocMaPTN; }
    public AppTextField getTxtLocMaXe() { return txtLocMaXe; }
}

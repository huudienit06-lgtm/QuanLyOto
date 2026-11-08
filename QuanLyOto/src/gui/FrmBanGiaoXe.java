package gui;

import controller.BanGiaoXeController;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuBanGiao;
import entity.TaiKhoan;
import entity.Xe;
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
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmBanGiaoXe extends JPanel {
    private final JTextField txtMaPhieuBanGiao = new JTextField(14);
    private final JComboBox<Xe> cboXe = new JComboBox<>();
    private final JTextField txtMaKH = new JTextField(14);
    private final JTextField txtTenKhachHang = new JTextField(14);
    private final JComboBox<NhanVien> cboNhanVien = new JComboBox<>();
    private final JTextField txtTenNguoiNhan = new JTextField(14);
    private final JTextField txtSoDienThoaiNguoiNhan = new JTextField(14);
    private final JTextArea txaTinhTrangXe = new JTextArea(5, 20);
    private final JTextField txtTimKiem = new JTextField(20);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final JButton btnThem = createButton(
            "Lập phiếu", "/resources/icons/plus.png", UIConfig.COLOR_SUCCESS, Color.WHITE);
    private final JButton btnCapNhat = createButton(
            "Cập nhật", "/resources/icons/update.png", UIConfig.COLOR_PRIMARY, Color.WHITE);
    private final JButton btnXoa = createButton(
            "Xóa", "/resources/icons/trash.png", UIConfig.COLOR_DANGER, Color.WHITE);
    private final JButton btnLamMoi = createButton(
            "Làm mới", "/resources/icons/reset.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
    private final JButton btnTaiLai = createButton(
            "Tải lại", "/resources/icons/reload.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);

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
    private final JTable tblBanGiao = new JTable(modelBanGiao);
    private final TableRowSorter<DefaultTableModel> sorterBanGiao = new TableRowSorter<>(modelBanGiao);
    private final BanGiaoXeController controller;

    public FrmBanGiaoXe() {
        this(null);
    }

    public FrmBanGiaoXe(TaiKhoan taiKhoanHienTai) {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header trang
        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormCard(), BorderLayout.NORTH);
        pnlContent.add(createTableCard(), BorderLayout.CENTER);

        add(pnlContent, BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        configureComponents();
        controller = new BanGiaoXeController(this, taiKhoanHienTai);
        controller.khoiTao();
    }

    private JPanel createHeaderPanel() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 4));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("PHIẾU BÀN GIAO XE");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);

        JLabel lblSubtitle = new JLabel("Xác nhận tình trạng xe và thông tin người nhận sau khi hoàn tất dịch vụ");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel createFormCard() {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 12));

        JLabel lblSection = new JLabel("Thông tin bàn giao");
        lblSection.setFont(UIConfig.FONT_SECTION);
        lblSection.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblSection, BorderLayout.NORTH);

        // Các ô nhập
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã phiếu (tự động)", txtMaPhieuBanGiao, 0, 0);
        addField(pnlFields, "Xe bàn giao *", cboXe, 2, 0);
        addField(pnlFields, "Mã khách hàng", txtMaKH, 0, 1);
        addField(pnlFields, "Tên khách hàng", txtTenKhachHang, 2, 1);
        addField(pnlFields, "Nhân viên lập phiếu *", cboNhanVien, 0, 2);
        addField(pnlFields, "Tên người nhận *", txtTenNguoiNhan, 2, 2);
        addField(pnlFields, "Số điện thoại *", txtSoDienThoaiNguoiNhan, 0, 3);

        // Khu vực tình trạng xe
        JPanel pnlCondition = new JPanel(new BorderLayout(0, 4));
        pnlCondition.setOpaque(false);
        JLabel lblCondition = new JLabel("Tình trạng xe hiện tại *");
        lblCondition.setFont(UIConfig.FONT_REGULAR);
        lblCondition.setForeground(UIConfig.COLOR_MUTED);
        pnlCondition.add(lblCondition, BorderLayout.NORTH);

        txaTinhTrangXe.setLineWrap(true);
        txaTinhTrangXe.setWrapStyleWord(true);
        txaTinhTrangXe.setFont(UIConfig.FONT_REGULAR);
        txaTinhTrangXe.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane scrCondition = new JScrollPane(txaTinhTrangXe);
        scrCondition.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        pnlCondition.add(scrCondition, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(new GridLayout(1, 2, 24, 0));
        pnlBody.setOpaque(false);
        pnlBody.add(pnlFields);
        pnlBody.add(pnlCondition);

        // Hàng nút chức năng
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

        pnlCard.add(pnlBody, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createTableCard() {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách phiếu bàn giao");
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // Toolbar tìm kiếm
        JPanel pnlToolbar = new JPanel(new BorderLayout(8, 0));
        pnlToolbar.setOpaque(false);
        JLabel lblTimKiem = new JLabel("Tìm kiếm");
        lblTimKiem.setFont(UIConfig.FONT_REGULAR);
        lblTimKiem.setForeground(UIConfig.COLOR_MUTED);
        ImageIcon iconSearch = getScaledIcon("/resources/icons/search.png", 14, 14);
        if (iconSearch != null) {
            lblTimKiem.setIcon(iconSearch);
            lblTimKiem.setIconTextGap(4);
        }
        pnlToolbar.add(lblTimKiem, BorderLayout.WEST);
        pnlToolbar.add(txtTimKiem, BorderLayout.CENTER);
        pnlToolbar.add(btnTaiLai, BorderLayout.EAST);

        pnlHeader.add(pnlToolbar, BorderLayout.EAST);
        pnlCard.add(pnlHeader, BorderLayout.NORTH);

        JScrollPane scrTable = new JScrollPane(tblBanGiao);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(scrTable, BorderLayout.CENTER);

        return pnlCard;
    }

    private JPanel createStatusPanel() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private void configureComponents() {
        JTextField[] textFields = {
            txtMaPhieuBanGiao, txtMaKH, txtTenKhachHang, txtTenNguoiNhan,
            txtSoDienThoaiNguoiNhan, txtTimKiem
        };
        for (JTextField textField : textFields) {
            textField.setFont(UIConfig.FONT_REGULAR);
            textField.setForeground(UIConfig.COLOR_TEXT);
            textField.setBackground(UIConfig.COLOR_SURFACE);
            textField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1), new EmptyBorder(6, 8, 6, 8)));
        }
        txtMaPhieuBanGiao.setEditable(false);
        txtMaPhieuBanGiao.setToolTipText("Mã phiếu duy nhất, ví dụ: PBG001");
        txtMaKH.setEditable(false);
        txtTenKhachHang.setEditable(false);

        cboXe.setFont(UIConfig.FONT_REGULAR);
        cboNhanVien.setFont(UIConfig.FONT_REGULAR);

        cboXe.setRenderer(new ComboBoxRenderer<Xe>() {
            @Override
            protected String getText(Xe value) {
                return value.getBienSo() + " - " + value.getHangXe() + " " + value.getDongXe();
            }
        });
        cboNhanVien.setRenderer(new ComboBoxRenderer<NhanVien>() {
            @Override
            protected String getText(NhanVien value) {
                return value.getMaNV() + " - " + value.getHoTen();
            }
        });

        tblBanGiao.setFont(UIConfig.FONT_REGULAR);
        tblBanGiao.setForeground(UIConfig.COLOR_TEXT);
        tblBanGiao.setRowHeight(30);
        tblBanGiao.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblBanGiao.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblBanGiao.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblBanGiao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBanGiao.setFillsViewportHeight(true);
        tblBanGiao.setRowSorter(sorterBanGiao);

        tblBanGiao.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblBanGiao.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblBanGiao.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblBanGiao.getTableHeader().setPreferredSize(new Dimension(0, 34));
    }

    // --- Tiện ích style nội bộ ---
    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(UIConfig.COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)));
        return card;
    }

    private JButton createButton(String text, String iconPath, Color background, Color foreground) {
        JButton btn = new JButton(text);
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
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

    // --- Các phương thức nghiệp vụ kết nối với Controller ---
    public void hienThiDanhSachXe(List<Xe> danhSachXe) {
        cboXe.setModel(new DefaultComboBoxModel<>(danhSachXe.toArray(Xe[]::new)));
    }

    public void hienThiDanhSachNhanVien(List<NhanVien> danhSachNhanVien) {
        cboNhanVien.setModel(new DefaultComboBoxModel<>(danhSachNhanVien.toArray(NhanVien[]::new)));
    }

    public void hienThiDanhSachPhieu(List<PhieuBanGiao> danhSachPhieu) {
        modelBanGiao.setRowCount(0);
        for (PhieuBanGiao phieu : danhSachPhieu) {
            Xe xe = phieu.getXe();
            KhachHang khachHang = phieu.getKhachHang();
            NhanVien nhanVien = phieu.getNhanVien();
            modelBanGiao.addRow(new Object[] {
                    phieu.getMaPhieuBanGiao(),
                    xe == null ? "" : xe.getBienSo(),
                    khachHang == null ? "" : khachHang.getMaKH(),
                    khachHang == null ? "" : khachHang.getHoTen(),
                    nhanVien == null ? "" : nhanVien.getHoTen(),
                    phieu.getTenNguoiNhan(),
                    phieu.getSoDienThoaiNguoiNhan()
            });
        }
        locDanhSach();
    }

    public void hienThiThongTinKhachHang(KhachHang khachHang) {
        txtMaKH.setText(khachHang == null ? "" : khachHang.getMaKH());
        txtTenKhachHang.setText(khachHang == null ? "" : khachHang.getHoTen());
    }

    public void hienThiPhieu(PhieuBanGiao phieu) {
        txtMaPhieuBanGiao.setText(phieu.getMaPhieuBanGiao());
        chonXe(phieu.getXe() == null ? null : phieu.getXe().getMaXe());
        chonNhanVien(phieu.getNhanVien() == null ? null : phieu.getNhanVien().getMaNV());
        txtTenNguoiNhan.setText(phieu.getTenNguoiNhan());
        txtSoDienThoaiNguoiNhan.setText(phieu.getSoDienThoaiNguoiNhan());
        txaTinhTrangXe.setText(phieu.getTinhTrangXeHienTai());
        thongBao("Đang chọn phiếu " + phieu.getMaPhieuBanGiao());
    }

    public void lamMoiBieuMau(String maMoi, String maNVDangNhap) {
        txtMaPhieuBanGiao.setText(maMoi);
        if (cboXe.getItemCount() > 0) {
            cboXe.setSelectedIndex(0);
        } else {
            hienThiThongTinKhachHang(null);
        }
        if (maNVDangNhap != null && !maNVDangNhap.trim().isEmpty()) {
            chonNhanVien(maNVDangNhap.trim());
        } else if (cboNhanVien.getItemCount() > 0) {
            cboNhanVien.setSelectedIndex(0);
        }
        txtTenNguoiNhan.setText("");
        txtSoDienThoaiNguoiNhan.setText("");
        txaTinhTrangXe.setText("");
        tblBanGiao.clearSelection();
        thongBao("Sẵn sàng");
    }

    public void locDanhSach() {
        String tuKhoa = txtTimKiem.getText().trim();
        sorterBanGiao.setRowFilter(tuKhoa.isEmpty()
                ? null
                : RowFilter.regexFilter("(?iu)" + java.util.regex.Pattern.quote(tuKhoa)));
        lblThongBao.setText("Hiển thị " + sorterBanGiao.getViewRowCount()
                + " / " + modelBanGiao.getRowCount() + " phiếu bàn giao");
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

    public boolean xacNhanXoa(String maPhieu) {
        return JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa phiếu bàn giao " + maPhieu + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public boolean xacThucForm() {
        if (getXeDuocChon() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn xe bàn giao.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            cboXe.requestFocusInWindow();
            return false;
        }
        if (getNhanVienDuocChon() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên lập phiếu.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            cboNhanVien.requestFocusInWindow();
            return false;
        }
        if (getTenNguoiNhanNhap().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên người nhận.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtTenNguoiNhan.requestFocusInWindow();
            return false;
        }
        if (getSoDienThoaiNguoiNhanNhap().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại người nhận.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtSoDienThoaiNguoiNhan.requestFocusInWindow();
            return false;
        }
        if (getTinhTrangXeNhap().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tình trạng xe.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txaTinhTrangXe.requestFocusInWindow();
            return false;
        }
        return true;
    }

    public void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    public void hienThiLoi(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        lblThongBao.setText(message);
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
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

    public JComboBox<Xe> getCboXe() {
        return cboXe;
    }

    public JTextField getTxtTimKiem() {
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

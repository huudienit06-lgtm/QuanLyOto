package gui;

import entity.TaiKhoan;
import controller.NhapXuatKhoController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FrmNhapXuatPhuTung extends JPanel {
    private static final Color COLOR_BG_MAIN = new Color(244, 247, 252);
    private static final Color COLOR_PANEL_BG = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_SUCCESS = new Color(22, 163, 74);
    private static final Color COLOR_DANGER = new Color(220, 38, 38);
    private static final Color COLOR_WARNING = new Color(234, 88, 12);
    private static final Color COLOR_SECONDARY = new Color(241, 245, 249); 
    private static final Color COLOR_BORDER = new Color(221, 231, 245);
    private static final Color COLOR_READONLY = new Color(248, 250, 252);

    private final TaiKhoan taiKhoanHienTai;
    private JRadioButton radNhapKho, radXuatKho;
    private JLabel lblDonGia; 
    private final JTextField txtMaPhieu = new JTextField();
    private final JTextField txtNgayLap = new JTextField();
    private final JTextField txtNguoiLap = new JTextField();
    private final JTextField txtGhiChu = new JTextField();
    private JButton btnTaoPhieu, btnLuuPhieu, btnXoaPhieu, btnLamMoi;
    private final JComboBox<String> cboPhuTung = new JComboBox<>();
    private final JTextField txtTonKho = new JTextField();
    private final JTextField txtSoLuong = new JTextField();
    private final JTextField txtDonGia = new JTextField();
    private JButton btnThemChiTiet, btnXoaChiTiet;
    private DefaultTableModel modelChiTiet;
    private JTable tblChiTiet;
    private JLabel lblTongTien;
    private final JTextField txtTimMaPhieu = new JTextField();
    private final JTextField txtTimMaNV = new JTextField();
    private final JTextField txtTimPhuTung = new JTextField();
    private JButton btnTaiLai;
    private DefaultTableModel modelLichSu;
    private JTable tblLichSu;
    private JLabel lblSoLuongPhieu;

    private final NhapXuatKhoController controller;

    public FrmNhapXuatPhuTung(TaiKhoan taiKhoan) {
        this.taiKhoanHienTai = taiKhoan;
        initComponents();
        this.controller = new NhapXuatKhoController(this);
        radNhapKho.setSelected(true);
        controller.switchMode(true); 
        controller.khoiTao();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBackground(COLOR_BG_MAIN);
        setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_BG_MAIN);
        JLabel lblTitle = new JLabel("QUẢN LÝ GIAO DỊCH KHO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(32, 55, 92));

        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        pnlRadio.setBackground(COLOR_BG_MAIN);
        JLabel lblLoaiGD = new JLabel("LOẠI GIAO DỊCH:");
        lblLoaiGD.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        radNhapKho = new JRadioButton("PHIẾU NHẬP KHO");
        radNhapKho.setFont(new Font("Segoe UI", Font.BOLD, 12));
        radNhapKho.setBackground(COLOR_BG_MAIN);
        radNhapKho.setForeground(COLOR_PRIMARY);
        
        radXuatKho = new JRadioButton("PHIẾU XUẤT KHO");
        radXuatKho.setFont(new Font("Segoe UI", Font.BOLD, 12));
        radXuatKho.setBackground(COLOR_BG_MAIN);
        radXuatKho.setForeground(COLOR_WARNING);

        ButtonGroup grp = new ButtonGroup();
        grp.add(radNhapKho); grp.add(radXuatKho);
        pnlRadio.add(lblLoaiGD); pnlRadio.add(radNhapKho); pnlRadio.add(radXuatKho);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlRadio, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlMiddle = new JPanel(new GridBagLayout());
        pnlMiddle.setBackground(COLOR_BG_MAIN);
        GridBagConstraints gbcMiddle = new GridBagConstraints();
        gbcMiddle.fill = GridBagConstraints.BOTH;
        gbcMiddle.weighty = 1.0;

        gbcMiddle.gridx = 0; gbcMiddle.weightx = 0.5;
        gbcMiddle.insets = new Insets(0, 0, 0, 8);
        pnlMiddle.add(createLeftPanel(), gbcMiddle);

        gbcMiddle.gridx = 1; gbcMiddle.weightx = 0.5;
        gbcMiddle.insets = new Insets(0, 8, 0, 0);
        pnlMiddle.add(createRightPanel(), gbcMiddle);

        JPanel pnlMainCenter = new JPanel(new BorderLayout(0, 10));
        pnlMainCenter.setBackground(COLOR_BG_MAIN);
        pnlMainCenter.add(pnlMiddle, BorderLayout.CENTER);
        pnlMainCenter.add(createBottomPanel(), BorderLayout.SOUTH);

        add(pnlMainCenter, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PANEL_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER), " Thông tin phiếu ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), new Color(32, 55, 92)
        ));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(COLOR_PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 12, 12, 12);

        txtMaPhieu.setEditable(false); txtMaPhieu.setBackground(COLOR_READONLY);
        txtNgayLap.setEditable(false); txtNgayLap.setBackground(COLOR_READONLY);
        txtNguoiLap.setEditable(false); txtNguoiLap.setBackground(COLOR_READONLY);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; pnlForm.add(new JLabel("Mã phiếu"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; pnlForm.add(txtMaPhieu, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; pnlForm.add(new JLabel("Ngày lập"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; pnlForm.add(txtNgayLap, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; pnlForm.add(new JLabel("Người lập"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; pnlForm.add(txtNguoiLap, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; pnlForm.add(new JLabel("Ghi chú"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; pnlForm.add(txtGhiChu, gbc);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlButtons.setBackground(COLOR_PANEL_BG);
        btnTaoPhieu = createButton("➕ Tạo phiếu", COLOR_SUCCESS, Color.WHITE);
        btnLuuPhieu = createButton("✎ Cập nhật phiếu", COLOR_PRIMARY, Color.WHITE);
        btnXoaPhieu = createButton("✖ Xóa phiếu", COLOR_DANGER, Color.WHITE);
        btnLamMoi = createButton("⟳ Làm mới", COLOR_SECONDARY, new Color(47, 62, 84));

        pnlButtons.add(btnTaoPhieu); pnlButtons.add(btnLuuPhieu);
        pnlButtons.add(btnXoaPhieu); pnlButtons.add(btnLamMoi);

        panel.add(pnlForm, BorderLayout.CENTER);
        panel.add(pnlButtons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PANEL_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER), " Chi tiết hàng hóa ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), new Color(32, 55, 92)
        ));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(COLOR_PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 12, 8, 12);

        txtTonKho.setEditable(false); txtTonKho.setBackground(COLOR_READONLY);
        txtTonKho.setFont(new Font("Segoe UI", Font.BOLD, 12)); txtTonKho.setForeground(COLOR_DANGER);
        txtDonGia.setEditable(false); txtDonGia.setBackground(COLOR_READONLY);
        txtDonGia.setFont(new Font("Segoe UI", Font.BOLD, 12)); txtDonGia.setForeground(new Color(21, 128, 61));

        lblDonGia = new JLabel("Giá bán niêm yết (VNĐ)");
        lblDonGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        addField(pnlForm, "Phụ tùng *", cboPhuTung, 0, 0, gbc);
        addField(pnlForm, "Tồn kho hiện tại", txtTonKho, 2, 0, gbc);
        addField(pnlForm, "Số lượng *", txtSoLuong, 0, 1, gbc);

        gbc.gridwidth = 1; gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.1; pnlForm.add(lblDonGia, gbc);
        gbc.gridx = 3; gbc.weightx = 0.4; pnlForm.add(txtDonGia, gbc);

        JPanel pnlItemActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlItemActions.setBackground(COLOR_PANEL_BG);
        btnThemChiTiet = createButton("➕ Thêm vào danh sách", COLOR_PRIMARY, Color.WHITE);
        btnXoaChiTiet = createButton("✖ Xóa dòng", COLOR_DANGER, Color.WHITE);
        btnXoaChiTiet.setEnabled(false);
        pnlItemActions.add(btnThemChiTiet); pnlItemActions.add(btnXoaChiTiet);

        String[] columns = {"Mã PT", "Tên phụ tùng", "S.Lượng", "Đơn giá", "Thành tiền"};
        modelChiTiet = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(25);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);
        tblChiTiet.getColumnModel().getColumn(2).setCellRenderer(right);
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(right);
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(right);

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setPreferredSize(new Dimension(0, 110));

        JPanel pnlUpper = new JPanel(new BorderLayout());
        pnlUpper.setBackground(COLOR_PANEL_BG);
        pnlUpper.add(pnlForm, BorderLayout.NORTH);
        pnlUpper.add(pnlItemActions, BorderLayout.SOUTH);

        lblTongTien = new JLabel("Tổng tiền: 0 đ");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongTien.setForeground(COLOR_DANGER);
        lblTongTien.setHorizontalAlignment(JLabel.RIGHT);
        lblTongTien.setBorder(new EmptyBorder(10, 15, 10, 15));

        panel.add(pnlUpper, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblTongTien, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(COLOR_PANEL_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER), " Danh sách phiếu giao dịch đã lập ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), new Color(32, 55, 92)
        ));

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlSearch.setBackground(COLOR_PANEL_BG);
        pnlSearch.add(new JLabel("Mã phiếu:"));
        txtTimMaPhieu.setPreferredSize(new Dimension(140, 26)); pnlSearch.add(txtTimMaPhieu);
        pnlSearch.add(new JLabel("Mã nhân viên:"));
        txtTimMaNV.setPreferredSize(new Dimension(100, 26)); pnlSearch.add(txtTimMaNV);
        pnlSearch.add(new JLabel("Phụ tùng:"));
        txtTimPhuTung.setPreferredSize(new Dimension(130, 26)); pnlSearch.add(txtTimPhuTung);
        btnTaiLai = createButton("⟳ Tải lại", COLOR_SECONDARY, new Color(47, 62, 84));
        btnTaiLai.setPreferredSize(new Dimension(80, 26));
        pnlSearch.add(btnTaiLai);

        String[] columns = {"MÃ PHIẾU", "MÃ NV", "NGÀY LẬP", "LOẠI PHIẾU", "GHI CHÚ", "TỔNG TIỀN"};
        modelLichSu = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLichSu = new JTable(modelLichSu);
        tblLichSu.setRowHeight(28);
        tblLichSu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);
        tblLichSu.getColumnModel().getColumn(5).setCellRenderer(right);
        JScrollPane scroll = new JScrollPane(tblLichSu);
        scroll.setPreferredSize(new Dimension(0, 160));

        lblSoLuongPhieu = new JLabel("Đang tải dữ liệu...");
        lblSoLuongPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoLuongPhieu.setForeground(Color.GRAY);
        lblSoLuongPhieu.setBorder(new EmptyBorder(5, 10, 5, 0));

        panel.add(pnlSearch, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblSoLuongPhieu, BorderLayout.SOUTH);
        return panel;
    }

    private void addField(JPanel parent, String label, java.awt.Component field, int col, int row, GridBagConstraints gbc) {
        gbc.gridwidth = 1; gbc.gridx = col; gbc.gridy = row; gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        parent.add(lbl, gbc); gbc.gridx = col + 1; gbc.weightx = 0.4; parent.add(field, gbc);
    }
    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text); btn.setPreferredSize(new Dimension(135, 28)); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11)); btn.setBackground(bg); btn.setForeground(fg);
        btn.setFocusPainted(false); btn.setBorder(new EmptyBorder(0,0,0,0)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }

    public TaiKhoan getTaiKhoanHienTai() { return taiKhoanHienTai; }
    public JRadioButton getRadNhapKho() { return radNhapKho; }
    public JRadioButton getRadXuatKho() { return radXuatKho; }
    public JLabel getLblDonGia() { return lblDonGia; }
    public JTextField getTxtMaPhieu() { return txtMaPhieu; }
    public JTextField getTxtNgayLap() { return txtNgayLap; }
    public JTextField getTxtNguoiLap() { return txtNguoiLap; }
    public JTextField getTxtGhiChu() { return txtGhiChu; }
    public JButton getBtnTaoPhieu() { return btnTaoPhieu; }
    public JButton getBtnLuuPhieu() { return btnLuuPhieu; }
    public JButton getBtnXoaPhieu() { return btnXoaPhieu; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JComboBox<String> getCboPhuTung() { return cboPhuTung; }
    public JTextField getTxtTonKho() { return txtTonKho; }
    public JTextField getTxtSoLuong() { return txtSoLuong; }
    public JTextField getTxtDonGia() { return txtDonGia; }
    public JButton getBtnThemChiTiet() { return btnThemChiTiet; }
    public JButton getBtnXoaChiTiet() { return btnXoaChiTiet; }
    public DefaultTableModel getModelChiTiet() { return modelChiTiet; }
    public JTable getTblChiTiet() { return tblChiTiet; }
    public JLabel getLblTongTien() { return lblTongTien; }
    public JTextField getTxtTimMaPhieu() { return txtTimMaPhieu; }
    public JTextField getTxtTimMaNV() { return txtTimMaNV; }
    public JTextField getTxtTimPhuTung() { return txtTimPhuTung; }
    public JButton getBtnTaiLai() { return btnTaiLai; }
    public DefaultTableModel getModelLichSu() { return modelLichSu; }
    public JTable getTblLichSu() { return tblLichSu; }
    public JLabel getLblSoLuongPhieu() { return lblSoLuongPhieu; }
}
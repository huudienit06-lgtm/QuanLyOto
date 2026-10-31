package gui;

import dao.PhuTungDAO;
import dao.PhieuNhapXuatKhoDAO;
import entity.PhuTung;
import entity.PhieuNhapXuatKho;
import entity.ChiTietPhieuNhap;
import entity.TaiKhoan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private final PhuTungDAO phuTungDAO = new PhuTungDAO();
    private final PhieuNhapXuatKhoDAO giaoDichDAO = new PhieuNhapXuatKhoDAO();
    private final DecimalFormat df = new DecimalFormat("#,### đ");
    
    private final TaiKhoan taiKhoanHienTai;
    private boolean isNhapKho = true;

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

    public FrmNhapXuatPhuTung(TaiKhoan taiKhoan) {
        this.taiKhoanHienTai = taiKhoan;
        initComponents();
        setupActionEvents();
        radNhapKho.setSelected(true);
        switchMode(true); 
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

        // Dùng GridBagLayout dạng 2 cột (Nhãn trái - Ô nhập phải) với khoảng cách giãn rộng
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(COLOR_PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 12, 12, 12); // Tăng khoảng cách cách dòng rộng ra

        txtMaPhieu.setEditable(false); txtMaPhieu.setBackground(COLOR_READONLY);
        txtNgayLap.setEditable(false); txtNgayLap.setBackground(COLOR_READONLY);
        txtNguoiLap.setEditable(false); txtNguoiLap.setBackground(COLOR_READONLY);

        // Hàng 1: Mã phiếu
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Mã phiếu"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; // Ô nhập kéo to hết cỡ chiều ngang
        pnlForm.add(txtMaPhieu, gbc);

        // Hàng 2: Ngày lập
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Ngày lập"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnlForm.add(txtNgayLap, gbc);

        // Hàng 3: Người lập
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Người lập"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnlForm.add(txtNguoiLap, gbc);

        // Hàng 4: Ghi chú
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnlForm.add(new JLabel("Ghi chú"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnlForm.add(txtGhiChu, gbc);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlButtons.setBackground(COLOR_PANEL_BG);
        
        btnTaoPhieu = createButton("➕ Tạo phiếu", COLOR_SUCCESS, Color.WHITE);
        btnLuuPhieu = createButton("💾 Lưu phiếu", COLOR_PRIMARY, Color.WHITE);
        btnXoaPhieu = createButton("✖ Xóa phiếu", COLOR_DANGER, Color.WHITE);
        btnLamMoi = createButton("⟳ Làm mới", COLOR_SECONDARY, new Color(47, 62, 84));

        pnlButtons.add(btnTaoPhieu);
        pnlButtons.add(btnLuuPhieu);
        pnlButtons.add(btnXoaPhieu);
        pnlButtons.add(btnLamMoi);

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
        pnlItemActions.add(btnThemChiTiet);
        pnlItemActions.add(btnXoaChiTiet);

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

    private void switchMode(boolean isNhap) {
        this.isNhapKho = isNhap;
        modelChiTiet.setRowCount(0);
        capNhatTongTien();
        txtSoLuong.setText("");
        txtDonGia.setText("");
        txtTonKho.setText("");
        txtGhiChu.setText("");

        if (isNhap) {
            lblDonGia.setText("Giá nhập (VNĐ)");
        } else {
            lblDonGia.setText("Giá bán niêm yết (VNĐ)");
        }
        napDuLieuChung();
    }

    private void napDuLieuChung() {
        txtMaPhieu.setText("Tự động tạo khi lưu");
        txtNgayLap.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        if (taiKhoanHienTai != null) {
            String tenHienThi = (taiKhoanHienTai.getHoTenNhanVien() != null && !taiKhoanHienTai.getHoTenNhanVien().trim().isEmpty()) 
                    ? taiKhoanHienTai.getHoTenNhanVien() 
                    : taiKhoanHienTai.getMaNV();
            txtNguoiLap.setText(tenHienThi);
        } else {
            txtNguoiLap.setText("Chưa đăng nhập");
        }
        
        cboPhuTung.removeAllItems();
        cboPhuTung.addItem(""); 
        List<PhuTung> dsPT = phuTungDAO.layTatCaPhuTung();
        if (dsPT != null) {
            for (PhuTung pt : dsPT) {
                cboPhuTung.addItem(pt.getMaPhuTung() + " - " + pt.getTenPhuTung());
            }
        }
        taiDanhSachPhieuLichSu();
    }

    private void taiDanhSachPhieuLichSu() {
        modelLichSu.setRowCount(0);
        List<PhieuNhapXuatKho> dsPhieu = giaoDichDAO.layTatCaPhieu(); 
        if (dsPhieu != null && !dsPhieu.isEmpty()) {
            for (PhieuNhapXuatKho p : dsPhieu) {
                double tongTien = giaoDichDAO.tinhTongTienPhieu(p.getMaPhieuNhapXuat());
                String ngayStr = p.getNgayLapPhieu() != null 
                        ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(p.getNgayLapPhieu()) 
                        : "";

                modelLichSu.addRow(new Object[]{
                    p.getMaPhieuNhapXuat(),
                    p.getMaNV(),
                    ngayStr,
                    p.getKieuPhieu(),
                    p.getKieuPhieu() != null && p.getKieuPhieu().equalsIgnoreCase("Nhập") 
                            ? "Nhập phụ tùng kho" 
                            : "Sửa chữa bảo dưỡng xe",
                    df.format(tongTien)
                });
            }
            lblSoLuongPhieu.setText("Đã tải " + dsPhieu.size() + " phiếu giao dịch");
        } else {
            lblSoLuongPhieu.setText("Đã tải 0 phiếu giao dịch");
        }
    }

    private void setupActionEvents() {
        radNhapKho.addActionListener(e -> switchMode(true));
        radXuatKho.addActionListener(e -> switchMode(false));

        cboPhuTung.addActionListener(e -> {
            if (cboPhuTung.getSelectedIndex() <= 0) {
                txtTonKho.setText(""); txtDonGia.setText(""); return;
            }
            String ptInfo = cboPhuTung.getSelectedItem().toString();
            String maPT = ptInfo.contains(" - ") ? ptInfo.split(" - ")[0] : ptInfo;
            
            PhuTung pt = phuTungDAO.layPhuTungTheoMa(maPT); 
            if (pt != null) {
                txtTonKho.setText(String.valueOf(pt.getSoLuongTon()));
                double gia = isNhapKho ? pt.getGiaNhap() : pt.getGiaBan();
                txtDonGia.setText(df.format(gia).replace(" đ", ""));
            } else {
                txtTonKho.setText(""); txtDonGia.setText("");
            }
        });

        tblChiTiet.getSelectionModel().addListSelectionListener(e -> {
            btnXoaChiTiet.setEnabled(tblChiTiet.getSelectedRow() != -1);
        });

        btnXoaChiTiet.addActionListener(e -> {
            int row = tblChiTiet.getSelectedRow();
            if (row != -1) {
                modelChiTiet.removeRow(row);
                capNhatTongTien();
            }
        });

        btnLamMoi.addActionListener(e -> {
            switchMode(this.isNhapKho);
            taiDanhSachPhieuLichSu();
        });

        btnThemChiTiet.addActionListener(e -> {
            if (cboPhuTung.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String ptInfo = cboPhuTung.getSelectedItem().toString();
            String maPT = ptInfo.contains(" - ") ? ptInfo.split(" - ")[0] : ptInfo;
            String tenPT = ptInfo.contains(" - ") ? ptInfo.split(" - ")[1] : ptInfo;
            
            int soLuong = 0; int tonKho = 0; double donGia = 0;

            try {
                if (!txtTonKho.getText().trim().isEmpty()) tonKho = Integer.parseInt(txtTonKho.getText().trim());
            } catch (Exception ex) { tonKho = 0; }

            try {
                if (txtSoLuong.getText().trim().isEmpty()) throw new Exception("Vui lòng nhập số lượng!");
                soLuong = Integer.parseInt(txtSoLuong.getText().trim());
                if (soLuong <= 0) throw new Exception("Số lượng phải > 0");
                
                if (!isNhapKho && soLuong > tonKho) {
                    throw new Exception("Số lượng xuất (" + soLuong + ") vượt quá tồn kho (" + tonKho + ")");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtSoLuong.requestFocus(); txtSoLuong.selectAll(); return;
            }

            try {
                if (txtDonGia.getText().trim().isEmpty()) throw new Exception("Phụ tùng chưa có giá!");
                donGia = Double.parseDouble(txtDonGia.getText().trim().replace(" đ", "").replace(",", ""));
                if (donGia <= 0) throw new Exception("Giá không hợp lệ (<= 0).");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }

            boolean isMerged = false;
            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                if (modelChiTiet.getValueAt(i, 0).equals(maPT)) {
                    int oldQty = Integer.parseInt(modelChiTiet.getValueAt(i, 2).toString());
                    int newQty = oldQty + soLuong;
                    
                    if (!isNhapKho && newQty > tonKho) {
                        JOptionPane.showMessageDialog(this, "Tổng số lượng xuất (" + newQty + ") vượt quá tồn kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    modelChiTiet.setValueAt(newQty, i, 2);
                    modelChiTiet.setValueAt(df.format(donGia), i, 3);
                    modelChiTiet.setValueAt(df.format(newQty * donGia), i, 4);
                    isMerged = true; break;
                }
            }
            if (!isMerged) {
                modelChiTiet.addRow(new Object[]{ maPT, tenPT, soLuong, df.format(donGia), df.format(soLuong * donGia) });
            }

            capNhatTongTien();
            txtSoLuong.setText(""); cboPhuTung.requestFocus();
        });

        btnLuuPhieu.addActionListener(e -> thucHienLuuGiaoDich(this.isNhapKho));

        btnTaoPhieu.addActionListener(e -> {
            txtMaPhieu.setText("Tự động tạo khi lưu");
            txtNgayLap.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            txtGhiChu.setText("");
            modelChiTiet.setRowCount(0);
            capNhatTongTien();
            if (cboPhuTung.getItemCount() > 0) cboPhuTung.setSelectedIndex(0);
            txtSoLuong.setText("");
        });

        btnXoaPhieu.addActionListener(e -> {
            int row = tblLichSu.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu cần xóa từ bảng bên dưới!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maPhieu = tblLichSu.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phiếu " + maPhieu + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (giaoDichDAO.xoaPhieu(maPhieu)) {
                    JOptionPane.showMessageDialog(this, "Xóa phiếu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    btnLamMoi.doClick();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa phiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        DocumentListener searchListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { thucHienLocTimKiem(); }
            @Override public void removeUpdate(DocumentEvent e) { thucHienLocTimKiem(); }
            @Override public void changedUpdate(DocumentEvent e) { thucHienLocTimKiem(); }
        };
        txtTimMaPhieu.getDocument().addDocumentListener(searchListener);
        txtTimMaNV.getDocument().addDocumentListener(searchListener);
        txtTimPhuTung.getDocument().addDocumentListener(searchListener);

        btnTaiLai.addActionListener(e -> {
            txtTimMaPhieu.setText("");
            txtTimMaNV.setText("");
            txtTimPhuTung.setText("");
            taiDanhSachPhieuLichSu();
        });

        tblLichSu.getSelectionModel().addListSelectionListener(e -> {
            int row = tblLichSu.getSelectedRow();
            if (row != -1 && !e.getValueIsAdjusting()) {
                String maPhieu = tblLichSu.getValueAt(row, 0).toString();
                String kieu = tblLichSu.getValueAt(row, 3).toString();
                
                if (kieu != null && kieu.equalsIgnoreCase("Nhập")) {
                    radNhapKho.setSelected(true);
                    this.isNhapKho = true;
                } else {
                    radXuatKho.setSelected(true);
                    this.isNhapKho = false;
                }

                txtMaPhieu.setText(maPhieu);
                String ngayVal = tblLichSu.getValueAt(row, 2).toString();
                txtNgayLap.setText(ngayVal.contains(" ") ? ngayVal.split(" ")[0] : ngayVal);
                txtNguoiLap.setText(tblLichSu.getValueAt(row, 1).toString());
                txtGhiChu.setText(tblLichSu.getValueAt(row, 4) != null ? tblLichSu.getValueAt(row, 4).toString() : "");

                napChiTietHangVaoBangPhai(maPhieu);
            }
        });
    }

    private void napChiTietHangVaoBangPhai(String maPhieu) {
        modelChiTiet.setRowCount(0);
        List<ChiTietPhieuNhap> dsChiTiet = giaoDichDAO.layChiTietPhieu(maPhieu); 
        if (dsChiTiet != null) {
            for (ChiTietPhieuNhap ct : dsChiTiet) {
                PhuTung pt = phuTungDAO.layPhuTungTheoMa(ct.getMaPhuTung());
                String tenPT = pt != null ? pt.getTenPhuTung() : "Phụ tùng chưa xác định";
                double tongLine = (ct.getSoLuong() != null ? ct.getSoLuong() : 0) * (ct.getDonGia() != null ? ct.getDonGia() : 0);
                modelChiTiet.addRow(new Object[]{
                    ct.getMaPhuTung(),
                    tenPT,
                    ct.getSoLuong(),
                    df.format(ct.getDonGia() != null ? ct.getDonGia() : 0),
                    df.format(tongLine)
                });
            }
        }
        capNhatTongTien();
    }

    private void thucHienLocTimKiem() {
        String maP = txtTimMaPhieu.getText().trim().toLowerCase();
        String maNV = txtTimMaNV.getText().trim().toLowerCase();
        String phuTungText = txtTimPhuTung.getText().trim().toLowerCase();

        modelLichSu.setRowCount(0);
        List<PhieuNhapXuatKho> dsPhieu = giaoDichDAO.layTatCaPhieu();
        if (dsPhieu != null) {
            int count = 0;
            for (PhieuNhapXuatKho p : dsPhieu) {
                boolean matchMaP = maP.isEmpty() || p.getMaPhieuNhapXuat().toLowerCase().contains(maP);
                boolean matchMaNV = maNV.isEmpty() || (p.getMaNV() != null && p.getMaNV().toLowerCase().contains(maNV));
                
                boolean matchPT = phuTungText.isEmpty();
                if (!phuTungText.isEmpty()) {
                    List<ChiTietPhieuNhap> dsChiTiet = giaoDichDAO.layChiTietPhieu(p.getMaPhieuNhapXuat());
                    if (dsChiTiet != null) {
                        for (ChiTietPhieuNhap ct : dsChiTiet) {
                            PhuTung pt = phuTungDAO.layPhuTungTheoMa(ct.getMaPhuTung());
                            if (pt != null && pt.getTenPhuTung() != null && pt.getTenPhuTung().toLowerCase().contains(phuTungText)) {
                                matchPT = true; 
                                break;
                            }
                        }
                    }
                }

                if (matchMaP && matchMaNV && matchPT) {
                    double tongTien = giaoDichDAO.tinhTongTienPhieu(p.getMaPhieuNhapXuat());
                    String ngayStr = p.getNgayLapPhieu() != null 
                            ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(p.getNgayLapPhieu()) 
                            : "";
                    
                    modelLichSu.addRow(new Object[]{
                        p.getMaPhieuNhapXuat(),
                        p.getMaNV(),
                        ngayStr,
                        p.getKieuPhieu(),
                        p.getKieuPhieu() != null && p.getKieuPhieu().equalsIgnoreCase("Nhập") 
                                ? "Nhập phụ tùng kho" 
                                : "Sửa chữa bảo dưỡng xe",
                        df.format(tongTien)
                    });
                    count++;
                }
            }
            lblSoLuongPhieu.setText("Tìm thấy " + count + " phiếu giao dịch");
        }
    }

    private void thucHienLuuGiaoDich(boolean isNhap) {
        if (taiKhoanHienTai == null || taiKhoanHienTai.getMaNV() == null || taiKhoanHienTai.getMaNV().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiên đăng nhập hiện tại! Vui lòng đăng nhập lại.", "Lỗi phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (modelChiTiet.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Phiếu giao dịch chưa có hàng hóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PhieuNhapXuatKho phieu = new PhieuNhapXuatKho();
        final String maMoi;
        try {
            maMoi = giaoDichDAO.taoMaPhieuMoi(isNhap);
        } catch (java.sql.SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể phát sinh mã phiếu kho: " + exception.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        phieu.setMaPhieuNhapXuat(maMoi);
        txtMaPhieu.setText(maMoi);
        phieu.setMaNV(taiKhoanHienTai.getMaNV().trim());
        phieu.setNgayLapPhieu(new java.sql.Timestamp(System.currentTimeMillis()));
        // Giá trị kieuPhieu khớp đúng độ dài và từ khóa trong DB ("Nhập" hoặc "Xuất")
        phieu.setKieuPhieu(isNhap ? "Nhập" : "Xuất");

        List<ChiTietPhieuNhap> dsChiTiet = new ArrayList<>();
        String ghiChuChung = txtGhiChu.getText().trim(); 

        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
            ct.setMaPhieuNhapXuat(maMoi);
            ct.setMaPhuTung(modelChiTiet.getValueAt(i, 0).toString());
            ct.setSoLuong(Integer.parseInt(modelChiTiet.getValueAt(i, 2).toString()));
            ct.setDonGia(Double.parseDouble(modelChiTiet.getValueAt(i, 3).toString().replace(" đ", "").replace(",", "")));
            ct.setGhiChu(ghiChuChung); 
            dsChiTiet.add(ct);
        }

        boolean thanhCong = giaoDichDAO.luuGiaoDichKho(phieu, dsChiTiet, isNhap);
        if (thanhCong) {
            JOptionPane.showMessageDialog(this, "Lưu giao dịch thành công!\nMã phiếu: " + maMoi, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            switchMode(isNhap);
            taiDanhSachPhieuLichSu();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi ghi dữ liệu xuống Database! Kiểm tra lại ràng buộc khóa ngoại hoặc kết nối.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatTongTien() {
        double tongTien = 0;
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            tongTien += Double.parseDouble(modelChiTiet.getValueAt(i, 4).toString().replace(" đ", "").replace(",", ""));
        }
        lblTongTien.setText("Tổng tiền: " + df.format(tongTien));
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
}
package gui;

import dao.PhieuNhapXuatKhoDAO;
import dao.PhuTungDAO;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhapXuatKho;
import entity.PhuTung;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
public class FrmNhapXuatPhuTung extends JPanel {

    // --- HỆ MÀU CHUẨN ---
    private static final Color COLOR_BG_MAIN = new Color(244, 247, 252);
    private static final Color COLOR_PANEL_BG = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);   
    private static final Color COLOR_WARNING = new Color(234, 88, 12);   
    private static final Color COLOR_DANGER = new Color(220, 38, 38);    
    private static final Color COLOR_SECONDARY = new Color(241, 245, 249); 
    private static final Color COLOR_BORDER = new Color(221, 231, 245);
    private static final Color COLOR_READONLY = new Color(248, 250, 252);

    private final PhuTungDAO phuTungDAO = new PhuTungDAO();
    private final PhieuNhapXuatKhoDAO giaoDichDAO = new PhieuNhapXuatKhoDAO();
    private final DecimalFormat df = new DecimalFormat("#,### đ");
    
    private TaiKhoan taiKhoanHienTai; // Lưu phiên đăng nhập
    private boolean isNhapKho = true;

    // COMPONENTS
    private JRadioButton radNhapKho, radXuatKho;
    private JLabel lblDonGia; 
    
    // Master
    private final JTextField txtMaPhieu = new JTextField();
    private final JTextField txtNgayLap = new JTextField();
    private final JTextField txtNguoiLap = new JTextField();
    private final JTextField txtGhiChu = new JTextField();

    // Detail
    private final JComboBox<String> cboPhuTung = new JComboBox<>();
    private final JTextField txtTonKho = new JTextField();
    private final JTextField txtSoLuong = new JTextField();
    private final JTextField txtDonGia = new JTextField();
    private JButton btnThemChiTiet, btnXoaChiTiet, btnLuuPhieuNhap, btnLuuPhieuXuat, btnLamMoi;

    // Table
    private DefaultTableModel modelChiTiet;
    private JTable tblChiTiet;
    private JLabel lblTongTien;

    // Bắt buộc khởi tạo Form với tài khoản đăng nhập để lưu DB
   // SỬA LẠI HÀM KHỞI TẠO NÀY TRONG FrmNhapXuatPhuTung.java
    public FrmNhapXuatPhuTung ( TaiKhoan taiKhoan) {
        // NẾU BÊN NGOÀI TRUYỀN NULL VÀO, TỰ ĐỘNG KHỞI TẠO TÀI KHOẢN GIẢ ĐỂ CHỐNG LỖI
        if (taiKhoan == null) {
            this.taiKhoanHienTai = new TaiKhoan();
            this.taiKhoanHienTai.setMaNV("NV001");          // Mã nhân viên mặc định trong SQL
            this.taiKhoanHienTai.setHoTenNhanVien("Nhân viên Kho"); // Tên hiển thị lên ô Người lập
        } else {
            this.taiKhoanHienTai = taiKhoan;
        }
        
        initComponents();
        setupActionEvents();
        
        radNhapKho.setSelected(true);
        switchMode(true); 
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(COLOR_BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. HEADER & RADIO BUTTONS
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_BG_MAIN);
        
        JPanel pnlTitle = new JPanel();
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        pnlTitle.setBackground(COLOR_BG_MAIN);
        JLabel lblTitle = new JLabel("QUẢN LÝ GIAO DỊCH KHO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblTitle.setForeground(new Color(32, 55, 92));
        pnlTitle.add(lblTitle); 

        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlRadio.setBackground(COLOR_BG_MAIN);
        JLabel lblLoaiGD = new JLabel("LOẠI GIAO DỊCH: ");
        lblLoaiGD.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        radNhapKho = new JRadioButton("PHIẾU NHẬP KHO");
        radNhapKho.setFont(new Font("Segoe UI", Font.BOLD, 13));
        radNhapKho.setBackground(COLOR_BG_MAIN);
        radNhapKho.setForeground(COLOR_PRIMARY);
        
        radXuatKho = new JRadioButton("PHIẾU XUẤT KHO");
        radXuatKho.setFont(new Font("Segoe UI", Font.BOLD, 13));
        radXuatKho.setBackground(COLOR_BG_MAIN);
        radXuatKho.setForeground(COLOR_WARNING);

        ButtonGroup grpGiaoDich = new ButtonGroup();
        grpGiaoDich.add(radNhapKho); grpGiaoDich.add(radXuatKho);
        
        pnlRadio.add(lblLoaiGD); pnlRadio.add(radNhapKho); pnlRadio.add(radXuatKho);

        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(pnlRadio, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. CENTER CONTENT
        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setBackground(COLOR_BG_MAIN);

        pnlCenter.add(createMasterPanel());
        pnlCenter.add(Box.createVerticalStrut(15));
        pnlCenter.add(createDetailPanel());
        pnlCenter.add(Box.createVerticalStrut(15));
        pnlCenter.add(createTablePanel());

        add(pnlCenter, BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createMasterPanel() {
        JPanel pnlMaster = new JPanel(new GridBagLayout());
        pnlMaster.setBackground(COLOR_PANEL_BG);
        pnlMaster.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), " Thông tin phiếu ",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(32, 55, 92)
        ));

        // Khóa các ô tự động
        txtMaPhieu.setEditable(false); txtMaPhieu.setBackground(COLOR_READONLY);
        txtNgayLap.setEditable(false); txtNgayLap.setBackground(COLOR_READONLY);
        txtNguoiLap.setEditable(false); txtNguoiLap.setBackground(COLOR_READONLY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.insets = new Insets(10, 15, 10, 20); addField(pnlMaster, "Mã phiếu", txtMaPhieu, 0, 0, gbc);
        gbc.insets = new Insets(10, 20, 10, 15); addField(pnlMaster, "Ngày lập", txtNgayLap, 2, 0, gbc);
        
        gbc.insets = new Insets(0, 15, 15, 20);  addField(pnlMaster, "Người lập", txtNguoiLap, 0, 1, gbc);
        gbc.insets = new Insets(0, 20, 15, 15);  addField(pnlMaster, "Ghi chú", txtGhiChu, 2, 1, gbc);

        return pnlMaster;
    }

    private JPanel createDetailPanel() {
        JPanel pnlDetail = new JPanel(new GridBagLayout());
        pnlDetail.setBackground(COLOR_PANEL_BG);
        pnlDetail.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), " Chi tiết hàng hóa ",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(32, 55, 92)
        ));

        txtTonKho.setEditable(false); txtTonKho.setBackground(COLOR_READONLY);
        txtTonKho.setFont(new Font("Segoe UI", Font.BOLD, 12)); txtTonKho.setForeground(COLOR_DANGER);
        
        txtDonGia.setEditable(false); txtDonGia.setBackground(COLOR_READONLY);
        txtDonGia.setFont(new Font("Segoe UI", Font.BOLD, 12)); txtDonGia.setForeground(new Color(21, 128, 61));

        lblDonGia = new JLabel("Đơn giá (VNĐ) *");
        lblDonGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnThemChiTiet = createButton("Thêm vào danh sách", COLOR_PRIMARY, Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.insets = new Insets(10, 15, 10, 20); addField(pnlDetail, "Phụ tùng *", cboPhuTung, 0, 0, gbc);
        gbc.insets = new Insets(10, 20, 10, 15); addField(pnlDetail, "Tồn kho hiện tại", txtTonKho, 2, 0, gbc);
        
        gbc.insets = new Insets(0, 15, 15, 20);  addField(pnlDetail, "Số lượng *", txtSoLuong, 0, 1, gbc);
        
        gbc.insets = new Insets(0, 20, 15, 15);
        gbc.gridwidth = 1; gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.1; pnlDetail.add(lblDonGia, gbc);
        gbc.gridx = 3; gbc.weightx = 0.4; pnlDetail.add(txtDonGia, gbc);
        
        gbc.insets = new Insets(0, 20, 15, 15);
        gbc.gridx = 3; gbc.gridy = 2; gbc.gridwidth = 1; pnlDetail.add(btnThemChiTiet, gbc);

        return pnlDetail;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_PANEL_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), " Bảng dữ liệu ",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(32, 55, 92)
        ));

        String[] columns = {"Mã PT", "Tên phụ tùng", "S.Lượng", "Đơn giá", "Thành tiền"};
        modelChiTiet = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(30);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tblChiTiet.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JTableHeader header = tblChiTiet.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(239, 245, 253));
        header.setPreferredSize(new Dimension(0, 35));

        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlAction.setBackground(COLOR_PANEL_BG);
        pnlAction.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnXoaChiTiet = createButton("Xóa dòng", COLOR_DANGER, Color.WHITE);
        btnXoaChiTiet.setEnabled(false);
        pnlAction.add(btnXoaChiTiet);

        panel.add(pnlAction, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG_MAIN);
        
        lblTongTien = new JLabel("Tổng tiền: 0 đ");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongTien.setForeground(COLOR_DANGER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setBackground(COLOR_BG_MAIN);
        
        btnLuuPhieuNhap = createButton("LƯU PHIẾU NHẬP", COLOR_PRIMARY, Color.WHITE);
        btnLuuPhieuXuat = createButton("LƯU PHIẾU XUẤT", COLOR_WARNING, Color.WHITE);
        btnLamMoi = createButton("Làm mới", COLOR_SECONDARY, new Color(47, 62, 84));

        pnlButtons.add(btnLuuPhieuNhap);
        pnlButtons.add(btnLuuPhieuXuat);
        pnlButtons.add(btnLamMoi);

        panel.add(lblTongTien, BorderLayout.WEST);
        panel.add(pnlButtons, BorderLayout.EAST);
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

        btnLuuPhieuNhap.setVisible(isNhap);
        btnLuuPhieuXuat.setVisible(!isNhap);

        napDuLieuChung();
    }

    private void napDuLieuChung() {
        txtMaPhieu.setText("Tự động tạo khi lưu");
        txtNgayLap.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        if (taiKhoanHienTai != null) {
            txtNguoiLap.setText(taiKhoanHienTai.getHoTenNhanVien());
        } else {
            txtNguoiLap.setText("—");
        }
        
        cboPhuTung.removeAllItems();
        cboPhuTung.addItem(""); 
        List<PhuTung> dsPT = phuTungDAO.layTatCaPhuTung();
        if (dsPT != null) {
            for (PhuTung pt : dsPT) {
                cboPhuTung.addItem(pt.getMaPhuTung() + " - " + pt.getTenPhuTung());
            }
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

        btnLamMoi.addActionListener(e -> switchMode(this.isNhapKho));

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

        btnLuuPhieuNhap.addActionListener(e -> thucHienLuuGiaoDich(true));
        btnLuuPhieuXuat.addActionListener(e -> thucHienLuuGiaoDich(false));
    }

    private void thucHienLuuGiaoDich(boolean isNhap) {
        if (modelChiTiet.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Phiếu giao dịch chưa có hàng hóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (taiKhoanHienTai == null || taiKhoanHienTai.getMaNV() == null) {
            JOptionPane.showMessageDialog(this, "Lỗi phiên đăng nhập. Vui lòng đăng nhập lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Ánh xạ Entity Master
        PhieuNhapXuatKho phieu = new PhieuNhapXuatKho();
        String maMoi = (isNhap ? "PN" : "PX") + System.currentTimeMillis();
        phieu.setMaPhieuNhapXuat(maMoi);
        phieu.setMaNV(taiKhoanHienTai.getMaNV());
        phieu.setNgayLapPhieu(new java.sql.Timestamp(System.currentTimeMillis()));
        phieu.setKieuPhieu(isNhap ? "Nhập kho" : "Xuất kho");

        // 2. Ánh xạ Entity Detail nguyên bản 
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

        // 3. Thực thi Database
        boolean thanhCong = giaoDichDAO.luuGiaoDichKho(phieu, dsChiTiet, isNhap);
        if (thanhCong) {
            JOptionPane.showMessageDialog(this, "Lưu giao dịch thành công!\nMã phiếu: " + maMoi, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            switchMode(isNhap);
            txtMaPhieu.setText(maMoi);
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi ghi dữ liệu xuống Database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatTongTien() {
        double tongTien = 0;
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            tongTien += Double.parseDouble(modelChiTiet.getValueAt(i, 4).toString().replace(" đ", "").replace(",", ""));
        }
        String prefix = isNhapKho ? "Tổng tiền phiếu nhập: " : "Tổng tiền phiếu xuất: ";
        lblTongTien.setText(prefix + df.format(tongTien));
    }

    private void addField(JPanel parent, String label, java.awt.Component field, int col, int row, GridBagConstraints gbc) {
        gbc.gridwidth = 1; gbc.gridx = col; gbc.gridy = row; gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        parent.add(lbl, gbc); gbc.gridx = col + 1; gbc.weightx = 0.4; parent.add(field, gbc);
    }
    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text); btn.setPreferredSize(new Dimension(150, 36)); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); btn.setBackground(bg); btn.setForeground(fg);
        btn.setFocusPainted(false); btn.setBorder(new EmptyBorder(0,0,0,0)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }
}
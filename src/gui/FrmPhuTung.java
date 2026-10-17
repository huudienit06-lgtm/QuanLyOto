package gui;

import dao.PhuTungDAO;
import entity.PhuTung;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Màn hình Quản lý Phụ Tùng
 * Chuẩn hóa giao diện theo FrmXe và tích hợp trực tiếp tầng DAO xử lý sự kiện
 */
public class FrmPhuTung extends JPanel {

    // Hệ màu chuẩn giao diện Gara
    private static final Color COLOR_BG_MAIN = new Color(244, 247, 252);
    private static final Color COLOR_PANEL_BG = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);   // Xanh dương (Thêm)
    private static final Color COLOR_SUCCESS = new Color(22, 163, 74);   // Xanh lá (Cập nhật)
    private static final Color COLOR_DANGER = new Color(220, 38, 38);    // Đỏ (Xóa)
    private static final Color COLOR_SECONDARY = new Color(241, 245, 249); // Xám (Làm mới)
    private static final Color COLOR_BORDER = new Color(221, 231, 245);

    // Khai báo DAO kết nối CSDL
    private final PhuTungDAO phuTungDAO = new PhuTungDAO();

    private JPanel pnlNorth, pnlSouth;

    // Các thành phần Form Thông tin
    private final JTextField txtMaPT = new JTextField();
    private final JTextField txtTenPT = new JTextField();
    private final JTextField txtGiaNhap = new JTextField();
    private final JTextField txtGiaBan = new JTextField();
    private final JTextField txtSoLuongTon = new JTextField();
    
    private final JComboBox<String> cboDonViTinh = new JComboBox<>(new String[]{"", "Cái", "Bộ", "Hộp", "Chiếc", "Lít", "Can"});
    private final JComboBox<String> cboNhaCungCap = new JComboBox<>();
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{"Còn hàng", "Hết hàng", "Ngừng kinh doanh"});
    
    private JButton btnThem, btnCapNhat, btnXoa, btnLamMoi;

    // Các thành phần Tìm kiếm & Bảng
    private final JTextField txtTimMa = new JTextField();
    private final JTextField txtTimTen = new JTextField();
    private final JComboBox<String> cboTimNCC = new JComboBox<>();
    private JButton btnTimKiem, btnTaiLai;
    
    private DefaultTableModel modelPhuTung;
    private JTable tblPhuTung;
    private JLabel lblTrangThaiTai;

    public FrmPhuTung() {
        initComponents();
        setupViewLogic(); 
        setupActionEvents();
        
        // Nạp dữ liệu ban đầu khi mở form
        napDanhSachNhaCungCap();
        napDanhSachPhuTung();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(COLOR_BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Tiêu đề Header chung
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBackground(COLOR_BG_MAIN);
        
        JLabel lblTitle = new JLabel("QUẢN LÝ PHỤ TÙNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblTitle.setForeground(new Color(32, 55, 92));
        
        JLabel lblSubtitle = new JLabel("Thông tin phụ tùng và danh mục quản lý kho");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(112, 128, 150));
        lblSubtitle.setBorder(new EmptyBorder(5, 0, 10, 0));

        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSubtitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Chứa 2 vùng chính
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 15));
        pnlCenter.setBackground(COLOR_BG_MAIN);
        
        pnlNorth = createFormPanel();
        pnlSouth = createTablePanel();

        pnlCenter.add(pnlNorth, BorderLayout.NORTH);
        pnlCenter.add(pnlSouth, BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);
    }
    // 1. FORM NHẬP LIỆU
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PANEL_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            " Thông tin phụ tùng ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(32, 55, 92)
        ));

        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setBackground(COLOR_PANEL_BG);
        pnlFields.setBorder(new EmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 20);
        
        txtSoLuongTon.setEditable(false); // Khóa số lượng tồn không cho sửa tay
        txtSoLuongTon.setBackground(new Color(248, 250, 252));

        // Hàng 1
        addFieldToGrid(pnlFields, "Mã phụ tùng *", txtMaPT, 0, 0, gbc);
        gbc.insets = new Insets(0, 20, 12, 0);
        addFieldToGrid(pnlFields, "Tên phụ tùng *", txtTenPT, 2, 0, gbc);

        // Hàng 2
        gbc.insets = new Insets(0, 0, 12, 20);
        addFieldToGrid(pnlFields, "Đơn vị tính", cboDonViTinh, 0, 1, gbc);
        gbc.insets = new Insets(0, 20, 12, 0);
        addFieldToGrid(pnlFields, "Nhà cung cấp", cboNhaCungCap, 2, 1, gbc);

        // Hàng 3
        gbc.insets = new Insets(0, 0, 12, 20);
        addFieldToGrid(pnlFields, "Giá nhập (VNĐ) *", txtGiaNhap, 0, 2, gbc);
        gbc.insets = new Insets(0, 20, 12, 0);
        addFieldToGrid(pnlFields, "Giá bán (VNĐ) *", txtGiaBan, 2, 2, gbc);

        // Hàng 4
        gbc.insets = new Insets(0, 0, 12, 20);
        addFieldToGrid(pnlFields, "Số lượng tồn", txtSoLuongTon, 0, 3, gbc);
        gbc.insets = new Insets(0, 20, 12, 0);
        addFieldToGrid(pnlFields, "Trạng thái", cboTrangThai, 2, 3, gbc);
        cboNhaCungCap.setEditable(true);
        panel.add(pnlFields, BorderLayout.CENTER);

        // Vùng Nút Bấm
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlButtons.setBackground(COLOR_PANEL_BG);
        pnlButtons.setBorder(new EmptyBorder(0, 15, 10, 15));

        btnThem = createButton("Thêm phụ tùng", COLOR_PRIMARY, Color.WHITE);
        btnCapNhat = createButton("Cập nhật", COLOR_SUCCESS, Color.WHITE);
        btnXoa = createButton("Xóa phụ tùng", COLOR_DANGER, Color.WHITE);
        btnLamMoi = createButton("Làm mới", COLOR_SECONDARY, new Color(47, 62, 84));

        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);

        pnlButtons.add(btnThem);
        pnlButtons.add(btnCapNhat);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);

        panel.add(pnlButtons, BorderLayout.SOUTH);
        return panel;
    }
    // 2. BẢNG DANH SÁCH & TÌM KIẾM
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PANEL_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            " Danh sách phụ tùng ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(32, 55, 92)
        ));

        // Thanh Tìm Kiếm
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlSearch.setBackground(COLOR_PANEL_BG);
        
        txtTimMa.setPreferredSize(new Dimension(120, 30));
        txtTimTen.setPreferredSize(new Dimension(150, 30));
        cboTimNCC.setPreferredSize(new Dimension(150, 30));
        cboTimNCC.setBackground(Color.WHITE);

        btnTimKiem = createButton("Tìm kiếm", COLOR_PRIMARY, Color.WHITE);
        btnTimKiem.setPreferredSize(new Dimension(100, 30));
        
        btnTaiLai = createButton("Tải lại", COLOR_SECONDARY, new Color(47, 62, 84));
        btnTaiLai.setPreferredSize(new Dimension(80, 30));

        pnlSearch.add(new JLabel("Mã PT:"));
        pnlSearch.add(txtTimMa);
        pnlSearch.add(new JLabel("Tên:"));
        pnlSearch.add(txtTimTen);
        pnlSearch.add(new JLabel("NCC:"));
        pnlSearch.add(cboTimNCC);
        pnlSearch.add(btnTimKiem);
        pnlSearch.add(btnTaiLai);

        panel.add(pnlSearch, BorderLayout.NORTH);

        // Khởi tạo Bảng Dữ Liệu
        String[] columns = {"Mã PT", "Tên phụ tùng", "Đơn vị", "Giá nhập", "Giá bán", "Tồn kho", "Nhà cung cấp", "Trạng thái"};
        modelPhuTung = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblPhuTung = new JTable(modelPhuTung);
        tblPhuTung.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhuTung.setRowHeight(30); 
        tblPhuTung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblPhuTung.setGridColor(new Color(232, 238, 247));
        tblPhuTung.setSelectionBackground(new Color(225, 239, 255));
        tblPhuTung.setSelectionForeground(new Color(47, 62, 84));

        JTableHeader header = tblPhuTung.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(239, 245, 253));
        header.setPreferredSize(new Dimension(0, 35));

        JScrollPane scrollPane = new JScrollPane(tblPhuTung);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(COLOR_PANEL_BG);
        
        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        pnlTableWrapper.setBackground(COLOR_PANEL_BG);
        pnlTableWrapper.setBorder(new EmptyBorder(0, 15, 10, 15));
        pnlTableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(pnlTableWrapper, BorderLayout.CENTER);

        // Trạng thái cuối bảng
        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlStatus.setBackground(COLOR_PANEL_BG);
        lblTrangThaiTai = new JLabel("Đã tải 0 phụ tùng");
        lblTrangThaiTai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTrangThaiTai.setForeground(new Color(112, 128, 150));
        pnlStatus.add(lblTrangThaiTai);
        panel.add(pnlStatus, BorderLayout.SOUTH);

        return panel;
    }
    // 3. LOGIC XỬ LÝ SỰ KIỆN VÀ DAO BINDING
    private void setupViewLogic() {
        // Đổ dữ liệu từ Bảng lên Form khi click chọn dòng
        tblPhuTung.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblPhuTung.getSelectedRow();
                if (row >= 0) {
                    txtMaPT.setText(modelPhuTung.getValueAt(row, 0).toString());
                    txtTenPT.setText(modelPhuTung.getValueAt(row, 1).toString());
                    cboDonViTinh.setSelectedItem(modelPhuTung.getValueAt(row, 2).toString());
                    String giaNhapSach = modelPhuTung.getValueAt(row, 3).toString().replace(" đ", "").replace(",", "").trim();
                    String giaBanSach = modelPhuTung.getValueAt(row, 4).toString().replace(" đ", "").replace(",", "").trim();
                    txtSoLuongTon.setText(modelPhuTung.getValueAt(row, 5).toString());
                    cboNhaCungCap.setSelectedItem(modelPhuTung.getValueAt(row, 6).toString());
                    cboTrangThai.setSelectedItem(modelPhuTung.getValueAt(row, 7).toString());
                    
                    txtGiaNhap.setText(giaNhapSach);
                    txtGiaBan.setText(giaBanSach);
                    btnThem.setEnabled(false);
                    btnCapNhat.setEnabled(true);
                    btnXoa.setEnabled(true);
                    txtMaPT.setEditable(false); // Khóa mã không cho sửa khi đang chọn
                }
            }
        });
    }

    private void setupActionEvents() {
        // 1. Nút Thêm Phụ Tùng
        btnThem.addActionListener(e -> {
            if (kiemTraDuLieu()) {
                PhuTung pt = taoPhuTungForm();
                if (phuTungDAO.themPhuTung(pt)) {
                    JOptionPane.showMessageDialog(this, "Thêm phụ tùng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    napDanhSachPhuTung();
                    lamMoiForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại! Trùng mã phụ tùng.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 2. Nút Cập Nhật Phụ Tùng
        btnCapNhat.addActionListener(e -> {
            if (kiemTraDuLieu()) {
                PhuTung pt = taoPhuTungForm();
                if (phuTungDAO.capNhatPhuTung(pt)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật phụ tùng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    napDanhSachPhuTung();
                    lamMoiForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 3. Nút Xóa Phụ Tùng (Có Popup xác nhận)
        btnXoa.addActionListener(e -> {
            int row = tblPhuTung.getSelectedRow();
            if (row >= 0) {
                String maPT = txtMaPT.getText().trim();
                int confirm = JOptionPane.showConfirmDialog(
                    this, 
                    "Bạn có chắc chắn muốn xóa phụ tùng có mã: " + maPT + " không?", 
                    "Xác nhận xóa", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (phuTungDAO.xoaPhuTung(maPT)) {
                        JOptionPane.showMessageDialog(this, "Xóa phụ tùng thành công!");
                        napDanhSachPhuTung();
                        lamMoiForm();
                    } else {
                        JOptionPane.showMessageDialog(this, "Không thể xóa vì phụ tùng này đang được sử dụng trong giao dịch!", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // 4. Nút Làm Mới Form
        btnLamMoi.addActionListener(e -> {
            tblPhuTung.clearSelection();
            lamMoiForm();
        });

        // 5. Nút Tải Lại Danh Sách
        btnTaiLai.addActionListener(e -> {
            txtTimMa.setText("");
            txtTimTen.setText("");
            if(cboTimNCC.getItemCount() > 0) cboTimNCC.setSelectedIndex(0);
            napDanhSachPhuTung();
        });

        // 6. Nút Tìm Kiếm 3 tiêu chuẩn
        btnTimKiem.addActionListener(e -> {
            String maTim = txtTimMa.getText().trim().toLowerCase();
            String tenTim = txtTimTen.getText().trim().toLowerCase();
            String nccTim = cboTimNCC.getSelectedItem() != null ? cboTimNCC.getSelectedItem().toString() : "";

            List<PhuTung> list = phuTungDAO.layTatCaPhuTung();
            modelPhuTung.setRowCount(0);
            int count = 0;

            for (PhuTung pt : list) {
                boolean matchMa = maTim.isEmpty() || pt.getMaPhuTung().toLowerCase().contains(maTim);
                boolean matchTen = tenTim.isEmpty() || pt.getTenPhuTung().toLowerCase().contains(tenTim);
                boolean matchNCC = nccTim.equals("Tất cả NCC") || nccTim.isEmpty() || pt.getNhaCungCap().equalsIgnoreCase(nccTim);

                if (matchMa && matchTen && matchNCC) {
                    Object[] row = {
                        pt.getMaPhuTung(),
                        pt.getTenPhuTung(),
                        pt.getDonViTinh(),
                        String.format("%,.0f đ", pt.getGiaNhap()),
                        String.format("%,.0f đ", pt.getGiaBan()),
                        pt.getSoLuongTon(),
                        pt.getNhaCungCap(),
                        pt.getTrangThai()
                    };
                    modelPhuTung.addRow(row);
                    count++;
                }
            }
            lblTrangThaiTai.setText("Tìm thấy " + count + " phụ tùng thỏa mãn");
        });
    }
    // 4. CÁC HÀM TIỆN ÍCH, VALIDATION & NẠP DỮ LIỆU TỪ DAO
    private void napDanhSachPhuTung() {
        List<PhuTung> list = phuTungDAO.layTatCaPhuTung();
        modelPhuTung.setRowCount(0);
        for (PhuTung pt : list) {
            Object[] row = {
                pt.getMaPhuTung(),
                pt.getTenPhuTung(),
                pt.getDonViTinh(),
                String.format("%,.0f đ", pt.getGiaNhap()),
                String.format("%,.0f đ", pt.getGiaBan()),
                pt.getSoLuongTon(),
                pt.getNhaCungCap(),
                pt.getTrangThai()
            };
            modelPhuTung.addRow(row);
        }
        lblTrangThaiTai.setText("Đã tải " + list.size() + " phụ tùng");
    }

    private void napDanhSachNhaCungCap() {
        List<String> listNCC = phuTungDAO.layDanhSachNhaCungCap();
        cboNhaCungCap.removeAllItems();
        cboTimNCC.removeAllItems();
        
        cboNhaCungCap.addItem("Chưa có nhà cung cấp");
        cboTimNCC.addItem("Tất cả NCC");
        
        for (String ncc : listNCC) {
          if (ncc != null && !ncc.trim().isEmpty() 
                && !ncc.equalsIgnoreCase("Chưa có nhà cung cấp") 
                && !ncc.equalsIgnoreCase("Tất cả NCC")) {
                
                cboNhaCungCap.addItem(ncc);
                cboTimNCC.addItem(ncc);
                }
            }
    }

    private PhuTung taoPhuTungForm() {
        double giaNhap = Double.parseDouble(txtGiaNhap.getText().trim().replace(",", "").replace(" đ", ""));
        double giaBan = Double.parseDouble(txtGiaBan.getText().trim().replace(",", "").replace(" đ", ""));
        int ton = txtSoLuongTon.getText().isEmpty() ? 0 : Integer.parseInt(txtSoLuongTon.getText().trim());
        
        return new PhuTung(
            txtMaPT.getText().trim(),
            txtTenPT.getText().trim(),
            cboDonViTinh.getSelectedItem() != null ? cboDonViTinh.getSelectedItem().toString() : "",
            giaNhap,
            giaBan,
            ton,
            cboNhaCungCap.getSelectedItem() != null ? cboNhaCungCap.getSelectedItem().toString() : "",
            cboTrangThai.getSelectedItem().toString()
        );
    }

    private void lamMoiForm() {
        txtMaPT.setText("");
        txtMaPT.setEditable(true);
        txtTenPT.setText("");
        cboDonViTinh.setSelectedIndex(0);
        if(cboNhaCungCap.getItemCount() > 0) cboNhaCungCap.setSelectedIndex(0);
        txtGiaNhap.setText("");
        txtGiaBan.setText("");
        txtSoLuongTon.setText("");
        cboTrangThai.setSelectedIndex(0);

        btnThem.setEnabled(true);
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);
    }

    /**
     * Hàm kiểm tra tính hợp lệ dữ liệu (Data Validation)
     */
    private boolean kiemTraDuLieu() {
        if (txtMaPT.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã phụ tùng!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            txtMaPT.requestFocus();
            return false;
        }
        
        if (txtTenPT.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên phụ tùng!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            txtTenPT.requestFocus();
            return false;
        }

        String maPT = txtMaPT.getText().trim();
        if (!maPT.matches("^[a-zA-Z0-9]{3,10}$")) {
            JOptionPane.showMessageDialog(this, "Mã phụ tùng chỉ chứa chữ và số (3-10 ký tự), không có khoảng trắng!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            txtMaPT.selectAll();
            txtMaPT.requestFocus();
            return false;
        }

        double giaNhap = 0;
        double giaBan = 0;

        try {
            giaNhap = Double.parseDouble(txtGiaNhap.getText().trim().replace(",", ""));
            if (giaNhap <= 0) {
                JOptionPane.showMessageDialog(this, "Giá nhập phải lớn hơn 0!", "Lỗi giá trị", JOptionPane.WARNING_MESSAGE);
                txtGiaNhap.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá nhập phải là một con số hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            txtGiaNhap.requestFocus();
            return false;
        }

        try {
            giaBan = Double.parseDouble(txtGiaBan.getText().trim().replace(",", ""));
            if (giaBan <= 0) {
                JOptionPane.showMessageDialog(this, "Giá bán phải lớn hơn 0!", "Lỗi giá trị", JOptionPane.WARNING_MESSAGE);
                txtGiaBan.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá bán phải là một con số hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            txtGiaBan.requestFocus();
            return false;
        }

        if (giaBan < giaNhap) {
            JOptionPane.showMessageDialog(this, "Giá bán không được thấp hơn Giá nhập để tránh lỗ vốn!", "Cảnh báo nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            txtGiaBan.requestFocus();
            return false;
        }

        return true;
    }

    private void addFieldToGrid(JPanel parent, String label, java.awt.Component field, int col, int row, GridBagConstraints gbc) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        parent.add(lbl, gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 0.4;
        parent.add(field, gbc);
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(110, 35)); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(0,0,0,0)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
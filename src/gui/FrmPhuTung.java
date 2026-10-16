package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Màn hình Quản lý Phụ Tùng (Giao diện thuần - Pure View)
 * Đã chuẩn bị sẵn các hàm Getter/Setter để kết nối với lớp Sự kiện (Controller)
 */
public class FrmPhuTung extends JPanel {

    // Hệ màu theo Figma
    private static final Color COLOR_BG = new Color(248, 250, 252);
    private static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color COLOR_BTN_THEM = new Color(16, 185, 129);
    private static final Color COLOR_BTN_SUA = new Color(245, 158, 11);
    private static final Color COLOR_BTN_XOA = new Color(239, 68, 68);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);

    // Khai báo Component (Private để đóng gói)
    private JPanel pnlHeader, pnlTableContainer, pnlPagination;
    private JLabel lblTieuDe, lblMoTa, lblDaChon, lblKhoChinh;
    private JLabel lblDanhSachPhuTung, lblHuyHieu, lblCanhBaoTon, lblHienThi;
    
    private JButton btnThemMoi, btnCapNhat, btnXoa, btnLapPhieu, btnTimKiem;
    private JButton btnPrev, btnPage1, btnPage2, btnPage3, btnNext;
    
    private JComboBox<String> cboNhom, cboTrangThai, cboSoDong;
    
    private JTable tblPhuTung;
    private DefaultTableModel modelPhuTung;
    private JScrollPane scrPhuTung;

    public FrmPhuTung() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(24, 32, 24, 32));

        // ==========================================
        // 1. PHẦN HEADER VÀ ACTIONS
        // ==========================================
        pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBackground(Color.WHITE);

        lblTieuDe = new JLabel("Quản Lý Phụ Tùng");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTieuDe.setForeground(COLOR_TEXT_PRIMARY);
        
        lblMoTa = new JLabel("Quản lý danh mục phụ tùng, theo dõi tồn kho và chủ động kế hoạch nhập hàng cho gara.");
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMoTa.setForeground(COLOR_TEXT_SECONDARY);
        lblMoTa.setBorder(new EmptyBorder(5, 0, 20, 0));

        pnlHeader.add(lblTieuDe);
        pnlHeader.add(lblMoTa);

        JPanel pnlAction1 = new JPanel(new BorderLayout());
        pnlAction1.setBackground(Color.WHITE);
        
        JPanel pnlButtons1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlButtons1.setBackground(Color.WHITE);
        
        btnThemMoi = createSolidButton("+ Thêm mới", COLOR_BTN_THEM, Color.WHITE);
        btnCapNhat = createSolidButton("Cập nhật", COLOR_BTN_SUA, Color.WHITE);
        btnXoa = createSolidButton("Xóa", COLOR_BTN_XOA, Color.WHITE);
        
        // Trạng thái mặc định ban đầu
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);
        
        lblDaChon = new JLabel("Đã chọn 0 phụ tùng");
        lblDaChon.setForeground(COLOR_TEXT_SECONDARY);
        lblDaChon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDaChon.setBorder(new EmptyBorder(0, 10, 0, 0));

        pnlButtons1.add(btnThemMoi);
        pnlButtons1.add(btnCapNhat);
        pnlButtons1.add(btnXoa);
        pnlButtons1.add(lblDaChon);

        lblKhoChinh = new JLabel("Kho chính • AUTOGARA");
        lblKhoChinh.setForeground(COLOR_TEXT_SECONDARY);
        lblKhoChinh.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        pnlAction1.add(pnlButtons1, BorderLayout.WEST);
        pnlAction1.add(lblKhoChinh, BorderLayout.EAST);

        JPanel pnlAction2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        pnlAction2.setBackground(Color.WHITE);
        
        btnLapPhieu = createOutlineButton("Chuyển đến Lập Phiếu Nhập/Xuất ↗");
        btnTimKiem = createOutlineButton("Tìm kiếm Phụ Tùng ↗");
        
        pnlAction2.add(btnLapPhieu);
        pnlAction2.add(btnTimKiem);

        pnlHeader.add(pnlAction1);
        pnlHeader.add(pnlAction2);

        add(pnlHeader, BorderLayout.NORTH);

        // ==========================================
        // 2. PHẦN BẢNG DỮ LIỆU
        // ==========================================
        pnlTableContainer = new JPanel(new BorderLayout());
        pnlTableContainer.setBackground(Color.WHITE);
        pnlTableContainer.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 0, 0, 0),
            BorderFactory.createLineBorder(COLOR_BORDER, 1)
        ));

        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.setBackground(Color.WHITE);
        pnlTableHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlTableTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTableTitle.setBackground(Color.WHITE);
        lblDanhSachPhuTung = new JLabel("Danh sách phụ tùng");
        lblDanhSachPhuTung.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        // Huy hiệu đếm tổng số lượng ban đầu là 0
        lblHuyHieu = new JLabel("<html><span style='background-color:#E0E7FF; color:#4338CA; padding:3px 8px; border-radius:10px;'>0 phụ tùng</span></html>");
        
        pnlTableTitle.add(lblDanhSachPhuTung);
        pnlTableTitle.add(lblHuyHieu);

        JPanel pnlTableFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlTableFilters.setBackground(Color.WHITE);
        lblCanhBaoTon = new JLabel("<html><font color='red'>●</font> Tồn dưới 5: cần nhập thêm</html>");
        lblCanhBaoTon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        cboNhom = new JComboBox<>(new String[]{"Tất cả nhóm", "Động cơ", "Hệ thống phanh", "Điện ô tô"});
        cboTrangThai = new JComboBox<>(new String[]{"Tất cả trạng thái", "Còn hàng", "Sắp hết", "Hết hàng"});
        
        pnlTableFilters.add(lblCanhBaoTon);
        pnlTableFilters.add(cboNhom);
        pnlTableFilters.add(cboTrangThai);

        pnlTableHeader.add(pnlTableTitle, BorderLayout.WEST);
        pnlTableHeader.add(pnlTableFilters, BorderLayout.EAST);

        String[] columnNames = {"", "Mã PT", "Tên phụ tùng", "Nhóm", "Số lượng tồn", "Đơn giá (VNĐ)", "Trạng thái", "Hành động"};
        modelPhuTung = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; 
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; 
            }
        };

        tblPhuTung = new JTable(modelPhuTung);
        tblPhuTung.setRowHeight(45);
        tblPhuTung.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblPhuTung.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhuTung.setShowVerticalLines(false);
        tblPhuTung.setGridColor(COLOR_BORDER);
        
        tblPhuTung.getColumnModel().getColumn(0).setMaxWidth(40);
        tblPhuTung.getColumnModel().getColumn(7).setMaxWidth(80);

        JTableHeader header = tblPhuTung.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_BG);
        header.setForeground(COLOR_TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDER));

        scrPhuTung = new JScrollPane(tblPhuTung);
        scrPhuTung.setBorder(BorderFactory.createEmptyBorder());
        scrPhuTung.getViewport().setBackground(Color.WHITE);

        pnlTableContainer.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableContainer.add(scrPhuTung, BorderLayout.CENTER);

        // ==========================================
        // 3. PHẦN PHÂN TRANG
        // ==========================================
        pnlPagination = new JPanel(new BorderLayout());
        pnlPagination.setBackground(Color.WHITE);
        pnlPagination.setBorder(new EmptyBorder(15, 20, 15, 20));

        lblHienThi = new JLabel("Chưa có dữ liệu phụ tùng");
        lblHienThi.setForeground(COLOR_TEXT_SECONDARY);

        JPanel pnlPageControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlPageControls.setBackground(Color.WHITE);
        
        pnlPageControls.add(new JLabel("Số dòng: "));
        cboSoDong = new JComboBox<>(new String[]{"10", "20", "50"});
        pnlPageControls.add(cboSoDong);
        
        btnPrev = createPageButton("<");
        btnPage1 = createSolidButton("1", new Color(37, 99, 235), Color.WHITE);
        btnPage2 = createPageButton("2");
        btnPage3 = createPageButton("3");
        btnNext = createPageButton(">");
        
        pnlPageControls.add(btnPrev);
        pnlPageControls.add(btnPage1);
        pnlPageControls.add(btnPage2);
        pnlPageControls.add(btnPage3);
        pnlPageControls.add(new JLabel("..."));
        pnlPageControls.add(createPageButton("9"));
        pnlPageControls.add(btnNext);

        pnlPagination.add(lblHienThi, BorderLayout.WEST);
        pnlPagination.add(pnlPageControls, BorderLayout.EAST);
        
        pnlTableContainer.add(pnlPagination, BorderLayout.SOUTH);

        add(pnlTableContainer, BorderLayout.CENTER);
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH TẠO UI
    // ==========================================
    private JButton createSolidButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(37, 99, 235));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPageButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            new EmptyBorder(5, 10, 5, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ==========================================
    // CÁC HÀM CUNG CẤP CHO FILE SỰ KIỆN (CONTROLLER)
    // ==========================================
    
    /** Cập nhật UI hiển thị tổng số dòng trong cơ sở dữ liệu */
    public void capNhatHienThiTongSo(int tongSo) {
        lblHuyHieu.setText("<html><span style='background-color:#E0E7FF; color:#4338CA; padding:3px 8px; border-radius:10px;'>" + tongSo + " phụ tùng</span></html>");
        if (tongSo == 0) {
            lblHienThi.setText("Chưa có dữ liệu phụ tùng");
        } else {
            lblHienThi.setText("Hiển thị tổng số " + tongSo + " phụ tùng");
        }
    }

    /** Cập nhật UI hiển thị số checkbox đang được tích chọn */
    public void capNhatHienThiSoLuongChon(int soLuong) {
        lblDaChon.setText("Đã chọn " + soLuong + " phụ tùng");
        // Tự động bật/tắt nút Cập nhật và Xóa
        btnCapNhat.setEnabled(soLuong > 0);
        btnXoa.setEnabled(soLuong > 0);
    }

    // Getters để lớp Controller lấy các đối tượng và gắn Sự kiện (Event Listener)
    public JButton getBtnThemMoi() { return btnThemMoi; }
    public JButton getBtnCapNhat() { return btnCapNhat; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnLapPhieu() { return btnLapPhieu; }
    public JButton getBtnTimKiem() { return btnTimKiem; }
    
    public JComboBox<String> getCboNhom() { return cboNhom; }
    public JComboBox<String> getCboTrangThai() { return cboTrangThai; }
    
    public JTable getTblPhuTung() { return tblPhuTung; }
    public DefaultTableModel getModelPhuTung() { return modelPhuTung; }
}
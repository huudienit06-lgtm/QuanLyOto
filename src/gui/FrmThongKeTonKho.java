package gui;

import dao.PhuTungDAO;
import entity.PhuTung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
public class FrmThongKeTonKho extends JPanel {

    private static final Color COLOR_BG_MAIN = new Color(244, 247, 252);
    private static final Color COLOR_PANEL_BG = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);   
    private static final Color COLOR_BORDER = new Color(221, 231, 245);
    // Màu theo thiết kế mới (Đỏ <5, Cam 5-10, Xanh >10)
    private static final Color COLOR_DANGER = new Color(220, 38, 38);      
    private static final Color COLOR_WARNING = new Color(217, 119, 6);     
    private static final Color COLOR_SUCCESS = new Color(22, 163, 74);     
    private final PhuTungDAO phuTungDAO = new PhuTungDAO();

    // COMPONENTS
    private JComboBox<String> cboLocPhuTung;
    private JButton btnLoc;
    
    private JLabel lblTieuDeBang, lblPhuDeBang, lblCanNhapThem;
    private DefaultTableModel modelTonKho;
    private JTable tblTonKho;
    
    private JLabel lblFooterLeft, lblFooterRight;

    public FrmThongKeTonKho() {
        initComponents();
        setupActionEvents();
        napDuLieuBanDau();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(COLOR_BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        // 1. HEADER
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_BG_MAIN);
        pnlHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_PRIMARY)); 

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        pnlTitle.setBackground(COLOR_BG_MAIN);
        JLabel lblTitle = new JLabel("Phụ tùng tồn kho");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_PRIMARY);
        pnlTitle.add(lblTitle);

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlFilter.setBackground(COLOR_BG_MAIN);
        
        // Cập nhật bộ lọc theo logic 5-10
        cboLocPhuTung = new JComboBox<>(new String[]{"Tất cả phụ tùng", "Sắp hết (< 5)", "Tồn thấp (5 - 10)", "Đủ tồn (> 10)"});
        cboLocPhuTung.setPreferredSize(new Dimension(200, 32));
        
        btnLoc = new JButton("Lọc");
        btnLoc.setPreferredSize(new Dimension(80, 32));
        btnLoc.setBackground(COLOR_PRIMARY);
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoc.setFocusPainted(false);
        btnLoc.setBorder(BorderFactory.createEmptyBorder());
        btnLoc.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlFilter.add(cboLocPhuTung);
        pnlFilter.add(btnLoc);

        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(pnlFilter, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);
        // 2. CONTENT
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 15));
        pnlCenter.setBackground(COLOR_PANEL_BG);
        pnlCenter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            new EmptyBorder(15, 15, 15, 15)
        ));
        JPanel pnlTableTop = new JPanel();
        pnlTableTop.setLayout(new BoxLayout(pnlTableTop, BoxLayout.Y_AXIS));
        pnlTableTop.setBackground(COLOR_PANEL_BG);

        lblTieuDeBang = new JLabel("Danh mục phụ tùng tồn kho");
        lblTieuDeBang.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPhuDeBang = new JLabel("Đang tải dữ liệu... - Sắp xếp số lượng tồn từ thấp đến cao");
        lblPhuDeBang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhuDeBang.setForeground(Color.GRAY);

        pnlTableTop.add(lblTieuDeBang);
        pnlTableTop.add(lblPhuDeBang);
        pnlTableTop.add(Box.createVerticalStrut(15));
        pnlTableTop.add(createLegendPanel());
        pnlTableTop.add(Box.createVerticalStrut(10));

        pnlCenter.add(pnlTableTop, BorderLayout.NORTH);

        // 2.2 BẢNG JTABLE
        String[] columns = {"Mã phụ tùng", "Tên phụ tùng", "Đơn vị tính", "Số lượng tồn", "Mức tồn kho"};
        modelTonKho = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTonKho = new JTable(modelTonKho);
        tblTonKho.setRowHeight(38);
        tblTonKho.setFillsViewportHeight(true);
        tblTonKho.setShowGrid(false);
        tblTonKho.setShowHorizontalLines(true);
        tblTonKho.setGridColor(new Color(235, 235, 235));

        JTableHeader header = tblTonKho.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        tblTonKho.getColumnModel().getColumn(0).setPreferredWidth(120); // Mã
        tblTonKho.getColumnModel().getColumn(1).setPreferredWidth(250); // Tên
        tblTonKho.getColumnModel().getColumn(2).setPreferredWidth(120); // ĐVT
        tblTonKho.getColumnModel().getColumn(3).setPreferredWidth(120); // Số lượng
        tblTonKho.getColumnModel().getColumn(4).setPreferredWidth(200); // Mức tồn

        TonKhoRenderer customRenderer = new TonKhoRenderer();
        for (int i = 0; i < tblTonKho.getColumnCount(); i++) {
            tblTonKho.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tblTonKho);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(COLOR_PANEL_BG);
        pnlCenter.add(scrollPane, BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);

        // 3. FOOTER
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(COLOR_BG_MAIN);
        pnlFooter.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblFooterLeft = new JLabel("Hiển thị: 0 / 0 loại phụ tùng");
        lblFooterLeft.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooterLeft.setForeground(Color.GRAY);

        lblFooterRight = new JLabel("Kiểm kê: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")));
        lblFooterRight.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooterRight.setForeground(Color.GRAY);

        pnlFooter.add(lblFooterLeft, BorderLayout.WEST);
        pnlFooter.add(lblFooterRight, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel createLegendPanel() {
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLegend.setBackground(COLOR_PANEL_BG);

        JLabel lblMucTon = new JLabel("Mức tồn:");
        lblMucTon.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        pnlLegend.add(lblMucTon);
        // Màu cho số lượng tồn kho
        pnlLegend.add(createChip("Dưới 5: Sắp hết", COLOR_DANGER, new Color(254, 226, 226)));
        pnlLegend.add(createChip("5 - 10: Tồn thấp", COLOR_WARNING, new Color(255, 237, 213)));
        pnlLegend.add(createChip("Trên 10: Đủ tồn", COLOR_SUCCESS, new Color(220, 252, 231)));

        lblCanNhapThem = new JLabel("0 loại cần nhập thêm ngay");
        lblCanNhapThem.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCanNhapThem.setForeground(COLOR_DANGER);
        pnlLegend.add(Box.createHorizontalStrut(20));
        pnlLegend.add(lblCanNhapThem);

        return pnlLegend;
    }

    private JPanel createChip(String text, Color fgColor, Color bgColor) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(bgColor);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(fgColor);
        pnl.add(lbl);
        return pnl;
    }
    private void napDuLieuBanDau() {
        taiDuLieuVaoBang("Tất cả phụ tùng");
    }

    private void taiDuLieuVaoBang(String tieuChiLoc) {
        modelTonKho.setRowCount(0);
        int canNhapThemCount = 0;
        int tongSoLoai = 0;

        List<PhuTung> dsPhuTung = phuTungDAO.layTatCaPhuTung();
        
        if (dsPhuTung != null && !dsPhuTung.isEmpty()) {
            
            // Sắp xếp tăng dần theo Số lượng tồn
            Collections.sort(dsPhuTung, Comparator.comparingInt(PhuTung::getSoLuongTon));

            for (PhuTung pt : dsPhuTung) {
                int sl = pt.getSoLuongTon();
                
                // Xác định trạng thái & Đếm loại cần nhập
                String trangThai = "Đủ tồn";
                if (sl < 5) {
                    trangThai = "Sắp hết - Cần nhập thêm";
                    canNhapThemCount++;
                } else if (sl <= 10) {
                    trangThai = "Tồn thấp - Theo dõi";
                }

                // Lọc dữ liệu
                boolean isMatch = true;
                if (tieuChiLoc.equals("Sắp hết (< 5)") && sl >= 5) isMatch = false;
                if (tieuChiLoc.equals("Tồn thấp (5 - 10)") && (sl < 5 || sl > 10)) isMatch = false;
                if (tieuChiLoc.equals("Đủ tồn (> 10)") && sl <= 10) isMatch = false;

                if (isMatch) {
                    // Lấy chính xác Đơn vị tính từ Database
                    String donViTinh = pt.getDonViTinh() != null ? pt.getDonViTinh() : "—";
                    
                    modelTonKho.addRow(new Object[]{
                        pt.getMaPhuTung(), pt.getTenPhuTung(), donViTinh, sl, trangThai
                    });
                    tongSoLoai++;
                }
            }
        }

        // Cập nhật thống kê
        lblPhuDeBang.setText(tongSoLoai + " loại phụ tùng - Sắp xếp số lượng tồn từ thấp đến cao");
        lblCanNhapThem.setText(canNhapThemCount + " loại cần nhập thêm ngay!");
        lblFooterLeft.setText("Hiển thị: " + tongSoLoai + " / " + (dsPhuTung != null ? dsPhuTung.size() : 0) + " loại phụ tùng");
        lblFooterRight.setText("Kiểm kê: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")));
    }

    private void setupActionEvents() {
        btnLoc.addActionListener(e -> {
            String tieuChi = cboLocPhuTung.getSelectedItem().toString();
            taiDuLieuVaoBang(tieuChi);
        });
    }
    // Hàm đổi màu chữ theo tồn kho
    class TonKhoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Thiết lập font và khoảng trống
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 10, 0, 10)); 

            // Cột index 3 hiện tại là "Số lượng tồn"
            try {
                int qty = Integer.parseInt(table.getValueAt(row, 3).toString());
                
                // Thuật toán đổi màu chữ chuẩn xác
                if (qty < 5) {
                    c.setForeground(COLOR_DANGER);
                } else if (qty <= 10) {
                    c.setForeground(COLOR_WARNING);
                } else {
                    c.setForeground(COLOR_SUCCESS);
                }
            } catch (Exception e) {
                c.setForeground(Color.BLACK); 
            }

            // Căn lề Đơn vị tính và Số lượng tồn ra giữa
            if (column == 2 || column == 3) {
                setHorizontalAlignment(JLabel.CENTER);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }

            // Xử lý màu nền khi được Click
            if (isSelected) {
                c.setBackground(new Color(235, 244, 255)); // Xanh nhạt tinh tế khi click
                c.setForeground(Color.BLACK); // Giữ chữ đen để dễ đọc khi click
            } else {
                c.setBackground(COLOR_PANEL_BG);
            }

            return c;
        }
    }
}
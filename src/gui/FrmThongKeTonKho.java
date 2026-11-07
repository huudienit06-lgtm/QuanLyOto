package gui;

import controller.ThongKeTonKhoController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final Color COLOR_DANGER = new Color(220, 38, 38); 
    private static final Color COLOR_WARNING = new Color(217, 119, 6); 
    private static final Color COLOR_SUCCESS = new Color(22, 163, 74); 

    private JComboBox<String> cboLocPhuTung;
    private JButton btnLoc;
    private JComboBox<String> cboKyNhap;
    private JComboBox<String> cboKyXuat;
    private DefaultTableModel modelThongKeNhap;
    private DefaultTableModel modelThongKeXuat;
    
    private JLabel lblTieuDeBang, lblPhuDeBang, lblCanNhapThem;
    private DefaultTableModel modelTonKho;
    private JTable tblTonKho;
    private JLabel lblFooterLeft, lblFooterRight;

    private final ThongKeTonKhoController controller;

    public FrmThongKeTonKho() {
        initComponents();
        this.controller = new ThongKeTonKhoController(this);
        controller.khoiTao();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(COLOR_BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 15, 20));

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
        tblTonKho.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblTonKho.getColumnModel().getColumn(1).setPreferredWidth(250);
        tblTonKho.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblTonKho.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblTonKho.getColumnModel().getColumn(4).setPreferredWidth(200);

        TonKhoRenderer customRenderer = new TonKhoRenderer();
        for (int i = 0; i < tblTonKho.getColumnCount(); i++) {
            tblTonKho.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tblTonKho);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(COLOR_PANEL_BG);
        scrollPane.setPreferredSize(new Dimension(0, 260));
        pnlCenter.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setBackground(COLOR_BG_MAIN);
        pnlNoiDung.add(pnlCenter, BorderLayout.CENTER);
        pnlNoiDung.add(createThongKeNhapXuatPanel(), BorderLayout.SOUTH);
        JScrollPane scrNoiDung = new JScrollPane(pnlNoiDung);
        scrNoiDung.setBorder(BorderFactory.createEmptyBorder());
        scrNoiDung.getViewport().setBackground(COLOR_BG_MAIN);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);
        add(scrNoiDung, BorderLayout.CENTER);

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

    private JPanel createThongKeNhapXuatPanel() {
        JPanel pnlThongKe = new JPanel(new java.awt.GridLayout(1, 2, 12, 0));
        pnlThongKe.setBackground(COLOR_BG_MAIN);
        pnlThongKe.add(createKhungThongKe("Thống kê nhập kho", "Nhập", true));
        pnlThongKe.add(createKhungThongKe("Thống kê xuất kho", "Xuất", false));
        return pnlThongKe;
    }

    private JPanel createKhungThongKe(String tieuDe, String kieuPhieu, boolean laNhap) {
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 8));
        pnlKhung.setBackground(COLOR_PANEL_BG);
        pnlKhung.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(10, 12, 10, 12)));

        JPanel pnlHeaderKhung = new JPanel(new BorderLayout(8, 0));
        pnlHeaderKhung.setBackground(COLOR_PANEL_BG);
        JLabel lblTieuDeKhung = new JLabel(tieuDe);
        lblTieuDeKhung.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTieuDeKhung.setForeground(laNhap ? COLOR_SUCCESS : COLOR_PRIMARY);
        JComboBox<String> cboKy = new JComboBox<>(new String[] {"Tuần này", "Tháng này", "Năm nay"});
        cboKy.setPreferredSize(new Dimension(130, 30));
        pnlHeaderKhung.add(lblTieuDeKhung, BorderLayout.WEST);
        pnlHeaderKhung.add(cboKy, BorderLayout.EAST);

        DefaultTableModel model = new DefaultTableModel(new Object[] {"Mã phụ tùng", "Tên phụ tùng", "Đơn vị", "Số lượng"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tblThongKe = new JTable(model);
        tblThongKe.setRowHeight(28);
        tblThongKe.setFillsViewportHeight(true);
        tblThongKe.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblThongKe.getTableHeader().setBackground(new Color(248, 250, 252));
        tblThongKe.getColumnModel().getColumn(0).setPreferredWidth(95);
        tblThongKe.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblThongKe.getColumnModel().getColumn(2).setPreferredWidth(65);
        tblThongKe.getColumnModel().getColumn(3).setPreferredWidth(80);

        JScrollPane scrThongKe = new JScrollPane(tblThongKe);
        scrThongKe.setPreferredSize(new Dimension(0, 190));
        scrThongKe.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        pnlKhung.add(pnlHeaderKhung, BorderLayout.NORTH);
        pnlKhung.add(scrThongKe, BorderLayout.CENTER);

        if (laNhap) {
            cboKyNhap = cboKy;
            modelThongKeNhap = model;
        } else {
            cboKyXuat = cboKy;
            modelThongKeXuat = model;
        }
        return pnlKhung;
    }

    private JPanel createLegendPanel() {
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLegend.setBackground(COLOR_PANEL_BG);
        JLabel lblMucTon = new JLabel("Mức tồn:");
        lblMucTon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlLegend.add(lblMucTon);
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
        pnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bgColor.darker(), 1, true), new EmptyBorder(5, 12, 5, 12)));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(fgColor);
        pnl.add(lbl);
        return pnl;
    }

    class TonKhoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 10, 0, 10)); 
            try {
                int qty = Integer.parseInt(table.getValueAt(row, 3).toString());
                if (qty < 5) c.setForeground(COLOR_DANGER);
                else if (qty <= 10) c.setForeground(COLOR_WARNING);
                else c.setForeground(COLOR_SUCCESS);
            } catch (Exception e) { c.setForeground(Color.BLACK); }
            if (column == 2 || column == 3) setHorizontalAlignment(JLabel.CENTER);
            else setHorizontalAlignment(JLabel.LEFT);
            if (isSelected) {
                c.setBackground(new Color(235, 244, 255));
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(COLOR_PANEL_BG);
            }
            return c;
        }
    }

    public JComboBox<String> getCboLocPhuTung() { return cboLocPhuTung; }
    public JButton getBtnLoc() { return btnLoc; }
    public JComboBox<String> getCboKyNhap() { return cboKyNhap; }
    public JComboBox<String> getCboKyXuat() { return cboKyXuat; }
    public DefaultTableModel getModelThongKeNhap() { return modelThongKeNhap; }
    public DefaultTableModel getModelThongKeXuat() { return modelThongKeXuat; }
    public DefaultTableModel getModelTonKho() { return modelTonKho; }
    public JLabel getLblPhuDeBang() { return lblPhuDeBang; }
    public JLabel getLblCanNhapThem() { return lblCanNhapThem; }
    public JLabel getLblFooterLeft() { return lblFooterLeft; }
    public JLabel getLblFooterRight() { return lblFooterRight; }
}
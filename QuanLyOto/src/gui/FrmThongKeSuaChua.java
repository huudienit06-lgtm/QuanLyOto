package gui;

import controller.ThongKeSuaChuaController;
import dao.BaoCaoSuaChuaDAO.ThongKeBieuDo;
import dao.BaoCaoSuaChuaDAO.ThongKeSuDung;
import dao.BaoCaoSuaChuaDAO.ThongKeTongQuan;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class FrmThongKeSuaChua extends JPanel {
    private final JTextField txtTuNgay = new JTextField(12);
    private final JTextField txtDenNgay = new JTextField(12);
    private final JComboBox<String> cboKySuDung = new JComboBox<>();
    private final JComboBox<String> cboKyBieuDo = new JComboBox<>();
    private final JButton btnThongKe = new JButton("Thống kê");
    private final JButton btnLamMoi = new JButton("Làm mới");

    private final JLabel lblTongPhieu = new JLabel("—");
    private final JLabel lblDaHoanThanh = new JLabel("—");
    private final JLabel lblDangXuLy = new JLabel("—");
    private final JLabel lblDoanhThu = new JLabel("—");
    private final BieuDoDuong pnlBieuDoTheoThang = new BieuDoDuong();
    private final BangHot pnlDichVuHot = new BangHot("DỊCH VỤ HOT", UIConfig.COLOR_PRIMARY);
    private final BangHot pnlPhuTungHot = new BangHot("PHỤ TÙNG HOT", UIConfig.COLOR_SUCCESS);
    private final JLabel lblTrangThai = new JLabel("Chọn điều kiện rồi nhấn Thống kê");

    private final ThongKeSuaChuaController controller = new ThongKeSuaChuaController(this);
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ISO_LOCAL_DATE;

    public FrmThongKeSuaChua() {
        setOpaque(false);
        setLayout(new BorderLayout());
        JTextField[] textFields = { txtTuNgay, txtDenNgay };
        for (JTextField textField : textFields) {
            textField.setFont(UIConfig.FONT_REGULAR);
            textField.setForeground(UIConfig.COLOR_TEXT);
            textField.setBackground(UIConfig.COLOR_SURFACE);
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConfig.COLOR_BORDER),
                    BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        }
        cboKySuDung.setFont(UIConfig.FONT_REGULAR);
        cboKyBieuDo.setFont(UIConfig.FONT_REGULAR);
        btnThongKe.setFont(UIConfig.FONT_BOLD);
        btnThongKe.setBackground(UIConfig.COLOR_PRIMARY);
        btnThongKe.setForeground(Color.WHITE);
        btnThongKe.setOpaque(true);
        btnThongKe.setBorderPainted(false);
        btnThongKe.setFocusPainted(false);
        btnLamMoi.setFont(UIConfig.FONT_BOLD);
        btnLamMoi.setBackground(UIConfig.COLOR_SECONDARY);
        btnLamMoi.setForeground(UIConfig.COLOR_TEXT);
        btnLamMoi.setOpaque(true);
        btnLamMoi.setBorderPainted(false);
        btnLamMoi.setFocusPainted(false);
        JLabel[] metricLabels = { lblTongPhieu, lblDaHoanThanh, lblDangXuLy, lblDoanhThu };
        for (JLabel metricLabel : metricLabels) {
            metricLabel.setFont(UIConfig.FONT_TITLE);
            metricLabel.setForeground(UIConfig.COLOR_TITLE);
        }
        lblTrangThai.setFont(UIConfig.FONT_REGULAR);
        lblTrangThai.setForeground(UIConfig.COLOR_MUTED);

        cboKySuDung.addItem("Ngày");
        cboKySuDung.addItem("Tháng");
        cboKySuDung.addItem("Năm");
        cboKyBieuDo.addItem("Tuần");
        cboKyBieuDo.addItem("Tháng");
        cboKyBieuDo.addItem("Năm");

        LocalDate homNay = LocalDate.now();
        txtTuNgay.setText(homNay.withDayOfMonth(1).toString());
        txtDenNgay.setText(homNay.toString());
        txtTuNgay.setToolTipText("Định dạng ngày: yyyy-MM-dd");
        txtDenNgay.setToolTipText("Định dạng ngày: yyyy-MM-dd");
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFilterCard(), BorderLayout.NORTH);

        JPanel pnlReportContent = new JPanel(new BorderLayout(0, 14));
        pnlReportContent.setOpaque(false);
        pnlReportContent.add(createSummaryCards(), BorderLayout.NORTH);

        JPanel pnlDetails = new JPanel(new BorderLayout(0, 14));
        pnlDetails.setOpaque(false);
        pnlDetails.add(createChartPlaceholders(), BorderLayout.NORTH);
        JPanel pnlHot = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHot.setOpaque(false);
        pnlHot.add(pnlDichVuHot);
        pnlHot.add(pnlPhuTungHot);
        pnlDetails.add(pnlHot, BorderLayout.CENTER);
        pnlReportContent.add(pnlDetails, BorderLayout.CENTER);
        pnlContent.add(pnlReportContent, BorderLayout.CENTER);

        JScrollPane scrNoiDung = new JScrollPane(pnlContent);
        scrNoiDung.setBorder(null);
        scrNoiDung.setOpaque(false);
        scrNoiDung.getViewport().setOpaque(false);
        scrNoiDung.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrNoiDung.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);
        JPanel pnlPage = new JPanel(new BorderLayout(0, 16));
        pnlPage.setBackground(UIConfig.COLOR_BACKGROUND);
        pnlPage.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 5));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("THỐNG KÊ SỬA CHỮA");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new JLabel(
            "Theo dõi tình trạng phiếu sửa chữa, doanh thu và hiệu suất xử lý");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSubtitle, BorderLayout.CENTER);
        pnlPage.add(pnlHeader, BorderLayout.NORTH);
        pnlPage.add(scrNoiDung, BorderLayout.CENTER);
        add(pnlPage, BorderLayout.CENTER);
        add(lblTrangThai, BorderLayout.SOUTH);
        controller.khoiTao();
    }

    private LocalDate docNgay(String giaTri, String tenTruong) {
        try {
            return LocalDate.parse(giaTri.trim(), DINH_DANG_NGAY);
        } catch (DateTimeParseException exception) {
            throw new DateTimeParseException(
                    tenTruong + " không hợp lệ. Dùng định dạng yyyy-MM-dd.",
                    giaTri, exception.getErrorIndex(), exception);
        }
    }

    public void capNhatTongQuan(ThongKeTongQuan tongQuan) {
        lblTongPhieu.setText(String.valueOf(tongQuan.tongPhieu()));
        lblDaHoanThanh.setText(String.valueOf(tongQuan.daHoanThanh()));
        lblDangXuLy.setText(String.valueOf(tongQuan.dangXuLy()));
        lblDoanhThu.setText(dinhDangTien(tongQuan.doanhThu()));
    }

    public void capNhatBangHot(List<ThongKeSuDung> dichVuHot, List<ThongKeSuDung> phuTungHot) {
        pnlDichVuHot.setData(dichVuHot);
        pnlPhuTungHot.setData(phuTungHot);
    }

    public void capNhatBieuDo(List<ThongKeBieuDo> danhSach) {
        String[] nhan = new String[danhSach.size()];
        double[] giaTri = new double[danhSach.size()];
        for (int i = 0; i < danhSach.size(); i++) {
            nhan[i] = danhSach.get(i).nhan();
            giaTri[i] = danhSach.get(i).soLuong();
        }
        pnlBieuDoTheoThang.setData(nhan, giaTri);
    }

    public void lamMoiBieuMau() {
        LocalDate homNay = LocalDate.now();
        txtTuNgay.setText(homNay.withDayOfMonth(1).toString());
        txtDenNgay.setText(homNay.toString());
        cboKySuDung.setSelectedItem("Tháng");
        cboKyBieuDo.setSelectedIndex(0);
        lblTongPhieu.setText("—");
        lblDaHoanThanh.setText("—");
        lblDangXuLy.setText("—");
        lblDoanhThu.setText("—");
        pnlDichVuHot.setData(List.of());
        pnlPhuTungHot.setData(List.of());
        pnlBieuDoTheoThang.setData(new String[0], new double[0]);
        lblTrangThai.setText("Đã làm mới điều kiện thống kê.");
    }

    private String dinhDangTien(BigDecimal giaTri) {
        return giaTri == null ? "0 VNĐ" : String.format("%,.0f VNĐ", giaTri);
    }

    public void hienThiLoi(String thongBao) {
        JOptionPane.showMessageDialog(this, thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
        lblTrangThai.setText("Có lỗi khi xử lý thống kê.");
    }

    public JButton layNutThongKe() {
        return btnThongKe;
    }

    public JButton layNutLamMoi() {
        return btnLamMoi;
    }

    public LocalDate layTuNgay() {
        return docNgay(txtTuNgay.getText(), "Từ ngày");
    }

    public LocalDate layDenNgay() {
        return docNgay(txtDenNgay.getText(), "Đến ngày");
    }

    public String layKySuDung() {
        return (String) cboKySuDung.getSelectedItem();
    }

    public String layKyBieuDo() {
        return (String) cboKyBieuDo.getSelectedItem();
    }

    public void thongBao(String message) {
        lblTrangThai.setText(message);
    }

    private JPanel createFilterCard() {
        JPanel pnlCard = createCardPanel();
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Từ ngày", txtTuNgay, 0, 0);
        addField(pnlFields, "Đến ngày", txtDenNgay, 2, 0);
        addField(pnlFields, "Xếp hạng theo", cboKySuDung, 0, 1);
        addField(pnlFields, "Biểu đồ theo", cboKyBieuDo, 2, 1);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton[] buttons = { btnThongKe, btnLamMoi };
        for (int index = 0; index < buttons.length; index++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = index;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 8);
            pnlButtons.add(buttons[index], gbc);
        }
        JLabel lblSection = new JLabel("Điều kiện thống kê");
        lblSection.setFont(UIConfig.FONT_SECTION);
        lblSection.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblSection, BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createSummaryCards() {
        JPanel pnlSummary = new JPanel(new GridLayout(1, 1));
        pnlSummary.setOpaque(false);

        JPanel pnlTongQuan = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlTongQuan.setOpaque(false);
        pnlTongQuan.add(createMetricCard("Tổng phiếu sửa chữa", lblTongPhieu));
        pnlTongQuan.add(createMetricCard("Đã hoàn thành", lblDaHoanThanh));
        pnlTongQuan.add(createMetricCard("Đang xử lý", lblDangXuLy));
        pnlTongQuan.add(createMetricCard("Tổng doanh thu", lblDoanhThu));

        pnlSummary.add(pnlTongQuan);
        return pnlSummary;
    }

    private JPanel createMetricCard(String title, JLabel lblValue) {
        JPanel pnlCard = createCardPanel();
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConfig.FONT_REGULAR);
        lblTitle.setForeground(UIConfig.COLOR_MUTED);
        pnlCard.add(lblTitle, BorderLayout.NORTH);
        pnlCard.add(lblValue, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel createChartPlaceholders() {
        JPanel pnlCharts = new JPanel(new BorderLayout());
        pnlCharts.setOpaque(false);
        pnlCharts.add(createChartCard(
                "Số phiếu sửa chữa theo tuần, tháng, năm",
                pnlBieuDoTheoThang), BorderLayout.CENTER);
        return pnlCharts;
    }

    private JPanel createChartCard(String title, JComponent chart) {
        JPanel pnlCard = createCardPanel();
        pnlCard.setPreferredSize(new Dimension(0, 250));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblTitle, BorderLayout.NORTH);
        pnlCard.add(chart, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel createCardPanel() {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 12));
        pnlCard.setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConfig.COLOR_BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return pnlCard;
    }

    private void addField(JPanel pnlFields, String text, java.awt.Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lblField = new JLabel(text);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_MUTED);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlFields.add(component, gbcInput);
    }

    private static final class BangHot extends JPanel {
        private final Color mauChu;
        private final JPanel pnlNoiDung = new JPanel();

        private BangHot(String tieuDe, Color mauChu) {
            super(new BorderLayout());
            this.mauChu = mauChu;
            setLayout(new BorderLayout(0, 8));
            setOpaque(true);
                setBackground(UIConfig.COLOR_SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConfig.COLOR_BORDER),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)));
                JLabel lblTitle = new JLabel(tieuDe);
                lblTitle.setFont(UIConfig.FONT_SECTION);
                lblTitle.setForeground(UIConfig.COLOR_TITLE);
                add(lblTitle, BorderLayout.NORTH);
            pnlNoiDung.setOpaque(false);
            pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
            add(pnlNoiDung, BorderLayout.CENTER);
            setPreferredSize(new Dimension(0, 190));
            setData(List.of());
        }

        private void setData(List<ThongKeSuDung> danhSach) {
            pnlNoiDung.removeAll();
            if (danhSach.isEmpty()) {
                JLabel lblRong = new JLabel("Chưa có dữ liệu");
                lblRong.setForeground(UIConfig.COLOR_MUTED);
                pnlNoiDung.add(lblRong);
            } else {
                int[] coChu = { 22, 18, 15 };
                for (int i = 0; i < Math.min(3, danhSach.size()); i++) {
                    ThongKeSuDung item = danhSach.get(i);
                    JLabel lblItem = new JLabel(
                            (i + 1) + ". " + item.ten() + "  (" + item.soLuong() + ")",
                            SwingConstants.LEFT);
                    lblItem.setToolTipText(item.ma() + " - " + item.ten());
                    lblItem.setAlignmentX(Component.LEFT_ALIGNMENT);
                    lblItem.setFont(lblItem.getFont().deriveFont(Font.BOLD, (float) coChu[i]));
                    lblItem.setForeground(i == 0 ? mauChu : UIConfig.COLOR_TEXT);
                    lblItem.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                    pnlNoiDung.add(lblItem);
                    if (i < Math.min(3, danhSach.size()) - 1) {
                        pnlNoiDung.add(Box.createVerticalStrut(3));
                    }
                }
            }
            pnlNoiDung.revalidate();
            pnlNoiDung.repaint();
        }
    }

    private static final class BieuDoDuong extends JComponent {
        private String[] nhan = new String[0];
        private double[] giaTri = new double[0];

        private BieuDoDuong() {
            setOpaque(true);
            setBackground(UIConfig.COLOR_SURFACE);
            setPreferredSize(new Dimension(500, 190));
        }

        private void setData(String[] nhan, double[] giaTri) {
            this.nhan = Arrays.copyOf(nhan, nhan.length);
            this.giaTri = Arrays.copyOf(giaTri, giaTri.length);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (giaTri.length == 0 || Arrays.stream(giaTri).sum() <= 0) {
                    veThongBaoRong(g2);
                    return;
                }

                int paddingLeft = 78;
                int paddingRight = 64;
                int paddingTop = 24;
                int paddingBottom = 70;

                int chartWidth = getWidth() - paddingLeft - paddingRight;
                int chartHeight = getHeight() - paddingTop - paddingBottom;
                double giaTriLonNhat = Arrays.stream(giaTri).max().orElse(0);
                double mocLonNhat = giaTriLonNhat <= 0 ? 1 : giaTriLonNhat;

                veDuongLuoi(g2, paddingLeft, paddingTop, chartWidth, chartHeight, mocLonNhat);

                int[] toaDoX = new int[giaTri.length];
                int[] toaDoY = new int[giaTri.length];

                for (int i = 0; i < giaTri.length; i++) {
                    if (giaTri.length == 1) {
                        toaDoX[i] = paddingLeft + chartWidth / 2;
                    } else {
                        toaDoX[i] = paddingLeft + i * chartWidth / (giaTri.length - 1);
                    }
                    toaDoY[i] = paddingTop + chartHeight
                            - (int) Math.round(giaTri[i] / mocLonNhat * chartHeight);
                }

                g2.setColor(UIConfig.COLOR_PRIMARY);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 1; i < giaTri.length; i++) {
                    g2.drawLine(toaDoX[i - 1], toaDoY[i - 1], toaDoX[i], toaDoY[i]);
                }

                g2.setStroke(new BasicStroke(1.6f));
                for (int i = 0; i < giaTri.length; i++) {
                    g2.setColor(UIConfig.COLOR_PRIMARY);
                    g2.fillOval(toaDoX[i] - 5, toaDoY[i] - 5, 10, 10);

                    String giaTriHienTai = String.valueOf((int) giaTri[i]);
                    int chieuRongGiaTri = g2.getFontMetrics().stringWidth(giaTriHienTai);
                    g2.setColor(UIConfig.COLOR_TEXT);
                    g2.drawString(giaTriHienTai, toaDoX[i] - chieuRongGiaTri / 2, toaDoY[i] - 12);

                    int buocNhan = Math.max(1, (int) Math.ceil((double) nhan.length / 8));
                    if (i % buocNhan == 0 || i == nhan.length - 1) {
                        String nhanHienTai = rutGonNhan(nhan[i]);
                        int chieuRongNhan = g2.getFontMetrics().stringWidth(nhanHienTai);
                        g2.drawString(nhanHienTai, toaDoX[i] - chieuRongNhan / 2, getHeight() - 22);
                    }
                }

                g2.setColor(UIConfig.COLOR_TEXT);
                String nhanTrucX = "Mốc thời gian";
                int chieuRongTrucX = g2.getFontMetrics().stringWidth(nhanTrucX);
                g2.drawString(nhanTrucX,
                        paddingLeft + (chartWidth - chieuRongTrucX) / 2,
                        getHeight() - 2);

                String nhanTrucY = "Số phiếu sửa chữa";
                int chieuRongTrucY = g2.getFontMetrics().stringWidth(nhanTrucY);
                g2.translate(19, paddingTop + chartHeight / 2.0);
                g2.rotate(-Math.PI / 2);
                g2.drawString(nhanTrucY, -chieuRongTrucY / 2, 0);
            } finally {
                g2.dispose();
            }
        }

        private String rutGonNhan(String nhan) {
            if (nhan != null && nhan.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return nhan.substring(8) + "/" + nhan.substring(5, 7);
            }
            return nhan;
        }

        private void veDuongLuoi(Graphics2D g2, int paddingLeft, int paddingTop, int chartWidth, int chartHeight,
                double mocLonNhat) {
            int soDuongLuoi = 4;
            for (int i = 0; i <= soDuongLuoi; i++) {
                int y = paddingTop + chartHeight - i * chartHeight / soDuongLuoi;
                double giaTriThuc = mocLonNhat * i / soDuongLuoi;
                g2.setColor(UIConfig.COLOR_TABLE_GRID);
                g2.drawLine(paddingLeft, y, paddingLeft + chartWidth, y);
                g2.setColor(UIConfig.COLOR_MUTED);
                String nhanTruc = String.valueOf((int) giaTriThuc);
                int doRong = g2.getFontMetrics().stringWidth(nhanTruc);
                g2.drawString(nhanTruc, paddingLeft - doRong - 10, y + 4);
            }
            g2.setColor(UIConfig.COLOR_BORDER);
            g2.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight);
            g2.drawLine(paddingLeft, paddingTop + chartHeight, paddingLeft + chartWidth, paddingTop + chartHeight);
        }

        private void veThongBaoRong(Graphics2D g2) {
            String thongBao = "Chưa có dữ liệu";
            FontMetrics metrics = g2.getFontMetrics();
            g2.setColor(UIConfig.COLOR_MUTED);
            g2.drawString(thongBao, (getWidth() - metrics.stringWidth(thongBao)) / 2, getHeight() / 2);
        }
    }
}

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

public class FrmBaoCaoTongHop extends JPanel {
    private final AppTextField txtTuNgay = new AppTextField(12);
    private final AppTextField txtDenNgay = new AppTextField(12);
    private final AppComboBox<String> cboNhomBaoCao = new AppComboBox<>();
    private final DefaultTableModel modelBaoCao = new DefaultTableModel(
            new Object[] {"Chỉ tiêu", "Kỳ trước", "Kỳ báo cáo", "Chênh lệch"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final javax.swing.JTable tblBaoCao = FormUI.createTable(modelBaoCao);
    private final BieuDoCot pnlBieuDo = new BieuDoCot();

    public FrmBaoCaoTongHop() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        cboNhomBaoCao.addItem("Tất cả");
        cboNhomBaoCao.addItem("Doanh thu");
        cboNhomBaoCao.addItem("Sửa chữa");
        cboNhomBaoCao.addItem("Khách hàng");
        cboNhomBaoCao.addItem("Nhân viên");
        cboNhomBaoCao.addItem("Phụ tùng");

        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFilterCard(), BorderLayout.NORTH);
        JPanel pnlReportContent = new JPanel(new BorderLayout(0, 14));
        pnlReportContent.setOpaque(false);
        pnlReportContent.add(createSummaryCards(), BorderLayout.NORTH);
        JPanel pnlReportDetails = new JPanel(new BorderLayout(0, 14));
        pnlReportDetails.setOpaque(false);
        pnlReportDetails.add(createChartCard(), BorderLayout.NORTH);
        pnlReportDetails.add(FormUI.createTableCard("Bảng tổng hợp", null,
                new AppScrollPane(tblBaoCao)), BorderLayout.CENTER);
        pnlReportContent.add(pnlReportDetails, BorderLayout.CENTER);
        pnlContent.add(pnlReportContent, BorderLayout.CENTER);
        add(FormUI.createPage("BÁO CÁO TỔNG HỢP",
                "Tổng quan các hoạt động chính của gara theo khoảng thời gian", pnlContent), BorderLayout.CENTER);
        add(FormUI.createStatus("Chọn khoảng thời gian và nhóm báo cáo"), BorderLayout.SOUTH);
    }

    public void capNhatBieuDo(String[] nhan, double[] giaTri) {
        if (nhan == null || giaTri == null || nhan.length != giaTri.length) {
            throw new IllegalArgumentException("Nhãn và giá trị biểu đồ phải có cùng số phần tử.");
        }
        for (int index = 0; index < nhan.length; index++) {
            if (nhan[index] == null || !Double.isFinite(giaTri[index]) || giaTri[index] < 0) {
                throw new IllegalArgumentException("Dữ liệu biểu đồ không hợp lệ tại vị trí " + index + ".");
            }
        }
        pnlBieuDo.setData(nhan, giaTri);
    }

    private JPanel createChartCard() {
        AppPanel pnlCard = AppPanel.card(new BorderLayout(0, 12));
        pnlCard.add(FormUI.createSectionTitle("Biểu đồ tổng hợp"), BorderLayout.NORTH);
        pnlCard.add(pnlBieuDo, BorderLayout.CENTER);
        pnlCard.setPreferredSize(new java.awt.Dimension(0, 260));
        return pnlCard;
    }

    private JPanel createFilterCard() {
        AppPanel pnlCard = FormUI.createCard();
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Từ ngày", txtTuNgay, 0, 0);
        FormUI.addField(pnlFields, "Đến ngày", txtDenNgay, 2, 0);
        FormUI.addField(pnlFields, "Nhóm báo cáo", cboNhomBaoCao, 0, 1);
        txtTuNgay.setToolTipText("Định dạng ngày: yyyy-MM-dd");
        txtDenNgay.setToolTipText("Định dạng ngày: yyyy-MM-dd");

        JPanel pnlButtons = FormUI.createButtonRow(
                new AppButton("Tạo báo cáo"),
                new AppButton("Xuất báo cáo", AppButton.Style.SECONDARY));
        pnlCard.add(FormUI.createSectionTitle("Điều kiện báo cáo"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createSummaryCards() {
        JPanel pnlSummary = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlSummary.setOpaque(false);
        pnlSummary.add(createMetricCard("Doanh thu", "—"));
        pnlSummary.add(createMetricCard("Phiếu sửa chữa", "—"));
        pnlSummary.add(createMetricCard("Khách hàng", "—"));
        pnlSummary.add(createMetricCard("Phụ tùng đã xuất", "—"));
        return pnlSummary;
    }

    private JPanel createMetricCard(String title, String value) {
        AppPanel pnlCard = AppPanel.card(new BorderLayout(0, 8));
        pnlCard.add(new AppLabel(title, AppLabel.Style.MUTED), BorderLayout.NORTH);
        pnlCard.add(new AppLabel(value, AppLabel.Style.TITLE), BorderLayout.CENTER);
        return pnlCard;
    }

    private static final class BieuDoCot extends JComponent {
        private static final int PADDING_LEFT = 56;
        private static final int PADDING_RIGHT = 20;
        private static final int PADDING_TOP = 16;
        private static final int PADDING_BOTTOM = 40;
        private String[] nhan = new String[0];
        private double[] giaTri = new double[0];

        private BieuDoCot() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setPreferredSize(new java.awt.Dimension(600, 190));
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();
                int chartWidth = width - PADDING_LEFT - PADDING_RIGHT;
                int chartHeight = height - PADDING_TOP - PADDING_BOTTOM;
                if (chartWidth <= 0 || chartHeight <= 0) {
                    return;
                }

                if (giaTri.length == 0) {
                    g2.setColor(UIConfig.COLOR_MUTED);
                    String message = "Chưa có dữ liệu biểu đồ";
                    FontMetrics metrics = g2.getFontMetrics();
                    g2.drawString(message,
                            (width - metrics.stringWidth(message)) / 2,
                            (height + metrics.getAscent()) / 2);
                    return;
                }

                double giaTriLonNhat = Arrays.stream(giaTri).max().orElse(0);
                double mocTruc = giaTriLonNhat == 0 ? 1 : giaTriLonNhat;
                int soDuongLuoi = 4;
                for (int moc = 0; moc <= soDuongLuoi; moc++) {
                    int y = PADDING_TOP + chartHeight - chartHeight * moc / soDuongLuoi;
                    double giaTriTruc = mocTruc * moc / soDuongLuoi;
                    g2.setColor(UIConfig.COLOR_TABLE_GRID);
                    g2.drawLine(PADDING_LEFT, y, PADDING_LEFT + chartWidth, y);
                    g2.setColor(UIConfig.COLOR_MUTED);
                    String text = String.format("%.0f", giaTriTruc);
                    FontMetrics metrics = g2.getFontMetrics();
                    g2.drawString(text, PADDING_LEFT - metrics.stringWidth(text) - 8, y + 4);
                }

                double khoangMoiCot = (double) chartWidth / nhan.length;
                int rongCot = Math.max(4, (int) (khoangMoiCot * 0.56));
                for (int index = 0; index < giaTri.length; index++) {
                    int caoCot = (int) Math.round(giaTri[index] / mocTruc * chartHeight);
                    int x = PADDING_LEFT + (int) (index * khoangMoiCot
                            + (khoangMoiCot - rongCot) / 2);
                    int y = PADDING_TOP + chartHeight - caoCot;
                    g2.setColor(UIConfig.COLOR_PRIMARY);
                    g2.fillRoundRect(x, y, rongCot, Math.max(caoCot, 2), 8, 8);

                    g2.setColor(UIConfig.COLOR_TEXT);
                    FontMetrics metrics = g2.getFontMetrics();
                    String nhanCot = nhan[index];
                    int nhanX = x + (rongCot - metrics.stringWidth(nhanCot)) / 2;
                    g2.drawString(nhanCot, nhanX, height - 12);
                    String giaTriCot = String.format("%.0f", giaTri[index]);
                    g2.drawString(giaTriCot,
                            x + (rongCot - metrics.stringWidth(giaTriCot)) / 2,
                            Math.max(PADDING_TOP + metrics.getAscent(), y - 5));
                }

                g2.setColor(UIConfig.COLOR_BORDER);
                g2.drawLine(PADDING_LEFT, PADDING_TOP, PADDING_LEFT, PADDING_TOP + chartHeight);
                g2.drawLine(PADDING_LEFT, PADDING_TOP + chartHeight,
                        PADDING_LEFT + chartWidth, PADDING_TOP + chartHeight);
            } finally {
                g2.dispose();
            }
        }
    }
}

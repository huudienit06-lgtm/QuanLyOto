package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import dao.BaoCaoTongHopDAO;
import dao.BaoCaoTongHopDAO.ChiTieuBaoCao;

public class FrmBaoCaoTongHop extends JPanel {
    private final AppTextField txtTuNgay = new AppTextField(12);
    private final AppTextField txtDenNgay = new AppTextField(12);
    private final AppComboBox<String> cboNhomBaoCao = new AppComboBox<>();
    private final JLabel lblTrangThai = new AppLabel("Chọn khoảng thời gian và nhóm báo cáo", AppLabel.Style.MUTED);
    private final JLabel[] lblTieuChiTongHop = new JLabel[4];
    private final JLabel[] lblGiaTriTongHop = new JLabel[4];
    private final BaoCaoTongHopDAO baoCaoTongHopDAO = new BaoCaoTongHopDAO();
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
        LocalDate homNay = LocalDate.now();
        txtTuNgay.setText(homNay.withDayOfMonth(1).toString());
        txtDenNgay.setText(homNay.toString());

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
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblTrangThai, BorderLayout.WEST);
        add(pnlStatus, BorderLayout.SOUTH);
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

        AppButton btnTaoBaoCao = new AppButton("Tạo báo cáo");
        AppButton btnXuatBaoCao = new AppButton("Xuất báo cáo", AppButton.Style.SECONDARY);
        btnTaoBaoCao.addActionListener(event -> taoBaoCao());
        btnXuatBaoCao.addActionListener(event -> xuatBaoCao());
        JPanel pnlButtons = FormUI.createButtonRow(btnTaoBaoCao, btnXuatBaoCao);
        pnlCard.add(FormUI.createSectionTitle("Điều kiện báo cáo"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createSummaryCards() {
        JPanel pnlSummary = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlSummary.setOpaque(false);
        String[] tieuChiMacDinh = {"Doanh thu", "Phiếu sửa chữa", "Khách hàng", "Phụ tùng đã xuất"};
        for (int index = 0; index < lblTieuChiTongHop.length; index++) {
            pnlSummary.add(createMetricCard(index, tieuChiMacDinh[index]));
        }
        return pnlSummary;
    }

    private JPanel createMetricCard(int index, String title) {
        AppPanel pnlCard = AppPanel.card(new BorderLayout(0, 8));
        lblTieuChiTongHop[index] = new AppLabel(title, AppLabel.Style.MUTED);
        lblGiaTriTongHop[index] = new AppLabel("—", AppLabel.Style.TITLE);
        pnlCard.add(lblTieuChiTongHop[index], BorderLayout.NORTH);
        pnlCard.add(lblGiaTriTongHop[index], BorderLayout.CENTER);
        return pnlCard;
    }

    private void taoBaoCao() {
        LocalDate tuNgay;
        LocalDate denNgay;
        try {
            tuNgay = LocalDate.parse(txtTuNgay.getText().trim());
            denNgay = LocalDate.parse(txtDenNgay.getText().trim());
        } catch (DateTimeParseException exception) {
            hienThiLoi("Ngày không hợp lệ. Vui lòng nhập theo định dạng yyyy-MM-dd.");
            return;
        }
        if (denNgay.isBefore(tuNgay)) {
            hienThiLoi("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
            return;
        }

        LocalDate tuKyBaoCao = tuNgay;
        LocalDate denKyBaoCao = denNgay.plusDays(1);
        LocalDate tuKyTruoc = tuNgay.minusDays(denNgay.toEpochDay() - tuNgay.toEpochDay() + 1);
        try {
            List<ChiTieuBaoCao> tatCaChiTieu = baoCaoTongHopDAO.layBaoCao(
                    tuKyTruoc, tuKyBaoCao, denKyBaoCao);
            String nhomDuocChon = (String) cboNhomBaoCao.getSelectedItem();
            List<ChiTieuBaoCao> chiTieuHienThi = tatCaChiTieu.stream()
                    .filter(chiTieu -> "Tất cả".equals(nhomDuocChon)
                            || chiTieu.nhom().equals(nhomDuocChon))
                    .toList();
            modelBaoCao.setRowCount(0);
            for (ChiTieuBaoCao chiTieu : chiTieuHienThi) {
                modelBaoCao.addRow(new Object[] {
                        chiTieu.chiTieu(),
                        dinhDangSo(chiTieu.kyTruoc()),
                        dinhDangSo(chiTieu.kyBaoCao()),
                        dinhDangSo(chiTieu.kyBaoCao() - chiTieu.kyTruoc())
                });
            }
            capNhatTheTongHop(tatCaChiTieu);
            capNhatBieuDo(chiTieuHienThi.stream()
                    .map(chiTieu -> nhanBieuDo(chiTieu.chiTieu())).toArray(String[]::new),
                    chiTieuHienThi.stream().mapToDouble(ChiTieuBaoCao::kyBaoCao).toArray());
            lblTrangThai.setText("Báo cáo từ " + tuNgay + " đến " + denNgay
                    + " — " + chiTieuHienThi.size() + " chỉ tiêu.");
        } catch (java.sql.SQLException exception) {
            lblTrangThai.setText("Không thể tạo báo cáo do lỗi cơ sở dữ liệu.");
            hienThiLoi("Không thể truy vấn báo cáo:\n" + exception.getMessage());
        }
    }

    private String nhanBieuDo(String chiTieu) {
        return switch (chiTieu) {
            case "Tổng doanh thu" -> "Doanh thu";
            case "Số hóa đơn" -> "Hóa đơn";
            case "Số phiếu sửa chữa" -> "Phiếu sửa chữa";
            case "Phiếu đã hoàn thành" -> "Hoàn thành";
            case "Khách hàng sử dụng dịch vụ" -> "Khách hàng";
            case "Phiếu tiếp nhận" -> "Tiếp nhận";
            case "Nhân viên được phân công" -> "Nhân viên";
            case "Số ca được phân công" -> "Số ca";
            case "Số lượng phụ tùng đã xuất" -> "SL phụ tùng";
            case "Giá trị phụ tùng đã xuất" -> "GT phụ tùng";
            default -> chiTieu;
        };
    }

    private void capNhatTheTongHop(List<ChiTieuBaoCao> chiTieu) {
        String[] tieuChiCanHienThi = {
                "Tổng doanh thu", "Số phiếu sửa chữa",
                "Khách hàng sử dụng dịch vụ", "Số lượng phụ tùng đã xuất"
        };
        String[] nhanThe = {"Doanh thu", "Phiếu sửa chữa", "Khách hàng", "Phụ tùng đã xuất"};
        for (int index = 0; index < tieuChiCanHienThi.length; index++) {
            String tenChiTieu = tieuChiCanHienThi[index];
            ChiTieuBaoCao giaTri = chiTieu.stream()
                    .filter(chiTieuBaoCao -> chiTieuBaoCao.chiTieu().equals(tenChiTieu))
                    .findFirst().orElse(null);
            lblTieuChiTongHop[index].setText(nhanThe[index]);
            lblGiaTriTongHop[index].setText(giaTri == null ? "0" : dinhDangSo(giaTri.kyBaoCao()));
        }
    }

    private String dinhDangSo(double giaTri) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.2f", giaTri)
                .replaceAll(",00$", "");
    }

    private void xuatBaoCao() {
        if (modelBaoCao.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hãy tạo báo cáo trước khi xuất.",
                    "Chưa có dữ liệu", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất báo cáo tổng hợp");
        chooser.setSelectedFile(new java.io.File("BaoCaoTongHop.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp CSV (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            for (int column = 0; column < modelBaoCao.getColumnCount(); column++) {
                if (column > 0) {
                    writer.write(',');
                }
                writer.write(csv(modelBaoCao.getColumnName(column)));
            }
            writer.newLine();
            for (int row = 0; row < modelBaoCao.getRowCount(); row++) {
                for (int column = 0; column < modelBaoCao.getColumnCount(); column++) {
                    if (column > 0) {
                        writer.write(',');
                    }
                    Object value = modelBaoCao.getValueAt(row, column);
                    writer.write(csv(value == null ? "" : value.toString()));
                }
                writer.newLine();
            }
            lblTrangThai.setText("Đã xuất báo cáo: " + file.getAbsolutePath());
        } catch (IOException exception) {
            hienThiLoi("Không thể lưu tệp báo cáo:\n" + exception.getMessage());
        }
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void hienThiLoi(String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, "Báo cáo tổng hợp", JOptionPane.ERROR_MESSAGE);
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
            graphics.setColor(getBackground());
            graphics.fillRect(0, 0, getWidth(), getHeight());
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
                    int centerX = PADDING_LEFT + (int) ((index + 0.5) * khoangMoiCot);
                    int maxLabelWidth = Math.max(24, (int) khoangMoiCot - 6);
                    String nhanHienThi = nhanCot;
                    while (metrics.stringWidth(nhanHienThi) > maxLabelWidth
                            && nhanHienThi.length() > 3) {
                        nhanHienThi = nhanHienThi.substring(0, nhanHienThi.length() - 2) + "…";
                    }
                    g2.drawString(nhanHienThi,
                            centerX - metrics.stringWidth(nhanHienThi) / 2, height - 12);
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

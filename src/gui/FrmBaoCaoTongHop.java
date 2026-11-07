package gui;

import dao.BaoCaoTongHopDAO;
import dao.BaoCaoTongHopDAO.ChiTieuBaoCao;
import dao.BaoCaoTongHopDAO.DiemXuHuong;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class FrmBaoCaoTongHop extends JPanel {

    // ── Các thành phần điều kiện lọc ──────────────────────────────────────────
    private final JComboBox<String> cboLoaiKy = new JComboBox<>();
    private final JComboBox<String> cboNhomBaoCao = new JComboBox<>();

    // ── Nhãn trạng thái ───────────────────────────────────────────────────────
    private final JLabel lblTrangThai = new JLabel(
            "Chưa thống kê. Chọn điều kiện rồi nhấn Tạo báo cáo.");

    // ── Mảng nhãn thẻ tổng hợp ────────────────────────────────────────────────
    private final JLabel[] lblTieuChiTongHop = new JLabel[9];
    private final JLabel[] lblGiaTriTongHop = new JLabel[9];

    // ── Tiêu đề biểu đồ ───────────────────────────────────────────────────────
    private JLabel lblTieuDeBieuDo;

    // ── DAO và bảng dữ liệu ───────────────────────────────────────────────────
    private final BaoCaoTongHopDAO baoCaoTongHopDAO = new BaoCaoTongHopDAO();
    private final DefaultTableModel modelBaoCao=new DefaultTableModel(new Object[]{"Chỉ tiêu","Kỳ trước","Kỳ báo cáo","Chênh lệch"},0){@Override public boolean isCellEditable(int row,int column){return false;}};
    private final JTable tblBaoCao = FormUI.createTable(modelBaoCao);

    // ── Biểu đồ đường ─────────────────────────────────────────────────────────
    private final BieuDoDuong pnlBieuDo = new BieuDoDuong();

    // ── Nút bấm ───────────────────────────────────────────────────────────────
    private final JButton btnTaoBaoCao = new JButton("Tạo báo cáo");
    private final JButton btnXuatBaoCao = new JButton("Xuất báo cáo");

    public FrmBaoCaoTongHop() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setOpaque(true);

        // Khởi tạo combobox
        cboLoaiKy.addItem("Ngày");
        cboLoaiKy.addItem("Tháng");
        cboLoaiKy.addItem("Năm");
        cboLoaiKy.setFont(UIConfig.FONT_REGULAR);

        cboNhomBaoCao.addItem("Doanh thu");
        cboNhomBaoCao.addItem("Sửa chữa");
        cboNhomBaoCao.addItem("Khách hàng");
        cboNhomBaoCao.addItem("Nhân viên");
        cboNhomBaoCao.addItem("Phụ tùng");
        cboNhomBaoCao.addItem("Công nợ");
        cboNhomBaoCao.addItem("Kho");
        cboNhomBaoCao.setFont(UIConfig.FONT_REGULAR);

        // Áp dụng style nút bấm
        styleButtonPrimary(btnTaoBaoCao);
        styleButtonSecondary(btnXuatBaoCao);
        btnTaoBaoCao.addActionListener(event -> taoBaoCao());
        btnXuatBaoCao.addActionListener(event -> xuatBaoCao());

        // Nhãn trạng thái
        lblTrangThai.setFont(UIConfig.FONT_REGULAR);
        lblTrangThai.setForeground(UIConfig.COLOR_MUTED);

        // Xây dựng bố cục
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFilterCard(), BorderLayout.NORTH);

        JPanel pnlReportContent = new JPanel(new BorderLayout(0, 14));
        pnlReportContent.setOpaque(false);
        pnlReportContent.add(createSummaryCards(), BorderLayout.NORTH);

        JPanel pnlReportDetails = new JPanel(new BorderLayout(0, 14));
        pnlReportDetails.setOpaque(false);
        pnlReportDetails.add(createChartCard(), BorderLayout.NORTH);
        pnlReportDetails.add(createTableCard(), BorderLayout.CENTER);

        pnlReportContent.add(pnlReportDetails, BorderLayout.CENTER);
        pnlContent.add(pnlReportContent, BorderLayout.CENTER);

        JScrollPane scrNoiDung = new JScrollPane(pnlContent);
        scrNoiDung.setBorder(null);
        scrNoiDung.setOpaque(false);
        scrNoiDung.getViewport().setOpaque(false);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);

        add(createPageHeader("BÁO CÁO TỔNG HỢP",
                "Tổng quan các hoạt động chính của gara theo ngày, tháng hoặc năm",
                scrNoiDung), BorderLayout.CENTER);

        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblTrangThai, BorderLayout.WEST);
        add(pnlStatus, BorderLayout.SOUTH);

        datTrangThaiChuaThongKe();
    }

    // ── Helper: style nút Primary ──────────────────────────────────────────────
    private void styleButtonPrimary(JButton btn) {
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(UIConfig.COLOR_PRIMARY);
        btn.setForeground(UIConfig.COLOR_SURFACE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(6, 16, 6, 16));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    // ── Helper: style nút Secondary ───────────────────────────────────────────
    private void styleButtonSecondary(JButton btn) {
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(UIConfig.COLOR_SECONDARY);
        btn.setForeground(UIConfig.COLOR_TEXT);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(6, 16, 6, 16));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    // ── Helper: tạo card panel (trắng, bo viền) ───────────────────────────────
    private JPanel createCardPanel(java.awt.LayoutManager layout) {
        JPanel pnl = new JPanel(layout);
        pnl.setBackground(UIConfig.COLOR_SURFACE);
        pnl.setOpaque(true);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return pnl;
    }

    // ── Helper: tạo nhãn tiêu đề section ─────────────────────────────────────
    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConfig.FONT_SECTION);
        lbl.setForeground(UIConfig.COLOR_TITLE);
        return lbl;
    }

    // ── Helper: tạo header trang ─────────────────────────────────────────────
    private JPanel createPageHeader(String title, String subtitle, JScrollPane content) {
        JPanel pnlPage = new JPanel(new BorderLayout(0, 14));
        pnlPage.setOpaque(false);

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 4));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSubtitle, BorderLayout.CENTER);

        pnlPage.add(pnlHeader, BorderLayout.NORTH);
        pnlPage.add(content, BorderLayout.CENTER);
        return pnlPage;
    }

    // ── Tạo card điều kiện lọc ────────────────────────────────────────────────
    private JPanel createFilterCard() {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 10));

        pnlCard.add(createSectionTitle("Điều kiện báo cáo"), BorderLayout.NORTH);

        JPanel pnlFields = new JPanel(new java.awt.GridBagLayout());
        pnlFields.setOpaque(false);
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        JLabel lblLoaiKy = new JLabel("Thống kê theo:");
        lblLoaiKy.setFont(UIConfig.FONT_REGULAR);
        lblLoaiKy.setForeground(UIConfig.COLOR_TEXT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        pnlFields.add(lblLoaiKy, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        pnlFields.add(cboLoaiKy, gbc);

        JLabel lblNhom = new JLabel("Nhóm báo cáo:");
        lblNhom.setFont(UIConfig.FONT_REGULAR);
        lblNhom.setForeground(UIConfig.COLOR_TEXT);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        pnlFields.add(lblNhom, gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1;
        pnlFields.add(cboNhomBaoCao, gbc);

        pnlCard.add(pnlFields, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnTaoBaoCao);
        pnlButtons.add(btnXuatBaoCao);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);

        return pnlCard;
    }

    // ── Tạo các thẻ tổng hợp (3x3) ───────────────────────────────────────────
    private JPanel createSummaryCards() {
        JPanel pnlSummary = new JPanel(new GridLayout(3, 3, 12, 12));
        pnlSummary.setOpaque(false);
        String[] tieuChiMacDinh = {
                "Doanh thu", "Hóa đơn", "Phiếu sửa chữa",
                "Khách hàng", "Nhân viên", "Phụ tùng bán",
                "Công nợ", "Nhập kho", "Xuất kho"
        };
        for (int index = 0; index < lblTieuChiTongHop.length; index++) {
            pnlSummary.add(createMetricCard(index, tieuChiMacDinh[index]));
        }
        return pnlSummary;
    }

    // ── Tạo một thẻ chỉ tiêu ─────────────────────────────────────────────────
    private JPanel createMetricCard(int index, String title) {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 8));

        lblTieuChiTongHop[index] = new JLabel(title);
        lblTieuChiTongHop[index].setFont(UIConfig.FONT_REGULAR);
        lblTieuChiTongHop[index].setForeground(UIConfig.COLOR_MUTED);

        lblGiaTriTongHop[index] = new JLabel("0");
        lblGiaTriTongHop[index].setFont(UIConfig.FONT_TITLE);
        lblGiaTriTongHop[index].setForeground(UIConfig.COLOR_TITLE);

        pnlCard.add(lblTieuChiTongHop[index], BorderLayout.NORTH);
        pnlCard.add(lblGiaTriTongHop[index], BorderLayout.CENTER);
        return pnlCard;
    }

    // ── Tạo card biểu đồ ─────────────────────────────────────────────────────
    private JPanel createChartCard() {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 12));

        lblTieuDeBieuDo = new JLabel("Xu hướng theo thời gian");
        lblTieuDeBieuDo.setFont(UIConfig.FONT_SECTION);
        lblTieuDeBieuDo.setForeground(UIConfig.COLOR_TITLE);

        pnlCard.add(lblTieuDeBieuDo, BorderLayout.NORTH);
        pnlCard.add(pnlBieuDo, BorderLayout.CENTER);
        pnlCard.setPreferredSize(new Dimension(0, 360));
        return pnlCard;
    }

    // ── Tạo card bảng tổng hợp ────────────────────────────────────────────────
    private JPanel createTableCard() {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Bảng tổng hợp");
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblTitle, BorderLayout.NORTH);

        JScrollPane scr = new JScrollPane(tblBaoCao);
        scr.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scr.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(scr, BorderLayout.CENTER);

        return pnlCard;
    }

    // ── Cập nhật biểu đồ ─────────────────────────────────────────────────────
    public void capNhatBieuDo(String[] nhan, String[] tenDuong, double[][] giaTri) {
        if (nhan == null || tenDuong == null || giaTri == null || tenDuong.length != giaTri.length) {
            throw new IllegalArgumentException("Nhãn, đường biểu đồ và dữ liệu không khớp.");
        }
        for (double[] duong : giaTri) {
            if (duong == null || duong.length != nhan.length) {
                throw new IllegalArgumentException("Mỗi đường biểu đồ phải có đủ giá trị theo nhãn.");
            }
            for (double diem : duong) {
                if (!Double.isFinite(diem) || diem < 0) {
                    throw new IllegalArgumentException("Giá trị biểu đồ không hợp lệ.");
                }
            }
        }
        pnlBieuDo.setData(nhan, tenDuong, giaTri);
    }

    // ── Đặt trạng thái chưa thống kê ─────────────────────────────────────────
    private void datTrangThaiChuaThongKe() {
        modelBaoCao.setRowCount(0);
        for (JLabel lblGiaTri : lblGiaTriTongHop) {
            if (lblGiaTri != null) {
                lblGiaTri.setText("0");
            }
        }
        pnlBieuDo.setData(new String[0], new String[0], new double[0][]);
        if (lblTieuDeBieuDo != null) {
            lblTieuDeBieuDo.setText("Xu hướng theo thời gian");
        }
        lblTrangThai.setText("Chưa thống kê. Chọn điều kiện rồi nhấn Tạo báo cáo.");
    }

    // ── Tạo báo cáo ──────────────────────────────────────────────────────────
    private void taoBaoCao() {
        LocalDate tuNgay = layNgayBatDau();
        LocalDate denNgay = layNgayKetThuc(tuNgay);
        String loaiKy = (String) cboLoaiKy.getSelectedItem();
        LocalDate tuKyTruoc = switch (loaiKy) {
            case "Ngày" -> tuNgay.minusDays(1);
            case "Tháng" -> tuNgay.minusMonths(1);
            case "Năm" -> tuNgay.minusYears(1);
            default -> throw new IllegalStateException("Kỳ thống kê không hợp lệ.");
        };
        String nhomDuocChon = (String) cboNhomBaoCao.getSelectedItem();
        String doPhanGiai = switch (loaiKy) {
            case "Ngày" -> "HOUR";
            case "Tháng" -> "DAY";
            case "Năm" -> "MONTH";
            default -> throw new IllegalStateException("Kỳ thống kê không hợp lệ.");
        };
        try {
            List<ChiTieuBaoCao> tatCaChiTieu = baoCaoTongHopDAO.layBaoCao(tuKyTruoc, tuNgay, denNgay);
            List<DiemXuHuong> duLieuXuHuong = baoCaoTongHopDAO.layXuHuong(
                    tuNgay, denNgay, nhomDuocChon, doPhanGiai);
            List<ChiTieuBaoCao> chiTieuHienThi = tatCaChiTieu.stream()
                    .filter(chiTieu -> chiTieu.nhom().equals(nhomDuocChon))
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
            capNhatBieuDoXuHuong(tuNgay, denNgay, doPhanGiai, duLieuXuHuong);
            lblTrangThai.setText("Báo cáo " + nhomDuocChon + " từ " + tuNgay
                    + " đến " + denNgay.minusDays(1)
                    + " — " + chiTieuHienThi.size() + " chỉ tiêu.");
        } catch (java.sql.SQLException exception) {
            lblTrangThai.setText("Không thể tạo báo cáo do lỗi cơ sở dữ liệu.");
            hienThiLoi("Không thể truy vấn báo cáo:\n" + exception.getMessage());
        }
    }

    private LocalDate layNgayBatDau() {
        LocalDate homNay = LocalDate.now();
        return switch ((String) cboLoaiKy.getSelectedItem()) {
            case "Ngày" -> homNay;
            case "Tháng" -> homNay.withDayOfMonth(1);
            case "Năm" -> homNay.withDayOfYear(1);
            default -> throw new IllegalStateException("Kỳ thống kê không hợp lệ.");
        };
    }

    private LocalDate layNgayKetThuc(LocalDate tuNgay) {
        return switch ((String) cboLoaiKy.getSelectedItem()) {
            case "Ngày" -> tuNgay.plusDays(1);
            case "Tháng" -> tuNgay.plusMonths(1);
            case "Năm" -> tuNgay.plusYears(1);
            default -> throw new IllegalStateException("Kỳ thống kê không hợp lệ.");
        };
    }

    private void capNhatBieuDoXuHuong(
            LocalDate tuNgay, LocalDate denNgay, String doPhanGiai, List<DiemXuHuong> duLieu) {
        List<LocalDateTime> cacMoc = taoCacMocThoiGian(tuNgay, denNgay, doPhanGiai);
        Map<String, Map<LocalDateTime, Double>> giaTriTheoDuong = new LinkedHashMap<>();
        for (DiemXuHuong diem : duLieu) {
            giaTriTheoDuong.computeIfAbsent(diem.chiTieu(), key -> new TreeMap<>())
                    .put(diem.mocThoiGian(), diem.giaTri());
        }
        String nhom = (String) cboNhomBaoCao.getSelectedItem();
        if ("Kho".equals(nhom)) {
            giaTriTheoDuong.putIfAbsent("Số lượng phụ tùng nhập kho", Map.of());
            giaTriTheoDuong.putIfAbsent("Số lượng phụ tùng xuất kho", Map.of());
        } else if (giaTriTheoDuong.isEmpty()) {
            giaTriTheoDuong.put(tenChiTieuTheoNhom(nhom), Map.of());
        }
        String[] nhan = cacMoc.stream().map(moc -> dinhDangMoc(moc, doPhanGiai))
                .toArray(String[]::new);
        String[] tenDuong = giaTriTheoDuong.keySet().toArray(String[]::new);
        double[][] giaTri = new double[tenDuong.length][cacMoc.size()];
        for (int duong = 0; duong < tenDuong.length; duong++) {
            Map<LocalDateTime, Double> giaTriTheoMoc = giaTriTheoDuong.get(tenDuong[duong]);
            for (int moc = 0; moc < cacMoc.size(); moc++) {
                giaTri[duong][moc] = giaTriTheoMoc.getOrDefault(cacMoc.get(moc), 0.0);
            }
        }
        capNhatBieuDo(nhan, tenDuong, giaTri);
        String ky = switch ((String) cboLoaiKy.getSelectedItem()) {
            case "Ngày" -> tuNgay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case "Tháng" -> tuNgay.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            case "Năm" -> String.valueOf(tuNgay.getYear());
            default -> throw new IllegalStateException("Kỳ thống kê không hợp lệ.");
        };
        lblTieuDeBieuDo.setText("Xu hướng " + nhom + " — " + ky);
    }

    private List<LocalDateTime> taoCacMocThoiGian(
            LocalDate tuNgay, LocalDate denNgay, String doPhanGiai) {
        List<LocalDateTime> cacMoc = new ArrayList<>();
        LocalDateTime moc = switch (doPhanGiai) {
            case "HOUR", "DAY", "MONTH" -> tuNgay.atStartOfDay();
            default -> throw new IllegalArgumentException("Độ phân giải thời gian không hợp lệ.");
        };
        LocalDateTime ketThuc = denNgay.atStartOfDay();
        while (moc.isBefore(ketThuc)) {
            cacMoc.add(moc);
            moc = switch (doPhanGiai) {
                case "HOUR" -> moc.plusHours(1);
                case "DAY" -> moc.plusDays(1);
                case "MONTH" -> moc.plusMonths(1);
                default -> throw new IllegalArgumentException("Độ phân giải thời gian không hợp lệ.");
            };
        }
        return cacMoc;
    }

    private String dinhDangMoc(LocalDateTime moc, String doPhanGiai) {
        return switch (doPhanGiai) {
            case "HOUR" -> moc.format(DateTimeFormatter.ofPattern("HH:mm"));
            case "DAY" -> moc.format(DateTimeFormatter.ofPattern("dd"));
            case "MONTH" -> moc.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            default -> throw new IllegalArgumentException("Độ phân giải thời gian không hợp lệ.");
        };
    }

    private String tenChiTieuTheoNhom(String nhom) {
        return switch (nhom) {
            case "Doanh thu" -> "Tổng doanh thu";
            case "Sửa chữa" -> "Số phiếu sửa chữa";
            case "Khách hàng" -> "Khách hàng sử dụng dịch vụ";
            case "Nhân viên" -> "Nhân viên được phân công";
            case "Phụ tùng" -> "Số lượng phụ tùng đã xuất";
            case "Công nợ" -> "Công nợ còn lại của hóa đơn trong kỳ";
            case "Kho" -> "Số lượng phụ tùng nhập kho";
            default -> throw new IllegalArgumentException("Nhóm báo cáo không hợp lệ: " + nhom);
        };
    }

    private void capNhatTheTongHop(List<ChiTieuBaoCao> chiTieu) {
        String[] tieuChiCanHienThi = {
                "Tổng doanh thu", "Số hóa đơn", "Số phiếu sửa chữa",
                "Khách hàng sử dụng dịch vụ", "Nhân viên được phân công",
                "Số lượng phụ tùng đã xuất", "Công nợ còn lại của hóa đơn trong kỳ",
                "Số lượng phụ tùng nhập kho", "Số lượng phụ tùng xuất kho"
        };
        String[] nhanThe = {
                "Doanh thu", "Hóa đơn", "Phiếu sửa chữa",
                "Khách hàng", "Nhân viên", "Phụ tùng bán",
                "Công nợ", "Nhập kho", "Xuất kho"
        };
        for (int index = 0; index < tieuChiCanHienThi.length; index++) {
            String tenChiTieu = tieuChiCanHienThi[index];
            ChiTieuBaoCao giaTri = chiTieu.stream()
                    .filter(ct -> ct.chiTieu().equals(tenChiTieu))
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
                if (column > 0)
                    writer.write(',');
                writer.write(csv(modelBaoCao.getColumnName(column)));
            }
            writer.newLine();
            for (int row = 0; row < modelBaoCao.getRowCount(); row++) {
                for (int column = 0; column < modelBaoCao.getColumnCount(); column++) {
                    if (column > 0)
                        writer.write(',');
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

    // ── Lớp nội: vẽ biểu đồ đường ────────────────────────────────────────────
    private static final class BieuDoDuong extends JComponent {
        private static final int PADDING_LEFT = 64;
        private static final int PADDING_RIGHT = 28;
        private static final int PADDING_TOP = 42;
        private static final int PADDING_BOTTOM = 54;
        private static final Color[] MAU_DUONG = {
                new Color(37, 99, 235),
                new Color(22, 163, 74),
                new Color(217, 119, 6)
        };
        private String[] nhan = new String[0];
        private String[] tenDuong = new String[0];
        private double[][] giaTri = new double[0][];

        private BieuDoDuong() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 300));
        }

        private void setData(String[] nhan, String[] tenDuong, double[][] giaTri) {
            this.nhan = Arrays.copyOf(nhan, nhan.length);
            this.tenDuong = Arrays.copyOf(tenDuong, tenDuong.length);
            this.giaTri = Arrays.stream(giaTri)
                    .map(duong -> Arrays.copyOf(duong, duong.length))
                    .toArray(double[][]::new);
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
                int width  = getWidth();
                int height = getHeight();
                int chartWidth  = width  - PADDING_LEFT - PADDING_RIGHT;
                int chartHeight = height - PADDING_TOP  - PADDING_BOTTOM;
                if (chartWidth <= 0 || chartHeight <= 0) return;

                if (nhan.length == 0 || giaTri.length == 0) {
                    g2.setColor(UIConfig.COLOR_MUTED);
                    String message = "Chưa có dữ liệu biểu đồ";
                    FontMetrics metrics = g2.getFontMetrics();
                    g2.drawString(message,
                            (width  - metrics.stringWidth(message)) / 2,
                            (height + metrics.getAscent()) / 2);
                    return;
                }

                double giaTriLonNhat = Arrays.stream(giaTri)
                        .flatMapToDouble(Arrays::stream).max().orElse(0);
                double mocTruc  = giaTriLonNhat == 0 ? 1 : giaTriLonNhat * 1.1;
                int soDuongLuoi = 4;
                for (int moc = 0; moc <= soDuongLuoi; moc++) {
                    int y = PADDING_TOP + chartHeight - chartHeight * moc / soDuongLuoi;
                    double giaTriTruc = mocTruc * moc / soDuongLuoi;
                    g2.setColor(UIConfig.COLOR_TABLE_GRID);
                    g2.drawLine(PADDING_LEFT, y, PADDING_LEFT + chartWidth, y);
                    g2.setColor(UIConfig.COLOR_MUTED);
                    String text = String.format(Locale.forLanguageTag("vi-VN"), "%,.0f", giaTriTruc);
                    FontMetrics metrics = g2.getFontMetrics();
                    g2.drawString(text, PADDING_LEFT - metrics.stringWidth(text) - 8, y + 4);
                }

                FontMetrics metrics = g2.getFontMetrics();
                int viTriChuThich = PADDING_LEFT;
                for (int duong = 0; duong < tenDuong.length; duong++) {
                    Color mauDuong = MAU_DUONG[duong % MAU_DUONG.length];
                    g2.setColor(mauDuong);
                    g2.setStroke(new java.awt.BasicStroke(2.5f));
                    g2.drawLine(viTriChuThich, 18, viTriChuThich + 20, 18);
                    g2.fillOval(viTriChuThich + 8, 14, 8, 8);
                    g2.setColor(UIConfig.COLOR_TEXT);
                    String ten = tenDuong[duong];
                    g2.drawString(ten, viTriChuThich + 26, 22);
                    viTriChuThich += 34 + metrics.stringWidth(ten);
                }

                int buocNhan = Math.max(1, (int) Math.ceil((double) nhan.length / 10));
                for (int duong = 0; duong < giaTri.length; duong++) {
                    g2.setColor(MAU_DUONG[duong % MAU_DUONG.length]);
                    g2.setStroke(new java.awt.BasicStroke(2.5f));
                    int xTruoc = -1, yTruoc = -1;
                    for (int diem = 0; diem < nhan.length; diem++) {
                        int x = nhan.length == 1
                                ? PADDING_LEFT + chartWidth / 2
                                : PADDING_LEFT + (int) ((long) diem * chartWidth / (nhan.length - 1));
                        int y = PADDING_TOP + chartHeight
                                - (int) Math.round(giaTri[duong][diem] / mocTruc * chartHeight);
                        if (xTruoc >= 0) g2.drawLine(xTruoc, yTruoc, x, y);
                        g2.fillOval(x - 4, y - 4, 8, 8);
                        xTruoc = x;
                        yTruoc = y;
                    }
                }

                g2.setColor(UIConfig.COLOR_BORDER);
                g2.drawLine(PADDING_LEFT, PADDING_TOP, PADDING_LEFT, PADDING_TOP + chartHeight);
                g2.drawLine(PADDING_LEFT, PADDING_TOP + chartHeight,
                        PADDING_LEFT + chartWidth, PADDING_TOP + chartHeight);
                g2.setColor(UIConfig.COLOR_MUTED);
                for (int index = 0; index < nhan.length; index += buocNhan) {
                    veNhanTrucX(g2, nhan[index], index, chartWidth, metrics);
                }
                if ((nhan.length - 1) % buocNhan != 0) {
                    veNhanTrucX(g2, nhan[nhan.length - 1], nhan.length - 1, chartWidth, metrics);
                }
            } finally {
                g2.dispose();
            }
        }

        private void veNhanTrucX(
                Graphics2D g2, String nhanMoc, int index, int chartWidth, FontMetrics metrics) {
            int x = nhan.length == 1
                    ? PADDING_LEFT + chartWidth / 2
                    : PADDING_LEFT + (int) ((long) index * chartWidth / (nhan.length - 1));
            int xText = Math.max(PADDING_LEFT, Math.min(
                    x - metrics.stringWidth(nhanMoc) / 2,
                    PADDING_LEFT + chartWidth - metrics.stringWidth(nhanMoc)));
            g2.drawString(nhanMoc, xText, getHeight() - 18);
        }
    }
}

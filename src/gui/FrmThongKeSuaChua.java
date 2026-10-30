package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import dao.BaoCaoSuaChuaDAO;
import dao.BaoCaoSuaChuaDAO.NhanVienThongKe;
import dao.BaoCaoSuaChuaDAO.ThongKePhieuSuaChua;
import dao.BaoCaoSuaChuaDAO.ThongKeTongQuan;

public class FrmThongKeSuaChua extends JPanel {
    private final AppTextField txtTuNgay = new AppTextField(12);
    private final AppTextField txtDenNgay = new AppTextField(12);
    private final AppComboBox<String> cboTrangThai = new AppComboBox<>();
    private final AppComboBox<String> cboNhanVien = new AppComboBox<>();
    private final AppButton btnThongKe = new AppButton("Thống kê");
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnXuatBaoCao = new AppButton("Xuất báo cáo", AppButton.Style.SECONDARY);

    private final AppLabel lblTongPhieu = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDaHoanThanh = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDangXuLy = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDoanhThu = new AppLabel("—", AppLabel.Style.TITLE);
    private final BieuDoTron pnlBieuDoTrangThai = new BieuDoTron();
    private final BieuDoDuong pnlBieuDoTheoThang = new BieuDoDuong();
    private final AppLabel lblTrangThai = new AppLabel(
            "Chọn điều kiện rồi nhấn Thống kê", AppLabel.Style.MUTED);
    private final AppLabel lblSoDong = new AppLabel("0 phiếu", AppLabel.Style.MUTED);

    private final DefaultTableModel modelThongKe = new DefaultTableModel(
            new Object[] { "Mã phiếu", "Mã xe", "Khách hàng", "Nhân viên",
                    "Ngày bắt đầu", "Trạng thái", "Tổng tiền" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tblThongKe = FormUI.createTable(modelThongKe);

    private final BaoCaoSuaChuaDAO baoCaoSuaChuaDao = new BaoCaoSuaChuaDAO();
    private final List<NhanVienThongKe> danhSachNhanVien = new ArrayList<>();
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ISO_LOCAL_DATE;

    public FrmThongKeSuaChua() {
        setOpaque(false);
        setLayout(new BorderLayout());

        cboTrangThai.addItem("Tất cả trạng thái");
        cboTrangThai.addItem("Chờ sửa");
        cboTrangThai.addItem("Đang sửa");
        cboTrangThai.addItem("Hoàn thành");
        cboTrangThai.addItem("Đã hủy");
        cboNhanVien.addItem("Tất cả nhân viên");

        LocalDate homNay = LocalDate.now();
        txtTuNgay.setText(homNay.withDayOfMonth(1).toString());
        txtDenNgay.setText(homNay.toString());
        txtTuNgay.setToolTipText("Định dạng ngày: yyyy-MM-dd");
        txtDenNgay.setToolTipText("Định dạng ngày: yyyy-MM-dd");
        ganSuKien();
        napDanhSachNhanVien();

        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFilterCard(), BorderLayout.NORTH);

        JPanel pnlReportContent = new JPanel(new BorderLayout(0, 14));
        pnlReportContent.setOpaque(false);
        pnlReportContent.add(createSummaryCards(), BorderLayout.NORTH);

        JPanel pnlDetails = new JPanel(new BorderLayout(0, 14));
        pnlDetails.setOpaque(false);
        pnlDetails.add(createChartPlaceholders(), BorderLayout.NORTH);
        AppScrollPane scrChiTiet = new AppScrollPane(tblThongKe);
        scrChiTiet.setPreferredSize(new Dimension(0, 230));
        scrChiTiet.setMinimumSize(new Dimension(0, 180));
        pnlDetails.add(FormUI.createTableCard("Chi tiết phiếu sửa chữa", lblSoDong,
                scrChiTiet), BorderLayout.CENTER);
        pnlReportContent.add(pnlDetails, BorderLayout.CENTER);
        pnlContent.add(pnlReportContent, BorderLayout.CENTER);

        JScrollPane scrNoiDung = new JScrollPane(pnlContent);
        scrNoiDung.setBorder(null);
        scrNoiDung.setOpaque(false);
        scrNoiDung.getViewport().setOpaque(false);
        scrNoiDung.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrNoiDung.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrNoiDung.setWheelScrollingEnabled(false);
        scrNoiDung.addMouseWheelListener(event -> cuonNhanh(scrNoiDung, event));
        scrNoiDung.getViewport().addMouseWheelListener(
                event -> cuonNhanh(scrNoiDung, event));
        add(FormUI.createPage("THỐNG KÊ SỬA CHỮA",
                "Theo dõi tình trạng phiếu sửa chữa, doanh thu và hiệu suất xử lý",
                scrNoiDung), BorderLayout.CENTER);
        add(lblTrangThai, BorderLayout.SOUTH);
        tblThongKe.setRowHeight(32);
        tblThongKe.setFillsViewportHeight(true);
        thongKe();
    }

    private void cuonNhanh(JScrollPane scrNoiDung, MouseWheelEvent event) {
        event.consume();
        Point viTriHienTai = scrNoiDung.getViewport().getViewPosition();
        int doCaoNoiDung = scrNoiDung.getViewport().getView().getHeight();
        int doCaoKhungNhin = scrNoiDung.getViewport().getHeight();
        int buocCuon = event.getUnitsToScroll() * 36;
        int viTriMoi = Math.max(0, Math.min(
                viTriHienTai.y + buocCuon,
                Math.max(0, doCaoNoiDung - doCaoKhungNhin)));
        scrNoiDung.getViewport().setViewPosition(
                new Point(viTriHienTai.x, viTriMoi));
    }

    private void ganSuKien() {
        btnThongKe.addActionListener(event -> thongKe());
        btnLamMoi.addActionListener(event -> lamMoi());
        btnXuatBaoCao.addActionListener(event -> xuatBaoCao());
    }

    private void napDanhSachNhanVien() {
        try {
            danhSachNhanVien.clear();
            danhSachNhanVien.addAll(baoCaoSuaChuaDao.layDanhSachNhanVien());
            for (NhanVienThongKe nhanVien : danhSachNhanVien) {
                cboNhanVien.addItem(nhanVien.maNV() + " - " + nhanVien.hoTen());
            }
        } catch (Exception exception) {
            hienThiLoi("Không thể tải danh sách nhân viên: " + exception.getMessage());
        }
    }

    private void thongKe() {
        try {
            LocalDate tuNgay = docNgay(txtTuNgay.getText(), "Từ ngày");
            LocalDate denNgay = docNgay(txtDenNgay.getText(), "Đến ngày");
            if (denNgay.isBefore(tuNgay)) {
                throw new IllegalArgumentException("Đến ngày phải bằng hoặc sau Từ ngày.");
            }

            String trangThai = (String) cboTrangThai.getSelectedItem();
            String maNV = layMaNhanVienDangChon();
            ThongKeTongQuan tongQuan = baoCaoSuaChuaDao.layTongQuan(
                    tuNgay, denNgay, trangThai, maNV);

            capNhatTongQuan(tongQuan);
            capNhatBang(baoCaoSuaChuaDao.layChiTiet(tuNgay, denNgay, trangThai, maNV));
            capNhatBieuDo(tuNgay, denNgay, trangThai, maNV);
            lblTrangThai.setText("Đã cập nhật thống kê.");
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (Exception exception) {
            hienThiLoi("Không thể tải thống kê sửa chữa: " + exception.getMessage());
        }
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

    private String layMaNhanVienDangChon() {
        int viTri = cboNhanVien.getSelectedIndex();
        if (viTri <= 0 || viTri > danhSachNhanVien.size()) {
            return null;
        }
        return danhSachNhanVien.get(viTri - 1).maNV();
    }

    private void capNhatTongQuan(ThongKeTongQuan tongQuan) {
        lblTongPhieu.setText(String.valueOf(tongQuan.tongPhieu()));
        lblDaHoanThanh.setText(String.valueOf(tongQuan.daHoanThanh()));
        lblDangXuLy.setText(String.valueOf(tongQuan.dangXuLy()));
        lblDoanhThu.setText(dinhDangTien(tongQuan.doanhThu()));
    }

    private void capNhatBang(List<ThongKePhieuSuaChua> danhSach) {
        modelThongKe.setRowCount(0);
        for (ThongKePhieuSuaChua phieu : danhSach) {
            modelThongKe.addRow(new Object[] {
                    phieu.maPhieuSuaChua(),
                    phieu.maXe(),
                    phieu.tenKhachHang(),
                    phieu.tenNhanVien(),
                    phieu.ngayBatDau(),
                    phieu.trangThai(),
                    dinhDangTien(phieu.tongTien())
            });
        }
        lblSoDong.setText(danhSach.size() + " phiếu");
    }

    private void capNhatBieuDo(LocalDate tuNgay, LocalDate denNgay, String trangThai, String maNV) throws SQLException {
        List<BaoCaoSuaChuaDAO.ThongKeTrangThai> danhSachTrangThai = baoCaoSuaChuaDao.layTheoTrangThai(
                tuNgay, denNgay, trangThai, maNV);
        String[] nhanTrangThai = new String[danhSachTrangThai.size()];
        double[] giaTriTrangThai = new double[danhSachTrangThai.size()];
        for (int i = 0; i < danhSachTrangThai.size(); i++) {
            BaoCaoSuaChuaDAO.ThongKeTrangThai item = danhSachTrangThai.get(i);
            nhanTrangThai[i] = item.trangThai();
            giaTriTrangThai[i] = item.soLuong();
        }
        pnlBieuDoTrangThai.setData(nhanTrangThai, giaTriTrangThai);

        List<BaoCaoSuaChuaDAO.ThongKeTheoThang> danhSachTheoThang = baoCaoSuaChuaDao.layTheoThang(tuNgay, denNgay,
                trangThai, maNV);
        List<BaoCaoSuaChuaDAO.ThongKeTheoThang> danhSachTheoThangDayDu = taoDanhSachTheoThangDayDu(
                tuNgay, denNgay, danhSachTheoThang);

        String[] nhanTheoThang = new String[danhSachTheoThangDayDu.size()];
        double[] giaTriTheoThang = new double[danhSachTheoThangDayDu.size()];
        for (int i = 0; i < danhSachTheoThangDayDu.size(); i++) {
            BaoCaoSuaChuaDAO.ThongKeTheoThang item = danhSachTheoThangDayDu.get(i);
            nhanTheoThang[i] = item.thang() + "/" + item.nam();
            giaTriTheoThang[i] = item.soLuong();
        }
        pnlBieuDoTheoThang.setData(nhanTheoThang, giaTriTheoThang);
    }

    private List<BaoCaoSuaChuaDAO.ThongKeTheoThang> taoDanhSachTheoThangDayDu(
            LocalDate tuNgay, LocalDate denNgay,
            List<BaoCaoSuaChuaDAO.ThongKeTheoThang> danhSachTheoThang) {
        List<BaoCaoSuaChuaDAO.ThongKeTheoThang> ketQua = new ArrayList<>();
        LocalDate thangHienTai = tuNgay.withDayOfMonth(1);
        LocalDate thangCuoi = denNgay.withDayOfMonth(1);

        while (!thangHienTai.isAfter(thangCuoi)) {
            int nam = thangHienTai.getYear();
            int thang = thangHienTai.getMonthValue();
            int soLuong = 0;

            for (BaoCaoSuaChuaDAO.ThongKeTheoThang item : danhSachTheoThang) {
                if (item.nam() == nam && item.thang() == thang) {
                    soLuong = item.soLuong();
                    break;
                }
            }

            ketQua.add(new BaoCaoSuaChuaDAO.ThongKeTheoThang(nam, thang, soLuong));
            thangHienTai = thangHienTai.plusMonths(1);
        }
        return ketQua;
    }

    private void lamMoi() {
        LocalDate homNay = LocalDate.now();
        txtTuNgay.setText(homNay.withDayOfMonth(1).toString());
        txtDenNgay.setText(homNay.toString());
        cboTrangThai.setSelectedIndex(0);
        cboNhanVien.setSelectedIndex(0);
        modelThongKe.setRowCount(0);
        lblSoDong.setText("0 phiếu");
        lblTongPhieu.setText("—");
        lblDaHoanThanh.setText("—");
        lblDangXuLy.setText("—");
        lblDoanhThu.setText("—");
        pnlBieuDoTrangThai.setData(new String[0], new double[0]);
        pnlBieuDoTheoThang.setData(new String[0], new double[0]);
        lblTrangThai.setText("Đã làm mới điều kiện thống kê.");
    }

    private void xuatBaoCao() {
        if (modelThongKe.getRowCount() == 0) {
            hienThiLoi("Chưa có dữ liệu để xuất. Vui lòng thực hiện thống kê trước.");
            return;
        }

        JFileChooser boChonTep = new JFileChooser();
        boChonTep.setDialogTitle("Lưu báo cáo sửa chữa");
        boChonTep.setSelectedFile(new java.io.File("bao-cao-sua-chua.csv"));
        boChonTep.setFileFilter(new FileNameExtensionFilter("Tệp CSV (*.csv)", "csv"));
        if (boChonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path duongDan = boChonTep.getSelectedFile().toPath();
        try (BufferedWriter writer = Files.newBufferedWriter(duongDan, StandardCharsets.UTF_8)) {
            for (int column = 0; column < modelThongKe.getColumnCount(); column++) {
                if (column > 0) {
                    writer.write(",");
                }
                writer.write("\"" + modelThongKe.getColumnName(column) + "\"");
            }
            writer.newLine();
            for (int row = 0; row < modelThongKe.getRowCount(); row++) {
                for (int column = 0; column < modelThongKe.getColumnCount(); column++) {
                    if (column > 0) {
                        writer.write(",");
                    }
                    writer.write("\"" + String.valueOf(modelThongKe.getValueAt(row, column))
                            .replace("\"", "\"\"") + "\"");
                }
                writer.newLine();
            }
            lblTrangThai.setText("Đã xuất báo cáo: " + duongDan);
        } catch (IOException exception) {
            hienThiLoi("Không thể xuất báo cáo: " + exception.getMessage());
        }
    }

    private String dinhDangTien(BigDecimal giaTri) {
        return giaTri == null ? "0 VNĐ" : String.format("%,.0f VNĐ", giaTri);
    }

    private void hienThiLoi(String thongBao) {
        JOptionPane.showMessageDialog(this, thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
        lblTrangThai.setText("Có lỗi khi xử lý thống kê.");
    }

    private JPanel createFilterCard() {
        AppPanel pnlCard = FormUI.createCard();
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Từ ngày", txtTuNgay, 0, 0);
        FormUI.addField(pnlFields, "Đến ngày", txtDenNgay, 2, 0);
        FormUI.addField(pnlFields, "Trạng thái", cboTrangThai, 0, 1);
        FormUI.addField(pnlFields, "Nhân viên", cboNhanVien, 2, 1);

        JPanel pnlButtons = FormUI.createButtonRow(btnThongKe, btnLamMoi, btnXuatBaoCao);
        pnlCard.add(FormUI.createSectionTitle("Điều kiện thống kê"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createSummaryCards() {
        JPanel pnlSummary = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlSummary.setOpaque(false);
        pnlSummary.add(createMetricCard("Tổng phiếu sửa chữa", lblTongPhieu));
        pnlSummary.add(createMetricCard("Đã hoàn thành", lblDaHoanThanh));
        pnlSummary.add(createMetricCard("Đang xử lý", lblDangXuLy));
        pnlSummary.add(createMetricCard("Tổng doanh thu", lblDoanhThu));
        return pnlSummary;
    }

    private JPanel createMetricCard(String title, AppLabel lblValue) {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.add(new AppLabel(title, AppLabel.Style.MUTED), BorderLayout.NORTH);
        pnlCard.add(lblValue, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel createChartPlaceholders() {
        JPanel pnlCharts = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlCharts.setOpaque(false);
        pnlCharts.add(createChartCard(
                "TỶ lệ phiếu theo trạng thái",
                pnlBieuDoTrangThai));
        pnlCharts.add(createChartCard(
                "Số phiếu theo tháng",
                pnlBieuDoTheoThang));
        return pnlCharts;
    }

    private JPanel createChartCard(String title, JComponent chart) {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.setPreferredSize(new Dimension(0, 250));
        pnlCard.add(FormUI.createSectionTitle(title));
        pnlCard.add(chart, BorderLayout.CENTER);
        return pnlCard;
    }

    private static final class BieuDoTron extends JComponent {
        private String[] nhan = new String[0];
        private double[] giaTri = new double[0];
        private final Color[] color = {
                new Color(37, 99, 235),
                new Color(22, 163, 74),
                new Color(217, 119, 6),
                new Color(220, 38, 38),
                new Color(124, 58, 237)
        };

        private BieuDoTron() {
            setOpaque(true);
            setBackground(Color.WHITE);
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
                if (giaTri.length == 0) {
                    veThongBaoRong(g2);
                    return;
                }
                double tong = Arrays.stream(giaTri).sum();
                if (tong <= 0) {
                    veThongBaoRong(g2);
                    return;
                }
                int size = Math.min(getHeight() - 24, Math.min(getWidth() / 2, 190));
                int x = 20;
                int y = (getHeight() - size) / 2;
                int gocBanDau = 0;

                for (int i = 0; i < giaTri.length; i++) {
                    int gocCung = (int) Math.round(giaTri[i] / tong * 360);
                    g2.setColor(color[i % color.length]);
                    g2.fillArc(x, y, size, size, gocBanDau, gocCung);
                    gocBanDau += gocCung;
                }
                int xChuThich = size + 45;
                int yChuThich = 30;
                for (int i = 0; i < nhan.length; i++) {
                    double phanTram = giaTri[i] / tong * 100;
                    g2.setColor(color[i % color.length]);
                    g2.fillRect(xChuThich, yChuThich - 11, 12, 12);
                    g2.setColor(UIConfig.COLOR_TEXT);
                    g2.drawString(nhan[i] + " - " + String.format("%.1f%%", phanTram),
                            xChuThich + 18,
                            yChuThich);
                    yChuThich += 25;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                g2.dispose();
            }
        }

        private void veThongBaoRong(Graphics2D g2) {
            String thongBao = "Chưa có dữ liệu";
            FontMetrics metrics = g2.getFontMetrics();
            g2.setColor(UIConfig.COLOR_MUTED);
            g2.drawString(thongBao, (getWidth() - metrics.stringWidth(thongBao)) / 2, getHeight() / 2);
        }
    }

    private static final class BieuDoDuong extends JComponent {
        private String[] nhan = new String[0];
        private double[] giaTri = new double[0];

        private BieuDoDuong() {
            setOpaque(true);
            setBackground(Color.WHITE);
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

                int paddingLeft = 42;
                int paddingRight = 20;
                int paddingTop = 18;
                int paddingBottom = 38;

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

                    String nhanHienTai = nhan[i];
                    int chieuRongNhan = g2.getFontMetrics().stringWidth(nhanHienTai);
                    g2.drawString(nhanHienTai, toaDoX[i] - chieuRongNhan / 2, getHeight() - 12);
                }
            } finally {
                g2.dispose();
            }
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
                g2.drawString(String.valueOf((int) giaTriThuc), 8, y + 4);
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

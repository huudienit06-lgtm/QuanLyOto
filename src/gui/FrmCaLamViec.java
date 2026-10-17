package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseWheelEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FrmCaLamViec extends JPanel {
    private static final String CHUA_PHAN_CONG = "Chưa phân công";
    private static final LocalTime[] GIO_BAT_DAU_CA = {
            LocalTime.of(7, 0), LocalTime.of(13, 0), LocalTime.of(17, 0)
    };
    private static final LocalTime[] GIO_KET_THUC_CA = {
            LocalTime.of(12, 0), LocalTime.of(17, 0), LocalTime.of(21, 0)
    };
    private static final String[] TEN_CA = {"Ca sáng", "Ca chiều", "Ca tối"};
    private static final String[] TEN_THU = {
            "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"
    };
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DefaultTableModel modelLichTuan = new DefaultTableModel(
            new Object[] {"Ca làm việc", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5",
                    "Thứ 6", "Thứ 7", "Chủ nhật"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel modelDiemDanh = new DefaultTableModel(
            new Object[] {"Mã nhân viên", "Họ và tên", "Giờ vào", "Có mặt", "Ghi chú"}, 0) {
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 3 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 3;
        }
    };
    private final JTable tblLichTuan = FormUI.createTable(modelLichTuan);
    private final JTable tblDiemDanh = FormUI.createTable(modelDiemDanh);
    private final AppComboBox<String> cboNhanVien = new AppComboBox<>();
    private final AppComboBox<String> cboCaLamViec = new AppComboBox<>();
    private final DatePickerPanel pnlChonNgayLamViec = new DatePickerPanel(
            LocalDate.now(), this::chonNgayLamViec);
    private final JLabel lblCaHienTai = new AppLabel("", AppLabel.Style.BODY);
    private final JLabel lblTrangThaiDiemDanh = new AppLabel("", AppLabel.Style.MUTED);
    private final JLabel lblTuanHienThi = new AppLabel("", AppLabel.Style.MUTED);
    private final LichCaCellRenderer rendererLichCa = new LichCaCellRenderer();
    private final Timer timerCapNhatThoiGian = new Timer(30_000, event -> capNhatCaHienTai());
    private AppScrollPane scrNoiDung;
    private int caHienTai = -1;
    private int cotNgayHienTai = -1;
    private LocalDate ngayDauTuan = LocalDate.now().with(DayOfWeek.MONDAY);

    public FrmCaLamViec() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);

        napKhungCa();
        cauHinhLuaChon();
        cauHinhBangLich();
        cauHinhBangDiemDanh();
        capNhatTieuDeCotNgay();
        capNhatTieuDeNgay();
        capNhatCaHienTai();

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(createFormPhanCong(), BorderLayout.NORTH);
        pnlNoiDung.add(createLichTuanCard(), BorderLayout.CENTER);
        pnlNoiDung.add(createDiemDanhCard(), BorderLayout.SOUTH);

        scrNoiDung = new AppScrollPane(pnlNoiDung);
        add(FormUI.createPage("LỊCH CA LÀM VIỆC",
                "Theo dõi lịch tuần, ca đang diễn ra và điểm danh nhân viên", scrNoiDung),
                BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        capNhatCaHienTai();
        timerCapNhatThoiGian.start();
    }

    @Override
    public void removeNotify() {
        timerCapNhatThoiGian.stop();
        super.removeNotify();
    }

    private JPanel createFormPhanCong() {
        AppPanel pnlPhanCong = FormUI.createCard();
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Nhân viên", cboNhanVien, 0, 0);
        FormUI.addField(pnlFields, "Ca làm việc", cboCaLamViec, 2, 0);
        FormUI.addField(pnlFields, "Ngày làm việc", pnlChonNgayLamViec, 0, 1);

        JPanel pnlButtons = FormUI.createButtonRow(
                new AppButton("Thêm phân công"),
                new AppButton("Sửa phân công", AppButton.Style.SUCCESS),
                new AppButton("Xóa phân công", AppButton.Style.DANGER),
                new AppButton("Làm mới", AppButton.Style.SECONDARY));
        pnlPhanCong.add(FormUI.createSectionTitle("Thông tin phân công ca"), BorderLayout.NORTH);
        pnlPhanCong.add(pnlFields, BorderLayout.CENTER);
        pnlPhanCong.add(pnlButtons, BorderLayout.SOUTH);
        return pnlPhanCong;
    }

    private JPanel createLichTuanCard() {
        AppButton btnTuanTruoc = new AppButton("‹ Tuần trước", AppButton.Style.SECONDARY);
        AppButton btnTuanKeTiep = new AppButton("Tuần kế tiếp ›", AppButton.Style.SECONDARY);
        btnTuanTruoc.addActionListener(event -> dichTuan(-1));
        btnTuanKeTiep.addActionListener(event -> dichTuan(1));
        JPanel pnlDieuHuong = FormUI.createButtonRow(btnTuanTruoc, btnTuanKeTiep);

        JPanel pnlLich = FormUI.createTableCard("Lịch nhân viên theo ca trong tuần",
                pnlDieuHuong, new AppScrollPane(tblLichTuan));

        JPanel pnlInfo = new JPanel(new BorderLayout(8, 0));
        pnlInfo.setOpaque(false);
        pnlInfo.add(lblTuanHienThi, BorderLayout.WEST);
        lblCaHienTai.setFont(UIConfig.FONT_BOLD);
        pnlInfo.add(lblCaHienTai, BorderLayout.EAST);
        pnlLich.add(pnlInfo, BorderLayout.SOUTH);
        return pnlLich;
    }

    private void cauHinhLuaChon() {
        cboNhanVien.addItem("Chọn nhân viên");
        cboCaLamViec.addItem("Chọn ca làm việc");
        for (int index = 0; index < TEN_CA.length; index++) {
            cboCaLamViec.addItem(TEN_CA[index] + " ("
                    + GIO_BAT_DAU_CA[index].format(DateTimeFormatter.ofPattern("HH:mm")) + " - "
                    + GIO_KET_THUC_CA[index].format(DateTimeFormatter.ofPattern("HH:mm")) + ")");
        }
    }

    private void chonNgayLamViec(LocalDate ngayChon) {
        LocalDate dauTuanChon = ngayChon.with(DayOfWeek.MONDAY);
        if (!dauTuanChon.equals(ngayDauTuan)) {
            ngayDauTuan = dauTuanChon;
            capNhatTuan(ngayChon);
        }
    }

    private JPanel createDiemDanhCard() {
        AppPanel pnlDiemDanh = FormUI.createCard();
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.add(FormUI.createSectionTitle("Điểm danh nhân viên trong ca hiện tại"), BorderLayout.WEST);
        pnlHeader.add(lblTrangThaiDiemDanh, BorderLayout.EAST);

        AppScrollPane scrDiemDanh = new AppScrollPane(tblDiemDanh);
        scrDiemDanh.setPreferredSize(new java.awt.Dimension(700, 112));
        pnlDiemDanh.add(pnlHeader, BorderLayout.NORTH);
        pnlDiemDanh.add(scrDiemDanh, BorderLayout.CENTER);
        return pnlDiemDanh;
    }

    private void napKhungCa() {
        for (int index = 0; index < TEN_CA.length; index++) {
            String khungGio = GIO_BAT_DAU_CA[index].format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " - " + GIO_KET_THUC_CA[index].format(DateTimeFormatter.ofPattern("HH:mm"));
            modelLichTuan.addRow(new Object[] {
                    TEN_CA[index] + "\n" + khungGio,
                    CHUA_PHAN_CONG, CHUA_PHAN_CONG, CHUA_PHAN_CONG, CHUA_PHAN_CONG,
                    CHUA_PHAN_CONG, CHUA_PHAN_CONG, CHUA_PHAN_CONG
            });
        }
    }

    private void cauHinhBangLich() {
        tblLichTuan.setAutoCreateRowSorter(false);
        tblLichTuan.setRowHeight(62);
        tblLichTuan.setShowVerticalLines(true);
        tblLichTuan.setShowHorizontalLines(true);
        tblLichTuan.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblLichTuan.setFillsViewportHeight(true);
        tblLichTuan.setDefaultRenderer(Object.class, rendererLichCa);
        tblLichTuan.getColumnModel().getColumn(0).setPreferredWidth(145);
        tblLichTuan.getColumnModel().getColumn(0).setMinWidth(130);
        for (int column = 1; column < tblLichTuan.getColumnCount(); column++) {
            tblLichTuan.getColumnModel().getColumn(column).setPreferredWidth(135);
            tblLichTuan.getColumnModel().getColumn(column).setMinWidth(105);
        }
        tblLichTuan.getTableHeader().setReorderingAllowed(false);
        tblLichTuan.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));
        tblLichTuan.addMouseWheelListener(event -> chuyenCuonRaTrangNeuCan(tblLichTuan, event));
    }

    private void dichTuan(int soTuan) {
        int ngayTrongTuan = pnlChonNgayLamViec.getSelectedDate().getDayOfWeek().getValue() - 1;
        ngayDauTuan = ngayDauTuan.plusWeeks(soTuan);
        capNhatTuan(ngayDauTuan.plusDays(ngayTrongTuan));
    }

    private void capNhatTuan(LocalDate ngayDuocChon) {
        capNhatTieuDeCotNgay();
        cauHinhBangLich();
        pnlChonNgayLamViec.setSelectedDate(ngayDuocChon);
        capNhatTieuDeNgay();
        capNhatCaHienTai();
        tblLichTuan.repaint();
    }

    private void capNhatTieuDeCotNgay() {
        Object[] tieuDeCot = new Object[8];
        tieuDeCot[0] = "Ca làm việc";
        for (int index = 0; index < TEN_THU.length; index++) {
            LocalDate ngay = ngayDauTuan.plusDays(index);
            tieuDeCot[index + 1] = "<html><center>" + TEN_THU[index] + "<br>"
                    + ngay.format(DINH_DANG_NGAY) + "</center></html>";
        }
        modelLichTuan.setColumnIdentifiers(tieuDeCot);
    }

    private void capNhatTieuDeNgay() {
        lblTuanHienThi.setText("Tuần: " + ngayDauTuan.format(DINH_DANG_NGAY) + " - "
                + ngayDauTuan.plusDays(6).format(DINH_DANG_NGAY));
    }

    private void cauHinhBangDiemDanh() {
        tblDiemDanh.setRowHeight(28);
        tblDiemDanh.setFillsViewportHeight(true);
        tblDiemDanh.getTableHeader().setReorderingAllowed(false);
        tblDiemDanh.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 32));
        tblDiemDanh.getColumnModel().getColumn(0).setPreferredWidth(115);
        tblDiemDanh.getColumnModel().getColumn(1).setPreferredWidth(210);
        tblDiemDanh.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblDiemDanh.getColumnModel().getColumn(3).setPreferredWidth(85);
        tblDiemDanh.getColumnModel().getColumn(4).setPreferredWidth(180);
        tblDiemDanh.addMouseWheelListener(event -> chuyenCuonRaTrangNeuCan(tblDiemDanh, event));
    }

    private void chuyenCuonRaTrangNeuCan(JTable tblNguon, MouseWheelEvent event) {
        if (event.isConsumed() || scrNoiDung == null) {
            return;
        }

        JScrollPane scrBang = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tblNguon);
        if (scrBang != null && coTheCuon(scrBang.getVerticalScrollBar(), event.getWheelRotation())) {
            return;
        }

        JScrollBar scrDocTrang = scrNoiDung.getVerticalScrollBar();
        int huong = Integer.signum(event.getWheelRotation());
        if (!coTheCuon(scrDocTrang, huong)) {
            return;
        }

        int buocCuon = scrDocTrang.getUnitIncrement(huong);
        int delta = event.getUnitsToScroll() * buocCuon;
        scrDocTrang.setValue(scrDocTrang.getValue() + delta);
        event.consume();
    }

    private boolean coTheCuon(JScrollBar scrBar, int huong) {
        if (huong > 0) {
            return scrBar.getValue() + scrBar.getVisibleAmount() < scrBar.getMaximum();
        }
        if (huong < 0) {
            return scrBar.getValue() > scrBar.getMinimum();
        }
        return false;
    }

    private void capNhatCaHienTai() {
        LocalTime gioHienTai = LocalTime.now();
        caHienTai = timCaHienTai(gioHienTai);
        LocalDate homNay = LocalDate.now();
        cotNgayHienTai = homNay.with(DayOfWeek.MONDAY).equals(ngayDauTuan)
                ? homNay.getDayOfWeek().getValue()
                : -1;
        rendererLichCa.setCaHienTai(cotNgayHienTai >= 0 ? caHienTai : -1, cotNgayHienTai);
        tblLichTuan.repaint();

        String ngayHienTai = homNay.format(DINH_DANG_NGAY);
        String thoiGian = gioHienTai.format(DateTimeFormatter.ofPattern("HH:mm"));
        if (caHienTai >= 0) {
            lblCaHienTai.setText("Hôm nay " + ngayHienTai + "  |  " + thoiGian
                    + "  |  ĐANG DIỄN RA: " + TEN_CA[caHienTai]);
            lblCaHienTai.setForeground(UIConfig.COLOR_SUCCESS);
            lblTrangThaiDiemDanh.setText("Chưa có dữ liệu phân công cho " + TEN_CA[caHienTai]);
        } else {
            lblCaHienTai.setText("Hôm nay " + ngayHienTai + "  |  " + thoiGian
                    + "  |  Hiện không nằm trong khung giờ ca làm");
            lblCaHienTai.setForeground(UIConfig.COLOR_MUTED);
            lblTrangThaiDiemDanh.setText("Không có ca đang diễn ra");
        }
    }

    private int timCaHienTai(LocalTime gioHienTai) {
        for (int index = 0; index < GIO_BAT_DAU_CA.length; index++) {
            if (!gioHienTai.isBefore(GIO_BAT_DAU_CA[index])
                    && gioHienTai.isBefore(GIO_KET_THUC_CA[index])) {
                return index;
            }
        }
        return -1;
    }

    private static final class DatePickerPanel extends JPanel {
        private static final String[] TEN_NGAY_TRONG_TUAN = {
                "T2", "T3", "T4", "T5", "T6", "T7", "CN"
        };
        private static final Locale LOCALE_VI = Locale.forLanguageTag("vi-VN");
        private static final DateTimeFormatter DINH_DANG_NGAY_DAY_DU =
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", LOCALE_VI);
        private static final DateTimeFormatter DINH_DANG_THANG =
                DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_VI);
        private final AppTextField txtNgay = new AppTextField(18);
        private final AppButton btnMoLich = new AppButton("▣", AppButton.Style.SECONDARY);
        private final JPopupMenu popLich = new JPopupMenu();
        private final JPanel pnlLich = new JPanel(new BorderLayout(0, 8));
        private final JLabel lblThang = new AppLabel("", AppLabel.Style.SECTION);
        private final Consumer<LocalDate> khiChonNgay;
        private LocalDate ngayDuocChon;
        private LocalDate thangDangHienThi;

        private DatePickerPanel(LocalDate ngayBanDau, Consumer<LocalDate> khiChonNgay) {
            super(new BorderLayout(6, 0));
            this.khiChonNgay = khiChonNgay;
            this.ngayDuocChon = ngayBanDau;
            this.thangDangHienThi = ngayBanDau.withDayOfMonth(1);

            setOpaque(false);
            txtNgay.setEditable(false);
            txtNgay.setFocusable(false);
            btnMoLich.setToolTipText("Mở lịch chọn ngày");
            add(txtNgay, BorderLayout.CENTER);
            add(btnMoLich, BorderLayout.EAST);
            popLich.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConfig.COLOR_BORDER),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            popLich.add(pnlLich);
            btnMoLich.addActionListener(event -> moLich());
            capNhatHienThiNgay();
            capNhatLich();
        }

        private LocalDate getSelectedDate() {
            return ngayDuocChon;
        }

        private void setSelectedDate(LocalDate ngay) {
            ngayDuocChon = ngay;
            thangDangHienThi = ngay.withDayOfMonth(1);
            capNhatHienThiNgay();
            capNhatLich();
        }

        private void moLich() {
            capNhatLich();
            popLich.show(this, 0, getHeight());
        }

        private void capNhatHienThiNgay() {
            txtNgay.setText(ngayDuocChon.format(DINH_DANG_NGAY_DAY_DU));
        }

        private void capNhatLich() {
            pnlLich.removeAll();
            pnlLich.setOpaque(false);
            pnlLich.setPreferredSize(new java.awt.Dimension(280, 250));

            JPanel pnlDieuHuongThang = new JPanel(new BorderLayout(8, 0));
            pnlDieuHuongThang.setOpaque(false);
            JButton btnThangTruoc = new JButton("‹");
            JButton btnThangSau = new JButton("›");
            cauHinhNutThang(btnThangTruoc);
            cauHinhNutThang(btnThangSau);
            lblThang.setHorizontalAlignment(SwingConstants.CENTER);
            lblThang.setText(thangDangHienThi.format(DINH_DANG_THANG));
            btnThangTruoc.addActionListener(event -> {
                thangDangHienThi = thangDangHienThi.minusMonths(1);
                capNhatLich();
            });
            btnThangSau.addActionListener(event -> {
                thangDangHienThi = thangDangHienThi.plusMonths(1);
                capNhatLich();
            });
            pnlDieuHuongThang.add(btnThangTruoc, BorderLayout.WEST);
            pnlDieuHuongThang.add(lblThang, BorderLayout.CENTER);
            pnlDieuHuongThang.add(btnThangSau, BorderLayout.EAST);
            pnlLich.add(pnlDieuHuongThang, BorderLayout.NORTH);

            JPanel pnlNgay = new JPanel(new GridLayout(7, 7, 3, 3));
            pnlNgay.setOpaque(false);
            for (String tenNgay : TEN_NGAY_TRONG_TUAN) {
                JLabel lblNgay = new AppLabel(tenNgay, AppLabel.Style.MUTED);
                lblNgay.setHorizontalAlignment(SwingConstants.CENTER);
                pnlNgay.add(lblNgay);
            }

            LocalDate ngayDauO = thangDangHienThi.withDayOfMonth(1)
                    .with(DayOfWeek.MONDAY);
            LocalDate homNay = LocalDate.now();
            for (int index = 0; index < 42; index++) {
                LocalDate ngay = ngayDauO.plusDays(index);
                JButton btnNgay = new JButton(String.valueOf(ngay.getDayOfMonth()));
                btnNgay.setFont(UIConfig.FONT_REGULAR);
                btnNgay.setFocusPainted(false);
                btnNgay.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                btnNgay.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                boolean cungThang = ngay.getMonth() == thangDangHienThi.getMonth();
                boolean dangChon = ngay.equals(ngayDuocChon);
                btnNgay.setForeground(dangChon ? Color.WHITE
                        : cungThang ? UIConfig.COLOR_TEXT : UIConfig.COLOR_MUTED);
                btnNgay.setBackground(dangChon ? UIConfig.COLOR_PRIMARY
                        : ngay.equals(homNay) ? UIConfig.COLOR_SELECTION : Color.WHITE);
                btnNgay.setOpaque(true);
                btnNgay.setToolTipText(ngay.format(DINH_DANG_NGAY_DAY_DU));
                btnNgay.addActionListener(event -> {
                    setSelectedDate(ngay);
                    popLich.setVisible(false);
                    khiChonNgay.accept(ngay);
                });
                pnlNgay.add(btnNgay);
            }
            pnlLich.add(pnlNgay, BorderLayout.CENTER);
            pnlLich.revalidate();
            pnlLich.repaint();
            popLich.pack();
        }

        private void cauHinhNutThang(JButton btnThang) {
            btnThang.setFont(UIConfig.FONT_SECTION);
            btnThang.setForeground(UIConfig.COLOR_TITLE);
            btnThang.setBackground(Color.WHITE);
            btnThang.setFocusPainted(false);
            btnThang.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }
    }

    private static final class LichCaCellRenderer extends DefaultTableCellRenderer {
        private final JTextArea txaNhanVien = new JTextArea();
        private int caHienTai = -1;
        private int cotNgayHienTai = -1;

        private LichCaCellRenderer() {
            txaNhanVien.setLineWrap(true);
            txaNhanVien.setWrapStyleWord(true);
            txaNhanVien.setFont(UIConfig.FONT_REGULAR);
            txaNhanVien.setForeground(UIConfig.COLOR_TEXT);
            txaNhanVien.setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        private void setCaHienTai(int caHienTai, int cotNgayHienTai) {
            this.caHienTai = caHienTai;
            this.cotNgayHienTai = cotNgayHienTai;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();
            boolean laCaHienTai = row == caHienTai;
            boolean laNgayHienTai = column == cotNgayHienTai;

            if (column == 0) {
                JLabel lblCa = (JLabel) super.getTableCellRendererComponent(
                        table, text, isSelected, hasFocus, row, column);
                lblCa.setHorizontalAlignment(SwingConstants.CENTER);
                lblCa.setVerticalAlignment(SwingConstants.CENTER);
                lblCa.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblCa.setForeground(laCaHienTai ? UIConfig.COLOR_SUCCESS : UIConfig.COLOR_TITLE);
                lblCa.setBackground(isSelected ? UIConfig.COLOR_SELECTION
                        : laCaHienTai ? new Color(222, 246, 234) : UIConfig.COLOR_TABLE_HEADER);
                lblCa.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                if (laCaHienTai) {
                    lblCa.setText("<html><center>" + text.replace("\n", "<br>")
                            + "<br><b>● ĐANG DIỄN RA</b></center></html>");
                }
                return lblCa;
            }

            txaNhanVien.setText(text);
            if (isSelected) {
                txaNhanVien.setBackground(UIConfig.COLOR_SELECTION);
            } else if (laCaHienTai && laNgayHienTai) {
                txaNhanVien.setBackground(new Color(207, 241, 220));
            } else if (laCaHienTai) {
                txaNhanVien.setBackground(new Color(235, 249, 239));
            } else {
                txaNhanVien.setBackground(Color.WHITE);
            }
            txaNhanVien.setForeground(CHUA_PHAN_CONG.equals(text)
                    ? UIConfig.COLOR_MUTED
                    : UIConfig.COLOR_TEXT);
            txaNhanVien.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
            return txaNhanVien;
        }
    }
}

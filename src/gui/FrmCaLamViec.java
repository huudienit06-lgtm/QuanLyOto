package gui;

import controller.CaLamViecController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện quản lý lịch phân công ca theo tuần và điểm danh nhân viên.
 */
public class FrmCaLamViec extends JPanel {
    private static final String CHUA_PHAN_CONG = "Chưa phân công";
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
            new Object[] {"Mã nhân viên", "Họ và tên", "Có mặt"}, 0) {
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 2 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2;
        }
    };
    private final JTable tblLichTuan = FormUI.createTable(modelLichTuan);
    private final JTable tblDiemDanh = FormUI.createTable(modelDiemDanh);
    private final AppComboBox<String> cboNhanVien = new AppComboBox<>();
    private final AppComboBox<String> cboCaLamViec = new AppComboBox<>();
    private final AppButton btnThem = new AppButton("Thêm phân công");
    private final AppButton btnSua = new AppButton("Sửa phân công", AppButton.Style.SUCCESS);
    private final AppButton btnXoa = new AppButton("Xóa phân công", AppButton.Style.DANGER);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnTuanTruoc = new AppButton("‹ Tuần trước", AppButton.Style.SECONDARY);
    private final AppButton btnTuanKeTiep = new AppButton("Tuần kế tiếp ›", AppButton.Style.SECONDARY);
    private final DatePickerPanel pnlChonNgayLamViec = new DatePickerPanel(LocalDate.now());
    private final JLabel lblCaHienTai = new AppLabel("", AppLabel.Style.BODY);
    private final JLabel lblTrangThaiDiemDanh = new AppLabel("", AppLabel.Style.MUTED);
    private final JLabel lblTuanHienThi = new AppLabel("", AppLabel.Style.MUTED);
    private final LichCaCellRenderer rendererLichCa = new LichCaCellRenderer();
    private final CaLamViecController controller = new CaLamViecController(this);
    private final LocalDate ngayDauTuanBanDau = LocalDate.now().with(DayOfWeek.MONDAY);
    private LocalDate ngayDauTuan = ngayDauTuanBanDau;
    private AppScrollPane scrNoiDung;

    public FrmCaLamViec() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        cauHinhBangLich();
        cauHinhBangDiemDanh();
        capNhatHienThiTuan(ngayDauTuanBanDau, LocalDate.now());

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(createFormPhanCong(), BorderLayout.NORTH);
        pnlNoiDung.add(createLichTuanCard(), BorderLayout.CENTER);
        pnlNoiDung.add(createDiemDanhCard(), BorderLayout.SOUTH);

        scrNoiDung = new AppScrollPane(pnlNoiDung);
        add(FormUI.createPage("LỊCH CA LÀM VIỆC",
                "Theo dõi lịch tuần, ca đang diễn ra và điểm danh nhân viên", scrNoiDung),
                BorderLayout.CENTER);
        controller.khoiTao();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        controller.khiViewDuocHienThi();
    }

    @Override
    public void removeNotify() {
        controller.khiViewBiAn();
        super.removeNotify();
    }

    private JPanel createFormPhanCong() {
        AppPanel pnlPhanCong = FormUI.createCard();
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Nhân viên", cboNhanVien, 0, 0);
        FormUI.addField(pnlFields, "Ca làm việc", cboCaLamViec, 2, 0);
        FormUI.addField(pnlFields, "Ngày làm việc", pnlChonNgayLamViec, 0, 1);
        JPanel pnlButtons = FormUI.createButtonRow(btnThem, btnSua, btnXoa, btnLamMoi);
        pnlPhanCong.add(FormUI.createSectionTitle("Thông tin phân công ca"), BorderLayout.NORTH);
        pnlPhanCong.add(pnlFields, BorderLayout.CENTER);
        pnlPhanCong.add(pnlButtons, BorderLayout.SOUTH);
        return pnlPhanCong;
    }

    private JPanel createLichTuanCard() {
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

    private JPanel createDiemDanhCard() {
        AppPanel pnlDiemDanh = FormUI.createCard();
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.add(FormUI.createSectionTitle("Điểm danh nhân viên trong ca hiện tại"),
                BorderLayout.WEST);
        pnlHeader.add(lblTrangThaiDiemDanh, BorderLayout.EAST);
        AppScrollPane scrDiemDanh = new AppScrollPane(tblDiemDanh);
        scrDiemDanh.setPreferredSize(new java.awt.Dimension(700, 112));
        pnlDiemDanh.add(pnlHeader, BorderLayout.NORTH);
        pnlDiemDanh.add(scrDiemDanh, BorderLayout.CENTER);
        return pnlDiemDanh;
    }

    private void cauHinhBangLich() {
        tblLichTuan.setAutoCreateRowSorter(false);
        tblLichTuan.setCellSelectionEnabled(true);
        tblLichTuan.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
    }

    private void cauHinhBangDiemDanh() {
        tblDiemDanh.setRowHeight(28);
        tblDiemDanh.setFillsViewportHeight(true);
        tblDiemDanh.getTableHeader().setReorderingAllowed(false);
        tblDiemDanh.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 32));
        tblDiemDanh.getColumnModel().getColumn(0).setPreferredWidth(115);
        tblDiemDanh.getColumnModel().getColumn(1).setPreferredWidth(210);
        tblDiemDanh.getColumnModel().getColumn(2).setPreferredWidth(85);
        tblDiemDanh.getColumnModel().getColumn(2)
                .setCellEditor(new DefaultCellEditor(new javax.swing.JCheckBox()));
    }

    public void hienThiLuaChon(List<String> nhanVien, List<String> caLamViec) {
        cboNhanVien.removeAllItems();
        cboNhanVien.addItem("Chọn nhân viên");
        nhanVien.forEach(cboNhanVien::addItem);
        cboCaLamViec.removeAllItems();
        cboCaLamViec.addItem("Chọn ca làm việc");
        caLamViec.forEach(cboCaLamViec::addItem);
    }

    public void capNhatHienThiTuan(LocalDate ngayDauTuan, LocalDate ngayDuocChon) {
        this.ngayDauTuan = ngayDauTuan;
        pnlChonNgayLamViec.setSelectedDate(ngayDuocChon);
        Object[] tieuDeCot = new Object[8];
        tieuDeCot[0] = "Ca làm việc";
        String[] tenThu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        for (int index = 0; index < tenThu.length; index++) {
            LocalDate ngay = ngayDauTuan.plusDays(index);
            tieuDeCot[index + 1] = "<html><center>" + tenThu[index] + "<br>"
                    + ngay.format(DINH_DANG_NGAY) + "</center></html>";
        }
        modelLichTuan.setColumnIdentifiers(tieuDeCot);
        lblTuanHienThi.setText("Tuần: " + ngayDauTuan.format(DINH_DANG_NGAY) + " - "
                + ngayDauTuan.plusDays(6).format(DINH_DANG_NGAY));
    }

    public void hienThiLichTuan(Object[][] cacDong) {
        modelLichTuan.setRowCount(0);
        for (Object[] dong : cacDong) {
            modelLichTuan.addRow(dong);
        }
        tblLichTuan.setDefaultRenderer(Object.class, rendererLichCa);
        tblLichTuan.repaint();
    }

    public void hienThiDiemDanh(Object[][] cacDong, String thongBao) {
        modelDiemDanh.setRowCount(0);
        for (Object[] dong : cacDong) {
            modelDiemDanh.addRow(dong);
        }
        lblTrangThaiDiemDanh.setText(thongBao);
    }

    public void capNhatCaHienTai(String thongTin, boolean dangTrongCa, int chiSoCa, int cotNgay) {
        rendererLichCa.setCaHienTai(cotNgay >= 0 ? chiSoCa : -1, cotNgay);
        tblLichTuan.repaint();
        lblCaHienTai.setText(thongTin);
        lblCaHienTai.setForeground(dangTrongCa ? UIConfig.COLOR_SUCCESS : UIConfig.COLOR_MUTED);
    }

    public void capNhatTrangThaiNut(boolean ngayDuocSua, boolean coPhanCongDangChon) {
        btnThem.setEnabled(ngayDuocSua);
        btnSua.setEnabled(ngayDuocSua && coPhanCongDangChon);
        btnXoa.setEnabled(ngayDuocSua && coPhanCongDangChon);
    }

    public void chonNhanVien(String maNV) {
        cboNhanVien.setSelectedIndex(0);
        for (int index = 1; index < cboNhanVien.getItemCount(); index++) {
            if (cboNhanVien.getItemAt(index).startsWith(maNV + " - ")) {
                cboNhanVien.setSelectedIndex(index);
                break;
            }
        }
    }

    public void datLaiLuaChon() {
        cboNhanVien.setSelectedIndex(0);
        cboCaLamViec.setSelectedIndex(0);
    }

    public void thongBao(String noiDung, String tieuDe, int loai) {
        JOptionPane.showMessageDialog(this, noiDung, tieuDe, loai);
    }

    public boolean xacNhanXoa() {
        return JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phân công đã chọn?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public int chonPhanCong(List<String> luaChon) {
        Object luaChonNhanVien = JOptionPane.showInputDialog(this,
                "Chọn phân công cần thao tác:", "Chọn phân công",
                JOptionPane.QUESTION_MESSAGE, null, luaChon.toArray(), luaChon.get(0));
        return luaChonNhanVien == null ? -1 : luaChon.indexOf(luaChonNhanVien.toString());
    }

    public void hienThiLoiCSDL(String thongBao, Exception exception) {
        thongBao(thongBao + "\n" + exception.getMessage(),
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    public void hienThiLoi(String thongBao) {
        thongBao(thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void setTrangThai(String thongBao) {
        lblTrangThaiDiemDanh.setText(thongBao);
    }

    public LocalDate getNgayDuocChon() {
        return pnlChonNgayLamViec.getSelectedDate();
    }

    public int getChiSoCaDuocChon() {
        return cboCaLamViec.getSelectedIndex() - 1;
    }

    public void setSelectedShiftIndex(int chiSoCa) {
        cboCaLamViec.setSelectedIndex(chiSoCa + 1);
    }

    public String getMaNhanVienDuocChon() {
        String selected = (String) cboNhanVien.getSelectedItem();
        if (selected == null || cboNhanVien.getSelectedIndex() <= 0) {
            return null;
        }
        int separator = selected.indexOf(" - ");
        return separator > 0 ? selected.substring(0, separator) : null;
    }

    public JTable getTblLichTuan() { return tblLichTuan; }
    public JTable getTblDiemDanh() { return tblDiemDanh; }
    public DefaultTableModel getModelDiemDanh() { return modelDiemDanh; }
    public AppButton getBtnThem() { return btnThem; }
    public AppButton getBtnSua() { return btnSua; }
    public AppButton getBtnXoa() { return btnXoa; }
    public AppButton getBtnLamMoi() { return btnLamMoi; }
    public AppButton getBtnTuanTruoc() { return btnTuanTruoc; }
    public AppButton getBtnTuanKeTiep() { return btnTuanKeTiep; }
    public DatePickerPanel getPnlChonNgayLamViec() { return pnlChonNgayLamViec; }
    public AppScrollPane getScrNoiDung() { return scrNoiDung; }

    public static final class DatePickerPanel extends JPanel {
        private static final String[] TEN_NGAY_TRONG_TUAN = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        private static final Locale LOCALE_VI = Locale.forLanguageTag("vi-VN");
        private static final DateTimeFormatter DINH_DANG_NGAY_DAY_DU =
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", LOCALE_VI);
        private static final DateTimeFormatter DINH_DANG_THANG =
                DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_VI);
        private final AppTextField txtNgay = new AppTextField(18);
        private final AppButton btnMoLich = new AppButton("▣", AppButton.Style.SECONDARY);
        private final JButton btnThangTruoc = new JButton("‹");
        private final JButton btnThangSau = new JButton("›");
        private final JButton[] btnNgay = new JButton[42];
        private final JPopupMenu popLich = new JPopupMenu();
        private final JPanel pnlLich = new JPanel(new BorderLayout(0, 8));
        private final JLabel lblThang = new AppLabel("", AppLabel.Style.SECTION);
        private LocalDate ngayDuocChon;
        private LocalDate thangDangHienThi;

        private DatePickerPanel(LocalDate ngayBanDau) {
            super(new BorderLayout(6, 0));
            ngayDuocChon = ngayBanDau;
            thangDangHienThi = ngayBanDau.withDayOfMonth(1);
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
            cauHinhLich();
            setSelectedDate(ngayBanDau);
        }

        private void cauHinhLich() {
            pnlLich.setOpaque(false);
            pnlLich.setPreferredSize(new java.awt.Dimension(280, 250));
            JPanel pnlDieuHuongThang = new JPanel(new BorderLayout(8, 0));
            pnlDieuHuongThang.setOpaque(false);
            cauHinhNutThang(btnThangTruoc);
            cauHinhNutThang(btnThangSau);
            lblThang.setHorizontalAlignment(SwingConstants.CENTER);
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
            for (int index = 0; index < btnNgay.length; index++) {
                btnNgay[index] = new JButton();
                btnNgay[index].setFont(UIConfig.FONT_REGULAR);
                btnNgay[index].setFocusPainted(false);
                btnNgay[index].setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                btnNgay[index].setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                pnlNgay.add(btnNgay[index]);
            }
            pnlLich.add(pnlNgay, BorderLayout.CENTER);
        }

        private void capNhatLich() {
            lblThang.setText(thangDangHienThi.format(DINH_DANG_THANG));
            LocalDate ngayDauO = thangDangHienThi.withDayOfMonth(1).with(DayOfWeek.MONDAY);
            LocalDate homNay = LocalDate.now();
            for (int index = 0; index < btnNgay.length; index++) {
                LocalDate ngay = ngayDauO.plusDays(index);
                JButton btn = btnNgay[index];
                boolean cungThang = ngay.getMonth() == thangDangHienThi.getMonth();
                boolean dangChon = ngay.equals(ngayDuocChon);
                btn.setText(String.valueOf(ngay.getDayOfMonth()));
                btn.putClientProperty("ngay", ngay);
                btn.setForeground(dangChon ? Color.WHITE
                        : cungThang ? UIConfig.COLOR_TEXT : UIConfig.COLOR_MUTED);
                btn.setBackground(dangChon ? UIConfig.COLOR_PRIMARY
                        : ngay.equals(homNay) ? UIConfig.COLOR_SELECTION : Color.WHITE);
                btn.setOpaque(true);
                btn.setToolTipText(ngay.format(DINH_DANG_NGAY_DAY_DU));
            }
            pnlLich.revalidate();
            pnlLich.repaint();
            popLich.pack();
        }

        public void setSelectedDate(LocalDate ngay) {
            ngayDuocChon = ngay;
            thangDangHienThi = ngay.withDayOfMonth(1);
            txtNgay.setText(ngay.format(DINH_DANG_NGAY_DAY_DU));
            capNhatLich();
        }

        public void capNhatThang(int soThang) {
            thangDangHienThi = thangDangHienThi.plusMonths(soThang);
            capNhatLich();
        }

        private void cauHinhNutThang(JButton btnThang) {
            btnThang.setFont(UIConfig.FONT_SECTION);
            btnThang.setForeground(UIConfig.COLOR_TITLE);
            btnThang.setBackground(Color.WHITE);
            btnThang.setFocusPainted(false);
            btnThang.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }

        public JButton getBtnMoLich() { return btnMoLich; }
        public JButton getBtnThangTruoc() { return btnThangTruoc; }
        public JButton getBtnThangSau() { return btnThangSau; }
        public JButton[] getBtnNgay() { return btnNgay.clone(); }
        public JPopupMenu getPopLich() { return popLich; }
        public LocalDate getNgayDuocChon() { return ngayDuocChon; }
        public LocalDate getSelectedDate() { return ngayDuocChon; }
        public LocalDate getNgay(JButton btn) { return (LocalDate) btn.getClientProperty("ngay"); }
        public void hienThiLich() {
            capNhatLich();
            popLich.show(this, 0, getHeight());
        }
    }

    private final class LichCaCellRenderer extends DefaultTableCellRenderer {
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
            boolean laNgayQuaKhu = column > 0
                    && ngayDauTuan.plusDays(column - 1L).isBefore(LocalDate.now());
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
            if (laNgayQuaKhu) {
                txaNhanVien.setBackground(new Color(232, 232, 232));
            } else if (isSelected) {
                txaNhanVien.setBackground(UIConfig.COLOR_SELECTION);
            } else if (laCaHienTai && laNgayHienTai) {
                txaNhanVien.setBackground(new Color(207, 241, 220));
            } else if (laCaHienTai) {
                txaNhanVien.setBackground(new Color(235, 249, 239));
            } else {
                txaNhanVien.setBackground(Color.WHITE);
            }
            txaNhanVien.setForeground(laNgayQuaKhu || CHUA_PHAN_CONG.equals(text)
                    ? UIConfig.COLOR_MUTED : UIConfig.COLOR_TEXT);
            txaNhanVien.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
            return txaNhanVien;
        }
    }
}

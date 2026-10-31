package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseAdapter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import dao.CaLamViecDAO;
import dao.LichLamViecDAO;
import dao.LichLamViecDAO.PhanCong;
import dao.NhanVienDAO;
import entity.CaLamViec;
import entity.NhanVien;
import entity.LichLamViec;
import util.MaTuDong;

public class FrmCaLamViec extends JPanel {
    private static final String CHUA_PHAN_CONG = "Chưa phân công";
    private static final String[] TEN_THU = {
            "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"
    };
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");
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
    private final DatePickerPanel pnlChonNgayLamViec = new DatePickerPanel(
            LocalDate.now(), this::chonNgayLamViec);
    private final JLabel lblCaHienTai = new AppLabel("", AppLabel.Style.BODY);
    private final JLabel lblTrangThaiDiemDanh = new AppLabel("", AppLabel.Style.MUTED);
    private final JLabel lblTuanHienThi = new AppLabel("", AppLabel.Style.MUTED);
    private final LichCaCellRenderer rendererLichCa = new LichCaCellRenderer();
    private final Timer timerCapNhatThoiGian = new Timer(30_000, event -> capNhatCaHienTai());
    private final CaLamViecDAO caLamViecDAO = new CaLamViecDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final LichLamViecDAO lichLamViecDAO = new LichLamViecDAO();
    private List<CaLamViec> danhSachCa = List.of();
    private List<PhanCong> phanCongTuan = List.of();
    private AppScrollPane scrNoiDung;
    private String maLichDangChon;
    private boolean dangNapDiemDanh;
    private int caHienTai = -1;
    private int cotNgayHienTai = -1;
    private LocalDate ngayDauTuan = LocalDate.now().with(DayOfWeek.MONDAY);

    public FrmCaLamViec() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);

        cauHinhLuaChon();
        capNhatTieuDeCotNgay();
        capNhatTieuDeNgay();
        cauHinhBangLich();
        cauHinhBangDiemDanh();
        napPhanCongTuan();
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

        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
        btnThem.addActionListener(event -> themPhanCong());
        btnSua.addActionListener(event -> capNhatPhanCong());
        btnXoa.addActionListener(event -> xoaPhanCong());
        capNhatTrangThaiNutPhanCong();
        btnLamMoi.addActionListener(event -> lamMoiPhanCong());
        JPanel pnlButtons = FormUI.createButtonRow(
                btnThem, btnSua, btnXoa, btnLamMoi);
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
        try {
            danhSachCa = caLamViecDAO.findAll();
            for (CaLamViec ca : danhSachCa) {
                cboCaLamViec.addItem(ca.getMaCa() + " ("
                        + ca.getGioBatDau().toLocalTime().format(DINH_DANG_GIO) + " - "
                        + ca.getGioKetThuc().toLocalTime().format(DINH_DANG_GIO) + ")");
            }
            for (NhanVien nhanVien : nhanVienDAO.findAll()) {
                cboNhanVien.addItem(nhanVien.getMaNV() + " - " + nhanVien.getHoTen());
            }
        } catch (java.sql.SQLException exception) {
            hienThiLoiCSDL("Không thể tải danh sách ca làm việc và nhân viên.", exception);
        }
    }

    private void chonNgayLamViec(LocalDate ngayChon) {
        LocalDate dauTuanChon = ngayChon.with(DayOfWeek.MONDAY);
        if (!dauTuanChon.equals(ngayDauTuan)) {
            ngayDauTuan = dauTuanChon;
            capNhatTuan(ngayChon);
        }
        capNhatTrangThaiNutPhanCong();
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
        tblLichTuan.addMouseWheelListener(event -> chuyenCuonRaTrangNeuCan(tblLichTuan, event));
        tblLichTuan.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                chonOTrongLich();
            }
        });
        tblLichTuan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    chonPhanCongDeSua();
                }
            }
        });
    }

    private void dichTuan(int soTuan) {
        int ngayTrongTuan = pnlChonNgayLamViec.getSelectedDate().getDayOfWeek().getValue() - 1;
        ngayDauTuan = ngayDauTuan.plusWeeks(soTuan);
        capNhatTuan(ngayDauTuan.plusDays(ngayTrongTuan));
    }

    private void napPhanCongTuan() {
        modelLichTuan.setRowCount(0);
        try {
            phanCongTuan = lichLamViecDAO.layPhanCong(ngayDauTuan, ngayDauTuan.plusDays(7));
            for (CaLamViec ca : danhSachCa) {
                String khungGio = ca.getGioBatDau().toLocalTime().format(DINH_DANG_GIO)
                        + " - " + ca.getGioKetThuc().toLocalTime().format(DINH_DANG_GIO);
                Object[] dong = new Object[8];
                dong[0] = ca.getMaCa() + "\n" + khungGio;
                for (int ngay = 0; ngay < 7; ngay++) {
                    LocalDate ngayTrongTuan = ngayDauTuan.plusDays(ngay);
                    List<String> tenNhanVien = phanCongTuan.stream()
                            .filter(phanCong -> phanCong.maCa().equals(ca.getMaCa())
                                    && phanCong.ngayLamViec().equals(ngayTrongTuan))
                            .map(phanCong -> phanCong.hoTen()
                                    + ("Có mặt".equals(phanCong.trangThai()) ? " (Có mặt)"
                                            : "Vắng mặt".equals(phanCong.trangThai()) ? " (Vắng)" : ""))
                            .toList();
                    dong[ngay + 1] = tenNhanVien.isEmpty()
                            ? CHUA_PHAN_CONG : String.join("\n", tenNhanVien);
                }
                modelLichTuan.addRow(dong);
            }
        } catch (java.sql.SQLException exception) {
            phanCongTuan = List.of();
            lblTrangThaiDiemDanh.setText("Không thể tải lịch phân công.");
            hienThiLoiCSDL("Không thể tải lịch làm việc.", exception);
        }
        tblLichTuan.setDefaultRenderer(Object.class, rendererLichCa);
        tblLichTuan.repaint();
        napDiemDanh();
    }

    private void themPhanCong() {
        String maCa = layMaCaDuocChon();
        String maNV = layMaNhanVienDuocChon();
        if (ngayLamViecTrongQuaKhu()) {
            hienThiCanhBaoNgayQuaKhu();
            return;
        }
        if (maCa == null || maNV == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên và ca làm việc.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (lichLamViecDAO.daTrungPhanCong(
                    maCa, maNV, pnlChonNgayLamViec.getSelectedDate(), null)) {
                JOptionPane.showMessageDialog(this,
                        "Nhân viên này đã được xếp vào ca đã chọn trong ngày này.",
                        "Phân công bị trùng", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maLich = MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_LICH_LAM_VIEC,
                    lichLamViecDAO.findAll(),
                    LichLamViec::getMaLich);
            if (lichLamViecDAO.themPhanCong(maLich, maCa, maNV, pnlChonNgayLamViec.getSelectedDate())) {
                napPhanCongTuan();
                lblTrangThaiDiemDanh.setText("Đã thêm phân công.");
            }
        } catch (java.sql.SQLException exception) {
            hienThiLoiCSDL("Không thể thêm phân công.", exception);
        }
    }

    private void capNhatPhanCong() {
        String maCa = layMaCaDuocChon();
        String maNV = layMaNhanVienDuocChon();
        if (ngayLamViecTrongQuaKhu()) {
            hienThiCanhBaoNgayQuaKhu();
            return;
        }
        if (maLichDangChon == null || maCa == null || maNV == null) {
            JOptionPane.showMessageDialog(this, "Chọn một phân công trên lịch và kiểm tra thông tin cần sửa.",
                    "Chưa chọn phân công", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (lichLamViecDAO.daTrungPhanCong(
                    maCa, maNV, pnlChonNgayLamViec.getSelectedDate(), maLichDangChon)) {
                JOptionPane.showMessageDialog(this,
                        "Nhân viên này đã được xếp vào ca đã chọn trong ngày này.",
                        "Phân công bị trùng", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (lichLamViecDAO.capNhatPhanCong(maLichDangChon, maCa, maNV,
                    pnlChonNgayLamViec.getSelectedDate())) {
                napPhanCongTuan();
                lblTrangThaiDiemDanh.setText("Đã cập nhật phân công.");
            }
        } catch (java.sql.SQLException exception) {
            hienThiLoiCSDL("Không thể cập nhật phân công.", exception);
        }
    }

    private void xoaPhanCong() {
        if (ngayLamViecTrongQuaKhu()) {
            hienThiCanhBaoNgayQuaKhu();
            return;
        }
        if (maLichDangChon == null) {
            JOptionPane.showMessageDialog(this, "Chọn một phân công trên lịch trước khi xóa.",
                    "Chưa chọn phân công", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phân công đã chọn?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (lichLamViecDAO.xoaPhanCong(maLichDangChon)) {
                lamMoiPhanCong();
                lblTrangThaiDiemDanh.setText("Đã xóa phân công.");
            }
        } catch (java.sql.SQLException exception) {
            hienThiLoiCSDL("Không thể xóa phân công.", exception);
        }
    }

    private void lamMoiPhanCong() {
        maLichDangChon = null;
        cboNhanVien.setSelectedIndex(0);
        cboCaLamViec.setSelectedIndex(0);
        capNhatTrangThaiNutPhanCong();
        napPhanCongTuan();
        capNhatCaHienTai();
    }

    private String layMaCaDuocChon() {
        int selectedIndex = cboCaLamViec.getSelectedIndex() - 1;
        return selectedIndex >= 0 && selectedIndex < danhSachCa.size()
                ? danhSachCa.get(selectedIndex).getMaCa() : null;
    }

    private String layMaNhanVienDuocChon() {
        String selected = (String) cboNhanVien.getSelectedItem();
        if (selected == null || cboNhanVien.getSelectedIndex() <= 0) {
            return null;
        }
        int separator = selected.indexOf(" - ");
        return separator > 0 ? selected.substring(0, separator) : null;
    }

    private void chonOTrongLich() {
        int row = tblLichTuan.getSelectedRow();
        int column = tblLichTuan.getSelectedColumn();
        if (row < 0 || column < 1 || column > 7 || row >= danhSachCa.size()) {
            return;
        }
        LocalDate ngay = ngayDauTuan.plusDays(column - 1);
        maLichDangChon = null;
        cboNhanVien.setSelectedIndex(0);
        pnlChonNgayLamViec.setSelectedDate(ngay);
        cboCaLamViec.setSelectedIndex(row + 1);
        capNhatTrangThaiNutPhanCong();
    }

    private void chonPhanCongDeSua() {
        int row = tblLichTuan.getSelectedRow();
        int column = tblLichTuan.getSelectedColumn();
        if (row < 0 || column < 1 || column > 7 || row >= danhSachCa.size()) {
            return;
        }
        LocalDate ngay = ngayDauTuan.plusDays(column - 1);
        if (ngay.isBefore(LocalDate.now())) {
            return;
        }
        String maCa = danhSachCa.get(row).getMaCa();
        List<PhanCong> cacPhanCong = phanCongTuan.stream()
                .filter(phanCong -> phanCong.maCa().equals(maCa) && phanCong.ngayLamViec().equals(ngay))
                .toList();
        if (cacPhanCong.isEmpty()) {
            return;
        }
        PhanCong selected = cacPhanCong.get(0);
        if (cacPhanCong.size() > 1) {
            String[] luaChon = cacPhanCong.stream()
                    .map(phanCong -> phanCong.maNV() + " - " + phanCong.hoTen())
                    .toArray(String[]::new);
            Object luaChonNhanVien = JOptionPane.showInputDialog(this,
                    "Chọn phân công cần thao tác:", "Chọn phân công",
                    JOptionPane.QUESTION_MESSAGE, null, luaChon, luaChon[0]);
            if (luaChonNhanVien == null) {
                return;
            }
            int index = java.util.Arrays.asList(luaChon).indexOf(luaChonNhanVien.toString());
            if (index < 0) {
                return;
            }
            selected = cacPhanCong.get(index);
        }
        maLichDangChon = selected.maLich();
        pnlChonNgayLamViec.setSelectedDate(ngay);
        cboCaLamViec.setSelectedIndex(row + 1);
        for (int index = 1; index < cboNhanVien.getItemCount(); index++) {
            if (cboNhanVien.getItemAt(index).startsWith(selected.maNV() + " - ")) {
                cboNhanVien.setSelectedIndex(index);
                break;
            }
        }
        capNhatTrangThaiNutPhanCong();
    }

    private void napDiemDanh() {
        dangNapDiemDanh = true;
        modelDiemDanh.setRowCount(0);
        LocalDate homNay = LocalDate.now();
        if (caHienTai >= 0 && caHienTai < danhSachCa.size()) {
            String maCa = danhSachCa.get(caHienTai).getMaCa();
            for (PhanCong phanCong : phanCongTuan) {
                if (phanCong.maCa().equals(maCa) && phanCong.ngayLamViec().equals(homNay)) {
                    modelDiemDanh.addRow(new Object[] {
                            phanCong.maNV(), phanCong.hoTen(),
                            "Có mặt".equals(phanCong.trangThai())
                    });
                }
            }
        }
        dangNapDiemDanh = false;
        lblTrangThaiDiemDanh.setText(modelDiemDanh.getRowCount() == 0
                ? (caHienTai >= 0 ? "Chưa có dữ liệu phân công cho ca hiện tại" : "Không có ca đang diễn ra")
                : "Đánh dấu trạng thái có mặt của nhân viên trong ca");
    }

    private void capNhatDiemDanh(int row) {
        if (row < 0 || row >= modelDiemDanh.getRowCount()
                || caHienTai < 0 || caHienTai >= danhSachCa.size()) {
            return;
        }
        String maNV = modelDiemDanh.getValueAt(row, 0).toString();
        LocalDate homNay = LocalDate.now();
        String maCa = danhSachCa.get(caHienTai).getMaCa();
        PhanCong phanCong = phanCongTuan.stream()
                .filter(item -> item.maNV().equals(maNV) && item.maCa().equals(maCa)
                        && item.ngayLamViec().equals(homNay))
                .findFirst().orElse(null);
        if (phanCong == null) {
            return;
        }
        try {
            lichLamViecDAO.capNhatTrangThai(phanCong.maLich(),
                    Boolean.TRUE.equals(modelDiemDanh.getValueAt(row, 2)));
            napPhanCongTuan();
        } catch (java.sql.SQLException exception) {
            hienThiLoiCSDL("Không thể lưu trạng thái điểm danh.", exception);
            napDiemDanh();
        }
    }

    private void hienThiLoiCSDL(String thongBao, Exception exception) {
        JOptionPane.showMessageDialog(this, thongBao + "\n" + exception.getMessage(),
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private boolean ngayLamViecTrongQuaKhu() {
        return pnlChonNgayLamViec.getSelectedDate().isBefore(LocalDate.now());
    }

    private void capNhatTrangThaiNutPhanCong() {
        boolean ngayDuocSua = !ngayLamViecTrongQuaKhu();
        btnThem.setEnabled(ngayDuocSua);
        btnSua.setEnabled(ngayDuocSua && maLichDangChon != null);
        btnXoa.setEnabled(ngayDuocSua && maLichDangChon != null);
    }

    private void hienThiCanhBaoNgayQuaKhu() {
        JOptionPane.showMessageDialog(this,
                "Không thể thêm, sửa hoặc xóa phân công của ngày đã qua.",
                "Không thể cập nhật", JOptionPane.WARNING_MESSAGE);
    }

    private void capNhatTuan(LocalDate ngayDuocChon) {
        capNhatTieuDeCotNgay();
        pnlChonNgayLamViec.setSelectedDate(ngayDuocChon);
        capNhatTieuDeNgay();
        napPhanCongTuan();
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
        tblDiemDanh.getColumnModel().getColumn(2).setPreferredWidth(85);
        tblDiemDanh.addMouseWheelListener(event -> chuyenCuonRaTrangNeuCan(tblDiemDanh, event));
        tblDiemDanh.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new javax.swing.JCheckBox()));
        modelDiemDanh.addTableModelListener(event -> {
            if (!dangNapDiemDanh && event.getType() == TableModelEvent.UPDATE && event.getColumn() == 2) {
                capNhatDiemDanh(event.getFirstRow());
            }
        });
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
            CaLamViec ca = danhSachCa.get(caHienTai);
            lblCaHienTai.setText("Hôm nay " + ngayHienTai + "  |  " + thoiGian
                    + "  |  ĐANG DIỄN RA: " + ca.getMaCa());
            lblCaHienTai.setForeground(UIConfig.COLOR_SUCCESS);
        } else {
            lblCaHienTai.setText("Hôm nay " + ngayHienTai + "  |  " + thoiGian
                    + "  |  Hiện không nằm trong khung giờ ca làm");
            lblCaHienTai.setForeground(UIConfig.COLOR_MUTED);
        }
        napDiemDanh();
    }

    private int timCaHienTai(LocalTime gioHienTai) {
        for (int index = 0; index < danhSachCa.size(); index++) {
            LocalTime gioBatDau = danhSachCa.get(index).getGioBatDau().toLocalTime();
            LocalTime gioKetThuc = danhSachCa.get(index).getGioKetThuc().toLocalTime();
            if (!gioHienTai.isBefore(gioBatDau) && gioHienTai.isBefore(gioKetThuc)) {
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
                    ? UIConfig.COLOR_MUTED
                    : UIConfig.COLOR_TEXT);
            txaNhanVien.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
            return txaNhanVien;
        }
    }
}

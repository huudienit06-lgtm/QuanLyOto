package gui;

import controller.CaLamViecController;
import entity.CaLamViec;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmCaLamViec extends JPanel {
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final JTextField txtMaCa = taoONhap(14);
    private final JTextField txtGioBatDau = taoONhap(14);
    private final JTextField txtGioKetThuc = taoONhap(14);
    private final JTextField txtTimKiem = taoONhap(18);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final JButton btnThem = new JButton("Thêm");
    private final JButton btnCapNhat = new JButton("Cập nhật");
    private final JButton btnXoa = new JButton("Xóa");
    private final JButton btnLamMoi = new JButton("Làm mới");
    private final JButton btnTaiLai = new JButton("Tải lại");

    private final DefaultTableModel modelCaLamViec = new DefaultTableModel(
            new Object[] { "Mã ca", "Giờ bắt đầu", "Giờ kết thúc" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblCaLamViec = new JTable(modelCaLamViec);
    private final TableRowSorter<DefaultTableModel> sorterCaLamViec =
            new TableRowSorter<>(modelCaLamViec);
    private final CaLamViecController controller = new CaLamViecController(this);

    public FrmCaLamViec() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(22, 28, 22, 28));
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        configureTable();
        styleButton(btnThem, UIConfig.COLOR_SUCCESS, Color.WHITE);
        styleButton(btnCapNhat, UIConfig.COLOR_PRIMARY, Color.WHITE);
        styleButton(btnXoa, UIConfig.COLOR_DANGER, Color.WHITE);
        styleButton(btnLamMoi, UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
        styleButton(btnTaiLai, UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
        txtMaCa.setEditable(false);
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);
        controller.khoiTao();
    }

    private JPanel createHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 5));
        pnlHeader.setOpaque(false);
        JLabel lblTieuDe = new JLabel("QUẢN LÝ CA LÀM VIỆC");
        lblTieuDe.setFont(UIConfig.FONT_TITLE);
        lblTieuDe.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblMoTa = new JLabel("Thiết lập các khung giờ được dùng để phân công lịch làm việc");
        lblMoTa.setFont(UIConfig.FONT_REGULAR);
        lblMoTa.setForeground(UIConfig.COLOR_MUTED);
        pnlHeader.add(lblTieuDe, BorderLayout.NORTH);
        pnlHeader.add(lblMoTa, BorderLayout.CENTER);
        return pnlHeader;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormCard(), BorderLayout.NORTH);
        pnlContent.add(createTableCard(), BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormCard() {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 12));
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã ca *", txtMaCa, 0);
        addField(pnlFields, "Giờ bắt đầu * (HH:mm)", txtGioBatDau, 2);
        addField(pnlFields, "Giờ kết thúc * (HH:mm)", txtGioKetThuc, 4);
        JPanel pnlButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnThem);
        pnlButtons.add(btnCapNhat);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        pnlCard.add(createSectionTitle("Thông tin ca làm việc"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createTableCard() {
        JPanel pnlCard = createCardPanel(new BorderLayout(0, 10));
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.add(createSectionTitle("Danh sách ca làm việc"), BorderLayout.WEST);
        JPanel pnlTimKiem = new JPanel(new BorderLayout(8, 0));
        pnlTimKiem.setOpaque(false);
        JLabel lblTimKiem = new JLabel("Tìm kiếm");
        lblTimKiem.setFont(UIConfig.FONT_REGULAR);
        lblTimKiem.setForeground(UIConfig.COLOR_MUTED);
        pnlTimKiem.add(lblTimKiem, BorderLayout.WEST);
        pnlTimKiem.add(txtTimKiem, BorderLayout.CENTER);
        pnlTimKiem.add(btnTaiLai, BorderLayout.EAST);
        pnlHeader.add(pnlTimKiem, BorderLayout.EAST);
        JScrollPane scrTable = new JScrollPane(tblCaLamViec);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(pnlHeader, BorderLayout.NORTH);
        pnlCard.add(scrTable, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel createStatusBar() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private void configureTable() {
        tblCaLamViec.setRowSorter(sorterCaLamViec);
        tblCaLamViec.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCaLamViec.setRowHeight(30);
        tblCaLamViec.setFont(UIConfig.FONT_REGULAR);
        tblCaLamViec.setForeground(UIConfig.COLOR_TEXT);
        tblCaLamViec.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblCaLamViec.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblCaLamViec.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblCaLamViec.setFillsViewportHeight(true);
        tblCaLamViec.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblCaLamViec.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblCaLamViec.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblCaLamViec.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
    }

    private JPanel createCardPanel(java.awt.LayoutManager layout) {
        JPanel pnlCard = new JPanel(layout);
        pnlCard.setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return pnlCard;
    }

    private JLabel createSectionTitle(String text) {
        JLabel lblTieuDe = new JLabel(text);
        lblTieuDe.setFont(UIConfig.FONT_SECTION);
        lblTieuDe.setForeground(UIConfig.COLOR_TITLE);
        return lblTieuDe;
    }

    private void addField(JPanel pnlFields, String text, JTextField txtField, int column) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = 0;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lblField = new JLabel(text);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_MUTED);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = 0;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlFields.add(txtField, gbcInput);
    }

    private static JTextField taoONhap(int columns) {
        JTextField txtField = new JTextField(columns);
        txtField.setFont(UIConfig.FONT_REGULAR);
        txtField.setForeground(UIConfig.COLOR_TEXT);
        txtField.setBackground(UIConfig.COLOR_SURFACE);
        txtField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                new EmptyBorder(6, 9, 6, 9)));
        return txtField;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(UIConfig.FONT_BOLD);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(6, 14, 6, 14));
    }

    public void hienThiDanhSach(List<CaLamViec> danhSach) {
        modelCaLamViec.setRowCount(0);
        for (CaLamViec ca : danhSach) {
            modelCaLamViec.addRow(new Object[] {
                    ca.getMaCa(), dinhDangGio(ca.getGioBatDau()), dinhDangGio(ca.getGioKetThuc())
            });
        }
    }

    public CaLamViec layCaLamViecNhap() {
        String maCa = txtMaCa.getText().trim();
        if (maCa.isEmpty()) {
            throw new IllegalArgumentException("Mã ca không được để trống.");
        }
        try {
            LocalTime gioBatDau = LocalTime.parse(txtGioBatDau.getText().trim(), DINH_DANG_GIO);
            LocalTime gioKetThuc = LocalTime.parse(txtGioKetThuc.getText().trim(), DINH_DANG_GIO);
            if (gioBatDau.equals(gioKetThuc)) {
                throw new IllegalArgumentException("Giờ bắt đầu và giờ kết thúc không được trùng nhau.");
            }
            return new CaLamViec(maCa, Time.valueOf(gioBatDau), Time.valueOf(gioKetThuc));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Giờ phải đúng định dạng HH:mm, ví dụ 08:30.");
        }
    }

    public void hienThiCaDuocChon() {
        int viewRow = tblCaLamViec.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = tblCaLamViec.convertRowIndexToModel(viewRow);
        txtMaCa.setText(String.valueOf(modelCaLamViec.getValueAt(modelRow, 0)));
        txtGioBatDau.setText(String.valueOf(modelCaLamViec.getValueAt(modelRow, 1)));
        txtGioKetThuc.setText(String.valueOf(modelCaLamViec.getValueAt(modelRow, 2)));
        txtMaCa.setEditable(false);
        btnThem.setEnabled(false);
        btnCapNhat.setEnabled(true);
        btnXoa.setEnabled(true);
        thongBao("Đang chọn ca " + txtMaCa.getText() + ".");
    }

    public void lamMoiForm(String maMoi) {
        tblCaLamViec.clearSelection();
        txtMaCa.setText(maMoi);
        txtGioBatDau.setText("");
        txtGioKetThuc.setText("");
        btnThem.setEnabled(true);
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);
        thongBao("Sẵn sàng.");
    }

    public void locDanhSach() {
        String tuKhoa = txtTimKiem.getText().trim();
        sorterCaLamViec.setRowFilter(tuKhoa.isEmpty()
                ? null : RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa)));
        lblThongBao.setText("Hiển thị " + sorterCaLamViec.getViewRowCount()
                + " / " + modelCaLamViec.getRowCount() + " ca làm việc.");
    }

    public String getMaCaDangChon() {
        int viewRow = tblCaLamViec.getSelectedRow();
        if (viewRow < 0) return null;
        return String.valueOf(modelCaLamViec.getValueAt(tblCaLamViec.convertRowIndexToModel(viewRow), 0));
    }

    public boolean xacNhanXoa(String maCa) {
        return JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa ca " + maCa + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION;
    }

    public void thongBao(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
    }

    public void hienThiLoi(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        JOptionPane.showMessageDialog(this, message, "Quản lý ca làm việc", JOptionPane.ERROR_MESSAGE);
    }

    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnCapNhat() { return btnCapNhat; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JButton getBtnTaiLai() { return btnTaiLai; }
    public JTable getTblCaLamViec() { return tblCaLamViec; }
    public JTextField getTxtTimKiem() { return txtTimKiem; }

    private String dinhDangGio(Time thoiGian) {
        return thoiGian == null ? "" : thoiGian.toLocalTime().format(DINH_DANG_GIO);
    }
}
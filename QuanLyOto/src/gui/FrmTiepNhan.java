package gui;

import controller.TiepNhanController;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.Xe;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmTiepNhan extends JPanel {
    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] TRANG_THAI_MAC_DINH = {
            "Đã tiếp nhận", "Đang kiểm tra", "Đang sửa chữa", "Hoàn thành", "Đã hủy"
    };

    // --- Các thành phần Swing chuẩn ---
    private final JTextField txtMaPhieuTiepNhan = createTextField(16);
    private final JComboBox<Xe> cboXe = new JComboBox<>();
    private final JTextField txtMaKH = createTextField(16);
    private final JTextField txtTenKhachHang = createTextField(18);
    private final JTextField txtNhanVien = createTextField(18);
    private final JTextField txtNgayLap = createTextField(16);
    private final JTextField txtTienDo = createTextField(20);
    private final JComboBox<String> cboTrangThai = new JComboBox<>();
    private final JTextArea txaGhiChu = new JTextArea(3, 24);
    private final JTextField txtTimKiem = createTextField(20);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    // Nút bấm có gắn icon từ resources/icons và màu sắc từ UIConfig
    private final JButton btnThem = createButton(
            "Lập phiếu", "/resources/icons/plus.png", UIConfig.COLOR_SUCCESS, Color.WHITE);
    private final JButton btnCapNhat = createButton(
            "Cập nhật", "/resources/icons/update.png", UIConfig.COLOR_PRIMARY, Color.WHITE);
    private final JButton btnXoa = createButton(
            "Xóa phiếu", "/resources/icons/trash.png", UIConfig.COLOR_DANGER, Color.WHITE);
    private final JButton btnLamMoi = createButton(
            "Làm mới", "/resources/icons/reset.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
    private final JButton btnTaiLai = createButton(
            "Tải lại", "/resources/icons/reload.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);

    private final DefaultTableModel modelTiepNhan = new DefaultTableModel(
            new Object[] {
                    "Mã phiếu", "Biển số", "Mã khách hàng", "Tên khách hàng",
                    "Nhân viên", "Ngày lập", "Trạng thái", "Tiến độ", "Ghi chú"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblTiepNhan = new JTable(modelTiepNhan);
    private final TableRowSorter<DefaultTableModel> sorterTiepNhan =
            new TableRowSorter<>(modelTiepNhan);
    private final TiepNhanController controller;

    public FrmTiepNhan() {
        this(null);
    }

    public FrmTiepNhan(TaiKhoan taiKhoanHienTai) {
        this.controller = new TiepNhanController(this, taiKhoanHienTai);
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Tiêu đề trang
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Khung nội dung chính (Form nhập + Bảng dữ liệu)
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormCard(), BorderLayout.NORTH);
        pnlContent.add(createTableCard(), BorderLayout.CENTER);

        add(pnlContent, BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        configureComponents();
        controller.khoiTao();
    }

    // --- Tiêu đề trang ---
    private JPanel createHeaderPanel() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 4));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("TIẾP NHẬN XE");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);

        JLabel lblSubtitle = new JLabel("Lập phiếu tiếp nhận xe và theo dõi tình trạng xử lý tại gara");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    // --- Khung biểu mẫu nhập liệu (Card form) ---
    private JPanel createFormCard() {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 12));

        JLabel lblSection = new JLabel("Thông tin tiếp nhận");
        lblSection.setFont(UIConfig.FONT_SECTION);
        lblSection.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblSection, BorderLayout.NORTH);

        // Panel chứa các trường nhập liệu dạng 2 cột bằng GridBagLayout
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);

        addField(pnlFields, "Mã phiếu tiếp nhận (tự động)", txtMaPhieuTiepNhan, 0, 0);
        addField(pnlFields, "Xe tiếp nhận *", cboXe, 2, 0);
        addField(pnlFields, "Mã khách hàng", txtMaKH, 0, 1);
        addField(pnlFields, "Tên khách hàng", txtTenKhachHang, 2, 1);
        addField(pnlFields, "Nhân viên tiếp nhận *", txtNhanVien, 0, 2);
        addField(pnlFields, "Ngày lập", txtNgayLap, 2, 2);
        addField(pnlFields, "Trạng thái", cboTrangThai, 0, 3);
        addField(pnlFields, "Tiến độ", txtTienDo, 2, 3);

        // Khu vực ghi chú
        JPanel pnlGhiChu = new JPanel(new BorderLayout(0, 4));
        pnlGhiChu.setOpaque(false);
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(UIConfig.FONT_REGULAR);
        lblGhiChu.setForeground(UIConfig.COLOR_MUTED);
        pnlGhiChu.add(lblGhiChu, BorderLayout.NORTH);

        txaGhiChu.setLineWrap(true);
        txaGhiChu.setWrapStyleWord(true);
        txaGhiChu.setFont(UIConfig.FONT_REGULAR);
        txaGhiChu.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane scrGhiChu = new JScrollPane(txaGhiChu);
        scrGhiChu.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        pnlGhiChu.add(scrGhiChu, BorderLayout.CENTER);

        JPanel pnlFormBody = new JPanel(new BorderLayout(0, 8));
        pnlFormBody.setOpaque(false);
        pnlFormBody.add(pnlFields, BorderLayout.NORTH);
        pnlFormBody.add(pnlGhiChu, BorderLayout.CENTER);

        // Hàng nút chức năng
        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton[] buttons = { btnThem, btnCapNhat, btnXoa, btnLamMoi };
        for (int i = 0; i < buttons.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 8);
            pnlButtons.add(buttons[i], gbc);
        }

        pnlCard.add(pnlFormBody, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    // --- Khung danh sách bảng và thanh tìm kiếm ---
    private JPanel createTableCard() {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 10));

        // Header bảng + Toolbar tìm kiếm
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách phiếu tiếp nhận");
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // Thanh tìm kiếm
        JPanel pnlToolbar = new JPanel(new BorderLayout(8, 0));
        pnlToolbar.setOpaque(false);

        JLabel lblTimKiem = new JLabel("Tìm kiếm");
        lblTimKiem.setFont(UIConfig.FONT_REGULAR);
        lblTimKiem.setForeground(UIConfig.COLOR_MUTED);
        ImageIcon iconSearch = getScaledIcon("/resources/icons/search.png", 14, 14);
        if (iconSearch != null) {
            lblTimKiem.setIcon(iconSearch);
            lblTimKiem.setIconTextGap(4);
        }

        pnlToolbar.add(lblTimKiem, BorderLayout.WEST);
        pnlToolbar.add(txtTimKiem, BorderLayout.CENTER);
        pnlToolbar.add(btnTaiLai, BorderLayout.EAST);

        pnlHeader.add(pnlToolbar, BorderLayout.EAST);
        pnlCard.add(pnlHeader, BorderLayout.NORTH);

        // Bảng dữ liệu trong JScrollPane
        JScrollPane scrTable = new JScrollPane(tblTiepNhan);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.add(scrTable, BorderLayout.CENTER);

        return pnlCard;
    }

    private JPanel createStatusPanel() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    // --- Cấu hình thuộc tính cho các thành phần UI ---
    private void configureComponents() {
        // Cấu hình ô nhập liệu
        txtMaPhieuTiepNhan.setEditable(false);
        txtMaKH.setEditable(false);
        txtTenKhachHang.setEditable(false);
        txtNgayLap.setEditable(false);
        txtNhanVien.setEditable(false);
        txtTienDo.setEditable(false);
        txtNhanVien.setBackground(UIConfig.COLOR_BACKGROUND);
        txtTienDo.setBackground(UIConfig.COLOR_BACKGROUND);

        // Cấu hình ComboBox trạng thái
        cboTrangThai.setFont(UIConfig.FONT_REGULAR);
        cboTrangThai.setEditable(true);
        for (String trangThai : TRANG_THAI_MAC_DINH) {
            cboTrangThai.addItem(trangThai);
        }

        // Cấu hình ComboBox xe
        cboXe.setFont(UIConfig.FONT_REGULAR);
        cboXe.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lblItem = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Xe xe) {
                    lblItem.setText(xe.getBienSo() + " - " + xe.getHangXe() + " " + xe.getDongXe());
                }
                return lblItem;
            }
        });

        // Cấu hình JTable chuẩn mực với UIConfig
        tblTiepNhan.setFont(UIConfig.FONT_REGULAR);
        tblTiepNhan.setForeground(UIConfig.COLOR_TEXT);
        tblTiepNhan.setRowHeight(30);
        tblTiepNhan.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblTiepNhan.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblTiepNhan.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblTiepNhan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTiepNhan.setFillsViewportHeight(true);
        tblTiepNhan.setRowSorter(sorterTiepNhan);

        // Header bảng
        tblTiepNhan.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblTiepNhan.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblTiepNhan.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblTiepNhan.getTableHeader().setPreferredSize(new Dimension(0, 34));
    }

    // --- Các hàm tiện ích hỗ trợ style giao diện thuần Swing ---
    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(UIConfig.COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)));
        return card;
    }

    private JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UIConfig.FONT_REGULAR);
        tf.setForeground(UIConfig.COLOR_TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                new EmptyBorder(6, 8, 6, 8)));
        return tf;
    }

    private JButton createButton(String text, String iconPath, Color background, Color foreground) {
        JButton btn = new JButton(text);
        btn.setFont(UIConfig.FONT_BOLD);
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        ImageIcon icon = getScaledIcon(iconPath, 16, 16);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(6);
        }
        return btn;
    }

    private ImageIcon getScaledIcon(String path, int width, int height) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon originalIcon = new ImageIcon(url);
                Image img = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void addField(JPanel pnlFields, String labelText, Component component, int col, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = col;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConfig.FONT_REGULAR);
        lbl.setForeground(UIConfig.COLOR_TEXT);
        pnlFields.add(lbl, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = col + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1.0;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlFields.add(component, gbcInput);
    }

    // --- Các phương thức nghiệp vụ kết nối với Controller ---
    public void hienThiDanhSachXe(List<Xe> danhSachXe) {
        cboXe.removeAllItems();
        for (Xe xe : danhSachXe) {
            cboXe.addItem(xe);
        }
    }

    public void hienThiDanhSachPhieu(List<PhieuTiepNhan> danhSach) {
        modelTiepNhan.setRowCount(0);
        for (PhieuTiepNhan phieu : danhSach) {
            Xe xe = phieu.getXe();
            KhachHang khachHang = phieu.getKhachHang();
            NhanVien nhanVien = phieu.getNhanVien();
            modelTiepNhan.addRow(new Object[] {
                    phieu.getMaPhieuTiepNhan(),
                    xe == null ? "" : xe.getBienSo(),
                    khachHang == null ? "" : khachHang.getMaKH(),
                    khachHang == null ? "" : khachHang.getHoTen(),
                    nhanVien == null ? "" : nhanVien.getHoTen(),
                    dinhDangNgay(phieu.getNgayLap()),
                    phieu.getTrangThai(),
                    phieu.getTienDo(),
                    phieu.getGhiChu()
            });
        }
        locDanhSach();
    }

    public void hienThiThongTinKhachHang(KhachHang khachHang) {
        txtMaKH.setText(khachHang == null ? "" : khachHang.getMaKH());
        txtTenKhachHang.setText(khachHang == null ? "" : khachHang.getHoTen());
    }

    public void hienThiPhieu(PhieuTiepNhan phieu, NhanVien nhanVienDangNhap) {
        txtMaPhieuTiepNhan.setText(phieu.getMaPhieuTiepNhan());
        chonXe(phieu.getXe() == null ? null : phieu.getXe().getMaXe());
        txtNhanVien.setText(nhanVienDangNhap == null
                ? "Không xác định nhân viên đăng nhập"
                : nhanVienDangNhap.getMaNV() + " - " + nhanVienDangNhap.getHoTen());
        txtNgayLap.setText(dinhDangNgay(phieu.getNgayLap()));
        chonTrangThai(phieu.getTrangThai());
        txtTienDo.setText(phieu.getTienDo() == null
            ? PhieuTiepNhan.TIEN_DO_DA_TIEP_NHAN : phieu.getTienDo());
        txaGhiChu.setText(phieu.getGhiChu() == null ? "" : phieu.getGhiChu());
        thongBao("Đang chọn phiếu tiếp nhận " + phieu.getMaPhieuTiepNhan());
    }

    public void lamMoiBieuMau(String maMoi, NhanVien nhanVienDangNhap) {
        txtMaPhieuTiepNhan.setText(maMoi);
        if (cboXe.getItemCount() > 0) {
            cboXe.setSelectedIndex(0);
        } else {
            hienThiThongTinKhachHang(null);
        }
        txtNhanVien.setText(nhanVienDangNhap == null
                ? "Không xác định nhân viên đăng nhập"
                : nhanVienDangNhap.getMaNV() + " - " + nhanVienDangNhap.getHoTen());
        txtNgayLap.setText(dinhDangNgay(new Timestamp(System.currentTimeMillis())));
        chonTrangThai(TRANG_THAI_MAC_DINH[0]);
        txtTienDo.setText(PhieuTiepNhan.TIEN_DO_DA_TIEP_NHAN);
        txaGhiChu.setText("");
        tblTiepNhan.clearSelection();
        thongBao("Sẵn sàng");
    }

    public void locDanhSach() {
        String tuKhoa = txtTimKiem.getText().trim();
        sorterTiepNhan.setRowFilter(tuKhoa.isEmpty()
                ? null
                : RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa)));
        lblThongBao.setText("Hiển thị " + sorterTiepNhan.getViewRowCount()
                + " / " + modelTiepNhan.getRowCount() + " phiếu tiếp nhận");
    }

    public String getMaPhieuNhap() {
        return txtMaPhieuTiepNhan.getText().trim();
    }

    public Xe getXeDuocChon() {
        return (Xe) cboXe.getSelectedItem();
    }

    public String getTrangThaiNhap() {
        Object value = cboTrangThai.getEditor().getItem();
        return value == null ? "" : value.toString().trim();
    }

    public String getTienDoNhap() {
        return txtTienDo.getText().trim();
    }

    public String getGhiChuNhap() {
        return txaGhiChu.getText().trim();
    }

    public String getMaPhieuDangChon() {
        int viewRow = tblTiepNhan.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblTiepNhan.convertRowIndexToModel(viewRow);
        Object value = modelTiepNhan.getValueAt(modelRow, 0);
        return value == null ? null : value.toString();
    }

    public boolean xacNhanXoa(String maPhieu) {
        return JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa phiếu tiếp nhận " + maPhieu + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    public void hienThiLoi(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        lblThongBao.setText(message);
        JOptionPane.showMessageDialog(this, message, "Tiếp nhận xe", JOptionPane.ERROR_MESSAGE);
    }

    // --- Getters trả về thành phần chuẩn Swing ---
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnCapNhat() { return btnCapNhat; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JButton getBtnTaiLai() { return btnTaiLai; }
    public JComboBox<Xe> getCboXe() { return cboXe; }
    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JTable getTblTiepNhan() { return tblTiepNhan; }

    private void chonXe(String maXe) {
        if (maXe == null) {
            return;
        }
        for (int index = 0; index < cboXe.getItemCount(); index++) {
            if (maXe.equals(cboXe.getItemAt(index).getMaXe())) {
                cboXe.setSelectedIndex(index);
                return;
            }
        }
    }

    private void chonTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isEmpty()) {
            cboTrangThai.setSelectedItem(TRANG_THAI_MAC_DINH[0]);
            return;
        }
        boolean daTonTai = false;
        for (int index = 0; index < cboTrangThai.getItemCount(); index++) {
            if (trangThai.equals(cboTrangThai.getItemAt(index))) {
                daTonTai = true;
                break;
            }
        }
        if (!daTonTai) {
            cboTrangThai.addItem(trangThai);
        }
        cboTrangThai.setSelectedItem(trangThai);
    }

    private String dinhDangNgay(Timestamp ngayLap) {
        return ngayLap == null ? "" : DINH_DANG_NGAY.format(ngayLap.toLocalDateTime());
    }
}

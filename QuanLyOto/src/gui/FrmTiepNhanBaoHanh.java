package gui;

import controller.TiepNhanBaoHanhController;
import dao.PhieuYeuCauBaoHanhDAO.LuaChonHoaDon;
import dao.PhieuYeuCauBaoHanhDAO.LuaChonPhuTung;
import dao.PhieuYeuCauBaoHanhDAO.YeuCauHienThi;
import entity.HoaDon;
import entity.PhieuYeuCauBaoHanh;
import entity.PhuTung;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class FrmTiepNhanBaoHanh extends JPanel {
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JTextField txtMaPhieuYCBH = createTextField(14);
    private final JComboBox<LuaChonHoaDon> cboHoaDon = new JComboBox<>();
    private final JComboBox<LuaChonPhuTung> cboPhuTung = new JComboBox<>();
    private final JTextField txtKetQuaBaoHanh = createTextField(20);
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JTextArea txaLyDo = taoOThongTin();
    private final JTextArea txaGhiChuXuLy = taoOThongTin();
    private final JTextField txtTimKiem = createTextField(20);
    private final JLabel lblThongBao = new JLabel(
            "Yêu cầu chờ xử lý được ưu tiên hiển thị trước.");

    private final JButton btnThem = createButton(
            "Tiếp nhận", "/resources/icons/plus.png", UIConfig.COLOR_PRIMARY, Color.WHITE);
    private final JButton btnCapNhat = createButton(
            "Cập nhật", "/resources/icons/update.png", UIConfig.COLOR_SUCCESS, Color.WHITE);
    private final JButton btnXoa = createButton(
            "Xóa", "/resources/icons/trash.png", UIConfig.COLOR_DANGER, Color.WHITE);
    private final JButton btnChapNhan = createButton(
            "Chấp nhận bảo hành", "/resources/icons/plus.png", UIConfig.COLOR_SUCCESS, Color.WHITE);
    private final JButton btnHoanThanh = createButton(
            "Hoàn thành", "/resources/icons/update.png", UIConfig.COLOR_PRIMARY, Color.WHITE);
    private final JButton btnTuChoi = createButton(
            "Từ chối", "/resources/icons/trash.png", UIConfig.COLOR_DANGER, Color.WHITE);
    private final JButton btnLamMoi = createButton(
            "Làm mới", "/resources/icons/reset.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
    private final JButton btnTimKiem = createButton(
            "Tìm kiếm", "/resources/icons/search.png", UIConfig.COLOR_PRIMARY, Color.WHITE);
    private final JButton btnTaiLai = createButton(
            "Tải lại", "/resources/icons/reload.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);

    private final DefaultTableModel modelYeuCau = new DefaultTableModel(
            new Object[] { "Mã yêu cầu", "Hóa đơn", "Phụ tùng", "Khách hàng", "SL", "Ngày tiếp nhận", "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblYeuCau = new JTable(modelYeuCau);
    private final TiepNhanBaoHanhController controller;
    private boolean baoHanhConHan;

    public FrmTiepNhanBaoHanh() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        txtMaPhieuYCBH.setEditable(false);
        txtKetQuaBaoHanh.setEditable(false);
        txtKetQuaBaoHanh.setToolTipText("Kết quả tính theo ngày lập hóa đơn và thời hạn bảo hành");
        cboHoaDon.setFont(UIConfig.FONT_REGULAR);
        cboPhuTung.setFont(UIConfig.FONT_REGULAR);
        spnSoLuong.setFont(UIConfig.FONT_REGULAR);

        // Header trang
        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 14));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungThongTin(), BorderLayout.NORTH);
        pnlNoiDung.add(taoBangYeuCau(), BorderLayout.CENTER);

        add(pnlNoiDung, BorderLayout.CENTER);

        tblYeuCau.setFont(UIConfig.FONT_REGULAR);
        tblYeuCau.setForeground(UIConfig.COLOR_TEXT);
        tblYeuCau.setRowHeight(30);
        tblYeuCau.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblYeuCau.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblYeuCau.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblYeuCau.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblYeuCau.setAutoCreateRowSorter(false);

        tblYeuCau.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblYeuCau.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblYeuCau.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblYeuCau.getTableHeader().setPreferredSize(new Dimension(0, 34));

        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);

        controller = new TiepNhanBaoHanhController(this);
        controller.khoiTao();
    }

    private JPanel createHeaderPanel() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 4));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("TIẾP NHẬN BẢO HÀNH");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);

        JLabel lblSubtitle = new JLabel("Tiếp nhận, xử lý và theo dõi các yêu cầu bảo hành phụ tùng");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel taoKhungThongTin() {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 12));

        JLabel lblSection = new JLabel("Thông tin yêu cầu bảo hành");
        lblSection.setFont(UIConfig.FONT_SECTION);
        lblSection.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblSection, BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(16, 0));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoCacTruong(), BorderLayout.CENTER);
        pnlNoiDung.add(taoGhiChu(), BorderLayout.EAST);
        pnlCard.add(pnlNoiDung, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel taoCacTruong() {
        JPanel pnlTruong = new JPanel(new GridBagLayout());
        pnlTruong.setOpaque(false);

        addField(pnlTruong, "Mã yêu cầu", txtMaPhieuYCBH, 0, 0);
        addField(pnlTruong, "Hóa đơn *", cboHoaDon, 2, 0);
        addField(pnlTruong, "Phụ tùng *", cboPhuTung, 0, 1);
        addField(pnlTruong, "Số lượng *", spnSoLuong, 2, 1);

        JScrollPane scrLyDo = new JScrollPane(txaLyDo);
        scrLyDo.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        addField(pnlTruong, "Lý do yêu cầu *", scrLyDo, 0, 2);
        addField(pnlTruong, "Kết quả bảo hành", txtKetQuaBaoHanh, 2, 2);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 10));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(pnlTruong, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton[] buttons = { btnThem, btnCapNhat, btnXoa, btnChapNhan, btnHoanThanh, btnTuChoi, btnLamMoi };
        for (int i = 0; i < buttons.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 8);
            pnlButtons.add(buttons[i], gbc);
        }

        pnlNoiDung.add(pnlButtons, BorderLayout.SOUTH);
        return pnlNoiDung;
    }

    private JPanel taoGhiChu() {
        JPanel pnlGhiChu = new JPanel(new BorderLayout(0, 5));
        pnlGhiChu.setOpaque(false);
        pnlGhiChu.setPreferredSize(new Dimension(300, 180));

        JLabel lblGhiChu = new JLabel("Ghi chú xử lý");
        lblGhiChu.setFont(UIConfig.FONT_REGULAR);
        lblGhiChu.setForeground(UIConfig.COLOR_MUTED);
        pnlGhiChu.add(lblGhiChu, BorderLayout.NORTH);

        JScrollPane scrGhiChu = new JScrollPane(txaGhiChuXuLy);
        scrGhiChu.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        pnlGhiChu.add(scrGhiChu, BorderLayout.CENTER);
        return pnlGhiChu;
    }

    private JPanel taoBangYeuCau() {
        JPanel pnlBang = createCardPanel();
        pnlBang.setLayout(new BorderLayout(0, 10));

        JPanel pnlDau = new JPanel(new BorderLayout(10, 0));
        pnlDau.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách yêu cầu bảo hành");
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlDau.add(lblTitle, BorderLayout.WEST);

        txtTimKiem.setToolTipText("Tìm mã yêu cầu, hóa đơn, phụ tùng, khách hàng hoặc trạng thái");
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlTimKiem.setOpaque(false);
        pnlTimKiem.add(txtTimKiem);
        pnlTimKiem.add(btnTimKiem);
        pnlTimKiem.add(btnTaiLai);
        pnlDau.add(pnlTimKiem, BorderLayout.EAST);

        pnlBang.add(pnlDau, BorderLayout.NORTH);

        JScrollPane scrTable = new JScrollPane(tblYeuCau);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlBang.add(scrTable, BorderLayout.CENTER);
        pnlBang.add(lblThongBao, BorderLayout.SOUTH);
        return pnlBang;
    }

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

    public void hienThiDanhSachHoaDon(List<LuaChonHoaDon> danhSach) {
        cboHoaDon.removeAllItems();
        for (LuaChonHoaDon hoaDon : danhSach) {
            cboHoaDon.addItem(hoaDon);
        }
    }

    public void hienThiDanhSachPhuTung(List<LuaChonPhuTung> danhSach) {
        cboPhuTung.removeAllItems();
        for (LuaChonPhuTung phuTung : danhSach) {
            cboPhuTung.addItem(phuTung);
        }
    }

    public void hienThiDanhSachYeuCau(List<YeuCauHienThi> danhSach) {
        modelYeuCau.setRowCount(0);
        for (YeuCauHienThi yeuCau : danhSach) {
            modelYeuCau.addRow(new Object[] {
                    yeuCau.maPhieuYCBH(),
                    yeuCau.maHoaDon(),
                    yeuCau.maPhuTung() + " - " + yeuCau.tenPhuTung(),
                    yeuCau.tenKhachHang(),
                    yeuCau.soLuong(),
                    dinhDangNgay(yeuCau.ngayTiepNhan()),
                    yeuCau.trangThai()
            });
        }
        lblThongBao.setText(
                "Thứ tự: Chờ xử lý → Chấp nhận bảo hành → Hoàn thành bảo hành → Từ chối bảo hành.");
    }

    public void hienThiYeuCau(YeuCauHienThi yeuCau, LuaChonPhuTung phuTung,
            boolean choXuLy, boolean daChapNhan) {
        txtMaPhieuYCBH.setText(yeuCau.maPhieuYCBH());
        cboHoaDon.setEnabled(false);
        if (phuTung != null) {
            cboPhuTung.setSelectedItem(phuTung);
        }
        spnSoLuong.setValue(Math.max(1, yeuCau.soLuong()));
        txaLyDo.setText(yeuCau.lyDo() == null ? "" : yeuCau.lyDo());
        txaGhiChuXuLy.setText(yeuCau.ghiChuXuLy() == null ? "" : yeuCau.ghiChuXuLy());
        cboPhuTung.setEnabled(false);
        spnSoLuong.setEnabled(choXuLy);
        txaLyDo.setEditable(choXuLy);
        lblThongBao.setText("Trạng thái: " + yeuCau.trangThai()
                + (yeuCau.ngayXuLy() == null ? "" : " | Xử lý: " + dinhDangNgay(yeuCau.ngayXuLy()))
                + (yeuCau.ngayHoanThanh() == null
                        ? "" : " | Hoàn thành: " + dinhDangNgay(yeuCau.ngayHoanThanh())));
        capNhatTrangThaiNut(false, choXuLy, daChapNhan);
    }

    public void hienThiKetQuaBaoHanh(String noiDung, boolean conHan) {
        baoHanhConHan = conHan;
        txtKetQuaBaoHanh.setText(noiDung);
        txtKetQuaBaoHanh.setForeground(conHan
                ? UIConfig.COLOR_SUCCESS
                : noiDung.startsWith("Chọn hóa đơn") ? UIConfig.COLOR_MUTED : UIConfig.COLOR_DANGER);
    }

    public void capNhatGioiHanSoLuong(int gioiHan) {
        spnSoLuong.setModel(new SpinnerNumberModel(1, 1, gioiHan, 1));
    }

    public void lamMoiBieuMau(String maMoi) {
        tblYeuCau.clearSelection();
        txtMaPhieuYCBH.setText(maMoi);
        cboHoaDon.setEnabled(true);
        cboPhuTung.setEnabled(true);
        spnSoLuong.setEnabled(true);
        txaLyDo.setEditable(true);
        spnSoLuong.setValue(1);
        txaLyDo.setText("");
        txaGhiChuXuLy.setText("");
        hienThiKetQuaBaoHanh("Chọn hóa đơn và phụ tùng", false);
        capNhatTrangThaiNut(true, false, false);
    }

    public void capNhatTrangThaiNut(boolean dangTaoMoi, boolean choXuLy, boolean daChapNhan) {
        btnThem.setEnabled(dangTaoMoi && baoHanhConHan);
        btnCapNhat.setEnabled(choXuLy);
        btnXoa.setEnabled(choXuLy);
        btnChapNhan.setEnabled(choXuLy);
        btnTuChoi.setEnabled(choXuLy);
        btnHoanThanh.setEnabled(daChapNhan);
    }

    public LuaChonHoaDon getHoaDonDuocChon() {
        return (LuaChonHoaDon) cboHoaDon.getSelectedItem();
    }

    public LuaChonPhuTung getPhuTungDuocChon() {
        return (LuaChonPhuTung) cboPhuTung.getSelectedItem();
    }

    public int getDongDangChon() {
        return tblYeuCau.getSelectedRow();
    }

    public String getTuKhoa() {
        return txtTimKiem.getText();
    }

    public void xoaTuKhoa() {
        txtTimKiem.setText("");
    }

    public String getMaYeuCauMoi() {
        return txtMaPhieuYCBH.getText().trim();
    }

    public String getLyDoNhap() {
        return txaLyDo.getText().trim();
    }

    public String getGhiChuXuLyNhap() {
        return txaGhiChuXuLy.getText().trim();
    }

    public PhieuYeuCauBaoHanh taoYeuCauNhap(String maPhieuYCBH) {
        LuaChonHoaDon hoaDon = getHoaDonDuocChon();
        LuaChonPhuTung phuTung = getPhuTungDuocChon();
        HoaDon hoaDonEntity = null;
        if (hoaDon != null) {
            hoaDonEntity = new HoaDon();
            hoaDonEntity.setMaHoaDon(hoaDon.maHoaDon());
        }
        PhuTung phuTungEntity = null;
        if (phuTung != null) {
            phuTungEntity = new PhuTung();
            phuTungEntity.setMaPhuTung(phuTung.maPhuTung());
        }
        return new PhieuYeuCauBaoHanh(maPhieuYCBH, hoaDonEntity, phuTungEntity,
                (Integer) spnSoLuong.getValue(), null, null, txaLyDo.getText().trim(),
                txaGhiChuXuLy.getText().trim(), null, null);
    }

    public boolean kiemTraThem(boolean baoHanhConHan) {
        if (getHoaDonDuocChon() == null || getPhuTungDuocChon() == null) {
            thongBao("Vui lòng chọn hóa đơn và phụ tùng còn hạn bảo hành.", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!baoHanhConHan) {
            thongBao("Phụ tùng đã hết hạn hoặc không xác định được thời hạn bảo hành nên không thể lập phiếu.",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return kiemTraLyDo();
    }

    public boolean kiemTraLyDo() {
        if (!txaLyDo.getText().isBlank()) {
            return true;
        }
        thongBao("Vui lòng nhập lý do yêu cầu bảo hành.", JOptionPane.WARNING_MESSAGE);
        txaLyDo.requestFocusInWindow();
        return false;
    }

    public boolean kiemTraGhiChu(String loiNhac) {
        if (!txaGhiChuXuLy.getText().isBlank()) {
            return true;
        }
        thongBao(loiNhac, JOptionPane.WARNING_MESSAGE);
        txaGhiChuXuLy.requestFocusInWindow();
        return false;
    }

    public boolean xacNhanXoa(String maPhieuYCBH) {
        return JOptionPane.showConfirmDialog(this, "Xóa yêu cầu " + maPhieuYCBH + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION;
    }

    public boolean xacNhan(String loiNhac) {
        return JOptionPane.showConfirmDialog(this, loiNhac, "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void thongBao(String noiDung, int loai) {
        JOptionPane.showMessageDialog(this, noiDung, "Yêu cầu bảo hành", loai);
    }

    public void baoLoi(String tieuDe, Exception exception) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + exception.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void hienThiLoi(String noiDung) {
        thongBao(noiDung, JOptionPane.WARNING_MESSAGE);
    }

    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnCapNhat() { return btnCapNhat; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnChapNhan() { return btnChapNhan; }
    public JButton getBtnHoanThanh() { return btnHoanThanh; }
    public JButton getBtnTuChoi() { return btnTuChoi; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JButton getBtnTimKiem() { return btnTimKiem; }
    public JButton getBtnTaiLai() { return btnTaiLai; }
    public JComboBox<LuaChonHoaDon> getCboHoaDon() { return cboHoaDon; }
    public JComboBox<LuaChonPhuTung> getCboPhuTung() { return cboPhuTung; }
    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JTable getTblYeuCau() { return tblYeuCau; }

    public void chonHoaDon(String maHoaDon) {
        for (int i = 0; i < cboHoaDon.getItemCount(); i++) {
            if (maHoaDon.equals(cboHoaDon.getItemAt(i).maHoaDon())) {
                cboHoaDon.setSelectedIndex(i);
                return;
            }
        }
    }

    public LuaChonPhuTung timPhuTung(String maPhuTung) {
        for (int i = 0; i < cboPhuTung.getItemCount(); i++) {
            if (maPhuTung.equals(cboPhuTung.getItemAt(i).maPhuTung())) {
                return cboPhuTung.getItemAt(i);
            }
        }
        return null;
    }

    public void themPhuTung(LuaChonPhuTung phuTung) {
        cboPhuTung.addItem(phuTung);
    }

    public void chonYeuCau(String maPhieuYCBH) {
        for (int row = 0; row < modelYeuCau.getRowCount(); row++) {
            if (maPhieuYCBH.equals(modelYeuCau.getValueAt(row, 0))) {
                tblYeuCau.setRowSelectionInterval(row, row);
                tblYeuCau.scrollRectToVisible(tblYeuCau.getCellRect(row, 0, true));
                return;
            }
        }
    }

    private static JTextArea taoOThongTin() {
        JTextArea txa = new JTextArea(3, 18);
        txa.setFont(UIConfig.FONT_REGULAR);
        txa.setLineWrap(true);
        txa.setWrapStyleWord(true);
        return txa;
    }

    private static String dinhDangNgay(Timestamp ngay) {
        return ngay == null ? "" : ngay.toLocalDateTime().format(DINH_DANG_NGAY_GIO);
    }
}

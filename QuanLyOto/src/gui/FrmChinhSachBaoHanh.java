package gui;

import controller.ChinhSachBaoHanhController;
import entity.ChinhSachBaoHanh;
import entity.PhuTung;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

public class FrmChinhSachBaoHanh extends JPanel {
    private record DonViThoiGian(String ma, String ten) {
        @Override
        public String toString() {
            return ten;
        }
    }

    private final ChinhSachBaoHanhController controller;
    private final JTextField txtMaCSBH = createTextField(12);
    private final JTextField txtTenChinhSach = createTextField(24);
    private final JSpinner spnThoiHan = new JSpinner(
            new SpinnerNumberModel(12, 1, 9999, 1));
    private final JComboBox<DonViThoiGian> cboDonViThoiGian = new JComboBox<>(new DonViThoiGian[] {
            new DonViThoiGian("THANG", "Tháng"),
            new DonViThoiGian("NGAY", "Ngày"),
            new DonViThoiGian("NAM", "Năm")
    });
    private final JCheckBox chkTrangThai = new JCheckBox("Đang áp dụng", true);
    private final JTextArea txaDieuKien = taoOThongTin();
    private final JTextArea txaTruongHopTuChoi = taoOThongTin();
    private final JTextArea txaNoiDungBaoHanh = taoOThongTin();
    private final JTextField txtTimPhuTung = createTextField(16);
    private final JTextField txtTimChinhSach = createTextField(18);

    private final JButton btnThem = createButton(
            "Thêm", "/resources/icons/plus.png", UIConfig.COLOR_PRIMARY, Color.WHITE);
    private final JButton btnCapNhat = createButton(
            "Cập nhật", "/resources/icons/update.png", UIConfig.COLOR_SUCCESS, Color.WHITE);
    private final JButton btnXoa = createButton(
            "Xóa", "/resources/icons/trash.png", UIConfig.COLOR_DANGER, Color.WHITE);
    private final JButton btnLamMoi = createButton(
            "Làm mới", "/resources/icons/reset.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
    private final JButton btnTimKiem = createButton(
            "Tìm kiếm", "/resources/icons/search.png", UIConfig.COLOR_PRIMARY, Color.WHITE);

    private final Set<String> dsMaPhuTungDangChon = new LinkedHashSet<>();
    private final List<PhuTung> dsPhuTung = new ArrayList<>();
    private boolean dangNapBangPhuTung;
    private boolean dangThemMoi = true;

    private final DefaultTableModel modelPhuTung = new DefaultTableModel(
            new Object[] { "Áp dụng", "Mã phụ tùng", "Tên phụ tùng", "Tồn kho" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : column == 3 ? Integer.class : String.class;
        }
    };
    private final JTable tblPhuTung = new JTable(modelPhuTung);

    private final DefaultTableModel modelChinhSach = new DefaultTableModel(
            new Object[] { "Mã chính sách", "Tên chính sách", "Thời hạn", "Phụ tùng", "Trạng thái", "Ngày tạo" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblChinhSach = new JTable(modelChinhSach);

    public FrmChinhSachBaoHanh() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        txtMaCSBH.setEditable(false);
        spnThoiHan.setFont(UIConfig.FONT_REGULAR);
        cboDonViThoiGian.setFont(UIConfig.FONT_REGULAR);
        chkTrangThai.setFont(UIConfig.FONT_REGULAR);
        chkTrangThai.setOpaque(false);

        // Header trang
        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 14));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungChinhSach(), BorderLayout.NORTH);
        pnlNoiDung.add(taoBangChinhSach(), BorderLayout.CENTER);

        add(pnlNoiDung, BorderLayout.CENTER);

        styleTable(tblPhuTung);
        styleTable(tblChinhSach);
        tblPhuTung.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChinhSach.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChinhSach.setAutoCreateRowSorter(false);

        controller = new ChinhSachBaoHanhController(this);
        controller.taiDuLieuBanDau();
    }

    private JPanel createHeaderPanel() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 4));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("CHÍNH SÁCH BẢO HÀNH");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);

        JLabel lblSubtitle = new JLabel("Quản lý thời hạn, điều kiện và phụ tùng được áp dụng bảo hành");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel taoKhungChinhSach() {
        JPanel pnlCard = createCardPanel();
        pnlCard.setLayout(new BorderLayout(0, 12));

        JLabel lblSection = new JLabel("Thông tin chính sách");
        lblSection.setFont(UIConfig.FONT_SECTION);
        lblSection.setForeground(UIConfig.COLOR_TITLE);
        pnlCard.add(lblSection, BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(16, 0));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungThongTin(), BorderLayout.CENTER);
        pnlNoiDung.add(taoKhungPhuTung(), BorderLayout.EAST);
        pnlCard.add(pnlNoiDung, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel taoKhungThongTin() {
        JPanel pnlThongTin = new JPanel(new BorderLayout(0, 8));
        pnlThongTin.setOpaque(false);

        JPanel pnlTruong = new JPanel(new GridBagLayout());
        pnlTruong.setOpaque(false);

        addField(pnlTruong, "Mã chính sách", txtMaCSBH, 0, 0);
        addField(pnlTruong, "Tên chính sách *", txtTenChinhSach, 2, 0);

        JPanel pnlThoiHan = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlThoiHan.setOpaque(false);
        pnlThoiHan.add(spnThoiHan);
        pnlThoiHan.add(cboDonViThoiGian);
        addField(pnlTruong, "Thời hạn *", pnlThoiHan, 0, 1);
        addField(pnlTruong, "Trạng thái", chkTrangThai, 2, 1);
        pnlThongTin.add(pnlTruong, BorderLayout.NORTH);

        JPanel pnlMoTa = new JPanel(new GridLayout(1, 3, 8, 0));
        pnlMoTa.setOpaque(false);
        pnlMoTa.add(taoTruongMoTa("Nội dung bảo hành", txaNoiDungBaoHanh));
        pnlMoTa.add(taoTruongMoTa("Điều kiện bảo hành", txaDieuKien));
        pnlMoTa.add(taoTruongMoTa("Trường hợp từ chối", txaTruongHopTuChoi));
        pnlThongTin.add(pnlMoTa, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new GridBagLayout());
        pnlNut.setOpaque(false);
        JButton[] buttons = { btnThem, btnCapNhat, btnXoa, btnLamMoi };
        for (int i = 0; i < buttons.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 8);
            pnlNut.add(buttons[i], gbc);
        }

        pnlThongTin.add(pnlNut, BorderLayout.SOUTH);
        return pnlThongTin;
    }

    private JPanel taoTruongMoTa(String nhan, JTextArea txaNoiDung) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 5));
        pnlTruong.setOpaque(false);
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(UIConfig.FONT_REGULAR);
        lbl.setForeground(UIConfig.COLOR_MUTED);
        pnlTruong.add(lbl, BorderLayout.NORTH);

        JScrollPane scr = new JScrollPane(txaNoiDung);
        scr.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        pnlTruong.add(scr, BorderLayout.CENTER);
        return pnlTruong;
    }

    private JPanel taoKhungPhuTung() {
        JPanel pnlPhuTung = new JPanel(new BorderLayout(0, 8));
        pnlPhuTung.setOpaque(false);
        pnlPhuTung.setPreferredSize(new Dimension(410, 250));

        JLabel lblTitle = new JLabel("Phụ tùng áp dụng *");
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlPhuTung.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 6));
        pnlDanhSach.setOpaque(false);
        txtTimPhuTung.setToolTipText("Lọc phụ tùng theo mã hoặc tên");
        pnlDanhSach.add(txtTimPhuTung, BorderLayout.NORTH);

        JScrollPane scrPhuTung = new JScrollPane(tblPhuTung);
        scrPhuTung.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrPhuTung.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlDanhSach.add(scrPhuTung, BorderLayout.CENTER);

        pnlPhuTung.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlPhuTung;
    }

    private JPanel taoBangChinhSach() {
        JPanel pnlBang = createCardPanel();
        pnlBang.setLayout(new BorderLayout(0, 10));

        JPanel pnlThanhTimKiem = new JPanel(new BorderLayout(10, 0));
        pnlThanhTimKiem.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách chính sách");
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        pnlThanhTimKiem.add(lblTitle, BorderLayout.WEST);

        txtTimChinhSach.setToolTipText("Tìm theo mã, tên, nội dung hoặc điều kiện bảo hành");
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlTimKiem.setOpaque(false);
        pnlTimKiem.add(txtTimChinhSach);
        pnlTimKiem.add(btnTimKiem);
        pnlThanhTimKiem.add(pnlTimKiem, BorderLayout.EAST);

        pnlBang.add(pnlThanhTimKiem, BorderLayout.NORTH);

        JScrollPane scrTable = new JScrollPane(tblChinhSach);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlBang.add(scrTable, BorderLayout.CENTER);

        return pnlBang;
    }

    private void styleTable(JTable table) {
        table.setFont(UIConfig.FONT_REGULAR);
        table.setForeground(UIConfig.COLOR_TEXT);
        table.setRowHeight(28);
        table.setGridColor(UIConfig.COLOR_TABLE_GRID);
        table.setSelectionBackground(UIConfig.COLOR_SELECTION);
        table.setSelectionForeground(UIConfig.COLOR_TEXT);

        table.getTableHeader().setFont(UIConfig.FONT_BOLD);
        table.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        table.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
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

    public void hienThiDanhSachChinhSach(List<ChinhSachBaoHanh> dsChinhSach) {
        modelChinhSach.setRowCount(0);
        for (ChinhSachBaoHanh chinhSach : dsChinhSach) {
            modelChinhSach.addRow(new Object[] {
                    chinhSach.getMaCSBH(),
                    chinhSach.getTenChinhSach(),
                    hienThiThoiHan(chinhSach.getThoiHanBaoHanh(), chinhSach.getDonViThoiGian()),
                    chinhSach.getDsPhuTung().size(),
                    chinhSach.isTrangThai() ? "Đang áp dụng" : "Ngưng áp dụng",
                    chinhSach.getNgayTao() == null ? "" : chinhSach.getNgayTao().toLocalDateTime()
                            .toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            });
        }
    }

    public void hienThiChinhSach(ChinhSachBaoHanh chinhSach) {
        dangThemMoi = false;
        txtMaCSBH.setText(chinhSach.getMaCSBH());
        txtTenChinhSach.setText(chinhSach.getTenChinhSach());
        spnThoiHan.setValue(chinhSach.getThoiHanBaoHanh());
        chonDonViThoiGian(chinhSach.getDonViThoiGian());
        txaDieuKien.setText(chinhSach.getDieuKienBaoHanh());
        txaTruongHopTuChoi.setText(chinhSach.getTruongHopTuChoi());
        txaNoiDungBaoHanh.setText(chinhSach.getNoiDungBaoHanh());
        chkTrangThai.setSelected(chinhSach.isTrangThai());
        dsMaPhuTungDangChon.clear();
        for (PhuTung phuTung : chinhSach.getDsPhuTung()) {
            dsMaPhuTungDangChon.add(phuTung.getMaPhuTung());
        }
        locDanhSachPhuTung();
    }

    public ChinhSachBaoHanh layChinhSachNhap() {
        String maCSBH = txtMaCSBH.getText().trim();
        if (txtTenChinhSach.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Tên chính sách không được để trống.",
                    "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        DonViThoiGian donVi = (DonViThoiGian) cboDonViThoiGian.getSelectedItem();
        return new ChinhSachBaoHanh(
                maCSBH,
                txtTenChinhSach.getText().trim(),
                (Integer) spnThoiHan.getValue(),
                donVi.ma(),
                txaDieuKien.getText().trim(),
                txaTruongHopTuChoi.getText().trim(),
                txaNoiDungBaoHanh.getText().trim(),
                null,
                chkTrangThai.isSelected(),
                dsPhuTung.stream()
                        .filter(phuTung -> dsMaPhuTungDangChon.contains(phuTung.getMaPhuTung()))
                        .toList());
    }

    public void hienThiDanhSachPhuTung(List<PhuTung> dsPhuTung) {
        this.dsPhuTung.clear();
        this.dsPhuTung.addAll(dsPhuTung);
        locDanhSachPhuTung();
    }

    public void locDanhSachPhuTung() {
        dangNapBangPhuTung = true;
        String tuKhoa = txtTimPhuTung.getText().trim().toLowerCase();
        modelPhuTung.setRowCount(0);
        for (PhuTung phuTung : dsPhuTung) {
            String ma = phuTung.getMaPhuTung() == null ? "" : phuTung.getMaPhuTung().toLowerCase();
            String ten = phuTung.getTenPhuTung() == null ? "" : phuTung.getTenPhuTung().toLowerCase();
            if (tuKhoa.isEmpty() || ma.contains(tuKhoa) || ten.contains(tuKhoa)) {
                modelPhuTung.addRow(new Object[] {
                        dsMaPhuTungDangChon.contains(phuTung.getMaPhuTung()),
                        phuTung.getMaPhuTung(),
                        phuTung.getTenPhuTung(),
                        phuTung.getSoLuongTon()
                });
            }
        }
        dangNapBangPhuTung = false;
    }

    public void capNhatPhuTungDuocChon(int dong) {
        if (dangNapBangPhuTung || dong < 0 || dong >= modelPhuTung.getRowCount()) {
            return;
        }
        String maPhuTung = String.valueOf(modelPhuTung.getValueAt(dong, 1));
        boolean duocChon = Boolean.TRUE.equals(modelPhuTung.getValueAt(dong, 0));
        if (duocChon) {
            dsMaPhuTungDangChon.add(maPhuTung);
        } else {
            dsMaPhuTungDangChon.remove(maPhuTung);
        }
    }

    public void hienThiMaChinhSach(String maCSBH) {
        txtMaCSBH.setText(maCSBH);
    }

    public void datTrangThaiThemMoi(boolean themMoi) {
        dangThemMoi = themMoi;
    }

    public void lamMoiForm(String maCSBH) {
        tblChinhSach.clearSelection();
        dangThemMoi = true;
        txtMaCSBH.setText(maCSBH);
        txtTenChinhSach.setText("");
        spnThoiHan.setValue(12);
        cboDonViThoiGian.setSelectedIndex(0);
        txaDieuKien.setText("");
        txaTruongHopTuChoi.setText("");
        txaNoiDungBaoHanh.setText("");
        chkTrangThai.setSelected(true);
        dsMaPhuTungDangChon.clear();
        locDanhSachPhuTung();
    }

    public void chonChinhSach(String maCSBH) {
        for (int row = 0; row < modelChinhSach.getRowCount(); row++) {
            if (maCSBH.equals(modelChinhSach.getValueAt(row, 0))) {
                tblChinhSach.setRowSelectionInterval(row, row);
                tblChinhSach.scrollRectToVisible(tblChinhSach.getCellRect(row, 0, true));
                return;
            }
        }
    }

    public String layMaChinhSachDangChon() {
        int row = tblChinhSach.getSelectedRow();
        return row < 0 ? null : String.valueOf(modelChinhSach.getValueAt(row, 0));
    }

    public String layMaChinhSachNhap() {
        return txtMaCSBH.getText().trim();
    }

    public String layTuKhoaChinhSach() {
        return txtTimChinhSach.getText();
    }

    public boolean laThemMoi() {
        return dangThemMoi;
    }

    public void xoaTuKhoaChinhSach() {
        txtTimChinhSach.setText("");
    }

    public boolean xacNhanXoa(String maCSBH) {
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa chính sách " + maCSBH + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return luaChon == JOptionPane.YES_OPTION;
    }

    public void thongBaoCanhBao(String tieuDe, String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, tieuDe, JOptionPane.WARNING_MESSAGE);
    }

    public void thongBaoThanhCong(String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public JButton layNutThem() {
        return btnThem;
    }

    public JButton layNutCapNhat() {
        return btnCapNhat;
    }

    public JButton layNutXoa() {
        return btnXoa;
    }

    public JButton layNutLamMoi() {
        return btnLamMoi;
    }

    public JButton layNutTimKiem() {
        return btnTimKiem;
    }

    public JTextField layOTimChinhSach() {
        return txtTimChinhSach;
    }

    public JTextField layOTimPhuTung() {
        return txtTimPhuTung;
    }

    public JTable layBangChinhSach() {
        return tblChinhSach;
    }

    public JTable layBangPhuTung() {
        return tblPhuTung;
    }

    private void chonDonViThoiGian(String maDonVi) {
        for (int index = 0; index < cboDonViThoiGian.getItemCount(); index++) {
            if (cboDonViThoiGian.getItemAt(index).ma().equals(maDonVi)) {
                cboDonViThoiGian.setSelectedIndex(index);
                return;
            }
        }
    }

    private static String hienThiThoiHan(int thoiHan, String donVi) {
        String tenDonVi = switch (donVi) {
            case "NGAY" -> "ngày";
            case "NAM" -> "năm";
            default -> "tháng";
        };
        return thoiHan + " " + tenDonVi;
    }

    private static JTextArea taoOThongTin() {
        JTextArea txa = new JTextArea(3, 16);
        txa.setLineWrap(true);
        txa.setWrapStyleWord(true);
        txa.setFont(UIConfig.FONT_REGULAR);
        txa.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        return txa;
    }

    public void baoLoi(String tieuDe, String noiDung) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + noiDung,
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

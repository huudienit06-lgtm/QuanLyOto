package gui;

import controller.PhieuKiemTraController;
import dao.LichLamViecDAO;
import entity.PhieuKiemTra;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmPhieuKiemTra extends JPanel {
    private final PhieuKiemTraController controller = new PhieuKiemTraController(this);
    private final Map<String, String> bienSoTheoMaXe = new HashMap<>();
    private final Map<String, String> tenKhachHangTheoMaKH = new HashMap<>();

    private final JTextField txtMaPKT = createTextField(16);
    private final JComboBox<PhieuTiepNhan> cboMaPTN = new JComboBox<>();
    private final JTextField txtMaKH = createTextField(16);
    private final JTextField txtMaXe = createTextField(16);
    private final JTextField txtBienSo = createTextField(16);
    private final JComboBox<LichLamViecDAO.NhanVienTrongCa> cboNhanVienKiemTra = new JComboBox<>();
    private final JTextArea txaTrangThai = new JTextArea(2, 20);
    private final JTextArea txaVanDe = new JTextArea(3, 20);
    private final JTextField txtLocMaPKT = createTextField(12);
    private final JTextField txtLocMaPTN = createTextField(12);
    private final JTextField txtLocMaXe = createTextField(12);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final JButton btnThem = createButton(
            "Thêm phiếu", "/resources/icons/plus.png", UIConfig.COLOR_PRIMARY, Color.WHITE);
    private final JButton btnSua = createButton(
            "Cập nhật", "/resources/icons/update.png", UIConfig.COLOR_SUCCESS, Color.WHITE);
    private final JButton btnXoa = createButton(
            "Xóa phiếu", "/resources/icons/trash.png", UIConfig.COLOR_DANGER, Color.WHITE);
    private final JButton btnLamMoi = createButton(
            "Làm mới", "/resources/icons/reset.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);
    private final JButton btnTaiLai = createButton(
            "Tải lại", "/resources/icons/reload.png", UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);

    private final DefaultTableModel modelPhieuKiemTra = new DefaultTableModel(
            new Object[] {
                    "Mã phiếu kiểm tra", "Mã phiếu tiếp nhận", "Mã khách hàng", "Mã xe",
                    "Mã nhân viên", "Trạng thái ban đầu", "Vấn đề hư hỏng"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblPhieuKiemTra = new JTable(modelPhieuKiemTra);
    private final TableRowSorter<DefaultTableModel> sorterPhieuKiemTra =
            new TableRowSorter<>(modelPhieuKiemTra);

    public FrmPhieuKiemTra() {
        this(null);
    }

    public FrmPhieuKiemTra(TaiKhoan taiKhoanHienTai) {
        setLayout(new BorderLayout(0, 8));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(14, 22, 14, 22));

        add(createHeading(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        cauHinhThanhPhan();
        controller.khoiTao();
    }

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 5));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("PHIẾU KIỂM TRA");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);

        JLabel lblSubtitle = new JLabel("Lập phiếu kiểm tra tình trạng xe và tra cứu các phiếu đã lập");
        lblSubtitle.setFont(UIConfig.FONT_REGULAR);
        lblSubtitle.setForeground(UIConfig.COLOR_MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);

        JPanel pnlForm = createFormPanel();
        int chieuCaoForm = pnlForm.getPreferredSize().height;
        pnlForm.setMinimumSize(new Dimension(0, chieuCaoForm));
        JSplitPane splContent = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, pnlForm, createTablePanel());
        splContent.setBorder(null);
        splContent.setResizeWeight(0.0);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        SwingUtilities.invokeLater(() -> splContent.setDividerLocation(chieuCaoForm));
        pnlContent.add(splContent, BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormPanel() {
        JPanel pnlFormCard = createCardPanel();
        pnlFormCard.setLayout(new BorderLayout(0, 6));

        JLabel lblFormTitle = createSectionTitle("Thông tin phiếu kiểm tra");
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã phiếu kiểm tra (tự động)", txtMaPKT, 0, 0);
        addField(pnlFields, "Phiếu tiếp nhận *", cboMaPTN, 2, 0);
        addField(pnlFields, "Mã khách hàng", txtMaKH, 0, 1);
        addField(pnlFields, "Mã xe", txtMaXe, 2, 1);
        addField(pnlFields, "Nhân viên kiểm tra *", cboNhanVienKiemTra, 0, 2);
        addField(pnlFields, "Biển số xe", txtBienSo, 2, 2);
        addLongField(pnlFields, "Trạng thái xe ban đầu *", txaTrangThai, 0, 3);
        addLongField(pnlFields, "Vấn đề hư hỏng *", txaVanDe, 2, 3);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton[] buttons = { btnThem, btnSua, btnXoa, btnLamMoi };
        for (int i = 0; i < buttons.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i;
            gbc.insets = new Insets(3, 0, 2, 8);
            gbc.anchor = GridBagConstraints.WEST;
            pnlButtons.add(buttons[i], gbc);
        }

        JPanel pnlFieldsAndButtons = new JPanel();
        pnlFieldsAndButtons.setOpaque(false);
        pnlFieldsAndButtons.setLayout(new BoxLayout(pnlFieldsAndButtons, BoxLayout.Y_AXIS));
        pnlFields.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlFields.getPreferredSize().height));
        pnlButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlButtons.getPreferredSize().height));
        pnlFieldsAndButtons.add(pnlFields);
        pnlFieldsAndButtons.add(Box.createVerticalStrut(8));
        pnlFieldsAndButtons.add(pnlButtons);

        pnlFormCard.add(lblFormTitle, BorderLayout.NORTH);
        pnlFormCard.add(pnlFieldsAndButtons, BorderLayout.CENTER);
        return pnlFormCard;
    }

    private JPanel createTablePanel() {
        JPanel pnlTableCard = createCardPanel();
        pnlTableCard.setLayout(new BorderLayout(0, 8));

        JPanel pnlTableHeader = new JPanel(new BorderLayout(8, 8));
        pnlTableHeader.setOpaque(false);
        JPanel pnlTitleBar = new JPanel(new BorderLayout());
        pnlTitleBar.setOpaque(false);
        pnlTitleBar.add(createSectionTitle("Danh sách phiếu kiểm tra"), BorderLayout.WEST);
        pnlTitleBar.add(btnTaiLai, BorderLayout.EAST);

        JPanel pnlLoc = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã phiếu kiểm tra", txtLocMaPKT));
        pnlLoc.add(createSearchField("Mã phiếu tiếp nhận", txtLocMaPTN));
        pnlLoc.add(createSearchField("Mã xe", txtLocMaXe));
        pnlTableHeader.add(pnlTitleBar, BorderLayout.NORTH);
        pnlTableHeader.add(pnlLoc, BorderLayout.CENTER);

        tblPhieuKiemTra.setFont(UIConfig.FONT_REGULAR);
        tblPhieuKiemTra.setForeground(UIConfig.COLOR_TEXT);
        tblPhieuKiemTra.setRowHeight(30);
        tblPhieuKiemTra.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblPhieuKiemTra.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblPhieuKiemTra.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblPhieuKiemTra.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhieuKiemTra.setFillsViewportHeight(true);
        tblPhieuKiemTra.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblPhieuKiemTra.setRowSorter(sorterPhieuKiemTra);

        tblPhieuKiemTra.getTableHeader().setFont(UIConfig.FONT_BOLD);
        tblPhieuKiemTra.getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        tblPhieuKiemTra.getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        tblPhieuKiemTra.getTableHeader().setPreferredSize(new Dimension(0, 34));

        pnlTableCard.add(pnlTableHeader, BorderLayout.NORTH);
        JScrollPane scrTable = new JScrollPane(tblPhieuKiemTra);
        scrTable.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrTable.getViewport().setBackground(UIConfig.COLOR_SURFACE);
        pnlTableCard.add(scrTable, BorderLayout.CENTER);
        return pnlTableCard;
    }

    private JPanel createStatusBar() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private JLabel createSectionTitle(String text) {
        JLabel lblTitle = new JLabel(text);
        lblTitle.setFont(UIConfig.FONT_SECTION);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        return lblTitle;
    }

    private void addField(JPanel pnlFields, String label, Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(2, 0, 2, 8);
        JLabel lblField = new JLabel(label);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_TEXT);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(2, 0, 2, 12);
        pnlFields.add(component, gbcInput);
    }

    private void addLongField(
            JPanel pnlFields, String label, JTextArea txaField, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.gridwidth = 2;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(4, 0, 2, column == 0 ? 12 : 0);
        JLabel lblField = new JLabel(label);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_TEXT);
        pnlFields.add(lblField, gbcLabel);

        txaField.setLineWrap(true);
        txaField.setWrapStyleWord(true);
        txaField.setRows(3);
        txaField.setFont(UIConfig.FONT_REGULAR);
        txaField.setForeground(UIConfig.COLOR_TEXT);
        txaField.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));

        JScrollPane scrField = new JScrollPane(txaField);
        scrField.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrField.setPreferredSize(new Dimension(0, 108));
        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column;
        gbcInput.gridy = row + 1;
        gbcInput.gridwidth = 2;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(0, 0, 2, column == 0 ? 12 : 0);
        pnlFields.add(scrField, gbcInput);
    }

    private JPanel createSearchField(String labelText, JTextField txtField) {
        JPanel pnl = new JPanel(new BorderLayout(4, 0));
        pnl.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConfig.FONT_REGULAR);
        lbl.setForeground(UIConfig.COLOR_MUTED);
        pnl.add(lbl, BorderLayout.WEST);
        pnl.add(txtField, BorderLayout.CENTER);
        return pnl;
    }

    private void cauHinhThanhPhan() {
        txtMaPKT.setEditable(false);
        for (JTextField txtField : new JTextField[] {txtMaKH, txtMaXe, txtBienSo}) {
            txtField.setEditable(false);
            txtField.setBackground(UIConfig.COLOR_BACKGROUND);
        }
        cboMaPTN.setFont(UIConfig.FONT_REGULAR);
        cboNhanVienKiemTra.setFont(UIConfig.FONT_REGULAR);
        cboNhanVienKiemTra.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lblItem = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof LichLamViecDAO.NhanVienTrongCa nhanVien) {
                    lblItem.setText(nhanVien.hoTen() + " (" + nhanVien.maNV() + ")");
                }
                return lblItem;
            }
        });
        cboMaPTN.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lblItem = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhieuTiepNhan phieuTiepNhan) {
                    lblItem.setText(thongTinPhieuTiepNhan(phieuTiepNhan));
                }
                return lblItem;
            }
        });
        tblPhieuKiemTra.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblPhieuKiemTra.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblPhieuKiemTra.getColumnModel().getColumn(5).setPreferredWidth(180);
        tblPhieuKiemTra.getColumnModel().getColumn(6).setPreferredWidth(220);
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(UIConfig.COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)));
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

    public void hienThiDanhSachNhanVienTrongCa(
            List<LichLamViecDAO.NhanVienTrongCa> danhSach) {
        cboNhanVienKiemTra.removeAllItems();
        for (LichLamViecDAO.NhanVienTrongCa nhanVien : danhSach) {
            cboNhanVienKiemTra.addItem(nhanVien);
        }
        cboNhanVienKiemTra.setToolTipText(cboNhanVienKiemTra.getItemCount() == 0
                ? "Không có nhân viên được phân công trong ca hiện tại."
                : "Chọn nhân viên được phân công trong ca hiện tại.");
    }

    public void hienThiDanhSachPhieuTiepNhan(
            List<PhieuTiepNhan> danhSach,
            Map<String, String> bienSoTheoMaXeMoi,
            Map<String, String> tenKhachHangTheoMaKHMoi) {
        bienSoTheoMaXe.clear();
        bienSoTheoMaXe.putAll(bienSoTheoMaXeMoi);
        tenKhachHangTheoMaKH.clear();
        tenKhachHangTheoMaKH.putAll(tenKhachHangTheoMaKHMoi);
        cboMaPTN.removeAllItems();
        for (PhieuTiepNhan phieuTiepNhan : danhSach) {
            cboMaPTN.addItem(phieuTiepNhan);
        }
    }

    public void capNhatThongTinTheoPhieuTiepNhan() {
        PhieuTiepNhan phieuTiepNhan = (PhieuTiepNhan) cboMaPTN.getSelectedItem();
        if (phieuTiepNhan == null) {
            txtMaKH.setText("");
            txtMaXe.setText("");
            txtBienSo.setText("");
            return;
        }
        txtMaKH.setText(phieuTiepNhan.getKhachHang().getMaKH());
        txtMaXe.setText(phieuTiepNhan.getXe().getMaXe());
        txtBienSo.setText(giaTriThongTin(bienSoTheoMaXe, phieuTiepNhan.getXe().getMaXe()));
    }

    private String thongTinPhieuTiepNhan(PhieuTiepNhan phieuTiepNhan) {
        String bienSo = giaTriThongTin(bienSoTheoMaXe, phieuTiepNhan.getXe().getMaXe());
        String tenKhachHang = giaTriThongTin(
                tenKhachHangTheoMaKH, phieuTiepNhan.getKhachHang().getMaKH());
        return phieuTiepNhan.getMaPhieuTiepNhan()
                + " - Biển số: " + bienSo
                + " - Khách hàng: " + tenKhachHang;
    }

    private String giaTriThongTin(Map<String, String> danhSach, String khoa) {
        if (khoa == null) {
            return "";
        }
        String giaTri = danhSach.get(khoa);
        return giaTri == null ? "" : giaTri;
    }

    public void hienThiDanhSach(List<PhieuKiemTra> danhSach) {
        modelPhieuKiemTra.setRowCount(0);
        for (PhieuKiemTra phieuKiemTra : danhSach) {
            modelPhieuKiemTra.addRow(new Object[] {
                    phieuKiemTra.getMaPhieuKiemTra(),
                    phieuKiemTra.getPhieuTiepNhan().getMaPhieuTiepNhan(),
                    phieuKiemTra.getKhachHang().getMaKH(),
                    phieuKiemTra.getXe().getMaXe(),
                    phieuKiemTra.getNhanVien().getMaNV(),
                    phieuKiemTra.getTrangThaiBanDau(),
                    phieuKiemTra.getVanDeHuHong()
            });
        }
        locDanhSach();
    }

    public void hienThiPhieuKiemTra(PhieuKiemTra phieuKiemTra) {
        txtMaPKT.setText(phieuKiemTra.getMaPhieuKiemTra());
        chonPhieuTiepNhan(phieuKiemTra.getPhieuTiepNhan().getMaPhieuTiepNhan());
        chonNhanVien(phieuKiemTra.getNhanVien().getMaNV());
        txaTrangThai.setText(phieuKiemTra.getTrangThaiBanDau());
        txaVanDe.setText(phieuKiemTra.getVanDeHuHong());
        thongBao("Đang chọn phiếu kiểm tra " + phieuKiemTra.getMaPhieuKiemTra());
    }

    public void chonPhieuTiepNhan(String maPTN) {
        for (int index = 0; index < cboMaPTN.getItemCount(); index++) {
            PhieuTiepNhan phieuTiepNhan = cboMaPTN.getItemAt(index);
            if (phieuTiepNhan.getMaPhieuTiepNhan().equals(maPTN)) {
                cboMaPTN.setSelectedIndex(index);
                capNhatThongTinTheoPhieuTiepNhan();
                return;
            }
        }
    }

    public void chonNhanVien(String maNV) {
        for (int index = 0; index < cboNhanVienKiemTra.getItemCount(); index++) {
            LichLamViecDAO.NhanVienTrongCa nhanVien = cboNhanVienKiemTra.getItemAt(index);
            if (nhanVien.maNV().equals(maNV)) {
                cboNhanVienKiemTra.setSelectedIndex(index);
                return;
            }
        }
    }

    public void lamMoiForm(String maMoi) {
        txtMaPKT.setText(maMoi);
        if (cboMaPTN.getItemCount() > 0) {
            cboMaPTN.setSelectedIndex(0);
            capNhatThongTinTheoPhieuTiepNhan();
        } else {
            txtMaKH.setText("");
            txtMaXe.setText("");
            txtBienSo.setText("");
        }
        if (cboNhanVienKiemTra.getItemCount() > 0) {
            cboNhanVienKiemTra.setSelectedIndex(0);
        }
        txaTrangThai.setText("");
        txaVanDe.setText("");
        tblPhieuKiemTra.clearSelection();
        thongBao("Sẵn sàng");
    }

    public void locDanhSach() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        themBoLoc(filters, txtLocMaPKT.getText(), 0);
        themBoLoc(filters, txtLocMaPTN.getText(), 1);
        themBoLoc(filters, txtLocMaXe.getText(), 3);
        sorterPhieuKiemTra.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblThongBao.setText("Hiển thị " + sorterPhieuKiemTra.getViewRowCount()
                + " / " + modelPhieuKiemTra.getRowCount() + " phiếu kiểm tra");
    }

    private void themBoLoc(List<RowFilter<Object, Object>> filters, String giaTri, int cot) {
        String tuKhoa = giaTri.trim();
        if (!tuKhoa.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa), cot));
        }
    }

    public void xoaBoLoc() {
        txtLocMaPKT.setText("");
        txtLocMaPTN.setText("");
        txtLocMaXe.setText("");
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }

    public String getMaPhieuNhap() {
        return txtMaPKT.getText();
    }

    public String getMaPhieuDangChon() {
        int viewRow = tblPhieuKiemTra.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblPhieuKiemTra.convertRowIndexToModel(viewRow);
        return text(modelPhieuKiemTra.getValueAt(modelRow, 0));
    }

    public boolean xacNhanXoa(String maPhieu) {
        return JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa phiếu kiểm tra " + maPhieu + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    public void hienThiLoi(String message) {
        lblThongBao.setForeground(new java.awt.Color(190, 55, 55));
        lblThongBao.setText(message);
        JOptionPane.showMessageDialog(this, message, "Phiếu kiểm tra", JOptionPane.ERROR_MESSAGE);
    }

    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JButton getBtnTaiLai() { return btnTaiLai; }
    public JComboBox<PhieuTiepNhan> getCboMaPTN() { return cboMaPTN; }
    public JTable getTblPhieuKiemTra() { return tblPhieuKiemTra; }
    public JTextField getTxtLocMaPKT() { return txtLocMaPKT; }
    public JTextField getTxtLocMaPTN() { return txtLocMaPTN; }
    public JTextField getTxtLocMaXe() { return txtLocMaXe; }
}

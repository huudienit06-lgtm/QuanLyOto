package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import dao.ChiTietPhieuSuaChuaDAO;
import dao.DichVuDAO;
import dao.PhieuKiemTraDAO;
import dao.PhieuSuaChuaDAO;
import dao.PhuTungDAO;
import entity.ChiTietPhieuSuaChua;
import entity.DichVu;
import entity.PhieuKiemTra;
import entity.PhieuSuaChua;
import entity.PhuTung;
import entity.TaiKhoan;
import util.MaTuDong;

public class FrmPhieuSuaChua extends JPanel {
    private static final Color PALE_BLUE = new Color(242, 247, 255);
    private static final Color TEXT = new Color(47, 62, 84);
    private static final Color MUTED = new Color(112, 128, 150);
    private static final Color BORDER = new Color(221, 231, 245);

    private final JTextField txtMaPhieuSuaChua = new JTextField(8);
    private final JComboBox<PhieuKiemTra> cboPhieuKiemTra = new JComboBox<>();
    private final JTextField txtMaXe = new JTextField(8);
    private final JTextField txtMaKhachHang = new JTextField(8);
    private final JTextField txtMaNhanVien = new JTextField(8);
    private final JTextField txtNgayBatDau = new JTextField(8);
    private final JTextField txtNgayHoanThanhDuKien = new JTextField(8);
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {
            "Chờ xử lý", "Đang sửa chữa", "Hoàn thành", "Đã hủy"
    });
    private final JTextField txtLocMaPhieuSuaChua = new JTextField(10);
    private final JTextField txtLocMaXe = new JTextField(10);
    private final JTextField txtLocMaKH = new JTextField(10);

    private final JComboBox<PhuTung> cboPhuTung = new JComboBox<>();
    private final JComboBox<DichVu> cboMaDichVu = new JComboBox<>();
    private final JTextField txtDonGia = new JTextField(16);
    private final JTextField txtSoluong = new JTextField(16);
    private final JTextField txtThanhTien = new JTextField(16);

    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final DefaultTableModel modelSuaChua = new DefaultTableModel(
            new Object[] {
                    "MÃ PSC",
                    "MÃ PHIẾU KT",
                    "MÃ XE",
                    "MÃ KH",
                    "MÃ NV",
                    "NGÀY BẮT ĐẦU",
                    "NGÀY HOÀN THÀNH DỰ KIẾN"
            }, 0);
    private final JTable tblPhieuSuaChua = new JTable(modelSuaChua);

    private final DefaultTableModel modelChiTiet = new DefaultTableModel(
            new Object[] {
                    "MÃ CT",
                    "MÃ PT",
                    "TÊN PT",
                    "MÃ DV",
                    "TÊN DV",
                    "SL",
                    "ĐƠN GIÁ",
                    "THÀNH TIỀN"
            }, 0);
    private final JTable tblChiTietCongViec = new JTable(modelChiTiet);

    private final PhieuSuaChuaDAO phieuSuaChuaDAO = new PhieuSuaChuaDAO();
    private final ChiTietPhieuSuaChuaDAO chiTietDAO = new ChiTietPhieuSuaChuaDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final PhuTungDAO phuTungDAO = new PhuTungDAO();
    private final PhieuKiemTraDAO phieuKiemTraDAO = new PhieuKiemTraDAO();
    private final TaiKhoan taiKhoanHienTai;

    private final DecimalFormat df = new DecimalFormat("#,### VNĐ");

    private final TableRowSorter<DefaultTableModel> sortedPhieuKiemTra = new TableRowSorter<>(modelSuaChua);

    public FrmPhieuSuaChua() {
        this(null);
    }

    public FrmPhieuSuaChua(TaiKhoan taiKhoanHienTai) {
        this.taiKhoanHienTai = taiKhoanHienTai;
        setLayout(new BorderLayout(0, 18));
        setBackground(PALE_BLUE);
        setBorder(new EmptyBorder(22, 32, 22, 32));
        txtMaPhieuSuaChua.setEditable(false);

        JScrollPane mainScroll = new JScrollPane(createContent());
        mainScroll.setOpaque(false);
        mainScroll.setBorder(null);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(createHeading(), BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        setupComboBox(
                cboMaDichVu,
                txtDonGia,
                DichVu.class, dv -> dv.getMaDV() == null ? "Không" : dv.getMaDV() + " - " + dv.getTenDV(),
                DichVu::getGiaDV);
        setupComboBox(
                cboPhuTung,
                txtDonGia,
                PhuTung.class,
                pt -> pt.getMaPhuTung() == null ? "Không" : pt.getMaPhuTung() + " - " + pt.getTenPhuTung(),
                PhuTung::getGiaBan);
        txtSoluong.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                capNhatThanhTien();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                capNhatThanhTien();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                capNhatThanhTien();
            }
        });

        txtDonGia.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                capNhatThanhTien();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                capNhatThanhTien();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                capNhatThanhTien();
            }
        });

        cboPhieuKiemTra.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhieuKiemTra phieuKiemTra) {
                    setText(phieuKiemTra.getMaPhieuKiemTra() + " - " + phieuKiemTra.getMaPhieuTiepNhan());
                }
                return this;
            }
        });

        cboPhieuKiemTra.addActionListener(e -> {
            PhieuKiemTra phieuKiemTra = (PhieuKiemTra) cboPhieuKiemTra.getSelectedItem();
            if (phieuKiemTra == null) {
                txtMaXe.setText("");
                txtMaKhachHang.setText("");
                txtMaNhanVien.setText("");
                return;
            }
            txtMaXe.setText(phieuKiemTra.getMaXe());
            txtMaKhachHang.setText(phieuKiemTra.getMaKH());
            String maNV = maNhanVienDangNhap();
            txtMaNhanVien.setText(maNV.isEmpty() ? phieuKiemTra.getMaNV() : maNV);
        });

        napDanhSachPhieuSuaChua();
        napDanhSachDichVu();
        napDanhSachPhuTung();
        napDanhSachPhieuKiemTra();
        txtSoluong.setText("1");
        tblPhieuSuaChua.setRowSorter(sortedPhieuKiemTra);
        lamMoiForm();
    }

    private JPanel createHeading() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Quản lý phiếu sửa chữa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblTitle.setForeground(new Color(32, 55, 92));
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        return pnlHeader;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout(0, 18));
        pnlContent.setOpaque(false);

        JPanel pnlTop = new JPanel(new GridBagLayout());
        pnlTop.setOpaque(false);
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.gridx = 0;
        gbcInfo.gridy = 0;
        gbcInfo.weightx = 0.42;
        gbcInfo.weighty = 1;
        gbcInfo.fill = GridBagConstraints.BOTH;
        gbcInfo.insets = new Insets(0, 0, 0, 9);
        pnlTop.add(createFormPanelInfo(), gbcInfo);

        GridBagConstraints gbcDetail = new GridBagConstraints();
        gbcDetail.gridx = 1;
        gbcDetail.gridy = 0;
        gbcDetail.weightx = 0.58;
        gbcDetail.weighty = 1;
        gbcDetail.fill = GridBagConstraints.BOTH;
        gbcDetail.insets = new Insets(0, 9, 0, 0);
        pnlTop.add(createFormPanelDetail(), gbcDetail);

        pnlContent.add(pnlTop, BorderLayout.NORTH);
        pnlContent.add(createTablePanel(), BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormPanelInfo() {
        JPanel pnlFormInfo = createCardPanel();
        pnlFormInfo.setLayout(new BorderLayout(0, 12));
        JLabel lblTitle = createSectionLabel("Thông tin phiếu sửa chữa");
        JPanel pnlField = new JPanel(new GridBagLayout());
        pnlField.setOpaque(false);
        addField(pnlField, "Mã phiếu sửa chữa", txtMaPhieuSuaChua, 0, 0);
        addField(pnlField, "Mã phiếu kiểm tra", cboPhieuKiemTra, 0, 1);
        addField(pnlField, "Mã xe", txtMaXe, 0, 2);
        addField(pnlField, "Mã khách hàng", txtMaKhachHang, 0, 3);
        addField(pnlField, "Mã nhân viên", txtMaNhanVien, 0, 4);
        addField(pnlField, "Ngày bắt đầu", txtNgayBatDau, 0, 5);
        addField(pnlField, "Ngày hoàn thành dự kiến", txtNgayHoanThanhDuKien, 0, 6);
        addField(pnlField, "Trạng thái", cboTrangThai, 0, 7);
        addField(pnlField, "Mã phiếu sửa chữa (tự động)", txtMaPhieuSuaChua, 0, 0);
        addField(pnlField, "Mã phiếu kiểm tra", cboPhieuKiemTra, 2, 0);
        addField(pnlField, "Mã xe", txtMaXe, 0, 1);
        addField(pnlField, "Mã khách hàng", txtMaKhachHang, 2, 1);
        addField(pnlField, "Mã nhân viên", txtMaNhanVien, 0, 2);
        addField(pnlField, "Ngày bắt đầu", txtNgayBatDau, 2, 2);
        addField(pnlField, "Ngày hoàn thành dự kiến", txtNgayHoanThanhDuKien, 0, 3);
        addField(pnlField, "Trạng thái", cboTrangThai, 2, 3);

        txtMaXe.setEditable(false);
        txtMaXe.setBackground(new Color(240, 240, 240));
        txtMaXe.setFocusable(false);

        txtMaKhachHang.setEditable(false);
        txtMaKhachHang.setBackground(new Color(240, 240, 240));
        txtMaKhachHang.setFocusable(false);

        txtMaNhanVien.setEditable(false);
        txtMaNhanVien.setBackground(new Color(240, 240, 240));
        txtMaNhanVien.setFocusable(false);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        JButton btnAdd = createButton(
                "Tạo phiếu",
                new ImageIcon(getClass().getResource("/resources/icons/plus.png")),
                new Color(0x33cc33),
                new Color(0xffffff));
        btnAdd.addActionListener(e -> themPhieuSuaChua());

        JButton btnUpdate = createButton(
                "Cập nhật",
                new ImageIcon(getClass().getResource("/resources/icons/update.png")),
                new Color(0x3366cc),
                new Color(0xffffff));
        btnUpdate.addActionListener(e -> capNhatPhieuSuaChua());

        JButton btnDel = createButton(
                "Xóa",
                new ImageIcon(getClass().getResource("/resources/icons/trash.png")),
                new Color(0xff0033),
                new Color(0xffffff));
        btnDel.addActionListener(e -> xoaPhieuSuaChua());

        JButton btnReset = createButton(
                "Làm mới",
                new ImageIcon(getClass().getResource("/resources/icons/reset.png")),
                new Color(0x555555),
                new Color(0xffffff));
        btnReset.addActionListener(e -> lamMoiForm());

        pnlButtons.add(btnAdd, buttonConstraints(0));
        pnlButtons.add(btnUpdate, buttonConstraints(1));
        pnlButtons.add(btnDel, buttonConstraints(2));
        pnlButtons.add(btnReset, buttonConstraints(3));

        JPanel pnlLower = new JPanel(new BorderLayout());
        pnlLower.setOpaque(false);
        pnlLower.add(pnlButtons, BorderLayout.WEST);

        pnlFormInfo.add(lblTitle, BorderLayout.NORTH);
        pnlFormInfo.add(pnlField, BorderLayout.CENTER);
        pnlFormInfo.add(pnlLower, BorderLayout.SOUTH);
        return pnlFormInfo;
    }

    private JPanel createFormPanelDetail() {
        JPanel pnlFormDetail = createCardPanel();
        pnlFormDetail.setLayout(new BorderLayout(0, 12));
        JLabel lblTitle = createSectionLabel("Chi tiết phiếu sửa chữa");
        JPanel pnlField = new JPanel(new GridBagLayout());
        pnlField.setOpaque(false);
        addField(pnlField, "Mã dịch vụ", cboMaDichVu, 0, 0);
        addField(pnlField, "Số lượng", txtSoluong, 0, 1);
        addField(pnlField, "Mã phụ tùng", cboPhuTung, 2, 0);
        addField(pnlField, "Đơn giá", txtDonGia, 2, 1);
        addField(pnlField, "Thành tiền", txtThanhTien, 2, 2);

        txtDonGia.setEditable(false);
        txtDonGia.setFocusable(false);
        txtDonGia.setBackground(new Color(240, 240, 240));

        txtThanhTien.setEditable(false);
        txtThanhTien.setFocusable(false);
        txtThanhTien.setBackground(new Color(240, 240, 240));

        JPanel pnlButton = new JPanel(new GridBagLayout());
        pnlButton.setOpaque(false);
        JButton btnAdd = createButton(
                "Thêm chi tiết",
                new ImageIcon(getClass().getResource("/resources/icons/plus.png")),
                new Color(0x33cc33),
                new Color(0xffffff));
        btnAdd.addActionListener(e -> themChiTiet());

        JButton btnDel = createButton(
                "Xóa chi tiết",
                new ImageIcon(getClass().getResource("/resources/icons/trash.png")),
                new Color(0xff0033),
                new Color(0xffffff));
        btnDel.addActionListener(e -> xoaChiTiet());

        pnlButton.add(btnAdd, buttonConstraints(0));
        pnlButton.add(btnDel, buttonConstraints(1));

        JPanel pnlLower = new JPanel(new BorderLayout());
        pnlLower.setOpaque(false);
        pnlLower.add(pnlButton);

        JScrollPane scrChiTiet = createScroll(tblChiTietCongViec, 140);

        JPanel pnlTopDetail = new JPanel(new BorderLayout(0, 10));
        pnlTopDetail.setOpaque(false);
        pnlTopDetail.add(pnlField, BorderLayout.CENTER);
        pnlTopDetail.add(pnlLower, BorderLayout.SOUTH);

        JPanel pnlBodyDetail = new JPanel(new BorderLayout(0, 15));
        pnlBodyDetail.setOpaque(false);
        pnlBodyDetail.add(pnlTopDetail, BorderLayout.NORTH);
        pnlBodyDetail.add(scrChiTiet, BorderLayout.CENTER);

        pnlFormDetail.add(lblTitle, BorderLayout.NORTH);
        pnlFormDetail.add(pnlBodyDetail, BorderLayout.CENTER);

        return pnlFormDetail;
    }

    private JPanel createTablePanel() {
        JPanel pnlTable = createCardPanel();
        pnlTable.setLayout(new BorderLayout(0, 12));
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = createSectionLabel("Danh sách phiếu sửa chữa đã lập");
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(createSearchToolbar(), BorderLayout.EAST);

        JScrollPane scrSuaChua = createScroll(tblPhieuSuaChua, 280);

        tblPhieuSuaChua.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPhieuSuaChua.getSelectedRow() >= 0) {
                hienThiDaChon();
            }
        });

        pnlTable.add(pnlHeader, BorderLayout.NORTH);
        pnlTable.add(scrSuaChua, BorderLayout.CENTER);
        return pnlTable;
    }

    private JPanel createStatusBar() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        lblThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBao.setForeground(MUTED);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private JPanel createCardPanel() {
        JPanel pnlCard = new JPanel(new BorderLayout());
        pnlCard.setOpaque(true);
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        return pnlCard;
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.setBorder(new EmptyBorder(0, 0, 0, 50));
        JPanel pnlLoc = new JPanel(new GridLayout(1, 3, 8, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã phiếu sửa chữa", txtLocMaPhieuSuaChua));
        pnlLoc.add(createSearchField("Mã xe", txtLocMaXe));
        pnlLoc.add(createSearchField("Mã khách hàng", txtLocMaKH));
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTaiLai.addActionListener(e -> {
            txtLocMaPhieuSuaChua.setText("");
            txtLocMaXe.setText("");
            txtLocMaKH.setText("");
            napDanhSachPhieuSuaChua();
        });
        pnlSearch.add(pnlLoc, BorderLayout.CENTER);
        pnlSearch.add(btnTaiLai, BorderLayout.EAST);
        themBoLangNghe(txtLocMaPhieuSuaChua);
        themBoLangNghe(txtLocMaXe);
        themBoLangNghe(txtLocMaKH);

        return pnlSearch;
    }

    private JPanel createSearchField(String labelText, JTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new AppLabel(labelText, AppLabel.Style.MUTED);
        pnlField.add(lblField, BorderLayout.NORTH);
        pnlField.add(txtField, BorderLayout.CENTER);
        return pnlField;
    }

    private void themBoLangNghe(JTextField txtField) {
        txtField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSachPhieuKiemTra();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSachPhieuKiemTra();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSachPhieuKiemTra();
            }
        });
    }

    private void locDanhSachPhieuKiemTra() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        themBoLoc(filters, txtLocMaPhieuSuaChua.getText(), 0);
        themBoLoc(filters, txtLocMaXe.getText(), 2);
        themBoLoc(filters, txtLocMaKH.getText(), 3);
        sortedPhieuKiemTra.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblThongBao.setText("Hiện thị " + sortedPhieuKiemTra.getViewRowCount() + " / " + modelSuaChua.getRowCount()
                + " phiếu sửa chữa");
    }

    private void themBoLoc(List<RowFilter<Object, Object>> filters, String value, int column) {
        String key = value.trim();
        if (!key.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(key), column));
        }
    }

    private void addField(JPanel pnlField, String label, Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT);
        pnlField.add(lbl, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlField.add(component, gbcInput);
    }

    private JButton createButton(String text, ImageIcon icon, Color background, Color foreground) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(background);
        btn.setForeground(foreground);
        Image image = icon.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
        btn.setIcon(new ImageIcon(image));
        btn.setIconTextGap(8);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 13, 8, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, btn.getPreferredSize().height));
        return btn;
    }

    private GridBagConstraints buttonConstraints(int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        return gbc;
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(32, 55, 92));
        return lbl;
    }

    private JScrollPane createScroll(JTable tbl, int height) {
        JScrollPane scrPanel = new JScrollPane(tbl);
        scrPanel.setBorder(BorderFactory.createLineBorder(BORDER));
        scrPanel.getViewport().setBackground(new Color(0xffffff));
        scrPanel.setPreferredSize(new Dimension(0, height));

        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setRowHeight(30);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setForeground(TEXT);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBackground(new Color(239, 245, 253));
        tbl.getTableHeader().setForeground(new Color(32, 55, 92));
        return scrPanel;
    }

    private void napDanhSachPhieuSuaChua() {
        try {
            List<PhieuSuaChua> danhSach = phieuSuaChuaDAO.findAll();
            hienThiDanhSachPhieuSuaChua(danhSach);
            lblThongBao.setText("Đã tải " + danhSach.size() + " phiếu sửa chữa");
        } catch (SQLException e) {
            hienThiLoi("Không thể tải danh sách phiếu: " + e.getMessage());
        }
    }

    private void hienThiDanhSachPhieuSuaChua(List<PhieuSuaChua> danhSach) {
        tblPhieuSuaChua.setRowSorter(null);
        modelSuaChua.setRowCount(0);
        for (PhieuSuaChua phieu : danhSach) {
            modelSuaChua.addRow(new Object[] {
                    phieu.getMaPhieuSuaChua(),
                    phieu.getMaPhieuKiemTra(),
                    phieu.getMaXe(),
                    phieu.getMaKH(),
                    phieu.getMaNV(),
                    phieu.getNgayBatDau(),
                    phieu.getNgayHoanThanhDuKien()
            });
        }
        tblPhieuSuaChua.setRowSorter(sortedPhieuKiemTra);
        locDanhSachPhieuKiemTra();
        tblPhieuSuaChua.revalidate();
        tblPhieuSuaChua.repaint();
    }

    private void napDanhSachDichVu() {
        try {
            List<DichVu> danhSach = dichVuDAO.findAll();
            cboMaDichVu.removeAllItems();
            DichVu khong = new DichVu();
            khong.setTenDV("Không");
            cboMaDichVu.addItem(khong);
            for (DichVu dichVu : danhSach) {
                cboMaDichVu.addItem(dichVu);
            }
            if (danhSach.isEmpty()) {
                lblThongBao.setText("Chưa có dữ liệu dịch vụ");
            }
        } catch (SQLException e) {
            hienThiLoi("Không thể tải danh sách dịch vụ: " + e.getMessage());
        }
    }

    private final void napDanhSachPhuTung() {
        try {
            List<PhuTung> danhSach = phuTungDAO.findAll();
            cboPhuTung.removeAllItems();
            PhuTung khong = new PhuTung();
            khong.setTenPhuTung("Không");
            cboPhuTung.addItem(khong);
            for (PhuTung phuTung : danhSach) {
                cboPhuTung.addItem(phuTung);
            }
            if (danhSach.isEmpty()) {
                lblThongBao.setText("Chưa có dữ liệu phụ tùng");
            }
        } catch (SQLException e) {
            hienThiLoi("Không thể tải danh sách phụ tùng: " + e.getMessage());
        }
    }

    private void napDanhSachPhieuKiemTra() {
        try {
            List<PhieuKiemTra> danhSach = phieuKiemTraDAO.findAll();
            cboPhieuKiemTra.removeAll();
            for (PhieuKiemTra phieuKiemTra : danhSach) {
                cboPhieuKiemTra.addItem(phieuKiemTra);
            }
            if (danhSach.isEmpty()) {
                lblThongBao.setText("Chưa có dữ liệu phiếu kiểm tra");
            }
        } catch (SQLException e) {
            hienThiLoi("Không thể tải danh sách phiếu kiểm tra: " + e.getMessage());
        }
    }

    private void hienThiLoi(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(new Color(190, 55, 55));
        JOptionPane.showMessageDialog(this, message, "Phiếu sửa chữa", JOptionPane.ERROR_MESSAGE);
    }

    private PhieuSuaChua docThongTinPhieuSuaChua() {
        if (cboPhieuKiemTra.getItemCount() == 0) {
            throw new IllegalArgumentException("Chưa có phiếu kiểm tra để tạo phiếu sửa chữa.");
        }

        PhieuKiemTra phieuKiemTra = (PhieuKiemTra) cboPhieuKiemTra.getSelectedItem();
        if (phieuKiemTra == null) {
            throw new IllegalArgumentException("Hãy chọn phiếu kiểm tra");
        }
        String maPSC = txtMaPhieuSuaChua.getText().trim();
        String maPKT = phieuKiemTra.getMaPhieuKiemTra();
        String maXe = txtMaXe.getText().trim();
        String maKH = txtMaKhachHang.getText().trim();
        String maNV = txtMaNhanVien.getText().trim();

        if (maPSC == null || maPSC.isEmpty())
            throw new IllegalArgumentException("Mã phiếu sửa chữa không được để trống");
        if (maPKT == null || maPKT.isEmpty())
            throw new IllegalArgumentException("Mã phiếu kiểm tra không được để trống");
        if (maXe == null || maXe.isEmpty())
            throw new IllegalArgumentException("Mã xe không được để trống");
        if (maKH == null || maKH.isEmpty())
            throw new IllegalArgumentException("Mã khách hàng không được để trống");
        if (maNV == null || maNV.isEmpty())
            throw new IllegalArgumentException("Mã nhân viên không được để trống");

        PhieuSuaChua phieu = new PhieuSuaChua();
        phieu.setMaPhieuSuaChua(maPSC);
        phieu.setMaPhieuKiemTra(maPKT);
        phieu.setMaXe(maXe);
        phieu.setMaKH(maKH);
        phieu.setMaNV(maNV);
        phieu.setNgayBatDau(docTimestamp(txtNgayBatDau.getText()));
        phieu.setNgayHoanThanhDuKien(docTimestamp(txtNgayHoanThanhDuKien.getText()));
        phieu.setTrangThai(cboTrangThai.getSelectedItem() == null
                ? "Chờ xử lý"
                : cboTrangThai.getSelectedItem().toString());

        return phieu;
    }

    private Timestamp docTimestamp(String value) {
        String text = value.trim();
        if (text.isEmpty())
            return null;
        String[] patterns = { "d/M/yyyy", "yyyy-M-d", "d-M-yyyy" };
        for (String pattern : patterns) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
                LocalDate localDate = LocalDate.parse(text, dtf);
                return Timestamp.valueOf(localDate.atStartOfDay());
            } catch (DateTimeParseException ignored) {

            }
        }
        try {
            return Timestamp.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ngày phải có định dạng yyyy-MM-dd");

        }
    }

    private void themPhieuSuaChua() {
        try {
            PhieuSuaChua phieu = docThongTinPhieuSuaChua();

            if (phieuSuaChuaDAO.insert(phieu)) {
                napDanhSachPhieuSuaChua();
                lamMoiForm();
                lblThongBao.setText("Đã tạo phiếu " + phieu.getMaPhieuSuaChua());
            }
        } catch (IllegalArgumentException e) {
            hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            hienThiLoi("Không thể tạo phiếu sửa chữa: " + e.getMessage());
        }
    }

    private void capNhatPhieuSuaChua() {
        try {
            PhieuSuaChua phieu = docThongTinPhieuSuaChua();
            if (phieuSuaChuaDAO.update(phieu)) {
                napDanhSachPhieuSuaChua();
                lblThongBao.setText("Đã cập nhật phiếu " + phieu.getMaPhieuSuaChua());
            } else {
                hienThiLoi("Không tìm thấy phiếu cần cập nhật");
            }
        } catch (IllegalArgumentException e) {
            hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            lblThongBao.setText("Không thể cập nhật phiếu sửa chữa: " + e.getMessage());
        }
    }

    private void xoaPhieuSuaChua() {
        String maPSC = txtMaPhieuSuaChua.getText().trim();
        if (maPSC == null || maPSC.isEmpty()) {
            hienThiLoi("Hãy chọn phiếu cần xóa");
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa phiếu " + maPSC + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (phieuSuaChuaDAO.deleteById(maPSC)) {
                napDanhSachPhieuSuaChua();
                lamMoiForm();
                lblThongBao.setText("Đã xóa phiếu " + maPSC);
            } else {
                hienThiLoi("Không tìm thấy phiếu cần xóa");
            }
        } catch (SQLException e) {
            hienThiLoi("Không thể xóa phiếu sửa chữa: " + e.getMessage());
        }
    }

    private void lamMoiForm() {
        try {
            txtMaPhieuSuaChua.setText(MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_PHIEU_SUA_CHUA,
                    phieuSuaChuaDAO.findAll(),
                    PhieuSuaChua::getMaPhieuSuaChua));
        } catch (SQLException exception) {
            hienThiLoi("Không thể phát sinh mã phiếu sửa chữa: " + exception.getMessage());
        }
        if (cboPhieuKiemTra.getItemCount() > 0) {
            cboPhieuKiemTra.setSelectedIndex(0);
        }
        txtMaXe.setText("");
        txtMaKhachHang.setText("");
        txtMaNhanVien.setText(maNhanVienDangNhap());
        txtNgayBatDau.setText("");
        txtNgayHoanThanhDuKien.setText("");
        tblPhieuSuaChua.clearSelection();
    }

    private String maNhanVienDangNhap() {
        return taiKhoanHienTai == null || taiKhoanHienTai.getMaNV() == null
                ? ""
                : taiKhoanHienTai.getMaNV().trim();
    }

    private void hienThiDaChon() {
        int selectedRow = tblPhieuSuaChua.getSelectedRow();
        if (selectedRow < 0)
            return;
        int modelRow = tblPhieuSuaChua.convertRowIndexToModel(selectedRow);
        txtMaPhieuSuaChua.setText(cellText(modelSuaChua.getValueAt(modelRow, 0)));
        String maPhieuKiemTra = cellText(modelSuaChua.getValueAt(modelRow, 1));
        for (int i = 0; i < cboPhieuKiemTra.getItemCount(); i++) {
            PhieuKiemTra phieuKiemTra = cboPhieuKiemTra.getItemAt(i);

            if (phieuKiemTra.getMaPhieuKiemTra()
                    .equals(maPhieuKiemTra)) {
                cboPhieuKiemTra.setSelectedItem(phieuKiemTra);
                break;
            }
        }
        txtMaXe.setText(cellText(modelSuaChua.getValueAt(modelRow, 2)));
        txtMaKhachHang.setText(cellText(modelSuaChua.getValueAt(modelRow, 3)));
        txtMaNhanVien.setText(cellText(modelSuaChua.getValueAt(modelRow, 4)));
        txtNgayBatDau.setText(cellText(modelSuaChua.getValueAt(modelRow, 5)));
        txtNgayHoanThanhDuKien.setText(cellText(modelSuaChua.getValueAt(modelRow, 6)));
        hienThiChiTiet();
    }

    private String cellText(Object value) {
        return value == null ? "" : value.toString();
    }

    private ChiTietPhieuSuaChua docChiTietPhieuSuaChua() {
        PhuTung phuTung = (PhuTung) cboPhuTung.getSelectedItem();
        DichVu dichVu = (DichVu) cboMaDichVu.getSelectedItem();
        String maPSC = txtMaPhieuSuaChua.getText().trim();
        String maPhuTung = phuTung == null ? null : phuTung.getMaPhuTung();
        String maDichVu = dichVu == null ? null : dichVu.getMaDV();

        if (maPSC == null || maPSC.isEmpty())
            throw new IllegalArgumentException("Mã phiếu sửa chữa không được để trống");
        if (maPhuTung != null && maPhuTung.isEmpty()) {
            maPhuTung = null;
        }
        if (maDichVu != null && maDichVu.isEmpty()) {
            maDichVu = null;
        }
        if ((maPhuTung == null) == (maDichVu == null)) {
            throw new IllegalArgumentException("Phải chọn đúng một trong hai: dịch vụ hoặc phụ tùng");
        }

        int soLuong;
        try {
            soLuong = Integer.parseInt(txtSoluong.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên");
        }

        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        BigDecimal donGia;
        try {
            String donGiaText = txtDonGia.getText().replace(",", "").replace(" VNĐ", "").trim();
            donGia = donGiaText.isEmpty() ? BigDecimal.ZERO : new BigDecimal(donGiaText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Đơn giá không hợp lệ");
        }
        if (donGia.signum() < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm");
        }

        ChiTietPhieuSuaChua chiTiet = new ChiTietPhieuSuaChua();
        chiTiet.setMaPhieuSuaChua(maPSC);
        chiTiet.setMaPhuTung(maPhuTung);
        chiTiet.setMaDV(maDichVu);
        chiTiet.setSoLuong(soLuong);
        chiTiet.setDonGia(donGia);
        BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
        txtThanhTien.setText(thanhTien.toPlainString());

        return chiTiet;
    }

    private void hienThiChiTiet() {
        try {
            String maPSC = txtMaPhieuSuaChua.getText().trim();
            if (maPSC.isEmpty()) {
                modelChiTiet.setRowCount(0);
                return;
            }
            List<ChiTietPhieuSuaChua> danhSach = chiTietDAO.findByField("maPhieuSuaChua", maPSC);
            modelChiTiet.setRowCount(0);
            for (ChiTietPhieuSuaChua chiTiet : danhSach) {
                BigDecimal donGia = chiTiet.getDonGia() == null ? BigDecimal.ZERO : chiTiet.getDonGia();
                int soLuong = chiTiet.getSoLuong() == null ? 1 : chiTiet.getSoLuong();
                BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
                PhuTung phuTung = chiTiet.getMaPhuTung() == null
                        ? null
                        : phuTungDAO.findById(chiTiet.getMaPhuTung());
                DichVu dichVu = chiTiet.getMaDV() == null
                        ? null
                        : dichVuDAO.findById(chiTiet.getMaDV());
                modelChiTiet.addRow(new Object[] {
                        chiTiet.getMaChiTiet(),
                        chiTiet.getMaPhuTung(),
                        phuTung == null ? "" : phuTung.getTenPhuTung(),
                        chiTiet.getMaDV(),
                        dichVu == null ? "" : dichVu.getTenDV(),
                        soLuong,
                        donGia,
                        thanhTien
                });
            }
            lblThongBao.setText("Đã tải " + danhSach.size() + " chi tiết");
        } catch (SQLException e) {
            hienThiLoi("Không thể tải danh sách: " + e.getMessage());
        }
    }

    private void themChiTiet() {
        try {
            ChiTietPhieuSuaChua chiTiet = docChiTietPhieuSuaChua();
            if (chiTietDAO.insert(chiTiet)) {
                hienThiChiTiet();
                lblThongBao.setText("Đã thêm chi tiết vào phiếu");
            }
        } catch (IllegalArgumentException e) {
            hienThiLoi(e.getMessage());
        } catch (SQLException e) {
            hienThiLoi("Không thể thêm chi tiết: " + e.getMessage());
        }
    }

    private void xoaChiTiet() {
        String maPSC = txtMaPhieuSuaChua.getText().trim();
        if (maPSC.isEmpty()) {
            hienThiLoi("Mã phiếu sửa chữa không được để trống");
            return;
        }
        int selectedRow = tblChiTietCongViec.getSelectedRow();
        if (selectedRow < 0) {
            hienThiLoi("Hãy chọn chi tiết cần xóa trong bảng");
            return;
        }
        int modelRow = tblChiTietCongViec.convertRowIndexToModel(selectedRow);
        Long maChiTiet = Long.valueOf(modelChiTiet.getValueAt(modelRow, 0).toString());
        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa chi tiết khỏi phiếu " + maPSC + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (chiTietDAO.deleteById(maChiTiet)) {
                hienThiChiTiet();
                lamMoiFormChiTiet();
                lblThongBao.setText("Đã xóa chi tiết");
            } else {
                hienThiLoi("Không tìm thấy chi tiết cần xóa");
            }
        } catch (SQLException e) {
            hienThiLoi("Không thể xóa chi tiết dịch vụ: " + e.getMessage());
        }
    }

    private void lamMoiFormChiTiet() {
        if (cboMaDichVu.getItemCount() > 0) {
            cboMaDichVu.setSelectedIndex(0);
        }
        if (cboPhuTung.getItemCount() > 0) {
            cboPhuTung.setSelectedIndex(0);
        }
        txtDonGia.setText("");
        txtSoluong.setText("1");
        txtThanhTien.setText("");
        tblChiTietCongViec.clearSelection();
    }

    private void capNhatThanhTien() {
        try {
            String donGiaText = txtDonGia.getText().replace(",", "").replace(" VNĐ", "").trim();
            String soLuongText = txtSoluong.getText().trim();
            if (donGiaText.isEmpty() || soLuongText.isEmpty()) {
                txtThanhTien.setText("");
                return;
            }
            BigDecimal donGia = new BigDecimal(donGiaText);
            int soLuong = Integer.parseInt(soLuongText);
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));

            txtThanhTien.setText(df.format(thanhTien));
        } catch (NumberFormatException e) {
            txtThanhTien.setText("");
        }

    }

    private <T> void setupComboBox(
            JComboBox<T> comboBox,
            JTextField textField,
            Class<T> type,
            Function<T, String> textFormatter,
            Function<T, Number> priceEX) {

        // Cài đặt Renderer
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (type.isInstance(value)) {
                    setText(textFormatter.apply(type.cast(value)));
                }
                return this;
            }
        });

        // Cài đặt sự kiện chọn cho Item
        comboBox.addActionListener(e -> {
            Object select = comboBox.getSelectedItem();
            if (type.isInstance(select)) {
                Number price = priceEX.apply(type.cast(select));
                textField.setText(price != null ? df.format(price) : "");
            } else {
                textField.setText("");
            }
            capNhatThanhTien();
        });
    }
}

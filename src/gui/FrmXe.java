package gui;

import dao.KhachHangDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.Xe;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import util.MaTuDong;

public class FrmXe extends JPanel {
    private static final Set<String> MA_KHACH_HANG_DA_BIET = new HashSet<>();

    private final XeDAO xeDAO = new XeDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    private final AppTextField txtMaXe = new AppTextField(16);
    private final AppTextField txtBienSo = new AppTextField(16);
    private final AppTextField txtHangXe = new AppTextField(16);
    private final AppTextField txtDongXe = new AppTextField(16);
    private final AppTextField txtNamSanXuat = new AppTextField(16);
    private final AppTextField txtSoKhung = new AppTextField(16);
    private final AppTextField txtSoMay = new AppTextField(16);
    private final AppTextField txtKhachHang = new AppTextField(16);
    private final DefaultListModel<KhachHang> modelGoiYKhachHang = new DefaultListModel<>();
    private final JList<KhachHang> lstGoiYKhachHang = new JList<>(modelGoiYKhachHang);
    private JWindow wndGoiYKhachHang;
    private final List<KhachHang> danhSachKhachHang = new ArrayList<>();
    private KhachHang khachHangDuocChon;
    private boolean dangCapNhatOKhachHang;
    private final AppTextField txtLocMaXe = new AppTextField(12);
    private final AppTextField txtLocBienSo = new AppTextField(12);
    private final AppTextField txtLocHangXe = new AppTextField(12);
    private final AppTextField txtLocDongXe = new AppTextField(12);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");
    private final DefaultTableModel modelXe = new DefaultTableModel(
            new Object[] {"Mã xe", "Biển số", "Hãng xe", "Dòng xe", "Năm SX", "Số khung", "Số máy", "Mã KH"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final AppTable tblXe = new AppTable(modelXe);
    private final TableRowSorter<DefaultTableModel> sorterXe = new TableRowSorter<>(modelXe);

    public FrmXe() {
        setLayout(new BorderLayout(0, 8));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(14, 22, 14, 22));
        txtMaXe.setEditable(false);

        add(createHeading(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        configureComponents();
        napKhachHang();
        napDanhSachXe();
        lamMoiForm();
    }

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 5));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ XE");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new AppLabel("Thông tin xe và danh sách xe đã tiếp nhận", AppLabel.Style.MUTED);

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);
        JPanel pnlForm = createFormPanel();
        pnlForm.setMinimumSize(new java.awt.Dimension(0, 255));
        JSplitPane splContent = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, pnlForm, createTablePanel());
        splContent.setBorder(null);
        splContent.setResizeWeight(0.36);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        SwingUtilities.invokeLater(() -> splContent.setDividerLocation(0.36));
        pnlContent.add(splContent, BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormPanel() {
        JPanel pnlFormCard = createCardPanel();
        pnlFormCard.setLayout(new BorderLayout(0, 3));
        pnlFormCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConfig.COLOR_BORDER, 1, true),
                new EmptyBorder(7, 12, 7, 12)));

        JLabel lblFormTitle = createSectionTitle("Thông tin xe");
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã xe (tự động)", txtMaXe, 0, 0);
        addField(pnlFields, "Biển số *", txtBienSo, 2, 0);
        addField(pnlFields, "Hãng xe", txtHangXe, 0, 1);
        addField(pnlFields, "Dòng xe", txtDongXe, 2, 1);
        addField(pnlFields, "Năm sản xuất", txtNamSanXuat, 0, 2);
        addField(pnlFields, "Khách hàng *", txtKhachHang, 2, 2);
        addField(pnlFields, "Số khung", txtSoKhung, 0, 3);
        addField(pnlFields, "Số máy", txtSoMay, 2, 3);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        AppButton btnThem = new AppButton("Thêm xe", AppButton.Style.PRIMARY);
        AppButton btnSua = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
        AppButton btnXoa = new AppButton("Xóa xe", AppButton.Style.DANGER);
        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
        btnThem.addActionListener(event -> themXe());
        btnSua.addActionListener(event -> capNhatXe());
        btnXoa.addActionListener(event -> xoaXe());
        btnLamMoi.addActionListener(event -> lamMoiForm());
        pnlButtons.add(btnThem, buttonConstraints(0));
        pnlButtons.add(btnSua, buttonConstraints(1));
        pnlButtons.add(btnXoa, buttonConstraints(2));
        pnlButtons.add(btnLamMoi, buttonConstraints(3));

        JPanel pnlLower = new JPanel(new BorderLayout());
        pnlLower.setOpaque(false);
        pnlLower.add(pnlButtons, BorderLayout.WEST);

        pnlFormCard.add(lblFormTitle, BorderLayout.NORTH);
        pnlFormCard.add(pnlFields, BorderLayout.CENTER);
        pnlFormCard.add(pnlLower, BorderLayout.SOUTH);
        return pnlFormCard;
    }

    private JPanel createTablePanel() {
        JPanel pnlTableCard = createCardPanel();
        pnlTableCard.setLayout(new BorderLayout(0, 12));

        JPanel pnlTableHeader = new JPanel(new BorderLayout(12, 0));
        pnlTableHeader.setOpaque(false);
        JPanel pnlTitleBar = new JPanel(new BorderLayout(8, 0));
        pnlTitleBar.setOpaque(false);
        JLabel lblTableTitle = createSectionTitle("Danh sách xe đã tiếp nhận");
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTaiLai.addActionListener(event -> {
            xoaBoLocXe();
            napDanhSachXe();
        });
        pnlTitleBar.add(lblTableTitle, BorderLayout.WEST);
        pnlTitleBar.add(btnTaiLai, BorderLayout.EAST);
        JPanel pnlLoc = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã xe", txtLocMaXe));
        pnlLoc.add(createSearchField("Biển số", txtLocBienSo));
        pnlLoc.add(createSearchField("Hãng xe", txtLocHangXe));
        pnlLoc.add(createSearchField("Dòng xe", txtLocDongXe));
        pnlTableHeader.setLayout(new BorderLayout(0, 8));
        pnlTableHeader.add(pnlTitleBar, BorderLayout.NORTH);
        pnlTableHeader.add(pnlLoc, BorderLayout.CENTER);
        themBoLangNghe(txtLocMaXe);
        themBoLangNghe(txtLocBienSo);
        themBoLangNghe(txtLocHangXe);
        themBoLangNghe(txtLocDongXe);

        AppScrollPane scrXe = new AppScrollPane(tblXe);

        tblXe.setRowSorter(sorterXe);
        tblXe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblXe.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblXe.setFillsViewportHeight(true);
        int[] doRongCot = {85, 105, 125, 120, 75, 150, 150, 85};
        for (int cot = 0; cot < doRongCot.length; cot++) {
            tblXe.getColumnModel().getColumn(cot).setPreferredWidth(doRongCot[cot]);
        }
        tblXe.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblXe.getSelectedRow() >= 0) {
                hienThiXeDaChon();
            }
        });

        pnlTableCard.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableCard.add(scrXe, BorderLayout.CENTER);
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

    private JPanel createCardPanel() {
        JPanel pnlCard = AppPanel.card(new BorderLayout());
        return pnlCard;
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

    private void configureComponents() {
        txtKhachHang.setToolTipText("Nhập mã, họ tên, số điện thoại, email hoặc địa chỉ để tìm khách hàng");
        lstGoiYKhachHang.setFocusable(false);
        lstGoiYKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstGoiYKhachHang.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lblKhachHang = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang) {
                    KhachHang khachHang = (KhachHang) value;
                    lblKhachHang.setText(khachHang.getMaKH() + " - " + khachHang.getHoTen()
                            + " | " + cellText(khachHang.getSoDT())
                            + " | " + cellText(khachHang.getEmail()));
                }
                return lblKhachHang;
            }
        });
        lstGoiYKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 1) {
                    KhachHang khachHang = lstGoiYKhachHang.getSelectedValue();
                    if (khachHang != null) {
                        chonKhachHang(khachHang);
                    }
                }
            }
        });
        txtKhachHang.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                timKhachHangTheoThongTinNhap();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                timKhachHangTheoThongTinNhap();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                timKhachHangTheoThongTinNhap();
            }
        });
        txtKhachHang.addActionListener(event -> chonKhachHangNeuDuyNhat());
        txtKhachHang.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                napKhachHang(false);
                timKhachHangTheoThongTinNhap();
            }

            @Override
            public void focusLost(FocusEvent event) {
                SwingUtilities.invokeLater(() -> {
                    if (!txtKhachHang.isFocusOwner()) {
                        anGoiYKhachHang();
                    }
                });
            }
        });
    }

    private void napKhachHang() {
        napKhachHang(true);
    }

    private void napKhachHang(boolean thongBaoNeuChuaCoKhachHang) {
        try {
            List<KhachHang> danhSachMoi = khachHangDAO.findAll();
            List<KhachHang> khachHangMoiThem = new ArrayList<>();
            List<KhachHang> khachHangDaCo = new ArrayList<>();
            for (KhachHang khachHang : danhSachMoi) {
                if (MA_KHACH_HANG_DA_BIET.contains(khachHang.getMaKH())) {
                    khachHangDaCo.add(khachHang);
                } else {
                    khachHangMoiThem.add(khachHang);
                    MA_KHACH_HANG_DA_BIET.add(khachHang.getMaKH());
                }
            }
            danhSachKhachHang.clear();
            danhSachKhachHang.addAll(khachHangMoiThem);
            danhSachKhachHang.addAll(khachHangDaCo);
            if (thongBaoNeuChuaCoKhachHang && danhSachKhachHang.isEmpty()) {
                hienThiLoi("Chưa có khách hàng. Hãy thêm khách hàng trước khi nhập xe.");
            }
        } catch (SQLException exception) {
            hienThiLoi("Không thể tải danh sách khách hàng: " + exception.getMessage());
        }
    }

    private void napDanhSachXe() {
        try {
            hienThiDanhSach(xeDAO.findAll());
            locDanhSachXe();
        } catch (SQLException exception) {
            hienThiLoi("Không thể tải danh sách xe: " + exception.getMessage());
        }
    }

    private void hienThiDanhSach(List<Xe> danhSachXe) {
        modelXe.setRowCount(0);
        for (Xe xe : danhSachXe) {
            modelXe.addRow(new Object[] {
                    xe.getMaXe(), xe.getBienSo(), xe.getHangXe(), xe.getDongXe(),
                    xe.getNamSanXuat(), xe.getSoKhung(), xe.getSoMay(), xe.getMaKH()
            });
        }
    }

    private void themXe() {
        try {
            Xe xe = docThongTinXe();
            if (xeDAO.insert(xe)) {
                napDanhSachXe();
                lamMoiForm();
                lblThongBao.setText("Đã thêm xe " + xe.getMaXe());
            }
        } catch (IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            hienThiLoi("Không thể thêm xe: " + exception.getMessage());
        }
    }

    private void capNhatXe() {
        try {
            Xe xe = docThongTinXe();
            if (xeDAO.update(xe)) {
                napDanhSachXe();
                lblThongBao.setText("Đã cập nhật xe " + xe.getMaXe());
            } else {
                hienThiLoi("Không tìm thấy xe cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            hienThiLoi("Không thể cập nhật xe: " + exception.getMessage());
        }
    }

    private void xoaXe() {
        String maXe = txtMaXe.getText().trim();
        if (maXe.isEmpty()) {
            hienThiLoi("Hãy chọn xe trong bảng hoặc nhập mã xe cần xóa.");
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa xe " + maXe + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (xeDAO.deleteById(maXe)) {
                napDanhSachXe();
                lamMoiForm();
                lblThongBao.setText("Đã xóa xe " + maXe);
            } else {
                hienThiLoi("Không tìm thấy xe cần xóa.");
            }
        } catch (SQLException exception) {
            hienThiLoi("Không thể xóa xe: " + exception.getMessage());
        }
    }

    private JPanel createSearchField(String labelText, AppTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new JLabel(labelText);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_MUTED);
        pnlField.add(lblField, BorderLayout.NORTH);
        pnlField.add(txtField, BorderLayout.CENTER);
        return pnlField;
    }

    private void themBoLangNghe(AppTextField txtField) {
        txtField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSachXe();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSachXe();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSachXe();
            }
        });
    }

    private void locDanhSachXe() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        themBoLoc(filters, txtLocMaXe.getText(), 0);
        themBoLoc(filters, txtLocBienSo.getText(), 1);
        themBoLoc(filters, txtLocHangXe.getText(), 2);
        themBoLoc(filters, txtLocDongXe.getText(), 3);
        sorterXe.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblThongBao.setText("Hiển thị " + sorterXe.getViewRowCount() + " / " + modelXe.getRowCount() + " xe");
    }

    private void themBoLoc(List<RowFilter<Object, Object>> filters, String giaTri, int cot) {
        String tuKhoa = giaTri.trim();
        if (!tuKhoa.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa), cot));
        }
    }

    private void xoaBoLocXe() {
        txtLocMaXe.setText("");
        txtLocBienSo.setText("");
        txtLocHangXe.setText("");
        txtLocDongXe.setText("");
    }

    private Xe docThongTinXe() {
        String maXe = txtMaXe.getText().trim();
        String bienSo = txtBienSo.getText().trim().toUpperCase(Locale.ROOT);
        if (maXe.isEmpty() || bienSo.isEmpty()) {
            throw new IllegalArgumentException("Mã xe và biển số không được để trống.");
        }
        if (!FormValidator.isValidLicensePlate(bienSo)) {
            throw new IllegalArgumentException(
                    "Biển số xe không đúng định dạng (ví dụ: 30A-123.45 hoặc 30A-12345).");
        }
        if (!FormValidator.isWithinMaxLength(txtHangXe.getText().trim(), 50)
                || !FormValidator.isWithinMaxLength(txtDongXe.getText().trim(), 50)) {
            throw new IllegalArgumentException("Hãng xe và dòng xe không được vượt quá 50 ký tự.");
        }
        if (!FormValidator.isWithinMaxLength(txtSoKhung.getText().trim(), 50)
                || !FormValidator.isWithinMaxLength(txtSoMay.getText().trim(), 50)) {
            throw new IllegalArgumentException("Số khung và số máy không được vượt quá 50 ký tự.");
        }
        if (khachHangDuocChon == null) {
            throw new IllegalArgumentException("Hãy tìm và chọn khách hàng từ danh sách gợi ý.");
        }

        Integer namSanXuat = null;
        String namSanXuatText = txtNamSanXuat.getText().trim();
        if (!namSanXuatText.isEmpty()) {
            if (!FormValidator.isValidOptionalProductionYear(namSanXuatText)) {
                throw new IllegalArgumentException(
                        "Năm sản xuất phải là số từ 1886 đến " + java.time.Year.now().getValue() + ".");
            }
            namSanXuat = Integer.valueOf(namSanXuatText);
        }
        return new Xe(
                maXe,
                bienSo,
                giaTriRongLaNull(txtHangXe.getText()),
                giaTriRongLaNull(txtDongXe.getText()),
                namSanXuat,
                giaTriRongLaNull(txtSoKhung.getText()),
                giaTriRongLaNull(txtSoMay.getText()),
                khachHangDuocChon.getMaKH());
    }

    private void timKhachHangTheoThongTinNhap() {
        if (dangCapNhatOKhachHang) {
            return;
        }
        khachHangDuocChon = null;
        String tuKhoa = chuanHoaTimKiem(txtKhachHang.getText().trim());
        List<KhachHang> ketQua = new ArrayList<>();
        for (KhachHang khachHang : danhSachKhachHang) {
            String thongTin = chuanHoaTimKiem(String.join(" ",
                    cellText(khachHang.getMaKH()),
                    cellText(khachHang.getHoTen()),
                    cellText(khachHang.getSoDT()),
                    cellText(khachHang.getEmail()),
                    cellText(khachHang.getDiaChi())));
            if (tuKhoa.isEmpty() || thongTin.contains(tuKhoa)) {
                ketQua.add(khachHang);
            }
        }

        String noiDungNhap = txtKhachHang.getText();
        SwingUtilities.invokeLater(() -> hienThiGoiYKhachHang(ketQua, noiDungNhap));
    }

    private void hienThiGoiYKhachHang(List<KhachHang> ketQua, String noiDungNhap) {
        if (!txtKhachHang.isShowing() || !noiDungNhap.equals(txtKhachHang.getText())) {
            return;
        }
        int soGoiY = Math.min(ketQua.size(), 8);
        modelGoiYKhachHang.clear();
        for (int index = 0; index < soGoiY; index++) {
            modelGoiYKhachHang.addElement(ketQua.get(index));
        }
        if (soGoiY == 0) {
            anGoiYKhachHang();
            return;
        }
        int chieuRong = Math.max(txtKhachHang.getWidth(), 520);
        int chieuCao = Math.min(soGoiY * 28, 224);
        if (wndGoiYKhachHang == null) {
            java.awt.Window cuaSoCha = SwingUtilities.getWindowAncestor(txtKhachHang);
            wndGoiYKhachHang = new JWindow(cuaSoCha);
            wndGoiYKhachHang.setFocusableWindowState(false);
            wndGoiYKhachHang.setType(java.awt.Window.Type.POPUP);
            wndGoiYKhachHang.setContentPane(new JScrollPane(lstGoiYKhachHang));
        }
        wndGoiYKhachHang.setSize(chieuRong, chieuCao);
        Point viTri = txtKhachHang.getLocationOnScreen();
        wndGoiYKhachHang.setLocation(viTri.x, viTri.y + txtKhachHang.getHeight());
        wndGoiYKhachHang.setVisible(true);
    }

    private void anGoiYKhachHang() {
        if (wndGoiYKhachHang != null) {
            wndGoiYKhachHang.setVisible(false);
        }
    }

    private void chonKhachHang(KhachHang khachHang) {
        khachHangDuocChon = khachHang;
        dangCapNhatOKhachHang = true;
        txtKhachHang.setText(khachHang.getMaKH() + " - " + khachHang.getHoTen()
                + " | " + cellText(khachHang.getSoDT()));
        dangCapNhatOKhachHang = false;
        anGoiYKhachHang();
    }

    private void chonKhachHangNeuDuyNhat() {
        String tuKhoa = chuanHoaTimKiem(txtKhachHang.getText().trim());
        KhachHang khachHangPhuHop = null;
        for (KhachHang khachHang : danhSachKhachHang) {
            String thongTin = chuanHoaTimKiem(String.join(" ",
                    cellText(khachHang.getMaKH()),
                    cellText(khachHang.getHoTen()),
                    cellText(khachHang.getSoDT()),
                    cellText(khachHang.getEmail()),
                    cellText(khachHang.getDiaChi())));
            if (thongTin.contains(tuKhoa)) {
                if (khachHangPhuHop != null) {
                    return;
                }
                khachHangPhuHop = khachHang;
            }
        }
        if (khachHangPhuHop != null) {
            chonKhachHang(khachHangPhuHop);
        }
    }

    private String chuanHoaTimKiem(String giaTri) {
        return Normalizer.normalize(giaTri, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }

    private String giaTriRongLaNull(String value) {
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private void hienThiXeDaChon() {
        int selectedRow = tblXe.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = tblXe.convertRowIndexToModel(selectedRow);
        txtMaXe.setText(String.valueOf(modelXe.getValueAt(modelRow, 0)));
        txtBienSo.setText(cellText(modelXe.getValueAt(modelRow, 1)));
        txtHangXe.setText(cellText(modelXe.getValueAt(modelRow, 2)));
        txtDongXe.setText(cellText(modelXe.getValueAt(modelRow, 3)));
        txtNamSanXuat.setText(cellText(modelXe.getValueAt(modelRow, 4)));
        txtSoKhung.setText(cellText(modelXe.getValueAt(modelRow, 5)));
        txtSoMay.setText(cellText(modelXe.getValueAt(modelRow, 6)));

        String maKhachHang = cellText(modelXe.getValueAt(modelRow, 7));
        khachHangDuocChon = null;
        dangCapNhatOKhachHang = true;
        txtKhachHang.setText("");
        dangCapNhatOKhachHang = false;
        for (KhachHang khachHang : danhSachKhachHang) {
            if (khachHang.getMaKH().equals(maKhachHang)) {
                chonKhachHang(khachHang);
                break;
            }
        }
    }

    private String cellText(Object value) {
        return value == null ? "" : value.toString();
    }

    private void lamMoiForm() {
        try {
            txtMaXe.setText(MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_XE, xeDAO.findAll(), Xe::getMaXe));
        } catch (SQLException exception) {
            hienThiLoi("Không thể phát sinh mã xe: " + exception.getMessage());
        }
        txtBienSo.setText("");
        txtHangXe.setText("");
        txtDongXe.setText("");
        txtNamSanXuat.setText("");
        txtSoKhung.setText("");
        txtSoMay.setText("");
        khachHangDuocChon = null;
        dangCapNhatOKhachHang = true;
        txtKhachHang.setText("");
        dangCapNhatOKhachHang = false;
        anGoiYKhachHang();
        tblXe.clearSelection();
        txtMaXe.requestFocusInWindow();
    }

    private GridBagConstraints buttonConstraints(int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        return gbc;
    }

    private void hienThiLoi(String message) {
        lblThongBao.setText(message);
        lblThongBao.setForeground(new Color(190, 55, 55));
        JOptionPane.showMessageDialog(this, message, "Quản lý xe", JOptionPane.ERROR_MESSAGE);
    }
}

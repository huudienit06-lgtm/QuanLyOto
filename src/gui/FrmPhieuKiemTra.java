package gui;

import dao.PhieuKiemTraDAO;
import dao.PhieuTiepNhanDAO;
import entity.PhieuKiemTra;
import entity.PhieuTiepNhan;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;
import util.MaTuDong;

public class FrmPhieuKiemTra extends JPanel {
    private final PhieuKiemTraDAO phieuKiemTraDAO = new PhieuKiemTraDAO();
    private final PhieuTiepNhanDAO phieuTiepNhanDAO = new PhieuTiepNhanDAO();

    private final AppTextField txtMaPKT = new AppTextField(16);
    private final javax.swing.JComboBox<PhieuTiepNhan> cboMaPTN = new javax.swing.JComboBox<>();
    private final AppTextField txtMaKH = new AppTextField(16);
    private final AppTextField txtMaXe = new AppTextField(16);
    private final AppTextField txtMaNV = new AppTextField(16);
    private final javax.swing.JTextArea txaTrangThai = new javax.swing.JTextArea(2, 20);
    private final javax.swing.JTextArea txaVanDe = new javax.swing.JTextArea(3, 20);
    private final AppTextField txtLocMaPKT = new AppTextField(12);
    private final AppTextField txtLocMaPTN = new AppTextField(12);
    private final AppTextField txtLocMaXe = new AppTextField(12);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

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
    private final AppTable tblPhieuKiemTra = new AppTable(modelPhieuKiemTra);
    private final TableRowSorter<DefaultTableModel> sorterPhieuKiemTra =
            new TableRowSorter<>(modelPhieuKiemTra);

    public FrmPhieuKiemTra() {
        setLayout(new BorderLayout(0, 8));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(14, 22, 14, 22));

        add(createHeading(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        cauHinhThanhPhan();
        napDanhSachPhieuTiepNhan();
        napDanhSachPhieuKiemTra();
        lamMoiForm();
    }

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0, 5));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("PHIẾU KIỂM TRA");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new AppLabel(
                "Lập phiếu kiểm tra tình trạng xe và tra cứu các phiếu đã lập",
                AppLabel.Style.MUTED);

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
        javax.swing.JSplitPane splContent = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT, pnlForm, createTablePanel());
        splContent.setBorder(null);
        splContent.setResizeWeight(0.0);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        javax.swing.SwingUtilities.invokeLater(() -> splContent.setDividerLocation(chieuCaoForm));
        pnlContent.add(splContent, BorderLayout.CENTER);
        return pnlContent;
    }

    private JPanel createFormPanel() {
        JPanel pnlFormCard = AppPanel.card(new BorderLayout(0, 6));
        pnlFormCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConfig.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        JLabel lblFormTitle = createSectionTitle("Thông tin phiếu kiểm tra");
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        addField(pnlFields, "Mã phiếu kiểm tra (tự động)", txtMaPKT, 0, 0);
        addField(pnlFields, "Phiếu tiếp nhận *", cboMaPTN, 2, 0);
        addField(pnlFields, "Mã khách hàng", txtMaKH, 0, 1);
        addField(pnlFields, "Mã xe", txtMaXe, 2, 1);
        addField(pnlFields, "Mã nhân viên", txtMaNV, 0, 2);
        addLongField(pnlFields, "Trạng thái xe ban đầu *", txaTrangThai, 0, 3);
        addLongField(pnlFields, "Vấn đề hư hỏng *", txaVanDe, 2, 3);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        AppButton btnThem = new AppButton("Thêm phiếu", AppButton.Style.PRIMARY);
        AppButton btnSua = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
        AppButton btnXoa = new AppButton("Xóa phiếu", AppButton.Style.DANGER);
        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
        btnThem.addActionListener(event -> themPhieuKiemTra());
        btnSua.addActionListener(event -> capNhatPhieuKiemTra());
        btnXoa.addActionListener(event -> xoaPhieuKiemTra());
        btnLamMoi.addActionListener(event -> lamMoiForm());
        pnlButtons.add(btnThem, buttonConstraints(0));
        pnlButtons.add(btnSua, buttonConstraints(1));
        pnlButtons.add(btnXoa, buttonConstraints(2));
        pnlButtons.add(btnLamMoi, buttonConstraints(3));

        JPanel pnlFieldsAndButtons = new JPanel();
        pnlFieldsAndButtons.setOpaque(false);
        pnlFieldsAndButtons.setLayout(new javax.swing.BoxLayout(
                pnlFieldsAndButtons, javax.swing.BoxLayout.Y_AXIS));
        pnlFields.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlFields.getPreferredSize().height));
        pnlButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlButtons.getPreferredSize().height));
        pnlFieldsAndButtons.add(pnlFields);
        pnlFieldsAndButtons.add(javax.swing.Box.createVerticalStrut(8));
        pnlFieldsAndButtons.add(pnlButtons);

        pnlFormCard.add(lblFormTitle, BorderLayout.NORTH);
        pnlFormCard.add(pnlFieldsAndButtons, BorderLayout.CENTER);
        return pnlFormCard;
    }

    private JPanel createTablePanel() {
        JPanel pnlTableCard = AppPanel.card(new BorderLayout(0, 8));
        JPanel pnlTableHeader = new JPanel(new BorderLayout(8, 8));
        pnlTableHeader.setOpaque(false);
        JPanel pnlTitleBar = new JPanel(new BorderLayout());
        pnlTitleBar.setOpaque(false);
        pnlTitleBar.add(createSectionTitle("Danh sách phiếu kiểm tra"), BorderLayout.WEST);
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTaiLai.addActionListener(event -> {
            xoaBoLoc();
            napDanhSachPhieuTiepNhan();
            napDanhSachPhieuKiemTra();
        });
        pnlTitleBar.add(btnTaiLai, BorderLayout.EAST);

        JPanel pnlLoc = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã phiếu kiểm tra", txtLocMaPKT));
        pnlLoc.add(createSearchField("Mã phiếu tiếp nhận", txtLocMaPTN));
        pnlLoc.add(createSearchField("Mã xe", txtLocMaXe));
        themBoLangNghe(txtLocMaPKT);
        themBoLangNghe(txtLocMaPTN);
        themBoLangNghe(txtLocMaXe);
        pnlTableHeader.add(pnlTitleBar, BorderLayout.NORTH);
        pnlTableHeader.add(pnlLoc, BorderLayout.CENTER);

        tblPhieuKiemTra.setRowSorter(sorterPhieuKiemTra);
        tblPhieuKiemTra.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhieuKiemTra.setFillsViewportHeight(true);
        tblPhieuKiemTra.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblPhieuKiemTra.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblPhieuKiemTra.getSelectedRow() >= 0) {
                hienThiPhieuDuocChon();
            }
        });
        pnlTableCard.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableCard.add(new AppScrollPane(tblPhieuKiemTra), BorderLayout.CENTER);
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
            JPanel pnlFields, String label, javax.swing.JTextArea txaField, int column, int row) {
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
        javax.swing.JScrollPane scrField = new AppScrollPane(txaField);
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

    private GridBagConstraints buttonConstraints(int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.insets = new Insets(3, 0, 2, 8);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void cauHinhThanhPhan() {
        txtMaPKT.setEditable(false);
        for (AppTextField txtField : new AppTextField[] {txtMaKH, txtMaXe, txtMaNV}) {
            txtField.setEditable(false);
            txtField.setBackground(UIConfig.COLOR_BACKGROUND);
        }
        cboMaPTN.setFont(UIConfig.FONT_REGULAR);
        cboMaPTN.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lblItem = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhieuTiepNhan phieuTiepNhan) {
                    lblItem.setText(phieuTiepNhan.getMaPhieuTiepNhan() + " - " + phieuTiepNhan.getMaXe());
                }
                return lblItem;
            }
        });
        cboMaPTN.addActionListener(event -> capNhatThongTinTheoPhieuTiepNhan());
        tblPhieuKiemTra.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblPhieuKiemTra.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblPhieuKiemTra.getColumnModel().getColumn(5).setPreferredWidth(180);
        tblPhieuKiemTra.getColumnModel().getColumn(6).setPreferredWidth(220);
    }

    private void napDanhSachPhieuTiepNhan() {
        try {
            List<PhieuTiepNhan> danhSach = phieuTiepNhanDAO.findAll();
            cboMaPTN.removeAllItems();
            for (PhieuTiepNhan phieuTiepNhan : danhSach) {
                cboMaPTN.addItem(phieuTiepNhan);
            }
        } catch (SQLException exception) {
            hienThiLoi("Không thể tải danh sách phiếu tiếp nhận: " + exception.getMessage());
        }
    }

    private void capNhatThongTinTheoPhieuTiepNhan() {
        PhieuTiepNhan phieuTiepNhan = (PhieuTiepNhan) cboMaPTN.getSelectedItem();
        if (phieuTiepNhan == null) {
            txtMaKH.setText("");
            txtMaXe.setText("");
            txtMaNV.setText("");
            return;
        }
        txtMaKH.setText(phieuTiepNhan.getMaKH());
        txtMaXe.setText(phieuTiepNhan.getMaXe());
        txtMaNV.setText(phieuTiepNhan.getMaNV());
    }

    private void napDanhSachPhieuKiemTra() {
        try {
            List<PhieuKiemTra> danhSach = phieuKiemTraDAO.findAll();
            modelPhieuKiemTra.setRowCount(0);
            for (PhieuKiemTra phieu : danhSach) {
                modelPhieuKiemTra.addRow(new Object[] {
                        phieu.getMaPhieuKiemTra(),
                        phieu.getMaPhieuTiepNhan(),
                        phieu.getMaKH(),
                        phieu.getMaXe(),
                        phieu.getMaNV(),
                        phieu.getTrangThaiBanDau(),
                        phieu.getVanDeHuHong()
                });
            }
            locDanhSach();
            thongBao("Đã tải " + danhSach.size() + " phiếu kiểm tra");
        } catch (SQLException exception) {
            hienThiLoi("Không thể tải danh sách phiếu kiểm tra: " + exception.getMessage());
        }
    }

    private void themPhieuKiemTra() {
        try {
            PhieuKiemTra phieu = docThongTinPhieu();
            if (phieuKiemTraDAO.insert(phieu)) {
                napDanhSachPhieuKiemTra();
                lamMoiForm();
                thongBao("Đã thêm phiếu kiểm tra " + phieu.getMaPhieuKiemTra());
            }
        } catch (IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            hienThiLoi("Không thể thêm phiếu kiểm tra: " + exception.getMessage());
        }
    }

    private void capNhatPhieuKiemTra() {
        try {
            PhieuKiemTra phieu = docThongTinPhieu();
            if (phieuKiemTraDAO.update(phieu)) {
                napDanhSachPhieuKiemTra();
                thongBao("Đã cập nhật phiếu kiểm tra " + phieu.getMaPhieuKiemTra());
            } else {
                hienThiLoi("Không tìm thấy phiếu kiểm tra cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            hienThiLoi("Không thể cập nhật phiếu kiểm tra: " + exception.getMessage());
        }
    }

    private void xoaPhieuKiemTra() {
        String maPKT = txtMaPKT.getText().trim();
        if (maPKT.isEmpty()) {
            hienThiLoi("Hãy chọn phiếu kiểm tra cần xóa.");
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa phiếu kiểm tra " + maPKT + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (phieuKiemTraDAO.deleteById(maPKT)) {
                napDanhSachPhieuKiemTra();
                lamMoiForm();
                thongBao("Đã xóa phiếu kiểm tra " + maPKT);
            } else {
                hienThiLoi("Không tìm thấy phiếu kiểm tra cần xóa.");
            }
        } catch (SQLException exception) {
            hienThiLoi("Không thể xóa phiếu kiểm tra: " + exception.getMessage());
        }
    }

    private PhieuKiemTra docThongTinPhieu() {
        String maPKT = txtMaPKT.getText().trim();
        PhieuTiepNhan phieuTiepNhan = (PhieuTiepNhan) cboMaPTN.getSelectedItem();
        String trangThai = txaTrangThai.getText().trim();
        String vanDe = txaVanDe.getText().trim();
        if (maPKT.isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu kiểm tra không được để trống.");
        }
        if (maPKT.length() > 20) {
            throw new IllegalArgumentException("Mã phiếu kiểm tra không được vượt quá 20 ký tự.");
        }
        if (phieuTiepNhan == null) {
            throw new IllegalArgumentException("Hãy chọn phiếu tiếp nhận.");
        }
        if (trangThai.isEmpty()) {
            throw new IllegalArgumentException("Trạng thái xe ban đầu không được để trống.");
        }
        if (vanDe.isEmpty()) {
            throw new IllegalArgumentException("Vấn đề hư hỏng không được để trống.");
        }

        PhieuKiemTra phieu = new PhieuKiemTra();
        phieu.setMaPhieuKiemTra(maPKT);
        phieu.setMaPhieuTiepNhan(phieuTiepNhan.getMaPhieuTiepNhan());
        phieu.setMaKH(phieuTiepNhan.getMaKH());
        phieu.setMaXe(phieuTiepNhan.getMaXe());
        phieu.setMaNV(phieuTiepNhan.getMaNV());
        phieu.setTrangThaiBanDau(trangThai);
        phieu.setVanDeHuHong(vanDe);
        return phieu;
    }

    private void hienThiPhieuDuocChon() {
        int viewRow = tblPhieuKiemTra.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = tblPhieuKiemTra.convertRowIndexToModel(viewRow);
        txtMaPKT.setText(text(modelPhieuKiemTra.getValueAt(modelRow, 0)));
        String maPTN = text(modelPhieuKiemTra.getValueAt(modelRow, 1));
        for (int index = 0; index < cboMaPTN.getItemCount(); index++) {
            PhieuTiepNhan phieuTiepNhan = cboMaPTN.getItemAt(index);
            if (phieuTiepNhan.getMaPhieuTiepNhan().equals(maPTN)) {
                cboMaPTN.setSelectedItem(phieuTiepNhan);
                break;
            }
        }
        txaTrangThai.setText(text(modelPhieuKiemTra.getValueAt(modelRow, 5)));
        txaVanDe.setText(text(modelPhieuKiemTra.getValueAt(modelRow, 6)));
        thongBao("Đang chọn phiếu kiểm tra " + txtMaPKT.getText());
    }

    private void lamMoiForm() {
        try {
            txtMaPKT.setText(MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_PHIEU_KIEM_TRA,
                    phieuKiemTraDAO.findAll(),
                    PhieuKiemTra::getMaPhieuKiemTra));
        } catch (SQLException exception) {
            hienThiLoi("Không thể phát sinh mã phiếu kiểm tra: " + exception.getMessage());
            txtMaPKT.setText("");
        }
        if (cboMaPTN.getItemCount() > 0) {
            cboMaPTN.setSelectedIndex(0);
        } else {
            capNhatThongTinTheoPhieuTiepNhan();
        }
        txaTrangThai.setText("");
        txaVanDe.setText("");
        tblPhieuKiemTra.clearSelection();
        thongBao("Sẵn sàng");
    }

    private JPanel createSearchField(String label, AppTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new JLabel(label);
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
                locDanhSach();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSach();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSach();
            }
        });
    }

    private void locDanhSach() {
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

    private void xoaBoLoc() {
        txtLocMaPKT.setText("");
        txtLocMaPTN.setText("");
        txtLocMaXe.setText("");
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }

    private void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    private void hienThiLoi(String message) {
        lblThongBao.setForeground(new java.awt.Color(190, 55, 55));
        lblThongBao.setText(message);
        JOptionPane.showMessageDialog(this, message, "Phiếu kiểm tra", JOptionPane.ERROR_MESSAGE);
    }
}

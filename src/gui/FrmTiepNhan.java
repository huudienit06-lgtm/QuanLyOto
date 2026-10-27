package gui;

import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.PhieuTiepNhanDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuTiepNhan;
import entity.Xe;
import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import util.MaTuDong;

public class FrmTiepNhan extends JPanel {
    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] TRANG_THAI_MAC_DINH = {
            "Đã tiếp nhận", "Đang kiểm tra", "Đang sửa chữa", "Hoàn thành", "Đã hủy"
    };

    private final PhieuTiepNhanDAO phieuTiepNhanDAO = new PhieuTiepNhanDAO();
    private final XeDAO xeDAO = new XeDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private final AppTextField txtMaPhieuTiepNhan = new AppTextField(16);
    private final AppComboBox<Xe> cboXe = new AppComboBox<>();
    private final AppTextField txtMaKH = new AppTextField(16);
    private final AppTextField txtTenKhachHang = new AppTextField(18);
    private final AppComboBox<NhanVien> cboNhanVien = new AppComboBox<>();
    private final AppTextField txtNgayLap = new AppTextField(16);
    private final AppComboBox<String> cboTrangThai = new AppComboBox<>();
    private final JTextArea txaGhiChu = new JTextArea(3, 24);
    private final AppTextField txtTimKiem = new AppTextField(20);
    private final AppLabel lblThongBao = new AppLabel("Sẵn sàng", AppLabel.Style.MUTED);

    private final DefaultTableModel modelTiepNhan = new DefaultTableModel(
            new Object[] {
                    "Mã phiếu", "Biển số", "Mã khách hàng", "Tên khách hàng",
                    "Nhân viên", "Ngày lập", "Trạng thái", "Ghi chú"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final AppTable tblTiepNhan = new AppTable(modelTiepNhan);
    private final TableRowSorter<DefaultTableModel> sorterTiepNhan =
            new TableRowSorter<>(modelTiepNhan);
    private final Map<String, KhachHang> khachHangTheoMa = new HashMap<>();
    private final Map<String, PhieuTiepNhan> phieuTheoMa = new HashMap<>();
    private Timestamp ngayLapDangChon;
    private boolean dangCapNhatPhieu;

    public FrmTiepNhan() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setOpaque(false);
        pnlContent.add(createFormCard(), BorderLayout.NORTH);
        pnlContent.add(FormUI.createTableCard(
                "Danh sách phiếu tiếp nhận",
                createSearchToolbar(),
                new JScrollPane(tblTiepNhan)),
                BorderLayout.CENTER);

        add(FormUI.createPage(
                "TIẾP NHẬN XE",
                "Lập phiếu tiếp nhận xe và theo dõi tình trạng xử lý tại gara",
                pnlContent),
                BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        configureComponents();
        napDuLieuDanhMuc();
        napDanhSachPhieu();
        lamMoiForm();
    }

    private JPanel createFormCard() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.setLayout(new BorderLayout(0, 10));

        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Mã phiếu tiếp nhận (tự động)", txtMaPhieuTiepNhan, 0, 0);
        FormUI.addField(pnlFields, "Xe tiếp nhận *", cboXe, 2, 0);
        FormUI.addField(pnlFields, "Mã khách hàng", txtMaKH, 0, 1);
        FormUI.addField(pnlFields, "Tên khách hàng", txtTenKhachHang, 2, 1);
        FormUI.addField(pnlFields, "Nhân viên tiếp nhận *", cboNhanVien, 0, 2);
        FormUI.addField(pnlFields, "Ngày lập", txtNgayLap, 2, 2);
        FormUI.addField(pnlFields, "Trạng thái", cboTrangThai, 0, 3);

        JPanel pnlGhiChu = new JPanel(new BorderLayout(0, 4));
        pnlGhiChu.setOpaque(false);
        pnlGhiChu.add(new AppLabel("Ghi chú", AppLabel.Style.MUTED), BorderLayout.NORTH);
        txaGhiChu.setLineWrap(true);
        txaGhiChu.setWrapStyleWord(true);
        txaGhiChu.setFont(UIConfig.FONT_REGULAR);
        pnlGhiChu.add(new AppScrollPane(txaGhiChu), BorderLayout.CENTER);

        JPanel pnlMoTa = new JPanel(new BorderLayout());
        pnlMoTa.setOpaque(false);
        pnlMoTa.add(pnlGhiChu, BorderLayout.CENTER);

        AppButton btnThem = new AppButton("Lập phiếu", AppButton.Style.SUCCESS);
        AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.PRIMARY);
        AppButton btnXoa = new AppButton("Xóa phiếu", AppButton.Style.DANGER);
        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
        btnThem.addActionListener(event -> themPhieu());
        btnCapNhat.addActionListener(event -> capNhatPhieu());
        btnXoa.addActionListener(event -> xoaPhieu());
        btnLamMoi.addActionListener(event -> lamMoiForm());

        JPanel pnlFormBody = new JPanel(new BorderLayout(0, 4));
        pnlFormBody.setOpaque(false);
        pnlFormBody.add(pnlFields, BorderLayout.NORTH);
        pnlFormBody.add(pnlMoTa, BorderLayout.CENTER);

        pnlCard.add(FormUI.createSectionTitle("Thông tin tiếp nhận"), BorderLayout.NORTH);
        pnlCard.add(pnlFormBody, BorderLayout.CENTER);
        pnlCard.add(FormUI.createButtonRow(btnThem, btnCapNhat, btnXoa, btnLamMoi), BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createSearchToolbar() {
        JPanel pnlToolbar = new JPanel(new BorderLayout(8, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.add(new AppLabel("Tìm kiếm", AppLabel.Style.MUTED), BorderLayout.WEST);
        pnlToolbar.add(txtTimKiem, BorderLayout.CENTER);
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTaiLai.addActionListener(event -> {
            napDuLieuDanhMuc();
            napDanhSachPhieu();
        });
        pnlToolbar.add(btnTaiLai, BorderLayout.EAST);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
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
        return pnlToolbar;
    }

    private JPanel createStatusPanel() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private void configureComponents() {
        txtMaPhieuTiepNhan.setEditable(false);
        txtMaKH.setEditable(false);
        txtTenKhachHang.setEditable(false);
        txtNgayLap.setEditable(false);
        cboTrangThai.setEditable(true);
        for (String trangThai : TRANG_THAI_MAC_DINH) {
            cboTrangThai.addItem(trangThai);
        }
        cboXe.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                javax.swing.JLabel lblItem = (javax.swing.JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Xe xe) {
                    lblItem.setText(xe.getBienSo() + " - " + xe.getHangXe() + " " + xe.getDongXe());
                }
                return lblItem;
            }
        });
        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                javax.swing.JLabel lblItem = (javax.swing.JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien nhanVien) {
                    lblItem.setText(nhanVien.getMaNV() + " - " + nhanVien.getHoTen());
                }
                return lblItem;
            }
        });
        cboXe.addActionListener(event -> capNhatThongTinKhachHang());
        tblTiepNhan.setRowSorter(sorterTiepNhan);
        tblTiepNhan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTiepNhan.setFillsViewportHeight(true);
        tblTiepNhan.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblTiepNhan.getSelectedRow() >= 0) {
                hienThiPhieuDangChon();
            }
        });
    }

    private void napDuLieuDanhMuc() {
        try {
            List<Xe> danhSachXe = xeDAO.findAll();
            List<NhanVien> danhSachNhanVien = nhanVienDAO.findAll();
            List<KhachHang> danhSachKhachHang = khachHangDAO.findAll();

            cboXe.removeAllItems();
            for (Xe xe : danhSachXe) {
                cboXe.addItem(xe);
            }
            cboNhanVien.removeAllItems();
            for (NhanVien nhanVien : danhSachNhanVien) {
                cboNhanVien.addItem(nhanVien);
            }
            khachHangTheoMa.clear();
            for (KhachHang khachHang : danhSachKhachHang) {
                khachHangTheoMa.put(khachHang.getMaKH(), khachHang);
            }
            capNhatThongTinKhachHang();
        } catch (SQLException exception) {
            hienThiLoi("Không thể tải danh mục xe, khách hàng, nhân viên: " + exception.getMessage());
        }
    }

    private void capNhatThongTinKhachHang() {
        Xe xe = (Xe) cboXe.getSelectedItem();
        KhachHang khachHang = xe == null ? null : khachHangTheoMa.get(xe.getMaKH());
        txtMaKH.setText(khachHang == null ? "" : khachHang.getMaKH());
        txtTenKhachHang.setText(khachHang == null ? "" : khachHang.getHoTen());
    }

    private void napDanhSachPhieu() {
        try {
            List<PhieuTiepNhan> danhSach = phieuTiepNhanDAO.findAll();
            modelTiepNhan.setRowCount(0);
            phieuTheoMa.clear();
            for (PhieuTiepNhan phieu : danhSach) {
                Xe xe = timXe(phieu.getMaXe());
                KhachHang khachHang = khachHangTheoMa.get(phieu.getMaKH());
                NhanVien nhanVien = timNhanVien(phieu.getMaNV());
                phieuTheoMa.put(phieu.getMaPhieuTiepNhan(), phieu);
                modelTiepNhan.addRow(new Object[] {
                        phieu.getMaPhieuTiepNhan(),
                        xe == null ? phieu.getMaXe() : xe.getBienSo(),
                        phieu.getMaKH(),
                        khachHang == null ? "" : khachHang.getHoTen(),
                        nhanVien == null ? phieu.getMaNV() : nhanVien.getHoTen(),
                        dinhDangNgay(phieu.getNgayLap()),
                        phieu.getTrangThai(),
                        phieu.getGhiChu()
                });
            }
            locDanhSach();
            thongBao("Đã tải " + danhSach.size() + " phiếu tiếp nhận");
        } catch (SQLException exception) {
            hienThiLoi("Không thể tải danh sách phiếu tiếp nhận: " + exception.getMessage());
        }
    }

    private void themPhieu() {
        try {
            PhieuTiepNhan phieu = docThongTinPhieu();
            if (phieuTiepNhanDAO.insert(phieu)) {
                napDanhSachPhieu();
                lamMoiForm();
                thongBao("Đã lập phiếu tiếp nhận " + phieu.getMaPhieuTiepNhan());
            }
        } catch (IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            hienThiLoi("Không thể lập phiếu tiếp nhận: " + exception.getMessage());
        }
    }

    private void capNhatPhieu() {
        try {
            PhieuTiepNhan phieu = docThongTinPhieu();
            if (phieuTiepNhanDAO.update(phieu)) {
                napDanhSachPhieu();
                thongBao("Đã cập nhật phiếu tiếp nhận " + phieu.getMaPhieuTiepNhan());
            } else {
                hienThiLoi("Không tìm thấy phiếu tiếp nhận cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            hienThiLoi("Không thể cập nhật phiếu tiếp nhận: " + exception.getMessage());
        }
    }

    private void xoaPhieu() {
        String maPhieu = txtMaPhieuTiepNhan.getText().trim();
        if (maPhieu.isEmpty()) {
            hienThiLoi("Hãy chọn phiếu tiếp nhận cần xóa.");
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa phiếu tiếp nhận " + maPhieu + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (phieuTiepNhanDAO.deleteById(maPhieu)) {
                napDanhSachPhieu();
                lamMoiForm();
                thongBao("Đã xóa phiếu tiếp nhận " + maPhieu);
            } else {
                hienThiLoi("Không tìm thấy phiếu tiếp nhận cần xóa.");
            }
        } catch (SQLException exception) {
            hienThiLoi("Không thể xóa phiếu tiếp nhận. Phiếu có thể đang được phiếu kiểm tra sử dụng: "
                    + exception.getMessage());
        }
    }

    private PhieuTiepNhan docThongTinPhieu() {
        String maPhieu = txtMaPhieuTiepNhan.getText().trim();
        Xe xe = (Xe) cboXe.getSelectedItem();
        NhanVien nhanVien = (NhanVien) cboNhanVien.getSelectedItem();
        String trangThai = String.valueOf(cboTrangThai.getEditor().getItem()).trim();

        if (maPhieu.isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không được để trống.");
        }
        if (maPhieu.length() > 20) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không được vượt quá 20 ký tự.");
        }
        if (xe == null) {
            throw new IllegalArgumentException("Hãy chọn xe cần tiếp nhận.");
        }
        if (nhanVien == null) {
            throw new IllegalArgumentException("Hãy chọn nhân viên tiếp nhận.");
        }
        if (trangThai.isEmpty()) {
            throw new IllegalArgumentException("Trạng thái tiếp nhận không được để trống.");
        }
        if (trangThai.length() > 20) {
            throw new IllegalArgumentException("Trạng thái tiếp nhận không được vượt quá 20 ký tự.");
        }

        PhieuTiepNhan phieu = new PhieuTiepNhan();
        phieu.setMaPhieuTiepNhan(maPhieu);
        phieu.setMaXe(xe.getMaXe());
        phieu.setMaKH(xe.getMaKH());
        phieu.setMaNV(nhanVien.getMaNV());
        phieu.setNgayLap(dangCapNhatPhieu ? ngayLapDangChon : Timestamp.valueOf(LocalDateTime.now()));
        phieu.setTrangThai(trangThai);
        String ghiChu = txaGhiChu.getText().trim();
        phieu.setGhiChu(ghiChu.isEmpty() ? null : ghiChu);
        return phieu;
    }

    private void hienThiPhieuDangChon() {
        int viewRow = tblTiepNhan.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = tblTiepNhan.convertRowIndexToModel(viewRow);
        String maPhieu = text(modelTiepNhan.getValueAt(modelRow, 0));
        PhieuTiepNhan phieu = phieuTheoMa.get(maPhieu);
        if (phieu == null) {
            return;
        }

        txtMaPhieuTiepNhan.setText(phieu.getMaPhieuTiepNhan());
        chonXe(phieu.getMaXe());
        chonNhanVien(phieu.getMaNV());
        ngayLapDangChon = phieu.getNgayLap();
        txtNgayLap.setText(dinhDangNgay(ngayLapDangChon));
        chonTrangThai(phieu.getTrangThai());
        txaGhiChu.setText(phieu.getGhiChu() == null ? "" : phieu.getGhiChu());
        dangCapNhatPhieu = true;
        thongBao("Đang chọn phiếu tiếp nhận " + maPhieu);
    }

    private void lamMoiForm() {
        try {
            txtMaPhieuTiepNhan.setText(MaTuDong.sinhMa(
                    MaTuDong.TIEN_TO_PHIEU_TIEP_NHAN,
                    phieuTiepNhanDAO.findAll(),
                    PhieuTiepNhan::getMaPhieuTiepNhan));
        } catch (SQLException exception) {
            hienThiLoi("Không thể phát sinh mã phiếu tiếp nhận: " + exception.getMessage());
            txtMaPhieuTiepNhan.setText("");
        }
        dangCapNhatPhieu = false;
        if (cboXe.getItemCount() > 0) {
            cboXe.setSelectedIndex(0);
        } else {
            capNhatThongTinKhachHang();
        }
        if (cboNhanVien.getItemCount() > 0) {
            cboNhanVien.setSelectedIndex(0);
        }
        ngayLapDangChon = Timestamp.valueOf(LocalDateTime.now());
        txtNgayLap.setText(dinhDangNgay(ngayLapDangChon));
        chonTrangThai(TRANG_THAI_MAC_DINH[0]);
        txaGhiChu.setText("");
        tblTiepNhan.clearSelection();
        thongBao("Sẵn sàng");
    }

    private void chonXe(String maXe) {
        for (int index = 0; index < cboXe.getItemCount(); index++) {
            if (cboXe.getItemAt(index).getMaXe().equals(maXe)) {
                cboXe.setSelectedIndex(index);
                return;
            }
        }
    }

    private void chonNhanVien(String maNV) {
        for (int index = 0; index < cboNhanVien.getItemCount(); index++) {
            if (cboNhanVien.getItemAt(index).getMaNV().equals(maNV)) {
                cboNhanVien.setSelectedIndex(index);
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

    private Xe timXe(String maXe) {
        for (int index = 0; index < cboXe.getItemCount(); index++) {
            Xe xe = cboXe.getItemAt(index);
            if (xe.getMaXe().equals(maXe)) {
                return xe;
            }
        }
        return null;
    }

    private NhanVien timNhanVien(String maNV) {
        for (int index = 0; index < cboNhanVien.getItemCount(); index++) {
            NhanVien nhanVien = cboNhanVien.getItemAt(index);
            if (nhanVien.getMaNV().equals(maNV)) {
                return nhanVien;
            }
        }
        return null;
    }

    private String dinhDangNgay(Timestamp ngayLap) {
        return ngayLap == null ? "" : DINH_DANG_NGAY.format(ngayLap.toLocalDateTime());
    }

    private void locDanhSach() {
        String tuKhoa = txtTimKiem.getText().trim();
        sorterTiepNhan.setRowFilter(tuKhoa.isEmpty()
                ? null
                : RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa)));
        lblThongBao.setText("Hiển thị " + sorterTiepNhan.getViewRowCount()
                + " / " + modelTiepNhan.getRowCount() + " phiếu tiếp nhận");
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }

    private void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    private void hienThiLoi(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        lblThongBao.setText(message);
        JOptionPane.showMessageDialog(this, message, "Tiếp nhận xe", JOptionPane.ERROR_MESSAGE);
    }
}

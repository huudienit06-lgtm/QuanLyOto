package gui;

import controller.TiepNhanController;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.Xe;
import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmTiepNhan extends JPanel {
    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] TRANG_THAI_MAC_DINH = {
            "Đã tiếp nhận", "Đang kiểm tra", "Đang sửa chữa", "Hoàn thành", "Đã hủy"
    };

    private final AppTextField txtMaPhieuTiepNhan = new AppTextField(16);
    private final AppComboBox<Xe> cboXe = new AppComboBox<>();
    private final AppTextField txtMaKH = new AppTextField(16);
    private final AppTextField txtTenKhachHang = new AppTextField(18);
    private final AppTextField txtNhanVien = new AppTextField(18);
    private final AppTextField txtNgayLap = new AppTextField(16);
    private final AppComboBox<String> cboTrangThai = new AppComboBox<>();
    private final JTextArea txaGhiChu = new JTextArea(3, 24);
    private final AppTextField txtTimKiem = new AppTextField(20);
    private final AppLabel lblThongBao = new AppLabel("Sẵn sàng", AppLabel.Style.MUTED);
    private final AppButton btnThem = new AppButton("Lập phiếu", AppButton.Style.SUCCESS);
    private final AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.PRIMARY);
    private final AppButton btnXoa = new AppButton("Xóa phiếu", AppButton.Style.DANGER);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);

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
    private final TiepNhanController controller;

    public FrmTiepNhan() {
        this(null);
    }

    public FrmTiepNhan(TaiKhoan taiKhoanHienTai) {
        this.controller = new TiepNhanController(this, taiKhoanHienTai);
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
        controller.khoiTao();
    }

    private JPanel createFormCard() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.setLayout(new BorderLayout(0, 10));

        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Mã phiếu tiếp nhận (tự động)", txtMaPhieuTiepNhan, 0, 0);
        FormUI.addField(pnlFields, "Xe tiếp nhận *", cboXe, 2, 0);
        FormUI.addField(pnlFields, "Mã khách hàng", txtMaKH, 0, 1);
        FormUI.addField(pnlFields, "Tên khách hàng", txtTenKhachHang, 2, 1);
        FormUI.addField(pnlFields, "Nhân viên tiếp nhận *", txtNhanVien, 0, 2);
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
        pnlToolbar.add(btnTaiLai, BorderLayout.EAST);
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
        txtNhanVien.setEditable(false);
        txtNhanVien.setBackground(UIConfig.COLOR_BACKGROUND);
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
        tblTiepNhan.setRowSorter(sorterTiepNhan);
        tblTiepNhan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTiepNhan.setFillsViewportHeight(true);
    }

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

    public AppButton getBtnThem() { return btnThem; }
    public AppButton getBtnCapNhat() { return btnCapNhat; }
    public AppButton getBtnXoa() { return btnXoa; }
    public AppButton getBtnLamMoi() { return btnLamMoi; }
    public AppButton getBtnTaiLai() { return btnTaiLai; }
    public AppComboBox<Xe> getCboXe() { return cboXe; }
    public AppTextField getTxtTimKiem() { return txtTimKiem; }
    public AppTable getTblTiepNhan() { return tblTiepNhan; }

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

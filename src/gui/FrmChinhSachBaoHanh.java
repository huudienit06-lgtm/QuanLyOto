package gui;

import controller.ChinhSachBaoHanhController;
import entity.ChinhSachBaoHanh;
import entity.PhuTung;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class FrmChinhSachBaoHanh extends JPanel {
    private record DonViThoiGian(String ma, String ten) {
        @Override
        public String toString() {
            return ten;
        }
    }

    private final ChinhSachBaoHanhController controller;
    private final AppTextField txtMaCSBH = new AppTextField(12);
    private final AppTextField txtTenChinhSach = new AppTextField(24);
    private final javax.swing.JSpinner spnThoiHan = new javax.swing.JSpinner(
            new javax.swing.SpinnerNumberModel(12, 1, 9999, 1));
    private final JComboBox<DonViThoiGian> cboDonViThoiGian = new JComboBox<>(new DonViThoiGian[] {
            new DonViThoiGian("THANG", "Tháng"),
            new DonViThoiGian("NGAY", "Ngày"),
            new DonViThoiGian("NAM", "Năm")
    });
    private final JCheckBox chkTrangThai = new JCheckBox("Đang áp dụng", true);
    private final JTextArea txaDieuKien = taoOThongTin();
    private final JTextArea txaTruongHopTuChoi = taoOThongTin();
    private final JTextArea txaNoiDungBaoHanh = taoOThongTin();
    private final AppTextField txtTimPhuTung = new AppTextField(16);
    private final AppTextField txtTimChinhSach = new AppTextField(18);
    private final AppButton btnThem = new AppButton("Thêm");
    private final AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
    private final AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnTimKiem = new AppButton("Tìm kiếm");
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
    private final JTable tblPhuTung = FormUI.createTable(modelPhuTung);
    private final DefaultTableModel modelChinhSach = new DefaultTableModel(
            new Object[] { "Mã chính sách", "Tên chính sách", "Thời hạn", "Phụ tùng", "Trạng thái", "Ngày tạo" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblChinhSach = FormUI.createTable(modelChinhSach);

    public FrmChinhSachBaoHanh() {
        setLayout(new BorderLayout());
        setOpaque(false);
        txtMaCSBH.setEditable(false);
        tblPhuTung.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChinhSach.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChinhSach.setAutoCreateRowSorter(false);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungChinhSach(), BorderLayout.NORTH);
        pnlNoiDung.add(taoBangChinhSach(), BorderLayout.CENTER);
        add(FormUI.createPage("CHÍNH SÁCH BẢO HÀNH",
                "Quản lý thời hạn, điều kiện và phụ tùng được áp dụng bảo hành", pnlNoiDung),
                BorderLayout.CENTER);

        controller = new ChinhSachBaoHanhController(this);
        controller.taiDuLieuBanDau();
    }

    private JPanel taoKhungChinhSach() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.add(FormUI.createSectionTitle("Thông tin chính sách"), BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(12, 0));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungThongTin(), BorderLayout.CENTER);
        pnlNoiDung.add(taoKhungPhuTung(), BorderLayout.EAST);
        pnlCard.add(pnlNoiDung, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel taoKhungThongTin() {
        JPanel pnlThongTin = new JPanel(new BorderLayout(0, 8));
        pnlThongTin.setOpaque(false);

        JPanel pnlTruong = FormUI.createFieldsPanel();
        FormUI.addField(pnlTruong, "Mã chính sách", txtMaCSBH, 0, 0);
        FormUI.addField(pnlTruong, "Tên chính sách *", txtTenChinhSach, 2, 0);
        JPanel pnlThoiHan = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlThoiHan.setOpaque(false);
        pnlThoiHan.add(spnThoiHan);
        pnlThoiHan.add(cboDonViThoiGian);
        FormUI.addField(pnlTruong, "Thời hạn *", pnlThoiHan, 0, 1);
        FormUI.addField(pnlTruong, "Trạng thái", chkTrangThai, 2, 1);
        pnlThongTin.add(pnlTruong, BorderLayout.NORTH);

        JPanel pnlMoTa = new JPanel(new GridLayout(1, 3, 8, 0));
        pnlMoTa.setOpaque(false);
        pnlMoTa.add(taoTruongMoTa("Nội dung bảo hành", txaNoiDungBaoHanh));
        pnlMoTa.add(taoTruongMoTa("Điều kiện bảo hành", txaDieuKien));
        pnlMoTa.add(taoTruongMoTa("Trường hợp từ chối", txaTruongHopTuChoi));
        pnlThongTin.add(pnlMoTa, BorderLayout.CENTER);

        JPanel pnlNut = FormUI.createButtonRow(btnThem, btnCapNhat, btnXoa, btnLamMoi);
        pnlThongTin.add(pnlNut, BorderLayout.SOUTH);
        return pnlThongTin;
    }

    private JPanel taoTruongMoTa(String nhan, JTextArea txaNoiDung) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 5));
        pnlTruong.setOpaque(false);
        pnlTruong.add(new AppLabel(nhan), BorderLayout.NORTH);
        pnlTruong.add(new AppScrollPane(txaNoiDung), BorderLayout.CENTER);
        return pnlTruong;
    }

    private JPanel taoKhungPhuTung() {
        JPanel pnlPhuTung = new JPanel(new BorderLayout(0, 8));
        pnlPhuTung.setOpaque(false);
        pnlPhuTung.setPreferredSize(new Dimension(410, 250));
        pnlPhuTung.add(FormUI.createSectionTitle("Phụ tùng áp dụng *"), BorderLayout.NORTH);

        JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 6));
        pnlDanhSach.setOpaque(false);
        txtTimPhuTung.setToolTipText("Lọc phụ tùng theo mã hoặc tên");
        pnlDanhSach.add(txtTimPhuTung, BorderLayout.NORTH);
        pnlDanhSach.add(new AppScrollPane(tblPhuTung), BorderLayout.CENTER);
        pnlPhuTung.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlPhuTung;
    }

    private JPanel taoBangChinhSach() {
        JPanel pnlBang = FormUI.createCard();
        JPanel pnlThanhTimKiem = new JPanel(new BorderLayout(10, 0));
        pnlThanhTimKiem.setOpaque(false);
        pnlThanhTimKiem.add(FormUI.createSectionTitle("Danh sách chính sách"), BorderLayout.WEST);
        txtTimChinhSach.setToolTipText("Tìm theo mã, tên, nội dung hoặc điều kiện bảo hành");
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlTimKiem.setOpaque(false);
        pnlTimKiem.add(txtTimChinhSach);
        pnlTimKiem.add(btnTimKiem);
        pnlThanhTimKiem.add(pnlTimKiem, BorderLayout.EAST);
        pnlBang.add(pnlThanhTimKiem, BorderLayout.NORTH);
        pnlBang.add(new AppScrollPane(tblChinhSach), BorderLayout.CENTER);
        return pnlBang;
    }

    public void hienThiMaChinhSach(String maCSBH) {
        txtMaCSBH.setText(maCSBH);
    }

    public void hienThiDanhSachPhuTung(List<PhuTung> dsPhuTung) {
        this.dsPhuTung.clear();
        this.dsPhuTung.addAll(dsPhuTung);
        locDanhSachPhuTung();
    }

    public void locDanhSachPhuTung() {
        String tuKhoa = txtTimPhuTung.getText().trim().toLowerCase(java.util.Locale.ROOT);
        dangNapBangPhuTung = true;
        modelPhuTung.setRowCount(0);
        for (PhuTung phuTung : dsPhuTung) {
            String ma = phuTung.getMaPhuTung();
            String ten = phuTung.getTenPhuTung();
            if (!tuKhoa.isEmpty()
                    && !ma.toLowerCase(java.util.Locale.ROOT).contains(tuKhoa)
                    && !ten.toLowerCase(java.util.Locale.ROOT).contains(tuKhoa)) {
                continue;
            }
            modelPhuTung.addRow(new Object[] {
                    dsMaPhuTungDangChon.contains(ma), ma, ten, phuTung.getSoLuongTon()
            });
        }
        dangNapBangPhuTung = false;
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
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtTenChinhSach.requestFocusInWindow();
            return null;
        }
        if (dsMaPhuTungDangChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một phụ tùng áp dụng.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
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

    public void capNhatPhuTungDuocChon(int row) {
        if (dangNapBangPhuTung || row >= modelPhuTung.getRowCount()) {
            return;
        }
        String maPhuTung = String.valueOf(modelPhuTung.getValueAt(row, 1));
        if (Boolean.TRUE.equals(modelPhuTung.getValueAt(row, 0))) {
            dsMaPhuTungDangChon.add(maPhuTung);
        } else {
            dsMaPhuTungDangChon.remove(maPhuTung);
        }
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

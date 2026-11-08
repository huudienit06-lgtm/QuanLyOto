package gui;

import controller.TiepNhanBaoHanhController;
import dao.PhieuYeuCauBaoHanhDAO.LuaChonHoaDon;
import dao.PhieuYeuCauBaoHanhDAO.LuaChonPhuTung;
import dao.PhieuYeuCauBaoHanhDAO.YeuCauHienThi;
import entity.HoaDon;
import entity.PhieuYeuCauBaoHanh;
import entity.PhuTung;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class FrmTiepNhanBaoHanh extends JPanel {
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AppTextField txtMaPhieuYCBH = new AppTextField(14);
    private final JComboBox<LuaChonHoaDon> cboHoaDon = new JComboBox<>();
    private final JComboBox<LuaChonPhuTung> cboPhuTung = new JComboBox<>();
    private final AppTextField txtKetQuaBaoHanh = new AppTextField(20);
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JTextArea txaLyDo = taoOThongTin();
    private final JTextArea txaGhiChuXuLy = taoOThongTin();
    private final AppTextField txtTimKiem = new AppTextField(20);
    private final JLabel lblThongBao = new AppLabel(
            "Yêu cầu chờ xử lý được ưu tiên hiển thị trước.", AppLabel.Style.MUTED);
    private final AppButton btnThem = new AppButton("Tiếp nhận");
    private final AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
    private final AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
    private final AppButton btnChapNhan = new AppButton("Chấp nhận bảo hành", AppButton.Style.SUCCESS);
    private final AppButton btnHoanThanh = new AppButton("Hoàn thành", AppButton.Style.PRIMARY);
    private final AppButton btnTuChoi = new AppButton("Từ chối", AppButton.Style.DANGER);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnTimKiem = new AppButton("Tìm kiếm");
    private final AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);

    private final DefaultTableModel modelYeuCau = new DefaultTableModel(
            new Object[] { "Mã yêu cầu", "Hóa đơn", "Phụ tùng", "Khách hàng", "SL", "Ngày tiếp nhận", "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblYeuCau = FormUI.createTable(modelYeuCau);
    private final TiepNhanBaoHanhController controller;
    private boolean baoHanhConHan;

    public FrmTiepNhanBaoHanh() {
        setLayout(new BorderLayout());
        setOpaque(false);
        txtMaPhieuYCBH.setEditable(false);
        txtKetQuaBaoHanh.setEditable(false);
        txtKetQuaBaoHanh.setToolTipText("Kết quả tính theo ngày lập hóa đơn và thời hạn bảo hành");
        tblYeuCau.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblYeuCau.setAutoCreateRowSorter(false);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungThongTin(), BorderLayout.NORTH);
        pnlNoiDung.add(taoBangYeuCau(), BorderLayout.CENTER);
        add(FormUI.createPage("TIẾP NHẬN BẢO HÀNH",
                "Tiếp nhận, xử lý và theo dõi các yêu cầu bảo hành phụ tùng", pnlNoiDung),
                BorderLayout.CENTER);

        controller = new TiepNhanBaoHanhController(this);
        controller.khoiTao();
    }

    private JPanel taoKhungThongTin() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.add(FormUI.createSectionTitle("Thông tin yêu cầu bảo hành"), BorderLayout.NORTH);
        JPanel pnlNoiDung = new JPanel(new BorderLayout(12, 0));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoCacTruong(), BorderLayout.CENTER);
        pnlNoiDung.add(taoGhiChu(), BorderLayout.EAST);
        pnlCard.add(pnlNoiDung, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel taoCacTruong() {
        JPanel pnlTruong = FormUI.createFieldsPanel();
        FormUI.addField(pnlTruong, "Mã yêu cầu", txtMaPhieuYCBH, 0, 0);
        FormUI.addField(pnlTruong, "Hóa đơn *", cboHoaDon, 2, 0);
        FormUI.addField(pnlTruong, "Phụ tùng *", cboPhuTung, 0, 1);
        FormUI.addField(pnlTruong, "Số lượng *", spnSoLuong, 2, 1);
        FormUI.addField(pnlTruong, "Lý do yêu cầu *", new AppScrollPane(txaLyDo), 0, 2);
        FormUI.addField(pnlTruong, "Kết quả bảo hành", txtKetQuaBaoHanh, 2, 2);
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 10));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(pnlTruong, BorderLayout.CENTER);
        pnlNoiDung.add(FormUI.createButtonRow(
                btnThem, btnCapNhat, btnXoa, btnChapNhan, btnHoanThanh, btnTuChoi, btnLamMoi),
                BorderLayout.SOUTH);
        return pnlNoiDung;
    }

    private JPanel taoGhiChu() {
        JPanel pnlGhiChu = new JPanel(new BorderLayout(0, 5));
        pnlGhiChu.setOpaque(false);
        pnlGhiChu.setPreferredSize(new Dimension(300, 180));
        pnlGhiChu.add(new AppLabel("Ghi chú xử lý"), BorderLayout.NORTH);
        pnlGhiChu.add(new AppScrollPane(txaGhiChuXuLy), BorderLayout.CENTER);
        return pnlGhiChu;
    }

    private JPanel taoBangYeuCau() {
        AppPanel pnlBang = FormUI.createCard();
        JPanel pnlDau = new JPanel(new BorderLayout(10, 0));
        pnlDau.setOpaque(false);
        pnlDau.add(FormUI.createSectionTitle("Danh sách yêu cầu bảo hành"), BorderLayout.WEST);
        txtTimKiem.setToolTipText("Tìm mã yêu cầu, hóa đơn, phụ tùng, khách hàng hoặc trạng thái");
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlTimKiem.setOpaque(false);
        pnlTimKiem.add(txtTimKiem);
        pnlTimKiem.add(btnTimKiem);
        pnlTimKiem.add(btnTaiLai);
        pnlDau.add(pnlTimKiem, BorderLayout.EAST);
        pnlBang.add(pnlDau, BorderLayout.NORTH);
        pnlBang.add(new AppScrollPane(tblYeuCau), BorderLayout.CENTER);
        pnlBang.add(lblThongBao, BorderLayout.SOUTH);
        return pnlBang;
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

    public AppButton getBtnThem() { return btnThem; }
    public AppButton getBtnCapNhat() { return btnCapNhat; }
    public AppButton getBtnXoa() { return btnXoa; }
    public AppButton getBtnChapNhan() { return btnChapNhan; }
    public AppButton getBtnHoanThanh() { return btnHoanThanh; }
    public AppButton getBtnTuChoi() { return btnTuChoi; }
    public AppButton getBtnLamMoi() { return btnLamMoi; }
    public AppButton getBtnTimKiem() { return btnTimKiem; }
    public AppButton getBtnTaiLai() { return btnTaiLai; }
    public JComboBox<LuaChonHoaDon> getCboHoaDon() { return cboHoaDon; }
    public JComboBox<LuaChonPhuTung> getCboPhuTung() { return cboPhuTung; }
    public AppTextField getTxtTimKiem() { return txtTimKiem; }
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

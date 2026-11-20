package gui;

import controller.DanhMucXeController;
import entity.DongXe;
import entity.HangXe;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class FrmHangXe extends JPanel {
    private final JTextField txtTenHangXe = new JTextField(18);
    private final JTextField txtHangXeCuaDong = new JTextField(18);
    private final JTextField txtTenDongXe = new JTextField(18);
    private final JTextField txtTimKiemHangXe = new JTextField(18);
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");

    private final JButton btnThemHangXe = new JButton("Thêm");
    private final JButton btnCapNhatHangXe = new JButton("Cập nhật");
    private final JButton btnXoaHangXe = new JButton("Xóa");
    private final JButton btnLamMoiHangXe = new JButton("Làm mới");
    private final JButton btnThemDongXe = new JButton("Thêm");
    private final JButton btnCapNhatDongXe = new JButton("Cập nhật");
    private final JButton btnXoaDongXe = new JButton("Xóa");
    private final JButton btnLamMoiDongXe = new JButton("Làm mới");

    private final DefaultTableModel modelHangXe = new DefaultTableModel(
            new Object[] {"Hãng xe"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel modelDongXe = new DefaultTableModel(
            new Object[] {"Dòng xe"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblHangXe = new JTable(modelHangXe);
    private final JTable tblDongXe = new JTable(modelDongXe);
    private final DanhMucXeController controller;
    private final Runnable sauKhiThayDoi;

    private String tenHangXeDangChon;
    private String tenDongXeDangChon;

    public FrmHangXe() {
        this(() -> {});
    }

    public FrmHangXe(Runnable sauKhiThayDoi) {
        this.sauKhiThayDoi = sauKhiThayDoi;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConfig.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));

        JLabel lblTieuDe = new JLabel("QUẢN LÝ HÃNG XE VÀ DÒNG XE");
        lblTieuDe.setFont(UIConfig.FONT_TITLE);
        lblTieuDe.setForeground(UIConfig.COLOR_TITLE);
        add(lblTieuDe, BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhuVucThongTin(), BorderLayout.NORTH);
        pnlNoiDung.add(taoKhuVucDanhSach(), BorderLayout.CENTER);
        add(pnlNoiDung, BorderLayout.CENTER);

        lblThongBao.setFont(UIConfig.FONT_REGULAR);
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        add(lblThongBao, BorderLayout.SOUTH);

        txtHangXeCuaDong.setEditable(false);
        cauHinhBang(tblHangXe);
        cauHinhBang(tblDongXe);
        btnCapNhatHangXe.setEnabled(false);
        btnXoaHangXe.setEnabled(false);
        btnCapNhatDongXe.setEnabled(false);
        btnXoaDongXe.setEnabled(false);
        controller = new DanhMucXeController(this);
        controller.khoiTao();
    }

    private JPanel taoKhuVucThongTin() {
        JPanel pnlThongTin = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlThongTin.setOpaque(false);
        pnlThongTin.add(taoTheThongTinHangXe());
        pnlThongTin.add(taoTheThongTinDongXe());
        return pnlThongTin;
    }

    private JPanel taoTheThongTinHangXe() {
        JPanel pnlThe = taoThe(new BorderLayout(0, 10));
        JLabel lblTieuDeHang = taoTieuDeMuc("Thông tin hãng xe");
        JPanel pnlTruong = new JPanel(new BorderLayout(8, 0));
        pnlTruong.setOpaque(false);
        pnlTruong.add(new JLabel("Tên hãng xe"), BorderLayout.WEST);
        pnlTruong.add(txtTenHangXe, BorderLayout.CENTER);

        JPanel pnlNut = taoHangNut();
        pnlNut.add(btnThemHangXe);
        pnlNut.add(btnCapNhatHangXe);
        pnlNut.add(btnXoaHangXe);
        pnlNut.add(btnLamMoiHangXe);
        pnlThe.add(lblTieuDeHang, BorderLayout.NORTH);
        pnlThe.add(pnlTruong, BorderLayout.CENTER);
        pnlThe.add(pnlNut, BorderLayout.SOUTH);
        return pnlThe;
    }

    private JPanel taoTheThongTinDongXe() {
        JPanel pnlThe = taoThe(new BorderLayout(0, 10));
        JLabel lblTieuDeDong = taoTieuDeMuc("Thông tin dòng xe");
        JPanel pnlTruong = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlTruong.setOpaque(false);
        JPanel pnlHang = new JPanel(new BorderLayout(8, 0));
        pnlHang.setOpaque(false);
        pnlHang.add(new JLabel("Hãng xe đang chọn"), BorderLayout.WEST);
        pnlHang.add(txtHangXeCuaDong, BorderLayout.CENTER);
        JPanel pnlDong = new JPanel(new BorderLayout(8, 0));
        pnlDong.setOpaque(false);
        pnlDong.add(new JLabel("Tên dòng xe"), BorderLayout.WEST);
        pnlDong.add(txtTenDongXe, BorderLayout.CENTER);
        pnlTruong.add(pnlHang);
        pnlTruong.add(pnlDong);

        JPanel pnlNut = taoHangNut();
        pnlNut.add(btnThemDongXe);
        pnlNut.add(btnCapNhatDongXe);
        pnlNut.add(btnXoaDongXe);
        pnlNut.add(btnLamMoiDongXe);
        pnlThe.add(lblTieuDeDong, BorderLayout.NORTH);
        pnlThe.add(pnlTruong, BorderLayout.CENTER);
        pnlThe.add(pnlNut, BorderLayout.SOUTH);
        return pnlThe;
    }

    private JPanel taoKhuVucDanhSach() {
        JPanel pnlDanhSach = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.add(taoTheBangHangXe());
        pnlDanhSach.add(taoTheBangDongXe());
        return pnlDanhSach;
    }

    private JPanel taoTheBangHangXe() {
        JPanel pnlThe = taoThe(new BorderLayout(0, 8));
        JPanel pnlDauBang = new JPanel(new BorderLayout(8, 0));
        pnlDauBang.setOpaque(false);
        pnlDauBang.add(taoTieuDeMuc("Danh sách hãng xe"), BorderLayout.WEST);
        txtTimKiemHangXe.setToolTipText("Nhập tên hãng xe cần tìm");
        pnlDauBang.add(txtTimKiemHangXe, BorderLayout.CENTER);
        pnlThe.add(pnlDauBang, BorderLayout.NORTH);
        pnlThe.add(taoBang(tblHangXe), BorderLayout.CENTER);
        return pnlThe;
    }

    private JPanel taoTheBangDongXe() {
        JPanel pnlThe = taoThe(new BorderLayout(0, 8));
        pnlThe.add(taoTieuDeMuc("Các dòng xe thuộc hãng đang chọn"), BorderLayout.NORTH);
        pnlThe.add(taoBang(tblDongXe), BorderLayout.CENTER);
        return pnlThe;
    }

    private JPanel taoThe(BorderLayout boCuc) {
        JPanel pnlThe = new JPanel(boCuc);
        pnlThe.setBackground(Color.WHITE);
        pnlThe.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return pnlThe;
    }

    private JPanel taoHangNut() {
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlNut.setOpaque(false);
        return pnlNut;
    }

    private JLabel taoTieuDeMuc(String noiDung) {
        JLabel lblTieuDeMuc = new JLabel(noiDung);
        lblTieuDeMuc.setFont(UIConfig.FONT_BOLD);
        lblTieuDeMuc.setForeground(UIConfig.COLOR_TEXT);
        return lblTieuDeMuc;
    }

    private JScrollPane taoBang(JTable tblDuLieu) {
        JScrollPane scrDuLieu = new JScrollPane(tblDuLieu);
        scrDuLieu.setBorder(new LineBorder(UIConfig.COLOR_BORDER, 1));
        scrDuLieu.getViewport().setBackground(Color.WHITE);
        return scrDuLieu;
    }

    private void cauHinhBang(JTable tblDuLieu) {
        tblDuLieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDuLieu.setRowHeight(28);
        tblDuLieu.setFont(UIConfig.FONT_REGULAR);
        tblDuLieu.setForeground(UIConfig.COLOR_TEXT);
        tblDuLieu.setGridColor(UIConfig.COLOR_TABLE_GRID);
        tblDuLieu.setSelectionBackground(UIConfig.COLOR_SELECTION);
        tblDuLieu.setSelectionForeground(UIConfig.COLOR_TEXT);
        tblDuLieu.setFillsViewportHeight(true);
        tblDuLieu.getTableHeader().setFont(UIConfig.FONT_BOLD);
    }

    public void hienThiDanhSachHangXe(List<HangXe> danhSachHangXe) {
        modelHangXe.setRowCount(0);
        for (HangXe hangXe : danhSachHangXe) {
            modelHangXe.addRow(new Object[] {hangXe.getTenHangXe()});
        }
    }

    public void hienThiDanhSachDongXe(List<DongXe> danhSachDongXe) {
        modelDongXe.setRowCount(0);
        for (DongXe dongXe : danhSachDongXe) {
            modelDongXe.addRow(new Object[] {dongXe.getTenDongXe()});
        }
    }

    public String layTenHangXeNhap() {
        return layTenHopLe(txtTenHangXe.getText(), "Tên hãng xe");
    }

    public String layTenDongXeNhap() {
        return layTenHopLe(txtTenDongXe.getText(), "Tên dòng xe");
    }

    private String layTenHopLe(String ten, String nhan) {
        String tenDaTrim = ten.trim();
        if (tenDaTrim.isEmpty() || tenDaTrim.length() > 50) {
            throw new IllegalArgumentException(nhan + " phải có từ 1 đến 50 ký tự.");
        }
        return tenDaTrim;
    }

    public String getTenHangXeDangChon() {
        return tenHangXeDangChon;
    }

    public String getTenDongXeDangChon() {
        return tenDongXeDangChon;
    }

    public String getTimKiemHangXe() {
        return txtTimKiemHangXe.getText().trim();
    }

    public JTable getTblHangXe() {
        return tblHangXe;
    }

    public JTable getTblDongXe() {
        return tblDongXe;
    }

    public JTextField getTxtTimKiemHangXe() {
        return txtTimKiemHangXe;
    }

    public JButton getBtnThemHangXe() {
        return btnThemHangXe;
    }

    public JButton getBtnCapNhatHangXe() {
        return btnCapNhatHangXe;
    }

    public JButton getBtnXoaHangXe() {
        return btnXoaHangXe;
    }

    public JButton getBtnLamMoiHangXe() {
        return btnLamMoiHangXe;
    }

    public JButton getBtnThemDongXe() {
        return btnThemDongXe;
    }

    public JButton getBtnCapNhatDongXe() {
        return btnCapNhatDongXe;
    }

    public JButton getBtnXoaDongXe() {
        return btnXoaDongXe;
    }

    public JButton getBtnLamMoiDongXe() {
        return btnLamMoiDongXe;
    }

    public void chonHangXe(String tenHangXe) {
        tenHangXeDangChon = tenHangXe;
        tenDongXeDangChon = null;
        modelDongXe.setRowCount(0);
        txtTenHangXe.setText(tenHangXe == null ? "" : tenHangXe);
        txtHangXeCuaDong.setText(tenHangXe == null ? "" : tenHangXe);
        txtTenDongXe.setText("");
        btnCapNhatHangXe.setEnabled(tenHangXe != null);
        btnXoaHangXe.setEnabled(tenHangXe != null);
        capNhatTrangThaiNutDongXe(tenHangXe != null, false);
    }

    public void chonDongXe(String tenDongXe) {
        tenDongXeDangChon = tenDongXe;
        txtTenDongXe.setText(tenDongXe == null ? "" : tenDongXe);
        capNhatTrangThaiNutDongXe(tenHangXeDangChon != null, tenDongXe != null);
    }

    public void lamMoiFormHangXe() {
        tblHangXe.clearSelection();
        tblDongXe.clearSelection();
        chonHangXe(null);
    }

    public void lamMoiFormDongXe() {
        tblDongXe.clearSelection();
        chonDongXe(null);
    }

    public void capNhatTrangThaiNutDongXe(boolean coHangXe, boolean coDongXe) {
        btnThemDongXe.setEnabled(coHangXe);
        btnCapNhatDongXe.setEnabled(coHangXe && coDongXe);
        btnXoaDongXe.setEnabled(coHangXe && coDongXe);
        btnLamMoiDongXe.setEnabled(coHangXe);
        txtTenDongXe.setEnabled(coHangXe);
    }

    public void hienThiThongBao(String noiDung) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(noiDung);
    }

    public void hienThiLoi(String noiDung) {
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        lblThongBao.setText(noiDung);
        JOptionPane.showMessageDialog(this, noiDung, "Quản lý hãng xe và dòng xe",
                JOptionPane.ERROR_MESSAGE);
    }

    public boolean xacNhanXoa(String noiDung) {
        return JOptionPane.showConfirmDialog(this, noiDung, "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void thongBaoDanhMucThayDoi() {
        sauKhiThayDoi.run();
    }

}

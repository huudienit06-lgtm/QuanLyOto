package gui;

import dao.HoaDonDAO;
import dao.HoaDonDAO.DongChiTiet;
import dao.HoaDonDAO.KhuyenMai;
import dao.HoaDonDAO.Phieu;
import dao.HoaDonDAO.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

public class FrmHoaDon extends JPanel {
    private static final String SUA_CHUA = "Hóa đơn sửa chữa";
    private static final String MUA_PHU_TUNG = "Hóa đơn mua phụ tùng";
    private static final String DICH_VU = "Dịch vụ";
    private static final String PHU_TUNG = "Phụ tùng";
    private static final String THE_TRONG = "trong";
    private static final String THE_SUA_CHUA = "suachua";
    private static final String THE_PHU_TUNG = "phutung";
    private static final BigDecimal VAT = new BigDecimal("0.08");
    private static final Color MAU_CHU = new Color(40, 50, 65);
    private static final Color MAU_PHU = new Color(100, 112, 130);
    private static final Color MAU_NEN_TRANG = new Color(244, 247, 251);
    private static final Color MAU_NEN_O = new Color(247, 249, 252);
    private static final Color MAU_NUT = new Color(37, 99, 235);
    private static final Color MAU_NUT_DAM = new Color(29, 78, 216);
    private static final Color MAU_VIEN = new Color(220, 225, 232);
    private static final Color MAU_DO = new Color(220, 53, 69);
    private static final Color MAU_XANH_LA = new Color(22, 130, 70);
    private static final Font FONT_THUONG = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_DAM = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_GIA_TRI = new Font("Segoe UI", Font.BOLD, 15);
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // 2. THÀNH PHẦN VÀ TRẠNG THÁI
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    // Thông tin hóa đơn
    private final JTextField txtMaHoaDon = new JTextField("Tự động");
    private final JTextField txtNgayLap = new JTextField(LocalDate.now().format(DINH_DANG_NGAY));
    private final JComboBox<String> cboLoaiHoaDon = new JComboBox<>(new String[] {
            "-- Chọn loại hóa đơn --", SUA_CHUA, MUA_PHU_TUNG });
    // Nhân viên đang đăng nhập và lập hóa đơn (dùng cho cả hai loại hóa đơn)
    private final JTextField txtMaNhanVienLapHoaDon = new JTextField();
    private final JTextField txtTenNhanVienLapHoaDon = new JTextField();
    // Phiếu sửa chữa
    private final JTextField txtMaPhieuSuaChua = new JTextField();
    private final JTextField txtMaKhachHang = new JTextField();
    private final JTextField txtTenKhachHang = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtBienSoXe = new JTextField();
    private final JTextField txtTenXe = new JTextField();
    /** Các ô tự điền từ phiếu (chỉ đọc). */
    private final JTextField[] oTuDien = { txtMaKhachHang, txtTenKhachHang, txtSoDienThoai, txtBienSoXe, txtTenXe };
    private final JButton btnTimPhieu = taoNutPhu("Tìm phiếu");
    // Phụ tùng và khuyến mãi
    private final JComboBox<PhuTung> cboPhuTung = new JComboBox<>();
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JTextField txtDonGia = new JTextField("0");
    private final JComboBox<KhuyenMai> cboKhuyenMai = new JComboBox<>();
    // Tổng kết
    private final JLabel lblTienDichVu = new JLabel("0 đ");
    private final JLabel lblTienPhuTung = new JLabel("0 đ");
    private final JLabel lblTienVAT = new JLabel("0 đ");
    private final JLabel lblTienGiam = new JLabel("0 đ");
    private final JLabel lblTongHoaDon = new JLabel("0 đ");
    private final JLabel lblTongThanhToan = new JLabel("0 đ");
    private final JLabel lblSoDong = new JLabel("0 dòng");
    private BigDecimal tienVAT = BigDecimal.ZERO;
    private BigDecimal tienGiam = BigDecimal.ZERO;
    private BigDecimal tongThanhToan = BigDecimal.ZERO;
    /** Mã hóa đơn vừa lưu thành công, dùng cho nút Thanh toán. */
    private String maHoaDonVuaLuu;
    private record DuLieuKhoiTao(String maHoaDon, List<PhuTung> dsPhuTung,
            List<KhuyenMai> dsKhuyenMai) {
    }

    // Khu vực "Nguồn hóa đơn"
    private final CardLayout cardNguon = new CardTheoThe();
    private final JPanel pnlNguon = new JPanel(cardNguon);
    private The theNguon;
    // Bảng chi tiết: Loại, Mã, Tên, Số lượng, Đơn giá, Thành tiền (2 cột cuối là
    // BigDecimal)
    private final DefaultTableModel modelChiTiet = new DefaultTableModel(
            new Object[] { "Loại", "Mã", "Tên dịch vụ / phụ tùng", "Số lượng", "Đơn giá", "Thành tiền" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblChiTiet = new JTable(modelChiTiet) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(246, 248, 252));
                c.setForeground(MAU_CHU);
            }
            return c;
        }
    };
    private final JButton btnThemHoaDon = taoNut("Thêm");

    // 3. KHỞI TẠO
    public void setNhanVienLapHoaDon(String maNV, String tenNV) {
        txtMaNhanVienLapHoaDon.setText(maNV == null ? "" : maNV.trim());
        txtTenNhanVienLapHoaDon.setText(tenNV == null ? "" : tenNV.trim());
    }

    public FrmHoaDon() {
        setLayout(new BorderLayout(0, 12));
        setBackground(MAU_NEN_TRANG);
        setBorder(new EmptyBorder(14, 18, 14, 18));
        kieuCacOComponent();
        cauHinhBang();
        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoVungCuon(), BorderLayout.CENTER);

        caiDatSuKien();
        capNhatLoaiHoaDon();
    }

    public void taiDuLieuBanDauAsync() {
        new SwingWorker<DuLieuKhoiTao, Void>() {
            @Override
            protected DuLieuKhoiTao doInBackground() throws SQLException {
                return new DuLieuKhoiTao(
                        hoaDonDAO.taoMaHoaDonMoi(),
                        hoaDonDAO.layDanhSachPhuTung(),
                        hoaDonDAO.layKhuyenMaiDangApDung());
            }

            @Override
            protected void done() {
                try {
                    DuLieuKhoiTao duLieu = get();
                    txtMaHoaDon.setText(duLieu.maHoaDon().isEmpty() ? "Tự động" : duLieu.maHoaDon());

                    cboPhuTung.removeAllItems();
                    cboPhuTung.addItem(new PhuTung("", "-- Chọn phụ tùng --", BigDecimal.ZERO));
                    duLieu.dsPhuTung().forEach(cboPhuTung::addItem);

                    cboKhuyenMai.removeAllItems();
                    cboKhuyenMai.addItem(new KhuyenMai("", "-- Không áp dụng khuyến mãi --",
                            BigDecimal.ZERO));
                    duLieu.dsKhuyenMai().forEach(cboKhuyenMai::addItem);
                    cboKhuyenMai.setSelectedIndex(0);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    baoLoi("Đã dừng tải dữ liệu hóa đơn", exception);
                } catch (ExecutionException exception) {
                    baoLoi("Không thể tải dữ liệu ban đầu của hóa đơn", exception);
                }
            }
        }.execute();
    }

    // 4. DỰNG GIAO DIỆN
    private JScrollPane taoVungCuon() {
        // AS_NEEDED + ẩn thanh cuộn: vẫn cuộn được bằng con lăn chuột
        JScrollPane sp = new JScrollPane(taoNoiDung(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        return sp;
    }

    private JPanel taoTieuDe() {
        JLabel lblTieuDe = new JLabel("LẬP HÓA ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTieuDe.setForeground(MAU_CHU);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnThemHoaDon);

        // Khối trống bên trái cùng bề rộng với cụm nút để tiêu đề nằm chính giữa
        JPanel pnlCanBang = new JPanel();
        pnlCanBang.setOpaque(false);
        pnlCanBang.setPreferredSize(pnlNut.getPreferredSize());

        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 4, 0));
        pnl.add(pnlCanBang, BorderLayout.WEST);
        pnl.add(lblTieuDe, BorderLayout.CENTER);
        pnl.add(pnlNut, BorderLayout.EAST);
        return pnl;
    }

    private JPanel taoNoiDung() {
        JPanel pnl = new TrangCuon();
        pnl.setLayout(new GridBagLayout());
        pnl.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridy = 0;
        g.weighty = 1;

        JPanel cotTrai = taoCotTrai();
        cotTrai.setPreferredSize(new Dimension(430, 10));
        cotTrai.setMinimumSize(new Dimension(380, 10));
        g.gridx = 0;
        g.weightx = 0.35;
        g.insets = new Insets(0, 0, 0, 14);
        pnl.add(cotTrai, g);

        JPanel cotPhai = taoCotPhai();
        cotPhai.setPreferredSize(new Dimension(760, 10));
        cotPhai.setMinimumSize(new Dimension(560, 10));
        g.gridx = 1;
        g.weightx = 0.65;
        g.insets = new Insets(0, 0, 0, 0);
        pnl.add(cotPhai, g);
        return pnl;
    }

    // ----- Cột trái -----

    private JPanel taoCotTrai() {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;

        g.gridy = 0;
        col.add(taoTheThongTinHoaDon(), g);

        g.gridy = 1;
        g.insets = new Insets(12, 0, 0, 0);
        col.add(taoTheNguon(), g);

        // Khối đệm đẩy hai thẻ lên trên
        g.gridy = 2;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 0, 0);
        JPanel dem = new JPanel();
        dem.setOpaque(false);
        col.add(dem, g);
        return col;
    }

    private The taoTheThongTinHoaDon() {
        The the = new The("THÔNG TIN HÓA ĐƠN");
        themDong(the.noiDung, 0, "Mã hóa đơn:", txtMaHoaDon);
        themDong(the.noiDung, 1, "Ngày lập:", txtNgayLap);
        themDong(the.noiDung, 2, "Mã NV lập hóa đơn:", txtMaNhanVienLapHoaDon);
        themDong(the.noiDung, 3, "Tên NV lập hóa đơn:", txtTenNhanVienLapHoaDon);
        themDong(the.noiDung, 4, "Loại hóa đơn:", cboLoaiHoaDon);
        return the;
    }

    private The taoTheNguon() {
        theNguon = new The("NGUỒN HÓA ĐƠN");
        theNguon.noiDung.setLayout(new BorderLayout());

        pnlNguon.setOpaque(false);
        pnlNguon.add(taoTheTrong(), THE_TRONG);
        pnlNguon.add(taoTheSuaChua(), THE_SUA_CHUA);
        pnlNguon.add(taoThePhuTung(), THE_PHU_TUNG);

        theNguon.noiDung.add(pnlNguon, BorderLayout.CENTER);
        return theNguon;
    }

    /** Hiển thị khi chưa chọn loại hóa đơn. */
    private JPanel taoTheTrong() {
        JLabel lbl = new JLabel(
                "<html><div style='text-align:center'>Chọn loại hóa đơn ở trên<br>"
                        + "để bắt đầu lập hóa đơn.</div></html>",
                SwingConstants.CENTER);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(300, 120));
        pnl.add(lbl, BorderLayout.CENTER);
        return pnl;
    }

    // Hóa đơn sửa chữa: nhập mã phiếu, hệ thống tự điền khách hàng, xe và nhân
    // viên.
    private JPanel taoTheSuaChua() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        JPanel dongMaPhieu = new JPanel(new BorderLayout(8, 0));
        dongMaPhieu.setOpaque(false);
        dongMaPhieu.add(txtMaPhieuSuaChua, BorderLayout.CENTER);
        dongMaPhieu.add(btnTimPhieu, BorderLayout.EAST);
        themDong(pnl, 0, "Mã phiếu:", dongMaPhieu);

        themTieuMuc(pnl, 1, "KHÁCH HÀNG VÀ XE");
        themDong(pnl, 2, "Mã khách hàng:", txtMaKhachHang);
        themDong(pnl, 3, "Tên khách hàng:", txtTenKhachHang);
        themDong(pnl, 4, "Số điện thoại:", txtSoDienThoai);
        themDong(pnl, 5, "Biển số xe:", txtBienSoXe);
        themDong(pnl, 6, "Tên xe:", txtTenXe);
        return pnl;
    }

    /** Hóa đơn mua phụ tùng: chọn phụ tùng, số lượng, đơn giá rồi thêm vào bảng. */
    private JPanel taoThePhuTung() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        themDong(pnl, 0, "Phụ tùng:", cboPhuTung);
        themDong(pnl, 1, "Số lượng:", spnSoLuong);
        themDong(pnl, 2, "Đơn giá:", txtDonGia);

        JButton btnThem = taoNut("Thêm vào hóa đơn");
        btnThem.addActionListener(e -> themPhuTung());

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 3;
        g.gridwidth = 2;
        g.weightx = 1;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(10, 0, 0, 0);
        pnl.add(btnThem, g);
        return pnl;
    }

    // Cột phải
    private JPanel taoCotPhai() {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.BOTH;

        // Chi tiết hóa đơn: chiếm hết chiều cao còn lại
        g.gridy = 0;
        g.weighty = 1;
        col.add(taoTheChiTiet(), g);

        // Tổng kết: chiều cao cố định
        g.gridy = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 0, 0, 0);
        col.add(taoTheTongKet(), g);
        return col;
    }

    private The taoTheChiTiet() {
        The the = new The("CHI TIẾT HÓA ĐƠN");
        the.themBenPhaiTieuDe(lblSoDong);
        the.noiDung.setLayout(new BorderLayout(0, 10));

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(BorderFactory.createLineBorder(MAU_VIEN));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(500, 240));
        scroll.setMinimumSize(new Dimension(300, 140));

        JButton btnLamMoi = taoNutPhu("Làm mới nhập liệu");
        JButton btnXoaDong = taoNutNguyHiem("Xóa dòng");
        btnLamMoi.addActionListener(e -> lamMoiNhapLieu());
        btnXoaDong.addActionListener(e -> xoaDongChiTiet());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnXoaDong);

        the.noiDung.add(scroll, BorderLayout.CENTER);
        the.noiDung.add(pnlNut, BorderLayout.SOUTH);
        return the;
    }

    private The taoTheTongKet() {
        The the = new The("TỔNG KẾT HÓA ĐƠN");
        the.noiDung.setLayout(new BorderLayout(0, 12));
        the.noiDung.setBorder(new EmptyBorder(6, 0, 12, 12));

        for (JLabel lbl : new JLabel[] {
                lblTienDichVu, lblTienPhuTung, lblTongHoaDon, lblTienVAT, lblTienGiam }) {
            lbl.setFont(FONT_GIA_TRI);
            lbl.setForeground(MAU_CHU);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            lbl.setPreferredSize(new Dimension(160, 28));
        }
        lblTienGiam.setForeground(MAU_DO);
        lblTongHoaDon.setForeground(MAU_NUT);

        lblTongThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTongThanhToan.setForeground(MAU_XANH_LA);
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongThanhToan.setPreferredSize(new Dimension(200, 34));

        // Nửa trái: chi phí và khuyến mãi
        JPanel pnlTrai = new JPanel(new GridBagLayout());
        pnlTrai.setOpaque(false);
        pnlTrai.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, MAU_VIEN),
                new EmptyBorder(0, 0, 0, 12)));
        themDongTien(pnlTrai, 0, "Tiền dịch vụ:", lblTienDichVu);
        themDongTien(pnlTrai, 1, "Tiền phụ tùng:", lblTienPhuTung);

        JLabel lblKM = new JLabel("Khuyến mãi:");
        lblKM.setFont(FONT_THUONG);
        lblKM.setForeground(MAU_PHU);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.gridy = 2;
        g.insets = new Insets(10, 12, 2, 0);
        pnlTrai.add(lblKM, g);
        g.gridy = 3;
        g.insets = new Insets(2, 12, 6, 0);
        pnlTrai.add(cboKhuyenMai, g);

        // Nửa phải: tổng thanh toán
        JPanel pnlPhai = new JPanel(new GridBagLayout());
        pnlPhai.setOpaque(false);
        themDongTien(pnlPhai, 0, "Tổng hóa đơn (đã gồm VAT):", lblTongHoaDon);
        themDongTien(pnlPhai, 1, "Tiền thuế (VAT 8%):", lblTienVAT);
        themDongTien(pnlPhai, 2, "Tiền giảm:", lblTienGiam);

        GridBagConstraints gs = new GridBagConstraints();
        gs.gridy = 3;
        gs.gridwidth = 2;
        gs.weightx = 1;
        gs.fill = GridBagConstraints.HORIZONTAL;
        gs.insets = new Insets(6, 12, 6, 0);
        pnlPhai.add(new JSeparator(), gs);

        JLabel lblNhanTong = new JLabel("Tổng thanh toán:");
        lblNhanTong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNhanTong.setForeground(MAU_CHU);

        GridBagConstraints gt = new GridBagConstraints();
        gt.gridy = 4;
        gt.gridx = 0;
        gt.weightx = 1;
        gt.anchor = GridBagConstraints.WEST;
        gt.insets = new Insets(4, 12, 4, 8);
        pnlPhai.add(lblNhanTong, gt);
        gt.gridx = 1;
        gt.weightx = 0;
        gt.anchor = GridBagConstraints.EAST;
        gt.insets = new Insets(4, 0, 4, 0);
        pnlPhai.add(lblTongThanhToan, gt);

        JPanel pnlHaiNua = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHaiNua.setOpaque(false);
        pnlHaiNua.add(pnlTrai);
        pnlHaiNua.add(pnlPhai);

        // ----- Nút thao tác hóa đơn -----
        JButton btnLamMoi = taoNutPhu("Làm mới");
        JButton btnLuu = taoNut("Lưu hóa đơn");
        JButton btnThanhToan = taoNut("Thanh toán");
        btnThanhToan.setBackground(MAU_XANH_LA);
        btnThanhToan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAU_XANH_LA, 1),
                new EmptyBorder(4, 14, 4, 14)));

        btnLamMoi.addActionListener(e -> {
            int chon = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn làm mới dữ liệu đang nhập không?",
                    "Xác nhận làm mới", JOptionPane.YES_NO_OPTION);
            if (chon == JOptionPane.YES_OPTION) {
                lamMoiToanBo();
            }
        });
        btnLuu.addActionListener(e -> luuHoaDon());
        btnThanhToan.addActionListener(e -> moFormThanhToan());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnLuu);
        pnlNut.add(btnThanhToan);

        the.noiDung.add(pnlHaiNua, BorderLayout.CENTER);
        the.noiDung.add(pnlNut, BorderLayout.SOUTH);
        return the;
    }

    // Thành phần form dùng chung
    /** Một dòng "nhãn + ô nhập", các nhãn luôn thẳng hàng. */
    private void themDong(JPanel pnl, int hang, String nhan, JComponent o) {
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);
        lbl.setPreferredSize(new Dimension(122, 32));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 0, 4, 10);
        pnl.add(lbl, g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        pnl.add(o, g);
    }

    /** Tiêu đề nhỏ chia nhóm trong form. */
    private void themTieuMuc(JPanel pnl, int hang, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(MAU_NUT_DAM);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                new EmptyBorder(0, 0, 4, 0)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(12, 0, 4, 0);
        pnl.add(lbl, g);
    }

    /** Một dòng "nhãn bên trái - số tiền bên phải" trong khu tổng kết. */
    private void themDongTien(JPanel pnl, int hang, String nhan, JLabel giaTri) {
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.insets = new Insets(5, 12, 5, 0);
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        pnl.add(lbl, g);

        g.gridx = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        pnl.add(giaTri, g);
    }

    // 5. KIỂU DÁNG
    private void kieuCacOComponent() {
        kieuO(txtMaHoaDon, true);
        kieuO(txtNgayLap, true);
        kieuO(txtMaNhanVienLapHoaDon, true);
        kieuO(txtTenNhanVienLapHoaDon, true);
        for (JTextField t : oTuDien) {
            kieuO(t, true);
        }
        kieuO(txtMaPhieuSuaChua, false);
        kieuO(txtDonGia, false);
        txtMaPhieuSuaChua.setToolTipText("Nhập mã phiếu sửa chữa rồi nhấn Enter");

        for (JComboBox<?> c : new JComboBox<?>[] { cboLoaiHoaDon, cboPhuTung, cboKhuyenMai }) {
            kieuCombo(c);
        }
        cboKhuyenMai.setMaximumRowCount(8);
        cboKhuyenMai.setToolTipText("Chọn chương trình khuyến mãi");

        spnSoLuong.setFont(FONT_THUONG);
        spnSoLuong.setPreferredSize(new Dimension(120, 32));
        lblSoDong.setFont(FONT_THUONG);
        lblSoDong.setForeground(MAU_PHU);
    }

    private void kieuO(JTextField txt, boolean chiDoc) {
        txt.setFont(FONT_THUONG);
        txt.setForeground(MAU_CHU);
        txt.setEditable(!chiDoc);
        txt.setBackground(chiDoc ? MAU_NEN_O : Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAU_VIEN),
                new EmptyBorder(4, 8, 4, 8)));
        txt.setPreferredSize(new Dimension(120, 32));
    }

    private void kieuCombo(JComboBox<?> cbo) {
        cbo.setFont(FONT_THUONG);
        cbo.setBackground(Color.WHITE);
        cbo.setPreferredSize(new Dimension(120, 32));
    }

    private void cauHinhBang() {
        tblChiTiet.setFont(FONT_THUONG);
        tblChiTiet.setRowHeight(30);
        tblChiTiet.setShowVerticalLines(false);
        tblChiTiet.setGridColor(MAU_VIEN);
        tblChiTiet.setIntercellSpacing(new Dimension(0, 1));
        tblChiTiet.setFillsViewportHeight(true);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChiTiet.setSelectionBackground(new Color(219, 234, 254));
        tblChiTiet.setSelectionForeground(MAU_CHU);
        tblChiTiet.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = tblChiTiet.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(FONT_DAM);
        header.setBackground(new Color(240, 245, 255));
        header.setForeground(MAU_CHU);
        header.setPreferredSize(new Dimension(0, 34));

        int[] rong = { 80, 90, 280, 70, 120, 130 };
        for (int i = 0; i < rong.length; i++) {
            tblChiTiet.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        // Chữ ở giữa, tên canh trái, số tiền canh phải (định dạng từ BigDecimal)
        DefaultTableCellRenderer giua = new DefaultTableCellRenderer();
        giua.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer trai = new DefaultTableCellRenderer();
        trai.setBorder(new EmptyBorder(0, 10, 0, 6));

        DefaultTableCellRenderer tien = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object v) {
                setText(v instanceof BigDecimal b ? dinhDangTien(b) : "");
            }
        };
        tien.setHorizontalAlignment(SwingConstants.RIGHT);
        tien.setBorder(new EmptyBorder(0, 6, 0, 12));

        TableCellRenderer[] renderer = { giua, giua, trai, giua, tien, tien };
        for (int i = 0; i < renderer.length; i++) {
            tblChiTiet.getColumnModel().getColumn(i).setCellRenderer(renderer[i]);
        }

        // Bảng thay đổi thì tự tính lại tổng
        modelChiTiet.addTableModelListener(e -> {
            lblSoDong.setText(modelChiTiet.getRowCount() + " dòng");
            tinhTongHoaDon();
        });
    }

    // 6. SỰ KIỆN
    private void caiDatSuKien() {
        cboLoaiHoaDon.addActionListener(e -> capNhatLoaiHoaDon());
        btnThemHoaDon.addActionListener(e -> lamMoiToanBo());
        btnTimPhieu.addActionListener(e -> hienThiCuaSoTimPhieu());

        // Nhập mã phiếu rồi nhấn Enter để tải thông tin
        txtMaPhieuSuaChua.addActionListener(e -> {
            String ma = txtMaPhieuSuaChua.getText().trim();
            if (!ma.isEmpty()) {
                napThongTinPhieu(ma);
            }
        });

        cboPhuTung.addActionListener(e -> capNhatDonGiaPhuTung());
        cboKhuyenMai.addActionListener(e -> tinhTongHoaDon());
    }

    private void capNhatLoaiHoaDon() {
        String loai = (String) cboLoaiHoaDon.getSelectedItem();

        if (SUA_CHUA.equals(loai)) {
            cardNguon.show(pnlNguon, THE_SUA_CHUA);
            theNguon.datTieuDe("PHIẾU SỬA CHỮA");
        } else if (MUA_PHU_TUNG.equals(loai)) {
            cardNguon.show(pnlNguon, THE_PHU_TUNG);
            theNguon.datTieuDe("CHỌN PHỤ TÙNG");
        } else {
            cardNguon.show(pnlNguon, THE_TRONG);
            theNguon.datTieuDe("NGUỒN HÓA ĐƠN");
        }

        for (JTextField t : oTuDien) {
            t.setText("");
        }
        modelChiTiet.setRowCount(0);
        lamMoiNhapLieu();

        pnlNguon.revalidate();
        revalidate();
        repaint();
    }

    private void capNhatDonGiaPhuTung() {
        PhuTung pt = (PhuTung) cboPhuTung.getSelectedItem();
        txtDonGia.setText(pt == null ? "0" : pt.giaBan().toPlainString());
    }

    // 7. TẢI DỮ LIỆU
    private void capNhatMaHoaDon() {
        try {
            String ma = hoaDonDAO.taoMaHoaDonMoi();
            if (!ma.isEmpty()) {
                txtMaHoaDon.setText(ma);
            }
        } catch (SQLException ex) {
            baoLoi("Không thể phát sinh mã hóa đơn", ex);
        }
    }

    /** Điền thông tin khách, xe, nhân viên và chi tiết từ phiếu sửa chữa. */
    private boolean napThongTinPhieu(String maPhieu) {
        try {
            Phieu p = hoaDonDAO.layPhieu(maPhieu);
            if (p == null) {
                canhBao("Không tìm thấy phiếu sửa chữa: " + maPhieu);
                return false;
            }
            txtMaPhieuSuaChua.setText(p.ma());
            txtMaKhachHang.setText(p.maKH());
            txtTenKhachHang.setText(p.tenKH());
            txtSoDienThoai.setText(p.soDT());
            txtBienSoXe.setText(p.bienSo());
            txtTenXe.setText(p.tenXe());

            modelChiTiet.setRowCount(0);
            List<DongChiTiet> ds = hoaDonDAO.layChiTietPhieu(maPhieu);
            ds.forEach(this::themDongChiTiet);
            if (ds.isEmpty()) {
                canhBao("Phiếu sửa chữa chưa có dịch vụ hoặc phụ tùng.");
            }
            return true;
        } catch (SQLException ex) {
            baoLoi("Không thể tải thông tin phiếu", ex);
            return false;
        }
    }

    // 8. NGHIỆP VỤ
    private void themDongChiTiet(DongChiTiet d) {
        modelChiTiet.addRow(new Object[] {
                d.dichVu() ? DICH_VU : PHU_TUNG,
                d.ma(), d.ten(), d.soLuong(), d.donGia(), d.thanhTien() });
    }

    private List<DongChiTiet> layChiTietTuBang() {
        List<DongChiTiet> ds = new ArrayList<>();
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            ds.add(new DongChiTiet(
                    DICH_VU.equals(modelChiTiet.getValueAt(i, 0)),
                    (String) modelChiTiet.getValueAt(i, 1),
                    (String) modelChiTiet.getValueAt(i, 2),
                    (Integer) modelChiTiet.getValueAt(i, 3),
                    (BigDecimal) modelChiTiet.getValueAt(i, 4)));
        }
        return ds;
    }

    private void themPhuTung() {
        PhuTung pt = (PhuTung) cboPhuTung.getSelectedItem();

        if (pt == null || pt.ma().isEmpty()) {
            canhBao("Vui lòng chọn phụ tùng.");
            return;
        }

        try {
            BigDecimal donGia = new BigDecimal(txtDonGia.getText().trim());

            if (donGia.signum() < 0) {
                throw new NumberFormatException();
            }

            int soLuongThem = (Integer) spnSoLuong.getValue();
            boolean daCo = false;

            // Tìm phụ tùng đã có trong bảng chi tiết
            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                String loai = modelChiTiet.getValueAt(i, 0).toString();
                String ma = modelChiTiet.getValueAt(i, 1).toString();

                // Chỉ cộng số lượng nếu cùng là phụ tùng và cùng mã
                if (PHU_TUNG.equals(loai) && pt.ma().equals(ma)) {
                    int soLuongCu = (Integer) modelChiTiet.getValueAt(i, 3);
                    BigDecimal donGiaCu = (BigDecimal) modelChiTiet.getValueAt(i, 4);

                    // Cộng số lượng vào dòng đã có
                    modelChiTiet.setValueAt(
                            soLuongCu + soLuongThem, i, 3);

                    // Giữ đơn giá cũ của phụ tùng
                    modelChiTiet.setValueAt(
                            donGiaCu.multiply(
                                    BigDecimal.valueOf(soLuongCu + soLuongThem)),
                            i, 5);
                    daCo = true;
                    break;
                }
            }

            // Chưa có trong bảng thì thêm dòng mới
            if (!daCo) {
                themDongChiTiet(new DongChiTiet(
                        false,
                        pt.ma(),
                        pt.ten(),
                        soLuongThem,
                        donGia));
            }

            lamMoiNhapLieu();

        } catch (NumberFormatException ex) {
            canhBao("Đơn giá phải là số hợp lệ và không âm.");
        }
    }

    private void xoaDongChiTiet() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            canhBao("Vui lòng chọn dòng cần xóa.");
            return;
        }
        modelChiTiet.removeRow(tblChiTiet.convertRowIndexToModel(row));
    }

    private void tinhTongHoaDon() {
        BigDecimal dichVu = BigDecimal.ZERO;
        BigDecimal phuTung = BigDecimal.ZERO;

        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            BigDecimal tt = (BigDecimal) modelChiTiet.getValueAt(i, 5);
            if (DICH_VU.equals(modelChiTiet.getValueAt(i, 0))) {
                dichVu = dichVu.add(tt);
            } else {
                phuTung = phuTung.add(tt);
            }
        }

        BigDecimal truocThue = dichVu.add(phuTung);
        tienVAT = truocThue.multiply(VAT).setScale(0, RoundingMode.HALF_UP);
        BigDecimal tamTinh = truocThue.add(tienVAT);

        KhuyenMai km = (KhuyenMai) cboKhuyenMai.getSelectedItem();
        BigDecimal tile = km == null ? BigDecimal.ZERO : km.tile();
        tienGiam = tamTinh.multiply(tile)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .min(tamTinh);
        tongThanhToan = tamTinh.subtract(tienGiam);

        lblTienDichVu.setText(dinhDangTien(dichVu));
        lblTienPhuTung.setText(dinhDangTien(phuTung));
        lblTienVAT.setText(dinhDangTien(tienVAT));
        lblTongHoaDon.setText(dinhDangTien(tamTinh));
        lblTienGiam.setText(dinhDangTien(tienGiam));
        lblTongThanhToan.setText(dinhDangTien(tongThanhToan));
    }

    private void luuHoaDon() {
        String loai = (String) cboLoaiHoaDon.getSelectedItem();
        String maHD = txtMaHoaDon.getText().trim();
        // Người lập hóa đơn lấy từ tài khoản đăng nhập, không lấy từ phiếu sửa chữa.
        String maNV = txtMaNhanVienLapHoaDon.getText().trim();
        String maPSC = txtMaPhieuSuaChua.getText().trim();
        boolean laSuaChua = SUA_CHUA.equals(loai);

        if (!laSuaChua && !MUA_PHU_TUNG.equals(loai)) {
            canhBao("Vui lòng chọn loại hóa đơn.");
            return;
        }
        if (maHD.isEmpty() || maHD.equalsIgnoreCase("Tự động")) {
            canhBao("Mã hóa đơn chưa được tạo.");
            capNhatMaHoaDon();
            return;
        }
        if (maNV.isEmpty()) {
            canhBao("Chưa có thông tin nhân viên đang đăng nhập. Vui lòng đăng nhập lại hoặc kiểm tra cấu hình tài khoản.");
            return;
        }
        if (laSuaChua && maPSC.isEmpty()) {
            canhBao("Vui lòng nhập mã phiếu sửa chữa.");
            return;
        }
        if (modelChiTiet.getRowCount() == 0) {
            canhBao("Vui lòng thêm chi tiết hóa đơn.");
            return;
        }

        LocalDate ngayLap;
        try {
            ngayLap = LocalDate.parse(txtNgayLap.getText().trim(), DINH_DANG_NGAY);
        } catch (Exception ex) {
            canhBao("Ngày lập hóa đơn không hợp lệ.");
            return;
        }

        KhuyenMai km = (KhuyenMai) cboKhuyenMai.getSelectedItem();
        String maKM = (km == null || km.ma().isEmpty()) ? null : km.ma();

        try {
            hoaDonDAO.luuHoaDon(maHD, laSuaChua ? maPSC : null, maNV, maKM, loai,
                    ngayLap, tienGiam, tienVAT, tongThanhToan, layChiTietTuBang());
            maHoaDonVuaLuu = maHD;
            JOptionPane.showMessageDialog(
                    this,
                    "Lưu hóa đơn thành công!\nMã hóa đơn: " + maHD,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            baoLoi("Không thể lưu hóa đơn", ex);
        }
    }

    private void moFormThanhToan() {
        if (maHoaDonVuaLuu == null) {
            canhBao("Vui lòng lưu hóa đơn trước khi thanh toán!");
            return;
        }
        FrmThanhToan frm = new FrmThanhToan(maHoaDonVuaLuu);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    /** Xóa phần nhập phiếu/phụ tùng, giữ nguyên bảng chi tiết. */
    private void lamMoiNhapLieu() {
        txtMaPhieuSuaChua.setText("");
        if (cboPhuTung.getItemCount() > 0) {
            cboPhuTung.setSelectedIndex(0);
        } else {
            txtDonGia.setText("0");
        }
        spnSoLuong.setValue(1);
    }

    /** Đưa form về trạng thái lập hóa đơn mới. */
    private void lamMoiToanBo() {
        if (cboKhuyenMai.getItemCount() > 0) {
            cboKhuyenMai.setSelectedIndex(0);
        }
        txtNgayLap.setText(LocalDate.now().format(DINH_DANG_NGAY));
        cboLoaiHoaDon.setSelectedIndex(0);
        capNhatLoaiHoaDon(); // xóa phiếu, bảng chi tiết, ô phụ tùng
        maHoaDonVuaLuu = null;
        capNhatMaHoaDon();
    }

    // CỬA SỔ TÌM PHIẾU SỬA CHỮA
    private void hienThiCuaSoTimPhieu() {
        JFrame winTimPhieu = new JFrame("Tìm phiếu sửa chữa");
        winTimPhieu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        winTimPhieu.setSize(1000, 480);
        winTimPhieu.setMinimumSize(new Dimension(800, 400));
        winTimPhieu.setLocationRelativeTo(this);
        winTimPhieu.setContentPane(new FrmTimPhieuSuaChua(maPhieu -> {
            if (napThongTinPhieu(maPhieu)) {
                winTimPhieu.dispose();
            }
        }));
        winTimPhieu.setVisible(true);
    }

    // 9. TIỆN ÍCH
    private String dinhDangTien(BigDecimal tien) {
        return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(tien) + " đ";
    }

    private void canhBao(Component cha, String msg) {
        JOptionPane.showMessageDialog(cha, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void canhBao(String msg) {
        canhBao(this, msg);
    }

    private void baoLoi(Component cha, String tieuDe, Exception ex) {
        JOptionPane.showMessageDialog(cha, tieuDe + ":\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void baoLoi(String tieuDe, Exception ex) {
        baoLoi(this, tieuDe, ex);
    }

    /** Nút chính: nền xanh, chữ trắng. */
    private JButton taoNut(String text) {
        return taoNutMau(text, MAU_NUT, MAU_NUT_DAM, Color.WHITE, MAU_NUT_DAM);
    }

    /** Nút nguy hiểm (xóa): nền đỏ, chữ trắng. */
    private JButton taoNutNguyHiem(String text) {
        Color dam = new Color(185, 28, 40);
        return taoNutMau(text, MAU_DO, dam, Color.WHITE, dam);
    }

    /** Nút phụ: nền trắng, viền xám, chữ tối. */
    private JButton taoNutPhu(String text) {
        return taoNutMau(text, Color.WHITE, new Color(241, 245, 249), MAU_CHU, new Color(180, 190, 205));
    }

    private JButton taoNutMau(String text, Color nen, Color nenKhiRe, Color chu, Color vien) {
        JButton btn = new JButton(text);

        // BasicButtonUI để Look and Feel không ghi đè màu nền
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFont(FONT_DAM);
        btn.setForeground(chu);
        btn.setBackground(nen);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(vien, 1),
                BorderFactory.createEmptyBorder(4, 14, 4, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Bề rộng theo chữ, tối thiểu 90px, cao đều 34px
        int rong = Math.max(90, btn.getFontMetrics(FONT_DAM).stringWidth(text) + 40);
        btn.setPreferredSize(new Dimension(rong, 34));
        btn.setMinimumSize(new Dimension(rong, 34));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(nenKhiRe);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(nen);
            }
        });
        return btn;
    }

    // 10. LỚP LỒNG HỖ TRỢ GIAO DIỆN
    /** Thẻ (card) có thanh tiêu đề, dùng cho mọi khu vực của màn hình. */
    private static class The extends JPanel {
        final JPanel noiDung = new JPanel(new GridBagLayout());
        private final JLabel lblTieuDe = new JLabel();
        private final JPanel pnlDau = new JPanel(new BorderLayout(8, 0));

        The(String tieuDe) {
            super(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(MAU_VIEN));

            lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTieuDe.setForeground(MAU_NUT_DAM);
            datTieuDe(tieuDe);

            pnlDau.setBackground(new Color(240, 245, 255));
            pnlDau.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                    new EmptyBorder(8, 12, 8, 12)));
            pnlDau.add(lblTieuDe, BorderLayout.WEST);

            noiDung.setBackground(Color.WHITE);
            noiDung.setBorder(new EmptyBorder(10, 12, 12, 12));

            add(pnlDau, BorderLayout.NORTH);
            add(noiDung, BorderLayout.CENTER);
        }

        void datTieuDe(String tieuDe) {
            lblTieuDe.setText(tieuDe);
        }

        void themBenPhaiTieuDe(JComponent c) {
            pnlDau.add(c, BorderLayout.EAST);
        }
    }

    /** CardLayout chỉ tính kích thước theo thẻ đang hiển thị. */
    private static class CardTheoThe extends CardLayout {
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets in = parent.getInsets();
                for (Component c : parent.getComponents()) {
                    if (c.isVisible()) {
                        Dimension d = c.getPreferredSize();
                        return new Dimension(d.width + in.left + in.right,
                                d.height + in.top + in.bottom);
                    }
                }
                return super.preferredLayoutSize(parent);
            }
        }
    }

    /** Panel cuộn: co giãn theo chiều ngang, tự giãn đầy khung khi cửa sổ cao. */
    private static class TrangCuon extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return 120;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            Container cha = getParent();
            return cha instanceof JViewport && cha.getHeight() > getPreferredSize().height;
        }
    }

    // 11. MAIN: CHẠY RIÊNG GIAO DIỆN
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BaseFrame frame = new BaseFrame();
            FrmHoaDon frmHoaDon = new FrmHoaDon();
            frame.setContent(frmHoaDon);
            frmHoaDon.taiDuLieuBanDauAsync();
            frame.setVisible(true);
        });
    }
}
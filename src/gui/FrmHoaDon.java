package gui;

import connectDB.ConnectDB;
import dao.HoaDonDAO;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Màn hình lập hóa đơn.
 *
 * Bố cục:
 * - Trên cùng: tiêu đề ở giữa, nút Thêm / Sửa / Xóa bên phải.
 * - Cột trái: thông tin hóa đơn + nguồn hóa đơn (phiếu sửa chữa hoặc chọn phụ
 * tùng).
 * - Cột phải: chi tiết hóa đơn (bảng co giãn theo chiều cao) + tổng kết và các
 * nút thao tác.
 */
public class FrmHoaDon extends JPanel {

    private static final String SUA_CHUA = "Hóa đơn sửa chữa";
    private static final String MUA_PHU_TUNG = "Hóa đơn mua phụ tùng";

    // Tên các thẻ trong CardLayout của khu vực "Nguồn hóa đơn"
    private static final String THE_TRONG = "trong";
    private static final String THE_SUA_CHUA = "suachua";
    private static final String THE_PHU_TUNG = "phutung";

    // ==================== MÀU SẮC VÀ FONT ====================

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

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    // Khung cuộn của cả trang (cuộn bằng con lăn chuột, ẩn thanh cuộn)
    private JScrollPane cuonTrang;

    // ==================== KHUYẾN MÃI ====================

    private static class KhuyenMai {
        private final String maKM;
        private final String tenKM;
        private final BigDecimal tileGiam;

        public KhuyenMai(String maKM, String tenKM, BigDecimal tileGiam) {
            this.maKM = maKM;
            this.tenKM = tenKM;
            this.tileGiam = tileGiam;
        }

        public String getMaKM() {
            return maKM;
        }

        public BigDecimal getTileGiam() {
            return tileGiam;
        }

        @Override
        public String toString() {
            if (maKM.isEmpty()) {
                return tenKM;
            }

            return tenKM + " (" + maKM + ") - "
                    + tileGiam.stripTrailingZeros().toPlainString() + "%";
        }
    }

    // ==================== THÔNG TIN HÓA ĐƠN ====================

    private final JTextField txtMaHoaDon = new JTextField("Tự động");
    private final JTextField txtNgayLap = new JTextField(
            LocalDate.now().format(DINH_DANG_NGAY));

    private final JComboBox<String> cboLoaiHoaDon = new JComboBox<>(new String[] {
            "-- Chọn loại hóa đơn --",
            SUA_CHUA,
            MUA_PHU_TUNG
    });

    // ==================== NHÂN VIÊN ====================

    private final JTextField txtMaNhanVien = new JTextField();
    private final JTextField txtTenNhanVien = new JTextField();

    // ==================== KHÁCH HÀNG VÀ XE ====================

    private final JTextField txtMaKhachHang = new JTextField();
    private final JTextField txtTenKhachHang = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtBienSoXe = new JTextField();
    private final JTextField txtTenXe = new JTextField();

    // ==================== PHIẾU SỬA CHỮA ====================

    private final JTextField txtMaPhieuSuaChua = new JTextField();

    // ==================== PHỤ TÙNG ====================

    private final JComboBox<String> cboPhuTung = new JComboBox<>(new String[] {
            "-- Chọn phụ tùng --"
    });

    private final JSpinner spnSoLuong = new JSpinner(
            new SpinnerNumberModel(1, 1, 10000, 1));

    private final JTextField txtDonGia = new JTextField("0");
    private final JComboBox<KhuyenMai> cboKhuyenMai = new JComboBox<>();

    // ==================== TỔNG TIỀN ====================

    private final JLabel lblTienDichVu = new JLabel("0 đ");
    private final JLabel lblTienPhuTung = new JLabel("0 đ");
    private final JLabel lblTienVAT = new JLabel("0 đ");
    private final JLabel lblTienGiam = new JLabel("0 đ");
    private final JLabel lblTongHoaDon = new JLabel("0 đ");
    private final JLabel lblTongThanhToan = new JLabel("0 đ");

    // ==================== KHU VỰC "NGUỒN HÓA ĐƠN" ====================

    private final CardLayout cardNguon = new CardTheoThe();
    private final JPanel pnlNguon = new JPanel(cardNguon);
    private The theNguon;

    private final JLabel lblSoDong = new JLabel("0 dòng");

    // ==================== BẢNG CHI TIẾT ====================

    private final DefaultTableModel modelChiTiet = new DefaultTableModel(
            new Object[] { "Loại", "Mã", "Tên dịch vụ / phụ tùng", "Số lượng", "Đơn giá", "Thành tiền" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // Bảng có kẻ sọc xen kẽ để dễ đọc
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

    // ==================== NÚT QUẢN LÝ HÓA ĐƠN ====================

    private final JButton btnThemHoaDon = taoNut("Thêm");
    private final JButton btnSuaHoaDon = taoNutPhu("Sửa");
    private final JButton btnXoaHoaDon = taoNutNguyHiem("Xóa");
    private final JButton btnTimPhieu = taoNutPhu("Tìm phiếu");

    // =========================================================
    // KHỞI TẠO
    // =========================================================

    public FrmHoaDon() {
        // Kết nối SQL Server trước
        ConnectDB.getInstance().connect();

        setLayout(new BorderLayout(0, 12));
        setBackground(MAU_NEN_TRANG);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        kieuCacOComponent();
        cauHinhBang();

        add(taoTieuDe(), BorderLayout.NORTH);

        // Cuộn dọc khi cửa sổ thấp; khi cửa sổ cao thì nội dung tự giãn đầy khung
        cuonTrang = new JScrollPane(
                taoNoiDung(),
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        cuonTrang.setBorder(null);
        cuonTrang.setOpaque(false);
        cuonTrang.getViewport().setOpaque(false);

        // Ẩn thanh cuộn thì Swing không tự cuộn bằng con lăn -> tự xử lý
        cuonTrang.setWheelScrollingEnabled(false);
        cuonTrang.addMouseWheelListener(e -> {
            JViewport vp = cuonTrang.getViewport();
            Component view = vp.getView();

            if (view == null) {
                return;
            }

            int toiDa = Math.max(0, view.getHeight() - vp.getHeight());
            Point p = vp.getViewPosition();

            int y = p.y + e.getUnitsToScroll() * 24;
            y = Math.max(0, Math.min(toiDa, y));

            vp.setViewPosition(new Point(p.x, y));
        });

        add(cuonTrang, BorderLayout.CENTER);

        caiDatSuKien();
        capNhatLoaiHoaDon();
        taiDanhSachPhuTung();
        taiDanhSachKhuyenMai();
        capNhatMaHoaDon();
    }

    /** Khi khung cuộn con không tự cuộn được thì chuyển con lăn ra trang chính. */
    private void chuyenConLanRaNgoai(JScrollPane trong) {
        trong.addMouseWheelListener(e -> {
            if (cuonTrang != null && !trong.getVerticalScrollBar().isVisible()) {
                cuonTrang.dispatchEvent(
                        SwingUtilities.convertMouseEvent(trong, e, cuonTrang));
            }
        });
    }

    // =========================================================
    // CÁC LỚP HỖ TRỢ GIAO DIỆN
    // =========================================================

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

    /**
     * CardLayout chỉ lấy chiều cao của thẻ đang hiển thị (mặc định lấy thẻ cao
     * nhất).
     */
    private static class CardTheoThe extends CardLayout {
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets in = parent.getInsets();

                for (Component c : parent.getComponents()) {
                    if (c.isVisible()) {
                        Dimension d = c.getPreferredSize();
                        return new Dimension(
                                d.width + in.left + in.right,
                                d.height + in.top + in.bottom);
                    }
                }

                return super.preferredLayoutSize(parent);
            }
        }
    }

    /**
     * Cột có bề rộng ưa thích cố định, chiều cao tính theo nội dung thật
     * (để trang biết đủ cao mà cuộn, các thẻ không bị ép đè lên nhau).
     */
    private static class CotCoDinhRong extends JPanel {
        private final int rong;
        private final int rongToiThieu;

        CotCoDinhRong(LayoutManager layout, int rong, int rongToiThieu) {
            super(layout);
            this.rong = rong;
            this.rongToiThieu = rongToiThieu;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(rong, super.getPreferredSize().height);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(rongToiThieu, getPreferredSize().height);
        }
    }

    /**
     * Trang nội dung: luôn vừa chiều ngang, giãn theo chiều dọc nếu còn chỗ trống.
     */
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
            return cha instanceof JViewport
                    && cha.getHeight() > getPreferredSize().height;
        }
    }

    // =========================================================
    // KIỂU DÁNG CHUNG
    // =========================================================

    private void kieuCacOComponent() {
        // Chỉ đọc: thông tin tự điền
        kieuO(txtMaHoaDon, true);
        kieuO(txtNgayLap, true);
        kieuO(txtMaKhachHang, true);
        kieuO(txtTenKhachHang, true);
        kieuO(txtSoDienThoai, true);
        kieuO(txtBienSoXe, true);
        kieuO(txtTenXe, true);
        kieuO(txtMaNhanVien, true);
        kieuO(txtTenNhanVien, true);

        // Có thể nhập
        kieuO(txtMaPhieuSuaChua, false);
        kieuO(txtDonGia, false);
        txtMaPhieuSuaChua.setToolTipText("Nhập mã phiếu sửa chữa rồi nhấn Enter");

        kieuCombo(cboLoaiHoaDon);
        kieuCombo(cboPhuTung);
        kieuCombo(cboKhuyenMai);
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

        // Độ rộng cột: Loại, Mã, Tên, Số lượng, Đơn giá, Thành tiền
        int[] rong = { 80, 90, 280, 70, 120, 130 };
        for (int i = 0; i < rong.length; i++) {
            tblChiTiet.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        // Canh lề: chữ ở giữa, số tiền canh phải
        DefaultTableCellRenderer giua = new DefaultTableCellRenderer();
        giua.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer phai = new DefaultTableCellRenderer();
        phai.setHorizontalAlignment(SwingConstants.RIGHT);
        phai.setBorder(new EmptyBorder(0, 6, 0, 12));

        DefaultTableCellRenderer trai = new DefaultTableCellRenderer();
        trai.setBorder(new EmptyBorder(0, 10, 0, 6));

        tblChiTiet.getColumnModel().getColumn(0).setCellRenderer(giua);
        tblChiTiet.getColumnModel().getColumn(1).setCellRenderer(giua);
        tblChiTiet.getColumnModel().getColumn(2).setCellRenderer(trai);
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(giua);
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(phai);
        tblChiTiet.getColumnModel().getColumn(5).setCellRenderer(phai);

        // Cập nhật số dòng ở tiêu đề thẻ
        modelChiTiet.addTableModelListener(
                e -> lblSoDong.setText(modelChiTiet.getRowCount() + " dòng"));
    }

    /** Thêm một dòng "nhãn + ô nhập" vào form, các nhãn luôn thẳng hàng. */
    private void themDong(JPanel pnl, int hang, String nhan, JComponent o) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(4, 0, 4, 10);

        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);
        lbl.setPreferredSize(new Dimension(122, 32));
        pnl.add(lbl, g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        pnl.add(o, g);
    }

    /** Tiêu đề nhỏ chia nhóm trong form. */
    private void themTieuMuc(JPanel pnl, int hang, String text) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(12, 0, 4, 0);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(MAU_NUT_DAM);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                new EmptyBorder(0, 0, 4, 0)));
        pnl.add(lbl, g);
    }

    // =========================================================
    // TIÊU ĐỀ
    // =========================================================

    private JPanel taoTieuDe() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel lblTieuDe = new JLabel("LẬP HÓA ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTieuDe.setForeground(MAU_CHU);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnThemHoaDon);
        pnlNut.add(btnSuaHoaDon);
        pnlNut.add(btnXoaHoaDon);

        // Khối trống bên trái có cùng bề rộng với cụm nút để tiêu đề nằm chính giữa
        JPanel pnlCanBang = new JPanel();
        pnlCanBang.setOpaque(false);
        pnlCanBang.setPreferredSize(pnlNut.getPreferredSize());

        pnl.add(pnlCanBang, BorderLayout.WEST);
        pnl.add(lblTieuDe, BorderLayout.CENTER);
        pnl.add(pnlNut, BorderLayout.EAST);

        return pnl;
    }

    // =========================================================
    // NỘI DUNG: HAI CỘT
    // =========================================================

    private JPanel taoNoiDung() {
        JPanel pnl = new TrangCuon();
        pnl.setLayout(new GridBagLayout());
        pnl.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridy = 0;
        g.weighty = 1;

        // Kích thước cột do CotCoDinhRong tự tính (bề rộng cố định, cao theo nội dung)
        JPanel cotTrai = taoCotTrai();
        g.gridx = 0;
        g.weightx = 0.35;
        g.insets = new Insets(0, 0, 0, 14);
        pnl.add(cotTrai, g);

        JPanel cotPhai = taoCotPhai();
        g.gridx = 1;
        g.weightx = 0.65;
        g.insets = new Insets(0, 0, 0, 0);
        pnl.add(cotPhai, g);

        return pnl;
    }

    // ---------------------------------------------------------
    // CỘT TRÁI
    // ---------------------------------------------------------

    private JPanel taoCotTrai() {
        JPanel col = new CotCoDinhRong(new GridBagLayout(), 430, 380);
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
        themDong(the.noiDung, 2, "Loại hóa đơn:", cboLoaiHoaDon);

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
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(300, 120));

        JLabel lbl = new JLabel(
                "<html><div style='text-align:center'>Chọn loại hóa đơn ở trên<br>"
                        + "để bắt đầu lập hóa đơn.</div></html>",
                SwingConstants.CENTER);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);

        pnl.add(lbl, BorderLayout.CENTER);
        return pnl;
    }

    /**
     * Hóa đơn sửa chữa: nhập mã phiếu, hệ thống tự điền khách hàng, xe và nhân
     * viên.
     */
    private JPanel taoTheSuaChua() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        // Dòng mã phiếu: ô nhập + nút tìm
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

        themTieuMuc(pnl, 7, "NHÂN VIÊN PHỤ TRÁCH");
        themDong(pnl, 8, "Mã nhân viên:", txtMaNhanVien);
        themDong(pnl, 9, "Tên nhân viên:", txtTenNhanVien);

        return pnl;
    }

    /** Hóa đơn mua phụ tùng: chọn phụ tùng, số lượng, đơn giá rồi thêm vào bảng. */
    private JPanel taoThePhuTung() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        themDong(pnl, 0, "Phụ tùng:", cboPhuTung);
        themDong(pnl, 1, "Số lượng:", spnSoLuong);
        themDong(pnl, 2, "Đơn giá:", txtDonGia);

        JButton btnThemPhuTung = taoNut("Thêm vào hóa đơn");
        btnThemPhuTung.addActionListener(e -> themPhuTung());

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        g.weightx = 1;
        g.anchor = GridBagConstraints.EAST;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(10, 0, 0, 0);
        pnl.add(btnThemPhuTung, g);

        return pnl;
    }

    // ---------------------------------------------------------
    // CỘT PHẢI
    // ---------------------------------------------------------

    private JPanel taoCotPhai() {
        JPanel col = new CotCoDinhRong(new GridBagLayout(), 760, 560);
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

        // Bảng không cần cuộn riêng thì chuyển con lăn ra cuộn cả trang
        chuyenConLanRaNgoai(scroll);

        JButton btnLamMoi = taoNutPhu("Làm mới nhập liệu");
        JButton btnXoaChiTiet = taoNutNguyHiem("Xóa dòng");

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnXoaChiTiet);

        btnXoaChiTiet.addActionListener(e -> xoaDongChiTiet());
        btnLamMoi.addActionListener(e -> lamMoiNhapLieu());

        the.noiDung.add(scroll, BorderLayout.CENTER);
        the.noiDung.add(pnlNut, BorderLayout.SOUTH);

        return the;
    }

    private The taoTheTongKet() {
        The the = new The("TỔNG KẾT HÓA ĐƠN");
        the.noiDung.setLayout(new BorderLayout(0, 12));
        the.noiDung.setBorder(new EmptyBorder(6, 0, 12, 12));

        // Giá trị tiền: canh phải, cùng độ rộng
        JLabel[] cacGiaTri = {
                lblTienDichVu, lblTienPhuTung, lblTongHoaDon, lblTienVAT, lblTienGiam
        };
        for (JLabel lbl : cacGiaTri) {
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

        // ----- Nửa trái: các khoản chi phí và khuyến mãi -----
        JPanel pnlTrai = new JPanel(new GridBagLayout());
        pnlTrai.setOpaque(false);
        pnlTrai.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, MAU_VIEN),
                new EmptyBorder(0, 0, 0, 12)));

        themDongTien(pnlTrai, 0, "Tiền dịch vụ:", lblTienDichVu);
        themDongTien(pnlTrai, 1, "Tiền phụ tùng:", lblTienPhuTung);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(10, 12, 2, 0);
        JLabel lblKM = new JLabel("Khuyến mãi:");
        lblKM.setFont(FONT_THUONG);
        lblKM.setForeground(MAU_PHU);
        pnlTrai.add(lblKM, g);

        g.gridy = 3;
        g.insets = new Insets(2, 12, 6, 0);
        pnlTrai.add(cboKhuyenMai, g);

        // ----- Nửa phải: tổng kết thanh toán -----
        JPanel pnlPhai = new JPanel(new GridBagLayout());
        pnlPhai.setOpaque(false);

        themDongTien(pnlPhai, 0, "Tổng hóa đơn (đã gồm VAT):", lblTongHoaDon);
        themDongTien(pnlPhai, 1, "Tiền thuế (VAT 8%):", lblTienVAT);
        themDongTien(pnlPhai, 2, "Tiền giảm:", lblTienGiam);

        GridBagConstraints gs = new GridBagConstraints();
        gs.gridx = 0;
        gs.gridy = 3;
        gs.gridwidth = 2;
        gs.weightx = 1;
        gs.fill = GridBagConstraints.HORIZONTAL;
        gs.insets = new Insets(6, 12, 6, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(MAU_VIEN);
        pnlPhai.add(sep, gs);

        GridBagConstraints gt = new GridBagConstraints();
        gt.gridy = 4;
        gt.gridx = 0;
        gt.weightx = 1;
        gt.anchor = GridBagConstraints.WEST;
        gt.insets = new Insets(4, 12, 4, 8);
        JLabel lblNhanTong = new JLabel("Tổng thanh toán:");
        lblNhanTong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNhanTong.setForeground(MAU_CHU);
        pnlPhai.add(lblNhanTong, gt);

        gt.gridx = 1;
        gt.weightx = 0;
        gt.anchor = GridBagConstraints.EAST;
        gt.insets = new Insets(4, 0, 4, 0);
        pnlPhai.add(lblTongThanhToan, gt);

        // Hai nửa có bề rộng bằng nhau
        JPanel pnlHaiNua = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHaiNua.setOpaque(false);
        pnlHaiNua.add(pnlTrai);
        pnlHaiNua.add(pnlPhai);

        // ----- Các nút thao tác hóa đơn -----
        JButton btnLamMoi = taoNutPhu("Làm mới");
        JButton btnLuu = taoNut("Lưu hóa đơn");
        JButton btnThanhToan = taoNut("Thanh toán");

        // Màu xanh lá để phân biệt thao tác thanh toán
        btnThanhToan.setBackground(MAU_XANH_LA);
        btnThanhToan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAU_XANH_LA, 1),
                new EmptyBorder(4, 14, 4, 14)));

        JPanel pnlNut = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);

        pnlNut.add(btnLamMoi);
        pnlNut.add(btnLuu);
        pnlNut.add(btnThanhToan);

        // Làm mới để lập hóa đơn mới
        btnLamMoi.addActionListener(e -> {
            int luaChon = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có muốn làm mới dữ liệu đang nhập không?",
                    "Xác nhận làm mới",
                    JOptionPane.YES_NO_OPTION);

            if (luaChon == JOptionPane.YES_OPTION) {
                lamMoiToanBo();
                capNhatMaHoaDon();
            }
        });

        // Lưu hóa đơn
        btnLuu.addActionListener(e -> luuHoaDon());

        btnThanhToan.addActionListener(e -> moFormThanhToan());
        the.noiDung.add(pnlHaiNua, BorderLayout.CENTER);
        the.noiDung.add(pnlNut, BorderLayout.SOUTH);

        return the;
    }

    private void moFormThanhToan() {
        // Thay txtMaHoaDon bằng tên ô mã hóa đơn thực tế
        String maHD = txtMaHoaDon.getText().trim();

        if (maHD.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng lưu hóa đơn trước khi thanh toán!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        FrmThanhToan frm = new FrmThanhToan(maHD);
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);
    }

    /** Một dòng "nhãn bên trái - số tiền bên phải" trong khu tổng kết. */
    private void themDongTien(JPanel pnl, int hang, String nhan, JLabel giaTri) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.insets = new Insets(5, 12, 5, 0);

        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);
        pnl.add(lbl, g);

        g.gridx = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        pnl.add(giaTri, g);
    }

    // =========================================================
    // MÃ HÓA ĐƠN
    // =========================================================

    private void capNhatMaHoaDon() {
        String maMoi = hoaDonDAO.taoMaHoaDonMoi();

        if (!maMoi.isEmpty()) {
            txtMaHoaDon.setText(maMoi);
        }
    }

    // =========================================================
    // TẢI DỮ LIỆU
    // =========================================================

    private void taiDanhSachPhuTung() {
        String sql = "SELECT maPhuTung, tenPhuTung, giaBan " +
                "FROM PhuTung " +
                "ORDER BY maPhuTung";

        cboPhuTung.removeAllItems();
        cboPhuTung.addItem("-- Chọn phụ tùng --");

        try {
            Connection conn = ConnectDB.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String ma = rs.getString("maPhuTung");
                    String ten = rs.getString("tenPhuTung");

                    // Chỉ hiển thị mã và tên phụ tùng
                    cboPhuTung.addItem(ma + " - " + ten);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh sách phụ tùng:\n" + ex.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taiDanhSachKhuyenMai() {
        cboKhuyenMai.removeAllItems();

        // Lựa chọn mặc định
        cboKhuyenMai.addItem(
                new KhuyenMai(
                        "",
                        "-- Không áp dụng khuyến mãi --",
                        BigDecimal.ZERO));

        String sql = """
                SELECT maKM, tenKM, tileGiam
                FROM ChuongTrinhKhuyenMai
                WHERE trangThai = N'Đang áp dụng'
                  AND ngayBatDau <= CAST(GETDATE() AS DATE)
                  AND ngayKetThuc >= CAST(GETDATE() AS DATE)
                  AND tileGiam BETWEEN 0 AND 100
                ORDER BY maKM
                """;

        try {
            Connection conn = ConnectDB.getConnection();

            if (conn == null || conn.isClosed()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể kết nối SQL Server.",
                        "Lỗi kết nối",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String maKM = rs.getString("maKM");
                    String tenKM = rs.getString("tenKM");
                    BigDecimal tileGiam = rs.getBigDecimal("tileGiam");

                    if (tileGiam == null) {
                        continue;
                    }

                    cboKhuyenMai.addItem(
                            new KhuyenMai(maKM, tenKM, tileGiam));
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh sách khuyến mãi:\n"
                            + ex.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE);
        }

        cboKhuyenMai.setSelectedIndex(0);
    }

    private boolean napThongTinPhieu(String maPhieu) {
        String sql = "SELECT p.maPhieuSuaChua, p.maKH, kh.hoTen, kh.soDT, "
                + "p.maXe, x.bienSo, x.hangXe, x.dongXe, "
                + "p.maNV, nv.hoTen AS tenNhanVien "
                + "FROM PhieuSuaChua p "
                + "JOIN KhachHang kh ON p.maKH = kh.maKH "
                + "JOIN Xe x ON p.maXe = x.maXe "
                + "JOIN NhanVien nv ON p.maNV = nv.maNV "
                + "WHERE p.maPhieuSuaChua = ?";

        Connection conn = ConnectDB.getConnection();

        if (conn == null) {
            JOptionPane.showMessageDialog(
                    this, "Kết nối SQL Server đang null!");
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieu);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Không tìm thấy phiếu sửa chữa có mã: " + maPhieu);
                    return false;
                }

                txtMaPhieuSuaChua.setText(rs.getString("maPhieuSuaChua"));
                txtMaKhachHang.setText(rs.getString("maKH"));
                txtTenKhachHang.setText(rs.getString("hoTen"));
                txtSoDienThoai.setText(rs.getString("soDT"));
                txtBienSoXe.setText(rs.getString("bienSo"));

                txtTenXe.setText(
                        (rs.getString("hangXe") == null ? "" : rs.getString("hangXe"))
                                + " "
                                + (rs.getString("dongXe") == null ? "" : rs.getString("dongXe")));

                txtMaNhanVien.setText(rs.getString("maNV"));
                txtTenNhanVien.setText(rs.getString("tenNhanVien"));
            }

            napChiTietPhieu(maPhieu);
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải thông tin phiếu:\n" + ex.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void napChiTietPhieu(String maPhieu) {
        String sql = "SELECT ct.maDV, dv.tenDV, dv.giaDV, " +
                "ct.maPhuTung, pt.tenPhuTung, " +
                "ct.soLuong, ct.donGia, ct.tienCong " +
                "FROM ChiTietPhieuSuaChua ct " +
                "JOIN DichVu dv ON ct.maDV = dv.maDV " +
                "JOIN PhuTung pt ON ct.maPhuTung = pt.maPhuTung " +
                "WHERE ct.maPhieuSuaChua = ?";

        modelChiTiet.setRowCount(0);

        try {
            Connection conn = ConnectDB.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maPhieu);

                try (ResultSet rs = ps.executeQuery()) {
                    // Lưu riêng các dòng dịch vụ và phụ tùng
                    java.util.List<Object[]> danhSachDichVu = new java.util.ArrayList<>();
                    java.util.List<Object[]> danhSachPhuTung = new java.util.ArrayList<>();

                    while (rs.next()) {
                        int soLuong = rs.getInt("soLuong");

                        BigDecimal giaPhuTung = rs.getBigDecimal("donGia");
                        BigDecimal giaDichVu = rs.getBigDecimal("giaDV");
                        BigDecimal tienCong = rs.getBigDecimal("tienCong");

                        if (giaPhuTung == null) {
                            giaPhuTung = BigDecimal.ZERO;
                        }
                        if (giaDichVu == null) {
                            giaDichVu = BigDecimal.ZERO;
                        }
                        if (tienCong == null) {
                            tienCong = BigDecimal.ZERO;
                        }

                        // Tạo dòng dịch vụ
                        BigDecimal thanhTienDV = giaDichVu
                                .multiply(BigDecimal.valueOf(soLuong))
                                .add(tienCong);

                        danhSachDichVu.add(new Object[] {
                                "Dịch vụ",
                                rs.getString("maDV"),
                                rs.getString("tenDV"),
                                soLuong,
                                dinhDangTien(giaDichVu),
                                dinhDangTien(thanhTienDV)
                        });

                        // Tạo dòng phụ tùng
                        BigDecimal thanhTienPT = giaPhuTung
                                .multiply(BigDecimal.valueOf(soLuong));

                        danhSachPhuTung.add(new Object[] {
                                "Phụ tùng",
                                rs.getString("maPhuTung"),
                                rs.getString("tenPhuTung"),
                                soLuong,
                                dinhDangTien(giaPhuTung),
                                dinhDangTien(thanhTienPT)
                        });
                    }

                    // Thêm tất cả dịch vụ trước
                    for (Object[] dong : danhSachDichVu) {
                        modelChiTiet.addRow(dong);
                    }

                    // Sau đó mới thêm tất cả phụ tùng
                    for (Object[] dong : danhSachPhuTung) {
                        modelChiTiet.addRow(dong);
                    }
                }
            }

            tinhTongHoaDon();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải chi tiết phiếu sửa chữa:\n"
                            + ex.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    // SỰ KIỆN VÀ ĐỔI THẺ THEO LOẠI HÓA ĐƠN
    // =========================================================

    private void caiDatSuKien() {
        cboLoaiHoaDon.addActionListener(
                e -> capNhatLoaiHoaDon());

        btnThemHoaDon.addActionListener(e -> capNhatMaHoaDon());

        btnSuaHoaDon.addActionListener(e -> suaHoaDon());

        btnXoaHoaDon.addActionListener(e -> xoaHoaDon());

        btnTimPhieu.addActionListener(e -> hienThiCuaSoTimPhieu());

        // Nhập mã phiếu rồi nhấn Enter để tải thông tin
        txtMaPhieuSuaChua.addActionListener(e -> {
            if (!SUA_CHUA.equals(cboLoaiHoaDon.getSelectedItem())) {
                return;
            }

            String maPhieu = txtMaPhieuSuaChua.getText().trim();

            if (!maPhieu.isEmpty()) {
                napThongTinPhieu(maPhieu);
            }
        });

        cboPhuTung.addActionListener(e -> capNhatDonGiaPhuTung());
        cboKhuyenMai.addActionListener(e -> tinhTongHoaDon());
    }

    private void capNhatDonGiaPhuTung() {
        String luaChon = (String) cboPhuTung.getSelectedItem();

        if (luaChon == null || luaChon.startsWith("--")) {
            txtDonGia.setText("0");
            return;
        }

        String[] thongTin = luaChon.split(" - ", 2);
        String maPhuTung = thongTin[0].trim();

        String sql = "SELECT giaBan FROM PhuTung WHERE maPhuTung = ?";

        try {
            Connection conn = ConnectDB.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maPhuTung);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal giaBan = rs.getBigDecimal("giaBan");

                        if (giaBan == null) {
                            giaBan = BigDecimal.ZERO;
                        }

                        txtDonGia.setText(giaBan.toPlainString());
                    } else {
                        txtDonGia.setText("0");
                    }
                }
            }
        } catch (SQLException ex) {
            txtDonGia.setText("0");

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải giá bán phụ tùng:\n" + ex.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatLoaiHoaDon() {
        String loai = (String) cboLoaiHoaDon.getSelectedItem();

        boolean laSuaChua = SUA_CHUA.equals(loai);
        boolean laMuaPhuTung = MUA_PHU_TUNG.equals(loai);

        // Đổi thẻ hiển thị theo loại hóa đơn
        if (laSuaChua) {
            cardNguon.show(pnlNguon, THE_SUA_CHUA);
            theNguon.datTieuDe("PHIẾU SỬA CHỮA");
        } else if (laMuaPhuTung) {
            cardNguon.show(pnlNguon, THE_PHU_TUNG);
            theNguon.datTieuDe("CHỌN PHỤ TÙNG");
        } else {
            cardNguon.show(pnlNguon, THE_TRONG);
            theNguon.datTieuDe("NGUỒN HÓA ĐƠN");
        }

        // Mỗi loại hóa đơn có chi tiết khác nhau nên xóa dữ liệu cũ khi đổi loại
        txtMaPhieuSuaChua.setText("");

        txtMaKhachHang.setText("");
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");
        txtBienSoXe.setText("");
        txtTenXe.setText("");
        txtMaNhanVien.setText("");
        txtTenNhanVien.setText("");

        modelChiTiet.setRowCount(0);

        cboPhuTung.setSelectedIndex(0);
        spnSoLuong.setValue(1);
        txtDonGia.setText("0");

        tinhTongHoaDon();

        pnlNguon.revalidate();
        revalidate();
        repaint();
    }

    // Sửa hóa đơn đang chọn
    private void suaHoaDon() {
        int row = tblChiTiet.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn dữ liệu cần sửa.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Phần cập nhật hóa đơn cần kết nối HoaDonDAO.");
    }

    // Xóa dòng chi tiết đang chọn
    private void xoaHoaDon() {
        int row = tblChiTiet.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn dữ liệu cần xóa.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa dòng chi tiết này không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);

        if (luaChon == JOptionPane.YES_OPTION) {
            modelChiTiet.removeRow(
                    tblChiTiet.convertRowIndexToModel(row));

            tinhTongHoaDon();
        }
    }

    // =========================================================
    // CỬA SỔ TÌM PHIẾU SỬA CHỮA
    // =========================================================

    private void hienThiCuaSoTimPhieu() {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Tìm phiếu sửa chữa",
                Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(1000, 480);
        dialog.setMinimumSize(new Dimension(800, 400));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        // ===== KHU VỰC TÌM KIẾM =====
        JPanel pnlTim = new JPanel(new BorderLayout(8, 8));
        pnlTim.setOpaque(false);
        pnlTim.setBorder(new EmptyBorder(12, 12, 4, 12));

        JLabel lblHuongDan = new JLabel(
                "Nhập mã phiếu, mã khách hàng, mã xe, tên khách hàng, "
                        + "số điện thoại hoặc biển số xe:");
        lblHuongDan.setFont(FONT_THUONG);
        lblHuongDan.setForeground(MAU_PHU);

        JTextField txtTuKhoa = new JTextField();
        kieuO(txtTuKhoa, false);
        JButton btnTim = taoNut("Tìm kiếm");

        JPanel pnlNhap = new JPanel(new BorderLayout(8, 0));
        pnlNhap.setOpaque(false);
        pnlNhap.add(txtTuKhoa, BorderLayout.CENTER);
        pnlNhap.add(btnTim, BorderLayout.EAST);

        pnlTim.add(lblHuongDan, BorderLayout.NORTH);
        pnlTim.add(pnlNhap, BorderLayout.CENTER);

        // ===== BẢNG KẾT QUẢ =====
        String[] cot = {
                "Mã phiếu",
                "Mã khách hàng",
                "Tên khách hàng",
                "Số điện thoại",
                "Mã xe",
                "Biển số xe",
                "Nhân viên",
                "Ngày bắt đầu"
        };

        DefaultTableModel model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tblKetQua = new JTable(model);
        tblKetQua.setFont(FONT_THUONG);
        tblKetQua.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblKetQua.setRowHeight(28);
        tblKetQua.setAutoCreateRowSorter(true);
        tblKetQua.getTableHeader().setReorderingAllowed(false);
        tblKetQua.getTableHeader().setFont(FONT_DAM);
        tblKetQua.getTableHeader().setPreferredSize(new Dimension(0, 32));

        // ===== NÚT DƯỚI =====
        JButton btnChon = taoNut("Chọn phiếu");
        JButton btnDong = taoNutPhu("Đóng");

        JPanel pnlNut = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlNut.setOpaque(false);
        pnlNut.add(btnDong);
        pnlNut.add(btnChon);

        // ===== HÀM TÌM KIẾM =====
        Runnable timKiem = () -> {
            String tuKhoa = txtTuKhoa.getText().trim();

            if (tuKhoa.isEmpty()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Vui lòng nhập thông tin cần tìm.",
                        "Thiếu từ khóa",
                        JOptionPane.WARNING_MESSAGE);
                txtTuKhoa.requestFocusInWindow();
                return;
            }

            model.setRowCount(0);

            String sql = "SELECT p.maPhieuSuaChua, p.maKH, kh.hoTen, kh.soDT, "
                    + "p.maXe, x.bienSo, p.maNV, p.ngayBatDau "
                    + "FROM PhieuSuaChua p "
                    + "JOIN KhachHang kh ON p.maKH = kh.maKH "
                    + "JOIN Xe x ON p.maXe = x.maXe "
                    + "WHERE p.maPhieuSuaChua LIKE ? "
                    + "OR p.maKH LIKE ? "
                    + "OR p.maXe LIKE ? "
                    + "OR kh.hoTen LIKE ? "
                    + "OR kh.soDT LIKE ? "
                    + "OR x.bienSo LIKE ? "
                    + "ORDER BY p.ngayBatDau DESC";

            try {
                Connection conn = ConnectDB.getConnection();

                if (conn == null || conn.isClosed()) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Không thể kết nối SQL Server. "
                                    + "Vui lòng kiểm tra cấu hình kết nối.",
                            "Lỗi kết nối",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    String mau = "%" + tuKhoa + "%";

                    for (int i = 1; i <= 6; i++) {
                        ps.setString(i, mau);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            model.addRow(new Object[] {
                                    rs.getString("maPhieuSuaChua"),
                                    rs.getString("maKH"),
                                    rs.getString("hoTen"),
                                    rs.getString("soDT"),
                                    rs.getString("maXe"),
                                    rs.getString("bienSo"),
                                    rs.getString("maNV"),
                                    rs.getTimestamp("ngayBatDau")
                            });
                        }
                    }
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Không tìm thấy phiếu sửa chữa phù hợp.",
                            "Kết quả tìm kiếm",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Lỗi truy vấn phiếu sửa chữa:\n" + ex.getMessage(),
                        "Lỗi SQL",
                        JOptionPane.ERROR_MESSAGE);
            }
        };

        btnTim.addActionListener(e -> timKiem.run());
        txtTuKhoa.addActionListener(e -> timKiem.run());

        // ===== CHỌN PHIẾU =====
        Runnable chonPhieu = () -> {
            int selectedRow = tblKetQua.getSelectedRow();

            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Vui lòng chọn một phiếu sửa chữa trong bảng.",
                        "Chưa chọn phiếu",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int modelRow = tblKetQua.convertRowIndexToModel(selectedRow);
            String maPhieu = model.getValueAt(modelRow, 0).toString();

            if (napThongTinPhieu(maPhieu)) {
                dialog.dispose();
            }
        };

        btnChon.addActionListener(e -> chonPhieu.run());

        tblKetQua.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2
                        && SwingUtilities.isLeftMouseButton(e)) {
                    chonPhieu.run();
                }
            }
        });

        btnDong.addActionListener(e -> dialog.dispose());

        dialog.add(pnlTim, BorderLayout.NORTH);
        dialog.add(new JScrollPane(tblKetQua), BorderLayout.CENTER);
        dialog.add(pnlNut, BorderLayout.SOUTH);

        dialog.getRootPane().setDefaultButton(btnTim);
        dialog.setVisible(true);
    }

    // =========================================================
    // THÊM PHỤ TÙNG VÀO BẢNG
    // =========================================================

    private void themPhuTung() {
        if (!MUA_PHU_TUNG.equals(cboLoaiHoaDon.getSelectedItem())) {
            return;
        }

        String luaChon = (String) cboPhuTung.getSelectedItem();

        if (luaChon == null || luaChon.startsWith("--")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn phụ tùng.");
            return;
        }

        // ComboBox chỉ có dạng: Mã phụ tùng - Tên phụ tùng
        String[] thongTin = luaChon.split(" - ", 2);

        String maPhuTung = thongTin[0].trim();
        String tenPhuTung = thongTin.length > 1
                ? thongTin[1].trim()
                : "";

        try {
            int soLuong = (Integer) spnSoLuong.getValue();

            BigDecimal donGia = new BigDecimal(
                    txtDonGia.getText().trim());

            if (donGia.signum() < 0) {
                throw new NumberFormatException();
            }

            BigDecimal thanhTien = donGia.multiply(
                    BigDecimal.valueOf(soLuong));

            // Thêm phụ tùng vào bảng chi tiết hóa đơn
            modelChiTiet.addRow(new Object[] {
                    "Phụ tùng",
                    maPhuTung,
                    tenPhuTung,
                    soLuong,
                    dinhDangTien(donGia),
                    dinhDangTien(thanhTien)
            });

            // Cập nhật tổng tiền
            tinhTongHoaDon();

            // Làm mới các ô phụ tùng sau khi thêm thành công
            lamMoiNhapLieu();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đơn giá phải là số hợp lệ và không âm.",
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // =========================================================
    // XÓA DÒNG CHI TIẾT
    // =========================================================

    private void xoaDongChiTiet() {
        int row = tblChiTiet.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn dòng cần xóa.");
            return;
        }

        modelChiTiet.removeRow(
                tblChiTiet.convertRowIndexToModel(row));

        tinhTongHoaDon();
    }

    // =========================================================
    // TÍNH TỔNG HÓA ĐƠN
    // =========================================================

    private void tinhTongHoaDon() {
        BigDecimal tienDichVu = BigDecimal.ZERO;
        BigDecimal tienPhuTung = BigDecimal.ZERO;

        // 1. Tính tiền dịch vụ và phụ tùng từ bảng chi tiết
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            String loai = String.valueOf(
                    modelChiTiet.getValueAt(i, 0));

            BigDecimal thanhTien = docTien(
                    modelChiTiet.getValueAt(i, 5));

            if ("Dịch vụ".equals(loai)) {
                tienDichVu = tienDichVu.add(thanhTien);
            } else if ("Phụ tùng".equals(loai)) {
                tienPhuTung = tienPhuTung.add(thanhTien);
            }
        }

        // 2. Tính tiền trước thuế
        BigDecimal tienTruocThue = tienDichVu.add(tienPhuTung);

        // 3. Tính VAT 8%
        BigDecimal tienVAT = tienTruocThue
                .multiply(new BigDecimal("0.08"))
                .setScale(0, RoundingMode.HALF_UP);

        // Tổng hóa đơn gồm tiền dịch vụ, phụ tùng và VAT
        BigDecimal tamTinh = tienTruocThue.add(tienVAT);

        // 4. Lấy tỷ lệ giảm từ chương trình được chọn
        KhuyenMai km = (KhuyenMai) cboKhuyenMai.getSelectedItem();

        BigDecimal phanTramGiam = BigDecimal.ZERO;

        if (km != null && !km.getMaKM().isEmpty()) {
            phanTramGiam = km.getTileGiam();
        }

        // Bảo vệ dữ liệu tỷ lệ giảm giá
        if (phanTramGiam == null
                || phanTramGiam.signum() < 0
                || phanTramGiam.compareTo(
                        new BigDecimal("100")) > 0) {
            phanTramGiam = BigDecimal.ZERO;
        }

        // 5. Tính số tiền được giảm
        BigDecimal tienGiam = tamTinh
                .multiply(phanTramGiam)
                .divide(
                        new BigDecimal("100"),
                        0,
                        RoundingMode.HALF_UP);

        // Không cho tiền giảm vượt quá tổng tiền
        if (tienGiam.compareTo(tamTinh) > 0) {
            tienGiam = tamTinh;
        }

        // 6. Tổng thanh toán sau giảm giá
        BigDecimal tong = tamTinh.subtract(tienGiam);

        // 7. Hiển thị lên giao diện
        lblTienDichVu.setText(dinhDangTien(tienDichVu));
        lblTienPhuTung.setText(dinhDangTien(tienPhuTung));
        lblTienVAT.setText(dinhDangTien(tienVAT));
        lblTongHoaDon.setText(dinhDangTien(tamTinh));
        lblTienGiam.setText(dinhDangTien(tienGiam));
        lblTongThanhToan.setText(dinhDangTien(tong));
    }

    private BigDecimal docTien(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        try {
            if (value instanceof Number) {
                return new BigDecimal(value.toString());
            }

            String text = value.toString()
                    .trim()
                    .replace("đ", "")
                    .replace("Đ", "")
                    .replace(",", "")
                    .replace(".", "")
                    .replace("%", "")
                    .trim();

            if (text.isEmpty()) {
                return BigDecimal.ZERO;
            }

            return new BigDecimal(text);

        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private String dinhDangTien(BigDecimal tien) {
        NumberFormat nf = NumberFormat.getIntegerInstance(
                Locale.forLanguageTag("vi-VN"));

        return nf.format(tien) + " đ";
    }

    // =========================================================
    // LÀM MỚI NHẬP LIỆU
    // =========================================================

    private void lamMoiNhapLieu() {
        txtMaPhieuSuaChua.setText("");
        cboPhuTung.setSelectedIndex(0);
        spnSoLuong.setValue(1);
        txtDonGia.setText("0");
    }

    private void lamMoiToanBo() {
        lamMoiNhapLieu();

        cboKhuyenMai.setSelectedIndex(0);

        modelChiTiet.setRowCount(0);

        txtMaNhanVien.setText("");
        txtTenNhanVien.setText("");

        txtMaKhachHang.setText("");
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");

        txtBienSoXe.setText("");
        txtTenXe.setText("");

        txtNgayLap.setText(LocalDate.now().format(DINH_DANG_NGAY));

        cboLoaiHoaDon.setSelectedIndex(0);

        tinhTongHoaDon();
    }

    // =========================================================
    // LƯU VÀ IN HÓA ĐƠN
    // =========================================================

    private void luuHoaDon() {
        if (cboLoaiHoaDon.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn loại hóa đơn.");
            return;
        }

        if (modelChiTiet.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng thêm chi tiết hóa đơn.",
                    "Thiếu chi tiết",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Giao diện đã kiểm tra loại hóa đơn và chi tiết."
                        + "\nChưa lưu vào SQL Server vì chưa "
                        + "cấu hình câu lệnh INSERT thực tế.",
                "Chưa cấu hình lưu dữ liệu",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================
    // TẠO NÚT
    // =========================================================

    /** Nút chính: nền xanh, chữ trắng. */
    private JButton taoNut(String text) {
        return taoNutMau(text, MAU_NUT, MAU_NUT_DAM, Color.WHITE, MAU_NUT_DAM);
    }

    /** Nút nguy hiểm (xóa): nền đỏ, chữ trắng. */
    private JButton taoNutNguyHiem(String text) {
        return taoNutMau(text, MAU_DO, new Color(185, 28, 40), Color.WHITE, new Color(185, 28, 40));
    }

    /** Nút phụ: nền trắng, viền xám, chữ tối. */
    private JButton taoNutPhu(String text) {
        return taoNutMau(text, Color.WHITE, new Color(241, 245, 249), MAU_CHU, new Color(180, 190, 205));
    }

    private JButton taoNutMau(String text, Color nen, Color nenKhiRe, Color chu, Color vien) {
        JButton btn = new JButton(text);

        // Dùng BasicButtonUI để Look and Feel không ghi đè màu nền
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

        // Bề rộng theo độ dài chữ, tối thiểu 90px, cao đều 34px
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

}
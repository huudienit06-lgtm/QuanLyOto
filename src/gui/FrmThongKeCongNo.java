package gui;

import connectDB.ConnectDB;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmThongKeCongNo extends JFrame {
    private static final Color BG = new Color(244, 249, 255);
    private static final Color BLUE = new Color(35, 128, 238);
    private static final Color DARK_BLUE = new Color(18, 58, 119);
    private static final Color BORDER = new Color(196, 220, 250);
    private static final Color RED = new Color(231, 76, 76);
    private static final Color GREEN = new Color(48, 174, 125);
    private static final Color TEXT = new Color(40, 52, 75);
    private static final Color MUTED = new Color(110, 125, 150);
    private static final Color DEBT_RED = new Color(214, 48, 49);
    private static final Color PALE = new Color(246, 250, 255);
    private static final Color ZEBRA = new Color(247, 251, 255);
    private static final Color GRID = new Color(225, 236, 250);
    private static final Color SELECT_BG = new Color(207, 230, 255);
    private static final Color HEADER_BG = new Color(232, 243, 255);
    private static final Color DISABLED = new Color(160, 168, 180);
    // Định dạng
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter GIO_NGAY = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    // Chế độ thống kê
    private static final int CHE_DO_NGAY = 0;
    private static final int CHE_DO_THANG = 1;
    private static final int CHE_DO_NAM = 2;
    // Tiêu đề cột bảng danh sách theo từng chế độ
    private static final String[] COT_NGAY = { "Mã hóa đơn", "Khách hàng", "Số điện thoại", "Biển số xe",
            "Tổng tiền HĐ", "Còn nợ", "Trạng thái" };
    // Câu lệnh SQL
    private static final String SQL_KHACH_HANG = "SELECT maKH, hoTen, soDT FROM KhachHang ORDER BY maKH";
    // Hóa đơn kèm tổng tiền đã thanh toán thành công
    private static final String SQL_HOA_DON = "SELECT hd.maHoaDon, hd.ngayLap, hd.soTien, " +
            "       psc.maKH, " +
            "       COALESCE(tt.daThanhToan, 0) AS daThanhToan " +
            "FROM HoaDon hd " +
            "LEFT JOIN PhieuSuaChua psc " +
            "       ON hd.maPhieuSuaChua = psc.maPhieuSuaChua " +
            "LEFT JOIN ( " +
            "    SELECT maHoaDon, " +
            "           SUM(soTienThanhToan) AS daThanhToan " +
            "    FROM GiaoDichThanhToan " +
            "    WHERE trangThaiGD = N'Thành công' " +
            "    GROUP BY maHoaDon " +
            ") tt ON hd.maHoaDon = tt.maHoaDon " +
            "ORDER BY hd.ngayLap DESC";
    // Lịch sử giao dịch của một hóa đơn
    private static final String SQL_GIAO_DICH = "SELECT thoiGianGD, phuongThucThanhToan, soTienThanhToan, trangThaiGD "
            + "FROM GiaoDichThanhToan WHERE maHoaDon = ? ORDER BY thoiGianGD";

    // LỚP DỮ LIỆU
    private static class KhachHang {
        final String ma, ten, sdt;

        KhachHang(String ma, String ten, String sdt) {
            this.ma = ma;
            this.ten = ten;
            this.sdt = sdt;
        }
    }

    // phatSinh: công nợ phát sinh khi lập hóa đơn; conNo: số còn nợ hiện tại
    private static class HoaDon {
        final String ma, maKH, bienSo;
        final LocalDate ngay;
        final long tong, phatSinh, conNo;

        HoaDon(String ma, String bienSo, LocalDate ngay, String maKH, long tong, long phatSinh, long conNo) {
            this.ma = ma;
            this.bienSo = bienSo;
            this.ngay = ngay;
            this.maKH = maKH;
            this.tong = tong;
            this.phatSinh = phatSinh;
            this.conNo = conNo;
        }
    }

    private static class GiaoDich {
        String thoiGian, phuongThuc, trangThai;
        long soTien;
    }

    // DỮ LIỆU VÀ TRẠNG THÁI
    private final Map<String, KhachHang> dsKhachHang = new LinkedHashMap<>();
    private final List<HoaDon> dsHoaDon = new ArrayList<>();

    // Dòng đang hiển thị trên bảng danh sách (để mở chi tiết / đi sâu xuống ngày,
    // tháng)
    private final List<HoaDon> dongHoaDon = new ArrayList<>();
    private final List<LocalDate> dongKhoa = new ArrayList<>();

    private int cheDo = CHE_DO_NGAY;
    private int cheDoCuoi = -1; // chế độ của lần dựng cột gần nhất
    private boolean sanSang = false; // chỉ thống kê sau khi giao diện dựng xong
    private boolean dangDieuChinh = false; // chặn sự kiện khi đang đặt giá trị bằng code

    // THÀNH PHẦN GIAO DIỆN
    // Thanh tab và thanh thời gian
    private JToggleButton[] tglTab;
    private JPanel pnlThoiGian;
    private JComboBox<Integer> cmbNgay, cmbThang, cmbNam;
    private JLabel lblPhatSinh, lblConLai;
    // Khung bộ lọc
    private JComboBox<String> cmbPhamVi;
    private JTextField txtMaKH, txtTenKH, txtSoDienThoai;
    private ActionButton btnTimKiem;
    private JTable tblKhachHang;
    private DefaultTableModel modelKhachHang;
    private TableRowSorter<DefaultTableModel> sorterKhachHang;
    // Khung danh sách và biểu đồ
    private JPanel pnlCacCot, pnlCotDanhSach, pnlCotBieuDo;
    private JTable tblHoaDon;
    private DefaultTableModel modelHoaDon;
    private JLabel lblTieuDeDanhSach, lblTieuDeBieuDo, lblSoHoaDon, lblTongNoKy;
    private BieuDoCongNo pnlBieuDo;

    // KHỞI TẠO
    public FrmThongKeCongNo() {
        setTitle("Hệ thống quản lý gara ô tô");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 720));
        setSize(1500, 900);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        try {
            taiDuLieu();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu công nợ từ CSDL:\n" + ex.getMessage(),
                    "Lỗi kết nối CSDL", JOptionPane.ERROR_MESSAGE);
        }
        add(dungTieuDe(), BorderLayout.NORTH);
        add(dungKhungChinh(), BorderLayout.CENTER);
        ganSuKien();
        napBangKhachHang();
        apDungPhamVi();
        dongBoSoNgay();
        sanSang = true;
        lamMoiThongKe();
    }

    // TẢI DỮ LIỆU TỪ SQL SERVER
    private void taiDuLieu() throws SQLException {
        dsKhachHang.clear();
        dsHoaDon.clear();

        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối SQL Server.");
        }
        // 1. Danh sách khách hàng
        try (PreparedStatement ps = con.prepareStatement(SQL_KHACH_HANG); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maKH");
                dsKhachHang.put(ma, new KhachHang(ma, rs.getString("hoTen"), rs.getString("soDT")));
            }
        }
        // 2. Hóa đơn và số tiền đã thanh toán thành công
        try (PreparedStatement ps = con.prepareStatement(SQL_HOA_DON); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ngayLap = rs.getTimestamp("ngayLap");
                if (ngayLap == null) {
                    continue;
                }
                long tong = laySo(rs.getBigDecimal("soTien"));
                long daTra = laySo(rs.getBigDecimal("daThanhToan"));
                dsHoaDon.add(new HoaDon(
                        rs.getString("maHoaDon"),
                        "-", // Biển số: cần bổ sung JOIN bảng Xe
                        ngayLap.toLocalDateTime().toLocalDate(),
                        rs.getString("maKH"),
                        tong, tong, Math.max(0L, tong - daTra)));
            }
        }
    }

    private static long laySo(BigDecimal so) {
        return so == null ? 0L : so.longValue();
    }

    private String tenKhachHang(String ma) {
        KhachHang kh = dsKhachHang.get(ma);
        return kh == null ? ma : kh.ten;
    }

    private String sdtKhachHang(String ma) {
        KhachHang kh = dsKhachHang.get(ma);
        return kh == null ? "" : kh.sdt;
    }

    // DỰNG GIAO DIỆN CHÍNH
    private JPanel dungTieuDe() {
        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setBackground(BG);
        pnlTieuDe.setBorder(new EmptyBorder(10, 16, 0, 16));

        JLabel lblTieuDe = new JLabel("THỐNG KÊ CÔNG NỢ", SwingConstants.CENTER);
        lblTieuDe.setFont(font(Font.BOLD, 24));
        lblTieuDe.setForeground(Color.BLACK);
        lblTieuDe.setBorder(new EmptyBorder(2, 0, 12, 0));
        pnlTieuDe.add(lblTieuDe, BorderLayout.NORTH);

        // Thanh tab tự vẽ (không có vùng nội dung nên không còn khoảng trống phía dưới)
        JPanel pnlTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlTab.setBackground(BG);
        pnlTab.setBorder(new MatteBorder(0, 0, 2, 0, BLUE));
        String[] dsTen = { "Theo ngày", "Theo tháng", "Theo năm" };
        tglTab = new JToggleButton[dsTen.length];
        ButtonGroup nhom = new ButtonGroup();
        for (int i = 0; i < dsTen.length; i++) {
            final int chiSo = i;
            tglTab[i] = new TabButton(dsTen[i]);
            tglTab[i].addActionListener(e -> chonTab(chiSo));
            nhom.add(tglTab[i]);
            pnlTab.add(tglTab[i]);
        }
        tglTab[0].setSelected(true);
        pnlTieuDe.add(pnlTab, BorderLayout.SOUTH);
        return pnlTieuDe;
    }

    private JPanel dungKhungChinh() {
        JPanel pnlChinh = new JPanel(new BorderLayout(0, 10));
        pnlChinh.setBackground(BG);
        pnlChinh.setBorder(new EmptyBorder(10, 16, 14, 16));
        pnlChinh.add(dungThanhThoiGian(), BorderLayout.NORTH);

        JPanel pnlBoLoc = dungKhungBoLoc();
        pnlCotDanhSach = dungKhungDanhSach();
        pnlCotBieuDo = dungKhungBieuDo();

        // Kích thước ưu tiên nhỏ để các cột chia đúng theo tỉ lệ weight
        for (JPanel pnlCot : new JPanel[] { pnlBoLoc, pnlCotDanhSach, pnlCotBieuDo }) {
            pnlCot.setPreferredSize(new Dimension(10, 10));
        }

        pnlCacCot = new JPanel(new GridBagLayout());
        pnlCacCot.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.weightx = 0.28;
        c.insets = new Insets(0, 0, 0, 5);
        pnlCacCot.add(pnlBoLoc, c);
        c.gridx = 1;
        c.weightx = 0.38;
        c.insets = new Insets(0, 5, 0, 5);
        pnlCacCot.add(pnlCotDanhSach, c);
        c.gridx = 2;
        c.weightx = 0.34;
        c.insets = new Insets(0, 5, 0, 0);
        pnlCacCot.add(pnlCotBieuDo, c);

        pnlChinh.add(pnlCacCot, BorderLayout.CENTER);
        pnlChinh.add(dungThanhNut(), BorderLayout.SOUTH);
        return pnlChinh;
    }

    // Hàng trên cùng: thời gian thống kê + công nợ phát sinh + công nợ còn lại
    private JPanel dungThanhThoiGian() {
        JPanel pnlThanh = taoPnlThe();
        pnlThanh.setLayout(new BorderLayout(16, 0));
        pnlThanh.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));

        cmbNgay = new JComboBox<>();
        for (int i = 1; i <= 31; i++) {
            cmbNgay.addItem(i);
        }
        cmbThang = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            cmbThang.addItem(i);
        }
        cmbNam = new JComboBox<>();
        for (int i = 2022; i <= 2035; i++) {
            cmbNam.addItem(i);
        }
        LocalDate homNay = LocalDate.now();
        cmbNgay.setSelectedItem(homNay.getDayOfMonth());
        cmbThang.setSelectedItem(homNay.getMonthValue());
        cmbNam.setSelectedItem(homNay.getYear());

        pnlThoiGian = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlThoiGian.setOpaque(false);
        capNhatOThoiGian();

        JPanel pnlHang = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlHang.setOpaque(false);
        pnlHang.add(taoTieuMuc("Thời gian thống kê:"));
        pnlHang.add(pnlThoiGian);

        JPanel pnlTrai = new JPanel(new GridBagLayout());
        pnlTrai.setOpaque(false);
        pnlTrai.add(pnlHang);

        lblPhatSinh = taoLblSoLieu(RED);
        lblConLai = taoLblSoLieu(BLUE);
        JPanel pnlSoLieu = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlSoLieu.setOpaque(false);
        pnlSoLieu.add(taoOThongKe("Phát sinh (trong kỳ)", RED, lblPhatSinh));
        pnlSoLieu.add(taoOThongKe("Còn lại (đến cuối kỳ)", BLUE, lblConLai));

        pnlThanh.add(pnlTrai, BorderLayout.WEST);
        pnlThanh.add(pnlSoLieu, BorderLayout.EAST);
        return pnlThanh;
    }

    // Khung 1: bộ lọc
    private JPanel dungKhungBoLoc() {
        JPanel pnlBoLoc = taoPnlThe();
        JPanel pnlTren = new JPanel();
        pnlTren.setOpaque(false);
        pnlTren.setLayout(new BoxLayout(pnlTren, BoxLayout.Y_AXIS));

        // Thống kê theo
        cmbPhamVi = new JComboBox<>(new String[] { "Tất cả", "Khách hàng" });
        JPanel pnlPhamVi = new JPanel(new BorderLayout(10, 0));
        pnlPhamVi.setOpaque(false);
        pnlPhamVi.add(taoTieuMuc("Thống kê theo:"), BorderLayout.WEST);
        pnlPhamVi.add(cmbPhamVi, BorderLayout.CENTER);
        pnlTren.add(codinh(pnlPhamVi, 32));
        pnlTren.add(Box.createVerticalStrut(4));

        JLabel lblGoiY = new JLabel("Chọn \"Khách hàng\" để bật tìm kiếm và chọn khách hàng.");
        lblGoiY.setFont(font(Font.ITALIC, 11));
        lblGoiY.setForeground(MUTED);
        pnlTren.add(codinh(lblGoiY, 16));
        themDuongKe(pnlTren);

        // Tìm kiếm khách hàng
        pnlTren.add(taoTieuMuc("Tìm kiếm khách hàng"));
        pnlTren.add(Box.createVerticalStrut(6));
        txtMaKH = new JTextField();
        txtTenKH = new JTextField();
        txtSoDienThoai = new JTextField();
        themOTimKiem(pnlTren, "Mã KH:", txtMaKH);
        themOTimKiem(pnlTren, "Tên KH:", txtTenKH);
        themOTimKiem(pnlTren, "Số điện thoại:", txtSoDienThoai);

        btnTimKiem = new ActionButton("Tìm kiếm", true);
        btnTimKiem.addActionListener(e -> locKhachHang());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnTimKiem);
        pnlTren.add(Box.createVerticalStrut(2));
        pnlTren.add(codinh(pnlNut, 36));
        themDuongKe(pnlTren);

        // Danh sách khách hàng
        modelKhachHang = taoModel("Mã KH", "Tên khách hàng", "Số điện thoại");
        tblKhachHang = new JTable(modelKhachHang);
        caiDatBang(tblKhachHang);
        tblKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorterKhachHang = new TableRowSorter<>(modelKhachHang);
        tblKhachHang.setRowSorter(sorterKhachHang);
        datDoRongCot(tblKhachHang, 65, 130, 100);

        JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 6));
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.add(taoTieuMuc("Danh sách khách hàng"), BorderLayout.NORTH);
        pnlDanhSach.add(taoScr(tblKhachHang), BorderLayout.CENTER);

        pnlBoLoc.add(pnlTren, BorderLayout.NORTH);
        pnlBoLoc.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlBoLoc;
    }

    private void themOTimKiem(JPanel pnlCha, String nhan, JTextField txt) {
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(font(Font.PLAIN, 12));
        lblNhan.setForeground(DARK_BLUE);
        lblNhan.setPreferredSize(new Dimension(92, 30));

        txt.setFont(font(Font.PLAIN, 12));
        txt.setPreferredSize(new Dimension(100, 30));

        JPanel pnlHang = new JPanel(new BorderLayout(8, 0));
        pnlHang.setOpaque(false);
        pnlHang.add(lblNhan, BorderLayout.WEST);
        pnlHang.add(txt, BorderLayout.CENTER);
        pnlCha.add(codinh(pnlHang, 32));
        pnlCha.add(Box.createVerticalStrut(6));
    }

    private JPanel taoOThongKe(String tieuDe, Color mau, JLabel lblGiaTri) {
        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(font(Font.PLAIN, 12));
        lblTieuDe.setForeground(DARK_BLUE);
        lblTieuDe.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mau);
                g2.fillOval(x, y, 10, 10);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        });
        lblTieuDe.setIconTextGap(8);

        JPanel pnlO = new JPanel(new BorderLayout(8, 0));
        pnlO.setBackground(PALE);
        pnlO.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(5, 10, 5, 10)));
        pnlO.add(lblTieuDe, BorderLayout.WEST);
        pnlO.add(lblGiaTri, BorderLayout.EAST);
        return pnlO;
    }

    private JLabel taoLblSoLieu(Color mau) {
        JLabel lbl = new JLabel("0 đ", SwingConstants.RIGHT);
        lbl.setFont(font(Font.BOLD, 15));
        lbl.setForeground(mau);
        return lbl;
    }

    // Khung 2: danh sách
    private JPanel dungKhungDanhSach() {
        JPanel pnlDanhSach = taoPnlThe();

        ActionButton btnChiTiet = new ActionButton("Xem chi tiết", false);
        ActionButton btnXuatExcel = new ActionButton("Xuất EXCEL", false);
        btnChiTiet.addActionListener(e -> xemChiTiet());
        btnXuatExcel.addActionListener(e -> xuatExcel());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnChiTiet);
        pnlNut.add(btnXuatExcel);

        lblTieuDeDanhSach = taoTieuDe("DANH SÁCH HÓA ĐƠN CÓ CÔNG NỢ");
        JPanel pnlTieuDe = new JPanel(new BorderLayout(8, 0));
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(lblTieuDeDanhSach, BorderLayout.CENTER);
        pnlTieuDe.add(pnlNut, BorderLayout.EAST);
        pnlDanhSach.add(pnlTieuDe, BorderLayout.NORTH);

        modelHoaDon = taoModel();
        tblHoaDon = new JTable(modelHoaDon);
        caiDatBang(tblHoaDon);
        tblHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlDanhSach.add(taoScr(tblHoaDon), BorderLayout.CENTER);

        lblSoHoaDon = new JLabel("Tổng số hóa đơn có công nợ: 0");
        lblSoHoaDon.setFont(font(Font.PLAIN, 12));
        lblSoHoaDon.setForeground(DARK_BLUE);
        lblTongNoKy = new JLabel("Tổng còn nợ trong kỳ: 0 đ", SwingConstants.RIGHT);
        lblTongNoKy.setFont(font(Font.BOLD, 12));
        lblTongNoKy.setForeground(RED);

        JPanel pnlChan = new JPanel(new BorderLayout(10, 0));
        pnlChan.setOpaque(false);
        pnlChan.add(lblSoHoaDon, BorderLayout.WEST);
        pnlChan.add(lblTongNoKy, BorderLayout.EAST);
        pnlDanhSach.add(pnlChan, BorderLayout.SOUTH);
        return pnlDanhSach;
    }

    // Khung 3: biểu đồ
    private JPanel dungKhungBieuDo() {
        JPanel pnlKhung = taoPnlThe();
        lblTieuDeBieuDo = taoTieuDe("TÌNH HÌNH CÔNG NỢ");
        pnlBieuDo = new BieuDoCongNo();
        pnlKhung.add(lblTieuDeBieuDo, BorderLayout.NORTH);
        pnlKhung.add(pnlBieuDo, BorderLayout.CENTER);
        return pnlKhung;
    }

    private JPanel dungThanhNut() {
        ActionButton btnLamMoi = new ActionButton("Làm mới", false);
        ActionButton btnDong = new ActionButton("Đóng", false);
        btnLamMoi.addActionListener(e -> lamMoiBoLoc());
        btnDong.addActionListener(e -> dispose());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnDong);
        return pnlNut;
    }

    // SỰ KIỆN
    private void ganSuKien() {
        cmbNgay.addActionListener(e -> {
            if (!dangDieuChinh) {
                lamMoiThongKe();
            }
        });
        java.awt.event.ActionListener doiThangNam = e -> {
            if (!dangDieuChinh) {
                dongBoSoNgay();
                lamMoiThongKe();
            }
        };
        cmbThang.addActionListener(doiThangNam);
        cmbNam.addActionListener(doiThangNam);

        // Đổi phạm vi thống kê (Tất cả / Khách hàng)
        cmbPhamVi.addActionListener(e -> {
            if (dangDieuChinh) {
                return;
            }
            apDungPhamVi();
            lamMoiThongKe();
        });

        // Chọn một khách hàng trong danh sách
        tblKhachHang.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            // Đang ở "Tất cả" mà bấm chọn một khách hàng -> tự chuyển sang "Khách hàng"
            if (!dangDieuChinh && cmbPhamVi.getSelectedIndex() == 0 && tblKhachHang.getSelectedRow() >= 0) {
                dangDieuChinh = true;
                cmbPhamVi.setSelectedIndex(1);
                dangDieuChinh = false;
                apDungPhamVi(); // mở khóa các ô tìm kiếm, giữ nguyên dòng đang chọn
            }
            lamMoiThongKe();
        });

        // Gõ vào ô tìm kiếm -> lọc ngay
        khiThayDoi(txtMaKH, this::locKhachHang);
        khiThayDoi(txtTenKH, this::locKhachHang);
        khiThayDoi(txtSoDienThoai, this::locKhachHang);

        // Bấm đúp một dòng danh sách -> xem chi tiết
        tblHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblHoaDon.getSelectedRow() >= 0) {
                    xemChiTiet();
                }
            }
        });
    }

    private void khiThayDoi(JTextField txt, Runnable hanhDong) {
        txt.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                hanhDong.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                hanhDong.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                hanhDong.run();
            }
        });
    }

    private void chonTab(int chiSo) {
        tglTab[chiSo].setSelected(true);
        cheDo = chiSo;
        capNhatOThoiGian();
        lamMoiThongKe();
    }

    private void lamMoiBoLoc() {
        LocalDate homNay = LocalDate.now();
        dangDieuChinh = true;
        cmbPhamVi.setSelectedIndex(0);
        cmbNam.setSelectedItem(homNay.getYear());
        cmbThang.setSelectedItem(homNay.getMonthValue());
        dangDieuChinh = false;
        dongBoSoNgay();
        dangDieuChinh = true;
        cmbNgay.setSelectedItem(homNay.getDayOfMonth());
        dangDieuChinh = false;
        apDungPhamVi();
        lamMoiThongKe();
    }

    // BỘ LỌC: THỜI GIAN, PHẠM VI, KHÁCH HÀNG
    private static int layGiaTri(JComboBox<Integer> cmb, int macDinh) {
        Object giaTri = cmb.getSelectedItem();
        return giaTri == null ? macDinh : (Integer) giaTri;
    }

    private JLabel taoLblThoiGian(String chu) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont(font(Font.PLAIN, 12));
        lbl.setForeground(DARK_BLUE);
        return lbl;
    }

    // Hiện đúng ô chọn thời gian theo tab đang chọn
    private void capNhatOThoiGian() {
        if (pnlThoiGian == null) {
            return;
        }
        pnlThoiGian.removeAll();
        if (cheDo == CHE_DO_NGAY) {
            pnlThoiGian.add(taoLblThoiGian("Ngày:"));
            pnlThoiGian.add(cmbNgay);
        }
        if (cheDo <= CHE_DO_THANG) {
            pnlThoiGian.add(taoLblThoiGian("Tháng:"));
            pnlThoiGian.add(cmbThang);
        }
        pnlThoiGian.add(taoLblThoiGian("Năm:"));
        pnlThoiGian.add(cmbNam);
        pnlThoiGian.revalidate();
        pnlThoiGian.repaint();
    }

    // Số ngày trong ô "Ngày" luôn khớp với tháng/năm đang chọn
    private void dongBoSoNgay() {
        int toiDa = YearMonth.of(layGiaTri(cmbNam, LocalDate.now().getYear()), layGiaTri(cmbThang, 1)).lengthOfMonth();
        if (cmbNgay.getItemCount() == toiDa) {
            return;
        }
        int dangChon = Math.min(layGiaTri(cmbNgay, 1), toiDa);
        dangDieuChinh = true;
        cmbNgay.removeAllItems();
        for (int i = 1; i <= toiDa; i++) {
            cmbNgay.addItem(i);
        }
        cmbNgay.setSelectedItem(dangChon);
        dangDieuChinh = false;
    }

    private void datNgay(LocalDate ngay) {
        dangDieuChinh = true;
        cmbNam.setSelectedItem(ngay.getYear());
        cmbThang.setSelectedItem(ngay.getMonthValue());
        dangDieuChinh = false;
        dongBoSoNgay();
        dangDieuChinh = true;
        cmbNgay.setSelectedItem(ngay.getDayOfMonth());
        dangDieuChinh = false;
    }

    private void napBangKhachHang() {
        modelKhachHang.setRowCount(0);
        for (KhachHang kh : dsKhachHang.values()) {
            modelKhachHang.addRow(new Object[] { kh.ma, kh.ten, kh.sdt });
        }
    }

    // "Tất cả": khóa tìm kiếm và bỏ chọn khách hàng. "Khách hàng": mở khóa
    private void apDungPhamVi() {
        boolean theoKH = cmbPhamVi.getSelectedIndex() == 1;
        txtMaKH.setEnabled(theoKH);
        txtTenKH.setEnabled(theoKH);
        txtSoDienThoai.setEnabled(theoKH);
        btnTimKiem.setEnabled(theoKH);
        if (!theoKH) {
            txtMaKH.setText("");
            txtTenKH.setText("");
            txtSoDienThoai.setText("");
            sorterKhachHang.setRowFilter(null);
            tblKhachHang.clearSelection();
        } else {
            chonDongDauNeuChuaChon();
        }
        tblKhachHang.repaint();
    }

    // Đang thống kê theo khách hàng mà chưa có dòng nào được chọn thì chọn dòng đầu
    private void chonDongDauNeuChuaChon() {
        if (cmbPhamVi.getSelectedIndex() == 1
                && tblKhachHang.getSelectedRow() < 0
                && tblKhachHang.getRowCount() > 0) {
            tblKhachHang.setRowSelectionInterval(0, 0);
        }
    }

    // Bỏ dấu tiếng Việt, về chữ thường để tìm kiếm không phân biệt dấu
    private static String chuanHoa(String chu) {
        String s = Normalizer.normalize(chu == null ? "" : chu, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
    }

    private void locKhachHang() {
        final String ma = chuanHoa(txtMaKH.getText());
        final String ten = chuanHoa(txtTenKH.getText());
        final String sdt = chuanHoa(txtSoDienThoai.getText());

        if (ma.isEmpty() && ten.isEmpty() && sdt.isEmpty()) {
            sorterKhachHang.setRowFilter(null);
        } else {
            sorterKhachHang.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                    return chuanHoa(e.getStringValue(0)).contains(ma)
                            && chuanHoa(e.getStringValue(1)).contains(ten)
                            && chuanHoa(e.getStringValue(2)).contains(sdt);
                }
            });
        }
        chonDongDauNeuChuaChon();
    }

    private String maKhachHangDangChon() {
        int dong = tblKhachHang.getSelectedRow();
        if (dong < 0) {
            return null;
        }
        return modelKhachHang.getValueAt(tblKhachHang.convertRowIndexToModel(dong), 0).toString();
    }

    // THỐNG KÊ
    // Tính lại toàn bộ: tổng công nợ, danh sách, biểu đồ
    private void lamMoiThongKe() {
        if (!sanSang) {
            return;
        }
        int nam = layGiaTri(cmbNam, LocalDate.now().getYear());
        int thang = layGiaTri(cmbThang, 1);
        YearMonth ym = YearMonth.of(nam, thang);
        int ngay = Math.min(layGiaTri(cmbNgay, 1), ym.lengthOfMonth());

        // 1. Kỳ thống kê và tiêu đề
        LocalDate batDau, ketThuc;
        String tieuDeDS, tieuDeBD;
        if (cheDo == CHE_DO_NGAY) {
            batDau = ketThuc = ym.atDay(ngay);
            tieuDeDS = "DANH SÁCH HÓA ĐƠN CÓ CÔNG NỢ (" + batDau.format(DMY) + ")";
            tieuDeBD = "";
        } else if (cheDo == CHE_DO_THANG) {
            batDau = ym.atDay(1);
            ketThuc = ym.atEndOfMonth();
            String ky = String.format("%02d/%d", thang, nam);
            tieuDeDS = "CÔNG NỢ THEO THÁNG " + ky;
            tieuDeBD = "TÌNH HÌNH CÔNG NỢ THEO THÁNG " + ky;
        } else if (cheDo == CHE_DO_NAM) {
            batDau = LocalDate.of(nam, 1, 1);
            ketThuc = LocalDate.of(nam, 12, 31);
            tieuDeDS = "CÔNG NỢ THEO NĂM " + nam;
            tieuDeBD = "TÌNH HÌNH CÔNG NỢ THEO NĂM " + nam;
        } else {
            batDau = LocalDate.of(nam, 1, 1);
            ketThuc = LocalDate.of(nam, 12, 31);
            tieuDeDS = "CÔNG NỢ THEO NĂM " + nam;
            tieuDeBD = "TÌNH HÌNH CÔNG NỢ THEO NĂM " + nam;
        }
        lblTieuDeDanhSach.setText(tieuDeDS);
        lblTieuDeBieuDo.setText(tieuDeBD);

        // 2. Phạm vi khách hàng
        boolean theoKH = cmbPhamVi.getSelectedIndex() == 1;
        String maKH = theoKH ? maKhachHangDangChon() : null;
        List<HoaDon> dsLoc = new ArrayList<>();
        for (HoaDon hd : dsHoaDon) {
            if (!theoKH || (maKH != null && maKH.equals(hd.maKH))) {
                dsLoc.add(hd);
            }
        }

        // 3. Tổng công nợ: phát sinh trong kỳ, còn lại đến cuối kỳ
        long phatSinh = 0, conLai = 0;
        for (HoaDon hd : dsLoc) {
            if (!hd.ngay.isBefore(batDau) && !hd.ngay.isAfter(ketThuc)) {
                phatSinh += hd.phatSinh;
            }
            if (!hd.ngay.isAfter(ketThuc)) {
                conLai += hd.conNo;
            }
        }
        lblPhatSinh.setText(formatTien(phatSinh));
        lblConLai.setText(formatTien(conLai));

        // 4. Danh sách
        dongHoaDon.clear();
        dongKhoa.clear();
        modelHoaDon.setRowCount(0);
        if (cheDoCuoi != cheDo) {
            modelHoaDon.setColumnIdentifiers(layTieuDeCot());
            cheDoCuoi = cheDo;
        }
        if (cheDo != CHE_DO_NGAY) {
            datDoRongCot(tblHoaDon, 55, 75, 105, 105);
        }
        long[] ketQua = cheDo == CHE_DO_NGAY ? napBangNgay(dsLoc, batDau) : napBangNhom(dsLoc, batDau, ketThuc);

        if (theoKH && maKH == null) {
            lblSoHoaDon.setText("Hãy chọn một khách hàng trong danh sách.");
        } else {
            lblSoHoaDon.setText("Tổng số hóa đơn có công nợ: " + ketQua[0]);
        }
        lblTongNoKy.setText("Tổng còn nợ trong kỳ: " + formatTien(ketQua[1]));

        // 5. Bố cục các cột và biểu đồ
        capNhatBoCot();
        if (cheDo != CHE_DO_NGAY) {
            capNhatBieuDo(dsLoc, ym, nam);
        }
    }

    private String[] layTieuDeCot() {
        if (cheDo == CHE_DO_NGAY) {
            return COT_NGAY;
        }
        if (cheDo == CHE_DO_THANG) {
            return new String[] { "Ngày", "Số HĐ nợ", "Tổng tiền HĐ", "Tổng tiền nợ" };
        }
        if (cheDo == CHE_DO_NAM) {
            return new String[] { "Tháng", "Số HĐ nợ", "Tổng tiền HĐ", "Tổng tiền nợ" };
        }
        return new String[] { "Tháng", "Số HĐ nợ", "Tổng tiền HĐ", "Tổng tiền nợ" };
    }

    // Tab ngày: liệt kê từng hóa đơn còn nợ. Trả về {số hóa đơn, tổng còn nợ}
    private long[] napBangNgay(List<HoaDon> dsLoc, LocalDate ngay) {
        List<HoaDon> dsNgay = new ArrayList<>();
        for (HoaDon hd : dsLoc) {
            if (hd.ngay.equals(ngay) && hd.conNo > 0) {
                dsNgay.add(hd);
            }
        }
        dsNgay.sort((a, b) -> a.ma.compareTo(b.ma));

        long conNo = 0;
        for (HoaDon hd : dsNgay) {
            modelHoaDon.addRow(new Object[] {
                    hd.ma,
                    tenKhachHang(hd.maKH),
                    sdtKhachHang(hd.maKH),
                    hd.bienSo,
                    formatTien(hd.tong),
                    formatTien(hd.conNo),
                    hd.conNo > 0 ? "Còn nợ" : "Đã tất toán"
            });
            dongHoaDon.add(hd);
            conNo += hd.conNo;
        }
        return new long[] { dsNgay.size(), conNo };
    }

    // Tab tháng / năm: gom theo ngày hoặc tháng. Trả về {số hóa đơn, tổng còn nợ}
    private long[] napBangNhom(List<HoaDon> dsLoc, LocalDate batDau, LocalDate ketThuc) {
        // Mỗi nhóm: {số hóa đơn, tổng tiền, còn nợ}
        TreeMap<LocalDate, long[]> dsNhom = new TreeMap<>();
        for (HoaDon hd : dsLoc) {
            if (hd.ngay.isBefore(batDau) || hd.ngay.isAfter(ketThuc) || hd.phatSinh <= 0) {
                continue;
            }
            LocalDate khoa = cheDo == CHE_DO_THANG ? hd.ngay : hd.ngay.withDayOfMonth(1);
            long[] nhom = dsNhom.computeIfAbsent(khoa, k -> new long[3]);
            nhom[0]++;
            nhom[1] += hd.tong;
            nhom[2] += hd.conNo;
        }

        long soHD = 0, conNo = 0;
        for (Map.Entry<LocalDate, long[]> muc : dsNhom.entrySet()) {
            long[] nhom = muc.getValue();
            String nhan = String.valueOf(cheDo == CHE_DO_THANG
                    ? muc.getKey().getDayOfMonth()
                    : muc.getKey().getMonthValue());
            modelHoaDon.addRow(new Object[] { nhan, (int) nhom[0], formatTien(nhom[1]), formatTien(nhom[2]) });
            dongKhoa.add(muc.getKey());
            soHD += nhom[0];
            conNo += nhom[2];
        }
        return new long[] { soHD, conNo };
    }

    // Tab ngày cho danh sách chiếm luôn chỗ biểu đồ; khung bộ lọc giữ nguyên độ
    // rộng ở cả 3 tab
    private void capNhatBoCot() {
        boolean tabNgay = cheDo == CHE_DO_NGAY;
        GridBagLayout bocuc = (GridBagLayout) pnlCacCot.getLayout();
        GridBagConstraints rang = bocuc.getConstraints(pnlCotDanhSach);
        rang.gridwidth = tabNgay ? 2 : 1;
        rang.weightx = tabNgay ? 0.72 : 0.38;
        rang.insets = tabNgay ? new Insets(0, 5, 0, 0) : new Insets(0, 5, 0, 5);
        bocuc.setConstraints(pnlCotDanhSach, rang);
        pnlCotBieuDo.setVisible(!tabNgay);
        pnlCacCot.revalidate();
        pnlCacCot.repaint();
    }

    // Biểu đồ: theo ngày trong tháng hoặc theo tháng trong năm
    private void capNhatBieuDo(List<HoaDon> dsLoc, YearMonth ym, int nam) {
        int soCot;
        String nhanBieuDo;
        if (cheDo == CHE_DO_THANG) {
            soCot = ym.lengthOfMonth();
            nhanBieuDo = "Ngày";
        } else if (cheDo == CHE_DO_NAM) {
            soCot = 12;
            nhanBieuDo = "Tháng";
        } else {
            soCot = 12;
            nhanBieuDo = "Tháng";
        }
        String[] dsNhan = new String[soCot];
        long[] dsPhatSinh = new long[soCot];
        long[] dsConLai = new long[soCot];

        for (int i = 0; i < soCot; i++) {
            LocalDate tu, den;
            if (cheDo == CHE_DO_THANG) {
                tu = den = ym.atDay(i + 1);
            } else if (cheDo == CHE_DO_NAM) {
                YearMonth thang = YearMonth.of(nam, i + 1);
                tu = thang.atDay(1);
                den = thang.atEndOfMonth();
            } else {
                YearMonth thang = YearMonth.of(nam, i + 1);
                tu = thang.atDay(1);
                den = thang.atEndOfMonth();
            }
            dsNhan[i] = String.valueOf(i + 1);
            for (HoaDon hd : dsLoc) {
                if (!hd.ngay.isBefore(tu) && !hd.ngay.isAfter(den)) {
                    dsPhatSinh[i] += hd.phatSinh;
                }
                if (!hd.ngay.isAfter(den)) {
                    dsConLai[i] += hd.conNo;
                }
            }
        }
        pnlBieuDo.napDuLieu(dsNhan, dsPhatSinh, dsConLai, nhanBieuDo);
    }

    // XEM CHI TIẾT
    // Ngày: xem chi tiết hóa đơn. Tháng/Năm: đi sâu xuống ngày/tháng được chọn
    private void xemChiTiet() {
        int dong = tblHoaDon.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng trong danh sách để xem chi tiết.",
                    "Chưa chọn dòng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cheDo == CHE_DO_NGAY) {
            if (dong < dongHoaDon.size()) {
                hienChiTietHoaDon(dongHoaDon.get(dong));
            }
        } else if (dong < dongKhoa.size()) {
            datNgay(dongKhoa.get(dong));
            chonTab(cheDo == CHE_DO_THANG ? CHE_DO_NGAY : CHE_DO_THANG);
        }
    }

    // Lấy lịch sử giao dịch thanh toán của một hóa đơn
    private List<GiaoDich> taiGiaoDich(String maHoaDon) {
        List<GiaoDich> dsGiaoDich = new ArrayList<>();
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(SQL_GIAO_DICH)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        GiaoDich gd = new GiaoDich();
                        Timestamp thoiGian = rs.getTimestamp("thoiGianGD");
                        gd.thoiGian = thoiGian == null ? "-" : thoiGian.toLocalDateTime().format(GIO_NGAY);
                        gd.phuongThuc = rs.getString("phuongThucThanhToan");
                        gd.soTien = laySo(rs.getBigDecimal("soTienThanhToan"));
                        gd.trangThai = rs.getString("trangThaiGD");
                        dsGiaoDich.add(gd);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải lịch sử thanh toán:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
        return dsGiaoDich;
    }

    private JPanel taoOThongTin(String nhan, String giaTri) {
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(font(Font.PLAIN, 11));
        lblNhan.setForeground(MUTED);

        JLabel lblGiaTri = new JLabel(giaTri == null || giaTri.trim().isEmpty() ? "-" : giaTri);
        lblGiaTri.setFont(font(Font.BOLD, 13));
        lblGiaTri.setForeground(TEXT);

        JPanel pnlO = new JPanel(new BorderLayout(0, 2));
        pnlO.setOpaque(false);
        pnlO.add(lblNhan, BorderLayout.NORTH);
        pnlO.add(lblGiaTri, BorderLayout.CENTER);
        return pnlO;
    }

    private JPanel taoOTien(String nhan, long soTien, Color mau) {
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(font(Font.PLAIN, 12));
        lblNhan.setForeground(DARK_BLUE);

        JLabel lblGiaTri = new JLabel(formatTien(soTien));
        lblGiaTri.setFont(font(Font.BOLD, 16));
        lblGiaTri.setForeground(mau);

        JPanel pnlO = new JPanel(new BorderLayout(0, 4));
        pnlO.setBackground(PALE);
        pnlO.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        pnlO.add(lblNhan, BorderLayout.NORTH);
        pnlO.add(lblGiaTri, BorderLayout.CENTER);
        return pnlO;
    }

    // Khung chi tiết công nợ: 1) Thông tin hóa đơn và khách hàng, 2) Tài chính, 3)
    // Lịch sử thanh toán
    private void hienChiTietHoaDon(HoaDon hd) {
        JDialog winChiTiet = new JDialog(this, "Chi tiết công nợ - " + hd.ma, true);

        // Phần 1: thông tin hóa đơn và khách hàng
        JPanel pnlThongTin = taoPnlThe();
        pnlThongTin.add(taoTieuDe("1. THÔNG TIN HÓA ĐƠN VÀ KHÁCH HÀNG"), BorderLayout.NORTH);
        JPanel pnlLuoi = new JPanel(new GridLayout(0, 2, 24, 10));
        pnlLuoi.setOpaque(false);
        pnlLuoi.add(taoOThongTin("Mã hóa đơn", hd.ma));
        pnlLuoi.add(taoOThongTin("Ngày lập", hd.ngay.format(DMY)));
        pnlLuoi.add(taoOThongTin("Tên khách hàng", tenKhachHang(hd.maKH)));
        pnlLuoi.add(taoOThongTin("Mã khách hàng", hd.maKH));
        pnlLuoi.add(taoOThongTin("Số điện thoại", sdtKhachHang(hd.maKH)));
        pnlLuoi.add(taoOThongTin("Biển số xe", hd.bienSo));
        pnlThongTin.add(pnlLuoi, BorderLayout.CENTER);

        // Phần 2: thông tin tài chính
        long daThanhToan = Math.max(0L, hd.tong - hd.conNo);
        JPanel pnlTaiChinh = taoPnlThe();
        pnlTaiChinh.add(taoTieuDe("2. THÔNG TIN TÀI CHÍNH"), BorderLayout.NORTH);
        JPanel pnlTien = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlTien.setOpaque(false);
        pnlTien.add(taoOTien("Tổng tiền hóa đơn", hd.tong, DARK_BLUE));
        pnlTien.add(taoOTien("Số tiền đã thanh toán", daThanhToan, GREEN));
        pnlTien.add(taoOTien("Số tiền còn nợ", hd.conNo, hd.conNo > 0 ? RED : GREEN));
        pnlTaiChinh.add(pnlTien, BorderLayout.CENTER);

        // Phần 3: lịch sử thanh toán
        List<GiaoDich> dsGiaoDich = taiGiaoDich(hd.ma);
        DefaultTableModel modelGiaoDich = taoModel(
                "STT", "Thời gian thanh toán", "Phương thức", "Số tiền thanh toán", "Trạng thái");
        int stt = 1;
        for (GiaoDich gd : dsGiaoDich) {
            modelGiaoDich.addRow(new Object[] {
                    String.valueOf(stt++), gd.thoiGian,
                    gd.phuongThuc == null ? "-" : gd.phuongThuc, formatTien(gd.soTien),
                    gd.trangThai == null ? "-" : gd.trangThai });
        }

        JPanel pnlLichSu = taoPnlThe();
        pnlLichSu.add(taoTieuDe("3. LỊCH SỬ THANH TOÁN"), BorderLayout.NORTH);
        if (dsGiaoDich.isEmpty()) {
            JLabel lblTrong = new JLabel("Hóa đơn này chưa có giao dịch thanh toán nào.", SwingConstants.CENTER);
            lblTrong.setFont(font(Font.ITALIC, 12));
            lblTrong.setForeground(MUTED);
            lblTrong.setPreferredSize(new Dimension(100, 60));
            pnlLichSu.add(lblTrong, BorderLayout.CENTER);
        } else {
            JTable tblGiaoDich = new JTable(modelGiaoDich);
            caiDatBang(tblGiaoDich);
            tblGiaoDich.setRowHeight(30);
            datDoRongCot(tblGiaoDich, 40, 150, 130, 130, 100);
            JScrollPane scrGiaoDich = taoScr(tblGiaoDich);
            scrGiaoDich.setPreferredSize(new Dimension(100, 170));
            pnlLichSu.add(scrGiaoDich, BorderLayout.CENTER);
        }

        // Ghép các phần
        JPanel pnlNoiDung = new JPanel();
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
        for (JPanel pnl : new JPanel[] { pnlThongTin, pnlTaiChinh, pnlLichSu }) {
            pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (pnlNoiDung.getComponentCount() > 0) {
                pnlNoiDung.add(Box.createVerticalStrut(10));
            }
            pnlNoiDung.add(pnl);
        }

        ActionButton btnDong = new ActionButton("Đóng", false);
        btnDong.addActionListener(e -> winChiTiet.dispose());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnDong);

        JPanel pnlGoc = new JPanel(new BorderLayout(0, 12));
        pnlGoc.setBackground(BG);
        pnlGoc.setBorder(new EmptyBorder(14, 16, 14, 16));
        pnlGoc.add(pnlNoiDung, BorderLayout.CENTER);
        pnlGoc.add(pnlNut, BorderLayout.SOUTH);

        winChiTiet.setContentPane(pnlGoc);
        winChiTiet.setSize(640, 640);
        winChiTiet.setMinimumSize(new Dimension(560, 560));
        winChiTiet.setLocationRelativeTo(this);
        winChiTiet.setVisible(true);
    }

    // XUẤT EXCEL
    // Loại cột khi ghi Excel
    private static final int LOAI_CHU_TRAI = 0;
    private static final int LOAI_CHU_GIUA = 1;
    private static final int LOAI_TIEN = 2;
    private static final int LOAI_SO = 3;

    // Chỉ số kiểu ô trong XLSX_STYLES (cellXfs)
    private static final int KIEU_TIEU_DE = 1;
    private static final int KIEU_CHU_TRAI = 2;
    private static final int KIEU_GIUA = 3;
    private static final int KIEU_TIEN = 4;

    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    private static final String OOXML = "http://schemas.openxmlformats.org/";
    private static final String REL_DOC = OOXML + "officeDocument/2006/relationships";

    // Kiểu dáng: chỉ có viền đen mỏng + chữ in đậm cho tiêu đề, không màu
    private static final String XLSX_STYLES = XML_HEAD
            + "<styleSheet xmlns=\"" + OOXML + "spreadsheetml/2006/main\">"
            + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"#,##0&quot; đ&quot;\"/></numFmts>"
            + "<fonts count=\"2\">"
            + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
            + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
            + "</fonts>"
            + "<fills count=\"2\">"
            + "<fill><patternFill patternType=\"none\"/></fill>"
            + "<fill><patternFill patternType=\"gray125\"/></fill>"
            + "</fills>"
            + "<borders count=\"2\">"
            + "<border><left/><right/><top/><bottom/><diagonal/></border>"
            + "<border><left style=\"thin\"><color auto=\"1\"/></left><right style=\"thin\"><color auto=\"1\"/></right>"
            + "<top style=\"thin\"><color auto=\"1\"/></top><bottom style=\"thin\"><color auto=\"1\"/></bottom><diagonal/></border>"
            + "</borders>"
            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
            + "<cellXfs count=\"5\">"
            // 0 mặc định
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
            // 1 tiêu đề cột: in đậm, giữa, có viền
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
            // 2 chữ căn trái, có viền
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"
            // 3 căn giữa, có viền
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
            // 4 tiền, căn phải, có viền
            + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>"
            + "</cellXfs>"
            + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
            + "</styleSheet>";

    // Xuất danh sách đang hiển thị ra file Excel
    private void xuatExcel() {
        if (modelHoaDon.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất.", "Xuất file Excel",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fch = new JFileChooser();
        fch.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        fch.setSelectedFile(
                new File("CongNo_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx"));
        if (fch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File tep = fch.getSelectedFile();
        if (!tep.getName().toLowerCase().endsWith(".xlsx")) {
            tep = new File(tep.getParentFile(), tep.getName() + ".xlsx");
        }
        if (tep.exists() && JOptionPane.showConfirmDialog(this,
                "File đã tồn tại. Bạn có muốn ghi đè không?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ghiXlsx(tep);
            JOptionPane.showMessageDialog(this, "Đã xuất file thành công:\n" + tep.getAbsolutePath(),
                    "Xuất file Excel", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể ghi file (file có đang mở trong Excel không?):\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Xác định loại cột theo tên cột và nội dung
    private int xacDinhLoaiCot(int cot) {
        String ten = modelHoaDon.getColumnName(cot);
        if ("Số HĐ nợ".equals(ten)) {
            return LOAI_SO;
        }
        for (int dong = 0; dong < modelHoaDon.getRowCount(); dong++) {
            Object giaTri = modelHoaDon.getValueAt(dong, cot);
            if (giaTri instanceof String && ((String) giaTri).endsWith(" đ")) {
                return LOAI_TIEN;
            }
        }
        if ("Ngày".equals(ten) || "Tháng".equals(ten) || "Trạng thái".equals(ten)
                || "Mã hóa đơn".equals(ten) || "Biển số xe".equals(ten) || "Số điện thoại".equals(ten)) {
            return LOAI_CHU_GIUA;
        }
        return LOAI_CHU_TRAI;
    }

    // Đọc số tiền từ chuỗi "1.234.000 đ"
    private static long docTien(Object giaTri) {
        String s = giaTri == null ? "" : giaTri.toString().replaceAll("[^0-9-]", "");
        if (s.isEmpty() || s.equals("-")) {
            return 0L;
        }
        return Long.parseLong(s);
    }

    // Số cột -> tên cột Excel (0 -> A, 26 -> AA)
    private static String tenCotExcel(int cot) {
        StringBuilder s = new StringBuilder();
        for (cot++; cot > 0; cot = (cot - 1) / 26) {
            s.insert(0, (char) ('A' + (cot - 1) % 26));
        }
        return s.toString();
    }

    private static String thoatXml(String chu) {
        if (chu == null) {
            return "";
        }
        return chu.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static void ghiOChu(StringBuilder sb, int cot, int dong, String chu, int kieu) {
        sb.append("<c r=\"").append(tenCotExcel(cot)).append(dong).append("\" s=\"").append(kieu)
                .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(thoatXml(chu))
                .append("</t></is></c>");
    }

    private static void ghiOSo(StringBuilder sb, int cot, int dong, long so, int kieu) {
        sb.append("<c r=\"").append(tenCotExcel(cot)).append(dong).append("\" s=\"").append(kieu)
                .append("\"><v>").append(so).append("</v></c>");
    }

    private static void ghiZip(ZipOutputStream zos, String ten, String noiDung) throws IOException {
        zos.putNextEntry(new ZipEntry(ten));
        zos.write(noiDung.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    // Bảng đơn giản bắt đầu từ ô A1: cột STT + các cột đang hiển thị, có viền, tiền
    // lưu dạng số
    private void ghiXlsx(File tep) throws IOException {
        final int soCotNguon = modelHoaDon.getColumnCount();
        final int soDong = modelHoaDon.getRowCount();
        final int soCot = soCotNguon + 1; // thêm cột STT

        String[] dsTieuDe = new String[soCot];
        int[] dsLoai = new int[soCot];
        int[] dsRong = new int[soCot];
        dsTieuDe[0] = "STT";
        dsLoai[0] = LOAI_CHU_GIUA;
        for (int c = 0; c < soCotNguon; c++) {
            dsTieuDe[c + 1] = modelHoaDon.getColumnName(c);
            dsLoai[c + 1] = xacDinhLoaiCot(c);
        }
        for (int c = 0; c < soCot; c++) {
            dsRong[c] = dsTieuDe[c].length() + 4;
        }
        dsRong[0] = 7;

        StringBuilder dsDongXml = new StringBuilder();

        // Dòng tiêu đề (dòng 1)
        dsDongXml.append("<row r=\"1\">");
        for (int c = 0; c < soCot; c++) {
            ghiOChu(dsDongXml, c, 1, dsTieuDe[c], KIEU_TIEU_DE);
        }
        dsDongXml.append("</row>");

        // Dữ liệu
        for (int d = 0; d < soDong; d++) {
            int dongExcel = 2 + d;
            dsDongXml.append("<row r=\"").append(dongExcel).append("\">");
            ghiOSo(dsDongXml, 0, dongExcel, d + 1, KIEU_GIUA);
            for (int c = 1; c < soCot; c++) {
                Object giaTri = modelHoaDon.getValueAt(d, c - 1);
                String chu = giaTri == null ? "" : giaTri.toString();
                int doDai = chu.length();
                if (dsLoai[c] == LOAI_TIEN) {
                    long tien = docTien(giaTri);
                    ghiOSo(dsDongXml, c, dongExcel, tien, KIEU_TIEN);
                    doDai = MONEY.format(tien).length() + 4;
                } else if (dsLoai[c] == LOAI_SO) {
                    long so = giaTri instanceof Number ? ((Number) giaTri).longValue() : docTien(giaTri);
                    ghiOSo(dsDongXml, c, dongExcel, so, KIEU_GIUA);
                } else {
                    ghiOChu(dsDongXml, c, dongExcel, chu,
                            dsLoai[c] == LOAI_CHU_GIUA ? KIEU_GIUA : KIEU_CHU_TRAI);
                }
                dsRong[c] = Math.max(dsRong[c], Math.min(doDai + 3, 45));
            }
            dsDongXml.append("</row>");
        }

        // Sheet: giữ đường kẻ ô mặc định của Excel, cố định dòng tiêu đề
        StringBuilder sheet = new StringBuilder();
        sheet.append(XML_HEAD)
                .append("<worksheet xmlns=\"").append(OOXML).append("spreadsheetml/2006/main\">")
                .append("<sheetViews><sheetView workbookViewId=\"0\">")
                .append("<pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/>")
                .append("</sheetView></sheetViews>")
                .append("<sheetFormatPr defaultRowHeight=\"15\"/><cols>");
        for (int c = 0; c < soCot; c++) {
            sheet.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                    .append("\" width=\"").append(dsRong[c]).append("\" customWidth=\"1\"/>");
        }
        sheet.append("</cols><sheetData>").append(dsDongXml).append("</sheetData></worksheet>");

        // Đóng gói các phần của file .xlsx (thực chất là file zip chứa XML)
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tep))) {
            ghiZip(zos, "[Content_Types].xml", XML_HEAD
                    + "<Types xmlns=\"" + OOXML + "package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                    + "</Types>");
            ghiZip(zos, "_rels/.rels", XML_HEAD
                    + "<Relationships xmlns=\"" + OOXML + "package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"" + REL_DOC + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            ghiZip(zos, "xl/workbook.xml", XML_HEAD
                    + "<workbook xmlns=\"" + OOXML + "spreadsheetml/2006/main\" xmlns:r=\"" + REL_DOC + "\">"
                    + "<sheets><sheet name=\"Công nợ\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            ghiZip(zos, "xl/_rels/workbook.xml.rels", XML_HEAD
                    + "<Relationships xmlns=\"" + OOXML + "package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"" + REL_DOC + "/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "<Relationship Id=\"rId2\" Type=\"" + REL_DOC + "/styles\" Target=\"styles.xml\"/>"
                    + "</Relationships>");
            ghiZip(zos, "xl/styles.xml", XLSX_STYLES);
            ghiZip(zos, "xl/worksheets/sheet1.xml", sheet.toString());
        }
    }

    // TIỆN ÍCH GIAO DIỆN
    private static Font font(int kieu, int coChu) {
        return new Font("Segoe UI", kieu, coChu);
    }

    private static String formatTien(long soTien) {
        return MONEY.format(soTien) + " đ";
    }

    // Model bảng không cho sửa ô
    private static DefaultTableModel taoModel(String... dsCot) {
        return new DefaultTableModel(dsCot, 0) {
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return false;
            }
        };
    }

    private static JPanel taoPnlThe() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(12, 12, 12, 12)));
        return pnl;
    }

    private static JLabel taoTieuDe(String chu) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont(font(Font.BOLD, 14));
        lbl.setForeground(DARK_BLUE);
        return lbl;
    }

    private static JLabel taoTieuMuc(String chu) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont(font(Font.BOLD, 13));
        lbl.setForeground(DARK_BLUE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private static JComponent codinh(JComponent c, int cao) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, cao));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
        return c;
    }

    private static void themDuongKe(JPanel pnlCha) {
        JSeparator sprKe = new JSeparator();
        sprKe.setForeground(BORDER);
        sprKe.setAlignmentX(Component.LEFT_ALIGNMENT);
        sprKe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        pnlCha.add(Box.createVerticalStrut(8));
        pnlCha.add(sprKe);
        pnlCha.add(Box.createVerticalStrut(8));
    }

    private static JScrollPane taoScr(JTable tbl) {
        JScrollPane scr = new JScrollPane(tbl);
        scr.setBorder(new LineBorder(BORDER));
        scr.getViewport().setBackground(Color.WHITE);
        return scr;
    }

    private static void datDoRongCot(JTable tbl, int... dsRong) {
        for (int i = 0; i < dsRong.length; i++) {
            tbl.getColumnModel().getColumn(i).setPreferredWidth(dsRong[i]);
        }
    }

    private static void caiDatBang(JTable tbl) {
        tbl.setFont(font(Font.PLAIN, 12));
        tbl.setRowHeight(32);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(GRID);
        tbl.setSelectionBackground(SELECT_BG);
        tbl.setSelectionForeground(DARK_BLUE);
        tbl.setFillsViewportHeight(true);
        tbl.setDefaultRenderer(Object.class, new CellRenderer());
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 34));
        tbl.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object giaTri, boolean dangChon,
                    boolean coFocus, int dong, int cot) {
                super.getTableCellRendererComponent(t, giaTri, false, false, dong, cot);
                setBackground(HEADER_BG);
                setForeground(DARK_BLUE);
                setFont(font(Font.BOLD, 12));
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 1, BORDER), new EmptyBorder(0, 6, 0, 6)));
                return this;
            }
        });
    }

    private static boolean laCotNo(String ten) {
        return "Còn nợ".equals(ten) || "Tổng tiền nợ".equals(ten) || "Công nợ hiện tại".equals(ten);
    }

    // LỚP GIAO DIỆN TỰ VẼ
    // Hiển thị ô bảng: số tiền căn phải, nợ tô đỏ, dòng xen kẽ màu
    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object giaTri, boolean dangChon,
                boolean coFocus, int dong, int cot) {
            super.getTableCellRendererComponent(tbl, giaTri, dangChon, false, dong, cot);
            String chu = giaTri == null ? "" : giaTri.toString();
            boolean cotNo = laCotNo(tbl.getColumnName(cot));
            setBorder(new EmptyBorder(0, 8, 0, 8));

            if (giaTri instanceof Number) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else if (chu.endsWith(" đ")) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            setFont(font(cotNo ? Font.BOLD : Font.PLAIN, 12));

            Color mauChu;
            if (!tbl.isEnabled()) {
                mauChu = DISABLED;
            } else if (dangChon) {
                mauChu = DARK_BLUE;
            } else if (cotNo && !chu.equals("0 đ")) {
                mauChu = DEBT_RED;
            } else {
                mauChu = TEXT;
            }
            setForeground(mauChu);
            setBackground(dangChon && tbl.isEnabled() ? tbl.getSelectionBackground()
                    : (dong % 2 == 0 ? Color.WHITE : ZEBRA));
            return this;
        }
    }

    // Nút tab tự vẽ: tab đang chọn tô xanh, tab còn lại nền nhạt
    private static class TabButton extends JToggleButton {
        TabButton(String chu) {
            super(chu);
            setFont(font(Font.BOLD, 13));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(DARK_BLUE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(130, 34));
            addItemListener(e -> setForeground(isSelected() ? Color.WHITE : DARK_BLUE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int rong = getWidth(), cao = getHeight();
            if (isSelected()) {
                g2.setColor(BLUE);
                g2.fillRoundRect(0, 0, rong, cao + 12, 12, 12);
            } else {
                g2.setColor(getModel().isRollover() ? new Color(220, 237, 255) : new Color(234, 244, 255));
                g2.fillRoundRect(0, 0, rong, cao + 12, 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, rong - 1, cao + 12, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Nút bo góc
    private static class ActionButton extends JButton {
        private final boolean chinh;

        ActionButton(String chu, boolean chinh) {
            super(chu);
            this.chinh = chinh;
            setFont(font(Font.PLAIN, 12));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 12, 9, 12));
            setForeground(chinh ? Color.WHITE : DARK_BLUE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color mauNen;
            if (!isEnabled()) {
                mauNen = new Color(225, 230, 238);
            } else if (chinh) {
                mauNen = getModel().isPressed() ? DARK_BLUE
                        : getModel().isRollover() ? new Color(24, 105, 205) : BLUE;
            } else {
                mauNen = getModel().isRollover() ? new Color(232, 243, 255) : Color.WHITE;
            }
            g2.setColor(mauNen);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            if (!chinh) {
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Biểu đồ cột: công nợ phát sinh (xanh dương) và công nợ còn lại (xanh lá)
    private static class BieuDoCongNo extends JPanel {
        private static final int LE_TRAI = 64, LE_PHAI = 14, LE_TREN = 44, LE_DUOI = 50;

        private String[] dsNhan = new String[0];
        private long[] dsPhatSinh = new long[0], dsConLai = new long[0];
        private String tenTrucX = "";

        BieuDoCongNo() {
            setBackground(Color.WHITE);
            setToolTipText("");
        }

        void napDuLieu(String[] dsNhan, long[] dsPhatSinh, long[] dsConLai, String tenTrucX) {
            this.dsNhan = dsNhan;
            this.dsPhatSinh = dsPhatSinh;
            this.dsConLai = dsConLai;
            this.tenTrucX = tenTrucX;
            repaint();
        }

        // Bước chia trục Y "đẹp" (1, 2, 2.5, 5, 10 x lũy thừa 10)
        private long buocChia() {
            long lonNhat = 0;
            for (long v : dsPhatSinh) {
                lonNhat = Math.max(lonNhat, v);
            }
            for (long v : dsConLai) {
                lonNhat = Math.max(lonNhat, v);
            }
            double tho = lonNhat <= 0 ? 1_000_000 : lonNhat / 5.0;
            double luyThua = Math.pow(10, Math.floor(Math.log10(tho)));
            double chuan = tho / luyThua;
            double dep = chuan <= 1 ? 1 : chuan <= 2 ? 2 : chuan <= 2.5 ? 2.5 : chuan <= 5 ? 5 : 10;
            return Math.round(dep * luyThua);
        }

        // Rút gọn nhãn trục Y: 2500000 -> "2,5 tr"
        private String rutGon(long v) {
            if (v == 0) {
                return "0";
            }
            if (v >= 1_000_000) {
                double trieu = v / 1_000_000.0;
                String chu = trieu == Math.floor(trieu) ? String.valueOf((long) trieu)
                        : String.format(Locale.forLanguageTag("vi-VN"), "%.1f", trieu);
                return chu + " tr";
            }
            return MONEY.format(v);
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int rong = getWidth() - LE_TRAI - LE_PHAI;
            if (dsNhan.length == 0 || rong <= 0 || e.getX() < LE_TRAI || e.getX() > LE_TRAI + rong) {
                return null;
            }
            int i = (int) ((e.getX() - LE_TRAI) / ((double) rong / dsNhan.length));
            if (i < 0 || i >= dsNhan.length) {
                return null;
            }
            return "<html><b>" + tenTrucX + " " + dsNhan[i] + "</b><br>Phát sinh: " + formatTien(dsPhatSinh[i])
                    + "<br>Còn lại: " + formatTien(dsConLai[i]) + "</html>";
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int rong = getWidth() - LE_TRAI - LE_PHAI;
            int cao = getHeight() - LE_TREN - LE_DUOI;
            if (rong <= 0 || cao <= 0) {
                g2.dispose();
                return;
            }
            long buoc = buocChia();
            long yToiDa = buoc * 5;

            // Chú thích: góc trên bên trái của biểu đồ
            g2.setFont(font(Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int x = LE_TRAI, y = 18;
            g2.setColor(BLUE);
            g2.fillRoundRect(x, y - 10, 12, 12, 3, 3);
            g2.setColor(DARK_BLUE);
            String chuPhatSinh = "Công nợ phát sinh";
            g2.drawString(chuPhatSinh, x + 18, y);
            x += 18 + fm.stringWidth(chuPhatSinh) + 24;
            g2.setColor(GREEN);
            g2.fillRoundRect(x, y - 10, 12, 12, 3, 3);
            g2.setColor(DARK_BLUE);
            g2.drawString("Công nợ còn lại", x + 18, y);

            // Lưới ngang + nhãn trục Y
            g2.setFont(font(Font.PLAIN, 11));
            fm = g2.getFontMetrics();
            for (int i = 0; i <= 5; i++) {
                int yLuoi = LE_TREN + cao - (cao * i / 5);
                g2.setColor(new Color(221, 234, 250));
                g2.drawLine(LE_TRAI, yLuoi, LE_TRAI + rong, yLuoi);
                g2.setColor(DARK_BLUE);
                String nhan = rutGon(buoc * i);
                g2.drawString(nhan, LE_TRAI - 8 - fm.stringWidth(nhan), yLuoi + 4);
            }

            // Các cột
            int soCot = dsNhan.length;
            boolean khongCoDuLieu = true;
            if (soCot > 0) {
                double oCot = (double) rong / soCot;
                int beRongCot = (int) Math.max(2, Math.min(18, oCot / 3));
                int buocNhan = oCot >= 26 ? 1 : oCot >= 14 ? 2 : 5;
                for (int i = 0; i < soCot; i++) {
                    if (dsPhatSinh[i] > 0 || dsConLai[i] > 0) {
                        khongCoDuLieu = false;
                    }
                    double tam = LE_TRAI + oCot * i + oCot / 2;
                    int cao1 = (int) (cao * (double) dsPhatSinh[i] / yToiDa);
                    int cao2 = (int) (cao * (double) dsConLai[i] / yToiDa);
                    g2.setColor(BLUE);
                    g2.fillRect((int) tam - beRongCot - 1, LE_TREN + cao - cao1, beRongCot, cao1);
                    g2.setColor(GREEN);
                    g2.fillRect((int) tam + 1, LE_TREN + cao - cao2, beRongCot, cao2);
                    if (i % buocNhan == 0) {
                        g2.setColor(DARK_BLUE);
                        g2.drawString(dsNhan[i], (int) tam - fm.stringWidth(dsNhan[i]) / 2, LE_TREN + cao + 16);
                    }
                }
            }
            if (khongCoDuLieu) {
                g2.setFont(font(Font.ITALIC, 13));
                g2.setColor(MUTED);
                String chu = "Không có công nợ trong kỳ";
                g2.drawString(chu, LE_TRAI + (rong - g2.getFontMetrics().stringWidth(chu)) / 2, LE_TREN + cao / 2);
            }

            // Tên trục
            g2.setColor(DARK_BLUE);
            g2.setFont(font(Font.BOLD, 12));
            fm = g2.getFontMetrics();
            g2.drawString(tenTrucX, LE_TRAI + (rong - fm.stringWidth(tenTrucX)) / 2, getHeight() - 12);
            String tenTrucY = "Số tiền (VNĐ)";
            g2.rotate(-Math.PI / 2);
            g2.drawString(tenTrucY, -(LE_TREN + cao / 2 + fm.stringWidth(tenTrucY) / 2), 14);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new FrmThongKeCongNo().setVisible(true);
        });
    }
}
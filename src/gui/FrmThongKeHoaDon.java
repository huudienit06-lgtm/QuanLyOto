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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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

public class FrmThongKeHoaDon extends JFrame {
    private static final Color BG = new Color(244, 249, 255);
    private static final Color BLUE = new Color(35, 128, 238);
    private static final Color DARK_BLUE = new Color(18, 58, 119);
    private static final Color BORDER = new Color(196, 220, 250);
    private static final Color GREEN = new Color(48, 174, 125);
    private static final Color TEXT = new Color(40, 52, 75);
    private static final Color MUTED = new Color(110, 125, 150);
    private static final Color DEBT_RED = new Color(214, 48, 49);
    private static final Color ORANGE = new Color(217, 119, 6);
    private static final Color PALE = new Color(246, 250, 255);
    private static final Color ZEBRA = new Color(247, 251, 255);
    private static final Color GRID = new Color(225, 236, 250);
    private static final Color SELECT_BG = new Color(207, 230, 255);
    private static final Color HEADER_BG = new Color(232, 243, 255);
    private static final Color DISABLED = new Color(160, 168, 180);
    // Định dạng
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    // Chế độ thống kê
    private static final int CHE_DO_NGAY = 0;
    private static final int CHE_DO_THANG = 1;
    private static final int CHE_DO_NAM = 2;
    // Tiêu đề cột bảng danh sách ở chế độ ngày
    private static final String[] COT_NGAY = { "Mã hóa đơn", "Nhân viên", "Khách hàng", "Biển số xe",
            "Tổng tiền hóa đơn", "Trạng thái thanh toán" };
    // Câu lệnh SQL
    // Nhân viên có lập hóa đơn
    private static final String SQL_NHAN_VIEN = "SELECT DISTINCT maNV " +
            "FROM HoaDon " +
            "WHERE maNV IS NOT NULL";
    // Hóa đơn kèm khách hàng và tổng tiền đã thu thành công
    private static final String SQL_HOA_DON = "SELECT hd.maHoaDon, hd.ngayLap, hd.maNV, " +
            "       hd.soTien, " +
            "       kh.hoTen AS tenKhachHang, " +
            "       COALESCE(tt.tongDaThu, 0) AS tongDaThu " +
            "FROM HoaDon hd " +
            "LEFT JOIN PhieuSuaChua psc " +
            "       ON hd.maPhieuSuaChua = psc.maPhieuSuaChua " +
            "LEFT JOIN KhachHang kh " +
            "       ON psc.maKH = kh.maKH " +
            "LEFT JOIN ( " +
            "    SELECT gd.maHoaDon, " +
            "           SUM(gd.soTienThanhToan) AS tongDaThu " +
            "    FROM GiaoDichThanhToan gd " +
            "    WHERE gd.trangThaiGD = N'Thành công' " +
            "    GROUP BY gd.maHoaDon " +
            ") tt ON hd.maHoaDon = tt.maHoaDon " +
            "ORDER BY hd.ngayLap DESC";

    // LỚP DỮ LIỆU
    private static class NhanVien {
        final String ma, ten;

        NhanVien(String ma, String ten) {
            this.ma = ma;
            this.ten = ten;
        }
    }

    private static class HoaDon {
        final String ma, maNV, khachHang, bienSo;
        final LocalDate ngay;
        final LocalTime gio;
        final long tong, daThu;

        HoaDon(String ma, LocalDate ngay, LocalTime gio, String maNV, String khachHang, String bienSo,
                long tong, long daThu) {
            this.ma = ma;
            this.ngay = ngay;
            this.gio = gio;
            this.maNV = maNV;
            this.khachHang = khachHang;
            this.bienSo = bienSo;
            this.tong = tong;
            this.daThu = daThu;
        }

        long conPhaiThu() {
            return Math.max(0L, tong - daThu);
        }

        String trangThai() {
            if (daThu <= 0) {
                return "Chưa thanh toán";
            }
            if (daThu < tong) {
                return "Thanh toán một phần";
            }
            return "Đã thanh toán";
        }
    }

    // DỮ LIỆU VÀ TRẠNG THÁI
    private final Map<String, NhanVien> dsNhanVien = new LinkedHashMap<>();
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
    private JLabel lblTongSoHoaDon, lblTongTienHoaDon;

    // Khung bộ lọc
    private JComboBox<String> cmbPhamVi;
    private JTextField txtMaNV, txtTenNV;
    private ActionButton btnTimKiem;
    private JTable tblNhanVien;
    private DefaultTableModel modelNhanVien;
    private TableRowSorter<DefaultTableModel> sorterNhanVien;

    // Khung danh sách và biểu đồ
    private JPanel pnlCacCot, pnlCotDanhSach, pnlCotBieuDo;
    private JTable tblThongKe;
    private DefaultTableModel modelThongKe;
    private JLabel lblTieuDeDanhSach, lblTieuDeBieuDo, lblGhiChu;
    private BieuDoDoanhThu pnlBieuDo;

    // KHỞI TẠO
    public FrmThongKeHoaDon() {
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
                    "Không thể tải dữ liệu hóa đơn từ CSDL:\n" + ex.getMessage(),
                    "Lỗi kết nối CSDL", JOptionPane.ERROR_MESSAGE);
        }

        add(dungTieuDe(), BorderLayout.NORTH);
        add(dungKhungChinh(), BorderLayout.CENTER);
        ganSuKien();

        napBangNhanVien();
        apDungPhamVi();
        dongBoSoNgay();
        sanSang = true;
        lamMoiThongKe();
    }

    // TẢI DỮ LIỆU TỪ SQL SERVER
    private void taiDuLieu() throws SQLException {
        dsNhanVien.clear();
        dsHoaDon.clear();

        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối SQL Server.");
        }

        // 1. Nhân viên có lập hóa đơn (chưa có bảng nhân viên nên tên tạm lấy theo mã)
        try (PreparedStatement ps = con.prepareStatement(SQL_NHAN_VIEN); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maNV");
                dsNhanVien.put(ma, new NhanVien(ma, ma));
            }
        }

        // 2. Hóa đơn, khách hàng và số tiền đã thu
        try (PreparedStatement ps = con.prepareStatement(SQL_HOA_DON); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ngayLap = rs.getTimestamp("ngayLap");
                if (ngayLap == null) {
                    continue;
                }
                LocalDateTime thoiGian = ngayLap.toLocalDateTime();
                String tenKH = rs.getString("tenKhachHang");
                long tong = laySo(rs.getBigDecimal("soTien"));
                long daThu = Math.max(0L, Math.min(laySo(rs.getBigDecimal("tongDaThu")), tong));

                dsHoaDon.add(new HoaDon(
                        rs.getString("maHoaDon"),
                        thoiGian.toLocalDate(),
                        thoiGian.toLocalTime(),
                        rs.getString("maNV"),
                        tenKH == null ? "Chưa có thông tin khách hàng" : tenKH,
                        "Chưa xác định",
                        tong, daThu));
            }
        }
    }

    private static long laySo(BigDecimal so) {
        return so == null ? 0L : so.longValue();
    }

    private String tenNhanVien(String ma) {
        NhanVien nv = dsNhanVien.get(ma);
        return nv == null ? ma : nv.ten;
    }

    // DỰNG GIAO DIỆN CHÍNH
    private JPanel dungTieuDe() {
        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setBackground(BG);
        pnlTieuDe.setBorder(new EmptyBorder(10, 16, 0, 16));

        JLabel lblTieuDe = new JLabel("THỐNG KÊ HÓA ĐƠN", SwingConstants.CENTER);
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
        c.weightx = 0.27;
        c.insets = new Insets(0, 0, 0, 5);
        pnlCacCot.add(pnlBoLoc, c);
        c.gridx = 1;
        c.weightx = 0.36;
        c.insets = new Insets(0, 5, 0, 5);
        pnlCacCot.add(pnlCotDanhSach, c);
        c.gridx = 2;
        c.weightx = 0.37;
        c.insets = new Insets(0, 5, 0, 0);
        pnlCacCot.add(pnlCotBieuDo, c);

        pnlChinh.add(pnlCacCot, BorderLayout.CENTER);
        return pnlChinh;
    }

    // Hàng trên cùng: thời gian thống kê + tổng số hóa đơn + tổng tiền hóa đơn
    private JPanel dungThanhThoiGian() {
        JPanel pnlThanh = new JPanel(new BorderLayout(16, 0));
        pnlThanh.setBackground(Color.WHITE);
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

        lblTongSoHoaDon = taoLblSoLieu(BLUE);
        lblTongTienHoaDon = taoLblSoLieu(GREEN);
        JPanel pnlSoLieu = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlSoLieu.setOpaque(false);
        pnlSoLieu.add(taoOThongKe("Tổng số hóa đơn", BLUE, lblTongSoHoaDon));
        pnlSoLieu.add(taoOThongKe("Tổng tiền hóa đơn", GREEN, lblTongTienHoaDon));

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

        // Thống kê theo (ô chọn cùng hàng với tiêu đề)
        cmbPhamVi = new JComboBox<>(new String[] { "Tất cả", "Nhân viên" });
        JPanel pnlPhamVi = new JPanel(new BorderLayout(10, 0));
        pnlPhamVi.setOpaque(false);
        pnlPhamVi.add(taoTieuMuc("Thống kê theo:"), BorderLayout.WEST);
        pnlPhamVi.add(cmbPhamVi, BorderLayout.CENTER);
        pnlTren.add(codinh(pnlPhamVi, 32));
        pnlTren.add(Box.createVerticalStrut(4));
        themDuongKe(pnlTren);

        // Tìm kiếm nhân viên
        pnlTren.add(taoTieuMuc("Tìm kiếm nhân viên"));
        pnlTren.add(Box.createVerticalStrut(6));
        txtMaNV = new JTextField();
        txtTenNV = new JTextField();
        themOTimKiem(pnlTren, "Mã NV:", txtMaNV);
        themOTimKiem(pnlTren, "Tên NV:", txtTenNV);

        btnTimKiem = new ActionButton("Tìm kiếm", true);
        btnTimKiem.addActionListener(e -> locNhanVien());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnTimKiem);
        pnlTren.add(Box.createVerticalStrut(2));
        pnlTren.add(codinh(pnlNut, 36));
        themDuongKe(pnlTren);

        // Danh sách nhân viên
        modelNhanVien = taoModel("Mã NV", "Tên nhân viên", "Số hóa đơn");
        tblNhanVien = new JTable(modelNhanVien);
        caiDatBang(tblNhanVien);
        tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorterNhanVien = new TableRowSorter<>(modelNhanVien);
        tblNhanVien.setRowSorter(sorterNhanVien);
        datDoRongCot(tblNhanVien, 70, 150, 90);

        JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 6));
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.add(taoTieuMuc("Danh sách nhân viên"), BorderLayout.NORTH);
        pnlDanhSach.add(taoScr(tblNhanVien), BorderLayout.CENTER);

        pnlBoLoc.add(pnlTren, BorderLayout.NORTH);
        pnlBoLoc.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlBoLoc;
    }

    private void themOTimKiem(JPanel pnlCha, String nhan, JTextField txt) {
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(font(Font.PLAIN, 12));
        lblNhan.setForeground(DARK_BLUE);
        lblNhan.setPreferredSize(new Dimension(70, 30));

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

        JPanel pnlO = new JPanel(new BorderLayout(14, 0));
        pnlO.setBackground(PALE);
        pnlO.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(5, 10, 5, 10)));
        pnlO.add(lblTieuDe, BorderLayout.WEST);
        pnlO.add(lblGiaTri, BorderLayout.EAST);
        return pnlO;
    }

    private JLabel taoLblSoLieu(Color mau) {
        JLabel lbl = new JLabel("0", SwingConstants.RIGHT);
        lbl.setFont(font(Font.BOLD, 15));
        lbl.setForeground(mau);
        return lbl;
    }

    // Khung 2: danh sách (tiêu đề và các nút nằm cùng một hàng)
    private JPanel dungKhungDanhSach() {
        JPanel pnlDanhSach = taoPnlThe();

        ActionButton btnChiTiet = new ActionButton("Xem chi tiết", false);
        ActionButton btnXuatExcel = new ActionButton("Xuất Excel", true);
        btnChiTiet.addActionListener(e -> xemChiTiet());
        btnXuatExcel.addActionListener(e -> xuatExcel());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnChiTiet);
        pnlNut.add(btnXuatExcel);

        lblTieuDeDanhSach = taoTieuDe("DANH SÁCH HÓA ĐƠN");
        JPanel pnlTieuDe = new JPanel(new BorderLayout(8, 0));
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(lblTieuDeDanhSach, BorderLayout.CENTER);
        pnlTieuDe.add(pnlNut, BorderLayout.EAST);
        pnlDanhSach.add(pnlTieuDe, BorderLayout.NORTH);

        modelThongKe = taoModel();
        tblThongKe = new JTable(modelThongKe);
        caiDatBang(tblThongKe);
        tblThongKe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlDanhSach.add(taoScr(tblThongKe), BorderLayout.CENTER);

        // Ghi chú phía dưới bảng
        lblGhiChu = new JLabel(" ");
        lblGhiChu.setFont(font(Font.ITALIC, 11));
        lblGhiChu.setForeground(MUTED);
        pnlDanhSach.add(lblGhiChu, BorderLayout.SOUTH);
        return pnlDanhSach;
    }

    // Khung 3: biểu đồ
    private JPanel dungKhungBieuDo() {
        JPanel pnlKhung = taoPnlThe();
        lblTieuDeBieuDo = taoTieuDe("BIỂU ĐỒ THỐNG KÊ DOANH THU");
        pnlBieuDo = new BieuDoDoanhThu();
        pnlKhung.add(lblTieuDeBieuDo, BorderLayout.NORTH);
        pnlKhung.add(pnlBieuDo, BorderLayout.CENTER);
        return pnlKhung;
    }

    // SỰ KIỆN
    private void ganSuKien() {
        // Chọn ngày / tháng / năm
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

        // Đổi phạm vi thống kê (Tất cả / Nhân viên)
        cmbPhamVi.addActionListener(e -> {
            if (dangDieuChinh) {
                return;
            }
            apDungPhamVi();
            lamMoiThongKe();
        });

        // Chọn một nhân viên trong danh sách
        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                lamMoiThongKe();
            }
        });

        // Gõ vào ô tìm kiếm -> lọc ngay
        khiThayDoi(txtMaNV, this::locNhanVien);
        khiThayDoi(txtTenNV, this::locNhanVien);

        // Bấm đúp một dòng danh sách -> xem chi tiết
        tblThongKe.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblThongKe.getSelectedRow() >= 0) {
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

    // BỘ LỌC: THỜI GIAN, PHẠM VI, NHÂN VIÊN
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

    // Danh sách nhân viên kèm số hóa đơn đã lập
    private void napBangNhanVien() {
        Map<String, Integer> soHoaDon = new HashMap<>();
        for (HoaDon hd : dsHoaDon) {
            if (hd.maNV != null) {
                soHoaDon.merge(hd.maNV, 1, Integer::sum);
            }
        }
        modelNhanVien.setRowCount(0);
        for (NhanVien nv : dsNhanVien.values()) {
            modelNhanVien.addRow(new Object[] { nv.ma, nv.ten, soHoaDon.getOrDefault(nv.ma, 0) });
        }
    }

    // "Tất cả": khóa tìm kiếm và danh sách nhân viên. "Nhân viên": mở khóa
    private void apDungPhamVi() {
        boolean theoNV = cmbPhamVi.getSelectedIndex() == 1;
        txtMaNV.setEnabled(theoNV);
        txtTenNV.setEnabled(theoNV);
        btnTimKiem.setEnabled(theoNV);
        tblNhanVien.setEnabled(theoNV);
        if (!theoNV) {
            txtMaNV.setText("");
            txtTenNV.setText("");
            sorterNhanVien.setRowFilter(null);
            tblNhanVien.clearSelection();
        } else if (tblNhanVien.getSelectedRow() < 0 && tblNhanVien.getRowCount() > 0) {
            tblNhanVien.setRowSelectionInterval(0, 0);
        }
        tblNhanVien.repaint();
    }

    // Bỏ dấu tiếng Việt, về chữ thường để tìm kiếm không phân biệt dấu
    private static String chuanHoa(String chu) {
        String s = Normalizer.normalize(chu == null ? "" : chu, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
    }

    private void locNhanVien() {
        final String ma = chuanHoa(txtMaNV.getText());
        final String ten = chuanHoa(txtTenNV.getText());

        if (ma.isEmpty() && ten.isEmpty()) {
            sorterNhanVien.setRowFilter(null);
            return;
        }
        sorterNhanVien.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                return chuanHoa(e.getStringValue(0)).contains(ma)
                        && chuanHoa(e.getStringValue(1)).contains(ten);
            }
        });
    }

    private String maNhanVienDangChon() {
        int dong = tblNhanVien.getSelectedRow();
        if (dong < 0) {
            return null;
        }
        return modelNhanVien.getValueAt(tblNhanVien.convertRowIndexToModel(dong), 0).toString();
    }

    // THỐNG KÊ
    // Tính lại toàn bộ: tổng, danh sách, biểu đồ
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
            tieuDeDS = "DANH SÁCH HÓA ĐƠN (" + batDau.format(DMY) + ")";
            tieuDeBD = "";
        } else if (cheDo == CHE_DO_THANG) {
            batDau = ym.atDay(1);
            ketThuc = ym.atEndOfMonth();
            String ky = String.format("%02d/%d", thang, nam);
            tieuDeDS = "THỐNG KÊ THÁNG " + ky;
            tieuDeBD = "BIỂU ĐỒ DOANH THU ĐÃ THU THEO THÁNG " + ky;
        } else {
            batDau = LocalDate.of(nam, 1, 1);
            ketThuc = LocalDate.of(nam, 12, 31);
            tieuDeDS = "THỐNG KÊ THEO NĂM " + nam;
            tieuDeBD = "BIỂU ĐỒ DOANH THU ĐÃ THU THEO NĂM " + nam;
        }
        lblTieuDeDanhSach.setText(tieuDeDS);
        lblTieuDeBieuDo.setText(tieuDeBD);

        // 2. Phạm vi nhân viên
        boolean theoNV = cmbPhamVi.getSelectedIndex() == 1;
        String maNV = theoNV ? maNhanVienDangChon() : null;
        List<HoaDon> dsLoc = new ArrayList<>();
        for (HoaDon hd : dsHoaDon) {
            if (!theoNV || (maNV != null && maNV.equals(hd.maNV))) {
                dsLoc.add(hd);
            }
        }

        // 3. Danh sách
        dongHoaDon.clear();
        dongKhoa.clear();
        modelThongKe.setRowCount(0);
        if (cheDoCuoi != cheDo) {
            modelThongKe.setColumnIdentifiers(layTieuDeCot());
            cheDoCuoi = cheDo;
        }
        if (cheDo != CHE_DO_NGAY) {
            thuNganCot();
        }
        long[] ketQua = cheDo == CHE_DO_NGAY ? napBangNgay(dsLoc, batDau) : napBangNhom(dsLoc, batDau, ketThuc);

        lblTongSoHoaDon.setText(String.valueOf(ketQua[0]));
        lblTongTienHoaDon.setText(formatTien(ketQua[1]));
        capNhatGhiChu(theoNV, maNV);

        // 4. Bố cục các cột và biểu đồ
        capNhatBoCot();
        if (cheDo != CHE_DO_NGAY) {
            capNhatBieuDo(dsLoc, ym, nam);
        }
    }

    private String[] layTieuDeCot() {
        if (cheDo == CHE_DO_NGAY) {
            return COT_NGAY;
        }
        return new String[] { cheDo == CHE_DO_THANG ? "Ngày" : "Tháng", "Số hóa đơn", "Tổng tiền hóa đơn",
                "Tổng tiền đã thu" };
    }

    // Thu ngắn cột Ngày/Tháng và Số hóa đơn
    private void thuNganCot() {
        tblThongKe.getColumnModel().getColumn(0).setPreferredWidth(55);
        tblThongKe.getColumnModel().getColumn(0).setMaxWidth(70);
        tblThongKe.getColumnModel().getColumn(1).setPreferredWidth(85);
        tblThongKe.getColumnModel().getColumn(1).setMaxWidth(90);
    }

    // Tab ngày: liệt kê từng hóa đơn trong ngày. Trả về {số hóa đơn, tổng tiền hóa
    // đơn}
    private long[] napBangNgay(List<HoaDon> dsLoc, LocalDate ngay) {
        List<HoaDon> dsNgay = new ArrayList<>();
        for (HoaDon hd : dsLoc) {
            if (hd.ngay.equals(ngay)) {
                dsNgay.add(hd);
            }
        }
        dsNgay.sort((a, b) -> a.gio.compareTo(b.gio));

        long tongTien = 0;
        for (HoaDon hd : dsNgay) {
            modelThongKe.addRow(new Object[] {
                    hd.ma,
                    tenNhanVien(hd.maNV),
                    hd.khachHang,
                    hd.bienSo,
                    formatTien(hd.tong),
                    hd.trangThai()
            });
            dongHoaDon.add(hd);
            tongTien += hd.tong;
        }
        return new long[] { dsNgay.size(), tongTien };
    }

    // Tab tháng / năm: gom theo ngày hoặc tháng. Trả về {số hóa đơn, tổng tiền hóa
    // đơn}
    private long[] napBangNhom(List<HoaDon> dsLoc, LocalDate batDau, LocalDate ketThuc) {
        // Mỗi nhóm: {số hóa đơn, tổng tiền, tiền đã thu}
        TreeMap<LocalDate, long[]> dsNhom = new TreeMap<>();
        for (HoaDon hd : dsLoc) {
            if (hd.ngay.isBefore(batDau) || hd.ngay.isAfter(ketThuc)) {
                continue;
            }
            LocalDate khoa = cheDo == CHE_DO_THANG ? hd.ngay : hd.ngay.withDayOfMonth(1);
            long[] nhom = dsNhom.computeIfAbsent(khoa, k -> new long[3]);
            nhom[0]++;
            nhom[1] += hd.tong;
            nhom[2] += hd.daThu;
        }

        long soHD = 0, tongTien = 0;
        for (Map.Entry<LocalDate, long[]> muc : dsNhom.entrySet()) {
            long[] nhom = muc.getValue();
            String nhan = String.valueOf(cheDo == CHE_DO_THANG
                    ? muc.getKey().getDayOfMonth()
                    : muc.getKey().getMonthValue());
            modelThongKe.addRow(new Object[] { nhan, nhom[0], formatTien(nhom[1]), formatTien(nhom[2]) });
            dongKhoa.add(muc.getKey());
            soHD += nhom[0];
            tongTien += nhom[1];
        }
        return new long[] { soHD, tongTien };
    }

    private void capNhatGhiChu(boolean theoNV, String maNV) {
        if (theoNV && maNV == null) {
            lblGhiChu.setText("Hãy chọn một nhân viên trong danh sách.");
        } else if (modelThongKe.getRowCount() == 0) {
            lblGhiChu.setText("Không có hóa đơn trong kỳ này.");
        } else if (cheDo == CHE_DO_NGAY) {
            lblGhiChu.setText("Bấm đúp một hóa đơn để xem chi tiết.");
        } else {
            lblGhiChu.setText("Bấm đúp một dòng để xem chi tiết theo " + (cheDo == CHE_DO_THANG ? "ngày." : "tháng."));
        }
    }

    // Tab ngày cho danh sách chiếm luôn chỗ biểu đồ
    private void capNhatBoCot() {
        boolean tabNgay = cheDo == CHE_DO_NGAY;
        GridBagLayout bocuc = (GridBagLayout) pnlCacCot.getLayout();
        GridBagConstraints rang = bocuc.getConstraints(pnlCotDanhSach);
        rang.gridwidth = tabNgay ? 2 : 1;
        rang.weightx = tabNgay ? 0.73 : 0.36;
        rang.insets = tabNgay ? new Insets(0, 5, 0, 0) : new Insets(0, 5, 0, 5);
        bocuc.setConstraints(pnlCotDanhSach, rang);
        pnlCotBieuDo.setVisible(!tabNgay);
        pnlCacCot.revalidate();
        pnlCacCot.repaint();
    }

    // Biểu đồ: doanh thu đã thu theo ngày trong tháng hoặc theo tháng trong năm
    private void capNhatBieuDo(List<HoaDon> dsLoc, YearMonth ym, int nam) {
        int soCot = cheDo == CHE_DO_THANG ? ym.lengthOfMonth() : 12;
        String[] dsNhan = new String[soCot];
        long[] dsDoanhThu = new long[soCot];
        long[] dsSoHD = new long[soCot];

        for (int i = 0; i < soCot; i++) {
            LocalDate tu, den;
            if (cheDo == CHE_DO_THANG) {
                tu = den = ym.atDay(i + 1);
            } else {
                YearMonth thang = YearMonth.of(nam, i + 1);
                tu = thang.atDay(1);
                den = thang.atEndOfMonth();
            }
            dsNhan[i] = String.valueOf(i + 1);
            for (HoaDon hd : dsLoc) {
                if (!hd.ngay.isBefore(tu) && !hd.ngay.isAfter(den)) {
                    dsDoanhThu[i] += hd.daThu;
                    dsSoHD[i]++;
                }
            }
        }
        pnlBieuDo.napDuLieu(dsNhan, dsDoanhThu, dsSoHD, cheDo == CHE_DO_THANG ? "Ngày" : "Tháng");
    }

    // XEM CHI TIẾT
    // Ngày: xem chi tiết hóa đơn. Tháng/Năm: đi sâu xuống ngày/tháng được chọn
    private void xemChiTiet() {
        int dong = tblThongKe.getSelectedRow();
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

    private JPanel taoOTien(String nhan, String giaTri, Color mau) {
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(font(Font.PLAIN, 12));
        lblNhan.setForeground(DARK_BLUE);

        JLabel lblGiaTri = new JLabel(giaTri);
        lblGiaTri.setFont(font(Font.BOLD, 16));
        lblGiaTri.setForeground(mau);

        JPanel pnlO = new JPanel(new BorderLayout(0, 4));
        pnlO.setBackground(PALE);
        pnlO.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        pnlO.add(lblNhan, BorderLayout.NORTH);
        pnlO.add(lblGiaTri, BorderLayout.CENTER);
        return pnlO;
    }

    // Cửa sổ chi tiết hóa đơn: 1) Thông tin hóa đơn, 2) Thông tin thanh toán, 3)
    // Nút Đóng
    private void hienChiTietHoaDon(HoaDon hd) {
        JDialog winChiTiet = new JDialog(this, "Chi tiết hóa đơn - " + hd.ma, true);

        // Phần 1: thông tin hóa đơn
        JPanel pnlThongTin = taoPnlThe();
        pnlThongTin.add(taoTieuDe("1. THÔNG TIN HÓA ĐƠN"), BorderLayout.NORTH);
        JPanel pnlLuoi = new JPanel(new GridLayout(0, 2, 24, 10));
        pnlLuoi.setOpaque(false);
        pnlLuoi.add(taoOThongTin("Mã hóa đơn", hd.ma));
        pnlLuoi.add(taoOThongTin("Ngày lập", hd.ngay.format(DMY) + "  " + hd.gio.format(HM)));
        pnlLuoi.add(taoOThongTin("Nhân viên lập hóa đơn", tenNhanVien(hd.maNV)));
        pnlLuoi.add(taoOThongTin("Khách hàng", hd.khachHang));
        pnlLuoi.add(taoOThongTin("Biển số xe", hd.bienSo));
        pnlThongTin.add(pnlLuoi, BorderLayout.CENTER);

        // Phần 2: thông tin thanh toán
        Color mauTrangThai = hd.daThu <= 0 ? DEBT_RED : hd.daThu < hd.tong ? ORANGE : GREEN;
        Color mauConLai = hd.conPhaiThu() > 0 ? DEBT_RED : GREEN;

        JPanel pnlTien = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlTien.setOpaque(false);
        pnlTien.add(taoOTien("Tổng tiền hóa đơn", formatTien(hd.tong), DARK_BLUE));
        pnlTien.add(taoOTien("Tổng tiền đã thu", formatTien(hd.daThu), GREEN));
        pnlTien.add(taoOTien("Tổng tiền còn phải thu", formatTien(hd.conPhaiThu()), mauConLai));

        JLabel lblNhanTrangThai = new JLabel("Trạng thái thanh toán:  ");
        lblNhanTrangThai.setFont(font(Font.PLAIN, 12));
        lblNhanTrangThai.setForeground(MUTED);
        JLabel lblTrangThai = new JLabel(hd.trangThai());
        lblTrangThai.setFont(font(Font.BOLD, 14));
        lblTrangThai.setForeground(mauTrangThai);
        JPanel pnlTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTrangThai.setOpaque(false);
        pnlTrangThai.setBorder(new EmptyBorder(4, 0, 0, 0));
        pnlTrangThai.add(lblNhanTrangThai);
        pnlTrangThai.add(lblTrangThai);

        JPanel pnlThanhToan = taoPnlThe();
        pnlThanhToan.add(taoTieuDe("2. THÔNG TIN THANH TOÁN"), BorderLayout.NORTH);
        JPanel pnlNoiDungTT = new JPanel(new BorderLayout(0, 8));
        pnlNoiDungTT.setOpaque(false);
        pnlNoiDungTT.add(pnlTien, BorderLayout.CENTER);
        pnlNoiDungTT.add(pnlTrangThai, BorderLayout.SOUTH);
        pnlThanhToan.add(pnlNoiDungTT, BorderLayout.CENTER);

        // Phần 3: chỉ có nút Đóng
        ActionButton btnDong = new ActionButton("Đóng", false);
        btnDong.addActionListener(e -> winChiTiet.dispose());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnDong);

        // Ghép các phần
        JPanel pnlNoiDung = new JPanel();
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
        for (JPanel pnl : new JPanel[] { pnlThongTin, pnlThanhToan, pnlNut }) {
            pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (pnlNoiDung.getComponentCount() > 0) {
                pnlNoiDung.add(Box.createVerticalStrut(10));
            }
            pnlNoiDung.add(pnl);
        }

        JPanel pnlGoc = new JPanel(new BorderLayout());
        pnlGoc.setBackground(BG);
        pnlGoc.setBorder(new EmptyBorder(14, 16, 14, 16));
        pnlGoc.add(pnlNoiDung, BorderLayout.CENTER);

        winChiTiet.setContentPane(pnlGoc);
        winChiTiet.getRootPane().setDefaultButton(btnDong);
        winChiTiet.pack();
        winChiTiet.setMinimumSize(new Dimension(620, winChiTiet.getHeight()));
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
        if (modelThongKe.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất.", "Xuất file Excel",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fch = new JFileChooser();
        fch.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        fch.setSelectedFile(
                new File("HoaDon_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx"));
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
        String ten = modelThongKe.getColumnName(cot);
        if ("Số hóa đơn".equals(ten)) {
            return LOAI_SO;
        }
        for (int dong = 0; dong < modelThongKe.getRowCount(); dong++) {
            Object giaTri = modelThongKe.getValueAt(dong, cot);
            if (giaTri instanceof String && ((String) giaTri).endsWith(" đ")) {
                return LOAI_TIEN;
            }
        }
        if ("Ngày".equals(ten) || "Tháng".equals(ten) || "Mã hóa đơn".equals(ten)
                || "Biển số xe".equals(ten) || "Trạng thái thanh toán".equals(ten)) {
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
        final int soCotNguon = modelThongKe.getColumnCount();
        final int soDong = modelThongKe.getRowCount();
        final int soCot = soCotNguon + 1; // thêm cột STT

        String[] dsTieuDe = new String[soCot];
        int[] dsLoai = new int[soCot];
        int[] dsRong = new int[soCot];
        dsTieuDe[0] = "STT";
        dsLoai[0] = LOAI_CHU_GIUA;
        for (int c = 0; c < soCotNguon; c++) {
            dsTieuDe[c + 1] = modelThongKe.getColumnName(c);
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
                Object giaTri = modelThongKe.getValueAt(d, c - 1);
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
                    + "<sheets><sheet name=\"Hóa đơn\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
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

    private static boolean laCotTien(String ten) {
        return "Tổng tiền hóa đơn".equals(ten) || "Tổng tiền".equals(ten);
    }

    // LỚP GIAO DIỆN TỰ VẼ
    // Hiển thị ô bảng: số tiền căn phải (đậm), số lượng căn giữa, dòng xen kẽ màu
    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object giaTri, boolean dangChon,
                boolean coFocus, int dong, int cot) {
            super.getTableCellRendererComponent(tbl, giaTri, dangChon, false, dong, cot);
            String chu = giaTri == null ? "" : giaTri.toString();
            setBorder(new EmptyBorder(0, 8, 0, 8));

            if (giaTri instanceof Number) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else if (chu.endsWith(" đ")) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            setFont(font(laCotTien(tbl.getColumnName(cot)) ? Font.BOLD : Font.PLAIN, 12));

            Color mauChu;
            if (!tbl.isEnabled()) {
                mauChu = DISABLED;
            } else if (dangChon) {
                mauChu = DARK_BLUE;
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
            setBorder(new EmptyBorder(9, 14, 9, 14));
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

    // Biểu đồ cột doanh thu đã thu
    private static class BieuDoDoanhThu extends JPanel {
        private static final int LE_TRAI = 64, LE_PHAI = 14, LE_TREN = 36, LE_DUOI = 50;

        private String[] dsNhan = new String[0];
        private long[] dsDoanhThu = new long[0], dsSoHD = new long[0];
        private String tenTrucX = "";

        BieuDoDoanhThu() {
            setBackground(Color.WHITE);
            setToolTipText("");
        }

        void napDuLieu(String[] dsNhan, long[] dsDoanhThu, long[] dsSoHD, String tenTrucX) {
            this.dsNhan = dsNhan;
            this.dsDoanhThu = dsDoanhThu;
            this.dsSoHD = dsSoHD;
            this.tenTrucX = tenTrucX;
            repaint();
        }

        // Bước chia trục Y "đẹp" (1, 2, 2.5, 5, 10 x lũy thừa 10)
        private long buocChia() {
            long lonNhat = 0;
            for (long v : dsDoanhThu) {
                lonNhat = Math.max(lonNhat, v);
            }
            double tho = lonNhat <= 0 ? 1_000_000 : lonNhat / 5.0;
            double luyThua = Math.pow(10, Math.floor(Math.log10(tho)));
            double chuan = tho / luyThua;
            double dep = chuan <= 1 ? 1 : chuan <= 2 ? 2 : chuan <= 2.5 ? 2.5 : chuan <= 5 ? 5 : 10;
            return Math.round(dep * luyThua);
        }

        // Rút gọn nhãn trục Y: 2500000 -> "2,5 tr", 3000000000 -> "3 tỷ"
        private String rutGon(long v) {
            if (v == 0) {
                return "0";
            }
            Locale vi = Locale.forLanguageTag("vi-VN");
            if (v >= 1_000_000_000L) {
                double ty = v / 1_000_000_000.0;
                return (ty == Math.floor(ty) ? String.valueOf((long) ty) : String.format(vi, "%.1f", ty)) + " tỷ";
            }
            if (v >= 1_000_000) {
                double trieu = v / 1_000_000.0;
                return (trieu == Math.floor(trieu) ? String.valueOf((long) trieu) : String.format(vi, "%.1f", trieu))
                        + " tr";
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
            return "<html><b>" + tenTrucX + " " + dsNhan[i] + "</b><br>Số hóa đơn: " + dsSoHD[i]
                    + "<br>Doanh thu: " + formatTien(dsDoanhThu[i]) + "</html>";
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
            g2.setColor(BLUE);
            g2.fillRoundRect(LE_TRAI, 8, 12, 12, 3, 3);
            g2.setColor(DARK_BLUE);
            g2.drawString("Doanh thu đã thu", LE_TRAI + 18, 19);

            // Lưới ngang + nhãn trục Y
            g2.setFont(font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
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
                int beRongCot = (int) Math.max(3, Math.min(30, oCot * 0.6));
                int buocNhan = oCot >= 26 ? 1 : oCot >= 14 ? 2 : 5;
                for (int i = 0; i < soCot; i++) {
                    if (dsDoanhThu[i] > 0) {
                        khongCoDuLieu = false;
                    }
                    double tam = LE_TRAI + oCot * i + oCot / 2;
                    int caoCot = (int) (cao * (double) dsDoanhThu[i] / yToiDa);
                    g2.setColor(BLUE);
                    g2.fillRoundRect((int) tam - beRongCot / 2, LE_TREN + cao - caoCot, beRongCot, caoCot, 3, 3);
                    if (i % buocNhan == 0) {
                        g2.setColor(DARK_BLUE);
                        g2.drawString(dsNhan[i], (int) tam - fm.stringWidth(dsNhan[i]) / 2, LE_TREN + cao + 16);
                    }
                }
            }
            if (khongCoDuLieu) {
                g2.setFont(font(Font.ITALIC, 13));
                g2.setColor(MUTED);
                String chu = "Không có doanh thu trong kỳ";
                g2.drawString(chu, LE_TRAI + (rong - g2.getFontMetrics().stringWidth(chu)) / 2, LE_TREN + cao / 2);
            }

            // Tên trục
            g2.setColor(DARK_BLUE);
            g2.setFont(font(Font.BOLD, 12));
            fm = g2.getFontMetrics();
            g2.drawString(tenTrucX, LE_TRAI + (rong - fm.stringWidth(tenTrucX)) / 2, getHeight() - 12);
            String tenTrucY = "Doanh thu (VNĐ)";
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
            new FrmThongKeHoaDon().setVisible(true);
        });
    }
}
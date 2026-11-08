package gui;

import controller.ThongKeDoanhThuController;
import controller.ThongKeDoanhThuController.DuLieuBieuDo;
import controller.ThongKeDoanhThuController.KetQuaThongKe;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import static controller.ThongKeDoanhThuController.*;

/**
 * Giao diện Thống kê doanh thu. Mọi xử lý dữ liệu nằm ở
 * ThongKeDoanhThuController (và ThongKeDoanhThuDAO), lớp này chỉ dựng giao diện
 * và hiển thị kết quả.
 */
public class FrmThongKeDoanhThu extends JPanel {
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
    // Tên thẻ trong khung bộ lọc
    private static final String THE_NV = "NV";
    private static final String THE_KH = "KH";

    // CONTROLLER VÀ TRẠNG THÁI
    private final ThongKeDoanhThuController controller = new ThongKeDoanhThuController();
    private KetQuaThongKe ketQua; // kết quả thống kê đang hiển thị (để mở chi tiết / đi sâu xuống ngày, tháng)

    private int cheDo = CHE_DO_NGAY;
    private int cheDoCuoi = -1; // chế độ của lần dựng cột gần nhất
    private boolean sanSang = false; // chỉ thống kê sau khi giao diện dựng xong
    private boolean dangDieuChinh = false; // chặn sự kiện khi đang đặt giá trị bằng code

    // THÀNH PHẦN GIAO DIỆN
    // Thanh tab và thanh thời gian
    private JToggleButton[] tglTab;
    private JPanel pnlThoiGian;
    private JComboBox<Integer> cmbNgay, cmbThang, cmbNam;
    private JLabel lblTongSoHoaDon, lblTongTienHoaDon, lblNoTrongKy, lblNoLuyKe;

    // Khung bộ lọc
    private JComboBox<String> cmbPhamVi;
    private CardLayout cardBoLoc;
    private JPanel pnlTheBoLoc;
    private JTextField txtMaNV, txtTenNV, txtMaKH, txtTenKH, txtSoDienThoai;
    private ActionButton btnTimKiemNV, btnTimKiemKH;
    private JTable tblNhanVien, tblKhachHang;
    private DefaultTableModel modelNhanVien, modelKhachHang;
    private TableRowSorter<DefaultTableModel> sorterNhanVien, sorterKhachHang;

    // Khung danh sách và biểu đồ
    private JPanel pnlCacCot, pnlCotDanhSach, pnlCotBieuDo;
    private JTable tblThongKe;
    private DefaultTableModel modelThongKe;
    private JLabel lblTieuDeDanhSach, lblTieuDeBieuDo, lblGhiChu;
    private BieuDoThongKe pnlBieuDo;

    // KHỞI TẠO
    public FrmThongKeDoanhThu() {
        setMinimumSize(new Dimension(1200, 720));
        setPreferredSize(new Dimension(1500, 900));
        setBackground(BG);
        setLayout(new BorderLayout());

        try {
            controller.taiDuLieu();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu hóa đơn từ CSDL:\n" + ex.getMessage(),
                    "Lỗi kết nối CSDL", JOptionPane.ERROR_MESSAGE);
        }

        add(dungTieuDe(), BorderLayout.NORTH);
        add(dungKhungChinh(), BorderLayout.CENTER);
        ganSuKien();

        napBangNhanVien();
        napBangKhachHang();
        apDungPhamVi();
        dongBoSoNgay();
        sanSang = true;
        lamMoiThongKe();
    }

    // DỰNG GIAO DIỆN CHÍNH
    private JPanel dungTieuDe() {
        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setBackground(BG);
        pnlTieuDe.setBorder(new EmptyBorder(10, 16, 0, 16));

        JLabel lblTieuDe = new JLabel("THỐNG KÊ DOANH THU", SwingConstants.CENTER);
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

    // Hàng trên cùng: thời gian thống kê + tổng số hóa đơn + tổng tiền hóa đơn +
    // công nợ
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
        lblNoTrongKy = taoLblSoLieu(DEBT_RED);
        lblNoLuyKe = taoLblSoLieu(ORANGE);
        lblTongSoHoaDon.setText("0");
        JPanel pnlSoLieu = new JPanel(new GridLayout(2, 2, 10, 6));
        pnlSoLieu.setOpaque(false);
        pnlSoLieu.add(taoOThongKe("Tổng số hóa đơn", BLUE, lblTongSoHoaDon));
        pnlSoLieu.add(taoOThongKe("Tổng tiền hóa đơn", GREEN, lblTongTienHoaDon));
        pnlSoLieu.add(taoOThongKe("Còn nợ trong kỳ", DEBT_RED, lblNoTrongKy));
        pnlSoLieu.add(taoOThongKe("Công nợ lũy kế (đến cuối kỳ)", ORANGE, lblNoLuyKe));

        pnlThanh.add(pnlTrai, BorderLayout.WEST);
        pnlThanh.add(pnlSoLieu, BorderLayout.EAST);
        return pnlThanh;
    }

    // Khung 1: bộ lọc (thẻ nhân viên hoặc thẻ khách hàng tùy phạm vi)
    private JPanel dungKhungBoLoc() {
        JPanel pnlBoLoc = taoPnlThe();
        JPanel pnlTren = new JPanel();
        pnlTren.setOpaque(false);
        pnlTren.setLayout(new BoxLayout(pnlTren, BoxLayout.Y_AXIS));

        // Thống kê theo (ô chọn cùng hàng với tiêu đề)
        cmbPhamVi = new JComboBox<>(new String[] { "Tất cả", "Nhân viên", "Khách hàng" });
        JPanel pnlPhamVi = new JPanel(new BorderLayout(10, 0));
        pnlPhamVi.setOpaque(false);
        pnlPhamVi.add(taoTieuMuc("Thống kê theo:"), BorderLayout.WEST);
        pnlPhamVi.add(cmbPhamVi, BorderLayout.CENTER);
        pnlTren.add(codinh(pnlPhamVi, 32));
        pnlTren.add(Box.createVerticalStrut(4));
        themDuongKe(pnlTren);

        cardBoLoc = new CardLayout();
        pnlTheBoLoc = new JPanel(cardBoLoc);
        pnlTheBoLoc.setOpaque(false);
        pnlTheBoLoc.add(dungTheNhanVien(), THE_NV);
        pnlTheBoLoc.add(dungTheKhachHang(), THE_KH);

        pnlBoLoc.add(pnlTren, BorderLayout.NORTH);
        pnlBoLoc.add(pnlTheBoLoc, BorderLayout.CENTER);
        return pnlBoLoc;
    }

    // Thẻ tìm kiếm + danh sách nhân viên
    private JPanel dungTheNhanVien() {
        JPanel pnlThe = new JPanel(new BorderLayout());
        pnlThe.setOpaque(false);
        JPanel pnlTren = new JPanel();
        pnlTren.setOpaque(false);
        pnlTren.setLayout(new BoxLayout(pnlTren, BoxLayout.Y_AXIS));

        pnlTren.add(taoTieuMuc("Tìm kiếm nhân viên"));
        pnlTren.add(Box.createVerticalStrut(6));
        txtMaNV = new JTextField();
        txtTenNV = new JTextField();
        themOTimKiem(pnlTren, "Mã NV:", txtMaNV, 70);
        themOTimKiem(pnlTren, "Tên NV:", txtTenNV, 70);

        btnTimKiemNV = new ActionButton("Tìm kiếm", true);
        btnTimKiemNV.addActionListener(e -> locNhanVien());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnTimKiemNV);
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

        pnlThe.add(pnlTren, BorderLayout.NORTH);
        pnlThe.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlThe;
    }

    // Thẻ tìm kiếm + danh sách khách hàng
    private JPanel dungTheKhachHang() {
        JPanel pnlThe = new JPanel(new BorderLayout());
        pnlThe.setOpaque(false);
        JPanel pnlTren = new JPanel();
        pnlTren.setOpaque(false);
        pnlTren.setLayout(new BoxLayout(pnlTren, BoxLayout.Y_AXIS));

        pnlTren.add(taoTieuMuc("Tìm kiếm khách hàng"));
        pnlTren.add(Box.createVerticalStrut(6));
        txtMaKH = new JTextField();
        txtTenKH = new JTextField();
        txtSoDienThoai = new JTextField();
        themOTimKiem(pnlTren, "Mã KH:", txtMaKH, 92);
        themOTimKiem(pnlTren, "Tên KH:", txtTenKH, 92);
        themOTimKiem(pnlTren, "Số điện thoại:", txtSoDienThoai, 92);

        btnTimKiemKH = new ActionButton("Tìm kiếm", true);
        btnTimKiemKH.addActionListener(e -> locKhachHang());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnTimKiemKH);
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

        pnlThe.add(pnlTren, BorderLayout.NORTH);
        pnlThe.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlThe;
    }

    private void themOTimKiem(JPanel pnlCha, String nhan, JTextField txt, int rongNhan) {
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(font(Font.PLAIN, 12));
        lblNhan.setForeground(DARK_BLUE);
        lblNhan.setPreferredSize(new Dimension(rongNhan, 30));

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
        JLabel lbl = new JLabel("0 đ", SwingConstants.RIGHT);
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
        lblTieuDeBieuDo = taoTieuDe("BIỂU ĐỒ DOANH THU VÀ CÔNG NỢ");
        pnlBieuDo = new BieuDoThongKe();
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

        // Đổi phạm vi thống kê (Tất cả / Nhân viên / Khách hàng)
        cmbPhamVi.addActionListener(e -> {
            if (dangDieuChinh) {
                return;
            }
            apDungPhamVi();
            lamMoiThongKe();
        });

        // Chọn một nhân viên / khách hàng trong danh sách
        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                lamMoiThongKe();
            }
        });
        tblKhachHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                lamMoiThongKe();
            }
        });

        // Gõ vào ô tìm kiếm -> lọc ngay
        khiThayDoi(txtMaNV, this::locNhanVien);
        khiThayDoi(txtTenNV, this::locNhanVien);
        khiThayDoi(txtMaKH, this::locKhachHang);
        khiThayDoi(txtTenKH, this::locKhachHang);
        khiThayDoi(txtSoDienThoai, this::locKhachHang);

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

    // BỘ LỌC: THỜI GIAN, PHẠM VI, NHÂN VIÊN, KHÁCH HÀNG
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
        Map<String, Integer> soHoaDon = controller.demHoaDonTheoNhanVien();
        modelNhanVien.setRowCount(0);
        for (NhanVien nv : controller.getDanhSachNhanVien()) {
            modelNhanVien.addRow(new Object[] { nv.getMaNV(), nv.getHoTen(), soHoaDon.getOrDefault(nv.getMaNV(), 0) });
        }
    }

    private void napBangKhachHang() {
        modelKhachHang.setRowCount(0);
        for (KhachHang kh : controller.getDanhSachKhachHang()) {
            modelKhachHang.addRow(new Object[] { kh.getMaKH(), kh.getHoTen(), kh.getSoDT() });
        }
    }

    // "Tất cả": khóa cả hai danh sách. "Nhân viên"/"Khách hàng": mở khóa thẻ tương
    // ứng
    private void apDungPhamVi() {
        int phamVi = cmbPhamVi.getSelectedIndex();
        boolean theoNV = phamVi == PHAM_VI_NHAN_VIEN;
        boolean theoKH = phamVi == PHAM_VI_KHACH_HANG;

        txtMaNV.setEnabled(theoNV);
        txtTenNV.setEnabled(theoNV);
        btnTimKiemNV.setEnabled(theoNV);
        tblNhanVien.setEnabled(theoNV);
        if (!theoNV) {
            txtMaNV.setText("");
            txtTenNV.setText("");
            sorterNhanVien.setRowFilter(null);
            tblNhanVien.clearSelection();
        } else if (tblNhanVien.getSelectedRow() < 0 && tblNhanVien.getRowCount() > 0) {
            tblNhanVien.setRowSelectionInterval(0, 0);
        }

        txtMaKH.setEnabled(theoKH);
        txtTenKH.setEnabled(theoKH);
        txtSoDienThoai.setEnabled(theoKH);
        btnTimKiemKH.setEnabled(theoKH);
        tblKhachHang.setEnabled(theoKH);
        if (!theoKH) {
            txtMaKH.setText("");
            txtTenKH.setText("");
            txtSoDienThoai.setText("");
            sorterKhachHang.setRowFilter(null);
            tblKhachHang.clearSelection();
        } else {
            chonDongDauKhachHang();
        }

        cardBoLoc.show(pnlTheBoLoc, theoKH ? THE_KH : THE_NV);
        tblNhanVien.repaint();
        tblKhachHang.repaint();
    }

    // Đang thống kê theo khách hàng mà chưa có dòng nào được chọn thì chọn dòng đầu
    private void chonDongDauKhachHang() {
        if (cmbPhamVi.getSelectedIndex() == PHAM_VI_KHACH_HANG
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
        chonDongDauKhachHang();
    }

    private String maNhanVienDangChon() {
        int dong = tblNhanVien.getSelectedRow();
        if (dong < 0) {
            return null;
        }
        return modelNhanVien.getValueAt(tblNhanVien.convertRowIndexToModel(dong), 0).toString();
    }

    private String maKhachHangDangChon() {
        int dong = tblKhachHang.getSelectedRow();
        if (dong < 0) {
            return null;
        }
        return modelKhachHang.getValueAt(tblKhachHang.convertRowIndexToModel(dong), 0).toString();
    }

    // HIỂN THỊ THỐNG KÊ
    // Lấy kết quả từ controller rồi đổ lên: tổng, công nợ, danh sách, biểu đồ
    private void lamMoiThongKe() {
        if (!sanSang) {
            return;
        }
        int nam = layGiaTri(cmbNam, LocalDate.now().getYear());
        int thang = layGiaTri(cmbThang, 1);
        YearMonth ym = YearMonth.of(nam, thang);
        int ngay = Math.min(layGiaTri(cmbNgay, 1), ym.lengthOfMonth());

        // 1. Tiêu đề theo kỳ thống kê
        String tieuDeDS, tieuDeBD;
        if (cheDo == CHE_DO_NGAY) {
            tieuDeDS = "DANH SÁCH HÓA ĐƠN (" + ym.atDay(ngay).format(DMY) + ")";
            tieuDeBD = "";
        } else if (cheDo == CHE_DO_THANG) {
            String ky = String.format("%02d/%d", thang, nam);
            tieuDeDS = "THỐNG KÊ THÁNG " + ky;
            tieuDeBD = "BIỂU ĐỒ DOANH THU VÀ CÔNG NỢ THEO THÁNG " + ky;
        } else {
            tieuDeDS = "THỐNG KÊ THEO NĂM " + nam;
            tieuDeBD = "BIỂU ĐỒ DOANH THU VÀ CÔNG NỢ THEO NĂM " + nam;
        }
        lblTieuDeDanhSach.setText(tieuDeDS);
        lblTieuDeBieuDo.setText(tieuDeBD);

        // 2. Nhờ controller tính
        int phamVi = cmbPhamVi.getSelectedIndex();
        String maNV = phamVi == PHAM_VI_NHAN_VIEN ? maNhanVienDangChon() : null;
        String maKH = phamVi == PHAM_VI_KHACH_HANG ? maKhachHangDangChon() : null;
        ketQua = controller.tinhThongKe(cheDo, nam, thang, ngay, phamVi, maNV, maKH);

        // 3. Công nợ và tổng
        lblNoTrongKy.setText(formatTien(ketQua.noTrongKy));
        lblNoLuyKe.setText(formatTien(ketQua.noLuyKe));
        lblTongSoHoaDon.setText(String.valueOf(ketQua.soHoaDon));
        lblTongTienHoaDon.setText(formatTien(ketQua.tongTien));

        // 4. Danh sách
        modelThongKe.setRowCount(0);
        if (cheDoCuoi != cheDo) {
            modelThongKe.setColumnIdentifiers(ketQua.tieuDeCot);
            cheDoCuoi = cheDo;
        }
        if (cheDo != CHE_DO_NGAY) {
            thuNganCot();
        }
        for (Object[] dong : ketQua.dong) {
            modelThongKe.addRow(dong);
        }
        capNhatGhiChu(phamVi, maNV, maKH);

        // 5. Bố cục các cột và biểu đồ
        capNhatBoCot();
        if (ketQua.bieuDo != null) {
            DuLieuBieuDo bd = ketQua.bieuDo;
            pnlBieuDo.napDuLieu(bd.nhan, bd.giaTri, bd.soHoaDon, bd.tenTrucX);
        }
    }

    // Thu ngắn cột Ngày/Tháng và Số hóa đơn
    private void thuNganCot() {
        tblThongKe.getColumnModel().getColumn(0).setPreferredWidth(55);
        tblThongKe.getColumnModel().getColumn(0).setMaxWidth(70);
        tblThongKe.getColumnModel().getColumn(1).setPreferredWidth(85);
        tblThongKe.getColumnModel().getColumn(1).setMaxWidth(90);
    }

    private void capNhatGhiChu(int phamVi, String maNV, String maKH) {
        if (phamVi == PHAM_VI_NHAN_VIEN && maNV == null) {
            lblGhiChu.setText("Hãy chọn một nhân viên trong danh sách.");
        } else if (phamVi == PHAM_VI_KHACH_HANG && maKH == null) {
            lblGhiChu.setText("Hãy chọn một khách hàng trong danh sách.");
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

    // XEM CHI TIẾT
    // Ngày: xem chi tiết hóa đơn. Tháng/Năm: đi sâu xuống ngày/tháng được chọn
    private void xemChiTiet() {
        int dong = tblThongKe.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng trong danh sách để xem chi tiết.",
                    "Chưa chọn dòng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ketQua == null) {
            return;
        }
        if (cheDo == CHE_DO_NGAY) {
            if (dong < ketQua.dongHoaDon.size()) {
                hienChiTietHoaDon(ketQua.dongHoaDon.get(dong));
            }
        } else if (dong < ketQua.dongKhoa.size()) {
            datNgay(ketQua.dongKhoa.get(dong));
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

    // Cửa sổ chi tiết hóa đơn: 1) Thông tin hóa đơn, 2) Thông tin thanh toán và
    // công nợ, 3) Lịch sử thanh toán, 4) Nút Đóng
    private void hienChiTietHoaDon(HoaDon hd) {
        String ma = hd.getMaHoaDon();
        long tong = controller.tongTien(hd);
        long daThu = controller.daThu(hd);
        long conLai = controller.conPhaiThu(hd);

        JDialog winChiTiet = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn - " + ma,
                Dialog.ModalityType.APPLICATION_MODAL);

        // Phần 1: thông tin hóa đơn
        JPanel pnlThongTin = taoPnlThe();
        pnlThongTin.add(taoTieuDe("1. THÔNG TIN HÓA ĐƠN"), BorderLayout.NORTH);
        JPanel pnlLuoi = new JPanel(new GridLayout(0, 2, 24, 10));
        pnlLuoi.setOpaque(false);
        pnlLuoi.add(taoOThongTin("Mã hóa đơn", ma));
        pnlLuoi.add(taoOThongTin("Ngày lập",
                hd.getNgayLap().toLocalDateTime().format(DMY) + "  " + hd.getNgayLap().toLocalDateTime().format(HM)));
        pnlLuoi.add(taoOThongTin("Nhân viên lập hóa đơn", controller.tenNhanVien(hd)));
        pnlLuoi.add(taoOThongTin("Khách hàng", controller.tenKhachHang(hd)));
        pnlLuoi.add(taoOThongTin("Số điện thoại", controller.soDienThoai(hd)));
        pnlLuoi.add(taoOThongTin("Biển số xe", controller.bienSoXe(hd)));
        pnlThongTin.add(pnlLuoi, BorderLayout.CENTER);

        // Phần 2: thông tin thanh toán
        Color mauTrangThai = daThu <= 0 ? DEBT_RED : daThu < tong ? ORANGE : GREEN;
        Color mauConLai = conLai > 0 ? DEBT_RED : GREEN;

        JPanel pnlTien = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlTien.setOpaque(false);
        pnlTien.add(taoOTien("Tổng tiền hóa đơn", formatTien(tong), DARK_BLUE));
        pnlTien.add(taoOTien("Tổng tiền đã thu", formatTien(daThu), GREEN));
        pnlTien.add(taoOTien("Tổng tiền còn phải thu", formatTien(conLai), mauConLai));

        JLabel lblNhanTrangThai = new JLabel("Trạng thái thanh toán:  ");
        lblNhanTrangThai.setFont(font(Font.PLAIN, 12));
        lblNhanTrangThai.setForeground(MUTED);
        JLabel lblTrangThai = new JLabel(controller.trangThai(hd));
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

        // Phần 3: lịch sử thanh toán
        List<Object[]> dsGiaoDich = layLichSuThanhToan(ma);
        JPanel pnlLichSu = taoPnlThe();
        pnlLichSu.add(taoTieuDe("3. LỊCH SỬ THANH TOÁN"), BorderLayout.NORTH);
        if (dsGiaoDich.isEmpty()) {
            JLabel lblTrong = new JLabel("Hóa đơn này chưa có giao dịch thanh toán nào.", SwingConstants.CENTER);
            lblTrong.setFont(font(Font.ITALIC, 12));
            lblTrong.setForeground(MUTED);
            lblTrong.setPreferredSize(new Dimension(100, 60));
            pnlLichSu.add(lblTrong, BorderLayout.CENTER);
        } else {
            DefaultTableModel modelGiaoDich = taoModel(
                    "STT", "Thời gian thanh toán", "Phương thức", "Số tiền thanh toán", "Trạng thái");
            for (Object[] dong : dsGiaoDich) {
                modelGiaoDich.addRow(dong);
            }
            JTable tblGiaoDich = new JTable(modelGiaoDich);
            caiDatBang(tblGiaoDich);
            tblGiaoDich.setRowHeight(30);
            datDoRongCot(tblGiaoDich, 40, 150, 130, 130, 100);
            JScrollPane scrGiaoDich = taoScr(tblGiaoDich);
            scrGiaoDich.setPreferredSize(new Dimension(100, 150));
            pnlLichSu.add(scrGiaoDich, BorderLayout.CENTER);
        }

        // Phần 4: chỉ có nút Đóng
        ActionButton btnDong = new ActionButton("Đóng", false);
        btnDong.addActionListener(e -> winChiTiet.dispose());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnDong);

        // Ghép các phần
        JPanel pnlNoiDung = new JPanel();
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
        for (JPanel pnl : new JPanel[] { pnlThongTin, pnlThanhToan, pnlLichSu, pnlNut }) {
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
        winChiTiet.setMinimumSize(new Dimension(640, winChiTiet.getHeight()));
        winChiTiet.setLocationRelativeTo(this);
        winChiTiet.setVisible(true);
    }

    private List<Object[]> layLichSuThanhToan(String maHoaDon) {
        try {
            return controller.layLichSuThanhToan(maHoaDon);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải lịch sử thanh toán:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            return new java.util.ArrayList<>();
        }
    }

    // XUẤT EXCEL
    // Chọn nơi lưu rồi nhờ controller ghi file
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
            controller.xuatExcel(tep, modelThongKe);
            JOptionPane.showMessageDialog(this, "Đã xuất file thành công:\n" + tep.getAbsolutePath(),
                    "Xuất file Excel", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể ghi file (file có đang mở trong Excel không?):\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // TIỆN ÍCH GIAO DIỆN
    private static Font font(int kieu, int coChu) {
        return new Font("Segoe UI", kieu, coChu);
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

    private static boolean laCotNo(String ten) {
        return "Còn nợ".equals(ten) || "Tổng tiền nợ".equals(ten);
    }

    // LỚP GIAO DIỆN TỰ VẼ
    // Hiển thị ô bảng: số tiền căn phải (đậm), nợ tô đỏ, số lượng căn giữa, dòng
    // xen kẽ màu
    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object giaTri, boolean dangChon,
                boolean coFocus, int dong, int cot) {
            super.getTableCellRendererComponent(tbl, giaTri, dangChon, false, dong, cot);
            String chu = giaTri == null ? "" : giaTri.toString();
            String tenCot = tbl.getColumnName(cot);
            boolean cotNo = laCotNo(tenCot);
            setBorder(new EmptyBorder(0, 8, 0, 8));

            if (giaTri instanceof Number) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else if (chu.endsWith(" đ")) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            setFont(font(laCotTien(tenCot) || cotNo ? Font.BOLD : Font.PLAIN, 12));

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

    // Biểu đồ cột nhóm: doanh thu đã thu (xanh dương), công nợ trong kỳ (đỏ), công
    // nợ lũy kế (cam)
    private static class BieuDoThongKe extends JPanel {
        private static final int LE_TRAI = 64, LE_PHAI = 14, LE_TREN = 44, LE_DUOI = 50;
        private static final String[] TEN_CHUOI = { "Doanh thu đã thu", "Nợ trong kỳ", "Công nợ lũy kế" };
        private static final Color[] MAU_CHUOI = { BLUE, DEBT_RED, ORANGE };

        private String[] dsNhan = new String[0];
        private long[][] dsGiaTri = new long[TEN_CHUOI.length][0];
        private long[] dsSoHD = new long[0];
        private String tenTrucX = "";

        BieuDoThongKe() {
            setBackground(Color.WHITE);
            setToolTipText("");
        }

        void napDuLieu(String[] dsNhan, long[][] dsGiaTri, long[] dsSoHD, String tenTrucX) {
            this.dsNhan = dsNhan;
            this.dsGiaTri = dsGiaTri;
            this.dsSoHD = dsSoHD;
            this.tenTrucX = tenTrucX;
            repaint();
        }

        // Bước chia trục Y "đẹp" (1, 2, 2.5, 5, 10 x lũy thừa 10)
        private long buocChia() {
            long lonNhat = 0;
            for (long[] chuoi : dsGiaTri) {
                for (long v : chuoi) {
                    lonNhat = Math.max(lonNhat, v);
                }
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
                    + "<br>Doanh thu đã thu: " + formatTien(dsGiaTri[0][i])
                    + "<br>Nợ trong kỳ: " + formatTien(dsGiaTri[1][i])
                    + "<br>Công nợ lũy kế: " + formatTien(dsGiaTri[2][i]) + "</html>";
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
            int xChuThich = LE_TRAI, yChuThich = 18;
            for (int s = 0; s < TEN_CHUOI.length; s++) {
                g2.setColor(MAU_CHUOI[s]);
                g2.fillRoundRect(xChuThich, yChuThich - 10, 12, 12, 3, 3);
                g2.setColor(DARK_BLUE);
                g2.drawString(TEN_CHUOI[s], xChuThich + 18, yChuThich);
                xChuThich += 18 + fm.stringWidth(TEN_CHUOI[s]) + 16;
            }

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
            int soChuoi = TEN_CHUOI.length;
            boolean khongCoDuLieu = true;
            if (soCot > 0) {
                double oCot = (double) rong / soCot;
                int beRongCot = (int) Math.max(2, Math.min(14, oCot / 4));
                int rongNhom = soChuoi * beRongCot + (soChuoi - 1);
                int buocNhan = oCot >= 26 ? 1 : oCot >= 14 ? 2 : 5;
                for (int i = 0; i < soCot; i++) {
                    double tam = LE_TRAI + oCot * i + oCot / 2;
                    int xDau = (int) tam - rongNhom / 2;
                    for (int s = 0; s < soChuoi; s++) {
                        long giaTri = dsGiaTri[s][i];
                        if (giaTri > 0) {
                            khongCoDuLieu = false;
                        }
                        int caoCot = (int) (cao * (double) giaTri / yToiDa);
                        g2.setColor(MAU_CHUOI[s]);
                        g2.fillRect(xDau + s * (beRongCot + 1), LE_TREN + cao - caoCot, beRongCot, caoCot);
                    }
                    if (i % buocNhan == 0) {
                        g2.setColor(DARK_BLUE);
                        g2.drawString(dsNhan[i], (int) tam - fm.stringWidth(dsNhan[i]) / 2, LE_TREN + cao + 16);
                    }
                }
            }
            if (khongCoDuLieu) {
                g2.setFont(font(Font.ITALIC, 13));
                g2.setColor(MUTED);
                String chu = "Không có doanh thu hoặc công nợ trong kỳ";
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
            JFrame winThuNghiem = new JFrame("Hệ thống quản lý gara ô tô");
            winThuNghiem.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            winThuNghiem.setContentPane(new FrmThongKeDoanhThu());
            winThuNghiem.pack();
            winThuNghiem.setLocationRelativeTo(null);
            winThuNghiem.setVisible(true);
        });
    }
}
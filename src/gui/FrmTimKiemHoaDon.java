package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import com.toedter.calendar.JDateChooser;
import connectDB.ConnectDB;

public class FrmTimKiemHoaDon extends JPanel {
    private static final Color MAU_CHU = new Color(40, 50, 65);
    private static final Color MAU_PHU = new Color(100, 112, 130);
    private static final Color MAU_NEN = new Color(244, 247, 251);
    private static final Color MAU_NUT = new Color(37, 99, 235);
    private static final Color MAU_NUT_DAM = new Color(29, 78, 216);
    private static final Color MAU_VIEN = new Color(220, 225, 232);
    private static final Color MAU_DO = new Color(220, 53, 69);
    private static final Color MAU_XANH_LA = new Color(22, 130, 70);
    private static final Color MAU_CAM = new Color(180, 83, 9);
    private static final Color MAU_NUT_PHU = new Color(241, 245, 249);
    private static final Color MAU_VIEN_NUT_PHU = new Color(180, 190, 205);
    private static final Font FONT_THUONG = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_DAM = new Font("Segoe UI", Font.BOLD, 13);
    // Trạng thái thanh toán
    private static final String TAT_CA = "Tất cả";
    private static final String DA_THANH_TOAN = "Đã thanh toán";
    private static final String CHUA_THANH_TOAN = "Chưa thanh toán";
    private static final String THANH_TOAN_MOT_PHAN = "Thanh toán một phần";
    // Ngày giờ, số
    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DF_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat NF_TIEN = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
    // Câu lệnh SQL
    // LEFT JOIN để hóa đơn không có phiếu sửa chữa (mua phụ tùng) vẫn hiện ra
    private static final String SQL_TIM_KIEM = """
            SELECT hd.maHoaDon, hd.maPhieuSuaChua, hd.maNV,
                   kh.hoTen AS tenKhachHang, x.bienSo, hd.maKM,
                   hd.ngayLap, hd.soTien, hd.trangThai
            FROM HoaDon hd
            LEFT JOIN PhieuSuaChua p ON hd.maPhieuSuaChua = p.maPhieuSuaChua
            LEFT JOIN KhachHang kh ON p.maKH = kh.maKH
            LEFT JOIN Xe x ON p.maXe = x.maXe
            WHERE (? = '' OR hd.maHoaDon LIKE ?)
              AND (? = '' OR hd.maPhieuSuaChua LIKE ?)
              AND (? = '' OR hd.maNV LIKE ?)
              AND (? = '' OR ISNULL(hd.maKM, '') LIKE ?)
              AND (? = '' OR p.maKH LIKE ? OR kh.hoTen LIKE ?)
              AND (? = '' OR p.maXe LIKE ? OR x.bienSo LIKE ?)
              AND (? IS NULL OR (hd.ngayLap >= ? AND hd.ngayLap < DATEADD(DAY, 1, ?)))
              AND (? IS NULL OR hd.trangThai = ?)
            ORDER BY hd.ngayLap DESC
            """;
    // Ô nhập điều kiện
    private JTextField txtMaHoaDon;
    private JTextField txtMaPhieuSuaChua;
    private JTextField txtMaNV;
    private JTextField txtMaKM;
    private JTextField txtKhachHang; // mã khách hàng hoặc tên khách hàng
    private JTextField txtXe; // mã xe hoặc biển số xe
    private JDateChooser dchNgayLap;
    private JComboBox<String> cmbTrangThai;
    // Nút
    private JButton btnTimKiem;
    private JButton btnLamMoi;
    private JButton btnXemChiTiet;
    // Bảng kết quả
    private JTable tblHoaDon;
    private DefaultTableModel modelHoaDon;
    private final JLabel lblKetQua = new JLabel(" ");
    // Tránh tìm kiếm lặp nhiều lần khi bấm "Làm mới"
    private boolean dangLamMoi = false;

    // 2. KHỞI TẠO
    public FrmTimKiemHoaDon() {
        setLayout(new BorderLayout(0, 12));
        setBackground(MAU_NEN);
        setBorder(new EmptyBorder(14, 18, 14, 18));
        dungGiaoDien();
        caiDatSuKien();
        timKiemHoaDon();
    }

    // 3. XÂY DỰNG GIAO DIỆN CHÍNH
    private void dungGiaoDien() {
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 10));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoTheTimKiem(), BorderLayout.NORTH);
        pnlNoiDung.add(taoTheKetQua(), BorderLayout.CENTER);
        add(taoTieuDe(), BorderLayout.NORTH);
        add(pnlNoiDung, BorderLayout.CENTER);
    }

    // Tiêu đề
    private JLabel taoTieuDe() {
        JLabel lblTieuDe = new JLabel("TÌM KIẾM HÓA ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTieuDe.setForeground(MAU_CHU);
        lblTieuDe.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lblTieuDe;
    }

    private The taoTheTimKiem() {
        The pnlThe = new The("ĐIỀU KIỆN TÌM KIẾM");
        pnlThe.pnlNoiDung.setLayout(new BorderLayout());
        pnlThe.pnlNoiDung.setBorder(new EmptyBorder(8, 12, 10, 12));
        txtMaHoaDon = oNhap();
        txtMaPhieuSuaChua = oNhap();
        txtMaNV = oNhap();
        txtMaKM = oNhap();
        txtKhachHang = oNhap();
        txtXe = oNhap();
        txtKhachHang.setToolTipText("Nhập mã khách hàng hoặc tên khách hàng");
        txtXe.setToolTipText("Nhập mã xe hoặc biển số xe");

        dchNgayLap = new JDateChooser();
        dchNgayLap.setDateFormatString("dd/MM/yyyy");
        dchNgayLap.setFont(FONT_THUONG);
        dchNgayLap.setMaxSelectableDate(toDate(LocalDate.now(MUI_GIO)));
        dchNgayLap.setToolTipText("Chọn ngày lập hóa đơn, không được chọn ngày tương lai");

        cmbTrangThai = new JComboBox<>(new String[] {
                TAT_CA, DA_THANH_TOAN, CHUA_THANH_TOAN, THANH_TOAN_MOT_PHAN });
        cmbTrangThai.setFont(FONT_THUONG);
        cmbTrangThai.setBackground(Color.WHITE);

        btnTimKiem = taoNut("Tìm kiếm", MAU_NUT, MAU_NUT_DAM, Color.WHITE, MAU_NUT_DAM);
        btnLamMoi = taoNut("Làm mới", Color.WHITE, MAU_NUT_PHU, MAU_CHU, MAU_VIEN_NUT_PHU);
        Dimension dNut = new Dimension(90, 28);
        for (JButton b : new JButton[] { btnTimKiem, btnLamMoi }) {
            b.setPreferredSize(dNut);
            b.setMinimumSize(dNut);
        }

        // 8 ô nhập: 2 hàng x 4 cột
        JPanel pnlLuoi = new JPanel(new GridLayout(2, 4, 12, 6));
        pnlLuoi.setOpaque(false);
        pnlLuoi.add(taoO("Mã hóa đơn", txtMaHoaDon));
        pnlLuoi.add(taoO("Mã phiếu sửa chữa", txtMaPhieuSuaChua));
        pnlLuoi.add(taoO("Mã nhân viên", txtMaNV));
        pnlLuoi.add(taoO("Mã khuyến mãi", txtMaKM));
        pnlLuoi.add(taoO("Mã / tên khách hàng", txtKhachHang));
        pnlLuoi.add(taoO("Mã xe / biển số xe", txtXe));
        pnlLuoi.add(taoO("Ngày lập hóa đơn", dchNgayLap));
        pnlLuoi.add(taoO("Trạng thái thanh toán", cmbTrangThai));

        // Cột nút bên phải: mỗi nút thẳng hàng với ô nhập của từng hàng
        JPanel pnlNut = new JPanel(new GridLayout(2, 1, 0, 6));
        pnlNut.setOpaque(false);
        pnlNut.setBorder(new EmptyBorder(0, 16, 0, 0));
        pnlNut.add(oNut(btnTimKiem));
        pnlNut.add(oNut(btnLamMoi));

        pnlThe.pnlNoiDung.add(pnlLuoi, BorderLayout.CENTER);
        pnlThe.pnlNoiDung.add(pnlNut, BorderLayout.EAST);
        return pnlThe;
    }

    // Bọc nút sát đáy ô để thẳng hàng với các ô nhập
    private JPanel oNut(JButton btn) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.add(btn, BorderLayout.SOUTH);
        return pnl;
    }

    // Một ô: nhãn phía trên, ô nhập phía dưới, cùng căn lề trái
    private JPanel taoO(String nhan, JComponent input) {
        JPanel pnlO = new JPanel();
        pnlO.setOpaque(false);
        pnlO.setLayout(new BoxLayout(pnlO, BoxLayout.Y_AXIS));

        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNhan.setForeground(MAU_PHU);
        lblNhan.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setPreferredSize(new Dimension(100, 28));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        pnlO.add(lblNhan);
        pnlO.add(Box.createVerticalStrut(3));
        pnlO.add(input);
        return pnlO;
    }

    // Thẻ kết quả
    private The taoTheKetQua() {
        The pnlThe = new The("DANH SÁCH HÓA ĐƠN");
        pnlThe.pnlNoiDung.setLayout(new BorderLayout(0, 10));

        lblKetQua.setFont(FONT_DAM);
        lblKetQua.setForeground(MAU_PHU);
        pnlThe.themBenPhaiTieuDe(lblKetQua);

        JScrollPane scrBang = new JScrollPane(taoBang());
        scrBang.setBorder(BorderFactory.createLineBorder(MAU_VIEN));
        scrBang.getViewport().setBackground(Color.WHITE);

        // Hàng dưới: gợi ý bên trái, nút xem chi tiết bên phải
        btnXemChiTiet = taoNut("Xem chi tiết", MAU_XANH_LA, new Color(21, 110, 60),
                Color.WHITE, new Color(21, 110, 60));
        btnXemChiTiet.setEnabled(false);

        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.setOpaque(false);
        pnlDuoi.add(btnXemChiTiet, BorderLayout.EAST);

        pnlThe.pnlNoiDung.add(scrBang, BorderLayout.CENTER);
        pnlThe.pnlNoiDung.add(pnlDuoi, BorderLayout.SOUTH);
        return pnlThe;
    }

    private JTable taoBang() {
        String[] cot = { "Mã hóa đơn", "Mã phiếu sửa chữa", "Mã nhân viên", "Tên khách hàng", "Biển số xe",
                "Mã khuyến mãi", "Ngày lập hóa đơn", "Tổng tiền (đ)", "Trạng thái thanh toán" };
        modelHoaDon = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Bảng có kẻ sọc xen kẽ để dễ đọc
        tblHoaDon = new JTable(modelHoaDon) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(246, 248, 252));
                }
                return c;
            }
        };

        tblHoaDon.setFont(FONT_THUONG);
        tblHoaDon.setForeground(MAU_CHU);
        tblHoaDon.setRowHeight(30);
        tblHoaDon.setShowVerticalLines(false);
        tblHoaDon.setGridColor(MAU_VIEN);
        tblHoaDon.setIntercellSpacing(new Dimension(0, 1));
        tblHoaDon.setFillsViewportHeight(true);
        tblHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHoaDon.setSelectionBackground(new Color(219, 234, 254));
        tblHoaDon.setSelectionForeground(MAU_CHU);
        // Các cột tự giãn đầy chiều ngang, không cần kéo thanh cuộn ngang
        tblHoaDon.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Tiêu đề cột
        JTableHeader tbhHoaDon = tblHoaDon.getTableHeader();
        tbhHoaDon.setReorderingAllowed(false);
        tbhHoaDon.setPreferredSize(new Dimension(0, 36));
        tbhHoaDon.setDefaultRenderer(new TieuDeCotRenderer());

        // Độ rộng ưa thích (tỉ lệ giữa các cột): 0 Mã HĐ, 1 Mã phiếu, 2 Mã NV, 3 Tên
        // KH, 4 Biển số, 5 Mã KM, 6 Ngày lập, 7 Tổng tiền, 8 Trạng thái
        int[] doRong = { 105, 140, 105, 175, 110, 110, 140, 120, 165 };
        TableColumnModel cols = tblHoaDon.getColumnModel();
        for (int i = 0; i < doRong.length; i++) {
            cols.getColumn(i).setPreferredWidth(doRong[i]);
        }

        // Canh lề: mã, biển số, ngày ở giữa; tên khách hàng canh trái; tổng tiền canh
        // phải; trạng thái tô màu
        for (int i = 0; i <= 6; i++) {
            int canLe = (i == 3) ? SwingConstants.LEFT : SwingConstants.CENTER;
            cols.getColumn(i).setCellRenderer(new OCanLe(canLe, i == 0, false));
        }
        cols.getColumn(7).setCellRenderer(new OCanLe(SwingConstants.RIGHT, true, false));
        cols.getColumn(8).setCellRenderer(new OCanLe(SwingConstants.CENTER, true, true));
        return tblHoaDon;
    }

    // 4. CÁC LỚP HỖ TRỢ GIAO DIỆN
    // Thẻ (card) có thanh tiêu đề
    private static class The extends JPanel {
        final JPanel pnlNoiDung = new JPanel(new GridBagLayout());
        private final JPanel pnlDau = new JPanel(new BorderLayout(8, 0));

        The(String tieuDe) {
            super(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(MAU_VIEN));

            JLabel lblTieuDe = new JLabel(tieuDe);
            lblTieuDe.setFont(FONT_DAM);
            lblTieuDe.setForeground(MAU_NUT_DAM);

            pnlDau.setBackground(new Color(240, 245, 255));
            pnlDau.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                    new EmptyBorder(8, 12, 8, 12)));
            pnlDau.add(lblTieuDe, BorderLayout.WEST);

            pnlNoiDung.setBackground(Color.WHITE);
            pnlNoiDung.setBorder(new EmptyBorder(12, 14, 12, 14));

            add(pnlDau, BorderLayout.NORTH);
            add(pnlNoiDung, BorderLayout.CENTER);
        }

        void themBenPhaiTieuDe(JComponent comp) {
            pnlDau.add(comp, BorderLayout.EAST);
        }
    }

    // Vẽ nền nút (giữ đúng màu trên mọi Look and Feel, nút bị khóa thì xám)
    private static class NenNutUI extends BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton btn = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(btn.isEnabled() ? btn.getBackground() : new Color(226, 232, 240));
            g2.fillRect(0, 0, btn.getWidth(), btn.getHeight());
            g2.dispose();
            super.paint(g, c);
        }
    }

    // Renderer canh lề cho từng cột; có thể in đậm hoặc tô màu theo trạng thái
    private static class OCanLe extends DefaultTableCellRenderer {
        private final boolean dam;
        private final boolean laTrangThai;

        OCanLe(int canLe, boolean dam, boolean laTrangThai) {
            this.dam = dam;
            this.laTrangThai = laTrangThai;
            setHorizontalAlignment(canLe);
            setBorder(new EmptyBorder(0, 8, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row,
                int col) {
            Component c = super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
            setFont(dam || laTrangThai ? FONT_DAM : FONT_THUONG);
            if (laTrangThai) {
                setForeground(mauTrangThai(val == null ? "" : val.toString()));
            }
            return c;
        }
    }

    // Tiêu đề cột: chữ đậm, căn giữa, nền xanh nhạt
    private static class TieuDeCotRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row,
                int col) {
            super.getTableCellRendererComponent(tbl, val, false, false, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(FONT_DAM);
            setOpaque(true);
            setBackground(new Color(240, 245, 255));
            setForeground(MAU_CHU);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                    new EmptyBorder(0, 6, 0, 6)));
            return this;
        }
    }

    private static Color mauTrangThai(String trangThai) {
        if (DA_THANH_TOAN.equals(trangThai))
            return MAU_XANH_LA;
        if (THANH_TOAN_MOT_PHAN.equals(trangThai))
            return MAU_CAM;
        if (CHUA_THANH_TOAN.equals(trangThai))
            return MAU_DO;
        return MAU_CHU;
    }

    // 5. SỰ KIỆN
    private void caiDatSuKien() {
        btnTimKiem.addActionListener(e -> timKiemHoaDon());
        btnLamMoi.addActionListener(e -> lamMoi());
        btnXemChiTiet.addActionListener(e -> xemChiTiet());

        // Nhấn Enter ở bất kỳ ô nhập nào cũng tìm kiếm
        for (JTextField txt : new JTextField[] { txtMaHoaDon, txtMaPhieuSuaChua, txtMaNV, txtMaKM,
                txtKhachHang, txtXe }) {
            txt.addActionListener(e -> timKiemHoaDon());
        }

        // Đổi trạng thái hoặc chọn ngày thì tự tìm lại
        cmbTrangThai.addActionListener(e -> timKiemKhiDoiDieuKien());
        dchNgayLap.addPropertyChangeListener("date", e -> timKiemKhiDoiDieuKien());

        // Nút "Xem chi tiết" chỉ sáng khi đã chọn một dòng
        tblHoaDon.getSelectionModel().addListSelectionListener(
                e -> btnXemChiTiet.setEnabled(tblHoaDon.getSelectedRow() >= 0));

        // Nhấp đúp vào dòng để xem chi tiết
        tblHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)
                        && tblHoaDon.getSelectedRow() >= 0) {
                    xemChiTiet();
                }
            }
        });
    }

    private void timKiemKhiDoiDieuKien() {
        if (!dangLamMoi) {
            timKiemHoaDon();
        }
    }

    private void lamMoi() {
        dangLamMoi = true;

        for (JTextField txt : new JTextField[] { txtMaHoaDon, txtMaPhieuSuaChua, txtMaNV, txtMaKM,
                txtKhachHang, txtXe }) {
            txt.setText("");
        }
        dchNgayLap.setDate(null);
        cmbTrangThai.setSelectedIndex(0);

        dangLamMoi = false;

        timKiemHoaDon();
        txtMaHoaDon.requestFocus();
    }

    // 6. NGHIỆP VỤ: TÌM KIẾM (CSDL)
    private void timKiemHoaDon() {
        LocalDate ngayChon = layNgay(dchNgayLap);

        // Kiểm tra bổ sung để tránh ngày tương lai (trường hợp gõ tay)
        if (ngayChon != null && ngayChon.isAfter(LocalDate.now(MUI_GIO))) {
            JOptionPane.showMessageDialog(this, "Ngày lập không được lớn hơn ngày hiện tại.",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String trangThai = (String) cmbTrangThai.getSelectedItem();
        if (TAT_CA.equals(trangThai)) {
            trangThai = null;
        }

        modelHoaDon.setRowCount(0);

        try {
            // Dùng chung kết nối của ứng dụng, KHÔNG đóng sau khi tìm xong
            Connection conn = ConnectDB.getConnection();
            if (conn == null || conn.isClosed()) {
                datKetQua("Chưa kết nối được cơ sở dữ liệu", MAU_DO);
                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối SQL Server.\nVui lòng kiểm tra cấu hình kết nối.",
                        "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(SQL_TIM_KIEM)) {
                // Thứ tự tham số khớp với SQL_TIM_KIEM: giá trị rồi các mẫu LIKE
                int i = 1;
                i = datThamSoChuoi(ps, i, txtMaHoaDon.getText().trim(), 1);
                i = datThamSoChuoi(ps, i, txtMaPhieuSuaChua.getText().trim(), 1);
                i = datThamSoChuoi(ps, i, txtMaNV.getText().trim(), 1);
                i = datThamSoChuoi(ps, i, txtMaKM.getText().trim(), 1);
                i = datThamSoChuoi(ps, i, txtKhachHang.getText().trim(), 2); // mã hoặc tên khách hàng
                i = datThamSoChuoi(ps, i, txtXe.getText().trim(), 2); // mã xe hoặc biển số xe

                // Ngày lập: (IS NULL, >=, <) cùng một mốc ngày
                Timestamp mocNgay = ngayChon == null ? null : Timestamp.valueOf(ngayChon.atStartOfDay());
                for (int k = 0; k < 3; k++) {
                    ps.setTimestamp(i++, mocNgay);
                }
                // Trạng thái: (IS NULL, =)
                for (int k = 0; k < 2; k++) {
                    ps.setString(i++, trangThai);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp ngayLap = rs.getTimestamp("ngayLap");

                        modelHoaDon.addRow(new Object[] {
                                rs.getString("maHoaDon"),
                                rs.getString("maPhieuSuaChua"),
                                rs.getString("maNV"),
                                rs.getString("tenKhachHang"),
                                rs.getString("bienSo"),
                                rs.getString("maKM"),
                                ngayLap == null ? "" : ngayLap.toLocalDateTime().format(DF_NGAY),
                                dinhDangTien(rs.getBigDecimal("soTien")),
                                rs.getString("trangThai")
                        });
                    }
                }
            }

            // Thông báo kết quả ngay trên tiêu đề thẻ, không bật hộp thoại làm phiền
            if (modelHoaDon.getRowCount() == 0) {
                datKetQua("Không tìm thấy hóa đơn phù hợp", MAU_CAM);
            } else {
                datKetQua("Tìm thấy " + modelHoaDon.getRowCount() + " hóa đơn", MAU_XANH_LA);
            }

        } catch (SQLException ex) {
            datKetQua("Lỗi tìm kiếm", MAU_DO);
            JOptionPane.showMessageDialog(this,
                    "Không thể tìm kiếm hóa đơn.\nChi tiết: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Gán tham số chuỗi: giá trị gốc, rồi soLike mẫu "%giá trị%". Trả về vị trí
    // tham số kế tiếp.
    private int datThamSoChuoi(PreparedStatement ps, int i, String giaTri, int soLike) throws SQLException {
        ps.setString(i++, giaTri);
        for (int k = 0; k < soLike; k++) {
            ps.setString(i++, "%" + giaTri + "%");
        }
        return i;
    }

    private void datKetQua(String text, Color mau) {
        lblKetQua.setText(text);
        lblKetQua.setForeground(mau);
    }

    private String dinhDangTien(BigDecimal so) {
        return so == null ? "0" : NF_TIEN.format(so);
    }

    // maKH, maXe lấy qua phiếu sửa chữa; thueVAT, tienGiamGia, soTien lấy từ hóa
    // đơn
    private static final String SQL_THONG_TIN_PHU = """
            SELECT p.maKH, p.maXe, hd.thueVAT, hd.tienGiamGia, hd.soTien
            FROM HoaDon hd
            LEFT JOIN PhieuSuaChua p ON hd.maPhieuSuaChua = p.maPhieuSuaChua
            WHERE hd.maHoaDon = ?
            """;

    private static final String SQL_DONG_HANG = """
            SELECT 'D' AS loai, dv.maDV AS ma, dv.tenDV AS ten, ct.soLuong, ct.donGia
            FROM ChiTietHoaDon ct JOIN DichVu dv ON ct.maDV = dv.maDV
            WHERE ct.maHoaDon = ?
            UNION ALL
            SELECT 'P', pt.maPhuTung, pt.tenPhuTung, ct.soLuong, ct.donGia
            FROM ChiTietHoaDon ct JOIN PhuTung pt ON ct.maPhuTung = pt.maPhuTung
            WHERE ct.maHoaDon = ?
            ORDER BY loai
            """;

    private static final String SQL_DA_THANH_TOAN = """
            SELECT ISNULL(SUM(soTienThanhToan), 0)
            FROM GiaoDichThanhToan
            WHERE maHoaDon = ?
              AND trangThaiGD = N'Thành công'
            """;

    private static class ChiTietHD {
        String maKH = "", maXe = "";
        BigDecimal tienThue = BigDecimal.ZERO;
        BigDecimal tienGiam = BigDecimal.ZERO;
        BigDecimal tongThanhToan = BigDecimal.ZERO;
        BigDecimal tienDichVu = BigDecimal.ZERO;
        BigDecimal tienPhuTung = BigDecimal.ZERO;
        BigDecimal daThanhToan = BigDecimal.ZERO;
        DefaultTableModel modelDong = new DefaultTableModel(
                new String[] { "STT", "Mã", "Tên hàng hóa, dịch vụ", "SL", "Đơn giá", "Thành tiền" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private ChiTietHD taiChiTiet(String maHD) throws SQLException {
        ChiTietHD ct = new ChiTietHD();
        Connection conn = ConnectDB.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(SQL_THONG_TIN_PHU)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ct.maKH = rs.getString("maKH");
                    ct.maXe = rs.getString("maXe");
                    ct.tienThue = nz(rs.getBigDecimal("thueVAT"));
                    ct.tienGiam = nz(rs.getBigDecimal("tienGiamGia"));
                    ct.tongThanhToan = nz(rs.getBigDecimal("soTien"));
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_DONG_HANG)) {
            ps.setString(1, maHD);
            ps.setString(2, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    int sl = rs.getInt("soLuong");
                    BigDecimal donGia = nz(rs.getBigDecimal("donGia"));
                    BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(sl));
                    if ("D".equals(rs.getString("loai"))) {
                        ct.tienDichVu = ct.tienDichVu.add(thanhTien);
                    } else {
                        ct.tienPhuTung = ct.tienPhuTung.add(thanhTien);
                    }
                    ct.modelDong.addRow(new Object[] { stt++, rs.getString("ma"), rs.getString("ten"), sl,
                            dinhDangTien(donGia), dinhDangTien(thanhTien) });
                }
            }
        }

        // Lấy tổng số tiền đã thanh toán thành công
        try (PreparedStatement ps = conn.prepareStatement(SQL_DA_THANH_TOAN)) {
            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ct.daThanhToan = nz(rs.getBigDecimal(1));
                }
            }
        }
        return ct;
    }

    private BigDecimal nz(BigDecimal so) {
        return so == null ? BigDecimal.ZERO : so;
    }

    private void xemChiTiet() {
        int row = tblHoaDon.convertRowIndexToModel(tblHoaDon.getSelectedRow());
        String maHD = giaTriHienThi(modelHoaDon.getValueAt(row, 0));
        Object rawPhieu = modelHoaDon.getValueAt(row, 1);
        String maPhieu = (rawPhieu == null || rawPhieu.toString().isBlank()) ? null : rawPhieu.toString();
        String trangThai = giaTriHienThi(modelHoaDon.getValueAt(row, 8));

        ChiTietHD ct;
        try {
            ct = taiChiTiet(maHD);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết hóa đơn.\nChi tiết: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Tính tiền
        BigDecimal tongHoaDon = ct.tienDichVu.add(ct.tienPhuTung); // chưa gồm VAT
        boolean daTT = DA_THANH_TOAN.equals(trangThai);
        boolean motPhan = THANH_TOAN_MOT_PHAN.equals(trangThai);
        boolean chuaTT = CHUA_THANH_TOAN.equals(trangThai);

        BigDecimal daTra = daTT ? ct.tongThanhToan : (motPhan ? ct.daThanhToan : BigDecimal.ZERO);
        BigDecimal conLai = ct.tongThanhToan.subtract(daTra).max(BigDecimal.ZERO);

        Window winCha = SwingUtilities.getWindowAncestor(this);
        JDialog winChiTiet = new JDialog(winCha, "Thông tin hóa đơn " + maHD,
                Dialog.ModalityType.APPLICATION_MODAL);

        JLabel lblTieuDe = new JLabel("HÓA ĐƠN " + maHD, SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTieuDe.setForeground(MAU_CHU);

        // Thẻ thông tin chung: 3 thông tin / dòng
        The pnlThongTin = new The("THÔNG TIN CHUNG");
        pnlThongTin.pnlNoiDung.setLayout(new GridLayout(0, 3, 20, 8));
        String[][] cap = {
                { "Khách hàng", giaTriHienThi(modelHoaDon.getValueAt(row, 3)) },
                { "Mã khách hàng", giaTriHienThi(ct.maKH) },
                { "Ngày lập", giaTriHienThi(modelHoaDon.getValueAt(row, 6)) },
                { "Biển số xe", giaTriHienThi(modelHoaDon.getValueAt(row, 4)) },
                { "Mã xe", giaTriHienThi(ct.maXe) },
                { "Mã nhân viên", giaTriHienThi(modelHoaDon.getValueAt(row, 2)) },
                { "Phiếu sửa chữa", giaTriHienThi(maPhieu) },
                { "Mã khuyến mãi", giaTriHienThi(modelHoaDon.getValueAt(row, 5)) },
                { "Trạng thái", trangThai } };
        for (String[] c : cap) {
            JPanel pnlCap = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            pnlCap.setOpaque(false);
            JLabel lblNhan = new JLabel(c[0] + ":");
            lblNhan.setFont(FONT_THUONG);
            lblNhan.setForeground(MAU_PHU);
            JLabel lblGiaTri = new JLabel(c[1]);
            lblGiaTri.setFont(FONT_DAM);
            lblGiaTri.setForeground("Trạng thái".equals(c[0]) ? mauTrangThai(c[1]) : MAU_CHU);
            pnlCap.add(lblNhan);
            pnlCap.add(lblGiaTri);
            pnlThongTin.pnlNoiDung.add(pnlCap);
        }

        // Thẻ bảng dịch vụ, phụ tùng (cao theo số dòng)
        The pnlBang = new The("CHI TIẾT DỊCH VỤ, PHỤ TÙNG");
        pnlBang.pnlNoiDung.setLayout(new BorderLayout());
        JTable tblDong = new JTable(ct.modelDong);
        tblDong.setFont(FONT_THUONG);
        tblDong.setForeground(MAU_CHU);
        tblDong.setRowHeight(28);
        tblDong.setGridColor(MAU_VIEN);
        tblDong.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblDong.getTableHeader().setReorderingAllowed(false);
        tblDong.getTableHeader().setPreferredSize(new Dimension(0, 32));
        tblDong.getTableHeader().setDefaultRenderer(new TieuDeCotRenderer());
        int[] rong = { 40, 90, 300, 50, 120, 130 };
        int[] canLe = { SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT,
                SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.RIGHT };
        for (int i = 0; i < rong.length; i++) {
            tblDong.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
            tblDong.getColumnModel().getColumn(i).setCellRenderer(new OCanLe(canLe[i], i == 5, false));
        }
        // Không bọc JScrollPane: tự thêm tiêu đề cột, bảng cao đúng bằng số dòng
        JPanel pnlBangDong = new JPanel(new BorderLayout());
        pnlBangDong.setBorder(BorderFactory.createLineBorder(MAU_VIEN));
        pnlBangDong.add(tblDong.getTableHeader(), BorderLayout.NORTH);
        pnlBangDong.add(tblDong, BorderLayout.CENTER);
        pnlBang.pnlNoiDung.add(pnlBangDong, BorderLayout.CENTER);
        // Thẻ tổng kết
        The pnlTien = new The("TỔNG KẾT");
        int h = 0;
        themDongChiTiet(pnlTien.pnlNoiDung, h++, "Tổng hóa đơn (chưa gồm VAT)",
                dinhDangTien(tongHoaDon) + " đ", MAU_CHU, true);
        themDongChiTiet(pnlTien.pnlNoiDung, h++, "Tiền thuế (VAT)",
                dinhDangTien(ct.tienThue) + " đ", MAU_CHU, false);
        themDongChiTiet(pnlTien.pnlNoiDung, h++, "Tiền giảm",
                "- " + dinhDangTien(ct.tienGiam) + " đ", MAU_DO, false);
        themDongChiTiet(pnlTien.pnlNoiDung, h++, "TỔNG THANH TOÁN",
                dinhDangTien(ct.tongThanhToan) + " đ", MAU_NUT, true);

        if (daTT) {
            themDongChiTiet(pnlTien.pnlNoiDung, h++, "Đã thanh toán",
                    dinhDangTien(daTra) + " đ", MAU_XANH_LA, true);
        } else if (motPhan) {
            themDongChiTiet(pnlTien.pnlNoiDung, h++, "Đã thanh toán",
                    dinhDangTien(daTra) + " đ", MAU_XANH_LA, true);
            themDongChiTiet(pnlTien.pnlNoiDung, h++, "Còn lại",
                    dinhDangTien(conLai) + " đ", MAU_DO, true);
        } else if (chuaTT) {
            themDongChiTiet(pnlTien.pnlNoiDung, h++, "Còn lại",
                    dinhDangTien(conLai) + " đ", MAU_DO, true);
        }

        // Nút
        JButton btnDong = taoNut("Đóng", Color.WHITE, MAU_NUT_PHU, MAU_CHU, MAU_VIEN_NUT_PHU);
        btnDong.addActionListener(e -> winChiTiet.dispose());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnDong);

        if (chuaTT || motPhan) {
            JButton btnThanhToan = taoNut("Thanh toán", MAU_NUT, MAU_NUT_DAM, Color.WHITE, MAU_NUT_DAM);
            btnThanhToan.addActionListener(e -> {
                winChiTiet.dispose();
                moFrmThanhToan(maHD);
            });
            pnlNut.add(btnThanhToan);
            winChiTiet.getRootPane().setDefaultButton(btnThanhToan);
        } else {
            winChiTiet.getRootPane().setDefaultButton(btnDong);
        }

        // Ghép: phần giữa nằm trong khung cuộn
        JPanel pnlGiua = new JPanel(new BorderLayout(0, 12));
        pnlGiua.setOpaque(true);
        pnlGiua.setBackground(MAU_NEN);
        pnlGiua.add(pnlThongTin, BorderLayout.NORTH);
        pnlGiua.add(pnlBang, BorderLayout.CENTER);
        pnlGiua.add(pnlTien, BorderLayout.SOUTH);

        JScrollPane scrGiua = new JScrollPane(pnlGiua,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrGiua.setBorder(null);
        scrGiua.getViewport().setBackground(MAU_NEN);
        scrGiua.getVerticalScrollBar().setUnitIncrement(18);

        JPanel pnlRoot = new JPanel(new BorderLayout(0, 12));
        pnlRoot.setBackground(MAU_NEN);
        pnlRoot.setBorder(new EmptyBorder(16, 20, 16, 20));
        pnlRoot.add(lblTieuDe, BorderLayout.NORTH);
        pnlRoot.add(scrGiua, BorderLayout.CENTER);
        pnlRoot.add(pnlNut, BorderLayout.SOUTH);

        winChiTiet.setContentPane(pnlRoot);
        winChiTiet.pack();

        // Giới hạn chiều cao theo màn hình; cao hơn thì cuộn
        Rectangle man = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int caoToiDa = man.height - 40;
        boolean canCuon = winChiTiet.getHeight() > caoToiDa;
        int rongDialog = Math.max(winChiTiet.getWidth(), 800) + (canCuon ? 20 : 0);
        winChiTiet.setSize(rongDialog, Math.min(winChiTiet.getHeight(), caoToiDa));
        winChiTiet.setResizable(false);
        winChiTiet.setLocationRelativeTo(this);
        winChiTiet.setVisible(true);
    }

    // Mở màn hình thanh toán cho hóa đơn; đóng xong thì tìm kiếm lại để cập nhật
    // trạng thái
    private void moFrmThanhToan(String maHD) {
        Object frm = new FrmThanhToan(maHD);

        if (frm instanceof JFrame) {
            JFrame f = (JFrame) frm;
            f.setLocationRelativeTo(this);
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    timKiemHoaDon();
                }
            });
            f.setVisible(true);
        } else if (frm instanceof JPanel) {
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Thanh toán hóa đơn " + maHD,
                    Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setContentPane((JPanel) frm);
            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
            timKiemHoaDon(); // dialog modal: chạy tiếp ở đây sau khi đóng
        }
    }

    // Một dòng "nhãn bên trái - giá trị bên phải"
    private void themDongChiTiet(JPanel pnl, int hang, String nhan, String giaTri, Color mau, boolean dam) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = hang;

        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 4, 0);
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(dam ? FONT_DAM : FONT_THUONG);
        lblNhan.setForeground(dam ? mau : MAU_PHU);
        pnl.add(lblNhan, c);

        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(4, 24, 4, 0);
        JLabel lblGiaTri = new JLabel(giaTri);
        lblGiaTri.setFont(dam ? new Font("Segoe UI", Font.BOLD, 14) : FONT_THUONG);
        lblGiaTri.setForeground(mau);
        pnl.add(lblGiaTri, c);
    }

    // Giá trị rỗng / null hiển thị là "—"
    private String giaTriHienThi(Object value) {
        return (value == null || value.toString().trim().isEmpty()) ? "---" : value.toString();
    }

    // 8. TIỆN ÍCH
    // Ngày
    private LocalDate layNgay(JDateChooser dch) {
        Date date = dch.getDate();
        return date == null ? null : date.toInstant().atZone(MUI_GIO).toLocalDate();
    }

    private Date toDate(LocalDate ngay) {
        return Date.from(ngay.atStartOfDay(MUI_GIO).toInstant());
    }

    // Thành phần giao diện
    private JTextField oNhap() {
        JTextField txt = new JTextField();
        txt.setFont(FONT_THUONG);
        txt.setForeground(MAU_CHU);
        txt.setBackground(Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAU_VIEN),
                new EmptyBorder(4, 8, 4, 8)));
        return txt;
    }

    private JButton taoNut(String text, Color nen, Color nenKhiRe, Color chu, Color vien) {
        JButton btn = new JButton(text);

        btn.setUI(new NenNutUI());
        btn.setFont(FONT_DAM);
        btn.setForeground(chu);
        btn.setBackground(nen);
        btn.setOpaque(true);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(vien, 1),
                BorderFactory.createEmptyBorder(4, 14, 4, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Bề rộng theo độ dài chữ, tối thiểu 100px, cao đều 36px
        int rong = Math.max(100, btn.getFontMetrics(FONT_DAM).stringWidth(text) + 44);
        btn.setPreferredSize(new Dimension(rong, 36));
        btn.setMinimumSize(new Dimension(rong, 36));

        // Đổi màu khi rê chuột (chỉ khi nút đang bật)
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(nenKhiRe);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(nen);
            }
        });
        return btn;
    }

    public static void main(String[] args) {
        ConnectDB.getInstance().connect();
        SwingUtilities.invokeLater(() -> {
            JFrame winTimKiemHoaDon = new JFrame("Tìm kiếm hóa đơn");
            winTimKiemHoaDon.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            winTimKiemHoaDon.setSize(1250, 720);
            winTimKiemHoaDon.setMinimumSize(new Dimension(1000, 600));
            winTimKiemHoaDon.setLocationRelativeTo(null);
            winTimKiemHoaDon.setContentPane(new FrmTimKiemHoaDon());
            winTimKiemHoaDon.setVisible(true);
        });
    }
}
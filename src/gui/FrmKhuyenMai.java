package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import com.toedter.calendar.JDateChooser;
import connectDB.ConnectDB;

public class FrmKhuyenMai extends JPanel {
    private static final Color BLUE_DAM = new Color(25, 118, 210);
    private static final Color NAVY = new Color(20, 59, 105);
    private static final Color NAVY_DAM = new Color(25, 55, 90);
    private static final Color BG = new Color(244, 249, 255);
    private static final Color BORDER = new Color(205, 224, 246);
    private static final Font F_TEXT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    // Chiều rộng chung của các ô trong khung tìm kiếm
    private static final int W_O_NHAP = 200;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Trạng thái (cột trangThai NVARCHAR(20))
    private static final String TT_DANG = "Đang áp dụng";
    private static final String TT_CHUA = "Chưa áp dụng";
    private static final String TT_HET = "Hết hạn";
    private static final String TT_TAM = "Tạm ngưng";
    private static final String TT_CHUA_XAC_DINH = "—"; // ngày chưa đủ / không hợp lệ
    private static final String TT_TAT_CA = "Tất cả";

    // Câu lệnh SQL
    private static final String SQL_TAI = """
            SELECT maKM, tenKM, tileGiam, ngayBatDau, ngayKetThuc, dieuKienApDung, trangThai
            FROM ChuongTrinhKhuyenMai
            ORDER BY ngayBatDau DESC
            """;
    private static final String SQL_THEM = """
            INSERT INTO ChuongTrinhKhuyenMai
                (maKM, tenKM, tileGiam, ngayBatDau, ngayKetThuc, dieuKienApDung, trangThai)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String SQL_CAP_NHAT = """
            UPDATE ChuongTrinhKhuyenMai
            SET tenKM = ?, tileGiam = ?, ngayBatDau = ?, ngayKetThuc = ?,
                dieuKienApDung = ?, trangThai = ?
            WHERE maKM = ?
            """;
    private static final String SQL_XOA = "DELETE FROM ChuongTrinhKhuyenMai WHERE maKM = ?";

    /* Một dòng của bảng ChuongTrinhKhuyenMai */
    private static class KhuyenMai {
        String maKM;
        String tenKM;
        BigDecimal tileGiam;
        LocalDate ngayBatDau;
        LocalDate ngayKetThuc;
        String dieuKienApDung;
        String trangThai;

        KhuyenMai(String ma, String ten, BigDecimal tile, LocalDate bd, LocalDate kt, String dk, String tt) {
            this.maKM = ma;
            this.tenKM = ten;
            this.tileGiam = tile;
            this.ngayBatDau = bd;
            this.ngayKetThuc = kt;
            this.dieuKienApDung = dk;
            this.trangThai = tt;
        }
    }

    // Dữ liệu
    private final List<KhuyenMai> dsKM = new ArrayList<>(); // toàn bộ dữ liệu
    private final List<KhuyenMai> dsHienThi = new ArrayList<>(); // dữ liệu sau khi lọc (khớp dòng trên bảng)

    // Khung tìm kiếm
    private JTextField txtTimMa, txtTimTen, txtTileTu, txtTileDen;
    private JDateChooser dchLocTuNgay, dchLocDenNgay;
    private JComboBox<String> cmbTimTrangThai;

    // Bảng
    private JTable tblKhuyenMai;
    private DefaultTableModel modelKhuyenMai;
    private JLabel lblTong;

    // 2. KHỞI TẠO
    public FrmKhuyenMai() {
        setLayout(new BorderLayout(0, 8));
        setBackground(BG);

        JPanel pnlBody = new JPanel(new BorderLayout(0, 12));
        pnlBody.setBackground(BG);
        pnlBody.setBorder(new EmptyBorder(0, 16, 16, 16));
        pnlBody.add(buildSearchPanel(), BorderLayout.NORTH);
        pnlBody.add(buildTablePanel(), BorderLayout.CENTER);

        add(buildTitle(), BorderLayout.NORTH);
        add(pnlBody, BorderLayout.CENTER);

        lamMoiDuLieu();
    }

    // 3. XÂY DỰNG GIAO DIỆN CHÍNH
    // Tiêu đề
    private JPanel buildTitle() {
        JPanel pnlTieuDe = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        pnlTieuDe.setBackground(BG);

        JLabel lblTieuDe = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTieuDe.setForeground(NAVY);
        pnlTieuDe.add(lblTieuDe);
        return pnlTieuDe;
    }

    private JPanel khung(String tieuDe, Component benPhai) {
        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(10, 14, 12, 14)));

        JLabel lblHeading = new JLabel(tieuDe);
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeading.setForeground(NAVY);

        JPanel pnlDau = new JPanel(new BorderLayout());
        pnlDau.setOpaque(false);
        pnlDau.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlDau.add(lblHeading, BorderLayout.WEST);
        if (benPhai != null) {
            pnlDau.add(benPhai, BorderLayout.EAST);
        }
        pnlKhung.add(pnlDau, BorderLayout.NORTH);
        return pnlKhung;
    }

    // Khung tìm kiếm / lọc
    private JPanel buildSearchPanel() {
        JPanel pnlTimKiem = khung("Tìm kiếm / Lọc", null);

        txtTimMa = oNhap(W_O_NHAP);
        txtTimTen = oNhap(W_O_NHAP);
        cmbTimTrangThai = new JComboBox<>(new String[] { TT_TAT_CA, TT_DANG, TT_CHUA, TT_HET, TT_TAM });
        kichThuoc(cmbTimTrangThai, W_O_NHAP, 30);
        cmbTimTrangThai.setFont(F_TEXT);
        cmbTimTrangThai.setBackground(Color.WHITE);
        txtTileTu = oNhap(90);
        txtTileDen = oNhap(90);

        // Hai ô ngày lọc chỉ chọn từ hôm nay trở đi
        Date homNay = Date.valueOf(LocalDate.now());
        dchLocTuNgay = oChonNgay(W_O_NHAP);
        dchLocDenNgay = oChonNgay(W_O_NHAP);
        dchLocTuNgay.setMinSelectableDate(homNay);
        dchLocDenNgay.setMinSelectableDate(homNay);

        // "Đến ngày" luôn >= "Hiệu lực từ ngày"; xóa "từ ngày" thì quay về mốc hôm nay
        dchLocTuNgay.addPropertyChangeListener("date", e -> {
            LocalDate tu = layNgay(dchLocTuNgay);
            LocalDate den = layNgay(dchLocDenNgay);

            dchLocDenNgay.setMinSelectableDate(Date.valueOf(tu != null ? tu : LocalDate.now()));
            if (tu != null && den != null && den.isBefore(tu)) {
                dchLocDenNgay.setDate(null);
            }
        });

        // Lưới nhãn + ô nhập: cột chẵn là nhãn, cột lẻ là ô nhập
        Component[][] luoi = {
                { nhan("Mã khuyến mãi:"), txtTimMa, nhan("Tên khuyến mãi:"), txtTimTen,
                        nhan("Trạng thái:"), cmbTimTrangThai },
                { nhan("Tỉ lệ giảm (%):"), taoOTileGiam(), nhan("Hiệu lực từ ngày:"), dchLocTuNgay,
                        nhan("Đến ngày:"), dchLocDenNgay } };

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 10);
        for (int y = 0; y < luoi.length; y++) {
            for (int x = 0; x < luoi[y].length; x++) {
                boolean laO = (x % 2 == 1);
                c.gridx = x;
                c.gridy = y;
                c.weightx = laO ? 1 : 0; // nhãn giữ nguyên, ô nhập giãn đều nhau
                c.fill = laO ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
                pnlForm.add(luoi[y][x], c);
            }
        }

        // Hai nút ở hàng riêng bên dưới các trường tìm kiếm
        GridBagConstraints cNut = new GridBagConstraints();
        cNut.gridx = 0;
        cNut.gridy = luoi.length;
        cNut.gridwidth = 6;
        cNut.weightx = 1;
        cNut.anchor = GridBagConstraints.EAST;
        cNut.fill = GridBagConstraints.HORIZONTAL;
        cNut.insets = new Insets(8, 0, 0, 0);
        pnlForm.add(taoNutTimKiem(), cNut);

        pnlTimKiem.add(pnlForm, BorderLayout.CENTER);
        return pnlTimKiem;
    }

    // Ô lọc tỉ lệ giảm: hai ô giãn đều, dấu "–" ở giữa
    private JPanel taoOTileGiam() {
        JPanel pnlTile = new JPanel(new GridBagLayout());
        pnlTile.setOpaque(false);

        JLabel lblGach = nhan("–");
        lblGach.setHorizontalAlignment(SwingConstants.CENTER);
        lblGach.setPreferredSize(new Dimension(24, 30));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        pnlTile.add(txtTileTu, c);
        c.weightx = 0;
        pnlTile.add(lblGach, c);
        c.weightx = 1;
        pnlTile.add(txtTileDen, c);

        kichThuoc(pnlTile, W_O_NHAP, 30);
        return pnlTile;
    }

    private JPanel taoNutTimKiem() {
        JButton btnTim = nutChinh("Tìm kiếm");
        JButton btnLamMoi = nutThuong("Làm mới");
        btnLamMoi.setForeground(NAVY_DAM);
        kichThuoc(btnTim, 130, 34);
        kichThuoc(btnLamMoi, 130, 34);

        btnTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> {
            txtTimMa.setText("");
            txtTimTen.setText("");
            cmbTimTrangThai.setSelectedIndex(0);
            txtTileTu.setText("");
            txtTileDen.setText("");
            dchLocTuNgay.setDate(null);
            dchLocDenNgay.setDate(null);
            timKiem();
        });

        // Nhấn Enter trong các ô nhập cũng tìm kiếm
        for (JTextField txt : new JTextField[] { txtTimMa, txtTimTen, txtTileTu, txtTileDen }) {
            txt.addActionListener(e -> timKiem());
        }

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnTim);
        pnlNut.add(btnLamMoi);
        return pnlNut;
    }

    // Danh sách khuyến mãi
    private JPanel buildTablePanel() {
        JButton btnThem = nutChinh("+  Thêm mới");
        btnThem.addActionListener(e -> moKhungThongTin(null));

        JPanel pnlDanhSach = khung("Danh sách khuyến mãi", btnThem);
        pnlDanhSach.add(taoBang(), BorderLayout.CENTER);
        pnlDanhSach.add(taoChanBang(), BorderLayout.SOUTH);
        return pnlDanhSach;
    }

    private JScrollPane taoBang() {
        String[] cot = { "Mã KM", "Tên chương trình khuyến mãi", "Tỉ lệ giảm", "Ngày bắt đầu", "Ngày kết thúc",
                "Điều kiện áp dụng", "Trạng thái" };
        modelKhuyenMai = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKhuyenMai = new JTable(modelKhuyenMai);
        tblKhuyenMai.setRowHeight(34);
        tblKhuyenMai.setFont(F_TEXT);
        tblKhuyenMai.setSelectionBackground(new Color(214, 235, 255));
        tblKhuyenMai.setSelectionForeground(Color.BLACK);
        tblKhuyenMai.setGridColor(new Color(225, 235, 246));
        tblKhuyenMai.setShowVerticalLines(false);
        tblKhuyenMai.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblKhuyenMai.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JTableHeader tbhKhuyenMai = tblKhuyenMai.getTableHeader();
        tbhKhuyenMai.setFont(F_BOLD);
        tbhKhuyenMai.setBackground(new Color(236, 245, 255));
        tbhKhuyenMai.setForeground(NAVY);
        tbhKhuyenMai.setReorderingAllowed(false);

        // Độ rộng cột
        int[] rong = { 80, 260, 85, 105, 105, 300, 115 };
        for (int i = 0; i < rong.length; i++) {
            tblKhuyenMai.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }

        // Căn giữa các cột mã, tỉ lệ, ngày; cột trạng thái tô màu
        DefaultTableCellRenderer rdrGiua = new DefaultTableCellRenderer();
        rdrGiua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[] { 0, 2, 3, 4 }) {
            tblKhuyenMai.getColumnModel().getColumn(i).setCellRenderer(rdrGiua);
        }
        tblKhuyenMai.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());

        // Nhấn vào dòng -> mở khung thông tin riêng
        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhuyenMai.rowAtPoint(e.getPoint());
                if (row >= 0 && row < dsHienThi.size()) {
                    moKhungThongTin(dsHienThi.get(row));
                }
            }
        });

        JScrollPane scrBang = new JScrollPane(tblKhuyenMai);
        scrBang.setBorder(new LineBorder(BORDER));
        scrBang.getViewport().setBackground(Color.WHITE);
        return scrBang;
    }

    private JPanel taoChanBang() {
        lblTong = new JLabel("Tổng số bản ghi: 0");
        lblTong.setFont(F_TEXT);
        lblTong.setForeground(NAVY);

        JPanel pnlChan = new JPanel(new BorderLayout());
        pnlChan.setBackground(Color.WHITE);
        pnlChan.setBorder(new EmptyBorder(8, 0, 0, 0));
        pnlChan.add(lblTong, BorderLayout.WEST);
        return pnlChan;
    }

    // Cột trạng thái: chữ đậm, căn giữa, màu theo từng trạng thái
    private static class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row,
                int col) {
            super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(F_BOLD);
            setForeground(mauTrangThai(String.valueOf(val)));
            return this;
        }
    }

    private static Color mauTrangThai(String trangThai) {
        if (TT_DANG.equals(trangThai))
            return new Color(22, 140, 70);
        if (TT_CHUA.equals(trangThai))
            return new Color(25, 110, 200);
        if (TT_TAM.equals(trangThai))
            return new Color(210, 120, 10);
        return new Color(140, 140, 140); // Hết hạn / chưa xác định
    }

    // 4. NGHIỆP VỤ: TẢI -> TÌM KIẾM -> TRẠNG THÁI -> MÃ MỚI
    // Tải lại dữ liệu từ CSDL rồi áp dụng lại bộ lọc hiện tại
    private void lamMoiDuLieu() {
        taiDanhSachKhuyenMai();
        timKiem();
    }

    private void timKiem() {
        String ma = txtTimMa.getText().trim().toLowerCase();
        String ten = txtTimTen.getText().trim().toLowerCase();
        String trangThaiLoc = (String) cmbTimTrangThai.getSelectedItem();

        // Kiểm tra điều kiện lọc
        BigDecimal tileTu, tileDen;
        try {
            tileTu = docSo(txtTileTu);
            tileDen = docSo(txtTileDen);
        } catch (NumberFormatException ex) {
            canhBao(this, "Tỉ lệ giảm lọc phải là số.");
            return;
        }

        LocalDate tuNgay = layNgay(dchLocTuNgay);
        LocalDate denNgay = layNgay(dchLocDenNgay);
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            canhBao(this, "Khoảng ngày lọc không hợp lệ (đến ngày < từ ngày).");
            return;
        }

        // Cập nhật trạng thái theo ngày rồi lọc
        capNhatTrangThaiTheoNgay();
        dsHienThi.clear();
        modelKhuyenMai.setRowCount(0);

        for (KhuyenMai km : dsKM) {
            if (!km.maKM.toLowerCase().contains(ma) || !km.tenKM.toLowerCase().contains(ten))
                continue;
            if (!TT_TAT_CA.equals(trangThaiLoc) && !km.trangThai.equals(trangThaiLoc))
                continue;
            if (tileTu != null && km.tileGiam.compareTo(tileTu) < 0)
                continue;
            if (tileDen != null && km.tileGiam.compareTo(tileDen) > 0)
                continue;
            // Khuyến mãi có hiệu lực giao với khoảng ngày đang lọc
            if (tuNgay != null && km.ngayKetThuc.isBefore(tuNgay))
                continue;
            if (denNgay != null && km.ngayBatDau.isAfter(denNgay))
                continue;

            dsHienThi.add(km);
            modelKhuyenMai.addRow(new Object[] {
                    km.maKM,
                    km.tenKM,
                    km.tileGiam.stripTrailingZeros().toPlainString() + "%",
                    km.ngayBatDau.format(DF),
                    km.ngayKetThuc.format(DF),
                    km.dieuKienApDung == null ? "" : km.dieuKienApDung.replace('\n', ' '),
                    km.trangThai });
        }
        lblTong.setText("Tổng số bản ghi: " + dsHienThi.size() + " / " + dsKM.size());
        tblKhuyenMai.clearSelection();
    }

    // Tự chuyển trạng thái theo ngày hiện tại (khuyến mãi "Tạm ngưng" giữ nguyên)
    private void capNhatTrangThaiTheoNgay() {
        for (KhuyenMai km : dsKM) {
            if (!TT_TAM.equals(km.trangThai)) {
                km.trangThai = tinhTrangThai(km.ngayBatDau, km.ngayKetThuc);
            }
        }
    }

    private String tinhTrangThai(LocalDate bd, LocalDate kt) {
        LocalDate homNay = LocalDate.now();
        if (homNay.isBefore(bd))
            return TT_CHUA;
        if (homNay.isAfter(kt))
            return TT_HET;
        return TT_DANG;
    }

    private KhuyenMai timTheoMa(String ma) {
        for (KhuyenMai km : dsKM) {
            if (km.maKM.equalsIgnoreCase(ma))
                return km;
        }
        return null;
    }

    // Sinh mã dạng KM001, KM002... theo số lớn nhất hiện có
    private String taoMaMoi() {
        int max = 0;
        for (KhuyenMai km : dsKM) {
            try {
                max = Math.max(max, Integer.parseInt(km.maKM.substring(2)));
            } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                // mã không theo dạng KMxxx -> bỏ qua
            }
        }
        return String.format("KM%03d", max + 1);
    }

    // 5. TRUY CẬP CSDL
    private void taiDanhSachKhuyenMai() {
        dsKM.clear();

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_TAI);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsKM.add(new KhuyenMai(
                        rs.getString("maKM"),
                        rs.getString("tenKM"),
                        rs.getBigDecimal("tileGiam"),
                        rs.getDate("ngayBatDau").toLocalDate(),
                        rs.getDate("ngayKetThuc").toLocalDate(),
                        rs.getString("dieuKienApDung"),
                        rs.getString("trangThai")));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách khuyến mãi từ CSDL!\n" + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean luuVaoCSDL(KhuyenMai km, boolean themMoi) {
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(themMoi ? SQL_THEM : SQL_CAP_NHAT)) {

            int i = 1;
            if (themMoi) {
                ps.setString(i++, km.maKM);
            }
            ps.setNString(i++, km.tenKM);
            ps.setBigDecimal(i++, km.tileGiam);
            ps.setDate(i++, Date.valueOf(km.ngayBatDau));
            ps.setDate(i++, Date.valueOf(km.ngayKetThuc));
            if (km.dieuKienApDung == null || km.dieuKienApDung.isEmpty()) {
                ps.setNull(i++, Types.NVARCHAR);
            } else {
                ps.setNString(i++, km.dieuKienApDung);
            }
            ps.setNString(i++, km.trangThai);
            if (!themMoi) {
                ps.setString(i, km.maKM);
            }

            if (ps.executeUpdate() > 0) {
                thongBao(themMoi ? "Thêm khuyến mãi thành công!" : "Cập nhật khuyến mãi thành công!",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
            thongBao(themMoi ? "Không thêm được khuyến mãi." : "Không tìm thấy khuyến mãi cần cập nhật.",
                    JOptionPane.WARNING_MESSAGE);
            return false;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lưu khuyến mãi vào CSDL!\n" + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean xoaKhoiCSDL(String maKM) {
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_XOA)) {

            ps.setString(1, maKM);

            if (ps.executeUpdate() > 0) {
                thongBao("Xóa khuyến mãi thành công!", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
            thongBao("Không tìm thấy khuyến mãi cần xóa trong CSDL.", JOptionPane.WARNING_MESSAGE);
            return false;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa khuyến mãi khỏi CSDL!\n"
                            + "Có thể khuyến mãi đang được tham chiếu bởi hóa đơn.\n"
                            + e.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // 6. HỘP THOẠI THÔNG TIN KHUYẾN MÃI
    // km == null: thêm mới; khác null: xem / sửa / xóa khuyến mãi đó
    private void moKhungThongTin(KhuyenMai km) {
        new DlgKhuyenMai(km).setVisible(true);
    }

    private class DlgKhuyenMai extends JDialog {
        private final KhuyenMai km;
        private final boolean themMoi;

        private final JTextField txtMa = oNhap(310);
        private final JTextField txtTen = oNhap(310);
        private final JTextField txtTile = oNhap(135);
        private final JDateChooser dchBatDau = oChonNgay(150);
        private final JDateChooser dchKetThuc = oChonNgay(150);
        private final JTextArea txaDieuKien = new JTextArea(4, 28);
        private final JCheckBox chkTamNgung = new JCheckBox("Tạm ngưng áp dụng");
        private final JLabel lblTrangThai = new JLabel();

        DlgKhuyenMai(KhuyenMai km) {
            super(SwingUtilities.getWindowAncestor(FrmKhuyenMai.this),
                    km == null ? "Thêm khuyến mãi" : "Thông tin khuyến mãi",
                    Dialog.ModalityType.APPLICATION_MODAL);
            this.km = km;
            this.themMoi = (km == null);
            setResizable(false);

            napDuLieu();
            ganSuKien();
            capNhatTrangThai();
            dungGiaoDien();

            pack();
            setLocationRelativeTo(FrmKhuyenMai.this);
            txtTen.requestFocusInWindow();
        }

        // Dữ liệu ban đầu
        private void napDuLieu() {
            txaDieuKien.setFont(F_TEXT);
            txaDieuKien.setLineWrap(true);
            txaDieuKien.setWrapStyleWord(true);
            chkTamNgung.setFont(F_TEXT);
            chkTamNgung.setOpaque(false);
            lblTrangThai.setFont(F_BOLD);

            if (themMoi) {
                LocalDate homNay = LocalDate.now();
                txtMa.setText(taoMaMoi());
                ganNgay(dchBatDau, homNay);
                dchBatDau.setMinSelectableDate(Date.valueOf(homNay)); // không chọn ngày bắt đầu trước hôm nay
            } else {
                txtMa.setText(km.maKM);
                txtMa.setEditable(false);
                txtMa.setBackground(new Color(238, 242, 247));
                txtTen.setText(km.tenKM);
                txtTile.setText(km.tileGiam.stripTrailingZeros().toPlainString());
                ganNgay(dchBatDau, km.ngayBatDau);
                ganNgay(dchKetThuc, km.ngayKetThuc);
                txaDieuKien.setText(km.dieuKienApDung == null ? "" : km.dieuKienApDung);
                chkTamNgung.setSelected(TT_TAM.equals(km.trangThai));
            }
            txaDieuKien.setCaretPosition(0);

            // Ngày kết thúc không được trước ngày bắt đầu (ngày bắt đầu luôn có giá trị ban
            // đầu)
            dchKetThuc.setMinSelectableDate(Date.valueOf(layNgay(dchBatDau)));
        }

        private void ganSuKien() {
            chkTamNgung.addActionListener(e -> capNhatTrangThai());
            dchKetThuc.addPropertyChangeListener("date", e -> capNhatTrangThai());
            dchBatDau.addPropertyChangeListener("date", e -> {
                LocalDate bd = layNgay(dchBatDau);
                LocalDate kt = layNgay(dchKetThuc);

                // Ngày kết thúc chỉ chọn được khi đã có ngày bắt đầu
                dchKetThuc.setEnabled(bd != null);
                if (bd == null) {
                    dchKetThuc.setDate(null);
                } else {
                    dchKetThuc.setMinSelectableDate(Date.valueOf(bd));
                    if (kt != null && kt.isBefore(bd)) {
                        dchKetThuc.setDate(null);
                    }
                }
                capNhatTrangThai();
            });
        }

        // Trạng thái hiển thị do hệ thống tự tính theo ngày (trừ khi tạm ngưng)
        private void capNhatTrangThai() {
            LocalDate bd = layNgay(dchBatDau);
            LocalDate kt = layNgay(dchKetThuc);

            String trangThai;
            if (chkTamNgung.isSelected()) {
                trangThai = TT_TAM;
            } else if (bd == null || kt == null || kt.isBefore(bd)) {
                trangThai = TT_CHUA_XAC_DINH;
            } else {
                trangThai = tinhTrangThai(bd, kt);
            }
            lblTrangThai.setText(trangThai);
            lblTrangThai.setForeground(mauTrangThai(trangThai));
        }

        // ---------- Giao diện ----------
        private void dungGiaoDien() {
            JLabel lblTieuDe = new JLabel(themMoi ? "Thêm khuyến mãi mới" : "Thông tin khuyến mãi");
            lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTieuDe.setForeground(NAVY);
            lblTieuDe.setOpaque(true);
            lblTieuDe.setBackground(new Color(236, 245, 255));
            lblTieuDe.setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton btnLuu = nutChinh(themMoi ? "Lưu" : "Lưu thay đổi");
            JButton btnXoa = nutThuong("Xóa");
            JButton btnDong = nutThuong("Đóng");
            btnXoa.setForeground(new Color(200, 50, 50));
            btnXoa.setVisible(!themMoi);

            btnLuu.addActionListener(e -> luu());
            btnXoa.addActionListener(e -> xoa());
            btnDong.addActionListener(e -> dispose());

            JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            pnlNut.setBackground(Color.WHITE);
            pnlNut.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
            pnlNut.add(btnLuu);
            pnlNut.add(btnXoa);
            pnlNut.add(btnDong);

            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            add(lblTieuDe, BorderLayout.NORTH);
            add(taoForm(), BorderLayout.CENTER);
            add(pnlNut, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(btnLuu);
        }

        private JPanel taoForm() {
            JPanel pnlTile = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            pnlTile.setOpaque(false);
            pnlTile.add(txtTile);
            pnlTile.add(nhan("%  (0 < tỉ lệ ≤ 100)"));

            JPanel pnlNgay = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            pnlNgay.setOpaque(false);
            pnlNgay.add(dchBatDau);
            pnlNgay.add(nhan("đến"));
            pnlNgay.add(dchKetThuc);

            JPanel pnlTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
            pnlTrangThai.setOpaque(false);
            pnlTrangThai.add(lblTrangThai);
            pnlTrangThai.add(chkTamNgung);

            JScrollPane scrDieuKien = new JScrollPane(txaDieuKien);
            scrDieuKien.setPreferredSize(new Dimension(310, 90));

            String[] nhanTruong = { "Mã khuyến mãi:", "Tên khuyến mãi:", "Tỉ lệ giảm:", "Thời gian áp dụng:",
                    "Điều kiện áp dụng:", "Trạng thái:" };
            Component[] oTruong = { txtMa, txtTen, pnlTile, pnlNgay, scrDieuKien, pnlTrangThai };

            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setBackground(Color.WHITE);
            pnlForm.setBorder(new EmptyBorder(16, 20, 8, 20));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(6, 0, 6, 12);
            for (int i = 0; i < nhanTruong.length; i++) {
                c.gridy = i;
                c.gridx = 0;
                c.anchor = (i == 4) ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
                pnlForm.add(nhan(nhanTruong[i]), c);
                c.gridx = 1;
                c.anchor = GridBagConstraints.WEST;
                pnlForm.add(oTruong[i], c);
            }
            return pnlForm;
        }

        // Hành động
        private void luu() {
            KhuyenMai ban = docVaKiemTra();
            if (ban != null && luuVaoCSDL(ban, themMoi)) {
                dispose();
                lamMoiDuLieu();
            }
        }

        private void xoa() {
            int ans = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa khuyến mãi " + km.maKM + "?\n",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (ans == JOptionPane.YES_OPTION && xoaKhoiCSDL(km.maKM)) {
                dispose();
                lamMoiDuLieu();
            }
        }

        private KhuyenMai docVaKiemTra() {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String tileStr = txtTile.getText().trim().replace(',', '.');

            // Mã và tên
            if (ma.isEmpty())
                return loi("Vui lòng nhập mã khuyến mãi.", null);
            if (ma.length() > 20 || !ma.matches("[A-Za-z0-9_-]+"))
                return loi("Mã khuyến mãi tối đa 20 ký tự, chỉ gồm chữ không dấu, số, '-' hoặc '_'.", null);
            if (themMoi && timTheoMa(ma) != null)
                return loi("Mã khuyến mãi \"" + ma + "\" đã tồn tại.", null);
            if (ten.isEmpty())
                return loi("Vui lòng nhập tên khuyến mãi.", txtTen);
            if (ten.length() > 100)
                return loi("Tên khuyến mãi tối đa 100 ký tự.", null);

            // Tỉ lệ giảm
            BigDecimal tile;
            try {
                tile = new BigDecimal(tileStr);
            } catch (NumberFormatException ex) {
                return loi("Tỉ lệ giảm phải là số (ví dụ 10 hoặc 7.5).", txtTile);
            }
            if (tile.signum() <= 0 || tile.compareTo(new BigDecimal("100")) > 0)
                return loi("Tỉ lệ giảm phải lớn hơn 0 và không vượt quá 100%.", txtTile);
            if (tile.stripTrailingZeros().scale() > 2)
                return loi("Tỉ lệ giảm chỉ được tối đa 2 chữ số thập phân.", txtTile);

            // Ngày áp dụng
            LocalDate bd = layNgay(dchBatDau);
            LocalDate kt = layNgay(dchKetThuc);
            if (bd == null || kt == null)
                return loi("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.", null);
            if (kt.isBefore(bd))
                return loi("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.", null);

            String trangThai = chkTamNgung.isSelected() ? TT_TAM : tinhTrangThai(bd, kt);
            return new KhuyenMai(ma, ten, tile.setScale(2, RoundingMode.HALF_UP), bd, kt,
                    txaDieuKien.getText().trim(), trangThai);
        }

        // Báo lỗi, đưa con trỏ về ô sai (nếu có) và trả về null
        private KhuyenMai loi(String message, Component focus) {
            canhBao(this, message);
            if (focus != null) {
                focus.requestFocus();
            }
            return null;
        }
    }

    // 7. TIỆN ÍCH
    // Ngày / số
    private JDateChooser oChonNgay(int width) {
        JDateChooser dch = new JDateChooser();
        dch.setDateFormatString("dd/MM/yyyy");
        dch.setFont(F_TEXT);
        kichThuoc(dch, width, 30);
        return dch;
    }

    private LocalDate layNgay(JDateChooser dch) {
        java.util.Date date = dch.getDate();
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void ganNgay(JDateChooser dch, LocalDate date) {
        dch.setDate(date == null ? null
                : java.util.Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private BigDecimal docSo(JTextField txt) {
        String s = txt.getText().trim();
        return s.isEmpty() ? null : new BigDecimal(s.replace(',', '.'));
    }

    // Thành phần giao diện
    private JLabel nhan(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(F_TEXT);
        lbl.setForeground(NAVY);
        return lbl;
    }

    private JTextField oNhap(int width) {
        JTextField txt = new JTextField();
        txt.setFont(F_TEXT);
        kichThuoc(txt, width, 30);
        txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(2, 6, 2, 6)));
        return txt;
    }

    private void kichThuoc(JComponent comp, int w, int h) {
        Dimension d = new Dimension(w, h);
        comp.setPreferredSize(d);
        comp.setMinimumSize(d);
    }

    private JButton nutChinh(String text) {
        return taoNut(text, F_BOLD, Color.WHITE, BLUE_DAM, new Color(20, 100, 200));
    }

    private JButton nutThuong(String text) {
        return taoNut(text, F_TEXT, NAVY, Color.WHITE, BORDER);
    }

    private JButton taoNut(String text, Font font, Color chu, Color nen, Color vien) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setForeground(chu);
        btn.setBackground(nen);

        // Buộc nút hiển thị màu nền đã thiết lập
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);

        btn.setBorder(BorderFactory.createCompoundBorder(new LineBorder(vien, 1), new EmptyBorder(7, 15, 7, 15)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Thông báo
    private void canhBao(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void thongBao(String message, int loai) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", loai);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BaseFrame frame = new BaseFrame();
            frame.setContent(new FrmKhuyenMai());
            frame.setVisible(true);
        });
    }
}
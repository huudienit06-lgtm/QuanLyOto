package gui;

import connectDB.ConnectDB;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class FrmCongNo extends JFrame {
    // Màu sắc
    private static final Color BLUE = new Color(25, 100, 235);
    private static final Color BLUE_DARK = new Color(18, 72, 180);
    private static final Color PALE_BLUE = new Color(235, 244, 255);
    private static final Color BORDER = new Color(220, 232, 248);
    private static final Color TEXT = new Color(35, 67, 125);
    private static final Color MUTED = new Color(100, 125, 165);
    private static final Color BG = new Color(244, 248, 253);
    private static final Color ZEBRA = new Color(248, 251, 255);
    private static final Color RED = new Color(205, 35, 45);
    private static final Color GREEN = new Color(25, 130, 80);
    private static final Color ORANGE = new Color(225, 125, 15);
    // Định dạng
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Giá trị lọc / trạng thái
    private static final String TAT_CA = "Tất cả";
    private static final String CON_NO = "Còn nợ";
    private static final String DA_TT = "Đã thanh toán";
    // Câu lệnh SQL
    private static final String SQL_CONG_NO = "SELECT kh.maKH, kh.hoTen, kh.soDT, " +
            "ISNULL(cn.tongNo, 0) AS tongNo, ISNULL(cn.soHDNo, 0) AS soHDNo, " +
            "cn.ngayCapNhat AS ngayCapNhat " +
            "FROM KhachHang kh " +
            "OUTER APPLY (SELECT " +
            " SUM(CASE WHEN hd.soTien-ISNULL(tt.daTra,0)>0 " +
            " THEN hd.soTien-ISNULL(tt.daTra,0) ELSE 0 END) AS tongNo, " +
            " SUM(CASE WHEN hd.maHoaDon IS NOT NULL AND hd.soTien-ISNULL(tt.daTra,0)>0 " +
            " THEN 1 ELSE 0 END) AS soHDNo, MAX(hd.ngayLap) AS ngayCapNhat " +
            " FROM PhieuSuaChua p JOIN HoaDon hd ON hd.maPhieuSuaChua=p.maPhieuSuaChua " +
            " OUTER APPLY (SELECT SUM(g.soTienThanhToan) AS daTra FROM GiaoDichThanhToan g " +
            " WHERE g.maHoaDon=hd.maHoaDon " +
            " AND (g.trangThaiGD IS NULL OR g.trangThaiGD=N'Thành công')) tt " +
            " WHERE p.maKH=kh.maKH) cn " +
            "WHERE ISNULL(cn.tongNo, 0) > 0 " +
            "AND (? = '' OR kh.maKH LIKE ? OR kh.hoTen LIKE ? OR kh.soDT LIKE ? " +
            " OR EXISTS (SELECT 1 FROM PhieuSuaChua p2 JOIN HoaDon h2 ON h2.maPhieuSuaChua=p2.maPhieuSuaChua " +
            " WHERE p2.maKH=kh.maKH AND h2.maHoaDon LIKE ?)) " +
            "AND (? = N'Tất cả' OR (? = N'Còn nợ' AND ISNULL(cn.tongNo,0) > 0) " +
            " OR (? = N'Đã thanh toán' AND ISNULL(cn.tongNo,0) <= 0)) " +
            "AND (? = N'Tất cả' OR EXISTS (SELECT 1 FROM PhieuSuaChua p3 " +
            " JOIN HoaDon h3 ON h3.maPhieuSuaChua=p3.maPhieuSuaChua WHERE p3.maKH=kh.maKH " +
            " AND ((? = N'Hôm nay' AND CAST(h3.ngayLap AS DATE)=CAST(GETDATE() AS DATE)) " +
            " OR (? = N'7 ngày gần nhất' AND h3.ngayLap >= DATEADD(DAY,-7,GETDATE())) " +
            " OR (? = N'30 ngày gần nhất' AND h3.ngayLap >= DATEADD(DAY,-30,GETDATE()))))) " +
            "ORDER BY cn.ngayCapNhat DESC, kh.maKH";
    // Thông tin một khách hàng
    private static final String SQL_KHACH_HANG = "SELECT maKH, hoTen, soDT, diaChi FROM KhachHang WHERE maKH = ?";
    // Hóa đơn còn nợ của một khách hàng
    private static final String SQL_HOA_DON_NO = "SELECT hd.maHoaDon, hd.ngayLap, hd.soTien, ISNULL(tt.daTra,0) AS daTra, "
            + "hd.soTien-ISNULL(tt.daTra,0) AS conPhaiTra "
            + "FROM PhieuSuaChua psc JOIN HoaDon hd ON hd.maPhieuSuaChua=psc.maPhieuSuaChua "
            + "OUTER APPLY (SELECT SUM(g.soTienThanhToan) daTra FROM GiaoDichThanhToan g "
            + "WHERE g.maHoaDon=hd.maHoaDon AND (g.trangThaiGD IS NULL OR g.trangThaiGD=N'Thành công')) tt "
            + "WHERE psc.maKH=? AND hd.soTien-ISNULL(tt.daTra,0)>0 ORDER BY hd.ngayLap";

    // THÀNH PHẦN GIAO DIỆN
    // Khung tìm kiếm
    private final JTextField txtTimKiem = new JTextField();
    private final JComboBox<String> cmbNhomKH = new JComboBox<>(
            new String[] { TAT_CA, "Dưới 1 triệu", "1 - 5 triệu", "5 - 10 triệu", "Trên 10 triệu" });
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(
            new String[] { TAT_CA, CON_NO, DA_TT });
    private final JComboBox<String> cmbThoiGian = new JComboBox<>(
            new String[] { TAT_CA, "Hôm nay", "7 ngày gần nhất", "30 ngày gần nhất" });
    // Thống kê nhanh
    private final JLabel lblTongNo = new JLabel("0 đ");
    private final JLabel lblSoKHNo = new JLabel("0");
    private final JLabel lblSoHDNo = new JLabel("0");
    private final JLabel lblTongKH = new JLabel("0");
    // Danh sách công nợ
    private final DefaultTableModel modelCongNo = taoModel(
            "Mã KH", "Tên khách hàng", "Số điện thoại", "Tổng công nợ",
            "Số hóa đơn nợ", "Trạng thái", "Ngày cập nhật");
    private JTable tblCongNo;
    // Cờ chặn tải lại nhiều lần khi đặt lại các ô lọc
    private boolean dangDatLaiBoLoc = false;

    // KHỞI TẠO
    public FrmCongNo() {
        setTitle("QUẢN LÝ CÔNG NỢ");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1250, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(dungTieuDe(), BorderLayout.NORTH);
        add(dungKhungChinh(), BorderLayout.CENTER);
        ganSuKien();
        taiDuLieu();
    }

    // GIAO DIỆN CHÍNH
    private JLabel dungTieuDe() {
        JLabel lblTieuDe = new JLabel("QUẢN LÝ CÔNG NỢ", SwingConstants.CENTER);
        lblTieuDe.setForeground(Color.BLACK);
        lblTieuDe.setFont(font(Font.BOLD, 28));
        lblTieuDe.setBorder(new EmptyBorder(15, 0, 12, 0));
        return lblTieuDe;
    }

    private JPanel dungKhungChinh() {
        JPanel pnlChinh = new JPanel(new BorderLayout(0, 12));
        pnlChinh.setBackground(BG);
        pnlChinh.setBorder(new EmptyBorder(0, 14, 14, 14));
        pnlChinh.add(dungKhungTimKiem(), BorderLayout.NORTH);
        pnlChinh.add(dungKhungDanhSach(), BorderLayout.CENTER);
        return pnlChinh;
    }

    // Phần 1: tìm kiếm / lọc (gọn trên một hàng)
    private JPanel dungKhungTimKiem() {
        JPanel pnlTimKiem = new JPanel(new GridBagLayout());
        pnlTimKiem.setBackground(Color.WHITE);
        pnlTimKiem.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(10, 14, 12, 14)));

        txtTimKiem.setFont(font(Font.PLAIN, 12));
        txtTimKiem.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(6, 9, 6, 9)));
        txtTimKiem.setToolTipText("Tìm theo mã KH, tên KH, SĐT hoặc mã hóa đơn");

        JButton btnTimKiem = taoNutChinh("Tìm kiếm", BLUE);
        JButton btnLamMoi = taoNutPhu("Làm mới");
        btnTimKiem.setPreferredSize(new Dimension(112, 32));
        btnLamMoi.setPreferredSize(new Dimension(92, 32));
        btnTimKiem.addActionListener(e -> taiDuLieu());
        btnLamMoi.addActionListener(e -> lamMoiBoLoc());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnTimKiem);
        pnlNut.add(btnLamMoi);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.SOUTH;
        g.insets = new Insets(0, 0, 0, 10);

        g.gridx = 0;
        g.weightx = 1; // ô từ khóa giãn theo chiều ngang
        pnlTimKiem.add(taoOLoc("Từ khóa (mã KH, tên KH, SĐT, mã hóa đơn)", txtTimKiem, 260), g);

        g.weightx = 0;
        g.gridx = 1;
        pnlTimKiem.add(taoOLoc("Khoảng nợ", cmbNhomKH, 150), g);
        g.gridx = 2;
        pnlTimKiem.add(taoOLoc("Trạng thái", cmbTrangThai, 140), g);
        g.gridx = 3;
        pnlTimKiem.add(taoOLoc("Thời gian phát sinh", cmbThoiGian, 170), g);

        g.gridx = 4;
        g.insets = new Insets(0, 0, 0, 0);
        g.fill = GridBagConstraints.NONE;
        pnlTimKiem.add(pnlNut, g);
        return pnlTimKiem;
    }

    // Một ô lọc: nhãn nhỏ phía trên + control phía dưới
    private JPanel taoOLoc(String nhan, JComponent control, int rong) {
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(font(Font.PLAIN, 10));
        lblNhan.setForeground(TEXT);

        control.setFont(font(Font.PLAIN, 11));
        if (control instanceof JComboBox) {
            control.setBackground(Color.WHITE);
        }
        control.setPreferredSize(new Dimension(10, 32));

        JPanel pnlO = new JPanel(new BorderLayout(0, 3));
        pnlO.setOpaque(false);
        pnlO.add(lblNhan, BorderLayout.NORTH);
        pnlO.add(control, BorderLayout.CENTER);
        pnlO.setPreferredSize(new Dimension(rong, 51));
        return pnlO;
    }

    // Phần 2: danh sách công nợ
    private JPanel dungKhungDanhSach() {
        JPanel pnlDanhSach = taoPnlThe();
        pnlDanhSach.setLayout(new BorderLayout(0, 10));

        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(taoTieuMuc("Danh sách công nợ"), BorderLayout.WEST);
        pnlTieuDe.add(dungThongKeNhanh(), BorderLayout.EAST);
        pnlDanhSach.add(pnlTieuDe, BorderLayout.NORTH);

        tblCongNo = taoBang(modelCongNo, 38, 12);
        tblCongNo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        datDoRongCot(tblCongNo, 70, 190, 110, 130, 90, 100, 110);

        DefaultTableCellRenderer renderGiua = taoRenderGiua();
        for (int cot : new int[] { 0, 2, 4, 6 }) {
            tblCongNo.getColumnModel().getColumn(cot).setCellRenderer(renderGiua);
        }
        tblCongNo.getColumnModel().getColumn(3).setCellRenderer(new MoneyRenderer(true));
        tblCongNo.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        pnlDanhSach.add(taoScr(tblCongNo), BorderLayout.CENTER);
        return pnlDanhSach;
    }

    // Thống kê nhanh gọn trên một dòng, đặt bên phải tiêu đề danh sách
    private JPanel dungThongKeNhanh() {
        JPanel pnlThongKe = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlThongKe.setOpaque(false);
        pnlThongKe.add(taoMucThongKe("Tổng công nợ:", lblTongNo, RED));
        pnlThongKe.add(taoVachNgan());
        pnlThongKe.add(taoMucThongKe("Khách còn nợ:", lblSoKHNo, ORANGE));
        pnlThongKe.add(taoVachNgan());
        pnlThongKe.add(taoMucThongKe("Hóa đơn nợ:", lblSoHDNo, BLUE_DARK));
        pnlThongKe.add(taoVachNgan());
        pnlThongKe.add(taoMucThongKe("Tổng khách hàng:", lblTongKH, TEXT));
        return pnlThongKe;
    }

    private JPanel taoMucThongKe(String nhan, JLabel lblGiaTri, Color mau) {
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setForeground(MUTED);
        lblNhan.setFont(font(Font.PLAIN, 12));

        lblGiaTri.setForeground(mau);
        lblGiaTri.setFont(font(Font.BOLD, 14));

        JPanel pnlMuc = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlMuc.setOpaque(false);
        pnlMuc.add(lblNhan);
        pnlMuc.add(lblGiaTri);
        return pnlMuc;
    }

    private Component taoVachNgan() {
        JSeparator sprVach = new JSeparator(SwingConstants.VERTICAL);
        sprVach.setPreferredSize(new Dimension(1, 18));
        sprVach.setForeground(BORDER);
        return sprVach;
    }

    // SỰ KIỆN
    private void ganSuKien() {
        // Nhấn vào dòng bất kỳ -> mở chi tiết
        tblCongNo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int dong = tblCongNo.rowAtPoint(e.getPoint());
                if (dong >= 0) {
                    tblCongNo.setRowSelectionInterval(dong, dong);
                    moChiTiet(dong);
                }
            }
        });

        // Enter trên dòng đang chọn -> mở chi tiết
        tblCongNo.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "moChiTiet");
        tblCongNo.getActionMap().put("moChiTiet", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dong = tblCongNo.getSelectedRow();
                if (dong >= 0) {
                    moChiTiet(dong);
                }
            }
        });

        // Enter trong ô từ khóa hoặc đổi ô lọc -> tải lại danh sách
        txtTimKiem.addActionListener(e -> taiDuLieu());
        JComboBox<?>[] dsOLoc = { cmbNhomKH, cmbTrangThai, cmbThoiGian };
        for (JComboBox<?> cmbLoc : dsOLoc) {
            cmbLoc.addActionListener(e -> {
                if (!dangDatLaiBoLoc) {
                    taiDuLieu();
                }
            });
        }
    }

    private void lamMoiBoLoc() {
        dangDatLaiBoLoc = true;
        txtTimKiem.setText("");
        cmbNhomKH.setSelectedIndex(0);
        cmbTrangThai.setSelectedIndex(0);
        cmbThoiGian.setSelectedIndex(0);
        dangDatLaiBoLoc = false;
        taiDuLieu();
    }

    private void moChiTiet(int dongHienThi) {
        int dong = tblCongNo.convertRowIndexToModel(dongHienThi);
        String maKH = String.valueOf(modelCongNo.getValueAt(dong, 0));
        new DlgChiTietCongNo(maKH).setVisible(true);
    }

    // DỮ LIỆU DANH SÁCH CÔNG NỢ
    private Connection layKetNoi() throws SQLException {
        ConnectDB.getInstance().connect();
        return ConnectDB.getConnection();
    }

    private void taiDuLieu() {
        modelCongNo.setRowCount(0);

        String tuKhoa = txtTimKiem.getText().trim();
        String like = "%" + tuKhoa + "%";
        String nhomNo = String.valueOf(cmbNhomKH.getSelectedItem());
        String trangThai = String.valueOf(cmbTrangThai.getSelectedItem());
        String thoiGian = String.valueOf(cmbThoiGian.getSelectedItem());

        BigDecimal tongNo = BigDecimal.ZERO;
        int soKHNo = 0;
        int soHDNo = 0;

        try (Connection con = layKetNoi(); PreparedStatement ps = con.prepareStatement(SQL_CONG_NO)) {
            int i = 1;
            ps.setString(i++, tuKhoa);
            for (int k = 0; k < 4; k++) {
                ps.setString(i++, like);
            }
            for (int k = 0; k < 3; k++) {
                ps.setString(i++, trangThai);
            }
            for (int k = 0; k < 4; k++) {
                ps.setString(i++, thoiGian);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal no = nz(rs.getBigDecimal("tongNo"));
                    if (!thuocKhoangNo(no, nhomNo)) {
                        continue;
                    }
                    int soHD = rs.getInt("soHDNo");
                    modelCongNo.addRow(new Object[] {
                            rs.getString("maKH"), rs.getString("hoTen"), rs.getString("soDT"),
                            no, soHD, no.signum() > 0 ? CON_NO : DA_TT,
                            formatDate(rs.getTimestamp("ngayCapNhat"))
                    });
                    if (no.signum() > 0) {
                        tongNo = tongNo.add(no);
                        soKHNo++;
                        soHDNo += soHD;
                    }
                }
            }
        } catch (SQLException ex) {
            baoLoiCSDL(this, "Không tải được danh sách công nợ", ex);
        }

        lblTongNo.setText(formatMoney(tongNo) + " đ");
        lblSoKHNo.setText(String.valueOf(soKHNo));
        lblSoHDNo.setText(String.valueOf(soHDNo));
        lblTongKH.setText(String.valueOf(modelCongNo.getRowCount()));
        tblCongNo.clearSelection();
    }

    // Lọc theo khoảng nợ (chỉ áp dụng cho khách còn nợ): <1tr, 1-5tr, 5-10tr,
    // >=10tr
    private boolean thuocKhoangNo(BigDecimal no, String nhomNo) {
        if (TAT_CA.equals(nhomNo)) {
            return true;
        }
        if (no.signum() <= 0) {
            return false;
        }
        BigDecimal mot = new BigDecimal("1000000");
        BigDecimal nam = mot.multiply(BigDecimal.valueOf(5));
        BigDecimal muoi = mot.multiply(BigDecimal.TEN);
        switch (nhomNo) {
            case "Dưới 1 triệu":
                return no.compareTo(mot) < 0;
            case "1 - 5 triệu":
                return no.compareTo(mot) >= 0 && no.compareTo(nam) < 0;
            case "5 - 10 triệu":
                return no.compareTo(nam) >= 0 && no.compareTo(muoi) < 0;
            case "Trên 10 triệu":
                return no.compareTo(muoi) >= 0;
            default:
                return true;
        }
    }

    // KHUNG CHI TIẾT CÔNG NỢ
    private class DlgChiTietCongNo extends JDialog {
        private final String maKH;
        // Thông tin khách hàng
        private final JLabel lblMaKH = taoLblGiaTri();
        private final JLabel lblTenKH = taoLblGiaTri();
        private final JLabel lblSDT = taoLblGiaTri();
        private final JLabel lblDiaChi = taoLblGiaTri();
        // Danh sách hóa đơn còn nợ
        private final JLabel lblTieuDeHD = taoTieuMuc("▤  Danh sách hóa đơn còn nợ");
        private final JLabel lblTongNoChiTiet = new JLabel("0 đ");
        private final JLabel lblDaChon = new JLabel("0 đ");
        private final JCheckBox chkTatCa = new JCheckBox("Chọn tất cả hóa đơn");
        private final DefaultTableModel modelHoaDon = taoModel(
                "Mã hóa đơn", "Ngày lập", "Tổng tiền", "Đã thanh toán", "Còn phải trả");
        private JTable tblHoaDon;
        private JButton btnThanhToan;
        // Chặn vòng lặp giữa ô tích "Chọn tất cả" và bảng
        private boolean dangDongBo = false;

        DlgChiTietCongNo(String maKH) {
            super(FrmCongNo.this, "Chi tiết công nợ", true);
            this.maKH = maKH;

            setLayout(new BorderLayout());
            getContentPane().setBackground(BG);
            add(dungTieuDeChiTiet(), BorderLayout.NORTH);
            add(dungNoiDung(), BorderLayout.CENTER);
            add(dungNut(), BorderLayout.SOUTH);

            setMinimumSize(new Dimension(720, 500));
            setSize(800, 580);
            setLocationRelativeTo(FrmCongNo.this);

            napThongTinKH();
            napHoaDonNo();
        }

        // Dựng giao diện
        private JLabel dungTieuDeChiTiet() {
            JLabel lblTieuDe = new JLabel("Chi tiết công nợ khách hàng");
            lblTieuDe.setFont(font(Font.BOLD, 16));
            lblTieuDe.setForeground(BLUE_DARK);
            lblTieuDe.setOpaque(true);
            lblTieuDe.setBackground(new Color(236, 245, 255));
            lblTieuDe.setBorder(new EmptyBorder(12, 12, 12, 12));
            return lblTieuDe;
        }

        private JPanel dungNoiDung() {
            JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 10));
            pnlNoiDung.setOpaque(false);
            pnlNoiDung.setBorder(new EmptyBorder(12, 14, 10, 14));
            pnlNoiDung.add(dungThongTinKH(), BorderLayout.NORTH);
            pnlNoiDung.add(dungDanhSachHoaDon(), BorderLayout.CENTER);
            return pnlNoiDung;
        }

        // Thông tin khách hàng: lưới 2 x 2 (Mã | Tên, SĐT | Địa chỉ)
        private JPanel dungThongTinKH() {
            JPanel pnlThongTin = taoPnlThe();
            pnlThongTin.setLayout(new BorderLayout(0, 8));
            pnlThongTin.add(taoTieuMuc("Thông tin khách hàng"), BorderLayout.NORTH);

            String[] dsNhan = { "Mã khách hàng", "Tên khách hàng", "Số điện thoại", "Địa chỉ" };
            JLabel[] dsGiaTri = { lblMaKH, lblTenKH, lblSDT, lblDiaChi };

            JPanel pnlLuoi = new JPanel(new GridBagLayout());
            pnlLuoi.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(4, 0, 4, 10);
            for (int i = 0; i < dsNhan.length; i++) {
                JLabel lblNhan = new JLabel(dsNhan[i]);
                lblNhan.setForeground(MUTED);
                lblNhan.setFont(font(Font.PLAIN, 12));
                lblNhan.setPreferredSize(new Dimension(105, 20));

                int cot = (i % 2) * 2;
                c.gridy = i / 2;
                c.gridx = cot;
                c.weightx = 0;
                c.fill = GridBagConstraints.NONE;
                pnlLuoi.add(lblNhan, c);

                c.gridx = cot + 1;
                c.weightx = 0.5;
                c.fill = GridBagConstraints.HORIZONTAL;
                pnlLuoi.add(dsGiaTri[i], c);
            }
            pnlThongTin.add(pnlLuoi, BorderLayout.CENTER);
            return pnlThongTin;
        }

        private JPanel dungDanhSachHoaDon() {
            JPanel pnlHoaDon = taoPnlThe();
            pnlHoaDon.setLayout(new BorderLayout(0, 8));
            pnlHoaDon.add(lblTieuDeHD, BorderLayout.NORTH);

            tblHoaDon = taoBang(modelHoaDon, 34, 12);
            tblHoaDon.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            datDoRongCot(tblHoaDon, 120, 100, 120, 120, 120);

            DefaultTableCellRenderer renderGiua = taoRenderGiua();
            tblHoaDon.getColumnModel().getColumn(0).setCellRenderer(renderGiua);
            tblHoaDon.getColumnModel().getColumn(1).setCellRenderer(renderGiua);
            tblHoaDon.getColumnModel().getColumn(2).setCellRenderer(new MoneyRenderer(false));
            tblHoaDon.getColumnModel().getColumn(3).setCellRenderer(new MoneyRenderer(false));
            tblHoaDon.getColumnModel().getColumn(4).setCellRenderer(new MoneyRenderer(true));

            // Đổi lựa chọn -> cập nhật số tiền đã chọn
            tblHoaDon.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    capNhatLuaChon();
                }
            });
            // Bấm đúp một hóa đơn -> thanh toán
            tblHoaDon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && tblHoaDon.rowAtPoint(e.getPoint()) >= 0) {
                        thanhToan();
                    }
                }
            });

            JScrollPane scrHoaDon = taoScr(tblHoaDon);
            scrHoaDon.setPreferredSize(new Dimension(500, 230));
            pnlHoaDon.add(scrHoaDon, BorderLayout.CENTER);
            pnlHoaDon.add(dungChanHoaDon(), BorderLayout.SOUTH);
            return pnlHoaDon;
        }

        // Dưới bảng: ô tích "Chọn tất cả" (trái) | đã chọn + tổng công nợ (phải)
        private JPanel dungChanHoaDon() {
            chkTatCa.setFont(font(Font.BOLD, 12));
            chkTatCa.setForeground(TEXT);
            chkTatCa.setOpaque(false);
            chkTatCa.setFocusPainted(false);
            chkTatCa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chkTatCa.addActionListener(e -> {
                if (dangDongBo) {
                    return;
                }
                if (chkTatCa.isSelected()) {
                    tblHoaDon.selectAll();
                } else {
                    tblHoaDon.clearSelection();
                }
            });

            lblDaChon.setForeground(BLUE_DARK);
            lblDaChon.setFont(font(Font.BOLD, 13));
            lblTongNoChiTiet.setForeground(RED);
            lblTongNoChiTiet.setFont(font(Font.BOLD, 15));

            JPanel pnlTong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnlTong.setOpaque(false);
            pnlTong.add(taoLblPhu("Đã chọn:"));
            pnlTong.add(lblDaChon);
            pnlTong.add(Box.createHorizontalStrut(10));
            pnlTong.add(taoLblPhu("Tổng công nợ:"));
            pnlTong.add(lblTongNoChiTiet);

            JPanel pnlChan = new JPanel(new BorderLayout());
            pnlChan.setOpaque(false);
            pnlChan.setBorder(new EmptyBorder(2, 0, 0, 0));
            pnlChan.add(chkTatCa, BorderLayout.WEST);
            pnlChan.add(pnlTong, BorderLayout.EAST);
            return pnlChan;
        }

        private JPanel dungNut() {
            btnThanhToan = taoNutChinh("Thanh toán", BLUE);
            JButton btnDong = taoNutPhu("Đóng");
            btnThanhToan.setPreferredSize(new Dimension(130, 34));
            btnDong.setPreferredSize(new Dimension(90, 34));
            btnThanhToan.addActionListener(e -> thanhToan());
            btnDong.addActionListener(e -> dispose());

            JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            pnlNut.setBackground(Color.WHITE);
            pnlNut.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
            pnlNut.add(btnThanhToan);
            pnlNut.add(btnDong);
            return pnlNut;
        }

        // Nạp dữ liệu
        private void napThongTinKH() {
            try (Connection con = layKetNoi(); PreparedStatement ps = con.prepareStatement(SQL_KHACH_HANG)) {
                ps.setString(1, maKH);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        lblMaKH.setText(safe(rs.getString("maKH")));
                        lblTenKH.setText(safe(rs.getString("hoTen")));
                        lblSDT.setText(safe(rs.getString("soDT")));
                        lblDiaChi.setText(safe(rs.getString("diaChi")));
                    }
                }
            } catch (SQLException ex) {
                baoLoiCSDL(this, "Không tải được thông tin khách hàng", ex);
            }
        }

        private void napHoaDonNo() {
            modelHoaDon.setRowCount(0);
            BigDecimal tongNo = BigDecimal.ZERO;

            try (Connection con = layKetNoi(); PreparedStatement ps = con.prepareStatement(SQL_HOA_DON_NO)) {
                ps.setString(1, maKH);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BigDecimal conNo = nz(rs.getBigDecimal("conPhaiTra"));
                        tongNo = tongNo.add(conNo);
                        modelHoaDon.addRow(new Object[] {
                                rs.getString("maHoaDon"), formatDate(rs.getTimestamp("ngayLap")),
                                nz(rs.getBigDecimal("soTien")), nz(rs.getBigDecimal("daTra")), conNo });
                    }
                }
            } catch (SQLException ex) {
                baoLoiCSDL(this, "Không tải được hóa đơn còn nợ", ex);
            }

            int soHD = modelHoaDon.getRowCount();
            lblTieuDeHD.setText("Danh sách hóa đơn còn nợ (" + soHD + ")");
            lblTongNoChiTiet.setText(formatMoney(tongNo) + " đ");
            lblTongNoChiTiet.setForeground(soHD > 0 ? RED : GREEN);
            chkTatCa.setEnabled(soHD > 0);
            btnThanhToan.setEnabled(soHD > 0);
            capNhatLuaChon();
        }

        // Cập nhật số tiền đã chọn và đồng bộ ô tích "Chọn tất cả" theo bảng
        private void capNhatLuaChon() {
            dangDongBo = true;
            int[] dsDong = tblHoaDon.getSelectedRows();
            BigDecimal tong = BigDecimal.ZERO;
            for (int dong : dsDong) {
                tong = tong.add((BigDecimal) modelHoaDon.getValueAt(tblHoaDon.convertRowIndexToModel(dong), 4));
            }
            lblDaChon.setText(formatMoney(tong) + " đ");
            chkTatCa.setSelected(modelHoaDon.getRowCount() > 0 && dsDong.length == modelHoaDon.getRowCount());
            dangDongBo = false;
        }

        // Thanh toán
        private void thanhToan() {
            int[] dsDong = tblHoaDon.getSelectedRows();
            if (dsDong.length == 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần thanh toán.");
                return;
            }
            // Chỉ chuyển sang FrmThanhToan khi chọn đúng 1 hóa đơn
            if (dsDong.length > 1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chỉ chọn một hóa đơn để thanh toán.");
                return;
            }

            int dong = tblHoaDon.convertRowIndexToModel(dsDong[0]);
            String maHD = String.valueOf(modelHoaDon.getValueAt(dong, 0));

            // Đóng khung chi tiết, mở màn hình thanh toán; đóng xong thì tải lại danh sách
            // công nợ
            dispose();
            FrmThanhToan winThanhToan = new FrmThanhToan(maHD);
            winThanhToan.setLocationRelativeTo(FrmCongNo.this);
            winThanhToan.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    taiDuLieu();
                }
            });
            winThanhToan.setVisible(true);
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

    // Bảng xen kẽ màu dòng, đã cấu hình sẵn kiểu dáng
    private static JTable taoBang(DefaultTableModel model, int caoDong, int coChu) {
        JTable tbl = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer render, int dong, int cot) {
                Component c = super.prepareRenderer(render, dong, cot);
                if (!isRowSelected(dong)) {
                    c.setBackground(dong % 2 == 0 ? Color.WHITE : ZEBRA);
                }
                return c;
            }
        };
        tbl.setRowHeight(caoDong);
        tbl.setFont(font(Font.PLAIN, coChu));
        tbl.setForeground(TEXT);
        tbl.setGridColor(new Color(232, 239, 249));
        tbl.setShowVerticalLines(false);
        tbl.setFillsViewportHeight(true);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setSelectionBackground(new Color(222, 237, 255));
        tbl.setSelectionForeground(BLUE_DARK);

        JTableHeader tbhBang = tbl.getTableHeader();
        tbhBang.setFont(font(Font.BOLD, 11));
        tbhBang.setForeground(TEXT);
        tbhBang.setBackground(new Color(237, 245, 255));
        tbhBang.setPreferredSize(new Dimension(0, 34));
        tbhBang.setReorderingAllowed(false);
        return tbl;
    }

    private static void datDoRongCot(JTable tbl, int... dsRong) {
        for (int i = 0; i < dsRong.length; i++) {
            tbl.getColumnModel().getColumn(i).setPreferredWidth(dsRong[i]);
        }
    }

    private static DefaultTableCellRenderer taoRenderGiua() {
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.CENTER);
        return render;
    }

    private static JScrollPane taoScr(JTable tbl) {
        JScrollPane scr = new JScrollPane(tbl);
        scr.setBorder(new LineBorder(BORDER));
        scr.getViewport().setBackground(Color.WHITE);
        return scr;
    }

    private static JPanel taoPnlThe() {
        JPanel pnl = new JPanel();
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(12, 14, 12, 14)));
        return pnl;
    }

    private static JLabel taoTieuMuc(String chu) {
        JLabel lbl = new JLabel(chu);
        lbl.setForeground(BLUE_DARK);
        lbl.setFont(font(Font.BOLD, 14));
        return lbl;
    }

    private static JLabel taoLblGiaTri() {
        JLabel lbl = new JLabel("-");
        lbl.setForeground(TEXT);
        lbl.setFont(font(Font.BOLD, 12));
        return lbl;
    }

    private static JLabel taoLblPhu(String chu) {
        JLabel lbl = new JLabel(chu);
        lbl.setForeground(MUTED);
        lbl.setFont(font(Font.PLAIN, 12));
        return lbl;
    }

    // Nút chính: nền màu, chữ trắng, đổi màu khi rê chuột / nhấn
    private static JButton taoNutChinh(String chu, Color mauNen) {
        JButton btn = new JButton(chu);
        btn.setFont(font(Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(mauNen);

        // Ép JButton hiển thị màu nền, tránh Look & Feel của Windows vẽ đè màu trắng
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 12, 9, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.getModel().addChangeListener(e -> {
            ButtonModel model = btn.getModel();
            if (model.isPressed()) {
                btn.setBackground(mauNen.darker());
            } else if (model.isRollover()) {
                btn.setBackground(mauNen.brighter());
            } else {
                btn.setBackground(mauNen);
            }
        });
        return btn;
    }

    // Nút phụ: nền trắng, viền xanh nhạt
    private static JButton taoNutPhu(String chu) {
        JButton btn = new JButton(chu);
        btn.setFont(font(Font.BOLD, 11));
        btn.setForeground(TEXT);
        btn.setBackground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.getModel().addChangeListener(e -> {
            ButtonModel model = btn.getModel();
            btn.setBackground(model.isPressed() || model.isRollover() ? PALE_BLUE : Color.WHITE);
        });
        return btn;
    }

    // TIỆN ÍCH DỮ LIỆU
    private static BigDecimal nz(BigDecimal so) {
        return so == null ? BigDecimal.ZERO : so;
    }

    private static String safe(String chu) {
        return chu == null || chu.trim().isEmpty() ? "-" : chu;
    }

    private static String formatMoney(BigDecimal so) {
        return MONEY.format(nz(so));
    }

    private static String formatDate(Timestamp thoiGian) {
        return thoiGian == null ? "-" : thoiGian.toLocalDateTime().format(DMY);
    }

    private void baoLoiCSDL(Component cha, String tieuDe, SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(cha,
                tieuDe + ".\nChi tiết: " + ex.getMessage()
                        + "\n\nHãy kiểm tra tên bảng/cột trong phần SQL của FrmCongNo.",
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    // RENDERER
    // Hiển thị tiền "1,234,000 đ"; canhBaoNo = true thì tô đỏ đậm khi số tiền > 0
    private static class MoneyRenderer extends DefaultTableCellRenderer {
        private final boolean canhBaoNo;

        MoneyRenderer(boolean canhBaoNo) {
            this.canhBaoNo = canhBaoNo;
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object giaTri,
                boolean dangChon, boolean coFocus, int dong, int cot) {
            super.getTableCellRendererComponent(tbl, giaTri, dangChon, coFocus, dong, cot);
            BigDecimal soTien = giaTri instanceof BigDecimal ? (BigDecimal) giaTri : null;
            setText(soTien == null ? "" : formatMoney(soTien) + " đ");
            setBorder(new EmptyBorder(0, 4, 0, 14));

            boolean doDam = canhBaoNo && soTien != null && soTien.signum() > 0;
            setFont(tbl.getFont().deriveFont(doDam ? Font.BOLD : Font.PLAIN));
            setForeground(dangChon ? tbl.getSelectionForeground() : (doDam ? RED : TEXT));
            return this;
        }
    }

    // Trạng thái: chữ đậm có màu, căn giữa
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object giaTri,
                boolean dangChon, boolean coFocus, int dong, int cot) {
            super.getTableCellRendererComponent(tbl, giaTri, dangChon, coFocus, dong, cot);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(tbl.getFont().deriveFont(Font.BOLD));
            setForeground(CON_NO.equals(giaTri) ? RED : GREEN);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new FrmCongNo().setVisible(true);
        });
    }
}
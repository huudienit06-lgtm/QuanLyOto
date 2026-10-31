package gui;

import dao.HoaDonDAO;
import dao.HoaDonDAO.Phieu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.table.DefaultTableModel;

public class FrmTimPhieuSuaChua extends JPanel {
    private static final Color MAU_CHU = new Color(40, 50, 65);
    private static final Color MAU_PHU = new Color(100, 112, 130);
    private static final Color MAU_NEN = new Color(244, 247, 251);
    private static final Color MAU_NUT = new Color(37, 99, 235);

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final Consumer<String> khiChonPhieu;
    private final JTextField txtTuKhoa = new JTextField();
    private final JButton btnTimKiem = new JButton("Tìm kiếm");
    private final JButton btnChonPhieu = new JButton("Chọn phiếu");
    private final JButton btnDong = new JButton("Đóng");
    private final DefaultTableModel modelPhieu = new DefaultTableModel(new String[] {
            "Mã phiếu", "Mã khách hàng", "Tên khách hàng", "Số điện thoại",
            "Mã xe", "Biển số xe", "Nhân viên", "Ngày bắt đầu" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblPhieu = new JTable(modelPhieu);

    public FrmTimPhieuSuaChua(Consumer<String> khiChonPhieu) {
        this.khiChonPhieu = khiChonPhieu;
        setLayout(new BorderLayout(10, 10));
        setBackground(MAU_NEN);
        setBorder(new EmptyBorder(14, 14, 12, 14));

        cauHinhBang();
        add(taoVungTimKiem(), BorderLayout.NORTH);
        add(new JScrollPane(tblPhieu), BorderLayout.CENTER);
        add(taoVungNut(), BorderLayout.SOUTH);
        caiDatSuKien();
        timKiemPhieu();
    }

    private JPanel taoVungTimKiem() {
        JLabel lblTieuDe = new JLabel("TÌM PHIẾU SỬA CHỮA", SwingConstants.LEFT);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTieuDe.setForeground(MAU_NUT);

        JLabel lblHuongDan = new JLabel("Nhập mã phiếu, mã khách hàng, mã xe, tên khách hàng, "
                + "số điện thoại hoặc biển số xe:");
        lblHuongDan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHuongDan.setForeground(MAU_PHU);

        kieuNut(btnTimKiem, true);
        txtTuKhoa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTuKhoa.setPreferredSize(new Dimension(0, 36));
        txtTuKhoa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 230)),
                BorderFactory.createEmptyBorder(4, 9, 4, 9)));

        JPanel pnlNhap = new JPanel(new BorderLayout(8, 0));
        pnlNhap.setOpaque(false);
        pnlNhap.add(txtTuKhoa, BorderLayout.CENTER);
        pnlNhap.add(btnTimKiem, BorderLayout.EAST);

        JPanel pnlTimKiem = new JPanel(new BorderLayout(8, 7));
        pnlTimKiem.setOpaque(false);
        pnlTimKiem.add(lblTieuDe, BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 6));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(lblHuongDan, BorderLayout.NORTH);
        pnlNoiDung.add(pnlNhap, BorderLayout.CENTER);
        pnlTimKiem.add(pnlNoiDung, BorderLayout.CENTER);
        return pnlTimKiem;
    }

    private JPanel taoVungNut() {
        kieuNut(btnChonPhieu, true);
        kieuNut(btnDong, false);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnDong);
        pnlNut.add(btnChonPhieu);
        return pnlNut;
    }

    private void cauHinhBang() {
        tblPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblPhieu.setRowHeight(28);
        tblPhieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhieu.setAutoCreateRowSorter(true);
        tblPhieu.getTableHeader().setReorderingAllowed(false);
        tblPhieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblPhieu.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }

    private void caiDatSuKien() {
        btnTimKiem.addActionListener(e -> timKiemPhieu());
        txtTuKhoa.addActionListener(e -> timKiemPhieu());
        btnChonPhieu.addActionListener(e -> chonPhieu());
        btnDong.addActionListener(e -> dongCuaSo());
        tblPhieu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    chonPhieu();
                }
            }
        });
    }

    private void timKiemPhieu() {
        String tuKhoa = txtTuKhoa.getText().trim();
        modelPhieu.setRowCount(0);
        try {
            List<Phieu> danhSachPhieu = hoaDonDAO.timPhieu(tuKhoa);
            for (Phieu phieu : danhSachPhieu) {
                modelPhieu.addRow(new Object[] {
                        phieu.ma(), phieu.maKH(), phieu.tenKH(), phieu.soDT(),
                        phieu.maXe(), phieu.bienSo(), phieu.maNV(), phieu.ngayBatDau()
                });
            }
            if (danhSachPhieu.isEmpty()) {
                JOptionPane.showMessageDialog(this, tuKhoa.isEmpty()
                        ? "Chưa có phiếu sửa chữa nào trong hệ thống."
                        : "Không tìm thấy phiếu sửa chữa phù hợp.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn phiếu sửa chữa:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chonPhieu() {
        int selectedRow = tblPhieu.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu sửa chữa trong bảng.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maPhieu = modelPhieu.getValueAt(tblPhieu.convertRowIndexToModel(selectedRow), 0).toString();
        khiChonPhieu.accept(maPhieu);
    }

    private void dongCuaSo() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    private void kieuNut(JButton btn, boolean nutChinh) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(nutChinh ? Color.WHITE : MAU_CHU);
        btn.setBackground(nutChinh ? MAU_NUT : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 36));
    }
}

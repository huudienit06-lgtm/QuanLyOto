package gui;

import dao.DanhMucXeDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Form bổ sung hãng xe và các dòng xe thuộc hãng. */
public class FrmHangXe extends JPanel {
    private final DanhMucXeDAO dao = new DanhMucXeDAO();
    private final JTextField txtHangMoi = new JTextField(20);
    private final JTextField txtDongMoi = new JTextField(20);
    private final JComboBox<String> cboHang = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(new String[] {"Hãng xe", "Dòng xe"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel lblTrangThai = new JLabel(" ");
    private final Runnable sauKhiThem;

    public FrmHangXe() {
        this(() -> {});
    }

    public FrmHangXe(Runnable sauKhiThem) {
        this.sauKhiThem = sauKhiThem;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(660, 430));
        JPanel fields = new JPanel(new GridLayout(3, 3, 10, 10));
        JButton btnHang = new JButton("Thêm hãng xe");
        JButton btnDong = new JButton("Thêm dòng xe");
        fields.add(new JLabel("Tên hãng xe mới *"));
        fields.add(txtHangMoi);
        fields.add(btnHang);
        fields.add(new JLabel("Hãng của dòng xe *"));
        fields.add(cboHang);
        JButton btnTaiLai = new JButton("Tải lại danh mục");
        fields.add(btnTaiLai);
        fields.add(new JLabel("Tên dòng xe mới *"));
        fields.add(txtDongMoi);
        fields.add(btnDong);
        add(fields, BorderLayout.NORTH);
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(lblTrangThai, BorderLayout.SOUTH);
        btnHang.addActionListener(event -> them(true));
        btnDong.addActionListener(event -> them(false));
        btnTaiLai.addActionListener(event -> napDanhMuc());
        napDanhMuc();
    }

    private void napDanhMuc() {
        try {
            Map<String, Set<String>> danhMuc = dao.findAll();
            Object selected = cboHang.getSelectedItem();
            cboHang.removeAllItems();
            model.setRowCount(0);
            danhMuc.forEach((hang, dongXe) -> {
                cboHang.addItem(hang);
                if (dongXe.isEmpty()) model.addRow(new Object[] {hang, ""});
                for (String dong : dongXe) model.addRow(new Object[] {hang, dong});
            });
            if (selected != null) cboHang.setSelectedItem(selected);
            lblTrangThai.setText("Chọn hãng xe trước khi thêm dòng xe thuộc hãng đó.");
        } catch (SQLException ex) {
            baoLoi(ex);
        }
    }

    private void them(boolean laHang) {
        String ten = (laHang ? txtHangMoi : txtDongMoi).getText().trim();
        if (ten.isEmpty() || ten.length() > 50) {
            JOptionPane.showMessageDialog(this, "Tên phải có từ 1 đến 50 ký tự.");
            return;
        }
        if (!laHang && cboHang.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Hãy thêm và chọn hãng xe trước.");
            return;
        }
        try {
            if (laHang) dao.themHangXe(ten);
            else dao.themDongXe((String) cboHang.getSelectedItem(), ten);
            (laHang ? txtHangMoi : txtDongMoi).setText("");
            napDanhMuc();
            if (laHang) cboHang.setSelectedItem(ten);
            sauKhiThem.run();
            lblTrangThai.setText("Đã thêm " + (laHang ? "hãng xe " : "dòng xe ") + ten);
        } catch (SQLException ex) {
            baoLoi(ex);
        }
    }

    private void baoLoi(SQLException ex) {
        String message = ex.getErrorCode() == 2601 || ex.getErrorCode() == 2627
                ? "Tên đã tồn tại trong danh mục của hãng xe này."
                : "Không thể truy cập danh mục. Hãy kiểm tra kết nối và chạy Migration_DanhMucXe.sql.\n" + ex.getMessage();
        lblTrangThai.setText("Không thể cập nhật danh mục.");
        JOptionPane.showMessageDialog(this, message, "Danh mục xe", JOptionPane.ERROR_MESSAGE);
    }
}

package controller;

import dao.TimKiemHoaDonDAO;
import entity.ChiTietHoaDon;
import entity.GiaoDichThanhToan;
import entity.HoaDon;
import gui.FrmThanhToan;
import gui.FrmTimKiemHoaDon;
import gui.FrmTimKiemHoaDon.JDateChooser;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Controller cho màn hình Tìm kiếm hóa đơn: gắn sự kiện cho FrmTimKiemHoaDon,
 * gọi TimKiemHoaDonDAO lấy dữ liệu rồi đưa lên giao diện.
 */
public class TimKiemHoaDonController {
    private static final DateTimeFormatter DF_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat NF_TIEN = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private final FrmTimKiemHoaDon frm;
    private final TimKiemHoaDonDAO dao = new TimKiemHoaDonDAO();

    // Tránh tìm kiếm lặp nhiều lần khi bấm "Làm mới"
    private boolean dangLamMoi = false;

    public TimKiemHoaDonController(FrmTimKiemHoaDon frm) {
        this.frm = frm;
        caiDatSuKien();
        timKiemHoaDon();
    }

    // 1. SỰ KIỆN
    private void caiDatSuKien() {
        frm.getBtnTimKiem().addActionListener(e -> timKiemHoaDon());
        frm.getBtnLamMoi().addActionListener(e -> lamMoi());
        frm.getBtnXemChiTiet().addActionListener(e -> xemChiTiet());

        // Nhấn Enter ở bất kỳ ô nhập nào cũng tìm kiếm
        for (JTextField txt : frm.getDanhSachOChu()) {
            txt.addActionListener(e -> timKiemHoaDon());
        }

        // Đổi trạng thái hoặc chọn ngày thì tự tìm lại
        frm.getCmbTrangThai().addActionListener(e -> timKiemKhiDoiDieuKien());
        frm.getDchNgayLap().addPropertyChangeListener("date", e -> timKiemKhiDoiDieuKien());

        // Nút "Xem chi tiết" chỉ sáng khi đã chọn một dòng
        frm.getTblHoaDon().getSelectionModel().addListSelectionListener(
                e -> frm.getBtnXemChiTiet().setEnabled(frm.getTblHoaDon().getSelectedRow() >= 0));

        // Nhấp đúp vào dòng để xem chi tiết
        frm.getTblHoaDon().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)
                        && frm.getTblHoaDon().getSelectedRow() >= 0) {
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
        frm.xoaDieuKien();
        dangLamMoi = false;

        timKiemHoaDon();
        frm.getTxtMaHoaDon().requestFocus();
    }

    // 2. TÌM KIẾM
    private void timKiemHoaDon() {
        LocalDate ngayChon = layNgay(frm.getDchNgayLap());

        // Kiểm tra bổ sung để tránh ngày tương lai (trường hợp gõ tay)
        if (ngayChon != null && ngayChon.isAfter(LocalDate.now(FrmTimKiemHoaDon.MUI_GIO))) {
            frm.hienThongBao("Ngày lập không được lớn hơn ngày hiện tại.",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String trangThai = (String) frm.getCmbTrangThai().getSelectedItem();
        if (FrmTimKiemHoaDon.TAT_CA.equals(trangThai)) {
            trangThai = null;
        }

        frm.xoaBang();

        if (!dao.kiemTraKetNoi()) {
            frm.hienKetQua("Chưa kết nối được cơ sở dữ liệu", FrmTimKiemHoaDon.MAU_DO);
            frm.hienThongBao("Không thể kết nối SQL Server.\nVui lòng kiểm tra cấu hình kết nối.",
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<Object[]> ds = dao.timKiem(
                    frm.getTxtMaHoaDon().getText().trim(),
                    frm.getTxtMaPhieuSuaChua().getText().trim(),
                    frm.getTxtMaNV().getText().trim(),
                    frm.getTxtMaKM().getText().trim(),
                    frm.getTxtKhachHang().getText().trim(),
                    frm.getTxtXe().getText().trim(),
                    ngayChon, trangThai);

            for (Object[] r : ds) {
                Timestamp ngayLap = (Timestamp) r[6];
                frm.themDongBang(new Object[] {
                        r[0], r[1], r[2], r[3], r[4], r[5],
                        ngayLap == null ? "" : ngayLap.toLocalDateTime().format(DF_NGAY),
                        dinhDangTien((BigDecimal) r[7]),
                        r[8]
                });
            }

            if (ds.isEmpty()) {
                frm.hienKetQua("Không tìm thấy hóa đơn phù hợp", FrmTimKiemHoaDon.MAU_CAM);
            } else {
                frm.hienKetQua("Tìm thấy " + ds.size() + " hóa đơn", FrmTimKiemHoaDon.MAU_XANH_LA);
            }
        } catch (SQLException ex) {
            frm.hienKetQua("Lỗi tìm kiếm", FrmTimKiemHoaDon.MAU_DO);
            frm.hienThongBao("Không thể tìm kiếm hóa đơn.\nChi tiết: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 3. XEM CHI TIẾT
    private void xemChiTiet() {
        int dongChon = frm.getTblHoaDon().getSelectedRow();
        if (dongChon < 0) {
            return;
        }
        int row = frm.getTblHoaDon().convertRowIndexToModel(dongChon);
        DefaultTableModel model = frm.getModelHoaDon();

        String maHD = giaTriHienThi(model.getValueAt(row, 0));
        Object rawPhieu = model.getValueAt(row, 1);
        String maPhieu = (rawPhieu == null || rawPhieu.toString().isBlank()) ? null : rawPhieu.toString();
        String trangThai = giaTriHienThi(model.getValueAt(row, 8));

        // Lấy dữ liệu từ DAO
        HoaDon hd;
        String[] khXe;
        List<ChiTietHoaDon> dsChiTiet;
        Map<String, String> tenHang;
        List<GiaoDichThanhToan> dsGiaoDich;
        try {
            hd = dao.layHoaDon(maHD);
            khXe = dao.layMaKhachHangVaXe(maHD);
            dsChiTiet = dao.layChiTiet(maHD);
            tenHang = dao.layTenHang(maHD);
            dsGiaoDich = dao.layGiaoDichThanhCong(maHD);
        } catch (SQLException ex) {
            frm.hienThongBao("Không thể tải chi tiết hóa đơn.\nChi tiết: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (hd == null) {
            frm.hienThongBao("Không tìm thấy hóa đơn " + maHD + ".",
                    "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Các dòng dịch vụ, phụ tùng + tiền dịch vụ, tiền phụ tùng
        DefaultTableModel modelDong = frm.taoModelDongHang();
        BigDecimal tienDichVu = BigDecimal.ZERO;
        BigDecimal tienPhuTung = BigDecimal.ZERO;
        int stt = 1;
        for (ChiTietHoaDon ct : dsChiTiet) {
            boolean laDichVu = ct.getDichVu() != null;

            String ma;
            if (laDichVu) {
                ma = ct.getDichVu().getMaDV();
            } else {
                ma = ct.getPhuTung() != null
                        ? ct.getPhuTung().getMaPhuTung()
                        : null;
            }

            BigDecimal donGia = nz(ct.getDonGia());
            int soLuong = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));

            if (laDichVu) {
                tienDichVu = tienDichVu.add(thanhTien);
            } else {
                tienPhuTung = tienPhuTung.add(thanhTien);
            }

            modelDong.addRow(new Object[] {
                    stt++,
                    ma,
                    tenHang.get(ma),
                    soLuong,
                    dinhDangTien(donGia),
                    dinhDangTien(thanhTien)
            });
        }

        // Tổng đã thanh toán thành công (cộng các giao dịch)
        BigDecimal tongGiaoDich = BigDecimal.ZERO;
        for (GiaoDichThanhToan gd : dsGiaoDich) {
            tongGiaoDich = tongGiaoDich.add(nz(gd.getSoTienThanhToan()));
        }

        // Tính tiền
        BigDecimal tienThue = nz(hd.getThueVAT());
        BigDecimal tienGiam = nz(hd.getTienGiamGia());
        BigDecimal tongThanhToan = nz(hd.getSoTien());
        BigDecimal tongHoaDon = tienDichVu.add(tienPhuTung); // chưa gồm VAT

        boolean daTT = FrmTimKiemHoaDon.DA_THANH_TOAN.equals(trangThai);
        boolean motPhan = FrmTimKiemHoaDon.THANH_TOAN_MOT_PHAN.equals(trangThai);
        boolean chuaTT = FrmTimKiemHoaDon.CHUA_THANH_TOAN.equals(trangThai);

        BigDecimal daTra = daTT ? tongThanhToan : (motPhan ? tongGiaoDich : BigDecimal.ZERO);
        BigDecimal conLai = tongThanhToan.subtract(daTra).max(BigDecimal.ZERO);

        // Thẻ thông tin chung
        String[][] thongTin = {
                { "Khách hàng", giaTriHienThi(model.getValueAt(row, 3)) },
                { "Mã khách hàng", giaTriHienThi(khXe[0]) },
                { "Ngày lập", giaTriHienThi(model.getValueAt(row, 6)) },
                { "Biển số xe", giaTriHienThi(model.getValueAt(row, 4)) },
                { "Mã xe", giaTriHienThi(khXe[1]) },
                { "Mã nhân viên", giaTriHienThi(model.getValueAt(row, 2)) },
                { "Phiếu sửa chữa", giaTriHienThi(maPhieu) },
                { "Mã khuyến mãi", giaTriHienThi(model.getValueAt(row, 5)) },
                { "Trạng thái", trangThai } };

        // Thẻ tổng kết: { nhãn, giá trị, màu, in đậm }
        List<Object[]> dongTien = new ArrayList<>();
        dongTien.add(new Object[] { "Tổng hóa đơn (chưa gồm VAT)", dinhDangTien(tongHoaDon) + " đ",
                FrmTimKiemHoaDon.MAU_CHU, true });
        dongTien.add(new Object[] { "Tiền thuế (VAT)", dinhDangTien(tienThue) + " đ",
                FrmTimKiemHoaDon.MAU_CHU, false });
        dongTien.add(new Object[] { "Tiền giảm", "- " + dinhDangTien(tienGiam) + " đ",
                FrmTimKiemHoaDon.MAU_DO, false });
        dongTien.add(new Object[] { "TỔNG THANH TOÁN", dinhDangTien(tongThanhToan) + " đ",
                FrmTimKiemHoaDon.MAU_NUT, true });
        if (daTT) {
            dongTien.add(dongTienMau("Đã thanh toán", daTra, FrmTimKiemHoaDon.MAU_XANH_LA));
        } else if (motPhan) {
            dongTien.add(dongTienMau("Đã thanh toán", daTra, FrmTimKiemHoaDon.MAU_XANH_LA));
            dongTien.add(dongTienMau("Còn lại", conLai, FrmTimKiemHoaDon.MAU_DO));
        } else if (chuaTT) {
            dongTien.add(dongTienMau("Còn lại", conLai, FrmTimKiemHoaDon.MAU_DO));
        }

        frm.hienThiChiTiet(maHD, thongTin, modelDong, dongTien, chuaTT || motPhan,
                () -> moFrmThanhToan(maHD));
    }

    private Object[] dongTienMau(String nhan, BigDecimal tien, Color mau) {
        return new Object[] { nhan, dinhDangTien(tien) + " đ", mau, true };
    }

    // Mở màn hình thanh toán cho hóa đơn; đóng xong thì tìm kiếm lại để cập nhật
    // trạng thái
    private void moFrmThanhToan(String maHD) {
        Object frmThanhToan = new FrmThanhToan(maHD);

        if (frmThanhToan instanceof JFrame) {
            JFrame f = (JFrame) frmThanhToan;
            f.setLocationRelativeTo(frm);
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    timKiemHoaDon();
                }
            });
            f.setVisible(true);
        } else if (frmThanhToan instanceof JPanel) {
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(frm), "Thanh toán hóa đơn " + maHD,
                    Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setContentPane((JPanel) frmThanhToan);
            dlg.pack();
            dlg.setLocationRelativeTo(frm);
            dlg.setVisible(true);
            timKiemHoaDon(); // dialog modal: chạy tiếp ở đây sau khi đóng
        }
    }

    // 4. TIỆN ÍCH
    private LocalDate layNgay(JDateChooser dch) {
        Date date = dch.getDate();
        return date == null ? null : date.toInstant().atZone(FrmTimKiemHoaDon.MUI_GIO).toLocalDate();
    }

    private String dinhDangTien(BigDecimal so) {
        return so == null ? "0" : NF_TIEN.format(so);
    }

    private BigDecimal nz(BigDecimal so) {
        return so == null ? BigDecimal.ZERO : so;
    }

    // Giá trị rỗng / null hiển thị là "---"
    private String giaTriHienThi(Object value) {
        return (value == null || value.toString().trim().isEmpty()) ? "---" : value.toString();
    }
}
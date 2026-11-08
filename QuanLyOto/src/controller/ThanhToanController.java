package controller;

import dao.ThanhToanDAO;
import entity.GiaoDichThanhToan;
import entity.HoaDon;
import gui.FrmThanhToan;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ThanhToanController {
    private final FrmThanhToan view;
    private final ThanhToanDAO dao = new ThanhToanDAO();

    private String maHoaDon;
    private String maNhanVien = "";
    private String maXe = "";
    private String maKhachHang = "", tenKhachHang = "", maPhieu = "", ngayLap = "";
    // HoaDon.soTien là tổng thanh toán sau VAT và sau giảm giá.
    private BigDecimal tongTien = BigDecimal.ZERO;
    private BigDecimal daThanhToan = BigDecimal.ZERO;
    private BigDecimal tienDV = BigDecimal.ZERO;
    private BigDecimal tienPT = BigDecimal.ZERO;
    private BigDecimal tienThue = BigDecimal.ZERO;
    private BigDecimal tienGiamGia = BigDecimal.ZERO;
    private final List<ThanhToanDAO.ChiTiet> chiTietHD = new ArrayList<>();

    public ThanhToanController(FrmThanhToan view, String maHoaDon) {
        this.view = view;
        this.maHoaDon = maHoaDon == null ? "" : maHoaDon.trim();
        ganSuKien();
        taiHoaDon(this.maHoaDon);
    }

    // ===================== Gắn sự kiện =====================
    private void ganSuKien() {
        view.getBtnDong().addActionListener(e -> view.dispose());
        view.getBtnXacNhan().addActionListener(e -> xacNhanThanhToan());

        view.getBtnThanhToanDu().addActionListener(e -> {
            BigDecimal conNo = tinhConNo();
            if (conNo.signum() > 0) {
                view.setSoTienThanhToan(conNo.stripTrailingZeros().toPlainString());
                view.focusSoTien();
            } else {
                view.setSoTienThanhToan("0");
                view.baoLoi("Hóa đơn đã được thanh toán đủ.");
            }
            capNhatTienThua();
        });

        view.getBtnMotNua().addActionListener(e -> {
            BigDecimal conNo = tinhConNo();
            if (conNo.signum() > 0) {
                view.setSoTienThanhToan(
                        conNo.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP).toPlainString());
                view.focusSoTien();
            }
        });

        view.getBtnVuaDu().addActionListener(e -> {
            BigDecimal soTien = docSoTien(view.getSoTienThanhToan());
            if (soTien == null || soTien.signum() <= 0)
                soTien = tinhConNo();
            view.setTienKhachDua(soTien.stripTrailingZeros().toPlainString());
        });

        for (JButton b : view.getBtnMenhGia())
            b.addActionListener(e -> view.setTienKhachDua(b.getActionCommand()));

        java.awt.event.ItemListener doiPhuongThuc = e -> {
            view.capNhatPhuongThuc();
            capNhatTienThua();
        };
        view.getBtnTienMat().addItemListener(doiPhuongThuc);
        view.getBtnChuyenKhoan().addItemListener(doiPhuongThuc);

        khiThayDoi(view.getTxtTienKhachDua(), this::capNhatTienThua);
        khiThayDoi(view.getTxtSoTienThanhToan(), this::capNhatTienThua);
        view.getTxtSoTienThanhToan().addActionListener(e -> {
            if (view.getBtnXacNhan().isEnabled())
                xacNhanThanhToan();
        });

        view.capNhatPhuongThuc();
        capNhatTienThua();
    }

    private void khiThayDoi(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    // ===================== Nạp dữ liệu =====================
    private void xoaDuLieu() {
        tongTien = daThanhToan = tienDV = tienPT = tienThue = tienGiamGia = BigDecimal.ZERO;
        chiTietHD.clear();
        maNhanVien = maXe = maKhachHang = tenKhachHang = maPhieu = ngayLap = "";
        view.xoaThongTinHoaDon();
    }

    public void taiHoaDon(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            xoaDuLieu();
            capNhatGiaoDien();
            view.thongBaoCanhBao("Vui lòng mở thanh toán từ một hóa đơn đã lưu.");
            return;
        }
        try {
            ThanhToanDAO.ThongTinHoaDon hd = dao.layHoaDon(maHD.trim());
            if (hd == null) {
                maHoaDon = "";
                xoaDuLieu();
                capNhatGiaoDien();
                view.thongBaoCanhBao("Mã hóa đơn " + maHD + " không tồn tại trong cơ sở dữ liệu.");
                return;
            }

            maHoaDon = hd.maHoaDon;
            tongTien = bd(hd.tongTien);
            tienThue = bd(hd.thueVAT);
            tienGiamGia = bd(hd.tienGiamGia);
            maKhachHang = nvl(hd.maKhachHang);
            tenKhachHang = nvl(hd.tenKhachHang);
            maPhieu = nvl(hd.maPhieuSuaChua);
            maNhanVien = nvl(hd.maNhanVien);
            maXe = nvl(hd.maXe);
            ngayLap = FrmThanhToan.dinhDangNgayGio(hd.ngayLap);
            view.hienThiThongTinHoaDon(hd);

            chiTietHD.clear();
            chiTietHD.addAll(dao.layChiTiet(maHoaDon));
            tienDV = BigDecimal.ZERO;
            tienPT = BigDecimal.ZERO;
            for (ThanhToanDAO.ChiTiet ct : chiTietHD) {
                if (ThanhToanDAO.LOAI_DV.equals(ct.loai))
                    tienDV = tienDV.add(ct.thanhTien);
                else
                    tienPT = tienPT.add(ct.thanhTien);
            }

            view.xoaLichSu();
            daThanhToan = BigDecimal.ZERO;
            for (GiaoDichThanhToan gd : dao.layLichSu(maHoaDon)) {
                if (gd.isThanhCong() && gd.getSoTienThanhToan() != null)
                    daThanhToan = daThanhToan.add(gd.getSoTienThanhToan());
                view.themLichSu(gd);
            }
            capNhatGiaoDien();
            view.xoaThongBao();
        } catch (SQLException ex) {
            ex.printStackTrace();
            view.thongBaoLoi("Không thể đọc dữ liệu hóa đơn từ SQL Server.");
            view.baoLoiCSDL(ex.getMessage());
            view.datChoPhep(false);
        }
    }

    // ===================== Cập nhật giao diện theo trạng thái
    // =====================
    private BigDecimal tinhConNo() {
        return tongTien.subtract(daThanhToan).max(BigDecimal.ZERO);
    }

    private void capNhatGiaoDien() {
        BigDecimal conNo = tinhConNo();
        boolean coHoaDon = maHoaDon != null && !maHoaDon.trim().isEmpty() && tongTien.signum() > 0;

        FrmThanhToan.TongQuan q = new FrmThanhToan.TongQuan();
        q.coHoaDon = coHoaDon;
        q.tienDV = tienDV;
        q.tienPT = tienPT;
        // Tổng hóa đơn trước giảm giá = tổng thanh toán đã lưu + tiền giảm.
        q.tongHoaDon = tongTien.add(tienGiamGia);
        q.thue = tienThue;
        q.giamGia = tienGiamGia;
        q.tongThanhToan = tongTien;
        q.daThanhToan = daThanhToan;
        q.conNo = conNo;
        view.hienThiTongQuan(q);

        boolean choPhep = coHoaDon && conNo.signum() > 0;
        view.datChoPhep(choPhep);
        if (choPhep && view.getSoTienThanhToan().trim().isEmpty())
            view.setSoTienThanhToan(conNo.stripTrailingZeros().toPlainString());
        capNhatTienThua();
    }

    private void capNhatTienThua() {
        BigDecimal soTien = docSoTien(view.getSoTienThanhToan());
        BigDecimal khachDua = docSoTien(view.getTienKhachDua());
        if (!view.laTienMat() || khachDua == null || soTien == null)
            view.hienThiTienThua(null);
        else
            view.hienThiTienThua(khachDua.subtract(soTien));
    }

    // ===================== Xác nhận thanh toán =====================
    private void xacNhanThanhToan() {
        BigDecimal soTien = docSoTien(view.getSoTienThanhToan());
        if (maHoaDon == null || maHoaDon.isEmpty() || tongTien.signum() <= 0) {
            view.baoLoi("Chưa có thông tin hóa đơn để thanh toán.");
            return;
        }
        if (soTien == null || soTien.signum() <= 0) {
            view.baoLoi("Vui lòng nhập số tiền khách muốn thanh toán.");
            view.getTxtSoTienThanhToan().requestFocus();
            return;
        }
        BigDecimal conNo = tinhConNo();
        if (soTien.compareTo(conNo) > 0) {
            if (view.hoiDoiVeSoConNo(soTien, conNo)) {
                view.setSoTienThanhToan(conNo.stripTrailingZeros().toPlainString());
                view.focusSoTien();
            }
            return;
        }

        FrmThanhToan.DuLieuHoaDon d = new FrmThanhToan.DuLieuHoaDon();
        d.maHoaDon = maHoaDon;
        d.ngayLap = ngayLap;
        d.tenKhach = tenKhachHang;
        d.maKhach = maKhachHang;
        d.maPhieu = maPhieu;
        d.maXe = maXe;
        d.maNhanVien = maNhanVien;
        d.chiTiet = new ArrayList<>(chiTietHD);
        d.tienDV = tienDV;
        d.tienPT = tienPT;
        d.thue = tienThue;
        d.giamGia = tienGiamGia;
        d.tongHoaDon = tongTien.add(tienGiamGia);
        d.tongTien = tongTien;
        d.daTraTruoc = daThanhToan;
        d.lanNay = soTien;
        d.conLai = tongTien.subtract(daThanhToan).subtract(soTien).max(BigDecimal.ZERO);
        d.ngayIn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        if (view.laTienMat()) {
            BigDecimal khachDua = docSoTien(view.getTienKhachDua());
            if (khachDua == null || khachDua.compareTo(soTien) < 0) {
                view.baoLoi("Tiền khách đưa phải lớn hơn hoặc bằng số tiền thanh toán.");
                view.focusTienKhachDua();
                return;
            }
            d.phuongThuc = GiaoDichThanhToan.PT_TIEN_MAT;
            d.khachDua = khachDua;
            d.tienThua = khachDua.subtract(soTien);
        } else {
            if (!view.hienThiQR(maHoaDon, soTien)) {
                view.thongBaoCanhBao("Đã hủy giao dịch chuyển khoản. Chưa ghi nhận thanh toán.");
                return;
            }
            d.phuongThuc = GiaoDichThanhToan.PT_CHUYEN_KHOAN;
        }

        if (!ghiNhanThanhToan(soTien, d.phuongThuc))
            return;
        view.baoThanhToanThanhCong(soTien, d.phuongThuc);
        if (view.hoiInHoaDon())
            view.hienThiHoaDon(d);
    }

    private boolean ghiNhanThanhToan(BigDecimal soTien, String phuongThuc) {
        try {
            HoaDon hd = new HoaDon();
            hd.setMaHoaDon(maHoaDon);
            GiaoDichThanhToan gd = new GiaoDichThanhToan();
            gd.setHoaDon(hd);
            gd.setPhuongThucThanhToan(phuongThuc);
            gd.setSoTienThanhToan(soTien);
            dao.ghiNhanThanhToan(gd);
            view.setSoTienThanhToan("");
            view.setTienKhachDua("");
            taiHoaDon(maHoaDon);
            return true;
        } catch (SQLException e) {
            view.baoThanhToanThatBai(e.getMessage());
            return false;
        }
    }

    // ===================== Tiện ích =====================
    /**
     * Chuyển chuỗi người dùng nhập (có thể có . , ₫ VND) thành số; null nếu không
     * hợp lệ.
     */
    private BigDecimal docSoTien(String text) {
        if (text == null || text.trim().isEmpty())
            return null;
        try {
            String value = text.trim().replace("₫", "").replace("VND", "")
                    .replace(" ", "").replace(".", "").replace(",", "");
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal bd(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
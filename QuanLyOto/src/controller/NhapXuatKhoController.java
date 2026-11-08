package controller;

import dao.PhieuNhapXuatKhoDAO;
import dao.PhuTungDAO;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhapXuatKho;
import entity.PhuTung;
import gui.FrmNhapXuatPhuTung;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NhapXuatKhoController {
    private final FrmNhapXuatPhuTung view;
    private final PhuTungDAO phuTungDAO = new PhuTungDAO();
    private final PhieuNhapXuatKhoDAO giaoDichDAO = new PhieuNhapXuatKhoDAO();
    private final DecimalFormat df = new DecimalFormat("#,### đ");
    
    private boolean isNhapKho = true;
    private final Map<String, Integer> soLuongCuTheoPhuTung = new HashMap<>();

    public NhapXuatKhoController(FrmNhapXuatPhuTung view) {
        this.view = view;
    }

    public void khoiTao() {
        view.getRadNhapKho().addActionListener(e -> switchMode(true));
        view.getRadXuatKho().addActionListener(e -> switchMode(false));

        view.getCboPhuTung().addActionListener(e -> {
            if (view.getCboPhuTung().getSelectedIndex() <= 0) {
                view.getTxtTonKho().setText(""); view.getTxtDonGia().setText(""); return;
            }
            String ptInfo = view.getCboPhuTung().getSelectedItem().toString();
            String maPT = ptInfo.contains(" - ") ? ptInfo.split(" - ")[0] : ptInfo;
            try {
                PhuTung pt = phuTungDAO.findById(maPT); 
                if (pt != null) {
                    view.getTxtTonKho().setText(String.valueOf(pt.getSoLuongTon()));
                    double gia = isNhapKho ? pt.getGiaNhap() : pt.getGiaBan();
                    view.getTxtDonGia().setText(df.format(gia).replace(" đ", ""));
                } else {
                    view.getTxtTonKho().setText(""); view.getTxtDonGia().setText("");
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        view.getTblChiTiet().getSelectionModel().addListSelectionListener(e -> {
            view.getBtnXoaChiTiet().setEnabled(view.getTblChiTiet().getSelectedRow() != -1);
        });

        view.getBtnXoaChiTiet().addActionListener(e -> {
            int row = view.getTblChiTiet().getSelectedRow();
            if (row != -1) {
                view.getModelChiTiet().removeRow(row);
                capNhatTongTien();
            }
        });

        view.getBtnLamMoi().addActionListener(e -> {
            switchMode(this.isNhapKho);
            taiDanhSachPhieuLichSu();
        });

        view.getBtnThemChiTiet().addActionListener(e -> {
            if (view.getCboPhuTung().getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn phụ tùng!", "Lỗi", JOptionPane.WARNING_MESSAGE); return;
            }
            String ptInfo = view.getCboPhuTung().getSelectedItem().toString();
            String maPT = ptInfo.contains(" - ") ? ptInfo.split(" - ")[0] : ptInfo;
            String tenPT = ptInfo.contains(" - ") ? ptInfo.split(" - ")[1] : ptInfo;
            int soLuong = 0; int tonKho = 0; double donGia = 0;
            
            try {
                if (!view.getTxtTonKho().getText().trim().isEmpty()) {
                    tonKho = Integer.parseInt(view.getTxtTonKho().getText().trim());
                }
            } catch (Exception ex) { tonKho = 0; }
            int tonKhoCoTheXuat = tonKho + soLuongCuTheoPhuTung.getOrDefault(maPT, 0);

            try {
                if (view.getTxtSoLuong().getText().trim().isEmpty()) throw new Exception("Vui lòng nhập số lượng!");
                soLuong = Integer.parseInt(view.getTxtSoLuong().getText().trim());
                if (soLuong <= 0) throw new Exception("Số lượng phải > 0");
                if (!isNhapKho && soLuong > tonKhoCoTheXuat) {
                    throw new Exception("Số lượng xuất (" + soLuong + ") vượt quá tồn kho khả dụng (" + tonKhoCoTheXuat + ")");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }

            try {
                if (view.getTxtDonGia().getText().trim().isEmpty()) throw new Exception("Phụ tùng chưa có giá!");
                donGia = Double.parseDouble(view.getTxtDonGia().getText().trim().replace(" đ", "").replace(",", ""));
                if (donGia <= 0) throw new Exception("Giá không hợp lệ (<= 0).");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }

            boolean isMerged = false;
            for (int i = 0; i < view.getModelChiTiet().getRowCount(); i++) {
                if (view.getModelChiTiet().getValueAt(i, 0).equals(maPT)) {
                    int oldQty = Integer.parseInt(view.getModelChiTiet().getValueAt(i, 2).toString());
                    int newQty = oldQty + soLuong;
                    if (!isNhapKho && newQty > tonKhoCoTheXuat) {
                        JOptionPane.showMessageDialog(view, "Tổng số lượng xuất (" + newQty + ") vượt quá tồn kho khả dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
                    }
                    view.getModelChiTiet().setValueAt(newQty, i, 2);
                    view.getModelChiTiet().setValueAt(df.format(donGia), i, 3);
                    view.getModelChiTiet().setValueAt(df.format(newQty * donGia), i, 4);
                    isMerged = true; break;
                }
            }
            if (!isMerged) {
                view.getModelChiTiet().addRow(new Object[]{ maPT, tenPT, soLuong, df.format(donGia), df.format(soLuong * donGia) });
            }
            capNhatTongTien();
            view.getTxtSoLuong().setText(""); view.getCboPhuTung().requestFocus();
        });

        view.getBtnLuuPhieu().addActionListener(e -> thucHienLuuGiaoDich(this.isNhapKho, false));
        view.getBtnTaoPhieu().addActionListener(e -> thucHienLuuGiaoDich(this.isNhapKho, true));

        view.getBtnXoaPhieu().addActionListener(e -> {
            int row = view.getTblLichSu().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu cần xóa từ bảng bên dưới!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
            }
            String maPhieu = view.getTblLichSu().getValueAt(row, 0).toString();
            if (JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa phiếu " + maPhieu + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (giaoDichDAO.xoaPhieu(maPhieu)) {
                        JOptionPane.showMessageDialog(view, "Xóa phiếu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        view.getBtnLamMoi().doClick();
                    } else {
                        JOptionPane.showMessageDialog(view, "Xóa phiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        DocumentListener searchListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { thucHienLocTimKiem(); }
            @Override public void removeUpdate(DocumentEvent e) { thucHienLocTimKiem(); }
            @Override public void changedUpdate(DocumentEvent e) { thucHienLocTimKiem(); }
        };
        view.getTxtTimMaPhieu().getDocument().addDocumentListener(searchListener);
        view.getTxtTimMaNV().getDocument().addDocumentListener(searchListener);
        view.getTxtTimPhuTung().getDocument().addDocumentListener(searchListener);

        view.getBtnTaiLai().addActionListener(e -> {
            view.getTxtTimMaPhieu().setText(""); view.getTxtTimMaNV().setText(""); view.getTxtTimPhuTung().setText("");
            taiDanhSachPhieuLichSu();
        });

        view.getTblLichSu().getSelectionModel().addListSelectionListener(e -> {
            int row = view.getTblLichSu().getSelectedRow();
            if (row != -1 && !e.getValueIsAdjusting()) {
                String maPhieu = view.getTblLichSu().getValueAt(row, 0).toString();
                String kieu = view.getTblLichSu().getValueAt(row, 3).toString();
                if (kieu != null && kieu.equalsIgnoreCase("Nhập")) {
                    view.getRadNhapKho().setSelected(true); this.isNhapKho = true;
                } else {
                    view.getRadXuatKho().setSelected(true); this.isNhapKho = false;
                }
                view.getTxtMaPhieu().setText(maPhieu);
                capNhatNhanNutLuu();
                String ngayVal = view.getTblLichSu().getValueAt(row, 2).toString();
                view.getTxtNgayLap().setText(ngayVal.contains(" ") ? ngayVal.split(" ")[0] : ngayVal);
                view.getTxtNguoiLap().setText(view.getTblLichSu().getValueAt(row, 1).toString());
                view.getTxtGhiChu().setText(view.getTblLichSu().getValueAt(row, 4) != null ? view.getTblLichSu().getValueAt(row, 4).toString() : "");
                napChiTietHangVaoBangPhai(maPhieu);
            }
        });
    }

    public void switchMode(boolean isNhap) {
        this.isNhapKho = isNhap;
        view.getModelChiTiet().setRowCount(0);
        soLuongCuTheoPhuTung.clear();
        capNhatTongTien();
        view.getTxtSoLuong().setText("");
        view.getTxtDonGia().setText("");
        view.getTxtTonKho().setText("");
        view.getTxtGhiChu().setText("");

        view.getLblDonGia().setText(isNhap ? "Giá nhập (VNĐ)" : "Giá bán niêm yết (VNĐ)");
        napDuLieuChung();
    }

    private void napDuLieuChung() {
        view.getTxtMaPhieu().setText("Tự động tạo khi lưu");
        capNhatNhanNutLuu();
        view.getTxtNgayLap().setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (view.getTaiKhoanHienTai() != null) {
            String tenHienThi = (view.getTaiKhoanHienTai().getHoTenNhanVien() != null && !view.getTaiKhoanHienTai().getHoTenNhanVien().trim().isEmpty()) 
                ? view.getTaiKhoanHienTai().getHoTenNhanVien() : view.getTaiKhoanHienTai().getNhanVien().getMaNV();
            view.getTxtNguoiLap().setText(tenHienThi);
        } else {
            view.getTxtNguoiLap().setText("Chưa đăng nhập");
        }
        view.getCboPhuTung().removeAllItems();
        view.getCboPhuTung().addItem(""); 
        try {
            List<PhuTung> dsPT = phuTungDAO.findAll();
            if (dsPT != null) {
                for (PhuTung pt : dsPT) view.getCboPhuTung().addItem(pt.getMaPhuTung() + " - " + pt.getTenPhuTung());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        taiDanhSachPhieuLichSu();
    }

    private void taiDanhSachPhieuLichSu() {
        view.getModelLichSu().setRowCount(0);
        List<PhieuNhapXuatKho> dsPhieu = giaoDichDAO.layTatCaPhieu(); 
        if (dsPhieu != null && !dsPhieu.isEmpty()) {
            for (PhieuNhapXuatKho p : dsPhieu) {
                double tongTien = giaoDichDAO.tinhTongTienPhieu(p.getMaPhieuNhapXuat());
                String ngayStr = p.getNgayLapPhieu() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(p.getNgayLapPhieu()) : "";
                view.getModelLichSu().addRow(new Object[]{ p.getMaPhieuNhapXuat(), p.getMaNV(), ngayStr, p.getKieuPhieu(), layGhiChuPhieu(p.getMaPhieuNhapXuat()), df.format(tongTien) });
            }
            view.getLblSoLuongPhieu().setText("Đã tải " + dsPhieu.size() + " phiếu giao dịch");
        } else {
            view.getLblSoLuongPhieu().setText("Đã tải 0 phiếu giao dịch");
        }
    }

    private void napChiTietHangVaoBangPhai(String maPhieu) {
        view.getModelChiTiet().setRowCount(0);
        soLuongCuTheoPhuTung.clear();
        List<ChiTietPhieuNhap> dsChiTiet = giaoDichDAO.layChiTietPhieu(maPhieu); 
        if (dsChiTiet != null) {
            for (ChiTietPhieuNhap ct : dsChiTiet) {
                soLuongCuTheoPhuTung.put(ct.getMaPhuTung(), ct.getSoLuong());
                try {
                    PhuTung pt = phuTungDAO.findById(ct.getMaPhuTung());
                    String tenPT = pt != null ? pt.getTenPhuTung() : "Phụ tùng chưa xác định";
                    double tongLine = (ct.getSoLuong() != null ? ct.getSoLuong() : 0) * (ct.getDonGia() != null ? ct.getDonGia() : 0);
                    view.getModelChiTiet().addRow(new Object[]{ ct.getMaPhuTung(), tenPT, ct.getSoLuong(), df.format(ct.getDonGia() != null ? ct.getDonGia() : 0), df.format(tongLine) });
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        capNhatTongTien();
    }

    private void thucHienLocTimKiem() {
        String maP = view.getTxtTimMaPhieu().getText().trim().toLowerCase();
        String maNV = view.getTxtTimMaNV().getText().trim().toLowerCase();
        String phuTungText = view.getTxtTimPhuTung().getText().trim().toLowerCase();
        view.getModelLichSu().setRowCount(0);
        List<PhieuNhapXuatKho> dsPhieu = giaoDichDAO.layTatCaPhieu();
        if (dsPhieu != null) {
            int count = 0;
            for (PhieuNhapXuatKho p : dsPhieu) {
                boolean matchMaP = maP.isEmpty() || p.getMaPhieuNhapXuat().toLowerCase().contains(maP);
                boolean matchMaNV = maNV.isEmpty() || (p.getMaNV() != null && p.getMaNV().toLowerCase().contains(maNV));
                boolean matchPT = phuTungText.isEmpty();
                if (!phuTungText.isEmpty()) {
                    List<ChiTietPhieuNhap> dsChiTiet = giaoDichDAO.layChiTietPhieu(p.getMaPhieuNhapXuat());
                    if (dsChiTiet != null) {
                        for (ChiTietPhieuNhap ct : dsChiTiet) {
                            try {
                                PhuTung pt = phuTungDAO.findById(ct.getMaPhuTung());
                                if (pt != null && pt.getTenPhuTung() != null && pt.getTenPhuTung().toLowerCase().contains(phuTungText)) {
                                    matchPT = true; break;
                                }
                            } catch (SQLException e) { e.printStackTrace(); }
                        }
                    }
                }
                if (matchMaP && matchMaNV && matchPT) {
                    double tongTien = giaoDichDAO.tinhTongTienPhieu(p.getMaPhieuNhapXuat());
                    String ngayStr = p.getNgayLapPhieu() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(p.getNgayLapPhieu()) : "";
                    view.getModelLichSu().addRow(new Object[]{ p.getMaPhieuNhapXuat(), p.getMaNV(), ngayStr, p.getKieuPhieu(), layGhiChuPhieu(p.getMaPhieuNhapXuat()), df.format(tongTien) });
                    count++;
                }
            }
            view.getLblSoLuongPhieu().setText("Tìm thấy " + count + " phiếu giao dịch");
        }
    }

    private void thucHienLuuGiaoDich(boolean isNhap, boolean batBuocTaoMoi) {
        if (view.getTaiKhoanHienTai() == null || view.getTaiKhoanHienTai().getNhanVien() == null || view.getTaiKhoanHienTai().getNhanVien().getMaNV() == null) {
            JOptionPane.showMessageDialog(view, "Không tìm thấy phiên đăng nhập hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
        }
        if (view.getModelChiTiet().getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Phiếu giao dịch chưa có hàng hóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!batBuocTaoMoi && !dangCapNhatPhieu()) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu trong bảng lịch sử cần cập nhật.", "Chưa chọn phiếu", JOptionPane.WARNING_MESSAGE); return;
        }

        boolean dangCapNhat = !batBuocTaoMoi;
        String maPhieu = view.getTxtMaPhieu().getText().trim();
        if (!dangCapNhat) {
            try { maPhieu = giaoDichDAO.taoMaPhieuMoi(isNhap); } 
            catch (SQLException ex) { JOptionPane.showMessageDialog(view, "Lỗi tạo mã: " + ex.getMessage()); return; }
        }

        PhieuNhapXuatKho phieu = new PhieuNhapXuatKho();
        phieu.setMaPhieuNhapXuat(maPhieu);
        phieu.setMaNV(view.getTaiKhoanHienTai().getNhanVien().getMaNV().trim());
        phieu.setNgayLapPhieu(new java.sql.Timestamp(System.currentTimeMillis()));
        phieu.setKieuPhieu(isNhap ? "Nhập" : "Xuất");

        List<ChiTietPhieuNhap> dsChiTiet = new ArrayList<>();
        String ghiChuChung = view.getTxtGhiChu().getText().trim(); 
        for (int i = 0; i < view.getModelChiTiet().getRowCount(); i++) {
            ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
            ct.setMaPhieuNhapXuat(maPhieu);
            ct.setMaPhuTung(view.getModelChiTiet().getValueAt(i, 0).toString());
            ct.setSoLuong(Integer.parseInt(view.getModelChiTiet().getValueAt(i, 2).toString()));
            ct.setDonGia(Double.parseDouble(view.getModelChiTiet().getValueAt(i, 3).toString().replace(" đ", "").replace(",", "")));
            ct.setGhiChu(ghiChuChung); 
            dsChiTiet.add(ct);
        }

        try {
            if (dangCapNhat) giaoDichDAO.capNhatGiaoDichKho(maPhieu, dsChiTiet, isNhap);
            else giaoDichDAO.luuGiaoDichKho(phieu, dsChiTiet, isNhap);
            JOptionPane.showMessageDialog(view, (dangCapNhat ? "Cập nhật" : "Tạo") + " phiếu thành công!\nMã phiếu: " + maPhieu, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            view.getTblLichSu().clearSelection();
            switchMode(isNhap);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Lỗi lưu phiếu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean dangCapNhatPhieu() {
        String maPhieu = view.getTxtMaPhieu().getText().trim();
        return !maPhieu.isEmpty() && !"Tự động tạo khi lưu".equals(maPhieu);
    }

    private void capNhatNhanNutLuu() {
        if (view.getBtnLuuPhieu() != null) view.getBtnLuuPhieu().setText("✎ Cập nhật phiếu");
    }

    private String layGhiChuPhieu(String maPhieu) {
        for (ChiTietPhieuNhap chiTiet : giaoDichDAO.layChiTietPhieu(maPhieu)) {
            if (chiTiet.getGhiChu() != null && !chiTiet.getGhiChu().isBlank()) return chiTiet.getGhiChu();
        }
        return "";
    }

    private void capNhatTongTien() {
        double tongTien = 0;
        for (int i = 0; i < view.getModelChiTiet().getRowCount(); i++) {
            tongTien += Double.parseDouble(view.getModelChiTiet().getValueAt(i, 4).toString().replace(" đ", "").replace(",", ""));
        }
        view.getLblTongTien().setText("Tổng tiền: " + df.format(tongTien));
    }
}
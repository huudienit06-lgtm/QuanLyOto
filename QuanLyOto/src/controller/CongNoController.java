package controller;

import dao.CongNoDao;
import entity.CongNo;
import entity.KhachHang;
import gui.FrmCongNo;
import gui.FrmThanhToan;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class CongNoController {
    private final FrmCongNo frmCongNo;
    private final CongNoDao congNoDao;
    // Cờ chặn tải lại nhiều lần khi đặt lại các ô lọc
    private boolean dangDatLaiBoLoc = false;

    public CongNoController(FrmCongNo frmCongNo, CongNoDao congNoDao) {
        this.frmCongNo = frmCongNo;
        this.congNoDao = congNoDao;
        ganSuKien();
        taiDuLieu();
    }

    // SỰ KIỆN MÀN HÌNH CHÍNH
    private void ganSuKien() {
        JTable tblCongNo = frmCongNo.getTblCongNo();

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

        // Nút tìm kiếm / làm mới
        frmCongNo.getBtnTimKiem().addActionListener(e -> taiDuLieu());
        frmCongNo.getBtnLamMoi().addActionListener(e -> lamMoiBoLoc());

        // Enter trong ô từ khóa hoặc đổi ô lọc -> tải lại danh sách
        frmCongNo.getTxtTimKiem().addActionListener(e -> taiDuLieu());
        JComboBox<?>[] dsOLoc = { frmCongNo.getCmbNhomKH(), frmCongNo.getCmbTrangThai(),
                frmCongNo.getCmbThoiGian() };
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
        frmCongNo.datLaiBoLoc();
        dangDatLaiBoLoc = false;
        taiDuLieu();
    }

    public void taiDuLieu() {
        List<CongNo> dsCongNo = new ArrayList<>();
        try {
            dsCongNo = congNoDao.layDanhSachCongNo(frmCongNo.getTuKhoa(), frmCongNo.getNhomNo(),
                    frmCongNo.getTrangThai(), frmCongNo.getThoiGian());
        } catch (SQLException ex) {
            FrmCongNo.baoLoiCSDL(frmCongNo, "Không tải được danh sách công nợ", ex);
        }

        // Thống kê nhanh (chỉ tính khách còn nợ)
        BigDecimal tongNo = BigDecimal.ZERO;
        int soKHNo = 0;
        int soHDNo = 0;
        for (CongNo cn : dsCongNo) {
            if (cn.getTongNo().signum() > 0) {
                tongNo = tongNo.add(cn.getTongNo());
                soKHNo++;
                soHDNo += cn.getSoHDNo();
            }
        }

        frmCongNo.hienThiDanhSach(dsCongNo);
        frmCongNo.hienThiThongKe(tongNo, soKHNo, soHDNo, dsCongNo.size());
    }

    // KHUNG CHI TIẾT CÔNG NỢ
    private void moChiTiet(int dongHienThi) {
        String maKH = frmCongNo.layMaKH(dongHienThi);
        FrmCongNo.DlgChiTietCongNo dlgChiTiet = new FrmCongNo.DlgChiTietCongNo(frmCongNo);

        // Nạp dữ liệu (không tìm thấy khách hàng thì vẫn dùng đối tượng chỉ có mã)
        KhachHang khachHang = new KhachHang(maKH);
        try {
            KhachHang khTim = congNoDao.layKhachHang(maKH);
            if (khTim != null) {
                khachHang = khTim;
            }
            dlgChiTiet.hienThiKhachHang(khachHang);
        } catch (SQLException ex) {
            FrmCongNo.baoLoiCSDL(dlgChiTiet, "Không tải được thông tin khách hàng", ex);
        }
        try {
            dlgChiTiet.hienThiHoaDonNo(congNoDao.layHoaDonNo(khachHang));
        } catch (SQLException ex) {
            FrmCongNo.baoLoiCSDL(dlgChiTiet, "Không tải được hóa đơn còn nợ", ex);
        }

        ganSuKienChiTiet(dlgChiTiet);
        dlgChiTiet.setVisible(true);
    }

    private void ganSuKienChiTiet(FrmCongNo.DlgChiTietCongNo dlgChiTiet) {
        JTable tblHoaDon = dlgChiTiet.getTblHoaDon();

        // Đổi lựa chọn -> cập nhật số tiền đã chọn
        tblHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                dlgChiTiet.capNhatLuaChon();
            }
        });

        // Bấm đúp một hóa đơn -> thanh toán
        tblHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblHoaDon.rowAtPoint(e.getPoint()) >= 0) {
                    thanhToan(dlgChiTiet);
                }
            }
        });

        // Ô tích "Chọn tất cả" -> chọn / bỏ chọn mọi dòng
        dlgChiTiet.getChkTatCa().addActionListener(e -> {
            if (dlgChiTiet.isDangDongBo()) {
                return;
            }
            if (dlgChiTiet.getChkTatCa().isSelected()) {
                tblHoaDon.selectAll();
            } else {
                tblHoaDon.clearSelection();
            }
        });

        dlgChiTiet.getBtnThanhToan().addActionListener(e -> thanhToan(dlgChiTiet));
        dlgChiTiet.getBtnDong().addActionListener(e -> dlgChiTiet.dispose());
    }

    // Thanh toán
    private void thanhToan(FrmCongNo.DlgChiTietCongNo dlgChiTiet) {
        JTable tblHoaDon = dlgChiTiet.getTblHoaDon();
        int[] dsDong = tblHoaDon.getSelectedRows();
        if (dsDong.length == 0) {
            JOptionPane.showMessageDialog(dlgChiTiet, "Vui lòng chọn hóa đơn cần thanh toán.");
            return;
        }
        // Chỉ chuyển sang FrmThanhToan khi chọn đúng 1 hóa đơn
        if (dsDong.length > 1) {
            JOptionPane.showMessageDialog(dlgChiTiet, "Vui lòng chỉ chọn một hóa đơn để thanh toán.");
            return;
        }

        int dong = tblHoaDon.convertRowIndexToModel(dsDong[0]);
        String maHD = String.valueOf(dlgChiTiet.getModelHoaDon().getValueAt(dong, 0));

        // Đóng khung chi tiết, mở màn hình thanh toán; đóng xong thì tải lại danh sách
        // công nợ
        dlgChiTiet.dispose();
        FrmThanhToan winThanhToan = new FrmThanhToan(maHD);
        winThanhToan.setLocationRelativeTo(frmCongNo);
        winThanhToan.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                taiDuLieu();
            }
        });
        winThanhToan.setVisible(true);
    }
}
package controller;

import dao.CaLamViecDAO;
import dao.LichLamViecDAO;
import dao.LichLamViecDAO.PhanCong;
import dao.NhanVienDAO;
import entity.CaLamViec;
import entity.LichLamViec;
import entity.NhanVien;
import gui.FrmLichLamViec;
import gui.FrmLichLamViec.DatePickerPanel;
import java.awt.event.MouseWheelEvent;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import util.MaTuDong;

public final class LichLamViecController {
    private static final String CHUA_PHAN_CONG = "Chưa phân công";
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");
    private final FrmLichLamViec view;
    private final CaLamViecDAO caLamViecDAO = new CaLamViecDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final LichLamViecDAO lichLamViecDAO = new LichLamViecDAO();
    private final Timer timerCapNhatThoiGian;
    private List<CaLamViec> danhSachCa = List.of();
    private List<PhanCong> phanCongTuan = List.of();
    private LocalDate ngayDauTuan = LocalDate.now().with(DayOfWeek.MONDAY);
    private String maLichDangChon;
    private boolean dangNapDiemDanh;
    private int caHienTai = -1;

    public LichLamViecController(FrmLichLamViec view) {
        this.view = view;
        timerCapNhatThoiGian = new Timer(30_000, event -> capNhatCaHienTai());
    }

    public void khoiTao() {
        ganSuKien();
        taiDanhMuc();
        taiPhanCongTuan();
        capNhatCaHienTai();
    }

    public void khiViewDuocHienThi() {
        capNhatCaHienTai();
        timerCapNhatThoiGian.start();
    }

    public void khiViewBiAn() {
        timerCapNhatThoiGian.stop();
    }

    private void ganSuKien() {
        view.getBtnThem().addActionListener(event -> themPhanCong());
        view.getBtnSua().addActionListener(event -> capNhatPhanCong());
        view.getBtnXoa().addActionListener(event -> xoaPhanCong());
        view.getBtnLamMoi().addActionListener(event -> lamMoiPhanCong());
        view.getBtnTuanTruoc().addActionListener(event -> dichTuan(-1));
        view.getBtnTuanKeTiep().addActionListener(event -> dichTuan(1));

        JTable tblLich = view.getTblLichTuan();
        tblLich.addMouseWheelListener(event -> chuyenCuonRaTrangNeuCan(tblLich, event));
        tblLich.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                chonOTrongLich();
            }
        });
        tblLich.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    chonPhanCongDeSua();
                }
            }
        });

        JTable tblDiemDanh = view.getTblDiemDanh();
        tblDiemDanh.addMouseWheelListener(event -> chuyenCuonRaTrangNeuCan(tblDiemDanh, event));
        view.getModelDiemDanh().addTableModelListener(event -> {
            if (!dangNapDiemDanh && event.getType() == TableModelEvent.UPDATE
                    && event.getColumn() == 2) {
                capNhatDiemDanh(event.getFirstRow());
            }
        });

        DatePickerPanel pnlChonNgay = view.getPnlChonNgayLamViec();
        pnlChonNgay.getBtnMoLich().addActionListener(event -> pnlChonNgay.hienThiLich());
        pnlChonNgay.getBtnThangTruoc().addActionListener(event -> pnlChonNgay.capNhatThang(-1));
        pnlChonNgay.getBtnThangSau().addActionListener(event -> pnlChonNgay.capNhatThang(1));
        for (JButton btnNgay : pnlChonNgay.getBtnNgay()) {
            btnNgay.addActionListener(event -> chonNgayLamViec(pnlChonNgay.getNgay(btnNgay)));
        }
    }

    private void taiDanhMuc() {
        try {
            danhSachCa = caLamViecDAO.findAll();
            List<NhanVien> danhSachNhanVien = nhanVienDAO.findAll();
            List<String> tenNhanVien = danhSachNhanVien.stream()
                    .map(nhanVien -> nhanVien.getMaNV() + " - " + nhanVien.getHoTen())
                    .toList();
            List<String> tenCa = danhSachCa.stream()
                    .map(ca -> ca.getMaCa() + " (" + ca.getGioBatDau().toLocalTime().format(DINH_DANG_GIO)
                            + " - " + ca.getGioKetThuc().toLocalTime().format(DINH_DANG_GIO) + ")")
                    .toList();
            view.hienThiLuaChon(tenNhanVien, tenCa);
        } catch (SQLException exception) {
            view.hienThiLoiCSDL("Không thể tải danh sách ca làm việc và nhân viên.", exception);
        }
    }

    private void taiPhanCongTuan() {
        try {
            phanCongTuan = lichLamViecDAO.layPhanCong(ngayDauTuan, ngayDauTuan.plusDays(7));
            hienThiLichTuan();
        } catch (SQLException exception) {
            phanCongTuan = List.of();
            view.setTrangThai("Không thể tải lịch phân công.");
            view.hienThiLoiCSDL("Không thể tải lịch làm việc.", exception);
            hienThiLichTuan();
        }
        napDiemDanh();
    }

    private void hienThiLichTuan() {
        List<Object[]> cacDong = new ArrayList<>();
        for (CaLamViec ca : danhSachCa) {
            String khungGio = ca.getGioBatDau().toLocalTime().format(DINH_DANG_GIO)
                    + " - " + ca.getGioKetThuc().toLocalTime().format(DINH_DANG_GIO);
            Object[] dong = new Object[8];
            dong[0] = ca.getMaCa() + "\n" + khungGio;
            for (int ngay = 0; ngay < 7; ngay++) {
                LocalDate ngayTrongTuan = ngayDauTuan.plusDays(ngay);
                List<String> tenNhanVien = phanCongTuan.stream()
                        .filter(phanCong -> phanCong.maCa().equals(ca.getMaCa())
                                && phanCong.ngayLamViec().equals(ngayTrongTuan))
                        .map(phanCong -> phanCong.hoTen()
                                + ("Có mặt".equals(phanCong.trangThai()) ? " (Có mặt)"
                                        : "Vắng mặt".equals(phanCong.trangThai()) ? " (Vắng)" : ""))
                        .toList();
                dong[ngay + 1] = tenNhanVien.isEmpty()
                        ? CHUA_PHAN_CONG : String.join("\n", tenNhanVien);
            }
            cacDong.add(dong);
        }
        view.hienThiLichTuan(cacDong.toArray(Object[][]::new));
    }

    private void napDiemDanh() {
        dangNapDiemDanh = true;
        List<Object[]> cacDong = new ArrayList<>();
        LocalDate homNay = LocalDate.now();
        if (caHienTai >= 0 && caHienTai < danhSachCa.size()) {
            String maCa = danhSachCa.get(caHienTai).getMaCa();
            for (PhanCong phanCong : phanCongTuan) {
                if (phanCong.maCa().equals(maCa) && phanCong.ngayLamViec().equals(homNay)) {
                    cacDong.add(new Object[] {
                            phanCong.maNV(), phanCong.hoTen(),
                            "Có mặt".equals(phanCong.trangThai())
                    });
                }
            }
        }
        String thongBao = cacDong.isEmpty()
                ? (caHienTai >= 0 ? "Chưa có dữ liệu phân công cho ca hiện tại" : "Không có ca đang diễn ra")
                : "Đánh dấu trạng thái có mặt của nhân viên trong ca";
        view.hienThiDiemDanh(cacDong.toArray(Object[][]::new), thongBao);
        dangNapDiemDanh = false;
    }

    private void capNhatDiemDanh(int row) {
        if (row < 0 || row >= view.getModelDiemDanh().getRowCount()
                || caHienTai < 0 || caHienTai >= danhSachCa.size()) {
            return;
        }
        String maNV = view.getModelDiemDanh().getValueAt(row, 0).toString();
        LocalDate homNay = LocalDate.now();
        String maCa = danhSachCa.get(caHienTai).getMaCa();
        PhanCong phanCong = phanCongTuan.stream()
                .filter(item -> item.maNV().equals(maNV) && item.maCa().equals(maCa)
                        && item.ngayLamViec().equals(homNay))
                .findFirst().orElse(null);
        if (phanCong == null) {
            return;
        }
        try {
            lichLamViecDAO.capNhatTrangThai(phanCong.maLich(),
                    Boolean.TRUE.equals(view.getModelDiemDanh().getValueAt(row, 2)));
            taiPhanCongTuan();
        } catch (SQLException exception) {
            view.hienThiLoiCSDL("Không thể lưu trạng thái điểm danh.", exception);
            napDiemDanh();
        }
    }

    private void themPhanCong() {
        String maCa = layMaCaDuocChon();
        String maNV = view.getMaNhanVienDuocChon();
        if (ngayLamViecTrongQuaKhu()) {
            hienThiCanhBaoNgayQuaKhu();
            return;
        }
        if (maCa == null || maNV == null) {
            view.thongBao("Vui lòng chọn nhân viên và ca làm việc.",
                    "Thiếu thông tin", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate ngay = view.getNgayDuocChon();
            if (lichLamViecDAO.daTrungPhanCong(maCa, maNV, ngay, null)) {
                view.thongBao("Nhân viên này đã được xếp vào ca đã chọn trong ngày này.",
                        "Phân công bị trùng", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maLich = MaTuDong.sinhMa(MaTuDong.TIEN_TO_LICH_LAM_VIEC,
                    lichLamViecDAO.findAll(), LichLamViec::getMaLich);
            if (lichLamViecDAO.themPhanCong(maLich, maCa, maNV, ngay)) {
                taiPhanCongTuan();
                view.setTrangThai("Đã thêm phân công.");
            }
        } catch (SQLException exception) {
            view.hienThiLoiCSDL("Không thể thêm phân công.", exception);
        }
    }

    private void capNhatPhanCong() {
        String maCa = layMaCaDuocChon();
        String maNV = view.getMaNhanVienDuocChon();
        if (ngayLamViecTrongQuaKhu()) {
            hienThiCanhBaoNgayQuaKhu();
            return;
        }
        if (maLichDangChon == null || maCa == null || maNV == null) {
            view.thongBao("Chọn một phân công trên lịch và kiểm tra thông tin cần sửa.",
                    "Chưa chọn phân công", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate ngay = view.getNgayDuocChon();
            if (lichLamViecDAO.daTrungPhanCong(maCa, maNV, ngay, maLichDangChon)) {
                view.thongBao("Nhân viên này đã được xếp vào ca đã chọn trong ngày này.",
                        "Phân công bị trùng", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (lichLamViecDAO.capNhatPhanCong(maLichDangChon, maCa, maNV, ngay)) {
                taiPhanCongTuan();
                view.setTrangThai("Đã cập nhật phân công.");
            }
        } catch (SQLException exception) {
            view.hienThiLoiCSDL("Không thể cập nhật phân công.", exception);
        }
    }

    private void xoaPhanCong() {
        if (ngayLamViecTrongQuaKhu()) {
            hienThiCanhBaoNgayQuaKhu();
            return;
        }
        if (maLichDangChon == null) {
            view.thongBao("Chọn một phân công trên lịch trước khi xóa.",
                    "Chưa chọn phân công", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!view.xacNhanXoa()) {
            return;
        }
        try {
            if (lichLamViecDAO.xoaPhanCong(maLichDangChon)) {
                lamMoiPhanCong();
                view.setTrangThai("Đã xóa phân công.");
            }
        } catch (SQLException exception) {
            view.hienThiLoiCSDL("Không thể xóa phân công.", exception);
        }
    }

    private void lamMoiPhanCong() {
        maLichDangChon = null;
        view.datLaiLuaChon();
        capNhatTrangThaiNutPhanCong();
        taiPhanCongTuan();
        capNhatCaHienTai();
    }

    private void chonOTrongLich() {
        int row = view.getTblLichTuan().getSelectedRow();
        int column = view.getTblLichTuan().getSelectedColumn();
        if (row < 0 || column < 1 || column > 7 || row >= danhSachCa.size()) {
            return;
        }
        LocalDate ngay = ngayDauTuan.plusDays(column - 1L);
        maLichDangChon = null;
        view.datLaiLuaChon();
        view.getPnlChonNgayLamViec().setSelectedDate(ngay);
        view.setSelectedShiftIndex(row);
        view.capNhatTrangThaiNut(!ngay.isBefore(LocalDate.now()), false);
    }

    private void chonPhanCongDeSua() {
        int row = view.getTblLichTuan().getSelectedRow();
        int column = view.getTblLichTuan().getSelectedColumn();
        if (row < 0 || column < 1 || column > 7 || row >= danhSachCa.size()) {
            return;
        }
        LocalDate ngay = ngayDauTuan.plusDays(column - 1L);
        if (ngay.isBefore(LocalDate.now())) {
            return;
        }
        String maCa = danhSachCa.get(row).getMaCa();
        List<PhanCong> cacPhanCong = phanCongTuan.stream()
                .filter(phanCong -> phanCong.maCa().equals(maCa)
                        && phanCong.ngayLamViec().equals(ngay))
                .toList();
        if (cacPhanCong.isEmpty()) {
            return;
        }
        int index = 0;
        if (cacPhanCong.size() > 1) {
            List<String> cacLuaChon = cacPhanCong.stream()
                    .map(phanCong -> phanCong.maNV() + " - " + phanCong.hoTen())
                    .toList();
            index = view.chonPhanCong(cacLuaChon);
            if (index < 0 || index >= cacPhanCong.size()) {
                return;
            }
        }
        PhanCong selected = cacPhanCong.get(index);
        maLichDangChon = selected.maLich();
        view.getPnlChonNgayLamViec().setSelectedDate(ngay);
        view.setSelectedShiftIndex(row);
        view.chonNhanVien(selected.maNV());
        capNhatTrangThaiNutPhanCong();
    }

    private void chonNgayLamViec(LocalDate ngayChon) {
        view.getPnlChonNgayLamViec().getPopLich().setVisible(false);
        LocalDate dauTuanChon = ngayChon.with(DayOfWeek.MONDAY);
        if (!dauTuanChon.equals(ngayDauTuan)) {
            ngayDauTuan = dauTuanChon;
            capNhatTuan(ngayChon);
        }
        capNhatTrangThaiNutPhanCong();
    }

    private void dichTuan(int soTuan) {
        int ngayTrongTuan = view.getNgayDuocChon().getDayOfWeek().getValue() - 1;
        ngayDauTuan = ngayDauTuan.plusWeeks(soTuan);
        capNhatTuan(ngayDauTuan.plusDays(ngayTrongTuan));
    }

    private void capNhatTuan(LocalDate ngayDuocChon) {
        view.capNhatHienThiTuan(ngayDauTuan, ngayDuocChon);
        taiPhanCongTuan();
        capNhatCaHienTai();
        view.getTblLichTuan().repaint();
        capNhatTrangThaiNutPhanCong();
    }

    private void capNhatCaHienTai() {
        LocalTime gioHienTai = LocalTime.now();
        LocalDate homNay = LocalDate.now();
        caHienTai = timCaHienTai(gioHienTai);
        int cotNgayHienTai = homNay.with(DayOfWeek.MONDAY).equals(ngayDauTuan)
                ? homNay.getDayOfWeek().getValue() : -1;
        String thongTin = "Hôm nay " + homNay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + "  |  " + gioHienTai.format(DINH_DANG_GIO) + "  |  ";
        boolean dangTrongCa = caHienTai >= 0;
        if (dangTrongCa) {
            thongTin += "ĐANG DIỄN RA: " + danhSachCa.get(caHienTai).getMaCa();
        } else {
            thongTin += "Hiện không nằm trong khung giờ ca làm";
        }
        view.capNhatCaHienTai(thongTin, dangTrongCa, caHienTai, cotNgayHienTai);
        napDiemDanh();
    }

    private void capNhatTrangThaiNutPhanCong() {
        view.capNhatTrangThaiNut(!ngayLamViecTrongQuaKhu(), maLichDangChon != null);
    }

    private boolean ngayLamViecTrongQuaKhu() {
        return view.getNgayDuocChon().isBefore(LocalDate.now());
    }

    private void hienThiCanhBaoNgayQuaKhu() {
        view.thongBao("Không thể thêm, sửa hoặc xóa phân công của ngày đã qua.",
                "Không thể cập nhật", javax.swing.JOptionPane.WARNING_MESSAGE);
    }

    private String layMaCaDuocChon() {
        int selectedIndex = view.getChiSoCaDuocChon();
        return selectedIndex >= 0 && selectedIndex < danhSachCa.size()
                ? danhSachCa.get(selectedIndex).getMaCa() : null;
    }

    private int timCaHienTai(LocalTime gioHienTai) {
        for (int index = 0; index < danhSachCa.size(); index++) {
            LocalTime gioBatDau = danhSachCa.get(index).getGioBatDau().toLocalTime();
            LocalTime gioKetThuc = danhSachCa.get(index).getGioKetThuc().toLocalTime();
            if (!gioHienTai.isBefore(gioBatDau) && gioHienTai.isBefore(gioKetThuc)) {
                return index;
            }
        }
        return -1;
    }

    private void chuyenCuonRaTrangNeuCan(JTable tblNguon, MouseWheelEvent event) {
        if (event.isConsumed() || view.getScrNoiDung() == null) {
            return;
        }
        JScrollPane scrBang = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tblNguon);
        if (scrBang != null && coTheCuon(scrBang.getVerticalScrollBar(), event.getWheelRotation())) {
            return;
        }
        JScrollBar scrDocTrang = view.getScrNoiDung().getVerticalScrollBar();
        int huong = Integer.signum(event.getWheelRotation());
        if (!coTheCuon(scrDocTrang, huong)) {
            return;
        }
        int buocCuon = scrDocTrang.getUnitIncrement(huong);
        int delta = event.getUnitsToScroll() * buocCuon;
        scrDocTrang.setValue(scrDocTrang.getValue() + delta);
        event.consume();
    }

    private boolean coTheCuon(JScrollBar scrBar, int huong) {
        if (huong > 0) {
            return scrBar.getValue() + scrBar.getVisibleAmount() < scrBar.getMaximum();
        }
        if (huong < 0) {
            return scrBar.getValue() > scrBar.getMinimum();
        }
        return false;
    }
}

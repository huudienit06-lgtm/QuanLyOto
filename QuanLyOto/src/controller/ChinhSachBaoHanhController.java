package controller;

import dao.ChinhSachBaoHanhDAO;
import entity.ChinhSachBaoHanh;
import entity.PhuTung;
import gui.FrmChinhSachBaoHanh;
import java.sql.SQLException;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;

public final class ChinhSachBaoHanhController {
    private final FrmChinhSachBaoHanh view;
    private final ChinhSachBaoHanhDAO chinhSachBaoHanhDAO = new ChinhSachBaoHanhDAO();

    public ChinhSachBaoHanhController(FrmChinhSachBaoHanh view) {
        this.view = view;
        dangKySuKien();
    }

    public void taiDuLieuBanDau() {
        try {
            view.hienThiDanhSachPhuTung(chinhSachBaoHanhDAO.layDanhSachPhuTung());
            view.hienThiMaChinhSach(chinhSachBaoHanhDAO.layMaTiepTheo());
            taiDanhSachChinhSach();
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải dữ liệu chính sách bảo hành", exception.getMessage());
            view.hienThiMaChinhSach("");
        }
    }

    private void dangKySuKien() {
        view.layNutThem().addActionListener(event -> luuChinhSach(true));
        view.layNutCapNhat().addActionListener(event -> luuChinhSach(false));
        view.layNutXoa().addActionListener(event -> xoaChinhSach());
        view.layNutLamMoi().addActionListener(event -> lamMoiForm());
        view.layNutTimKiem().addActionListener(event -> taiDanhSachChinhSach());
        view.layOTimChinhSach().addActionListener(event -> taiDanhSachChinhSach());
        view.layBangChinhSach().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && view.layMaChinhSachDangChon() != null) {
                taiChinhSachDangChon();
            }
        });
        view.layOTimPhuTung().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                view.locDanhSachPhuTung();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                view.locDanhSachPhuTung();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                view.locDanhSachPhuTung();
            }
        });
        view.layBangPhuTung().getModel().addTableModelListener(event -> {
            if (event.getType() == TableModelEvent.UPDATE && event.getColumn() == 0
                    && event.getFirstRow() >= 0) {
                int row = event.getFirstRow();
                view.capNhatPhuTungDuocChon(row);
            }
        });
    }

    private void taiDanhSachChinhSach() {
        try {
            List<ChinhSachBaoHanh> dsChinhSach = chinhSachBaoHanhDAO.timKiem(view.layTuKhoaChinhSach());
            view.hienThiDanhSachChinhSach(dsChinhSach);
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải danh sách chính sách bảo hành", exception.getMessage());
        }
    }

    private void taiChinhSachDangChon() {
        String maCSBH = view.layMaChinhSachDangChon();
        try {
            for (ChinhSachBaoHanh chinhSach : chinhSachBaoHanhDAO.timKiem(maCSBH)) {
                if (maCSBH.equals(chinhSach.getMaCSBH())) {
                    view.hienThiChinhSach(chinhSach);
                    return;
                }
            }
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải thông tin chính sách", exception.getMessage());
        }
    }

    private void luuChinhSach(boolean themMoi) {
        if (!themMoi && view.layMaChinhSachNhap().isEmpty()) {
            view.thongBaoCanhBao("Thiếu thông tin", "Vui lòng chọn chính sách cần cập nhật.");
            return;
        }
        ChinhSachBaoHanh chinhSach = view.layChinhSachNhap();
        if (chinhSach == null) {
            return;
        }
        try {
            if (themMoi && chinhSachBaoHanhDAO.timKiem("").stream()
                    .anyMatch(existing -> existing.getMaCSBH().equals(chinhSach.getMaCSBH()))) {
                view.thongBaoCanhBao("Mã bị trùng", "Mã chính sách bảo hành đã tồn tại.");
                return;
            }
            chinhSachBaoHanhDAO.luu(chinhSach, themMoi);
            view.datTrangThaiThemMoi(false);
            view.xoaTuKhoaChinhSach();
            taiDanhSachChinhSach();
            view.chonChinhSach(chinhSach.getMaCSBH());
            view.thongBaoThanhCong(
                    (themMoi ? "Đã thêm" : "Đã cập nhật") + " chính sách " + chinhSach.getMaCSBH() + ".");
        } catch (SQLException exception) {
            view.baoLoi(themMoi ? "Không thể thêm chính sách bảo hành" : "Không thể cập nhật chính sách bảo hành",
                    exception.getMessage());
        }
    }

    private void xoaChinhSach() {
        String maCSBH = view.layMaChinhSachNhap();
        if (view.laThemMoi() || maCSBH.isEmpty()) {
            view.thongBaoCanhBao("Thiếu thông tin", "Vui lòng chọn chính sách cần xóa.");
            return;
        }
        if (!view.xacNhanXoa(maCSBH)) {
            return;
        }
        try {
            if (!chinhSachBaoHanhDAO.xoa(maCSBH)) {
                throw new SQLException("Không tìm thấy chính sách cần xóa: " + maCSBH);
            }
            lamMoiForm();
            taiDanhSachChinhSach();
        } catch (SQLException exception) {
            view.baoLoi("Không thể xóa chính sách bảo hành", exception.getMessage());
        }
    }

    private void lamMoiForm() {
        try {
            view.lamMoiForm(chinhSachBaoHanhDAO.layMaTiepTheo());
        } catch (SQLException exception) {
            view.baoLoi("Không thể tạo mã chính sách bảo hành", exception.getMessage());
            view.lamMoiForm("");
        }
    }
}

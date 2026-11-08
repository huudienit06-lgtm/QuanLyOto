package controller;

import dao.CaLamViecDAO;
import entity.CaLamViec;
import gui.FrmCaLamViec;
import java.sql.SQLException;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import util.MaTuDong;

public final class CaLamViecController {
    private final FrmCaLamViec view;
    private final CaLamViecDAO caLamViecDAO;
    private List<CaLamViec> danhSachCa = List.of();

    public CaLamViecController(FrmCaLamViec view) {
        this(view, new CaLamViecDAO());
    }

    CaLamViecController(FrmCaLamViec view, CaLamViecDAO caLamViecDAO) {
        this.view = view;
        this.caLamViecDAO = caLamViecDAO;
    }

    public void khoiTao() {
        view.getBtnThem().addActionListener(event -> themCaLamViec());
        view.getBtnCapNhat().addActionListener(event -> capNhatCaLamViec());
        view.getBtnXoa().addActionListener(event -> xoaCaLamViec());
        view.getBtnLamMoi().addActionListener(event -> view.lamMoiForm(phatSinhMaCa()));
        view.getBtnTaiLai().addActionListener(event -> {
            view.getTxtTimKiem().setText("");
            taiDanhSach();
        });
        view.getTblCaLamViec().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                view.hienThiCaDuocChon();
            }
        });
        view.getTxtTimKiem().getDocument().addDocumentListener(taoBoLangNgheTimKiem());
        taiDanhSach();
    }

    private void taiDanhSach() {
        try {
            danhSachCa = caLamViecDAO.findAll();
            view.hienThiDanhSach(danhSachCa);
            view.lamMoiForm(phatSinhMaCa());
            view.thongBao("Đã tải " + view.getTblCaLamViec().getRowCount() + " ca làm việc.");
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể tải danh sách ca làm việc: " + exception.getMessage());
        }
    }

    private void themCaLamViec() {
        try {
            CaLamViec caLamViec = view.layCaLamViecNhap();
            if (caLamViecDAO.insert(caLamViec)) {
                taiDanhSach();
                view.thongBao("Đã thêm ca " + caLamViec.getMaCa() + ".");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể thêm ca làm việc: " + exception.getMessage());
        }
    }

    private void capNhatCaLamViec() {
        try {
            CaLamViec caLamViec = view.layCaLamViecNhap();
            if (caLamViecDAO.update(caLamViec)) {
                taiDanhSach();
                view.thongBao("Đã cập nhật ca " + caLamViec.getMaCa() + ".");
            } else {
                view.hienThiLoi("Không tìm thấy ca làm việc cần cập nhật.");
            }
        } catch (IllegalArgumentException exception) {
            view.hienThiLoi(exception.getMessage());
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể cập nhật ca làm việc: " + exception.getMessage());
        }
    }

    private void xoaCaLamViec() {
        String maCa = view.getMaCaDangChon();
        if (maCa == null) {
            view.hienThiLoi("Hãy chọn ca làm việc cần xóa.");
            return;
        }
        try {
            if (caLamViecDAO.daDuocPhanCong(maCa)) {
                view.hienThiLoi("Không thể xóa ca đang được sử dụng trong lịch phân công.");
                return;
            }
            if (!view.xacNhanXoa(maCa)) {
                return;
            }
            if (caLamViecDAO.deleteById(maCa)) {
                taiDanhSach();
                view.thongBao("Đã xóa ca " + maCa + ".");
            } else {
                view.hienThiLoi("Không tìm thấy ca làm việc cần xóa.");
            }
        } catch (SQLException exception) {
            view.hienThiLoi("Không thể xóa ca làm việc: " + exception.getMessage());
        }
    }

    private DocumentListener taoBoLangNgheTimKiem() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                view.locDanhSach();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                view.locDanhSach();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                view.locDanhSach();
            }
        };
    }

    private String phatSinhMaCa() {
        return MaTuDong.sinhMa(
                MaTuDong.TIEN_TO_CA_LAM_VIEC, danhSachCa, CaLamViec::getMaCa);
    }
}
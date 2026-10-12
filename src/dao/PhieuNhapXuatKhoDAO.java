package dao;

import entity.PhieuNhapXuatKho;

public class PhieuNhapXuatKhoDAO extends BaseDAO<PhieuNhapXuatKho> {
    public PhieuNhapXuatKhoDAO() {
        super(PhieuNhapXuatKho.class, "PhieuNhapXuatKho", "maPhieuNhapXuat");
    }
}

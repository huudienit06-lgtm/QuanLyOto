package dao;

import entity.PhieuKiemTra;

public class PhieuKiemTraDAO extends BaseDAO<PhieuKiemTra> {
    public PhieuKiemTraDAO() {
        super(PhieuKiemTra.class, "PhieuKiemTra", "maPhieuKiemTra");
    }
}

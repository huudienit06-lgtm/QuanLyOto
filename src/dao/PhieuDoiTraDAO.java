package dao;

import entity.PhieuDoiTra;

public class PhieuDoiTraDAO extends BaseDAO<PhieuDoiTra> {
    public PhieuDoiTraDAO() {
        super(PhieuDoiTra.class, "PhieuDoiTra", "maPhieuDoiTra");
    }
}
